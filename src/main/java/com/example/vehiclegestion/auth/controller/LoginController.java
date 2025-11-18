package com.example.vehiclegestion.auth.controller;

import com.example.vehiclegestion.auth.dao.UtilisateurDAO;
import com.example.vehiclegestion.auth.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    /**
     * Gérer la connexion
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Format d'email invalide");
            return;
        }

        // Tentative de connexion
        Utilisateur user = utilisateurDAO.seConnecter(email, password);

        if (user != null) {
            hideError();

            // Redirection selon le rôle
            try {
                switch (user.getRole()) {
                    case "client":
                        redirectToClientDashboard(user);
                        break;
                    case "vendeur":
                        redirectToVendeurDashboard(user);
                        break;
                    case "admin":
                        redirectToAdminDashboard(user);
                        break;
                    default:
                        showError("Rôle utilisateur inconnu");
                }
            } catch (IOException e) {
                showError("Erreur lors du chargement du tableau de bord");
                e.printStackTrace();
            }
        } else {
            showError("Email ou mot de passe incorrect");
        }
    }

    /**
     * Gérer le mot de passe oublié
     */
    @FXML
    private void handleForgotPassword() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/vehiclegestion/view/auth/ForgotPassword.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Fonctionnalité en cours de développement");
            e.printStackTrace();
        }
    }

    /**
     * Connexion avec Microsoft
     */
    @FXML
    private void handleMicrosoftLogin() {
        showError("Connexion Microsoft en cours de développement");
    }

    /**
     * Redirection vers le dashboard client
     */
    private void redirectToClientDashboard(Utilisateur user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/vehiclegestion/view/client/ClientDashboard.fxml"));
        Parent root = loader.load();

        // Passer les données utilisateur au contrôleur
        // ClientDashboardController controller = loader.getController();
        // controller.setUser(user);

        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Gestion Véhicules - Client");
        stage.setMaximized(true);
    }

    /**
     * Redirection vers le dashboard vendeur
     */
    private void redirectToVendeurDashboard(Utilisateur user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/VendeurDashboard.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) emailField.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Gestion Véhicules - Vendeur");
        stage.setMaximized(true);
    }

    /**
     * Redirection vers le dashboard admin
     */
    private void redirectToAdminDashboard(Utilisateur user) throws IOException {
        // TODO: Créer le dashboard admin
        showError("Dashboard admin en cours de développement");
    }

    /**
     * Aller vers la page d'inscription
     */




    @FXML
    private void handleGoToRegister() {
        debugRegisterPath(); // Activez le débogage

        try {
            // Essai 1 - Chemin principal
            System.out.println("Tentative de chargement Register.fxml...");
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/vehiclegestion/view/auth/Register.fxml"));

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Inscription - Gestion Véhicules");
            System.out.println("✅ Redirection réussie vers Register!");

        } catch (IOException e1) {
            System.err.println("❌ Erreur chemin 1: " + e1.getMessage());

            try {
                // Essai 2 - Chemin alternatif
                Parent root = FXMLLoader.load(getClass().getResource("/view/auth/Register.fxml"));

                Stage stage = (Stage) emailField.getScene().getWindow();
                stage.setScene(new Scene(root, 1000, 700));
                stage.setTitle("Inscription - Gestion Véhicules");
                System.out.println("✅ Redirection réussie avec chemin alternatif!");

            } catch (IOException e2) {
                System.err.println("❌ Erreur chemin 2: " + e2.getMessage());

                // Afficher l'erreur détaillée
                showError("Register.fxml introuvable. Vérifiez la console.");
                e2.printStackTrace();
            }
        }
    }

    /**
     * Validation email
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Afficher une erreur
     */
    private void showError(String message) {
        errorLabel.setText("⚠️ " + message);
        errorLabel.setVisible(true);
    }

    /**
     * Masquer l'erreur
     */
    private void hideError() {
        errorLabel.setVisible(false);
    }
    private void debugRegisterPath() {
        System.out.println("=== DÉBOGAGE CHEMIN REGISTER ===");

        String[] paths = {
                "/com/example/vehiclegestion/view/auth/Register.fxml",
                "/view/auth/Register.fxml",
                "com/example/vehiclegestion/view/auth/Register.fxml",
                "/auth/Register.fxml",
                "Register.fxml"
        };

        for (String path : paths) {
            try {
                java.net.URL url = getClass().getResource(path);
                System.out.println("Test: '" + path + "' → " + (url != null ? "✅ TROUVÉ" : "❌ NON TROUVÉ"));
                if (url != null) {
                    System.out.println("   URL: " + url);
                }
            } catch (Exception e) {
                System.out.println("   ❌ Erreur: " + e.getMessage());
            }
        }

        // Test supplémentaire avec ClassLoader
        System.out.println("=== TEST CLASSLOADER ===");
        try {
            java.net.URL url = getClass().getClassLoader().getResource("com/example/vehiclegestion/view/auth/Register.fxml");
            System.out.println("ClassLoader: " + (url != null ? "✅ TROUVÉ" : "❌ NON TROUVÉ"));
            if (url != null) {
                System.out.println("   URL: " + url);
            }
        } catch (Exception e) {
            System.out.println("   ❌ Erreur: " + e.getMessage());
        }
    }



}