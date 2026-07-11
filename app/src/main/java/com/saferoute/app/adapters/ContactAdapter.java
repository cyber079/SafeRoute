package com.saferoute.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saferoute.app.R;
import com.saferoute.app.models.Contact;

import java.util.List;

/**
 * ContactAdapter — used in SosActivity for emergency contacts list.
 * FIX: Delete button is now wired up via OnDeleteListener callback.
 * Member 4.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    // Callback interface — SosActivity handles the actual delete logic
    public interface OnDeleteListener {
        void onDelete(Contact contact);
    }

    private final List<Contact> contacts;
    private final Context context;
    private final OnDeleteListener deleteListener;

    public ContactAdapter(List<Contact> contacts, Context context, OnDeleteListener deleteListener) {
        this.contacts       = contacts;
        this.context        = context;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Contact c = contacts.get(position);
        h.tvName.setText(c.getName());
        h.tvPhone.setText(c.getPhone());

        // FIX: Wire up delete button — calls back to SosActivity which handles
        // the confirmation dialog and SQLite deletion
        h.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(c);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvName, tvPhone;
        ImageButton btnDelete;

        ViewHolder(View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvContactName);
            tvPhone   = v.findViewById(R.id.tvContactPhone);
            btnDelete = v.findViewById(R.id.btnDeleteContact);
        }
    }
}