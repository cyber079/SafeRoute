package com.saferoute.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.saferoute.app.R;
import com.saferoute.app.adapters.ContactAdapter;
import com.saferoute.app.database.SafeRouteDatabase;
import com.saferoute.app.models.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * SosActivity — Quick-Alert screen
 * FIX 1: SMS now actually sends using correct Android 12+ API
 * FIX 2: Delete contact button is now wired up
 * FIX 3: Location uses high-accuracy fresh fix, not stale cached location
 * Member 4 owns this activity.
 */
public class SosActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 101;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
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

        // Load contacts from SQLite
        RecyclerView rvContacts = findViewById(R.id.rvSosContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        contacts = db.getAllContacts();

        // FIX: Pass delete callback so the delete button actually works
        contactAdapter = new ContactAdapter(contacts, this, contact -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Contact")
                    .setMessage("Remove " + contact.getName() + " from emergency contacts?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.deleteContact(contact.getId());
                        contacts.clear();
                        contacts.addAll(db.getAllContacts());
                        contactAdapter.notifyDataSetChanged();
                        Toast.makeText(this, contact.getName() + " removed", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
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
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.SEND_SMS
                    }, REQUEST_PERMISSIONS);
            return;
        }

        captureLocationAndSendSos();
    }

    @SuppressLint("MissingPermission")
    private void captureLocationAndSendSos() {
        tvStatus.setText("Getting your location...");
        btnSendSos.setEnabled(false);

        // FIX: Always request a FRESH high-accuracy location
        // Never rely on getLastLocation() — it can be null or hours old
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMaxUpdates(1)
                .setWaitForAccurateLocation(false)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                fusedLocationClient.removeLocationUpdates(this);
                if (!result.getLocations().isEmpty()) {
                    android.location.Location loc = result.getLocations().get(0);
                    sendSosToAllContacts(loc.getLatitude(), loc.getLongitude());
                } else {
                    runOnUiThread(() -> {
                        tvStatus.setText("Could not get location. Make sure GPS is on.");
                        btnSendSos.setEnabled(true);
                    });
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );

        // Timeout after 15 seconds if no GPS fix
        new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!btnSendSos.isEnabled()) return; // already got location
            fusedLocationClient.removeLocationUpdates(locationCallback);
            tvStatus.setText("GPS timeout. Make sure location is enabled.");
            btnSendSos.setEnabled(true);
        }, 15000);
    }

    private void sendSosToAllContacts(double lat, double lng) {
        if (isFinishing() || isDestroyed()) return;

        String mapsLink = "https://maps.google.com/?q=" + lat + "," + lng;
        String message  = "SOS ALERT from SafeRoute! "
                + "I need help. My location: "
                + mapsLink
                + " Please contact me or call emergency services.";

        // FIX: Use correct SmsManager API for Android 12+ (API 31+)
        // getSystemService(SmsManager.class) is required on Android 12+
        // SmsManager.getDefault() is deprecated and unreliable on newer devices
        SmsManager smsManager;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            smsManager = this.getSystemService(SmsManager.class);
        } else {
            smsManager = SmsManager.getDefault();
        }

        if (smsManager == null) {
            runOnUiThread(() -> {
                Toast.makeText(this,
                        "SMS not available on this device",
                        Toast.LENGTH_LONG).show();
                btnSendSos.setEnabled(true);
            });
            return;
        }

        int successCount = 0;
        for (Contact contact : contacts) {
            try {
                ArrayList<String> parts = smsManager.divideMessage(message);
                smsManager.sendMultipartTextMessage(
                        contact.getPhone(),
                        null,
                        parts,
                        null,
                        null
                );
                successCount++;
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this,
                                "Failed to send to " + contact.getName() + ": " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
            }
        }

        final int sent = successCount;
        final double finalLat = lat;
        final double finalLng = lng;

        runOnUiThread(() -> {
            btnSendSos.setEnabled(true);
            if (sent > 0) {
                tvStatus.setText("SOS sent to " + sent + " contact(s)!\nLat: "
                        + String.format("%.5f", finalLat)
                        + ", Lng: " + String.format("%.5f", finalLng));
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.safe_green));
            } else {
                tvStatus.setText("Failed to send. Check SMS permission in phone Settings.");
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.primary_red));
            }
        });
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

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (phone.isEmpty()) {
                        Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Basic phone validation
                    if (!phone.matches("[0-9+\\-]{8,15}")) {
                        Toast.makeText(this,
                                "Enter a valid phone number (e.g. 0123456789 or +60123456789)",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    Contact c = new Contact(name, phone);
                    db.insertContact(c);
                    contacts.clear();
                    contacts.addAll(db.getAllContacts());
                    contactAdapter.notifyDataSetChanged();
                    Toast.makeText(this, name + " added as emergency contact", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                captureLocationAndSendSos();
            } else {
                // Guide user to settings if permission permanently denied
                new AlertDialog.Builder(this)
                        .setTitle("Permissions Required")
                        .setMessage("Location and SMS permissions are required for SOS. "
                                + "Please enable them in phone Settings → Apps → SafeRoute → Permissions.")
                        .setPositiveButton("Open Settings", (d, w) -> {
                            Intent intent = new Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up location callback to prevent memory leaks
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}