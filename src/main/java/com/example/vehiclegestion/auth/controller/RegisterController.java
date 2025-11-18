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

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private RadioButton clientRadio;

    @FXML
    private RadioButton vendeurRadio;

    @FXML
    private Label errorLabel;

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

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

        // Validation
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

        // Vérifier si l'email existe déjà
        if (utilisateurDAO.emailExists(email)) {
            showError("Cet email est déjà utilisé");
            return;
        }

        // Déterminer le rôle
        String role = clientRadio.isSelected() ? "client" : "vendeur";

        // Créer l'utilisateur
        Utilisateur user = new Utilisateur(nom, prenom, email, password, role);

        // Inscription
        if (utilisateurDAO.inscrire(user)) {
            hideError();
            showSuccessAndRedirect();
        } else {
            showError("Erreur lors de l'inscription. Veuillez réessayer.");
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

        handleGoToLogin();
    }

    /**
     * Retour à la page de connexion
     */
    @FXML
    private void handleGoToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/vehiclegestion/view/auth/Login.fxml"));
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur lors du chargement de la page de connexion");
            e.printStackTrace();
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
}