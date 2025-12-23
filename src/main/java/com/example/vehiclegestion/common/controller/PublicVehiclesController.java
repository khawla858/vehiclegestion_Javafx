package com.example.vehiclegestion.common.controller;

import com.example.vehiclegestion.client.doa.VehicleDAO;
import com.example.vehiclegestion.client.model.Vehicle;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.File;
        import javafx.scene.layout.*;
        import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class PublicVehiclesController {

    @FXML private GridPane vehiclesGrid;
    @FXML private VBox emptyState;
    @FXML private Label resultsCount;

    private VehicleDAO vehicleDAO = new VehicleDAO();
    private List<Vehicle> vehicles = new ArrayList<>();

    @FXML
    public void initialize() {
        System.out.println("🚗 Contrôleur véhicules publics initialisé");
        loadVehicles();
    }

    private void loadVehicles() {
        try {
            vehicles = vehicleDAO.getAllVehicles();

            if (vehicles.isEmpty()) {
                showEmptyState();
            } else {
                displayVehicles();
                updateResultsCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les véhicules");
        }
    }

    private void displayVehicles() {
        vehiclesGrid.getChildren().clear();

        int column = 0;
        int row = 0;
        int columns = 3;

        for (Vehicle vehicle : vehicles) {
            VBox vehicleCard = createVehicleCard(vehicle);
            vehiclesGrid.add(vehicleCard, column, row);

            column++;
            if (column >= columns) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createVehicleCard(Vehicle vehicle) {
        VBox card = new VBox(0);
        card.setPrefWidth(300);
        card.setStyle("-fx-background-color: rgba(30, 41, 59, 0.8); " +
                "-fx-border-radius: 15; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0.5, 0, 5);");
        card.setCursor(javafx.scene.Cursor.HAND);

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(180);
        loadVehicleImage(vehicle, imageContainer);

        // Contenu
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        Label title = new Label(vehicle.getTitle());
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #f1f5f9;");
        title.setWrapText(true);

        Label price = new Label(String.format("%,.0f DH", vehicle.getPrice()));
        price.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #3b82f6;");

        Button viewBtn = new Button("Voir détails");
        viewBtn.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #1e40af); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");

        viewBtn.setOnAction(e -> showLoginForDetails(vehicle));

        content.getChildren().addAll(title, price, viewBtn);
        card.getChildren().addAll(imageContainer, content);

        return card;
    }

    private void showLoginForDetails(Vehicle vehicle) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connexion requise");
        alert.setHeaderText("Fonctionnalité réservée aux membres");
        alert.setContentText("Veuillez vous connecter ou créer un compte pour:\n" +
                "• Voir les détails complets\n" +
                "• Contacter le vendeur\n" +
                "• Ajouter aux favoris\n" +
                "• Faire une réservation");

        ButtonType loginBtn = new ButtonType("Se connecter");
        ButtonType registerBtn = new ButtonType("S'inscrire");
        ButtonType cancelBtn = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(loginBtn, registerBtn, cancelBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == loginBtn) {
                redirectToLogin();
            } else if (response == registerBtn) {
                redirectToRegister();
            }
        });
    }

    private void redirectToLogin() {
        // Redirection vers la page de login
        try {
            Stage stage = (Stage) vehiclesGrid.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
            Parent loginPage = loader.load();
            stage.setScene(new Scene(loginPage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void redirectToRegister() {
        // Redirection vers la page d'inscription
        try {
            Stage stage = (Stage) vehiclesGrid.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/register.fxml"));
            Parent registerPage = loader.load();
            stage.setScene(new Scene(registerPage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadVehicleImage(Vehicle vehicle, StackPane container) {
        // Même logique que dans ClientVehiclesController mais simplifiée
        if (vehicle.getImage() != null && !vehicle.getImage().isEmpty()) {
            try {
                Image image = new Image(new File(vehicle.getImage()).toURI().toString());
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(300);
                imageView.setFitHeight(180);
                imageView.setPreserveRatio(true);
                container.getChildren().add(imageView);
            } catch (Exception e) {
                showDefaultImage(container);
            }
        } else {
            showDefaultImage(container);
        }
    }

    private void showDefaultImage(StackPane container) {
        // Image par défaut
        container.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);");
        Label carIcon = new Label("🚗");
        carIcon.setStyle("-fx-font-size: 48px; -fx-text-fill: white;");
        container.getChildren().add(carIcon);
    }

    private void updateResultsCount() {
        resultsCount.setText(vehicles.size() + " véhicules disponibles");
    }

    private void showEmptyState() {
        emptyState.setVisible(true);
        vehiclesGrid.setVisible(false);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}