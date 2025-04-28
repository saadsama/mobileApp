package com.example.locationapp.model;

public class Offre {
    private String id;
    private String titre;
    private String description;
    private double superficie;
    private int pieces;
    private int etage;
    private int sdb;
    private double loyer;
    private String adresse;
    private String ville;
    private String pays;
    private Agent agent;

    // Default constructor
    public Offre() {
    }

    // Parameterized constructor
    public Offre(String titre, String description, double superficie,
                 int pieces, int etage, int sdb, double loyer,
                 String adresse, String ville, String pays, Agent agent) {
        this.titre = titre;
        this.description = description;
        this.superficie = superficie;
        this.pieces = pieces;
        this.etage = etage;
        this.sdb = sdb;
        this.loyer = loyer;
        this.adresse = adresse;
        this.ville = ville;
        this.pays = pays;
        this.agent = agent;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getSuperficie() {
        return superficie;
    }

    public void setSuperficie(double superficie) {
        this.superficie = superficie;
    }

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
    }

    public int getEtage() {
        return etage;
    }

    public void setEtage(int etage) {
        this.etage = etage;
    }

    public int getSdb() {
        return sdb;
    }

    public void setSdb(int sdb) {
        this.sdb = sdb;
    }

    public double getLoyer() {
        return loyer;
    }

    public void setLoyer(double loyer) {
        this.loyer = loyer;
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

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }
}