# 🌍 Land Management Tool

A Java desktop application for managing land requests using **Firebase Authentication** and **Firestore**. This project demonstrates how to integrate Firebase's Admin SDK with Java and handle Email/Password authentication via REST API.

---

## 📦 Features

- 🔐 Firebase Email/Password Authentication (via REST)
- 🧾 Application submission by users
- ✅ Officer review dashboard for pending applications
- ☁️ Firestore integration for real-time data

---

## ✅ Prerequisites

- Java 8 or higher
- Maven installed
- A Firebase Project with:
  - Firestore database enabled
  - Email/Password authentication enabled

---

## 🚀 Project Initialization

### 1. Configure Firebase Credentials

#### 🔹 Step 1: Add `serviceAccountKey.json`

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project → ⚙️ **Project Settings** → **Service Accounts**
3. Click **Generate new private key**
4. Download the file and **rename it to**: *serviceAccountKey.json*

5. Place this file in the root of your project

---

#### 🔹 Step 2: Add Web API Key to JSON


This setup uses Firebase's Identity Toolkit REST API, which requires a valid Web API Key. 

Open the `serviceAccountKey.json` file and add your Firebase Web API Key as a new field at the end of the JSON:

```json
"web_api_key": "your_firebase_web_api_key"
```

✅ Example (truncated for clarity):
```json
{
  "type": "service_account",
  "project_id": "your-project-id",
  "private_key_id": "abc123...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...",
  "client_email": "firebase-adminsdk-abc@your-project.iam.gserviceaccount.com",
  "client_id": "1234567890",
  ...
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/...",
  "universe_domain": "googleapis.com",
  "web_api_key": "your_firebase_web_api_key"
}
```

Make sure the JSON remains valid — all commas and structure must be correct.

### 2. Run the Project
Use the following Maven command to compile and run the application:
```
mvn clean compile exec:java -Dexec.mainClass="login.Main"
```

