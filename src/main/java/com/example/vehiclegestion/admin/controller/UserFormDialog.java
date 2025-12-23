package com.example.vehiclegestion.admin.controller;

import com.example.vehiclegestion.auth.model.Utilisateur;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Dialog pour créer ou modifier un utilisateur
 */
public class UserFormDialog extends Dialog<Utilisateur> {

    private final TextField nomField;
    private final TextField prenomField;
    private final TextField emailField;
    private final PasswordField passwordField;
    private final ComboBox<String> roleCombo;
    private final ComboBox<String> statutCombo;

    private final Utilisateur existingUser; // null si création

    /**
     * Constructeur pour CRÉATION d'un utilisateur
     */
    public UserFormDialog() {
        this(null);
    }

    /**
     * Constructeur pour MODIFICATION d'un utilisateur
     */
    public UserFormDialog(Utilisateur user) {
        this.existingUser = user;

        // Configuration du dialog
        setTitle(user == null ? "Créer un utilisateur" : "Modifier l'utilisateur");
        setHeaderText(user == null ? "Remplissez les informations du nouvel utilisateur" :
                "Modifiez les informations de l'utilisateur");

        // Boutons
        ButtonType saveButtonType = new ButtonType(
                user == null ? "Créer" : "Modifier",
                ButtonBar.ButtonData.OK_DONE
        );
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Créer le formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Champs du formulaire
        nomField = new TextField();
        nomField.setPromptText("Nom");

        prenomField = new TextField();
        prenomField.setPromptText("Prénom");

        emailField = new TextField();
        emailField.setPromptText("Email");

        passwordField = new PasswordField();
        passwordField.setPromptText(user == null ? "Mot de passe" : "Nouveau mot de passe (optionnel)");

        roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("admin", "vendeur", "client");
        roleCombo.setValue("client");

        statutCombo = new ComboBox<>();
        statutCombo.getItems().addAll("actif", "inactif");
        statutCombo.setValue("actif");

        // Ajouter les champs au grid
        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);

        grid.add(new Label("Prénom:"), 0, 1);
        grid.add(prenomField, 1, 1);

        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);

        grid.add(new Label("Mot de passe:"), 0, 3);
        grid.add(passwordField, 1, 3);

        grid.add(new Label("Rôle:"), 0, 4);
        grid.add(roleCombo, 1, 4);

        grid.add(new Label("Statut:"), 0, 5);
        grid.add(statutCombo, 1, 5);

        getDialogPane().setContent(grid);

        // Si modification, remplir les champs
        if (user != null) {
            fillFieldsWithUserData(user);
        }

        // Validation et conversion du résultat
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (!validateFields()) {
                    return null;
                }
                return createUserFromFields();
            }
            return null;
        });

        // Focus sur le premier champ
        nomField.requestFocus();
    }

    /**
     * Remplir les champs avec les données existantes
     */
    private void fillFieldsWithUserData(Utilisateur user) {
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        roleCombo.setValue(user.getRole());
        statutCombo.setValue(user.getStatut());
        // Ne pas remplir le mot de passe pour des raisons de sécurité
    }

    /**
     * Valider les champs
     */
    private boolean validateFields() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert("Le nom est requis");
            return false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            showAlert("Le prénom est requis");
            return false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showAlert("L'email est requis");
            return false;
        }

        // Validation email
        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Format d'email invalide");
            return false;
        }

        // Si création, mot de passe obligatoire
        if (existingUser == null && passwordField.getText().trim().isEmpty()) {
            showAlert("Le mot de passe est requis pour un nouvel utilisateur");
            return false;
        }

        // Si mot de passe fourni, vérifier la longueur minimale
        if (!passwordField.getText().trim().isEmpty() && passwordField.getText().length() < 6) {
            showAlert("Le mot de passe doit contenir au moins 6 caractères");
            return false;
        }

        if (roleCombo.getValue() == null) {
            showAlert("Veuillez sélectionner un rôle");
            return false;
        }

        if (statutCombo.getValue() == null) {
            showAlert("Veuillez sélectionner un statut");
            return false;
        }

        return true;
    }

    /**
     * Créer un objet Utilisateur à partir des champs
     */
    private Utilisateur createUserFromFields() {
        Utilisateur user;

        if (existingUser != null) {
            // Modification : garder l'utilisateur existant
            user = existingUser;
        } else {
            // Création : nouvel utilisateur
            user = new Utilisateur();
            user.setDateCreation(LocalDateTime.now());
        }

        user.setNom(nomField.getText().trim());
        user.setPrenom(prenomField.getText().trim());
        user.setEmail(emailField.getText().trim());
        user.setRole(roleCombo.getValue());
        user.setStatut(statutCombo.getValue());

        // Mot de passe : seulement si renseigné
        if (!passwordField.getText().trim().isEmpty()) {
            // TODO: Hasher le mot de passe avant de le sauvegarder
            // user.setMotDePasse(PasswordUtil.hash(passwordField.getText()));
            user.setMotDePasse(passwordField.getText()); // Temporaire - à hasher
        }

        return user;
    }

    /**
     * Afficher une alerte d'erreur
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher le dialog et retourner le résultat
     */
    /*public Optional<Utilisateur> showAndWait() {
        return super.showAndWait();
    }*/
}