package com.example.vehiclegestion.client.controller;

import javafx.scene.control.Alert;
import com.example.vehiclegestion.client.model.RendezVs;
import com.example.vehiclegestion.client.doa.RendezVsDAO;
import com.example.vehiclegestion.client.model.Vehicle;
import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.common.utils.NotificationService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class RendezVousFormController {

    @FXML private VBox mainContainer;
    @FXML private Label titleLabel;
    @FXML private Label vehicleInfoLabel;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private ComboBox<String> durationComboBox;
    @FXML private ComboBox<String> typeRdvComboBox;
    @FXML private TextArea descriptionTextArea;
    @FXML private Button submitBtn;
    @FXML private Button cancelBtn;
    @FXML private VBox successMessage;

    private Vehicle vehicle;
    private RendezVsDAO rendezVsDAO = new RendezVsDAO();
    private Runnable onRendezVousCreated;
    private NotificationService notificationService;

    public void initialize() {
        notificationService = NotificationService.getInstance();
        setupForm();
        setupStyling();
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        updateVehicleInfo();
    }

    public void setOnRendezVousCreated(Runnable callback) {
        this.onRendezVousCreated = callback;
    }

    private void setupForm() {
        // Configuration du DatePicker
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                // Désactiver les dates passées
                if (date.isBefore(today)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc;");
                }

                // Désactiver les dates trop éloignées (max 3 mois)
                if (date.isAfter(today.plusMonths(3))) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });

        // Configuration des heures disponibles
        timeComboBox.getItems().addAll(
                "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "11:30", "14:00", "14:30", "15:00", "15:30",
                "16:00", "16:30", "17:00", "17:30"
        );
        timeComboBox.setValue("10:00");

        // Configuration des durées
        durationComboBox.getItems().addAll(
                "30 minutes",
                "1 heure",
                "1 heure 30",
                "2 heures"
        );
        durationComboBox.setValue("1 heure");

        // Configuration des types de rendez-vous
        typeRdvComboBox.getItems().addAll(
                "Essai routier",
                "Consultation technique",
                "Visite de vérification",
                "Expertise détaillée",
                "Négociation prix"
        );
        typeRdvComboBox.setValue("Essai routier");

        // Configuration de la description
        descriptionTextArea.setPromptText("Précisez vos besoins particuliers, questions techniques, ou toute information utile pour préparer le rendez-vous...");

        // Gestion des événements
        submitBtn.setOnAction(e -> handleSubmit());
        cancelBtn.setOnAction(e -> closeForm());
    }

    private void setupStyling() {
        // Style général du container
        mainContainer.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-border-radius: 10;");

        // Style du titre
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-alignment: center;");

        // Style des labels
        vehicleInfoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-alignment: center; -fx-padding: 0 0 15 0;");

        // Style des champs de formulaire
        String fieldStyle = "-fx-background-color: #f8f9fa; -fx-border-color: #e9ecef; -fx-border-radius: 6; " +
                "-fx-padding: 12; -fx-font-size: 14px;";

        datePicker.setStyle(fieldStyle);
        timeComboBox.setStyle(fieldStyle);
        durationComboBox.setStyle(fieldStyle);
        typeRdvComboBox.setStyle(fieldStyle);
        descriptionTextArea.setStyle(fieldStyle + " -fx-pref-height: 100;");

        // Style des boutons
        submitBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 14px; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand;");

        cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-font-size: 14px; -fx-padding: 12 30; -fx-background-radius: 8; -fx-cursor: hand;");

        // Style du message de succès
        successMessage.setStyle("-fx-background-color: #d4edda; -fx-border-color: #c3e6cb; -fx-border-radius: 8; " +
                "-fx-padding: 20; -fx-alignment: center;");
        successMessage.setVisible(false);
    }

    private void updateVehicleInfo() {
        if (vehicle != null) {
            String[] titleParts = vehicle.getTitle().split(" ");
            String marque = titleParts.length > 0 ? titleParts[0] : "Marque";
            String modele = titleParts.length > 1 ? titleParts[1] : "Modèle";

            String info = String.format("%s %s • %s • %,d DH",
                    marque,
                    modele,
                    vehicle.getCategory(),
                    Math.round(vehicle.getPrice())
            );
            vehicleInfoLabel.setText(info);
        }
    }

    @FXML
    private void handleSubmit() {
        System.out.println("📅 CRÉATION RENDEZ-VOUS SIMPLE");

        // Validation simple
        if (datePicker.getValue() == null || timeComboBox.getValue() == null || vehicle == null) {
            System.out.println("❌ Données manquantes");
            showError("Erreur", "Veuillez remplir tous les champs");
            return;
        }

        try {
            // Créer l'objet rendez-vous
            RendezVs rendezVs = createRendezVsFromForm();

            System.out.println("🎯 Détails RDV:");
            System.out.println("   Client: " + rendezVs.getIdClient());
            System.out.println("   Vendeur: " + rendezVs.getIdVendeur());
            System.out.println("   Date: " + rendezVs.getDateRdv());
            System.out.println("   Heure: " + rendezVs.getHeureRdv());

            // Utiliser la méthode qui crée RDV + notifications
            boolean success = rendezVsDAO.creerRendezVousAvecNotification(rendezVs);

            if (success) {
                System.out.println("✅ RENDEZ-VOUS CRÉÉ AVEC NOTIFICATIONS");
                showSuccessMessage();
                if (onRendezVousCreated != null) {
                    onRendezVousCreated.run();
                }
            } else {
                System.out.println("❌ Échec création RDV");
                showError("Erreur", "Impossible de créer le rendez-vous");
            }

        } catch (Exception e) {
            System.err.println("💥 Erreur: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Problème technique");
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (datePicker.getValue() == null) {
            errors.append("• Veuillez sélectionner une date\n");
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            errors.append("• Veuillez sélectionner une date future\n");
        }

        if (timeComboBox.getValue() == null || timeComboBox.getValue().isEmpty()) {
            errors.append("• Veuillez sélectionner une heure\n");
        }

        if (typeRdvComboBox.getValue() == null || typeRdvComboBox.getValue().isEmpty()) {
            errors.append("• Veuillez sélectionner un type de rendez-vous\n");
        }

        if (!SessionManager.getInstance().estConnecte()) {
            errors.append("• Vous devez être connecté\n");
        }

        if (vehicle == null) {
            errors.append("• Aucun véhicule sélectionné\n");
        }

        if (errors.length() > 0) {
            showError("Validation", "Veuillez corriger les erreurs suivantes :\n" + errors.toString());
            return false;
        }

        return true;
    }

    private RendezVs createRendezVsFromForm() {
        System.out.println("=== DEBUG VEHICLE INFO ===");
        System.out.println("Vehicle ID: " + vehicle.getId());
        System.out.println("Seller ID: " + vehicle.getSellerId());
        System.out.println("Vehicle Title: " + vehicle.getTitle());

        RendezVs rdv = new RendezVs();

        // Informations de base
        rdv.setIdClient(SessionManager.getInstance().getUserId());
        rdv.setIdVendeur(vehicle.getSellerId());
        rdv.setIdArticle(vehicle.getId());

        // Date et heure
        rdv.setDateRdv(datePicker.getValue());
        rdv.setHeureRdv(LocalTime.parse(timeComboBox.getValue()));

        // Durée
        String dureeText = durationComboBox.getValue();
        int dureeMinutes = convertDurationToMinutes(dureeText);
        rdv.setDuree(dureeMinutes);

        // Type de rendez-vous
        String typeRdv = convertTypeRdvToDB(typeRdvComboBox.getValue());
        rdv.setTypeRdv(typeRdv);

        // Description
        rdv.setDescription(descriptionTextArea.getText().trim());

        // Statut par défaut
        rdv.setStatut("en attente");

        return rdv;
    }

    private int convertDurationToMinutes(String dureeText) {
        switch (dureeText) {
            case "30 minutes": return 30;
            case "1 heure": return 60;
            case "1 heure 30": return 90;
            case "2 heures": return 120;
            default: return 60;
        }
    }

    private String convertTypeRdvToDB(String typeDisplay) {
        switch (typeDisplay) {
            case "Essai routier": return "essai";
            case "Consultation technique": return "consultation";
            case "Visite de vérification": return "visite";
            case "Expertise détaillée": return "expertise";
            case "Négociation prix": return "consultation";
            default: return "essai";
        }
    }

    private void showSuccessMessage() {
        try {
            // Masquer tous les enfants sauf le message de succès
            for (Node node : mainContainer.getChildren()) {
                if (node != successMessage) {
                    node.setVisible(false);
                    node.setManaged(false);
                }
            }

            // Afficher le message de succès
            successMessage.setVisible(true);
            successMessage.setManaged(true);

            // Trouver et configurer le bouton de fermeture dans le message de succès
            for (Node node : successMessage.getChildren()) {
                if (node instanceof Button) {
                    Button closeBtn = (Button) node;
                    closeBtn.setOnAction(e -> closeForm());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur", "Problème d'affichage du message de succès.");
        }
    }

    @FXML
    private void closeForm() {
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}