package com.example.vehiclegestion.vendeur.model;

import java.time.LocalDate;

public class Client {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private String status; // Prospect, Actif, Inactif, Acheteur
    private LocalDate dateCreation;
    private LocalDate dernierContact;
    private int vendeurId;
    private boolean isBuyer;
    private int nbVentes;
    private double totalDepense;
    private LocalDate dernierAchat;

    // Constructeurs
    public Client() {}

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getFullName() { return prenom + " " + nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation) { this.dateCreation = dateCreation; }

    public LocalDate getLastContact() { return dernierContact; }
    public void setLastContact(LocalDate dernierContact) { this.dernierContact = dernierContact; }

    public int getVendeurId() { return vendeurId; }
    public void setVendeurId(int vendeurId) { this.vendeurId = vendeurId; }

    public boolean isBuyer() { return isBuyer; }
    public void setBuyer(boolean buyer) { isBuyer = buyer; }
    public String getStatutClient() {
        return status;
    }

    public void setStatutClient(String statutClient) {
        this.status = statutClient;
    }
    public int getNbVentes() { return nbVentes; }
    public void setNbVentes(int nbVentes) { this.nbVentes = nbVentes; }

    public double getTotalDepense() { return totalDepense; }
    public void setTotalDepense(double totalDepense) { this.totalDepense = totalDepense; }

    public LocalDate getDernierAchat() { return dernierAchat; }
    public void setDernierAchat(LocalDate dernierAchat) { this.dernierAchat = dernierAchat; }
}