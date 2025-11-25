package com.example.vehiclegestion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("🚀 Démarrage de l'application VehicleGestion...");

        try {
            // Charger le fichier FXML de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/vehiclegestion/view/auth/login.fxml"));
            Parent root = loader.load();

            // Configurer la scène
            Scene scene = new Scene(root, 900, 700);

            // Ajouter le CSS si disponible
            try {
                scene.getStylesheets().add(getClass().getResource("/com/example/vehiclegestion/css/client-dashboard.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("⚠️ CSS non trouvé, continuation sans style...");
            }

            // Configurer la fenêtre principale
            primaryStage.setTitle("AutoSales Pro - Gestion de Véhicules");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Icône de l'application
            try {
                Image icon = new Image(getClass().getResourceAsStream("/com/example/vehiclegestion/images/car-logo.PNG"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                System.out.println("⚠️ Icône non trouvée");
            }

            primaryStage.show();
            System.out.println("✅ Application démarrée avec succès !");

        } catch (Exception e) {
            System.err.println("❌ Erreur critique au démarrage: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur de démarrage", "Impossible de charger l'interface: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("=== AutoSales Pro ===");
        System.out.println("📦 Version: 1.0.0");
        System.out.println("🚀 Lancement de l'application...");

        // Vérifier que JavaFX est disponible
        try {
            Class.forName("javafx.application.Application");
            System.out.println("✅ JavaFX détecté");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ JavaFX non disponible!");
            System.exit(1);
        }

        launch(args);
    }

    private void showErrorDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }}