package com.saferoute.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.saferoute.app.database.FirebaseHelper;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.saferoute.app.R;
import com.saferoute.app.adapters.AlertAdapter;
import com.saferoute.app.models.Alert;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * MainActivity — Home Dashboard (Screen 1 in mockup)
 * Uses OSMDroid mini-map (free, no API key) and Firebase Realtime Database
 * (free Spark plan, no billing required) for recent alerts.
 * Member 1 owns navigation. Member 3 owns Realtime Database data loading.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView rvRecentAlerts;
    private AlertAdapter alertAdapter;
    private List<Alert> alertList = new ArrayList<>();

    private DatabaseReference db;
    private TextView tvIncidentCount;
    private MapView mapPreview;

    private static final GeoPoint TAYLORS_CAMPUS = new GeoPoint(3.0672, 101.6007);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Required OSMDroid config before setContentView
        // FIX for blank map: set user agent + cache directory
        Configuration.getInstance().load(this,
            PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(getCacheDir());
        Configuration.getInstance().setOsmdroidTileCache(
            new java.io.File(getCacheDir(), "osmdroid"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseHelper.getDatabase().getReference();

        initViews();
        setupBottomNav();
        setupQuickActions();
        setupMiniMap();
        loadRecentAlerts();
    }

    private void initViews() {
        tvIncidentCount = findViewById(R.id.tvIncidentCount);

        rvRecentAlerts = findViewById(R.id.rvRecentAlerts);
        rvRecentAlerts.setLayoutManager(new LinearLayoutManager(this));
        alertAdapter = new AlertAdapter(alertList, this);
        rvRecentAlerts.setAdapter(alertAdapter);

        findViewById(R.id.tvViewAll).setOnClickListener(v ->
            startActivity(new Intent(this, AlertsActivity.class)));

        findViewById(R.id.tvFullMap).setOnClickListener(v ->
            startActivity(new Intent(this, MapActivity.class)));
    }

    private void setupQuickActions() {
        MaterialCardView cardSos = findViewById(R.id.cardSos);
        cardSos.setOnClickListener(v ->
            startActivity(new Intent(this, SosActivity.class)));

        MaterialCardView cardReport = findViewById(R.id.cardReport);
        cardReport.setOnClickListener(v -> {
            Intent i = new Intent(this, AlertsActivity.class);
            i.putExtra("open_report", true);
            startActivity(i);
        });

        MaterialCardView cardRoute = findViewById(R.id.cardRoute);
        cardRoute.setOnClickListener(v ->
            startActivity(new Intent(this, MapActivity.class)));

        MaterialCardView cardAlert = findViewById(R.id.cardAlert);
        cardAlert.setOnClickListener(v -> {
            Intent i = new Intent(this, SosActivity.class);
            i.putExtra("alert_mode", true);
            startActivity(i);
        });
    }

    /**
     * Small, non-interactive OSMDroid map preview on the Home screen.
     * Tapping "Full Map" opens the interactive MapActivity.
     */
    private void setupMiniMap() {
        mapPreview = findViewById(R.id.mapPreview);
        mapPreview.setTileSource(TileSourceFactory.MAPNIK);
        mapPreview.getController().setZoom(15.5);
        mapPreview.getController().setCenter(TAYLORS_CAMPUS);
        // Disable gestures so it behaves like a static preview
        mapPreview.setOnTouchListener((v, event) -> true);
    }

    private void loadRecentAlerts() {
        // Load the 3 most recent incidents from Realtime Database
        Query recentQuery = db.child("incidents")
            .orderByChild("timestamp")
            .limitToLast(3);

        recentQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                alertList.clear();
                for (DataSnapshot doc : snapshot.getChildren()) {
                    Alert alert = doc.getValue(Alert.class);
                    if (alert != null) {
                        alert.setId(doc.getKey());
                        alertList.add(0, alert); // newest first
                    }
                }
                alertAdapter.notifyDataSetChanged();
                tvIncidentCount.setText(String.valueOf(alertList.size()));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Fail silently on home screen — full list available in AlertsActivity
            }
        });
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> { /* already here */ });
        findViewById(R.id.navMap).setOnClickListener(v ->
            startActivity(new Intent(this, MapActivity.class)));
        findViewById(R.id.navAlerts).setOnClickListener(v ->
            startActivity(new Intent(this, AlertsActivity.class)));
        findViewById(R.id.navHelp).setOnClickListener(v ->
            startActivity(new Intent(this, HelpActivity.class)));
        findViewById(R.id.navCheckin).setOnClickListener(v ->
            startActivity(new Intent(this, CheckInActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapPreview != null) mapPreview.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapPreview != null) mapPreview.onPause();
    }
}
