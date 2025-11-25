package com.example.vehiclegestion.auth.controller;

import com.example.vehiclegestion.auth.dao.UtilisateurDAO;
import com.example.vehiclegestion.auth.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    // Champs du formulaire
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    // RadioButtons pour le rôle
    @FXML private RadioButton clientRadio;
    @FXML private RadioButton vendeurRadio;

    // Message d'erreur
    @FXML private Label errorLabel;

    private ToggleGroup roleGroup;
    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @FXML
    public void initialize() {
        System.out.println("✅ RegisterController initialisé");

        // Créer le ToggleGroup programmatiquement
        roleGroup = new ToggleGroup();
        clientRadio.setToggleGroup(roleGroup);
        vendeurRadio.setToggleGroup(roleGroup);
        clientRadio.setSelected(true);

        // Données de test pour le développement
        if (isDevelopmentMode()) {
            nomField.setText("Dupont");
            prenomField.setText("Jean");
            emailField.setText("jean.dupont@email.com");
            passwordField.setText("test123");
            confirmPasswordField.setText("test123");
        }
    }

    /**
     * Gérer l'inscription
     */
    @FXML
    private void handleRegister() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String role = getSelectedRole();

        // VALIDATION
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        if (!isValidEmail(email)) {
            showError("Format d'email invalide");
            return;
        }

        if (password.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }

        try {
            // Vérifier email existant
            if (utilisateurDAO.emailExists(email)) {
                showError("Cet email est déjà utilisé");
                return;
            }

            // Créer objet utilisateur (sans mot de passe dans le constructeur)
            Utilisateur newUser = new Utilisateur();
            newUser.setNom(nom);
            newUser.setPrenom(prenom);
            newUser.setEmail(email);
            newUser.setRole(role);

            // Utiliser la méthode CORRECTE : inscrireUtilisateur
            if (utilisateurDAO.inscrire(newUser, password)) {
                hideError();
                showSuccessAndRedirect();
            } else {
                showError("Erreur durant l'inscription");
            }

        } catch (Exception e) {
            showError("Erreur système : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mode développement - Inscription mockée
     */
    private boolean registerMockUser(String nom, String prenom, String email, String password, String role) {
        try {
            System.out.println("✅ INSCRIPTION MOCK RÉUSSIE:");
            System.out.println("   👤 Nom: " + nom + " " + prenom);
            System.out.println("   📧 Email: " + email);
            System.out.println("   🔑 Mot de passe: " + password);
            System.out.println("   🎯 Rôle: " + role);
            System.out.println("   🕒 Date: " + java.time.LocalDateTime.now());

            // Simuler un délai de traitement
            Thread.sleep(1000);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Erreur mode mock: " + e.getMessage());
            return false;
        }
    }

    /**
     * Afficher succès et rediriger vers login
     */
    private void showSuccessAndRedirect() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inscription réussie");
        alert.setHeaderText(null);
        alert.setContentText("✅ Votre compte a été créé avec succès !\nVous pouvez maintenant vous connecter.");
        alert.showAndWait();

        // Rediriger vers la page de login
        handleGoToLogin();
    }

    /**
     * Retour à la page de connexion
     */
    @FXML
    private void handleGoToLogin() {
        try {
            System.out.println("🔄 Redirection vers la page de login...");
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/vehiclegestion/view/auth/Login.fxml"));
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root, 1000, 700));
            stage.setTitle("Connexion - Gestion Véhicules");
            System.out.println("✅ Redirection vers login réussie");
        } catch (IOException e) {
            System.err.println("❌ Erreur lors du chargement de la page de connexion: " + e.getMessage());
            showError("Erreur de navigation: " + e.getMessage());
        }
    }

    /**
     * Obtenir le rôle sélectionné
     */
    private String getSelectedRole() {
        if (vendeurRadio.isSelected()) {
            return "vendeur";
        }
        return "client"; // Par défaut
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
        errorLabel.setText("❌ " + message);
        errorLabel.setVisible(true);
        System.err.println("🚨 Erreur inscription: " + message);
    }

    /**
     * Masquer l'erreur
     */
    private void hideError() {
        errorLabel.setVisible(false);
    }

    /**
     * Vérifier si on est en mode développement
     */
    private boolean isDevelopmentMode() {
        return true; // Toujours en mode développement pour l'instant
    }
}