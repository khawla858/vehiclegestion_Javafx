package com.example.vehiclegestion.vendeur.controller.layout;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.utils.NavigationManager;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * 🎯 NavbarController - Version Simplifiée
 *
 * Utilise NavigationManager pour toute la navigation
 * Plus besoin de loadContent() ici !
 */
public class NavbarController {

    @FXML private TextField searchField;
    @FXML private MenuButton profileMenu;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button dashboardBtn;

    private StackPane contentPane;
    private SessionManager session = SessionManager.getInstance();
    private NavigationManager nav = NavigationManager.getInstance();

    /**
     * Injection du contentPane (pour compatibilité)
     */
    public void setContentPane(StackPane contentPane) {
        this.contentPane = contentPane;
        System.out.println("✅ ContentPane injecté dans NavbarController");
    }

    @FXML
    public void initialize() {
        System.out.println("📋 NavbarController initialisé");

        loadUserInfo();
        setupSearchField();
        playWelcomeAnimation();
        setupHoverEffects();
    }

    private void loadUserInfo() {
        if (session.estConnecte()) {
            Utilisateur user = session.getUtilisateurConnecte();
            if (userNameLabel != null) {
                userNameLabel.setText(user.getNom() + " " + user.getPrenom());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(user.getRole());
            }
            System.out.println("✅ Infos utilisateur chargées: " + user.getNom());
        }
    }

    private void setupSearchField() {
        if (searchField != null) {
            searchField.setOnAction(event -> performSearch());
        }
    }

    private void setupHoverEffects() {
        if (dashboardBtn != null) {
            dashboardBtn.setOnMouseEntered(e ->
                    dashboardBtn.setStyle(dashboardBtn.getStyle() + "-fx-background-color: #ecf0f1;")
            );
            dashboardBtn.setOnMouseExited(e ->
                    dashboardBtn.setStyle(dashboardBtn.getStyle().replace("-fx-background-color: #ecf0f1;", ""))
            );
        }
    }

    private void playWelcomeAnimation() {
        if (userNameLabel != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(800), userNameLabel);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            ScaleTransition scale = new ScaleTransition(Duration.millis(600), userNameLabel);
            scale.setFromX(0.9);
            scale.setFromY(0.9);
            scale.setToX(1.0);
            scale.setToY(1.0);

            fade.play();
            scale.play();
        }
    }

    // ========================================
    // 🚀 NAVIGATION - Utilise NavigationManager
    // ========================================

    @FXML
    private void showDashboard() {
        System.out.println("📊 Navigation: Dashboard");
        nav.goToDashboard();
    }

    @FXML
    private void showCustomerList() {
        System.out.println("👥 Navigation: Liste clients");
        nav.goToClients();
    }

    @FXML
    private void showCustomerHistory() {
        System.out.println("📊 Navigation: Historique clients");
        showAlert("Clients", "Historique des interactions\n(Fonctionnalité à implémenter)", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showVehicleManagement() {
        System.out.println("🚗 Navigation: Gestion véhicules");
        nav.goToVehicles();
    }

    @FXML
    private void showStockManagement() {
        System.out.println("📦 Navigation: Gestion stock");
        nav.goToMagasinDetails();
    }

    @FXML
    private void showPendingReservations() {
        System.out.println("🛒 Navigation: Réservations");
        nav.goToReservations();
    }

    @FXML
    private void showSalesHistory() {
        System.out.println("💰 Navigation: Historique ventes");
        showAlert("Ventes", "Historique des ventes\n(Fonctionnalité à implémenter)", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showMonthlySales() {
        System.out.println("📊 Navigation: Ventes mensuelles");
        showAlert("Statistiques", "Ventes mensuelles\n(Fonctionnalité à implémenter)", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showRevenueStats() {
        System.out.println("💰 Navigation: Stats revenus");
        showAlert("Statistiques", "Statistiques de revenus\n(Fonctionnalité à implémenter)", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showClientPerformance() {
        System.out.println("🎯 Navigation: Performances clients");
        showAlert("Statistiques", "Performances clients\n(Fonctionnalité à implémenter)", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showSalesAnalytics() {
        System.out.println("📈 Navigation: Analytics");
        showAlert("Analytics", "Analytics ventes\n(Fonctionnalité à implémenter)", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showRendezVous() {
        System.out.println("📅 Navigation: Rendez-vous");
        nav.goToRendezVous();
    }

    @FXML
    private void showMagasins() {
        System.out.println("🏪 Navigation: Magasins");
        nav.goToMagasins();
    }

    @FXML
    private void ouvrirDetailMagasin() {
        System.out.println("🏪 Navigation: Détails magasin");
        nav.goToMagasinDetails();
    }

    // ========================================
    // 🔍 RECHERCHE
    // ========================================

    @FXML
    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            System.out.println("🔍 Recherche: " + searchText);
            showAlert("Recherche",
                    "Recherche pour: " + searchText + "\n\n" +
                            "Cette fonctionnalité permettra de chercher parmi:\n" +
                            "• Les véhicules\n" +
                            "• Les clients\n" +
                            "• Les commandes\n" +
                            "• Les rendez-vous",
                    Alert.AlertType.INFORMATION);
        }
    }

    // ========================================
    // 🔔 NOTIFICATIONS & MESSAGES
    // ========================================

    @FXML
    private void showNotifications() {
        System.out.println("🔔 Notifications");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText("Vous avez 3 nouvelles notifications");
        alert.setContentText(
                "📋 Nouvelle réservation pour BMW X5\n" +
                        "📅 Rappel: RDV demain 10h avec M. Dupont\n" +
                        "💬 Message de Mme. Martin concernant l'Audi A4"
        );
        styleAlert(alert);
        alert.show();
    }

    @FXML
    private void showCart() {
        System.out.println("🛒 Panier");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Panier");
        alert.setHeaderText("Votre panier de commandes");
        alert.setContentText(
                "🚗 BMW X5 - En cours de réservation\n" +
                        "🚗 Audi A4 - Option client Dupont\n\n" +
                        "Total: 2 véhicules"
        );
        styleAlert(alert);
        alert.show();
    }

    @FXML
    private void showMessages() {
        System.out.println("💬 Messages");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Messages");
        alert.setHeaderText("Vous avez 5 nouveaux messages");
        alert.setContentText(
                "💬 Question sur le financement - BMW Série 3\n" +
                        "🚗 Demande de test drive - Mercedes Classe C\n" +
                        "✅ Confirmation RDV - M. Dubois\n" +
                        "❓ Renseignement sur garantie - Audi Q5\n" +
                        "📋 Suivi dossier - Mme. Petit"
        );
        styleAlert(alert);
        alert.show();
    }

    // ========================================
    // 👤 PROFIL UTILISATEUR
    // ========================================

    @FXML
    private void showUserProfile() {
        System.out.println("👤 Profil utilisateur");
        showAlert("Profil Utilisateur",
                "Accès au profil utilisateur\n\n" +
                        "Vous pourrez modifier:\n" +
                        "• Informations personnelles\n" +
                        "• Photo de profil\n" +
                        "• Coordonnées\n" +
                        "• Préférences",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showAccountSettings() {
        System.out.println("⚙️ Paramètres compte");
        showAlert("Paramètres du Compte",
                "Configuration du compte\n\n" +
                        "Options disponibles:\n" +
                        "• Changer le mot de passe\n" +
                        "• Notifications\n" +
                        "• Sécurité\n" +
                        "• Confidentialité",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleLogout() {
        System.out.println("🚪 Déconnexion demandée");

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        confirmation.setContentText("Vous serez redirigé vers la page de connexion.");

        styleAlert(confirmation);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                session.fermerSession();
                System.out.println("✅ Session fermée");

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Déconnexion");
                success.setHeaderText("Déconnexion réussie !");
                success.setContentText("À bientôt sur AutoSales Pro 👋");
                styleAlert(success);
                success.show();

                // TODO: Rediriger vers page de login
            }
        });
    }

    // ========================================
    // 🛠️ UTILITAIRES
    // ========================================

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.show();
    }

    private void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: white; " +
                        "-fx-font-family: 'Segoe UI', 'Arial', sans-serif; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 20;"
        );

        dialogPane.lookup(".header-panel").setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-padding: 15;"
        );
    }
}