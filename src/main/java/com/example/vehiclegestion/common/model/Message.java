package com.example.vehiclegestion.common.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private int idMessage;
    private int idConversation;
    private int idExpediteur;
    private String roleExpediteur;
    private String contenu;
    private String typeMessage;
    private String fichierUrl;
    private boolean estLu;
    private LocalDateTime dateEnvoi;
    private LocalDateTime dateLecture;

    // Informations supplémentaires pour l'affichage
    private String prenomExpediteur;
    private String nomExpediteur;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Constructeurs
    public Message() {}

    // Getters et Setters
    public int getIdMessage() {
        return idMessage;
    }

    public void setIdMessage(int idMessage) {
        this.idMessage = idMessage;
    }

    public int getIdConversation() {
        return idConversation;
    }

    public void setIdConversation(int idConversation) {
        this.idConversation = idConversation;
    }

    public int getIdExpediteur() {
        return idExpediteur;
    }

    public void setIdExpediteur(int idExpediteur) {
        this.idExpediteur = idExpediteur;
    }

    public String getRoleExpediteur() {
        return roleExpediteur;
    }

    public void setRoleExpediteur(String roleExpediteur) {
        this.roleExpediteur = roleExpediteur;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(String typeMessage) {
        this.typeMessage = typeMessage;
    }

    public String getFichierUrl() {
        return fichierUrl;
    }

    public void setFichierUrl(String fichierUrl) {
        this.fichierUrl = fichierUrl;
    }

    public boolean isEstLu() {
        return estLu;
    }

    public void setEstLu(boolean estLu) {
        this.estLu = estLu;
    }

    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public LocalDateTime getDateLecture() {
        return dateLecture;
    }

    public void setDateLecture(LocalDateTime dateLecture) {
        this.dateLecture = dateLecture;
    }

    public String getPrenomExpediteur() {
        return prenomExpediteur;
    }

    public void setPrenomExpediteur(String prenomExpediteur) {
        this.prenomExpediteur = prenomExpediteur;
    }

    public String getNomExpediteur() {
        return nomExpediteur;
    }

    public void setNomExpediteur(String nomExpediteur) {
        this.nomExpediteur = nomExpediteur;
    }

    // Méthodes utilitaires
    public String getHeureEnvoi() {
        if (dateEnvoi == null) return "";
        return dateEnvoi.format(TIME_FORMATTER);
    }

    public String getDateEnvoiFormatted() {
        if (dateEnvoi == null) return "";

        LocalDateTime now = LocalDateTime.now();
        if (dateEnvoi.toLocalDate().equals(now.toLocalDate())) {
            return "Aujourd'hui " + dateEnvoi.format(TIME_FORMATTER);
        } else if (dateEnvoi.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            return "Hier " + dateEnvoi.format(TIME_FORMATTER);
        } else {
            return dateEnvoi.format(DATE_TIME_FORMATTER);
        }
    }

    public String getExpediteurComplet() {
        if (prenomExpediteur != null && nomExpediteur != null) {
            return prenomExpediteur + " " + nomExpediteur;
        }
        return nomExpediteur != null ? nomExpediteur : "Utilisateur";
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + idMessage +
                ", expediteur='" + getExpediteurComplet() + '\'' +
                ", contenu='" + contenu + '\'' +
                ", lu=" + estLu +
                '}';
    }
}