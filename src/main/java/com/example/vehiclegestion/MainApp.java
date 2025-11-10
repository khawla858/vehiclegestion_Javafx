package com.example.vehiclegestion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger le layout principal commun
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/example/vehiclegestion/view/auth/Login.fxml"
            ));

            Parent root = loader.load();

            // Titre et scène principale
            Scene scene = new Scene(root);
            primaryStage.setTitle("Système de Gestion des Ventes");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
