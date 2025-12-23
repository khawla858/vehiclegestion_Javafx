package com.example.vehiclegestion.vendeur.controller.layout;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.utils.NavigationManager;
import com.example.vehiclegestion.common.dao.ChatDAO;
import com.example.vehiclegestion.common.model.Notification;
import com.example.vehiclegestion.common.utils.NotificationService;
import com.example.vehiclegestion.common.utils.NotificationManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.io.IOException;
import java.util.List;

/**
 * 🎯 NavbarController - Version avec Notifications
 */
public class NavbarController implements NotificationManager.NotificationListener {

    @FXML private TextField searchField;
    @FXML private MenuButton profileMenu;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button dashboardBtn;

    // ✅ Badge messages
    @FXML private Label messageBadge;

    // ✅ Nouveaux éléments pour les notifications
    @FXML private Button notificationBtn;
    @FXML private Label notificationBadge;
    @FXML private BorderPane notificationDropdown;
    @FXML private VBox notificationItemsContainer;
    @FXML private Label lblNotificationStatus;
    @FXML private ScrollPane notificationScrollPane;
    @FXML private Button btnMarkAllRead;
    @FXML private Button btnSeeAll;

    private StackPane contentPane;
    private SessionManager session = SessionManager.getInstance();
    private NavigationManager nav = NavigationManager.getInstance();
    private ChatDAO chatDAO = new ChatDAO();

    // ✅ Service de notifications
    private NotificationService notificationService;
    private Timeline notificationCheckTimeline;
    private Timeline notificationRefreshTimeline;
    private Timeline badgeRefreshTimeline;
    private int lastNotificationCount = 0;
    private boolean notificationDropdownVisible = false;

    /**
     * Injection du contentPane (pour compatibilité)
     */
    public void setContentPane(StackPane contentPane) {
        this.contentPane = contentPane;
        System.out.println("✅ ContentPane injecté dans NavbarController");
    }

    @FXML
    public void initialize() {
        System.out.println("📋 NavbarController vendeur initialisé");

        loadUserInfo();
        setupSearchField();
        playWelcomeAnimation();
        setupHoverEffects();

        // ✅ Initialiser les notifications
        setupNotifications();

        // ✅ Initialiser le badge messages
        updateMessageBadge();
        startBadgeRefresh();
    }

    // ✅ IMPLÉMENTATION DE L'INTERFACE NotificationListener
    @Override
    public void onNewNotification(int userId) {
        System.out.println("🔔 NotificationManager: Nouvelle notification pour user " + userId);

        if (session.estConnecte()) {
            Utilisateur currentUser = session.getUtilisateurConnecte();
            if (currentUser != null && currentUser.getIdUtilisateur() == userId) {
                Platform.runLater(() -> {
                    System.out.println("🔄 Mise à jour UI pour nouvelle notification");
                    updateNotificationBadge();
                    if (notificationDropdownVisible) {
                        loadNotificationDropdown();
                    }
                });
            }
        }
    }

    @Override
    public void onNotificationRead(int notificationId) {
        Platform.runLater(() -> {
            System.out.println("📖 Notification lue: " + notificationId);
            if (notificationDropdownVisible) {
                loadNotificationDropdown();
            }
        });
    }

    private void setupNotifications() {
        notificationService = NotificationService.getInstance();

        // S'abonner aux notifications
        NotificationManager.getInstance().addListener(this);

        // Mettre à jour le badge
        updateNotificationBadge();

        // Vérifier les nouvelles notifications périodiquement
        startNotificationChecker();

        // Rafraîchir la liste toutes les 30 secondes
        startNotificationRefresh();

        // Initialiser les boutons du dropdown
        setupDropdownButtons();

        // Cacher le dropdown au démarrage
        notificationDropdown.setVisible(false);
        notificationDropdown.setManaged(false);

        // Fermer le dropdown en cliquant ailleurs
        setupClickOutsideListener();
    }

    private void setupDropdownButtons() {
        // Bouton "Tout marquer comme lu"
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnAction(e -> markAllNotificationsAsRead());
        }

        // Bouton "Voir tout"
        if (btnSeeAll != null) {
            btnSeeAll.setOnAction(e -> showAllNotificationsPage());
        }
    }

    @FXML
    private void toggleNotificationDropdown() {
        System.out.println("🔔 Toggle dropdown notifications vendeur");

        if (!notificationDropdownVisible) {
            showNotificationDropdown();
        } else {
            hideNotificationDropdown();
        }
    }

    private void showNotificationDropdown() {
        System.out.println("📱 Affichage dropdown notifications vendeur");

        notificationDropdownVisible = true;
        notificationDropdown.setVisible(true);
        notificationDropdown.setManaged(true);

        // Ajuster la taille
        notificationDropdown.setPrefWidth(400);
        notificationDropdown.setPrefHeight(500);

        // Positionner le dropdown
        positionNotificationDropdown();

        // Charger les notifications
        loadNotificationDropdown();

        // Rafraîchir le badge
        updateNotificationBadge();

        // Empêcher la propagation du clic
        notificationDropdown.setOnMouseClicked(e -> e.consume());
    }

    private void hideNotificationDropdown() {
        System.out.println("📱 Masquage dropdown notifications vendeur");

        notificationDropdownVisible = false;
        notificationDropdown.setVisible(false);
        notificationDropdown.setManaged(false);
    }

    private void positionNotificationDropdown() {
        if (notificationBtn != null && notificationDropdown != null) {
            double dropdownX = notificationBtn.localToScene(0, 0).getX() - 150;
            double dropdownY = notificationBtn.localToScene(0, 0).getY() + 65;

            notificationDropdown.setTranslateX(dropdownX);
            notificationDropdown.setTranslateY(dropdownY);
        }
    }

    private void loadNotificationDropdown() {
        if (!session.estConnecte() || notificationService == null) return;

        int userId = session.getUtilisateurConnecte().getIdUtilisateur();
        System.out.println("📋 Chargement notifications vendeur pour user: " + userId);

        List<Notification> notifications = notificationService.getNotificationsUtilisateur(userId, false);

        Platform.runLater(() -> {
            notificationItemsContainer.getChildren().clear();

            if (notifications.isEmpty()) {
                System.out.println("📭 Aucune notification trouvée pour vendeur");
                showEmptyNotificationState();
                lblNotificationStatus.setText("0 notification(s)");
                return;
            }

            System.out.println("✅ " + notifications.size() + " notifications chargées");

            int nonLuesCount = 0;

            for (Notification notif : notifications) {
                System.out.println("   - " + notif.getTitre() + " (lue: " + notif.isEstLue() + ")");
                if (!notif.isEstLue()) {
                    nonLuesCount++;
                }
                addNotificationItemToDropdown(notif);
            }

            lblNotificationStatus.setText(notifications.size() + " notification(s) • " +
                    nonLuesCount + " non lue(s)");
        });
    }

    private void addNotificationItemToDropdown(Notification notification) {
        try {
            HBox notificationItem = new HBox(10);
            notificationItem.setStyle("-fx-padding: 12 15; " +
                    "-fx-border-width: 0 0 1 0; " +
                    "-fx-border-color: #f0f0f0; " +
                    "-fx-background-color: " +
                    (notification.isEstLue() ? "white" : "#f8fbff") + ";");

            // Icône
            Label iconLabel = new Label(notification.getIcon());
            iconLabel.setStyle("-fx-font-size: 18px; -fx-padding: 0 10 0 0;");

            // Contenu
            VBox contentBox = new VBox(3);
            contentBox.setMaxWidth(250);

            // Titre et temps
            HBox headerBox = new HBox(5);
            Label titleLabel = new Label(notification.getTitre());
            titleLabel.setStyle("-fx-font-weight: bold; " +
                    "-fx-text-fill: #1a1a1a; " +
                    "-fx-font-size: 13px;");
            titleLabel.setWrapText(true);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label timeLabel = new Label(notification.getTimeAgo());
            timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

            headerBox.getChildren().addAll(titleLabel, spacer, timeLabel);

            // Message
            Label messageLabel = new Label(notification.getMessage());
            messageLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
            messageLabel.setWrapText(true);

            // Catégorie
            HBox footerBox = new HBox(5);
            Label categoryLabel = new Label(notification.getCategorie());
            categoryLabel.setStyle("-fx-background-color: #e8f4fd; " +
                    "-fx-text-fill: #3498db; " +
                    "-fx-padding: 2 8; " +
                    "-fx-font-size: 10px; " +
                    "-fx-background-radius: 10;");

            Region footerSpacer = new Region();
            HBox.setHgrow(footerSpacer, Priority.ALWAYS);

            // Indicateur "non lu"
            if (!notification.isEstLue()) {
                Label unreadDot = new Label("●");
                unreadDot.setStyle("-fx-text-fill: #3498db; -fx-font-size: 8px;");
                footerBox.getChildren().add(unreadDot);
            }

            footerBox.getChildren().addAll(categoryLabel, footerSpacer);

            contentBox.getChildren().addAll(headerBox, messageLabel, footerBox);

            notificationItem.getChildren().addAll(iconLabel, contentBox);

            // Style pour les priorités
            if ("urgente".equals(notification.getPriorite())) {
                notificationItem.setStyle(notificationItem.getStyle() +
                        "-fx-border-left-color: #dc3545; -fx-border-left-width: 3;");
            } else if ("haute".equals(notification.getPriorite())) {
                notificationItem.setStyle(notificationItem.getStyle() +
                        "-fx-border-left-color: #ffc107; -fx-border-left-width: 3;");
            }

            // Gestion du clic
            notificationItem.setOnMouseClicked(e -> {
                System.out.println("📱 Notification cliquée: " + notification.getTitre());

                // Marquer comme lue
                if (!notification.isEstLue()) {
                    notificationService.marquerCommeLue(notification.getIdNotification());

                    // Mettre à jour l'UI
                    notificationItem.setStyle(notificationItem.getStyle().replace("#f8fbff", "white"));
                    loadNotificationDropdown();
                    updateNotificationBadge();
                }

                // Fermer le dropdown
                hideNotificationDropdown();

                // Naviguer selon le type
                handleNotificationNavigation(notification);

                e.consume();
            });

            // Effet hover
            notificationItem.setOnMouseEntered(e -> {
                notificationItem.setStyle(notificationItem.getStyle() +
                        "-fx-background-color: #f5f9ff; -fx-cursor: hand;");
            });

            notificationItem.setOnMouseExited(e -> {
                String baseStyle = "-fx-padding: 12 15; -fx-border-width: 0 0 1 0; " +
                        "-fx-border-color: #f0f0f0; " +
                        "-fx-background-color: " +
                        (notification.isEstLue() ? "white" : "#f8fbff") + ";";

                if ("urgente".equals(notification.getPriorite())) {
                    baseStyle += "-fx-border-left-color: #dc3545; -fx-border-left-width: 3;";
                } else if ("haute".equals(notification.getPriorite())) {
                    baseStyle += "-fx-border-left-color: #ffc107; -fx-border-left-width: 3;";
                }

                notificationItem.setStyle(baseStyle);
            });

            notificationItemsContainer.getChildren().add(notificationItem);

        } catch (Exception e) {
            System.err.println("❌ Erreur création item notification vendeur: " + e.getMessage());
        }
    }

    private void showEmptyNotificationState() {
        VBox emptyState = new VBox(15);
        emptyState.setStyle("-fx-alignment: center; -fx-padding: 40 20;");

        Label icon = new Label("🔔");
        icon.setStyle("-fx-font-size: 40px;");

        Label message = new Label("Aucune notification");
        message.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");

        Label subMessage = new Label("Vous serez notifié ici des nouvelles activités");
        subMessage.setStyle("-fx-font-size: 12px; -fx-text-fill: #999; -fx-alignment: center;");
        subMessage.setWrapText(true);

        emptyState.getChildren().addAll(icon, message, subMessage);
        notificationItemsContainer.getChildren().add(emptyState);
    }

    private void handleNotificationNavigation(Notification notification) {
        String lien = notification.getLienAction();
        String type = notification.getTypeNotification();

        System.out.println("🎯 Navigation depuis notification vendeur: " + type);

        if (lien != null) {
            if (lien.contains("message") || lien.startsWith("/messages")) {
                showMessages();
            } else if (lien.contains("vehicule") || lien.startsWith("/vehicules")) {
                showVehicleManagement();
            } else if (lien.contains("client") || lien.startsWith("/clients")) {
                showCustomerList();
            } else if (lien.contains("reservation") || lien.startsWith("/reservations")) {
                showPendingReservations();
            } else if (lien.contains("rendezvous")) {
                showRendezVous();
            }
        }
    }

    private void setupClickOutsideListener() {
        // Au lieu d'utiliser getScene() immédiatement, attendre que la scène soit disponible
        if (notificationBtn != null && notificationBtn.getScene() != null) {
            notificationBtn.getScene().setOnMouseClicked(e -> {
                if (notificationDropdownVisible) {
                    if (!notificationDropdown.getBoundsInParent().contains(e.getX(), e.getY()) &&
                            !notificationBtn.getBoundsInParent().contains(e.getX(), e.getY())) {
                        hideNotificationDropdown();
                    }
                }
            });
        } else {
            // Si la scène n'est pas encore disponible, on écoute l'événement plus tard
            notificationBtn.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.setOnMouseClicked(e -> {
                        if (notificationDropdownVisible) {
                            if (!notificationDropdown.getBoundsInParent().contains(e.getX(), e.getY()) &&
                                    !notificationBtn.getBoundsInParent().contains(e.getX(), e.getY())) {
                                hideNotificationDropdown();
                            }
                        }
                    });
                }
            });
        }
    }

    private void markAllNotificationsAsRead() {
        if (!session.estConnecte() || notificationService == null) return;

        int userId = session.getUtilisateurConnecte().getIdUtilisateur();
        notificationService.marquerToutesCommeLues(userId);

        // Rafraîchir l'UI
        loadNotificationDropdown();
        updateNotificationBadge();

        System.out.println("✅ Toutes les notifications marquées comme lues");
    }

    private void showAllNotificationsPage() {
        System.out.println("📋 Navigation vers page notifications complète");

        hideNotificationDropdown();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications complètes");
        alert.setHeaderText("Page des notifications");
        alert.setContentText("Cette fonctionnalité sera implémentée prochainement.");
        alert.showAndWait();
    }

    private void startNotificationChecker() {
        notificationCheckTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> updateNotificationBadge())
        );
        notificationCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        notificationCheckTimeline.play();
    }

    private void startNotificationRefresh() {
        notificationRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> {
                    if (notificationDropdownVisible) {
                        loadNotificationDropdown();
                    }
                })
        );
        notificationRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        notificationRefreshTimeline.play();
    }

    private void updateNotificationBadge() {
        if (!session.estConnecte() || notificationService == null) return;

        Platform.runLater(() -> {
            try {
                int userId = session.getUtilisateurConnecte().getIdUtilisateur();
                int count = notificationService.getNombreNotificationsNonLues(userId);

                System.out.println("📊 Mise à jour badge vendeur: " + count + " notifications non lues");

                if (notificationBadge != null) {
                    if (count > 0) {
                        notificationBadge.setText(String.valueOf(count > 99 ? "99+" : count));
                        notificationBadge.setVisible(true);

                        // Animation pour nouvelles notifications
                        if (count > lastNotificationCount && lastNotificationCount > 0) {
                            animateNotificationBadge();
                        }
                    } else {
                        notificationBadge.setVisible(false);
                    }
                }

                lastNotificationCount = count;

            } catch (Exception e) {
                System.err.println("❌ Erreur mise à jour badge vendeur: " + e.getMessage());
            }
        });
    }

    private void animateNotificationBadge() {
        if (notificationBadge == null) return;

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.millis(0), e -> {
                    notificationBadge.setScaleX(1.0);
                    notificationBadge.setScaleY(1.0);
                }),
                new KeyFrame(Duration.millis(150), e -> {
                    notificationBadge.setScaleX(1.3);
                    notificationBadge.setScaleY(1.3);
                }),
                new KeyFrame(Duration.millis(300), e -> {
                    notificationBadge.setScaleX(1.0);
                    notificationBadge.setScaleY(1.0);
                })
        );
        pulse.setCycleCount(2);
        pulse.play();
    }

    private void loadUserInfo() {
        if (session.estConnecte()) {
            Utilisateur user = session.getUtilisateurConnecte();
            if (userNameLabel != null) {
                userNameLabel.setText(user.getNom() + " " + user.getPrenom());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(user.getRole());
            }
            System.out.println("✅ Infos utilisateur chargées: " + user.getNom());
        }
    }

    private void setupSearchField() {
        if (searchField != null) {
            searchField.setOnAction(event -> performSearch());
        }
    }

    private void setupHoverEffects() {
        if (dashboardBtn != null) {
            dashboardBtn.setOnMouseEntered(e ->
                    dashboardBtn.setStyle(dashboardBtn.getStyle() + "-fx-background-color: #ecf0f1;")
            );
            dashboardBtn.setOnMouseExited(e ->
                    dashboardBtn.setStyle(dashboardBtn.getStyle().replace("-fx-background-color: #ecf0f1;", ""))
            );
        }
    }

    private void playWelcomeAnimation() {
        if (userNameLabel != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(800), userNameLabel);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);

            ScaleTransition scale = new ScaleTransition(Duration.millis(600), userNameLabel);
            scale.setFromX(0.9);
            scale.setFromY(0.9);
            scale.setToX(1.0);
            scale.setToY(1.0);

            fade.play();
            scale.play();
        }
    }

    // ========================================
    // 💬 GESTION DU BADGE MESSAGES
    // ========================================

    private void updateMessageBadge() {
        if (!session.estConnecte()) return;

        try {
            int unreadCount = chatDAO.countUnreadMessages(
                    session.getUserId(),
                    session.getUserRole()
            );

            if (messageBadge != null) {
                if (unreadCount > 0) {
                    messageBadge.setText(String.valueOf(unreadCount));
                    messageBadge.setVisible(true);
                    System.out.println("📬 " + unreadCount + " messages non lus");
                } else {
                    messageBadge.setVisible(false);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour badge messages: " + e.getMessage());
        }
    }

    private void startBadgeRefresh() {
        badgeRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> updateMessageBadge())
        );
        badgeRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        badgeRefreshTimeline.play();
        System.out.println("🔄 Auto-refresh badge messages démarré");
    }

    // ========================================
    // 🚀 NAVIGATION
    // ========================================

    @FXML
    private void showDashboard() {
        System.out.println("📊 Navigation: Dashboard");
        nav.goToDashboard();
    }

    @FXML
    private void showCustomerList() {
        System.out.println("👥 Navigation: Liste clients");
        nav.goToClients();
    }

    @FXML
    private void showVehicleManagement() {
        System.out.println("🚗 Navigation: Gestion véhicules");
        nav.goToVehicles();
    }

    @FXML
    private void showPendingReservations() {
        System.out.println("🛒 Navigation: Réservations");
        nav.goToReservations();
    }

    @FXML
    private void showRendezVous() {
        System.out.println("📅 Navigation: Rendez-vous");
        nav.goToRendezVous();
    }

    @FXML
    private void showMagasins() {
        System.out.println("🏪 Navigation: Magasins");
        nav.goToMagasins();
    }

    @FXML
    private void ouvrirDetailMagasin() {
        System.out.println("🏪 Navigation: Détails magasin");
        nav.goToMagasinDetails();
    }

    // ========================================
    // 🔍 RECHERCHE
    // ========================================

    @FXML
    private void performSearch() {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            System.out.println("🔍 Recherche: " + searchText);
            showAlert("Recherche",
                    "Recherche pour: " + searchText + "\n\n" +
                            "Cette fonctionnalité permettra de chercher parmi:\n" +
                            "• Les véhicules\n" +
                            "• Les clients\n" +
                            "• Les commandes\n" +
                            "• Les rendez-vous",
                    Alert.AlertType.INFORMATION);
        }
    }

    // ========================================
    // 🔔 NOTIFICATIONS & MESSAGES
    // ========================================

    @FXML
    private void showNotifications() {
        // Cette méthode est maintenant gérée par toggleNotificationDropdown
        toggleNotificationDropdown();
    }

    @FXML
    private void showCart() {
        System.out.println("🛒 Panier");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Panier");
        alert.setHeaderText("Votre panier de commandes");
        alert.setContentText(
                "🚗 BMW X5 - En cours de réservation\n" +
                        "🚗 Audi A4 - Option client Dupont\n\n" +
                        "Total: 2 véhicules"
        );
        styleAlert(alert);
        alert.show();
    }

    @FXML
    private void showMessages() {
        System.out.println("💬 Navigation vers la page de chat");

        // ✅ Utiliser NavigationManager pour naviguer vers la page de chat
        // Cela affichera le chat dans la zone de contenu principale
        nav.showMessages();

        // Mettre à jour le badge après navigation
        updateMessageBadge();
    }
    // ========================================
    // 👤 PROFIL UTILISATEUR
    // ========================================

    @FXML
    private void showUserProfile() {
        System.out.println("👤 Profil utilisateur");
        showAlert("Profil Utilisateur",
                "Accès au profil utilisateur\n\n" +
                        "Vous pourrez modifier:\n" +
                        "• Informations personnelles\n" +
                        "• Photo de profil\n" +
                        "• Coordonnées\n" +
                        "• Préférences",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void showAccountSettings() {
        System.out.println("⚙️ Paramètres compte");
        showAlert("Paramètres du Compte",
                "Configuration du compte\n\n" +
                        "Options disponibles:\n" +
                        "• Changer le mot de passe\n" +
                        "• Notifications\n" +
                        "• Sécurité\n" +
                        "• Confidentialité",
                Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleLogout() {
        System.out.println("🚪 Déconnexion demandée");

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Déconnexion");
        confirmation.setHeaderText("Êtes-vous sûr de vouloir vous déconnecter ?");
        confirmation.setContentText("Vous serez redirigé vers la page de connexion.");
        styleAlert(confirmation);

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cleanup();
                session.fermerSession();
                System.out.println("✅ Session fermée");

                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Déconnexion");
                success.setHeaderText("Déconnexion réussie !");
                success.setContentText("À bientôt sur AutoSales Pro 👋");
                styleAlert(success);
                success.show();
            }
        });
    }

    public void cleanup() {
        // Se désabonner des notifications
        NotificationManager.getInstance().removeListener(this);

        // Arrêter les timelines
        stopBadgeRefresh();

        if (notificationCheckTimeline != null) {
            notificationCheckTimeline.stop();
        }
        if (notificationRefreshTimeline != null) {
            notificationRefreshTimeline.stop();
        }
    }

    public void stopBadgeRefresh() {
        if (badgeRefreshTimeline != null) {
            badgeRefreshTimeline.stop();
        }
    }

    // ========================================
    // 🛠️ UTILITAIRES
    // ========================================

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.show();
    }

    private void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-background-color: white; " +
                        "-fx-font-family: 'Segoe UI', 'Arial', sans-serif; " +
                        "-fx-font-size: 13px; " +
                        "-fx-padding: 20;"
        );

        dialogPane.lookup(".header-panel").setStyle(
                "-fx-background-color: #f8f9fa; " +
                        "-fx-padding: 15;"
        );
    }



}