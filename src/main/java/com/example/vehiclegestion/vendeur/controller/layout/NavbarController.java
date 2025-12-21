package com.example.vehiclegestion.vendeur.controller.layout;

import com.example.vehiclegestion.auth.utils.SessionManager;
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
import javafx.stage.Popup;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.geometry.Bounds;
import java.io.IOException;
import java.util.List;

/**
 * 🎯 NavbarController - Version CORRIGÉE avec Notifications
 */
public class NavbarController implements NotificationManager.NotificationListener {

    @FXML private TextField searchField;
    @FXML private MenuButton profileMenu;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Button dashboardBtn;
    @FXML private Label messageBadge;

    // ✅ Éléments notifications
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

    // 🆕 Popup pour afficher le dropdown
    private Popup notificationPopup;

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

        // 🆕 Créer le popup pour le dropdown
        createNotificationPopup();
    }

    // ================================
    // 🆕 CRÉATION DU POPUP
    // ================================

    private void createNotificationPopup() {
        notificationPopup = new Popup();
        notificationPopup.setAutoHide(true);
        notificationPopup.setHideOnEscape(true);

        // Le dropdown sera ajouté au popup quand on l'affichera
        System.out.println("✅ Popup notification créé");
    }

    // ================================
    // 🔔 GESTION DES NOTIFICATIONS
    // ================================

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
            updateNotificationBadge();
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
        if (notificationDropdown != null) {
            notificationDropdown.setVisible(false);
            notificationDropdown.setManaged(false);
        }
    }

    private void setupDropdownButtons() {
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnAction(e -> markAllNotificationsAsRead());
        }

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

        // 🆕 CORRECTION : Afficher via Popup pour ne pas écraser le contenu
        if (notificationBtn != null && notificationDropdown != null) {

            // Retirer le dropdown du parent s'il existe
            if (notificationDropdown.getParent() != null) {
                ((Pane) notificationDropdown.getParent()).getChildren().remove(notificationDropdown);
            }

            // Réinitialiser les propriétés
            notificationDropdown.setVisible(true);
            notificationDropdown.setManaged(true);
            notificationDropdown.setPrefWidth(400);
            notificationDropdown.setMaxWidth(400);
            notificationDropdown.setPrefHeight(500);
            notificationDropdown.setMaxHeight(500);

            // Style avec ombre
            notificationDropdown.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #e0e0e0;" +
                            "-fx-border-width: 1;" +
                            "-fx-background-radius: 8;" +
                            "-fx-border-radius: 8;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);"
            );

            // Ajouter au popup
            notificationPopup.getContent().clear();
            notificationPopup.getContent().add(notificationDropdown);

            // Calculer la position sous le bouton
            Bounds bounds = notificationBtn.localToScreen(notificationBtn.getBoundsInLocal());

            // Position : aligné à droite du bouton, juste en dessous
            double x = bounds.getMaxX() - 400; // 400 = largeur du dropdown
            double y = bounds.getMaxY() + 5;   // 5px d'espace

            // Afficher le popup
            notificationPopup.show(notificationBtn, x, y);

            System.out.println("📍 Popup affiché à x=" + x + ", y=" + y);

            // Charger les notifications
            loadNotificationDropdown();

            // Rafraîchir le badge
            updateNotificationBadge();
        }
    }

    private void hideNotificationDropdown() {
        System.out.println("📱 Masquage dropdown notifications vendeur");

        notificationDropdownVisible = false;

        if (notificationPopup != null) {
            notificationPopup.hide();
        }
    }

    private void loadNotificationDropdown() {
        if (!session.estConnecte() || notificationService == null) {
            System.out.println("⚠️ Session non connectée ou service null");
            return;
        }

        int userId = session.getUtilisateurConnecte().getIdUtilisateur();
        String userRole = session.getUtilisateurConnecte().getRole();

        System.out.println("📋 Chargement notifications vendeur:");
        System.out.println("   User ID: " + userId);
        System.out.println("   Rôle: " + userRole);

        // 🆕 CORRECTION : Charger TOUTES les notifications (pas seulement non lues)
        List<Notification> allNotifications = notificationService.getNotificationsUtilisateur(userId, false);

        System.out.println("📊 Notifications brutes reçues: " +
                (allNotifications != null ? allNotifications.size() : "null"));

        Platform.runLater(() -> {
            notificationItemsContainer.getChildren().clear();

            if (allNotifications == null || allNotifications.isEmpty()) {
                System.out.println("📭 Aucune notification trouvée");
                showEmptyNotificationState();
                lblNotificationStatus.setText("0 notification(s)");
                return;
            }

            // 🆕 FILTRER par rôle si nécessaire
            List<Notification> notifications = allNotifications.stream()
                    .filter(n -> {
                        boolean matches = n.getRoleDestinataire() == null ||
                                n.getRoleDestinataire().equalsIgnoreCase(userRole);
                        System.out.println("   - Notif: " + n.getTitre() +
                                " | Rôle: " + n.getRoleDestinataire() +
                                " | Match: " + matches);
                        return matches;
                    })
                    .toList();

            System.out.println("✅ " + notifications.size() + " notifications après filtrage");

            if (notifications.isEmpty()) {
                showEmptyNotificationState();
                lblNotificationStatus.setText("0 notification(s)");
                return;
            }

            int nonLuesCount = 0;
            for (Notification notif : notifications) {
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
            notificationItem.setStyle(
                    "-fx-padding: 12 15; " +
                            "-fx-border-width: 0 0 1 0; " +
                            "-fx-border-color: #f0f0f0; " +
                            "-fx-background-color: " + (notification.isEstLue() ? "white" : "#f0f8ff") + ";"
            );

            // Icône
            Label iconLabel = new Label(getNotificationIcon(notification));
            iconLabel.setStyle("-fx-font-size: 18px; -fx-padding: 0 10 0 0;");

            // Contenu
            VBox contentBox = new VBox(4);
            contentBox.setMaxWidth(280);

            // Titre et temps
            HBox headerBox = new HBox(5);
            Label titleLabel = new Label(notification.getTitre());
            titleLabel.setStyle(
                    "-fx-font-weight: bold; " +
                            "-fx-text-fill: #1a1a1a; " +
                            "-fx-font-size: 13px;"
            );
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(200);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label timeLabel = new Label(notification.getTimeAgo());
            timeLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");

            headerBox.getChildren().addAll(titleLabel, spacer, timeLabel);

            // Message
            Label messageLabel = new Label(notification.getMessage());
            messageLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(280);

            // Pied de page
            HBox footerBox = new HBox(5);
            Label categoryLabel = new Label(notification.getCategorie().toUpperCase());
            categoryLabel.setStyle(
                    "-fx-background-color: #e8f4fd; " +
                            "-fx-text-fill: #3498db; " +
                            "-fx-padding: 2 8; " +
                            "-fx-font-size: 10px; " +
                            "-fx-background-radius: 10;"
            );

            // Priorité
            if ("urgente".equalsIgnoreCase(notification.getPriorite())) {
                Label prioriteLabel = new Label("URGENT");
                prioriteLabel.setStyle(
                        "-fx-background-color: #ffebee; " +
                                "-fx-text-fill: #e74c3c; " +
                                "-fx-padding: 2 8; " +
                                "-fx-font-size: 10px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-background-radius: 10;"
                );
                footerBox.getChildren().add(prioriteLabel);
            }

            // Indicateur non lu
            if (!notification.isEstLue()) {
                Label unreadDot = new Label("●");
                unreadDot.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px;");
                footerBox.getChildren().add(unreadDot);
            }

            Region footerSpacer = new Region();
            HBox.setHgrow(footerSpacer, Priority.ALWAYS);

            footerBox.getChildren().addAll(categoryLabel, footerSpacer);

            contentBox.getChildren().addAll(headerBox, messageLabel, footerBox);
            notificationItem.getChildren().addAll(iconLabel, contentBox);

            // Clic sur la notification
            notificationItem.setOnMouseClicked(e -> {
                System.out.println("📱 Notification cliquée: " + notification.getTitre());

                // Marquer comme lue
                if (!notification.isEstLue()) {
                    notificationService.marquerCommeLue(notification.getIdNotification());
                    loadNotificationDropdown();
                    updateNotificationBadge();
                }

                // Fermer le dropdown
                hideNotificationDropdown();

                // Naviguer
                handleNotificationNavigation(notification);

                e.consume();
            });

            // Effet hover
            notificationItem.setOnMouseEntered(e -> {
                notificationItem.setStyle(
                        notificationItem.getStyle() +
                                "-fx-background-color: #f5f9ff; -fx-cursor: hand;"
                );
            });

            notificationItem.setOnMouseExited(e -> {
                notificationItem.setStyle(
                        "-fx-padding: 12 15; " +
                                "-fx-border-width: 0 0 1 0; " +
                                "-fx-border-color: #f0f0f0; " +
                                "-fx-background-color: " + (notification.isEstLue() ? "white" : "#f0f8ff") + ";"
                );
            });

            notificationItemsContainer.getChildren().add(notificationItem);

        } catch (Exception e) {
            System.err.println("❌ Erreur création item notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getNotificationIcon(Notification notification) {
        String categorie = notification.getCategorie().toLowerCase();
        switch (categorie) {
            case "message": return "💬";
            case "transaction": return "💰";
            case "rendezvous": return "📅";
            case "vehicule": return "🚗";
            case "client": return "👤";
            case "alerte": return "⚠️";
            case "systeme": return "⚙️";
            default: return "🔔";
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
        subMessage.setStyle("-fx-font-size: 12px; -fx-text-fill: #999;");
        subMessage.setWrapText(true);

        emptyState.getChildren().addAll(icon, message, subMessage);
        notificationItemsContainer.getChildren().add(emptyState);
    }

    private void handleNotificationNavigation(Notification notification) {
        String lien = notification.getLienAction();
        String type = notification.getTypeNotification();

        System.out.println("🎯 Navigation: " + type);

        if (lien != null && !lien.isEmpty()) {
            if (lien.contains("message")) showMessages();
            else if (lien.contains("vehicule")) showVehicleManagement();
            else if (lien.contains("client")) showCustomerList();
            else if (lien.contains("reservation")) showPendingReservations();
            else if (lien.contains("rendezvous")) showRendezVous();
        }
    }

    private void markAllNotificationsAsRead() {
        if (!session.estConnecte()) return;

        int userId = session.getUtilisateurConnecte().getIdUtilisateur();
        notificationService.marquerToutesCommeLues(userId);

        loadNotificationDropdown();
        updateNotificationBadge();

        System.out.println("✅ Toutes les notifications marquées comme lues");
    }

    private void showAllNotificationsPage() {
        System.out.println("📋 Page notifications complète");
        hideNotificationDropdown();

        // Ici vous pouvez ouvrir une fenêtre dédiée aux notifications
        showAlert("Notifications",
                "Page complète des notifications en développement",
                Alert.AlertType.INFORMATION);
    }

    private void startNotificationChecker() {
        notificationCheckTimeline = new Timeline(
                new KeyFrame(Duration.seconds(15), e -> {
                    System.out.println("🔄 Vérification notifications...");
                    updateNotificationBadge();
                })
        );
        notificationCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        notificationCheckTimeline.play();
    }

    private void startNotificationRefresh() {
        notificationRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> {
                    if (notificationDropdownVisible) {
                        System.out.println("🔄 Rafraîchissement dropdown...");
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

                System.out.println("📊 Badge: " + count + " notifications non lues");

                if (notificationBadge != null) {
                    if (count > 0) {
                        notificationBadge.setText(count > 99 ? "99+" : String.valueOf(count));
                        notificationBadge.setVisible(true);

                        if (count > lastNotificationCount) {
                            animateNotificationBadge();
                        }
                    } else {
                        notificationBadge.setVisible(false);
                    }
                }

                lastNotificationCount = count;

            } catch (Exception e) {
                System.err.println("❌ Erreur badge: " + e.getMessage());
            }
        });
    }

    private void animateNotificationBadge() {
        if (notificationBadge == null) return;

        ScaleTransition scale = new ScaleTransition(Duration.millis(300), notificationBadge);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.4);
        scale.setToY(1.4);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    // ================================
    // 💬 MESSAGES
    // ================================

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
                } else {
                    messageBadge.setVisible(false);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur badge messages: " + e.getMessage());
        }
    }

    private void startBadgeRefresh() {
        badgeRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(10), e -> updateMessageBadge())
        );
        badgeRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        badgeRefreshTimeline.play();
    }

    // ================================
    // 🎯 NAVIGATION
    // ================================

    private void loadUserInfo() {
        if (session.estConnecte()) {
            Utilisateur user = session.getUtilisateurConnecte();
            if (userNameLabel != null) {
                userNameLabel.setText(user.getNom() + " " + user.getPrenom());
            }
            if (userRoleLabel != null) {
                userRoleLabel.setText(user.getRole());
            }
        }
    }

    private void setupSearchField() {
        if (searchField != null) {
            searchField.setOnAction(event -> performSearch());
        }
    }

    private void setupHoverEffects() {
        // Effets hover sur les boutons
    }

    private void playWelcomeAnimation() {
        if (userNameLabel != null) {
            FadeTransition fade = new FadeTransition(Duration.millis(800), userNameLabel);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    @FXML private void showDashboard() { nav.goToDashboard(); }
    @FXML private void showCustomerList() { nav.goToClients(); }
    @FXML private void showVehicleManagement() { nav.goToVehicles(); }
    @FXML private void showPendingReservations() { nav.goToReservations(); }
    @FXML private void showRendezVous() { nav.goToRendezVous(); }
    @FXML private void showMagasins() { nav.goToMagasins(); }
    @FXML private void performSearch() {
        String searchText = searchField.getText().trim();
        if (!searchText.isEmpty()) {
            System.out.println("🔍 Recherche: " + searchText);
        }
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

    @FXML private void showCart() {
        showAlert("Panier", "Fonction panier en développement", Alert.AlertType.INFORMATION);
    }

    @FXML private void showUserProfile() {
        showAlert("Profil", "Profil utilisateur", Alert.AlertType.INFORMATION);
    }

    @FXML private void showAccountSettings() {
        showAlert("Paramètres", "Paramètres du compte", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText(null);
        confirmation.setContentText("Voulez-vous vous déconnecter ?");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                // 🟦 1) Stopper tout ce qui tourne encore
                cleanup();                  // Stop timelines, listeners, popup
                session.fermerSession();    // Détruit les données utilisateur

                // 🟦 2) Charger une NOUVELLE scène
                try {
                    FXMLLoader loader =
                            new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
                    Parent root = loader.load();

                    // Récupérer la fenêtre
                    Stage stage = (Stage) searchField.getScene().getWindow();

                    // Remplacer toute la scène
                    stage.setScene(new Scene(root));
                    stage.show();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void cleanup() {
        NotificationManager.getInstance().removeListener(this);
        stopBadgeRefresh();

        if (notificationCheckTimeline != null) notificationCheckTimeline.stop();
        if (notificationRefreshTimeline != null) notificationRefreshTimeline.stop();
        if (notificationPopup != null) notificationPopup.hide();
    }

    public void stopBadgeRefresh() {
        if (badgeRefreshTimeline != null) badgeRefreshTimeline.stop();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}