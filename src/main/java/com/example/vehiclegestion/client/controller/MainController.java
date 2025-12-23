package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.common.controller.ChatWindowController;
import com.example.vehiclegestion.common.model.Notification;
import com.example.vehiclegestion.common.utils.NotificationService;
import com.example.vehiclegestion.common.service.SearchService;
import com.example.vehiclegestion.client.model.Vehicle;
import javafx.animation.KeyFrame;
import com.example.vehiclegestion.client.doa.VehicleDAO;
import com.example.vehiclegestion.vendeur.dao.MagasinDAO;
import com.example.vehiclegestion.vendeur.model.Magasin;

// Service de logs
import com.example.vehiclegestion.client.service.ClientLogService;

// Imports JavaFX
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Background;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;

// Imports pour les listes
import java.util.List;
import java.util.ArrayList;
import com.example.vehiclegestion.common.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.animation.Timeline;
import javafx.application.Platform;

// Imports FXML
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.stage.Window;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;

// Import pour VehiDetaiCo (assurez-vous que cette classe existe)
import com.example.vehiclegestion.client.controller.VehiDetaiCo;

// Import pour ClientVehiclesController (assurez-vous que cette classe existe)
import com.example.vehiclegestion.client.controller.ClientVehiclesController;

public class MainController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label userNameLabel;
    @FXML private TextField searchField;
    @FXML private Button vehiclesBtn;
    @FXML private Button messagesBtn;
    @FXML private Button messagesIconBtn;
    @FXML private Button favoritesBtn;
    @FXML private Button historyBtn;
    @FXML private Button profileBtn;
    @FXML private Button logoutBtn;
    @FXML private Button notificationBtn;
    @FXML private Button btnMarkAllRead;
    @FXML private Button btnSeeAll;
    @FXML private Label messagesBadge;

    // Éléments du dropdown de notifications
    @FXML private BorderPane notificationDropdown;
    @FXML private VBox notificationItemsContainer;
    @FXML private Label lblNotificationStatus;
    @FXML private Label notificationBadge;
    @FXML private ScrollPane notificationScrollPane;

    // 🔍 Éléments pour la recherche
    private Stage searchResultsStage;
    private VBox searchResultsContainer;
    private boolean searchDropdownVisible = false;
    private Timeline hideSearchResultsTimer;

    private SessionManager sessionManager = SessionManager.getInstance();
    private NotificationService notificationService;
    private SearchService searchService;
    private Timeline notificationCheckTimeline;
    private Timeline notificationRefreshTimeline;
    private int lastNotificationCount = 0;
    private boolean notificationDropdownVisible = false;

    // Service de logs
    private ClientLogService clientLogService;

    @FXML
    public void initialize() {
        System.out.println("🚀 MainController initialisé");

        // Initialiser le service de logs
        clientLogService = new ClientLogService();

        if (!sessionManager.estConnecte()) {
            System.err.println("❌ Session invalide détectée dans initialize()");
            sessionManager.debugSession();
            showAlert("Session expirée", "Votre session a expiré. Veuillez vous reconnecter.");

            // Log de session expirée
            clientLogService.logSessionExpired("MainController.initialize");

            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    Platform.runLater(() -> redirectToLogin());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return;
        }

        System.out.println("✅ Session valide, initialisation de l'interface");

        // Log de démarrage de session
        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        if (currentUser != null) {
            clientLogService.logUserActivity("UI_INITIALIZATION",
                    currentUser.getIdUtilisateur(),
                    currentUser.getEmail(),
                    "Initialisation de l'interface principale");
        }

        initializeInterface();
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void initializeInterface() {
        System.out.println("🎨 Initialisation de l'interface utilisateur");

        if (mainBorderPane != null) {
            mainBorderPane.setUserData(this);
        }

        notificationService = NotificationService.getInstance();
        searchService = SearchService.getInstance();

        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        if (userNameLabel != null && currentUser != null) {
            userNameLabel.setText(currentUser.getPrenom() + " " + currentUser.getNom());
            System.out.println("👤 Utilisateur: " + currentUser.getPrenom() + " " + currentUser.getNom());
        }

        setupNotifications();
        showVehicles();
        initializeSearch();
        setupDropdownButtons();

        mainBorderPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Attendre que la scène soit rendue
                Platform.runLater(() -> {
                    if (notificationDropdownVisible) {
                        positionNotificationDropdownDynamic();
                    }
                });
            }
        });

        // IMPORTANT : S'assurer que le dropdown est bien initialisé
        if (notificationDropdown != null) {
            notificationDropdown.setVisible(false);
            notificationDropdown.setManaged(false);

            // NOUVEAU : Ajouter un style pour s'assurer qu'il est au-dessus
            notificationDropdown.setStyle(notificationDropdown.getStyle() +
                    "-fx-view-order: -1000;"); // Valeur négative = au premier plan
        }

        setupClickOutsideListener();
        debugButtons();

        // ✅ AJOUTER LE DEBUG DES NOTIFICATIONS
        debugNotificationElements();
    }

    // ============================================
    // 🔍 SYSTÈME DE RECHERCHE COMPLET - CORRIGÉ
    // ============================================

    private void initializeSearch() {
        if (searchField != null) {
            System.out.println("🔍 Initialisation de la barre de recherche");

            // Log d'initialisation
            clientLogService.logUIEvent("SEARCH_INIT", "Initialisation du système de recherche");

            // Listener pour la recherche en temps réel
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && !newValue.trim().isEmpty()) {
                    if (newValue.trim().length() >= 2) {
                        performSearch(newValue.trim());
                    } else {
                        hideSearchResults();
                    }
                } else {
                    hideSearchResults();
                }
            });

            // Focus listener modifié - plus de délai automatique
            searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    // Annuler le timer s'il existe
                    if (hideSearchResultsTimer != null) {
                        hideSearchResultsTimer.stop();
                    }
                    // Ne pas masquer automatiquement - laisser le clic extérieur gérer
                }
            });

            // Écouteur pour la touche Entrée
            searchField.setOnAction(e -> {
                String query = searchField.getText().trim();
                if (!query.isEmpty() && query.length() >= 2) {
                    // Log de recherche avec Entrée
                    clientLogService.logSearchEvent(query, "ENTER_KEY");
                    performSearch(query);
                }
            });

            searchField.setPromptText("Rechercher par marque, modèle, année...");
        }
    }

    private void performSearch(String query) {
        System.out.println("🔍 Recherche en cours pour: " + query);

        // Log de l'action de recherche
        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        if (currentUser != null) {
            clientLogService.logSearchEvent(query, "REAL_TIME", currentUser.getIdUtilisateur());
        } else {
            clientLogService.logSearchEvent(query, "REAL_TIME");
        }

        List<SearchResult> results = searchInDatabase(query);

        if (results.isEmpty()) {
            showNoResults(query);
            // Log aucun résultat trouvé
            clientLogService.logSearchNoResults(query);
        } else {
            displaySearchResults(results);
            // Log résultats trouvés
            clientLogService.logSearchResults(query, results.size());
        }
    }

    private void displaySearchResults(List<SearchResult> results) {
        Platform.runLater(() -> {
            if (searchResultsContainer == null) {
                createSearchResultsContainer();
            }

            searchResultsContainer.getChildren().clear();

            // Header
            HBox header = new HBox();
            header.setStyle("-fx-padding: 15; -fx-background-color: rgba(15, 23, 42, 0.95); " +
                    "-fx-border-width: 0 0 1 0; -fx-border-color: #334155;");
            Label headerLabel = new Label("Résultats de recherche (" + results.size() + ")");
            headerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");
            header.getChildren().add(headerLabel);
            searchResultsContainer.getChildren().add(header);

            // Résultats
            for (SearchResult result : results) {
                HBox resultItem = createSearchResultItem(result);
                searchResultsContainer.getChildren().add(resultItem);
            }

            showSearchResults();
        });
    }

    private HBox createSearchResultItem(SearchResult result) {
        HBox item = new HBox(12);
        item.setStyle("-fx-padding: 12 15; -fx-border-width: 0 0 1 0; " +
                "-fx-border-color: #334155; -fx-background-color: rgba(30, 41, 59, 0.8); " +
                "-fx-cursor: hand;");
        item.setAlignment(Pos.CENTER_LEFT);

        // Icône
        Label icon = new Label(result.getIcon());
        icon.setStyle("-fx-font-size: 20px; -fx-min-width: 30px;");

        // Contenu
        VBox content = new VBox(3);
        content.setMaxWidth(400);

        Label title = new Label(result.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #f1f5f9;");
        title.setWrapText(true);

        Label description = new Label(result.getDescription());
        description.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        description.setWrapText(true);

        Label category = new Label(result.getCategory());
        category.setStyle("-fx-background-color: rgba(59, 130, 246, 0.2); " +
                "-fx-text-fill: #3b82f6; -fx-padding: 2 8; " +
                "-fx-font-size: 10px; -fx-background-radius: 10;");

        content.getChildren().addAll(title, description, category);
        item.getChildren().addAll(icon, content);

        // ✅ CORRECTION : Événement de clic
        item.setOnMouseClicked(e -> {
            hideSearchResults();
            searchField.clear();

            // Log du clic sur un résultat
            clientLogService.logSearchResultClick(result.getType(), result.getId(), result.getTitle());

            // Si c'est un article/véhicule, afficher les détails
            if ("ARTICLE".equals(result.getType()) || "VEHICLE".equals(result.getType())) {
                int vehicleId = result.getId();
                showVehicleDetails(vehicleId);
            } else if ("STORE".equals(result.getType())) {
                int storeId = result.getId();
                showStoreDetails(storeId);
            } else if ("CITY".equals(result.getType())) {
                result.getAction().run();
            } else {
                // Sinon, exécuter l'action par défaut
                result.getAction().run();
            }
        });

        // Effets de survol
        item.setOnMouseEntered(e -> item.setStyle(item.getStyle().replace(
                "-fx-background-color: rgba(30, 41, 59, 0.8);",
                "-fx-background-color: rgba(59, 130, 246, 0.1);"
        )));

        item.setOnMouseExited(e -> item.setStyle(item.getStyle().replace(
                "-fx-background-color: rgba(59, 130, 246, 0.1);",
                "-fx-background-color: rgba(30, 41, 59, 0.8);"
        )));

        return item;
    }

    private void createSearchResultsContainer() {
        if (searchResultsStage == null) {
            searchResultsStage = new Stage();
            searchResultsStage.initStyle(StageStyle.UNDECORATED);
            searchResultsStage.initOwner(findCurrentStage());

            searchResultsContainer = new VBox();
            searchResultsContainer.setStyle("-fx-background-color: #1e293b; -fx-background-radius: 8;");

            Scene scene = new Scene(searchResultsContainer, 500, 400);
            scene.setFill(null);
            searchResultsStage.setScene(scene);

            // Effet d'ombre
            searchResultsContainer.setEffect(new javafx.scene.effect.DropShadow(20, javafx.scene.paint.Color.BLACK));

            // Empêcher la fenêtre de prendre le focus
            searchResultsStage.setAlwaysOnTop(true);
        }
    }

    private void showSearchResults() {
        if (searchResultsStage != null && searchField != null) {
            // Positionner la fenêtre sous la barre de recherche
            javafx.geometry.Bounds bounds = searchField.localToScreen(searchField.getBoundsInLocal());
            searchResultsStage.setX(bounds.getMinX() + 50);
            searchResultsStage.setY(bounds.getMaxY() + 5);

            searchResultsStage.show();
            searchDropdownVisible = true;

            // Log d'affichage des résultats
            clientLogService.logUIEvent("SEARCH_DROPDOWN_SHOW", "Affichage des résultats de recherche");
        }
    }

    private void hideSearchResults() {
        if (searchResultsStage != null && searchDropdownVisible) {
            searchResultsStage.hide();
            searchDropdownVisible = false;
        }
    }

    private void showNoResults(String query) {
        Platform.runLater(() -> {
            if (searchResultsContainer == null) {
                createSearchResultsContainer();
            }

            searchResultsContainer.getChildren().clear();

            VBox emptyState = new VBox(15);
            emptyState.setStyle("-fx-alignment: center; -fx-padding: 60 20;");

            Label icon = new Label("🔍");
            icon.setStyle("-fx-font-size: 40px;");

            Label message = new Label("Aucun résultat pour \"" + query + "\"");
            message.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

            Label suggestion = new Label("Essayez avec d'autres mots-clés");
            suggestion.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

            emptyState.getChildren().addAll(icon, message, suggestion);
            searchResultsContainer.getChildren().add(emptyState);

            showSearchResults();
        });
    }

    // ============================================
    // GESTION DU CLIC EXTÉRIEUR - CORRIGÉ
    // ============================================

    private HBox createNotificationItem(Notification notification) {
        HBox item = new HBox(12);
        item.setStyle("-fx-padding: 15 18; " +
                "-fx-border-width: 0 0 1 0; " +
                "-fx-border-color: #334155; " +
                "-fx-background-color: " +
                (notification.isEstLue() ? "rgba(30, 41, 59, 0.8)" : "rgba(59, 130, 246, 0.15)") + ";");

        // Icône
        Label iconLabel = new Label(notification.getIcon());
        iconLabel.setStyle("-fx-font-size: 20px; -fx-padding: 0 12 0 0;");

        // Contenu
        VBox contentBox = new VBox(5);
        contentBox.setMaxWidth(320);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        // Titre
        Label titleLabel = new Label(notification.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #f1f5f9; -fx-font-size: 14px;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(280);

        // Message
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(320);

        // Heure
        Label timeLabel = new Label(notification.getTimeAgo());
        timeLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");

        contentBox.getChildren().addAll(titleLabel, messageLabel, timeLabel);
        item.getChildren().addAll(iconLabel, contentBox);

        // Effet hover
        item.setOnMouseEntered(e -> {
            item.setStyle(item.getStyle() +
                    "-fx-background-color: rgba(59, 130, 246, 0.25); " +
                    "-fx-cursor: hand;");
        });

        item.setOnMouseExited(e -> {
            item.setStyle("-fx-padding: 15 18; " +
                    "-fx-border-width: 0 0 1 0; " +
                    "-fx-border-color: #334155; " +
                    "-fx-background-color: " +
                    (notification.isEstLue() ? "rgba(30, 41, 59, 0.8)" : "rgba(59, 130, 246, 0.15)") + ";");
        });

        // Clic
        item.setOnMouseClicked(e -> {
            System.out.println("📱 Notification cliquée: " + notification.getTitre());

            // Log du clic sur notification
            clientLogService.logNotificationClick(
                    notification.getIdNotification(),
                    notification.getCategorie(),
                    notification.getTitre()
            );

            if (!notification.isEstLue()) {
                notificationService.marquerCommeLue(notification.getIdNotification());
                updateNotificationBadge();
                loadNotificationDropdown();
            }

            hideNotificationDropdown();
            handleNotificationNavigation(notification);
        });

        return item;
    }

    private void setupClickOutsideListener() {
        if (mainBorderPane != null && mainBorderPane.getScene() != null) {
            mainBorderPane.getScene().addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
                handleClickOutside(e);
            });
        }
    }

    private void handleClickOutside(javafx.scene.input.MouseEvent e) {
        boolean clickedOnNotificationBtn = isClickOnNode(e, notificationBtn);
        boolean clickedOnNotificationDropdown = isClickOnNode(e, notificationDropdown);

        System.out.println("🖱️ Clic détecté:");
        System.out.println("   Sur bouton: " + clickedOnNotificationBtn);
        System.out.println("   Sur dropdown: " + clickedOnNotificationDropdown);
        System.out.println("   Dropdown visible: " + notificationDropdownVisible);

        // Gérer le clic en dehors du dropdown de notifications
        if (notificationDropdownVisible && !clickedOnNotificationBtn && !clickedOnNotificationDropdown) {
            System.out.println("✅ Masquage dropdown (clic extérieur)");
            hideNotificationDropdown();
        }
    }

    private boolean isClickOnNode(javafx.scene.input.MouseEvent e, javafx.scene.Node node) {
        if (node == null || !node.isVisible()) return false;

        javafx.geometry.Bounds bounds = node.localToScene(node.getBoundsInLocal());
        return bounds.contains(e.getSceneX(), e.getSceneY());
    }

    private boolean isClickOnStage(javafx.scene.input.MouseEvent e, Stage stage) {
        if (stage == null || !stage.isShowing()) return false;

        javafx.geometry.Bounds bounds = new javafx.geometry.BoundingBox(
                stage.getX(), stage.getY(),
                stage.getWidth(), stage.getHeight()
        );
        return bounds.contains(e.getScreenX(), e.getScreenY());
    }

    // ============================================
    // SYSTÈME DE NOTIFICATIONS
    // ============================================

    private void setupNotifications() {
        updateNotificationBadge();
        startNotificationChecker();
        startNotificationRefresh();

        // Log d'initialisation des notifications
        clientLogService.logUIEvent("NOTIFICATIONS_INIT", "Initialisation du système de notifications");
    }

    private void setupDropdownButtons() {
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnAction(e -> {
                markAllNotificationsAsRead();
                // Log d'action utilisateur
                clientLogService.logUserAction("MARK_ALL_NOTIFICATIONS_READ", "Marquer toutes les notifications comme lues");
            });
        }

        if (btnSeeAll != null) {
            btnSeeAll.setOnAction(e -> {
                showAllNotificationsPage();
                // Log d'action utilisateur
                clientLogService.logUserAction("SEE_ALL_NOTIFICATIONS", "Voir toutes les notifications");
            });
        }
    }

    @FXML
    private void toggleNotificationDropdown() {
        System.out.println("🔔 === DÉBUT toggleNotificationDropdown ===");
        System.out.println("   État actuel: " + notificationDropdownVisible);

        // Log de l'action utilisateur
        clientLogService.logUIEvent("NOTIFICATION_DROPDOWN_TOGGLE",
                notificationDropdownVisible ? "Masquer dropdown" : "Afficher dropdown");

        if (!notificationDropdownVisible) {
            System.out.println("   → Affichage du dropdown");
            showNotificationDropdown();
        } else {
            System.out.println("   → Masquage du dropdown");
            hideNotificationDropdown();
        }

        System.out.println("✅ === FIN toggleNotificationDropdown ===");
    }

    private void showNotificationDropdown() {
        System.out.println("📱 === DÉBUT showNotificationDropdown ===");

        if (notificationDropdown == null) {
            System.err.println("❌ notificationDropdown est null!");
            return;
        }

        try {
            // 1. S'assurer qu'il est au premier plan
            notificationDropdown.toFront();

            // 2. Charger les notifications
            loadNotificationDropdown();

            // 3. Positionner
            positionNotificationDropdownDynamic();

            // 4. Mettre à jour l'état et afficher
            notificationDropdownVisible = true;
            notificationDropdown.setVisible(true);
            notificationDropdown.setManaged(true);

            // 5. Forcer le rendu
            notificationDropdown.requestLayout();
            notificationDropdown.applyCss();

            // 6. DEBUG : Vérifier la position et la visibilité
            System.out.println("📍 Position finale:");
            System.out.println("   TranslateX: " + notificationDropdown.getTranslateX());
            System.out.println("   TranslateY: " + notificationDropdown.getTranslateY());
            System.out.println("   LayoutX: " + notificationDropdown.getLayoutX());
            System.out.println("   LayoutY: " + notificationDropdown.getLayoutY());
            System.out.println("   Visible: " + notificationDropdown.isVisible());
            System.out.println("   Managed: " + notificationDropdown.isManaged());
            System.out.println("   Parent: " + notificationDropdown.getParent());

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage dropdown: " + e.getMessage());
            e.printStackTrace();
            // Log d'erreur
            clientLogService.logError("SHOW_NOTIFICATION_DROPDOWN", e.getMessage());
        }

        System.out.println("📱 === FIN showNotificationDropdown ===");
    }

    private void hideNotificationDropdown() {
        System.out.println("📱 Masquage dropdown notifications");

        // Log d'action UI
        clientLogService.logUIEvent("NOTIFICATION_DROPDOWN_HIDE", "Masquage du dropdown notifications");

        notificationDropdownVisible = false;

        if (notificationDropdown != null) {
            notificationDropdown.setVisible(false);
            notificationDropdown.setManaged(false);
        }
    }

    private void positionNotificationDropdownDynamic() {
        if (notificationBtn == null || notificationDropdown == null) {
            System.err.println("❌ Bouton ou dropdown null");
            return;
        }

        try {
            // Vérifier que le bouton est attaché à une scène
            if (notificationBtn.getScene() == null) {
                System.err.println("❌ Le bouton n'a pas de scène");
                notificationDropdown.setTranslateX(-200);
                notificationDropdown.setTranslateY(65);
                return;
            }

            // Obtenir la position du bouton dans les coordonnées d'écran
            javafx.geometry.Bounds btnBounds = notificationBtn.localToScreen(
                    notificationBtn.getBoundsInLocal()
            );

            if (btnBounds == null) {
                System.err.println("❌ Impossible d'obtenir les bounds du bouton");
                notificationDropdown.setTranslateX(-200);
                notificationDropdown.setTranslateY(65);
                return;
            }

            System.out.println("📍 Bounds du bouton:");
            System.out.println("   MinX: " + btnBounds.getMinX());
            System.out.println("   MinY: " + btnBounds.getMinY());
            System.out.println("   MaxX: " + btnBounds.getMaxX());
            System.out.println("   MaxY: " + btnBounds.getMaxY());

            // Positionner le dropdown sous le bouton, aligné à droite
            double dropdownWidth = 420; // Largeur fixe du dropdown
            double dropdownX = btnBounds.getMaxX() - dropdownWidth;
            double dropdownY = btnBounds.getMaxY() + 5;

            // Si le dropdown dépasse à gauche, ajuster
            if (dropdownX < btnBounds.getMinX()) {
                dropdownX = btnBounds.getMinX();
            }

            // Obtenir la fenêtre pour vérifier les limites
            Window window = notificationBtn.getScene().getWindow();
            if (window != null) {
                double windowWidth = window.getWidth();
                double windowHeight = window.getHeight();

                // Ajuster si le dropdown dépasse à droite
                if (dropdownX + dropdownWidth > windowWidth) {
                    dropdownX = windowWidth - dropdownWidth - 10;
                }

                // Ajuster si le dropdown dépasse en bas
                if (dropdownY + 600 > windowHeight) { // 600 = hauteur du dropdown
                    dropdownY = btnBounds.getMinY() - 600 - 5; // Afficher au-dessus
                }
            }

            System.out.println("📍 Position calculée:");
            System.out.println("   X: " + dropdownX);
            System.out.println("   Y: " + dropdownY);

            // Appliquer la position
            notificationDropdown.setTranslateX(dropdownX);
            notificationDropdown.setTranslateY(dropdownY);

        } catch (Exception e) {
            System.err.println("❌ Erreur positionnement dropdown: " + e.getMessage());
            e.printStackTrace();

            // Fallback absolu
            notificationDropdown.setTranslateX(-200);
            notificationDropdown.setTranslateY(65);
        }
    }

    private void loadNotificationDropdown() {
        System.out.println("📋 === DÉBUT loadNotificationDropdown ===");

        if (!sessionManager.estConnecte() || notificationService == null) {
            System.err.println("❌ Session ou service non disponible");
            return;
        }

        int userId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();
        System.out.println("👤 User ID: " + userId);

        List<Notification> notifications = notificationService.getNotificationsUtilisateur(userId, false);
        System.out.println("📊 Nombre de notifications: " + notifications.size());

        // Log du chargement des notifications
        clientLogService.logNotificationLoad(userId, notifications.size());

        Platform.runLater(() -> {
            try {
                if (notificationItemsContainer == null) {
                    System.err.println("❌ notificationItemsContainer est NULL!");
                    return;
                }

                // 1. Vider le conteneur
                notificationItemsContainer.getChildren().clear();
                System.out.println("🧹 Conteneur vidé");

                // 2. Vérifier si vide
                if (notifications.isEmpty()) {
                    System.out.println("📭 Aucune notification");
                    showEmptyNotificationState();

                    if (lblNotificationStatus != null) {
                        lblNotificationStatus.setText("0 notification(s)");
                    }
                    return;
                }

                // 3. Créer les items
                int nonLuesCount = 0;
                System.out.println("🎨 Création des items...");

                for (Notification notif : notifications) {
                    if (!notif.isEstLue()) {
                        nonLuesCount++;
                    }

                    try {
                        HBox item = createNotificationItem(notif);
                        notificationItemsContainer.getChildren().add(item);
                        System.out.println("✅ Item ajouté: " + notif.getTitre());
                    } catch (Exception e) {
                        System.err.println("❌ Erreur création item: " + e.getMessage());
                        e.printStackTrace();
                        // Log d'erreur
                        clientLogService.logError("CREATE_NOTIFICATION_ITEM", e.getMessage());
                    }
                }

                System.out.println("✅ " + notifications.size() + " items créés (" + nonLuesCount + " non lues)");

                // 4. Mettre à jour le statut
                if (lblNotificationStatus != null) {
                    lblNotificationStatus.setText(notifications.size() + " notification(s) • " +
                            nonLuesCount + " non lue(s)");
                    System.out.println("📝 Statut: " + lblNotificationStatus.getText());
                }

                // 5. IMPORTANT : Forcer le rafraîchissement
                notificationItemsContainer.requestLayout();

                // 6. Debug final
                System.out.println("📊 Conteneur final:");
                System.out.println("   Enfants: " + notificationItemsContainer.getChildren().size());
                System.out.println("   Visible: " + notificationItemsContainer.isVisible());
                System.out.println("   Managed: " + notificationItemsContainer.isManaged());

            } catch (Exception e) {
                System.err.println("❌ Erreur dans Platform.runLater: " + e.getMessage());
                e.printStackTrace();
            }
        });

        System.out.println("✅ === FIN loadNotificationDropdown ===");
    }

    private void showEmptyNotificationState() {
        VBox emptyState = new VBox(15);
        emptyState.setStyle("-fx-alignment: center; " +
                "-fx-padding: 50 20; " +
                "-fx-background-color: transparent;");

        Label icon = new Label("🔔");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #94a3b8;");

        Label message = new Label("Aucune notification");
        message.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

        Label subMessage = new Label("Vous serez notifié des nouvelles activités");
        subMessage.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        subMessage.setWrapText(true);
        subMessage.setMaxWidth(300);

        emptyState.getChildren().addAll(icon, message, subMessage);

        if (notificationItemsContainer != null) {
            notificationItemsContainer.getChildren().add(emptyState);
        }
    }

    private void debugNotificationElements() {
        System.out.println("\n=== DEBUG ÉLÉMENTS NOTIFICATION ===");
        System.out.println("notificationBtn: " + (notificationBtn != null ? "✅" : "❌"));
        System.out.println("notificationBadge: " + (notificationBadge != null ? "✅" : "❌"));
        System.out.println("notificationDropdown: " + (notificationDropdown != null ? "✅" : "❌"));
        System.out.println("notificationItemsContainer: " + (notificationItemsContainer != null ? "✅" : "❌"));
        System.out.println("lblNotificationStatus: " + (lblNotificationStatus != null ? "✅" : "❌"));
        System.out.println("notificationScrollPane: " + (notificationScrollPane != null ? "✅" : "❌"));

        if (notificationDropdown != null) {
            System.out.println("\nPropriétés dropdown:");
            System.out.println("   Visible: " + notificationDropdown.isVisible());
            System.out.println("   Managed: " + notificationDropdown.isManaged());
            System.out.println("   Width: " + notificationDropdown.getWidth());
            System.out.println("   Height: " + notificationDropdown.getHeight());
            System.out.println("   TranslateX: " + notificationDropdown.getTranslateX());
            System.out.println("   TranslateY: " + notificationDropdown.getTranslateY());
        }

        System.out.println("=====================================\n");
    }

    private void handleNotificationNavigation(Notification notification) {
        System.out.println("🎯 Navigation depuis notification: " + notification.getCategorie());

        // Log de navigation depuis notification
        clientLogService.logNotificationNavigation(
                notification.getIdNotification(),
                notification.getCategorie(),
                notification.getTitre()
        );

        // Fermer le dropdown
        hideNotificationDropdown();

        // Naviguer selon la catégorie
        switch (notification.getCategorie()) {
            case "message":
            case "chat":
                System.out.println("💬 Ouverture du chat...");
                openChatFromMenu();
                break;
            case "vehicule":
                System.out.println("🚗 Ouverture des véhicules...");
                showVehicles();
                break;
            case "transaction":
            case "reservation":
                System.out.println("📊 Ouverture historique...");
                showHistory();
                break;
            default:
                System.out.println("⚠️ Catégorie non reconnue, ouverture véhicules par défaut");
                showVehicles();
        }
    }

    private void showVehicleDetails(int vehicleId) {
        System.out.println("🚗 Affichage détails véhicule ID: " + vehicleId);

        // Log de navigation vers détails véhicule
        clientLogService.logVehicleView(vehicleId, "VEHICLE_DETAILS");

        hideSearchResults();
        searchField.clear();

        try {
            // Récupérer le véhicule depuis la base de données
            Vehicle vehicle = getVehicleFromDatabase(vehicleId);

            if (vehicle == null) {
                System.err.println("❌ Véhicule non trouvé ID: " + vehicleId);
                showAlert("Erreur", "Véhicule introuvable");
                // Log d'erreur
                clientLogService.logError("VEHICLE_NOT_FOUND", "ID: " + vehicleId);
                return;
            }

            // Charger la page de détails
            loadVehicleDetailPage(vehicle);

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage véhicule: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails du véhicule");
            // Log d'erreur
            clientLogService.logError("SHOW_VEHICLE_DETAILS", e.getMessage());
        }
    }

    private void loadVehicleDetailPage(Vehicle vehicle) {
        try {
            System.out.println("🚗 Chargement des détails pour: " + vehicle.getTitle());

            // ✅ ESSAYER PLUSIEURS CHEMINS POSSIBLES
            String[] possiblePaths = {
                    "/view/client/vehicle-Detail.fxml",
                    "/view/client/vehicle-Detail.fxml",
                    "/view/client/Vehicle-Detail.fxml",
                    "/fxml/client/vehicle-Detail.fxml",
                    "/fxml/vehicle-Detail.fxml",
                    "/vehicle-Detail.fxml"
            };

            URL fxmlUrl = null;
            String foundPath = null;

            // Chercher le fichier dans tous les chemins possibles
            for (String path : possiblePaths) {
                fxmlUrl = getClass().getResource(path);
                if (fxmlUrl != null) {
                    foundPath = path;
                    System.out.println("✅ Fichier FXML trouvé : " + path);
                    break;
                }
            }

            if (fxmlUrl == null) {
                System.err.println("❌ Fichier FXML introuvable dans tous les chemins testés");
                System.err.println("📂 Chemins testés :");
                for (String path : possiblePaths) {
                    System.err.println("   - " + path);
                }

                // ✅ SOLUTION ALTERNATIVE : Créer une vue simple directement
                showVehicleDetailsSimple(vehicle);
                return;
            }

            // Charger le FXML avec l'URL trouvée
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent detailContent = loader.load();

            // Obtenir le contrôleur
            VehiDetaiCo controller = loader.getController();

            // Convertir Vehicle en Article pour le contrôleur
            com.example.vehiclegestion.vendeur.model.Article article = convertVehicleToArticle(vehicle);

            // Passer les données au contrôleur
            controller.receiveData(article);

            // Afficher dans la zone centrale
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(detailContent);
                System.out.println("✅ Détails du véhicule affichés dans la zone centrale");

                // Log de succès
                clientLogService.logUISuccess("VEHICLE_DETAILS_LOADED", "ID: " + vehicle.getId());
            } else {
                System.err.println("❌ mainBorderPane est null");
                openVehicleDetailInNewWindow(article);
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement page détails: " + e.getMessage());
            e.printStackTrace();

            // Log d'erreur
            clientLogService.logError("LOAD_VEHICLE_DETAIL_PAGE", e.getMessage());

            // Fallback vers une vue simple
            showVehicleDetailsSimple(vehicle);
        }
    }

    private void showVehicleDetailsSimple(Vehicle vehicle) {
        System.out.println("🎨 Création d'une vue simple pour: " + vehicle.getTitle());

        // Log de création de vue simple
        clientLogService.logUIEvent("VEHICLE_SIMPLE_VIEW", "Création vue simple pour véhicule ID: " + vehicle.getId());

        try {
            // Créer un ScrollPane principal
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: #0f172a;");

            // Conteneur principal
            VBox mainContainer = new VBox(20);
            mainContainer.setStyle("-fx-padding: 30; -fx-background-color: #0f172a;");

            // === HEADER : Titre et Prix ===
            VBox headerBox = new VBox(15);
            headerBox.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); " +
                    "-fx-background-radius: 12; " +
                    "-fx-padding: 25; " +
                    "-fx-border-color: linear-gradient(to right, #8b5cf6, #3b82f6); " +
                    "-fx-border-width: 1.5; " +
                    "-fx-border-radius: 12;");

            Label titleLabel = new Label(vehicle.getTitle());
            titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");
            titleLabel.setWrapText(true);

            Label priceLabel = new Label(String.format("%.2f DH", vehicle.getPrice()));
            priceLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; " +
                    "-fx-text-fill: linear-gradient(to right, #ec4899, #8b5cf6);");

            Label categoryLabel = new Label("📁 " + vehicle.getCategory());
            categoryLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");

            Label stateLabel = new Label("🔖 État: " + vehicle.getState());
            stateLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");

            headerBox.getChildren().addAll(titleLabel, priceLabel, categoryLabel, stateLabel);

            // === IMAGE ===
            if (vehicle.getImage() != null && !vehicle.getImage().isEmpty()) {
                VBox imageBox = new VBox(10);
                imageBox.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 20; " +
                        "-fx-border-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                        "-fx-border-width: 1.5; " +
                        "-fx-border-radius: 12;");

                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
                try {
                    javafx.scene.image.Image image = new javafx.scene.image.Image(vehicle.getImage());
                    imageView.setImage(image);
                    imageView.setFitWidth(800);
                    imageView.setFitHeight(500);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle("-fx-background-radius: 8;");

                    imageBox.getChildren().add(imageView);
                } catch (Exception e) {
                    Label noImage = new Label("📷 Image non disponible");
                    noImage.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
                    imageBox.getChildren().add(noImage);
                }

                mainContainer.getChildren().add(imageBox);
            }

            // === DESCRIPTION ===
            VBox descBox = new VBox(10);
            descBox.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); " +
                    "-fx-background-radius: 12; " +
                    "-fx-padding: 25; " +
                    "-fx-border-color: linear-gradient(to right, #ec4899, #8b5cf6); " +
                    "-fx-border-width: 1.5; " +
                    "-fx-border-radius: 12;");

            Label descTitle = new Label("📝 Description");
            descTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

            Label descText = new Label(vehicle.getDescription());
            descText.setStyle("-fx-font-size: 15px; -fx-text-fill: #cbd5e1; -fx-line-spacing: 1.5;");
            descText.setWrapText(true);

            descBox.getChildren().addAll(descTitle, descText);

            // === BOUTONS D'ACTION ===
            HBox actionBox = new HBox(15);
            actionBox.setStyle("-fx-padding: 20; -fx-alignment: center;");

            Button contactBtn = new Button("💬 Contacter le vendeur");
            contactBtn.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #3b82f6); " +
                    "-fx-text-fill: white; " +
                    "-fx-padding: 15 30; " +
                    "-fx-background-radius: 8; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand; " +
                    "-fx-font-size: 16px;");
            contactBtn.setOnAction(e -> {
                System.out.println("💬 Ouverture du chat avec le vendeur ID: " + vehicle.getSellerId());
                // Log d'action
                clientLogService.logUserAction("CONTACT_SELLER", "Vendeur ID: " + vehicle.getSellerId());
                // TODO: Implémenter l'ouverture du chat
            });

            Button backBtn = new Button("◀ Retour");
            backBtn.setStyle("-fx-background-color: transparent; " +
                    "-fx-text-fill: #94a3b8; " +
                    "-fx-padding: 15 30; " +
                    "-fx-border-color: #475569; " +
                    "-fx-border-width: 1.5; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand; " +
                    "-fx-font-size: 16px;");
            backBtn.setOnAction(e -> showVehicles());

            actionBox.getChildren().addAll(backBtn, contactBtn);

            // Assembler tous les éléments
            mainContainer.getChildren().addAll(headerBox, descBox, actionBox);
            scrollPane.setContent(mainContainer);

            // Afficher dans la zone centrale
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(scrollPane);
                System.out.println("✅ Vue simple affichée avec succès");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur création vue simple: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails du véhicule");
            // Log d'erreur
            clientLogService.logError("SHOW_VEHICLE_DETAILS_SIMPLE", e.getMessage());
        }
    }

    /**
     * ✅ Récupère un véhicule depuis la base de données
     */
    private Vehicle getVehicleFromDatabase(int vehicleId) {
        try {
            // Utiliser VehicleDAO pour récupérer le véhicule
            VehicleDAO vehicleDAO = new VehicleDAO();
            Vehicle vehicle = vehicleDAO.getVehicleById(vehicleId);

            if (vehicle != null) {
                System.out.println("✅ Véhicule récupéré : " + vehicle.getTitle());
                // Log de succès
                clientLogService.logDataRetrieval("VEHICLE", vehicleId, true);
            } else {
                System.err.println("❌ Aucun véhicule trouvé avec l'ID : " + vehicleId);
                // Log d'erreur
                clientLogService.logDataRetrieval("VEHICLE", vehicleId, false);
            }

            return vehicle;

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération véhicule BD: " + e.getMessage());
            e.printStackTrace();
            // Log d'erreur
            clientLogService.logError("GET_VEHICLE_FROM_DB", e.getMessage());
            return null;
        }
    }

    private com.example.vehiclegestion.vendeur.model.Article convertVehicleToArticle(Vehicle vehicle) {
        com.example.vehiclegestion.vendeur.model.Article article = new com.example.vehiclegestion.vendeur.model.Article();

        article.setId(vehicle.getId());
        article.setTitre(vehicle.getTitle());
        article.setDescription(vehicle.getDescription());
        article.setPrix(vehicle.getPrice());
        article.setCategorie(vehicle.getCategory());
        article.setEtat(vehicle.getState());
        article.setImage(vehicle.getImage());
        article.setIdVendeur(vehicle.getSellerId());

        // Ajouter des valeurs par défaut pour les champs manquants
        article.setAnnee(2020);
        article.setKilometrage(50000);

        // Extraire marque/modèle du titre si possible
        String titre = vehicle.getTitle();
        if (titre != null && !titre.isEmpty()) {
            String[] parts = titre.split(" ", 2);
            article.setMarque(parts[0]);
            article.setModele(parts.length > 1 ? parts[1] : "Modèle");
        } else {
            article.setMarque("Marque");
            article.setModele("Modèle");
        }

        System.out.println("✅ Article créé : " + article.getTitre());
        return article;
    }

    private void openVehicleDetailInNewWindow(com.example.vehiclegestion.vendeur.model.Article article) {
        try {
            System.out.println("🪟 Ouverture dans une nouvelle fenêtre (fallback)");

            // Log de l'action
            clientLogService.logUIEvent("VEHICLE_DETAIL_NEW_WINDOW", "Fallback pour article ID: " + article.getId());

            URL fxmlUrl = getClass().getResource("/view/client/vehicleDetail.fxml");

            if (fxmlUrl == null) {
                System.err.println("❌ Fichier FXML introuvable");
                showAlert("Erreur", "Page de détails introuvable");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent detailRoot = loader.load();

            VehiDetaiCo controller = loader.getController();
            controller.receiveData(article);

            Stage detailStage = new Stage();
            detailStage.setTitle("Détails du véhicule - " + article.getTitre());
            detailStage.setScene(new Scene(detailRoot, 1200, 800));
            detailStage.show();

            System.out.println("✅ Fenêtre de détails ouverte");

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture fenêtre: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page de détails");
            // Log d'erreur
            clientLogService.logError("OPEN_VEHICLE_DETAIL_WINDOW", e.getMessage());
        }
    }

    private void markAllNotificationsAsRead() {
        if (!sessionManager.estConnecte() || notificationService == null) return;

        int userId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();
        notificationService.marquerToutesCommeLues(userId);

        loadNotificationDropdown();
        updateNotificationBadge();

        System.out.println("✅ Toutes les notifications marquées comme lues");

        // Log de l'action
        clientLogService.logUserAction("MARK_ALL_NOTIFICATIONS_READ_SUCCESS",
                "Utilisateur ID: " + userId);
    }

    private void showAllNotificationsPage() {
        System.out.println("📋 Navigation vers page notifications complète");

        // Log de l'action
        clientLogService.logUserAction("VIEW_ALL_NOTIFICATIONS", "Navigation vers page complète");

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
        if (!sessionManager.estConnecte() || notificationService == null) return;

        Platform.runLater(() -> {
            try {
                int userId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();
                int count = notificationService.getNombreNotificationsNonLues(userId);

                if (notificationBadge != null) {
                    if (count > 0) {
                        notificationBadge.setText(String.valueOf(count > 99 ? "99+" : count));
                        notificationBadge.setVisible(true);

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
                // Log d'erreur
                clientLogService.logError("UPDATE_NOTIFICATION_BADGE", e.getMessage());
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

    // ============================================
    // NAVIGATION PRINCIPALE
    // ============================================

    @FXML
    void openChatFromMenu() {
        System.out.println("\n💬 === OUVERTURE CHAT (Affichage central) ===");

        // Log de navigation
        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        if (currentUser != null) {
            clientLogService.logUserNavigation("CHAT", currentUser.getIdUtilisateur());
        }

        setActiveMenu(messagesBtn);
        loadContent("/view/common/ChatWindow.fxml");
    }

    @FXML
    private void showVehicles() {
        System.out.println("🚗 Navigation vers Véhicules");

        // Log de navigation
        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        if (currentUser != null) {
            clientLogService.logUserNavigation("VEHICLES", currentUser.getIdUtilisateur());
        }

        setActiveMenu(vehiclesBtn);
        loadContent("/view/client/vehicles-view.fxml");
    }

    @FXML
    private void showFavorites() {
        System.out.println("❤️ Navigation vers Favoris");

        // Log de navigation
        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        if (currentUser != null) {
            clientLogService.logUserNavigation("FAVORITES", currentUser.getIdUtilisateur());
        }

        setActiveMenu(favoritesBtn);
        loadContent("/view/client/ClientFavoritesView.fxml");
    }

    @FXML
    private void showHistory() {
        System.out.println("📊 Navigation vers Historique");

        // Log de navigation
        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        if (currentUser != null) {
            clientLogService.logUserNavigation("HISTORY", currentUser.getIdUtilisateur());
        }

        setActiveMenu(historyBtn);
        loadContent("/view/client/historique-client.fxml");
    }

    @FXML
    private void showProfile() {
        System.out.println("👤 Navigation vers Profil");

        // Log de navigation
        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        if (currentUser != null) {
            clientLogService.logUserNavigation("PROFILE", currentUser.getIdUtilisateur());
        }

        setActiveMenu(profileBtn);
        loadContent("/view/client/profile-view.fxml");
    }

    @FXML
    void logout() {
        System.out.println("🚪 Déconnexion");

        // Log de déconnexion
        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        if (currentUser != null) {
            clientLogService.logUserLogout(currentUser.getIdUtilisateur(), currentUser.getEmail());
        }

        if (notificationCheckTimeline != null) {
            notificationCheckTimeline.stop();
        }
        if (notificationRefreshTimeline != null) {
            notificationRefreshTimeline.stop();
        }
        if (hideSearchResultsTimer != null) {
            hideSearchResultsTimer.stop();
        }
        sessionManager.fermerSession();
        redirectToLogin();
    }

    private void setActiveMenu(Button activeButton) {
        if (vehiclesBtn != null) vehiclesBtn.setStyle(getInactiveStyle());
        if (messagesBtn != null) messagesBtn.setStyle(getInactiveStyle());
        if (favoritesBtn != null) favoritesBtn.setStyle(getInactiveStyle());
        if (historyBtn != null) historyBtn.setStyle(getInactiveStyle());
        if (profileBtn != null) profileBtn.setStyle(getInactiveStyle());

        if (activeButton != null) {
            activeButton.setStyle(getActiveStyle());
        }
    }

    private String getInactiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #cbd5e1; " +
                "-fx-font-size: 14px; -fx-font-weight: 500; -fx-padding: 20 25; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: transparent;";
    }

    private String getActiveStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #3b82f6; " +
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-padding: 20 25; " +
                "-fx-cursor: hand; -fx-border-width: 0 0 2 0; -fx-border-color: #3b82f6;";
    }

    public void loadContent(String fxmlPath) {
        try {
            System.out.println("📁 Chargement du contenu: " + fxmlPath);

            // Log du chargement de contenu
            clientLogService.logContentLoad(fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(content);
                System.out.println("✅ Contenu chargé avec succès: " + fxmlPath);

                // Log de succès
                clientLogService.logUISuccess("CONTENT_LOADED", fxmlPath);
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur de chargement: " + fxmlPath);
            e.printStackTrace();
            showErrorPage("Page non disponible: " + fxmlPath);

            // Log d'erreur
            clientLogService.logError("LOAD_CONTENT", "FXML: " + fxmlPath + " - " + e.getMessage());
        }
    }

    private void showErrorPage(String message) {
        VBox errorBox = new VBox();
        errorBox.setStyle("-fx-background-color: #0f172a; -fx-padding: 40; -fx-alignment: center; -fx-spacing: 15;");

        Label errorLabel = new Label("⚠️ " + message);
        errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1f5f9;");

        Label infoLabel = new Label("Cette page est en cours de développement");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");

        errorBox.getChildren().addAll(errorLabel, infoLabel);
        if (mainBorderPane != null) {
            mainBorderPane.setCenter(errorBox);
        }

        // Log d'erreur
        clientLogService.logError("SHOW_ERROR_PAGE", message);
    }

    private void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public void cleanup() {
        if (notificationCheckTimeline != null) {
            notificationCheckTimeline.stop();
        }
        if (notificationRefreshTimeline != null) {
            notificationRefreshTimeline.stop();
        }
        if (hideSearchResultsTimer != null) {
            hideSearchResultsTimer.stop();
        }

        // Fermer la fenêtre de recherche si elle est ouverte
        if (searchResultsStage != null && searchResultsStage.isShowing()) {
            searchResultsStage.close();
        }

        // Log de cleanup
        clientLogService.logUIEvent("CONTROLLER_CLEANUP", "Nettoyage des ressources");
    }

    // ============================================
    // CLASSE INTERNE POUR LES RÉSULTATS
    // ============================================
    private static class SearchResult {
        private final String icon;
        private final String category;
        private final String title;
        private final String description;
        private final Runnable action;
        private final int id;
        private final String type; // ✅ IMPORTANT : Ajouter ce champ

        public SearchResult(String icon, String category, String title,
                            String description, Runnable action, int id, String type) {
            this.icon = icon;
            this.category = category;
            this.title = title;
            this.description = description;
            this.action = action;
            this.id = id;
            this.type = type; // ✅ IMPORTANT
        }

        public String getIcon() { return icon; }
        public String getCategory() { return category; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public Runnable getAction() { return action; }
        public int getId() { return id; }
        public String getType() { return type; } // ✅ IMPORTANT
    }

    // Méthodes auxiliaires qui doivent rester inchangées
    private void debugButtons() {
        System.out.println("\n=== DEBUG BOUTONS ===");
        System.out.println("messagesBtn (menu inférieur): " + (messagesBtn != null ? "✅ OK" : "❌ NULL"));
        System.out.println("messagesIconBtn (icône enveloppe): " + (messagesIconBtn != null ? "✅ OK" : "❌ NULL"));

        if (messagesBtn != null) {
            System.out.println("messagesBtn onAction: " + messagesBtn.getOnAction());
        }
        if (messagesIconBtn != null) {
            System.out.println("messagesIconBtn onAction: " + messagesIconBtn.getOnAction());
        }
        System.out.println("===================\n");
    }

    private Stage findCurrentStage() {
        if (mainBorderPane != null && mainBorderPane.getScene() != null) {
            return (Stage) mainBorderPane.getScene().getWindow();
        }

        if (userNameLabel != null && userNameLabel.getScene() != null) {
            return (Stage) userNameLabel.getScene().getWindow();
        }

        return (Stage) Stage.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }

    public void loadContentNode(Parent content) {
        if (mainBorderPane != null) {
            mainBorderPane.setCenter(content);
            System.out.println("✅ Contenu chargé dans la zone centrale");
        } else {
            System.err.println("❌ mainBorderPane est null");
        }
    }

    public static MainController getInstance(BorderPane borderPane) {
        Object userData = borderPane.getUserData();
        if (userData instanceof MainController) {
            return (MainController) userData;
        }
        return null;
    }

    private void redirectToLogin() {
        try {
            System.out.println("🔄 Redirection vers la page de connexion...");

            // Log de redirection
            clientLogService.logUIEvent("REDIRECT_TO_LOGIN", "Déconnexion et redirection vers login");

            // Obtenir la fenêtre actuelle
            Stage currentStage = findCurrentStage();
            if (currentStage == null) {
                System.err.println("❌ Impossible de trouver la fenêtre actuelle");
                return;
            }

            // Charger la page de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
            Parent loginPage = loader.load();

            // Configurer la nouvelle scène
            Scene scene = new Scene(loginPage);
            currentStage.setScene(scene);
            currentStage.setTitle("Connexion - AutoSales Pro");
            currentStage.setMaximized(false);
            currentStage.setWidth(1000);
            currentStage.setHeight(700);
            currentStage.centerOnScreen();

            System.out.println("✅ Redirection vers login réussie");

        } catch (IOException e) {
            System.err.println("❌ Erreur lors de la redirection vers login: " + e.getMessage());
            e.printStackTrace();

            // Log d'erreur critique
            clientLogService.logError("REDIRECT_TO_LOGIN_FAILED", e.getMessage());

            // Fallback: afficher une alerte et fermer l'application
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de redirection");
            alert.setHeaderText("Impossible de charger la page de connexion");
            alert.setContentText("Veuillez redémarrer l'application.");
            alert.showAndWait();

            // Fermer l'application
            Platform.exit();
        }
    }

    /**
     * ✅ Affiche les détails d'un magasin depuis la recherche
     */
    private void showStoreDetails(int storeId) {
        System.out.println("🏪 Affichage détails magasin ID: " + storeId);

        // Log de navigation
        clientLogService.logStoreView(storeId, "STORE_DETAILS");

        hideSearchResults();
        searchField.clear();

        try {
            // Récupérer le magasin depuis la base de données
            MagasinDAO magasinDAO = new MagasinDAO();
            Magasin magasin = magasinDAO.getMagasinById(storeId);

            if (magasin == null) {
                System.err.println("❌ Magasin non trouvé ID: " + storeId);
                showAlert("Erreur", "Magasin introuvable");
                // Log d'erreur
                clientLogService.logError("STORE_NOT_FOUND", "ID: " + storeId);
                return;
            }

            System.out.println("✅ Magasin récupéré: " + magasin.getNomMagasin());

            // Charger la page de détails du magasin
            loadMagasinDetailPage(magasin);

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage magasin: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les détails du magasin");
            // Log d'erreur
            clientLogService.logError("SHOW_STORE_DETAILS", e.getMessage());
        }
    }

    /**
     * ✅ Charge la page de détails du magasin
     */
    private void loadMagasinDetailPage(Magasin magasin) {
        try {
            System.out.println("🏪 Chargement page détails pour: " + magasin.getNomMagasin());

            // Chercher le fichier FXML
            URL fxmlUrl = getClass().getResource("/view/vendeur/MagasinDetails.fxml");

            if (fxmlUrl == null) {
                System.err.println("❌ Fichier FXML MagasinDetails introuvable");
                // Fallback : Créer une vue simple
                showMagasinDetailsSimple(magasin);
                return;
            }

            // Charger le FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent detailContent = loader.load();

            // Obtenir le contrôleur
            com.example.vehiclegestion.vendeur.controller.MagasinDetailsController controller = loader.getController();

            // Passer les données au contrôleur
            controller.receiveData(magasin);

            // Afficher dans la zone centrale
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(detailContent);
                System.out.println("✅ Détails du magasin affichés dans la zone centrale");

                // Log de succès
                clientLogService.logUISuccess("STORE_DETAILS_LOADED", "ID: " + magasin.getIdMagasin());
            } else {
                System.err.println("❌ mainBorderPane est null");
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur chargement page détails magasin: " + e.getMessage());
            e.printStackTrace();

            // Log d'erreur
            clientLogService.logError("LOAD_MAGASIN_DETAIL_PAGE", e.getMessage());

            // Fallback vers une vue simple
            showMagasinDetailsSimple(magasin);
        }
    }

    /**
     * ✅ Vue simple pour afficher un magasin (fallback)
     */
    private void showMagasinDetailsSimple(Magasin magasin) {
        System.out.println("🎨 Création vue simple pour magasin: " + magasin.getNomMagasin());

        // Log de création de vue simple
        clientLogService.logUIEvent("STORE_SIMPLE_VIEW", "Création vue simple pour magasin ID: " + magasin.getIdMagasin());

        try {
            // Créer un ScrollPane
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: #0f172a;");

            // Conteneur principal
            VBox mainContainer = new VBox(25);
            mainContainer.setStyle("-fx-padding: 40; -fx-background-color: #0f172a;");

            // === HEADER ===
            VBox headerBox = new VBox(15);
            headerBox.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); " +
                    "-fx-background-radius: 12; " +
                    "-fx-padding: 30; " +
                    "-fx-border-color: linear-gradient(to right, #3b82f6, #8b5cf6); " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 12;");

            Label icon = new Label("🏪");
            icon.setStyle("-fx-font-size: 48px;");

            Label nomLabel = new Label(magasin.getNomMagasin());
            nomLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");
            nomLabel.setWrapText(true);

            Label categorieLabel = new Label(magasin.getCategorie() != null ?
                    "📁 " + magasin.getCategorie() : "");
            categorieLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");

            headerBox.getChildren().addAll(icon, nomLabel, categorieLabel);

            // === INFORMATIONS ===
            VBox infoBox = new VBox(15);
            infoBox.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); " +
                    "-fx-background-radius: 12; " +
                    "-fx-padding: 30; " +
                    "-fx-border-color: linear-gradient(to right, #ec4899, #8b5cf6); " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 12;");

            Label infoTitle = new Label("📍 Informations");
            infoTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

            VBox details = new VBox(10);

            if (magasin.getAdresse() != null) {
                Label adresse = new Label("📍 Adresse: " + magasin.getAdresse());
                adresse.setStyle("-fx-font-size: 15px; -fx-text-fill: #cbd5e1;");
                adresse.setWrapText(true);
                details.getChildren().add(adresse);
            }

            if (magasin.getLocalisation() != null) {
                Label localisation = new Label("🗺️ Localisation: " + magasin.getLocalisation());
                localisation.setStyle("-fx-font-size: 15px; -fx-text-fill: #cbd5e1;");
                details.getChildren().add(localisation);
            }

            if (magasin.getTelephone() != null) {
                Label telephone = new Label("📞 Téléphone: " + magasin.getTelephone());
                telephone.setStyle("-fx-font-size: 15px; -fx-text-fill: #cbd5e1;");
                details.getChildren().add(telephone);
            }

            if (magasin.getEmailContact() != null) {
                Label email = new Label("✉️ Email: " + magasin.getEmailContact());
                email.setStyle("-fx-font-size: 15px; -fx-text-fill: #cbd5e1;");
                details.getChildren().add(email);
            }

            infoBox.getChildren().addAll(infoTitle, details);

            // === DESCRIPTION ===
            if (magasin.getDescription() != null && !magasin.getDescription().isEmpty()) {
                VBox descBox = new VBox(10);
                descBox.setStyle("-fx-background-color: rgba(30, 41, 59, 0.9); " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 30; " +
                        "-fx-border-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 12;");

                Label descTitle = new Label("📝 Description");
                descTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");

                Label descText = new Label(magasin.getDescription());
                descText.setStyle("-fx-font-size: 15px; -fx-text-fill: #cbd5e1; -fx-line-spacing: 1.5;");
                descText.setWrapText(true);

                descBox.getChildren().addAll(descTitle, descText);
                mainContainer.getChildren().add(descBox);
            }

            // === BOUTONS ===
            HBox actionBox = new HBox(15);
            actionBox.setStyle("-fx-padding: 20; -fx-alignment: center;");

            Button backBtn = new Button("◀ Retour");
            backBtn.setStyle("-fx-background-color: transparent; " +
                    "-fx-text-fill: #94a3b8; " +
                    "-fx-padding: 15 30; " +
                    "-fx-border-color: #475569; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 8; " +
                    "-fx-background-radius: 8; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand; " +
                    "-fx-font-size: 16px;");
            backBtn.setOnAction(e -> showVehicles());

            Button vehiculesBtn = new Button("🚗 Voir les véhicules");
            vehiculesBtn.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #3b82f6); " +
                    "-fx-text-fill: white; " +
                    "-fx-padding: 15 30; " +
                    "-fx-background-radius: 8; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand; " +
                    "-fx-font-size: 16px;");
            vehiculesBtn.setOnAction(e -> {
                System.out.println("🚗 Filtrage véhicules du magasin ID: " + magasin.getIdMagasin());
                // Log d'action
                clientLogService.logUserAction("VIEW_STORE_VEHICLES", "Magasin ID: " + magasin.getIdMagasin());
                showMagasinArticles(magasin.getIdMagasin(), magasin.getNomMagasin());
            });

            actionBox.getChildren().addAll(backBtn, vehiculesBtn);

            // Assembler
            mainContainer.getChildren().addAll(headerBox, infoBox, actionBox);
            scrollPane.setContent(mainContainer);

            // Afficher
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(scrollPane);
                System.out.println("✅ Vue simple magasin affichée");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur création vue simple magasin: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails du magasin");
            // Log d'erreur
            clientLogService.logError("SHOW_MAGASIN_DETAILS_SIMPLE", e.getMessage());
        }
    }

    /**
     * ✅ MODIFIER showArticleDetails() pour utiliser showVehicleDetails()
     */
    private void showArticleDetails(int articleId) {
        System.out.println("📦 Affichage détails article ID: " + articleId);

        // Log de navigation
        clientLogService.logVehicleView(articleId, "ARTICLE_DETAILS");

        // Rediriger vers showVehicleDetails car c'est la même chose
        showVehicleDetails(articleId);
    }

    /**
     * ✅ MODIFIER showSellerDetails() - Version améliorée
     */
    private void showSellerDetails(int sellerId) {
        System.out.println("👨‍💼 Affichage détails vendeur ID: " + sellerId);

        // Log de navigation
        clientLogService.logSellerView(sellerId, "SELLER_DETAILS");

        hideSearchResults();
        searchField.clear();

        try {
            // Option 1 : Afficher le magasin du vendeur
            MagasinDAO magasinDAO = new MagasinDAO();
            Magasin magasin = magasinDAO.getMagasinByVendeurId(sellerId);

            if (magasin != null) {
                System.out.println("✅ Magasin du vendeur trouvé: " + magasin.getNomMagasin());
                showStoreDetails(magasin.getIdMagasin());
            } else {
                // Option 2 : Afficher les véhicules du vendeur
                System.out.println("ℹ️ Aucun magasin, affichage véhicules du vendeur");
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Vendeur");
                    alert.setHeaderText("Vendeur #" + sellerId);
                    alert.setContentText("Redirection vers les véhicules de ce vendeur...");
                    alert.showAndWait();

                    // Log d'action
                    clientLogService.logUserAction("VIEW_SELLER_VEHICLES", "Vendeur ID: " + sellerId);

                    // Filtrer par vendeur
                    showMagasinArticles(0, "Vendeur #" + sellerId); // 0 indique que c'est un vendeur sans magasin
                });
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage vendeur: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les informations du vendeur");
            // Log d'erreur
            clientLogService.logError("SHOW_SELLER_DETAILS", e.getMessage());
        }
    }

    /**
     * ✅ Modifier handleActionNavigation() pour gérer les résumés de ville
     */
    private void handleActionNavigation(String actionType) {
        if (actionType == null) return;

        // Log de navigation
        clientLogService.logUserNavigation(actionType,
                sessionManager.getUtilisateurConnecte() != null ?
                        sessionManager.getUtilisateurConnecte().getIdUtilisateur() : 0);

        hideSearchResults();
        searchField.clear();

        switch (actionType) {
            case "RESERVATIONS":
                showHistory();
                break;
            case "FAVORITES":
                showFavorites();
                break;
            case "PROFILE":
                showProfile();
                break;
            case "MESSAGES":
                openChatFromMenu();
                break;
            case "ARTICLES":
                showVehicles();
                break;
            default:
                // ✅ Si c'est une ville, relancer la recherche pour afficher tous les magasins
                if (actionType != null && !actionType.isEmpty()) {
                    System.out.println("🏙️ Recherche ville: " + actionType);
                    // Log de recherche de ville
                    clientLogService.logSearchEvent(actionType, "CITY_NAVIGATION");
                    performSearch(actionType);
                } else {
                    System.out.println("Action inconnue: " + actionType);
                }
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Affiche tous les magasins d'une ville dans une page dédiée
     */
    private void showCityStoresPage(String ville) {
        System.out.println("🏙️ Affichage page magasins de: " + ville);

        // Log de navigation
        clientLogService.logCityView(ville, "CITY_STORES_PAGE");

        hideSearchResults();
        searchField.clear();

        try {
            // Récupérer tous les magasins de la ville
            MagasinDAO magasinDAO = new MagasinDAO();
            List<Magasin> magasins = getMagasinsByCity(ville);

            if (magasins.isEmpty()) {
                showAlert("Aucun magasin", "Aucun magasin trouvé à " + ville);
                return;
            }

            // Créer la page
            createCityStoresView(ville, magasins);

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage magasins ville: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les magasins de " + ville);
            // Log d'erreur
            clientLogService.logError("SHOW_CITY_STORES_PAGE", e.getMessage());
        }
    }

    /**
     * ✅ DANS MainController - Modifier searchInDatabase() pour gérer les villes
     */
    private List<SearchResult> searchInDatabase(String query) {
        List<SearchResult> results = new ArrayList<>();

        if (searchService == null) {
            System.err.println("❌ SearchService non initialisé");
            // Log d'erreur
            clientLogService.logError("SEARCH_SERVICE_NOT_INIT", "Service de recherche non initialisé");
            return results;
        }

        try {
            // Test de connexion
            if (!searchService.testConnexion()) {
                System.err.println("❌ Connexion BD échouée");
                Platform.runLater(() -> {
                    showAlert("Erreur de connexion", "Impossible de se connecter à la base de données.");
                });
                // Log d'erreur
                clientLogService.logDatabaseError("SEARCH", "Connexion BD échouée");
                return results;
            }

            // ✅ VÉRIFIER SI C'EST UNE VILLE MAROCAINE
            if (isVilleMarocaine(query)) {
                System.out.println("🏙️ Recherche de ville détectée: " + query);

                // Log de recherche de ville
                clientLogService.logSearchEvent(query, "CITY_DETECTED");

                // ✅ AJOUTER UN RÉSULTAT SPÉCIAL POUR LA VILLE
                final String ville = query;
                results.add(new SearchResult(
                        "🏙️",
                        "Ville",
                        capitalizeWords(ville),
                        "Afficher tous les magasins de " + capitalizeWords(ville),
                        () -> showAllStoresInCity(ville),
                        0,
                        "CITY"
                ));

                // ✅ AJOUTER AUSSI LES MAGASINS DE LA VILLE DANS LE DROPDOWN
                List<SearchService.SearchResultData> magasinsVille = searchService.rechercherMagasinsParVille(query);

                for (SearchService.SearchResultData data : magasinsVille) {
                    final int storeId = data.getId();
                    results.add(new SearchResult(
                            data.getIcon(),
                            data.getCategory(),
                            data.getTitle(),
                            data.getDescription(),
                            () -> showStoreDetails(storeId),
                            data.getId(),
                            data.getType()
                    ));
                }

                System.out.println("✅ Trouvé " + magasinsVille.size() + " magasin(s) à " + ville);
                return results;
            }

            // ✅ Recherche normale si ce n'est pas une ville
            List<SearchService.SearchResultData> searchResults = searchService.rechercheGlobale(query);

            // Conversion des résultats
            for (SearchService.SearchResultData data : searchResults) {
                Runnable action = null;

                switch (data.getType()) {
                    case "ARTICLE":
                    case "VEHICLE":
                        final int articleId = data.getId();
                        action = () -> showArticleDetails(articleId);
                        break;

                    case "STORE":
                        final int storeId = data.getId();
                        action = () -> showStoreDetails(storeId);
                        break;

                    case "SELLER":
                        final int sellerId = data.getId();
                        action = () -> showSellerDetails(sellerId);
                        break;

                    case "ACTION":
                        action = () -> handleActionNavigation(data.getActionType());
                        break;

                    default:
                        action = () -> System.out.println("Action non définie");
                }

                results.add(new SearchResult(
                        data.getIcon(),
                        data.getCategory(),
                        data.getTitle(),
                        data.getDescription(),
                        action,
                        data.getId(),
                        data.getType()
                ));
            }

            System.out.println("✅ Recherche terminée: " + results.size() + " résultats trouvés");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la recherche: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Erreur de recherche", "Une erreur s'est produite lors de la recherche.");
            });
            // Log d'erreur
            clientLogService.logError("SEARCH_IN_DATABASE", e.getMessage());
        }

        return results;
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Affiche tous les magasins d'une ville dans l'écran principal
     */
    private void showAllStoresInCity(String ville) {
        System.out.println("🏙️ Affichage de tous les magasins de: " + ville);

        // Log de navigation
        clientLogService.logCityView(ville, "ALL_STORES_IN_CITY");

        hideSearchResults();
        searchField.clear();

        try {
            // Récupérer tous les magasins de la ville
            List<Magasin> magasins = getMagasinsByCity(ville);

            if (magasins.isEmpty()) {
                Platform.runLater(() -> {
                    showAlert("Aucun magasin", "Aucun magasin trouvé à " + capitalizeWords(ville));
                    // Log d'information
                    clientLogService.logInfo("NO_STORES_IN_CITY", "Ville: " + ville);
                    showVehicles(); // Retour à la page des véhicules
                });
                return;
            }

            // ✅ CRÉER LA VUE AVEC TOUS LES MAGASINS
            Platform.runLater(() -> {
                createCityStoresView(ville, magasins);
            });

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage magasins ville: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                showAlert("Erreur", "Impossible de charger les magasins de " + ville);
            });
            // Log d'erreur
            clientLogService.logError("SHOW_ALL_STORES_IN_CITY", e.getMessage());
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Récupère les magasins d'une ville avec toutes leurs infos
     */
    private List<Magasin> getMagasinsByCity(String ville) {
        List<Magasin> magasins = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT m.*, COUNT(a.id_article) as nb_articles " +
                    "FROM Magasin m " +
                    "LEFT JOIN Article a ON m.id_vendeur = a.id_vendeur " +
                    "WHERE LOWER(m.localisation) LIKE LOWER(?) " +
                    "GROUP BY m.id_magasin, m.nom_magasin, m.adresse, m.localisation, " +
                    "         m.categorie, m.description, m.telephone, m.email_contact, " +
                    "         m.id_vendeur, m.logo_magasin, m.site_web, m.facebook, " +
                    "         m.instagram, m.nb_ventes_mensuelles " +
                    "ORDER BY nb_articles DESC, m.nom_magasin";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + ville + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Magasin m = new Magasin();
                m.setIdMagasin(rs.getInt("id_magasin"));
                m.setNomMagasin(rs.getString("nom_magasin"));
                m.setAdresse(rs.getString("adresse"));
                m.setLocalisation(rs.getString("localisation"));
                m.setCategorie(rs.getString("categorie"));
                m.setDescription(rs.getString("description"));
                m.setTelephone(rs.getString("telephone"));
                m.setEmailContact(rs.getString("email_contact"));
                m.setIdVendeur(rs.getInt("id_vendeur"));
                m.setLogoMagasin(rs.getString("logo_magasin"));
                m.setSiteWeb(rs.getString("site_web"));
                m.setFacebook(rs.getString("facebook"));
                m.setInstagram(rs.getString("instagram"));
                m.setNbVentesMensuelles(rs.getInt("nb_ventes_mensuelles"));
                m.setNbCommentaires(rs.getInt("nb_articles")); // Utiliser temporairement ce champ

                magasins.add(m);
            }

            System.out.println("✅ Récupéré " + magasins.size() + " magasin(s) de " + ville);

            // Log de récupération des données
            clientLogService.logDataRetrieval("MAGASINS_BY_CITY", ville, magasins.size());

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération magasins: " + e.getMessage());
            e.printStackTrace();
            // Log d'erreur
            clientLogService.logError("GET_MAGASINS_BY_CITY", e.getMessage());
        }

        return magasins;
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Crée la vue complète avec tous les magasins de la ville
     */
    private void createCityStoresView(String ville, List<Magasin> magasins) {
        try {
            // ScrollPane principal - FOND NOIR UNIFORME
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: #0a0f1c; -fx-background: #0a0f1c;");

            // IMPORTANT: Désactiver la barre de défilement horizontale
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            // Conteneur principal - MÊME COULEUR QUE LE FOND
            VBox mainContainer = new VBox(15); // Espacement réduit entre cartes
            mainContainer.setStyle("-fx-background-color: #0a0f1c; -fx-padding: 20;");

            // IMPORTANT: Éviter les espaces blancs/bleus
            mainContainer.setBackground(Background.EMPTY);

            // === HEADER SIMPLIFIÉ ===
            VBox headerBox = new VBox(8);
            headerBox.setStyle("-fx-background-color: transparent; " + // Transparent
                    "-fx-padding: 0 0 15 0;"); // Pas de bordure ni de fond

            Label villeLabel = new Label("Magasins à " + capitalizeWords(ville));
            villeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; " +
                    "-fx-text-fill: #e2e8f0;");

            Label countLabel = new Label(magasins.size() + " magasin(s)");
            countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");

            headerBox.getChildren().addAll(villeLabel, countLabel);

            // === GRILLE DE MAGASINS ===
            VBox storesGrid = new VBox(10); // Espacement réduit entre cartes
            storesGrid.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

            // IMPORTANT: Pas de fond ni de bordure sur le conteneur
            storesGrid.setBackground(Background.EMPTY);

            // Créer une carte pour chaque magasin
            for (Magasin magasin : magasins) {
                VBox storeCard = createDetailedStoreCard(magasin);
                storesGrid.getChildren().add(storeCard);
            }

            // === BOUTON DE RETOUR SIMPLE ===
            HBox actionBox = new HBox();
            actionBox.setStyle("-fx-padding: 20 0 0 0; -fx-alignment: center-left;");

            Button backBtn = new Button("← Retour aux véhicules");
            backBtn.setStyle("-fx-background-color: transparent; " +
                    "-fx-text-fill: #60a5fa; " +
                    "-fx-padding: 8 16; " +
                    "-fx-border-color: #3b82f6; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-cursor: hand;");
            backBtn.setOnAction(e -> showVehicles());

            actionBox.getChildren().add(backBtn);

            // Assembler tout - IMPORTANT: dans l'ordre
            mainContainer.getChildren().addAll(headerBox, storesGrid, actionBox);
            scrollPane.setContent(mainContainer);

            // IMPORTANT: S'assurer que le viewport a la même couleur
            scrollPane.viewportBoundsProperty().addListener((obs, old, bounds) -> {
                Region viewport = (Region) scrollPane.lookup(".viewport");
                if (viewport != null) {
                    viewport.setStyle("-fx-background-color: #0a0f1c;");
                }
            });

            // Afficher dans l'écran principal
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(scrollPane);
                System.out.println("✅ Page ville affichée: " + magasins.size() + " magasins");

                // Log de succès
                clientLogService.logUISuccess("CITY_STORES_VIEW_CREATED",
                        "Ville: " + ville + ", Magasins: " + magasins.size());

                // S'assurer que le fond du BorderPane est uniforme
                mainBorderPane.setStyle("-fx-background-color: #0a0f1c;");

                // IMPORTANT: Masquer tous les en-têtes et filtres de la vue véhicules
                hideVehicleViewHeaders();
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur création page ville: " + e.getMessage());
            e.printStackTrace();
            // Log d'erreur
            clientLogService.logError("CREATE_CITY_STORES_VIEW", e.getMessage());
        }
    }

    /**
     * ✅ Masque les en-têtes de la vue véhicules - VERSION CORRIGÉE
     */
    private void hideVehicleViewHeaders() {
        System.out.println("🔄 Masquage des en-têtes de la vue véhicules...");

        // Log de l'action UI
        clientLogService.logUIEvent("HIDE_VEHICLE_HEADERS", "Masquage des en-têtes de la vue véhicules");

        if (mainBorderPane != null) {
            // Parcourir TOUS les nœuds de la scène
            mainBorderPane.getChildren().forEach(this::hideHeadersInNode);

            // Chercher spécifiquement dans le centre
            if (mainBorderPane.getCenter() != null) {
                hideHeadersInNode(mainBorderPane.getCenter());
            }
        }
    }

    /**
     * ✅ Méthode récursive pour masquer les en-têtes dans un nœud et ses enfants
     */
    private void hideHeadersInNode(Node node) {
        if (node == null) return;

        // Si c'est un Label avec le texte problématique
        if (node instanceof Label) {
            Label label = (Label) node;
            String text = label.getText();
            if (text != null) {
                String lowerText = text.toLowerCase();
                if (lowerText.contains("explore") ||
                        lowerText.contains("sélection") ||
                        lowerText.contains("premium") ||
                        lowerText.contains("trier")) {
                    System.out.println("✅ Masquage du label: " + text);
                    label.setVisible(false);
                    label.setManaged(false);
                    label.setText(""); // Vide le texte
                }
            }
        }

        // Si c'est un ComboBox de tri
        if (node instanceof ComboBox && "sortFilter".equals(node.getId())) {
            System.out.println("✅ Masquage du ComboBox de tri");
            node.setVisible(false);
            node.setManaged(false);
        }

        // Si c'est une HBox avec le style de la barre d'outils
        if (node instanceof HBox) {
            HBox hbox = (HBox) node;
            if (hbox.getStyle() != null && hbox.getStyle().contains("rgba(15, 23, 42, 0.9)")) {
                System.out.println("✅ Masquage de la HBox de la barre d'outils");
                hbox.setVisible(false);
                hbox.setManaged(false);
            }
        }

        // Si le nœud a des enfants, les parcourir aussi
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            parent.getChildrenUnmodifiable().forEach(this::hideHeadersInNode);
        }

        // Chercher aussi dans les lookup
        node.lookupAll("*").forEach(this::hideHeadersInNode);
    }

    /**
     * ✅ Crée une carte détaillée pour un magasin avec thème Carbon Blue
     */
    private VBox createDetailedStoreCard(Magasin magasin) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #111827; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 20; " +
                "-fx-border-color: #1f2937; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 8;");

        // En-tête
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label storeIcon = new Label("🏪");
        storeIcon.setStyle("-fx-font-size: 32px;");

        VBox nameBox = new VBox(3);
        Label nomLabel = new Label(magasin.getNomMagasin());
        nomLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f3f4f6;");
        nomLabel.setWrapText(true);

        if (magasin.getCategorie() != null && !magasin.getCategorie().isEmpty()) {
            Label catLabel = new Label(magasin.getCategorie());
            catLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af; " +
                    "-fx-background-color: #374151; -fx-padding: 2 8; " +
                    "-fx-background-radius: 4;");
            nameBox.getChildren().add(catLabel);
        }

        nameBox.getChildren().add(0, nomLabel);
        header.getChildren().addAll(storeIcon, nameBox);

        // Informations
        VBox infoBox = new VBox(8);
        infoBox.setStyle("-fx-padding: 10 0;");

        if (magasin.getAdresse() != null && !magasin.getAdresse().isEmpty()) {
            HBox addrRow = createInfoRow("📍", magasin.getAdresse());
            infoBox.getChildren().add(addrRow);
        }

        if (magasin.getLocalisation() != null && !magasin.getLocalisation().isEmpty()) {
            HBox locRow = createInfoRow("🗺️", magasin.getLocalisation());
            infoBox.getChildren().add(locRow);
        }

        if (magasin.getTelephone() != null && !magasin.getTelephone().isEmpty()) {
            HBox telRow = createInfoRow("📞", magasin.getTelephone());
            infoBox.getChildren().add(telRow);
        }

        if (magasin.getNbCommentaires() > 0) {
            HBox articlesRow = new HBox(5);
            articlesRow.setAlignment(Pos.CENTER_LEFT);
            Label articleIcon = new Label("📦");
            articleIcon.setStyle("-fx-font-size: 14px;");
            Label articleText = new Label(magasin.getNbCommentaires() + " articles disponibles");
            articleText.setStyle("-fx-text-fill: #60a5fa; -fx-font-size: 12px;");
            articlesRow.getChildren().addAll(articleIcon, articleText);
            infoBox.getChildren().add(articlesRow);
        }

        // Boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-padding: 10 0 0 0;");

        Button articlesBtn = new Button("Voir les articles");
        articlesBtn.setStyle("-fx-background-color: #2563eb; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 8 16; " +
                "-fx-background-radius: 6; " +
                "-fx-font-weight: 500; " +
                "-fx-cursor: hand;");
        articlesBtn.setOnAction(e -> {
            System.out.println("📦 Affichage des articles du magasin: " + magasin.getNomMagasin());
            // Log d'action
            clientLogService.logUserAction("VIEW_STORE_ARTICLES",
                    "Magasin: " + magasin.getNomMagasin() + " (ID: " + magasin.getIdMagasin() + ")");
            showMagasinArticles(magasin.getIdMagasin(), magasin.getNomMagasin());
        });

        Button detailsBtn = new Button("Détails");
        detailsBtn.setStyle("-fx-background-color: transparent; " +
                "-fx-text-fill: #9ca3af; " +
                "-fx-padding: 8 16; " +
                "-fx-border-color: #4b5563; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 6; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand;");
        detailsBtn.setOnAction(e -> showStoreDetails(magasin.getIdMagasin()));

        buttonBox.getChildren().addAll(articlesBtn, detailsBtn);

        // Assembler
        card.getChildren().addAll(header, infoBox, buttonBox);

        // Effet hover
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() +
                    "-fx-border-color: #3b82f6; " +
                    "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.2), 10, 0.5, 0, 2);");
        });

        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: #111827; " +
                    "-fx-background-radius: 8; " +
                    "-fx-padding: 20; " +
                    "-fx-border-color: #1f2937; " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 8;");
        });

        return card;
    }

    /**
     * ✅ Crée une ligne d'information simplifiée
     */
    private HBox createInfoRow(String emoji, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(emoji);
        iconLabel.setStyle("-fx-font-size: 14px; -fx-min-width: 20px;");

        Label valueText = new Label(value);
        valueText.setStyle("-fx-font-size: 13px; -fx-text-fill: #d1d5db;");
        valueText.setWrapText(true);

        row.getChildren().addAll(iconLabel, valueText);
        return row;
    }

    /**
     * ✅ Vérifie si la recherche est une ville marocaine
     */
    private boolean isVilleMarocaine(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        String lowerQuery = query.toLowerCase().trim();

        String[] villesMarocaines = {
                "rabat", "casablanca", "fès", "fes", "marrakech", "tanger", "agadir",
                "meknès", "meknes", "oujda", "kenitra", "tétouan", "tetouan", "safi",
                "mohammedia", "khouribga", "beni mellal", "el jadida", "taza", "nador",
                "settat", "ksar el kebir", "larache", "khemisset", "guelmim", "berrechid",
                "taourirt", "berkane", "sidi slimane", "errachidia", "sale", "salé",
                "sidi kacem", "khenifra", "tiznit", "tan-tan", "ouarzazate", "sefrou"
        };

        for (String ville : villesMarocaines) {
            if (lowerQuery.equals(ville) || lowerQuery.startsWith(ville + " ")) {
                return true;
            }
        }

        return false;
    }

    /**
     * ✅ Capitalise les mots d'une chaîne
     */
    private String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * ✅ Affiche les articles d'un magasin spécifique
     */
    private void showMagasinArticles(int magasinId, String magasinNom) {
        System.out.println("📦 Affichage des articles du magasin ID: " + magasinId + " - " + magasinNom);

        // Log de navigation
        clientLogService.logStoreView(magasinId, "STORE_ARTICLES");

        try {
            // Récupérer l'ID du vendeur associé au magasin
            int idVendeur = getVendeurIdFromMagasin(magasinId);

            if (idVendeur <= 0) {
                showAlert("Erreur", "Impossible de trouver les articles de ce magasin.");
                // Log d'erreur
                clientLogService.logError("SELLER_NOT_FOUND_FOR_STORE", "Magasin ID: " + magasinId);
                return;
            }

            System.out.println("✅ Vendeur trouvé ID: " + idVendeur + " pour le magasin: " + magasinNom);

            // Charger simplement la vue des véhicules
            // Le filtre sera appliqué dans le contrôleur
            loadMagasinArticlesView(idVendeur, magasinNom);

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage articles magasin: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les articles du magasin.");
            // Log d'erreur
            clientLogService.logError("SHOW_MAGASIN_ARTICLES", e.getMessage());
        }
    }

    /**
     * ✅ Récupère l'ID du vendeur à partir de l'ID du magasin
     */
    private int getVendeurIdFromMagasin(int magasinId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id_vendeur FROM Magasin WHERE id_magasin = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, magasinId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int vendeurId = rs.getInt("id_vendeur");
                // Log de récupération
                clientLogService.logDataRetrieval("VENDOR_ID_FOR_STORE", magasinId, vendeurId);
                return vendeurId;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur récupération vendeur: " + e.getMessage());
            e.printStackTrace();
            // Log d'erreur
            clientLogService.logError("GET_VENDOR_ID_FROM_STORE", e.getMessage());
        }
        return 0;
    }

    /**
     * ✅ Charge la vue des véhicules avec filtre par vendeur
     */
    private void loadMagasinArticlesView(int idVendeur, String magasinNom) {
        try {
            System.out.println("🚗 Chargement des articles pour le vendeur ID: " + idVendeur + " - " + magasinNom);

            // Log de chargement
            clientLogService.logUIEvent("LOAD_STORE_ARTICLES_VIEW",
                    "Vendeur ID: " + idVendeur + ", Magasin: " + magasinNom);

            // Charger la vue des véhicules
            String fxmlPath = "/view/client/vehicles-view.fxml";
            URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                System.err.println("❌ Fichier FXML introuvable: " + fxmlPath);
                showAlert("Erreur", "Page des véhicules introuvable.");
                // Log d'erreur
                clientLogService.logError("VEHICLES_VIEW_FXML_NOT_FOUND", fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent vehiclesView = loader.load();

            // Obtenir le contrôleur
            ClientVehiclesController controller = loader.getController();

            // Appliquer le filtre par vendeur
            controller.filterByVendeur(idVendeur, magasinNom);

            // Afficher dans la zone centrale
            if (mainBorderPane != null) {
                mainBorderPane.setCenter(vehiclesView);
                System.out.println("✅ Articles du magasin affichés: " + magasinNom);

                // Log de succès
                clientLogService.logUISuccess("STORE_ARTICLES_LOADED",
                        "Magasin: " + magasinNom + ", Vendeur ID: " + idVendeur);

                // **SUPPRIMÉ : pas d'en-tête "Retour à tous les articles"**
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement articles magasin: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les articles du magasin.");
            // Log d'erreur
            clientLogService.logError("LOAD_MAGASIN_ARTICLES_VIEW", e.getMessage());
        }
    }

}