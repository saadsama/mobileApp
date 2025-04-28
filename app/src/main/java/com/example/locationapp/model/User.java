package com.example.locationapp.model;

import java.io.Serializable;

public class User implements Serializable {
    private String id;
    private String email;
    private String name;
    private String phoneNumber;
    private String userType; // "AGENT" or "CLIENT"
    private String profileImageUrl;
    private String address;
    private String bio;

    // Required empty constructor for Firestore
    public User() {}

    public User(String email, String name, String phoneNumber, String userType) {
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.userType = userType;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public boolean isAgent() {
        return "AGENT".equals(userType);
    }

    public boolean isClient() {
        return "CLIENT".equals(userType);
    }
} 