package com.example.locationapp.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.locationapp.model.Property;
import com.example.locationapp.R;
import com.example.locationapp.model.Request;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.ViewHolder> {
    private List<Property> properties;
    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public OffersAdapter(Context context, List<Property> properties) {
        this.context = context;
        this.properties = properties != null ? properties : new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void updateProperties(List<Property> newProperties) {
        this.properties = newProperties != null ? newProperties : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = properties.get(position);
        if (property == null) return;
        
        holder.tvTitle.setText(property.getTitle());
        holder.tvPrice.setText(String.format(Locale.getDefault(), "%,.0f DH", property.getPrice()));
        holder.tvAddress.setText(String.format("%s, %s", 
            property.getAddress(), 
            property.getCity() != null ? property.getCity() : ""));
        holder.tvRooms.setText(String.format(Locale.getDefault(), "%d chambres • %.0f m²", 
            property.getRooms(), property.getArea()));

        // Load image with proper error handling
        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
            Glide.with(context)
                .load(property.getImageUrls().get(0))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.ic_home)
                .error(R.drawable.ic_home)
                .into(holder.ivProperty);
        } else {
            holder.ivProperty.setImageResource(R.drawable.ic_home);
        }

        holder.btnRequest.setOnClickListener(v -> showRequestDialog(property));
    }

    private void showRequestDialog(Property property) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(context, "Veuillez vous connecter pour faire une demande", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_request, null);
        TextInputEditText etMessage = dialogView.findViewById(R.id.etMessage);

        new AlertDialog.Builder(context)
            .setTitle("Faire une demande")
            .setView(dialogView)
            .setPositiveButton("Envoyer", (dialog, which) -> {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendRequest(property, message);
                } else {
                    Toast.makeText(context, "Veuillez entrer un message", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Annuler", null)
            .show();
    }

    private void sendRequest(Property property, String message) {
        if (auth.getCurrentUser() == null || property == null) return;
        
        String clientId = auth.getCurrentUser().getUid();
        Request request = new Request(property.getId(), clientId, property.getAgentId(), message);

        db.collection("requests")
            .add(request)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(context, "Demande envoyée avec succès", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Erreur lors de l'envoi de la demande: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }

    @Override
    public int getItemCount() {
        return properties != null ? properties.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivProperty;
        final TextView tvTitle, tvPrice, tvAddress, tvRooms;
        final Button btnRequest;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProperty = itemView.findViewById(R.id.ivProperty);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvRooms = itemView.findViewById(R.id.tvRooms);
            btnRequest = itemView.findViewById(R.id.btnRequest);
        }
    }
} 