package com.example.vehiclegestion.auth.controller;

import com.example.vehiclegestion.auth.AuthentificationService;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.auth.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private CheckBox rememberCheckbox;

    private AuthentificationService authService;

    public LoginController() {
        this.authService = new AuthentificationService();
    }

    @FXML
    private void initialize() {
        System.out.println("✅ LoginController initialisé");
        errorLabel.setVisible(false);
        emailField.requestFocus();
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("⚠️ Veuillez remplir tous les champs");
            return;
        }

        try {
            System.out.println("🔐 Tentative de connexion: " + email);
            Utilisateur user = authService.seConnecter(email, password);

            if (user != null) {
                System.out.println("✅ Connexion réussie: " + user.getEmail());

                // ⭐⭐ DÉMARRER LA SESSION AVANT LA REDIRECTION ⭐⭐
                SessionManager.getInstance().demarrerSession(user);

                // Vérifier que la session est bien démarrée
                SessionManager.getInstance().debugSession();

                redirectToDashboard(user);
            } else {
                showError("❌ Email ou mot de passe incorrect");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur connexion: " + e.getMessage());
            showError("❌ Erreur lors de la connexion");
        }
    }

    /**
     * Rediriger vers le dashboard approprié selon le rôle
     */
    private void redirectToDashboard(Utilisateur user) {
        try {
            // ⭐⭐ AJOUTER CETTE LIGNE CRITIQUE ⭐⭐
            SessionManager.getInstance().demarrerSession(user);
            String fxmlPath = "";
            String title = "";

            // ✅ CHEMINS CORRECTS SELON VOTRE ARBORESCENCE
            switch (user.getRole().toLowerCase()) {
                case "admin":
                    fxmlPath = "/view/admin/admin-main.fxml";
                    title = "Admin Panel - Gestion Véhicules";
                    break;

                case "vendeur":
                    // ⚠️ VÉRIFIEZ LE NOM EXACT DU FICHIER
                    // D'après votre arborescence, c'est "VendeurDashboard.fxml"
                    fxmlPath = "/view/vendeur/layout/vendeur-layout.fxml";
                    title = "Dashboard Vendeur - Gestion Véhicules";
                    break;

                case "client":
                    fxmlPath = "/view/client/main-layout.fxml";
                    title = "Marketplace - Gestion Véhicules";
                    break;

                default:
                    showError("❌ Rôle utilisateur non reconnu: " + user.getRole());
                    System.err.println("❌ Rôle inconnu: " + user.getRole());
                    return;
            }

            // Vérifier que le fichier existe
            if (getClass().getResource(fxmlPath) == null) {
                System.err.println("❌ FICHIER FXML NON TROUVÉ: " + fxmlPath);
                System.err.println("📁 Vérifiez que le fichier existe dans: src/main/resources" + fxmlPath);
                showError("❌ Page de destination non disponible: " + fxmlPath);
                return;
            }

            System.out.println("📂 Chargement de: " + fxmlPath);

            // Charger la vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Changer de scène
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root, 1400, 800);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setMaximized(true);
            stage.centerOnScreen();

            System.out.println("✅ Redirection réussie vers: " + user.getRole());

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement FXML: " + e.getMessage());
            e.printStackTrace();
            showError("❌ Impossible de charger le dashboard: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur redirection: " + e.getMessage());
            e.printStackTrace();
            showError("❌ Erreur lors de la redirection");
        }
    }

    @FXML
    private void handleGoToRegister() {
        System.out.println("📝 Redirection vers inscription");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/Register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Inscription - Gestion Véhicules");
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement inscription: " + e.getMessage());
            e.printStackTrace();
            showError("Impossible de charger la page d'inscription");
        }
    }

    @FXML
    private void handleForgotPassword() {
        System.out.println("🔑 Mot de passe oublié cliqué");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/ForgotPassword.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Mot de passe oublié - Gestion Véhicules");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement mot de passe oublié: " + e.getMessage());
            e.printStackTrace();
            showError("Fonctionnalité non disponible");
        }
    }

    @FXML
    private void handleMicrosoftLogin() {
        System.out.println("🔵 Microsoft login cliqué");
        showError("Connexion Microsoft non disponible pour le moment");
    }

    /**
     * Afficher un message d'erreur
     */
    private void showError(String message) {
        System.err.println("⚠️ " + message);
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Effacer après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(this::hideError);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Masquer le message d'erreur
     */
    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    /**
     * Valider le format email
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}