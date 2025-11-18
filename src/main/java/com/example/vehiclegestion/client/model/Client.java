package com.example.vehiclegestion.client.model;


import java.time.LocalDateTime;

/**
 * Modèle représentant un client authentifié avec toutes ses spécificités
 */
public class Client {
    private int id;
    private String lastName;
    private String firstName;
    private String email;
    private String address;
    private String phone;
    private String paymentMethod;
    private String interests;
    private String profilePhoto;
    private String status;
    private LocalDateTime creationDate;

    public Client() {}

    public Client(int id, String lastName, String firstName, String email,
                  String address, String phone, String paymentMethod,
                  String interests, String profilePhoto, String status,
                  LocalDateTime creationDate) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.address = address;
        this.phone = phone;
        this.paymentMethod = paymentMethod;
        this.interests = interests;
        this.profilePhoto = profilePhoto;
        this.status = status;
        this.creationDate = creationDate;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getInterests() { return interests; }
    public void setInterests(String interests) { this.interests = interests; }

    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isPremium() {
        return "premium".equals(status);
    }
}