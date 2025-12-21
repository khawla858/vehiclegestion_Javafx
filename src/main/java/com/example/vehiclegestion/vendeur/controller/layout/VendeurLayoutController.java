package com.example.vehiclegestion.vendeur.controller.layout;

import com.example.vehiclegestion.utils.NavigationManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class VendeurLayoutController {

    @FXML private AnchorPane navbarContainer;
    @FXML private StackPane contentPane;

    private NavigationManager navigationManager;

    @FXML
    public void initialize() {
        System.out.println("\n🚀 === INITIALISATION VENDEUR LAYOUT ===");

        try {
            // ✅ ÉTAPE 1 : Charger la Navbar
            System.out.println("📋 Chargement Navbar...");
            FXMLLoader navbarLoader = new FXMLLoader(
                    getClass().getResource("/view/vendeur/layout/Navbar.fxml")
            );
            Node navbar = navbarLoader.load();

            // ✅ FORCER LES ANCRAGES (navbarContainer est un AnchorPane)
            AnchorPane.setLeftAnchor(navbar, 0.0);
            AnchorPane.setRightAnchor(navbar, 0.0);
            AnchorPane.setTopAnchor(navbar, 0.0);
            AnchorPane.setBottomAnchor(navbar, 0.0);

            navbarContainer.getChildren().setAll(navbar);
            System.out.println("✅ Navbar chargée avec ancrages full-width");

            // ✅ ÉTAPE 2 : Récupérer le contrôleur Navbar et lui passer contentPane
            NavbarController navbarController = navbarLoader.getController();
            if (navbarController != null) {
                navbarController.setContentPane(contentPane);
                System.out.println("✅ ContentPane injecté dans NavbarController");
            } else {
                System.err.println("❌ NavbarController est null !");
            }

            // ✅ ÉTAPE 3 : Initialiser NavigationManager
            navigationManager = NavigationManager.getInstance();
            navigationManager.setContentPane(contentPane);
            System.out.println("✅ NavigationManager initialisé");

            // ✅ ÉTAPE 4 : Charger Dashboard par défaut
            navigationManager.goToDashboard();

            System.out.println("🚀 === LAYOUT PRÊT ===\n");

        } catch (IOException e) {
            System.err.println("❌ ERREUR INITIALISATION LAYOUT");
            e.printStackTrace();
            showErrorContent();
        } catch (Exception e) {
            System.err.println("❌ ERREUR INATTENDUE");
            e.printStackTrace();
            showErrorContent();
        }
    }
    /**
     * Affiche un message d'erreur en cas de problème
     */
    private void showErrorContent() {
        javafx.scene.control.Label errorLabel = new javafx.scene.control.Label(
                "❌ Erreur de chargement de l'interface"
        );
        errorLabel.setStyle(
                "-fx-text-fill: #e74c3c; " +
                        "-fx-font-size: 18px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 50;"
        );
        contentPane.getChildren().setAll(errorLabel);
    }
}