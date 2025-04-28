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

import com.example.locationapp.adapter.ClientRequestsAdapter;
import com.example.locationapp.model.Request;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientRequestsActivity extends AppCompatActivity {
    private static final String TAG = "ClientRequestsActivity";
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private RecyclerView rvRequests;
    private ProgressBar progressBar;
    private TextView tvNoRequests;
    private ClientRequestsAdapter adapter;
    private List<Request> requestList;
    private List<ListenerRegistration> listeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_requests);

        try {
            // Initialize Firebase
            db = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            listeners = new ArrayList<>();

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
            getSupportActionBar().setTitle("Mes demandes");

            rvRequests = findViewById(R.id.rvRequests);
            progressBar = findViewById(R.id.progressBar);
            tvNoRequests = findViewById(R.id.tvNoRequests);

            // Setup RecyclerView
            requestList = new ArrayList<>();
            adapter = new ClientRequestsAdapter(this, requestList);
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
        String clientId = auth.getCurrentUser().getUid();
        Log.d(TAG, "Loading requests for client: " + clientId);

        progressBar.setVisibility(View.VISIBLE);
        tvNoRequests.setVisibility(View.GONE);

        // Add real-time listener for requests
        ListenerRegistration requestListener = db.collection("requests")
                .whereEqualTo("clientId", clientId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error listening for requests: " + error.getMessage());
                        progressBar.setVisibility(View.GONE);
                        tvNoRequests.setVisibility(View.VISIBLE);
                        Toast.makeText(this, 
                            "Erreur lors du chargement des demandes: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (value != null) {
                        List<Request> requests = new ArrayList<>();
                        for (DocumentSnapshot doc : value) {
                            Request request = doc.toObject(Request.class);
                            if (request != null) {
                                request.setId(doc.getId());
                                requests.add(request);
                                Log.d(TAG, "Request loaded: " + doc.getId() + 
                                    ", Property: " + request.getPropertyId() + 
                                    ", Status: " + request.getStatus());
                            }
                        }

                        requestList.clear();
                        requestList.addAll(requests);
                        adapter.updateRequests(requestList);
                        loadAdditionalDetails();
                        progressBar.setVisibility(View.GONE);
                        
                        if (requests.isEmpty()) {
                            tvNoRequests.setVisibility(View.VISIBLE);
                            tvNoRequests.setText("Vous n'avez pas encore envoyé de demandes");
                        } else {
                            tvNoRequests.setVisibility(View.GONE);
                        }
                    }
                });

        listeners.add(requestListener);
    }

    private void loadAdditionalDetails() {
        try {
            for (Request request : requestList) {
                // Load property details
                if (request.getPropertyId() != null) {
                    ListenerRegistration propertyListener = db.collection("properties")
                        .document(request.getPropertyId())
                        .addSnapshotListener((documentSnapshot, e) -> {
                            if (e != null) {
                                Log.e(TAG, "Error listening to property: " + e.getMessage());
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                Property property = documentSnapshot.toObject(Property.class);
                                if (property != null) {
                                    request.setPropertyDetails(property);
                                    request.setPropertyTitle(property.getTitle());
                                    request.setPropertyAddress(property.getAddress());
                                    request.setPropertyPrice(property.getPrice());
                                    if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
                                        request.setPropertyImageUrl(property.getImageUrls().get(0));
                                    }
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        });
                    listeners.add(propertyListener);
                }

                // Load agent details
                if (request.getAgentId() != null) {
                    ListenerRegistration agentListener = db.collection("agents")
                        .document(request.getAgentId())
                        .addSnapshotListener((documentSnapshot, e) -> {
                            if (e != null) {
                                Log.e(TAG, "Error listening to agent: " + e.getMessage());
                                return;
                            }

                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                // Set agent name
                                String firstName = documentSnapshot.getString("firstName");
                                String lastName = documentSnapshot.getString("lastName");
                                String agentName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                                request.setAgentName(agentName.trim());
                                
                                // Set agent contact information
                                request.setAgentEmail(documentSnapshot.getString("email"));
                                request.setAgentPhone(documentSnapshot.getString("phoneNumber"));
                                
                                Log.d(TAG, "Loaded agent details for request " + request.getId() + 
                                    ": Name=" + agentName + 
                                    ", Email=" + request.getAgentEmail() + 
                                    ", Phone=" + request.getAgentPhone());
                                
                                adapter.notifyDataSetChanged();
                            }
                        });
                    listeners.add(agentListener);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadAdditionalDetails: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove all listeners when activity is destroyed
        for (ListenerRegistration listener : listeners) {
            listener.remove();
        }
        listeners.clear();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 