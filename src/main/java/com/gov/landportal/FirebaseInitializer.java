package com.gov.landportal;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;

import java.io.FileInputStream;

public class FirebaseInitializer {
    private static boolean initialized = false;

    public static void init() {
        if (!initialized) {
            try {
                FileInputStream serviceAccount = new FileInputStream("serviceAccountKey.json");
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
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
}