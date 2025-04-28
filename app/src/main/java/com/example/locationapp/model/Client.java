package com.example.locationapp.model;

import java.util.List;
import java.util.ArrayList;

public class Client {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private int age;
    private String adresse;
    private String ville;
    private String pays;

    private List<Request> requests;
    private List<String> listeFavoris;

    // Default constructor
    public Client() {
        this.requests = new ArrayList<>();
        this.listeFavoris = new ArrayList<>();
    }

    // Parameterized constructor
    public Client(String nom, String prenom, String email, String telephone,
                  int age, String adresse, String ville, String pays) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.age = age;
        this.adresse = adresse;
        this.ville = ville;
        this.pays = pays;
        this.requests = new ArrayList<>();
        this.listeFavoris = new ArrayList<>();
    }

    // Getters and Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequestss(List<Request> requests) {
        this.requests = requests;
    }

    public void addDemande(Request request) {
        if (this.requests == null) {
            this.requests = new ArrayList<>();
        }
        this.requests.add(request);
    }

    public List<String> getListeFavoris() {
        return listeFavoris;
    }

    public void setListeFavoris(List<String> listeFavoris) {
        this.listeFavoris = listeFavoris;
    }

    public void addFavori(String favori) {
        if (this.listeFavoris == null) {
            this.listeFavoris = new ArrayList<>();
        }
        this.listeFavoris.add(favori);
    }
}
