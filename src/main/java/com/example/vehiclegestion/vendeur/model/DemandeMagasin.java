package com.example.vehiclegestion.vendeur.model;

import java.sql.Timestamp;

public class DemandeMagasin {
    private int idDemande;
    private int idVendeur;
    private String nomMagasin;
    private String adresse;
    private String localisation;
    private String description;
    private String photoProfil;
    private String statut;
    private Timestamp dateDemande;

    public DemandeMagasin(int idVendeur, String nomMagasin, String adresse, String localisation, String description, String photoProfil) {
        this.idVendeur = idVendeur;
        this.nomMagasin = nomMagasin;
        this.adresse = adresse;
        this.localisation = localisation;
        this.description = description;
        this.photoProfil = photoProfil;
        this.statut = "en attente";
    }

    public DemandeMagasin(int idDemande, int idVendeur, String nomMagasin, String adresse, String localisation, String description, String photoProfil, String statut, Timestamp dateDemande) {
        this.idDemande = idDemande;
        this.idVendeur = idVendeur;
        this.nomMagasin = nomMagasin;
        this.adresse = adresse;
        this.localisation = localisation;
        this.description = description;
        this.photoProfil = photoProfil;
        this.statut = statut;
        this.dateDemande = dateDemande;
    }

    // Getters et setters
    public int getIdVendeur() { return idVendeur; }
    public String getNomMagasin() { return nomMagasin; }
    public String getAdresse() { return adresse; }
    public String getLocalisation() { return localisation; }
    public String getDescription() { return description; }
    public String getPhotoProfil() { return photoProfil; }
    public String getStatut() { return statut; }
}
