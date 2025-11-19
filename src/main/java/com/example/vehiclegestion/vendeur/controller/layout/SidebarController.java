package com.example.vehiclegestion.vendeur.controller.layout;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.io.IOException;

public class SidebarController {

    private StackPane contentPane;

    // Références aux sous-menus (existants)
    @FXML private VBox clientSubMenu;
    @FXML private VBox productSubMenu;
    @FXML private VBox orderSubMenu;
    @FXML private VBox cartSubMenu;
    @FXML private VBox statsSubMenu;
    @FXML private VBox messageSubMenu;
    @FXML private VBox settingsSubMenu;

    // Références aux flèches (existants)
    @FXML private Label clientArrow;
    @FXML private Label productArrow;
    @FXML private Label orderArrow;
    @FXML private Label cartArrow;
    @FXML private Label statsArrow;
    @FXML private Label messageArrow;
    @FXML private Label settingsArrow;

    // NOUVEAUX : Références pour la sidebar dynamique
    @FXML private VBox sidebar;  // La VBox principale de la sidebar
    @FXML private Button toggleBtn;  // Bouton toggle (☰)
    @FXML private VBox menuContainer;  // Conteneur du menu pour masquer les textes
    @FXML private Label mainNavLabel;  // Label "MAIN NAVIGATION"
    @FXML private Button dashboardBtn;  // Bouton Dashboard
    @FXML private Label dashboardText;  // Texte "Dashboard"
    @FXML private Button clientBtn;  // Bouton Clients
    @FXML private Label clientText;  // Texte "Clients"
    @FXML private Button productBtn;  // Bouton Produits
    @FXML private Label productText;  // Texte "Produits / Véhicules"
    @FXML private Button orderBtn;  // Bouton Commandes
    @FXML private Label orderText;  // Texte "Commandes / Réservations"
    @FXML private Button cartBtn;  // Bouton Achats
    @FXML private Label cartText;  // Texte "Achats / Panier"
    @FXML private Button statsBtn;  // Bouton Statistiques
    @FXML private Label statsText;  // Texte "Statistiques"
    @FXML private Button messageBtn;  // Bouton Messages
    @FXML private Label messageText;  // Texte "Messages / Notifications"
    @FXML private Button settingsBtn;  // Bouton Paramètres
    @FXML private Label settingsText;  // Texte "Paramètres"

    // Variables pour l'état de la sidebar
    private boolean isExpanded = false;
    private boolean isHovered = false;  // Pour suivre si c'est étendu par hover
    private final double collapsedWidth = 60;
    private final double expandedWidth = 255;

    public void setContentPane(StackPane contentPane) {
        this.contentPane = contentPane;
    }

    @FXML
    public void initialize() {
        System.out.println("SidebarController initialisé");
        // Masquer les textes au démarrage pour commencer rétracté
        hideTexts();

        // Ajouter les listeners pour le hover
        sidebar.setOnMouseEntered(event -> onMouseEnter());
        sidebar.setOnMouseExited(event -> onMouseExit());
    }

    // NOUVEAU : Méthode appelée quand la souris entre dans la sidebar
    private void onMouseEnter() {
        if (!isExpanded) {  // Étendre seulement si pas déjà étendu manuellement
            expandSidebar();
            isHovered = true;
        }
    }

    // NOUVEAU : Méthode appelée quand la souris sort de la sidebar
    private void onMouseExit() {
        if (isHovered && !isExpanded) {  // Rétracter seulement si étendu par hover et pas manuellement
            collapseSidebar();
            isHovered = false;
        }
    }

    // Méthode pour étendre la sidebar (utilisée par hover et toggle)
    private void expandSidebar() {
        showTexts();
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(sidebar.prefWidthProperty(), collapsedWidth)),
                new KeyFrame(Duration.millis(300), new KeyValue(sidebar.prefWidthProperty(), expandedWidth))
        );
        timeline.play();
    }

    // Méthode pour rétracter la sidebar (utilisée par hover et toggle)
    private void collapseSidebar() {
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(sidebar.prefWidthProperty(), expandedWidth)),
                new KeyFrame(Duration.millis(300), new KeyValue(sidebar.prefWidthProperty(), collapsedWidth))
        );
        timeline.setOnFinished(e -> hideTexts());
        timeline.play();
    }

    // NOUVEAU : Méthode toggle sidebar
    @FXML
    private void toggleSidebar() {
        if (isExpanded) {
            collapseSidebar();
            isExpanded = false;
            isHovered = false;  // Réinitialiser le hover
        } else {
            expandSidebar();
            isExpanded = true;
        }
    }

    // Méthodes utilitaires pour masquer/montrer les textes
    private void hideTexts() {
        mainNavLabel.setVisible(false);
        dashboardText.setVisible(false);
        clientText.setVisible(false);
        productText.setVisible(false);
        orderText.setVisible(false);
        cartText.setVisible(false);
        statsText.setVisible(false);
        messageText.setVisible(false);
        settingsText.setVisible(false);
    }

    private void showTexts() {
        mainNavLabel.setVisible(true);
        dashboardText.setVisible(true);
        clientText.setVisible(true);
        productText.setVisible(true);
        orderText.setVisible(true);
        cartText.setVisible(true);
        statsText.setVisible(true);
        messageText.setVisible(true);
        settingsText.setVisible(true);
    }

    // ================= MÉTHODES TOGGLE MENUS =================

    @FXML
    private void toggleClientMenu() {
        toggleSubMenu(clientSubMenu, clientArrow);
    }

    @FXML
    private void toggleProductMenu() {
        toggleSubMenu(productSubMenu, productArrow);
    }

    @FXML
    private void toggleOrderMenu() {
        toggleSubMenu(orderSubMenu, orderArrow);
    }

    @FXML
    private void toggleCartMenu() {
        toggleSubMenu(cartSubMenu, cartArrow);
    }

    @FXML
    private void toggleStatsMenu() {
        toggleSubMenu(statsSubMenu, statsArrow);
    }

    @FXML
    private void toggleMessageMenu() {
        loadContent("/view/vendeur/RendezVousList.fxml");
    }


    @FXML
    private void toggleSettingsMenu() {
        toggleSubMenu(settingsSubMenu, settingsArrow);
    }

    private void toggleSubMenu(VBox subMenu, Label arrow) {
        boolean isVisible = subMenu.isVisible();
        subMenu.setVisible(!isVisible);
        subMenu.setManaged(!isVisible);
        arrow.setText(isVisible ? "▶" : "▼");
    }

// ================= MÉTHODES DASHBOARD =================
@FXML
private void showDashboard() {
    loadContent("/view/vendeur/VendeurDashboard.fxml");
}

    // ================= MÉTHODES CLIENTS =================

    @FXML
    private void showCustomerList() {
        loadContent("/view/vendeur/ClientList.fxml");
    }

    @FXML
    private void showAddCustomer() {
        showAlert("Clients", "Ajouter un client");
    }

    @FXML
    private void showCustomerHistory() {
        showAlert("Clients", "Historique des interactions");
    }

    // ================= MÉTHODES PRODUITS/VÉHICULES =================


    @FXML
    private void showAddProduct() {
        showAlert("Produits", "Ajouter un produit");
    }

    @FXML
    private void showStockManagement() {
        loadContent("/view/vendeur/MagasinDetails.fxml");
    }

    @FXML
    private void showVehicleManagement() {
        loadContent("/view/vendeur/VendeurVehicle.fxml");
    }

    // ================= MÉTHODES COMMANDES =================

    @FXML
    private void showCurrentOrders() {
        showAlert("Commandes", "Commandes en cours");
    }

    @FXML
    private void showCompletedOrders() {
        showAlert("Commandes", "Commandes terminées");
    }

    @FXML
    private void showPendingReservations() {
        loadContent("/view/vendeur/reservations_vendeur.fxml");
    }

    @FXML
    private void showPurchaseRequests() {
        //loadContent("/view/vendeur/VendeurPurchaseRequests.fxml");
    }

    @FXML
    private void showMagasines() {
        loadContent("/view/vendeur/MagasinList.fxml");
    }

    @FXML
    private void showSalesHistory() {
        showAlert("Ventes", "Historique des ventes");
    }

    // ================= MÉTHODES ACHATS/PANIER =================

    @FXML
    private void showMyCart() {
        showAlert("Panier", "Mon panier");
    }

    @FXML
    private void showMyPurchases() {
        showAlert("Achats", "Mes achats");
    }

    // ================= MÉTHODES STATISTIQUES =================

    @FXML
    private void showMonthlySales() {
        showAlert("Statistiques", "Ventes mensuelles");
    }

    @FXML
    private void showRevenueStats() {
        showAlert("Statistiques", "Statistiques de revenus");
    }

    @FXML
    private void showClientPerformance() {
        showAlert("Statistiques", "Performances clients");
    }

    @FXML
    private void showSalesAnalytics() {
        showAlert("Analytics", "Analytics ventes");
    }

    @FXML
    private void showLeadFunnel() {
        showAlert("Analytics", "Lead Funnel");
    }

    // ================= MÉTHODES MESSAGES =================

    @FXML
    private void showInbox() {
        showAlert("Messages", "Boîte de réception");
    }

    @FXML
    private void showImportantAlerts() {
        showAlert("Notifications", "Alertes importantes");
    }

    // ================= MÉTHODES PARAMÈTRES =================

    @FXML
    private void showProfile() {
        showAlert("Profil", "Mon profil");
    }

    @FXML
    private void showPreferences() {
        showAlert("Paramètres", "Préférences");
    }

    @FXML
    private void showPrivacySecurity() {
        showAlert("Sécurité", "Confidentialité et sécurité");
    }

    // ================= MÉTHODE UTILITAIRE =================

    private void loadContent(String fxmlPath) {
        if (contentPane == null) {
            System.err.println("ContentPane non défini");
            return;
        }

        try {
            Node content = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentPane.getChildren().setAll(content);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorContent("Erreur de chargement: " + fxmlPath);
        }
    }

    private void showErrorContent(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-padding: 20;");
        contentPane.getChildren().setAll(errorLabel);
    }

    private void showAlert(String title, String message) {
        System.out.println(title + ": " + message);
        // Pour l'instant on affiche dans la console
        // Vous pouvez remplacer par des vraies alertes plus tard
    }
}