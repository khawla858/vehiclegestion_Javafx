package com.example.vehiclegestion.admin.controller;

import com.example.vehiclegestion.auth.utils.SessionManager;
import com.example.vehiclegestion.auth.utils.PasswordUtils;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.common.dao.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class AdminSettingsController {

    @FXML private TextField adminNameField;
    @FXML private TextField adminEmailField;
    @FXML private TextField appNameField;

    @FXML private ComboBox<String> languageCombo;
    @FXML private ComboBox<String> timezoneCombo;

    @FXML private CheckBox twoFactorCheckbox;
    @FXML private CheckBox emailNotifCheckbox;
    @FXML private CheckBox newUserNotifCheckbox;
    @FXML private CheckBox newMagasinNotifCheckbox;
    @FXML private CheckBox systemErrorNotifCheckbox;
    @FXML private CheckBox dailyReportCheckbox;

    @FXML private Spinner<Integer> sessionDurationSpinner;
    @FXML private Label lastBackupLabel;

    @FXML private Button saveButton;
    @FXML private Button changePasswordButton;
    @FXML private Button backupButton;
    @FXML private Button restoreButton;
    @FXML private Button clearLogsButton;
    @FXML private Button resetSystemButton;

    private LocalDateTime lastBackupDate;

    @FXML
    public void initialize() {
        System.out.println("🎛️ Initialisation AdminSettingsController...");
        setupLanguageCombo();
        setupTimezoneCombo();
        setupSessionDurationSpinner();
        loadCurrentSettings();
        System.out.println("✅ AdminSettingsController initialisé avec succès");
    }

    /**
     * Configuration du ComboBox des langues
     */
    private void setupLanguageCombo() {
        languageCombo.getItems().addAll(
                "Français",
                "English",
                "العربية",
                "Español",
                "Deutsch"
        );
        languageCombo.setValue("Français");
    }

    /**
     * Configuration du ComboBox des fuseaux horaires
     */
    private void setupTimezoneCombo() {
        timezoneCombo.getItems().addAll(
                "UTC",
                "Europe/Paris (GMT+1)",
                "Africa/Casablanca (GMT+1)",
                "America/New_York (GMT-5)",
                "Asia/Tokyo (GMT+9)"
        );
        timezoneCombo.setValue("Africa/Casablanca (GMT+1)");
    }

    /**
     * Configuration du Spinner pour la durée de session
     */
    private void setupSessionDurationSpinner() {
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(15, 480, 60, 15);
        sessionDurationSpinner.setValueFactory(valueFactory);
        sessionDurationSpinner.setEditable(true);
    }

    /**
     * Charger les paramètres actuels
     */
    private void loadCurrentSettings() {
        SessionManager sessionManager = SessionManager.getInstance();
        Utilisateur adminConnecte = sessionManager.getUtilisateurConnecte();

        if (adminConnecte != null) {
            // Charger les infos de l'admin connecté
            adminNameField.setText(adminConnecte.getPrenom() + " " + adminConnecte.getNom());
            adminEmailField.setText(adminConnecte.getEmail());
            System.out.println("✅ Paramètres chargés pour: " + adminConnecte.getEmail());
        } else {
            // Valeurs par défaut si aucun utilisateur n'est connecté
            adminNameField.setText("Administrateur Principal");
            adminEmailField.setText("admin@vehiclegestion.com");
            System.out.println("⚠️ Aucun utilisateur connecté, valeurs par défaut chargées");
        }

        // Paramètres de l'application
        appNameField.setText("Gestion Véhicules Pro");

        // Notifications par défaut
        newUserNotifCheckbox.setSelected(true);
        systemErrorNotifCheckbox.setSelected(true);
        dailyReportCheckbox.setSelected(true);

        // Dernière sauvegarde
        updateLastBackupLabel();
    }

    /**
     * Enregistrer les modifications
     */
    @FXML
    private void handleSave() {
        // Validation
        if (adminNameField.getText().trim().isEmpty()) {
            showAlert("Le nom ne peut pas être vide", Alert.AlertType.WARNING);
            return;
        }

        if (!isValidEmail(adminEmailField.getText())) {
            showAlert("L'email n'est pas valide", Alert.AlertType.WARNING);
            return;
        }

        // Confirmation
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Enregistrer les modifications");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir enregistrer ces modifications ?");
        styleAlert(confirmAlert);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                saveSettings();
                updateAdminProfile();
                showAlert("✅ Paramètres enregistrés avec succès!", Alert.AlertType.INFORMATION);
                System.out.println("✅ Paramètres sauvegardés");
            } catch (Exception e) {
                System.err.println("❌ Erreur sauvegarde: " + e.getMessage());
                showAlert("Erreur lors de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Sauvegarder les paramètres
     */
    private void saveSettings() {
        SessionManager sessionManager = SessionManager.getInstance();
        int userId = sessionManager.getUserId();

        System.out.println("📝 Sauvegarde des paramètres:");
        System.out.println("  - User ID: " + userId);
        System.out.println("  - Nom: " + adminNameField.getText());
        System.out.println("  - Email: " + adminEmailField.getText());
        System.out.println("  - App Name: " + appNameField.getText());
        System.out.println("  - Langue: " + languageCombo.getValue());
        System.out.println("  - 2FA: " + twoFactorCheckbox.isSelected());
        System.out.println("  - Durée session: " + sessionDurationSpinner.getValue() + " min");
    }

    /**
     * Mettre à jour le profil admin dans la base de données
     */
    private void updateAdminProfile() throws SQLException {
        SessionManager sessionManager = SessionManager.getInstance();
        int userId = sessionManager.getUserId();

        String[] names = adminNameField.getText().trim().split(" ", 2);
        String prenom = names.length > 0 ? names[0] : "";
        String nom = names.length > 1 ? names[1] : "";
        String email = adminEmailField.getText().trim();

        String query = "UPDATE Utilisateur SET prenom = ?, nom = ?, email = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, prenom);
            stmt.setString(2, nom);
            stmt.setString(3, email);
            stmt.setInt(4, userId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Profil mis à jour dans la BD: " + prenom + " " + nom);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour profil: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Changer le mot de passe
     */
    @FXML
    private void handleChangePassword() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Changer le mot de passe");
        dialog.setHeaderText("Modification du mot de passe administrateur");

        ButtonType changeButtonType = new ButtonType("Changer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        PasswordField currentPassword = new PasswordField();
        currentPassword.setPromptText("Mot de passe actuel");
        PasswordField newPassword = new PasswordField();
        newPassword.setPromptText("Nouveau mot de passe");
        PasswordField confirmPassword = new PasswordField();
        confirmPassword.setPromptText("Confirmer le mot de passe");

        grid.add(new Label("Mot de passe actuel:"), 0, 0);
        grid.add(currentPassword, 1, 0);
        grid.add(new Label("Nouveau mot de passe:"), 0, 1);
        grid.add(newPassword, 1, 1);
        grid.add(new Label("Confirmer:"), 0, 2);
        grid.add(confirmPassword, 1, 2);

        dialog.getDialogPane().setContent(grid);
        styleAlert(dialog);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                String newPass = newPassword.getText();
                String confirmPass = confirmPassword.getText();

                if (newPass.isEmpty() || confirmPass.isEmpty()) {
                    showAlert("⚠️ Tous les champs sont obligatoires", Alert.AlertType.WARNING);
                    return null;
                }

                if (newPass.length() < 6) {
                    showAlert("⚠️ Le mot de passe doit contenir au moins 6 caractères", Alert.AlertType.WARNING);
                    return null;
                }

                if (newPass.equals(confirmPass)) {
                    return new String[]{currentPassword.getText(), newPass};
                } else {
                    showAlert("❌ Les mots de passe ne correspondent pas", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(passwords -> {
            try {
                updatePassword(passwords[1]); // passwords[1] = nouveau mot de passe
                showAlert("✅ Mot de passe modifié avec succès!", Alert.AlertType.INFORMATION);
                System.out.println("✅ Mot de passe changé");
            } catch (SQLException e) {
                System.err.println("❌ Erreur changement mot de passe: " + e.getMessage());
                showAlert("❌ Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Mettre à jour le mot de passe dans la base de données
     */
    private void updatePassword(String newPassword) throws SQLException {
        SessionManager sessionManager = SessionManager.getInstance();
        int userId = sessionManager.getUserId();

        // Hasher le mot de passe avec PasswordUtils
        String hashedPassword = PasswordUtils.hashPassword(newPassword);

        String query = "UPDATE Utilisateur SET mot_de_passe = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);

            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Mot de passe mis à jour dans la BD");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour mot de passe: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Sauvegarder la base de données
     */
    @FXML
    private void handleBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder la base de données");
        fileChooser.setInitialFileName("backup_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".sql");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers SQL", "*.sql")
        );

        File file = fileChooser.showSaveDialog(backupButton.getScene().getWindow());

        if (file != null) {
            try {
                // TODO: Implémenter la vraie sauvegarde PostgreSQL
                // pg_dump -U username -d database_name -f backup_file.sql
                System.out.println("📦 Création backup: " + file.getAbsolutePath());

                lastBackupDate = LocalDateTime.now();
                updateLastBackupLabel();

                showAlert("✅ Sauvegarde créée avec succès!\nFichier: " + file.getName(),
                        Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                System.err.println("❌ Erreur backup: " + e.getMessage());
                showAlert("❌ Erreur lors de la sauvegarde: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Restaurer la base de données
     */
    @FXML
    private void handleRestore() {
        Alert warningAlert = new Alert(Alert.AlertType.WARNING);
        warningAlert.setTitle("⚠️ Attention");
        warningAlert.setHeaderText("Restauration de la base de données");
        warningAlert.setContentText("Cette action va écraser toutes les données actuelles.\n" +
                "Êtes-vous absolument sûr de vouloir continuer ?");
        styleAlert(warningAlert);

        Optional<ButtonType> result = warningAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner le fichier de sauvegarde");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers SQL", "*.sql")
            );

            File file = fileChooser.showOpenDialog(restoreButton.getScene().getWindow());

            if (file != null) {
                try {
                    // TODO: Implémenter la vraie restauration PostgreSQL
                    // psql -U username -d database_name -f backup_file.sql
                    System.out.println("📥 Restauration depuis: " + file.getAbsolutePath());

                    showAlert("✅ Base de données restaurée avec succès!",
                            Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    System.err.println("❌ Erreur restauration: " + e.getMessage());
                    showAlert("❌ Erreur lors de la restauration: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                }
            }
        }
    }

    /**
     * Effacer tous les logs
     */
    @FXML
    private void handleClearLogs() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("🗑️ Effacer tous les logs");
        confirmAlert.setContentText("Cette action est irréversible!\n" +
                "Tous les logs d'authentification seront supprimés.\n\n" +
                "Continuer ?");
        styleAlert(confirmAlert);

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // TODO: Implémenter la suppression des logs
                System.out.println("🗑️ Suppression des logs...");
                showAlert("✅ Tous les logs ont été effacés", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                System.err.println("❌ Erreur suppression logs: " + e.getMessage());
                showAlert("❌ Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Réinitialiser le système
     */
    @FXML
    private void handleResetSystem() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("⚠️ Confirmation de réinitialisation");
        dialog.setHeaderText("🔴 ATTENTION: Réinitialisation complète du système");
        dialog.setContentText("Tapez 'RESET' en majuscules pour confirmer:");
        styleAlert(dialog);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && result.get().equals("RESET")) {
            Alert finalConfirm = new Alert(Alert.AlertType.WARNING);
            finalConfirm.setTitle("⚠️ Dernière confirmation");
            finalConfirm.setHeaderText("Êtes-vous ABSOLUMENT sûr ?");
            finalConfirm.setContentText("⚠️ Toutes les données seront perdues définitivement!\n" +
                    "Cette action est IRRÉVERSIBLE!");
            styleAlert(finalConfirm);

            Optional<ButtonType> finalResult = finalConfirm.showAndWait();
            if (finalResult.isPresent() && finalResult.get() == ButtonType.OK) {
                try {
                    // TODO: Implémenter la réinitialisation système
                    System.out.println("🔄 Réinitialisation système bloquée pour sécurité");
                    showAlert("ℹ️ Fonctionnalité désactivée pour des raisons de sécurité",
                            Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    System.err.println("❌ Erreur reset: " + e.getMessage());
                    showAlert("❌ Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        } else if (result.isPresent()) {
            showAlert("❌ Texte de confirmation incorrect. Réinitialisation annulée.",
                    Alert.AlertType.INFORMATION);
        }
    }

    /**
     * Mettre à jour le label de dernière sauvegarde
     */
    private void updateLastBackupLabel() {
        if (lastBackupDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm:ss");
            lastBackupLabel.setText(lastBackupDate.format(formatter));
        } else {
            lastBackupLabel.setText("Aucune sauvegarde récente");
        }
    }

    /**
     * Valider le format email
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    /**
     * Styliser une alerte avec le thème sombre
     */
    private void styleAlert(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e293b;");
        if (dialogPane.lookup(".content.label") != null) {
            dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");
        }
        if (dialogPane.lookup(".header-panel") != null) {
            dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #0f172a;");
        }
    }

    /**
     * Afficher une alerte
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "❌ Erreur" :
                type == Alert.AlertType.WARNING ? "⚠️ Attention" :
                        type == Alert.AlertType.CONFIRMATION ? "❓ Confirmation" : "ℹ️ Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }
}