package com.saferoute.app.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.saferoute.app.database.FirebaseHelper;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.saferoute.app.R;
import com.saferoute.app.adapters.CheckInAdapter;
import com.saferoute.app.models.CheckIn;

import java.util.ArrayList;
import java.util.List;

/**
 * CheckInActivity — Safety Check-in screen (Screen 5 in mockup)
 * Users mark themselves safe at a campus location.
 * Community check-ins are loaded from Firebase Realtime Database (free, no billing).
 * Member 3 owns database write. Member 2 owns the UI.
 */
public class CheckInActivity extends AppCompatActivity {

    private Spinner spLocation;
    private MaterialButton btnCheckIn;
    private RecyclerView rvCheckIns;
    private TextView tvCheckInCount;

    private DatabaseReference db;
    private FirebaseAuth mAuth;
    private List<CheckIn> checkInList = new ArrayList<>();
    private CheckInAdapter adapter;

    private final String[] CAMPUS_LOCATIONS = {
        "Main Library", "Block A — Lecture Hall", "Block B — Computer Lab",
        "Student Hub", "Cafeteria", "Sports Complex", "Car Park A",
        "Car Park B", "Main Gate", "Back Gate", "Medical Centre", "Mosque"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);

        db = FirebaseHelper.getRef("checkins");
        mAuth = FirebaseAuth.getInstance();

        spLocation     = findViewById(R.id.spLocation);
        btnCheckIn     = findViewById(R.id.btnCheckIn);
        tvCheckInCount = findViewById(R.id.tvCheckInCount);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
            this, android.R.layout.simple_spinner_dropdown_item, CAMPUS_LOCATIONS);
        spLocation.setAdapter(spinnerAdapter);

        rvCheckIns = findViewById(R.id.rvCheckIns);
        rvCheckIns.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CheckInAdapter(checkInList, this);
        rvCheckIns.setAdapter(adapter);

        btnCheckIn.setOnClickListener(v -> submitCheckIn());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadCommunityCheckIns();
    }

    /**
     * Submits the user's safety check-in to Realtime Database.
     * All other users will see this in their community check-ins list.
     */
    private void submitCheckIn() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        String location = spLocation.getSelectedItem().toString();
        String email = user.getEmail() != null ? user.getEmail() : "Anonymous";
        String displayName = email.contains("@") ? email.split("@")[0] : email;

        CheckIn checkIn = new CheckIn();
        checkIn.setUserId(user.getUid());
        checkIn.setDisplayName(displayName);
        checkIn.setLocation(location);
        checkIn.setTimestamp(System.currentTimeMillis());

        btnCheckIn.setEnabled(false);
        btnCheckIn.setText("Checking in...");

        String key = db.push().getKey();
        if (key == null) {
            btnCheckIn.setEnabled(true);
            btnCheckIn.setText("Check In — I'm Safe");
            return;
        }

        db.child(key).setValue(checkIn)
            .addOnSuccessListener(unused -> {
                Toast.makeText(this, "✅ Checked in at " + location, Toast.LENGTH_SHORT).show();
                btnCheckIn.setEnabled(true);
                btnCheckIn.setText("Check In — I'm Safe");
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Check-in failed: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
                btnCheckIn.setEnabled(true);
                btnCheckIn.setText("Check In — I'm Safe");
            });
    }

    /**
     * Real-time listener for community check-ins.
     * Shows the 20 most recent check-ins from all users.
     */
    private void loadCommunityCheckIns() {
        Query recentQuery = db.orderByChild("timestamp").limitToLast(20);

        recentQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                checkInList.clear();
                for (DataSnapshot doc : snapshot.getChildren()) {
                    CheckIn checkIn = doc.getValue(CheckIn.class);
                    if (checkIn != null) {
                        checkIn.setId(doc.getKey());
                        checkInList.add(0, checkIn); // newest first
                    }
                }
                adapter.notifyDataSetChanged();
                tvCheckInCount.setText(checkInList.size() + " today");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(CheckInActivity.this,
                    "Failed to load check-ins", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
