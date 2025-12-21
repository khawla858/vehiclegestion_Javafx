package com.example.vehiclegestion.common.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
    private int idNotification;
    private int idUtilisateur;
    private String roleDestinataire;
    private Integer idSource;
    private String typeSource;
    private String titre;
    private String message;
    private String typeNotification;
    private String categorie;
    private boolean estLue;
    private LocalDateTime dateCreation;
    private LocalDateTime dateLecture;
    private String priorite;
    private String lienAction;
    private String dataContext; // JSON formaté

    // Constructeur
    public Notification() {}

    public Notification(int idUtilisateur, String roleDestinataire, String titre,
                        String message, String typeNotification, String categorie) {
        this.idUtilisateur = idUtilisateur;
        this.roleDestinataire = roleDestinataire;
        this.titre = titre;
        this.message = message;
        this.typeNotification = typeNotification;
        this.categorie = categorie;
        this.estLue = false;
        this.dateCreation = LocalDateTime.now();
        this.priorite = "normale";
    }

    // Getters et Setters
    public int getIdNotification() { return idNotification; }
    public void setIdNotification(int idNotification) { this.idNotification = idNotification; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getRoleDestinataire() { return roleDestinataire; }
    public void setRoleDestinataire(String roleDestinataire) { this.roleDestinataire = roleDestinataire; }

    public Integer getIdSource() { return idSource; }
    public void setIdSource(Integer idSource) { this.idSource = idSource; }

    public String getTypeSource() { return typeSource; }
    public void setTypeSource(String typeSource) { this.typeSource = typeSource; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTypeNotification() { return typeNotification; }
    public void setTypeNotification(String typeNotification) { this.typeNotification = typeNotification; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public boolean isEstLue() { return estLue; }
    public void setEstLue(boolean estLue) { this.estLue = estLue; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateLecture() { return dateLecture; }
    public void setDateLecture(LocalDateTime dateLecture) { this.dateLecture = dateLecture; }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) { this.priorite = priorite; }

    public String getLienAction() { return lienAction; }
    public void setLienAction(String lienAction) { this.lienAction = lienAction; }

    public String getDataContext() { return dataContext; }
    public void setDataContext(String dataContext) { this.dataContext = dataContext; }

    // Méthodes utilitaires
    public String getDateCreationFormatted() {
        if (dateCreation == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateCreation.format(formatter);
    }

    public String getTimeAgo() {
        if (dateCreation == null) return "";
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateCreation, now).toMinutes();

        if (minutes < 1) return "À l'instant";
        if (minutes < 60) return "Il y a " + minutes + " min";
        if (minutes < 1440) return "Il y a " + (minutes / 60) + " h";
        return "Il y a " + (minutes / 1440) + " j";
    }

    public String getIcon() {
        switch (categorie) {
            case "vehicule": return "🚗";
            case "transaction": return "💰";
            case "message": return "💬";
            case "favori": return "⭐";
            case "statistique": return "📊";
            default: return "🔔";
        }
    }

    public String getStyleClass() {
        switch (priorite) {
            case "urgente": return "notification-urgent";
            case "haute": return "notification-high";
            case "basse": return "notification-low";
            default: return "notification-normal";
        }
    }
}