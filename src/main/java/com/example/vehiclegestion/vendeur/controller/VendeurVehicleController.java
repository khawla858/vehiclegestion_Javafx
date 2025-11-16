package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.ArticleDAO;
import com.example.vehiclegestion.vendeur.model.Article;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.sql.SQLException;
import java.util.List;

public class VendeurVehicleController {

    @FXML
    private VBox cardsContainer;

    private ArticleDAO articleDAO = new ArticleDAO();
    private final int VENDEUR_ID = 1; // ID du vendeur connecté

    @FXML
    public void initialize() {
        System.out.println("🚀 Initialisation de la page véhicules...");
        loadVehicles();
    }

    @FXML
    private void addVehicle() {
        try {
            System.out.println("➕ Ouverture du formulaire d'ajout...");

            // Charger le formulaire FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/AddVehicleForm.fxml"));
            VBox form = loader.load();

            // Configurer la fenêtre modale
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Ajouter un véhicule");
            dialogStage.initModality(Modality.APPLICATION_MODAL);

            Scene scene = new Scene(form);
            dialogStage.setScene(scene);

            // Passer la référence de la fenêtre au contrôleur
            AddVehicleFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setVendeurId(VENDEUR_ID);

            // Fermeture de la fenêtre = recharger les véhicules
            dialogStage.setOnHidden(e -> {
                System.out.println("🔄 Rechargement après ajout...");
                loadVehicles();
            });

            dialogStage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'ouverture du formulaire: " + e.getMessage());
        }
    }

    private void loadVehicles() {
        cardsContainer.getChildren().clear();
        try {
            List<Article> articles = articleDAO.getArticlesByVendeur(VENDEUR_ID);

            System.out.println("🚗 Chargement de " + articles.size() + " véhicules depuis la base de données");

            if (articles.isEmpty()) {
                showEmptyState();
                return;
            }

            // Créer un GridPane pour la disposition en grille CENTRÉE
            GridPane gridPane = new GridPane();
            gridPane.setAlignment(Pos.CENTER);
            gridPane.setHgap(20);
            gridPane.setVgap(20);
            gridPane.setStyle("-fx-padding: 20;");

            int columns = 3; // 3 colonnes
            int row = 0;
            int col = 0;

            for (int i = 0; i < articles.size(); i++) {
                Article article = articles.get(i);
                VBox vehicleCard = createVehicleCard(article, i);

                // Ajouter la carte à la grille
                gridPane.add(vehicleCard, col, row);

                // Passer à la colonne suivante
                col++;

                // Si on atteint le nombre maximum de colonnes, passer à la ligne suivante
                if (col >= columns) {
                    col = 0;
                    row++;
                }
            }

            // Ajouter directement la grille au conteneur
            cardsContainer.getChildren().add(gridPane);
            cardsContainer.setAlignment(Pos.CENTER);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement des véhicules: " + e.getMessage());
        }
    }

    private VBox createVehicleCard(Article article, int index) {
        VBox card = new VBox();
        card.setPrefSize(278, 350);
        card.setStyle("-fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3); " +
                "-fx-border-color: #e0e0e0; -fx-border-radius: 15; -fx-border-width: 1;");

        VBox contentBox = new VBox(15);
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle("-fx-alignment: center;");

        // Image du véhicule
        ImageView vehicleImage = createVehicleImage(article, index);
        vehicleImage.setFitWidth(240);
        vehicleImage.setFitHeight(160);
        vehicleImage.setPreserveRatio(true);

        // Informations du véhicule
        VBox infoBox = new VBox(8);
        infoBox.setStyle("-fx-alignment: center-left;");

        Label titleLabel = new Label(article.getTitre());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(240);

        Label priceLabel = new Label(String.format("%,.0f MAD", article.getPrix()));
        priceLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Détails (catégorie + état)
        HBox detailBox = new HBox(10);
        Label categoryLabel = new Label("📦 " + article.getCategorie());
        categoryLabel.setStyle("-fx-text-fill: #3498db; -fx-font-size: 12px;");

        Label stateLabel = new Label(getStateIcon(article.getEtat()) + " " + article.getEtat());
        stateLabel.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 12px;");

        detailBox.getChildren().addAll(categoryLabel, stateLabel);

        // Description
        Label descLabel = new Label(article.getDescription());
        descLabel.setWrapText(true);
        descLabel.setPrefWidth(240);
        descLabel.setMaxHeight(40);
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        // Boutons d'action
        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");

        Button editBtn = new Button("✏️ Modifier");
        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-padding: 8 15; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editVehicle(article));

        Button deleteBtn = new Button("🗑️ Supprimer");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-padding: 8 15; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteVehicle(article));

        Button detailBtn = new Button("🔍 Détails");
        detailBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-size: 12px; " +
                "-fx-padding: 8 15; -fx-background-radius: 6;");
        detailBtn.setOnAction(e -> detailVehicle(article));

        buttonBox.getChildren().addAll(editBtn, deleteBtn, detailBtn);
        // Assemblage de la carte
        infoBox.getChildren().addAll(titleLabel, priceLabel, detailBox, descLabel);
        contentBox.getChildren().addAll(vehicleImage, infoBox, buttonBox);
        card.getChildren().add(contentBox);

        return card;
    }

    private ImageView createVehicleImage(Article article, int index) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(240);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-background-radius: 10;");

        String imageUrl = article.getImage();

        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            try {
                System.out.println("🖼️ Chargement image: " + imageUrl);

                Image image = new Image(imageUrl, true);

                // Gérer les erreurs de chargement
                image.errorProperty().addListener((obs, wasError, isError) -> {
                    if (isError) {
                        System.err.println("❌ Erreur image: " + imageUrl);
                        setFallbackStyle(imageView);
                    }
                });

                imageView.setImage(image);

            } catch (Exception e) {
                System.err.println("❌ Erreur chargement image: " + e.getMessage());
                setFallbackStyle(imageView);
            }
        } else {
            setFallbackStyle(imageView);
        }

        return imageView;
    }

    private void setFallbackStyle(ImageView imageView) {
        imageView.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                "-fx-background-radius: 10; -fx-border-radius: 10;");
    }

    private String getStateIcon(String state) {
        if (state == null) return "🚗";
        switch (state.toLowerCase()) {
            case "neuf": return "🆕";
            case "occasion": return "🔧";
            case "vendu": return "✅";
            default: return "🚗";
        }
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(20);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(60));
        emptyState.setPrefSize(600, 300);

        Label emptyIcon = new Label("🚗");
        emptyIcon.setStyle("-fx-font-size: 48px;");

        Label emptyText = new Label("Aucun véhicule en vente");
        emptyText.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");

        Label emptySubtext = new Label("Commencez par ajouter votre premier véhicule");
        emptySubtext.setStyle("-fx-font-size: 14px; -fx-text-fill: #bdc3c7;");

        emptyState.getChildren().addAll(emptyIcon, emptyText, emptySubtext);
        cardsContainer.getChildren().add(emptyState);
        cardsContainer.setAlignment(Pos.CENTER);
    }

    private void showError(String message) {
        VBox errorState = new VBox(15);
        errorState.setAlignment(Pos.CENTER);
        errorState.setPadding(new Insets(40));

        Label errorIcon = new Label("❌");
        errorIcon.setStyle("-fx-font-size: 36px;");

        Label errorText = new Label("Erreur de chargement");
        errorText.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");

        Label errorDetail = new Label(message);
        errorDetail.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");

        Button retryButton = new Button("🔄 Réessayer");
        retryButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-padding: 10 20; -fx-background-radius: 6;");
        retryButton.setOnAction(e -> loadVehicles());

        errorState.getChildren().addAll(errorIcon, errorText, errorDetail, retryButton);
        cardsContainer.getChildren().add(errorState);
        cardsContainer.setAlignment(Pos.CENTER);
    }

    private void editVehicle(Article article) {
        System.out.println("✏️ Modification du véhicule: " + article.getTitre());
        // TODO: Implémenter la modification
    }

    private void deleteVehicle(Article article) {
        System.out.println("🗑️ Suppression du véhicule: " + article.getTitre());
        // TODO: Implémenter la suppression avec confirmation
    }
    private void detailVehicle(Article article) {
        System.out.println("🔍 Détails du véhicule: " + article.getTitre());
        // TODO: Implémenter l'affichage des détails
    }


}