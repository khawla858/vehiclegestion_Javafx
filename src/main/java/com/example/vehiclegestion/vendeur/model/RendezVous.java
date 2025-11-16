package com.example.vehiclegestion.vendeur.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class RendezVous {
    private int idRdv;
    private int idClient;
    private String nomClient;  // Pour affichage (jointure avec Client/Utilisateur)
    private String telephoneClient;
    private String emailClient;
    private int idVendeur;
    private int idArticle;  // Voiture concernée
    private String titreArticle;  // Pour affichage (jointure avec Article)
    private LocalDate dateRdv;
    private LocalTime heureRdv;
    private String typeRdv;  // Ex. : "Visite", "Test Drive", "Négociation"
    private String statut;  // "en attente", "confirmé", "annulé", "terminé"
    private String description;  // Description du RDV
    private int duree;  // Durée en minutes
    private String commentaire;  // Commentaire du vendeur

    // Constructeurs
    public RendezVous() {}

    public RendezVous(int idRdv, int idClient, String nomClient, String telephoneClient, String emailClient,
                      int idVendeur, int idArticle, String titreArticle, LocalDate dateRdv, LocalTime heureRdv,
                      String typeRdv, String statut, String description, int duree, String commentaire) {
        this.idRdv = idRdv;
        this.idClient = idClient;
        this.nomClient = nomClient;
        this.telephoneClient = telephoneClient;
        this.emailClient = emailClient;
        this.idVendeur = idVendeur;
        this.idArticle = idArticle;
        this.titreArticle = titreArticle;
        this.dateRdv = dateRdv;
        this.heureRdv = heureRdv;
        this.typeRdv = typeRdv;
        this.statut = statut;
        this.description = description;
        this.duree = duree;
        this.commentaire = commentaire;
    }

    // Getters et Setters
    public int getIdRdv() { return idRdv; }
    public void setIdRdv(int idRdv) { this.idRdv = idRdv; }

    public int getIdClient() { return idClient; }
    public void setIdClient(int idClient) { this.idClient = idClient; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getTelephoneClient() { return telephoneClient; }
    public void setTelephoneClient(String telephoneClient) { this.telephoneClient = telephoneClient; }

    public String getEmailClient() { return emailClient; }
    public void setEmailClient(String emailClient) { this.emailClient = emailClient; }

    public int getIdVendeur() { return idVendeur; }
    public void setIdVendeur(int idVendeur) { this.idVendeur = idVendeur; }

    public int getIdArticle() { return idArticle; }
    public void setIdArticle(int idArticle) { this.idArticle = idArticle; }

    public String getTitreArticle() { return titreArticle; }
    public void setTitreArticle(String titreArticle) { this.titreArticle = titreArticle; }

    public LocalDate getDateRdv() { return dateRdv; }
    public void setDateRdv(LocalDate dateRdv) { this.dateRdv = dateRdv; }

    public LocalTime getHeureRdv() { return heureRdv; }
    public void setHeureRdv(LocalTime heureRdv) { this.heureRdv = heureRdv; }

    public String getTypeRdv() { return typeRdv; }
    public void setTypeRdv(String typeRdv) { this.typeRdv = typeRdv; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    @Override
    public String toString() {
        return "RendezVous{" +
                "idRdv=" + idRdv +
                ", nomClient='" + nomClient + '\'' +
                ", titreArticle='" + titreArticle + '\'' +
                ", dateRdv=" + dateRdv +
                ", heureRdv=" + heureRdv +
                ", statut='" + statut + '\'' +
                '}';
    }
}