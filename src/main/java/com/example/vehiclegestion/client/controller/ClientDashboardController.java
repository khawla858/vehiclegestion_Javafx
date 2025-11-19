package com.example.vehiclegestion.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ClientDashboardController {

    // Éléments de la barre supérieure (top)
    @FXML private Label userNameLabel;
    @FXML private TextField searchField; // Si vous ajoutez un fx:id dans le FXML

    // Boutons du menu de navigation
    @FXML private Button dashboardBtn;
    @FXML private Button vehiclesBtn;
    @FXML private Button favoritesBtn;
    @FXML private Button historyBtn;
    @FXML private Button profileBtn;
    @FXML private Button publishBtn;
    @FXML private Button logoutBtn;

    // Zone de contenu principal
    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        System.out.println("✅ AutoSales Pro Dashboard initialisé");

        // Vérifier que contentArea est bien initialisé
        if (contentArea == null) {
            System.err.println("❌ ERREUR: contentArea est null! Vérifiez le fx:id dans le FXML");
        } else {
            System.out.println("✅ contentArea initialisé avec succès");
        }

        // Initialiser les données utilisateur
        userNameLabel.setText("Technicien");

        // Définir le dashboard comme actif par défaut
        if (dashboardBtn != null) {
            setActiveMenu(dashboardBtn);
            loadDashboardContent(); // Charger le contenu du dashboard
        }

        // Initialiser les autres éléments de la barre supérieure si nécessaire
        initializeTopBar();
    }

    // Méthode pour initialiser la barre supérieure
    private void initializeTopBar() {
        // Vous pouvez ajouter des écouteurs pour la barre de recherche
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                // Logique de recherche en temps réel
                System.out.println("Recherche: " + newValue);
            });
        }

        // Initialiser les infos utilisateur
        System.out.println("✅ Barre supérieure initialisée - Utilisateur: " + userNameLabel.getText());
    }

    // ========== MÉTHODES DE NAVIGATION (appelées par les boutons du menu) ==========

    @FXML
    private void showDashboard() {
        System.out.println("📊 Navigation vers le tableau de bord");
        setActiveMenu(dashboardBtn);
        loadDashboardContent();
    }

    @FXML
    private void showVehicles() {
        System.out.println("🚗 Navigation vers la liste des véhicules");
        setActiveMenu(vehiclesBtn);
        loadPage("/view/client/vehicles-view.fxml");
    }

    @FXML
    private void showFavorites() {
        System.out.println("❤️ Navigation vers les favoris");
        setActiveMenu(favoritesBtn);
        showInfo("Fonctionnalité", "Les favoris seront disponibles prochainement");
        loadPage("/view/client/vehicles-view.fxml");
    }

    @FXML
    private void showHistory() {
        System.out.println("📊 Navigation vers l'historique");
        setActiveMenu(historyBtn);
        showInfo("Fonctionnalité", "L'historique sera disponible prochainement");
        loadPage("/view/client/vehicles-view.fxml");
    }

    @FXML
    private void showProfile() {
        System.out.println("👤 Navigation vers le profil");
        setActiveMenu(profileBtn);
        showInfo("Fonctionnalité", "Le profil sera disponible prochainement");
        loadPage("/view/client/vehicles-view.fxml");
    }

    @FXML
    private void publishVehicle() {
        System.out.println("➕ Publication d'un véhicule");
        showInfo("Publication", "Fonctionnalité de publication bientôt disponible!");
        // Ici vous pouvez charger une page de publication si elle existe
        // loadPage("/com/example/vehiclegestion/view/client/publish-vehicle.fxml");
    }

    @FXML
    private void logout() {
        System.out.println("🚪 Déconnexion de l'utilisateur");
        showInfo("Déconnexion", "Vous allez être redirigé vers la page de connexion.");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
            Parent loginPage = loader.load();
            userNameLabel.getScene().setRoot(loginPage);
        } catch (IOException e) {
            showError("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }

    // ========== MÉTHODES UTILITAIRES POUR LA BARRE SUPÉRIEURE ==========

    // Méthode pour gérer le menu actif (ORANGE)
    private void setActiveMenu(Button activeButton) {
        // Vérifier que tous les boutons sont initialisés
        if (dashboardBtn == null || vehiclesBtn == null || favoritesBtn == null ||
                historyBtn == null || profileBtn == null) {
            System.err.println("⚠️ Certains boutons ne sont pas initialisés");
            return;
        }

        // Style pour bouton inactif (gris)
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #333; -fx-padding: 12 0; " +
                "-fx-cursor: hand; -fx-font-size: 13; -fx-border-width: 0;";

        // Style pour bouton actif (orange)
        String activeStyle = "-fx-background-color: transparent; -fx-text-fill: #FF6B35; -fx-font-weight: bold; " +
                "-fx-padding: 12 0; -fx-border-color: #FF6B35; -fx-border-width: 0 0 3 0; " +
                "-fx-cursor: hand; -fx-font-size: 13;";

        // Réinitialiser tous les boutons à inactif
        dashboardBtn.setStyle(inactiveStyle);
        vehiclesBtn.setStyle(inactiveStyle);
        favoritesBtn.setStyle(inactiveStyle);
        historyBtn.setStyle(inactiveStyle);
        profileBtn.setStyle(inactiveStyle);

        // Activer le bouton sélectionné
        if (activeButton != null) {
            activeButton.setStyle(activeStyle);
            System.out.println("✅ Menu actif: " + activeButton.getText());
        }
    }

    // Méthode pour charger le contenu du dashboard
    private void loadDashboardContent() {
        try {
            // Créer un contenu simple pour le dashboard
            VBox dashboardContent = new VBox(20);
            dashboardContent.setStyle("-fx-padding: 40; -fx-alignment: CENTER;");

            Label welcomeLabel = new Label("Bienvenue sur AutoSales Pro");
            welcomeLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #333;");

            Label statsLabel = new Label("Tableau de bord en construction");
            statsLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #666;");

            dashboardContent.getChildren().addAll(welcomeLabel, statsLabel);

            if (contentArea != null) {
                contentArea.getChildren().setAll(dashboardContent);
                System.out.println("✅ Tableau de bord chargé");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement dashboard: " + e.getMessage());
        }
    }

    // Méthode pour charger les pages dynamiquement
    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            if (contentArea != null) {
                contentArea.getChildren().setAll(page);
                System.out.println("✅ Page chargée avec succès: " + fxmlPath);
            } else {
                System.err.println("❌ Impossible de charger la page: contentArea est null");
                showError("Erreur de navigation: contentArea non initialisé");
            }
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la page: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de chargement: " + e.getMessage());

            // Fallback: créer un contenu simple
            Label errorLabel = new Label("Page en construction: " + fxmlPath);
            errorLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #7f8c8d;");
            if (contentArea != null) {
                contentArea.getChildren().setAll(errorLabel);
            }
        }
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
}