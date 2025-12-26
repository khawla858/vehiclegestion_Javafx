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

    // Champs supplémentaires pour affichage
    private String nomVendeur;
    private String emailVendeur;
    private String telephoneVendeur;

    // Constructeurs
    public DemandeMagasin() {}

    public DemandeMagasin(int idDemande, int idVendeur, String nomMagasin, String adresse,
                          String localisation, String description, String photoProfil,
                          String statut, Timestamp dateDemande) {
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

    // Getters et Setters
    public int getIdDemande() { return idDemande; }
    public void setIdDemande(int idDemande) { this.idDemande = idDemande; }

    public int getIdVendeur() { return idVendeur; }
    public void setIdVendeur(int idVendeur) { this.idVendeur = idVendeur; }

    public String getNomMagasin() { return nomMagasin; }
    public void setNomMagasin(String nomMagasin) { this.nomMagasin = nomMagasin; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhotoProfil() { return photoProfil; }
    public void setPhotoProfil(String photoProfil) { this.photoProfil = photoProfil; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Timestamp getDateDemande() { return dateDemande; }
    public void setDateDemande(Timestamp dateDemande) { this.dateDemande = dateDemande; }

    // Getters et Setters pour les champs supplémentaires
    public String getNomVendeur() { return nomVendeur; }
    public void setNomVendeur(String nomVendeur) { this.nomVendeur = nomVendeur; }

    public String getEmailVendeur() { return emailVendeur; }
    public void setEmailVendeur(String emailVendeur) { this.emailVendeur = emailVendeur; }

    public String getTelephoneVendeur() { return telephoneVendeur; }
    public void setTelephoneVendeur(String telephoneVendeur) { this.telephoneVendeur = telephoneVendeur; }

    // Méthode utilitaire pour affichage
    public String getStatutColor() {
        switch (statut.toLowerCase()) {
            case "approuvée": return "#10b981"; // vert
            case "rejetée": return "#ef4444";   // rouge
            case "en attente": return "#f59e0b"; // orange
            default: return "#6b7280"; // gris
        }
    }
}