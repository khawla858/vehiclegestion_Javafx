package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ClientDashboardController {

    @FXML private Label userNameLabel;
    @FXML private TextField searchField;
    @FXML private Button dashboardBtn;
    @FXML private Button vehiclesBtn;
    @FXML private Button favoritesBtn;
    @FXML private Button historyBtn;
    @FXML private Button profileBtn;
    @FXML private Button publishBtn;
    @FXML private Button logoutBtn;
    @FXML private StackPane contentArea;

    private SessionManager sessionManager = SessionManager.getInstance();

    // Données dynamiques
    private int vehiclesViewed = 0;
    private int favoriteCount = 5;
    private int activeReservations = 0;
    private int unreadMessages = 3;
    private int notifications = 2;

    @FXML
    public void initialize() {
        System.out.println("✅ Dashboard moderne initialisé");

        if (!sessionManager.estConnecte()) {
            showError("Session invalide - Veuillez vous reconnecter");
            redirectToLogin();
            return;
        }

        if (contentArea == null) {
            System.err.println("❌ ERREUR: contentArea est null!");
            return;
        }

        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        String clientName = currentUser.getPrenom() + " " + currentUser.getNom();
        userNameLabel.setText(clientName);

        System.out.println("👤 Dashboard chargé pour: " + clientName);

        // Charger les données dynamiques
        loadDynamicData();

        // Initialiser la recherche
        initializeSearch();

        // Définir le menu actif par défaut
        if (dashboardBtn != null) {
            setActiveMenu(dashboardBtn);
            showDashboardWithCharts();
        }

        // Ajouter les effets hover aux boutons de navigation
        setupNavigationEffects();
    }

    // ========== GESTION DE LA RECHERCHE ==========

    private void initializeSearch() {
        if (searchField != null) {
            searchField.setPromptText("Rechercher par marque, modèle, année...");

            // Recherche en temps réel
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && newValue.length() > 2) {
                    performSearch(newValue);
                }
            });

            // Effet focus
            searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (isNowFocused) {
                    searchField.getParent().setStyle(
                            searchField.getParent().getStyle() +
                                    "-fx-border-color: #FF6B35; -fx-border-width: 1;"
                    );
                } else {
                    searchField.getParent().setStyle(
                            searchField.getParent().getStyle().replace("-fx-border-color: #FF6B35;", "")
                    );
                }
            });
        }
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) return;

        System.out.println("🔍 Recherche: " + query);

        // TODO: Implémenter la logique de recherche réelle
        // Simuler une recherche
        Timeline searchAnimation = new Timeline(
                new KeyFrame(Duration.millis(300), e -> {
                    showInfo("Recherche", "Recherche de véhicules correspondant à: \"" + query + "\"");
                })
        );
        searchAnimation.play();
    }

    // ========== EFFETS NAVIGATION ==========

    private void setupNavigationEffects() {
        List<Button> navButtons = Arrays.asList(
                dashboardBtn, vehiclesBtn, favoritesBtn, historyBtn, profileBtn
        );

        for (Button btn : navButtons) {
            if (btn != null) {
                // Effet hover
                btn.setOnMouseEntered(e -> {
                    if (!btn.getStyleClass().contains("active")) {
                        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
                        st.setToX(1.05);
                        st.setToY(1.05);
                        st.play();
                    }
                });

                btn.setOnMouseExited(e -> {
                    if (!btn.getStyleClass().contains("active")) {
                        ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
                        st.setToX(1.0);
                        st.setToY(1.0);
                        st.play();
                    }
                });
            }
        }
    }

    // ========== CHARGEMENT DES DONNÉES ==========

    private void loadDynamicData() {
        // TODO: Remplacer par de vraies requêtes à votre base de données
        vehiclesViewed = (int) (Math.random() * 50) + 10;
        favoriteCount = 5;
        activeReservations = (int) (Math.random() * 10) + 1;
        unreadMessages = 3;
        notifications = 2;

        System.out.println("📊 Données chargées: " + vehiclesViewed + " véhicules consultés");
    }

    // ========== NAVIGATION ==========

    @FXML
    private void showDashboard() {
        System.out.println("📊 Navigation vers le tableau de bord");
        setActiveMenu(dashboardBtn);
        showDashboardWithCharts();
    }

    @FXML
    private void showVehicles() {
        System.out.println("🚗 Navigation vers les véhicules");
        setActiveMenu(vehiclesBtn);
        loadPage("/com/example/vehiclegestion/view/client/vehicles-view.fxml");
    }

    @FXML
    private void showFavorites() {
        System.out.println("❤️ Navigation vers les favoris");
        if (!sessionManager.estConnecte()) {
            showError("Veuillez vous connecter pour accéder aux favoris");
            return;
        }
        setActiveMenu(favoritesBtn);
        loadPage("/com/example/vehiclegestion/view/client/ClientFavoritesView.fxml");
    }

    @FXML
    private void showHistory() {
        System.out.println("📊 Navigation vers l'historique");
        setActiveMenu(historyBtn);
        loadPage("/com/example/vehiclegestion/view/client/ClientFavoritesView.fxml");
    }

    @FXML
    private void showProfile() {
        System.out.println("👤 Navigation vers le profil");
        setActiveMenu(profileBtn);
        if (!sessionManager.estConnecte()) {
            showError("Veuillez vous connecter pour accéder au profil");
            return;
        }
        loadPage("/com/example/vehiclegestion/view/client/profile-view.fxml");
    }

    @FXML
    private void publishVehicle() {
        System.out.println("➕ Publication d'un véhicule");
        if (!sessionManager.estConnecte()) {
            showError("Veuillez vous connecter pour publier un véhicule");
            return;
        }

        // Animation du bouton
        Button btn = publishBtn;
        ScaleTransition st = new ScaleTransition(Duration.millis(100), btn);
        st.setToX(0.95);
        st.setToY(0.95);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.play();

        showInfo("Publication", "Formulaire de publication bientôt disponible!");
    }

    @FXML
    private void logout() {
        System.out.println("🚪 Déconnexion");
        Utilisateur user = sessionManager.getUtilisateurConnecte();

        // Animation de déconnexion
        FadeTransition fade = new FadeTransition(Duration.millis(300), contentArea);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            sessionManager.fermerSession();
            showInfo("Déconnexion", "Au revoir " + user.getPrenom() + " !");
            redirectToLogin();
        });
        fade.play();
    }

    // ========== DASHBOARD AVEC GRAPHIQUES ==========

    private void showDashboardWithCharts() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f5f5f5; -fx-background-color: #f5f5f5;");

        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(30, 40, 30, 40));
        mainContent.setStyle("-fx-background-color: #f5f5f5;");

        // En-tête avec bannière
        VBox header = createModernHeader();

        // Cartes de statistiques
        HBox statsBox = createStatsCards();

        // Graphiques
        VBox chartsSection = createChartsSection();

        // Activités récentes
        VBox activitiesBox = createActivitiesSection();

        mainContent.getChildren().addAll(header, statsBox, chartsSection, activitiesBox);
        scrollPane.setContent(mainContent);

        contentArea.getChildren().setAll(scrollPane);

        // Animation d'entrée
        animateContentEntrance(mainContent);
    }

    private VBox createModernHeader() {
        VBox header = new VBox(20);

        // Bannière d'accueil
        HBox banner = new HBox(20);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setStyle("-fx-background-color: linear-gradient(to right, #3498db, #5dade2); " +
                "-fx-background-radius: 15; -fx-padding: 30 40;");

        VBox textBox = new VBox(8);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH));

        Label welcomeLabel = new Label("Bienvenue " + currentUser.getPrenom() + " ! 👋");
        welcomeLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label descLabel = new Label("Gérez vos véhicules, suivez vos favoris et consultez l'historique en un clic");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: rgba(255,255,255,0.9);");

        Label dateLabel = new Label(today);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.8); -fx-font-style: italic;");

        textBox.getChildren().addAll(welcomeLabel, descLabel, dateLabel);

        Button exploreBtn = new Button("Explorer →");
        exploreBtn.setStyle("-fx-background-color: white; -fx-text-fill: #3498db; " +
                "-fx-font-weight: bold; -fx-padding: 12 30; -fx-background-radius: 25; " +
                "-fx-cursor: hand; -fx-font-size: 14px;");
        exploreBtn.setOnAction(e -> showVehicles());

        banner.getChildren().addAll(textBox, exploreBtn);
        header.getChildren().add(banner);

        return header;
    }

    private HBox createStatsCards() {
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER);

        statsBox.getChildren().addAll(
                createStatCard("📈", String.valueOf(vehiclesViewed), "Véhicules actifs", "#3498db"),
                createStatCard("❤️", String.valueOf(favoriteCount), "Favoris enregistrés", "#e74c3c"),
                createStatCard("📋", "28", "Transactions totales", "#95a5a6"),
                createStatCard("⭐", "4.8", "Note moyenne", "#f39c12")
        );

        return statsBox;
    }

    private VBox createStatCard(String emoji, String value, String title, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-padding: 25; -fx-border-color: #e8e8e8; -fx-border-radius: 12; " +
                "-fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        card.setPrefSize(200, 140);
        HBox.setHgrow(card, Priority.ALWAYS);

        // Effet hover
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.05);
            st.setToY(1.05);
            card.setStyle(card.getStyle() + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);");
            st.play();
        });

        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            card.setStyle(card.getStyle().replace("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 12, 0, 0, 4);",
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);"));
            st.play();
        });

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 36px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666; -fx-wrap-text: true; -fx-text-alignment: center;");
        titleLabel.setMaxWidth(170);

        card.getChildren().addAll(emojiLabel, valueLabel, titleLabel);
        return card;
    }

    private VBox createChartsSection() {
        VBox section = new VBox(15);

        // Graphique d'activité
        LineChart<String, Number> activityChart = createActivityChart();
        VBox chartBox = wrapInCard(activityChart, "📈 Activité des 7 derniers jours");

        section.getChildren().add(chartBox);
        return section;
    }

    private LineChart<String, Number> createActivityChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Jour");
        yAxis.setLabel("Consultations");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("");
        chart.setLegendVisible(false);
        chart.setPrefHeight(300);
        chart.setAnimated(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String day = date.format(DateTimeFormatter.ofPattern("EEE", Locale.FRENCH));
            int views = (int) (Math.random() * 15) + 5;
            series.getData().add(new XYChart.Data<>(day, views));
        }

        chart.getData().add(series);
        return chart;
    }

    private VBox createActivitiesSection() {
        VBox box = new VBox(15);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-padding: 25; -fx-border-color: #e8e8e8; -fx-border-radius: 12; " +
                "-fx-border-width: 1;");

        Label titleLabel = new Label("📊 Activités Récentes");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox activityList = new VBox(10);

        List<String[]> activities = Arrays.asList(
                new String[]{"🚗", "Peugeot 208 GTI consultée", "Il y a 2 heures"},
                new String[]{"❤️", "Toyota Yaris ajoutée aux favoris", "Hier"},
                new String[]{"📅", "Renault Clio réservée", "Il y a 3 jours"},
                new String[]{"✉️", "Nouveau message reçu", "Il y a 1 heure"}
        );

        for (String[] activity : activities) {
            HBox row = createActivityRow(activity[0], activity[1], activity[2]);
            activityList.getChildren().add(row);
        }

        box.getChildren().addAll(titleLabel, activityList);
        return box;
    }

    private HBox createActivityRow(String icon, String text, String time) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        row.setOnMouseEntered(e -> {
            row.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 8; -fx-cursor: hand;");
        });
        row.setOnMouseExited(e -> {
            row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        });

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px; -fx-min-width: 30;");

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        HBox.setHgrow(textLabel, Priority.ALWAYS);

        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");

        row.getChildren().addAll(iconLabel, textLabel, timeLabel);
        return row;
    }

    private VBox wrapInCard(javafx.scene.Node content, String title) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-padding: 20; -fx-border-color: #e8e8e8; -fx-border-radius: 12; " +
                "-fx-border-width: 1;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        card.getChildren().addAll(titleLabel, content);
        return card;
    }

    private void animateContentEntrance(VBox content) {
        content.setOpacity(0);
        content.setTranslateY(30);

        FadeTransition fade = new FadeTransition(Duration.millis(500), content);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.millis(500), content);
        translate.setToY(0);

        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.play();
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private void setActiveMenu(Button activeButton) {
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #2c3e50; " +
                "-fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 18 20; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: transparent;";

        String activeStyle = "-fx-background-color: transparent; -fx-text-fill: #3498db; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 18 20; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: #3498db;";

        List<Button> buttons = Arrays.asList(dashboardBtn, vehiclesBtn, favoritesBtn, historyBtn, profileBtn);

        for (Button btn : buttons) {
            if (btn != null) {
                btn.setStyle(inactiveStyle);
                btn.getStyleClass().remove("active");
            }
        }

        if (activeButton != null) {
            activeButton.setStyle(activeStyle);
            activeButton.getStyleClass().add("active");
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            // Animation de transition
            page.setOpacity(0);
            contentArea.getChildren().setAll(page);

            FadeTransition fade = new FadeTransition(Duration.millis(300), page);
            fade.setToValue(1);
            fade.play();

            System.out.println("✅ Page chargée: " + fxmlPath);
        } catch (IOException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            showError("Erreur de chargement: " + e.getMessage());
        }
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/vehiclegestion/view/auth/login.fxml"));
            Parent loginPage = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(loginPage));
            stage.setTitle("Connexion - AutoSales Pro");
            stage.setMaximized(false);
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Erreur lors de la déconnexion: " + e.getMessage());
        }
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}