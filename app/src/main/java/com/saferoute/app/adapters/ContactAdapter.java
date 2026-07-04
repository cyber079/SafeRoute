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

/** ContactAdapter — used in SosActivity for emergency contacts list. Member 4. */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private final List<Contact> contacts;
    private final Context       context;

    public ContactAdapter(List<Contact> contacts, Context context) {
        this.contacts = contacts;
        this.context  = context;
    }

    @NonNull @Override
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
    }

    @Override public int getItemCount() { return contacts.size(); }

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
