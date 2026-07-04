package com.saferoute.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.saferoute.app.R;
import com.saferoute.app.adapters.ContactAdapter;
import com.saferoute.app.database.SafeRouteDatabase;
import com.saferoute.app.models.Contact;

import java.util.List;

/**
 * SosActivity — Quick-Alert screen
 * Captures live GPS via FusedLocationProviderClient
 * then sends SMS with coordinates via SmsManager.
 * Works without internet — uses cellular network only.
 * Member 4 owns this activity.
 */
public class SosActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 101;
    private static final int REQUEST_SMS      = 102;

    private FusedLocationProviderClient fusedLocationClient;
    private SafeRouteDatabase db;
    private List<Contact> contacts;
    private ContactAdapter contactAdapter;

    private TextView tvStatus;
    private Button btnSendSos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = new SafeRouteDatabase(this);

        tvStatus   = findViewById(R.id.tvSosStatus);
        btnSendSos = findViewById(R.id.btnSendSos);

        // Load emergency contacts from SQLite
        RecyclerView rvContacts = findViewById(R.id.rvSosContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        contacts = db.getAllContacts();
        contactAdapter = new ContactAdapter(contacts, this);
        rvContacts.setAdapter(contactAdapter);

        btnSendSos.setOnClickListener(v -> checkPermissionsAndSend());

        findViewById(R.id.btnAddContact).setOnClickListener(v -> showAddContactDialog());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void checkPermissionsAndSend() {
        if (contacts.isEmpty()) {
            Toast.makeText(this, "Add at least one emergency contact first", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasLocation = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean hasSms = ActivityCompat.checkSelfPermission(this,
            Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED;

        if (!hasLocation || !hasSms) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS},
                REQUEST_LOCATION);
            return;
        }

        captureLocationAndSendSos();
    }

    @SuppressLint("MissingPermission")
    private void captureLocationAndSendSos() {
        tvStatus.setText("Capturing your location...");
        btnSendSos.setEnabled(false);

        // FusedLocationProviderClient — most accurate, battery-efficient
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    sendSosToAllContacts(location.getLatitude(), location.getLongitude());
                } else {
                    // Location not cached — request a fresh one
                    tvStatus.setText("Getting GPS fix...");
                    requestFreshLocation();
                }
            })
            .addOnFailureListener(e -> {
                tvStatus.setText("Could not get location. Check GPS settings.");
                btnSendSos.setEnabled(true);
            });
    }

    @SuppressLint("MissingPermission")
    private void requestFreshLocation() {
        com.google.android.gms.location.LocationRequest locationRequest =
            com.google.android.gms.location.LocationRequest.create()
                .setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
                .setInterval(1000);

        fusedLocationClient.requestLocationUpdates(locationRequest,
            new com.google.android.gms.location.LocationCallback() {
                @Override
                public void onLocationResult(@NonNull com.google.android.gms.location.LocationResult result) {
                    if (!result.getLocations().isEmpty()) {
                        android.location.Location loc = result.getLocations().get(0);
                        sendSosToAllContacts(loc.getLatitude(), loc.getLongitude());
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }
            },
            getMainLooper());
    }

    private void sendSosToAllContacts(double lat, double lng) {
        String mapsLink = "https://maps.google.com/?q=" + lat + "," + lng;
        String message  = "🚨 SOS ALERT from SafeRoute!\n"
            + "I need help. My location:\n"
            + mapsLink + "\n"
            + "Please contact me or call emergency services.";

        SmsManager smsManager = SmsManager.getDefault();
        int successCount = 0;

        for (Contact contact : contacts) {
            try {
                // Split message if > 160 chars (handles long SMS automatically)
                java.util.ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(
                    contact.getPhone(),
                    null,
                    parts,
                    null,
                    null
                );
                successCount++;
            } catch (Exception e) {
                Toast.makeText(this,
                    "Failed to send to " + contact.getName() + ": " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            }
        }

        btnSendSos.setEnabled(true);
        if (successCount > 0) {
            tvStatus.setText("✅ SOS sent to " + successCount + " contact(s)!\nLat: "
                + String.format("%.6f", lat) + ", Lng: " + String.format("%.6f", lng));
            tvStatus.setTextColor(getColor(R.color.safe_green));
        } else {
            tvStatus.setText("Failed to send SOS. Check SMS permissions.");
        }
    }

    private void showAddContactDialog() {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View dialogView = inflater.inflate(R.layout.dialog_add_contact, null);

        android.widget.EditText etName  = dialogView.findViewById(R.id.etContactName);
        android.widget.EditText etPhone = dialogView.findViewById(R.id.etContactPhone);

        new AlertDialog.Builder(this)
            .setTitle("Add Emergency Contact")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                String name  = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                if (!name.isEmpty() && !phone.isEmpty()) {
                    Contact c = new Contact(name, phone);
                    db.insertContact(c);
                    contacts.clear();
                    contacts.addAll(db.getAllContacts());
                    contactAdapter.notifyDataSetChanged();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) { allGranted = false; break; }
            }
            if (allGranted) {
                captureLocationAndSendSos();
            } else {
                Toast.makeText(this,
                    "Location and SMS permissions are required for SOS",
                    Toast.LENGTH_LONG).show();
            }
        }
    }
}
