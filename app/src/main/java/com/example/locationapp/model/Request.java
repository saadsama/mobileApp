package com.example.locationapp.model;

import com.example.locationapp.Property;
import com.google.firebase.Timestamp;
import java.io.Serializable;
import java.util.List;

public class Request implements Serializable {
    private String id;
    private String propertyId;
    private String clientId;
    private String agentId;
    private String message;
    private long createdAt;
    private long updatedAt;
    private boolean pending;
    private boolean approved;
    private boolean rejected;
    private String status;
    
    // Client details
    private String clientEmail;
    private String clientName;
    private String clientPhone;
    
    // Agent details
    private String agentEmail;
    private String agentPhone;
    
    // Property details cached in request
    private String propertyTitle;
    private String propertyAddress;
    private String propertyImageUrl;
    private double propertyPrice;
    
    // Additional fields for UI display (not stored in Firestore)
    private Property propertyDetails;
    private String agentName;

    // Default constructor for Firestore
    public Request() {}

    // Constructor with parameters
    public Request(String propertyId, String clientId, String agentId, String message) {
        this.propertyId = propertyId;
        this.clientId = clientId;
        this.agentId = agentId;
        this.message = message;
        this.pending = true;
        this.approved = false;
        this.rejected = false;
        this.status = "PENDING";
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
        updateStatus();
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
        updateStatus();
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
        updateStatus();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        // Update boolean flags based on status
        this.pending = "PENDING".equals(status);
        this.approved = "APPROVED".equals(status);
        this.rejected = "REJECTED".equals(status);
    }

    private void updateStatus() {
        if (pending) {
            status = "PENDING";
        } else if (approved) {
            status = "APPROVED";
        } else if (rejected) {
            status = "REJECTED";
        }
    }

    // Client details getters and setters
    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    // Agent details getters and setters
    public String getAgentEmail() {
        return agentEmail;
    }

    public void setAgentEmail(String agentEmail) {
        this.agentEmail = agentEmail;
    }

    public String getAgentPhone() {
        return agentPhone;
    }

    public void setAgentPhone(String agentPhone) {
        this.agentPhone = agentPhone;
    }

    // Property details getters and setters
    public String getPropertyTitle() {
        return propertyTitle;
    }

    public void setPropertyTitle(String propertyTitle) {
        this.propertyTitle = propertyTitle;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public String getPropertyImageUrl() {
        return propertyImageUrl;
    }

    public void setPropertyImageUrl(String propertyImageUrl) {
        this.propertyImageUrl = propertyImageUrl;
    }

    public double getPropertyPrice() {
        return propertyPrice;
    }

    public void setPropertyPrice(double propertyPrice) {
        this.propertyPrice = propertyPrice;
    }

    // Additional fields getters and setters
    public Property getPropertyDetails() {
        return propertyDetails;
    }

    public void setPropertyDetails(Property propertyDetails) {
        this.propertyDetails = propertyDetails;
        if (propertyDetails != null) {
            this.propertyTitle = propertyDetails.getTitle();
            this.propertyAddress = propertyDetails.getAddress();
            // Get the first image URL from the list if available
            List<String> urls = propertyDetails.getImageUrls();
            this.propertyImageUrl = urls != null && !urls.isEmpty() ? urls.get(0) : null;
            this.propertyPrice = propertyDetails.getPrice();
        }
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
}