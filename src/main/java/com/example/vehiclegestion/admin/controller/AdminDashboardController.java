package com.example.vehiclegestion.admin.controller;

import com.example.vehiclegestion.admin.service.AdminService;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Map;
import java.util.List;

/**
 * Controller JavaFX pour le Dashboard Admin
 */
public class AdminDashboardController {

    @FXML private Label totalUsersLabel;
    @FXML private Label totalMagasinsLabel;
    @FXML private Label newUsersLabel;
    @FXML private Label activeUsersLabel;

    @FXML private PieChart usersByRolePieChart;
    @FXML private PieChart usersByStatusPieChart;
    @FXML private BarChart<String, Number> magasinsByCategorie;
    @FXML private BarChart<String, Number> topMagasinsChart;

    private final AdminService adminService;

    public AdminDashboardController() {
        this.adminService = new AdminService();
    }

    /**
     * Initialisation du controller (appelé automatiquement par JavaFX)
     */
    @FXML
    public void initialize() {
        System.out.println("🎛️ Initialisation Dashboard Admin...");
        loadDashboardData();
    }

    /**
     * Charger toutes les données du dashboard
     */
    public void loadDashboardData() {
        try {
            Map<String, Object> stats = adminService.getDashboardStats();

            // Statistiques générales
            loadGeneralStats(stats);

            // Graphiques utilisateurs
            loadUserCharts(stats);

            // Graphiques magasins
            loadMagasinCharts(stats);

            System.out.println("✅ Dashboard chargé avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Charger les statistiques générales
     */
    private void loadGeneralStats(Map<String, Object> stats) {
        // Total utilisateurs
        Integer totalUsers = (Integer) stats.get("totalUsers");
        totalUsersLabel.setText(totalUsers != null ? String.valueOf(totalUsers) : "0");

        // Total magasins
        Integer totalMagasins = (Integer) stats.get("totalMagasins");
        totalMagasinsLabel.setText(totalMagasins != null ? String.valueOf(totalMagasins) : "0");

        // Nouveaux utilisateurs (30 derniers jours)
        Integer newUsers = (Integer) stats.get("newUsersLast30Days");
        newUsersLabel.setText(newUsers != null ? String.valueOf(newUsers) : "0");

        // Utilisateurs actifs
        @SuppressWarnings("unchecked")
        Map<String, Integer> usersByStatus = (Map<String, Integer>) stats.get("usersByStatus");
        if (usersByStatus != null) {
            Integer active = usersByStatus.getOrDefault("actif", 0);
            activeUsersLabel.setText(String.valueOf(active));
        } else {
            activeUsersLabel.setText("0");
        }
    }

    /**
     * Charger les graphiques utilisateurs
     */
    @SuppressWarnings("unchecked")
    private void loadUserCharts(Map<String, Object> stats) {
        // Graphique par rôle (Pie Chart)
        Map<String, Integer> usersByRole = (Map<String, Integer>) stats.get("usersByRole");
        if (usersByRole != null && usersByRolePieChart != null) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            usersByRole.forEach((role, count) -> {
                String label = capitalizeRole(role);
                pieData.add(new PieChart.Data(label + " (" + count + ")", count));
            });

            usersByRolePieChart.setData(pieData);
            usersByRolePieChart.setTitle("Utilisateurs par Rôle");
        }

        // Graphique par statut (Pie Chart)
        Map<String, Integer> usersByStatus = (Map<String, Integer>) stats.get("usersByStatus");
        if (usersByStatus != null && usersByStatusPieChart != null) {
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            usersByStatus.forEach((statut, count) -> {
                String label = capitalizeRole(statut);
                pieData.add(new PieChart.Data(label + " (" + count + ")", count));
            });

            usersByStatusPieChart.setData(pieData);
            usersByStatusPieChart.setTitle("Utilisateurs par Statut");
        }
    }

    /**
     * Charger les graphiques magasins
     */
    @SuppressWarnings("unchecked")
    private void loadMagasinCharts(Map<String, Object> stats) {
        // Magasins par catégorie (Bar Chart)
        Map<String, Integer> magasinsByCategorie = (Map<String, Integer>) stats.get("magasinsByCategorie");
        if (magasinsByCategorie != null && this.magasinsByCategorie != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de magasins");

            magasinsByCategorie.forEach((categorie, count) -> {
                series.getData().add(new XYChart.Data<>(categorie, count));
            });

            this.magasinsByCategorie.getData().clear();
            this.magasinsByCategorie.getData().add(series);
            this.magasinsByCategorie.setTitle("Magasins par Catégorie");
        }

        // Top magasins (Bar Chart)
        List<Map<String, Object>> topMagasins = (List<Map<String, Object>>) stats.get("topMagasins");
        if (topMagasins != null && topMagasinsChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de véhicules");

            for (Map<String, Object> magasin : topMagasins) {
                String nom = (String) magasin.get("nom");
                Integer nbVehicules = (Integer) magasin.get("nbVehicules");

                if (nom != null && nbVehicules != null) {
                    // Limiter la longueur du nom pour l'affichage
                    String shortName = nom.length() > 15 ? nom.substring(0, 15) + "..." : nom;
                    series.getData().add(new XYChart.Data<>(shortName, nbVehicules));
                }
            }

            topMagasinsChart.getData().clear();
            topMagasinsChart.getData().add(series);
            topMagasinsChart.setTitle("Top 5 Magasins (par véhicules)");
        }
    }

    /**
     * Rafraîchir les données du dashboard
     */
    @FXML
    public void handleRefresh() {
        System.out.println("🔄 Rafraîchissement du dashboard...");
        loadDashboardData();
    }

    /**
     * Capitaliser la première lettre (pour affichage)
     */
    private String capitalizeRole(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
}