package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.client.doa.FavoriteDAO;
import com.example.vehiclegestion.client.doa.ReservationDAO;
import com.example.vehiclegestion.client.doa.VehicleDAO;
import com.example.vehiclegestion.client.model.Vehicle;
import com.example.vehiclegestion.client.controller.VehiDetaiCo;
import com.example.vehiclegestion.vendeur.model.Article;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.io.File;

public class ClientFavoritesController implements Initializable {

    @FXML private GridPane favoritesGrid;
    @FXML private VBox emptyState;
    @FXML private Label countLabel;
    @FXML private ScrollPane scrollPane;
    @FXML private Button clearAllButton;

    private FavoriteDAO favoriteDAO = new FavoriteDAO();
    private VehicleDAO vehicleDAO = new VehicleDAO();
    private ReservationDAO reservationDAO = new ReservationDAO();
    private SessionManager sessionManager = SessionManager.getInstance();
    private int currentClientId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🎯 Initialisation de ClientFavoritesController...");

        if (!sessionManager.estConnecte()) {
            showAlert("🔒 Connexion requise", "Veuillez vous connecter pour accéder à vos favoris");
            redirectToLogin();
            return;
        }

        Utilisateur currentUser = sessionManager.getUtilisateurConnecte();
        currentClientId = currentUser.getIdUtilisateur();

        System.out.println("✅ ClientFavoritesController initialisé pour: " +
                currentUser.getPrenom() + " " + currentUser.getNom() +
                " (ID: " + currentClientId + ")");

        // Mise en style initiale
        setupInitialStyles();
        loadFavorites();
        setupEventHandlers();
    }

    private void setupInitialStyles() {
        if (scrollPane != null) {
            scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        }

        if (favoritesGrid != null) {
            favoritesGrid.setStyle("-fx-background-color: transparent;");
        }
    }

    private void setupEventHandlers() {
        if (clearAllButton != null) {
            clearAllButton.setOnAction(e -> clearAllFavorites());
            clearAllButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #ef4444, #dc2626); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-font-size: 13px; " +
                            "-fx-padding: 10 20; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(239, 68, 68, 0.4), 10, 0.5, 0, 3);"
            );
        }
    }

    private void loadFavorites() {
        try {
            reservationDAO.updateExpiredReservations();
            List<Vehicle> favoriteVehicles = favoriteDAO.getFavoriteVehicles(currentClientId);
            displayFavorites(favoriteVehicles);
            updateFavoriteCount(favoriteVehicles.size());

            System.out.println("📊 " + favoriteVehicles.size() + " favoris chargés pour l'utilisateur ID: " + currentClientId);
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement favoris: " + e.getMessage());
            e.printStackTrace();
            showAlert("❌ Erreur", "Impossible de charger vos favoris");
        }
    }

    private void displayFavorites(List<Vehicle> favorites) {
        if (favoritesGrid == null) {
            System.err.println("❌ ERREUR: favoritesGrid est null");
            return;
        }

        favoritesGrid.getChildren().clear();

        if (favorites.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            if (scrollPane != null) scrollPane.setVisible(false);
            return;
        }

        emptyState.setVisible(false);
        emptyState.setManaged(false);
        if (scrollPane != null) scrollPane.setVisible(true);

        int column = 0;
        int row = 0;
        int columns = 4;

        for (Vehicle vehicle : favorites) {
            try {
                VBox vehicleCard = createModernFavoriteVehicleCard(vehicle);
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

    private VBox createModernFavoriteVehicleCard(Vehicle vehicle) {
        VBox card = new VBox(0);
        card.setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.8); " +
                        "-fx-border-color: linear-gradient(to bottom, #ec4899, #8b5cf6); " +
                        "-fx-border-radius: 15; " +
                        "-fx-border-width: 2; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0.5, 0, 5);"
        );
        card.setPrefWidth(300);
        card.setMaxWidth(300);
        card.setCursor(javafx.scene.Cursor.HAND);

        // Header avec info vendeur
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-padding: 15 20; " +
                        "-fx-border-color: linear-gradient(to right, #334155, transparent); " +
                        "-fx-border-width: 0 0 1 0;"
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
        avatarText.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        avatar.getChildren().add(avatarText);

        VBox vendorInfo = new VBox(2);
        Label vendorName = new Label(vehicle.getSellerName() != null ? vehicle.getSellerName() : "Vendeur");
        vendorName.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #f1f5f9;");

        Label timeAgo = new Label("il y a " + getTimeAgo(vehicle.getDateAdded()));
        timeAgo.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");

        vendorInfo.getChildren().addAll(vendorName, timeAgo);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge "Favori"
        Label favoriteBadge = new Label("❤️ FAVORI");
        favoriteBadge.setStyle(
                "-fx-background-color: linear-gradient(to right, #ec4899, #8b5cf6); " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 6 12; " +
                        "-fx-background-radius: 15; " +
                        "-fx-font-size: 11; " +
                        "-fx-font-weight: bold;"
        );

        header.getChildren().addAll(avatar, vendorInfo, spacer, favoriteBadge);

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
                        "-fx-text-fill: white; " +
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
        title.setMaxWidth(260);

        String descriptionText = vehicle.getDescription() != null ?
                truncateDescription(vehicle.getDescription()) : "Aucune description disponible";
        Label description = new Label(descriptionText);
        description.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13;");
        description.setWrapText(true);
        description.setMaxWidth(260);

        HBox specs = new HBox(15);
        specs.setAlignment(Pos.CENTER_LEFT);

        Label year = new Label("📅 " + extractYearFromTitle(vehicle.getTitle()));
        year.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12;");

        Label transmission = new Label("⚙️ " + getRandomTransmission());
        transmission.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12;");

        Label fuel = new Label("⛽ " + getRandomFuel());
        fuel.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12;");

        specs.getChildren().addAll(year, transmission, fuel);

        // Footer avec prix et boutons
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

        // Bouton de disponibilité
        Button availabilityBtn = createAvailabilityButton(vehicle);

        // Bouton pour retirer des favoris
        Button removeFavoriteBtn = new Button("🗑️");
        removeFavoriteBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #ef4444, #dc2626); " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14; " +
                        "-fx-padding: 8 10; " +
                        "-fx-background-radius: 8; " +
                        "-fx-cursor: hand; " +
                        "-fx-min-width: 40; " +
                        "-fx-min-height: 40;"
        );
        removeFavoriteBtn.setOnAction(e -> removeFromFavorites(vehicle, card));

        HBox buttonsContainer = new HBox(10);
        buttonsContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonsContainer.getChildren().addAll(availabilityBtn, removeFavoriteBtn);

        footer.getChildren().addAll(priceBox, priceSpacer, buttonsContainer);
        content.getChildren().addAll(locationBox, title, description, specs);
        card.getChildren().addAll(header, imageContainer, content, footer);

        card.setOnMouseClicked(e -> {
            if (e.getTarget() != removeFavoriteBtn && e.getTarget() != availabilityBtn) {
                viewVehicleDetails(vehicle);
            }
        });

        setupCardHoverEffects(card);

        return card;
    }

    private Button createAvailabilityButton(Vehicle vehicle) {
        Button availabilityBtn = new Button();

        String statut = vehicle.getStatutVehicule() != null ? vehicle.getStatutVehicule().toLowerCase() : "disponible";
        boolean estReserve = reservationDAO.isVehiculeReserved(vehicle.getId());
        boolean estReserveParMoi = reservationDAO.hasClientReservedVehicule(currentClientId, vehicle.getId());

        switch (statut) {
            case "vendu":
                availabilityBtn.setText("⛔ VENDU");
                availabilityBtn.setStyle(
                        "-fx-background-color: linear-gradient(to right, #dc2626, #b91c1c); " +
                                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; " +
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
                                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; " +
                                    "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                    "-fx-cursor: default;"
                    );
                } else {
                    availabilityBtn.setText("🔒 RÉSERVÉ");
                    availabilityBtn.setStyle(
                            "-fx-background-color: linear-gradient(to right, #f59e0b, #d97706); " +
                                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; " +
                                    "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                    "-fx-cursor: default;"
                    );
                }
                break;

            case "en attente":
                availabilityBtn.setText("⏳ EN ATTENTE");
                availabilityBtn.setStyle(
                        "-fx-background-color: linear-gradient(to right, #f59e0b, #d97706); " +
                                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; " +
                                "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                "-fx-cursor: default;"
                );
                break;

            case "indisponible":
                availabilityBtn.setText("🚫 INDISPONIBLE");
                availabilityBtn.setStyle(
                        "-fx-background-color: linear-gradient(to right, #64748b, #475569); " +
                                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; " +
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
                                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; " +
                                    "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                    "-fx-cursor: default;"
                    );
                } else {
                    availabilityBtn.setText("✅ DISPONIBLE");
                    availabilityBtn.setStyle(
                            "-fx-background-color: linear-gradient(to right, #10b981, #059669); " +
                                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11; " +
                                    "-fx-padding: 8 12; -fx-background-radius: 8; " +
                                    "-fx-cursor: default;"
                    );
                }
                break;
        }

        availabilityBtn.setDisable(true);
        return availabilityBtn;
    }

    private void removeFromFavorites(Vehicle vehicle, VBox card) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Retirer des favoris");
        confirmation.setContentText("Êtes-vous sûr de vouloir retirer ce véhicule de vos favoris ?");

        confirmation.getDialogPane().setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.9); " +
                        "-fx-border-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                        "-fx-border-width: 2;"
        );

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = favoriteDAO.removeFavorite(currentClientId, vehicle.getId());
            if (success) {
                favoritesGrid.getChildren().remove(card);
                loadFavorites();
                showAlert("✅ Succès", "Véhicule retiré des favoris: " + vehicle.getTitle());
            } else {
                showAlert("❌ Erreur", "Impossible de retirer le véhicule des favoris");
            }
        }
    }

    @FXML
    private void clearAllFavorites() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer tous les favoris");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer tous vos véhicules favoris ?\nCette action est irréversible.");

        confirmation.getDialogPane().setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.9); " +
                        "-fx-border-color: linear-gradient(to right, #ef4444, #dc2626); " +
                        "-fx-border-width: 2;"
        );

        if (confirmation.showAndWait().get() == ButtonType.OK) {
            boolean success = favoriteDAO.clearAllFavorites(currentClientId);
            if (success) {
                loadFavorites();
                showAlert("✅ Succès", "Tous les favoris ont été supprimés");
            } else {
                showAlert("❌ Erreur", "Impossible de supprimer tous les favoris");
            }
        }
    }

    @FXML
    private void goToVehicles() {
        try {
            String[] possiblePaths = {
                    "/com/example/vehiclegestion/view/client/ClientVehiclesView.fxml",
                    "/view/client/ClientVehiclesView.fxml",
                    "/ClientVehiclesView.fxml"
            };

            FXMLLoader loader = null;
            Parent root = null;

            for (String path : possiblePaths) {
                try {
                    URL url = getClass().getResource(path);
                    if (url != null) {
                        loader = new FXMLLoader(url);
                        root = loader.load();
                        System.out.println("✅ FXML chargé: " + path);
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Échec pour: " + path);
                }
            }

            if (root != null) {
                Stage stage = (Stage) (favoritesGrid != null ? favoritesGrid.getScene().getWindow() : null);
                if (stage != null) {
                    stage.setScene(new Scene(root));
                    stage.setTitle("Marketplace - Gestion Véhicules");
                    stage.show();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur navigation vers véhicules: " + e.getMessage());
            showAlert("❌ Erreur", "Impossible de naviguer vers la page des véhicules");
        }
    }

    private void updateFavoriteCount(int count) {
        if (countLabel != null) {
            countLabel.setText(count + " véhicule" + (count > 1 ? "s" : "") + " sauvegardé" + (count > 1 ? "s" : ""));
        }
    }

    private void redirectToLogin() {
        try {
            String[] possiblePaths = {
                    "/com/example/vehiclegestion/view/auth/login.fxml",
                    "/view/auth/login.fxml",
                    "/login.fxml"
            };

            FXMLLoader loader = null;
            Parent root = null;

            for (String path : possiblePaths) {
                try {
                    URL url = getClass().getResource(path);
                    if (url != null) {
                        loader = new FXMLLoader(url);
                        root = loader.load();
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Échec pour: " + path);
                }
            }

            if (root != null) {
                Stage stage = (Stage) (favoritesGrid != null ? favoritesGrid.getScene().getWindow() : null);
                if (stage != null) {
                    stage.setScene(new Scene(root, 1000, 700));
                    stage.setTitle("Connexion - Gestion Véhicules");
                    stage.centerOnScreen();
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur redirection login: " + e.getMessage());
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
                    imageView.setFitWidth(300);
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
        container.setStyle(
                "-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                        "-fx-background-radius: 13 13 0 0;"
        );

        VBox placeholder = new VBox(5);
        placeholder.setAlignment(Pos.CENTER);
        placeholder.setStyle("-fx-padding: 30;");

        Label carIcon = new Label("🚗");
        carIcon.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");

        Label noImageText = new Label("Image non disponible");
        noImageText.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12px;");

        placeholder.getChildren().addAll(carIcon, noImageText);
        container.getChildren().add(placeholder);
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

    private void viewVehicleDetails(Vehicle vehicle) {
        System.out.println("🎯 DEBUT viewVehicleDetails pour: " + vehicle.getTitle());

        try {
            String[] possiblePaths = {
                    "/com/example/vehiclegestion/view/client/Vehicle-Detail.fxml",
                    "/view/client/Vehicle-Detail.fxml",
                    "/client/Vehicle-Detail.fxml",
                    "Vehicle-Detail.fxml"
            };

            FXMLLoader loader = null;
            Parent root = null;
            URL url = null;

            for (String path : possiblePaths) {
                url = getClass().getResource(path);
                if (url != null) {
                    System.out.println("✅ FXML trouvé: " + path);
                    break;
                }
            }

            if (url == null) {
                throw new IOException("Fichier FXML introuvable: Vehicle-Detail.fxml");
            }

            loader = new FXMLLoader(url);
            root = loader.load();

            VehiDetaiCo controller = loader.getController();
            Article article = convertVehicleToArticle(vehicle);
            controller.receiveData(article);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Détails du véhicule - " + vehicle.getTitle());
            stage.show();

            System.out.println("🎉 Fenêtre de détails affichée avec succès!");

        } catch (Exception e) {
            System.err.println("❌ ERREUR CRITIQUE dans viewVehicleDetails: " + e.getMessage());
            e.printStackTrace();
            showAlert("❌ Erreur", "Impossible d'ouvrir les détails du véhicule: " + e.getMessage());
        }
    }

    private Article convertVehicleToArticle(Vehicle vehicle) {
        System.out.println("🔄 Conversion VEHICLE → ARTICLE - ID: " + vehicle.getId());

        Article article = new Article();
        article.setId(vehicle.getId());
        article.setIdVendeur(vehicle.getSellerId()); // Important pour les rendez-vous
        article.setTitre(vehicle.getTitle());
        article.setPrix(vehicle.getPrice());
        article.setDescription(vehicle.getDescription());
        article.setImage(vehicle.getImage());
        article.setCategorie(vehicle.getCategory());

        // Valeurs par défaut pour le développement
        article.setAnnee(2023);
        article.setKilometrage(50000);
        article.setTransmission("Manuelle");
        article.setCarburant("Essence");
        article.setMarque(extractBrandFromTitle(vehicle.getTitle()));
        article.setModele(vehicle.getTitle());
        article.setPuissance(120);
        article.setEtat("Excellent");

        System.out.println("✅ Article converti - ID: " + article.getId() +
                ", Titre: " + article.getTitre() +
                ", Vendeur: " + article.getIdVendeur());

        return article;
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Style de l'alerte pour correspondre au thème
        alert.getDialogPane().setStyle(
                "-fx-background-color: rgba(30, 41, 59, 0.95); " +
                        "-fx-border-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                        "-fx-border-width: 2; " +
                        "-fx-border-radius: 8; " +
                        "-fx-background-radius: 8;"
        );

        // Style du texte
        alert.getDialogPane().lookup(".content.label").setStyle(
                "-fx-text-fill: #e2e8f0; -fx-font-size: 14px;"
        );

        // Style des boutons
        Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setStyle(
                    "-fx-background-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                            "-fx-text-fill: white; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 8 20; " +
                            "-fx-background-radius: 6;"
            );
        }

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
        String[] colors = {"#3b82f6", "#8b5cf6", "#ec4899", "#10b981", "#f59e0b", "#ef4444"};
        return colors[(int)(Math.random() * colors.length)];
    }

    private String truncateDescription(String description) {
        if (description == null) return "Description non disponible";
        return description.length() <= 80 ? description : description.substring(0, 77) + "...";
    }
}