package com.example.vehiclegestion.utils;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Stack;

/**
 *  GESTIONNAIRE DE NAVIGATION CENTRALISÉ
 */
public class NavigationManager {

    private static NavigationManager instance;
    private StackPane contentPane;
    private Stack<String> navigationHistory;

    private NavigationManager() {
        navigationHistory = new Stack<>();
    }

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void setContentPane(StackPane contentPane) {
        this.contentPane = contentPane;
        System.out.println("✅ NavigationManager initialisé avec contentPane");
    }

    // ========================================
    //  MÉTHODES DE NAVIGATION PRINCIPALES
    // ========================================

    public void navigateTo(String fxmlPath) {
        navigateTo(fxmlPath, true);
    }

    public void navigateTo(String fxmlPath, boolean withAnimation) {
        if (contentPane == null) {
            System.err.println("❌ ContentPane non initialisé !");
            showError("Erreur de navigation", "Le gestionnaire n'est pas initialisé.");
            return;
        }

        System.out.println("🔄 Navigation vers: " + fxmlPath);

        if (!navigationHistory.isEmpty()) {
            String dernierePage = navigationHistory.peek();
            if (!dernierePage.equals(fxmlPath)) {
                navigationHistory.push(fxmlPath);
            }
        } else {
            navigationHistory.push(fxmlPath);
        }

        if (withAnimation) {
            navigateWithAnimation(fxmlPath);
        } else {
            navigateDirectly(fxmlPath);
        }
    }

    private void navigateWithAnimation(String fxmlPath) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), contentPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(e -> {
            loadPage(fxmlPath);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), contentPane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    private void navigateDirectly(String fxmlPath) {
        loadPage(fxmlPath);
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();
            contentPane.getChildren().setAll(content);
            System.out.println("✅ Page chargée: " + fxmlPath);

        } catch (IOException ex) {
            System.err.println("❌ Erreur chargement page: " + ex.getMessage());
            ex.printStackTrace();
            showErrorContent("Impossible de charger la page: " + fxmlPath);
        }
    }

    /**
     * Navigation avec données (pour passer des objets entre pages)
     */
    public <T> void navigateWithData(String fxmlPath, Object data) {
        if (contentPane == null) {
            System.err.println("❌ ContentPane non initialisé !");
            return;
        }

        try {
            System.out.println("🔄 Navigation avec données vers: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node content = loader.load();

            // Passer les données au contrôleur
            T controller = loader.getController();
            if (controller instanceof DataReceiver) {
                ((DataReceiver) controller).receiveData(data);
            }

            contentPane.getChildren().setAll(content);
            navigationHistory.push(fxmlPath);
            System.out.println("✅ Page chargée avec données");

        } catch (IOException ex) {
            System.err.println("❌ Erreur chargement: " + ex.getMessage());
            ex.printStackTrace();
            showErrorContent("Erreur: " + fxmlPath);
        }
    }

    // ========================================
    //  RACCOURCIS DE NAVIGATION
    // ========================================

    public void goToDashboard() {
        navigateTo("/view/vendeur/VendeurDashboard.fxml");
    }

    public void goToClients() {
        navigateTo("/view/vendeur/VentesList.fxml");
    }

    public void goToVehicles() {
        navigateTo("/view/client/vehicles-view.fxml");
    }

    public void goToMagasins() {
        navigateTo("/view/vendeur/MagasinList.fxml");
    }

    public void goToMagasinDetails() {
        navigateTo("/view/vendeur/MagasinDetails.fxml");
    }

    public void goToReservations() {
        navigateTo("/view/vendeur/reservations_vendeur.fxml");
    }

    public void goToRendezVous() {
        navigateTo("/view/vendeur/RendezVousList.fxml");
    }
    public void showMessages() {
        navigateTo("/view/common/ChatWindow.fxml");
    }

    public void addVehicle() {
        navigateTo("/view/vendeur/AddVehicleForm.fxml");
    }


    /**
     * ⬅ Retour à la page précédente
     */
    public void goBack() {
        if (navigationHistory.size() > 1) {
            navigationHistory.pop();
            String previousPage = navigationHistory.peek();
            System.out.println("⬅️ Retour vers: " + previousPage);
            navigateDirectly(previousPage);
        } else {
            System.out.println("⬅️ Déjà à la première page, retour au Dashboard");
            goToDashboard();
        }
    }

    /**
     *  Rafraîchir la page actuelle
     */
    public void refresh() {
        if (!navigationHistory.isEmpty()) {
            String currentPage = navigationHistory.peek();
            System.out.println("🔄 Rafraîchissement: " + currentPage);
            navigateDirectly(currentPage);
        }
    }

    // ========================================
    //  GESTION DES ERREURS
    // ========================================

    private void showErrorContent(String message) {
        javafx.scene.control.Label errorLabel = new javafx.scene.control.Label("❌ " + message);
        errorLabel.setStyle(
                "-fx-text-fill: #e74c3c; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 50; " +
                        "-fx-alignment: center;"
        );
        contentPane.getChildren().setAll(errorLabel);
        contentPane.setOpacity(1.0);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========================================
    //  INFORMATIONS
    // ========================================

    public String getCurrentPage() {
        return navigationHistory.isEmpty() ? null : navigationHistory.peek();
    }

    public boolean canGoBack() {
        return navigationHistory.size() > 1;
    }

    public void clearHistory() {
        navigationHistory.clear();
        System.out.println("🗑️ Historique de navigation effacé");
    }






}