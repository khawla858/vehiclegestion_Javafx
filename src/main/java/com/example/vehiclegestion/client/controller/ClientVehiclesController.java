package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.client.doa.VehicleDAO;
import com.example.vehiclegestion.client.doa.FavoriteDAO;
import com.example.vehiclegestion.client.doa.ReservationDAO;
import com.example.vehiclegestion.client.model.Vehicle;
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

public class ClientVehiclesController {

    @FXML private TextField minPriceField;
    @FXML private TextField maxPriceField;
    @FXML private RadioButton particulierRadio;
    @FXML private RadioButton professionnelRadio;
    @FXML private ComboBox<String> brandFilter;
    @FXML private ComboBox<String> typeFilter;
    @FXML private ComboBox<String> priceFilter;
    @FXML private ComboBox<String> sortFilter;
    @FXML private Label resultsCount;
    @FXML private GridPane vehiclesGrid;
    @FXML private VBox emptyState;

    private List<Vehicle> vehicles = new ArrayList<>();
    private ObservableList<Vehicle> filteredVehicles = FXCollections.observableArrayList();
    private VehicleDAO vehicleDAO = new VehicleDAO();
    private FavoriteDAO favoriteDAO = new FavoriteDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();

    // Session management
    private SessionManager sessionManager = SessionManager.getInstance();
    private int currentClientId;

    // Map pour stocker les boutons favoris
    private Map<Integer, Button> favoriteButtons = new HashMap<>();
    private Map<Integer, Button> reservationButtons = new HashMap<>();

    @FXML
    public void initialize() {
        // Vérifier la session avec votre SessionManager
        if (!sessionManager.estConnecte()) {
            showAlert("Erreur", "Session invalide - Veuillez vous reconnecter");
            return;
        }

        // Récupérer l'ID de l'utilisateur connecté
        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        currentClientId = currentUser.getIdUtilisateur();

        System.out.println("✅ ClientVehiclesController initialisé pour: " +
                currentUser.getPrenom() + " " + currentUser.getNom() +
                " (ID: " + currentClientId + " - Rôle: " + currentUser.getRole() + ")");

        // Mettre à jour les réservations expirées
        reservationDAO.updateExpiredReservations();

        initializeFilters();
        loadVehiclesFromDatabase();

        ToggleGroup sellerTypeGroup = new ToggleGroup();
        particulierRadio.setToggleGroup(sellerTypeGroup);
        professionnelRadio.setToggleGroup(sellerTypeGroup);

        brandFilter.setOnAction(e -> applyAllFilters());
        typeFilter.setOnAction(e -> applyAllFilters());
        priceFilter.setOnAction(e -> applyAllFilters());
        sortFilter.setOnAction(e -> sortVehicles());

        minPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                minPriceField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            applyAllFilters();
        });

        maxPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                maxPriceField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            applyAllFilters();
        });
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
        try {
            vehicles = vehicleDAO.getAllVehicles();

            if (vehicles.isEmpty()) {
                System.out.println("ℹ️ Aucun véhicule trouvé dans la base de données");
                showAlert("Information", "Aucun véhicule n'est disponible pour le moment.");
            } else {
                System.out.println("✅ " + vehicles.size() + " véhicules chargés depuis la base de données");
            }

            filteredVehicles.setAll(vehicles);
            updateFiltersWithRealData();
            displayVehicles();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des véhicules: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les véhicules depuis la base de données: " + e.getMessage());
        }
    }

    private void updateFiltersWithRealData() {
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
    }

    private String extractBrandFromTitle(String title) {
        if (title == null || title.isEmpty()) return "Autre";

        String[] knownBrands = {"Toyota", "Renault", "Peugeot", "BMW", "Mercedes", "Audi",
                "Volkswagen", "Ford", "Nissan", "Hyundai", "Dacia", "Kia",
                "Chevrolet", "Citroën", "Opel", "Fiat", "Seat", "Skoda",
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
        vehiclesGrid.getChildren().clear();
        favoriteButtons.clear();
        reservationButtons.clear();

        if (filteredVehicles.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            vehiclesGrid.setVisible(false);
            updateResultsCount();
            System.out.println("⚠️ Aucun véhicule à afficher");
            return;
        }

        emptyState.setVisible(false);
        emptyState.setManaged(false);
        vehiclesGrid.setVisible(true);

        System.out.println("📦 Affichage de " + filteredVehicles.size() + " véhicules");

        int column = 0;
        int row = 0;
        int columns = 3;

        for (Vehicle vehicle : filteredVehicles) {
            try {
                VBox vehicleCard = createModernVehicleCard(vehicle);
                vehiclesGrid.add(vehicleCard, column, row);

                System.out.println("✅ Carte ajoutée: " + vehicle.getTitle() + " à position [" + column + "," + row + "]");

                column++;
                if (column >= columns) {
                    column = 0;
                    row++;
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur création carte pour: " + vehicle.getTitle());
                e.printStackTrace();
            }
        }

        updateResultsCount();
        System.out.println("✅ Grille mise à jour avec " + vehiclesGrid.getChildren().size() + " cartes");
    }

    private VBox createModernVehicleCard(Vehicle vehicle) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e8e8e8; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Header avec info vendeur
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 12 15; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color: " + getRandomColor() + "; -fx-background-radius: 20; -fx-min-width: 35; " +
                "-fx-min-height: 35; -fx-max-width: 35; -fx-max-height: 35;");

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

        Label premiumBadge = new Label("⭐ Premium");
        premiumBadge.setStyle("-fx-background-color: #FFF3E0; -fx-text-fill: #FF9800; -fx-padding: 4 8; " +
                "-fx-background-radius: 4; -fx-font-size: 11; -fx-font-weight: bold;");

        header.getChildren().addAll(avatar, vendorInfo, spacer, premiumBadge);

        // Image du véhicule
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8;");
        imageContainer.setPrefHeight(180);
        imageContainer.setMaxHeight(180);

        loadVehicleImage(vehicle, imageContainer);

        Label photoCount = new Label("📷 " + getRandomPhotoCount());
        photoCount.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: white; -fx-padding: 5 10; " +
                "-fx-background-radius: 15; -fx-font-size: 11;");
        StackPane.setAlignment(photoCount, Pos.BOTTOM_LEFT);
        StackPane.setMargin(photoCount, new Insets(10));
        imageContainer.getChildren().add(photoCount);

        // Contenu de la carte
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 15;");

        HBox locationBox = new HBox(5);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        Label location = new Label(getRandomCity());
        location.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
        locationBox.getChildren().addAll(locationIcon, location);

        Label title = new Label(vehicle.getTitle());
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #333;");
        title.setWrapText(true);
        title.setMaxWidth(250);

        String descriptionText = vehicle.getDescription() != null ?
                truncateDescription(vehicle.getDescription()) : "Aucune description disponible";
        Label description = new Label(descriptionText);
        description.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        description.setWrapText(true);
        description.setMaxWidth(250);

        HBox specs = new HBox(15);
        specs.setAlignment(Pos.CENTER_LEFT);

        Label year = new Label("📅 " + extractYearFromTitle(vehicle.getTitle()));
        year.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        Label transmission = new Label("⚙️ " + getRandomTransmission());
        transmission.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        Label fuel = new Label("⛽ " + getRandomFuel());
        fuel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        specs.getChildren().addAll(year, transmission, fuel);

        // Footer avec prix, bouton réservation et bouton favori
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-padding: 15 15 12 15; -fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");

        VBox priceBox = new VBox(2);
        Label price = new Label(String.format("%,.0f DH", vehicle.getPrice()));
        price.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #0066FF;");

        double monthlyPrice = vehicle.getPrice() / 48;
        Label pricePerMonth = new Label("~" + String.format("%,.0f DH / mois", monthlyPrice));
        pricePerMonth.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");

        priceBox.getChildren().addAll(price, pricePerMonth);

        Region priceSpacer = new Region();
        HBox.setHgrow(priceSpacer, Priority.ALWAYS);

        // Bouton de réservation
        Button reserveButton = createReservationButton(vehicle);
        reservationButtons.put(vehicle.getId(), reserveButton);

        // Bouton favori avec état dynamique
        Button favoriteBtn = createFavoriteButton(vehicle);
        favoriteButtons.put(vehicle.getId(), favoriteBtn);

        HBox buttonsContainer = new HBox(5);
        buttonsContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonsContainer.getChildren().addAll(reserveButton, favoriteBtn);

        footer.getChildren().addAll(priceBox, priceSpacer, buttonsContainer);

        content.getChildren().addAll(locationBox, title, description, specs);
        card.getChildren().addAll(header, imageContainer, content, footer);

        card.setOnMouseClicked(e -> {
            if (e.getTarget() != favoriteBtn && e.getTarget() != reserveButton &&
                    !favoriteBtn.getParent().equals(e.getTarget()) && !reserveButton.getParent().equals(e.getTarget())) {
                viewVehicleDetails(vehicle);
            }
        });

        setupCardHoverEffects(card);

        return card;
    }

    /**
     * Créer un bouton de réservation avec l'état actuel
     */
    private Button createReservationButton(Vehicle vehicle) {
        Button reserveButton = new Button();

        // Vérifier si le véhicule est déjà réservé
        if (reservationDAO.isVehiculeReserved(vehicle.getId())) {
            // Vérifier si c'est le client actuel qui a réservé
            if (reservationDAO.hasClientReservedVehicule(currentClientId, vehicle.getId())) {
                reserveButton.setText("✅ Déjà réservé");
                reserveButton.setStyle(
                        "-fx-background-color: #E8F5E8; " +
                                "-fx-text-fill: #2E7D32; " +
                                "-fx-font-size: 11; -fx-font-weight: bold; -fx-padding: 6 10; " +
                                "-fx-background-radius: 12; -fx-cursor: default;"
                );
                reserveButton.setDisable(true);
            } else {
                reserveButton.setText("⛔ Déjà réservé");
                reserveButton.setStyle(
                        "-fx-background-color: #FFEBEE; " +
                                "-fx-text-fill: #C62828; " +
                                "-fx-font-size: 11; -fx-font-weight: bold; -fx-padding: 6 10; " +
                                "-fx-background-radius: 12; -fx-cursor: default;"
                );
                reserveButton.setDisable(true);
            }
        } else {
            reserveButton.setText("📅 Réserver");
            reserveButton.setStyle(
                    "-fx-background-color: #4CAF50; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 11; -fx-font-weight: bold; -fx-padding: 6 12; " +
                            "-fx-background-radius: 12; -fx-cursor: hand;"
            );
            reserveButton.setOnAction(e -> handleReservation(vehicle, reserveButton));
        }

        return reserveButton;
    }

    /**
     * Gérer la réservation d'un véhicule
     */
    private void handleReservation(Vehicle vehicle, Button reserveButton) {
        // Vérifier à nouveau si le véhicule est disponible
        if (reservationDAO.isVehiculeReserved(vehicle.getId())) {
            showAlert("Réservation impossible", "❌ Ce véhicule a déjà été réservé par un autre client.");
            updateReservationButton(reserveButton, vehicle);
            return;
        }

        // Vérifier si l'utilisateur a déjà réservé ce véhicule
        if (reservationDAO.hasClientReservedVehicule(currentClientId, vehicle.getId())) {
            showAlert("Réservation existante", "ℹ️ Vous avez déjà réservé ce véhicule.");
            updateReservationButton(reserveButton, vehicle);
            return;
        }

        // Demander confirmation
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de réservation");
        confirmation.setHeaderText("Confirmer la réservation");
        confirmation.setContentText("Voulez-vous réserver le véhicule : " + vehicle.getTitle() + " ?\n\n" +
                "Prix: " + String.format("%,.0f DH", vehicle.getPrice()) + "\n" +
                "La réservation sera valable pendant 24 heures.");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = reservationDAO.createReservation(currentClientId, vehicle.getId());
            if (success) {
                showAlert("Réservation confirmée", "✅ Véhicule réservé avec succès!\n\n" +
                        "Vous avez 24 heures pour finaliser votre achat.\n" +
                        "Véhicule: " + vehicle.getTitle());
                updateReservationButton(reserveButton, vehicle);
            } else {
                showAlert("Erreur", "❌ Impossible de réserver le véhicule. Veuillez réessayer.");
            }
        }
    }

    /**
     * Mettre à jour l'état du bouton de réservation
     */
    private void updateReservationButton(Button reserveButton, Vehicle vehicle) {
        if (reservationDAO.isVehiculeReserved(vehicle.getId())) {
            if (reservationDAO.hasClientReservedVehicule(currentClientId, vehicle.getId())) {
                reserveButton.setText("✅ Déjà réservé");
                reserveButton.setStyle(
                        "-fx-background-color: #E8F5E8; " +
                                "-fx-text-fill: #2E7D32; " +
                                "-fx-font-size: 11; -fx-font-weight: bold; -fx-padding: 6 10; " +
                                "-fx-background-radius: 12; -fx-cursor: default;"
                );
                reserveButton.setDisable(true);
            } else {
                reserveButton.setText("⛔ Déjà réservé");
                reserveButton.setStyle(
                        "-fx-background-color: #FFEBEE; " +
                                "-fx-text-fill: #C62828; " +
                                "-fx-font-size: 11; -fx-font-weight: bold; -fx-padding: 6 10; " +
                                "-fx-background-radius: 12; -fx-cursor: default;"
                );
                reserveButton.setDisable(true);
            }
        }
    }

    /**
     * Créer un bouton favori avec l'état actuel (rouge si déjà favori)
     */
    private Button createFavoriteButton(Vehicle vehicle) {
        boolean isFav = favoriteDAO.isFavorite(currentClientId, vehicle.getId());

        Button favoriteBtn = new Button(isFav ? "❤" : "♡");
        favoriteBtn.setStyle(
                "-fx-background-color: #f5f5f5; " +
                        "-fx-text-fill: " + (isFav ? "#FF0000" : "#FF6B35") + "; " +
                        "-fx-font-size: 16; -fx-padding: 6 8; " +
                        "-fx-background-radius: 20; -fx-cursor: hand; -fx-border-width: 0;"
        );

        favoriteBtn.setOnAction(e -> toggleFavorite(vehicle, favoriteBtn));

        return favoriteBtn;
    }

    /**
     * Basculer l'état favori (ajouter/retirer)
     */
    private void toggleFavorite(Vehicle vehicle, Button button) {
        boolean isFav = favoriteDAO.isFavorite(currentClientId, vehicle.getId());

        if (isFav) {
            // Retirer des favoris
            boolean success = favoriteDAO.removeFavorite(currentClientId, vehicle.getId());
            if (success) {
                button.setText("♡");
                button.setStyle(
                        "-fx-background-color: #f5f5f5; -fx-text-fill: #FF6B35; " +
                                "-fx-font-size: 16; -fx-padding: 6 8; -fx-background-radius: 20; " +
                                "-fx-cursor: hand; -fx-border-width: 0;"
                );
                showToast("Retiré des favoris", false);
            }
        } else {
            // Ajouter aux favoris
            boolean success = favoriteDAO.addFavorite(currentClientId, vehicle.getId());
            if (success) {
                button.setText("❤");
                button.setStyle(
                        "-fx-background-color: #f5f5f5; -fx-text-fill: #FF0000; " +
                                "-fx-font-size: 16; -fx-padding: 6 8; -fx-background-radius: 20; " +
                                "-fx-cursor: hand; -fx-border-width: 0;"
                );
                showToast("Ajouté aux favoris", true);
            }
        }
    }

    /**
     * Afficher une notification toast
     */
    private void showToast(String message, boolean isSuccess) {
        Alert toast = new Alert(Alert.AlertType.INFORMATION);
        toast.setTitle(isSuccess ? "Succès" : "Information");
        toast.setHeaderText(null);
        toast.setContentText(message);
        toast.showAndWait();
    }

    private void loadVehicleImage(Vehicle vehicle, StackPane container) {
        System.out.println("🖼️ Tentative de chargement image pour: " + vehicle.getTitle());

        if (vehicle.getImage() != null && !vehicle.getImage().trim().isEmpty()) {
            try {
                String imagePath = vehicle.getImage();
                System.out.println("📁 Chemin image: " + imagePath);

                File imageFile = new File(imagePath);

                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), true);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(280);
                    imageView.setFitHeight(180);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);

                    image.errorProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            System.err.println("❌ Erreur de chargement de l'image: " + imagePath);
                            showDefaultImage(container);
                        }
                    });

                    image.progressProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal.doubleValue() == 1.0) {
                            System.out.println("✅ Image chargée avec succès: " + imagePath);
                        }
                    });

                    container.getChildren().add(0, imageView);

                } else {
                    System.err.println("❌ Fichier image non trouvé: " + imagePath);
                    showDefaultImage(container);
                }

            } catch (Exception e) {
                System.err.println("❌ Erreur chargement image pour: " + vehicle.getTitle());
                e.printStackTrace();
                showDefaultImage(container);
            }
        } else {
            System.out.println("ℹ️ Aucune image définie pour: " + vehicle.getTitle());
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
        filteredVehicles.setAll(vehicles);

        if (brandFilter.getValue() != null && !brandFilter.getValue().equals("Toutes les marques")) {
            filteredVehicles.removeIf(v -> !extractBrandFromTitle(v.getTitle()).equals(brandFilter.getValue()));
        }

        if (typeFilter.getValue() != null && !typeFilter.getValue().equals("Tous les types")) {
            filteredVehicles.removeIf(v -> v.getCategory() == null || !v.getCategory().equals(typeFilter.getValue()));
        }

        if (priceFilter.getValue() != null && !priceFilter.getValue().equals("Tous les prix")) {
            filteredVehicles.removeIf(v -> !matchesPriceRange(v.getPrice(), priceFilter.getValue()));
        }

        try {
            double minPrice = minPriceField.getText().isEmpty() ? 0 : Double.parseDouble(minPriceField.getText());
            double maxPrice = maxPriceField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPriceField.getText());

            if (minPrice > 0 || maxPrice < Double.MAX_VALUE) {
                filteredVehicles.removeIf(v -> v.getPrice() < minPrice || v.getPrice() > maxPrice);
            }
        } catch (NumberFormatException e) {
            System.err.println("❌ Format de prix invalide");
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
        particulierRadio.setSelected(true);

        filteredVehicles.setAll(vehicles);
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
            displayVehicles();
        }
    }

    private void showDefaultImage(StackPane container) {
        container.getChildren().clear();
        container.setStyle("-fx-background-color: " + getRandomLightColor() + "; -fx-background-radius: 8;");

        VBox placeholder = new VBox(5);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setStyle("-fx-padding: 20;");

        Label carIcon = new Label("🚗");
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

    private void viewVehicleDetails(Vehicle vehicle) {
        showAlert("Détails du véhicule",
                vehicle.getTitle() + "\n\n" +
                        "Prix: " + String.format("%,.0f DH", vehicle.getPrice()) + "\n" +
                        "Catégorie: " + vehicle.getCategory() + "\n" +
                        "Vendeur: " + vehicle.getSellerName() + "\n" +
                        "Description: " + vehicle.getDescription());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}