package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.common.service.SearchService;
import com.example.vehiclegestion.client.model.Vehicle;
import javafx.animation.KeyFrame;
import com.example.vehiclegestion.client.doa.VehicleDAO;
import com.example.vehiclegestion.vendeur.dao.MagasinDAO;
import com.example.vehiclegestion.vendeur.model.Magasin;

// Imports JavaFX
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Background;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;

// Imports pour les listes
import java.util.List;
import java.util.ArrayList;
import com.example.vehiclegestion.common.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.animation.Timeline;
import javafx.application.Platform;

// Imports FXML
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.stage.Window;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;

// Import pour VehiDetaiCo (assurez-vous que cette classe existe)
import com.example.vehiclegestion.client.controller.VehiDetaiCo;

public class PublicMainController {

    @FXML private BorderPane mainBorderPane;
    @FXML private TextField searchField;
    @FXML private Button homeBtn;
    @FXML private Button vehiclesBtn;
    @FXML private Button promotionsBtn;
    @FXML private Button aboutBtn;
    @FXML private Button contactBtn;
    @FXML private Button loginBtn;
    @FXML private Button registerBtn;

    // 🔍 Éléments pour la recherche
    private Stage searchResultsStage;
    private VBox searchResultsContainer;
    private boolean searchDropdownVisible = false;
    private Timeline hideSearchResultsTimer;

    private SearchService searchService;

    @FXML
    public void initialize() {
        System.out.println("🚀 HomeController initialisé (Page d'accueil avant login)");

        initializeInterface();
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void initializeInterface() {
        System.out.println("🎨 Initialisation de l'interface d'accueil");

        if (mainBorderPane != null) {
            mainBorderPane.setUserData(this);
        }

        searchService = SearchService.getInstance();

        showHome();
        initializeSearch();
        setupClickOutsideListener();
    }

    // ============================================
    // 🔍 SYSTÈME DE RECHERCHE COMPLET
    // ============================================

    private void initializeSearch() {
        if (searchField != null) {
            System.out.println("🔍 Initialisation de la barre de recherche");

            // Listener pour la recherche en temps réel
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.trim().isEmpty()) {
                    if (newValue.trim().length() >= 2) {
                        performSearch(newValue.trim());
                    } else {
                        hideSearchResults();
                    }
                } else {
                    hideSearchResults();
                }
            });

            // Focus listener modifié - plus de délai automatique
            searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    // Annuler le timer s'il existe
                    if (hideSearchResultsTimer != null) {
                        hideSearchResultsTimer.stop();
                    }
                    // Ne pas masquer automatiquement - laisser le clic extérieur gérer
                }
            });

            // Écouteur pour la touche Entrée
            searchField.setOnAction(e -> {
                String query = searchField.getText().trim();
                if (!query.isEmpty() && query.length() >= 2) {
                    performSearch(query);
                }
            });

            searchField.setPromptText("Rechercher par marque, modèle, année...");
        }
    }

    private void performSearch(String query) {
        System.out.println("🔍 Recherche en cours pour: " + query);

        List<SearchResult> results = searchInDatabase(query);

        if (results.isEmpty()) {
            showNoResults(query);
        } else {
            displaySearchResults(results);
        }
    }

    private void displaySearchResults(List<SearchResult> results) {
        Platform.runLater(() -> {
            if (searchResultsContainer == null) {
                createSearchResultsContainer();
            }

            searchResultsContainer.getChildren().clear();

            // Header
            HBox header = new HBox();
            header.setStyle("-fx-padding: 15; -fx-background-color: rgba(15, 23, 42, 0.95); " +
                    "-fx-border-width: 0 0 1 0; -fx-border-color: #334155;");
            Label headerLabel = new Label("Résultats de recherche (" + results.size() + ")");
            headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");
            header.getChildren().add(headerLabel);
            searchResultsContainer.getChildren().add(header);

            // Résultats
            for (SearchResult result : results) {
                HBox resultItem = createSearchResultItem(result);
                searchResultsContainer.getChildren().add(resultItem);
            }

            showSearchResults();
        });
    }

    private HBox createSearchResultItem(SearchResult result) {
        HBox item = new HBox(12);
        item.setStyle("-fx-padding: 12 15; -fx-border-width: 0 0 1 0; " +
                "-fx-border-color: #334155; -fx-background-color: rgba(30, 41, 59, 0.8); " +
                "-fx-cursor: hand;");
        item.setAlignment(Pos.CENTER_LEFT);

        // Icône
        Label icon = new Label(result.getIcon());
        icon.setStyle("-fx-font-size: 20px; -fx-min-width: 30px;");

        // Contenu
        VBox content = new VBox(3);
        content.setMaxWidth(400);

        Label title = new Label(result.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #f1f5f9;");
        title.setWrapText(true);

        Label description = new Label(result.getDescription());
        description.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        description.setWrapText(true);

        Label category = new Label(result.getCategory());
        category.setStyle("-fx-background-color: rgba(59, 130, 246, 0.2); " +
                "-fx-text-fill: #3b82f6; -fx-padding: 2 8; " +
                "-fx-font-size: 10px; -fx-background-radius: 10;");

        content.getChildren().addAll(title, description, category);
        item.getChildren().addAll(icon, content);

        // Événement de clic
        item.setOnMouseClicked(e -> {
            hideSearchResults();
            searchField.clear();

            // Si c'est un article/véhicule, rediriger vers login
            if ("ARTICLE".equals(result.getType()) || "VEHICLE".equals(result.getType())) {
                int vehicleId = result.getId();
                showLoginForVehicleDetails(vehicleId);
            } else if ("STORE".equals(result.getType())) {
                int storeId = result.getId();
                showLoginForStoreDetails(storeId);
            } else if ("CITY".equals(result.getType())) {
                result.getAction().run();
            } else {
                // Sinon, exécuter l'action par défaut
                result.getAction().run();
            }
        });

        // Effets de survol
        item.setOnMouseEntered(e -> item.setStyle(item.getStyle().replace(
                "-fx-background-color: rgba(30, 41, 59, 0.8);",
                "-fx-background-color: rgba(59, 130, 246, 0.1);"
        )));

        item.setOnMouseExited(e -> item.setStyle(item.getStyle().replace(
                "-fx-background-color: rgba(59, 130, 246, 0.1);",
                "-fx-background-color: rgba(30, 41, 59, 0.8);"
        )));

        return item;
    }

    private void createSearchResultsContainer() {
        if (searchResultsStage == null) {
            searchResultsStage = new Stage();
            searchResultsStage.initStyle(StageStyle.UNDECORATED);
            searchResultsStage.initOwner(findCurrentStage());

            searchResultsContainer = new VBox();
            searchResultsContainer.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8;");

            Scene scene = new Scene(searchResultsContainer, 500, 400);
            scene.setFill(null);
            searchResultsStage.setScene(scene);

            // Effet d'ombre
            searchResultsContainer.setEffect(new javafx.scene.effect.DropShadow(20, javafx.scene.paint.Color.BLACK));

            // Empêcher la fenêtre de prendre le focus
            searchResultsStage.setAlwaysOnTop(true);
        }
    }

    private void showSearchResults() {
        if (searchResultsStage != null && searchField != null) {
            // Positionner la fenêtre sous la barre de recherche
            javafx.geometry.Bounds bounds = searchField.localToScreen(searchField.getBoundsInLocal());
            searchResultsStage.setX(bounds.getMinX() + 50);
            searchResultsStage.setY(bounds.getMaxY() + 5);

            searchResultsStage.show();
            searchDropdownVisible = true;
        }
    }

    private void hideSearchResults() {
        if (searchResultsStage != null && searchDropdownVisible) {
            searchResultsStage.hide();
            searchDropdownVisible = false;
        }
    }

    private void showNoResults(String query) {
        Platform.runLater(() -> {
            if (searchResultsContainer == null) {
                createSearchResultsContainer();
            }

            searchResultsContainer.getChildren().clear();

            VBox emptyState = new VBox(15);
            emptyState.setStyle("-fx-alignment: center; -fx-padding: 60 20;");

            Label icon = new Label("🔍");
            icon.setStyle("-fx-font-size: 40px;");

            Label message = new Label("Aucun résultat pour \"" + query + "\"");
            message.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

            Label suggestion = new Label("Essayez avec d'autres mots-clés");
            suggestion.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

            emptyState.getChildren().addAll(icon, message, suggestion);
            searchResultsContainer.getChildren().add(emptyState);

            showSearchResults();
        });
    }

    // ============================================
    // GESTION DU CLIC EXTÉRIEUR
    // ============================================

    private void setupClickOutsideListener() {
        if (mainBorderPane != null && mainBorderPane.getScene() != null) {
            mainBorderPane.getScene().addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
                handleClickOutside(e);
            });
        }
    }

    private void handleClickOutside(javafx.scene.input.MouseEvent e) {
        boolean clickedOnSearchField = isClickOnNode(e, searchField);
        boolean clickedOnSearchDropdown = isClickOnStage(e, searchResultsStage);

        // Gérer le clic en dehors du dropdown de recherche
        if (searchDropdownVisible && !clickedOnSearchField && !clickedOnSearchDropdown) {
            hideSearchResults();
        }
    }

    private boolean isClickOnNode(javafx.scene.input.MouseEvent e, javafx.scene.Node node) {
        if (node == null || !node.isVisible()) return false;

        javafx.geometry.Bounds bounds = node.localToScene(node.getBoundsInLocal());
        return bounds.contains(e.getSceneX(), e.getSceneY());
    }

    private boolean isClickOnStage(javafx.scene.input.MouseEvent e, Stage stage) {
        if (stage == null || !stage.isShowing()) return false;

        javafx.geometry.Bounds bounds = new javafx.geometry.BoundingBox(
                stage.getX(), stage.getY(),
                stage.getWidth(), stage.getHeight()
        );
        return bounds.contains(e.getScreenX(), e.getScreenY());
    }

    // ============================================
    // NAVIGATION PRINCIPALE - VERSION ACCUEIL
    // ============================================

    @FXML
    private void showHome() {
        System.out.println("🏠 Navigation vers Accueil");
        setActiveMenu(homeBtn);
        // Le contenu d'accueil est déjà dans le FXML
        // Pas besoin de charger un autre FXML
    }

    @FXML
    private void showVehicles() {
        System.out.println("🚗 Navigation vers Véhicules");
        setActiveMenu(vehiclesBtn);
        // Pour les visiteurs, on affiche une page simplifiée
        loadVisitorVehicles();
    }

    @FXML
    private void showPromotions() {
        System.out.println("🔥 Navigation vers Promotions");
        setActiveMenu(promotionsBtn);
        showVisitorMessage("Promotions", "Connectez-vous pour voir nos promotions exclusives !");
    }

    @FXML
    private void showAbout() {
        System.out.println("ℹ️ Navigation vers À Propos");
        setActiveMenu(aboutBtn);
        showAboutPage();
    }

    @FXML
    private void showContact() {
        System.out.println("📞 Navigation vers Contact");
        setActiveMenu(contactBtn);
        showContactPage();
    }

    @FXML
    private void showLogin() {
        System.out.println("🔐 Ouverture page de connexion");
        redirectToLogin();
    }

    @FXML
    private void showRegister() {
        System.out.println("📝 Ouverture page d'inscription");
        redirectToRegister();
    }

    private void setActiveMenu(Button activeButton) {
        if (homeBtn != null) homeBtn.setStyle(getInactiveStyle());
        if (vehiclesBtn != null) vehiclesBtn.setStyle(getInactiveStyle());
        if (promotionsBtn != null) promotionsBtn.setStyle(getInactiveStyle());
        if (aboutBtn != null) aboutBtn.setStyle(getInactiveStyle());
        if (contactBtn != null) contactBtn.setStyle(getInactiveStyle());

        if (activeButton != null) {
            activeButton.setStyle(getActiveStyle());
        }
    }

    private String getInactiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #cbd5e1; " +
                "-fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 20 25; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: transparent;";
    }

    private String getActiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #3b82f6; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 20 25; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: #3b82f6;";
    }

    private void showVisitorMessage(String title, String message) {
        VBox messageBox = new VBox(20);
        messageBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 40; -fx-alignment: center; -fx-spacing: 15;");

        Label titleLabel = new Label("🔒 " + title);
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #cbd5e1; -fx-text-alignment: center;");
        messageLabel.setWrapText(true);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginButton = new Button("Se Connecter");
        loginButton.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        loginButton.setOnAction(e -> redirectToLogin());

        Button registerButton = new Button("S'inscrire");
        registerButton.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-padding: 12 24; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        registerButton.setOnAction(e -> redirectToRegister());

        buttonBox.getChildren().addAll(loginButton, registerButton);

        messageBox.getChildren().addAll(titleLabel, messageLabel, buttonBox);

        if (mainBorderPane != null) {
            mainBorderPane.setCenter(messageBox);
        }
    }

    private void loadVisitorVehicles() {
        try {
            System.out.println("🚗 Chargement des véhicules pour visiteurs");

            // Charger une version simplifiée des véhicules
            URL url = getClass().getResource("/view/client/visitor-vehicles.fxml");
            if (url == null) {
                // Fallback vers une page simple
                showVisitorMessage("Véhicules", "Explorez notre sélection de véhicules. Connectez-vous pour plus de détails !");
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent vehiclesView = loader.load();

            // Afficher dans la zone centrale
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(vehiclesView);
                System.out.println("✅ Véhicules visiteurs affichés");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement véhicules visiteurs: " + e.getMessage());
            e.printStackTrace();
            showVisitorMessage("Véhicules", "Explorez notre sélection de véhicules. Connectez-vous pour plus de détails !");
        }
    }

    private void showAboutPage() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #0f172a;");

        VBox content = new VBox(30);
        content.setStyle("-fx-padding: 40; -fx-background-color: #0f172a;");

        // Titre
        Label title = new Label("À Propos d'AutoSales Pro");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

        // Description
        VBox aboutSection = new VBox(15);
        aboutSection.setStyle("-fx-background-color: rgba(30, 41, 59, 0.8); " +
                "-fx-background-radius: 12; -fx-padding: 25;");

        Label aboutTitle = new Label("Notre Mission");
        aboutTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        Label aboutText = new Label("AutoSales Pro est la plateforme premium pour l'achat et la vente de véhicules au Maroc. " +
                "Nous connectons les passionnés d'automobiles avec les meilleurs vendeurs et les véhicules les plus recherchés. " +
                "Notre mission est de rendre l'expérience d'achat de véhicules transparente, sécurisée et agréable.");
        aboutText.setStyle("-fx-font-size: 16px; -fx-text-fill: #cbd5e1; -fx-line-spacing: 1.5;");
        aboutText.setWrapText(true);

        aboutSection.getChildren().addAll(aboutTitle, aboutText);

        // Statistiques
        HBox statsBox = new HBox(40);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle("-fx-padding: 30;");

        statsBox.getChildren().addAll(
                createStat("500+", "Véhicules"),
                createStat("50+", "Vendeurs"),
                createStat("98%", "Satisfaction"),
                createStat("10+", "Années d'expérience")
        );

        // Rejoindre nous
        VBox joinSection = new VBox(20);
        joinSection.setStyle("-fx-background-color: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(139, 92, 246, 0.1)); " +
                "-fx-background-radius: 12; -fx-padding: 30; -fx-alignment: center;");

        Label joinTitle = new Label("Rejoignez notre communauté !");
        joinTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

        Label joinText = new Label("Créez un compte gratuitement pour accéder à toutes les fonctionnalités");
        joinText.setStyle("-fx-font-size: 16px; -fx-text-fill: #cbd5e1;");

        HBox joinButtons = new HBox(15);
        joinButtons.setAlignment(Pos.CENTER);

        Button joinLogin = new Button("Se Connecter");
        joinLogin.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        joinLogin.setOnAction(e -> redirectToLogin());

        Button joinRegister = new Button("S'inscrire");
        joinRegister.setStyle("-fx-background-color: linear-gradient(135deg, #ec4899 0%, #8b5cf6 100%); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        joinRegister.setOnAction(e -> redirectToRegister());

        joinButtons.getChildren().addAll(joinLogin, joinRegister);
        joinSection.getChildren().addAll(joinTitle, joinText, joinButtons);

        content.getChildren().addAll(title, aboutSection, statsBox, joinSection);
        scrollPane.setContent(content);

        if (mainBorderPane != null) {
            mainBorderPane.setCenter(scrollPane);
        }
    }

    private VBox createStat(String value, String label) {
        VBox stat = new VBox(5);
        stat.setAlignment(Pos.CENTER);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; " +
                "-fx-text-fill: linear-gradient(to right, #ec4899, #8b5cf6);");

        Label descLabel = new Label(label);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");

        stat.getChildren().addAll(valueLabel, descLabel);
        return stat;
    }

    private void showContactPage() {
        VBox content = new VBox(30);
        content.setStyle("-fx-padding: 40; -fx-background-color: #0f172a; -fx-alignment: center;");

        // Titre
        Label title = new Label("📞 Contactez-nous");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

        // Carte de contact
        VBox contactCard = new VBox(20);
        contactCard.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); " +
                "-fx-background-radius: 12; -fx-padding: 30; " +
                "-fx-border-color: linear-gradient(to right, #3b82f6, #8b5cf6); " +
                "-fx-border-width: 2; -fx-border-radius: 12;");
        contactCard.setMaxWidth(500);

        Label contactTitle = new Label("Informations de contact");
        contactTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

        VBox contactInfo = new VBox(15);
        contactInfo.getChildren().addAll(
                createContactRow("📍", "Adresse", "Casablanca, Maroc"),
                createContactRow("📞", "Téléphone", "+212 5 XX XX XX XX"),
                createContactRow("✉️", "Email", "contact@autosalespro.ma"),
                createContactRow("⏰", "Horaires", "Lun-Ven: 9h-18h, Sam: 10h-16h")
        );

        Label needAccount = new Label("Pour contacter un vendeur spécifique, vous devez être connecté.");
        needAccount.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");

        HBox contactButtons = new HBox(15);
        contactButtons.setAlignment(Pos.CENTER);

        Button loginBtn = new Button("Se Connecter");
        loginBtn.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        loginBtn.setOnAction(e -> redirectToLogin());

        Button registerBtn = new Button("S'inscrire");
        registerBtn.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-padding: 12 24; " +
                "-fx-border-color: #3b82f6; -fx-border-width: 2; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-cursor: hand;");
        registerBtn.setOnAction(e -> redirectToRegister());

        contactButtons.getChildren().addAll(loginBtn, registerBtn);

        contactCard.getChildren().addAll(contactTitle, contactInfo, needAccount, contactButtons);
        content.getChildren().addAll(title, contactCard);

        if (mainBorderPane != null) {
            mainBorderPane.setCenter(content);
        }
    }

    private HBox createContactRow(String icon, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px; -fx-min-width: 30px;");

        VBox textBox = new VBox(2);
        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 15px; -fx-text-fill: #f1f5f9;");

        textBox.getChildren().addAll(labelText, valueText);
        row.getChildren().addAll(iconLabel, textBox);
        return row;
    }

    // ============================================
    // REDIRECTIONS VERS AUTH
    // ============================================

    private void redirectToLogin() {
        try {
            System.out.println("🔄 Redirection vers la page de connexion...");

            // Obtenir la fenêtre actuelle
            Stage currentStage = findCurrentStage();
            if (currentStage == null) {
                System.err.println("❌ Impossible de trouver la fenêtre actuelle");
                return;
            }

            // Charger la page de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
            Parent loginPage = loader.load();

            // Configurer la nouvelle scène
            Scene scene = new Scene(loginPage);
            currentStage.setScene(scene);
            currentStage.setTitle("Connexion - AutoSales Pro");
            currentStage.setMaximized(false);
            currentStage.setWidth(1000);
            currentStage.setHeight(700);
            currentStage.centerOnScreen();

            System.out.println("✅ Redirection vers login réussie");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la redirection vers login: " + e.getMessage());
            e.printStackTrace();

            // Fallback: afficher une alerte
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de redirection");
            alert.setHeaderText("Impossible de charger la page de connexion");
            alert.setContentText("Veuillez réessayer.");
            alert.showAndWait();
        }
    }

    private void redirectToRegister() {
        try {
            System.out.println("🔄 Redirection vers la page d'inscription...");

            // Obtenir la fenêtre actuelle
            Stage currentStage = findCurrentStage();
            if (currentStage == null) {
                System.err.println("❌ Impossible de trouver la fenêtre actuelle");
                return;
            }

            // Charger la page d'inscription
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/register.fxml"));
            Parent registerPage = loader.load();

            // Configurer la nouvelle scène
            Scene scene = new Scene(registerPage);
            currentStage.setScene(scene);
            currentStage.setTitle("Inscription - AutoSales Pro");
            currentStage.setMaximized(false);
            currentStage.setWidth(1000);
            currentStage.setHeight(700);
            currentStage.centerOnScreen();

            System.out.println("✅ Redirection vers inscription réussie");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la redirection vers inscription: " + e.getMessage());
            e.printStackTrace();

            // Fallback: afficher une alerte
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de redirection");
            alert.setHeaderText("Impossible de charger la page d'inscription");
            alert.setContentText("Veuillez réessayer.");
            alert.showAndWait();
        }
    }

    // ============================================
    // RECHERCHE ET DÉTAILS - VERSION VISITEUR
    // ============================================

    /**
     * Redirige vers login pour voir les détails d'un véhicule
     */
    private void showLoginForVehicleDetails(int vehicleId) {
        System.out.println("🔒 Redirection vers login pour véhicule ID: " + vehicleId);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Connexion requise");
            alert.setHeaderText("Connectez-vous pour voir les détails du véhicule");
            alert.setContentText("Vous devez être connecté pour accéder aux détails complets du véhicule.");

            ButtonType loginButton = new ButtonType("Se Connecter");
            ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(loginButton, cancelButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == loginButton) {
                    redirectToLogin();
                }
            });
        });
    }

    /**
     * Redirige vers login pour voir les détails d'un magasin
     */
    private void showLoginForStoreDetails(int storeId) {
        System.out.println("🔒 Redirection vers login pour magasin ID: " + storeId);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Connexion requise");
            alert.setHeaderText("Connectez-vous pour voir les détails du magasin");
            alert.setContentText("Vous devez être connecté pour accéder aux informations détaillées du magasin.");

            ButtonType loginButton = new ButtonType("Se Connecter");
            ButtonType cancelButton = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(loginButton, cancelButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == loginButton) {
                    redirectToLogin();
                }
            });
        });
    }

    /**
     * Affiche tous les magasins d'une ville (accessible aux visiteurs)
     */
    private void showAllStoresInCity(String ville) {
        System.out.println("🏙️ Affichage de tous les magasins de: " + ville);

        hideSearchResults();
        searchField.clear();

        try {
            // Récupérer tous les magasins de la ville
            List<Magasin> magasins = getMagasinsByCity(ville);

            if (magasins.isEmpty()) {
                Platform.runLater(() -> {
                    showAlert("Aucun magasin", "Aucun magasin trouvé à " + capitalizeWords(ville));
                    showHome(); // Retour à la page d'accueil
                });
                return;
            }

            // Créer la vue
            Platform.runLater(() -> {
                createCityStoresView(ville, magasins);
            });

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage magasins ville: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Erreur", "Impossible de charger les magasins de " + ville);
            });
        }
    }

    // ============================================
    // MÉTHODES AUXILIAIRES (reprises de MainController)
    // ============================================

    private List<SearchResult> searchInDatabase(String query) {
        List<SearchResult> results = new ArrayList<>();

        if (searchService == null) {
            System.err.println("❌ SearchService non initialisé");
            return results;
        }

        try {
            // Vérifier si c'est une ville marocaine
            if (isVilleMarocaine(query)) {
                System.out.println("🏙️ Recherche de ville détectée: " + query);

                final String ville = query;
                results.add(new SearchResult(
                        "🏙️",
                        "Ville",
                        capitalizeWords(ville),
                        "Afficher tous les magasins de " + capitalizeWords(ville),
                        () -> showAllStoresInCity(ville),
                        0,
                        "CITY"
                ));

                List<SearchService.SearchResultData> magasinsVille = searchService.rechercherMagasinsParVille(query);

                for (SearchService.SearchResultData data : magasinsVille) {
                    final int storeId = data.getId();
                    results.add(new SearchResult(
                            data.getIcon(),
                            data.getCategory(),
                            data.getTitle(),
                            data.getDescription(),
                            () -> showLoginForStoreDetails(storeId), // Redirige vers login
                            data.getId(),
                            data.getType()
                    ));
                }

                System.out.println("✅ Trouvé " + magasinsVille.size() + " magasin(s) à " + ville);
                return results;
            }

            // Recherche normale
            List<SearchService.SearchResultData> searchResults = searchService.rechercheGlobale(query);

            for (SearchService.SearchResultData data : searchResults) {
                Runnable action = null;

                switch (data.getType()) {
                    case "ARTICLE":
                    case "VEHICLE":
                        final int articleId = data.getId();
                        action = () -> showLoginForVehicleDetails(articleId);
                        break;

                    case "STORE":
                        final int storeId = data.getId();
                        action = () -> showLoginForStoreDetails(storeId);
                        break;

                    case "SELLER":
                        final int sellerId = data.getId();
                        action = () -> redirectToLogin(); // Redirige vers login pour les vendeurs
                        break;

                    default:
                        action = () -> System.out.println("Action non définie");
                }

                results.add(new SearchResult(
                        data.getIcon(),
                        data.getCategory(),
                        data.getTitle(),
                        data.getDescription(),
                        action,
                        data.getId(),
                        data.getType()
                ));
            }

            System.out.println("✅ Recherche terminée: " + results.size() + " résultats trouvés");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Erreur de recherche", "Une erreur s'est produite lors de la recherche.");
            });
        }

        return results;
    }

    private List<Magasin> getMagasinsByCity(String ville) {
        List<Magasin> magasins = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT m.*, COUNT(a.id_article) as nb_articles " +
                    "FROM Magasin m " +
                    "LEFT JOIN Article a ON m.id_vendeur = a.id_vendeur " +
                    "WHERE LOWER(m.localisation) LIKE LOWER(?) " +
                    "GROUP BY m.id_magasin, m.nom_magasin, m.adresse, m.localisation, " +
                    "         m.categorie, m.description, m.telephone, m.email_contact, " +
                    "         m.id_vendeur, m.logo_magasin, m.site_web, m.facebook, " +
                    "         m.instagram, m.nb_ventes_mensuelles " +
                    "ORDER BY nb_articles DESC, m.nom_magasin";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + ville + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Magasin m = new Magasin();
                m.setIdMagasin(rs.getInt("id_magasin"));
                m.setNomMagasin(rs.getString("nom_magasin"));
                m.setAdresse(rs.getString("adresse"));
                m.setLocalisation(rs.getString("localisation"));
                m.setCategorie(rs.getString("categorie"));
                m.setDescription(rs.getString("description"));
                m.setTelephone(rs.getString("telephone"));
                m.setEmailContact(rs.getString("email_contact"));
                m.setIdVendeur(rs.getInt("id_vendeur"));
                m.setLogoMagasin(rs.getString("logo_magasin"));
                m.setSiteWeb(rs.getString("site_web"));
                m.setFacebook(rs.getString("facebook"));
                m.setInstagram(rs.getString("instagram"));
                m.setNbVentesMensuelles(rs.getInt("nb_ventes_mensuelles"));
                m.setNbCommentaires(rs.getInt("nb_articles"));

                magasins.add(m);
            }

            System.out.println("✅ Récupéré " + magasins.size() + " magasin(s) de " + ville);

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération magasins: " + e.getMessage());
            e.printStackTrace();
        }

        return magasins;
    }

    private void createCityStoresView(String ville, List<Magasin> magasins) {
        try {
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: #0a0f1c; -fx-background: #0a0f1c;");
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            VBox mainContainer = new VBox(15);
            mainContainer.setStyle("-fx-background-color: #0a0f1c; -fx-padding: 20;");
            mainContainer.setBackground(Background.EMPTY);

            // Header
            VBox headerBox = new VBox(8);
            headerBox.setStyle("-fx-background-color: transparent; -fx-padding: 0 0 15 0;");

            Label villeLabel = new Label("Magasins à " + capitalizeWords(ville));
            villeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e2e8f0;");

            Label countLabel = new Label(magasins.size() + " magasin(s) - Connectez-vous pour plus de détails");
            countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");

            headerBox.getChildren().addAll(villeLabel, countLabel);

            // Grille de magasins
            VBox storesGrid = new VBox(10);
            storesGrid.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
            storesGrid.setBackground(Background.EMPTY);

            for (Magasin magasin : magasins) {
                VBox storeCard = createVisitorStoreCard(magasin);
                storesGrid.getChildren().add(storeCard);
            }

            // Bouton retour
            HBox actionBox = new HBox();
            actionBox.setStyle("-fx-padding: 20 0 0 0; -fx-alignment: center-left;");

            Button backBtn = new Button("← Retour à l'accueil");
            backBtn.setStyle("-fx-background-color: transparent; " +
                    "-fx-text-fill: #60a5fa; " +
                    "-fx-padding: 8 16; " +
                    "-fx-border-color: #3b82f6; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand;");
            backBtn.setOnAction(e -> showHome());

            actionBox.getChildren().add(backBtn);

            // Assembler
            mainContainer.getChildren().addAll(headerBox, storesGrid, actionBox);
            scrollPane.setContent(mainContainer);

            // Afficher
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(scrollPane);
                System.out.println("✅ Page ville affichée: " + magasins.size() + " magasins");
                mainBorderPane.setStyle("-fx-background-color: #0a0f1c;");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur création page ville: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createVisitorStoreCard(Magasin magasin) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #111827; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 20; " +
                "-fx-border-color: #1f2937; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8;");

        // En-tête
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label storeIcon = new Label("🏪");
        storeIcon.setStyle("-fx-font-size: 32px;");

        VBox nameBox = new VBox(3);
        Label nomLabel = new Label(magasin.getNomMagasin());
        nomLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");
        nomLabel.setWrapText(true);

        if (magasin.getCategorie() != null && !magasin.getCategorie().isEmpty()) {
            Label catLabel = new Label(magasin.getCategorie());
            catLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af; " +
                    "-fx-background-color: #374151; -fx-padding: 2 8; " +
                    "-fx-background-radius: 4;");
            nameBox.getChildren().add(catLabel);
        }

        nameBox.getChildren().add(0, nomLabel);
        header.getChildren().addAll(storeIcon, nameBox);

        // Informations limitées
        VBox infoBox = new VBox(8);
        infoBox.setStyle("-fx-padding: 10 0;");

        if (magasin.getAdresse() != null && !magasin.getAdresse().isEmpty()) {
            HBox addrRow = createInfoRow("📍", magasin.getAdresse());
            infoBox.getChildren().add(addrRow);
        }

        if (magasin.getLocalisation() != null && !magasin.getLocalisation().isEmpty()) {
            HBox locRow = createInfoRow("🗺️", magasin.getLocalisation());
            infoBox.getChildren().add(locRow);
        }

        if (magasin.getNbCommentaires() > 0) {
            HBox articlesRow = new HBox(5);
            articlesRow.setAlignment(Pos.CENTER_LEFT);
            Label articleIcon = new Label("📦");
            articleIcon.setStyle("-fx-font-size: 14px;");
            Label articleText = new Label(magasin.getNbCommentaires() + " articles disponibles");
            articleText.setStyle("-fx-text-fill: #60a5fa; -fx-font-size: 12px;");
            articlesRow.getChildren().addAll(articleIcon, articleText);
            infoBox.getChildren().add(articlesRow);
        }

        // Message de connexion
        VBox loginBox = new VBox(10);
        loginBox.setStyle("-fx-padding: 15 0 0 0; -fx-alignment: center;");

        Label loginMessage = new Label("Connectez-vous pour voir les articles et détails");
        loginMessage.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8; -fx-font-style: italic;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginBtn = new Button("Se Connecter");
        loginBtn.setStyle("-fx-background-color: #2563eb; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 8 16; " +
                "-fx-background-radius: 6; " +
                "-fx-font-weight: 500; " +
                "-fx-cursor: hand;");
        loginBtn.setOnAction(e -> redirectToLogin());

        buttonBox.getChildren().add(loginBtn);
        loginBox.getChildren().addAll(loginMessage, buttonBox);

        // Assembler
        card.getChildren().addAll(header, infoBox, loginBox);

        // Effet hover
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() +
                    "-fx-border-color: #3b82f6; " +
                    "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.2), 10, 0.5, 0, 2);");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #111827; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 20; " +
                    "-fx-border-color: #1f2937; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 8;");
        });

        return card;
    }

    private HBox createInfoRow(String emoji, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 14px; -fx-min-width: 20px;");

        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 13px; -fx-text-fill: #d1d5db;");
        valueText.setWrapText(true);

        row.getChildren().addAll(iconLabel, valueText);
        return row;
    }

    private boolean isVilleMarocaine(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        String lowerQuery = query.toLowerCase().trim();

        String[] villesMarocaines = {
                "rabat", "casablanca", "fès", "fes", "marrakech", "tanger", "agadir",
                "meknès", "meknes", "oujda", "kenitra", "tétouan", "tetouan", "safi",
                "mohammedia", "khouribga", "beni mellal", "el jadida", "taza", "nador",
                "settat", "ksar el kebir", "larache", "khemisset", "guelmim", "berrechid",
                "taourirt", "berkane", "sidi slimane", "errachidia", "sale", "salé",
                "sidi kacem", "khenifra", "tiznit", "tan-tan", "ouarzazate", "sefrou"
        };

        for (String ville : villesMarocaines) {
            if (lowerQuery.equals(ville) || lowerQuery.startsWith(ville + " ")) {
                return true;
            }
        }

        return false;
    }

    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    public void cleanup() {
        if (hideSearchResultsTimer != null) {
            hideSearchResultsTimer.stop();
        }

        // Fermer la fenêtre de recherche si elle est ouverte
        if (searchResultsStage != null && searchResultsStage.isShowing()) {
            searchResultsStage.close();
        }
    }

    // ============================================
    // CLASSE INTERNE POUR LES RÉSULTATS
    // ============================================
    private static class SearchResult {
        private final String icon;
        private final String category;
        private final String title;
        private final String description;
        private final Runnable action;
        private final int id;
        private final String type;

        public SearchResult(String icon, String category, String title,
                            String description, Runnable action, int id, String type) {
            this.icon = icon;
            this.category = category;
            this.title = title;
            this.description = description;
            this.action = action;
            this.id = id;
            this.type = type;
        }

        public String getIcon() { return icon; }
        public String getCategory() { return category; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public Runnable getAction() { return action; }
        public int getId() { return id; }
        public String getType() { return type; }
    }

    private Stage findCurrentStage() {
        if (mainBorderPane != null && mainBorderPane.getScene() != null) {
            return (Stage) mainBorderPane.getScene().getWindow();
        }

        return (Stage) Stage.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }
}