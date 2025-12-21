package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.client.doa.VehicleDAO;
import com.example.vehiclegestion.client.doa.FavoriteDAO;
import com.example.vehiclegestion.client.doa.ReservationDAO;
import com.example.vehiclegestion.client.model.Vehicle;
import com.example.vehiclegestion.auth.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

import com.example.vehiclegestion.common.dao.ChatDAO;
import com.example.vehiclegestion.common.model.Conversation;

import java.net.URL;

import com.example.vehiclegestion.vendeur.model.Article;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

// ========== IMPORTS LOGGING ==========
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientVehiclesController {

    // ========== LOGGER ==========
    private static final Logger logger = LoggerFactory.getLogger(ClientVehiclesController.class);

    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private ComboBox<String> brandFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> priceFilter;
    @FXML private ComboBox<String> sortFilter;
    @FXML private Label resultsCount;
    @FXML private GridPane vehiclesGrid;
    @FXML private VBox emptyState;
    @FXML private VBox filterSidebar;
    @FXML private Button filterToggleBtn;

    private List<Vehicle> vehicles = new ArrayList<>();
    private ObservableList<Vehicle> filteredVehicles = FXCollections.observableArrayList();
    private VehicleDAO vehicleDAO = new VehicleDAO();
    private FavoriteDAO favoriteDAO = new FavoriteDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    private SessionManager sessionManager = SessionManager.getInstance();
    private int currentClientId;

    private Map<Integer, Button> favoriteButtons = new HashMap<>();
    private boolean filtersVisible = false;
    private static final int FILTERS_WIDTH = 280;
    private static final int COLUMNS_WITH_FILTERS = 3;
    private static final int COLUMNS_WITHOUT_FILTERS = 4;

    @FXML
    public void initialize() {
        logger.info("=== INITIALISATION ClientVehiclesController ===");

        try {
            // VÃ©rification de la session
            if (!sessionManager.estConnecte()) {
                logger.warn("Tentative d'accÃ¨s sans session valide");
                showAlert("Erreur", "Session invalide");
                return;
            }

            currentClientId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();
            logger.info("Client connectÃ© - ID: {}", currentClientId);

            // Mise Ã  jour des rÃ©servations expirÃ©es
            logger.debug("Mise Ã  jour des rÃ©servations expirÃ©es...");
            reservationDAO.updateExpiredReservations();

            // Initialisation des composants
            initializeFilters();
            setupFilterAnimations();
            loadVehiclesFromDatabase();

            // Configuration des listeners
            setupFilterListeners();
            setupPriceFieldValidation();

            logger.info("Initialisation terminÃ©e avec succÃ¨s - {} vÃ©hicules chargÃ©s", vehicles.size());

        } catch (Exception e) {
            logger.error("Erreur critique lors de l'initialisation du contrÃ´leur", e);
            showAlert("Erreur", "Erreur lors de l'initialisation: " + e.getMessage());
        }
    }

    private void setupFilterListeners() {
        logger.debug("Configuration des listeners de filtres");
        brandFilter.setOnAction(e -> applyAllFilters());
        typeFilter.setOnAction(e -> applyAllFilters());
        priceFilter.setOnAction(e -> applyAllFilters());
        sortFilter.setOnAction(e -> sortVehicles());
    }

    private void setupPriceFieldValidation() {
        minPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                minPriceField.setText(newValue.replaceAll("[^\\d]", ""));
                logger.debug("Validation prix minimum: caractÃ¨res non numÃ©riques supprimÃ©s");
            }
            applyAllFilters();
        });

        maxPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                maxPriceField.setText(newValue.replaceAll("[^\\d]", ""));
                logger.debug("Validation prix maximum: caractÃ¨res non numÃ©riques supprimÃ©s");
            }
            applyAllFilters();
        });
    }

    private void setupFilterAnimations() {
        filterToggleBtn.setOnMouseEntered(e -> showFilters());
        filterToggleBtn.setOnMouseClicked(e -> toggleFilters());
        filterSidebar.setOnMouseEntered(e -> keepFiltersVisible());
        filterSidebar.setOnMouseExited(e -> hideFiltersAfterDelay());
    }

    private void toggleFilters() {
        logger.debug("Toggle filtres - Ã‰tat actuel: {}", filtersVisible ? "visible" : "cachÃ©");
        if (filtersVisible) {
            hideFilters();
        } else {
            showFilters();
        }
    }

    private void showFilters() {
        if (!filtersVisible) {
            logger.debug("Affichage de la barre de filtres");
            filtersVisible = true;
            filterSidebar.setMinWidth(FILTERS_WIDTH);
            filterSidebar.setMaxWidth(FILTERS_WIDTH);
            filterToggleBtn.setStyle("-fx-background-color: #e3f2fd; -fx-border-width: 0; -fx-font-size: 14; -fx-padding: 15 5; -fx-cursor: hand; -fx-text-fill: #0066FF; -fx-alignment: center; -fx-content-display: top; -fx-wrap-text: true;");
            displayVehicles();
        }
    }

    private void hideFilters() {
        if (filtersVisible) {
            logger.debug("Masquage de la barre de filtres");
            filtersVisible = false;
            filterSidebar.setMinWidth(0);
            filterSidebar.setMaxWidth(0);
            filterToggleBtn.setStyle("-fx-background-color: transparent; -fx-border-width: 0; -fx-font-size: 14; -fx-padding: 15 5; -fx-cursor: hand; -fx-text-fill: #666; -fx-alignment: center; -fx-content-display: top; -fx-wrap-text: true;");
            displayVehicles();
        }
    }

    private void hideFiltersAfterDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(500);
                javafx.application.Platform.runLater(() -> {
                    if (filtersVisible && !filterSidebar.isHover() && !filterToggleBtn.isHover()) {
                        hideFilters();
                    }
                });
            } catch (InterruptedException e) {
                logger.error("Erreur lors du dÃ©lai de masquage des filtres", e);
            }
        }).start();
    }

    private void keepFiltersVisible() {
        // Les filtres restent visibles tant que la souris est dessus
    }

    private void initializeFilters() {
        logger.debug("Initialisation des filtres");

        brandFilter.setItems(FXCollections.observableArrayList(
                "Toutes les marques", "Toyota", "Renault", "Peugeot", "BMW", "Mercedes",
                "Audi", "Volkswagen", "Ford", "Nissan", "Hyundai", "Dacia", "Kia", "Chevrolet", "Suzuki"
        ));
        brandFilter.setValue("Toutes les marques");

        typeFilter.setItems(FXCollections.observableArrayList(
                "Tous les types", "Berline", "SUV", "Compact", "Citadine", "Break",
                "Monospace", "Sportive", "Utilitaire", "4x4", "Cabriolet", "Moto"
        ));
        typeFilter.setValue("Tous les types");

        priceFilter.setItems(FXCollections.observableArrayList(
                "Tous les prix", "Moins de 50 000 DH", "50 000 - 100 000 DH",
                "100 000 - 200 000 DH", "200 000 - 500 000 DH", "Plus de 500 000 DH"
        ));
        priceFilter.setValue("Tous les prix");

        sortFilter.setItems(FXCollections.observableArrayList(
                "Plus rÃ©centes", "Prix croissant", "Prix dÃ©croissant",
                "Marque A-Z", "Les plus consultÃ©es", "Meilleures affaires"
        ));
        sortFilter.setValue("Plus rÃ©centes");

        minPriceField.setText("2000");
        maxPriceField.setText("15000");

        logger.debug("Filtres initialisÃ©s avec succÃ¨s");
    }

    private void loadVehiclesFromDatabase() {
        logger.info("=== CHARGEMENT DES VÃ‰HICULES DEPUIS LA BASE ===");

        try {
            long startTime = System.currentTimeMillis();
            vehicles = vehicleDAO.getAllVehicles();
            long loadTime = System.currentTimeMillis() - startTime;

            if (vehicles.isEmpty()) {
                logger.warn("Aucun vÃ©hicule trouvÃ© dans la base de donnÃ©es");
                showAlert("Information", "Aucun vÃ©hicule n'est disponible pour le moment.");
            } else {
                logger.info("âœ… {} vÃ©hicules chargÃ©s en {} ms", vehicles.size(), loadTime);

                // Log dÃ©taillÃ© du premier vÃ©hicule (debug)
                if (logger.isDebugEnabled() && !vehicles.isEmpty()) {
                    Vehicle firstVehicle = vehicles.get(0);
                    logger.debug("Exemple vÃ©hicule - ID: {}, Titre: {}, Prix: {} DH",
                            firstVehicle.getId(),
                            firstVehicle.getTitle(),
                            firstVehicle.getPrice());
                }
            }

            filteredVehicles.setAll(vehicles);
            updateFiltersWithRealData();
            displayVehicles();

        } catch (Exception e) {
            logger.error("âŒ ERREUR CRITIQUE lors du chargement des vÃ©hicules", e);
            showAlert("Erreur", "Impossible de charger les vÃ©hicules: " + e.getMessage());
        }
    }

    private void updateFiltersWithRealData() {
        logger.debug("Mise Ã  jour des filtres avec les donnÃ©es rÃ©elles");

        ObservableList<String> brands = FXCollections.observableArrayList("Toutes les marques");
        ObservableList<String> types = FXCollections.observableArrayList("Tous les types");

        for (Vehicle vehicle : vehicles) {
            String brand = extractBrandFromTitle(vehicle.getTitle());
            if (brand != null && !brands.contains(brand)) {
                brands.add(brand);
            }

            if (vehicle.getCategory() != null && !types.contains(vehicle.getCategory())) {
                types.add(vehicle.getCategory());
            }
        }

        brandFilter.setItems(brands);
        typeFilter.setItems(types);
        updateResultsCount();

        logger.debug("Filtres mis Ã  jour - {} marques, {} types", brands.size() - 1, types.size() - 1);
    }

    private String extractBrandFromTitle(String title) {
        if (title == null || title.isEmpty()) {
            logger.debug("Titre vide, marque par dÃ©faut: Autre");
            return "Autre";
        }

        String[] knownBrands = {"Toyota", "Renault", "Peugeot", "BMW", "Mercedes", "Audi",
                "Volkswagen", "Ford", "Nissan", "Hyundai", "Dacia", "Kia",
                "Chevrolet", "CitroÃ«n", "Opel", "Fiat", "Seat", "Skoda",
                "Mazda", "Mitsubishi", "Honda", "Suzuki", "Volvo", "Jeep"};

        for (String brand : knownBrands) {
            if (title.toLowerCase().contains(brand.toLowerCase())) {
                return brand;
            }
        }

        String[] words = title.split(" ");
        return words.length > 0 ? words[0] : "Autre";
    }

    private void displayVehicles() {
        logger.debug("=== AFFICHAGE DES VÃ‰HICULES ===");
        logger.debug("Nombre de vÃ©hicules Ã  afficher: {}", filteredVehicles.size());

        vehiclesGrid.getChildren().clear();
        favoriteButtons.clear();

        if (filteredVehicles.isEmpty()) {
            logger.info("Aucun vÃ©hicule Ã  afficher - affichage de l'Ã©tat vide");
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            vehiclesGrid.setVisible(false);
            updateResultsCount();
            return;
        }

        emptyState.setVisible(false);
        emptyState.setManaged(false);
        vehiclesGrid.setVisible(true);

        int column = 0;
        int row = 0;
        int columns = filtersVisible ? COLUMNS_WITH_FILTERS : COLUMNS_WITHOUT_FILTERS;

        logger.debug("Configuration grille - Colonnes: {}, Filtres visibles: {}", columns, filtersVisible);

        int displayedCount = 0;
        for (Vehicle vehicle : filteredVehicles) {
            try {
                VBox vehicleCard = createModernVehicleCard(vehicle);
                vehiclesGrid.add(vehicleCard, column, row);
                displayedCount++;

                column++;
                if (column >= columns) {
                    column = 0;
                    row++;
                }
            } catch (Exception e) {
                logger.error("Erreur lors de la crÃ©ation de la carte pour le vÃ©hicule ID: {} - {}",
                        vehicle.getId(), vehicle.getTitle(), e);
            }
        }

        logger.info("âœ… {} cartes de vÃ©hicules affichÃ©es avec succÃ¨s", displayedCount);
        updateResultsCount();
    }

    private VBox createModernVehicleCard(Vehicle vehicle) {
        logger.trace("CrÃ©ation carte pour vÃ©hicule ID: {} - {}", vehicle.getId(), vehicle.getTitle());

        VBox card = new VBox(0);
        int cardWidth = filtersVisible ? 280 : 300;
        card.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #e8e8e8; " +
                "-fx-border-radius: 8; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setPrefWidth(cardWidth);
        card.setMaxWidth(cardWidth);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Header avec info vendeur
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 12 15; " +
                "-fx-border-color: #f0f0f0; " +
                "-fx-border-width: 0 0 1 0; " +
                "-fx-background-color: white; " +
                "-fx-background-radius: 8 8 0 0;");

        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color: " + getRandomColor() + "; " +
                "-fx-background-radius: 20; " +
                "-fx-min-width: 35; " +
                "-fx-min-height: 35; " +
                "-fx-max-width: 35; " +
                "-fx-max-height: 35;");

        String sellerInitials = getInitials(vehicle.getSellerName());
        Label avatarText = new Label(sellerInitials);
        avatarText.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12;");
        avatar.getChildren().add(avatarText);

        VBox vendorInfo = new VBox(2);
        Label vendorName = new Label(vehicle.getSellerName() != null ? vehicle.getSellerName() : "Vendeur");
        vendorName.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #333;");

        Label timeAgo = new Label("il y a " + getTimeAgo(vehicle.getDateAdded()));
        timeAgo.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");

        vendorInfo.getChildren().addAll(vendorName, timeAgo);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label premiumBadge = new Label("â­ Premium");
        premiumBadge.setStyle("-fx-background-color: #FFF3E0; " +
                "-fx-text-fill: #FF9800; " +
                "-fx-padding: 4 8; " +
                "-fx-background-radius: 4; " +
                "-fx-font-size: 11; " +
                "-fx-font-weight: bold;");

        header.getChildren().addAll(avatar, vendorInfo, spacer, premiumBadge);

        // Image du vÃ©hicule
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #f5f5f5; " +
                "-fx-background-radius: 0;");
        imageContainer.setPrefHeight(180);
        imageContainer.setMaxHeight(180);

        loadVehicleImage(vehicle, imageContainer);

        Label photoCount = new Label("ðŸ“· " + getRandomPhotoCount());
        photoCount.setStyle("-fx-background-color: rgba(0,0,0,0.6); " +
                "-fx-text-fill: white; " +
                "-fx-padding: 5 10; " +
                "-fx-background-radius: 15; " +
                "-fx-font-size: 11;");
        StackPane.setAlignment(photoCount, Pos.BOTTOM_LEFT);
        StackPane.setMargin(photoCount, new Insets(10));
        imageContainer.getChildren().add(photoCount);

        // Contenu de la carte
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-background-radius: 0;");

        HBox locationBox = new HBox(5);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("ðŸ“");
        Label location = new Label(getRandomCity());
        location.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
        locationBox.getChildren().addAll(locationIcon, location);

        Label title = new Label(vehicle.getTitle());
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #333;");
        title.setWrapText(true);
        title.setMaxWidth(cardWidth - 30);

        String descriptionText = vehicle.getDescription() != null ?
                truncateDescription(vehicle.getDescription()) : "Aucune description disponible";
        Label description = new Label(descriptionText);
        description.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        description.setWrapText(true);
        description.setMaxWidth(cardWidth - 30);

        HBox specs = new HBox(15);
        specs.setAlignment(Pos.CENTER_LEFT);

        Label year = new Label("ðŸ“… " + extractYearFromTitle(vehicle.getTitle()));
        year.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        Label transmission = new Label("âš™ï¸ " + getRandomTransmission());
        transmission.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        Label fuel = new Label("â›½ " + getRandomFuel());
        fuel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        specs.getChildren().addAll(year, transmission, fuel);

        // Footer avec prix, disponibilitÃ© et favori
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-padding: 15 15 12 15; " +
                "-fx-border-color: #f0f0f0; " +
                "-fx-border-width: 1 0 0 0; " +
                "-fx-background-color: white; " +
                "-fx-background-radius: 0 0 8 8;");

        VBox priceBox = new VBox(2);
        Label price = new Label(String.format("%,.0f DH", vehicle.getPrice()));
        price.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #0066FF;");

        double monthlyPrice = vehicle.getPrice() / 48;
        Label pricePerMonth = new Label("~" + String.format("%,.0f DH / mois", monthlyPrice));
        pricePerMonth.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");

        priceBox.getChildren().addAll(price, pricePerMonth);

        Region priceSpacer = new Region();
        HBox.setHgrow(priceSpacer, Priority.ALWAYS);

        Button availabilityBtn = createAvailabilityButton(vehicle);
        Button favoriteBtn = createFavoriteButton(vehicle);
        favoriteButtons.put(vehicle.getId(), favoriteBtn);

        HBox buttonsContainer = new HBox(5);
        buttonsContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonsContainer.getChildren().addAll(availabilityBtn, favoriteBtn);

        footer.getChildren().addAll(priceBox, priceSpacer, buttonsContainer);

        content.getChildren().addAll(locationBox, title, description, specs);
        card.getChildren().addAll(header, imageContainer, content, footer);

        card.setOnMouseClicked(e -> {
            if (e.getTarget() != favoriteBtn && e.getTarget() != availabilityBtn &&
                    !favoriteBtn.getParent().equals(e.getTarget()) && !availabilityBtn.getParent().equals(e.getTarget())) {
                viewVehicleDetails(vehicle);
            }
        });

        setupCardHoverEffects(card);

        return card;
    }

    private Button createAvailabilityButton(Vehicle vehicle) {
        logger.trace("CrÃ©ation bouton disponibilitÃ© pour vÃ©hicule ID: {}", vehicle.getId());

        Button availabilityBtn = new Button();

        try {
            String statut = vehicle.getStatutVehicule() != null ? vehicle.getStatutVehicule().toLowerCase() : "disponible";
            boolean estReserve = reservationDAO.isVehiculeReserved(vehicle.getId());
            boolean estReserveParMoi = reservationDAO.hasClientReservedVehicule(currentClientId, vehicle.getId());

            logger.debug("VÃ©hicule ID {} - Statut: {}, RÃ©servÃ©: {}, Par moi: {}",
                    vehicle.getId(), statut, estReserve, estReserveParMoi);

            switch (statut) {
                case "vendu":
                    availabilityBtn.setText("â›” VENDU");
                    availabilityBtn.setStyle(
                            "-fx-background-color: linear-gradient(to bottom, #D32F2F, #B71C1C); " +
                                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; " +
                                    "-fx-padding: 8 12; -fx-background-radius: 15; " +
                                    "-fx-border-radius: 15; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(211,47,47,0.3), 4, 0, 0, 2); " +
                                    "-fx-cursor: default; -fx-border-color: #C62828; -fx-border-width: 1;"
                    );
                    break;

                case "reserve":
                case "rÃ©servÃ©":
                    if (estReserveParMoi) {
                        availabilityBtn.setText("â­ VOTRE RÃ‰SERVATION");
                        availabilityBtn.setStyle(
                                "-fx-background-color: linear-gradient(to bottom, #FFD54F, #FFB300); " +
                                        "-fx-text-fill: #5D4037; -fx-font-weight: bold; -fx-font-size: 10; " +
                                        "-fx-padding: 8 10; -fx-background-radius: 15; " +
                                        "-fx-border-radius: 15; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(255,183,0,0.3), 4, 0, 0, 2); " +
                                        "-fx-cursor: default; -fx-border-color: #FFA000; -fx-border-width: 1;"
                        );
                    } else {
                        availabilityBtn.setText("ðŸ”’ RÃ‰SERVÃ‰");
                        availabilityBtn.setStyle(
                                "-fx-background-color: linear-gradient(to bottom, #FFB74D, #FF9800); " +
                                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; " +
                                        "-fx-padding: 8 12; -fx-background-radius: 15; " +
                                        "-fx-border-radius: 15; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(255,152,0,0.3), 4, 0, 0, 2); " +
                                        "-fx-cursor: default; -fx-border-color: #F57C00; -fx-border-width: 1;"
                        );
                    }
                    break;

                case "en attente":
                    availabilityBtn.setText("â³ EN ATTENTE");
                    availabilityBtn.setStyle(
                            "-fx-background-color: linear-gradient(to bottom, #FFCC80, #FFA726); " +
                                    "-fx-text-fill: #5D4037; -fx-font-weight: bold; -fx-font-size: 10; " +
                                    "-fx-padding: 8 10; -fx-background-radius: 15; " +
                                    "-fx-border-radius: 15; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(255,167,38,0.3), 4, 0, 0, 2); " +
                                    "-fx-cursor: default; -fx-border-color: #FF9800; -fx-border-width: 1;"
                    );
                    break;

                case "indisponible":
                    availabilityBtn.setText("ðŸš« INDISPONIBLE");
                    availabilityBtn.setStyle(
                            "-fx-background-color: linear-gradient(to bottom, #90A4AE, #78909C); " +
                                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10; " +
                                    "-fx-padding: 8 10; -fx-background-radius: 15; " +
                                    "-fx-border-radius: 15; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(120,144,156,0.3), 4, 0, 0, 2); " +
                                    "-fx-cursor: default; -fx-border-color: #607D8B; -fx-border-width: 1;"
                    );
                    break;

                case "disponible":
                case "en stock":
                default:
                    if (estReserve) {
                        availabilityBtn.setText("ðŸ“ EN COURS");
                        availabilityBtn.setStyle(
                                "-fx-background-color: linear-gradient(to bottom, #81C784, #4CAF50); " +
                                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10; " +
                                        "-fx-padding: 8 10; -fx-background-radius: 15; " +
                                        "-fx-border-radius: 15; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(76,175,80,0.3), 4, 0, 0, 2); " +
                                        "-fx-cursor: default; -fx-border-color: #388E3C; -fx-border-width: 1;"
                        );
                    } else {
                        availabilityBtn.setText("âœ… DISPONIBLE");
                        availabilityBtn.setStyle(
                                "-fx-background-color: linear-gradient(to bottom, #66BB6A, #43A047); " +
                                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; " +
                                        "-fx-padding: 8 12; -fx-background-radius: 15; " +
                                        "-fx-border-radius: 15; " +
                                        "-fx-effect: dropshadow(gaussian, rgba(67,160,71,0.3), 4, 0, 0, 2); " +
                                        "-fx-cursor: default; -fx-border-color: #2E7D32; -fx-border-width: 1;"
                        );
                    }
                    break;
            }

            availabilityBtn.setDisable(true);

        } catch (Exception e) {
            logger.error("Erreur lors de la crÃ©ation du bouton disponibilitÃ© pour vÃ©hicule ID: {}", vehicle.getId(), e);
            availabilityBtn.setText("â“ STATUT INCONNU");
            availabilityBtn.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #666; -fx-font-size: 10; -fx-padding: 8 10;");
            availabilityBtn.setDisable(true);
        }

        return availabilityBtn;
    }

    private Button createFavoriteButton(Vehicle vehicle) {
        logger.trace("CrÃ©ation bouton favori pour vÃ©hicule ID: {}", vehicle.getId());

        try {
            boolean isFav = favoriteDAO.isFavorite(currentClientId, vehicle.getId());
            logger.debug("VÃ©hicule ID {} - Est favori: {}", vehicle.getId(), isFav);

            Button favoriteBtn = new Button("ðŸ›’");
            favoriteBtn.setStyle(
                    "-fx-background-color: " + (isFav ? "#4CAF50" : "#f5f5f5") + "; " +
                            "-fx-text-fill: " + (isFav ? "white" : "#666") + "; " +
                            "-fx-font-size: 16; -fx-padding: 6 8; " +
                            "-fx-background-radius: 20; -fx-cursor: hand; -fx-border-width: 0;"
            );

            favoriteBtn.setOnAction(e -> toggleFavorite(vehicle, favoriteBtn));

            return favoriteBtn;

        } catch (Exception e) {
            logger.error("Erreur lors de la crÃ©ation du bouton favori pour vÃ©hicule ID: {}", vehicle.getId(), e);
            Button errorBtn = new Button("ðŸ›’");
            errorBtn.setDisable(true);
            return errorBtn;
        }
    }

    private void toggleFavorite(Vehicle vehicle, Button button) {
        logger.info("Toggle favori pour vÃ©hicule ID: {} - Client ID: {}", vehicle.getId(), currentClientId);

        try {
            boolean isFav = favoriteDAO.isFavorite(currentClientId, vehicle.getId());

            if (isFav) {
                boolean success = favoriteDAO.removeFavorite(currentClientId, vehicle.getId());
                if (success) {
                    logger.info("âœ… Favori retirÃ© - VÃ©hicule ID: {}", vehicle.getId());
                    button.setText("ðŸ›’");
                    button.setStyle(
                            "-fx-background-color: #f5f5f5; -fx-text-fill: #666; " +
                                    "-fx-font-size: 16; -fx-padding: 6 8; -fx-background-radius: 20; " +
                                    "-fx-cursor: hand; -fx-border-width: 0;"
                    );
                } else {
                    logger.warn("âŒ Ã‰chec du retrait du favori - VÃ©hicule ID: {}", vehicle.getId());
                }
            } else {
                boolean success = favoriteDAO.addFavorite(currentClientId, vehicle.getId());
                if (success) {
                    logger.info("âœ… Favori ajoutÃ© - VÃ©hicule ID: {}", vehicle.getId());
                    button.setText("ðŸ›’");
                    button.setStyle(
                            "-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                                    "-fx-font-size: 16; -fx-padding: 6 8; -fx-background-radius: 20; " +
                                    "-fx-cursor: hand; -fx-border-width: 0;"
                    );
                } else {
                    logger.warn("âŒ Ã‰chec de l'ajout du favori - VÃ©hicule ID: {}", vehicle.getId());
                }
            }

        } catch (Exception e) {
            logger.error("Erreur lors du toggle favori - VÃ©hicule ID: " + vehicle.getId(), e);
            showAlert("Erreur", "Impossible de modifier les favoris");
        }
    }

    private void loadVehicleImage(Vehicle vehicle, StackPane container) {
        logger.trace("Chargement image pour vÃ©hicule ID: {}", vehicle.getId());

        if (vehicle.getImage() != null && !vehicle.getImage().trim().isEmpty()) {
            try {
                String imagePath = vehicle.getImage();
                File imageFile = new File(imagePath);

                if (imageFile.exists()) {
                    logger.debug("Image trouvÃ©e: {}", imagePath);
                    Image image = new Image(imageFile.toURI().toString(), true);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(filtersVisible ? 280 : 240);
                    imageView.setFitHeight(180);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);

                    image.errorProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            logger.warn("Erreur de chargement image pour vÃ©hicule ID: {}", vehicle.getId());
                            showDefaultImage(container);
                        }
                    });

                    container.getChildren().add(0, imageView);
                } else {
                    logger.debug("Image non trouvÃ©e sur le disque: {}", imagePath);
                    showDefaultImage(container);
                }
            } catch (Exception e) {
                logger.error("Exception lors du chargement de l'image pour vÃ©hicule ID: {}", vehicle.getId(), e);
                showDefaultImage(container);
            }
        } else {
            logger.debug("Pas d'image dÃ©finie pour vÃ©hicule ID: {}", vehicle.getId());
            showDefaultImage(container);
        }
    }

    private void setupCardHoverEffects(VBox card) {
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: white; -fx-border-color: #0066FF; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,102,255,0.2), 12, 0, 0, 4);"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-border-color: #e8e8e8; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"));
    }

    @FXML
    private void applyAllFilters() {
        logger.info("=== APPLICATION DES FILTRES ===");
        long startTime = System.currentTimeMillis();

        int initialCount = vehicles.size();
        filteredVehicles.setAll(vehicles);

        try {
            // Filtre marque
            if (brandFilter.getValue() != null && !brandFilter.getValue().equals("Toutes les marques")) {
                String selectedBrand = brandFilter.getValue();
                filteredVehicles.removeIf(v -> !extractBrandFromTitle(v.getTitle()).equals(selectedBrand));
                logger.debug("Filtre marque '{}' appliquÃ© - {} vÃ©hicules restants", selectedBrand, filteredVehicles.size());
            }

            // Filtre type
            if (typeFilter.getValue() != null && !typeFilter.getValue().equals("Tous les types")) {
                String selectedType = typeFilter.getValue();
                filteredVehicles.removeIf(v -> v.getCategory() == null || !v.getCategory().equals(selectedType));
                logger.debug("Filtre type '{}' appliquÃ© - {} vÃ©hicules restants", selectedType, filteredVehicles.size());
            }

            // Filtre gamme de prix
            if (priceFilter.getValue() != null && !priceFilter.getValue().equals("Tous les prix")) {
                String selectedRange = priceFilter.getValue();
                filteredVehicles.removeIf(v -> !matchesPriceRange(v.getPrice(), selectedRange));
                logger.debug("Filtre prix '{}' appliquÃ© - {} vÃ©hicules restants", selectedRange, filteredVehicles.size());
            }

            // Filtre prix min/max
            double minPrice = minPriceField.getText().isEmpty() ? 0 : Double.parseDouble(minPriceField.getText());
            double maxPrice = maxPriceField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPriceField.getText());

            if (minPrice > 0 || maxPrice < Double.MAX_VALUE) {
                filteredVehicles.removeIf(v -> v.getPrice() < minPrice || v.getPrice() > maxPrice);
                logger.debug("Filtre prix personnalisÃ© [{} - {}] appliquÃ© - {} vÃ©hicules restants",
                        minPrice, maxPrice, filteredVehicles.size());
            }

            long filterTime = System.currentTimeMillis() - startTime;
            logger.info("âœ… Filtres appliquÃ©s en {} ms - {} â†’ {} vÃ©hicules",
                    filterTime, initialCount, filteredVehicles.size());

            displayVehicles();

        } catch (NumberFormatException e) {
            logger.error("Erreur de format dans les champs de prix", e);
            showAlert("Erreur", "Format de prix invalide");
        } catch (Exception e) {
            logger.error("Erreur lors de l'application des filtres", e);
            showAlert("Erreur", "Erreur lors du filtrage: " + e.getMessage());
        }
    }

    @FXML
    private void resetFilters() {
        logger.info("RÃ©initialisation des filtres");

        brandFilter.setValue("Toutes les marques");
        typeFilter.setValue("Tous les types");
        priceFilter.setValue("Tous les prix");
        minPriceField.setText("2000");
        maxPriceField.setText("15000");

        filteredVehicles.setAll(vehicles);
        displayVehicles();

        logger.info("Filtres rÃ©initialisÃ©s - {} vÃ©hicules affichÃ©s", filteredVehicles.size());
    }

    private boolean matchesPriceRange(double price, String range) {
        switch (range) {
            case "Moins de 50 000 DH": return price < 50000;
            case "50 000 - 100 000 DH": return price >= 50000 && price <= 100000;
            case "100 000 - 200 000 DH": return price > 100000 && price <= 200000;
            case "200 000 - 500 000 DH": return price > 200000 && price <= 500000;
            case "Plus de 500 000 DH": return price > 500000;
            default: return true;
        }
    }

    @FXML
    private void sortVehicles() {
        String sortBy = sortFilter.getValue();
        logger.info("Tri des vÃ©hicules par: {}", sortBy);

        if (sortBy != null) {
            try {
                long startTime = System.currentTimeMillis();

                switch (sortBy) {
                    case "Prix croissant":
                        filteredVehicles.sort((v1, v2) -> Double.compare(v1.getPrice(), v2.getPrice()));
                        break;
                    case "Prix dÃ©croissant":
                        filteredVehicles.sort((v1, v2) -> Double.compare(v2.getPrice(), v1.getPrice()));
                        break;
                    case "Plus rÃ©centes":
                        filteredVehicles.sort((v1, v2) -> {
                            if (v1.getDateAdded() == null) return -1;
                            if (v2.getDateAdded() == null) return 1;
                            return v2.getDateAdded().compareTo(v1.getDateAdded());
                        });
                        break;
                    case "Marque A-Z":
                        filteredVehicles.sort((v1, v2) -> extractBrandFromTitle(v1.getTitle()).compareTo(extractBrandFromTitle(v2.getTitle())));
                        break;
                }

                long sortTime = System.currentTimeMillis() - startTime;
                logger.info("âœ… Tri effectuÃ© en {} ms", sortTime);
                displayVehicles();

            } catch (Exception e) {
                logger.error("Erreur lors du tri des vÃ©hicules", e);
            }
        }
    }

    private void viewVehicleDetails(Vehicle vehicle) {
        logger.info("=== OUVERTURE DÃ‰TAILS VÃ‰HICULE ===");
        logger.info("VÃ©hicule ID: {} - Titre: {}", vehicle.getId(), vehicle.getTitle());

        try {
            String fxmlPath = "/view/client/Vehicle-Detail.fxml";
            logger.debug("Chemin FXML: {}", fxmlPath);

            URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                logger.warn("Fichier FXML non trouvÃ©, tentative de chemins alternatifs");
                String[] testPaths = {
                        "/com/example/vehiclegestion/view/client/Vehicle-Detail.fxml",
                        "/view/client/Vehicle-Detail.fxml",
                        "/client/Vehicle-Detail.fxml",
                        "Vehicle-Detail.fxml"
                };

                for (String path : testPaths) {
                    url = getClass().getResource(path);
                    logger.debug("Test '{}' â†’ {}", path, (url != null ? "TROUVÃ‰" : "NON TROUVÃ‰"));
                    if (url != null) {
                        fxmlPath = path;
                        break;
                    }
                }
            }

            if (url == null) {
                logger.error("âŒ Fichier FXML introuvable: Vehicle-Detail.fxml");
                throw new IOException("Fichier FXML introuvable: Vehicle-Detail.fxml");
            }

            logger.info("âœ… Chargement FXML depuis: {}", fxmlPath);

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            logger.debug("âœ… FXML chargÃ©");

            VehiDetaiCo controller = loader.getController();
            logger.debug("âœ… ContrÃ´leur rÃ©cupÃ©rÃ©: {}", controller.getClass().getSimpleName());

            Article article = convertVehicleToArticle(vehicle);
            logger.debug("âœ… Vehicle converti en Article - ID: {}", article.getId());

            controller.receiveData(article);
            logger.debug("âœ… DonnÃ©es transmises au contrÃ´leur");

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("DÃ©tails du vÃ©hicule - " + vehicle.getTitle());
            stage.show();

            logger.info("ðŸŽ‰ FenÃªtre de dÃ©tails ouverte avec succÃ¨s");

        } catch (IOException e) {
            logger.error("âŒ Erreur IO lors de l'ouverture des dÃ©tails", e);
            showAlert("Erreur", "Impossible d'ouvrir les dÃ©tails: " + e.getMessage());
        } catch (Exception e) {
            logger.error("âŒ Erreur critique lors de l'ouverture des dÃ©tails", e);
            showAlert("Erreur", "Erreur inattendue: " + e.getMessage());
        }
    }

    private Article convertVehicleToArticle(Vehicle vehicle) {
        logger.debug("Conversion Vehicle â†’ Article - ID: {}", vehicle.getId());

        Article article = new Article();
        article.setId(vehicle.getId());
        article.setTitre(vehicle.getTitle());
        article.setPrix(vehicle.getPrice());
        article.setDescription(vehicle.getDescription());
        article.setImage(vehicle.getImage());
        article.setCategorie(vehicle.getCategory());
        article.setAnnee(2023);
        article.setKilometrage(50000);
        article.setTransmission("Manuelle");
        article.setCarburant("Essence");
        article.setMarque(extractBrandFromTitle(vehicle.getTitle()));
        article.setModele(vehicle.getTitle());
        article.setPuissance(120);
        article.setEtat("Excellent");

        logger.debug("âœ… Article crÃ©Ã© - ID: {}, Titre: {}", article.getId(), article.getTitre());
        return article;
    }

    private void showAlert(String title, String message) {
        logger.debug("Affichage alerte - Titre: {}, Message: {}", title, message);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========== MÃ‰THODES UTILITAIRES ==========

    private void showDefaultImage(StackPane container) {
        container.getChildren().clear();
        container.setStyle("-fx-background-color: " + getRandomLightColor() + "; -fx-background-radius: 8;");

        VBox placeholder = new VBox(5);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setStyle("-fx-padding: 20;");

        Label carIcon = new Label("ðŸš—");
        carIcon.setStyle("-fx-font-size: 48;");

        Label noImageText = new Label("Aucune image");
        noImageText.setStyle("-fx-text-fill: #999; -fx-font-size: 12;");

        placeholder.getChildren().addAll(carIcon, noImageText);
        container.getChildren().add(placeholder);
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "V";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return String.valueOf(parts[0].charAt(0)) + parts[1].charAt(0);
        }
        return String.valueOf(name.charAt(0));
    }

    private String getTimeAgo(java.time.LocalDateTime date) {
        if (date == null) return "quelques jours";
        java.time.Duration duration = java.time.Duration.between(date, java.time.LocalDateTime.now());
        long hours = duration.toHours();
        if (hours < 1) return "moins d'1 heure";
        if (hours < 24) return hours + " heure" + (hours > 1 ? "s" : "");
        if (hours < 168) return (hours / 24) + " jour" + (hours / 24 > 1 ? "s" : "");
        return (hours / 168) + " semaine" + (hours / 168 > 1 ? "s" : "");
    }

    private int getRandomPhotoCount() {
        return (int)(Math.random() * 15) + 3;
    }

    private String getRandomCity() {
        String[] cities = {"Casablanca", "Rabat", "Marrakech", "FÃ¨s", "Tanger", "Agadir", "El Jadida"};
        return cities[(int)(Math.random() * cities.length)];
    }

    private String extractYearFromTitle(String title) {
        if (title == null) return "2023";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(19|20)\\d{2}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(title);
        return matcher.find() ? matcher.group() : "2023";
    }

    private String getRandomTransmission() {
        String[] transmissions = {"Automatique", "Manuelle", "SÃ©quentielle"};
        return transmissions[(int)(Math.random() * transmissions.length)];
    }

    private String getRandomFuel() {
        String[] fuels = {"Essence", "Diesel", "Hybride", "Ã‰lectrique"};
        return fuels[(int)(Math.random() * fuels.length)];
    }

    private String getRandomColor() {
        String[] colors = {"#FF6B35", "#0066FF", "#00C853", "#FF4081", "#9C27B0", "#FF9800"};
        return colors[(int)(Math.random() * colors.length)];
    }

    private String getRandomLightColor() {
        String[] colors = {"#E3F2FD", "#F3E5F5", "#E8F5E8", "#FFF3E0", "#FCE4EC", "#E0F2F1"};
        return colors[(int)(Math.random() * colors.length)];
    }

    private String truncateDescription(String description) {
        if (description == null) return "Description non disponible";
        return description.length() <= 80 ? description : description.substring(0, 77) + "...";
    }

    private void updateResultsCount() {
        resultsCount.setText(filteredVehicles.size() + " annonces");
    }
}