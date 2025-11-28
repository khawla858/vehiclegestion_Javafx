// ========================================
// 1. Reservation.java (Model)
// ========================================
package com.example.vehiclegestion.vendeur.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reservation {
    private int idReservation;
    private int idClient;
    private String nomClient;
    private int idArticle;
    private String titreArticle;
    private double prixArticle;
    private LocalDateTime dateReservation;
    private String statut;
    private LocalDateTime dateExpiration;

    // Constructeur
    public Reservation(int idReservation, int idClient, String nomClient,
                       int idArticle, String titreArticle, double prixArticle,
                       LocalDateTime dateReservation, String statut,
                       LocalDateTime dateExpiration) {
        this.idReservation = idReservation;
        this.idClient = idClient;
        this.nomClient = nomClient;
        this.idArticle = idArticle;
        this.titreArticle = titreArticle;
        this.prixArticle = prixArticle;
        this.dateReservation = dateReservation;
        this.statut = statut;
        this.dateExpiration = dateExpiration;
    }

    // Getters
    public int getIdReservation() { return idReservation; }
    public int getIdClient() { return idClient; }
    public String getNomClient() { return nomClient; }
    public int getIdArticle() { return idArticle; }
    public String getTitreArticle() { return titreArticle; }
    public double getPrixArticle() { return prixArticle; }
    public LocalDateTime getDateReservation() { return dateReservation; }
    public String getStatut() { return statut; }
    public LocalDateTime getDateExpiration() { return dateExpiration; }

    // Setters
    public void setStatut(String statut) { this.statut = statut; }

    // Méthodes utilitaires
    public String getDateReservationFormatted() {
        return dateReservation.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public String getDateExpirationFormatted() {
        return dateExpiration.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(dateExpiration) && statut.equals("en attente");
    }

    public String getPrixFormatted() {
        return String.format("%.2f DH", prixArticle);
    }
}