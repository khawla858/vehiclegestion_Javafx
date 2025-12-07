package com.example.vehiclegestion.admin.controller;

import com.example.vehiclegestion.auth.AuthentificationService;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.auth.utils.SessionManager;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller principal de l'interface Admin
 * Gère la navigation entre les différentes pages
 */
public class AdminMainController {

    @FXML private BorderPane contentArea;
    @FXML private Label adminNameLabel;
    @FXML private Label pageTitle;
    @FXML private Label dateTimeLabel;

    @FXML private Button dashboardBtn;
    @FXML private Button usersBtn;
    @FXML private Button magasinsBtn;

    private final AuthentificationService authService;

    public AdminMainController() {
        this.authService = new AuthentificationService();
    }

    /**
     * Initialisation du controller
     */
    @FXML
    public void initialize() {
        System.out.println("🎛️ Initialisation AdminMainController...");

        // Afficher le nom de l'admin connecté
        Utilisateur admin = SessionManager.getInstance().getUtilisateurConnecte();
        if (admin != null) {
            adminNameLabel.setText(admin.getPrenom() + " " + admin.getNom());
        }

        // Mettre à jour la date/heure en temps réel
        startDateTimeClock();

        // Charger le dashboard par défaut
        showDashboard();

        System.out.println("✅ AdminMainController initialisé");
    }

    /**
     * Afficher le Dashboard
     */
    @FXML
    public void showDashboard() {
        try {
            loadPage("/view/admin/admin-dashboard.fxml", "Dashboard");
            setActiveButton(dashboardBtn);
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger le dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Afficher la gestion des utilisateurs
     */
    @FXML
    public void showUsers() {
        try {
            loadPage("/view/admin/admin-users.fxml", "Gestion des Utilisateurs");
            setActiveButton(usersBtn);
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger la page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Afficher la gestion des magasins
     */
    @FXML
    public void showMagasins() {
        try {
            loadPage("/view/admin/admin-magasins.fxml", "Gestion des Magasins");
            setActiveButton(magasinsBtn);
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger la page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Afficher les logs d'authentification
     */
    @FXML
    public void showAuthLogs() {
        // TODO: Créer la page des logs
        showInfo("En développement", "La page des logs sera bientôt disponible");
    }

    /**
     * Afficher les paramètres
     */
    @FXML
    public void showSettings() {
        // TODO: Créer la page des paramètres
        showInfo("En développement", "La page des paramètres sera bientôt disponible");
    }

    /**
     * Charger une page FXML dans la zone de contenu
     */
    private void loadPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            contentArea.setCenter(page);
            pageTitle.setText(title);
            System.out.println("✅ Page chargée: " + title);
        } catch (IOException e) {
            System.err.println("❌ Erreur chargement page: " + fxmlPath);
            e.printStackTrace();
            throw new RuntimeException("Impossible de charger la page: " + fxmlPath, e);
        }
    }

    /**
     * Mettre en surbrillance le bouton actif
     */
    private void setActiveButton(Button activeBtn) {
        // Réinitialiser tous les boutons
        dashboardBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: CENTER_LEFT; " +
                "-fx-padding: 10px 20px;");
        usersBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: CENTER_LEFT; " +
                "-fx-padding: 10px 20px;");
        magasinsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: CENTER_LEFT; " +
                "-fx-padding: 10px 20px;");

        // Mettre en surbrillance le bouton actif
        activeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-alignment: CENTER_LEFT; " +
                "-fx-padding: 10px 20px; -fx-background-radius: 5px;");
    }

    /**
     * Démarrer l'horloge date/heure
     */
    private void startDateTimeClock() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            dateTimeLabel.setText(now.format(formatter));
        }), new KeyFrame(Duration.seconds(1)));

        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    /**
     * Gérer la déconnexion
     */
    @FXML
    public void handleLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Voulez-vous vraiment vous déconnecter ?");
        confirmation.setContentText("Vous serez redirigé vers la page de connexion.");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Déconnexion
                authService.seDeconnecter();
                System.out.println("🚪 Déconnexion admin réussie");

                // Retour à la page de login
                Stage stage = (Stage) contentArea.getScene().getWindow();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root, 1000, 700);
                stage.setScene(scene);
                stage.setTitle("Connexion - Gestion Véhicules");

            } catch (IOException e) {
                showError("Erreur", "Impossible de charger la page de connexion: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}