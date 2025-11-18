package com.example.vehiclegestion.vendeur.model;

public class Magasin {

    private int idMagasin;
    private String nomMagasin;
    private String adresse;
    private String localisation;
    private String description;
    private int idVendeur;

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

    public int getIdMagasin() { return idMagasin; }
    public String getNomMagasin() { return nomMagasin; }
    public String getAdresse() { return adresse; }
    public String getLocalisation() { return localisation; }
    public String getDescription() { return description; }
    public int getIdVendeur() { return idVendeur; }

    public void setNomMagasin(String nomMagasin) { this.nomMagasin = nomMagasin; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }
    public void setDescription(String description) { this.description = description; }
    public void setIdMagasin(int id){this.idMagasin=id;}
}
