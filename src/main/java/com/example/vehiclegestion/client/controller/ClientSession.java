package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.client.model.Client;

/**
 * Gestionnaire de session pour les clients authentifiés
 */
public class ClientSession {
    private static ClientSession instance;
    private Client currentClient;
    private boolean isLoggedIn = false;

    private ClientSession() {}

    public static ClientSession getInstance() {
        if (instance == null) {
            instance = new ClientSession();
        }
        return instance;
    }

    public void login(Client client) {
        this.currentClient = client;
        this.isLoggedIn = true;
        System.out.println("✅ Client connecté: " + client.getFullName() + " (ID: " + client.getId() + ")");
    }

    public void logout() {
        this.currentClient = null;
        this.isLoggedIn = false;
        System.out.println("🔒 Client déconnecté");
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public Client getCurrentClient() {
        return currentClient;
    }

    public int getClientId() {
        return currentClient != null ? currentClient.getId() : -1;
    }

    public String getClientName() {
        return currentClient != null ? currentClient.getFullName() : "Invité";
    }

    public String getClientEmail() {
        return currentClient != null ? currentClient.getEmail() : "";
    }
}