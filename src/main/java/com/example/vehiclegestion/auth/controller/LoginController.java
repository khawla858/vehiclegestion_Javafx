package com.example.vehiclegestion.auth.controller;

import com.example.vehiclegestion.auth.AuthentificationService;
import com.example.vehiclegestion.auth.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

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

        // Effacer le message d'erreur au démarrage
        errorLabel.setVisible(false);

        // Optionnel: Focus sur le champ email
        emailField.requestFocus();
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        try {
            Utilisateur user = authService.seConnecter(email, password);

            if (user != null) {
                System.out.println("✅ Connexion réussie: " + user.getEmail() + " (Rôle: " + user.getRole() + ")");

                // ✅ REDIRECTION SELON LE RÔLE
                Stage stage = (Stage) emailField.getScene().getWindow();
                FXMLLoader loader;
                String titre;

                switch (user.getRole().toLowerCase()) {
                    case "admin":
                        // Charger l'interface admin
                        loader = new FXMLLoader(getClass().getResource("/view/admin/admin-main.fxml"));
                        titre = "Admin Panel - Gestion Véhicules";
                        break;

                    case "vendeur":
                        // Charger l'interface vendeur
                        loader = new FXMLLoader(getClass().getResource("/view/vendeur/dashboard-vendeur.fxml"));
                        titre = "Dashboard Vendeur - Gestion Véhicules";
                        break;

                    case "client":
                        // Charger l'interface client
                        loader = new FXMLLoader(getClass().getResource("/view/client/home-client.fxml"));
                        titre = "Marketplace - Gestion Véhicules";
                        break;

                    default:
                        showError( "Rôle utilisateur non reconnu");
                        return;
                }

                Parent root = loader.load();
                Scene scene = new Scene(root, 1400, 800);
                stage.setScene(scene);
                stage.setTitle(titre);
                stage.setMaximized(true);

            } else {
                showError("Email ou mot de passe incorrect");
            }

        } catch (Exception e) {
            showError("Erreur lors de la connexion: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void handleGoToRegister() {
        System.out.println("📝 Redirection vers inscription");
        try {
            // Charger la vue d'inscription
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/Register.fxml"));
            Parent root = loader.load();

            // Récupérer la scène actuelle
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Inscription - Gestion Véhicules");
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement inscription: " + e.getMessage());
            showError("Impossible de charger la page d'inscription");
        }
    }

    @FXML
    private void handleForgotPassword() {
        System.out.println("🔑 Mot de passe oublié cliqué");
        try {
            // Charger la vue mot de passe oublié
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/ForgotPassword.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("Mot de passe oublié - Gestion Véhicules");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement mot de passe oublié: " + e.getMessage());
            showError("Fonctionnalité non disponible");
        }
    }

    @FXML
    private void handleMicrosoftLogin() {
        System.out.println("🔵 Microsoft login cliqué");
        showError("Connexion Microsoft non disponible pour le moment");
        // Implémentation future
    }

    private void redirectToDashboard(Utilisateur user) {
        try {
            String fxmlPath = "";
            String title = "";

            switch (user.getRole()) {
                case "admin":
                    fxmlPath = "/view/admin/admin-dashboard.fxml";
                    title = "Tableau de bord Admin";
                    break;
                case "vendeur":
                    fxmlPath = "/view/vendeur/VendeurDashboard.fxml";
                    title = "Tableau de bord Vendeur";
                    break;
                case "client":
                    fxmlPath = "/view/client/main-layout.fxml";
                    title = "Tableau de bord Client";
                    break;
                default:
                    showError("Rôle utilisateur inconnu");
                    return;
            }

            // Vérifier si le fichier existe
            if (getClass().getResource(fxmlPath) == null) {
                System.err.println("❌ Fichier FXML non trouvé: " + fxmlPath);
                showError("Page de destination non disponible");
                return;
            }

            // Charger la vue
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle(title);
            stage.centerOnScreen();

            System.out.println("✅ Redirection vers: " + user.getRole());

        } catch (Exception e) {
            System.err.println("❌ Erreur redirection: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors de la redirection: " + e.getLocalizedMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        // Effacer après 5 secondes
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                javafx.application.Platform.runLater(() -> {
                    hideError();
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // Méthode pour tester depuis MainApp si besoin
    public void testConnection() {
        System.out.println("🧪 Test connexion depuis LoginController");
    }
}