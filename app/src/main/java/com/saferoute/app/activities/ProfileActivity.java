package com.saferoute.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.saferoute.app.R;

/**
 * ProfileActivity — My Profile screen
 * Shows current user info (email, UID, display name initials).
 * Provides logout and change password functionality.
 * Accessible from the profile icon button on the Home screen top bar.
 * Member 1 owns this activity.
 */
public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private TextView tvAvatar, tvDisplayName, tvEmail, tvEmailDetail, tvUserId, tvVerifiedBadge;
    private MaterialButton btnLogout;
    private LinearLayout btnChangePassword, btnMyContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        initViews();
        loadUserInfo();
        setListeners();
    }

    private void initViews() {
        tvAvatar        = findViewById(R.id.tvAvatar);
        tvDisplayName   = findViewById(R.id.tvDisplayName);
        tvEmail         = findViewById(R.id.tvEmail);
        tvEmailDetail   = findViewById(R.id.tvEmailDetail);
        tvUserId        = findViewById(R.id.tvUserId);
        tvVerifiedBadge = findViewById(R.id.tvVerifiedBadge);
        btnLogout       = findViewById(R.id.btnLogout);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnMyContacts   = findViewById(R.id.btnMyContacts);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Load the current Firebase user's info and display it.
     * Uses email prefix as display name (e.g. "john.doe" from "john.doe@taylors.edu.my").
     */
    private void loadUserInfo() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // Not logged in — go back to login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String email = user.getEmail() != null ? user.getEmail() : "Unknown";

        // Derive display name from email prefix
        String displayName = email.contains("@")
            ? email.split("@")[0]
            : email;

        // Avatar initial — first letter of display name, uppercase
        String initial = displayName.length() > 0
            ? String.valueOf(displayName.charAt(0)).toUpperCase()
            : "?";

        tvAvatar.setText(initial);
        tvDisplayName.setText(displayName);
        tvEmail.setText(email);
        tvEmailDetail.setText(email);

        // Show truncated UID for reference
        String uid = user.getUid();
        tvUserId.setText(uid.length() > 16 ? uid.substring(0, 16) + "..." : uid);

        // Show verified badge if email is verified
        if (user.isEmailVerified()) {
            tvVerifiedBadge.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void setListeners() {
        // Logout — show confirmation dialog first
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());

        // Change password — send reset email to current address
        btnChangePassword.setOnClickListener(v -> sendPasswordResetEmail());

        // My emergency contacts — go to SOS screen
        btnMyContacts.setOnClickListener(v ->
            startActivity(new Intent(this, SosActivity.class)));
    }

    /**
     * Shows a confirmation dialog before logging out.
     * Prevents accidental logouts.
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out of SafeRoute?")
            .setPositiveButton("Log Out", (dialog, which) -> performLogout())
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Signs out from Firebase Auth and navigates back to LoginActivity.
     * Calls finish() so the back button does not return to Profile or Home.
     */
    private void performLogout() {
        mAuth.signOut();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Clear the entire back stack and go to Login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Sends a password reset email to the current user's email address.
     * The user receives a link to set a new password.
     */
    private void sendPasswordResetEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = user.getEmail();

        new AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setMessage("A password reset link will be sent to:\n\n" + email)
            .setPositiveButton("Send Reset Email", (dialog, which) -> {
                mAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this,
                            "Reset email sent to " + email + ". Check your inbox.",
                            Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                            "Failed to send reset email: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
