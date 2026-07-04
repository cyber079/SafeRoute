package com.saferoute.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saferoute.app.R;
import com.saferoute.app.models.CheckIn;

import java.util.List;

/** CheckInAdapter — community check-ins list in CheckInActivity. Member 2. */
public class CheckInAdapter extends RecyclerView.Adapter<CheckInAdapter.ViewHolder> {

    private final List<CheckIn> checkIns;
    private final Context       context;

    public CheckInAdapter(List<CheckIn> checkIns, Context context) {
        this.checkIns = checkIns;
        this.context  = context;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_checkin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CheckIn c = checkIns.get(position);
        h.tvName.setText(c.getDisplayName() != null ? c.getDisplayName() : "Student");
        h.tvLocation.setText(c.getLocation());
        h.tvTime.setText(c.getRelativeTime());
    }

    @Override public int getItemCount() { return checkIns.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvTime;
        ViewHolder(View v) {
            super(v);
            tvName     = v.findViewById(R.id.tvCheckinName);
            tvLocation = v.findViewById(R.id.tvCheckinLocation);
            tvTime     = v.findViewById(R.id.tvCheckinTime);
        }
    }
}
