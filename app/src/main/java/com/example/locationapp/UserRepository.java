package com.example.locationapp;

import com.example.locationapp.model.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final String COLLECTION_USERS = "users";
    private final FirebaseFirestore db;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Void> createUser(User user) {
        return db.collection(COLLECTION_USERS)
                .document(user.getId())
                .set(user);
    }

    public Task<DocumentSnapshot> getUser(String userId) {
        return db.collection(COLLECTION_USERS)
                .document(userId)
                .get();
    }

    public Task<Void> updateUser(User user) {
        return db.collection(COLLECTION_USERS)
                .document(user.getId())
                .set(user);
    }

    public Task<QuerySnapshot> getAgents() {
        return db.collection(COLLECTION_USERS)
                .whereEqualTo("userType", "AGENT")
                .get();
    }

    public Task<QuerySnapshot> getClients() {
        return db.collection(COLLECTION_USERS)
                .whereEqualTo("userType", "CLIENT")
                .get();
    }

    public Task<DocumentSnapshot> getUserByEmail(String email) {
        return db.collection(COLLECTION_USERS)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        return task.getResult().getDocuments().get(0);
                    }
                    return null;
                });
    }

    public List<User> convertQuerySnapshotToUsers(QuerySnapshot querySnapshot) {
        List<User> users = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot) {
            User user = document.toObject(User.class);
            user.setId(document.getId());
            users.add(user);
        }
        return users;
    }
}
