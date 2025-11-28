package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label userNameLabel;
    @FXML private TextField searchField;
    @FXML private Button vehiclesBtn;
    @FXML private Button favoritesBtn;
    @FXML private Button historyBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;

    private SessionManager sessionManager = SessionManager.getInstance();

    @FXML
    public void initialize() {
        System.out.println("🚀 MainController initialisé");

        if (!sessionManager.estConnecte()) {
            redirectToLogin();
            return;
        }

        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

        System.out.println("👤 Utilisateur connecté: " + currentUser.getPrenom() + " " + currentUser.getNom());

        // Charger la page véhicules par défaut
        showVehicles();

        // Initialiser la recherche
        initializeSearch();
    }

    private void initializeSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 2) {
                performSearch(newValue);
            }
        });
    }

    private void performSearch(String query) {
        System.out.println("🔍 Recherche globale: " + query);
        // La recherche sera gérée dans chaque contrôleur spécifique
    }

    @FXML
    private void showVehicles() {
        System.out.println("🚗 Navigation vers Véhicules");
        setActiveMenu(vehiclesBtn);
        loadContent("/view/client/vehicles-view.fxml");
    }

    @FXML
    private void showFavorites() {
        System.out.println("❤️ Navigation vers Favoris");
        setActiveMenu(favoritesBtn);
        loadContent("/view/client/ClientFavoritesView.fxml");
    }

    @FXML
    private void showHistory() {
        System.out.println("📊 Navigation vers Historique");
        setActiveMenu(historyBtn);
        // Pour l'instant, rediriger vers les favoris en attendant de créer history-view.fxml
        loadContent("/view/client/ClientFavoritesView.fxml");
    }

    @FXML
    private void showProfile() {
        System.out.println("👤 Navigation vers Profil");
        setActiveMenu(profileBtn);
        loadContent("/view/client/profile-view.fxml");
    }

    @FXML
    private void logout() {
        System.out.println("🚪 Déconnexion");
        sessionManager.fermerSession();
        redirectToLogin();
    }

    private void setActiveMenu(Button activeButton) {
        // Réinitialiser tous les boutons
        vehiclesBtn.setStyle(getInactiveStyle());
        favoritesBtn.setStyle(getInactiveStyle());
        historyBtn.setStyle(getInactiveStyle());
        profileBtn.setStyle(getInactiveStyle());

        // Activer le bouton sélectionné
        if (activeButton != null) {
            activeButton.setStyle(getActiveStyle());
        }
    }

    private String getInactiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #2c3e50; " +
                "-fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 18 20; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: transparent;";
    }

    private String getActiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #3498db; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 18 20; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: #3498db;";
    }

    private void loadContent(String fxmlPath) {
        try {
            System.out.println("📁 Chargement du contenu: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            mainBorderPane.setCenter(content);
            System.out.println("✅ Contenu chargé avec succès: " + fxmlPath);

        } catch (IOException e) {
            System.err.println("❌ Erreur de chargement: " + fxmlPath);
            showErrorPage("Page non disponible: " + fxmlPath);
        }
    }

    private void showErrorPage(String message) {
        VBox errorBox = new VBox();
        errorBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 20; -fx-alignment: center; -fx-spacing: 10;");

        Label errorLabel = new Label("⚠️ " + message);
        errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");

        Label infoLabel = new Label("Cette page est en cours de développement");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");

        errorBox.getChildren().addAll(errorLabel, infoLabel);
        mainBorderPane.setCenter(errorBox);
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
            Parent loginPage = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(loginPage));
            stage.setTitle("Connexion - AutoSales Pro");
            stage.setMaximized(false);
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("❌ Erreur critique lors de la redirection vers login");
        }
    }
}