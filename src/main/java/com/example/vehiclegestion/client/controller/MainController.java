package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.common.controller.ChatWindowController;
import com.example.vehiclegestion.common.model.Notification;
import com.example.vehiclegestion.common.utils.NotificationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class MainController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label userNameLabel;
    @FXML private TextField searchField;
    @FXML private Button vehiclesBtn;
    @FXML private Button favoritesBtn;
    @FXML private Button historyBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;
    @FXML private Button notificationBtn;
    @FXML private Button btnMarkAllRead;
    @FXML private Button btnSeeAll;

    // Éléments du dropdown de notifications
    @FXML private BorderPane notificationDropdown;
    @FXML private VBox notificationItemsContainer;
    @FXML private Label lblNotificationStatus;
    @FXML private Label notificationBadge;
    @FXML private ScrollPane notificationScrollPane;

    private SessionManager sessionManager = SessionManager.getInstance();
    private NotificationService notificationService;
    private Timeline notificationCheckTimeline;
    private Timeline notificationRefreshTimeline;
    private int lastNotificationCount = 0;
    private boolean notificationDropdownVisible = false;

    @FXML
    public void initialize() {
        System.out.println("🚀 MainController initialisé");

        if (!sessionManager.estConnecte()) {
            redirectToLogin();
            return;
        }

        notificationService = NotificationService.getInstance();

        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());

        System.out.println("👤 Utilisateur connecté: " + currentUser.getPrenom() + " " + currentUser.getNom());

        // Initialiser les notifications
        setupNotifications();

        // Charger la page véhicules par défaut
        showVehicles();

        // Initialiser la recherche
        initializeSearch();

        // Initialiser les boutons du dropdown
        setupDropdownButtons();

        // Cacher le dropdown au démarrage
        notificationDropdown.setVisible(false);
        notificationDropdown.setManaged(false);

        // Fermer le dropdown en cliquant ailleurs
        setupClickOutsideListener();
    }

    private void setupNotifications() {
        // Mettre à jour le badge
        updateNotificationBadge();

        // Vérifier les nouvelles notifications périodiquement
        startNotificationChecker();

        // Rafraîchir la liste toutes les 30 secondes
        startNotificationRefresh();
    }

    private void setupDropdownButtons() {
        // Bouton "Tout marquer comme lu"
        btnMarkAllRead.setOnAction(e -> markAllNotificationsAsRead());

        // Bouton "Voir tout"
        btnSeeAll.setOnAction(e -> showAllNotificationsPage());
    }

    @FXML
    private void toggleNotificationDropdown() {
        System.out.println("🔔 Toggle dropdown notifications");

        if (!notificationDropdownVisible) {
            // Afficher le dropdown
            showNotificationDropdown();
        } else {
            // Cacher le dropdown
            hideNotificationDropdown();
        }
    }

    private void showNotificationDropdown() {
        System.out.println("📱 Affichage dropdown notifications");

        notificationDropdownVisible = true;
        notificationDropdown.setVisible(true);
        notificationDropdown.setManaged(true);

        // ✨ AJUSTER LA TAILLE DYNAMIQUEMENT
        notificationDropdown.setPrefWidth(500);
        notificationDropdown.setPrefHeight(600);
        notificationDropdown.setMaxWidth(550);
        notificationDropdown.setMaxHeight(650);

        // Positionner le dropdown sous le bouton
        positionNotificationDropdown();

        // Charger les notifications
        loadNotificationDropdown();

        // Rafraîchir le badge
        updateNotificationBadge();

        // Empêcher la propagation du clic
        notificationDropdown.setOnMouseClicked(e -> e.consume());
    }
    private void hideNotificationDropdown() {
        System.out.println("📱 Masquage dropdown notifications");

        notificationDropdownVisible = false;
        notificationDropdown.setVisible(false);
        notificationDropdown.setManaged(false);
    }

    private void positionNotificationDropdown() {
        // Position approximative - ajustez selon votre layout
        double dropdownX = notificationBtn.localToScene(0, 0).getX() - 150;
        double dropdownY = notificationBtn.localToScene(0, 0).getY() + 65;

        notificationDropdown.setTranslateX(dropdownX);
        notificationDropdown.setTranslateY(dropdownY);

        notificationDropdown.setPrefWidth(350);
        notificationDropdown.setPrefHeight(450);
        notificationDropdown.setMinWidth(300);


        notificationDropdown.setTranslateX(dropdownX);
        notificationDropdown.setTranslateY(dropdownY);
    }

    private void loadNotificationDropdown() {
        if (!sessionManager.estConnecte() || notificationService == null) return;

        int userId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();
        List<Notification> notifications = notificationService.getNotificationsUtilisateur(userId, false);

        Platform.runLater(() -> {
            notificationItemsContainer.getChildren().clear();

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
            System.err.println("❌ Erreur création item notification: " + e.getMessage());
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
        // Navigation selon le type de notification
        String lien = notification.getLienAction();
        String type = notification.getTypeNotification();

        System.out.println("🎯 Navigation depuis notification: " + type);

        if (lien != null) {
            if (lien.contains("message") || lien.startsWith("/messages")) {
                openChatFromMenu();
            } else if (lien.contains("vehicule") || lien.startsWith("/vehicules")) {
                showVehicles();
            } else if (lien.contains("reservation") || lien.startsWith("/reservations")) {
                showHistory();
            }
        } else {
            // Navigation par type
            switch (notification.getCategorie()) {
                case "message":
                    openChatFromMenu();
                    break;
                case "vehicule":
                    showVehicles();
                    break;
                case "transaction":
                    showHistory();
                    break;
                default:
                    // Ne rien faire
                    break;
            }
        }
    }

    private void setupClickOutsideListener() {
        // Fermer le dropdown quand on clique ailleurs
        mainBorderPane.setOnMouseClicked(e -> {
            if (notificationDropdownVisible &&
                    !notificationDropdown.getBoundsInParent().contains(e.getX(), e.getY()) &&
                    !notificationBtn.getBoundsInParent().contains(e.getX(), e.getY())) {
                hideNotificationDropdown();
            }
        });
    }

    private void markAllNotificationsAsRead() {
        if (!sessionManager.estConnecte() || notificationService == null) return;

        int userId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();
        notificationService.marquerToutesCommeLues(userId);

        // Rafraîchir l'UI
        loadNotificationDropdown();
        updateNotificationBadge();

        System.out.println("✅ Toutes les notifications marquées comme lues");
    }

    private void showAllNotificationsPage() {
        System.out.println("📋 Navigation vers page notifications complète");

        // Cacher le dropdown
        hideNotificationDropdown();

        // Vous pouvez créer une page dédiée pour l'historique complet
        // Pour l'instant, simplement logger
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications complètes");
        alert.setHeaderText("Page des notifications");
        alert.setContentText("Cette fonctionnalité sera implémentée prochainement.");
        alert.showAndWait();
    }

    private void startNotificationChecker() {
        // Vérifier les nouvelles notifications toutes les 30 secondes
        notificationCheckTimeline = new Timeline(
                new KeyFrame(Duration.seconds(30), e -> updateNotificationBadge())
        );
        notificationCheckTimeline.setCycleCount(Timeline.INDEFINITE);
        notificationCheckTimeline.play();
    }

    private void startNotificationRefresh() {
        // Rafraîchir la liste des notifications toutes les 30 secondes
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
        if (!sessionManager.estConnecte() || notificationService == null) return;

        Platform.runLater(() -> {
            try {
                int userId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();
                int count = notificationService.getNombreNotificationsNonLues(userId);

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
                System.err.println("❌ Erreur mise à jour badge: " + e.getMessage());
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

    // Les autres méthodes restent les mêmes...

    @FXML
    private void openChatFromMenu() {
        System.out.println("\n💬 === OUVERTURE CHAT DEPUIS MENU ===");

        try {
            String[] possiblePaths = {
                    "/view/common/ChatWindow.fxml",
                    "/com/example/vehiclegestion/view/common/ChatWindow.fxml",
                    "view/common/ChatWindow.fxml",
                    "/ChatWindow.fxml",
                    "ChatWindow.fxml"
            };

            FXMLLoader loader = null;
            Parent chatRoot = null;
            String foundPath = null;

            for (String path : possiblePaths) {
                try {
                    System.out.println("🔍 Essai du chemin: " + path);
                    URL url = getClass().getResource(path);
                    if (url != null) {
                        System.out.println("✅ URL trouvée: " + url);
                        loader = new FXMLLoader(url);
                        chatRoot = loader.load();
                        foundPath = path;
                        System.out.println("✅ FXML chargé avec succès: " + path);
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("❌ Échec pour: " + path + " - " + e.getMessage());
                }
            }

            if (chatRoot == null || loader == null) {
                System.err.println("❌ Fichier ChatWindow.fxml introuvable dans tous les chemins testés");
                showError("Erreur", "Impossible de charger l'interface de chat");
                return;
            }

            System.out.println("✅ FXML chargé depuis: " + foundPath);

            // Récupérer le contrôleur
            ChatWindowController chatController = loader.getController();

            // Vérifier que le contrôleur a bien été chargé
            if (chatController == null) {
                System.err.println("❌ Contrôleur ChatWindowController non trouvé!");
                return;
            }

            // Créer et afficher la fenêtre
            Stage stage = new Stage();
            stage.setTitle("Messages - Historique des conversations");
            stage.setScene(new Scene(chatRoot, 1000, 700));
            stage.setMinWidth(800);
            stage.setMinHeight(600);

            // Fermer proprement
            stage.setOnCloseRequest(e -> {
                if (chatController != null) {
                    chatController.cleanup();
                }
            });

            stage.show();

            System.out.println("✅ Fenêtre de chat (mode menu) ouverte avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 2) {
                performSearch(newValue);
            }
        });
    }

    private void performSearch(String query) {
        System.out.println("🔍 Recherche globale: " + query);
        // Implémentation de la recherche
    }

    @FXML
    private void showVehicles() {
        System.out.println("🚗 Navigation vers Véhicules");
        setActiveMenu(vehiclesBtn);
        loadContent("/view/client/vehicles-view.fxml");
    }

    @FXML
    private void showFavorites() {
        System.out.println("❤️ Navigation vers Favoris");
        setActiveMenu(favoritesBtn);
        loadContent("/view/client/ClientFavoritesView.fxml");
    }

    @FXML
    private void showHistory() {
        System.out.println("📊 Navigation vers Historique");
        setActiveMenu(historyBtn);
        loadContent("/view/client/historique-client.fxml");
    }

    @FXML
    private void showProfile() {
        System.out.println("👤 Navigation vers Profil");
        setActiveMenu(profileBtn);
        loadContent("/view/client/profile-view.fxml");
    }

    @FXML
    private void logout() {
        System.out.println("🚪 Déconnexion");
        // Nettoyer les timers
        if (notificationCheckTimeline != null) {
            notificationCheckTimeline.stop();
        }
        if (notificationRefreshTimeline != null) {
            notificationRefreshTimeline.stop();
        }
        sessionManager.fermerSession();
        redirectToLogin();
    }

    private void setActiveMenu(Button activeButton) {
        // Réinitialiser tous les boutons
        vehiclesBtn.setStyle(getInactiveStyle());
        favoritesBtn.setStyle(getInactiveStyle());
        historyBtn.setStyle(getInactiveStyle());
        profileBtn.setStyle(getInactiveStyle());

        // Activer le bouton sélectionné
        if (activeButton != null) {
            activeButton.setStyle(getActiveStyle());
        }
    }

    private String getInactiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #2c3e50; " +
                "-fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 18 20; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: transparent;";
    }

    private String getActiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #3498db; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 18 20; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: #3498db;";
    }

    private void loadContent(String fxmlPath) {
        try {
            System.out.println("📁 Chargement du contenu: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            mainBorderPane.setCenter(content);
            System.out.println("✅ Contenu chargé avec succès: " + fxmlPath);

        } catch (IOException e) {
            System.err.println("❌ Erreur de chargement: " + fxmlPath);
            showErrorPage("Page non disponible: " + fxmlPath);
        }
    }

    private void showErrorPage(String message) {
        VBox errorBox = new VBox();
        errorBox.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 20; -fx-alignment: center; -fx-spacing: 10;");

        Label errorLabel = new Label("⚠️ " + message);
        errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");

        Label infoLabel = new Label("Cette page est en cours de développement");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");

        errorBox.getChildren().addAll(errorLabel, infoLabel);
        mainBorderPane.setCenter(errorBox);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
            Parent loginPage = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(loginPage));
            stage.setTitle("Connexion - AutoSales Pro");
            stage.setMaximized(false);
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("❌ Erreur critique lors de la redirection vers login");
        }
    }

    public void cleanup() {
        if (notificationCheckTimeline != null) {
            notificationCheckTimeline.stop();
        }
        if (notificationRefreshTimeline != null) {
            notificationRefreshTimeline.stop();
        }
    }




}