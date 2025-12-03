package com.example.vehiclegestion.client.controller;

import javafx.scene.control.Alert;
import com.example.vehiclegestion.client.model.RendezVs;
import com.example.vehiclegestion.client.doa.RendezVsDAO;
import com.example.vehiclegestion.client.model.Vehicle; // Utilise Vehicle
import com.example.vehiclegestion.auth.SessionManager;
// AJOUTER CET IMPORT
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

    private Vehicle vehicle; // Utilise Vehicle au lieu de Article
    private RendezVsDAO rendezVsDAO = new RendezVsDAO();
    private Runnable onRendezVousCreated;

    // AJOUTER CET ATTRIBUT
    private NotificationService notificationService;

    public void initialize() {
        // AJOUTER CETTE LIGNE - Initialiser NotificationService
        notificationService = NotificationService.getInstance();
        setupForm();
        setupStyling();
    }

    public void setVehicle(Vehicle vehicle) { // Change setArticle en setVehicle
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
            // Extraire les informations du titre si nécessaire
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
        System.out.println("🔄 Début de la soumission du formulaire...");

        if (!validateForm()) {
            System.out.println("❌ Validation échouée");
            return;
        }

        try {
            RendezVs rendezVs = createRendezVsFromForm();
            System.out.println("📋 Rendez-vous créé, envoi à la DAO...");

            // Debug des données
            rendezVsDAO.debugRendezVs(rendezVs);

            boolean success = rendezVsDAO.creerRendezVous(rendezVs);

            if (success) {
                System.out.println("✅ Rendez-vous créé avec succès en base");

                // ================================================
                // AJOUTER CE CODE POUR LA NOTIFICATION
                // ================================================
                try {
                    // Formater la date et l'heure
                    String dateStr = rendezVs.getDateRdv().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    String heureStr = rendezVs.getHeureRdv().format(DateTimeFormatter.ofPattern("HH:mm"));

                    // Obtenir le nom du client

                    String nomClient = SessionManager.getInstance().getUserFullName();
                    if (nomClient == null || nomClient.isEmpty()) {
                        nomClient = "Un client";
                    }

                    System.out.println("📅 Envoi des notifications pour le RDV:");
                    System.out.println("   Client: " + rendezVs.getIdClient());
                    System.out.println("   Vendeur: " + rendezVs.getIdVendeur());
                    System.out.println("   Date: " + dateStr + " " + heureStr);

                    // 1. Notifier le CLIENT (notification immédiate)
                    notificationService.notifierRappelRendezVous(
                            rendezVs.getIdClient(),
                            999, // ID temporaire (remplacez par l'ID réel si disponible)
                            dateStr,
                            heureStr
                    );
                    System.out.println("   ✅ Notification envoyée au client");

                    // 2. Notifier le VENDEUR
                    notificationService.notifierDemandeEssai(
                            rendezVs.getIdVendeur(),
                            999, // ID temporaire
                            nomClient,
                            dateStr + " à " + heureStr
                    );
                    System.out.println("   ✅ Notification envoyée au vendeur");

                } catch (Exception e) {
                    System.err.println("⚠️ Erreur lors de l'envoi des notifications: " + e.getMessage());
                    e.printStackTrace();
                }
                // ================================================

                showSuccessMessage();
                if (onRendezVousCreated != null) {
                    onRendezVousCreated.run();
                }
            } else {
                System.out.println("❌ Échec de création en base");
                showError("Erreur", "Impossible de créer le rendez-vous. Veuillez réessayer.");
            }
        } catch (Exception e) {
            System.err.println("💥 Erreur lors de la création: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Une erreur est survenue: " + e.getMessage());
        }

        System.out.println("=== DEBUG HANDLESUBMIT ===");
        System.out.println("Date: " + datePicker.getValue());
        System.out.println("Heure: " + timeComboBox.getValue());
        System.out.println("Durée: " + durationComboBox.getValue());
        System.out.println("Type: " + typeRdvComboBox.getValue());
        System.out.println("Véhicule: " + (vehicle != null ? vehicle.getTitle() : "NULL"));
        System.out.println("Utilisateur connecté: " + SessionManager.getInstance().estConnecte());
        System.out.println("ID User: " + SessionManager.getInstance().getUserId());

        if (!validateForm()) {
            System.out.println("❌ Validation échouée");
            return;
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
        rdv.setIdClient(SessionManager.getInstance().getUserId());
        rdv.setIdVendeur(vehicle.getSellerId());
        rdv.setIdArticle(vehicle.getId());

        // Informations de base
        rdv.setIdClient(SessionManager.getInstance().getUserId());
        rdv.setIdVendeur(vehicle.getSellerId()); // Utilise getSellerId()
        rdv.setIdArticle(vehicle.getId()); // Utilise getId()

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