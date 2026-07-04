package com.saferoute.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.saferoute.app.R;
import com.saferoute.app.models.Alert;

import java.util.List;

/**
 * AlertAdapter — RecyclerView adapter for safety alerts feed.
 * Renders colour-coded cards matching the mockup:
 *   ALERT    → red background
 *   INFO     → blue background
 *   SAFE     → green background
 *   (default)→ yellow/unverified
 * Member 2 owns this adapter.
 */
public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {

    private final List<Alert> alerts;
    private final Context     context;

    public AlertAdapter(List<Alert> alerts, Context context) {
        this.alerts  = alerts;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_alert, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Alert alert = alerts.get(position);

        holder.tvTitle.setText(alert.getDescription());
        holder.tvLocation.setText(alert.getLocation());
        holder.tvTime.setText(alert.getRelativeTime());

        // Colour-code by type matching the mockup
        switch (alert.getType() != null ? alert.getType() : "INFO") {
            case "ALERT":
                holder.tvType.setText("ALERT");
                holder.tvType.setBackgroundColor(Color.parseColor("#FFCDD2"));
                holder.tvType.setTextColor(Color.parseColor("#C62828"));
                holder.card.setCardBackgroundColor(Color.parseColor("#FFF8F8"));
                holder.iconBg.setBackgroundColor(Color.parseColor("#FFEBEE"));
                break;
            case "SAFE":
                holder.tvType.setText("SAFE");
                holder.tvType.setBackgroundColor(Color.parseColor("#C8E6C9"));
                holder.tvType.setTextColor(Color.parseColor("#2E7D32"));
                holder.card.setCardBackgroundColor(Color.parseColor("#F1F8E9"));
                holder.iconBg.setBackgroundColor(Color.parseColor("#E8F5E9"));
                break;
            case "INFO":
            default:
                holder.tvType.setText("INFO");
                holder.tvType.setBackgroundColor(Color.parseColor("#BBDEFB"));
                holder.tvType.setTextColor(Color.parseColor("#1565C0"));
                holder.card.setCardBackgroundColor(Color.parseColor("#F8FBFF"));
                holder.iconBg.setBackgroundColor(Color.parseColor("#E3F2FD"));
                break;
        }

        // Show UNVERIFIED badge if not verified
        if (!alert.isVerified() && "ALERT".equals(alert.getType())) {
            holder.tvUnverified.setVisibility(View.VISIBLE);
        } else {
            holder.tvUnverified.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return alerts.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        View             iconBg;
        TextView         tvType, tvUnverified, tvTitle, tvLocation, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            card          = itemView.findViewById(R.id.alertCard);
            iconBg        = itemView.findViewById(R.id.alertIconBg);
            tvType        = itemView.findViewById(R.id.tvAlertType);
            tvUnverified  = itemView.findViewById(R.id.tvUnverified);
            tvTitle       = itemView.findViewById(R.id.tvAlertTitle);
            tvLocation    = itemView.findViewById(R.id.tvAlertLocation);
            tvTime        = itemView.findViewById(R.id.tvAlertTime);
        }
    }
}
