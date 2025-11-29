package com.example.vehiclegestion;
import com.example.vehiclegestion.logging.service.ElasticLogService;

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
        Parent root = loader.load();
        new ElasticLogService().sendLog("INFO", "Test connexion ES");

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
