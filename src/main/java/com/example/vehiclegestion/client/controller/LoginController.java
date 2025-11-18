package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.client.model.Client;
import com.example.vehiclegestion.utils.NavigationController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.time.LocalDateTime;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {
        System.out.println("✅ LoginController initialisé");
        emailField.setText("test@automarket.com");
        passwordField.setText("test123");
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        System.out.println("🔄 Tentative de connexion...");
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        try {
            System.out.println("✅ Connexion réussie: " + email);
            NavigationController.loadDashboard();
        } catch (Exception e) {
            System.err.println("❌ Erreur connexion: " + e.getMessage());
            showAlert("Erreur", "Échec de la connexion: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        System.out.println("📝 Inscription demandée");
        showAlert("Inscription", "Fonctionnalité d'inscription à implémenter");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}