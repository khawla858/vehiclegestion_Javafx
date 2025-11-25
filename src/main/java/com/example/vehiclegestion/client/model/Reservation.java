package com.example.vehiclegestion.client.model;

import java.time.LocalDateTime;

public class Reservation {
    private int idReservation;
    private int clientId;
    private int vehiculeId;
    private String vehiculeTitre;
    private double vehiculePrix;
    private LocalDateTime dateReservation;
    private String statut;
    private LocalDateTime dateExpiration;

    public Reservation() {}

    public Reservation(int idReservation, int clientId, int vehiculeId, String vehiculeTitre,
                       double vehiculePrix, LocalDateTime dateReservation, String statut,
                       LocalDateTime dateExpiration) {
        this.idReservation = idReservation;
        this.clientId = clientId;
        this.vehiculeId = vehiculeId;
        this.vehiculeTitre = vehiculeTitre;
        this.vehiculePrix = vehiculePrix;
        this.dateReservation = dateReservation;
        this.statut = statut;
        this.dateExpiration = dateExpiration;
    }

    // Getters et Setters
    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }

    public int getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(int vehiculeId) { this.vehiculeId = vehiculeId; }

    public String getVehiculeTitre() { return vehiculeTitre; }
    public void setVehiculeTitre(String vehiculeTitre) { this.vehiculeTitre = vehiculeTitre; }

    public double getVehiculePrix() { return vehiculePrix; }
    public void setVehiculePrix(double vehiculePrix) { this.vehiculePrix = vehiculePrix; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDateTime dateExpiration) { this.dateExpiration = dateExpiration; }

    // Méthodes utilitaires
    public boolean isExpired() {
        return dateExpiration != null && LocalDateTime.now().isAfter(dateExpiration);
    }

    public boolean isActive() {
        return "en attente".equals(statut) || "confirmée".equals(statut);
    }

    public boolean isPending() {
        return "en attente".equals(statut);
    }

    public boolean isConfirmed() {
        return "confirmée".equals(statut);
    }

    public boolean isCancelled() {
        return "annulée".equals(statut);
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "idReservation=" + idReservation +
                ", vehicule='" + vehiculeTitre + '\'' +
                ", prix=" + vehiculePrix +
                ", dateReservation=" + dateReservation +
                ", statut='" + statut + '\'' +
                ", dateExpiration=" + dateExpiration +
                '}';
    }
}