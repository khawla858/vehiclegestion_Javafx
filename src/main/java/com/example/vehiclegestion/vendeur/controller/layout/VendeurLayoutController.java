package com.example.vehiclegestion.vendeur.controller.layout;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane; // Importez Pane
import java.io.IOException;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Node; // Importez Node

public class VendeurLayoutController {

    @FXML
    private AnchorPane navbarContainer;

    @FXML
    private AnchorPane sidebarContainer;

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        try {
            // Charger Navbar - utilisez Node au lieu d'AnchorPane
            FXMLLoader navbarLoader = new FXMLLoader(getClass().getResource("/view/vendeur/layout/Navbar.fxml"));
            Node navbar = navbarLoader.load(); // Changez AnchorPane en Node
            navbarContainer.getChildren().setAll(navbar);

            // Charger Sidebar - utilisez Node au lieu de VBox
            FXMLLoader sidebarLoader = new FXMLLoader(getClass().getResource("/view/vendeur/layout/Sidebar.fxml"));
            Node sidebar = sidebarLoader.load(); // Changez VBox en Node
            sidebarContainer.getChildren().setAll(sidebar);

            // Récupérer le controller Sidebar pour lui passer contentPane
            SidebarController sidebarController = sidebarLoader.getController();
            if (sidebarController != null) {
                sidebarController.setContentPane(contentPane);
            } else {
                System.err.println("SidebarController est null");
            }

            // Charger Dashboard par défaut
            loadDashboard();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorContent();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorContent();
        }
    }

    public void loadDashboard() {
        try {
            Node dashboard = FXMLLoader.load(getClass().getResource("/view/vendeur/VendeurDashboard.fxml"));
            contentPane.getChildren().setAll(dashboard);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorContent();
        }
    }

    private void showErrorContent() {
        // Affichez un contenu d'erreur simple
        javafx.scene.control.Label errorLabel = new javafx.scene.control.Label("Erreur de chargement");
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
        contentPane.getChildren().setAll(errorLabel);
    }
}