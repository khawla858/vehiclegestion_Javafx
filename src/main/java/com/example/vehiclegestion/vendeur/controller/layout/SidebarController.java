package com.example.vehiclegestion.vendeur.controller.layout;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class SidebarController {

    private StackPane contentPane;

    // Références aux sous-menus
    @FXML private VBox clientSubMenu;
    @FXML private VBox productSubMenu;
    @FXML private VBox orderSubMenu;
    @FXML private VBox cartSubMenu;
    @FXML private VBox statsSubMenu;
    @FXML private VBox messageSubMenu;
    @FXML private VBox settingsSubMenu;

    // Références aux flèches
    @FXML private Label clientArrow;
    @FXML private Label productArrow;
    @FXML private Label orderArrow;
    @FXML private Label cartArrow;
    @FXML private Label statsArrow;
    @FXML private Label messageArrow;
    @FXML private Label settingsArrow;

    public void setContentPane(StackPane contentPane) {
        this.contentPane = contentPane;
    }

    @FXML
    public void initialize() {
        System.out.println("SidebarController initialisé");
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
        toggleSubMenu(messageSubMenu, messageArrow);
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
        showAlert("Clients", "Liste des clients chargée");
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
        showAlert("Produits", "Gestion du stock");
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
        showAlert("Réservations", "Réservations à confirmer");
    }

    @FXML
    private void showPurchaseRequests() {
        loadContent("/view/vendeur/VendeurPurchaseRequests.fxml");
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