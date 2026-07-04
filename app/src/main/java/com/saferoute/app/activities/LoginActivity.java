package com.saferoute.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.saferoute.app.R;

/**
 * LoginActivity — Screen 6 in mockup
 * Handles Firebase Authentication login.
 * FIX: Now properly validates existing session against the server
 * before skipping to MainActivity, preventing stale-cache bypass.
 * Member 1 owns this activity.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnSignIn;
    private TextView tvForgotPassword, tvRegister;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // FIX: Reload from Firebase server before trusting the local cache.
        // Without this, a stale cached user from a previous project/google-services.json
        // causes the app to skip login even when the user has not registered yet.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                    // Real valid session — go to main
                    goToMain();
                } else {
                    // Stale session (project changed, token expired) — force login
                    mAuth.signOut();
                    initViews();
                    setListeners();
                }
            });
        } else {
            initViews();
            setListeners();
        }
    }

    private void initViews() {
        etEmail          = findViewById(R.id.etEmail);
        etPassword       = findViewById(R.id.etPassword);
        btnSignIn        = findViewById(R.id.btnSignIn);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister       = findViewById(R.id.tvRegister);

        tvRegister.setText(Html.fromHtml(
            "No account? <font color='#E53935'><b>Register with your university email</b></font>",
            Html.FROM_HTML_MODE_LEGACY));
    }

    private void setListeners() {
        btnSignIn.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v ->
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                            "Error: " + task.getException().getMessage(),
                            Toast.LENGTH_LONG).show();
                    }
                });
        });
    }

    private void attemptLogin() {
        String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        btnSignIn.setEnabled(false);
        btnSignIn.setText("Signing in...");

        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                btnSignIn.setEnabled(true);
                btnSignIn.setText("Sign In \u2192");

                if (task.isSuccessful()) {
                    goToMain();
                } else {
                    Toast.makeText(this,
                        "Login failed: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            });
    }

    private void goToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
