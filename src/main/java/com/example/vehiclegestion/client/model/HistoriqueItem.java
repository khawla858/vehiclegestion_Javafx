package com.example.vehiclegestion.client.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoriqueItem {
    private int idHistorique;
    private int idClient;
    private String typeAction;
    private Timestamp dateAction;
    private String details;
    private String articleTitre;
    private String articleMarque;
    private String articleModele;
    private String vendeurNom;
    private Double montant;
    private Integer note;
    private String statut;

    // Constructeur
    public HistoriqueItem() {
        this.details = "{}";
    }

    // Getters
    public int getIdHistorique() { return idHistorique; }
    public int getIdClient() { return idClient; }
    public String getTypeAction() { return typeAction; }
    public Timestamp getDateAction() { return dateAction; }
    public String getDetails() { return details; }
    public String getArticleTitre() { return articleTitre; }
    public String getArticleMarque() { return articleMarque; }
    public String getArticleModele() { return articleModele; }
    public String getVendeurNom() { return vendeurNom; }
    public Double getMontant() { return montant; }
    public Integer getNote() { return note; }
    public String getStatut() { return statut; }

    // Setters
    public void setIdHistorique(int idHistorique) { this.idHistorique = idHistorique; }
    public void setIdClient(int idClient) { this.idClient = idClient; }
    public void setTypeAction(String typeAction) { this.typeAction = typeAction; }
    public void setDateAction(Timestamp dateAction) { this.dateAction = dateAction; }
    public void setDetails(String details) { this.details = details != null ? details : "{}"; }
    public void setArticleTitre(String articleTitre) { this.articleTitre = articleTitre; }
    public void setArticleMarque(String articleMarque) { this.articleMarque = articleMarque; }
    public void setArticleModele(String articleModele) { this.articleModele = articleModele; }
    public void setVendeurNom(String vendeurNom) { this.vendeurNom = vendeurNom; }
    public void setMontant(Double montant) { this.montant = montant; }
    public void setNote(Integer note) { this.note = note; }
    public void setStatut(String statut) { this.statut = statut; }

    // Méthodes utilitaires


    public String getShortDate() {
        if (dateAction == null) return "";
        LocalDateTime dateTime = dateAction.toLocalDateTime();
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

    public String getIcone() {
        if (typeAction == null) return "📋";
        if (typeAction.equals("rendez_vous")) return "📅";
        if (typeAction.equals("vente")) return "💰";
        if (typeAction.equals("commentaire")) return "⭐";
        return "📋";
    }

    public String getCouleur() {
        if (typeAction == null) return "#95a5a6";
        if (typeAction.equals("rendez_vous")) return "#3498db";
        if (typeAction.equals("vente")) return "#2ecc71";
        if (typeAction.equals("commentaire")) return "#f39c12";
        return "#95a5a6";
    }

    public String getTypeDisplay() {
        if (typeAction == null) return "Action";
        if (typeAction.equals("rendez_vous")) return "Rendez-vous";
        if (typeAction.equals("vente")) return "Transaction";
        if (typeAction.equals("commentaire")) return "Avis";
        return "Action";
    }

    public String getDescription() {
        if (articleTitre != null && !articleTitre.isEmpty()) {
            return articleTitre;
        }
        return getTypeDisplay() + " enregistré";
    }

    public boolean hasMontant() {
        return montant != null && montant > 0;
    }




    public String getFormattedDate() {
        if (dateAction == null) return "";
        LocalDateTime dateTime = dateAction.toLocalDateTime();
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    public boolean hasNote() {
        return note != null && note > 0;
    }


}