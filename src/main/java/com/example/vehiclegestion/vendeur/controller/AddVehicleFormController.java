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
                // dossier de stockage (externe, à côté du jar ou dans le répertoire de travail)
                Path imagesDir = Path.of(System.getProperty("user.dir"), "images", "articles");
                if (!Files.exists(imagesDir)) Files.createDirectories(imagesDir);

                // extension d'origine
                String originalName = selectedFile.getName();
                String ext = "";
                int i = originalName.lastIndexOf('.');
                if (i > 0) ext = originalName.substring(i);

                // nom unique
                String newName = "article_" + System.currentTimeMillis() + ext;

                Path dest = imagesDir.resolve(newName);

                // copier le fichier sélectionné vers le dossier de l'application
                Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                // stocker le chemin RELATIF (par rapport au dossier de l'application)
                // tu peux stocker "images/articles/..." ou absolute file: URI selon ton choix.
                selectedImagePath = "images/articles/" + newName;

                // afficher le nom dans le champ
                imageField.setText(newName);

                // afficher un aperçu (utiliser file: URI)
                Image image = new Image(dest.toUri().toString(), true);
                imagePreview.setImage(image);
                imagePreviewContainer.setManaged(true);
                imagePreviewContainer.setVisible(true);

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
            //article.setCouleur(couleurField.getText().trim());
            //article.setReduction(!reductionField.getText().isEmpty() ? Integer.parseInt(reductionField.getText()) : 0);
            //article.setPrixPromo(!prixPromoField.getText().isEmpty() ? Double.parseDouble(prixPromoField.getText()) : article.getPrix());


            // ================================================
            // 1. SAUVEGARDE DE L’IMAGE DANS LE PROJET
            // ================================================
            String imageRelativePath = null;

            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {

                // dossier où tu veux stocker les images dans ton projet
                File destDir = new File("images/articles");
                if (!destDir.exists()) destDir.mkdirs();

                // nom unique pour éviter les conflits
                String fileName = "article_" + System.currentTimeMillis() + ".png";

                File source = new File(selectedImagePath);
                File destination = new File(destDir, fileName);

                // copier l'image dans le projet
                java.nio.file.Files.copy(
                        source.toPath(),
                        destination.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );

                // chemin RELATIF que tu stockes dans la base
                imageRelativePath = "images/articles/" + fileName;
            }

            article.setImage(imageRelativePath);

            // ================================================
            // 2. INSERTION DANS LA BASE
            // ================================================
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
}
