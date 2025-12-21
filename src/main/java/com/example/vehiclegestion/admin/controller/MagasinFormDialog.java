package com.example.vehiclegestion.admin.controller;

import com.example.vehiclegestion.vendeur.model.Magasin;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/**
 * Dialog pour modifier un magasin
 */
public class MagasinFormDialog extends Dialog<Magasin> {

    private final TextField nomField;
    private final TextField adresseField;
    private final TextField localisationField;
    private final TextArea descriptionArea;
    private final TextField telephoneField;
    private final TextField emailField;
    private final TextField siteWebField;
    private final TextField facebookField;
    private final TextField instagramField;
    private final ComboBox<String> categorieCombo;
    private final TextField horairesField;

    private final Magasin existingMagasin;

    /**
     * Constructeur pour modification d'un magasin
     */
    public MagasinFormDialog(Magasin magasin) {
        this.existingMagasin = magasin;

        // Configuration du dialog
        setTitle("Modifier le magasin");
        setHeaderText("Modifiez les informations du magasin");

        // Boutons
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Créer le formulaire avec ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        // Champs du formulaire
        nomField = new TextField();
        nomField.setPromptText("Nom du magasin");
        nomField.setPrefWidth(300);

        adresseField = new TextField();
        adresseField.setPromptText("Adresse complète");
        adresseField.setPrefWidth(300);

        localisationField = new TextField();
        localisationField.setPromptText("Ville");
        localisationField.setPrefWidth(300);

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description du magasin");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setPrefWidth(300);
        descriptionArea.setWrapText(true);

        telephoneField = new TextField();
        telephoneField.setPromptText("Téléphone");
        telephoneField.setPrefWidth(300);

        emailField = new TextField();
        emailField.setPromptText("Email de contact");
        emailField.setPrefWidth(300);

        siteWebField = new TextField();
        siteWebField.setPromptText("https://...");
        siteWebField.setPrefWidth(300);

        facebookField = new TextField();
        facebookField.setPromptText("URL Facebook");
        facebookField.setPrefWidth(300);

        instagramField = new TextField();
        instagramField.setPromptText("URL Instagram");
        instagramField.setPrefWidth(300);

        categorieCombo = new ComboBox<>();
        categorieCombo.getItems().addAll(
                "Voitures neuves", "Voitures d'occasion", "Voitures de luxe",
                "Voitures sportives", "4x4/SUV", "Utilitaires", "Motos", "Pièces détachées"
        );
        categorieCombo.setPrefWidth(300);

        horairesField = new TextField();
        horairesField.setPromptText("Ex: Lun-Ven 9h-18h, Sam 10h-17h");
        horairesField.setPrefWidth(300);

        // Ajouter les champs au grid
        int row = 0;

        // Informations de base
        grid.add(new Label("📋 INFORMATIONS DE BASE"), 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);

        grid.add(new Label("Nom:"), 0, row);
        grid.add(nomField, 1, row++);

        grid.add(new Label("Catégorie:"), 0, row);
        grid.add(categorieCombo, 1, row++);

        grid.add(new Label("Description:"), 0, row);
        grid.add(descriptionArea, 1, row++);

        // Localisation
        grid.add(new Label("📍 LOCALISATION"), 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);

        grid.add(new Label("Adresse:"), 0, row);
        grid.add(adresseField, 1, row++);

        grid.add(new Label("Ville:"), 0, row);
        grid.add(localisationField, 1, row++);

        // Contact
        grid.add(new Label("📞 CONTACT"), 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);

        grid.add(new Label("Téléphone:"), 0, row);
        grid.add(telephoneField, 1, row++);

        grid.add(new Label("Email:"), 0, row);
        grid.add(emailField, 1, row++);

        grid.add(new Label("Horaires:"), 0, row);
        grid.add(horairesField, 1, row++);

        // Web & Réseaux sociaux
        grid.add(new Label("🌐 WEB & RÉSEAUX SOCIAUX"), 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);

        grid.add(new Label("Site web:"), 0, row);
        grid.add(siteWebField, 1, row++);

        grid.add(new Label("Facebook:"), 0, row);
        grid.add(facebookField, 1, row++);

        grid.add(new Label("Instagram:"), 0, row);
        grid.add(instagramField, 1, row++);

        scrollPane.setContent(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);

        getDialogPane().setContent(scrollPane);

        // Remplir les champs avec les données existantes
        fillFieldsWithMagasinData(magasin);

        // Validation et conversion du résultat
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (!validateFields()) {
                    return null;
                }
                return createMagasinFromFields();
            }
            return null;
        });

        // Focus sur le premier champ
        nomField.requestFocus();
    }

    /**
     * Remplir les champs avec les données existantes
     */
    private void fillFieldsWithMagasinData(Magasin magasin) {
        nomField.setText(magasin.getNomMagasin());
        adresseField.setText(magasin.getAdresse());
        localisationField.setText(magasin.getLocalisation());
        descriptionArea.setText(magasin.getDescription());

        if (magasin.getTelephone() != null) {
            telephoneField.setText(magasin.getTelephone());
        }

        if (magasin.getEmailContact() != null) {
            emailField.setText(magasin.getEmailContact());
        }

        if (magasin.getSiteWeb() != null) {
            siteWebField.setText(magasin.getSiteWeb());
        }

        if (magasin.getFacebook() != null) {
            facebookField.setText(magasin.getFacebook());
        }

        if (magasin.getInstagram() != null) {
            instagramField.setText(magasin.getInstagram());
        }

        if (magasin.getCategorie() != null) {
            categorieCombo.setValue(magasin.getCategorie());
        }

        if (magasin.getHoraires() != null) {
            horairesField.setText(magasin.getHoraires());
        }
    }

    /**
     * Valider les champs obligatoires
     */
    private boolean validateFields() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert("Le nom du magasin est requis");
            return false;
        }

        if (adresseField.getText().trim().isEmpty()) {
            showAlert("L'adresse est requise");
            return false;
        }

        if (localisationField.getText().trim().isEmpty()) {
            showAlert("La localisation (ville) est requise");
            return false;
        }

        // Validation email si renseigné
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Format d'email invalide");
            return false;
        }

        return true;
    }

    /**
     * Créer un objet Magasin à partir des champs
     */
    private Magasin createMagasinFromFields() {
        Magasin magasin = existingMagasin;

        magasin.setNomMagasin(nomField.getText().trim());
        magasin.setAdresse(adresseField.getText().trim());
        magasin.setLocalisation(localisationField.getText().trim());
        magasin.setDescription(descriptionArea.getText().trim());
        magasin.setTelephone(telephoneField.getText().trim());
        magasin.setEmailContact(emailField.getText().trim());
        magasin.setSiteWeb(siteWebField.getText().trim());
        magasin.setFacebook(facebookField.getText().trim());
        magasin.setInstagram(instagramField.getText().trim());
        magasin.setCategorie(categorieCombo.getValue());
        magasin.setHoraires(horairesField.getText().trim());

        return magasin;
    }

    /**
     * Afficher une alerte d'erreur
     */
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Afficher le dialog et retourner le résultat
     */
    /*public Optional<Magasin> showAndWait() {
        return super.showAndWait();
    }*/
}