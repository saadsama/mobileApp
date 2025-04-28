package com.example.locationapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AgentPropertyAdapter extends RecyclerView.Adapter<AgentPropertyAdapter.PropertyViewHolder> {
    private Context context;
    private List<Property> propertyList;
    private OnPropertyClickListener listener;
    private FirebaseFirestore db;

    public interface OnPropertyClickListener {
        void onPropertyClick(Property property, int position);
    }

    public AgentPropertyAdapter(Context context, List<Property> propertyList, OnPropertyClickListener listener) {
        this.context = context;
        this.propertyList = propertyList;
        this.listener = listener;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_agent_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = propertyList.get(position);

        // Set property details
        holder.tvTitle.setText(property.getTitle());
        holder.tvPrice.setText(formatPrice(property.getPrice()));
        holder.tvLocation.setText(property.getCity());
        holder.tvDetails.setText(formatDetails(property));
        holder.tvStatus.setText(property.isAvailable() ? "Disponible" : "Non disponible");

        // Set image
        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
            Picasso.get().load(property.getImageUrls().get(0))
                    .placeholder(R.drawable.ic_home)
                    .into(holder.ivProperty);
        } else {
            holder.ivProperty.setImageResource(R.drawable.ic_home);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPropertyClick(property, position);
            }
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> deleteProperty(property, position));

        // Toggle availability button
        holder.btnToggleAvailability.setOnClickListener(v -> toggleAvailability(property, position));
    }

    private void deleteProperty(Property property, int position) {
        db.collection("properties").document(property.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    propertyList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, propertyList.size());
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void toggleAvailability(Property property, int position) {
        // Toggle availability status
        boolean newStatus = !property.isAvailable();

        db.collection("properties").document(property.getId())
                .update("available", newStatus)
                .addOnSuccessListener(aVoid -> {
                    property.setAvailable(newStatus);
                    notifyItemChanged(position);
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        return formatter.format(price);
    }

    private String formatDetails(Property property) {
        return property.getBedrooms() + " Ch | " +
                property.getBathrooms() + " SdB | " +
                property.getArea() + " m²";
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProperty;
        TextView tvTitle, tvPrice, tvLocation, tvDetails, tvStatus;
        ImageButton btnDelete, btnToggleAvailability;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProperty = itemView.findViewById(R.id.ivProperty);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnToggleAvailability = itemView.findViewById(R.id.btnToggleAvailability);
        }
    }
}
