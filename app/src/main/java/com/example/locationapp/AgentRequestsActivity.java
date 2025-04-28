package com.example.locationapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.locationapp.adapter.AgentRequestsAdapter;
import com.example.locationapp.model.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AgentRequestsActivity extends AppCompatActivity {
    private static final String TAG = "AgentRequestsActivity";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView rvRequests;
    private ProgressBar progressBar;
    private TextView tvNoRequests;
    private AgentRequestsAdapter adapter;
    private List<Request> requestList;
    private ListenerRegistration propertyListener;
    private ListenerRegistration clientListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_requests);

        try {
            // Initialize Firebase
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();

            // Check if user is logged in
            if (auth.getCurrentUser() == null) {
                Log.e(TAG, "User not logged in");
                Toast.makeText(this, "Veuillez vous connecter pour voir vos demandes", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize views
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Demandes reçues");

            rvRequests = findViewById(R.id.recyclerViewRequests);
        progressBar = findViewById(R.id.progressBar);
        tvNoRequests = findViewById(R.id.tvNoRequests);

            // Setup RecyclerView
        requestList = new ArrayList<>();
            adapter = new AgentRequestsAdapter(this, requestList);
            rvRequests.setLayoutManager(new LinearLayoutManager(this));
            rvRequests.setAdapter(adapter);

        // Load requests
        loadRequests();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRequests() {
        String agentId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Loading requests for agent: " + agentId);

        progressBar.setVisibility(View.VISIBLE);
        tvNoRequests.setVisibility(View.GONE);

        // Query requests by agentId only, without ordering
        db.collection("requests")
            .whereEqualTo("agentId", agentId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Request> requests = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot) {
                    Request request = doc.toObject(Request.class);
                    if (request != null) {
                        request.setId(doc.getId());
                        requests.add(request);
                    }
                }

                // Sort requests by createdAt locally
                Collections.sort(requests, (r1, r2) -> {
                    if (r2.getCreatedAt() == r1.getCreatedAt()) return 0;
                    return r2.getCreatedAt() > r1.getCreatedAt() ? 1 : -1;
                });

                requestList.clear();
                requestList.addAll(requests);
                adapter.updateRequests(requestList);
                loadAdditionalDetails();
                progressBar.setVisibility(View.GONE);

                if (requests.isEmpty()) {
                    tvNoRequests.setVisibility(View.VISIBLE);
                } else {
                    tvNoRequests.setVisibility(View.GONE);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error loading requests: " + e.getMessage());
                Toast.makeText(this, "Erreur lors du chargement des demandes", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                tvNoRequests.setVisibility(View.VISIBLE);
            });
    }

    private void loadAdditionalDetails() {
        try {
            for (Request request : requestList) {
            // Load property details
                if (request.getPropertyId() != null) {
                    propertyListener = db.collection("properties").document(request.getPropertyId())
                        .addSnapshotListener((documentSnapshot, e) -> {
                            if (e != null) {
                                Log.e(TAG, "Error listening to property changes: " + e.getMessage());
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                try {
                                    request.setPropertyDetails(documentSnapshot.toObject(Property.class));
                                    Log.d(TAG, "Loaded property details for request " + request.getId());
                                    adapter.notifyDataSetChanged();
                                } catch (Exception ex) {
                                    Log.e(TAG, "Error processing property details: " + ex.getMessage());
                                }
                        }
                    });
                }

            // Load client details
                if (request.getClientId() != null) {
                    clientListener = db.collection("users").document(request.getClientId())
                        .addSnapshotListener((documentSnapshot, e) -> {
                            if (e != null) {
                                Log.e(TAG, "Error listening to client changes: " + e.getMessage());
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                try {
                                    request.setClientName(documentSnapshot.getString("name"));
                                    Log.d(TAG, "Loaded client details for request " + request.getId());
                                    adapter.notifyDataSetChanged();
                                } catch (Exception ex) {
                                    Log.e(TAG, "Error processing client details: " + ex.getMessage());
                                }
                            }
                        });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadAdditionalDetails: " + e.getMessage());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listeners when activity is destroyed
        if (propertyListener != null) {
            propertyListener.remove();
        }
        if (clientListener != null) {
            clientListener.remove();
        }
    }
}