package com.example.locationapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationapp.model.Request;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AgentRequestsAdapter extends RecyclerView.Adapter<AgentRequestsAdapter.ViewHolder> {
    private Context context;
    private List<Request> requests;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat;

    public AgentRequestsAdapter(Context context, List<Request> requests) {
        this.context = context;
        this.requests = requests;
        this.db = FirebaseFirestore.getInstance();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Request request = requests.get(position);

        // Set property details
        if (request.getPropertyDetails() != null) {
            holder.tvPropertyTitle.setText(request.getPropertyDetails().getTitle());
            holder.tvPropertyPrice.setText(String.format(Locale.getDefault(), "%.2f €", request.getPropertyDetails().getPrice()));
        } else {
            holder.tvPropertyTitle.setText("Chargement...");
            holder.tvPropertyPrice.setText("...");
        }

        // Set client name
        if (request.getClientName() != null) {
            holder.tvClientName.setText("Client: " + request.getClientName());
        } else {
            holder.tvClientName.setText("Chargement...");
        }

        // Set message
        if (request.getMessage() != null && !request.getMessage().isEmpty()) {
            holder.tvMessage.setText(request.getMessage());
            holder.tvMessage.setVisibility(View.VISIBLE);
        } else {
            holder.tvMessage.setVisibility(View.GONE);
        }

        // Set date
        if (request.getCreatedAt() > 0) {
            holder.tvDate.setText(dateFormat.format(new Date(request.getCreatedAt())));
        } else {
            holder.tvDate.setText("Date inconnue");
        }

        // Set status and buttons visibility
        if (request.isPending()) {
            holder.tvStatus.setText("En attente");
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        } else {
            holder.tvStatus.setText("Acceptée");
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
        }

        // Set button listeners
        holder.btnAccept.setOnClickListener(v -> acceptRequest(request));
        holder.btnReject.setOnClickListener(v -> rejectRequest(request));
    }

    private void acceptRequest(Request request) {
        request.setPending(false);
        updateRequestStatus(request, "Demande acceptée");
    }

    private void rejectRequest(Request request) {
        // Pour le moment, on supprime simplement la demande si elle est rejetée
        db.collection("requests").document(request.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                requests.remove(request);
                notifyDataSetChanged();
                Toast.makeText(context, "Demande rejetée", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(context, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateRequestStatus(Request request, String message) {
        db.collection("requests").document(request.getId())
            .update("pending", request.isPending())
            .addOnSuccessListener(aVoid -> {
                notifyDataSetChanged();
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(context, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateRequests(List<Request> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPropertyTitle;
        TextView tvPropertyPrice;
        TextView tvClientName;
        TextView tvMessage;
        TextView tvDate;
        TextView tvStatus;
        Button btnAccept;
        Button btnReject;

        ViewHolder(View itemView) {
            super(itemView);
            tvPropertyTitle = itemView.findViewById(R.id.tvPropertyTitle);
            tvPropertyPrice = itemView.findViewById(R.id.tvPropertyPrice);
            tvClientName = itemView.findViewById(R.id.tvClientName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}