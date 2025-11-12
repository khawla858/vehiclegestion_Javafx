package com.example.vehiclegestion.vendeur.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class VendeurDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        System.out.println("DashboardController initialisé");
    }
}