package com.example.vehiclegestion.admin.controller;

import com.example.vehiclegestion.vendeur.model.Magasin;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Dialog pour ajouter ou modifier un magasin
 */
public class MagasinFormDialog extends Dialog<Magasin> {

    private final TextField nomField;
    private final TextField adresseField;
    private final TextField latitudeField;
    private final TextField longitudeField;
    private final TextArea descriptionArea;
    private final TextField telephoneField;
    private final TextField emailField;
    private final TextField siteWebField;
    private final TextField facebookField;
    private final TextField instagramField;
    private final ComboBox<String> categorieCombo;
    private final ComboBox<Integer> vendeurCombo;
    private final TextField logoField;
    private final Button browseLogoButton;
    private final TextField nbVentesField;

    // Champs pour les horaires (JSONB)
    private final Map<String, TextField> horairesFields;

    private final Magasin existingMagasin;
    private final boolean isEditMode;

    /**
     * Constructeur pour ajout d'un nouveau magasin
     */
    public MagasinFormDialog() {
        this(null);
    }

    /**
     * Constructeur pour modification d'un magasin existant
     */
    public MagasinFormDialog(Magasin magasin) {
        this.existingMagasin = magasin;
        this.isEditMode = (magasin != null);
        this.horairesFields = new HashMap<>();

        // Configuration du dialog
        setTitle(isEditMode ? "Modifier le magasin" : "Ajouter un nouveau magasin");
        setHeaderText(isEditMode ? "Modifiez les informations du magasin" : "Remplissez les informations du nouveau magasin");

        // Boutons
        ButtonType saveButtonType = new ButtonType(isEditMode ? "Enregistrer" : "Créer", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Créer le formulaire avec ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));
        grid.setStyle("-fx-background-color: #1e293b;");

        // Initialisation des champs
        nomField = createStyledTextField("Nom du magasin");
        adresseField = createStyledTextField("Adresse complète");
        latitudeField = createStyledTextField("Ex: 33.5731");
        longitudeField = createStyledTextField("Ex: -7.5898");

        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description du magasin");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setPrefWidth(300);
        descriptionArea.setWrapText(true);
        descriptionArea.setStyle("-fx-control-inner-background: #0f172a; -fx-text-fill: white; -fx-prompt-text-fill: #64748b;");

        telephoneField = createStyledTextField("Ex: 0612345678");
        emailField = createStyledTextField("contact@magasin.com");
        siteWebField = createStyledTextField("https://...");
        facebookField = createStyledTextField("URL Facebook");
        instagramField = createStyledTextField("URL Instagram");

        categorieCombo = new ComboBox<>();
        categorieCombo.getItems().addAll(
                "Voitures neuves", "Voitures d'occasion", "Voitures de luxe",
                "Voitures sportives", "4x4/SUV", "Utilitaires", "Motos",
                "Pièces détachées", "Services & Réparations", "Vente de véhicules"
        );
        categorieCombo.setPrefWidth(300);
        categorieCombo.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white;");

        vendeurCombo = new ComboBox<>();
        vendeurCombo.setPrefWidth(300);
        vendeurCombo.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white;");
        // TODO: Remplir avec les vendeurs disponibles depuis la BD
        // Exemple: vendeurCombo.getItems().addAll(vendeurDAO.getAllVendeursIds());

        logoField = createStyledTextField("Chemin du logo (images/logos/...)");
        browseLogoButton = new Button("📁 Parcourir");
        browseLogoButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-cursor: hand;");

        nbVentesField = createStyledTextField("0");
        nbVentesField.setPromptText("Nombre de ventes mensuelles");

        // Ajouter les champs au grid
        int row = 0;

        // En-tête
        Label headerLabel = new Label("📋 INFORMATIONS DE BASE");
        headerLabel.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(headerLabel, 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);

        grid.add(createStyledLabel("* Nom:"), 0, row);
        grid.add(nomField, 1, row++);

        grid.add(createStyledLabel("* Vendeur (ID):"), 0, row);
        grid.add(vendeurCombo, 1, row++);

        grid.add(createStyledLabel("* Catégorie:"), 0, row);
        grid.add(categorieCombo, 1, row++);

        grid.add(createStyledLabel("Description:"), 0, row);
        grid.add(descriptionArea, 1, row++);

        grid.add(createStyledLabel("Nb Ventes/Mois:"), 0, row);
        grid.add(nbVentesField, 1, row++);

        // Localisation
        Label locationLabel = new Label("📍 LOCALISATION GPS");
        locationLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(locationLabel, 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);

        grid.add(createStyledLabel("* Adresse:"), 0, row);
        grid.add(adresseField, 1, row++);

        grid.add(createStyledLabel("* Latitude:"), 0, row);
        VBox latBox = new VBox(5);
        latBox.getChildren().addAll(
                latitudeField,
                createInfoLabel("📌 Coordonnée géographique Nord-Sud (-90 à 90)")
        );
        grid.add(latBox, 1, row++);

        grid.add(createStyledLabel("* Longitude:"), 0, row);
        VBox lonBox = new VBox(5);
        lonBox.getChildren().addAll(
                longitudeField,
                createInfoLabel("📌 Coordonnée géographique Est-Ouest (-180 à 180)")
        );
        grid.add(lonBox, 1, row++);

        // Aide pour la localisation
        Label gpsHelp = createInfoLabel("💡 Astuce: Utilisez Google Maps pour obtenir les coordonnées GPS exactes");
        grid.add(gpsHelp, 0, row++, 2, 1);

        // Contact
        Label contactLabel = new Label("📞 CONTACT");
        contactLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(contactLabel, 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);

        grid.add(createStyledLabel("* Téléphone:"), 0, row);
        grid.add(telephoneField, 1, row++);

        grid.add(createStyledLabel("Email:"), 0, row);
        grid.add(emailField, 1, row++);

        // Horaires d'ouverture (JSONB)
        Label horairesLabel = new Label("🕐 HORAIRES D'OUVERTURE");
        horairesLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(horairesLabel, 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);

        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        for (String jour : jours) {
            TextField horaireField = createStyledTextField("Ex: 09:00 - 18:00 ou Fermé");
            horairesFields.put(jour, horaireField);

            grid.add(createStyledLabel(jour + ":"), 0, row);
            grid.add(horaireField, 1, row++);
        }

        // Web & Réseaux sociaux
        Label webLabel = new Label("🌐 WEB & RÉSEAUX SOCIAUX");
        webLabel.setStyle("-fx-text-fill: #06b6d4; -fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(webLabel, 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);

        grid.add(createStyledLabel("Site web:"), 0, row);
        grid.add(siteWebField, 1, row++);

        grid.add(createStyledLabel("Facebook:"), 0, row);
        grid.add(facebookField, 1, row++);

        grid.add(createStyledLabel("Instagram:"), 0, row);
        grid.add(instagramField, 1, row++);

        // Logo
        Label logoLabel = new Label("🖼️ LOGO DU MAGASIN");
        logoLabel.setStyle("-fx-text-fill: #ec4899; -fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(logoLabel, 0, row++, 2, 1);
        grid.add(new Separator(), 0, row++, 2, 1);

        grid.add(createStyledLabel("Logo:"), 0, row);
        HBox logoBox = new HBox(10);
        logoBox.getChildren().addAll(logoField, browseLogoButton);
        grid.add(logoBox, 1, row++);

        scrollPane.setContent(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(700);
        scrollPane.setStyle("-fx-background: #1e293b; -fx-background-color: #1e293b;");

        getDialogPane().setContent(scrollPane);
        getDialogPane().setStyle("-fx-background-color: #1e293b;");

        // Remplir les champs si mode édition
        if (isEditMode) {
            fillFieldsWithMagasinData(magasin);
        }

        // Action du bouton parcourir
        browseLogoButton.setOnAction(e -> handleBrowseLogo());

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
     * Créer un TextField stylisé
     */
    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(300);
        field.setStyle("-fx-background-color: #0f172a; -fx-text-fill: white; " +
                "-fx-prompt-text-fill: #64748b; -fx-border-color: #334155; " +
                "-fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 8px;");
        return field;
    }

    /**
     * Créer un Label stylisé
     */
    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 13px;");
        return label;
    }

    /**
     * Créer un label d'information
     */
    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-style: italic;");
        label.setWrapText(true);
        label.setMaxWidth(300);
        return label;
    }

    /**
     * Gérer la sélection du logo
     */
    private void handleBrowseLogo() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Sélectionner le logo du magasin");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        java.io.File file = fileChooser.showOpenDialog(getDialogPane().getScene().getWindow());
        if (file != null) {
            // Extraire le chemin relatif à partir de "images/logos/"
            String path = file.getAbsolutePath();
            if (path.contains("images")) {
                path = path.substring(path.indexOf("images"));
                path = path.replace("\\", "/");
            }
            logoField.setText(path);
        }
    }

    /**
     * Remplir les champs avec les données existantes
     */
    private void fillFieldsWithMagasinData(Magasin magasin) {
        nomField.setText(magasin.getNomMagasin());
        adresseField.setText(magasin.getAdresse());

        // Parser la localisation "latitude,longitude"
        if (magasin.getLocalisation() != null && magasin.getLocalisation().contains(",")) {
            String[] coords = magasin.getLocalisation().split(",");
            if (coords.length == 2) {
                latitudeField.setText(coords[0].trim());
                longitudeField.setText(coords[1].trim());
            }
        }

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

        if (magasin.getLogoMagasin() != null) {
            logoField.setText(magasin.getLogoMagasin());
        }

        if (magasin.getIdVendeur() != 0) {
            vendeurCombo.setValue(magasin.getIdVendeur());
        }

        if (magasin.getNbVentesMensuelles() != 0) {
            nbVentesField.setText(String.valueOf(magasin.getNbVentesMensuelles()));
        }

        // Remplir les horaires depuis le JSONB
        if (magasin.getHoraires() != null) {
            try {
                JSONObject horairesJson = new JSONObject(magasin.getHoraires());
                for (String jour : horairesFields.keySet()) {
                    if (horairesJson.has(jour)) {
                        horairesFields.get(jour).setText(horairesJson.getString(jour));
                    }
                }
            } catch (Exception e) {
                System.err.println("Erreur lors du parsing des horaires: " + e.getMessage());
            }
        }
    }

    /**
     * Valider les champs obligatoires
     */
    private boolean validateFields() {
        if (nomField.getText().trim().isEmpty()) {
            showAlert("Le nom du magasin est requis", Alert.AlertType.ERROR);
            return false;
        }

        if (adresseField.getText().trim().isEmpty()) {
            showAlert("L'adresse est requise", Alert.AlertType.ERROR);
            return false;
        }

        if (categorieCombo.getValue() == null) {
            showAlert("La catégorie est requise", Alert.AlertType.ERROR);
            return false;
        }

        // Validation des coordonnées GPS
        String lat = latitudeField.getText().trim();
        String lon = longitudeField.getText().trim();

        if (lat.isEmpty() || lon.isEmpty()) {
            showAlert("Les coordonnées GPS (latitude et longitude) sont requises", Alert.AlertType.ERROR);
            return false;
        }

        try {
            double latitude = Double.parseDouble(lat);
            double longitude = Double.parseDouble(lon);

            // Validation des ranges GPS
            if (latitude < -90 || latitude > 90) {
                showAlert("La latitude doit être entre -90 et 90", Alert.AlertType.ERROR);
                return false;
            }

            if (longitude < -180 || longitude > 180) {
                showAlert("La longitude doit être entre -180 et 180", Alert.AlertType.ERROR);
                return false;
            }

        } catch (NumberFormatException e) {
            showAlert("Les coordonnées GPS doivent être des nombres valides", Alert.AlertType.ERROR);
            return false;
        }

        // Validation téléphone
        if (telephoneField.getText().trim().isEmpty()) {
            showAlert("Le téléphone est requis", Alert.AlertType.ERROR);
            return false;
        }

        // Validation email si renseigné
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Format d'email invalide", Alert.AlertType.ERROR);
            return false;
        }

        // Validation nb_ventes_mensuelles
        if (!nbVentesField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(nbVentesField.getText().trim());
            } catch (NumberFormatException e) {
                showAlert("Le nombre de ventes mensuelles doit être un nombre entier", Alert.AlertType.ERROR);
                return false;
            }
        }

        return true;
    }

    /**
     * Créer un objet Magasin à partir des champs
     */
    private Magasin createMagasinFromFields() {
        Magasin magasin = isEditMode ? existingMagasin : new Magasin();

        magasin.setNomMagasin(nomField.getText().trim());
        magasin.setAdresse(adresseField.getText().trim());

        // Combiner latitude et longitude
        String localisation = latitudeField.getText().trim() + "," + longitudeField.getText().trim();
        magasin.setLocalisation(localisation);

        magasin.setDescription(descriptionArea.getText().trim());
        magasin.setTelephone(telephoneField.getText().trim());
        magasin.setEmailContact(emailField.getText().trim());
        magasin.setSiteWeb(siteWebField.getText().trim());
        magasin.setFacebook(facebookField.getText().trim());
        magasin.setInstagram(instagramField.getText().trim());
        magasin.setCategorie(categorieCombo.getValue());
        magasin.setLogoMagasin(logoField.getText().trim());

        if (vendeurCombo.getValue() != null) {
            magasin.setIdVendeur(vendeurCombo.getValue());
        }

        // Nb ventes mensuelles
        if (!nbVentesField.getText().trim().isEmpty()) {
            magasin.setNbVentesMensuelles(Integer.parseInt(nbVentesField.getText().trim()));
        } else {
            magasin.setNbVentesMensuelles(0);
        }

        // Construire le JSON pour horaires_ouverture
        JSONObject horairesJson = new JSONObject();
        for (Map.Entry<String, TextField> entry : horairesFields.entrySet()) {
            String jour = entry.getKey();
            String horaire = entry.getValue().getText().trim();
            if (!horaire.isEmpty()) {
                horairesJson.put(jour, horaire);
            } else {
                horairesJson.put(jour, "Fermé");
            }
        }
        magasin.setHoraires(horairesJson.toString());

        return magasin;
    }

    /**
     * Afficher une alerte
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Erreur de validation" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Styliser l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e293b;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }
}