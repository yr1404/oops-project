package com.gov.landportal;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.cloud.FirestoreClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import login.officer.Application;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FirebaseInitializer {

    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            try {
                // Initialize Firebase with your service account credentials
                FileInputStream serviceAccount = new FileInputStream("serviceAccountKey.json"); // Update path
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                // Initialize FirebaseApp
                FirebaseApp.initializeApp(options);
                initialized = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Firestore getDB() {
        return FirestoreClient.getFirestore();
    }

    // Register a new user
    public static void registerUser(String email, String password, String fullName, String role) {
        try {
            if (!initialized) {
                init();  // Ensure initialization
            }

            // Create user record for Firebase Auth
            CreateRequest createRequest = new CreateRequest()
                    .setEmail(email)
                    .setPassword(password)
                    .setDisplayName(fullName);  // Display name is now fullName

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);

            // Store user details (fullName, role) in Firestore
            Firestore db = getDB();
            DocumentReference userRef = db.collection("users").document(userRecord.getUid());

            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("fullName", fullName);
            userDetails.put("role", role);
            userDetails.put("email", email);

            // Save user details in Firestore
            userRef.set(userDetails);

            // Set custom claims (user role) for the user after creation
            FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), Map.of("role", role));

            System.out.println("User registered successfully with role: " + role);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create a new application for a user
    public static void createApplication(String uid, String plotId, String requestType, String description) {
        Firestore db = getDB();
        DocumentReference docRef = db.collection("applications").document(); // auto ID

        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);
        data.put("plotId", plotId);
        data.put("requestType", requestType);
        data.put("description", description);
        data.put("status", "Pending");
        data.put("timestamp", FieldValue.serverTimestamp());
        data.put("title", requestType + " for Plot " + plotId); // ✅ Add title

        docRef.set(data);
    }

    public static String getWebApiKeyFromServiceAccount(String filePath) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(new FileReader(filePath));
            return (String) json.get("web_api_key");
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to read web API key from serviceAccountKey.json", e);
        }
    }


    public static UserRecord authenticateUser(String email, String password) {
        try {
            if (!initialized) {
                init();  // Ensure initialization
            }

            // Step 1: Get the Firebase Auth instance
            FirebaseAuth auth = FirebaseAuth.getInstance();

            // First try to get the user by email
            UserRecord userRecord = auth.getUserByEmail(email);

            // Since Firebase Admin SDK doesn't provide direct password verification,
            // we'll need to use the Firebase Authentication REST API

            // Get the API key from your Firebase project settings
            String firebaseWebApiKey = getWebApiKeyFromServiceAccount("serviceAccountKey.json");
            if (firebaseWebApiKey == null || firebaseWebApiKey.isEmpty()) {
                throw new IllegalStateException("Firebase Web API Key not found in environment variables");
            }

            // Create URL for Firebase Auth REST API
            URL url = new URL("https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + firebaseWebApiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Create request body
            String requestBody = String.format("{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                    email, password);

            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Check response
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                // Authentication successful
                return userRecord;
            } else {
                // Authentication failed
                System.err.println("Authentication failed with response code: " + responseCode);
                return null;
            }
        } catch (FirebaseAuthException e) {
            System.err.println("Authentication error: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error during authentication: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static String getUserRole(String uid) {
        try {
            if (!initialized) {
                init();  // Ensure initialization
            }

            // Fetch the custom claims for the user using Firebase Authentication
            UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
            Map<String, Object> claims = userRecord.getCustomClaims();

            // Return the role if available, otherwise return a default role
            return claims != null && claims.containsKey("role") ? claims.get("role").toString() : "User";
        } catch (FirebaseAuthException e) {
            e.printStackTrace();
            return "User";  // Default role
        }
    }

    public static String getUserName(String uid) {
        try {
            Firestore db = getDB(); // Get Firestore instance
            ApiFuture<DocumentSnapshot> future = db.collection("users").document(uid).get();
            DocumentSnapshot document = future.get(); // Blocking call

            if (document.exists()) {
                return document.getString("fullName"); // Assuming the field name is 'fullName'
            } else {
                System.out.println("No user document found for UID: " + uid);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Application> getPendingApplications() {
        List<Application> applications = new ArrayList<>();
        try {
            Firestore db = getDB();
            Query query = db.collection("applications").whereEqualTo("status", "Pending");
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            QuerySnapshot snapshot = querySnapshot.get();

            for (DocumentSnapshot document : snapshot.getDocuments()) {
                String id = document.getId(); // <-- Get Document ID (applicationId)
                String title = document.getString("title");
                String description = document.getString("description");
                String status = document.getString("status");
                String uid = document.getString("uid");

                Application app = new Application(title, description, status, id); // <-- Correct order
                applications.add(app);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return applications;
    }


    public static void updateApplicationStatus(String applicationUid, String status) {
        Firestore db = getDB();  // Get Firestore instance

        // Reference to the application document
        DocumentReference applicationRef = db.collection("applications").document(applicationUid);

        // Update the status field in the document
        ApiFuture<WriteResult> future = applicationRef.update("status", status);

        try {
            // Block until the update is completed and get the result
            WriteResult result = future.get();
            System.out.println("Application status updated to: " + status);
            System.out.println("Update time: " + result.getUpdateTime());
        } catch (Exception e) {
            // Handle failure
            System.err.println("Error updating status: " + e.getMessage());
        }
    }

    public static void createPlot(String uid, String plotNo, boolean isSellable, String address, String mapLink) {
        try {
            Firestore db = getDB();

            Map<String, Object> plotData = new HashMap<>();
            plotData.put("uid", uid);  // Owner/user ID
            plotData.put("plotNo", plotNo);
            plotData.put("isSellable", isSellable);
            plotData.put("address", address);
            plotData.put("mapLink", mapLink);
            plotData.put("approved", false); // Initially set to false
            plotData.put("createdAt", FieldValue.serverTimestamp());

            // Add to 'plots' collection
            db.collection("plots").add(plotData).get(); // Optional: block on write completion

            System.out.println("Plot added successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getUserPlots(String uid) {
        List<String> plotDescriptions = new ArrayList<>();
        Firestore db = getDB();

        try {
            ApiFuture<QuerySnapshot> future = db.collection("plots")
                    .whereEqualTo("uid", uid)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                String plotNo = doc.getString("plotNo");
                String address = doc.getString("address");
                plotDescriptions.add(plotNo + " | " + address);
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return plotDescriptions;
    }

    public static Map<String, String> getUserPlotsMap(String uid) {
        Map<String, String> plotMap = new HashMap<>();
        Firestore db = getDB();

        try {
            ApiFuture<QuerySnapshot> future = db.collection("plots")
                    .whereEqualTo("uid", uid)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                String plotNo = doc.getString("plotNo");
                String address = doc.getString("address");
                String description = plotNo + " | " + address;
                String plotId = doc.getId(); // Firestore document ID
                plotMap.put(description, plotId);
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return plotMap;
    }

}
