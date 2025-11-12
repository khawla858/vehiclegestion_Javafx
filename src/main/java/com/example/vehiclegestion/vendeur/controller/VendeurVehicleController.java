package com.example.vehiclegestion.vendeur.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;

public class VendeurVehicleController {

    @FXML
    private TableView<?> vehiclesTable;

    @FXML
    public void initialize() {
        System.out.println("VendeurVehicleController initialisé");
        // Initialisez votre tableau ici
        setupTable();
    }

    private void setupTable() {
        // Configurez votre TableView
        System.out.println("Configuration du tableau des véhicules");

        // Exemple de données (à remplacer par vos vraies données)
        // vehiclesTable.setItems(yourDataList);
    }

    @FXML
    private void showAddVehicleForm() {
        System.out.println("Ouverture du formulaire d'ajout de véhicule");
        showAlert("Ajout Véhicule", "Formulaire d'ajout de véhicule ouvert");

        // Implémentez l'ouverture du formulaire d'ajout
        // Vous pouvez utiliser une nouvelle fenêtre ou changer le contenu
    }

    // Autres méthodes pour gérer les actions (modifier, supprimer, etc.)
    @FXML
    private void editVehicle() {
        System.out.println("Modification du véhicule sélectionné");
    }

    @FXML
    private void deleteVehicle() {
        System.out.println("Suppression du véhicule sélectionné");
    }

    @FXML
    private void viewVehicleDetails() {
        System.out.println("Voir les détails du véhicule");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}