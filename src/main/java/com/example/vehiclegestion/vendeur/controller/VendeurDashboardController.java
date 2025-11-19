package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.VendeurDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.util.HashMap;


public class VendeurDashboardController implements Initializable {

    @FXML private Label totalSalesLabel;
    @FXML private Label revenueLabel;
    @FXML private Label activeClientsLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label totalArticlesLabel;
    @FXML private Label availableArticlesLabel;
    @FXML private Label avgRatingLabel;

    @FXML private LineChart<String, Number> salesChart;
    @FXML private PieChart productsPieChart;

    @FXML private VBox activitiesContainer;
    @FXML private Label systemStatusLabel;
    @FXML private Label systemStatusValue;
    @FXML private Label systemStatusDesc;
    @FXML
    private StackedBarChart<String, Number> categorySalesChart;


    private VendeurDAO vendeurDAO;
    private final int VENDEUR_ID = 1; // 🔹 ID du vendeur connecté
    private final DecimalFormat df = new DecimalFormat("#,##0.00");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚀 Initialisation du Dashboard...");

        try {
            vendeurDAO = new VendeurDAO(VENDEUR_ID);

            // 1. Initialiser les statistiques principales
            initializeStatistics();

            // 2. Initialiser les statistiques supplémentaires
            initializeAdditionalStatistics();

            // 3. Initialiser les graphiques
            initializeCharts();
            loadCategorySalesChart();


            // 4. Initialiser les activités dynamiques
            initializeActivities();

            System.out.println("✅ Dashboard initialisé avec succès!");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation: " + e.getMessage());
            e.printStackTrace();
            showErrorStatistics();
        }
    }

    private void initializeStatistics() {
        try {
            int totalSales = vendeurDAO.getTotalSales();
            double totalRevenue = vendeurDAO.getTotalRevenue();
            int activeClients = vendeurDAO.getActiveClientsCount();
            int pendingOrders = vendeurDAO.getPendingOrdersCount();

            totalSalesLabel.setText(String.valueOf(totalSales));
            revenueLabel.setText(df.format(totalRevenue) + " MAD");
            activeClientsLabel.setText(String.valueOf(activeClients));
            pendingOrdersLabel.setText(String.valueOf(pendingOrders));

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du chargement des statistiques: " + e.getMessage());
            showErrorStatistics();
        }
    }

    private void initializeAdditionalStatistics() {
        try {
            int totalArticles = vendeurDAO.getTotalArticles();
            int availableArticles = vendeurDAO.getAvailableArticles();
            double avgRating = vendeurDAO.getAverageRating();

            totalArticlesLabel.setText(String.valueOf(totalArticles));
            availableArticlesLabel.setText(String.valueOf(availableArticles));
            avgRatingLabel.setText(String.format("%.1f", avgRating));

        } catch (SQLException e) {
            System.err.println("❌ Erreur statistiques supplémentaires: " + e.getMessage());
            totalArticlesLabel.setText("0");
            availableArticlesLabel.setText("0");
            avgRatingLabel.setText("0.0");
        }
    }

    private void initializeCharts() {
        try {
            initializeSalesChart();
            initializeProductsChart();

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du chargement des graphiques: " + e.getMessage());
            initializeSampleCharts(); // Graphiques d'exemple en cas d'erreur
        }
    }

    private void initializeSalesChart() throws SQLException {
        Map<String, Integer> salesData = vendeurDAO.getSalesPerMonth();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes 2024");

        for (Map.Entry<String, Integer> entry : salesData.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        salesChart.getData().clear();
        salesChart.getData().add(series);
        salesChart.setLegendVisible(false);
        salesChart.setTitle("Évolution des ventes mensuelles");
    }

    private void initializeProductsChart() throws SQLException {
        Map<String, Integer> productData = vendeurDAO.getProductDistribution();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : productData.entrySet()) {
            String category = entry.getKey() != null ? entry.getKey() : "Non catégorisé";
            pieData.add(new PieChart.Data(category + " (" + entry.getValue() + ")", entry.getValue()));
        }

        // Si pas de données, afficher un message
        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("Aucune vente", 1));
        }

        productsPieChart.setData(pieData);
        productsPieChart.setTitle("Répartition des produits vendus");
    }

    private void initializeActivities() {
        try {
            refreshActivities();
            initializeSystemStatus();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des activités: " + e.getMessage());
        }
    }

    // Méthode pour rafraîchir les activités
    @FXML
    private void refreshActivities() {
        try {
            activitiesContainer.getChildren().clear();

            List<Map<String, String>> activities = vendeurDAO.getRecentActivities();

            if (activities.isEmpty()) {
                Label emptyLabel = new Label("Aucune activité récente");
                emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px; -fx-padding: 20;");
                activitiesContainer.getChildren().add(emptyLabel);
                return;
            }

            for (Map<String, String> activity : activities) {
                VBox activityCard = createActivityCard(
                        activity.get("type"),
                        activity.get("description"),
                        activity.get("status")
                );
                activitiesContainer.getChildren().add(activityCard);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du rafraîchissement des activités: " + e.getMessage());
            Label errorLabel = new Label("Erreur de chargement des activités");
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-padding: 20;");
            activitiesContainer.getChildren().add(errorLabel);
        }
    }

    // Méthode pour créer une carte d'activité
    private VBox createActivityCard(String type, String description, String status) {
        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");

        // Icône selon le type
        String icon = getIconForType(type);
        String statusText = getStatusText(status);
        String statusColor = getStatusColor(status);
        String statusBgColor = getStatusBackgroundColor(status);

        Label titleLabel = new Label(icon + " " + description);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(300);

        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);

        Label viewLabel = new Label("Voir détails");
        viewLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db; -fx-cursor: hand;");

        // Ajouter un événement de clic
        viewLabel.setOnMouseClicked(e -> {
            System.out.println("Détails de l'activité: " + description);
            // Ici vous pouvez ouvrir une fenêtre de détails
        });

        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-text-fill: " + statusColor + "; -fx-font-size: 12px; " +
                "-fx-font-weight: bold; -fx-padding: 2 8; " +
                "-fx-background-color: " + statusBgColor + "; -fx-background-radius: 10;");

        statusBox.getChildren().addAll(viewLabel, statusLabel);
        card.getChildren().addAll(titleLabel, statusBox);

        // Effet au survol
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-padding: 15; -fx-background-color: #e8f4f8; -fx-background-radius: 8; -fx-border-color: #3498db; -fx-border-width: 1; -fx-border-radius: 8;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        });

        return card;
    }

    // Méthodes utilitaires pour les icônes et statuts
    private String getIconForType(String type) {
        switch (type) {
            case "VENTE": return "💰";
            case "COMMENTAIRE": return "💬";
            case "RDV": return "📅";
            default: return "📌";
        }
    }

    private String getStatusText(String status) {
        return status.equals("COMPLETED") ? "Terminé" : "En cours";
    }

    private String getStatusColor(String status) {
        return status.equals("COMPLETED") ? "#27ae60" : "#e67e22";
    }

    private String getStatusBackgroundColor(String status) {
        return status.equals("COMPLETED") ? "#eafaf1" : "#fef5e7";
    }

    // Méthode pour le statut système
    private void initializeSystemStatus() {
        try {
            Map<String, String> systemStatus = vendeurDAO.getSystemStatus();

            systemStatusValue.setText(systemStatus.get("statut"));
            systemStatusValue.setStyle("-fx-text-fill: " + systemStatus.get("couleur") +
                    "; -fx-font-weight: bold; -fx-font-size: 16px;");
            systemStatusDesc.setText(systemStatus.get("description"));

            systemStatusLabel.setText("Statut système • " +
                    systemStatus.get("ventes_en_cours") + " ventes en cours • " +
                    systemStatus.get("rdv_confirmes") + " RDV • Note: " +
                    systemStatus.get("note_moyenne") + "/5");

        } catch (SQLException e) {
            System.err.println("❌ Erreur statut système: " + e.getMessage());
            systemStatusValue.setText("ERREUR");
            systemStatusValue.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 16px;");
            systemStatusDesc.setText("Impossible de charger le statut système");
        }
    }

    private void initializeSampleCharts() {
        // Graphiques d'exemple en cas d'erreur BD
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes 2024");

        series.getData().add(new XYChart.Data<>("Jan", 45));
        series.getData().add(new XYChart.Data<>("Fév", 68));
        series.getData().add(new XYChart.Data<>("Mar", 72));
        series.getData().add(new XYChart.Data<>("Avr", 85));

        salesChart.getData().add(series);
        salesChart.setLegendVisible(false);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Berlines", 45),
                new PieChart.Data("SUV", 30),
                new PieChart.Data("Compactes", 15)
        );
        productsPieChart.setData(pieData);
    }

    private void showErrorStatistics() {
        totalSalesLabel.setText("Erreur");
        revenueLabel.setText("Erreur");
        activeClientsLabel.setText("Erreur");
        pendingOrdersLabel.setText("Erreur");
        totalArticlesLabel.setText("0");
        availableArticlesLabel.setText("0");
        avgRatingLabel.setText("0.0");
    }

    @FXML
    private void refreshData() {
        System.out.println("🔄 Actualisation des données...");
        try {
            initializeStatistics();
            initializeAdditionalStatistics();
            initializeCharts();
            refreshActivities();
            initializeSystemStatus();
            System.out.println("✅ Données actualisées avec succès!");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'actualisation: " + e.getMessage());
        }
    }

    // Méthode utilitaire pour formater les dates
    private String formatDate(String dateString) {
        try {
            // Simple formattage de date - vous pouvez utiliser SimpleDateFormat pour plus de sophistication
            return dateString.substring(0, 16); // Retourne "YYYY-MM-DD HH:MM"
        } catch (Exception e) {
            return dateString;
        }
    }
    public void loadCategorySalesChart() {
        try {
            // Récupérer les catégories existantes
            Map<String, Integer> productCounts = vendeurDAO.getProductDistribution(); // total par catégorie
            Map<String, Map<String, Double>> monthlyCategorySales = vendeurDAO.getMonthlyCategoryRevenuePercentage();

            categorySalesChart.getData().clear();

            for (String category : productCounts.keySet()) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(category);

                Map<String, Double> monthData = monthlyCategorySales.getOrDefault(category, new HashMap<>());

                // Ajouter chaque mois
                String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
                for (String month : months) {
                    series.getData().add(new XYChart.Data<>(month, monthData.getOrDefault(month, 0.0)));
                }

                categorySalesChart.getData().add(series);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}