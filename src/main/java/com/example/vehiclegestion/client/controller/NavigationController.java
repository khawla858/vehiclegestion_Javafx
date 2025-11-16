
package com.example.vehiclegestion.client.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Contrôleur pour gérer la navigation entre les différentes vues
 */
public class NavigationController {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Charge une vue FXML dans la scène principale
     */
    public static void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("✅ Navigation vers: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("❌ Erreur de navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthodes spécifiques pour chaque vue
     */
    public static void loadDashboard() {
        loadView("/com/example/vehiclegestion/view/client/client-dashboard.fxml");
    }

    public static void loadFavorites() {
        loadView("/com/example/vehiclegestion/view/client/favorites.fxml");
    }

    public static void loadVehicles() {
        loadView("main/resources/com/example/vehiclegestion/view/client/vehicles-view.fxml");
    }

    public static void loadHistory() {
        loadView("/com/example/vehiclegestion/view/client/history.fxml");
    }

    public static void loadProfile() {
        loadView("/com/example/vehiclegestion/view/client/profile.fxml");
    }

    public static void loadLogin() {
        loadView("/com/example/vehiclegestion/view/auth/login.fxml");
    }
}