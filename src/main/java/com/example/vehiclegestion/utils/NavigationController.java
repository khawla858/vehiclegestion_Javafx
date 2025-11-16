package com.example.vehiclegestion.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationController {
    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadFXML(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1200, 800);
            primaryStage.setScene(scene);
            primaryStage.show();

            System.out.println("✅ Navigation vers: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("❌ Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadLogin() {
        loadFXML("/com/example/vehiclegestion/view/auth/login.fxml");
    }

    public static void loadDashboard() {
        loadFXML("/com/example/vehiclegestion/view/client/client-dashboard.fxml");
    }
}