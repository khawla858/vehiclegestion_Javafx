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
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
    private int favoriteCount = 0;
    private int activeReservations = 0;
    private int unreadMessages = 0;
    private int specialOffers = 0;

    // Cartes de statistiques pour animation
    private VBox viewedCard, favoritesCard, reservationsCard, messagesCard;

    // Graphiques
    private LineChart<String, Number> activityChart;
    private PieChart vehicleTypeChart;
    private BarChart<String, Number> monthlyStatsChart;

    @FXML
    public void initialize() {
        System.out.println("✅ Dashboard dynamique initialisé");

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

        if (vehiclesBtn != null) {
            vehiclesBtn.setText("🚗 Véhicules (" + vehiclesViewed + ")");
        }

        if (dashboardBtn != null) {
            setActiveMenu(dashboardBtn);
            showDashboardWithCharts();
        }

        initializeTopBar();
        startRealTimeUpdates();
    }

    // Charger les données depuis la base de données (simulé)
    private void loadDynamicData() {
        // TODO: Remplacer par de vraies requêtes à votre base de données
        vehiclesViewed = (int) (Math.random() * 50) + 10;
        favoriteCount = (int) (Math.random() * 20) + 5;
        activeReservations = (int) (Math.random() * 10) + 1;
        unreadMessages = (int) (Math.random() * 15) + 2;
        specialOffers = (int) (Math.random() * 8) + 1;

        System.out.println("📊 Données chargées: " + vehiclesViewed + " véhicules consultés");
    }

    // Mise à jour en temps réel
    private void startRealTimeUpdates() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            // Simuler des changements de données
            int oldMessages = unreadMessages;
            unreadMessages += (Math.random() > 0.7) ? 1 : 0;

            if (unreadMessages != oldMessages && messagesCard != null) {
                animateCardUpdate(messagesCard, String.valueOf(unreadMessages));
            }

            // Mettre à jour les graphiques
            if (activityChart != null) {
                updateActivityChart();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void initializeTopBar() {
        if (searchField != null) {
            searchField.setPromptText("🔍 Rechercher un véhicule...");
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                performSearch(newValue);
            });
        }
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) return;

        System.out.println("🔍 Recherche: " + query);
        // TODO: Implémenter la logique de recherche avec votre base de données
        showInfo("Recherche", "Recherche de: " + query);
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
        loadPage("/com/example/vehiclegestion/view/client/history-view.fxml");
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
        showInfo("Publication", "Fonctionnalité de publication bientôt disponible!");
    }

    @FXML
    private void logout() {
        System.out.println("🚪 Déconnexion");
        Utilisateur user = sessionManager.getUtilisateurConnecte();
        sessionManager.fermerSession();
        showInfo("Déconnexion", "Au revoir " + user.getPrenom() + " !");
        redirectToLogin();
    }

    // ========== DASHBOARD AVEC GRAPHIQUES ==========

    private void showDashboardWithCharts() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #f5f7fa; -fx-background-color: #f5f7fa;");

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle("-fx-background-color: #f5f7fa;");

        // En-tête
        VBox header = createHeader();

        // Cartes de statistiques animées
        HBox statsBox = createAnimatedStatsSection();

        // Section graphiques
        GridPane chartsGrid = createChartsSection();

        // Activités récentes avec style moderne
        VBox activitiesBox = createModernActivitiesSection();

        // Alertes et notifications
        HBox alertsBox = createAlertsSection();

        mainContent.getChildren().addAll(header, statsBox, chartsGrid, activitiesBox, alertsBox);
        scrollPane.setContent(mainContent);

        contentArea.getChildren().setAll(scrollPane);

        // Animer l'apparition
        animateContentEntrance(mainContent);
    }

    private VBox createHeader() {
        VBox header = new VBox(10);

        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH));

        Label titleLabel = new Label("🚗 Tableau de Bord AutoSales Pro");
        titleLabel.setStyle("-fx-font-size: 32; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label welcomeLabel = new Label("Bonjour " + currentUser.getPrenom() + " ! 👋");
        welcomeLabel.setStyle("-fx-font-size: 22; -fx-text-fill: #FF6B35; -fx-font-weight: bold;");

        Label dateLabel = new Label(today);
        dateLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        header.getChildren().addAll(titleLabel, welcomeLabel, dateLabel);
        return header;
    }

    private HBox createAnimatedStatsSection() {
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);

        viewedCard = createAnimatedStatCard("🚗", "Véhicules consultés", String.valueOf(vehiclesViewed), "#3498db");
        favoritesCard = createAnimatedStatCard("❤️", "Favoris", String.valueOf(favoriteCount), "#e74c3c");
        reservationsCard = createAnimatedStatCard("📅", "Réservations actives", String.valueOf(activeReservations), "#2ecc71");
        messagesCard = createAnimatedStatCard("✉️", "Messages non lus", String.valueOf(unreadMessages), "#f39c12");

        statsBox.getChildren().addAll(viewedCard, favoritesCard, reservationsCard, messagesCard);
        return statsBox;
    }

    private VBox createAnimatedStatCard(String emoji, String title, String value, String color) {
        VBox card = new VBox(15);
        card.setStyle("-fx-padding: 25; -fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-alignment: CENTER; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        card.setPrefSize(180, 140);

        // Effet hover
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.05);
            st.setToY(1.05);
            st.play();
        });

        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        Label emojiLabel = new Label(emoji);
        emojiLabel.setStyle("-fx-font-size: 32;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 36; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #666; -fx-wrap-text: true; -fx-text-alignment: center;");
        titleLabel.setMaxWidth(150);

        card.getChildren().addAll(emojiLabel, valueLabel, titleLabel);
        return card;
    }

    private void animateCardUpdate(VBox card, String newValue) {
        Label valueLabel = (Label) card.getChildren().get(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), valueLabel);
        fadeOut.setToValue(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), valueLabel);
        fadeIn.setToValue(1);

        fadeOut.setOnFinished(e -> {
            valueLabel.setText(newValue);
            fadeIn.play();
        });

        fadeOut.play();

        // Animation de pulsation
        ScaleTransition pulse = new ScaleTransition(Duration.millis(300), card);
        pulse.setToX(1.1);
        pulse.setToY(1.1);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
    }

    private GridPane createChartsSection() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setPadding(new Insets(10, 0, 10, 0));

        // Graphique d'activité (ligne)
        activityChart = createActivityLineChart();
        VBox activityBox = wrapChartInCard(activityChart, "📈 Activité des 7 derniers jours");

        // Graphique types de véhicules (camembert)
        vehicleTypeChart = createVehicleTypePieChart();
        VBox pieBox = wrapChartInCard(vehicleTypeChart, "🚗 Types de véhicules consultés");

        // Graphique statistiques mensuelles (barres)
        monthlyStatsChart = createMonthlyStatsBarChart();
        VBox barBox = wrapChartInCard(monthlyStatsChart, "📊 Statistiques mensuelles");

        grid.add(activityBox, 0, 0, 2, 1);
        grid.add(pieBox, 0, 1);
        grid.add(barBox, 1, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);

        return grid;
    }

    private LineChart<String, Number> createActivityLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Jour");
        yAxis.setLabel("Véhicules consultés");

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Votre activité cette semaine");
        chart.setLegendVisible(false);
        chart.setPrefHeight(300);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String day = date.format(DateTimeFormatter.ofPattern("EEE", Locale.FRENCH));
            int views = (int) (Math.random() * 15) + 5;
            series.getData().add(new XYChart.Data<>(day, views));
        }

        chart.getData().add(series);

        // Animation
        chart.setAnimated(true);

        return chart;
    }

    private void updateActivityChart() {
        if (activityChart == null) return;

        XYChart.Series<String, Number> series = activityChart.getData().get(0);

        // Décaler les données et ajouter une nouvelle valeur
        series.getData().remove(0);

        LocalDate today = LocalDate.now();
        String day = today.format(DateTimeFormatter.ofPattern("EEE", Locale.FRENCH));
        int views = (int) (Math.random() * 15) + 5;
        series.getData().add(new XYChart.Data<>(day, views));
    }

    private PieChart createVehicleTypePieChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Répartition par type");
        chart.setPrefHeight(300);

        PieChart.Data berline = new PieChart.Data("Berline", 35);
        PieChart.Data suv = new PieChart.Data("SUV", 28);
        PieChart.Data citadine = new PieChart.Data("Citadine", 22);
        PieChart.Data sportive = new PieChart.Data("Sportive", 10);
        PieChart.Data utilitaire = new PieChart.Data("Utilitaire", 5);

        chart.getData().addAll(berline, suv, citadine, sportive, utilitaire);

        // Effet hover
        chart.getData().forEach(data -> {
            data.getNode().setOnMouseEntered(e -> {
                data.getNode().setStyle("-fx-scale-x: 1.1; -fx-scale-y: 1.1;");
            });
            data.getNode().setOnMouseExited(e -> {
                data.getNode().setStyle("-fx-scale-x: 1.0; -fx-scale-y: 1.0;");
            });
        });

        return chart;
    }

    private BarChart<String, Number> createMonthlyStatsBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Mois");
        yAxis.setLabel("Nombre");

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Évolution mensuelle");
        chart.setPrefHeight(300);

        XYChart.Series<String, Number> viewsSeries = new XYChart.Series<>();
        viewsSeries.setName("Consultations");

        XYChart.Series<String, Number> favoritesSeries = new XYChart.Series<>();
        favoritesSeries.setName("Favoris");

        String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"};
        for (String month : months) {
            viewsSeries.getData().add(new XYChart.Data<>(month, (int)(Math.random() * 50) + 20));
            favoritesSeries.getData().add(new XYChart.Data<>(month, (int)(Math.random() * 20) + 5));
        }

        chart.getData().addAll(viewsSeries, favoritesSeries);
        chart.setAnimated(true);

        return chart;
    }

    private VBox wrapChartInCard(javafx.scene.chart.Chart chart, String title) {
        VBox card = new VBox(15);
        card.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        card.getChildren().addAll(titleLabel, chart);
        return card;
    }

    private VBox createModernActivitiesSection() {
        VBox box = new VBox(15);
        box.setStyle("-fx-padding: 25; -fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");

        Label titleLabel = new Label("📊 Activités Récentes");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        List<ActivityItem> activities = Arrays.asList(
                new ActivityItem("🚗", "Peugeot 208 GTI", "Consulté", LocalDate.now().minusDays(1), "#3498db"),
                new ActivityItem("❤️", "Toyota Yaris", "Ajouté aux favoris", LocalDate.now().minusDays(2), "#e74c3c"),
                new ActivityItem("📅", "Renault Clio", "Réservation confirmée", LocalDate.now().minusDays(3), "#2ecc71"),
                new ActivityItem("✉️", "Vendeur BMW", "Nouveau message", LocalDate.now().minusDays(1), "#f39c12"),
                new ActivityItem("🎉", "Offre spéciale", "Promotion disponible", LocalDate.now(), "#9b59b6")
        );

        VBox activityList = new VBox(10);
        for (ActivityItem activity : activities) {
            HBox activityRow = createActivityRow(activity);
            activityList.getChildren().add(activityRow);
        }

        box.getChildren().addAll(titleLabel, activityList);
        return box;
    }

    private HBox createActivityRow(ActivityItem activity) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        // Effet hover
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #e9ecef; -fx-background-radius: 10; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;"));

        Label iconLabel = new Label(activity.icon);
        iconLabel.setStyle("-fx-font-size: 24; -fx-min-width: 40; -fx-alignment: center;");

        VBox textBox = new VBox(5);
        Label titleLabel = new Label(activity.title);
        titleLabel.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: " + activity.color + ";");

        Label actionLabel = new Label(activity.action);
        actionLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #666;");

        textBox.getChildren().addAll(titleLabel, actionLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label dateLabel = new Label(activity.date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH)));
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #999;");

        row.getChildren().addAll(iconLabel, textBox, dateLabel);
        return row;
    }

    private HBox createAlertsSection() {
        HBox alertsBox = new HBox(15);
        alertsBox.setAlignment(Pos.CENTER);

        if (specialOffers > 0) {
            VBox offerAlert = createAlert("🎁", "Offres spéciales",
                    specialOffers + " nouvelles offres disponibles", "#9b59b6");
            alertsBox.getChildren().add(offerAlert);
        }

        if (activeReservations > 0) {
            VBox reservationAlert = createAlert("⏰", "Réservations",
                    activeReservations + " réservations en attente", "#f39c12");
            alertsBox.getChildren().add(reservationAlert);
        }

        return alertsBox;
    }

    private VBox createAlert(String icon, String title, String message, String color) {
        VBox alert = new VBox(10);
        alert.setStyle("-fx-padding: 20; -fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: " + color + "; -fx-border-width: 2; -fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        alert.setPrefWidth(300);
        alert.setAlignment(Pos.CENTER_LEFT);

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        header.getChildren().addAll(iconLabel, titleLabel);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #666;");
        messageLabel.setWrapText(true);

        alert.getChildren().addAll(header, messageLabel);

        // Animation de pulsation
        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(alert.scaleXProperty(), 1)),
                new KeyFrame(Duration.millis(1000), new KeyValue(alert.scaleXProperty(), 1.02)),
                new KeyFrame(Duration.millis(2000), new KeyValue(alert.scaleXProperty(), 1))
        );
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.play();

        return alert;
    }

    private void animateContentEntrance(VBox content) {
        content.setOpacity(0);
        content.setTranslateY(20);

        FadeTransition fade = new FadeTransition(Duration.millis(600), content);
        fade.setToValue(1);

        TranslateTransition translate = new TranslateTransition(Duration.millis(600), content);
        translate.setToY(0);

        ParallelTransition parallel = new ParallelTransition(fade, translate);
        parallel.play();
    }

    // Classe interne pour les activités
    private static class ActivityItem {
        String icon;
        String title;
        String action;
        LocalDate date;
        String color;

        ActivityItem(String icon, String title, String action, LocalDate date, String color) {
            this.icon = icon;
            this.title = title;
            this.action = action;
            this.date = date;
            this.color = color;
        }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private void setActiveMenu(Button activeButton) {
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #333; -fx-padding: 12 0; " +
                "-fx-cursor: hand; -fx-font-size: 13; -fx-border-width: 0;";
        String activeStyle = "-fx-background-color: transparent; -fx-text-fill: #FF6B35; -fx-font-weight: bold; " +
                "-fx-padding: 12 0; -fx-border-color: #FF6B35; -fx-border-width: 0 0 3 0; " +
                "-fx-cursor: hand; -fx-font-size: 13;";

        dashboardBtn.setStyle(inactiveStyle);
        vehiclesBtn.setStyle(inactiveStyle);
        favoritesBtn.setStyle(inactiveStyle);
        historyBtn.setStyle(inactiveStyle);
        profileBtn.setStyle(inactiveStyle);

        if (activeButton != null) {
            activeButton.setStyle(activeStyle);
        }
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();
            contentArea.getChildren().setAll(page);
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
            stage.setTitle("Connexion - Gestion Véhicules");
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