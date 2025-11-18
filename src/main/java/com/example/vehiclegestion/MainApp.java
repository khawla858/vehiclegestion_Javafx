package com.example.vehiclegestion;

import com.example.vehiclegestion.utils.DatabaseConnection;
import com.example.vehiclegestion.utils.NavigationController;
import com.example.vehiclegestion.client.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.sql.Connection;

/**
 * Application principale AutoMarket
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        try {
            MainApp.primaryStage = primaryStage;

            // Initialiser la navigation
            NavigationController.setPrimaryStage(primaryStage);

            // Configuration de la fenêtre
            setupPrimaryStage();

            // Test de la base de données
            testDatabaseConnection();

            // Charger la page de login
            NavigationController.loadLogin();

            System.out.println("✅ Application AutoMarket démarrée avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur critique au démarrage: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur au démarrage", e.getMessage());
        }
    }

    private void setupPrimaryStage() {
        primaryStage.setTitle("AutoMarket - Connexion");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("🔌 Fermeture de l'application AutoMarket");
        });
    }

    private void testDatabaseConnection() {
        try {
            DatabaseConnection.getConnection();
            System.out.println("✅ Connexion à la base de données réussie");
        } catch (Exception e) {
            System.out.println("⚠️ Attention: Base de données non disponible - Mode démo activé");
        }
    }

    private void showErrorDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;

    }}