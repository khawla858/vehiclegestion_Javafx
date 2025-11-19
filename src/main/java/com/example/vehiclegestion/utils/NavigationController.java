package com.example.vehiclegestion.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class NavigationController {

    public static void loadDashboard() {
        try {
            System.out.println("🚀 Chargement du dashboard...");

            // Charger le FXML du dashboard
            FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource("/com/example/vehiclegestion/view/client/client-dashboard.fxml"));
            Parent dashboard = loader.load();

            // Créer une nouvelle fenêtre pour le dashboard
            Stage dashboardStage = new Stage();
            dashboardStage.setTitle("AutoSales Pro - Tableau de Bord");
            dashboardStage.setScene(new Scene(dashboard, 1200, 800));
            dashboardStage.setMaximized(true);

            // Fermer la fenêtre de login actuelle
            closeLoginWindow();

            // Afficher le dashboard
            dashboardStage.show();

            System.out.println("✅ Dashboard chargé avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur navigation dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void closeLoginWindow() {
        try {
            // Fermer toutes les fenêtres de type Stage qui pourraient être la fenêtre de login
            for (Stage stage : Stage.getWindows().toArray(new Stage[0])) {
                if (stage.getTitle() != null && stage.getTitle().contains("AutoSales Pro")) {
                    stage.close();
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Impossible de fermer la fenêtre de login: " + e.getMessage());
        }
    }

    public static void loadLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource("/com/example/vehiclegestion/view/auth/login.fxml"));
            Parent login = loader.load();

            Stage stage = new Stage();
            stage.setTitle("AutoSales Pro - Connexion");
            stage.setScene(new Scene(login, 900, 700));
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ Erreur navigation login: " + e.getMessage());
        }
    }
}