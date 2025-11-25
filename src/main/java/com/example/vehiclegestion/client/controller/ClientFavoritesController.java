package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.client.doa.FavoriteDAO;
import com.example.vehiclegestion.client.doa.ReservationDAO;
import com.example.vehiclegestion.client.doa.VehicleDAO;
import com.example.vehiclegestion.client.model.Vehicle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.io.File;

public class ClientFavoritesController implements Initializable {

    @FXML private GridPane favoritesGrid;
    @FXML private VBox emptyState;
    @FXML private Label favoriteCountLabel;
    @FXML private ScrollPane scrollPane;
    @FXML private Button clearAllButton; // backButton supprimé

    private FavoriteDAO favoriteDAO = new FavoriteDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    private SessionManager sessionManager = SessionManager.getInstance();
    private int currentClientId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Vérifier la session avec votre SessionManager
        if (!sessionManager.estConnecte()) {
            showAlert("Erreur", "Veuillez vous connecter pour accéder aux favoris");
            redirectToLogin();
            return;
        }

        // Récupérer l'ID de l'utilisateur connecté
        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        currentClientId = currentUser.getIdUtilisateur();

        System.out.println("✅ ClientFavoritesController initialisé pour: " +
                currentUser.getPrenom() + " " + currentUser.getNom() +
                " (ID: " + currentClientId + ")");

        loadFavorites();
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // backButton supprimé - seulement clearAllButton reste
        clearAllButton.setOnAction(e -> clearAllFavorites());
    }

    private void loadFavorites() {
        try {
            // Mettre à jour les réservations expirées
            reservationDAO.updateExpiredReservations();

            List<Vehicle> favoriteVehicles = favoriteDAO.getFavoriteVehicles(currentClientId);
            displayFavorites(favoriteVehicles);
            updateFavoriteCount(favoriteVehicles.size());

            System.out.println("✅ " + favoriteVehicles.size() + " favoris chargés pour l'utilisateur ID: " + currentClientId);
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement favoris: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger vos favoris");
        }
    }

    private void displayFavorites(List<Vehicle> favorites) {
        favoritesGrid.getChildren().clear();

        if (favorites.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            scrollPane.setVisible(false);
            return;
        }

        emptyState.setVisible(false);
        emptyState.setManaged(false);
        scrollPane.setVisible(true);

        int column = 0;
        int row = 0;
        int columns = 3;

        for (Vehicle vehicle : favorites) {
            try {
                VBox vehicleCard = createFavoriteVehicleCard(vehicle);
                favoritesGrid.add(vehicleCard, column, row);

                column++;
                if (column >= columns) {
                    column = 0;
                    row++;
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur création carte favori: " + vehicle.getTitle());
                e.printStackTrace();
            }
        }
    }

    private VBox createFavoriteVehicleCard(Vehicle vehicle) {
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

        // Badge "Favori"
        Label favoriteBadge = new Label("❤️ Favori");
        favoriteBadge.setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #FF0000; -fx-padding: 4 8; " +
                "-fx-background-radius: 4; -fx-font-size: 11; -fx-font-weight: bold;");

        header.getChildren().addAll(avatar, vendorInfo, spacer, favoriteBadge);

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

        // Footer avec prix, bouton réservation et bouton suppression
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

        // Bouton pour retirer des favoris
        Button removeFavoriteBtn = new Button("❌");
        removeFavoriteBtn.setStyle(
                "-fx-background-color: #FFEBEE; " +
                        "-fx-text-fill: #FF0000; " +
                        "-fx-font-size: 16; -fx-padding: 8 12; " +
                        "-fx-background-radius: 20; -fx-cursor: hand; -fx-border-width: 0;"
        );

        removeFavoriteBtn.setOnAction(e -> removeFromFavorites(vehicle, card));

        footer.getChildren().addAll(priceBox, priceSpacer, reserveButton, removeFavoriteBtn);
        footer.setSpacing(10);

        content.getChildren().addAll(locationBox, title, description, specs);
        card.getChildren().addAll(header, imageContainer, content, footer);

        card.setOnMouseClicked(e -> {
            if (e.getTarget() != removeFavoriteBtn && e.getTarget() != reserveButton) {
                viewVehicleDetails(vehicle);
            }
        });

        setupCardHoverEffects(card);

        return card;
    }

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
                                "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 8 12; " +
                                "-fx-background-radius: 15; -fx-cursor: default;"
                );
                reserveButton.setDisable(true);
            } else {
                reserveButton.setText("⛔ Déjà réservé");
                reserveButton.setStyle(
                        "-fx-background-color: #FFEBEE; " +
                                "-fx-text-fill: #C62828; " +
                                "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 8 12; " +
                                "-fx-background-radius: 15; -fx-cursor: default;"
                );
                reserveButton.setDisable(true);
            }
        } else {
            reserveButton.setText("📅 Réserver");
            reserveButton.setStyle(
                    "-fx-background-color: #4CAF50; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 8 16; " +
                            "-fx-background-radius: 15; -fx-cursor: hand;"
            );
            reserveButton.setOnAction(e -> handleReservation(vehicle, reserveButton));
        }

        return reserveButton;
    }

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

    private void updateReservationButton(Button reserveButton, Vehicle vehicle) {
        if (reservationDAO.isVehiculeReserved(vehicle.getId())) {
            if (reservationDAO.hasClientReservedVehicule(currentClientId, vehicle.getId())) {
                reserveButton.setText("✅ Déjà réservé");
                reserveButton.setStyle(
                        "-fx-background-color: #E8F5E8; " +
                                "-fx-text-fill: #2E7D32; " +
                                "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 8 12; " +
                                "-fx-background-radius: 15; -fx-cursor: default;"
                );
                reserveButton.setDisable(true);
            } else {
                reserveButton.setText("⛔ Déjà réservé");
                reserveButton.setStyle(
                        "-fx-background-color: #FFEBEE; " +
                                "-fx-text-fill: #C62828; " +
                                "-fx-font-size: 12; -fx-font-weight: bold; -fx-padding: 8 12; " +
                                "-fx-background-radius: 15; -fx-cursor: default;"
                );
                reserveButton.setDisable(true);
            }
        }
    }

    private void removeFromFavorites(Vehicle vehicle, VBox card) {
        boolean success = favoriteDAO.removeFavorite(currentClientId, vehicle.getId());
        if (success) {
            // Retirer la carte de la grille
            favoritesGrid.getChildren().remove(card);
            // Recharger pour mettre à jour le compteur
            loadFavorites();
            showAlert("Succès", "✅ Véhicule retiré des favoris: " + vehicle.getTitle());
        } else {
            showAlert("Erreur", "❌ Impossible de retirer le véhicule des favoris");
        }
    }

    @FXML
    private void clearAllFavorites() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer tous les favoris");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer tous vos véhicules favoris ?");

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = favoriteDAO.clearAllFavorites(currentClientId);
            if (success) {
                loadFavorites();
                showAlert("Succès", "✅ Tous les favoris ont été supprimés");
            } else {
                showAlert("Erreur", "❌ Impossible de supprimer tous les favoris");
            }
        }
    }

    // Méthode goBackToVehicles supprimée car le bouton n'existe plus

    @FXML
    private void goToVehicles() {
        // Utiliser favoritesGrid pour obtenir la scène puisque backButton n'existe plus
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/vehiclegestion/view/client/vehicles-view.fxml"));
            Stage stage = (Stage) favoritesGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation vers véhicules: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateFavoriteCount(int count) {
        favoriteCountLabel.setText(count + " véhicule(s) favori(s)");
    }

    private void redirectToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/vehiclegestion/view/auth/login.fxml"));
            // Utiliser favoritesGrid pour obtenir la scène puisque backButton n'existe plus
            Stage stage = (Stage) favoritesGrid.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Connexion - Gestion Véhicules");
            stage.setMaximized(false);
            stage.setWidth(1000);
            stage.setHeight(700);
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("❌ Erreur redirection login: " + e.getMessage());
        }
    }

    // Méthodes utilitaires (reprises de ClientVehiclesController)

    private void loadVehicleImage(Vehicle vehicle, StackPane container) {
        if (vehicle.getImage() != null && !vehicle.getImage().trim().isEmpty()) {
            try {
                String imagePath = vehicle.getImage();
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
                            showDefaultImage(container);
                        }
                    });

                    container.getChildren().add(0, imageView);
                } else {
                    showDefaultImage(container);
                }
            } catch (Exception e) {
                showDefaultImage(container);
            }
        } else {
            showDefaultImage(container);
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

    private void setupCardHoverEffects(VBox card) {
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: white; -fx-border-color: #0066FF; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,102,255,0.2), 12, 0, 0, 4);"));
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; -fx-border-color: #e8e8e8; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"));
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

    // Méthodes utilitaires
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
}