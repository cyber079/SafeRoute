package com.saferoute.app.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.saferoute.app.R;
import com.saferoute.app.adapters.AlertAdapter;
import com.saferoute.app.database.FirebaseHelper;
import com.saferoute.app.models.Alert;

import java.util.ArrayList;
import java.util.List;

/**
 * AlertsActivity — Safety Alerts screen (Screen 3 in mockup)
 *
 * FIX for "permission denied":
 * Go to Firebase Console → Realtime Database → Rules tab → set:
 * { "rules": { ".read": true, ".write": true } } → Publish
 *
 * Member 3 owns Realtime Database integration.
 * Member 4 owns testing of this screen.
 */
public class AlertsActivity extends AppCompatActivity {

    private RecyclerView rvAlerts;
    private AlertAdapter adapter;
    private List<Alert> alertList = new ArrayList<>();
    private TextView tvActiveCount;

    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);

        // Use FirebaseHelper — sets persistence + correct DB instance
        db = FirebaseHelper.getRef("incidents");

        tvActiveCount = findViewById(R.id.tvActiveCount);

        rvAlerts = findViewById(R.id.rvAlerts);
        rvAlerts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AlertAdapter(alertList, this);
        rvAlerts.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabReport);
        fab.setOnClickListener(v -> showReportDialog());

        if (getIntent().getBooleanExtra("open_report", false)) {
            showReportDialog();
        }

        loadAlertsFromDatabase();
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadAlertsFromDatabase() {
        Query alertsQuery = db.orderByChild("timestamp");

        alertsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                alertList.clear();
                int activeCount = 0;

                for (DataSnapshot doc : snapshot.getChildren()) {
                    Alert alert = doc.getValue(Alert.class);
                    if (alert == null) continue;
                    alert.setId(doc.getKey());
                    alertList.add(0, alert);
                    if ("ALERT".equals(alert.getType())) activeCount++;
                }

                adapter.notifyDataSetChanged();
                tvActiveCount.setText(activeCount + " ACTIVE");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Friendly error explaining what to do
                String msg;
                if (error.getCode() == DatabaseError.PERMISSION_DENIED) {
                    msg = "Permission denied. Fix: Firebase Console → Realtime Database → Rules → set .read and .write to true → Publish";
                } else {
                    msg = "Failed to load alerts: " + error.getMessage();
                }
                Toast.makeText(AlertsActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showReportDialog() {
        Dialog dialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_report_incident, null);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText etDescription = view.findViewById(R.id.etDescription);
        EditText etLocation    = view.findViewById(R.id.etLocation);
        Spinner  spType        = view.findViewById(R.id.spAlertType);

        view.findViewById(R.id.btnSubmitReport).setOnClickListener(v -> {
            String description = etDescription.getText().toString().trim();
            String location    = etLocation.getText().toString().trim();
            String type        = spType.getSelectedItem().toString();

            if (description.isEmpty()) {
                etDescription.setError("Please describe the incident");
                return;
            }
            if (location.isEmpty()) {
                etLocation.setError("Please enter the location");
                return;
            }
            submitIncident(description, location, type);
            dialog.dismiss();
        });

        view.findViewById(R.id.btnCancelReport).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void submitIncident(String description, String location, String type) {
        Alert alert = new Alert(description, location, type);
        alert.setUserId(FirebaseHelper.getCurrentUserId());

        String key = db.push().getKey();
        if (key == null) {
            Toast.makeText(this, "Failed to generate ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.child(key).setValue(alert)
            .addOnSuccessListener(unused ->
                Toast.makeText(this, "Report submitted. Thank you!", Toast.LENGTH_SHORT).show())
            .addOnFailureListener(e -> {
                String msg = e.getMessage() != null && e.getMessage().contains("Permission denied")
                    ? "Permission denied — fix Firebase Rules (see README)"
                    : "Failed to submit: " + e.getMessage();
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            });
    }
}
