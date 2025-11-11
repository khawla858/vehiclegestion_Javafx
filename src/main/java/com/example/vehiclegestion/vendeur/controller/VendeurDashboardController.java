package com.example.vehiclegestion.vendeur.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class VendeurDashboardController {

    @FXML
    private Label lblVehiclesCount;

    @FXML
    private Label lblPendingRequests;

    @FXML
    private Label lblSalesCount;

    @FXML
    public void initialize() {
        // Ici on peut récupérer les données réelles via le service
        // Exemple statique pour commencer
        lblVehiclesCount.setText("12");
        lblPendingRequests.setText("3");
        lblSalesCount.setText("8");
    }
}
