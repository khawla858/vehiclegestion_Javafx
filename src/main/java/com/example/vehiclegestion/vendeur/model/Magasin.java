package com.example.vehiclegestion.vendeur.model;

public class Magasin {

    private int idMagasin;
    private String nomMagasin;
    private String adresse;
    private String localisation;
    private String description;
    private int idVendeur;
    private int nbVentesMensuelles;

    private String logoMagasin;
    private String telephone;
    private String emailContact;

    // ---------- Horaires stockés en JSON ----------
    private String horaires;  // EX: {"Lundi":"09:00-18:00", "Dimanche":"Fermé"}

    private String siteWeb;
    private String facebook;
    private String instagram;
    private String categorie;
    private String imagePath;

    private int nbCommentaires;

    public Magasin() {}

    public Magasin(String nom, String adresse, String localisation, String description, int idVendeur) {
        this.nomMagasin = nom;
        this.adresse = adresse;
        this.localisation = localisation;
        this.description = description;
        this.idVendeur = idVendeur;
    }

    // =======================
    // Getters / Setters
    // =======================

    public int getIdMagasin() { return idMagasin; }
    public void setIdMagasin(int idMagasin) { this.idMagasin = idMagasin; }

    public String getNomMagasin() { return nomMagasin; }
    public void setNomMagasin(String nomMagasin) { this.nomMagasin = nomMagasin; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getIdVendeur() { return idVendeur; }
    public void setIdVendeur(int idVendeur) { this.idVendeur = idVendeur; }

    public int getNbVentesMensuelles() { return nbVentesMensuelles; }
    public void setNbVentesMensuelles(int nbVentesMensuelles) { this.nbVentesMensuelles = nbVentesMensuelles; }

    public String getLogoMagasin() { return logoMagasin; }
    public void setLogoMagasin(String logoMagasin) { this.logoMagasin = logoMagasin; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmailContact() { return emailContact; }
    public void setEmailContact(String emailContact) { this.emailContact = emailContact; }

    public String getHoraires() { return horaires; }
    public void setHoraires(String horaires) { this.horaires = horaires; }

    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }

    public String getFacebook() { return facebook; }
    public void setFacebook(String facebook) { this.facebook = facebook; }

    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getNbCommentaires() { return nbCommentaires; }
    public void setNbCommentaires(int nbCommentaires) { this.nbCommentaires = nbCommentaires; }

    // =======================
    // Helpers
    // =======================

    public boolean hasLogo() { return logoMagasin != null && !logoMagasin.isEmpty(); }

    public boolean hasSocialMedia() {
        return (facebook != null && !facebook.isEmpty()) ||
                (instagram != null && !instagram.isEmpty());
    }

    @Override
    public String toString() {
        return "Magasin{" +
                "idMagasin=" + idMagasin +
                ", nomMagasin='" + nomMagasin + '\'' +
                ", adresse='" + adresse + '\'' +
                ", localisation='" + localisation + '\'' +
                '}';
    }
}
