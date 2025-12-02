package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.common.dao.ChatDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class MainController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label userNameLabel;
    @FXML private TextField searchField;
    @FXML private Button vehiclesBtn;
    @FXML private Button favoritesBtn;
    @FXML private Button historyBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;

    // ✅ NOUVEAU : Badge messages
    @FXML private Label badgeMessages;  // Le badge "3" sur l'icône messages
    @FXML private Button btnMessages;   // Le bouton messages dans le header

    private SessionManager sessionManager = SessionManager.getInstance();

    // ✅ NOUVEAU : DAO pour le chat
    private ChatDAO chatDAO = new ChatDAO();

    // ✅ NOUVEAU : Timeline pour rafraîchir le badge
    private Timeline badgeRefreshTimeline;

    @FXML
    public void initialize() {
        System.out.println("🚀 MainController initialisé");

        if (!sessionManager.estConnecte()) {
            redirectToLogin();
            return;
        }

        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

        System.out.println("👤 Utilisateur connecté: " + currentUser.getPrenom() + " " + currentUser.getNom());

        // Charger la page véhicules par défaut
        showVehicles();

        // Initialiser la recherche
        initializeSearch();

        // ✅ NOUVEAU : Initialiser le chat
        initializeChat();
    }

    // ========================================
    // 💬 GESTION DU CHAT
    // ========================================

    /**
     * Initialise le système de chat (badge + refresh)
     */
    private void initializeChat() {
        // Mise à jour initiale du badge
        updateMessageBadge();

        // Démarrer le rafraîchissement automatique
        startBadgeRefresh();

        System.out.println("✅ Système de chat initialisé");
    }

    /**
     * Met à jour le badge avec le nombre de messages non lus
     */
    private void updateMessageBadge() {
        if (!sessionManager.estConnecte()) return;

        try {
            int unreadCount = chatDAO.countUnreadMessages(
                    sessionManager.getUserId(),
                    sessionManager.getUserRole()
            );

            if (badgeMessages != null) {
                if (unreadCount > 0) {
                    badgeMessages.setText(String.valueOf(unreadCount));
                    badgeMessages.setVisible(true);
                    System.out.println("📬 " + unreadCount + " messages non lus");
                } else {
                    badgeMessages.setVisible(false);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour badge: " + e.getMessage());
        }
    }

    /**
     * Démarre le rafraîchissement automatique du badge toutes les 10 secondes
     */
    private void startBadgeRefresh() {
        badgeRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> updateMessageBadge())
        );
        badgeRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        badgeRefreshTimeline.play();
        System.out.println("🔄 Auto-refresh badge messages démarré");
    }

    /**
     * Arrête le rafraîchissement (à appeler lors de la fermeture)
     */
    public void stopBadgeRefresh() {
        if (badgeRefreshTimeline != null) {
            badgeRefreshTimeline.stop();
        }
    }

    /**
     * Ouvre la fenêtre de chat
     */
    @FXML
    private void showMessages() {
        System.out.println("💬 Ouverture de la fenêtre de chat...");

        if (!sessionManager.estConnecte()) {
            showError("Erreur", "Vous devez être connecté pour accéder au chat");
            return;
        }

        try {
            // Option 1 : Ouvrir dans la zone de contenu principale
            loadContent("/view/common/ChatWindow.fxml");

            // Option 2 : Ouvrir dans une fenêtre séparée (décommentez si préféré)
            // openChatInNewWindow();

            // Rafraîchir le badge après ouverture
            updateMessageBadge();

            System.out.println("✅ Chat ouvert avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture chat: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir le chat: " + e.getMessage());
        }
    }

    /**
     * Option alternative : Ouvre le chat dans une nouvelle fenêtre
     */
    private void openChatInNewWindow() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/common/ChatWindow.fxml")
        );
        Parent root = loader.load();

        Stage chatStage = new Stage();
        chatStage.setTitle("💬 Messages - AutoSales Pro");
        chatStage.setScene(new Scene(root, 900, 600));
        chatStage.show();
    }

    // ========================================
    // 🔍 RECHERCHE
    // ========================================

    private void initializeSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.length() > 2) {
                    performSearch(newValue);
                }
            });
        }
    }

    private void performSearch(String query) {
        System.out.println("🔍 Recherche globale: " + query);
        // La recherche sera gérée dans chaque contrôleur spécifique
    }

    // ========================================
    // 🚀 NAVIGATION
    // ========================================

    @FXML
    private void showVehicles() {
        System.out.println("🚗 Navigation vers Véhicules");
        setActiveMenu(vehiclesBtn);
        loadContent("/view/client/vehicles-view.fxml");
    }

    @FXML
    private void showFavorites() {
        System.out.println("❤️ Navigation vers Favoris");
        setActiveMenu(favoritesBtn);
        loadContent("/view/client/ClientFavoritesView.fxml");
    }

    @FXML
    private void showHistory() {
        System.out.println("📊 Navigation vers Historique");
        setActiveMenu(historyBtn);
        loadContent("/view/client/ClientFavoritesView.fxml");
    }

    @FXML
    private void showProfile() {
        System.out.println("👤 Navigation vers Profil");
        setActiveMenu(profileBtn);
        loadContent("/view/client/profile-view.fxml");
    }

    @FXML
    private void logout() {
        System.out.println("🚪 Déconnexion");

        // Arrêter le refresh du badge
        stopBadgeRefresh();

        sessionManager.fermerSession();
        redirectToLogin();
    }

    // ========================================
    // 🛠️ UTILITAIRES
    // ========================================

    private void setActiveMenu(Button activeButton) {
        // Réinitialiser tous les boutons
        vehiclesBtn.setStyle(getInactiveStyle());
        favoritesBtn.setStyle(getInactiveStyle());
        historyBtn.setStyle(getInactiveStyle());
        profileBtn.setStyle(getInactiveStyle());

        // Activer le bouton sélectionné
        if (activeButton != null) {
            activeButton.setStyle(getActiveStyle());
        }
    }

    private String getInactiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #2c3e50; " +
                "-fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 18 20; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: transparent;";
    }

    private String getActiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #3498db; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 18 20; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: #3498db;";
    }

    private void loadContent(String fxmlPath) {
        try {
            System.out.println("📁 Chargement du contenu: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            mainBorderPane.setCenter(content);
            System.out.println("✅ Contenu chargé avec succès: " + fxmlPath);

        } catch (IOException e) {
            System.err.println("❌ Erreur de chargement: " + fxmlPath);
            e.printStackTrace();
            showErrorPage("Page non disponible: " + fxmlPath);
        }
    }

    private void showErrorPage(String message) {
        VBox errorBox = new VBox();
        errorBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 20; -fx-alignment: center; -fx-spacing: 10;");

        Label errorLabel = new Label("⚠️ " + message);
        errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");

        Label infoLabel = new Label("Cette page est en cours de développement");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");

        errorBox.getChildren().addAll(errorLabel, infoLabel);
        mainBorderPane.setCenter(errorBox);
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
            Parent loginPage = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(loginPage));
            stage.setTitle("Connexion - AutoSales Pro");
            stage.setMaximized(false);
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("❌ Erreur critique lors de la redirection vers login");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}