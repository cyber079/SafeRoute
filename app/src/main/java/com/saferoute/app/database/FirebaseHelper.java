package com.saferoute.app.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * FirebaseHelper — centralised Firebase access.
 *
 * FIX for "permission denied" error:
 * The error occurs because Firebase Realtime Database security rules
 * in test mode expire after 30 days, OR because the database was not
 * created yet, OR because rules block unauthenticated writes.
 *
 * HOW TO FIX IN FIREBASE CONSOLE:
 * 1. Go to console.firebase.google.com
 * 2. Select your project → Build → Realtime Database
 * 3. Click the "Rules" tab
 * 4. Replace the rules with:
 *    {
 *      "rules": {
 *        ".read": true,
 *        ".write": true
 *      }
 *    }
 * 5. Click Publish
 * (This is test mode — fine for student prototype. Tighten before production.)
 *
 * Member 3 owns this class.
 */
public class FirebaseHelper {

    private static FirebaseDatabase instance;

    /**
     * Returns a configured FirebaseDatabase instance.
     * Sets persistence enabled so data is cached offline.
     */
    public static FirebaseDatabase getDatabase() {
        if (instance == null) {
            instance = FirebaseDatabase.getInstance();
            // Enable offline persistence — data is readable even without internet
            instance.setPersistenceEnabled(true);
        }
        return instance;
    }

    /** Shortcut to get a DatabaseReference at a specific path. */
    public static DatabaseReference getRef(String path) {
        return getDatabase().getReference(path);
    }

    /** Returns the currently signed-in Firebase user, or null. */
    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /** Returns current user ID, or "anonymous" if not signed in. */
    public static String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : "anonymous";
    }
}
