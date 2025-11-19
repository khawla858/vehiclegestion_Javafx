package com.example.vehiclegestion.vendeur.model;

import java.util.Map;
import java.util.HashMap;

public class Magasin {

    private int idMagasin;
    private String nomMagasin;
    private String adresse;
    private String localisation;
    private String description;
    private int idVendeur;
    private int nbVentesMensuelles;

    // Nouvelles colonnes
    private String logoMagasin;
    private String telephone;
    private String emailContact;
    private Map<String, String> horairesOuverture; // pour JSONB
    private String siteWeb;
    private String facebook;
    private String instagram;

    // Champ supplémentaire pour la catégorie (si nécessaire)
    private String categorie;
    private String imagePath; // Chemin vers l'image

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    // Constructeurs
    public Magasin() {}

    public Magasin(String nom, String adresse, String localisation, String description, int idVendeur) {
        this.nomMagasin = nom;
        this.adresse = adresse;
        this.localisation = localisation;
        this.description = description;
        this.idVendeur = idVendeur;
    }
    private int nbCommentaires;

    // ... getter et setter ...
    public int getNbCommentaires() {
        return nbCommentaires;
    }

    public void setNbCommentaires(int nbCommentaires) {
        this.nbCommentaires = nbCommentaires;
    }
    public Magasin(int id, String nom, String adresse, String localisation, String description, int idVendeur) {
        this.idMagasin = id;
        this.nomMagasin = nom;
        this.adresse = adresse;
        this.localisation = localisation;
        this.description = description;
        this.idVendeur = idVendeur;
    }

    // ==========================================
    // Getters & Setters de base
    // ==========================================

    public int getIdMagasin() {
        return idMagasin;
    }

    public void setIdMagasin(int idMagasin) {
        this.idMagasin = idMagasin;
    }

    public String getNomMagasin() {
        return nomMagasin;
    }

    public void setNomMagasin(String nomMagasin) {
        this.nomMagasin = nomMagasin;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIdVendeur() {
        return idVendeur;
    }

    public void setIdVendeur(int idVendeur) {
        this.idVendeur = idVendeur;
    }

    public int getNbVentesMensuelles() {
        return nbVentesMensuelles;
    }

    public void setNbVentesMensuelles(int nbVentesMensuelles) {
        this.nbVentesMensuelles = nbVentesMensuelles;
    }

    // ==========================================
    // Getters & Setters des nouveaux champs
    // ==========================================

    public String getLogoMagasin() {
        return logoMagasin;
    }

    public void setLogoMagasin(String logoMagasin) {
        this.logoMagasin = logoMagasin;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmailContact() {
        return emailContact;
    }

    public void setEmailContact(String emailContact) {
        this.emailContact = emailContact;
    }

    public Map<String, String> getHorairesOuverture() {
        return horairesOuverture;
    }

    public void setHorairesOuverture(Map<String, String> horairesOuverture) {
        this.horairesOuverture = horairesOuverture;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    // ==========================================
    // MÉTHODES MANQUANTES - À AJOUTER
    // ==========================================

    /**
     * Retourne la catégorie du magasin
     */
    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    /**
     * Alias pour getFacebook() - pour compatibilité avec le contrôleur
     */
    public String getFacebookUrl() {
        return facebook;
    }

    public void setFacebookUrl(String facebook) {
        this.facebook = facebook;
    }
    public double getPrixMinimum() {
        return 17.0; // Valeur par défaut ou depuis la base de données
    }

    public void setPrixMinimum(double prixMinimum) {
        // Implémentez si nécessaire
    }

    /**
     * Alias pour getInstagram() - pour compatibilité avec le contrôleur
     */
    public String getInstagramUrl() {
        return instagram;
    }

    public void setInstagramUrl(String instagram) {
        this.instagram = instagram;
    }

    // ==========================================
    // Méthodes utilitaires
    // ==========================================

    /**
     * Vérifie si le magasin a des horaires définis
     */
    public boolean hasHoraires() {
        return horairesOuverture != null && !horairesOuverture.isEmpty();
    }

    /**
     * Vérifie si le magasin a un logo
     */
    public boolean hasLogo() {
        return logoMagasin != null && !logoMagasin.isEmpty();
    }

    /**
     * Vérifie si le magasin a des réseaux sociaux
     */
    public boolean hasSocialMedia() {
        return (facebook != null && !facebook.isEmpty()) ||
                (instagram != null && !instagram.isEmpty());
    }


    /**
     * Retourne une représentation textuelle du magasin
     */
    @Override
    public String toString() {
        return "Magasin{" +
                "idMagasin=" + idMagasin +
                ", nomMagasin='" + nomMagasin + '\'' +
                ", adresse='" + adresse + '\'' +
                ", localisation='" + localisation + '\'' +
                ", telephone='" + telephone + '\'' +
                ", categorie='" + categorie + '\'' +
                '}';
    }
}