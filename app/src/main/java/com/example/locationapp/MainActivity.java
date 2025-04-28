package com.example.locationapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Layout views
    private View layoutAuth;
    private View layoutCreateAccount;
    private View layoutMainMenu;

    // Authentication layout views
    private EditText authEditTextEmail;
    private EditText authEditTextPassword;
    private TextView authTextViewError;
    private Button authButtonLogin;
    private Button authButtonSignup;

    // Account creation layout views
    private RadioGroup cptRadioGroupRole;
    private RadioButton cptRadioButtonAgent;
    private RadioButton cptRadioButtonClient;
    private EditText cptEditTextNom;
    private EditText cptEditTextPrenom;
    private EditText cptEditTextAge;
    private EditText cptEditTextEmail;
    private EditText cptEditTextPassword;
    private EditText cptEditTextTel;
    private EditText cptEditTextPays;
    private EditText cptEditTextAgency;
    private Button cptButtonConfirmer;
    private Button cptButtonAnnuler;

    // Firebase services
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase services
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize layouts
        layoutAuth = findViewById(R.id.layout_auth);
        layoutCreateAccount = findViewById(R.id.layout_create_account);
        layoutMainMenu = findViewById(R.id.layout_main_menu);

        // Initialize authentication layout views
        authEditTextEmail = findViewById(R.id.auth_edit_text_email);
        authEditTextPassword = findViewById(R.id.auth_edit_text_password);
        authTextViewError = findViewById(R.id.auth_text_view_error);
        authButtonLogin = findViewById(R.id.auth_button_login);
        authButtonSignup = findViewById(R.id.auth_button_signup);

        // Initialize account creation layout views
        cptRadioGroupRole = findViewById(R.id.cpt_radio_group_role);
        cptRadioButtonAgent = findViewById(R.id.cpt_radio_button_agent);
        cptRadioButtonClient = findViewById(R.id.cpt_radio_button_client);
        cptEditTextNom = findViewById(R.id.cpt_edit_text_nom);
        cptEditTextPrenom = findViewById(R.id.cpt_edit_text_prenom);
        cptEditTextAge = findViewById(R.id.cpt_edit_text_age);
        cptEditTextEmail = findViewById(R.id.cpt_edit_text_email);
        cptEditTextPassword = findViewById(R.id.cpt_edit_text_password);
        cptEditTextTel = findViewById(R.id.cpt_edit_text_tel);
        cptEditTextPays = findViewById(R.id.cpt_edit_text_pays);
        cptEditTextAgency = findViewById(R.id.cpt_edit_text_agency);
        cptButtonConfirmer = findViewById(R.id.cpt_button_confirmer);
        cptButtonAnnuler = findViewById(R.id.cpt_button_annuler);

        // Set up listeners
        setupAuthListeners();
        setupCreateAccountListeners();
        setupMainMenuListeners();
    }

    private void setupAuthListeners() {
        // Login button click listener
        authButtonLogin.setOnClickListener(v -> {
            String email = authEditTextEmail.getText().toString().trim();
            String password = authEditTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showAuthError("Veuillez remplir tous les champs");
                return;
            }

            // Attempt login with Firebase
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Check user type in Firestore
                            String userId = firebaseAuth.getCurrentUser().getUid();

                            // First check in agents collection
                            db.collection("agents").document(userId).get()
                                    .addOnSuccessListener(agentDoc -> {
                                        if (agentDoc.exists()) {
                                            // User is an agent
                                            Intent intent = new Intent(MainActivity.this, AgentDashboardActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            // Check in clients collection
                                            db.collection("clients").document(userId).get()
                                                    .addOnSuccessListener(clientDoc -> {
                                                        if (clientDoc.exists()) {
                                                            // User is a client
                                                            Intent intent = new Intent(MainActivity.this, ClientDashboardActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            // User not found in either collection
                                                            showAuthError("Erreur: Utilisateur non trouvé");
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> showAuthError("Erreur d'accès à la base de données"));
                                        }
                                    })
                                    .addOnFailureListener(e -> showAuthError("Erreur d'accès à la base de données"));
                        } else {
                            // Login failed, show error message
                            showAuthError("Login et/ou mot de passe incorrects");
                        }
                    });
        });

        // Sign up button click listener
        authButtonSignup.setOnClickListener(v -> {
            // Show account creation layout
            showCreateAccountLayout();
        });
    }

    private void setupCreateAccountListeners() {
        // Toggle agent/client specific fields
        cptRadioGroupRole.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.cpt_radio_button_agent) {
                // Show agent-specific fields
                cptEditTextAgency.setVisibility(View.VISIBLE);
            } else {
                // Hide agent-specific fields
                cptEditTextAgency.setVisibility(View.GONE);
            }
        });

        // Confirm button click listener
        cptButtonConfirmer.setOnClickListener(v -> {
            // Get user input
            String email = cptEditTextEmail.getText().toString().trim();
            String password = cptEditTextPassword.getText().toString().trim();
            String nom = cptEditTextNom.getText().toString().trim();
            String prenom = cptEditTextPrenom.getText().toString().trim();

            // Validate input
            if (email.isEmpty() || password.isEmpty() || nom.isEmpty() || prenom.isEmpty()) {
                Toast.makeText(MainActivity.this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check password length (Firebase requires at least 6 characters)
            if (password.length() < 6) {
                Toast.makeText(MainActivity.this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user with Firebase Auth
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Get current user ID
                            String userId = firebaseAuth.getCurrentUser().getUid();

                            // Determine if agent or client
                            boolean isAgent = cptRadioButtonAgent.isChecked();

                            // Create user data object
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("firstName", prenom);
                            userData.put("lastName", nom);
                            userData.put("phoneNumber", cptEditTextTel.getText().toString().trim());
                            userData.put("isActive", true);
                            userData.put("registrationDate", System.currentTimeMillis());

                            if (isAgent) {
                                // Add agency name for agents
                                userData.put("agencyName", cptEditTextAgency.getText().toString().trim());

                                // Save to "agents" collection
                                db.collection("agents").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(MainActivity.this, "Compte agent créé avec succès", Toast.LENGTH_SHORT).show();
                                            showAuthLayout();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(MainActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                // Save to "clients" collection
                                db.collection("clients").document(userId)
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(MainActivity.this, "Compte client créé avec succès", Toast.LENGTH_SHORT).show();
                                            showAuthLayout();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(MainActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            // If creation fails
                            Toast.makeText(MainActivity.this, "Erreur de création de compte: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Cancel button click listener
        cptButtonAnnuler.setOnClickListener(v -> {
            // Return to authentication layout
            showAuthLayout();
        });
    }

    private void setupMainMenuListeners() {
        // Find and set up main menu buttons
        Button btnConsulter = findViewById(R.id.main_button_consulter);
        Button btnGererDemandes = findViewById(R.id.main_button_gerer_demandes);
        Button btnProfil = findViewById(R.id.main_button_profil);
        Button btnLogout = findViewById(R.id.main_button_logout);

        // Set listeners if the buttons exist
        if (btnConsulter != null) {
            btnConsulter.setOnClickListener(v -> {
                // Navigate to OffersActivity
                // Intent intent = new Intent(MainActivity.this, OffersActivity.class);
                // startActivity(intent);
                Toast.makeText(MainActivity.this, "Navigation vers Consulter les offres", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnGererDemandes != null) {
            btnGererDemandes.setOnClickListener(v -> {
                // Navigate to RequestsActivity
                // Intent intent = new Intent(MainActivity.this, RequestsActivity.class);
                // startActivity(intent);
                Toast.makeText(MainActivity.this, "Navigation vers Gérer les demandes", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnProfil != null) {
            btnProfil.setOnClickListener(v -> {
                // Navigate to ProfileManagementActivity
                Intent intent = new Intent(MainActivity.this, ProfileManagementActivity.class);
                startActivity(intent);
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                // Sign out the user
                firebaseAuth.signOut();
                showAuthLayout();
                Toast.makeText(MainActivity.this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void showAuthLayout() {
        layoutAuth.setVisibility(View.VISIBLE);
        layoutCreateAccount.setVisibility(View.GONE);
        layoutMainMenu.setVisibility(View.GONE);

        // Clear error message
        authTextViewError.setVisibility(View.GONE);

        // Clear input fields
        authEditTextEmail.setText("");
        authEditTextPassword.setText("");
    }

    private void showCreateAccountLayout() {
        layoutAuth.setVisibility(View.GONE);
        layoutCreateAccount.setVisibility(View.VISIBLE);
        layoutMainMenu.setVisibility(View.GONE);

        // Clear input fields
        cptEditTextNom.setText("");
        cptEditTextPrenom.setText("");
        cptEditTextAge.setText("");
        cptEditTextEmail.setText("");
        cptEditTextPassword.setText("");
        cptEditTextTel.setText("");
        cptEditTextPays.setText("");
        cptEditTextAgency.setText("");

        // Reset radio button
        cptRadioButtonAgent.setChecked(true);
        cptEditTextAgency.setVisibility(View.VISIBLE);
    }

    private void showMainMenu() {
        layoutAuth.setVisibility(View.GONE);
        layoutCreateAccount.setVisibility(View.GONE);
        layoutMainMenu.setVisibility(View.VISIBLE);
    }

    private void showAuthError(String message) {
        authTextViewError.setText(message);
        authTextViewError.setVisibility(View.VISIBLE);
    }
}