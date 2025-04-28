package com.example.locationapp.model;

import java.util.Date;

public class Abonnement {
    private String id;
    private Agent agent;
    private TypeAbonnement typeAbonnement;
    private Date dateDebut;
    private Date dateFin;
    private boolean actif;
    private int nombreOffresPubliees;

    // Default constructor
    public Abonnement() {
    }

    // Parameterized constructor
    public Abonnement(Agent agent, TypeAbonnement typeAbonnement,
                      Date dateDebut, Date dateFin, boolean actif) {
        this.agent = agent;
        this.typeAbonnement = typeAbonnement;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.actif = actif;
        this.nombreOffresPubliees = 0;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public TypeAbonnement getTypeAbonnement() {
        return typeAbonnement;
    }

    public void setTypeAbonnement(TypeAbonnement typeAbonnement) {
        this.typeAbonnement = typeAbonnement;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public int getNombreOffresPubliees() {
        return nombreOffresPubliees;
    }

    public void setNombreOffresPubliees(int nombreOffresPubliees) {
        this.nombreOffresPubliees = nombreOffresPubliees;
    }
}
