package com.example.vehiclegestion.client.controller;

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
import  com.example.vehiclegestion.auth.utils.SessionManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class NotificationController implements Initializable, NotificationManager.NotificationListener {

    @FXML private VBox notificationsContainer;
    @FXML private Label lblStatut;
    @FXML private Button btnToutMarquer;
    @FXML private ToggleButton toggleToutes, toggleNonLues, toggleUrgentes;
    @FXML private Button btnVoirTout;
    @FXML private Button btnParametres;
    @FXML private ScrollPane scrollPane;
    @FXML private ProgressIndicator loadingIndicator;

    @FXML private HBox notificationTemplate;
    @FXML private Label templateIcon, templateTitre, templateMessage, templateTemps, templateCategorie, templateNonLue;
    @FXML private Button templateAction;

    private NotificationService notificationService;
    private SessionManager sessionManager;
    private Timeline refreshTimeline;
    private Timeline newNotificationPulse;
    private int currentFilter = 0; // 0=toutes, 1=non lues, 2=urgentes
    private boolean isLoading = false;

    // Instance statique pour accès depuis l'extérieur
    private static NotificationController currentInstance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🔔 NotificationController initialisé");

        currentInstance = this;

        notificationService = NotificationService.getInstance();
        sessionManager = SessionManager.getInstance();

        if (!sessionManager.estConnecte()) {
            showError("Utilisateur non connecté");
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
        // S'enregistrer auprès du NotificationManager
        NotificationManager.getInstance().addListener(this);
        System.out.println("👂 NotificationController enregistré comme listener");
    }

    @Override
    public void onNewNotification(int userId) {
        System.out.println("📬 onNewNotification reçu pour user: " + userId);

        int currentUserId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();
        if (userId == currentUserId) {
            Platform.runLater(() -> {
                // Rafraîchir les notifications
                loadNotifications();

                // Animation de nouvelle notification
                pulseNewNotification();

                // Son ou vibration (optionnel)
                playNotificationSound();
            });
        }
    }

    @Override
    public void onNotificationRead(int notificationId) {
        // Réagir si une notification a été marquée comme lue
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

        // Style pour les boutons
        updateToggleStyles();

        // Mettre à jour les styles quand la sélection change
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

        // Style du scrollpane
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
        System.out.println("🔄 Chargement notifications pour user: " + userId);

        new Thread(() -> {
            try {
                List<Notification> notifications = getFilteredNotifications(userId);

                Platform.runLater(() -> {
                    notificationsContainer.getChildren().clear();

                    if (notifications.isEmpty()) {
                        showEmptyState();
                        lblStatut.setText("0 notification(s)");
                    } else {
                        int nonLuesCount = 0;
                        for (Notification notif : notifications) {
                            if (!notif.isEstLue()) {
                                nonLuesCount++;
                            }
                            addNotificationItem(notif);
                        }
                        lblStatut.setText(notifications.size() + " notification(s) • " + nonLuesCount + " non lue(s)");
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

        switch (currentFilter) {
            case 1: // Non lues seulement
                allNotifications.removeIf(Notification::isEstLue);
                break;
            case 2: // Urgentes seulement
                allNotifications.removeIf(n -> !"urgente".equals(n.getPriorite()));
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
            notificationItem.setStyle("-fx-padding: 12; -fx-background-radius: 8; "
                    + "-fx-background-color: white; -fx-border-color: #e0e0e0; "
                    + "-fx-border-radius: 8; -fx-border-width: 1;");

            // Marqueur "non lue"
            if (!notification.isEstLue()) {
                notificationItem.setStyle(notificationItem.getStyle()
                        + "-fx-border-color: #3498db; -fx-border-width: 2;");
            }

            // Icône
            Label iconLabel = new Label(notification.getIcon());
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

            // Message
            Label messageLabel = new Label(notification.getMessage());
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
            if ("urgente".equals(notification.getPriorite())) {
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
                if (!notification.isEstLue()) {
                    notificationService.marquerCommeLue(notification.getIdNotification());
                    notificationItem.setStyle(notificationItem.getStyle().replace(
                            "-fx-border-color: #3498db; -fx-border-width: 2;",
                            "-fx-border-color: #e0e0e0; -fx-border-width: 1;"
                    ));
                }

                if (e.getClickCount() == 2) {
                    handleNotificationClick(notification);
                }
            });

            // Effet hover
            notificationItem.setOnMouseEntered(e -> {
                notificationItem.setStyle(notificationItem.getStyle()
                        + "-fx-background-color: #f9f9f9;");
            });

            notificationItem.setOnMouseExited(e -> {
                notificationItem.setStyle(notificationItem.getStyle().replace(
                        "-fx-background-color: #f9f9f9;",
                        "-fx-background-color: white;"
                ));
            });

            notificationsContainer.getChildren().add(notificationItem);

        } catch (Exception e) {
            System.err.println("Erreur création item notification: " + e.getMessage());
        }
    }

    private void handleNotificationClick(Notification notification) {
        System.out.println("📱 Notification cliquée: " + notification.getTitre());

        // Marquer comme lue si ce n'est pas déjà fait
        if (!notification.isEstLue()) {
            notificationService.marquerCommeLue(notification.getIdNotification());
        }

        // Naviguer selon le lien
        navigateTo(notification.getLienAction());
    }

    private void navigateTo(String lien) {
        System.out.println("🔗 Navigation vers: " + lien);

        // Fermer la fenêtre de notifications
        Stage currentStage = (Stage) btnToutMarquer.getScene().getWindow();
        currentStage.close();

        if (lien == null || lien.isEmpty()) return;

        try {
            // Navigation simplifiée - ajustez selon vos routes
            if (lien.contains("/messages") || "message".equals(lien)) {
                openChatWindow();
            } else if (lien.contains("/vehicules/")) {
                openVehicleDetails(lien);
            } else if (lien.contains("/reservations/")) {
                openReservationDetails(lien);
            } else if (lien.contains("/rendezvous/")) {
                openRendezVousDetails(lien);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation: " + e.getMessage());
        }
    }

    private void openChatWindow() {
        try {
            // Essayez différents chemins FXML
            String[] possiblePaths = {
                    "/com/example/vehiclegestion/view/client/ChatView.fxml",
                    "/com/example/vehiclegestion/view/common/ChatWindow.fxml",
                    "/view/client/ChatView.fxml",
                    "/view/common/ChatWindow.fxml"
            };

            Parent chatRoot = null;
            for (String path : possiblePaths) {
                try {
                    URL url = getClass().getResource(path);
                    if (url != null) {
                        chatRoot = FXMLLoader.load(url);
                        break;
                    }
                } catch (Exception e) {
                    // Continuer avec le chemin suivant
                }
            }

            if (chatRoot != null) {
                Stage chatStage = new Stage();
                chatStage.setTitle("Messages");
                chatStage.setScene(new Scene(chatRoot, 800, 600));
                chatStage.show();
            } else {
                showInfo("Chat", "L'interface de chat n'est pas disponible pour le moment.");
            }
        } catch (Exception e) {
            showError("Impossible d'ouvrir le chat: " + e.getMessage());
        }
    }

    private void openVehicleDetails(String lien) {
        showInfo("Détails véhicule", "Redirection vers: " + lien);
        // Implémentez votre logique d'ouverture de détails véhicule
    }

    private void openReservationDetails(String lien) {
        showInfo("Détails réservation", "Redirection vers: " + lien);
        // Implémentez votre logique d'ouverture de détails réservation
    }

    private void openRendezVousDetails(String lien) {
        showInfo("Détails rendez-vous", "Redirection vers: " + lien);
        // Implémentez votre logique d'ouverture de détails RDV
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
            loadingIndicator.setProgress(-1); // Indéterminé
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

    private void setupAutoRefresh() {
        // Rafraîchissement toutes les 10 secondes
        refreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> {
                    System.out.println("🔄 Auto-refresh des notifications");
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
            System.out.println("✅ Toutes les notifications marquées comme lues");
        });

        btnVoirTout.setOnAction(e -> {
            // Ouvrir l'historique complet
            openNotificationHistory();
        });

        btnParametres.setOnAction(e -> {
            // Ouvrir les paramètres
            openNotificationSettings();
        });
    }

    private void openNotificationHistory() {
        try {
            // Chargez la vue d'historique si elle existe
            String historyPath = "/com/example/vehiclegestion/view/client/NotificationHistory.fxml";
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
        // Animation pour nouvelle notification
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
        // Optionnel: Jouer un son (simulé ici avec un print)
        System.out.println("🔊 Son de notification joué");
    }

    public void cleanup() {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        if (newNotificationPulse != null) {
            newNotificationPulse.stop();
        }

        // Se désenregistrer du NotificationManager
        NotificationManager.getInstance().removeListener(this);
        currentInstance = null;

        System.out.println("🧹 NotificationController nettoyé");
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
}