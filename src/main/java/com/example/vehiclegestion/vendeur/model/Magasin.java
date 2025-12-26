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
    private Map<String, String> horairesOuverture;
    private String siteWeb;
    private String facebook;
    private String instagram;
    private String horaires;

    // Champ supplémentaire pour la catégorie
    private String categorie;
    private String imagePath;

    // ✅ NOUVEAUX CHAMPS POUR STATISTIQUES DYNAMIQUES
    private int nbVehicules;
    private int nbVehiculesDisponibles;
    private int nbVehiculesReserves;
    private int nbVehiculesVendus;
    private int nbCommentaires;
    private int nbRdvAvenir;
    private double noteMoyenne;
    private int nbVentesMois;

    public Magasin() {}

    public Magasin(String nom, String adresse, String localisation, String description, int idVendeur) {
        this.nomMagasin = nom;
        this.adresse = adresse;
        this.localisation = localisation;
        this.description = description;
        this.idVendeur = idVendeur;
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
    // Getters & Setters des champs supplémentaires
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

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getHoraires() {
        return horaires;
    }

    public void setHoraires(String horaires) {
        this.horaires = horaires;
    }

    // ==========================================
    // Getters & Setters pour les statistiques
    // ==========================================

    public int getNbVehicules() {
        return nbVehicules;
    }

    public void setNbVehicules(int nbVehicules) {
        this.nbVehicules = nbVehicules;
    }

    public int getNbVehiculesDisponibles() {
        return nbVehiculesDisponibles;
    }

    public void setNbVehiculesDisponibles(int nbVehiculesDisponibles) {
        this.nbVehiculesDisponibles = nbVehiculesDisponibles;
    }

    public int getNbVehiculesReserves() {
        return nbVehiculesReserves;
    }

    public void setNbVehiculesReserves(int nbVehiculesReserves) {
        this.nbVehiculesReserves = nbVehiculesReserves;
    }

    public int getNbVehiculesVendus() {
        return nbVehiculesVendus;
    }

    public void setNbVehiculesVendus(int nbVehiculesVendus) {
        this.nbVehiculesVendus = nbVehiculesVendus;
    }

    public int getNbCommentaires() {
        return nbCommentaires;
    }

    public void setNbCommentaires(int nbCommentaires) {
        this.nbCommentaires = nbCommentaires;
    }

    public int getNbRdvAvenir() {
        return nbRdvAvenir;
    }

    public void setNbRdvAvenir(int nbRdvAvenir) {
        this.nbRdvAvenir = nbRdvAvenir;
    }

    public double getNoteMoyenne() {
        return noteMoyenne;
    }

    public void setNoteMoyenne(double noteMoyenne) {
        this.noteMoyenne = noteMoyenne;
    }

    public int getNbVentesMois() {
        return nbVentesMois;
    }

    public void setNbVentesMois(int nbVentesMois) {
        this.nbVentesMois = nbVentesMois;
    }

    // ==========================================
    // Méthodes utilitaires
    // ==========================================

    public boolean hasHoraires() {
        return horairesOuverture != null && !horairesOuverture.isEmpty();
    }

    public boolean hasLogo() {
        return logoMagasin != null && !logoMagasin.isEmpty();
    }

    public boolean hasSocialMedia() {
        return (facebook != null && !facebook.isEmpty()) ||
                (instagram != null && !instagram.isEmpty());
    }

    public String getFacebookUrl() {
        return facebook;
    }

    public void setFacebookUrl(String facebook) {
        this.facebook = facebook;
    }

    public double getPrixMinimum() {
        return 17.0;
    }

    public void setPrixMinimum(double prixMinimum) {
        // Implémentez si nécessaire
    }

    public String getInstagramUrl() {
        return instagram;
    }

    public void setInstagramUrl(String instagram) {
        this.instagram = instagram;
    }

    /**
     * ✅ Méthode pour formater la note moyenne
     */
    public String getFormattedRating() {
        return String.format("%.1f", noteMoyenne);
    }

    /**
     * ✅ Méthode pour obtenir le statut du magasin
     */
    public String getStatutMagasin() {
        if (nbVehiculesDisponibles > 0) {
            return "Actif";
        } else if (nbVehicules > 0) {
            return "En attente";
        } else {
            return "Inactif";
        }
    }

    /**
     * ✅ Méthode pour obtenir le pourcentage de véhicules disponibles
     */
    public int getPourcentageDisponibles() {
        if (nbVehicules == 0) return 0;
        return (nbVehiculesDisponibles * 100) / nbVehicules;
    }

    @Override
    public String toString() {
        return "Magasin{" +
                "idMagasin=" + idMagasin +
                ", nomMagasin='" + nomMagasin + '\'' +
                ", adresse='" + adresse + '\'' +
                ", localisation='" + localisation + '\'' +
                ", telephone='" + telephone + '\'' +
                ", categorie='" + categorie + '\'' +
                ", nbVehicules=" + nbVehicules +
                ", noteMoyenne=" + noteMoyenne +
                '}';
    }
}