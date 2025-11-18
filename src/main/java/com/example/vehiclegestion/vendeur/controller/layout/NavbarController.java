package com.example.vehiclegestion.vendeur.controller.layout;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;

public class NavbarController {

    @FXML
    private TextField searchField;

    @FXML
    private MenuButton profileMenu;

    @FXML
    public void initialize() {
        System.out.println("NavbarController initialisé");

        // Configuration de la barre de recherche
        setupSearchField();
    }

    private void setupSearchField() {
        // Action lors de la pression sur Entrée
        searchField.setOnAction(event -> {
            performSearch();
        });
    }

    // ================= MÉTHODES RECHERCHE =================

    @FXML
    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            System.out.println("Recherche: " + searchText);
            showAlert("Recherche", "Recherche pour: " + searchText);
            // Implémentez votre logique de recherche ici
        }
    }

    // ================= MÉTHODES NOTIFICATIONS =================

    @FXML
    private void showNotifications() {
        System.out.println("Notifications ouvertes");
        showAlert("Notifications", "Vous avez 3 nouvelles notifications");
        // Implémentez l'ouverture du panneau de notifications
    }

    @FXML
    private void showMessages() {
        System.out.println("Messages ouverts");
        showAlert("Messages", "Vous avez 5 nouveaux messages");
        // Implémentez l'ouverture de la messagerie
    }

    @FXML
    private void showCart() {
        System.out.println("Panier ouvert");
        showAlert("Panier", "Votre panier contient 2 articles");
        // Implémentez l'ouverture du panier
    }

    // ================= MÉTHODES PROFIL =================

    @FXML
    private void showUserProfile() {
        System.out.println("Profil utilisateur");
        showAlert("Profil", "Ouverture du profil utilisateur");
        // Implémentez l'ouverture du profil
    }

    @FXML
    private void showAccountSettings() {
        System.out.println("Paramètres du compte");
        showAlert("Paramètres", "Ouverture des paramètres du compte");
        // Implémentez l'ouverture des paramètres
    }

    @FXML
    private void showLastUpdates() {
        System.out.println("Dernières mises à jour");
        showAlert("Mises à jour", "Affichage des dernières mises à jour");
        // Implémentez l'affichage des mises à jour
    }

    @FXML
    private void showHelpCenter() {
        System.out.println("Centre d'aide");
        showAlert("Aide", "Ouverture du centre d'aide");
        // Implémentez l'ouverture de l'aide
    }

    @FXML
    private void sendFeedback() {
        System.out.println("Envoi de feedback");
        showAlert("Feedback", "Ouverture du formulaire de feedback");
        // Implémentez l'envoi de feedback
    }

    @FXML
    private void handleLogout() {
        System.out.println("Déconnexion demandée");

        // Confirmation de déconnexion
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        confirmation.setContentText("Vous serez redirigé vers la page de connexion.");

        // Pour l'instant, on affiche juste un message
        showAlert("Déconnexion", "Déconnexion effectuée avec succès");

        // Implémentez la logique de déconnexion ici :
        // - Fermeture de session
        // - Redirection vers la page de login
        // - Fermeture de l'application si nécessaire
    }

    // ================= MÉTHODES UTILITAIRES =================

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Méthode pour mettre à jour les compteurs de notifications
    public void updateNotificationCount(int notificationCount, int messageCount, int cartCount) {
        // Implémentez la mise à jour des badges
        System.out.println("Mise à jour des compteurs - Notifs: " + notificationCount +
                ", Messages: " + messageCount + ", Panier: " + cartCount);
    }

    // Méthode pour mettre à jour les infos utilisateur
    public void updateUserInfo(String userName, String userRole) {
        System.out.println("Mise à jour infos utilisateur: " + userName + " - " + userRole);
        // Implémentez la mise à jour des labels utilisateur
    }
}