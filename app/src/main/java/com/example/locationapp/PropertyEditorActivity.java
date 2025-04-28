package com.example.locationapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PropertyEditorActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private EditText etTitle, etDescription, etPrice, etAddress, etCity;
    private EditText etBedrooms, etBathrooms, etArea;
    private Spinner spPropertyType;
    private CheckBox cbAvailable;
    private Button btnAddImage, btnSaveProperty;
    private ImageView ivPropertyImage;
    private ProgressBar progressBar;

    private String propertyId;
    private Property currentProperty;
    private List<String> imageUrls = new ArrayList<>();
    private Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_editor);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize views
        initViews();

        // Set up property type spinner
        setupPropertyTypeSpinner();

        // Get property ID if editing existing property
        propertyId = getIntent().getStringExtra("PROPERTY_ID");
        if (propertyId != null) {
            getSupportActionBar().setTitle("Modifier la propriété");
            loadPropertyDetails();
        } else {
            getSupportActionBar().setTitle("Ajouter une propriété");
        }

        // Set up image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            Picasso.get().load(selectedImageUri).into(ivPropertyImage);
                            ivPropertyImage.setVisibility(View.VISIBLE);
                        }
                    }
                });

        // Set up button click listeners
        btnAddImage.setOnClickListener(v -> openImagePicker());
        btnSaveProperty.setOnClickListener(v -> saveProperty());
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etPrice = findViewById(R.id.etPrice);
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etBedrooms = findViewById(R.id.etBedrooms);
        etBathrooms = findViewById(R.id.etBathrooms);
        etArea = findViewById(R.id.etArea);
        spPropertyType = findViewById(R.id.spPropertyType);
        cbAvailable = findViewById(R.id.cbAvailable);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnSaveProperty = findViewById(R.id.btnSaveProperty);
        ivPropertyImage = findViewById(R.id.ivPropertyImage);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupPropertyTypeSpinner() {
        String[] propertyTypes = {"Appartement", "Maison", "Villa", "Studio", "Duplex", "Autre"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, propertyTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spPropertyType.setAdapter(adapter);
    }

    private void loadPropertyDetails() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("properties").document(propertyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        currentProperty = documentSnapshot.toObject(Property.class);
                        currentProperty.setId(documentSnapshot.getId());

                        // Fill form with property details
                        fillPropertyForm(currentProperty);
                    } else {
                        Toast.makeText(PropertyEditorActivity.this,
                                "Propriété non trouvée", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PropertyEditorActivity.this,
                            "Erreur lors du chargement: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void fillPropertyForm(Property property) {
        etTitle.setText(property.getTitle());
        etDescription.setText(property.getDescription());
        etPrice.setText(String.valueOf(property.getPrice()));
        etAddress.setText(property.getAddress());
        etCity.setText(property.getCity());
        etBedrooms.setText(String.valueOf(property.getBedrooms()));
        etBathrooms.setText(String.valueOf(property.getBathrooms()));
        etArea.setText(String.valueOf(property.getArea()));
        cbAvailable.setChecked(property.isAvailable());

        // Set property type spinner
        String propertyType = property.getPropertyType();
        ArrayAdapter adapter = (ArrayAdapter) spPropertyType.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equals(propertyType)) {
                spPropertyType.setSelection(i);
                break;
            }
        }

        // Load first image if available
        if (property.getImageUrls() != null && !property.getImageUrls().isEmpty()) {
            imageUrls = property.getImageUrls();
            Picasso.get().load(imageUrls.get(0)).into(ivPropertyImage);
            ivPropertyImage.setVisibility(View.VISIBLE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Sélectionner une image"));
    }

    private void saveProperty() {
        // Validate form
        if (!validateForm()) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // If image was selected, upload it first
        if (selectedImageUri != null) {
            uploadImage();
        } else {
            // If no new image, save property directly
            savePropertyToFirestore();
        }
    }

    private boolean validateForm() {
        String title = etTitle.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();

        if (title.isEmpty() || price.isEmpty() || address.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void uploadImage() {
        String imageName = UUID.randomUUID().toString();
        StorageReference storageRef = storage.getReference().child("property_images/" + imageName);

        UploadTask uploadTask = storageRef.putFile(selectedImageUri);
        uploadTask.addOnSuccessListener(taskSnapshot ->
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Add new image URL to list
                    if (imageUrls.isEmpty()) {
                        imageUrls.add(uri.toString());
                    } else {
                        // Replace first image for simplicity
                        imageUrls.set(0, uri.toString());
                    }

                    // Continue saving property
                    savePropertyToFirestore();
                })
        ).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(PropertyEditorActivity.this,
                    "Erreur lors du téléchargement de l'image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void savePropertyToFirestore() {
        String currentUserId = mAuth.getCurrentUser().getUid();

        try {
            // Create property object
            Map<String, Object> propertyData = new HashMap<>();
            propertyData.put("title", etTitle.getText().toString().trim());
            propertyData.put("description", etDescription.getText().toString().trim());
            propertyData.put("price", Double.parseDouble(etPrice.getText().toString().trim()));
            propertyData.put("address", etAddress.getText().toString().trim());
            propertyData.put("city", etCity.getText().toString().trim());
            propertyData.put("bedrooms", Integer.parseInt(etBedrooms.getText().toString().trim()));
            propertyData.put("bathrooms", Integer.parseInt(etBathrooms.getText().toString().trim()));
            propertyData.put("area", Double.parseDouble(etArea.getText().toString().trim()));
            propertyData.put("propertyType", spPropertyType.getSelectedItem().toString());
            propertyData.put("available", cbAvailable.isChecked());
            propertyData.put("agentId", currentUserId);
            propertyData.put("imageUrls", imageUrls);

            if (propertyId == null) {
                // Add timestamp for new properties
                propertyData.put("createdAt", System.currentTimeMillis());

                // Add new property
                db.collection("properties")
                        .add(propertyData)
                        .addOnSuccessListener(documentReference -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(PropertyEditorActivity.this,
                                    "Propriété ajoutée avec succès", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(PropertyEditorActivity.this,
                                    "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Update existing property
                db.collection("properties")
                        .document(propertyId)
                        .update(propertyData)
                        .addOnSuccessListener(aVoid -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(PropertyEditorActivity.this,
                                    "Propriété mise à jour avec succès", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(PropertyEditorActivity.this,
                                    "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        } catch (NumberFormatException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Veuillez entrer des valeurs numériques valides",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}