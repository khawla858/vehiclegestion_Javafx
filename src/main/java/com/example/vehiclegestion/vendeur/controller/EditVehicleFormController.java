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
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class EditVehicleFormController implements Initializable {

    // ✅ VÉRIFIER QUE TOUS CES CHAMPS EXISTENT DANS LE FXML
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField prixField;
    @FXML private ComboBox<String> categorieComboBox;
    @FXML private ComboBox<String> etatComboBox;
    @FXML private TextField imageField;
    @FXML private ImageView imagePreview;
    @FXML private VBox imagePreviewContainer;
    @FXML private Label messageLabel;
    @FXML private TextField marqueField;        // ✅ VÉRIFIER
    @FXML private TextField modeleField;        // ✅ VÉRIFIER
    @FXML private TextField anneeField;         // ✅ VÉRIFIER
    @FXML private TextField kilometrageField;   // ✅ VÉRIFIER
    @FXML private ComboBox<String> transmissionComboBox;
    @FXML private ComboBox<String> carburantComboBox;
    @FXML private TextField puissanceField;     // ✅ VÉRIFIER
    @FXML private TextField couleurField;       // ✅ VÉRIFIER
    @FXML private TextField reductionField;     // ✅ VÉRIFIER
    @FXML private TextField prixPromoField;     // ✅ VÉRIFIER
    @FXML private Label formTitleLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private ArticleDAO articleDAO;
    private Stage dialogStage;
    private Article currentArticle;
    private String selectedImagePath;
    private boolean imageChanged = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        articleDAO = new ArticleDAO();
        setupForm();
        setupValidation();
    }

    private void setupForm() {
        // Configuration des ComboBox
        categorieComboBox.getItems().addAll(
                "Berline", "SUV", "Compacte", "Citadine", "Sportive",
                "Utilitaire", "4x4", "Luxe", "Cabriolet", "Break"
        );

        transmissionComboBox.getItems().addAll(
                "Manuelle", "Automatique", "Semi-automatique"
        );

        carburantComboBox.getItems().addAll(
                "Essence", "Diesel", "Électrique", "Hybride", "GPL"
        );

        etatComboBox.getItems().addAll("neuf", "occasion", "vendu");

        // Style du titre
        if (formTitleLabel != null) {
            formTitleLabel.setStyle(
                    "-fx-font-size: 24px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-text-fill: #2c3e50;"
            );
        }

        // Style des boutons
        if (saveButton != null) {
            saveButton.setStyle(
                    "-fx-background-color: #27ae60; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 12 30; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;"
            );
        }

        if (cancelButton != null) {
            cancelButton.setStyle(
                    "-fx-background-color: #95a5a6; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 14px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 12 30; " +
                            "-fx-background-radius: 8; " +
                            "-fx-cursor: hand;"
            );
        }

        // Masquer le conteneur d'aperçu initialement
        if (imagePreviewContainer != null) {
            imagePreviewContainer.setManaged(true);
            imagePreviewContainer.setVisible(true);
        }
    }

    private void setupValidation() {
        // Validation numérique pour le prix
        prixField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                prixField.setText(oldValue);
            }
        });

        // Validation numérique pour l'année
        anneeField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                anneeField.setText(oldValue);
            }
        });

        // Validation numérique pour le kilométrage
        kilometrageField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                kilometrageField.setText(oldValue);
            }
        });

        // Validation numérique pour la puissance
        puissanceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                puissanceField.setText(oldValue);
            }
        });

        // Validation numérique pour la réduction
        reductionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                reductionField.setText(oldValue);
            }
        });

        // Validation numérique pour le prix promo
        prixPromoField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                prixPromoField.setText(oldValue);
            }
        });
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Méthode principale : charger l'article à modifier
     */
    public void setArticle(Article article) {
        this.currentArticle = article;
        loadArticleData();
    }

    /**
     * Pré-remplir le formulaire avec les données de l'article
     */
    private void loadArticleData() {
        if (currentArticle == null) {
            System.err.println("❌ Aucun article à charger !");
            return;
        }

        System.out.println("\n📋 === CHARGEMENT ARTICLE POUR MODIFICATION ===");
        System.out.println("   - ID: " + currentArticle.getId());
        System.out.println("   - Titre: " + currentArticle.getTitre());

        // Remplir les champs de base
        titreField.setText(currentArticle.getTitre());
        descriptionField.setText(currentArticle.getDescription());
        prixField.setText(String.valueOf(currentArticle.getPrix()));

        // Sélectionner les valeurs des ComboBox
        categorieComboBox.setValue(currentArticle.getCategorie());
        etatComboBox.setValue(currentArticle.getEtat());

        // Remplir les champs spécifiques véhicule
        marqueField.setText(currentArticle.getMarque());
        modeleField.setText(currentArticle.getModele());
        anneeField.setText(String.valueOf(currentArticle.getAnnee()));
        kilometrageField.setText(String.valueOf(currentArticle.getKilometrage()));

        transmissionComboBox.setValue(currentArticle.getTransmission());
        carburantComboBox.setValue(currentArticle.getCarburant());

        puissanceField.setText(String.valueOf(currentArticle.getPuissance()));
        couleurField.setText(currentArticle.getCouleur());
        reductionField.setText(String.valueOf(currentArticle.getReduction()));
        prixPromoField.setText(String.valueOf(currentArticle.getPrixPromo()));

        // Charger l'image actuelle
        loadCurrentImage();
    }

    /**
     * Charger et afficher l'image actuelle de l'article
     */
    private void loadCurrentImage() {
        String imagePath = currentArticle.getImage();

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);

                if (imageFile.exists()) {
                    Image image = new Image(imageFile.toURI().toString(), true);
                    imagePreview.setImage(image);
                    imageField.setText(imageFile.getName());
                    selectedImagePath = imagePath;

                    System.out.println("   ✅ Image chargée: " + imagePath);
                } else {
                    System.err.println("   ⚠️ Image introuvable: " + imagePath);
                    setPlaceholderImage();
                }
            } catch (Exception e) {
                System.err.println("   ❌ Erreur chargement image: " + e.getMessage());
                setPlaceholderImage();
            }
        } else {
            setPlaceholderImage();
        }
    }

    /**
     * Afficher une image de placeholder si aucune image
     */
    private void setPlaceholderImage() {
        imageField.setText("Aucune image");
        imagePreview.setImage(null);
        // Optionnel : mettre une image par défaut
    }

    /**
     * Parcourir et sélectionner une nouvelle image
     */
    @FXML
    private void browseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une nouvelle image du véhicule");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(dialogStage);

        if (selectedFile != null) {
            try {
                // Créer le dossier de stockage
                Path imagesDir = Path.of(System.getProperty("user.dir"), "images", "articles");
                if (!Files.exists(imagesDir)) {
                    Files.createDirectories(imagesDir);
                }

                // Générer un nom unique
                String originalName = selectedFile.getName();
                String ext = "";
                int i = originalName.lastIndexOf('.');
                if (i > 0) ext = originalName.substring(i);

                String newName = "article_" + currentArticle.getId() + "_" + System.currentTimeMillis() + ext;
                Path dest = imagesDir.resolve(newName);

                // Copier le fichier
                Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                // Stocker le nouveau chemin
                selectedImagePath = "images/articles/" + newName;
                imageChanged = true;

                // Afficher l'aperçu
                imageField.setText(newName);
                Image image = new Image(dest.toUri().toString(), true);
                imagePreview.setImage(image);

                System.out.println("   ✅ Nouvelle image sélectionnée: " + newName);

            } catch (IOException ex) {
                ex.printStackTrace();
                showMessage("❌ Erreur lors de la copie de l'image : " + ex.getMessage(), true);
            }
        }
    }

    /**
     * Sauvegarder les modifications
     */
    @FXML
    private void saveChanges() {
        System.out.println("\n💾 === DÉBUT MODIFICATION ARTICLE ===");

        if (!validateForm()) {
            return;
        }

        try {
            // ✅ CORRECTION : Vérifier null avant trim()
            currentArticle.setTitre(titreField.getText() != null ? titreField.getText().trim() : "");
            currentArticle.setDescription(descriptionField.getText() != null ? descriptionField.getText().trim() : "");
            currentArticle.setPrix(Double.parseDouble(prixField.getText() != null ? prixField.getText() : "0"));
            currentArticle.setCategorie(categorieComboBox.getValue());
            currentArticle.setEtat(etatComboBox.getValue());

            // ✅ CORRECTION : Gestion des champs nullables
            currentArticle.setMarque(marqueField.getText() != null ? marqueField.getText().trim() : null);
            currentArticle.setModele(modeleField.getText() != null ? modeleField.getText().trim() : null);

            currentArticle.setAnnee(anneeField.getText() != null && !anneeField.getText().isEmpty()
                    ? Integer.parseInt(anneeField.getText()) : 0);

            currentArticle.setKilometrage(kilometrageField.getText() != null && !kilometrageField.getText().isEmpty()
                    ? Integer.parseInt(kilometrageField.getText()) : 0);

            currentArticle.setTransmission(transmissionComboBox.getValue());
            currentArticle.setCarburant(carburantComboBox.getValue());

            currentArticle.setPuissance(puissanceField.getText() != null && !puissanceField.getText().isEmpty()
                    ? Integer.parseInt(puissanceField.getText()) : 0);

            currentArticle.setCouleur(couleurField.getText() != null ? couleurField.getText().trim() : null);

            currentArticle.setReduction(reductionField.getText() != null && !reductionField.getText().isEmpty()
                    ? Integer.parseInt(reductionField.getText()) : 0);

            currentArticle.setPrixPromo(prixPromoField.getText() != null && !prixPromoField.getText().isEmpty()
                    ? Double.parseDouble(prixPromoField.getText()) : currentArticle.getPrix());

            // Mettre à jour l'image si elle a changé
            if (imageChanged && selectedImagePath != null) {
                deleteOldImage(currentArticle.getImage());
                currentArticle.setImage(selectedImagePath);
            }

            System.out.println("   - ID Article: " + currentArticle.getId());
            System.out.println("   - Nouveau titre: " + currentArticle.getTitre());
            System.out.println("   - Nouveau prix: " + currentArticle.getPrix() + " DH");

            // ✅ DEBUG : Vérifier les données avant sauvegarde
            System.out.println("   - Marque: " + currentArticle.getMarque());
            System.out.println("   - Modèle: " + currentArticle.getModele());
            System.out.println("   - Année: " + currentArticle.getAnnee());

            // Appel DAO pour mise à jour
            boolean success = articleDAO.updateArticle(currentArticle);

            if (success) {
                System.out.println("   ✅ Modification réussie en base de données");
                showMessage("✅ Véhicule modifié avec succès!", false);

                // Fermer la fenêtre après 2 secondes
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                javafx.application.Platform.runLater(() -> {
                                    if (dialogStage != null) {
                                        dialogStage.close();
                                    }
                                });
                            }
                        }, 2000
                );
            } else {
                System.err.println("   ❌ Échec de la modification en base");
                showMessage("❌ Erreur lors de la modification du véhicule", true);
            }

        } catch (SQLException e) {
            System.err.println("   ❌ Erreur SQL: " + e.getMessage());
            showMessage("❌ Erreur base de données: " + e.getMessage(), true);
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("   ❌ Erreur format numérique");
            showMessage("❌ Format de nombre invalide", true);
        } catch (Exception e) {
            System.err.println("   ❌ Erreur: " + e.getMessage());
            showMessage("❌ Erreur: " + e.getMessage(), true);
            e.printStackTrace();
        }

        System.out.println("💾 === FIN MODIFICATION ARTICLE ===\n");
    }

    /**
     * Supprimer l'ancienne image du disque
     */
    private void deleteOldImage(String oldImagePath) {
        if (oldImagePath != null && !oldImagePath.isEmpty()) {
            try {
                Path oldPath = Path.of(System.getProperty("user.dir"), oldImagePath);
                Files.deleteIfExists(oldPath);
                System.out.println("   🗑️ Ancienne image supprimée: " + oldImagePath);
            } catch (IOException e) {
                System.err.println("   ⚠️ Impossible de supprimer l'ancienne image: " + e.getMessage());
            }
        }
    }

    /**
     * Annuler la modification
     */
    @FXML
    private void cancel() {
        System.out.println("❌ Modification annulée");

        // Supprimer la nouvelle image si elle a été uploadée mais pas sauvegardée
        if (imageChanged && selectedImagePath != null &&
                !selectedImagePath.equals(currentArticle.getImage())) {
            deleteOldImage(selectedImagePath);
        }

        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Valider le formulaire
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (titreField.getText().trim().isEmpty()) {
            errors.append("• Le titre est obligatoire\n");
        }

        if (descriptionField.getText().trim().isEmpty()) {
            errors.append("• La description est obligatoire\n");
        }

        if (prixField.getText().trim().isEmpty()) {
            errors.append("• Le prix est obligatoire\n");
        } else {
            try {
                double prix = Double.parseDouble(prixField.getText());
                if (prix <= 0) {
                    errors.append("• Le prix doit être supérieur à 0\n");
                }
            } catch (NumberFormatException e) {
                errors.append("• Le prix doit être un nombre valide\n");
            }
        }

        if (categorieComboBox.getValue() == null) {
            errors.append("• La catégorie est obligatoire\n");
        }

        if (etatComboBox.getValue() == null) {
            errors.append("• L'état est obligatoire\n");
        }

        if (errors.length() > 0) {
            showMessage("Veuillez corriger les erreurs suivantes:\n" + errors, true);
            return false;
        }

        return true;
    }

    /**
     * Afficher un message
     */
    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);

        if (isError) {
            messageLabel.setStyle(
                    "-fx-text-fill: #e74c3c; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 10; " +
                            "-fx-background-color: #fadbd8; " +
                            "-fx-background-radius: 5;"
            );
        } else {
            messageLabel.setStyle(
                    "-fx-text-fill: #27ae60; " +
                            "-fx-font-size: 13px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-padding: 10; " +
                            "-fx-background-color: #d4edda; " +
                            "-fx-background-radius: 5;"
            );
        }
    }

}