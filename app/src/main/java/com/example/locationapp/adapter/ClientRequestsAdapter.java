package com.example.locationapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationapp.R;
import com.example.locationapp.model.Request;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClientRequestsAdapter extends RecyclerView.Adapter<ClientRequestsAdapter.RequestViewHolder> {
    private List<Request> requestList;
    private Context context;
    private SimpleDateFormat dateFormat;

    public ClientRequestsAdapter(Context context, List<Request> requestList) {
        this.context = context;
        this.requestList = requestList;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_client_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        Request request = requestList.get(position);

        // Set property details
        if (request.getPropertyDetails() != null) {
            holder.tvPropertyTitle.setText(request.getPropertyDetails().getTitle());
            holder.tvPropertyPrice.setText(String.format(Locale.getDefault(), "%.2f €", request.getPropertyDetails().getPrice()));
        } else {
            holder.tvPropertyTitle.setText("Chargement...");
            holder.tvPropertyPrice.setText("...");
        }

        // Set agent name
        if (request.getAgentName() != null) {
            holder.tvAgentName.setText("Agent: " + request.getAgentName());
        } else {
            holder.tvAgentName.setText("Chargement...");
        }

        // Set message
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

        // Set status with appropriate color
        String status = request.getStatus();
        if (status != null) {
            switch (status.toUpperCase()) {
                case "PENDING":
                    holder.tvStatus.setText("En attente");
                    holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                    break;
                case "APPROVED":
                    holder.tvStatus.setText("Acceptée");
                    holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    break;
                case "REJECTED":
                    holder.tvStatus.setText("Refusée");
                    holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    break;
                default:
                    holder.tvStatus.setText("Statut inconnu");
                    holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            holder.tvStatus.setText("Statut inconnu");
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }

        // Set agent contact information
        if (request.getAgentId() != null) {
            // Show contact information only for approved requests
            if ("APPROVED".equals(status)) {
                holder.layoutAgentContact.setVisibility(View.VISIBLE);
                
                // Set agent phone
                if (request.getAgentPhone() != null && !request.getAgentPhone().isEmpty()) {
                    holder.tvAgentPhone.setText(request.getAgentPhone());
                    holder.tvAgentPhone.setVisibility(View.VISIBLE);
                } else {
                    holder.tvAgentPhone.setVisibility(View.GONE);
                }
                
                // Set agent email
                if (request.getAgentEmail() != null && !request.getAgentEmail().isEmpty()) {
                    holder.tvAgentEmail.setText(request.getAgentEmail());
                    holder.tvAgentEmail.setVisibility(View.VISIBLE);
                } else {
                    holder.tvAgentEmail.setVisibility(View.GONE);
                }
                
                // Set up contact button
                holder.btnContactAgent.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("mailto:" + request.getAgentEmail()));
                    context.startActivity(intent);
                });
            } else {
                holder.layoutAgentContact.setVisibility(View.GONE);
            }
        } else {
            holder.layoutAgentContact.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    public void updateRequests(List<Request> newRequests) {
        this.requestList = newRequests;
        notifyDataSetChanged();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvPropertyTitle;
        TextView tvPropertyPrice;
        TextView tvAgentName;
        TextView tvMessage;
        TextView tvDate;
        TextView tvStatus;
        TextView tvAgentPhone;
        TextView tvAgentEmail;
        Button btnContactAgent;
        LinearLayout layoutAgentContact;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPropertyTitle = itemView.findViewById(R.id.tvPropertyTitle);
            tvPropertyPrice = itemView.findViewById(R.id.tvPropertyPrice);
            tvAgentName = itemView.findViewById(R.id.tvAgentName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAgentPhone = itemView.findViewById(R.id.tvAgentPhone);
            tvAgentEmail = itemView.findViewById(R.id.tvAgentEmail);
            btnContactAgent = itemView.findViewById(R.id.btnContactAgent);
            layoutAgentContact = itemView.findViewById(R.id.layoutAgentContact);
        }
    }
} 