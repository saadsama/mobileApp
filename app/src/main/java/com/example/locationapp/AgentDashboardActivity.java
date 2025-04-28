package com.example.locationapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AgentDashboardActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_dashboard);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        Button btnManageProperties = findViewById(R.id.btnManageProperties);
        Button btnViewRequests = findViewById(R.id.btnViewRequests);
        Button btnManageProfile = findViewById(R.id.btnManageProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        // Load agent name
        loadAgentName();

        // Set up button listeners
        btnManageProperties.setOnClickListener(v -> {
            Intent intent = new Intent(AgentDashboardActivity.this, AgentPropertiesActivity.class);
            startActivity(intent);
        });

        btnViewRequests.setOnClickListener(v -> {
            Intent intent = new Intent(AgentDashboardActivity.this, AgentRequestsActivity.class);
            startActivity(intent);
        });

        btnManageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AgentDashboardActivity.this, ProfileManagementActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(AgentDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadAgentName() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("agents").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            if (firstName != null && !firstName.isEmpty()) {
                                tvWelcome.setText("Bonjour " + firstName + " !!!");
                            }
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to login screen
        moveTaskToBack(true);
    }
}