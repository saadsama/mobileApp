package com.example.locationapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationapp.R;
import com.example.locationapp.model.Request;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class AgentRequestsAdapter extends RecyclerView.Adapter<AgentRequestsAdapter.RequestViewHolder> {
    private List<Request> requestList;
    private Context context;
    private FirebaseFirestore db;

    public AgentRequestsAdapter(Context context, List<Request> requestList) {
        this.context = context;
        this.requestList = requestList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requestList.get(position);
        System.out.println("AgentRequestsAdapter - Binding request at position " + position + 
            "\nRequest ID: " + request.getId() +
            "\nStatus: " + request.getStatus() +
            "\nClient Name: " + request.getClientName() +
            "\nProperty Title: " + (request.getPropertyDetails() != null ? request.getPropertyDetails().getTitle() : "null"));

        // Set property details
        if (request.getPropertyDetails() != null) {
            holder.tvPropertyTitle.setText(request.getPropertyDetails().getTitle());
            holder.tvPropertyPrice.setText(String.format("%.2f €", request.getPropertyDetails().getPrice()));
        } else {
            holder.tvPropertyTitle.setText("Chargement...");
            holder.tvPropertyPrice.setText("...");
        }

        // Set client details
        if (request.getClientName() != null) {
            holder.tvClientName.setText(request.getClientName());
        } else {
            holder.tvClientName.setText("Chargement...");
        }

        // Set request message
        if (request.getMessage() != null && !request.getMessage().isEmpty()) {
            holder.tvMessage.setText(request.getMessage());
            holder.tvMessage.setVisibility(View.VISIBLE);
        } else {
            holder.tvMessage.setVisibility(View.GONE);
        }

        // Set request date
        if (request.getCreatedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateStr = sdf.format(new Date(request.getCreatedAt()));
            holder.tvDate.setText(dateStr);
        } else {
            holder.tvDate.setText("Date inconnue");
        }

        // Set status and buttons visibility
        String status = request.getStatus();
        if (status != null) status = status.toUpperCase();
        else status = "";
        System.out.println("AgentRequestsAdapter - Setting status: " + status);
        
        switch (status) {
            case "APPROVED":
                holder.tvStatus.setText("Acceptée");
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
                break;
            case "REJECTED":
                holder.tvStatus.setText("Refusée");
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
                break;
            case "PENDING":
                holder.tvStatus.setText("En attente");
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                break;
            default:
                holder.tvStatus.setText("Statut inconnu");
                holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                holder.btnAccept.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
        }

        // Set button click listeners
        holder.btnAccept.setOnClickListener(v -> {
            System.out.println("AgentRequestsAdapter - Accepting request: " + request.getId());
            updateRequestStatus(request.getId(), "APPROVED");
        });

        holder.btnReject.setOnClickListener(v -> {
            System.out.println("AgentRequestsAdapter - Rejecting request: " + request.getId());
            updateRequestStatus(request.getId(), "REJECTED");
        });
    }

    @Override
    public int getItemCount() {
        System.out.println("AgentRequestsAdapter - getItemCount: " + requestList.size());
        return requestList.size();
    }

    private void updateRequestStatus(String requestId, String newStatus) {
        // Harmonize status values with client expectations
        if ("accepted".equalsIgnoreCase(newStatus)) newStatus = "APPROVED";
        if ("rejected".equalsIgnoreCase(newStatus)) newStatus = "REJECTED";
        System.out.println("AgentRequestsAdapter - Updating request " + requestId + " to status: " + newStatus);
        db.collection("requests").document(requestId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("AgentRequestsAdapter - Request status updated successfully");
                    Toast.makeText(context, "Statut de la demande mis à jour", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    System.out.println("AgentRequestsAdapter - Error updating request status: " + e.getMessage());
                    Toast.makeText(context, "Erreur lors de la mise à jour du statut", Toast.LENGTH_SHORT).show();
                });
    }

    public void updateRequests(List<Request> newRequests) {
        System.out.println("AgentRequestsAdapter - Updating requests list. Old size: " + 
            requestList.size() + ", New size: " + newRequests.size());
        this.requestList = newRequests;
        notifyDataSetChanged();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvPropertyTitle;
        TextView tvPropertyPrice;
        TextView tvClientName;
        TextView tvMessage;
        TextView tvDate;
        TextView tvStatus;
        Button btnAccept;
        Button btnReject;

        RequestViewHolder(@NonNull View itemView) {
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