package com.example.vehiclegestion.client.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Contrôleur de navigation spécifique aux clients authentifiés
 * Vérifie la session avant chaque navigation
 */
public class ClientNavigationController {

    private static Stage primaryStage;
    private static ClientSession clientSession = ClientSession.getInstance();

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Charge une vue client avec vérification de session
     */
    public static void loadClientView(String fxmlPath) {
        if (!clientSession.isLoggedIn()) {
            System.err.println("❌ Accès refusé: Client non authentifié");
            loadLogin();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(ClientNavigationController.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("✅ Navigation client vers: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("❌ Erreur de navigation client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Navigation spécifique client
    public static void loadClientDashboard() {
        loadClientView("/view/client/client-dashboard.fxml");
    }

    public static void loadClientVehicles() {
        loadClientView("/view/client/client-vehicles.fxml");
    }

    public static void loadClientFavorites() {
        loadClientView("/view/client/client-favorites.fxml");
    }

    public static void loadClientHistory() {
        loadClientView("/view/client/client-history.fxml");
    }

    public static void loadClientProfile() {
        loadClientView("/view/client/client-profile.fxml");
    }

    public static void loadLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(ClientNavigationController.class.getResource("/view/auth/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logout() {
        clientSession.logout();
        loadLogin();
    }
}