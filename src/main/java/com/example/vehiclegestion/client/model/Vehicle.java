package com.example.vehiclegestion.client.model;

import java.time.LocalDateTime;

public class Vehicle {
    private int id;
    private String title;
    private String description;
    private double price;
    private String category;
    private String state;
    private String image;
    private LocalDateTime dateAdded;
    private int sellerId;
    private String sellerName;

    // Constructeurs
    public Vehicle() {}

    public Vehicle(int id, String title, String description, double price,
                   String category, String state, String image,
                   LocalDateTime dateAdded, int sellerId, String sellerName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.state = state;
        this.image = image;
        this.dateAdded = dateAdded;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    private String sellerEmail;

    public String getSellerEmail() {
        return sellerEmail;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public LocalDateTime getDateAdded() { return dateAdded; }
    public void setDateAdded(LocalDateTime dateAdded) { this.dateAdded = dateAdded; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getFormattedPrice() {
        return String.format("%,.2f €", price);
    }
}