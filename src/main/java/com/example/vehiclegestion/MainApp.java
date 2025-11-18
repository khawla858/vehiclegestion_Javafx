package com.example.vehiclegestion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/VendeurDashboard.fxml"));
        Parent root = null;   // Charge le FXML une seule fois
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Scene scene = new Scene(root); // Utilise root ici
        stage.setScene(scene);
        stage.setTitle("Dashboard Vendeur");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
