package com.example.vehiclegestion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Chargez la page de login au lieu du dashboard vendeur
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/vehiclegestion/view/auth/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Connexion - Gestion Véhicules");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}