package com.saferoute.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.saferoute.app.R;

/**
 * RegisterActivity — Firebase Auth registration.
 * Member 1 owns this activity.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> attemptRegister());

        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
        String confirm  = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        if (TextUtils.isEmpty(email)) { etEmail.setError("Required"); return; }
        if (!email.endsWith(".edu.my") && !email.endsWith(".edu")) {
            etEmail.setError("Use your university email");
            return;
        }
        if (password.length() < 6) { etPassword.setError("Min 6 characters"); return; }
        if (!password.equals(confirm)) { etConfirmPassword.setError("Passwords do not match"); return; }

        btnRegister.setEnabled(false);
        btnRegister.setText("Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                btnRegister.setEnabled(true);
                btnRegister.setText("Create Account");

                if (task.isSuccessful()) {
                    Toast.makeText(this, "Account created! Welcome to SafeRoute.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this,
                        "Registration failed: " + task.getException().getMessage(),
                        Toast.LENGTH_LONG).show();
                }
            });
    }
}
