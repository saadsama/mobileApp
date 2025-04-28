package com.example.locationapp;

import com.example.locationapp.model.Request;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RequestRepository {
    private static final String COLLECTION_REQUESTS = "requests";
    private final FirebaseFirestore db;

    public RequestRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Void> createRequest(Request request) {
        return db.collection(COLLECTION_REQUESTS)
                .document(request.getId())
                .set(request);
    }

    public Task<DocumentSnapshot> getRequest(String requestId) {
        return db.collection(COLLECTION_REQUESTS)
                .document(requestId)
                .get();
    }

    public Task<Void> updateRequest(Request request) {
        return db.collection(COLLECTION_REQUESTS)
                .document(request.getId())
                .set(request);
    }

    public Task<QuerySnapshot> getRequestsByAgent(String agentId) {
        return db.collection(COLLECTION_REQUESTS)
                .whereEqualTo("agentId", agentId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get();
    }

    public Task<QuerySnapshot> getRequestsByClient(String clientId) {
        return db.collection(COLLECTION_REQUESTS)
                .whereEqualTo("clientId", clientId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get();
    }

    public Task<QuerySnapshot> getRequestsByProperty(String propertyId) {
        return db.collection(COLLECTION_REQUESTS)
                .whereEqualTo("propertyId", propertyId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get();
    }

    public Task<Void> deleteRequest(String requestId) {
        return db.collection(COLLECTION_REQUESTS)
                .document(requestId)
                .delete();
    }

    public List<Request> convertQuerySnapshotToRequests(QuerySnapshot querySnapshot) {
        List<Request> requests = new ArrayList<>();
        for (QueryDocumentSnapshot document : querySnapshot) {
            Request request = document.toObject(Request.class);
            request.setId(document.getId());
            requests.add(request);
        }
        return requests;
    }
} 