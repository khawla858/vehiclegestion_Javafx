package com.example.vehiclegestion.vendeur.model;

import java.time.LocalDateTime;

public class Commentaire {
    private int idCommentaire;
    private int idUtilisateur;  // ✅ Renommé
    private int idArticle;
    private double note;
    private String texteCommentaire;
    private LocalDateTime dateCommentaire;
    private String nomClient; // Pour l'affichage

    // Constructeurs
    public Commentaire() {
    }

    public Commentaire(int idClient, int idArticle, double note, String texteCommentaire) {
        this.idUtilisateur = idClient;
        this.idArticle = idArticle;
        this.note = note;
        this.texteCommentaire = texteCommentaire;
        this.dateCommentaire = LocalDateTime.now();
    }

    // Getters et Setters
    public int getIdCommentaire() {
        return idCommentaire;
    }

    public void setIdCommentaire(int idCommentaire) {
        this.idCommentaire = idCommentaire;
    }

    public int getIdUtilisateur() {  // ✅ Renommé
        return idUtilisateur;
    }

    public void setIdUtilisateur(int idUtilisateur) {  // ✅ Renommé
        this.idUtilisateur = idUtilisateur;
    }

    public int getIdArticle() {
        return idArticle;
    }

    public void setIdArticle(int idArticle) {
        this.idArticle = idArticle;
    }

    public double getNote() {
        return note;
    }

    public void setNote(double note) {
        this.note = note;
    }

    public String getTexteCommentaire() {
        return texteCommentaire;
    }

    public void setTexteCommentaire(String texteCommentaire) {
        this.texteCommentaire = texteCommentaire;
    }

    public LocalDateTime getDateCommentaire() {
        return dateCommentaire;
    }

    public void setDateCommentaire(LocalDateTime dateCommentaire) {
        this.dateCommentaire = dateCommentaire;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    @Override
    public String toString() {
        return "Commentaire{" +
                "idCommentaire=" + idCommentaire +
                ", idClient=" + idUtilisateur +
                ", idArticle=" + idArticle +
                ", note=" + note +
                ", texteCommentaire='" + texteCommentaire + '\'' +
                ", dateCommentaire=" + dateCommentaire +
                ", nomClient='" + nomClient + '\'' +
                '}';
    }
}