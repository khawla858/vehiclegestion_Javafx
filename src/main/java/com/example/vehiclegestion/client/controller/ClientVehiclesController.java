package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.client.doa.VehicleDAO;
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

    @FXML
    public void initialize() {
        System.out.println("✅ ClientVehiclesController initialisé");
        initializeFilters();
        loadVehiclesFromDatabase();

        // Grouper les radio buttons
        ToggleGroup sellerTypeGroup = new ToggleGroup();
        particulierRadio.setToggleGroup(sellerTypeGroup);
        professionnelRadio.setToggleGroup(sellerTypeGroup);

        // Écouteurs pour les filtres
        brandFilter.setOnAction(e -> applyAllFilters());
        typeFilter.setOnAction(e -> applyAllFilters());
        priceFilter.setOnAction(e -> applyAllFilters());
        sortFilter.setOnAction(e -> sortVehicles());

        // Écouteurs pour les champs de prix
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
        // Initialisation des filtres avec valeurs par défaut
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

        // Valeurs par défaut pour les prix
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
            // Extraire la marque du titre
            String brand = extractBrandFromTitle(vehicle.getTitle());
            if (brand != null && !brands.contains(brand)) {
                brands.add(brand);
            }

            // Extraire le type de la catégorie
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

        // Si aucune marque connue, prendre le premier mot
        String[] words = title.split(" ");
        return words.length > 0 ? words[0] : "Autre";
    }

    // REMPLACE uniquement la méthode displayVehicles() dans ton Controller

    private void displayVehicles() {
        vehiclesGrid.getChildren().clear();

        if (filteredVehicles.isEmpty()) {
            // Afficher le message "aucun véhicule"
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            vehiclesGrid.setVisible(false);
            updateResultsCount();
            System.out.println("⚠️ Aucun véhicule à afficher");
            return;
        }

        // Masquer le message vide
        emptyState.setVisible(false);
        emptyState.setManaged(false);
        vehiclesGrid.setVisible(true);

        System.out.println("📦 Affichage de " + filteredVehicles.size() + " véhicules");

        int column = 0;
        int row = 0;
        int columns = 3; // 3 colonnes

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

        // Avatar vendeur
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

        // Charger l'image depuis la base de données
        loadVehicleImage(vehicle, imageContainer);

        // Badge nombre de photos
        Label photoCount = new Label("📷 " + getRandomPhotoCount());
        photoCount.setStyle("-fx-background-color: rgba(0,0,0,0.6); -fx-text-fill: white; -fx-padding: 5 10; " +
                "-fx-background-radius: 15; -fx-font-size: 11;");
        StackPane.setAlignment(photoCount, Pos.BOTTOM_LEFT);
        StackPane.setMargin(photoCount, new Insets(10));
        imageContainer.getChildren().add(photoCount);

        // Contenu de la carte
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 15;");

        // Localisation
        HBox locationBox = new HBox(5);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        Label location = new Label(getRandomCity());
        location.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
        locationBox.getChildren().addAll(locationIcon, location);

        // Titre du véhicule
        Label title = new Label(vehicle.getTitle());
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #333;");
        title.setWrapText(true);
        title.setMaxWidth(250);

        // Description
        String descriptionText = vehicle.getDescription() != null ?
                truncateDescription(vehicle.getDescription()) : "Aucune description disponible";
        Label description = new Label(descriptionText);
        description.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        description.setWrapText(true);
        description.setMaxWidth(250);

        // Caractéristiques
        HBox specs = new HBox(15);
        specs.setAlignment(Pos.CENTER_LEFT);

        Label year = new Label("📅 " + extractYearFromTitle(vehicle.getTitle()));
        year.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        Label transmission = new Label("⚙️ " + getRandomTransmission());
        transmission.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        Label fuel = new Label("⛽ " + getRandomFuel());
        fuel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");

        specs.getChildren().addAll(year, transmission, fuel);

        // Footer avec prix
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

        Button favoriteBtn = new Button("♡");
        favoriteBtn.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #FF6B35; -fx-font-size: 18; " +
                "-fx-padding: 8 12; -fx-background-radius: 20; -fx-cursor: hand; -fx-border-width: 0;");
        favoriteBtn.setOnAction(e -> addToFavorites(vehicle));

        footer.getChildren().addAll(priceBox, priceSpacer, favoriteBtn);

        // Assembler la carte
        content.getChildren().addAll(locationBox, title, description, specs);
        card.getChildren().addAll(header, imageContainer, content, footer);

        // Interactions
        card.setOnMouseClicked(e -> viewVehicleDetails(vehicle));
        setupCardHoverEffects(card);

        return card;
    }

    private void loadVehicleImage(Vehicle vehicle, StackPane container) {
        System.out.println("🖼️ Tentative de chargement image pour: " + vehicle.getTitle());

        if (vehicle.getImage() != null && !vehicle.getImage().trim().isEmpty()) {
            try {
                String imagePath = vehicle.getImage();
                System.out.println("📁 Chemin image: " + imagePath);

                File imageFile = new File(imagePath);

                if (imageFile.exists()) {
                    // Charger depuis le système de fichiers
                    Image image = new Image(imageFile.toURI().toString(), true);
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(280);
                    imageView.setFitHeight(180);
                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);

                    // Ajouter un écouteur pour les erreurs de chargement
                    image.errorProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            System.err.println("❌ Erreur de chargement de l'image: " + imagePath);
                            showDefaultImage(container);
                        }
                    });

                    // Ajouter un écouteur pour quand l'image est chargée
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

    // === FILTRES ===

    @FXML
    private void applyAllFilters() {
        filteredVehicles.setAll(vehicles);

        // Filtre par marque
        if (brandFilter.getValue() != null && !brandFilter.getValue().equals("Toutes les marques")) {
            filteredVehicles.removeIf(v -> !extractBrandFromTitle(v.getTitle()).equals(brandFilter.getValue()));
        }

        // Filtre par type
        if (typeFilter.getValue() != null && !typeFilter.getValue().equals("Tous les types")) {
            filteredVehicles.removeIf(v -> v.getCategory() == null || !v.getCategory().equals(typeFilter.getValue()));
        }

        // Filtre par prix prédéfini
        if (priceFilter.getValue() != null && !priceFilter.getValue().equals("Tous les prix")) {
            filteredVehicles.removeIf(v -> !matchesPriceRange(v.getPrice(), priceFilter.getValue()));
        }

        // Filtre par prix personnalisé
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

    // Méthodes utilitaires
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

    private String getRandomTime() {
        String[] times = {"2 heures", "5 heures", "1 jour", "2 jours", "3 jours", "4 heures", "22 heures"};
        return times[(int)(Math.random() * times.length)];
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

    private void addToFavorites(Vehicle vehicle) {
        showAlert("Favoris", vehicle.getTitle() + " ajouté à vos favoris !");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}