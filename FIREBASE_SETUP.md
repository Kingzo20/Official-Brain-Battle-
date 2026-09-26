# Brain Battle — Firebase Setup Guide (Phase 3)

This document provides instructions for connecting your Firebase project to Brain Battle.

---

## 1. Firebase Project Configuration

1. Visit the [Firebase Console](https://console.firebase.google.com/).
2. Create a new Firebase Project (or select an existing project) named **Brain Battle**.
3. Add an Android app to your project with:
   - **Android package name**: Check your `app/build.gradle.kts` (e.g. `com.aistudio.brainbattle...` or `com.example`).
   - **Debug signing certificate SHA-1**: Generate via `./gradlew signingReport` or Android Studio Gradle panel.
4. Download the generated **`google-services.json`** file.
5. Place **`google-services.json`** into the project's **`/app`** directory:
   ```
   /app/google-services.json
   ```

---

## 2. Enable Authentication Providers

In the Firebase Console -> **Build** -> **Authentication**:

1. Click **Get Started**.
2. Under the **Sign-in method** tab, enable:
   - **Email/Password**: Turn ON the toggle and click **Save**. (Email link / passwordless can remain disabled).
   - **Google**: Turn ON the toggle, specify your support email, and click **Save**. Ensure the Web Client ID is configured in Android Credential Manager.

---

## 3. Enable Cloud Firestore

1. In the Firebase Console -> **Build** -> **Firestore Database**.
2. Click **Create Database**.
3. Select your location (e.g. `us-central1` or `europe-west`).
4. Under the **Rules** tab, deploy the security rules provided in the project root:
   ```bash
   # Using Firebase CLI
   firebase deploy --only firestore:rules
   ```
   Or copy-paste the contents of `firestore.rules` directly into the Firestore Rules editor in the Firebase Console.

---

## 4. Firestore Collections Hierarchy

The application interacts with the following schema:

```
├── usernames/
│   └── {username_lowercase}
│       ├── uid: string
│       ├── username: string
│       └── createdAt: timestamp
│
├── users/
│   └── {uid}
│       ├── uid: string
│       ├── username: string
│       ├── displayName: string
│       ├── email: string
│       ├── avatarEmoji: string
│       ├── level: number
│       ├── xp: number
│       ├── totalGames: number
│       ├── totalScore: number
│       ├── bestScore: number
│       ├── currentStreak: number
│       ├── bestStreak: number
│       ├── lastDailyChallengeDate: string ("yyyy-MM-dd")
│       ├── dailyChallengeCompleted: boolean
│       ├── createdAt: number (epoch ms)
│       ├── updatedAt: number (epoch ms)
│       ├── lastActiveAt: number (epoch ms)
│       ├── country: string
│       ├── countryFlag: string
│       ├── language: string
│       ├── settings: map
│       └── statistics: map
│           ├── gameHistory/
│           │   └── {gameId} (immutable records)
│           ├── achievements/
│           │   └── {achievementId}
│           └── dailyChallenges/
│               └── {dateId}
│
└── leaderboards/
    └── global/
        └── entries/
            └── {uid}
```

---

## 5. Offline-First & Fallback Mode

If `google-services.json` is not yet placed in `/app`:
- The app operates safely in **Offline Mode**.
- Local games, scores, and settings are preserved in local storage.
- When `google-services.json` is added, the app connects to real Firebase Authentication and Cloud Firestore, automatically synchronizing pending offline game history and player stats.
