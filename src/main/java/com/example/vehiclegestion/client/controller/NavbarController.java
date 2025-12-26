package com.example.vehiclegestion.client.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import  com.example.vehiclegestion.auth.utils.SessionManager;

import java.io.IOException;
import com.example.vehiclegestion.common.model.Notification;
import javafx.application.Platform;  // Pour Platform.runLater()
import java.util.List;               // Pour les notifications si vous l'implémentez
// AJOUTER CE IMPORT


public class NavbarController {

    @FXML private TextField searchField;
    @FXML private Label userNameLabel;
    @FXML private Button vehiclesBtn;
    @FXML private Button messagesBtn;
    @FXML private Button favoritesBtn;
    @FXML private Button historyBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;
    @FXML private Button messagesIconBtn;
    @FXML private Button favoritesIconBtn;
    @FXML private Button notificationBtn;
    @FXML private Label messagesBadge;
    @FXML private Label notificationBadge;

    // AJOUTER CES ATTRIBUTS
    private Runnable backAction;
    private Runnable refreshAction;
    private boolean isChatMode = false;

    @FXML
    public void initialize() {
        // Initialiser les informations utilisateur
        updateUserProfile();

        // Configurer les actions des boutons
        setupButtonActions();
    }

    // ============ AJOUTER CES MÉTHODES ============

    /**
     * Définit l'action du bouton retour
     */
    public void setBackButtonAction(Runnable action) {
        this.backAction = action;
        System.out.println("🔙 Action retour définie pour la navbar");
    }

    /**
     * Définit l'action du bouton rafraîchir
     */
    public void setRefreshButtonAction(Runnable action) {
        this.refreshAction = action;
        System.out.println("🔄 Action rafraîchir définie pour la navbar");
    }

    /**
     * Met à jour les informations du profil utilisateur
     */
    public void updateUserProfile() {
        Platform.runLater(() -> {
            SessionManager session = SessionManager.getInstance();
            if (session.estConnecte()) {
                String fullName = session.getUserFullName();
                String userRole = session.getUserRole();
                String userEmail = session.getUserEmail();

                if (userNameLabel != null) {
                    userNameLabel.setText(fullName != null ? fullName : "Utilisateur");
                }

                // Vous pouvez aussi mettre à jour d'autres éléments si nécessaire
                System.out.println("👤 Profil navbar mis à jour: " + fullName + " (" + userRole + ")");
            }
        });
    }

    /**
     * Affiche/masque le bouton retour
     */
    public void setBackButtonVisible(boolean visible) {
        // Si vous n'avez pas de bouton retour dans votre navbar actuelle,
        // vous pouvez ignorer cette méthode ou l'implémenter si vous ajoutez le bouton
        System.out.println("👁️‍🗨️ Bouton retour " + (visible ? "affiché" : "masqué"));
    }

    /**
     * Configure la navbar pour le mode chat
     */
    public void configureForChatMode() {
        this.isChatMode = true;
        System.out.println("💬 Navbar configurée pour le mode chat");

        // Vous pouvez modifier l'apparence si nécessaire
        // Par exemple: cacher certains boutons, changer le style, etc.
    }

    /**
     * Met à jour l'état de connexion
     */
    public void updateLoginState(boolean isLoggedIn) {
        Platform.runLater(() -> {
            if (userNameLabel != null) {
                userNameLabel.setVisible(isLoggedIn);
            }

            if (logoutBtn != null) {
                logoutBtn.setVisible(isLoggedIn);
            }
        });
    }

    /**
     * Met à jour les informations utilisateur
     */
    public void updateUserInfo(String userName, String userRole, String userEmail) {
        Platform.runLater(() -> {
            if (userNameLabel != null) {
                userNameLabel.setText(userName != null ? userName : "Utilisateur");
            }
        });
    }

    /**
     * Désactive le lien Messages (quand on est déjà dans le chat)
     */
    public void setMessagesLinkActive(boolean active) {
        if (messagesBtn != null) {
            messagesBtn.setDisable(!active);
            messagesBtn.setStyle(active ? "" : "-fx-opacity: 0.5;");
        }

        if (messagesIconBtn != null) {
            messagesIconBtn.setDisable(!active);
            messagesIconBtn.setStyle(active ? "" : "-fx-opacity: 0.5;");
        }
    }

    /**
     * Définit un handler de navigation
     */
    public void setNavigationHandler(NavigationHandler handler) {
        // Implémentez cette méthode si vous avez besoin d'un handler complexe
        System.out.println("🗺️ Handler de navigation défini");
    }

    // Interface pour NavigationHandler (optionnel)
    public interface NavigationHandler {
        void navigateTo(String viewName);
        void navigateTo(String viewName, Object data);
        void goBack();
        void logout();
    }

    /**
     * Affiche une notification dans la navbar
     */
    public void showNotification(String message, String type) {
        System.out.println("📢 Notification: " + message + " (" + type + ")");
        // Vous pouvez implémenter l'affichage d'une notification visuelle
    }

    /**
     * Met à jour le badge du panier
     */
    public void setCartItemCount(int count) {
        // Implémentez si vous avez un panier
        System.out.println("🛒 Nombre d'articles dans le panier: " + count);
    }

    /**
     * Met à jour les notifications
     */
    public void setNotifications(List<Notification> notifications) {
        // Implémentez si vous avez un système de notifications
        System.out.println("🔔 Nombre de notifications: " + (notifications != null ? notifications.size() : 0));
    }

    // AJOUTEZ CET IMPORT SI VOUS UTILISEZ List
    // import java.util.List;

    // ============ MÉTHODES EXISTANTES RESTENT CI-DESSOUS ============

    private void setupButtonActions() {
        if (vehiclesBtn != null) {
            vehiclesBtn.setOnAction(e -> navigateToVehicles());
        }

        if (messagesBtn != null) {
            messagesBtn.setOnAction(e -> openChat());
        }

        if (messagesIconBtn != null) {
            messagesIconBtn.setOnAction(e -> openChat());
        }

        if (favoritesBtn != null) {
            favoritesBtn.setOnAction(e -> navigateToFavorites());
        }

        if (favoritesIconBtn != null) {
            favoritesIconBtn.setOnAction(e -> navigateToFavorites());
        }

        if (historyBtn != null) {
            historyBtn.setOnAction(e -> navigateToHistory());
        }

        if (profileBtn != null) {
            profileBtn.setOnAction(e -> navigateToProfile());
        }

        if (logoutBtn != null) {
            logoutBtn.setOnAction(e -> logout());
        }

        // AJOUTER UN BOUTON RAFRAÎCHIR SI NÉCESSAIRE
        // Vous pouvez ajouter un bouton refreshBtn dans votre FXML
    }

    private void navigateToVehicles() {
        loadPage("/view/client/vehicles-view.fxml");
    }

    private void navigateToFavorites() {
        loadPage("/view/client/ClientFavoritesView.fxml");
    }

    private void navigateToHistory() {
        loadPage("/view/client/historique-client.fxml");
    }

    private void navigateToProfile() {
        loadPage("/view/client/profile-view.fxml");
    }

    private void loadPage(String fxmlPath) {
        try {
            Stage stage = getCurrentStage();
            if (stage == null) return;

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur de navigation", "Impossible de charger la page: " + fxmlPath);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Erreur inattendue: " + e.getMessage());
        }
    }

    private Stage getCurrentStage() {
        if (vehiclesBtn != null && vehiclesBtn.getScene() != null) {
            return (Stage) vehiclesBtn.getScene().getWindow();
        }
        if (messagesBtn != null && messagesBtn.getScene() != null) {
            return (Stage) messagesBtn.getScene().getWindow();
        }
        if (userNameLabel != null && userNameLabel.getScene() != null) {
            return (Stage) userNameLabel.getScene().getWindow();
        }
        return null;
    }

    private void openChat() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/common/ChatWindow.fxml"));
            Parent chatRoot = loader.load();

            Stage chatStage = new Stage();
            chatStage.setTitle("Messages");
            chatStage.setScene(new Scene(chatRoot, 1000, 700));
            chatStage.setMinWidth(800);
            chatStage.setMinHeight(600);
            chatStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible d'ouvrir la messagerie");
        }
    }

    private void logout() {
        try {
            SessionManager session = SessionManager.getInstance();
            session.fermerSession();

            Stage stage = getCurrentStage();
            if (stage == null) return;

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
            Parent loginPage = loader.load();

            Scene scene = new Scene(loginPage);
            stage.setScene(scene);
            stage.setTitle("Connexion - AutoSales Pro");
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur", "Impossible de se déconnecter");
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