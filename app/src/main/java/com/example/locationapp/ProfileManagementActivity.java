package com.example.locationapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.locationapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileManagementActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 2;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private User currentUser;

    private ImageView ivProfileImage;
    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etAddress;
    private EditText etBio;
    private Button btnSave;
    private ProgressBar progressBar;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_management);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Gérer le profil");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // Initialize views
        initializeViews();
        
        // Load user profile
        loadUserProfile();
    }

    private void initializeViews() {
        ivProfileImage = findViewById(R.id.ivProfileImage);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etBio = findViewById(R.id.etBio);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        // Set click listeners
        ivProfileImage.setOnClickListener(v -> checkPermissionAndOpenImagePicker());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void checkPermissionAndOpenImagePicker() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
            } else {
                openImagePicker();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission refusée pour accéder aux images", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String filePath = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                filePath = cursor.getString(column_index);
                cursor.close();
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération du chemin du fichier: " + e.getMessage());
        }
        return filePath;
    }

    private void loadUserProfile() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            progressBar.setVisibility(View.VISIBLE);

            db.collection("agents").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Create User object from agent data
                            currentUser = new User();
                            currentUser.setId(documentSnapshot.getId());
                            currentUser.setName(documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName"));
                            currentUser.setEmail(documentSnapshot.getString("email"));
                            currentUser.setPhoneNumber(documentSnapshot.getString("phone"));
                            currentUser.setAddress(documentSnapshot.getString("address"));
                            currentUser.setBio(documentSnapshot.getString("bio"));
                            currentUser.setProfileImageUrl(documentSnapshot.getString("profileImageUrl"));
                            
                            populateProfileFields();
                        }
                        progressBar.setVisibility(View.GONE);
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void populateProfileFields() {
        etName.setText(currentUser.getName());
        etEmail.setText(currentUser.getEmail());
        etPhone.setText(currentUser.getPhoneNumber());
        etAddress.setText(currentUser.getAddress());
        etBio.setText(currentUser.getBio());

        // Load profile image if exists
        if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getProfileImageUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .into(ivProfileImage);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            ivProfileImage.setImageURI(selectedImageUri);
        }
    }

    private void saveProfile() {
        if (currentUser == null) return;

        progressBar.setVisibility(View.VISIBLE);

        // Update user object with new values
        currentUser.setName(etName.getText().toString().trim());
        currentUser.setPhoneNumber(etPhone.getText().toString().trim());
        currentUser.setAddress(etAddress.getText().toString().trim());
        currentUser.setBio(etBio.getText().toString().trim());

        // If there's a new image, upload it first
        if (selectedImageUri != null) {
            uploadProfileImage();
        } else {
            updateUserProfile();
        }
    }

    private void uploadProfileImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Aucune image sélectionnée", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        String userId = mAuth.getCurrentUser().getUid();
        String imageFileName = "profile_" + userId + ".jpg";
        StorageReference imageRef = storage.getReference().child("agent_profiles").child(imageFileName);

        System.out.println("Début du téléchargement de l'image: " + imageFileName);
        System.out.println("URI de l'image: " + selectedImageUri.toString());

        // Compress image if needed
        try {
            imageRef.putFile(selectedImageUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    System.out.println("Progression du téléchargement : " + progress + "%");
                    progressBar.setProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    System.out.println("Image téléchargée avec succès");
                    imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            System.out.println("URL de téléchargement obtenue: " + uri.toString());
                            currentUser.setProfileImageUrl(uri.toString());
                            updateUserProfile();
                        })
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(View.GONE);
                            System.out.println("Erreur lors de l'obtention de l'URL: " + e.getMessage());
                            Toast.makeText(this, "Erreur lors de l'obtention de l'URL de l'image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    System.out.println("Erreur lors du téléchargement: " + e.getMessage());
                    Toast.makeText(this, "Erreur lors du téléchargement de l'image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            System.out.println("Exception lors du téléchargement: " + e.getMessage());
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateUserProfile() {
        if (currentUser == null) {
            Toast.makeText(this, "Erreur: utilisateur non initialisé", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> agentData = new HashMap<>();
        String[] nameParts = currentUser.getName().split(" ", 2);
        agentData.put("firstName", nameParts[0]);
        agentData.put("lastName", nameParts.length > 1 ? nameParts[1] : "");
        agentData.put("email", currentUser.getEmail());
        agentData.put("phoneNumber", currentUser.getPhoneNumber());
        agentData.put("phone", currentUser.getPhoneNumber()); // Update both phone fields
        agentData.put("address", currentUser.getAddress());
        agentData.put("bio", currentUser.getBio());
        
        // Only update profileImageUrl if it's not null
        if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
            agentData.put("profileImageUrl", currentUser.getProfileImageUrl());
        }

        System.out.println("Mise à jour du profil avec les données: " + agentData.toString());

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("agents").document(userId)
                .update(agentData)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    System.out.println("Profil mis à jour avec succès");
                    Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    System.out.println("Erreur lors de la mise à jour du profil: " + e.getMessage());
                    Toast.makeText(this, "Erreur lors de la mise à jour du profil: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 