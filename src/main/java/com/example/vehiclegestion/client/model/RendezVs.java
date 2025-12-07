package com.example.vehiclegestion.client.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

public class RendezVs {
    private int idRdv;
    private int idClient;
    private int idVendeur;
    private int idArticle;
    private String typeRdv;
    private LocalDate dateRdv;
    private LocalTime heureRdv;
    private int duree;
    private String description;
    private String statut;
    private LocalDateTime dateCreation;

    // Constructeurs
    public RendezVs() {}

    public RendezVs(int idClient, int idVendeur, int idArticle, String typeRdv,
                    LocalDate dateRdv, LocalTime heureRdv, int duree, String description) {
        this.idClient = idClient;
        this.idVendeur = idVendeur;
        this.idArticle = idArticle;
        this.typeRdv = typeRdv;
        this.dateRdv = dateRdv;
        this.heureRdv = heureRdv;
        this.duree = duree;
        this.description = description;
        this.statut = "en attente";
        this.dateCreation = LocalDateTime.now();
    }

    // Getters et Setters
    public int getIdRdv() { return idRdv; }
    public void setIdRdv(int idRdv) { this.idRdv = idRdv; }

    public int getIdClient() { return idClient; }
    public void setIdClient(int idClient) { this.idClient = idClient; }

    public int getIdVendeur() { return idVendeur; }
    public void setIdVendeur(int idVendeur) { this.idVendeur = idVendeur; }

    public int getIdArticle() { return idArticle; }
    public void setIdArticle(int idArticle) { this.idArticle = idArticle; }

    public String getTypeRdv() { return typeRdv; }
    public void setTypeRdv(String typeRdv) { this.typeRdv = typeRdv; }

    public LocalDate getDateRdv() { return dateRdv; }
    public void setDateRdv(LocalDate dateRdv) { this.dateRdv = dateRdv; }

    public LocalTime getHeureRdv() { return heureRdv; }
    public void setHeureRdv(LocalTime heureRdv) { this.heureRdv = heureRdv; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}