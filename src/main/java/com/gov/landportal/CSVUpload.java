package com.gov.landportal;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CSVUpload {

    static Firestore db;

    public static void initFirebase() throws IOException {
        FileInputStream serviceAccount = new FileInputStream("serviceAccountKey.json");

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);
        db = FirestoreClient.getFirestore();
    }

    public static void uploadCSV(String csvFilePath) throws IOException, ExecutionException, InterruptedException {
        BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
        String line;
        boolean header = true;

        while ((line = reader.readLine()) != null) {
            if (header) {
                header = false;
                continue; // Skip header
            }

            // Regular expression to split CSV fields, excluding parentheses.
            String[] parts = splitCSVLine(line);
//            System.out.println(Arrays.toString(parts));
            if (parts.length < 4) continue;

            String id = parts[0];
            String owner = parts[1];
            boolean isSellable = Boolean.parseBoolean(parts[2]);

            // Parse xPoints and yPoints as List of Integers instead of string
            String xPoints = parts[3];
            String yPoints = parts[4];

            Map<String, Object> plot = new HashMap<>();
            plot.put("id", id);
            plot.put("owner", owner);
            plot.put("isSellable", isSellable);
            plot.put("xPoints", xPoints); // Save as a list of Integers
            plot.put("yPoints", yPoints); // Save as a list of Integers

            DocumentReference docRef = db.collection("plots").document(id);
            DocumentSnapshot snapshot = docRef.get().get();

//            if (!snapshot.exists()) {
                docRef.set(plot);
                System.out.println("Added new plot: " + id);
//            } else {
//                System.out.println("Skipped existing plot: " + id);
//            }
        }

        reader.close();
        System.out.println("CSV upload complete.");
    }

//    private static List<Integer> parsePoints(String points) {
//        String[] pointStrings = points.replaceAll("[()]", "").split(",");
//        List<Integer> pointList = new ArrayList<>();
//        for (String point : pointStrings) {
//            pointList.add(Integer.parseInt(point.trim()));
//        }
//        return pointList;
//    }

    private static String[] splitCSVLine(String line) {
        List<String> result = new ArrayList<>();

        for(int i = 0; i < line.length(); i++){
            char ch = line.charAt(i);
            StringBuilder elem = new StringBuilder();
            if(ch == '('){
                i++;
                while(line.charAt(i) != ')'){
                    elem.append(line.charAt(i));
                    i++;
                }
                result.add(elem.toString());
            } else if (ch != ','){
                while(line.charAt(i) != ','){
                    elem.append(line.charAt(i));
                    i++;
                }
                result.add(elem.toString());
            }
        }

        // Further split the data on commas, considering the first five elements are not inside parentheses
        return result.toArray(new String[0]);
    }

    public static void main(String[] args) {
        try {
            initFirebase();
            uploadCSV("plots.csv");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
