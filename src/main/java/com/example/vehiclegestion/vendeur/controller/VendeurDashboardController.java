package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.auth.utils.SessionManager;
import com.example.vehiclegestion.vendeur.dao.VendeurDAO;
import com.example.vehiclegestion.auth.model.Utilisateur;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

public class VendeurDashboardController implements Initializable {

    @FXML private Label totalSalesLabel;
    @FXML private Label revenueLabel;
    @FXML private Label activeClientsLabel;
    @FXML private Label totalArticlesLabel;
    @FXML private Label availableArticlesLabel;

    @FXML private LineChart<String, Number> salesChart;
    @FXML private PieChart productsPieChart;
    @FXML private VBox activitiesContainer;
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

            totalSalesLabel.setText(String.valueOf(totalSales));
            revenueLabel.setText(df.format(totalRevenue) + " MAD");
            activeClientsLabel.setText(String.valueOf(activeClients));

        } catch (SQLException e) {
            showErrorStatistics();
        }
    }

    private void initializeAdditionalStatistics() {
        try {
            int totalArticles = vendeurDAO.getTotalArticles();
            int availableArticles = vendeurDAO.getAvailableArticles();

            totalArticlesLabel.setText(String.valueOf(totalArticles));
            availableArticlesLabel.setText(String.valueOf(availableArticles));

        } catch (SQLException e) {
            totalArticlesLabel.setText("0");
            availableArticlesLabel.setText("0");
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
                VBox emptyState = createEmptyStateCard();
                activitiesContainer.getChildren().add(emptyState);
                return;
            }

            for (Map<String, String> activity : activities) {
                VBox card = createEnhancedActivityCard(
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

    private VBox createEnhancedActivityCard(String type, String description, String status) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-padding: 18; " +
                        "-fx-background-color: rgba(15, 23, 42, 0.6); " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: " + getBorderColorForType(type) + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 8, 0.4, 0, 2);"
        );

        // En-tête avec icône colorée et type
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icône avec fond circulaire coloré
        VBox iconContainer = new VBox();
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setStyle(
                "-fx-background-color: " + getIconBackgroundColor(type) + "; " +
                        "-fx-background-radius: 20; " +
                        "-fx-min-width: 40; " +
                        "-fx-min-height: 40; " +
                        "-fx-max-width: 40; " +
                        "-fx-max-height: 40;"
        );

        FontIcon icon = new FontIcon(getIconLiteralForType(type));
        icon.setIconSize(20);
        icon.setIconColor(javafx.scene.paint.Color.web(getIconColor(type)));
        iconContainer.getChildren().add(icon);

        // Description
        VBox textContainer = new VBox(4);
        Label typeLabel = new Label(getTypeLabelText(type));
        typeLabel.setStyle(
                "-fx-font-size: 11px; " +
                        "-fx-text-fill: " + getIconColor(type) + "; " +
                        "-fx-font-weight: bold;"
        );

        Label descLabel = new Label(description);
        descLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: #e2e8f0; " +
                        "-fx-font-weight: 600; " +
                        "-fx-wrap-text: true;"
        );
        descLabel.setMaxWidth(280);

        textContainer.getChildren().addAll(typeLabel, descLabel);

        header.getChildren().addAll(iconContainer, textContainer);

        // Pied de carte avec statut
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);

        FontIcon statusIcon = new FontIcon(getStatusIcon(status));
        statusIcon.setIconSize(12);
        statusIcon.setIconColor(javafx.scene.paint.Color.web(getStatusColor(status)));

        Label statusLabel = new Label(getStatusText(status));
        statusLabel.setStyle(
                "-fx-text-fill: " + getStatusColor(status) + "; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 12px;"
        );

        footer.getChildren().addAll(statusIcon, statusLabel);

        card.getChildren().addAll(header, footer);

        // Effet hover
        card.setOnMouseEntered(e -> card.setStyle(
                card.getStyle().replace("rgba(15, 23, 42, 0.6)", "rgba(15, 23, 42, 0.8)")
        ));
        card.setOnMouseExited(e -> card.setStyle(
                card.getStyle().replace("rgba(15, 23, 42, 0.8)", "rgba(15, 23, 42, 0.6)")
        ));

        return card;
    }

    private VBox createEmptyStateCard() {
        VBox emptyCard = new VBox(15);
        emptyCard.setAlignment(Pos.CENTER);
        emptyCard.setStyle(
                "-fx-padding: 40; " +
                        "-fx-background-color: rgba(15, 23, 42, 0.4); " +
                        "-fx-background-radius: 12;"
        );

        FontIcon emptyIcon = new FontIcon("fas-inbox");
        emptyIcon.setIconSize(48);
        emptyIcon.setIconColor(javafx.scene.paint.Color.web("#64748b"));

        Label emptyLabel = new Label("Aucune activité récente");
        emptyLabel.setStyle(
                "-fx-text-fill: #94a3b8; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: 600;"
        );

        emptyCard.getChildren().addAll(emptyIcon, emptyLabel);
        return emptyCard;
    }

    // Méthodes utilitaires pour les couleurs et icônes
    private String getIconLiteralForType(String type) {
        switch (type) {
            case "VENTE": return "fas-shopping-bag";
            case "COMMENTAIRE": return "fas-comment-dots";
            case "RDV": return "fas-calendar-check";
            default: return "fas-info-circle";
        }
    }

    private String getIconColor(String type) {
        switch (type) {
            case "VENTE": return "#10b981";
            case "COMMENTAIRE": return "#3b82f6";
            case "RDV": return "#f59e0b";
            default: return "#8b5cf6";
        }
    }

    private String getIconBackgroundColor(String type) {
        switch (type) {
            case "VENTE": return "rgba(16, 185, 129, 0.15)";
            case "COMMENTAIRE": return "rgba(59, 130, 246, 0.15)";
            case "RDV": return "rgba(245, 158, 11, 0.15)";
            default: return "rgba(139, 92, 246, 0.15)";
        }
    }

    private String getBorderColorForType(String type) {
        switch (type) {
            case "VENTE": return "#10b981";
            case "COMMENTAIRE": return "#3b82f6";
            case "RDV": return "#f59e0b";
            default: return "#8b5cf6";
        }
    }

    private String getTypeLabelText(String type) {
        switch (type) {
            case "VENTE": return "💰 VENTE";
            case "COMMENTAIRE": return "💬 COMMENTAIRE";
            case "RDV": return "📅 RENDEZ-VOUS";
            default: return "📌 ACTIVITÉ";
        }
    }

    private String getStatusIcon(String status) {
        return status.equals("COMPLETED") ? "fas-check-circle" : "fas-clock";
    }

    private String getStatusText(String status) {
        return status.equals("COMPLETED") ? "Terminé" : "En cours";
    }

    private String getStatusColor(String status) {
        return status.equals("COMPLETED") ? "#10b981" : "#f59e0b";
    }

    private void initializeSystemStatus() {
        try {
            Map<String, String> systemStatus = vendeurDAO.getSystemStatus();
            systemStatusValue.setText(systemStatus.get("statut"));
            systemStatusDesc.setText(systemStatus.get("description"));

            // Couleur dynamique selon le statut
            String statusColor = systemStatus.get("statut").equals("OPTIMUM") ? "#10b981" : "#f59e0b";
            systemStatusValue.setStyle(
                    "-fx-text-fill: " + statusColor + "; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 18px;"
            );
        } catch (SQLException e) {
            systemStatusValue.setText("ERREUR");
            systemStatusValue.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 18px;");
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
        totalArticlesLabel.setText("0");
        availableArticlesLabel.setText("0");
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