package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.ArticleDAO;
import com.example.vehiclegestion.vendeur.model.Article;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;

public class AddVehicleFormController implements Initializable {

    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> categorieComboBox;
    @FXML private ComboBox<String> etatComboBox;
    @FXML private TextField imageField;
    @FXML private ImageView imagePreview;
    @FXML private VBox imagePreviewContainer;
    @FXML private Label messageLabel;

    @FXML private TextField marqueField;
    @FXML private TextField modeleField;
    @FXML private TextField anneeField;
    @FXML private TextField kilometrageField;
    @FXML private ComboBox<String> transmissionComboBox;
    @FXML private ComboBox<String> carburantComboBox;
    @FXML private TextField puissanceField;
    @FXML private TextField couleurField;
    @FXML private TextField reductionField;
    @FXML private TextField prixPromoField;


    private ArticleDAO articleDAO;
    private Stage dialogStage;
    private int vendeurId;
    private String selectedImagePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        articleDAO = new ArticleDAO();
        checkImageStructure(); // Ajoutez cette ligne

        setupForm();
    }

    private void setupForm() {
        categorieComboBox.getItems().addAll(
                "Berline", "SUV", "Compacte", "Citadine", "Sportive",
                "Utilitaire", "4x4", "Luxe", "Cabriolet", "Break"
        );
        transmissionComboBox.getItems().addAll("Manuelle", "Automatique", "Semi-automatique");

        carburantComboBox.getItems().addAll("Essence", "Diesel", "Électrique", "Hybride", "GPL");


        etatComboBox.getItems().addAll("neuf", "occasion");

        prixField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                prixField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        imagePreviewContainer.setManaged(false);
        imagePreviewContainer.setVisible(false);
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setVendeurId(int vendeurId) {
        this.vendeurId = vendeurId;
    }

    @FXML
    private void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image du véhicule");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {
            try {
                // CHANGEMENT: Utiliser LE MÊME DOSSIER que pour les clients
                Path targetDir = Path.of(System.getProperty("user.dir"), "src", "main", "resources", "uploads", "vehicles");
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                    System.out.println("📁 Dossier créé: " + targetDir);
                }

                // Générer un nom unique
                String originalName = selectedFile.getName();
                String extension = "";
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex > 0) {
                    extension = originalName.substring(dotIndex);
                }

                // Nom basé sur le timestamp + nom original
                String timestamp = String.valueOf(System.currentTimeMillis());
                String cleanName = originalName.substring(0, dotIndex > 0 ? dotIndex : originalName.length())
                        .replaceAll("[^a-zA-Z0-9]", "_");
                String newName = timestamp + "_" + cleanName + extension;

                Path destination = targetDir.resolve(newName);

                // Copier le fichier
                Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Stocker le chemin RELATIF (comme pour les clients)
                selectedImagePath = "uploads/vehicles/" + newName;

                // Afficher dans le champ
                imageField.setText(newName);

                // Aperçu
                Image image = new Image(destination.toUri().toString(), true);
                imagePreview.setImage(image);
                imagePreviewContainer.setManaged(true);
                imagePreviewContainer.setVisible(true);

                System.out.println("✅ Image copiée vers: " + destination);
                System.out.println("📋 Chemin stocké en DB: " + selectedImagePath);

            } catch (IOException ex) {
                ex.printStackTrace();
                showMessage("Erreur lors de la copie de l'image : " + ex.getMessage(), true);
            }
        }
    }

    @FXML
    private void addVehicle() {
        if (!validateForm()) {
            return;
        }

        try {
            Article article = new Article();
            article.setTitre(titreField.getText().trim());
            article.setDescription(descriptionField.getText().trim());
            article.setPrix(Double.parseDouble(prixField.getText()));
            article.setCategorie(categorieComboBox.getValue());
            article.setEtat(etatComboBox.getValue());
            article.setMarque(marqueField.getText().trim());
            article.setModele(modeleField.getText().trim());
            article.setAnnee(!anneeField.getText().isEmpty() ? Integer.parseInt(anneeField.getText()) : 2023);
            article.setKilometrage(!kilometrageField.getText().isEmpty() ? Integer.parseInt(kilometrageField.getText()) : 0);
            article.setTransmission(transmissionComboBox.getValue());
            article.setCarburant(carburantComboBox.getValue());
            article.setPuissance(!puissanceField.getText().isEmpty() ? Integer.parseInt(puissanceField.getText()) : 0);

            // SIMPLIFICATION: Utiliser directement le chemin déjà copié
            article.setImage(selectedImagePath); // "uploads/vehicles/nom_fichier.jpg"

            System.out.println("📋 Article à insérer - Image: " + article.getImage());

            boolean success = articleDAO.addArticle(article, vendeurId);

            if (success) {
                showMessage("✅ Véhicule ajouté avec succès!", false);
                clearForm();

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                javafx.application.Platform.runLater(() -> {
                                    if (dialogStage != null) dialogStage.close();
                                });
                            }
                        }, 2000
                );

            } else {
                showMessage("❌ Erreur lors de l'ajout du véhicule", true);
            }

        } catch (SQLException e) {
            showMessage("❌ Erreur base de données: " + e.getMessage(), true);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            showMessage("❌ Format de prix invalide", true);
        } catch (Exception e) {
            showMessage("❌ Erreur: " + e.getMessage(), true);
            e.printStackTrace();
        }
    }

    @FXML
    private void cancel() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (titreField.getText().trim().isEmpty()) errors.append("• Le titre est obligatoire\n");
        if (descriptionField.getText().trim().isEmpty()) errors.append("• La description est obligatoire\n");

        if (prixField.getText().trim().isEmpty()) {
            errors.append("• Le prix est obligatoire\n");
        } else {
            try {
                double prix = Double.parseDouble(prixField.getText());
                if (prix <= 0) errors.append("• Le prix doit être supérieur à 0\n");
            } catch (NumberFormatException e) {
                errors.append("• Le prix doit être un nombre valide\n");
            }
        }

        if (categorieComboBox.getValue() == null) errors.append("• La catégorie est obligatoire\n");
        if (etatComboBox.getValue() == null) errors.append("• L'état est obligatoire\n");

        if (errors.length() > 0) {
            showMessage("Veuillez corriger les erreurs suivantes:\n" + errors, true);
            return false;
        }

        return true;
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        if (isError) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12px;");
        }
    }

    private void clearForm() {
        titreField.clear();
        descriptionField.clear();
        prixField.clear();
        categorieComboBox.setValue(null);
        etatComboBox.setValue(null);
        imageField.clear();
        imagePreview.setImage(null);
        imagePreviewContainer.setManaged(false);
        imagePreviewContainer.setVisible(false);
        selectedImagePath = null;
        messageLabel.setText("");
    }


    private void checkImageStructure() {
        // Vérifier la structure des dossiers
        String[] pathsToCheck = {
                "src/main/resources/uploads/vehicles",
                "uploads/vehicles",
                "images/articles"
        };

        System.out.println("📁 === VÉRIFICATION STRUCTURE IMAGES ===");
        for (String path : pathsToCheck) {
            File dir = new File(path);
            if (dir.exists()) {
                System.out.println("✅ " + path + " - Existe (" +
                        dir.listFiles().length + " fichiers)");

                // Lister les fichiers
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        System.out.println("   📄 " + f.getName());
                    }
                }
            } else {
                System.out.println("❌ " + path + " - N'existe pas");
            }
        }
    }

}