package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.VendeurDAO;
import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

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
    @FXML private StackedBarChart<String, Number> categorySalesChart;

    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private VendeurDAO vendeurDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Utilisateur utilisateurConnecte = SessionManager.getInstance().getUtilisateurConnecte();
        if (utilisateurConnecte == null || !SessionManager.getInstance().estVendeur()) {
            System.err.println("❌ Aucun vendeur connecté ou accès refusé !");
            return;
        }

        int vendeurId = utilisateurConnecte.getIdUtilisateur();
        vendeurDAO = new VendeurDAO(vendeurId);

        initializeStatistics();
        initializeAdditionalStatistics();
        initializeCharts();
        loadCategorySalesChart();
        initializeActivities();
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
            initializeSampleCharts();
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
    }

    private void initializeProductsChart() throws SQLException {
        Map<String, Integer> productData = vendeurDAO.getProductDistribution();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : productData.entrySet()) {
            String category = entry.getKey() != null ? entry.getKey() : "Non catégorisé";
            pieData.add(new PieChart.Data(category + " (" + entry.getValue() + ")", entry.getValue()));
        }

        if (pieData.isEmpty()) {
            pieData.add(new PieChart.Data("Aucune vente", 1));
        }

        productsPieChart.setData(pieData);
    }

    private void initializeActivities() {
        try {
            refreshActivities();
            initializeSystemStatus();
        } catch (Exception ignored) {}
    }

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
                VBox card = createActivityCard(
                        activity.get("type"),
                        activity.get("description"),
                        activity.get("status")
                );
                activitiesContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            System.err.println("Erreur chargement activités : " + e.getMessage());
        }
    }

    private VBox createActivityCard(String type, String description, String status) {
        VBox card = new VBox(5);
        card.setStyle("-fx-padding: 15; -fx-background-color: #f8f9fa; -fx-background-radius: 8;");
        Label titleLabel = new Label(getIconForType(type) + " " + description);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox statusBox = new HBox(10);
        statusBox.setStyle("-fx-font-size: 12px;");
        Label statusLabel = new Label(getStatusText(status));
        statusLabel.setStyle("-fx-text-fill: " + getStatusColor(status) + "; -fx-font-weight: bold;");
        statusBox.getChildren().add(statusLabel);

        card.getChildren().addAll(titleLabel, statusBox);
        return card;
    }

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

    private void initializeSystemStatus() {
        try {
            Map<String, String> systemStatus = vendeurDAO.getSystemStatus();
            systemStatusValue.setText(systemStatus.get("statut"));
            systemStatusDesc.setText(systemStatus.get("description"));
        } catch (SQLException e) {
            systemStatusValue.setText("ERREUR");
            systemStatusDesc.setText("Impossible de charger le statut système");
        }
    }

    private void initializeSampleCharts() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Jan", 45));
        series.getData().add(new XYChart.Data<>("Fév", 68));
        salesChart.getData().add(series);

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Berlines", 45),
                new PieChart.Data("SUV", 30)
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
        initializeStatistics();
        initializeAdditionalStatistics();
        initializeCharts();
        refreshActivities();
        initializeSystemStatus();
    }

    public void loadCategorySalesChart() {
        try {
            Map<String, Map<String, Double>> monthlyCategorySales = vendeurDAO.getMonthlyCategoryRevenuePercentage();
            categorySalesChart.getData().clear();

            for (String category : monthlyCategorySales.keySet()) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(category);

                Map<String, Double> monthData = monthlyCategorySales.get(category);
                String[] months = {"Jan","Fév","Mar","Avr","Mai","Jun","Jul","Aoû","Sep","Oct","Nov","Déc"};

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
