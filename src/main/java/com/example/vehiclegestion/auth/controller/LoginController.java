package com.example.vehiclegestion.auth.controller;

import com.example.vehiclegestion.client.model.Client;
import com.example.vehiclegestion.utils.NavigationController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    private Connection connection;

    public void setConnection(Connection connection) {
        this.connection = connection;
        System.out.println("✅ Connexion BD injectée dans LoginController");
    }

    @FXML
    public void initialize() {
        System.out.println("✅ LoginController initialisé");

        // Auto-remplir pour tests
        emailField.setText("test@automarket.com");
        passwordField.setText("test123");

        // Enter key pour connexion
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        try {
            // Ici tu peux remplacer par la vraie vérification en DB
            if (email.equals("admin@test.com") && password.equals("admin")) {
                showAlert("Succès", "Connexion réussie pour : " + email);
            } else {
                showAlert("Erreur", "Email ou mot de passe incorrect");
            }

            // Rediriger vers le dashboard
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

    @FXML
    private void handleReset() {
        emailField.clear();
        passwordField.clear();
        System.out.println("Formulaire réinitialisé");
    }

    @FXML
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
