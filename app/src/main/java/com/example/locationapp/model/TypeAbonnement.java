package com.example.locationapp.model;

public class TypeAbonnement {
    private String id;
    private String nom;
    private String description;
    private double prix;
    private int dureeEnMois;
    private int nombreOffresAutorisees;

    // Default constructor
    public TypeAbonnement() {
    }

    // Parameterized constructor
    public TypeAbonnement(String nom, String description, double prix,
                          int dureeEnMois, int nombreOffresAutorisees) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.dureeEnMois = dureeEnMois;
        this.nombreOffresAutorisees = nombreOffresAutorisees;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public int getDureeEnMois() {
        return dureeEnMois;
    }

    public void setDureeEnMois(int dureeEnMois) {
        this.dureeEnMois = dureeEnMois;
    }

    public int getNombreOffresAutorisees() {
        return nombreOffresAutorisees;
    }

    public void setNombreOffresAutorisees(int nombreOffresAutorisees) {
        this.nombreOffresAutorisees = nombreOffresAutorisees;
    }
}
