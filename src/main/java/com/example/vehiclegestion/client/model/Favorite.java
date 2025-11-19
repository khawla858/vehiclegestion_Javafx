package com.example.vehiclegestion.client.model;

import java.time.LocalDateTime;

public class Favorite {
    private int id;
    private int idClient;
    private int idArticle;
    private LocalDateTime dateAdded;

    // Informations du véhicule (pour affichage)
    private Vehicle vehicle;

    // Constructeurs
    public Favorite() {}

    public Favorite(int idClient, int idArticle) {
        this.idClient = idClient;
        this.idArticle = idArticle;
        this.dateAdded = LocalDateTime.now();
    }

    public Favorite(int id, int idClient, int idArticle, LocalDateTime dateAdded) {
        this.id = id;
        this.idClient = idClient;
        this.idArticle = idArticle;
        this.dateAdded = dateAdded;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public int getIdArticle() {
        return idArticle;
    }

    public void setIdArticle(int idArticle) {
        this.idArticle = idArticle;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public String toString() {
        return "Favorite{" +
                "id=" + id +
                ", idClient=" + idClient +
                ", idArticle=" + idArticle +
                ", dateAdded=" + dateAdded +
                '}';
    }
}