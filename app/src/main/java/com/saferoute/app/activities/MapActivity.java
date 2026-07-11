package com.saferoute.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.google.firebase.database.ValueEventListener;
import com.saferoute.app.database.FirebaseHelper;
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
 * MapActivity — Campus Map screen
 * FIX 1: Blue "you are here" dot now works and centres on user on first fix
 * FIX 2: Map centres on user's real location, not hardcoded campus point
 * FIX 3: Location overlay properly enabled with follow + first fix animation
 * Member 2 owns this activity.
 */
public class MapActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 200;

    // Taylor's University Subang Jaya — campus centre
    // UPDATE these with real coordinates from Google Maps if needed
    private static final GeoPoint TAYLORS_CAMPUS  = new GeoPoint(3.06731, 101.60033);
    private static final GeoPoint SECURITY_POST_A = new GeoPoint(3.06800, 101.60150);
    private static final GeoPoint HEALTH_CENTER   = new GeoPoint(3.06650, 101.60000);
    private static final GeoPoint MAIN_GATE       = new GeoPoint(3.06900, 101.60200);

    private MapView mapView;
    private SafeRouteDatabase localDb;
    private DatabaseReference firebaseDb;
    private MyLocationNewOverlay myLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        mapView.getController().setZoom(18.0); // zoom in more for campus detail
        mapView.getController().setCenter(TAYLORS_CAMPUS);

        enableMyLocation();
        drawSeedSafePaths();
        addCampusMarkers();
        loadFirebasePaths();
        loadFirebaseIncidentZones();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnNavigate).setOnClickListener(v -> centreOnMyLocation());
    }

    /**
     * FIX: Properly enables the blue location dot and centres the map
     * on the user's actual position on first GPS fix.
     */
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            myLocationOverlay = new MyLocationNewOverlay(
                    new GpsMyLocationProvider(this), mapView);

            myLocationOverlay.enableMyLocation();

            // FIX: Centre map on user's real location when first GPS fix is received
            myLocationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
                GeoPoint myLocation = myLocationOverlay.getMyLocation();
                if (myLocation != null) {
                    mapView.getController().setZoom(18.0);
                    mapView.getController().animateTo(myLocation);
                }
            }));

            mapView.getOverlays().add(myLocationOverlay);
            mapView.invalidate();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    /**
     * Centres map on user's current location.
     * Called when Navigate button is tapped.
     */
    private void centreOnMyLocation() {
        if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
            mapView.getController().animateTo(myLocationOverlay.getMyLocation());
            mapView.getController().setZoom(18.0);
        } else {
            Toast.makeText(this,
                    "Location not found yet. Make sure GPS is enabled.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Draws pre-loaded safe paths from SQLite as green Polylines.
     * UPDATE coordinates in SafeRouteDatabase.seedSafePaths() with real campus paths.
     */
    private void drawSeedSafePaths() {
        List<SafePath> paths = localDb.getAllSafePaths();
        for (SafePath path : paths) {
            Polyline line = new Polyline();
            line.addPoint(new GeoPoint(path.getLatStart(), path.getLngStart()));
            line.addPoint(new GeoPoint(path.getLatEnd(), path.getLngEnd()));
            line.getOutlinePaint().setColor(Color.parseColor("#43A047")); // green
            line.getOutlinePaint().setStrokeWidth(12f);
            line.setTitle(path.getLabel());
            mapView.getOverlays().add(line);
        }
        mapView.invalidate();
    }

    private void addCampusMarkers() {
        addMarker(SECURITY_POST_A, "Security Post A",  "Available 24/7");
        addMarker(HEALTH_CENTER,   "Health Center",    "Mon–Sat 8am–8pm");
        addMarker(MAIN_GATE,       "Main Gate",        "Entry / Exit point");
    }

    private void addMarker(GeoPoint point, String title, String snippet) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setSnippet(snippet);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

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
                        if (latStart == null || lngStart == null
                                || latEnd == null || lngEnd == null) continue;

                        Polyline line = new Polyline();
                        line.addPoint(new GeoPoint(latStart, lngStart));
                        line.addPoint(new GeoPoint(latEnd, lngEnd));
                        line.getOutlinePaint().setColor(Color.parseColor("#1E88E5")); // blue
                        line.getOutlinePaint().setStrokeWidth(8f);
                        mapView.getOverlays().add(line);
                    } catch (Exception ignored) {}
                }
                mapView.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MapActivity.this,
                        "Failed to load community paths", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFirebaseIncidentZones() {
        firebaseDb.child("incidents").orderByChild("type").equalTo("ALERT")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot incidentSnap : snapshot.getChildren()) {
                            try {
                                Double lat  = incidentSnap.child("latitude").getValue(Double.class);
                                Double lng  = incidentSnap.child("longitude").getValue(Double.class);
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
        if (requestCode == REQUEST_LOCATION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            Toast.makeText(this,
                    "Location permission denied — cannot show your position",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (myLocationOverlay != null) myLocationOverlay.enableMyLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (myLocationOverlay != null) myLocationOverlay.disableMyLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDetach();
    }
}