package com.example.locationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClientDashboardActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        Button btnConsultOffers = findViewById(R.id.btnConsultOffers);
        Button btnManageRequests = findViewById(R.id.btnManageRequests);
        Button btnManageProfile = findViewById(R.id.btnManageProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Load client name
        loadClientName();

        // Set up button listeners
        btnConsultOffers.setOnClickListener(v -> {
            Intent intent = new Intent(ClientDashboardActivity.this, OffersActivity.class);
            startActivity(intent);
        });

        btnManageRequests.setOnClickListener(v -> {
            Intent intent = new Intent(ClientDashboardActivity.this, ClientRequestsActivity.class);
            startActivity(intent);
        });

        btnManageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ClientDashboardActivity.this, ProfileManagementActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(ClientDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadClientName() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("clients").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");
                            String fullName = firstName + " " + lastName;
                            tvWelcome.setText("Bonjour " + fullName + " !!!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ClientDashboardActivity.this, 
                            "Erreur lors du chargement du profil", Toast.LENGTH_SHORT).show();
                    });
        }
    }
} 