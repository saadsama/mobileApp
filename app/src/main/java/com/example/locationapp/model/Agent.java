package com.example.locationapp.model;

public class Agent {
    private String adresseAgence;
    private String villeAgence;
    private String paysAgence;
    private String telAgence;
    private String emailAgence;
    private String nom;
    private String prenom;

    // Default constructor
    public Agent() {
    }

    // Parameterized constructor
    public Agent(String adresseAgence, String villeAgence, String paysAgence,
                 String telAgence, String emailAgence, String nom, String prenom) {
        this.adresseAgence = adresseAgence;
        this.villeAgence = villeAgence;
        this.paysAgence = paysAgence;
        this.telAgence = telAgence;
        this.emailAgence = emailAgence;
        this.nom = nom;
        this.prenom = prenom;
    }

    // Getters and Setters
    public String getAdresseAgence() {
        return adresseAgence;
    }

    public void setAdresseAgence(String adresseAgence) {
        this.adresseAgence = adresseAgence;
    }

    public String getVilleAgence() {
        return villeAgence;
    }

    public void setVilleAgence(String villeAgence) {
        this.villeAgence = villeAgence;
    }

    public String getPaysAgence() {
        return paysAgence;
    }

    public void setPaysAgence(String paysAgence) {
        this.paysAgence = paysAgence;
    }

    public String getTelAgence() {
        return telAgence;
    }

    public void setTelAgence(String telAgence) {
        this.telAgence = telAgence;
    }

    public String getEmailAgence() {
        return emailAgence;
    }

    public void setEmailAgence(String emailAgence) {
        this.emailAgence = emailAgence;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
}
