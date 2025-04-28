package com.example.locationapp.model;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Property {
    private String id;
    private String title;
    private String description;
    private String address;
    private String city;
    private double price;
    private int rooms;
    private int bathrooms;
    private double area;
    private String agentId;
    private List<String> imageUrls;
    private boolean available;
    private long createdAt;
    private String propertyType;

    // Empty constructor required for Firestore
    public Property() {
        imageUrls = new ArrayList<>();
        available = true;
    }

    public Property(String title, String description, String address, String city, 
                   double price, int rooms, String agentId) {
        this.title = title;
        this.description = description;
        this.address = address;
        this.city = city.toLowerCase();
        this.price = price;
        this.rooms = rooms;
        this.agentId = agentId;
        this.imageUrls = new ArrayList<>();
        this.available = true;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city != null ? city.toLowerCase() : null;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getRooms() {
        return rooms;
    }

    public void setRooms(int rooms) {
        this.rooms = rooms;
    }

    public int getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(int bathrooms) {
        this.bathrooms = bathrooms;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(String propertyType) {
        this.propertyType = propertyType;
    }
} 