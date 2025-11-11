package com.example.vehiclegestion.vendeur.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.Node;

import java.io.IOException;

public class VendeurLayoutController {

    @FXML
    private AnchorPane contentPane;

    @FXML
    public void initialize() throws IOException {
        // Par défaut, charger le Dashboard
        loadPage("dashboard-content.fxml");
    }

    // Méthode générique pour charger une page dans le center
    private void loadPage(String fxmlFile) {
        try {
            Node node = FXMLLoader.load(getClass().getResource("/com/example/vehiclegestion/vendeur/view/" + fxmlFile));
            contentPane.getChildren().setAll(node);
            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Boutons de la sidebar
    @FXML
    private void showDashboard() { loadPage("dashboard-content.fxml"); }

    @FXML
    private void showVehicleManagement() { loadPage("vehicle-management.fxml"); }

    @FXML
    private void showPurchaseRequests() { loadPage("purchase-request.fxml"); }

    @FXML
    private void showSalesHistory() { loadPage("sales-history.fxml"); }

    @FXML
    private void showProfile() { loadPage("profile.fxml"); }

    @FXML
    private void handleLogout() {
        // Rediriger vers login
        // Navigation.navigateTo("common/login.fxml", ???);
    }
}
