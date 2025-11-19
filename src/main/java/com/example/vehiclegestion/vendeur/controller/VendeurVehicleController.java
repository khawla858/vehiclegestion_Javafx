package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.ArticleDAO;
import com.example.vehiclegestion.vendeur.model.Article;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VendeurVehicleController {

    @FXML private VBox cardsContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private ComboBox<String> etatCombo;
    @FXML private ComboBox<String> anneeMaxCombo;
    @FXML private Label resultsLabel;

    private ArticleDAO articleDAO = new ArticleDAO();
    private final int VENDEUR_ID = 1;
    private List<Article> allArticles = new ArrayList<>();

    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation de la page véhicules...");
        setupFilters();
        loadVehicles();
    }

    private void setupFilters() {
        // Catégories
        categorieCombo.getItems().addAll("Toutes", "Voiture", "Moto", "Camion", "SUV", "Utilitaire");
        categorieCombo.setValue("Toutes");

        // États
        etatCombo.getItems().addAll("Tous", "Neuf", "Occasion", "Très bon état", "Bon état");
        etatCombo.setValue("Tous");

        // Années
        List<String> years = new ArrayList<>();
        years.add("Toutes");
        for (int year = 2024; year >= 2000; year--) {
            years.add(String.valueOf(year));
        }
        anneeMaxCombo.getItems().addAll(years);
        anneeMaxCombo.setValue("Toutes");

        // Listener pour filtrage en temps réel
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        categorieCombo.setOnAction(e -> applyFilters());
        etatCombo.setOnAction(e -> applyFilters());
        anneeMaxCombo.setOnAction(e -> applyFilters());
    }

    @FXML
    private void applyFilters() {
        List<Article> filtered = new ArrayList<>(allArticles);

        // Recherche par texte
        String searchText = searchField.getText().toLowerCase().trim();
        if (!searchText.isEmpty()) {
            filtered = filtered.stream()
                    .filter(a -> a.getTitre().toLowerCase().contains(searchText) ||
                            (a.getMarque() != null && a.getMarque().toLowerCase().contains(searchText)) ||
                            (a.getModele() != null && a.getModele().toLowerCase().contains(searchText)))
                    .collect(Collectors.toList());
        }

        // Filtre catégorie
        String categorie = categorieCombo.getValue();
        if (categorie != null && !categorie.equals("Toutes")) {
            filtered = filtered.stream()
                    .filter(a -> a.getCategorie() != null && a.getCategorie().equalsIgnoreCase(categorie))
                    .collect(Collectors.toList());
        }

        // Filtre état
        String etat = etatCombo.getValue();
        if (etat != null && !etat.equals("Tous")) {
            filtered = filtered.stream()
                    .filter(a -> a.getEtat() != null && a.getEtat().equalsIgnoreCase(etat))
                    .collect(Collectors.toList());
        }

        // Filtre année max
        String anneeMax = anneeMaxCombo.getValue();
        if (anneeMax != null && !anneeMax.equals("Toutes")) {
            int maxYear = Integer.parseInt(anneeMax);
            filtered = filtered.stream()
                    .filter(a -> a.getAnnee() <= maxYear)
                    .collect(Collectors.toList());
        }

        displayVehicles(filtered);
        updateResultsLabel(filtered.size());
    }

    @FXML
    private void resetFilters() {
        searchField.clear();
        categorieCombo.setValue("Toutes");
        etatCombo.setValue("Tous");
        anneeMaxCombo.setValue("Toutes");
        displayVehicles(allArticles);
        updateResultsLabel(allArticles.size());
    }

    private void updateResultsLabel(int count) {
        resultsLabel.setText(count + " véhicule(s) trouvé(s)");
    }

    @FXML
    private void addVehicle() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/AddVehicleForm.fxml"));
            VBox form = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter un véhicule");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(form);
            dialogStage.setScene(scene);

            AddVehicleFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setVendeurId(VENDEUR_ID);

            dialogStage.setOnHidden(e -> loadVehicles());
            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire: " + e.getMessage());
        }
    }

    private void loadVehicles() {
        try {
            allArticles = articleDAO.getArticlesByVendeur(VENDEUR_ID);
            displayVehicles(allArticles);
            updateResultsLabel(allArticles.size());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement des véhicules: " + e.getMessage());
        }
    }

    private void displayVehicles(List<Article> articles) {
        cardsContainer.getChildren().clear();

        if (articles.isEmpty()) {
            showEmptyState();
            return;
        }

        // FlowPane pour un affichage responsive qui remplit toute la largeur
        FlowPane flowPane = new FlowPane();
        flowPane.setHgap(20);
        flowPane.setVgap(20);
        flowPane.setStyle("-fx-padding: 20;");
        flowPane.setAlignment(Pos.TOP_LEFT);
        flowPane.setPrefWrapLength(0); // Permet le wrap automatique

        for (Article article : articles) {
            VBox card = createVehicleCard(article);
            flowPane.getChildren().add(card);
        }

        cardsContainer.getChildren().add(flowPane);
        cardsContainer.setAlignment(Pos.TOP_CENTER);
    }

    private VBox createVehicleCard(Article article) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; -fx-border-color: #e8e8e8; -fx-border-radius: 8; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setPrefWidth(280);
        card.setMaxWidth(280);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> showVehicleDetails(article));

        // IMAGE
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8;");
        imageContainer.setPrefHeight(180);
        imageContainer.setMaxHeight(180);
        loadVehicleImage(article, imageContainer);

        // CONTENU
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 15;");

        HBox locationBox = new HBox(5);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        Label location = new Label(getRandomCity());
        location.setStyle("-fx-text-fill: #666; -fx-font-size: 11;");
        locationBox.getChildren().addAll(locationIcon, location);

        Label title = new Label(article.getTitre());
        title.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-text-fill: #333;");
        title.setWrapText(true);
        title.setMaxWidth(250);

        HBox specs = new HBox(15);
        specs.setAlignment(Pos.CENTER_LEFT);
        Label year = new Label("📅 " + article.getAnnee());
        year.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        Label transmission = new Label("⚙ " + (article.getTransmission() != null ? article.getTransmission() : "N/A"));
        transmission.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        Label fuel = new Label("⛽ " + (article.getCarburant() != null ? article.getCarburant() : "N/A"));
        fuel.setStyle("-fx-text-fill: #666; -fx-font-size: 12;");
        specs.getChildren().addAll(year, transmission, fuel);

        content.getChildren().addAll(locationBox, title, specs);

        // FOOTER
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setStyle("-fx-padding: 15 15 12 15; -fx-border-color: #f0f0f0; -fx-border-width: 1 0 0 0;");

        VBox priceBox = new VBox(2);
        Label price = new Label(String.format("%,.0f DH", article.getPrix()));
        price.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #0066FF;");
        double monthlyPrice = article.getPrix() / 48;
        Label pricePerMonth = new Label("~" + String.format("%,.0f DH / mois", monthlyPrice));
        pricePerMonth.setStyle("-fx-text-fill: #999; -fx-font-size: 11;");
        priceBox.getChildren().addAll(price, pricePerMonth);

        Region priceSpacer = new Region();
        HBox.setHgrow(priceSpacer, Priority.ALWAYS);

        Button favoriteBtn = new Button("♡");
        favoriteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18; -fx-cursor: hand;");

        Button editBtn = new Button("✏️");
        editBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-cursor: hand;");
        editBtn.setOnAction(e -> {
            e.consume();
            editVehicle(article);
        });

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            e.consume();
            deleteVehicle(article);
        });

        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.getChildren().addAll(favoriteBtn, editBtn, deleteBtn);

        applyHoverEffect(editBtn);
        applyHoverEffect(deleteBtn);

        footer.getChildren().addAll(priceBox, priceSpacer, actionsBox);

        card.getChildren().addAll(imageContainer, content, footer);
        setupCardHoverEffects(card);

        return card;
    }

    private void showVehicleDetails(Article article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/VehicleDetail.fxml"));
            BorderPane detailView = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Détails - " + article.getTitre());
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(detailView, 1100, 700);
            dialogStage.setScene(scene);

            VehicleDetailController controller = loader.getController();
            controller.setArticle(article);
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture des détails: " + e.getMessage());
        }
    }

    private void loadVehicleImage(Article article, StackPane container) {
        container.getChildren().clear();
        if (article.getImage() != null && !article.getImage().isEmpty()) {
            try {
                File file = new File(article.getImage());
                Image image = new Image(file.toURI().toString(), true);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(280);
                imageView.setFitHeight(180);
                imageView.setPreserveRatio(true);
                container.getChildren().add(imageView);
            } catch (Exception e) {
                setFallbackStyle(container);
            }
        } else {
            setFallbackStyle(container);
        }
    }

    private void setFallbackStyle(StackPane container) {
        container.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); -fx-background-radius: 8;");
        Label placeholder = new Label("🚗");
        placeholder.setStyle("-fx-font-size: 48px;");
        container.getChildren().add(placeholder);
    }

    private String getRandomCity() {
        String[] cities = {"Tanger", "Casablanca", "Marrakech", "Rabat", "Fès"};
        return cities[(int)(Math.random() * cities.length)];
    }

    private void setupCardHoverEffects(VBox card) {
        String baseStyle = "-fx-background-color: white; -fx-border-color: #e8e8e8; -fx-border-radius: 8; -fx-background-radius: 8;";
        card.setOnMouseEntered(e -> card.setStyle(baseStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 12, 0, 0, 4);"));
        card.setOnMouseExited(e -> card.setStyle(baseStyle + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"));
    }

    private void applyHoverEffect(Button btn) {
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #e8f1ff; -fx-font-size: 16; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-font-size: 16; -fx-cursor: hand;"));
    }

    private void showEmptyState() {
        VBox empty = new VBox(15);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(60));
        Label icon = new Label("🚗");
        icon.setStyle("-fx-font-size: 48px;");
        Label text = new Label("Aucun véhicule trouvé");
        text.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");
        Label subtext = new Label("Essayez de modifier vos filtres");
        subtext.setStyle("-fx-font-size: 14px; -fx-text-fill: #95a5a6;");
        empty.getChildren().addAll(icon, text, subtext);
        cardsContainer.getChildren().add(empty);
    }

    private void showError(String message) {
        VBox errorBox = new VBox(10);
        errorBox.setAlignment(Pos.CENTER);
        Label icon = new Label("❌");
        icon.setStyle("-fx-font-size: 36px;");
        Label msg = new Label(message);
        msg.setStyle("-fx-text-fill: #e74c3c;");
        errorBox.getChildren().addAll(icon, msg);
        cardsContainer.getChildren().add(errorBox);
    }

    private void editVehicle(Article article) {
        System.out.println("✏️ Modifier: " + article.getTitre());
        // TODO: Implémenter la modification
    }

    private void deleteVehicle(Article article) {
        System.out.println("🗑️ Supprimer: " + article.getTitre());
        try {
            boolean ok = articleDAO.deleteArticle(article.getId());
            if (ok) {
                System.out.println("✅ Article supprimé avec succès");
                loadVehicles();
            } else {
                showError("Impossible de supprimer l'article");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Erreur lors de la suppression: " + ex.getMessage());
        }
    }
}