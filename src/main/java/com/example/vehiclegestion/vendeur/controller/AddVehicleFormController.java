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

    private ArticleDAO articleDAO;
    private Stage dialogStage;
    private int vendeurId;
    private String selectedImagePath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            articleDAO = new ArticleDAO();
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'initialisation de ArticleDAO: " + e.getMessage());
            e.printStackTrace();
            // Optionnel : lancer une exception runtime si critique
            // throw new RuntimeException("Impossible de se connecter à la base de données", e);
        }
        setupForm();
    }

    private void setupForm() {
        categorieComboBox.getItems().addAll(
                "Berline", "SUV", "Compacte", "Citadine", "Sportive",
                "Utilitaire", "4x4", "Luxe", "Cabriolet", "Break"
        );

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
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif","*.webp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(dialogStage);
        if (selectedFile != null) {

            // ⇩⇩⇩ CORRECTION 1 : garder chemin ABSOLU avant conversion ⇩⇩⇩
            selectedImagePath = selectedFile.getPath();

            imageField.setText(selectedFile.getName());

            try {
                Image image = new Image(selectedFile.toURI().toString());
                imagePreview.setImage(image);
                imagePreviewContainer.setManaged(true);
                imagePreviewContainer.setVisible(true);
            } catch (Exception e) {
                showMessage("Erreur lors du chargement de l'image: " + e.getMessage(), true);
            }
        }
    }

    @FXML
    private void addVehicle() {
        if (validateForm()) {
            try {
                Article article = new Article();
                article.setTitre(titreField.getText().trim());
                article.setDescription(descriptionField.getText().trim());
                article.setPrix(Double.parseDouble(prixField.getText()));
                article.setCategorie(categorieComboBox.getValue());
                article.setEtat(etatComboBox.getValue());

                // ⇩⇩⇩ CORRECTION 2 : convertir chemin Windows en URL JavaFX ⇩⇩⇩
                String imageUrl = "";
                if (selectedImagePath != null) {
                    File f = new File(selectedImagePath);
                    imageUrl = f.toURI().toString();
                }
                article.setImage(imageUrl);

                boolean success = articleDAO.addArticle(article, vendeurId);

                if (success) {
                    showMessage("✅ Véhicule ajouté avec succès! L'image a été sauvegardée.", false);
                    clearForm();

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
                            },
                            2000
                    );
                } else {
                    showMessage("❌ Erreur lors de l'ajout du véhicule", true);
                }

            } catch (SQLException e) {
                showMessage("❌ Erreur base de données: " + e.getMessage(), true);
                e.printStackTrace();
            } catch (NumberFormatException e) {
                showMessage("❌ Format de prix invalide", true);
            }
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
