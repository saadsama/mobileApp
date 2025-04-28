package com.example.locationapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AgentPropertiesActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentAgentId;

    private RecyclerView recyclerViewProperties;
    private AgentPropertyAdapter propertyAdapter;
    private List<Property> propertyList;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddProperty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_properties);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentAgentId = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : null;

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gérer ses offres");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        recyclerViewProperties = findViewById(R.id.recyclerViewProperties);
        progressBar = findViewById(R.id.progressBar);
        fabAddProperty = findViewById(R.id.fabAddProperty);

        // Set up RecyclerView
        recyclerViewProperties.setLayoutManager(new LinearLayoutManager(this));
        propertyList = new ArrayList<>();
        propertyAdapter = new AgentPropertyAdapter(this, propertyList, this::onPropertyClick);
        recyclerViewProperties.setAdapter(propertyAdapter);

        // Set up FAB
        fabAddProperty.setOnClickListener(v -> {
            Intent intent = new Intent(AgentPropertiesActivity.this, PropertyEditorActivity.class);
            startActivity(intent);
        });

        // Load properties
        loadProperties();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProperties(); // Reload properties when returning to this screen
    }

    private void loadProperties() {
        if (currentAgentId == null) {
            Toast.makeText(this, "Vous devez être connecté pour accéder à vos offres", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("properties")
                .whereEqualTo("agentId", currentAgentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    propertyList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Property property = document.toObject(Property.class);
                        property.setId(document.getId());
                        propertyList.add(property);
                    }

                    propertyAdapter.notifyDataSetChanged();

                    // Show empty state if no properties
                    if (propertyList.isEmpty()) {
                        findViewById(R.id.emptyStateLayout).setVisibility(View.VISIBLE);
                        recyclerViewProperties.setVisibility(View.GONE);
                    } else {
                        findViewById(R.id.emptyStateLayout).setVisibility(View.GONE);
                        recyclerViewProperties.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AgentPropertiesActivity.this,
                            "Erreur lors du chargement des offres: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void onPropertyClick(Property property, int position) {
        // Show property options (edit/delete)
        Intent intent = new Intent(AgentPropertiesActivity.this, PropertyEditorActivity.class);
        intent.putExtra("PROPERTY_ID", property.getId());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_agent_properties, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            loadProperties();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}