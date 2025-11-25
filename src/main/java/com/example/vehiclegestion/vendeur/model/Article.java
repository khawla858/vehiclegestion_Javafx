package com.example.vehiclegestion.vendeur.model;

public class Article {
    private int id;
    private String titre;
    private String description;
    private double prix;
    private String categorie;
    private String etat;
    private String image;

    // --- Constructeurs ---
    public Article() {}

    public Article(int id, String titre, String description, double prix, String categorie, String etat, String image) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.prix = prix;
        this.categorie = categorie;
        this.etat = etat;
        this.image = image;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public double getPrix() { return prix; }
    public String getCategorie() { return categorie; }
    public String getEtat() { return etat; }
    public String getImage() { return "/"+image; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setTitre(String titre) { this.titre = titre; }
    public void setDescription(String description) { this.description = description; }
    public void setPrix(double prix) { this.prix = prix; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
    public void setEtat(String etat) { this.etat = etat; }
    public void setImage(String image) { this.image = image; }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", prix=" + prix +
                ", categorie='" + categorie + '\'' +
                ", etat='" + etat + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
