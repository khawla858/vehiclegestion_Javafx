package com.example.vehiclegestion.admin.controller;

import com.example.vehiclegestion.admin.service.AdminService;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.auth.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.List;

import com.example.vehiclegestion.common.utils.NotificationService;
import com.example.vehiclegestion.common.model.Notification;
import javafx.stage.Popup;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * Controller JavaFX pour le Dashboard Admin avec profil utilisateur
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

    // Labels du profil utilisateur
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    private final AdminService adminService;
    private Utilisateur currentUser;

    public AdminDashboardController() {
        this.adminService = new AdminService();
    }

    /**
     * Initialisation du controller (appelé automatiquement par JavaFX)
     */
    @FXML
    public void initialize() {
        System.out.println("🎛️ Initialisation Dashboard Admin...");

        // Charger l'utilisateur connecté
        loadCurrentUser();

        // Charger les données du dashboard
        loadDashboardData();
    }

    /**
     * Charger les informations de l'utilisateur connecté
     */
    private void loadCurrentUser() {
        currentUser = SessionManager.getCurrentUser();

        if (currentUser != null) {
            // Afficher le nom complet
            String fullName = currentUser.getPrenom() + " " + currentUser.getNom();
            userNameLabel.setText(fullName);

            // Afficher le rôle avec formatage
            String role = formatRole(currentUser.getRole());
            userRoleLabel.setText(role);

            System.out.println("✅ Profil chargé: " + fullName + " (" + role + ")");
        } else {
            userNameLabel.setText("Utilisateur");
            userRoleLabel.setText("Non connecté");
            System.out.println("⚠️ Aucun utilisateur connecté");
        }
    }

    /**
     * Gérer le clic sur le profil (afficher un menu ou une page de profil)
     */
    @FXML
    public void handleProfileClick() {
        if (currentUser != null) {
            showProfileDialog();
        }
    }

    /**
     * Afficher une fenêtre de profil stylisée
     */
    private void showProfileDialog() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Profil Utilisateur");
        dialogStage.setResizable(false);

        // Container principal
        VBox mainContainer = new VBox(25);
        mainContainer.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #0f172a, #1e293b);" +
                        "-fx-padding: 30;"
        );

        // Header avec avatar
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        header.setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.8);" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: linear-gradient(to right, #3b82f6, #8b5cf6);" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-padding: 25;"
        );

        // Avatar circle
        Circle avatar = new Circle(50);
        avatar.setStyle(
                "-fx-fill: linear-gradient(to bottom right, #3b82f6, #8b5cf6);" +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.6), 15, 0.6, 0, 0);"
        );

        Label avatarIcon = new Label("👤");
        avatarIcon.setStyle("-fx-font-size: 50px;");
        StackPane avatarStack = new StackPane(avatar, avatarIcon);

        // Nom complet
        Label nameLabel = new Label(currentUser.getPrenom() + " " + currentUser.getNom());
        nameLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;"
        );

        // Badge rôle
        Label roleLabel = new Label(formatRole(currentUser.getRole()));
        roleLabel.setStyle(
                "-fx-text-fill: #10b981;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: rgba(16, 185, 129, 0.2);" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 5 15;"
        );

        header.getChildren().addAll(avatarStack, nameLabel, roleLabel);

        // Corps avec informations
        VBox body = new VBox(15);
        body.setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.8);" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 15;" +
                        "-fx-padding: 25;"
        );

        // Créer les lignes d'information
        body.getChildren().addAll(
                createInfoRow("✉️", "Email", currentUser.getEmail()),
                createInfoRow("🆔", "ID Utilisateur", String.valueOf(currentUser.getIdUtilisateur())),
                createInfoRow("📊", "Statut", capitalizeFirst(currentUser.getStatut())),
                createInfoRow("📅", "Date de création",
                        currentUser.getDateCreation() != null ?
                                formatDate(currentUser.getDateCreation()) : "N/A")
        );

        // Footer avec bouton
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER);

        Button closeButton = new Button("Fermer");
        closeButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #3b82f6, #8b5cf6);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 30;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.5), 10, 0.5, 0, 3);"
        );
        closeButton.setOnAction(e -> dialogStage.close());

        footer.getChildren().add(closeButton);

        mainContainer.getChildren().addAll(header, body, footer);

        Scene scene = new Scene(mainContainer, 450, 550);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }

    /**
     * Créer une ligne d'information stylisée
     */
    private HBox createInfoRow(String icon, String label, String value) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle(
                "-fx-background-color: rgba(15, 23, 42, 0.5);" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 15;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        VBox textBox = new VBox(3);

        Label labelText = new Label(label);
        labelText.setStyle(
                "-fx-text-fill: #94a3b8;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
        );

        Label valueText = new Label(value);
        valueText.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;"
        );

        textBox.getChildren().addAll(labelText, valueText);
        row.getChildren().addAll(iconLabel, textBox);

        return row;
    }

    /**
     * Formater une date
     */
    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Capitaliser la première lettre
     */
    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    /**
     * Formater le rôle pour l'affichage
     */
    private String formatRole(String role) {
        if (role == null) return "Utilisateur";

        switch (role.toLowerCase()) {
            case "admin":
                return "Administrateur";
            case "gestionnaire":
                return "Gestionnaire";
            case "vendeur":
                return "Vendeur";
            default:
                return capitalizeRole(role);
        }
    }

    /**
     * Gérer la déconnexion
     */
    @FXML
    public void handleLogout() {
        try {
            // Nettoyer la session
            SessionManager.clearSession();

            System.out.println("👋 Déconnexion de l'utilisateur");

            // Rediriger vers la page de login
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/vehiclegestion/auth/view/Login.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Connexion - Gestion Véhicules");
            stage.show();

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la déconnexion: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de déconnexion");
            alert.setContentText("Une erreur s'est produite lors de la déconnexion.");
            alert.showAndWait();
        }
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
        Integer totalUsers = (Integer) stats.get("totalUsers");
        totalUsersLabel.setText(totalUsers != null ? String.valueOf(totalUsers) : "0");

        Integer totalMagasins = (Integer) stats.get("totalMagasins");
        totalMagasinsLabel.setText(totalMagasins != null ? String.valueOf(totalMagasins) : "0");

        Integer newUsers = (Integer) stats.get("newUsersLast30Days");
        newUsersLabel.setText(newUsers != null ? String.valueOf(newUsers) : "0");

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

        List<Map<String, Object>> topMagasins = (List<Map<String, Object>>) stats.get("topMagasins");
        if (topMagasins != null && topMagasinsChart != null) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Nombre de véhicules");

            for (Map<String, Object> magasin : topMagasins) {
                String nom = (String) magasin.get("nom");
                Integer nbVehicules = (Integer) magasin.get("nbVehicules");

                if (nom != null && nbVehicules != null) {
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
     * Capitaliser la première lettre
     */
    private String capitalizeRole(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }
    // Ajoutez ces attributs à la classe
    @FXML private Label notificationBadge;
    @FXML private StackPane notificationBadgeContainer;
    @FXML private Button notificationButton;

    private NotificationService notificationService;
    private Popup notificationPopup;

    /*/ Ajoutez dans le constructeur
    public AdminDashboardController() {
        this.adminService = new AdminService();
        this.notificationService = new NotificationService(); // AJOUTER CETTE LIGNE
    }*/

    /*/ Ajoutez dans la méthode initialize()
    @FXML
    public void initialize() {
        System.out.println("🎛️ Initialisation Dashboard Admin...");

        loadCurrentUser();
        loadDashboardData();

        // AJOUTER CES LIGNES
        updateNotificationBadge();
        startNotificationRefresh();
    }*/

    /**
     * Mettre à jour le badge de notifications
     */
    private void updateNotificationBadge() {
        if (currentUser != null) {
            int count = notificationService.compterNotificationsNonLues(currentUser.getIdUtilisateur());

            if (count > 0) {
                notificationBadge.setText(String.valueOf(Math.min(count, 99)));
                notificationBadgeContainer.setVisible(true);
            } else {
                notificationBadgeContainer.setVisible(false);
            }
        }
    }

    /**
     * Démarrer le rafraîchissement automatique des notifications
     */
    private void startNotificationRefresh() {
        Thread notifThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // 30 secondes
                    javafx.application.Platform.runLater(this::updateNotificationBadge);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        notifThread.setDaemon(true);
        notifThread.start();
    }

    /**
     * Afficher le panneau de notifications
     */
    @FXML
    public void handleShowNotifications() {
        try {
            if (notificationPopup == null || !notificationPopup.isShowing()) {
                // Charger le panneau de notifications
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/vehiclegestion/common/view/NotificationPanel.fxml")
                );
                Parent notifPanel = loader.load();

                // Créer le popup
                notificationPopup = new Popup();
                notificationPopup.setAutoHide(true);
                notificationPopup.getContent().add(notifPanel);

                // Positionner le popup sous le bouton de notification
                javafx.geometry.Bounds bounds = notificationButton.localToScreen(
                        notificationButton.getBoundsInLocal()
                );

                notificationPopup.show(
                        notificationButton.getScene().getWindow(),
                        bounds.getMinX() - 350, // Ajuster selon la largeur du panel
                        bounds.getMaxY() + 10
                );

                // Mettre à jour le badge après affichage
                notificationPopup.setOnHidden(e -> updateNotificationBadge());

            } else {
                notificationPopup.hide();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Exemple: Créer une notification de test
     */
    @FXML
    public void handleCreateTestNotification() {
        if (currentUser != null) {
            Notification testNotif = new Notification();
            testNotif.setIdUtilisateur(currentUser.getIdUtilisateur());
            testNotif.setRoleDestinataire(currentUser.getRole());
            testNotif.setTitre("🧪 Notification de test");
            testNotif.setMessage("Ceci est une notification de test créée manuellement.");
            testNotif.setTypeNotification("info");
            testNotif.setCategorie("test");
            testNotif.setPriorite("normale");

            notificationService.creerNotification(testNotif);
            updateNotificationBadge();

            System.out.println("✅ Notification de test créée");
        }
    }

}