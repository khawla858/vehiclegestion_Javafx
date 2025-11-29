package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.ArticleDAO;
import com.example.vehiclegestion.vendeur.model.Article;
import com.example.vehiclegestion.auth.SessionManager; // ✅ AJOUT IMPORT
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
    private SessionManager sessionManager = SessionManager.getInstance(); // ✅ AJOUT
    private Integer currentMagasinId; // ✅ AJOUT: Pour stocker l'ID du magasin

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        articleDAO = new ArticleDAO();
        setupForm();

        // ✅ AJOUT: Récupérer l'ID du magasin courant depuis SessionManager
        currentMagasinId = sessionManager.getCurrentMagasinId();
        String magasinNom = sessionManager.getCurrentMagasinNom();

        if (currentMagasinId != null) {
            System.out.println("✅ AddVehicleForm - Magasin courant détecté: " + magasinNom + " (ID: " + currentMagasinId + ")");
            if (messageLabel != null) {
                messageLabel.setText("Ajout au magasin: " + magasinNom);
                messageLabel.setStyle("-fx-text-fill: #17a2b8; -fx-font-size: 12px;");
            }
        } else {
            System.out.println("ℹ️ AddVehicleForm - Aucun magasin spécifique");
        }
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
        System.out.println("✅ Vendeur ID défini dans formulaire: " + vendeurId);
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
                Path imagesDir = Path.of(System.getProperty("user.dir"), "images", "articles");
                if (!Files.exists(imagesDir)) Files.createDirectories(imagesDir);

                String originalName = selectedFile.getName();
                String ext = "";
                int i = originalName.lastIndexOf('.');
                if (i > 0) ext = originalName.substring(i);

                String newName = "article_" + System.currentTimeMillis() + ext;
                Path dest = imagesDir.resolve(newName);

                Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                selectedImagePath = "images/articles/" + newName;
                imageField.setText(newName);

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

            // ✅ AJOUT CRITIQUE: Associer l'article au magasin courant
            if (currentMagasinId != null) {
                article.setIdMagasin(currentMagasinId);
                System.out.println("✅ Véhicule associé au magasin ID: " + currentMagasinId);
            } else {
                System.out.println("⚠️ Aucun magasin spécifique détecté, l'article n'aura pas de magasin associé");
                article.setIdMagasin(0); // ou null selon votre modèle
            }

            // Gestion de l'image
            String imageRelativePath = null;
            if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                File destDir = new File("images/articles");
                if (!destDir.exists()) destDir.mkdirs();

                String fileName = "article_" + System.currentTimeMillis() + ".png";
                File source = new File(selectedImagePath);
                File destination = new File(destDir, fileName);

                Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                imageRelativePath = "images/articles/" + fileName;
            }

            article.setImage(imageRelativePath);

            // Insertion dans la base
            boolean success = articleDAO.addArticle(article, this.vendeurId);

            if (success) {
                String successMessage = "✅ Véhicule ajouté avec succès!";
                if (currentMagasinId != null) {
                    String magasinNom = sessionManager.getCurrentMagasinNom();
                    successMessage += " (Magasin: " + magasinNom + ")";
                }
                showMessage(successMessage, false);
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