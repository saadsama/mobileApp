package com.example.locationapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PropertyDetailActivity extends AppCompatActivity {
    private String propertyId;
    private Property property;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private boolean isFavorite = false;

    private ViewPager2 viewPagerImages;
    private TabLayout tabLayoutIndicator;
    private TextView tvTitle, tvPrice, tvLocation, tvDescription;
    private TextView tvBedrooms, tvBathrooms, tvArea, tvType;
    private TextView tvAvailability, tvListingDate;
    private Button btnContact, btnRequest;
    private ImageView ivFavorite;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_detail);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            currentUserId = mAuth.getCurrentUser().getUid();
        }

        // Get property ID from intent
        propertyId = getIntent().getStringExtra("PROPERTY_ID");
        if (propertyId == null) {
            Toast.makeText(this, "Erreur: Propriété non trouvée", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Détail de la propriété");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initialize views
        initViews();

        // Load property details
        loadPropertyDetails();

        // Check if property is in favorites
        checkFavoriteStatus();

        // Set up favorite button
        ivFavorite.setOnClickListener(v -> toggleFavorite());

        // Set up contact button
        btnContact.setOnClickListener(v -> contactAgent());

        // Set up request button
        btnRequest.setOnClickListener(v -> requestProperty());
    }

    private void initViews() {
        viewPagerImages = findViewById(R.id.viewPagerImages);
        tabLayoutIndicator = findViewById(R.id.tabLayoutIndicator);
        tvTitle = findViewById(R.id.tvTitle);
        tvPrice = findViewById(R.id.tvPrice);
        tvLocation = findViewById(R.id.tvLocation);
        tvDescription = findViewById(R.id.tvDescription);
        tvBedrooms = findViewById(R.id.tvBedrooms);
        tvBathrooms = findViewById(R.id.tvBathrooms);
        tvArea = findViewById(R.id.tvArea);
        tvType = findViewById(R.id.tvType);
        tvAvailability = findViewById(R.id.tvAvailability);
        tvListingDate = findViewById(R.id.tvListingDate);
        btnContact = findViewById(R.id.btnContact);
        btnRequest = findViewById(R.id.btnRequest);
        ivFavorite = findViewById(R.id.ivFavorite);
        progressBar = findViewById(R.id.progressBar);
    }

    private void loadPropertyDetails() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("properties").document(propertyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        property = documentSnapshot.toObject(Property.class);
                        property.setId(documentSnapshot.getId());
                        updateUI();
                    } else {
                        Toast.makeText(this, "Propriété non trouvée", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateUI() {
        // Set property details
        tvTitle.setText(property.getTitle());
        tvPrice.setText(formatPrice(property.getPrice()));
        tvLocation.setText(property.getCity() + ", " + property.getAddress());
        tvDescription.setText(property.getDescription());
        tvBedrooms.setText(String.valueOf(property.getBedrooms()));
        tvBathrooms.setText(String.valueOf(property.getBathrooms()));
        tvArea.setText(property.getArea() + " m²");
        tvType.setText(property.getPropertyType());
        tvAvailability.setText(property.isAvailable() ? "Disponible" : "Non disponible");
        tvListingDate.setText(formatDate(property.getCreatedAt()));

        // Set up images slider
        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
            PropertyImageAdapter adapter = new PropertyImageAdapter(this, property.getImageUrls());
            viewPagerImages.setAdapter(adapter);

            // Set up tab layout indicator
            new TabLayoutMediator(tabLayoutIndicator, viewPagerImages,
                    (tab, position) -> {}).attach();
        } else {
            // If no images, show placeholder
            viewPagerImages.setVisibility(View.GONE);
            tabLayoutIndicator.setVisibility(View.GONE);
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        return formatter.format(price);
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void checkFavoriteStatus() {
        if (currentUserId != null) {
            db.collection("favorites")
                    .whereEqualTo("userId", currentUserId)
                    .whereEqualTo("propertyId", propertyId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        isFavorite = !queryDocumentSnapshots.isEmpty();
                        updateFavoriteIcon();
                    });
        }
    }

    private void updateFavoriteIcon() {
        ivFavorite.setImageResource(isFavorite ?
                R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
    }

    private void toggleFavorite() {
        if (currentUserId == null) {
            Toast.makeText(this, "Veuillez vous connecter pour ajouter aux favoris",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (isFavorite) {
            // Remove from favorites
            db.collection("favorites")
                    .whereEqualTo("userId", currentUserId)
                    .whereEqualTo("propertyId", propertyId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            document.getReference().delete();
                        }
                        isFavorite = false;
                        updateFavoriteIcon();
                        Toast.makeText(PropertyDetailActivity.this,
                                "Retiré des favoris", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Add to favorites
            Map<String, Object> favorite = new HashMap<>();
            favorite.put("userId", currentUserId);
            favorite.put("propertyId", propertyId);
            favorite.put("timestamp", System.currentTimeMillis());

            db.collection("favorites")
                    .add(favorite)
                    .addOnSuccessListener(documentReference -> {
                        isFavorite = true;
                        updateFavoriteIcon();
                        Toast.makeText(PropertyDetailActivity.this,
                                "Ajouté aux favoris", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(PropertyDetailActivity.this,
                                    "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void contactAgent() {
        if (property != null) {
            db.collection("agents").document(property.getAgentId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String agentName = documentSnapshot.getString("firstName") + " " +
                                    documentSnapshot.getString("lastName");
                            String agentEmail = documentSnapshot.getString("email");
                            String agentPhone = documentSnapshot.getString("phoneNumber");

                            Intent intent = new Intent(PropertyDetailActivity.this,
                                    ContactAgentActivity.class);
                            intent.putExtra("AGENT_NAME", agentName);
                            intent.putExtra("AGENT_EMAIL", agentEmail);
                            intent.putExtra("AGENT_PHONE", agentPhone);
                            intent.putExtra("PROPERTY_TITLE", property.getTitle());
                            startActivity(intent);
                        } else {
                            Toast.makeText(PropertyDetailActivity.this,
                                    "Information agent non disponible", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(PropertyDetailActivity.this,
                                    "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void requestProperty() {
        if (currentUserId == null) {
            Toast.makeText(this, "Veuillez vous connecter pour faire une demande",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (property != null) {
            Map<String, Object> request = new HashMap<>();
            request.put("clientId", currentUserId);
            request.put("propertyId", propertyId);
            request.put("agentId", property.getAgentId());
            request.put("status", "pending");
            request.put("timestamp", System.currentTimeMillis());

            db.collection("requests")
                    .add(request)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(PropertyDetailActivity.this,
                                    "Demande envoyée avec succès", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(PropertyDetailActivity.this,
                                    "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_property_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_share) {
            shareProperty();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareProperty() {
        if (property != null) {
            String shareText = property.getTitle() + "\n" +
                    formatPrice(property.getPrice()) + "\n" +
                    property.getCity() + ", " + property.getAddress() + "\n\n" +
                    "Voir cette propriété sur l'application LocationApp";

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Partager via"));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}