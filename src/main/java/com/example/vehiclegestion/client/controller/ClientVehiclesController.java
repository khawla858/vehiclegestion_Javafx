package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.client.doa.VehicleDAO;
import com.example.vehiclegestion.client.doa.FavoriteDAO;
import com.example.vehiclegestion.client.doa.ReservationDAO;
import com.example.vehiclegestion.client.model.Vehicle;
import com.example.vehiclegestion.client.service.VehicleLogService;
import com.example.vehiclegestion.auth.model.Utilisateur;

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
import  com.example.vehiclegestion.auth.utils.SessionManager;

import java.net.URL;

import com.example.vehiclegestion.vendeur.model.Article;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ClientVehiclesController {

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
    private Utilisateur currentUser;

    // Service de logs
    private VehicleLogService vehicleLogService;

    private Map<Integer, Button> favoriteButtons = new HashMap<>();
    private boolean filtersVisible = false;
    private static final int FILTERS_WIDTH = 280;
    private static final int COLUMNS_WITH_FILTERS = 3;
    private static final int COLUMNS_WITHOUT_FILTERS = 4;

    @FXML
    public void initialize() {
        // Initialiser le service de logs
        vehicleLogService = new VehicleLogService();

        if (!sessionManager.estConnecte()) {
            vehicleLogService.logVehicleLoadError("Session invalide - utilisateur non connecté");
            showAlert("Erreur", "Session invalide");
            return;
        }

        currentUser = sessionManager.getUtilisateurConnecte();
        currentClientId = currentUser.getIdUtilisateur();

        System.out.println("🚗 Initialisation du contrôleur véhicules pour client ID: " + currentClientId);
        vehicleLogService.sendLog("INFO", "Initialisation ClientVehiclesController pour client: " + currentUser.getEmail());

        reservationDAO.updateExpiredReservations();

        initializeFilters();
        setupFilterAnimations();
        loadVehiclesFromDatabase();
        hideAllUnwantedHeaders();
        // RETIRER les écouteurs d'action automatiques sur les ComboBox
        // NE PAS ajouter brandFilter.setOnAction(e -> applyAllFilters());
        // NE PAS ajouter typeFilter.setOnAction(e -> applyAllFilters());
        // NE PAS ajouter priceFilter.setOnAction(e -> applyAllFilters());

        sortFilter.setOnAction(e -> sortVehicles());
        hideSortHeaderAndLabel();

        // RETIRER les écouteurs automatiques sur les champs de prix
        // Le filtrage se fera seulement quand on clique sur "Appliquer"
        // NE PAS ajouter les PropertyChangeListeners ici
    }
    private void setupFilterAnimations() {
        filterToggleBtn.setOnMouseEntered(e -> showFilters());
        filterToggleBtn.setOnMouseClicked(e -> toggleFilters());

        filterSidebar.setOnMouseEntered(e -> keepFiltersVisible());
        // filterSidebar.setOnMouseExited(e -> hideFiltersAfterDelay());
    }

    private void toggleFilters() {
        if (filtersVisible) {
            hideFilters();
        } else {
            showFilters();
        }
    }

    private void hideAllUnwantedHeaders() {
        System.out.println("🗑️ Masquage des en-têtes indésirables...");

        // 1. Masquer les labels "Explore notre sélection"
        if (vehiclesGrid != null && vehiclesGrid.getScene() != null) {
            Parent root = vehiclesGrid.getScene().getRoot();

            // Parcourir tous les labels
            root.lookupAll(".label").forEach(node -> {
                Label label = (Label) node;
                if (label.getText() != null) {
                    String text = label.getText().toLowerCase();
                    if (text.contains("explore") ||
                            text.contains("sélection") ||
                            text.contains("premium") ||
                            text.contains("trouvez") ||
                            text.contains("découvrez")) {
                        System.out.println("✅ Masqué: " + label.getText());
                        label.setVisible(false);
                        label.setManaged(false);
                    }
                }
            });

            // 2. Masquer les HBox contenant ces en-têtes
            root.lookupAll(".hbox").forEach(node -> {
                HBox hbox = (HBox) node;
                // Vérifier si cette HBox contient des labels avec ces textes
                boolean hasUnwantedHeader = false;
                for (javafx.scene.Node child : hbox.getChildren()) {
                    if (child instanceof Label) {
                        Label label = (Label) child;
                        if (label.getText() != null) {
                            String text = label.getText().toLowerCase();
                            if (text.contains("explore") ||
                                    text.contains("sélection") ||
                                    text.contains("premium")) {
                                hasUnwantedHeader = true;
                                break;
                            }
                        }
                    }
                }

                if (hasUnwantedHeader) {
                    System.out.println("✅ Masqué HBox avec en-tête indésirable");
                    hbox.setVisible(false);
                    hbox.setManaged(false);
                }
            });

            // 3. Masquer les VBox similaires
            root.lookupAll(".vbox").forEach(node -> {
                VBox vbox = (VBox) node;
                boolean hasUnwantedHeader = false;
                for (javafx.scene.Node child : vbox.getChildren()) {
                    if (child instanceof Label) {
                        Label label = (Label) child;
                        if (label.getText() != null) {
                            String text = label.getText().toLowerCase();
                            if (text.contains("explore") ||
                                    text.contains("sélection") ||
                                    text.contains("premium")) {
                                hasUnwantedHeader = true;
                                break;
                            }
                        }
                    }
                }

                if (hasUnwantedHeader) {
                    System.out.println("✅ Masqué VBox avec en-tête indésirable");
                    vbox.setVisible(false);
                    vbox.setManaged(false);
                }
            });
        }
    }
    private void hideSortHeaderAndLabel() {
        if (sortFilter != null && sortFilter.getParent() != null) {
            // Masquer la ComboBox de tri
            sortFilter.setVisible(false);
            sortFilter.setManaged(false);

            // Chercher et masquer le label "Trier par:"
            Parent parent = sortFilter.getParent();
            if (parent instanceof HBox) {
                HBox hbox = (HBox) parent;
                for (javafx.scene.Node node : hbox.getChildren()) {
                    if (node instanceof Label) {
                        Label label = (Label) node;
                        if (label.getText() != null &&
                                label.getText().contains("Trier")) {
                            label.setVisible(false);
                            label.setManaged(false);
                        }
                    }
                }
            }
        }

        // Masquer aussi la section HBox contenant le tri
        if (sortFilter != null && sortFilter.getScene() != null) {
            javafx.scene.Node node = sortFilter.getScene().lookup(".sort-header-container");
            if (node != null) {
                node.setVisible(false);
                node.setManaged(false);
            }
        }
    }
    private void showFilters() {
        if (!filtersVisible) {
            filtersVisible = true;
            filterSidebar.setMinWidth(FILTERS_WIDTH);
            filterSidebar.setMaxWidth(FILTERS_WIDTH);
            filterToggleBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                            "-fx-text-fill: #f1f5f9; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 15 5; " +
                            "-fx-cursor: hand; " +
                            "-fx-alignment: center; " +
                            "-fx-content-display: top; " +
                            "-fx-wrap-text: true;" +
                            "-fx-background-radius: 0;"
            );
            // Log de l'affichage des filtres
            vehicleLogService.logFiltersVisibilityToggle(currentUser.getEmail(),
                    (long) currentClientId, true);
            displayVehicles();
        }
    }

    private void hideFilters() {
        if (filtersVisible) {
            filtersVisible = false;
            filterSidebar.setMinWidth(0);
            filterSidebar.setMaxWidth(0);
            filterToggleBtn.setStyle(
                    "-fx-background-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                            "-fx-text-fill: #f1f5f9; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 15 5; " +
                            "-fx-cursor: hand; " +
                            "-fx-alignment: center; " +
                            "-fx-content-display: top; " +
                            "-fx-wrap-text: true;" +
                            "-fx-background-radius: 0;"
            );
            // Log du masquage des filtres
            vehicleLogService.logFiltersVisibilityToggle(currentUser.getEmail(),
                    (long) currentClientId, false);
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
                e.printStackTrace();
                vehicleLogService.sendLog("ERROR", "Erreur dans hideFiltersAfterDelay: " + e.getMessage());
            }
        }).start();
    }

    private void keepFiltersVisible() {
        // Les filtres restent visibles tant que la souris est dessus
    }

    private void initializeFilters() {
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
                "Plus récentes", "Prix croissant", "Prix décroissant",
                "Marque A-Z", "Les plus consultées", "Meilleures affaires"
        ));
        sortFilter.setValue("Plus récentes");

        minPriceField.setText("2000");
        maxPriceField.setText("15000");
    }

    private void loadVehiclesFromDatabase() {
        long startTime = System.currentTimeMillis();

        try {
            vehicles = vehicleDAO.getAllVehicles();
            long loadTime = System.currentTimeMillis() - startTime;

            if (vehicles.isEmpty()) {
                System.out.println("ℹ️ Aucun véhicule trouvé dans la base de données");
                vehicleLogService.logNoVehiclesFound();
                showAlert("Information", "Aucun véhicule n'est disponible pour le moment.");
            } else {
                System.out.println("✅ " + vehicles.size() + " véhicules chargés depuis la base de données");
                // Log du succès de chargement
                vehicleLogService.logVehicleLoadSuccess(vehicles.size());
            }

            filteredVehicles.setAll(vehicles);
            updateFiltersWithRealData();

            long displayStartTime = System.currentTimeMillis();
            displayVehicles();
            long displayTime = System.currentTimeMillis() - displayStartTime;

            // Log des performances d'affichage
            vehicleLogService.logDisplayPerformance(vehicles.size(), loadTime + displayTime, filtersVisible);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des véhicules: " + e.getMessage());
            // Log de l'erreur
            vehicleLogService.logVehicleLoadError(e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les véhicules depuis la base de données: " + e.getMessage());
        }
    }

    private void updateFiltersWithRealData() {
        ObservableList<String> brands = FXCollections.observableArrayList("Toutes les marques");
        ObservableList<String> types = FXCollections.observableArrayList("Tous les types");

        Map<String, Integer> brandCount = new HashMap<>();
        Map<String, Integer> typeCount = new HashMap<>();

        for (Vehicle vehicle : vehicles) {
            String brand = extractBrandFromTitle(vehicle.getTitle());
            brandCount.put(brand, brandCount.getOrDefault(brand, 0) + 1);

            if (vehicle.getCategory() != null) {
                typeCount.put(vehicle.getCategory(), typeCount.getOrDefault(vehicle.getCategory(), 0) + 1);
            }
        }

        // Ajouter les marques avec leur nombre
        for (Map.Entry<String, Integer> entry : brandCount.entrySet()) {
            brands.add(entry.getKey() + " (" + entry.getValue() + ")");
        }

        // Ajouter les types avec leur nombre
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            types.add(entry.getKey() + " (" + entry.getValue() + ")");
        }

        brandFilter.setItems(brands);
        typeFilter.setItems(types);

        // DEBUG: afficher ce qui a été trouvé
        System.out.println("\n📊 MARQUES DÉTECTÉES:");
        for (String brand : brands) {
            System.out.println("  - " + brand);
        }

        System.out.println("\n📊 TYPES DÉTECTÉS:");
        for (String type : types) {
            System.out.println("  - " + type);
        }

        updateResultsCount();
    }

    private String extractBrandFromTitle(String title) {
        if (title == null || title.isEmpty()) return "Autre";

        // Normaliser le titre
        String normalizedTitle = title.toLowerCase().trim();

        // Liste élargie des marques
        Map<String, String[]> brandVariations = new HashMap<>();
        brandVariations.put("toyota", new String[]{"toyota"});
        brandVariations.put("renault", new String[]{"renault"});
        brandVariations.put("peugeot", new String[]{"peugeot"});
        brandVariations.put("bmw", new String[]{"bmw"});
        brandVariations.put("mercedes", new String[]{"mercedes", "mercedes-benz", "benz"});
        brandVariations.put("audi", new String[]{"audi"});
        brandVariations.put("volkswagen", new String[]{"volkswagen", "vw"});
        brandVariations.put("ford", new String[]{"ford"});
        brandVariations.put("nissan", new String[]{"nissan"});
        brandVariations.put("hyundai", new String[]{"hyundai"});
        brandVariations.put("dacia", new String[]{"dacia"});
        brandVariations.put("kia", new String[]{"kia"});
        brandVariations.put("chevrolet", new String[]{"chevrolet", "chevy"});
        brandVariations.put("suzuki", new String[]{"suzuki"});
        brandVariations.put("citroen", new String[]{"citroen", "citroën"});
        brandVariations.put("opel", new String[]{"opel"});
        brandVariations.put("fiat", new String[]{"fiat"});
        brandVariations.put("seat", new String[]{"seat"});
        brandVariations.put("skoda", new String[]{"skoda", "škoda"});
        brandVariations.put("mazda", new String[]{"mazda"});
        brandVariations.put("mitsubishi", new String[]{"mitsubishi"});
        brandVariations.put("honda", new String[]{"honda"});
        brandVariations.put("volvo", new String[]{"volvo"});
        brandVariations.put("jeep", new String[]{"jeep"});

        for (Map.Entry<String, String[]> entry : brandVariations.entrySet()) {
            String brand = entry.getKey();
            String[] variations = entry.getValue();

            for (String variation : variations) {
                if (normalizedTitle.contains(variation)) {
                    // Capitaliser la première lettre
                    return brand.substring(0, 1).toUpperCase() + brand.substring(1);
                }
            }
        }

        // Si aucune marque connue n'est trouvée, prendre le premier mot
        String[] words = title.split("\\s+");
        if (words.length > 0) {
            String firstWord = words[0];
            // Nettoyer le mot (enlever la ponctuation)
            firstWord = firstWord.replaceAll("[^a-zA-Z0-9]", "");
            return firstWord.isEmpty() ? "Autre" : firstWord;
        }

        return "Autre";
    }

    private void displayVehicles() {
        vehiclesGrid.getChildren().clear();
        favoriteButtons.clear();

        if (filteredVehicles.isEmpty()) {
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

        for (Vehicle vehicle : filteredVehicles) {
            try {
                VBox vehicleCard = createModernVehicleCard(vehicle);
                vehiclesGrid.add(vehicleCard, column, row);

                column++;
                if (column >= columns) {
                    column = 0;
                    row++;
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur création carte pour: " + vehicle.getTitle());
                vehicleLogService.sendLog("ERROR", "Erreur création carte véhicule: " + e.getMessage());
                e.printStackTrace();
            }
        }

        updateResultsCount();
    }

    private VBox createModernVehicleCard(Vehicle vehicle) {
        VBox card = new VBox(0);
        int cardWidth = filtersVisible ? 280 : 300;
        card.setStyle(
                "-fx-background-color: #111827; " +
                        "-fx-border-color: #1f2937; " +
                        "-fx-border-radius: 8; " +
                        "-fx-border-width: 1; " +
                        "-fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.2, 0, 1);"
        );
        card.setPrefWidth(cardWidth);
        card.setMaxWidth(cardWidth);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Header avec info vendeur
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-padding: 12 16; " +
                        "-fx-background-color: #1f2937; " +
                        "-fx-background-radius: 8 8 0 0;"
        );

        StackPane avatar = new StackPane();
        avatar.setStyle(
                "-fx-background-color: " + getRandomColor() + "; " +
                        "-fx-background-radius: 20; " +
                        "-fx-min-width: 40; " +
                        "-fx-min-height: 40; " +
                        "-fx-max-width: 40; " +
                        "-fx-max-height: 40;"
        );

        String sellerInitials = getInitials(vehicle.getSellerName());
        Label avatarText = new Label(sellerInitials);
        avatarText.setStyle("-fx-text-fill: #f1f5f9; -fx-font-weight: bold; -fx-font-size: 14;");
        avatar.getChildren().add(avatarText);

        VBox vendorInfo = new VBox(2);
        Label vendorName = new Label(vehicle.getSellerName() != null ? vehicle.getSellerName() : "Vendeur");
        vendorName.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #f1f5f9;");

        Label timeAgo = new Label("il y a " + getTimeAgo(vehicle.getDateAdded()));
        timeAgo.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");

        vendorInfo.getChildren().addAll(vendorName, timeAgo);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label premiumBadge = new Label("⭐ PREMIUM");
        premiumBadge.setStyle(
                "-fx-background-color: linear-gradient(to right, #f59e0b, #d97706); " +
                        "-fx-text-fill: #f1f5f9; " +
                        "-fx-padding: 6 12; " +
                        "-fx-background-radius: 15; " +
                        "-fx-font-size: 11; " +
                        "-fx-font-weight: bold;"
        );

        header.getChildren().addAll(avatar, vendorInfo, spacer, premiumBadge);

        // Image du véhicule
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle(
                "-fx-background-color: rgba(15, 23, 42, 0.6); " +
                        "-fx-background-radius: 13 13 0 0;"
        );
        imageContainer.setPrefHeight(200);
        imageContainer.setMaxHeight(200);

        loadVehicleImage(vehicle, imageContainer);

        Label photoCount = new Label("📷 " + getRandomPhotoCount());
        photoCount.setStyle(
                "-fx-background-color: rgba(0,0,0,0.7); " +
                        "-fx-text-fill: #f1f5f9; " +
                        "-fx-padding: 6 12; " +
                        "-fx-background-radius: 15; " +
                        "-fx-font-size: 11;"
        );
        StackPane.setAlignment(photoCount, Pos.BOTTOM_LEFT);
        StackPane.setMargin(photoCount, new Insets(10));
        imageContainer.getChildren().add(photoCount);

        // Contenu de la carte
        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20;");

        HBox locationBox = new HBox(8);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        Label location = new Label(getRandomCity());
        location.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12;");
        locationBox.getChildren().addAll(locationIcon, location);

        Label title = new Label(vehicle.getTitle());
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");
        title.setWrapText(true);
        title.setMaxWidth(cardWidth - 40);

        String descriptionText = vehicle.getDescription() != null ?
                truncateDescription(vehicle.getDescription()) : "Aucune description disponible";
        Label description = new Label(descriptionText);
        description.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13;");
        description.setWrapText(true);
        description.setMaxWidth(cardWidth - 40);

        HBox specs = new HBox(15);
        specs.setAlignment(Pos.CENTER_LEFT);

        Label year = new Label("📅 " + extractYearFromTitle(vehicle.getTitle()));
        year.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12;");

        Label transmission = new Label("⚙️ " + getRandomTransmission());
        transmission.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12;");

        Label fuel = new Label("⛽ " + getRandomFuel());
        fuel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12;");

        specs.getChildren().addAll(year, transmission, fuel);

        // Footer avec prix, indicateur disponibilité et bouton favori
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle(
                "-fx-padding: 15 20; " +
                        "-fx-border-color: linear-gradient(to right, transparent, #334155, transparent); " +
                        "-fx-border-width: 1 0 0 0;"
        );

        VBox priceBox = new VBox(2);
        Label price = new Label(String.format("%,.0f DH", vehicle.getPrice()));
        price.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        double monthlyPrice = vehicle.getPrice() / 48;
        Label pricePerMonth = new Label("~" + String.format("%,.0f DH / mois", monthlyPrice));
        pricePerMonth.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");

        priceBox.getChildren().addAll(price, pricePerMonth);

        Region priceSpacer = new Region();
        HBox.setHgrow(priceSpacer, Priority.ALWAYS);

        // INDICATEUR DE DISPONIBILITÉ
        Button availabilityBtn = createAvailabilityButton(vehicle);

        // Bouton favori avec état dynamique
        Button favoriteBtn = createFavoriteButton(vehicle);
        favoriteButtons.put(vehicle.getId(), favoriteBtn);

        HBox buttonsContainer = new HBox(10);
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
        Button availabilityBtn = new Button();

        // Récupérer le statut réel depuis la base de données
        String statut = vehicle.getStatutVehicule() != null ? vehicle.getStatutVehicule().toLowerCase() : "disponible";
        boolean estReserve = reservationDAO.isVehiculeReserved(vehicle.getId());
        boolean estReserveParMoi = reservationDAO.hasClientReservedVehicule(currentClientId, vehicle.getId());

        // Logique d'affichage basée sur le statut réel
        switch (statut) {
            case "vendu":
                availabilityBtn.setText("⛔ VENDU");
                availabilityBtn.setStyle(
                        "-fx-background-color: linear-gradient(to right, #dc2626, #b91c1c); " +
                                "-fx-text-fill: #f1f5f9; -fx-font-weight: bold; -fx-font-size: 11; " +
                                "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                "-fx-cursor: default;"
                );
                break;

            case "reserve":
            case "réservé":
                if (estReserveParMoi) {
                    availabilityBtn.setText("⭐ VOTRE");
                    availabilityBtn.setStyle(
                            "-fx-background-color: linear-gradient(to right, #f59e0b, #d97706); " +
                                    "-fx-text-fill: #f1f5f9; -fx-font-weight: bold; -fx-font-size: 11; " +
                                    "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                    "-fx-cursor: default;"
                    );
                } else {
                    availabilityBtn.setText("🔒 RÉSERVÉ");
                    availabilityBtn.setStyle(
                            "-fx-background-color: linear-gradient(to right, #f59e0b, #d97706); " +
                                    "-fx-text-fill: #f1f5f9; -fx-font-weight: bold; -fx-font-size: 11; " +
                                    "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                    "-fx-cursor: default;"
                    );
                }
                break;

            case "en attente":
                availabilityBtn.setText("⏳ EN ATTENTE");
                availabilityBtn.setStyle(
                        "-fx-background-color: linear-gradient(to right, #f59e0b, #d97706); " +
                                "-fx-text-fill: #f1f5f9; -fx-font-weight: bold; -fx-font-size: 11; " +
                                "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                "-fx-cursor: default;"
                );
                break;

            case "indisponible":
                availabilityBtn.setText("🚫 INDISPONIBLE");
                availabilityBtn.setStyle(
                        "-fx-background-color: linear-gradient(to right, #64748b, #475569); " +
                                "-fx-text-fill: #f1f5f9; -fx-font-weight: bold; -fx-font-size: 11; " +
                                "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                "-fx-cursor: default;"
                );
                break;

            case "disponible":
            case "en stock":
            default:
                if (estReserve) {
                    availabilityBtn.setText("📝 EN COURS");
                    availabilityBtn.setStyle(
                            "-fx-background-color: linear-gradient(to right, #10b981, #059669); " +
                                    "-fx-text-fill: #f1f5f9; -fx-font-weight: bold; -fx-font-size: 11; " +
                                    "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                    "-fx-cursor: default;"
                    );
                } else {
                    availabilityBtn.setText("✅ DISPONIBLE");
                    availabilityBtn.setStyle(
                            "-fx-background-color: linear-gradient(to right, #10b981, #059669); " +
                                    "-fx-text-fill: #f1f5f9; -fx-font-weight: bold; -fx-font-size: 11; " +
                                    "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                    "-fx-cursor: default;"
                    );
                }
                break;
        }

        availabilityBtn.setDisable(true);
        return availabilityBtn;
    }

    private Button createFavoriteButton(Vehicle vehicle) {
        boolean isFav = favoriteDAO.isFavorite(currentClientId, vehicle.getId());

        Button favoriteBtn = new Button(isFav ? "❤️" : "🤍");
        favoriteBtn.setStyle(
                "-fx-background-color: " + (isFav ? "linear-gradient(to right, #ec4899, #8b5cf6)" : "rgba(255,255,255,0.1)") + "; " +
                        "-fx-text-fill: " + (isFav ? "#f1f5f9" : "#94a3b8") + "; " +
                        "-fx-font-size: 16px; -fx-padding: 6 8; " +
                        "-fx-background-radius: 20; -fx-cursor: hand; -fx-border-width: 0;"
        );

        favoriteBtn.setOnAction(e -> toggleFavorite(vehicle, favoriteBtn));

        return favoriteBtn;
    }

    private void toggleFavorite(Vehicle vehicle, Button button) {
        boolean isFav = favoriteDAO.isFavorite(currentClientId, vehicle.getId());

        try {
            if (isFav) {
                boolean success = favoriteDAO.removeFavorite(currentClientId, vehicle.getId());
                if (success) {
                    button.setText("🤍");
                    button.setStyle(
                            "-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: #94a3b8; " +
                                    "-fx-font-size: 16px; -fx-padding: 6 8; -fx-background-radius: 20; " +
                                    "-fx-cursor: hand; -fx-border-width: 0;"
                    );
                    // Log du retrait des favoris
                    vehicleLogService.logFavoriteToggle(currentUser.getEmail(),
                            (long) currentClientId, vehicle.getId(), vehicle.getTitle(), false);
                } else {
                    vehicleLogService.logFavoriteError(currentUser.getEmail(),
                            (long) currentClientId, vehicle.getId(), "Erreur lors du retrait des favoris");
                }
            } else {
                boolean success = favoriteDAO.addFavorite(currentClientId, vehicle.getId());
                if (success) {
                    button.setText("❤️");
                    button.setStyle(
                            "-fx-background-color: linear-gradient(to right, #ec4899, #8b5cf6); -fx-text-fill: #f1f5f9; " +
                                    "-fx-font-size: 16px; -fx-padding: 6 8; -fx-background-radius: 20; " +
                                    "-fx-cursor: hand; -fx-border-width: 0;"
                    );
                    // Log de l'ajout aux favoris
                    vehicleLogService.logFavoriteToggle(currentUser.getEmail(),
                            (long) currentClientId, vehicle.getId(), vehicle.getTitle(), true);
                } else {
                    vehicleLogService.logFavoriteError(currentUser.getEmail(),
                            (long) currentClientId, vehicle.getId(), "Erreur lors de l'ajout aux favoris");
                }
            }
        } catch (Exception e) {
            vehicleLogService.logFavoriteError(currentUser.getEmail(),
                    (long) currentClientId, vehicle.getId(), e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadVehicleImage(Vehicle vehicle, StackPane container) {
        if (vehicle.getImage() != null && !vehicle.getImage().trim().isEmpty()) {
            try {
                String imagePath = vehicle.getImage();
                File imageFile = new File(imagePath);

                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), true);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(filtersVisible ? 280 : 300);
                    imageView.setFitHeight(200);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);

                    imageView.setStyle(
                            "-fx-background-radius: 13 13 0 0; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0.5, 0, 3);"
                    );

                    image.errorProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            showDefaultImage(container);
                            vehicleLogService.logImageLoadError(vehicle.getId(), imagePath,
                                    "Erreur de chargement de l'image");
                        }
                    });

                    container.getChildren().add(0, imageView);
                } else {
                    showDefaultImage(container);
                    vehicleLogService.logImageLoadError(vehicle.getId(), imagePath,
                            "Fichier image non trouvé");
                }
            } catch (Exception e) {
                showDefaultImage(container);
                vehicleLogService.logImageLoadError(vehicle.getId(), vehicle.getImage(),
                        e.getMessage());
            }
        } else {
            showDefaultImage(container);
            vehicleLogService.sendLog("WARN", "Pas d'image pour le véhicule ID: " + vehicle.getId());
        }
    }

    private void setupCardHoverEffects(VBox card) {
        card.setOnMouseEntered(e ->
                card.setStyle(
                        "-fx-background-color: rgba(30, 41, 59, 0.95); " +
                                "-fx-border-color: linear-gradient(to bottom, #3b82f6, #1e40af); " +
                                "-fx-border-radius: 15; " +
                                "-fx-border-width: 2; " +
                                "-fx-background-radius: 15; " +
                                "-fx-effect: dropshadow(gaussian, rgba(59, 130, 246, 0.4), 20, 0.5, 0, 5);"
                )
        );
        card.setOnMouseExited(e ->
                card.setStyle(
                        "-fx-background-color: rgba(30, 41, 59, 0.8); " +
                                "-fx-border-color: linear-gradient(to bottom, #ec4899, #8b5cf6); " +
                                "-fx-border-radius: 15; " +
                                "-fx-border-width: 2; " +
                                "-fx-background-radius: 15; " +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0.5, 0, 5);"
                )
        );
    }

    @FXML
    private void applyAllFilters() {
        System.out.println("🔍 Application des filtres...");

        // Réinitialiser la liste filtrée avec tous les véhicules
        filteredVehicles.clear();
        filteredVehicles.addAll(vehicles);

        // Log du nombre initial
        System.out.println("📊 Véhicules avant filtrage: " + filteredVehicles.size());

        // 1. Filtre par marque
        if (brandFilter.getValue() != null && !brandFilter.getValue().equals("Toutes les marques")) {
            String selectedBrand = brandFilter.getValue();
            System.out.println("🔄 Filtre marque activé: " + selectedBrand);

            List<Vehicle> toRemove = new ArrayList<>();
            for (Vehicle v : filteredVehicles) {
                String vehicleBrand = extractBrandFromTitle(v.getTitle());
                System.out.println("   Véhicule: " + v.getTitle() + " -> Marque détectée: " + vehicleBrand);

                if (!selectedBrand.equalsIgnoreCase(vehicleBrand)) {
                    toRemove.add(v);
                }
            }
            filteredVehicles.removeAll(toRemove);

            System.out.println("✅ Après filtre marque: " + filteredVehicles.size() + " véhicules");
            vehicleLogService.logFilterApplied(currentUser.getEmail(),
                    (long) currentClientId, "MARQUE", selectedBrand);
        }

        // 2. Filtre par type
        if (typeFilter.getValue() != null && !typeFilter.getValue().equals("Tous les types")) {
            String selectedType = typeFilter.getValue();
            System.out.println("🔄 Filtre type activé: " + selectedType);

            List<Vehicle> toRemove = new ArrayList<>();
            for (Vehicle v : filteredVehicles) {
                String vehicleType = v.getCategory();
                if (vehicleType == null || !vehicleType.equalsIgnoreCase(selectedType)) {
                    toRemove.add(v);
                }
            }
            filteredVehicles.removeAll(toRemove);

            System.out.println("✅ Après filtre type: " + filteredVehicles.size() + " véhicules");
            vehicleLogService.logFilterApplied(currentUser.getEmail(),
                    (long) currentClientId, "TYPE", selectedType);
        }

        // 3. Filtre par plage de prix prédéfinie
        if (priceFilter.getValue() != null && !priceFilter.getValue().equals("Tous les prix")) {
            String selectedPriceRange = priceFilter.getValue();
            System.out.println("🔄 Filtre prix prédéfini activé: " + selectedPriceRange);

            List<Vehicle> toRemove = new ArrayList<>();
            for (Vehicle v : filteredVehicles) {
                if (!matchesPriceRange(v.getPrice(), selectedPriceRange)) {
                    toRemove.add(v);
                }
            }
            filteredVehicles.removeAll(toRemove);

            System.out.println("✅ Après filtre prix prédéfini: " + filteredVehicles.size() + " véhicules");
            vehicleLogService.logFilterApplied(currentUser.getEmail(),
                    (long) currentClientId, "PRIX_RANGE", selectedPriceRange);
        }

        // 4. Filtre par prix min/max
        try {
            double minPrice = minPriceField.getText().isEmpty() ? 0 : Double.parseDouble(minPriceField.getText().trim());
            double maxPrice = maxPriceField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPriceField.getText().trim());

            System.out.println("💰 Filtre prix min/max: " + minPrice + " - " + maxPrice);

            // Échanger si min > max
            if (minPrice > maxPrice) {
                double temp = minPrice;
                minPrice = maxPrice;
                maxPrice = temp;
                minPriceField.setText(String.valueOf((int)minPrice));
                maxPriceField.setText(String.valueOf((int)maxPrice));
            }

            if (minPrice > 0 || maxPrice < Double.MAX_VALUE) {
                List<Vehicle> toRemove = new ArrayList<>();
                for (Vehicle v : filteredVehicles) {
                    if (v.getPrice() < minPrice || v.getPrice() > maxPrice) {
                        toRemove.add(v);
                    }
                }
                filteredVehicles.removeAll(toRemove);

                System.out.println("✅ Après filtre prix min/max: " + filteredVehicles.size() + " véhicules");
                vehicleLogService.logFilterApplied(currentUser.getEmail(),
                        (long) currentClientId, "PRIX_MINMAX", minPrice + "-" + maxPrice);
            }
        } catch (NumberFormatException e) {
            System.err.println("❌ Format de prix invalide: " + e.getMessage());
            showAlert("Erreur de prix", "Veuillez entrer des valeurs numériques valides pour les prix.");
            vehicleLogService.sendLog("ERROR", "Format de prix invalide: " + e.getMessage());
        }

        // 5. Afficher les résultats
        System.out.println("🎯 Résultats finaux: " + filteredVehicles.size() + " véhicules");

        // Debug: afficher les véhicules restants
        for (Vehicle v : filteredVehicles) {
            System.out.println("   - " + v.getTitle() + " | " + v.getPrice() + " DH | " + v.getCategory());
        }

        displayVehicles();
    }

    @FXML
    private void resetFilters() {
        brandFilter.setValue("Toutes les marques");
        typeFilter.setValue("Tous les types");
        priceFilter.setValue("Tous les prix");
        minPriceField.setText("2000");
        maxPriceField.setText("15000");

        filteredVehicles.setAll(vehicles);

        // Log de la réinitialisation des filtres
        vehicleLogService.logFiltersReset(currentUser.getEmail(), (long) currentClientId);

        displayVehicles();
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
        if (sortBy != null) {
            switch (sortBy) {
                case "Prix croissant":
                    filteredVehicles.sort((v1, v2) -> Double.compare(v1.getPrice(), v2.getPrice()));
                    break;
                case "Prix décroissant":
                    filteredVehicles.sort((v1, v2) -> Double.compare(v2.getPrice(), v1.getPrice()));
                    break;
                case "Plus récentes":
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
            // Log du tri
            vehicleLogService.logSortApplied(currentUser.getEmail(), (long) currentClientId, sortBy);
            displayVehicles();
        }
    }

    private void showDefaultImage(StackPane container) {
        container.getChildren().clear();
        container.setStyle(
                "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                        "-fx-background-radius: 13 13 0 0;"
        );

        VBox placeholder = new VBox(5);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setStyle("-fx-padding: 30;");

        Label carIcon = new Label("🚗");
        carIcon.setStyle("-fx-font-size: 48px; -fx-text-fill: #f1f5f9;");

        Label noImageText = new Label("Image non disponible");
        noImageText.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");

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
        String[] cities = {"Casablanca", "Rabat", "Marrakech", "Fès", "Tanger", "Agadir", "El Jadida"};
        return cities[(int)(Math.random() * cities.length)];
    }

    private String extractYearFromTitle(String title) {
        if (title == null) return "2023";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(19|20)\\d{2}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(title);
        return matcher.find() ? matcher.group() : "2023";
    }

    private String getRandomTransmission() {
        String[] transmissions = {"Automatique", "Manuelle", "Séquentielle"};
        return transmissions[(int)(Math.random() * transmissions.length)];
    }

    private String getRandomFuel() {
        String[] fuels = {"Essence", "Diesel", "Hybride", "Électrique"};
        return fuels[(int)(Math.random() * fuels.length)];
    }

    private String getRandomColor() {
        String[] colors = {"#3b82f6", "#8b5cf6", "#ec4899", "#10b981", "#f59e0b", "#ef4444"};
        return colors[(int)(Math.random() * colors.length)];
    }

    private String truncateDescription(String description) {
        if (description == null) return "Description non disponible";
        return description.length() <= 80 ? description : description.substring(0, 77) + "...";
    }

    private void updateResultsCount() {
        resultsCount.setText(filteredVehicles.size() + " annonces");
    }

    private void viewVehicleDetails(Vehicle vehicle) {
        System.out.println("🎯 Affichage des détails pour: " + vehicle.getTitle());

        // Log de la consultation du véhicule
        vehicleLogService.logVehicleView(currentUser.getEmail(),
                (long) currentClientId, vehicle.getId(), vehicle.getTitle());

        try {
            // ✅ Trouver le MainController parent
            MainController mainController = findMainController();

            if (mainController == null) {
                System.err.println("❌ MainController introuvable, ouverture en fenêtre séparée");
                vehicleLogService.sendLog("WARN", "MainController introuvable - ouverture fenêtre séparée");
                viewVehicleDetailsInNewWindow(vehicle);
                return;
            }

            // ✅ Charger la vue des détails
            String fxmlPath = "/view/client/Vehicle-Detail.fxml";
            URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                String[] testPaths = {
                        "/com/example/vehiclegestion/view/client/Vehicle-Detail.fxml",
                        "/view/client/Vehicle-Detail.fxml",
                        "/client/Vehicle-Detail.fxml"
                };

                for (String path : testPaths) {
                    url = getClass().getResource(path);
                    if (url != null) {
                        fxmlPath = path;
                        break;
                    }
                }
            }

            if (url == null) {
                vehicleLogService.sendLog("ERROR", "Fichier FXML introuvable: Vehicle-Detail.fxml");
                throw new IOException("Fichier FXML introuvable: Vehicle-Detail.fxml");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent detailsView = loader.load();

            VehiDetaiCo controller = loader.getController();
            Article article = convertVehicleToArticle(vehicle);
            controller.receiveData(article);

            // ✅ Afficher dans la zone centrale du MainController
            mainController.loadContentNode(detailsView);

            System.out.println("✅ Détails affichés dans la zone centrale");

        } catch (Exception e) {
            System.err.println("❌ Erreur affichage détails: " + e.getMessage());
            vehicleLogService.sendLog("ERROR", "Erreur affichage détails: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'afficher les détails: " + e.getMessage());
        }
    }

    // ✅ Méthode pour trouver le MainController parent
    private MainController findMainController() {
        try {
            // Méthode 1: Depuis vehiclesGrid
            if (vehiclesGrid != null && vehiclesGrid.getScene() != null) {
                Parent root = vehiclesGrid.getScene().getRoot();
                if (root instanceof BorderPane) {
                    BorderPane borderPane = (BorderPane) root;
                    // MainController devrait être le contrôleur de la racine
                    Object userData = borderPane.getUserData();
                    if (userData instanceof MainController) {
                        return (MainController) userData;
                    }
                }
            }

            // Méthode 2: Parcourir la hiérarchie des nœuds
            Parent current = vehiclesGrid;
            while (current != null && current.getParent() != null) {
                current = current.getParent();
                if (current instanceof BorderPane) {
                    BorderPane bp = (BorderPane) current;
                    Object userData = bp.getUserData();
                    if (userData instanceof MainController) {
                        return (MainController) userData;
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erreur recherche MainController: " + e.getMessage());
            vehicleLogService.sendLog("ERROR", "Erreur recherche MainController: " + e.getMessage());
        }

        return null;
    }

    // ✅ Fallback: ouvrir dans une nouvelle fenêtre si MainController introuvable
    private void viewVehicleDetailsInNewWindow(Vehicle vehicle) {
        try {
            String fxmlPath = "/view/client/Vehicle-Detail.fxml";
            URL url = getClass().getResource(fxmlPath);

            if (url == null) {
                throw new IOException("Fichier FXML introuvable");
            }

            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();

            VehiDetaiCo controller = loader.getController();
            Article article = convertVehicleToArticle(vehicle);
            controller.receiveData(article);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Détails - " + vehicle.getTitle());
            stage.show();

            System.out.println("✅ Détails ouverts en fenêtre séparée");

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture fenêtre: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Article convertVehicleToArticle(Vehicle vehicle) {
        System.out.println("🔄 === CONVERSION VEHICLE → ARTICLE ===");
        System.out.println("   Vehicle ID: " + vehicle.getId());

        Article article = new Article();

        // ✅ CORRECTION CRITIQUE: Définir l'ID de l'article
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

        System.out.println("✅ Article converti - ID: " + article.getId() + ", Titre: " + article.getTitre());

        return article;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style de l'alerte pour correspondre au thème sombre
        alert.getDialogPane().setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.95); " +
                        "-fx-border-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;"
        );

        // Style du texte
        alert.getDialogPane().lookup(".content.label").setStyle(
                "-fx-text-fill: #f1f5f9; -fx-font-size: 14px;"
        );

        // Style des boutons
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                            "-fx-text-fill: #f1f5f9; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8 20; " +
                            "-fx-background-radius: 6;"
            );
        }

        alert.showAndWait();
    }

    // Méthode pour nettoyer les ressources (appelée lors de la fermeture)
    public void cleanup() {
        if (vehicleLogService != null) {
            vehicleLogService.shutdown();
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Filtre les véhicules par vendeur
     * @param idVendeur ID du vendeur à filtrer
     * @param magasinNom Nom du magasin pour l'affichage
     */
    public void filterByVendeur(int idVendeur, String magasinNom) {
        System.out.println("🔍 Filtrage par vendeur ID: " + idVendeur + " (" + magasinNom + ")");

        // ✅ ÉTAPE 1 : Filtrer les véhicules par vendeur
        List<Vehicle> filteredList = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            if (vehicle.getSellerId() == idVendeur) {
                filteredList.add(vehicle);
            }
        }

        // ✅ ÉTAPE 2 : Mettre à jour la liste filtrée
        filteredVehicles.clear();
        filteredVehicles.addAll(filteredList);

        // ✅ ÉTAPE 3 : Mettre à jour le compteur avec le nom du magasin
        if (resultsCount != null) {
            resultsCount.setText(filteredVehicles.size() + " annonces du magasin " + magasinNom);
        }

        // ✅ ÉTAPE 4 : Afficher les résultats
        displayVehicles();
        hideAllFilterHeaders();
        hideAllFilterHeaders();
        hideSortHeaderAndLabel();
        hideTopToolbar();
        // ✅ ÉTAPE 5 : Log
        vehicleLogService.logFilterApplied(currentUser.getEmail(),
                (long) currentClientId, "VENDEUR", "Magasin: " + magasinNom);

        System.out.println("✅ " + filteredVehicles.size() + " articles trouvés pour le vendeur ID: " + idVendeur);
    }

    /**
     * ✅ Masque complètement la barre d'outils supérieure avec "Trier par:"
     */
    private void hideTopToolbar() {
        if (vehiclesGrid != null && vehiclesGrid.getScene() != null) {
            // Chercher la HBox parente de la barre d'outils
            Parent root = vehiclesGrid.getScene().getRoot();
            root.lookupAll(".top-toolbar").forEach(node -> {
                node.setVisible(false);
                node.setManaged(false);
            });

            // Alternative : chercher par style
            root.lookupAll(".hbox").forEach(node -> {
                if (node.getStyle() != null &&
                        node.getStyle().contains("rgba(15, 23, 42, 0.9)")) {
                    node.setVisible(false);
                    node.setManaged(false);
                }
            });
        }
    }
    /**
     * ✅ NOUVELLE MÉTHODE : Retour à tous les véhicules (utilisée depuis l'en-tête)
     */
    public void resetToAllVehicles() {
        System.out.println("🔄 Retour à tous les véhicules");

        filteredVehicles.clear();
        filteredVehicles.addAll(vehicles);

        if (resultsCount != null) {
            resultsCount.setText(filteredVehicles.size() + " annonces");
        }

        displayVehicles();
    }
    /**
     * ✅ Masque tous les éléments d'en-tête de filtre
     */
    public void hideAllFilterHeaders() {
        // Masquer le compteur d'annonces
        if (resultsCount != null) {
            resultsCount.setVisible(false);
            resultsCount.setManaged(false);
            resultsCount.setText("");
        }

        // Masquer le tri
        if (sortFilter != null) {
            sortFilter.setVisible(false);
            sortFilter.setManaged(false);
        }

        // Masquer les labels "Trier par:"
        if (vehiclesGrid != null && vehiclesGrid.getScene() != null) {
            vehiclesGrid.getScene().getRoot().lookupAll(".label").forEach(node -> {
                Label label = (Label) node;
                if (label.getText() != null &&
                        (label.getText().contains("Trier") || label.getText().contains("annonces"))) {
                    label.setVisible(false);
                    label.setManaged(false);
                }
            });
        }

        System.out.println("✅ En-têtes de filtre masqués");
    }



    // Appeler cette méthode dans initialize() et dans filterByVendeur()



}