package com.example.vehiclegestion.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class FavoritesController {

    @FXML
    private Label pageTitle;

    @FXML
    public void initialize() {
        System.out.println("✅ Page Favoris initialisée");
        if (pageTitle != null) {
            pageTitle.setText("Mes Véhicules Favoris ❤️");
        }
    }

    // Méthode pour retourner au dashboard
    @FXML
    private void goBackToDashboard() {
        System.out.println("↩️ Retour au tableau de bord");
        NavigationController.loadDashboard();
    }

    // Méthode pour voir les véhicules
    @FXML
    private void browseVehicles() {
        System.out.println("🚗 Navigation vers tous les véhicules");
        NavigationController.loadVehicles();
    }

    // Exemple de méthode pour gérer les favoris
    @FXML
    private void removeFromFavorites() {
        System.out.println("🗑️ Suppression d'un favori");
        showInfo("Favoris", "Véhicule retiré des favoris");
    }

    @FXML
    private void contactSeller() {
        System.out.println("📞 Contact du vendeur");
        showInfo("Contact", "Formulaire de contact ouvert");
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}