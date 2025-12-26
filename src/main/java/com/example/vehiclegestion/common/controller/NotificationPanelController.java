package com.example.vehiclegestion.common.controller;

import com.example.vehiclegestion.common.model.Notification;
import com.example.vehiclegestion.common.utils.NotificationService;
import com.example.vehiclegestion.auth.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.util.List;

/**
 * Controller pour le panneau de notifications
 */
public class NotificationPanelController {

    @FXML private VBox notificationsContainer;
    @FXML private Label badgeLabel;
    @FXML private StackPane badgeContainer;
    @FXML private Button markAllReadBtn;
    @FXML private ToggleButton filterAllBtn;
    @FXML private ToggleButton filterUnreadBtn;

    private NotificationService notificationService;
    private int currentUserId;
    private boolean showOnlyUnread = false;

    @FXML
    public void initialize() {
        System.out.println("🔔 Initialisation NotificationPanelController");

        notificationService = new NotificationService();

        // Récupérer l'utilisateur connecté
        var currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getIdUtilisateur();
            System.out.println("✅ User ID: " + currentUserId);

            // Charger les notifications
            loadNotifications();

            // Démarrer le rafraîchissement automatique toutes les 30 secondes
            startAutoRefresh();
        } else {
            System.err.println("❌ Aucun utilisateur connecté");
        }
    }

    /**
     * Charger les notifications
     */
    public void loadNotifications() {
        System.out.println("📥 Chargement notifications pour user: " + currentUserId);

        List<Notification> notifications = notificationService.getNotificationsUtilisateur(
                currentUserId,
                showOnlyUnread
        );

        System.out.println("📊 " + notifications.size() + " notifications trouvées");

        // Mettre à jour le badge
        updateBadge();

        // Afficher les notifications
        displayNotifications(notifications);
    }

    /**
     * Afficher les notifications dans le conteneur
     */
    private void displayNotifications(List<Notification> notifications) {
        Platform.runLater(() -> {
            notificationsContainer.getChildren().clear();

            if (notifications.isEmpty()) {
                // Message "Aucune notification"
                VBox emptyState = createEmptyState();
                notificationsContainer.getChildren().add(emptyState);
            } else {
                // Créer une carte pour chaque notification
                for (Notification notif : notifications) {
                    VBox notifCard = createNotificationCard(notif);
                    notificationsContainer.getChildren().add(notifCard);
                }
            }
        });
    }

    /**
     * Créer une carte de notification
     */
    private VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);

        // Style selon si lue ou non
        String bgColor = notification.isEstLue()
                ? "rgba(15, 23, 42, 0.4)"
                : "rgba(59, 130, 246, 0.1)";

        String borderColor = notification.isEstLue()
                ? "#334155"
                : "#3b82f6";

        card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 12;" +
                        "-fx-cursor: hand;"
        );

        // Ajouter effet hover
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-background-color: rgba(59, 130, 246, 0.15);"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + borderColor + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-padding: 12;" +
                        "-fx-cursor: hand;"
        ));

        // Header: icône + titre + temps
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        // Icône
        Label icon = new Label(notification.getIcon());
        icon.setStyle("-fx-font-size: 18px;");

        // Titre
        Label titre = new Label(notification.getTitre());
        titre.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 13px;"
        );
        titre.setWrapText(true);
        titre.setMaxWidth(250);
        HBox.setHgrow(titre, Priority.ALWAYS);

        // Badge "Non lu"
        if (!notification.isEstLue()) {
            Circle dot = new Circle(4);
            dot.setStyle("-fx-fill: #ef4444;");
            header.getChildren().add(dot);
        }

        header.getChildren().addAll(icon, titre);

        // Message
        Label message = new Label(notification.getMessage());
        message.setStyle(
                "-fx-text-fill: #cbd5e1;" +
                        "-fx-font-size: 12px;"
        );
        message.setWrapText(true);
        message.setMaxWidth(350);

        // Footer: temps + actions
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label time = new Label(notification.getTimeAgo());
        time.setStyle(
                "-fx-text-fill: #64748b;" +
                        "-fx-font-size: 11px;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bouton marquer comme lu/non lu
        Button toggleReadBtn = new Button(notification.isEstLue() ? "👁" : "✓");
        toggleReadBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #94a3b8;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 2 8;"
        );
        toggleReadBtn.setOnAction(e -> {
            if (!notification.isEstLue()) {
                marquerCommeLue(notification.getIdNotification());
            }
        });

        // Bouton supprimer
        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #ef4444;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 2 8;"
        );
        deleteBtn.setOnAction(e -> supprimerNotification(notification.getIdNotification()));

        footer.getChildren().addAll(time, spacer, toggleReadBtn, deleteBtn);

        // Action au clic sur la carte
        card.setOnMouseClicked(e -> {
            if (!notification.isEstLue()) {
                marquerCommeLue(notification.getIdNotification());
            }
            // TODO: Naviguer vers l'action liée
        });

        card.getChildren().addAll(header, message, footer);

        return card;
    }

    /**
     * Créer l'état vide
     */
    private VBox createEmptyState() {
        VBox emptyBox = new VBox(15);
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setPadding(new Insets(40, 20, 40, 20));

        Label icon = new Label("🔔");
        icon.setStyle("-fx-font-size: 48px; -fx-opacity: 0.5;");

        Label text = new Label(showOnlyUnread
                ? "Aucune notification non lue"
                : "Aucune notification");
        text.setStyle(
                "-fx-text-fill: #64748b;" +
                        "-fx-font-size: 14px;"
        );

        emptyBox.getChildren().addAll(icon, text);

        return emptyBox;
    }

    /**
     * Mettre à jour le badge de compteur
     */
    private void updateBadge() {
        int count = notificationService.compterNotificationsNonLues(currentUserId);

        Platform.runLater(() -> {
            if (count > 0) {
                badgeLabel.setText(String.valueOf(Math.min(count, 99)));
                badgeContainer.setVisible(true);
            } else {
                badgeContainer.setVisible(false);
            }

            markAllReadBtn.setVisible(count > 0);
        });
    }

    /**
     * Marquer une notification comme lue
     */
    private void marquerCommeLue(int idNotification) {
        notificationService.marquerCommeLue(idNotification);
        loadNotifications();
    }

    /**
     * Supprimer une notification
     */
    private void supprimerNotification(int idNotification) {
        notificationService.supprimerNotification(idNotification);
        loadNotifications();
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    @FXML
    public void handleMarkAllAsRead() {
        notificationService.marquerToutesCommeLues(currentUserId);
        loadNotifications();
    }

    /**
     * Filtrer: toutes les notifications
     */
    @FXML
    public void handleFilterAll() {
        showOnlyUnread = false;
        loadNotifications();
    }

    /**
     * Filtrer: notifications non lues
     */
    @FXML
    public void handleFilterUnread() {
        showOnlyUnread = true;
        loadNotifications();
    }

    /**
     * Rafraîchir manuellement
     */
    @FXML
    public void handleRefresh() {
        System.out.println("🔄 Rafraîchissement manuel des notifications");
        loadNotifications();
    }

    /**
     * Démarrer le rafraîchissement automatique
     */
    private void startAutoRefresh() {
        Thread refreshThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(30000); // 30 secondes
                    Platform.runLater(this::loadNotifications);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        refreshThread.setDaemon(true);
        refreshThread.start();
    }
}