package com.example.vehiclegestion.vendeur.controller;
import javafx.scene.control.Alert;
import com.example.vehiclegestion.vendeur.model.Article;
import com.example.vehiclegestion.utils.DataReceiver;
import com.example.vehiclegestion.utils.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VehicleDetailController implements DataReceiver {

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private HBox mainContainer;

    @FXML
    private VBox leftSection;

    @FXML
    private ImageView mainImageView;

    @FXML
    private Label titleLabel;

    @FXML
    private Label priceLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private VBox characteristicsContainer;

    @FXML
    private Label descriptionLabel;

    @FXML
    private VBox sellerSection;

    @FXML
    private Button contactBtn;

    @FXML
    private Button backBtn;

    private Article article;
    private NavigationManager nav = NavigationManager.getInstance();

    /**
     * ✅ Implémentation de DataReceiver pour recevoir l'article
     */
    @Override
    public void receiveData(Object data) {
        if (data instanceof Article) {
            this.article = (Article) data;
            System.out.println("✅ Article reçu dans VehicleDetailController: " + article.getTitre());
            displayArticleDetails();
        } else {
            System.err.println("❌ Données reçues non valides dans VehicleDetailController");
            showError("Erreur", "Impossible de charger les données du véhicule");
        }
    }

    @FXML
    private void initialize() {
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");

        // ✅ Configuration du bouton retour
        if (backBtn != null) {
            backBtn.setOnAction(e -> nav.goBack());
            backBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                    "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        }

        // ✅ Configuration du bouton contact
        if (contactBtn != null) {
            contactBtn.setOnAction(e -> handleContact());
            contactBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                    "-fx-padding: 12 30; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        }
    }

    private void displayArticleDetails() {
        // Image principale
        loadMainImage();

        // Titre
        titleLabel.setText(article.getTitre());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Prix
        priceLabel.setText(String.format("%,.0f DH", article.getPrix()));
        priceLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #0066FF;");

        // Localisation
        locationLabel.setText("📍 " + getRandomCity());
        locationLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

        // Date de publication
        dateLabel.setText("Publiée le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 12px;");

        // Caractéristiques
        displayCharacteristics();

        // Description
        displayDescription();

        // Section vendeur
        displaySellerSection();
    }

    private void loadMainImage() {
        if (article.getImage() != null && !article.getImage().isEmpty()) {
            try {
                File file = new File(article.getImage());
                Image image = new Image(file.toURI().toString(), true);
                mainImageView.setImage(image);
                mainImageView.setFitWidth(650);
                mainImageView.setFitHeight(450);
                mainImageView.setPreserveRatio(true);
                mainImageView.setStyle("-fx-background-radius: 8;");
            } catch (Exception e) {
                setFallbackImage();
            }
        } else {
            setFallbackImage();
        }
    }

    private void setFallbackImage() {
        // Image par défaut si pas d'image
        StackPane placeholder = new StackPane();
        placeholder.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                "-fx-background-radius: 8;");
        placeholder.setPrefSize(650, 450);
        Label icon = new Label("🚗");
        icon.setStyle("-fx-font-size: 80px;");
        placeholder.getChildren().add(icon);

        // Remplacer l'imageView par le placeholder
        if (mainImageView.getParent() instanceof Pane) {
            Pane parent = (Pane) mainImageView.getParent();
            parent.getChildren().remove(mainImageView);
            parent.getChildren().add(placeholder);
        }
    }

    private void displayCharacteristics() {
        characteristicsContainer.getChildren().clear();
        characteristicsContainer.setStyle("-fx-spacing: 12; -fx-padding: 20; -fx-background-color: white; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label sectionTitle = new Label("Caractéristiques");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        characteristicsContainer.getChildren().add(sectionTitle);

        // Année
        addCharacteristic("📅 Année-Modèle", String.valueOf(article.getAnnee()));

        // Kilométrage
        addCharacteristic("📏 Kilométrage", String.valueOf(article.getKilometrage()) + " km");

        // Boîte de vitesses
        if (article.getTransmission() != null) {
            addCharacteristic("⚙️ Boîte de vitesses", article.getTransmission());
        }

        // Carburant
        if (article.getCarburant() != null) {
            addCharacteristic("⛽ Type de carburant", article.getCarburant());
        }

        // Marque
        if (article.getMarque() != null) {
            addCharacteristic("🚗 Marque", article.getMarque());
        }

        // Modèle
        if (article.getModele() != null) {
            addCharacteristic("🏷️ Modèle", article.getModele());
        }

        // Puissance
        if (article.getPuissance() > 0) {
            addCharacteristic("🔋 Puissance", article.getPuissance() + " ch");
        }

        // Origine
        addCharacteristic("🌍 Origine", "Maroc");

        // État
        if (article.getEtat() != null) {
            addCharacteristic("⭐ État", article.getEtat());
        }

        // Catégorie
        if (article.getCategorie() != null) {
            addCharacteristic("📂 Catégorie", article.getCategorie());
        }
    }

    private void addCharacteristic(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8 0; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

        Label labelField = new Label(label);
        labelField.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
        labelField.setPrefWidth(200);

        Label valueField = new Label(value != null ? value : "Non spécifié");
        valueField.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(labelField, spacer, valueField);
        characteristicsContainer.getChildren().add(row);
    }

    private void displayDescription() {
        descriptionLabel.setText(article.getDescription() != null && !article.getDescription().isEmpty()
                ? article.getDescription()
                : "Véhicule en excellent état, bien entretenu. Toutes les révisions effectuées à temps. " +
                "Véhicule non fumeur. Disponible pour essai routier.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555; -fx-line-spacing: 1.5;");
    }

    private void displaySellerSection() {
        sellerSection.setStyle("-fx-spacing: 15; -fx-padding: 20; -fx-background-color: white; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // Avatar et nom du vendeur
        HBox sellerHeader = new HBox(12);
        sellerHeader.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color: #3498db; -fx-background-radius: 30; " +
                "-fx-min-width: 60; -fx-min-height: 60;");
        Label avatarText = new Label("V");
        avatarText.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 24px;");
        avatar.getChildren().add(avatarText);

        VBox sellerInfo = new VBox(5);
        Label sellerName = new Label("Vendeur Professionnel");
        sellerName.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");
        Label sellerBadge = new Label("⭐ Membre depuis 2020");
        sellerBadge.setStyle("-fx-text-fill: #FF9800; -fx-font-size: 12px;");
        sellerInfo.getChildren().addAll(sellerName, sellerBadge);

        sellerHeader.getChildren().addAll(avatar, sellerInfo);

        // Avertissement
        VBox warningBox = new VBox(8);
        warningBox.setStyle("-fx-background-color: #FFF3E0; -fx-padding: 12; -fx-background-radius: 6;");
        Label warningIcon = new Label("⚠️ Important");
        warningIcon.setStyle("-fx-font-weight: bold; -fx-text-fill: #F57C00; -fx-font-size: 13px;");
        Label warningText = new Label("Il ne faut jamais envoyer d'argent ni d'avance en cas de transfert.");
        warningText.setWrapText(true);
        warningText.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        warningBox.getChildren().addAll(warningIcon, warningText);

        sellerSection.getChildren().clear();
        sellerSection.getChildren().addAll(sellerHeader, warningBox);
    }

    @FXML
    private void handleContact() {
        System.out.println("📞 Contacter le vendeur pour: " + article.getTitre());
        // TODO: Implémenter la logique de contact
        showInfo("Contact", "Fonctionnalité de contact bientôt disponible !");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ✅ Gardez cette méthode pour la compatibilité
    public void setArticle(Article article) {
        this.article = article;
        displayArticleDetails();
    }

    // ✅ Gardez cette méthode pour la compatibilité (si utilisée ailleurs)
    public void setDialogStage(Stage stage) {
        // Ne rien faire - nous utilisons NavigationManager maintenant
    }

    private String getRandomCity() {
        String[] cities = {"Tanger", "Casablanca", "Marrakech", "Rabat", "Fès"};
        return cities[(int)(Math.random() * cities.length)];
    }
}