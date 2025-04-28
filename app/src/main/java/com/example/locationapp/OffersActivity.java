package com.example.locationapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.locationapp.adapter.OffersAdapter;
import com.example.locationapp.model.Property;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OffersActivity extends AppCompatActivity {
    private static final String TAG = "OffersActivity";
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvNoOffers;
    private List<Property> propertyList;
    private List<Property> filteredList;
    private OffersAdapter adapter;

    private EditText etCity;
    private EditText etMinPrice;
    private EditText etMaxPrice;
    private EditText etRooms;
    private Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offers);
        Log.d(TAG, "onCreate: Starting OffersActivity");

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        Log.d(TAG, "onCreate: Firebase initialized");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Consulter les offres");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewOffers);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressBar = findViewById(R.id.progressBar);
        tvNoOffers = findViewById(R.id.tvNoOffers);
        etCity = findViewById(R.id.etCity);
        etMinPrice = findViewById(R.id.etMinPrice);
        etMaxPrice = findViewById(R.id.etMaxPrice);
        etRooms = findViewById(R.id.etRooms);
        btnSearch = findViewById(R.id.btnSearch);
        Log.d(TAG, "onCreate: Views initialized");

        // Setup RecyclerView
        propertyList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new OffersAdapter(this, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "onCreate: RecyclerView setup completed");

        // Setup SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this::loadProperties);

        // Setup search button
        btnSearch.setOnClickListener(v -> performSearch());

        // Load initial data
        loadProperties();
    }

    private void loadProperties() {
        showLoading(true);
        Log.d(TAG, "loadProperties: Starting to load properties from Firestore");

        // First, let's check if we can access Firestore
        db.collection("properties")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "loadProperties: Successfully connected to Firestore");
                    // Now load all properties
                    loadAllProperties();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadProperties: Failed to connect to Firestore", e);
                    Toast.makeText(this, "Erreur de connexion à la base de données", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void loadAllProperties() {
        Log.d(TAG, "loadAllProperties: Starting to load all properties");
        db.collection("properties")
                .get()  // Removed filters temporarily to see all documents
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "loadAllProperties: Successfully loaded " + queryDocumentSnapshots.size() + " properties");
                    propertyList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "loadAllProperties: No properties found in Firestore");
                        updateUI();
                        return;
                    }

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Log.d(TAG, "loadAllProperties: Raw document data: " + document.getData());
                            Property property = document.toObject(Property.class);
                            property.setId(document.getId());
                            propertyList.add(property);
                            Log.d(TAG, "loadAllProperties: Successfully converted document to Property object");
                            Log.d(TAG, String.format("loadAllProperties: Property details - ID: %s, Title: %s, City: %s, Price: %.2f, Rooms: %d, Available: %b",
                                    property.getId(),
                                    property.getTitle(),
                                    property.getCity(),
                                    property.getPrice(),
                                    property.getRooms(),
                                    property.isAvailable()));
                        } catch (Exception e) {
                            Log.e(TAG, "loadAllProperties: Error converting document to Property", e);
                            Log.e(TAG, "loadAllProperties: Document data: " + document.getData());
                            e.printStackTrace();
                        }
                    }
                    
                    // Initially show all properties
                    filteredList.clear();
                    filteredList.addAll(propertyList);
                    Log.d(TAG, "loadAllProperties: Added " + propertyList.size() + " properties to filtered list");
                    
                    adapter.notifyDataSetChanged();
                    updateUI();
                    swipeRefresh.setRefreshing(false);
                    Log.d(TAG, "loadAllProperties: Updated UI with " + filteredList.size() + " properties");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadAllProperties: Error loading properties", e);
                    Log.e(TAG, "loadAllProperties: Error details: " + e.getMessage());
                    e.printStackTrace();
                    Toast.makeText(this, "Erreur lors du chargement des offres: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    swipeRefresh.setRefreshing(false);
                });
    }

    private void performSearch() {
        String city = etCity.getText().toString().trim().toLowerCase();
        String minPriceStr = etMinPrice.getText().toString().trim();
        String maxPriceStr = etMaxPrice.getText().toString().trim();
        String roomsStr = etRooms.getText().toString().trim();

        Log.d(TAG, "performSearch: Starting search with filters - City: " + city + 
              ", Min Price: " + minPriceStr + 
              ", Max Price: " + maxPriceStr + 
              ", Rooms: " + roomsStr);

        filteredList.clear();

        for (Property property : propertyList) {
            boolean matches = true;

            // City filter
            if (!TextUtils.isEmpty(city) && !property.getCity().toLowerCase().contains(city)) {
                matches = false;
            }

            // Price filter
            if (!TextUtils.isEmpty(minPriceStr)) {
                double minPrice = Double.parseDouble(minPriceStr);
                if (property.getPrice() < minPrice) {
                    matches = false;
                }
            }
            if (!TextUtils.isEmpty(maxPriceStr)) {
                double maxPrice = Double.parseDouble(maxPriceStr);
                if (property.getPrice() > maxPrice) {
                    matches = false;
                }
            }

            // Rooms filter
            if (!TextUtils.isEmpty(roomsStr)) {
                int rooms = Integer.parseInt(roomsStr);
                if (property.getRooms() < rooms) {
                    matches = false;
                }
            }

            if (matches) {
                filteredList.add(property);
                Log.d(TAG, "performSearch: Property matches filters - " + property.getTitle());
            }
        }

        adapter.notifyDataSetChanged();
        updateUI();
        Log.d(TAG, "performSearch: Search completed. Found " + filteredList.size() + " matching properties");
        
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Aucune offre ne correspond à vos critères", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        Log.d(TAG, "showLoading: " + (show ? "Showing" : "Hiding") + " progress bar");
    }

    private void updateUI() {
        showLoading(false);
        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvNoOffers.setVisibility(View.VISIBLE);
            Log.d(TAG, "updateUI: No properties to display, showing empty state");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvNoOffers.setVisibility(View.GONE);
            Log.d(TAG, "updateUI: Displaying " + filteredList.size() + " properties");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}