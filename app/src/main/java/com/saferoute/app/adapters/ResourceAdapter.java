package com.saferoute.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.saferoute.app.R;
import com.saferoute.app.models.CampusResource;

import java.util.List;

/** ResourceAdapter — campus resources list in HelpActivity. Member 1. */
public class ResourceAdapter extends RecyclerView.Adapter<ResourceAdapter.ViewHolder> {

    public interface OnCallListener { void onCall(String phone); }

    private final List<CampusResource> resources;
    private final Context              context;
    private final OnCallListener       listener;

    public ResourceAdapter(List<CampusResource> resources, Context context, OnCallListener listener) {
        this.resources = resources;
        this.context   = context;
        this.listener  = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_resource, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CampusResource r = resources.get(position);

        h.tvName.setText(r.getName());
        h.tvDistance.setText("📍 " + r.getDistance());
        h.tvHours.setText("🕐 " + r.getHours());
        h.tvPhone.setText(r.getPhone());

        h.tvStatus.setText(r.isOpen() ? "● OPEN" : "○ CLOSED");
        h.tvStatus.setTextColor(r.isOpen()
            ? Color.parseColor("#43A047")
            : Color.parseColor("#9CA3AF"));

        // Colour the Call Now button to match the resource accent colour
        try {
            h.btnCall.setBackgroundColor(Color.parseColor(r.getAccentColor()));
        } catch (Exception ignored) {}

        h.btnCall.setOnClickListener(v -> listener.onCall(r.getPhone()));
    }

    @Override public int getItemCount() { return resources.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView       tvName, tvDistance, tvHours, tvPhone, tvStatus;
        MaterialButton btnCall;
        ViewHolder(View v) {
            super(v);
            tvName     = v.findViewById(R.id.tvResourceName);
            tvDistance = v.findViewById(R.id.tvResourceDistance);
            tvHours    = v.findViewById(R.id.tvResourceHours);
            tvPhone    = v.findViewById(R.id.tvResourcePhone);
            tvStatus   = v.findViewById(R.id.tvResourceStatus);
            btnCall    = v.findViewById(R.id.btnCallNow);
        }
    }
}
