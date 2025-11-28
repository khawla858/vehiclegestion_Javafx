package com.example.vehiclegestion.vendeur.model;

public class Article {

    private int id;
    private String titre;
    private String description;
    private double prix;
    private double prixPromo;
    private int reduction;
    private String categorie;
    private String etat;
    private String image;
    private int idVendeur;

    // Nouveaux attributs
    private String marque;
    private String modele;
    private int annee;
    private int kilometrage;
    private String transmission;
    private String carburant;
    private int puissance;
    private String couleur;

    public Article() {}

    public Article(int id, String titre, String description, double prix, double prixPromo, int reduction,
                   String categorie, String etat, String image, int idVendeur,
                   String marque, String modele, int annee, int kilometrage,
                   String transmission, String carburant, int puissance, String couleur) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.prixPromo = prixPromo;
        this.reduction = reduction;
        this.categorie = categorie;
        this.etat = etat;
        this.image = image;
        this.idVendeur = idVendeur;
        this.marque = marque;
        this.modele = modele;
        this.annee = annee;
        this.kilometrage = kilometrage;
        this.transmission = transmission;
        this.carburant = carburant;
        this.puissance = puissance;
        this.couleur = couleur;
    }

    // Getters et Setters pour tous les attributs
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public double getPrixPromo() { return prixPromo; }
    public void setPrixPromo(double prixPromo) { this.prixPromo = prixPromo; }

    public int getReduction() { return reduction; }
    public void setReduction(int reduction) { this.reduction = reduction; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public String getEtat() { return etat; }
    public void setEtat(String etat) { this.etat = etat; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getIdVendeur() { return idVendeur; }
    public void setIdVendeur(int idVendeur) { this.idVendeur = idVendeur; }

    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }

    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }

    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }

    public int getKilometrage() { return kilometrage; }
    public void setKilometrage(int kilometrage) { this.kilometrage = kilometrage; }

    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }

    public String getCarburant() { return carburant; }
    public void setCarburant(String carburant) { this.carburant = carburant; }

    public int getPuissance() { return puissance; }
    public void setPuissance(int puissance) { this.puissance = puissance; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
}
