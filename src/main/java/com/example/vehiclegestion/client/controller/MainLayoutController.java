package com.example.vehiclegestion.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import java.io.IOException;

public class MainLayoutController {

    @FXML private StackPane contentArea;
    @FXML private Label userNameLabel;
    @FXML private Label vehiclesCount;
    @FXML private Label favoritesCount;

    // Références aux contrôleurs des pages
    private Object currentController;

    @FXML
    public void initialize() {
        System.out.println("✅ Main Layout initialisé");
        // Charger le tableau de bord par défaut
        showDashboard();
    }

    // Méthodes de navigation
    @FXML
    private void showDashboard() {
        loadPage("/com/example/vehiclegestion/client/views/DashboardView.fxml");
        setActiveMenu("dashboard");
    }

    @FXML
    private void showVehicles() {
        loadPage("/com/example/vehiclegestion/client/views/VehiclesView.fxml");
        setActiveMenu("vehicles");
    }

    @FXML
    private void showFavorites() {
        loadPage("/com/example/vehiclegestion/client/views/FavoritesView.fxml");
        setActiveMenu("favorites");
    }

    @FXML
    private void showHistory() {
        loadPage("/com/example/vehiclegestion/client/views/HistoryView.fxml");
        setActiveMenu("history");
    }

    @FXML
    private void showProfile() {
        loadPage("/com/example/vehiclegestion/client/views/ProfileView.fxml");
        setActiveMenu("profile");
    }

    @FXML
    private void logout() {
        System.out.println("🚪 Déconnexion de l'utilisateur");
        showInfo("Déconnexion", "Vous allez être redirigé vers la page de connexion.");
        // NavigationController.loadLogin();
    }

    // Méthode pour charger les pages dynamiquement
    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            // Stocker le contrôleur de la page actuelle
            currentController = loader.getController();

            // Mettre à jour le contenu
            contentArea.getChildren().setAll(page);

        } catch (IOException e) {
            showError("Erreur lors du chargement de la page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Méthode pour mettre en surbrillance le menu actif
    private void setActiveMenu(String activeMenu) {
        // Implémentez la logique pour mettre en surbrillance le menu actif
        System.out.println("Menu actif: " + activeMenu);
    }

    // Méthodes utilitaires
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Getters pour les contrôleurs enfants
    public Object getCurrentController() {
        return currentController;
    }
}