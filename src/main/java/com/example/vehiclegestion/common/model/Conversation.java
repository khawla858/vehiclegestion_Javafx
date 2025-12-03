package com.example.vehiclegestion.common.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Conversation {
    private int idConversation;
    private String typeConversation; // 'vendeur_client', 'admin_vendeur', 'admin_client'

    // Participants
    private Integer idVendeur;
    private Integer idClient;
    private Integer idAdmin;

    // Contexte
    private Integer idArticle;
    private Integer idPlainte;
    private String sujet;

    // Métadonnées
    private LocalDateTime dateCreation;
    private LocalDateTime dernierMessageDate;
    private String statut; // 'active', 'archivee', 'resolue', 'bloquee'

    // Informations complémentaires (pour l'affichage)
    private String nomInterlocuteur;
    private String prenomInterlocuteur;
    private String photoInterlocuteur;
    private String dernierMessage;
    private int nbMessagesNonLus;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Constructeurs
    public Conversation() {}

    public Conversation(int idConversation, String typeConversation, String statut) {
        this.idConversation = idConversation;
        this.typeConversation = typeConversation;
        this.statut = statut;
    }

    // Getters et Setters
    public int getIdConversation() {
        return idConversation;
    }

    public void setIdConversation(int idConversation) {
        this.idConversation = idConversation;
    }

    public String getTypeConversation() {
        return typeConversation;
    }

    public void setTypeConversation(String typeConversation) {
        this.typeConversation = typeConversation;
    }

    public Integer getIdVendeur() {
        return idVendeur;
    }

    public void setIdVendeur(Integer idVendeur) {
        this.idVendeur = idVendeur;
    }

    public Integer getIdClient() {
        return idClient;
    }

    public void setIdClient(Integer idClient) {
        this.idClient = idClient;
    }

    public Integer getIdAdmin() {
        return idAdmin;
    }

    public void setIdAdmin(Integer idAdmin) {
        this.idAdmin = idAdmin;
    }

    public Integer getIdArticle() {
        return idArticle;
    }

    public void setIdArticle(Integer idArticle) {
        this.idArticle = idArticle;
    }

    public Integer getIdPlainte() {
        return idPlainte;
    }

    public void setIdPlainte(Integer idPlainte) {
        this.idPlainte = idPlainte;
    }

    public String getSujet() {
        return sujet;
    }

    public void setSujet(String sujet) {
        this.sujet = sujet;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDernierMessageDate() {
        return dernierMessageDate;
    }

    public void setDernierMessageDate(LocalDateTime dernierMessageDate) {
        this.dernierMessageDate = dernierMessageDate;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNomInterlocuteur() {
        return nomInterlocuteur;
    }

    public void setNomInterlocuteur(String nomInterlocuteur) {
        this.nomInterlocuteur = nomInterlocuteur;
    }

    public String getPrenomInterlocuteur() {
        return prenomInterlocuteur;
    }

    public void setPrenomInterlocuteur(String prenomInterlocuteur) {
        this.prenomInterlocuteur = prenomInterlocuteur;
    }

    public String getPhotoInterlocuteur() {
        return photoInterlocuteur;
    }

    public void setPhotoInterlocuteur(String photoInterlocuteur) {
        this.photoInterlocuteur = photoInterlocuteur;
    }

    public String getDernierMessage() {
        return dernierMessage;
    }

    public void setDernierMessage(String dernierMessage) {
        this.dernierMessage = dernierMessage;
    }

    public int getNbMessagesNonLus() {
        return nbMessagesNonLus;
    }

    public void setNbMessagesNonLus(int nbMessagesNonLus) {
        this.nbMessagesNonLus = nbMessagesNonLus;
    }

    // Méthodes utilitaires
    public String getInterlocuteurComplet() {
        if (prenomInterlocuteur != null && nomInterlocuteur != null) {
            return prenomInterlocuteur + " " + nomInterlocuteur;
        }
        return nomInterlocuteur != null ? nomInterlocuteur : "Inconnu";
    }

    public String getDernierMessageDateFormatted() {
        if (dernierMessageDate == null) return "";

        LocalDateTime now = LocalDateTime.now();
        if (dernierMessageDate.toLocalDate().equals(now.toLocalDate())) {
            // Aujourd'hui : afficher l'heure uniquement
            return dernierMessageDate.format(TIME_FORMATTER);
        } else if (dernierMessageDate.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            // Hier
            return "Hier";
        } else if (dernierMessageDate.isAfter(now.minusDays(7))) {
            // Cette semaine : afficher le jour
            return dernierMessageDate.getDayOfWeek().toString();
        } else {
            // Plus ancien : afficher la date
            return dernierMessageDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    public String getDernierMessagePreview() {
        if (dernierMessage == null || dernierMessage.isEmpty()) {
            return "Aucun message";
        }
        // Limiter à 50 caractères
        if (dernierMessage.length() > 50) {
            return dernierMessage.substring(0, 47) + "...";
        }
        return dernierMessage;
    }

    public String getTypeConversationLabel() {
        switch (typeConversation) {
            case "vendeur_client":
                return "💬 Conversation Client";
            case "admin_vendeur":
                return "🛠️ Support Admin";
            case "admin_client":
                return "🆘 Assistance Client";
            default:
                return "💬 Conversation";
        }
    }

    public boolean hasUnreadMessages() {
        return nbMessagesNonLus > 0;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + idConversation +
                ", type='" + typeConversation + '\'' +
                ", interlocuteur='" + getInterlocuteurComplet() + '\'' +
                ", nonLus=" + nbMessagesNonLus +
                ", statut='" + statut + '\'' +
                '}';
    }
}