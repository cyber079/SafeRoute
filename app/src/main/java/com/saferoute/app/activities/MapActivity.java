package com.saferoute.app.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.saferoute.app.database.FirebaseHelper;
import com.google.firebase.database.ValueEventListener;
import com.saferoute.app.R;
import com.saferoute.app.database.SafeRouteDatabase;
import com.saferoute.app.models.SafePath;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.List;

/**
 * MapActivity — Campus Map screen (Screen 2 in mockup)
 * Uses OSMDroid (OpenStreetMap) instead of Google Maps SDK.
 * 100% free — no API key, no billing account, no Google Cloud setup needed.
 * Member 2 owns this activity.
 */
public class MapActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 200;

    private static final GeoPoint TAYLORS_CAMPUS  = new GeoPoint(3.0672, 101.6007);
    private static final GeoPoint SECURITY_POST_A = new GeoPoint(3.0680, 101.6015);
    private static final GeoPoint HEALTH_CENTER   = new GeoPoint(3.0665, 101.6000);
    private static final GeoPoint MAIN_GATE       = new GeoPoint(3.0690, 101.6020);

    private MapView mapView;
    private SafeRouteDatabase localDb;
    private DatabaseReference firebaseDb;
    private MyLocationNewOverlay myLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // OSMDroid MUST be configured before setContentView.
        // FIX for blank map:
        // 1. Set user agent — OpenStreetMap tile servers REJECT requests without one.
        //    Without this the map shows grey/blank with no error.
        // 2. Set tile cache to app's private cache dir — avoids permission issues on API 29+.
        Configuration.getInstance().load(this,
            PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(getCacheDir());
        Configuration.getInstance().setOsmdroidTileCache(
            new java.io.File(getCacheDir(), "osmdroid"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        localDb    = new SafeRouteDatabase(this);
        firebaseDb = FirebaseHelper.getDatabase().getReference();

        mapView = findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(17.0);
        mapView.getController().setCenter(TAYLORS_CAMPUS);

        enableMyLocation();
        drawSeedSafePaths();
        addCampusMarkers();
        loadFirebasePaths();
        loadFirebaseIncidentZones();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnNavigate).setOnClickListener(v ->
            Toast.makeText(this, "Opening navigation...", Toast.LENGTH_SHORT).show());
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            myLocationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), mapView);
            myLocationOverlay.enableMyLocation();
            mapView.getOverlays().add(myLocationOverlay);
        } else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    private void drawSeedSafePaths() {
        List<SafePath> paths = localDb.getAllSafePaths();
        for (SafePath path : paths) {
            Polyline line = new Polyline();
            line.addPoint(new GeoPoint(path.getLatStart(), path.getLngStart()));
            line.addPoint(new GeoPoint(path.getLatEnd(), path.getLngEnd()));
            line.getOutlinePaint().setColor(Color.parseColor("#43A047"));
            line.getOutlinePaint().setStrokeWidth(10f);
            line.setTitle(path.getLabel());
            mapView.getOverlays().add(line);
        }
        mapView.invalidate();
    }

    private void addCampusMarkers() {
        addMarker(SECURITY_POST_A, "Security Post A", "120m away · Available 24/7");
        addMarker(HEALTH_CENTER,   "Health Center",   "340m away · Mon–Sat 8am–8pm");
        addMarker(MAIN_GATE,       "Main Gate",       "Entry / Exit point");
    }

    private void addMarker(GeoPoint point, String title, String snippet) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setSnippet(snippet);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

    /** Community-submitted safe paths from Firebase Realtime Database. */
    private void loadFirebasePaths() {
        firebaseDb.child("safe_paths").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot pathSnap : snapshot.getChildren()) {
                    try {
                        Double latStart = pathSnap.child("lat_start").getValue(Double.class);
                        Double lngStart = pathSnap.child("lng_start").getValue(Double.class);
                        Double latEnd   = pathSnap.child("lat_end").getValue(Double.class);
                        Double lngEnd   = pathSnap.child("lng_end").getValue(Double.class);

                        if (latStart == null || lngStart == null || latEnd == null || lngEnd == null) continue;

                        Polyline line = new Polyline();
                        line.addPoint(new GeoPoint(latStart, lngStart));
                        line.addPoint(new GeoPoint(latEnd, lngEnd));
                        line.getOutlinePaint().setColor(Color.parseColor("#1E88E5"));
                        line.getOutlinePaint().setStrokeWidth(8f);
                        mapView.getOverlays().add(line);
                    } catch (Exception ignored) {}
                }
                mapView.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapActivity.this, "Failed to load community paths",
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Incident locations from Firebase Realtime Database shown as red warning markers. */
    private void loadFirebaseIncidentZones() {
        firebaseDb.child("incidents").orderByChild("type").equalTo("ALERT")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot incidentSnap : snapshot.getChildren()) {
                        try {
                            Double lat = incidentSnap.child("latitude").getValue(Double.class);
                            Double lng = incidentSnap.child("longitude").getValue(Double.class);
                            String desc = incidentSnap.child("description").getValue(String.class);
                            if (lat == null || lng == null) continue;

                            Marker marker = new Marker(mapView);
                            marker.setPosition(new GeoPoint(lat, lng));
                            marker.setTitle("⚠ Incident");
                            marker.setSnippet(desc != null ? desc : "Reported incident");
                            mapView.getOverlays().add(marker);
                        } catch (Exception ignored) {}
                    }
                    mapView.invalidate();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION && grantResults.length > 0
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}
