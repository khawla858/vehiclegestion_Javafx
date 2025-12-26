package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.auth.utils.SessionManager;
import com.example.vehiclegestion.vendeur.dao.VendeurDAO;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.logging.service.ElasticLogService;
import com.example.vehiclegestion.logging.model.LogEntry;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VendeurDashboardController implements Initializable {

    @FXML private Label totalSalesLabel;
    @FXML private Label revenueLabel;
    @FXML private Label activeClientsLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label totalArticlesLabel;
    @FXML private Label availableArticlesLabel;
    @FXML private Label avgRatingLabel;
    @FXML private Label systemStatusValue;
    @FXML private Label systemStatusDesc;

    @FXML private LineChart<String, Number> salesChart;
    @FXML private PieChart productsPieChart;
    @FXML private StackedBarChart<String, Number> categorySalesChart;
    @FXML private VBox activitiesContainer;

    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private final ElasticLogService logService = new ElasticLogService();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private VendeurDAO vendeurDAO;
    private Utilisateur utilisateurConnecte;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            logService.sendLog(
                    LogEntry.info(
                            LogEntry.ACTION_DISPLAY_PERFORMANCE,
                            null,
                            "Initialisation du Dashboard vendeur"
                    ).setModule(LogEntry.MODULE_CLIENT)
            );

            utilisateurConnecte = SessionManager.getInstance().getUtilisateurConnecte();

            if (utilisateurConnecte == null) {
                logService.sendLog(
                        LogEntry.error(
                                LogEntry.ACTION_DISPLAY_PERFORMANCE,
                                null,
                                "Aucun utilisateur connecté"
                        ).setModule(LogEntry.MODULE_CLIENT)
                );
                showError("Aucun utilisateur connecté");
                return;
            }

            if (!SessionManager.getInstance().estVendeur()) {
                logService.sendLog(
                        LogEntry.error(
                                LogEntry.ACTION_DISPLAY_PERFORMANCE,
                                utilisateurConnecte.getEmail(),
                                "Accès refusé - rôle non vendeur"
                        ).setModule(LogEntry.MODULE_CLIENT)
                );
                showError("Accès refusé");
                return;
            }

            initializeDefaultUI();
            loadDashboardDataAsync();

        } catch (Exception e) {
            logService.sendLog(
                    LogEntry.error(
                            LogEntry.ACTION_DISPLAY_PERFORMANCE,
                            null,
                            "Erreur critique dashboard : " + e.getMessage()
                    ).setModule(LogEntry.MODULE_CLIENT)
            );
            showError(e.getMessage());
        }
    }

    private void initializeDefaultUI() {
        totalSalesLabel.setText("0");
        revenueLabel.setText("0 MAD");
        activeClientsLabel.setText("0");
        pendingOrdersLabel.setText("0");
        totalArticlesLabel.setText("0");
        availableArticlesLabel.setText("0");
        avgRatingLabel.setText("0.0");
        systemStatusValue.setText("Chargement...");
        systemStatusDesc.setText("Chargement des données...");

        salesChart.getData().clear();
        productsPieChart.getData().clear();
        categorySalesChart.getData().clear();
        activitiesContainer.getChildren().clear();
    }

    private void loadDashboardDataAsync() {
        executor.submit(() -> {
            try {
                vendeurDAO = new VendeurDAO(utilisateurConnecte.getIdUtilisateur());

                Platform.runLater(() -> {
                    initializeStatistics();
                    initializeAdditionalStatistics();
                    systemStatusValue.setText("OK");
                    systemStatusDesc.setText("Système opérationnel");
                });

                Thread.sleep(400);

                Platform.runLater(() -> {
                    initializeCharts();
                    loadCategorySalesChart();
                });

                Thread.sleep(300);

                Platform.runLater(this::refreshActivities);

                logService.sendLog(
                        LogEntry.success(
                                LogEntry.ACTION_DISPLAY_PERFORMANCE,
                                utilisateurConnecte.getEmail(),
                                "Dashboard vendeur chargé"
                        ).setModule(LogEntry.MODULE_CLIENT)
                );

            } catch (Exception e) {
                logService.sendLog(
                        LogEntry.error(
                                LogEntry.ACTION_DISPLAY_PERFORMANCE,
                                utilisateurConnecte.getEmail(),
                                "Erreur chargement async : " + e.getMessage()
                        ).setModule(LogEntry.MODULE_CLIENT)
                );
            }
        });
    }

    private void initializeStatistics() {
        try {
            totalSalesLabel.setText(String.valueOf(vendeurDAO.getTotalSales()));
            revenueLabel.setText(df.format(vendeurDAO.getTotalRevenue()) + " MAD");
            activeClientsLabel.setText(String.valueOf(vendeurDAO.getActiveClientsCount()));
            pendingOrdersLabel.setText(String.valueOf(vendeurDAO.getPendingOrdersCount()));
        } catch (SQLException e) {
            logService.sendLog(
                    LogEntry.error(
                            LogEntry.ACTION_DISPLAY_PERFORMANCE,
                            utilisateurConnecte.getEmail(),
                            "Erreur statistiques : " + e.getMessage()
                    ).setModule(LogEntry.MODULE_CLIENT)
            );
        }
    }

    private void initializeAdditionalStatistics() {
        try {
            totalArticlesLabel.setText(String.valueOf(vendeurDAO.getTotalArticles()));
            availableArticlesLabel.setText(String.valueOf(vendeurDAO.getAvailableArticles()));
            avgRatingLabel.setText(String.format("%.1f", vendeurDAO.getAverageRating()));
        } catch (SQLException ignored) {}
    }

    private void initializeCharts() {
        try {
            initializeSalesChart();
            initializeProductsChart();
        } catch (SQLException ignored) {}
    }

    private void initializeSalesChart() throws SQLException {
        Map<String, Integer> data = vendeurDAO.getSalesPerMonth();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (String m : data.keySet()) {
            series.getData().add(new XYChart.Data<>(m, data.get(m)));
        }
        salesChart.getData().setAll(series);
    }

    private void initializeProductsChart() throws SQLException {
        ObservableList<PieChart.Data> pie = FXCollections.observableArrayList();
        vendeurDAO.getProductDistribution()
                .forEach((k, v) -> pie.add(new PieChart.Data(k, v)));
        productsPieChart.setData(pie);
    }

    @FXML
    private void refreshActivities() {
        executor.submit(() -> {
            try {
                List<Map<String, String>> activities = vendeurDAO.getRecentActivities();
                Platform.runLater(() -> {
                    activitiesContainer.getChildren().clear();
                    activities.forEach(a ->
                            activitiesContainer.getChildren().add(
                                    createActivityCard(a.get("type"), a.get("description"), a.get("status"))
                            )
                    );
                });

                logService.sendLog(
                        LogEntry.info(
                                LogEntry.ACTION_DISPLAY_PERFORMANCE,
                                utilisateurConnecte.getEmail(),
                                "Activités chargées"
                        ).setModule(LogEntry.MODULE_CLIENT)
                );

            } catch (SQLException e) {
                logService.sendLog(
                        LogEntry.error(
                                LogEntry.ACTION_DISPLAY_PERFORMANCE,
                                utilisateurConnecte.getEmail(),
                                "Erreur activités : " + e.getMessage()
                        ).setModule(LogEntry.MODULE_CLIENT)
                );
            }
        });
    }

    private VBox createActivityCard(String type, String desc, String status) {
        VBox box = new VBox(5);
        box.getChildren().addAll(new Label(desc), new Label(status));
        return box;
    }

    @FXML
    private void refreshData() {
        logService.sendLog(
                LogEntry.info(
                        LogEntry.ACTION_DISPLAY_PERFORMANCE,
                        utilisateurConnecte.getEmail(),
                        "Rafraîchissement manuel"
                ).setModule(LogEntry.MODULE_CLIENT)
        );
        loadDashboardDataAsync();
    }

    public void shutdown() {
        executor.shutdown();
    }
    private void showError(String message) {
        Platform.runLater(() -> {
            VBox errorBox = new VBox(10);
            errorBox.setStyle("-fx-padding: 20; -fx-alignment: center;");

            Label errorLabel = new Label("⚠️ " + message);
            errorLabel.setStyle(
                    "-fx-text-fill: #ef4444; " +
                            "-fx-font-size: 16px; " +
                            "-fx-font-weight: bold;"
            );
            errorLabel.setWrapText(true);

            Button retryButton = new Button("Réessayer");
            retryButton.setOnAction(e -> loadDashboardDataAsync());
            retryButton.setStyle(
                    "-fx-background-color: #3b82f6; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 10 20; " +
                            "-fx-background-radius: 8;"
            );

            errorBox.getChildren().addAll(errorLabel, retryButton);

            ScrollPane scrollPane = (ScrollPane) totalSalesLabel.getScene().getRoot();
            scrollPane.setContent(errorBox);
        });
    }
    public void loadCategorySalesChart() {
        try {
            Map<String, Map<String, Double>> monthlyCategorySales =
                    vendeurDAO.getMonthlyCategoryRevenuePercentage();

            Platform.runLater(() -> {
                categorySalesChart.getData().clear();

                if (monthlyCategorySales.isEmpty()) {
                    return;
                }

                String[] months = {"Jan","Fév","Mar","Avr","Mai","Jun",
                        "Jul","Aoû","Sep","Oct","Nov","Déc"};

                for (String category : monthlyCategorySales.keySet()) {
                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName(category);

                    Map<String, Double> monthData = monthlyCategorySales.get(category);

                    for (String month : months) {
                        double value = monthData.getOrDefault(month, 0.0);
                        series.getData().add(new XYChart.Data<>(month, value));
                    }

                    categorySalesChart.getData().add(series);
                }
            });

            logService.sendLog(
                    LogEntry.info(
                            LogEntry.ACTION_DISPLAY_PERFORMANCE,
                            utilisateurConnecte.getEmail(),
                            "Graphique ventes par catégorie chargé"
                    ).setModule(LogEntry.MODULE_CLIENT)
            );

        } catch (SQLException e) {
            logService.sendLog(
                    LogEntry.error(
                            LogEntry.ACTION_DISPLAY_PERFORMANCE,
                            utilisateurConnecte.getEmail(),
                            "Erreur graphique catégories : " + e.getMessage()
                    ).setModule(LogEntry.MODULE_CLIENT)
            );
        }
    }

}
