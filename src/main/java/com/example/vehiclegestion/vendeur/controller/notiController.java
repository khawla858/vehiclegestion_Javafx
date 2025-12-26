package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.common.model.Notification;
import com.example.vehiclegestion.common.utils.NotificationManager;
import com.example.vehiclegestion.common.utils.NotificationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class notiController implements Initializable, NotificationManager.NotificationListener {

    @FXML private VBox notificationsContainer;
    @FXML private Label lblStatut;
    @FXML private Button btnToutMarquer;
    @FXML private ToggleButton toggleToutes, toggleNonLues, toggleUrgentes;
    @FXML private Button btnVoirTout;
    @FXML private Button btnParametres;
    @FXML private ScrollPane scrollPane;
    @FXML private ProgressIndicator loadingIndicator;

    private NotificationService notificationService;
    private SessionManager sessionManager;
    private Timeline refreshTimeline;
    private Timeline newNotificationPulse;
    private int currentFilter = 0; // 0=toutes, 1=non lues, 2=urgentes
    private boolean isLoading = false;

    // Instance statique pour accès depuis l'extérieur
    private static notiController currentInstance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔔 notiController (vendeur) initialisé");

        currentInstance = this;

        notificationService = NotificationService.getInstance();
        sessionManager = SessionManager.getInstance();

        if (!sessionManager.estConnecte()) {
            showError("Vendeur non connecté");
            return;
        }

        setupUI();
        loadNotifications();
        setupAutoRefresh();
        setupEventHandlers();
        registerNotificationListener();

        // Animation de chargement initiale
        showLoading(true);
        Timeline initialLoad = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            showLoading(false);
        }));
        initialLoad.play();
    }

    private void registerNotificationListener() {
        NotificationManager.getInstance().addListener(this);
        System.out.println("👂 notiController enregistré comme listener");
    }

    @Override
    public void onNewNotification(int userId) {
        System.out.println("📬 onNewNotification reçu pour user (vendeur): " + userId);

        int currentUserId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();
        if (userId == currentUserId) {
            Platform.runLater(() -> {
                loadNotifications();
                pulseNewNotification();
                playNotificationSound();
            });
        }
    }

    @Override
    public void onNotificationRead(int notificationId) {
        Platform.runLater(() -> {
            loadNotifications();
        });
    }

    private void setupUI() {
        // Grouper les toggle buttons
        ToggleGroup filterGroup = new ToggleGroup();
        toggleToutes.setToggleGroup(filterGroup);
        toggleNonLues.setToggleGroup(filterGroup);
        toggleUrgentes.setToggleGroup(filterGroup);
        toggleToutes.setSelected(true);

        updateToggleStyles();

        filterGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == toggleToutes) {
                currentFilter = 0;
            } else if (newToggle == toggleNonLues) {
                currentFilter = 1;
            } else if (newToggle == toggleUrgentes) {
                currentFilter = 2;
            }
            updateToggleStyles();
            loadNotifications();
        });

        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("notification-scrollpane");
    }

    private void updateToggleStyles() {
        String selectedStyle = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 15; "
                + "-fx-background-radius: 4; -fx-border-radius: 4; -fx-font-weight: bold;";
        String unselectedStyle = "-fx-background-color: #f5f5f5; -fx-text-fill: #666; -fx-padding: 5 15; "
                + "-fx-background-radius: 4; -fx-border-radius: 4; -fx-border-color: #ddd; -fx-border-width: 1;";

        toggleToutes.setStyle(toggleToutes.isSelected() ? selectedStyle : unselectedStyle);
        toggleNonLues.setStyle(toggleNonLues.isSelected() ? selectedStyle : unselectedStyle);
        toggleUrgentes.setStyle(toggleUrgentes.isSelected() ? selectedStyle : unselectedStyle);
    }

    private void loadNotifications() {
        if (isLoading) return;

        isLoading = true;
        showLoading(true);

        int userId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();
        System.out.println("🔄 Chargement notifications pour vendeur: " + userId);
        System.out.println("👤 ID utilisateur connecté: " + userId);

        new Thread(() -> {
            try {
                // DEBUG: Appel direct au DAO pour vérifier
                System.out.println("🔍 Appel à notificationService.getNotificationsUtilisateur(" + userId + ", false)");

                List<Notification> allNotifications = notificationService.getNotificationsUtilisateur(userId, false);

                System.out.println("📊 Notifications brutes reçues: " + (allNotifications != null ? allNotifications.size() : "null"));

                if (allNotifications != null && !allNotifications.isEmpty()) {
                    for (Notification n : allNotifications) {
                        System.out.println("   - ID: " + n.getIdNotification() +
                                ", Titre: " + n.getTitre() +
                                ", Catégorie: " + n.getCategorie() +
                                ", Lue: " + n.isEstLue() +
                                ", Rôle: " + n.getRoleDestinataire());
                    }
                }

                List<Notification> notifications = getFilteredNotifications(userId);

                Platform.runLater(() -> {
                    notificationsContainer.getChildren().clear();

                    if (notifications.isEmpty()) {
                        showEmptyState();
                        lblStatut.setText("0 notification(s)");
                        System.out.println("⚠️ Aucune notification après filtrage");
                    } else {
                        int nonLuesCount = 0;
                        for (Notification notif : notifications) {
                            if (!notif.isEstLue()) {
                                nonLuesCount++;
                            }
                            addNotificationItem(notif);
                        }
                        lblStatut.setText(notifications.size() + " notification(s) • " + nonLuesCount + " non lue(s)");
                        System.out.println("✅ " + notifications.size() + " notifications affichées");
                    }

                    showLoading(false);
                    isLoading = false;
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Erreur de chargement: " + e.getMessage());
                    showLoading(false);
                    isLoading = false;
                });
                e.printStackTrace();
            }
        }).start();
    }
    private List<Notification> getFilteredNotifications(int userId) {
        List<Notification> allNotifications = notificationService.getNotificationsUtilisateur(userId, false);

        if (allNotifications == null) {
            System.out.println("⚠️ Aucune notification reçue du service");
            return List.of();
        }

        switch (currentFilter) {
            case 1: // Non lues seulement
                allNotifications.removeIf(Notification::isEstLue);
                break;
            case 2: // Urgentes seulement
                allNotifications.removeIf(n -> !"urgente".equalsIgnoreCase(n.getPriorite()));
                break;
            default: // Toutes
                break;
        }

        return allNotifications;
    }

    private void addNotificationItem(Notification notification) {
        try {
            HBox notificationItem = new HBox(10);
            notificationItem.getStyleClass().add("notification-item");

            String baseStyle = "-fx-padding: 12; -fx-background-radius: 8; "
                    + "-fx-background-color: white; -fx-border-color: #e0e0e0; "
                    + "-fx-border-radius: 8; -fx-border-width: 1;";

            // Marqueur "non lue"
            if (!notification.isEstLue()) {
                notificationItem.setStyle(baseStyle + "-fx-border-color: #3498db; -fx-border-width: 2;");
            } else {
                notificationItem.setStyle(baseStyle);
            }

            // Icône (adaptée pour vendeur)
            Label iconLabel = new Label(getNotificationIcon(notification));
            iconLabel.setStyle("-fx-font-size: 24px; -fx-padding: 0 10 0 0;");

            // Contenu
            VBox contentBox = new VBox(5);

            // En-tête
            HBox headerBox = new HBox();
            Label titreLabel = new Label(notification.getTitre());
            titreLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label tempsLabel = new Label(notification.getTimeAgo());
            tempsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

            headerBox.getChildren().addAll(titreLabel, spacer, tempsLabel);

            // Message - CORRECTION ICI : vérifier si getContenu() ou getMessage() existe
            String messageText = "";
            try {
                // Essayer d'abord getContenu()
                messageText = notification.getMessage();
            } catch (Exception e1) {
                try {
                    // Si getContenu() n'existe pas, essayer getMessage()
                    messageText = notification.getMessage();
                } catch (Exception e2) {
                    messageText = "Contenu non disponible";
                }
            }

            Label messageLabel = new Label(messageText);
            messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(400);

            // Pied de page
            HBox footerBox = new HBox(10);
            Label categorieLabel = new Label(notification.getCategorie().toUpperCase());
            categorieLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666; "
                    + "-fx-padding: 2 6; -fx-background-color: #f0f0f0; "
                    + "-fx-background-radius: 10;");

            // Priorité
            if ("urgente".equalsIgnoreCase(notification.getPriorite())) {
                Label prioriteLabel = new Label("URGENT");
                prioriteLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #e74c3c; "
                        + "-fx-padding: 2 6; -fx-background-color: #ffeaea; "
                        + "-fx-background-radius: 10; -fx-font-weight: bold;");
                footerBox.getChildren().add(prioriteLabel);
            }

            Region footerSpacer = new Region();
            HBox.setHgrow(footerSpacer, Priority.ALWAYS);

            Button actionButton = new Button("Ouvrir");
            actionButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; "
                    + "-fx-padding: 4 12; -fx-background-radius: 4; -fx-font-size: 12px;");
            actionButton.setOnAction(e -> handleNotificationClick(notification));

            footerBox.getChildren().addAll(categorieLabel, footerSpacer, actionButton);

            contentBox.getChildren().addAll(headerBox, messageLabel, footerBox);

            // Assemblage
            notificationItem.getChildren().addAll(iconLabel, contentBox);

            // Interaction
            notificationItem.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1) {
                    if (!notification.isEstLue()) {
                        notificationService.marquerCommeLue(notification.getIdNotification());
                        notificationItem.setStyle(baseStyle);
                    }
                }

                if (e.getClickCount() == 2) {
                    handleNotificationClick(notification);
                }
            });

            // Effet hover
            notificationItem.setOnMouseEntered(e -> {
                String currentStyle = notificationItem.getStyle();
                notificationItem.setStyle(currentStyle + "-fx-background-color: #f9f9f9;");
            });

            notificationItem.setOnMouseExited(e -> {
                String currentStyle = notificationItem.getStyle();
                notificationItem.setStyle(currentStyle.replace(
                        "-fx-background-color: #f9f9f9;",
                        "-fx-background-color: white;"
                ));
            });

            notificationsContainer.getChildren().add(notificationItem);

        } catch (Exception e) {
            System.err.println("Erreur création item notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getNotificationIcon(Notification notification) {
        String categorie = notification.getCategorie().toLowerCase();
        switch (categorie) {
            case "message":
                return "💬";
            case "vente":
            case "transaction":
                return "💰";
            case "rdv":
            case "rendezvous":
                return "📅";
            case "vehicule":
            case "stock":
                return "🚗";
            case "client":
            case "customer":
                return "👤";
            case "commande":
            case "order":
                return "📦";
            case "promotion":
            case "offer":
                return "🎉";
            case "alerte":
                return "⚠️";
            case "systeme":
                return "⚙️";
            default:
                return "🔔";
        }
    }

    private void handleNotificationClick(Notification notification) {
        System.out.println("📱 Notification cliquée (vendeur): " + notification.getTitre());

        if (!notification.isEstLue()) {
            notificationService.marquerCommeLue(notification.getIdNotification());
        }

        handleNotificationNavigation(notification);
    }

    private void handleNotificationNavigation(Notification notification) {
        String lien = notification.getLienAction();
        String categorie = notification.getCategorie();

        System.out.println("🎯 Vendeur - Navigation depuis notification: " + categorie);

        Stage currentStage = (Stage) btnToutMarquer.getScene().getWindow();
        currentStage.close();

        try {
            if (lien != null && !lien.isEmpty()) {
                navigateByLien(lien);
                return;
            }

            navigateByCategorie(categorie, notification);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la navigation: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la notification", Alert.AlertType.ERROR);
        }
    }

    private void navigateByLien(String lien) {
        // Implémentez la logique de navigation selon vos besoins
        if (lien.contains("message") || lien.contains("chat")) {
            openChatWindow();
        } else if (lien.contains("vehicule")) {
            openVehicleManagement();
        } else if (lien.contains("reservation") || lien.contains("rdv")) {
            openRendezVousManagement();
        } else if (lien.contains("client")) {
            openCustomerManagement();
        } else {
            showInfo("Navigation", "Redirection vers: " + lien);
        }
    }

    private void navigateByCategorie(String categorie, Notification notification) {
        // Méthode pour obtenir le contenu de la notification
        String contenu = "";
        try {
            contenu = notification.getMessage();
        } catch (Exception e) {
            try {
                contenu = notification.getMessage();
            } catch (Exception e2) {
                contenu = "";
            }
        }

        switch (categorie.toLowerCase()) {
            case "message":
            case "chat":
                openChatWindow();
                break;
            case "vente":
            case "transaction":
                openSalesManagement();
                break;
            case "rdv":
            case "rendezvous":
                openRendezVousManagement();
                break;
            case "client":
            case "customer":
                openCustomerManagement();
                break;
            case "vehicule":
            case "stock":
                openVehicleManagement();
                break;
            case "alerte":
            case "alert":
                if (contenu.toLowerCase().contains("stock")) {
                    openLowStockAlert();
                } else if (contenu.toLowerCase().contains("rdv")) {
                    openRendezVousManagement();
                }
                break;
            default:
                showInfo("Notification", notification.getTitre() + "\n" + contenu);
                break;
        }
    }

    // Méthodes d'ouverture des différentes vues
    private void openChatWindow() {
        showInfo("Chat", "Ouverture du chat...");
        // Implémentez l'ouverture de la fenêtre de chat
    }

    private void openVehicleManagement() {
        showInfo("Véhicules", "Gestion des véhicules...");
        // Implémentez l'ouverture de la gestion des véhicules
    }

    private void openRendezVousManagement() {
        showInfo("Rendez-vous", "Gestion des rendez-vous...");
        // Implémentez l'ouverture de la gestion des RDV
    }

    private void openCustomerManagement() {
        showInfo("Clients", "Gestion des clients...");
        // Implémentez l'ouverture de la gestion des clients
    }

    private void openSalesManagement() {
        showInfo("Ventes", "Gestion des ventes...");
        // Implémentez l'ouverture de la gestion des ventes
    }

    private void openLowStockAlert() {
        showInfo("Stock faible", "Alerte de stock faible...");
        // Implémentez l'ouverture des alertes de stock
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(15);
        emptyState.setStyle("-fx-alignment: center; -fx-padding: 40;");

        Label icon = new Label("🔔");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #ccc;");

        Label message = new Label("Aucune notification");
        message.setStyle("-fx-font-size: 16px; -fx-text-fill: #666; -fx-font-weight: bold;");

        Label subMessage = new Label("Vous serez notifié des nouvelles activités ici");
        subMessage.setStyle("-fx-font-size: 13px; -fx-text-fill: #999;");

        emptyState.getChildren().addAll(icon, message, subMessage);
        notificationsContainer.getChildren().add(emptyState);
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisible(show);
        scrollPane.setVisible(!show);
        if (show) {
            loadingIndicator.setProgress(-1);
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de notifications");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void setupAutoRefresh() {
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> {
                    System.out.println("🔄 Auto-refresh des notifications (vendeur)");
                    loadNotifications();
                })
        );
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void setupEventHandlers() {
        btnToutMarquer.setOnAction(e -> {
            int userId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();
            notificationService.marquerToutesCommeLues(userId);
            loadNotifications();
            System.out.println("✅ Toutes les notifications marquées comme lues (vendeur)");
        });

        btnVoirTout.setOnAction(e -> {
            openNotificationHistory();
        });

        btnParametres.setOnAction(e -> {
            openNotificationSettings();
        });
    }

    private void openNotificationHistory() {
        try {
            String historyPath = "/com/example/vehiclegestion/view/vendeur/NotificationHistory.fxml";
            URL url = getClass().getResource(historyPath);

            if (url != null) {
                Parent historyRoot = FXMLLoader.load(url);
                Stage historyStage = new Stage();
                historyStage.setTitle("Historique des notifications");
                historyStage.setScene(new Scene(historyRoot, 900, 700));
                historyStage.show();
            } else {
                showInfo("Historique", "La vue d'historique n'est pas encore disponible.");
            }
        } catch (Exception e) {
            showError("Erreur ouverture historique: " + e.getMessage());
        }
    }

    private void openNotificationSettings() {
        showInfo("Paramètres", "Les paramètres de notifications seront disponibles prochainement.");
    }

    private void pulseNewNotification() {
        if (newNotificationPulse != null) {
            newNotificationPulse.stop();
        }

        newNotificationPulse = new Timeline(
                new KeyFrame(Duration.millis(0), e -> {
                    lblStatut.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                }),
                new KeyFrame(Duration.millis(500), e -> {
                    lblStatut.setStyle("-fx-text-fill: #3498db; -fx-font-weight: normal;");
                }),
                new KeyFrame(Duration.millis(1000), e -> {
                    lblStatut.setStyle("-fx-text-fill: #333;");
                })
        );
        newNotificationPulse.setCycleCount(3);
        newNotificationPulse.play();
    }

    private void playNotificationSound() {
        System.out.println("🔊 Son de notification joué (vendeur)");
    }

    public void cleanup() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        if (newNotificationPulse != null) {
            newNotificationPulse.stop();
        }

        NotificationManager.getInstance().removeListener(this);
        currentInstance = null;

        System.out.println("🧹 notiController nettoyé");
    }

    // Méthode statique pour rafraîchir depuis l'extérieur
    public static void refreshIfOpen() {
        if (currentInstance != null) {
            Platform.runLater(() -> currentInstance.loadNotifications());
        }
    }

    // Méthode pour vérifier si l'instance est ouverte
    public static boolean isOpen() {
        return currentInstance != null;
    }

    // Méthode pour ouvrir le contrôleur de notifications
    public static void openNotificationWindow() {
        if (isOpen()) {
            System.out.println("⚠️ Fenêtre de notifications déjà ouverte");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    notiController.class.getResource(
                            "/com/example/vehiclegestion/view/vendeur/Notifications.fxml"
                    )
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Notifications - Vendeur");
            stage.setScene(new Scene(root, 450, 600));
            stage.setResizable(false);
            stage.show();

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture fenêtre notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }

}