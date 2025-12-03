package com.example.vehiclegestion.vendeur.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ✅ MODÈLE VENTE CORRIGÉ
 * Compatible avec VenteDAO et VentesListController
 */
public class Vente {

    // ============================================
    // ATTRIBUTS DE BASE
    // ============================================
    private int idVente;
    private int idClient;
    private int idVendeur;
    private int idArticle;
    private LocalDateTime dateVente;
    private double montantTotal;
    private String moyenPaiement;
    private String statutVente;

    // ============================================
    // ATTRIBUTS SUPPLÉMENTAIRES (JOINS)
    // ============================================
    private String nomClient;
    private String prenomClient;
    private String emailClient;
    private String nomArticle;
    private String nomMagasin;
    private int nbVentesClient;

    // ============================================
    // FORMATTER
    // ============================================
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ============================================
    // CONSTRUCTEURS
    // ============================================

    public Vente() {}

    public Vente(int idVente, int idClient, int idVendeur, int idArticle,
                 LocalDateTime dateVente, double montantTotal,
                 String moyenPaiement, String statutVente) {
        this.idVente = idVente;
        this.idClient = idClient;
        this.idVendeur = idVendeur;
        this.idArticle = idArticle;
        this.dateVente = dateVente;
        this.montantTotal = montantTotal;
        this.moyenPaiement = moyenPaiement;
        this.statutVente = statutVente;
    }

    // ============================================
    // GETTERS & SETTERS
    // ============================================

    public int getIdVente() {
        return idVente;
    }

    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public int getIdVendeur() {
        return idVendeur;
    }

    public void setIdVendeur(int idVendeur) {
        this.idVendeur = idVendeur;
    }

    public int getIdArticle() {
        return idArticle;
    }

    public void setIdArticle(int idArticle) {
        this.idArticle = idArticle;
    }

    public LocalDateTime getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
    }

    public double getMontantTotal() {
        return montantTotal;
    }

    public void setMontantTotal(double montantTotal) {
        this.montantTotal = montantTotal;
    }

    public String getMoyenPaiement() {
        return moyenPaiement;
    }

    public void setMoyenPaiement(String moyenPaiement) {
        this.moyenPaiement = moyenPaiement;
    }

    public String getStatutVente() {
        return statutVente;
    }

    public void setStatutVente(String statutVente) {
        this.statutVente = statutVente;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getPrenomClient() {
        return prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    public String getEmailClient() {
        return emailClient;
    }

    public void setEmailClient(String emailClient) {
        this.emailClient = emailClient;
    }

    public String getNomArticle() {
        return nomArticle;
    }

    public void setNomArticle(String nomArticle) {
        this.nomArticle = nomArticle;
    }

    public String getNomMagasin() {
        return nomMagasin;
    }

    public void setNomMagasin(String nomMagasin) {
        this.nomMagasin = nomMagasin;
    }

    public int getNbVentesClient() {
        return nbVentesClient;
    }

    public void setNbVentesClient(int nbVentesClient) {
        this.nbVentesClient = nbVentesClient;
    }

    // ============================================
    // MÉTHODES UTILITAIRES
    // ============================================

    /**
     * Retourne le nom complet du client (Prénom + Nom)
     */
    public String getClientComplet() {
        if (prenomClient != null && nomClient != null) {
            return prenomClient + " " + nomClient;
        }
        return nomClient != null ? nomClient : "Client inconnu";
    }

    /**
     * Formatte la date pour l'affichage (dd/MM/yyyy HH:mm)
     */
    public String getDateVenteFormatted() {
        return dateVente != null ? dateVente.format(DATE_FORMATTER) : "";
    }

    /**
     * Formatte le montant avec "DH"
     */
    public String getMontantFormatted() {
        return String.format("%.2f DH", montantTotal);
    }

    /**
     * Retourne un badge coloré pour le statut
     */
    public String getStatutBadge() {
        if (statutVente == null) return "❓ Inconnu";

        switch (statutVente.toLowerCase()) {
            case "terminée":
            case "terminee":
                return "✅ Terminée";
            case "en cours":
                return "⏳ En cours";
            case "annulée":
            case "annulee":
                return "❌ Annulée";
            default:
                return "❓ " + statutVente;
        }
    }

    /**
     * Vérifie si la vente est terminée
     */
    public boolean estTerminee() {
        return "terminée".equalsIgnoreCase(statutVente) ||
                "terminee".equalsIgnoreCase(statutVente);
    }

    /**
     * Vérifie si la vente est annulée
     */
    public boolean estAnnulee() {
        return "annulée".equalsIgnoreCase(statutVente) ||
                "annulee".equalsIgnoreCase(statutVente);
    }

    /**
     * Vérifie si la vente est en cours
     */
    public boolean estEnCours() {
        return "en cours".equalsIgnoreCase(statutVente);
    }

    @Override
    public String toString() {
        return "Vente{" +
                "idVente=" + idVente +
                ", client='" + getClientComplet() + '\'' +
                ", article='" + nomArticle + '\'' +
                ", magasin='" + nomMagasin + '\'' +
                ", montant=" + montantTotal +
                ", statut='" + statutVente + '\'' +
                ", date=" + getDateVenteFormatted() +
                '}';
    }
}