package com.example.locationapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationapp.model.Request;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private final List<Request> requests;
    private final SimpleDateFormat dateFormat;
    private final FirebaseFirestore db;

    public RequestAdapter(List<Request> requests) {
        this.requests = requests;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_client_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requests.get(position);
        
        // Load property details
        db.collection("properties").document(request.getPropertyId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String propertyTitle = documentSnapshot.getString("title");
                        String propertyAddress = documentSnapshot.getString("address");
                        double propertyPrice = documentSnapshot.getDouble("price");

                        holder.tvPropertyTitle.setText(propertyTitle);
                        holder.tvPropertyAddress.setText(propertyAddress);
                        holder.tvPropertyPrice.setText(String.format(Locale.getDefault(), "%.2f €", propertyPrice));
                    }
                });

        // Set request status
        holder.tvStatus.setText(request.getStatus());
        
        // Format and display the date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        if (request.getCreatedAt() > 0) {
            holder.tvDate.setText(sdf.format(new Date(request.getCreatedAt())));
        } else {
            holder.tvDate.setText("Date non disponible");
        }

        // Set request message
        holder.tvMessage.setText(request.getMessage());
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvPropertyTitle;
        TextView tvPropertyAddress;
        TextView tvPropertyPrice;
        TextView tvStatus;
        TextView tvDate;
        TextView tvMessage;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPropertyTitle = itemView.findViewById(R.id.tvPropertyTitle);
            tvPropertyAddress = itemView.findViewById(R.id.tvPropertyAddress);
            tvPropertyPrice = itemView.findViewById(R.id.tvPropertyPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
} 