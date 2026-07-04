package com.saferoute.app.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.saferoute.app.R;
import com.saferoute.app.adapters.ResourceAdapter;
import com.saferoute.app.models.CampusResource;

import java.util.ArrayList;
import java.util.List;

/**
 * HelpActivity — Campus Resources screen (Screen 4 in mockup)
 * Shows list of campus emergency contacts with Call Now buttons.
 * Data is hardcoded (Taylor's campus contacts — update for real deployment).
 * Member 1 owns this activity.
 */
public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        RecyclerView rvResources = findViewById(R.id.rvResources);
        rvResources.setLayoutManager(new LinearLayoutManager(this));

        List<CampusResource> resources = buildResourceList();
        ResourceAdapter adapter = new ResourceAdapter(resources, this, phone -> {
            // Call Now button action
            Intent callIntent = new Intent(Intent.ACTION_DIAL,
                Uri.parse("tel:" + phone));
            startActivity(callIntent);
        });
        rvResources.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    /**
     * Taylor's University campus emergency contacts.
     * Update these with real numbers before submission.
     */
    private List<CampusResource> buildResourceList() {
        List<CampusResource> list = new ArrayList<>();
        list.add(new CampusResource(
            "Campus Security Office", "120m", "24/7",
            "+60 3-5629 5000", "#E53935", true));
        list.add(new CampusResource(
            "University Health Center", "340m", "Mon–Sat, 8am–8pm",
            "+60 3-5629 5001", "#43A047", true));
        list.add(new CampusResource(
            "Fire Safety & Emergency", "580m", "24/7",
            "999", "#FB8C00", true));
        list.add(new CampusResource(
            "Student Affairs Office", "210m", "Mon–Fri, 8am–5pm",
            "+60 3-5629 5002", "#7B1FA2", false));
        list.add(new CampusResource(
            "Guidance & Counseling", "450m", "Mon–Fri, 9am–5pm",
            "+60 3-5629 5003", "#1E88E5", false));
        list.add(new CampusResource(
            "Emergency Hotline", "—", "24/7",
            "999", "#E53935", true));
        return list;
    }
}
