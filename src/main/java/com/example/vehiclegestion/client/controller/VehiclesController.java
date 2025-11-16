
package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.client.doa.VehicleDAO;
import com.example.vehiclegestion.client.model.Vehicle;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class VehiclesController implements Initializable {

    @FXML private TextField searchField;
    @FXML private FlowPane vehiclesContainer;

    private VehicleDAO vehicleDAO;
    private List<Vehicle> allVehicles;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        vehicleDAO = new VehicleDAO();
        loadAllVehicles();
    }

    private void loadAllVehicles() {
        allVehicles = vehicleDAO.getAllVehicles();
        displayVehicles(allVehicles);
    }

    private void displayVehicles(List<Vehicle> vehicles) {
        vehiclesContainer.getChildren().clear();

        for (Vehicle vehicle : vehicles) {
            vehiclesContainer.getChildren().add(createDetailedVehicleCard(vehicle));
        }
    }

    private VBox createDetailedVehicleCard(Vehicle vehicle) {
        VBox card = new VBox();
        card.getStyleClass().add("detailed-vehicle-card");
        card.setPrefSize(250, 300);

        // Image du véhicule
        ImageView imageView = new ImageView();
        try {
            if (vehicle.getImage() != null && !vehicle.getImage().isEmpty()) {
                Image image = new Image("file:" + vehicle.getImage());
                imageView.setImage(image);
            } else {
                // Image par défaut
                Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/vehiclegestion/images/default-vehicle.png"));
                imageView.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur de chargement de l'image: " + e.getMessage());
            // Image par défaut en cas d'erreur
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/com/example/vehiclegestion/images/default-vehicle.png"));
                imageView.setImage(defaultImage);
            } catch (Exception ex) {
                // Si l'image par défaut n'existe pas, on crée un label à la place
                Label imageErrorLabel = new Label("🚗 Image non disponible");
                imageErrorLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");
                card.getChildren().add(imageErrorLabel);
            }
        }

        imageView.setFitWidth(250);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(false);

        Label titleLabel = new Label(vehicle.getTitle());
        titleLabel.getStyleClass().add("vehicle-card-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(230);

        Label priceLabel = new Label(vehicle.getFormattedPrice());
        priceLabel.getStyleClass().add("vehicle-card-price");

        Label sellerLabel = new Label("Vendeur: " + vehicle.getSellerName());
        sellerLabel.getStyleClass().add("vehicle-card-seller");
        sellerLabel.setWrapText(true);
        sellerLabel.setMaxWidth(230);

        Label stateLabel = new Label("État: " + vehicle.getState());
        stateLabel.getStyleClass().add("vehicle-card-state");

        VBox infoContainer = new VBox(5, titleLabel, priceLabel, sellerLabel, stateLabel);
        infoContainer.setPadding(new Insets(10));

        // Ajouter les éléments à la carte
        card.getChildren().clear();
        card.getChildren().addAll(imageView, infoContainer);

        // Action sur le clic
        card.setOnMouseClicked(e -> showVehicleDetails(vehicle));

        return card;
    }

    private void showVehicleDetails(Vehicle vehicle) {
        // Implémenter l'affichage des détails du véhicule
        System.out.println("Détails du véhicule: " + vehicle.getTitle());
        // Vous pouvez ouvrir une nouvelle fenêtre ou changer de vue ici
    }

    @FXML
    private void searchVehicles() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            displayVehicles(allVehicles);
        } else {
            // Correction: remplacer toList() par collect(Collectors.toList()) pour Java 8/11
            List<Vehicle> filteredVehicles = allVehicles.stream()
                    .filter(v -> v.getTitle().toLowerCase().contains(searchText) ||
                            v.getDescription().toLowerCase().contains(searchText) ||
                            v.getCategory().toLowerCase().contains(searchText))
                    .collect(Collectors.toList()); // ✅ Correction ici
            displayVehicles(filteredVehicles);
        }
    }

    @FXML
    private void showDashboard() {
        ClientNavigationController.loadClientDashboard();
    }

    @FXML
    private void showFavorites() {
        ClientNavigationController.loadClientFavorites();
    }

    @FXML
    private void showHistory() {
        ClientNavigationController.loadClientHistory();
    }

    @FXML
    private void showProfile() {
        ClientNavigationController.loadClientProfile();
    }

    @FXML
    private void logout() {
        ClientNavigationController.logout();
    }
}