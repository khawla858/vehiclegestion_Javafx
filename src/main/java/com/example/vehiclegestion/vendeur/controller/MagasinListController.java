package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.MagasinDAO;
import com.example.vehiclegestion.vendeur.dao.DemandeMagasinDAO;
import com.example.vehiclegestion.vendeur.model.Magasin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import org.kordamp.ikonli.javafx.FontIcon;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

// Nouveaux imports
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;

public class MagasinListController {
    @FXML
    private VBox magasinContainer;
    @FXML
    private Button btnAjouterMagasin;

    @FXML
    public void initialize() {
        chargerMagasins();
        // Supprimer l'appel à chargerLogoMagasin() sans paramètres
        btnAjouterMagasin.setOnAction(e -> ouvrirAjouterMagasin());


        // Effet hover sur le bouton principal
        btnAjouterMagasin.setOnMouseEntered(e -> btnAjouterMagasin.setStyle("-fx-font-size: 15px; -fx-background-color: #e02849; -fx-text-fill: white; " +
                "-fx-padding: 12 25; -fx-background-radius: 10; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(gaussian, rgba(255,45,85,0.4), 12, 0, 0, 4); -fx-cursor: hand;")
        );
        btnAjouterMagasin.setOnMouseExited(e -> btnAjouterMagasin.setStyle("-fx-font-size: 15px; -fx-background-color: #ff2d55; -fx-text-fill: white; " +
                "-fx-padding: 12 25; -fx-background-radius: 10; -fx-font-weight: bold; " +
                "-fx-effect: dropshadow(gaussian, rgba(255,45,85,0.3), 10, 0, 0, 3); -fx-cursor: hand;")
        );
    }

    private void ouvrirAjouterMagasin() {
        MagasinDAO magasinDAO = new MagasinDAO();
        DemandeMagasinDAO demandeDAO = new DemandeMagasinDAO();
        int vendeurId = 1;

        if (!magasinDAO.hasMagasin(vendeurId)) {
            if (demandeDAO.hasDemandePending(vendeurId)) {
                showStyledAlert(Alert.AlertType.INFORMATION, "Demande en attente", "Vous avez déjà une demande de création de magasin en attente.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Première création de magasin");
            alert.setHeaderText("Vous ne pouvez créer qu'un seul magasin !");
            alert.setContentText("Voulez-vous envoyer une demande à l'admin pour créer votre premier magasin ?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                ouvrirFormulaireDemandeMagasin(vendeurId);
            }
        } else {
            try {
                Parent form = FXMLLoader.load(getClass().getResource("/view/vendeur/AjouterMagasin.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Ajouter un Magasin");
                stage.setScene(new Scene(form));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void ouvrirFormulaireDemandeMagasin(int vendeurId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/AjouterDemandeMagasin.fxml"));
            Parent root = loader.load();
            AjouterDemandeMagasinController controller = loader.getController();
            controller.setVendeurId(vendeurId);
            Stage stage = new Stage();
            stage.setTitle("Envoyer une demande de création de magasin");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void chargerMagasins() {
        magasinContainer.getChildren().clear();
        MagasinDAO dao = new MagasinDAO();
        List<Magasin> magasins = dao.getAllMagasins();

        if (magasins.isEmpty()) {
            showEmptyState();
            return;
        }

        for (Magasin m : magasins) {
            HBox card = createMagasinCard(m, dao);
            magasinContainer.getChildren().add(card);
        }
    }

    private HBox createMagasinCard(Magasin magasin, MagasinDAO dao) {
        HBox card = new HBox(0);
        card.setStyle("-fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3); " +
                "-fx-padding: 0;");
        card.setPrefHeight(220);
        card.setMaxHeight(220);

        // ========== PARTIE GAUCHE : IMAGE ==========
        StackPane imagePane = new StackPane();
        imagePane.setPrefWidth(280);
        imagePane.setMaxWidth(280);
        imagePane.setStyle("" +
                "-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        // ✅ CHANGEMENT : ImageView pour le logo au lieu de l'icône
        ImageView logoView = new ImageView();
        logoView.setFitWidth(260);
        logoView.setFitHeight(260);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);

        // Charger le logo du magasin
        chargerLogoMagasin(magasin, logoView);

        StackPane.setAlignment(logoView, Pos.CENTER);

        // Bouton J'aime (coeur) en haut à droite
        Button likeBtn = new Button("🤍");
        likeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 50%; " +
                "-fx-font-size: 20px; -fx-padding: 8; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");
        likeBtn.setOnAction(e -> {
            e.consume();
            if (likeBtn.getText().equals("🤍")) {
                likeBtn.setText("❤️");
                showStyledAlert(Alert.AlertType.INFORMATION, "Favori ajouté", magasin.getNomMagasin() + " a été ajouté à vos favoris !");
            } else {
                likeBtn.setText("🤍");
            }
        });
        StackPane.setAlignment(likeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(likeBtn, new Insets(15));

        // Indicateurs de position (comme sur TripAdvisor)
        HBox dotsIndicator = new HBox(6);
        dotsIndicator.setAlignment(Pos.CENTER);
        for (int i = 0; i < 5; i++) {
            Label dot = new Label("•");
            dot.setStyle("-fx-text-fill: " + (i == 0 ? "white" : "rgba(255,255,255,0.5)") + "; " +
                    "-fx-font-size: 10px;");
            dotsIndicator.getChildren().add(dot);
        }
        StackPane.setAlignment(dotsIndicator, Pos.BOTTOM_CENTER);
        StackPane.setMargin(dotsIndicator, new Insets(0, 0, 15, 0));

        // ✅ REMPLACER bigIcon par logoView
        imagePane.getChildren().addAll(logoView, likeBtn, dotsIndicator);

        // ========== PARTIE DROITE : INFORMATIONS ==========
        VBox infoPane = new VBox(12);
        infoPane.setPadding(new Insets(20, 20, 20, 25));
        HBox.setHgrow(infoPane, Priority.ALWAYS);

        // Numéro et Nom du magasin
        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label numero = new Label("1.");
        numero.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #95a5a6;");
        Label nomLabel = new Label(magasin.getNomMagasin());
        nomLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleBox.getChildren().addAll(numero, nomLabel);

        // Note avec étoiles
        HBox ratingBox = new HBox(8);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label rating = new Label("4.1");
        rating.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label stars = new Label("★★★★☆");
        stars.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700;");
        Label reviewCount = new Label("(2,443)");
        reviewCount.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        ratingBox.getChildren().addAll(rating, stars, reviewCount);

        // Catégorie
        Label categoryLabel = new Label("Marchés aux puces et marchés de rue");
        categoryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        // Statut
        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Ouvert");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        statusBox.getChildren().add(statusLabel);

        // Description avec auteur
        HBox descBox = new HBox(10);
        descBox.setAlignment(Pos.TOP_LEFT);
        Label authorIcon = new Label("👤");
        authorIcon.setStyle("-fx-font-size: 16px;");
        VBox textBox = new VBox(4);
        Label authorLabel = new Label("Par Alxmyma");
        authorLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #7f8c8d;");
        String description = magasin.getDescription();
        if (description != null && description.length() > 150) {
            description = description.substring(0, 150) + "...";
        }
        Label descLabel = new Label(description != null ? description : "Découvrez ce magasin exceptionnel...");
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50; -fx-line-spacing: 2px;");
        textBox.getChildren().addAll(authorLabel, descLabel);
        descBox.getChildren().addAll(authorIcon, textBox);

        // Spacer pour pousser les boutons en bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Boutons d'action en bas
        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER_LEFT);
        Button voirBilletsBtn = new Button("Voir les vhécules");
        voirBilletsBtn.setStyle("-fx-background-color: #00aa6c; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 20; " +
                "-fx-background-radius: 20; -fx-cursor: hand;");
        voirBilletsBtn.setOnAction(e -> {
            e.consume();
            System.out.println("Voir articles pour : " + magasin.getNomMagasin());
        });

        Button voirVisitesBtn = new Button("Voir les visites");
        voirVisitesBtn.setStyle("-fx-background-color: #00aa6c; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 20; " +
                "-fx-background-radius: 20; -fx-cursor: hand;");
        voirVisitesBtn.setOnAction(e -> {
            e.consume();
            ouvrirDetailMagasin(magasin);
        });
        int nbComments = magasin.getNbCommentaires(); // Supposons que cette méthode existe
        String commentText = formatCommentCount(nbComments);
        Label commentBadge = new Label(commentText);
        commentBadge.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8; " +
                "-fx-background-radius: 10;");

        actionBox.getChildren().addAll(voirBilletsBtn, voirVisitesBtn, commentBadge);

        // Boutons d'édition/suppression en haut à droite
        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);
        HBox editDeleteBox = new HBox(8);
        editDeleteBox.setAlignment(Pos.TOP_RIGHT);

        Button editBtn = createSmallActionButton("✏️", "#3498db");
        editBtn.setOnAction(e -> {
            e.consume();
            System.out.println("Modifier magasin : " + magasin.getNomMagasin());
        });

        Button deleteBtn = createSmallActionButton("🗑️", "#e74c3c");
        deleteBtn.setOnAction(e -> {
            e.consume();
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer la suppression");
            confirm.setHeaderText("Supprimer le magasin ?");
            confirm.setContentText("Cette action est irréversible.");
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                dao.deleteMagasin(magasin.getIdMagasin());
                chargerMagasins();
            }
        });

        Button locBtn = createSmallActionButton("📍", "#9b59b6");
        locBtn.setOnAction(e -> {
            e.consume();
            ouvrirLocalisation(magasin.getLocalisation());
        });

        editDeleteBox.getChildren().addAll(editBtn, deleteBtn, locBtn);

        // Assemblage de la partie info
        infoPane.getChildren().addAll(
                titleBox,
                ratingBox,
                categoryLabel,
                statusBox,
                descBox,
                spacer,
                actionBox
        );

        // Ajout des boutons edit/delete en overlay
        HBox topRightOverlay = new HBox();
        topRightOverlay.setAlignment(Pos.TOP_RIGHT);
        topRightOverlay.setPadding(new Insets(10, 10, 0, 0));
        topRightOverlay.getChildren().add(editDeleteBox);

        StackPane.setAlignment(topRightOverlay, Pos.TOP_RIGHT);

        StackPane rightStack = new StackPane();
        rightStack.getChildren().addAll(infoPane, topRightOverlay);
        HBox.setHgrow(rightStack, Priority.ALWAYS);

        // ========== ASSEMBLAGE FINAL ==========
        card.getChildren().addAll(imagePane, rightStack);
        setupCardHoverEffect(card);

        return card;
    }

    private Button createSmallActionButton(String emoji, String color) {
        Button btn = new Button(emoji);
        btn.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 14px; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 3, 0, 0, 1);");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 14px; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 4, 0, 0, 2);")
        );
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 14px; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 3, 0, 0, 1);")
        );
        return btn;
    }

    private Button createFooterButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-padding: 8 15; -fx-background-radius: 8;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + color + "20; -fx-text-fill: " + color + "; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-padding: 8 15; -fx-background-radius: 8;")
        );
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + color + "; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-padding: 8 15; -fx-background-radius: 8;")
        );
        return btn;
    }

    private void setupCardHoverEffect(HBox card) {
        String baseStyle = "-fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3); " +
                "-fx-padding: 0;";
        String hoverStyle = "-fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5); " +
                "-fx-padding: 0; -fx-scale-y: 1.01; -fx-scale-x: 1.01;";

        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));
    }

    private void showEmptyState() {
        VBox empty = new VBox(20);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(80));
        empty.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3);");

        Label icon = new Label("🏪");
        icon.setStyle("-fx-font-size: 72px;");

        Label title = new Label("Aucun magasin pour le moment");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Créez votre premier magasin pour commencer à vendre");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Button createBtn = new Button("➕ Créer mon premier magasin");
        createBtn.setStyle("-fx-background-color: #ff2d55; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 12 25; -fx-background-radius: 10; -fx-cursor: hand;");
        createBtn.setOnAction(e -> ouvrirAjouterMagasin());

        empty.getChildren().addAll(icon, title, subtitle, createBtn);
        magasinContainer.getChildren().add(empty);
    }

    private void ouvrirDetailMagasin(Magasin magasin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/MagasinDetails.fxml"));
            Parent root = loader.load();
            MagasinDetailsController controller = loader.getController();
            controller.setMagasin(magasin);
            Stage stage = new Stage();
            stage.setTitle("Détails du magasin");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void ouvrirLocalisation(String localisation) {
        if (localisation == null || localisation.isEmpty()) {
            showStyledAlert(Alert.AlertType.WARNING, "Localisation manquante", "Aucune localisation n'est définie pour ce magasin.");
            return;
        }

        try {
            String url = "https://www.google.com/maps/search/?api=1&query=" + localisation.replace(" ", "+");
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showStyledAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String formatCommentCount(int count) {
        if (count < 1000) {
            return count + " commentaires";
        } else {
            return String.format("%.1fk commentaires", count / 1000.0);
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Charge le logo du magasin depuis le dossier projet/images/logos
     */
    private void chargerLogoMagasin(Magasin magasin, ImageView logoView) {
        try {
            String logoPath = magasin.getLogoMagasin();

            if (logoPath != null && !logoPath.isEmpty()) {
                // Construire le chemin complet vers le dossier images/logos
                File projetDir = new File(System.getProperty("user.dir")); // Répertoire du projet
                File logosDir = new File(projetDir, "images/logos");

                File logoFile;

                // Si le chemin contient déjà le dossier images/logos
                if (logoPath.contains("images/logos")) {
                    logoFile = new File(projetDir, logoPath);
                } else {
                    // Sinon, chercher directement dans images/logos
                    logoFile = new File(logosDir, logoPath);
                }

                System.out.println("🔍 Recherche du logo: " + logoFile.getAbsolutePath());

                if (logoFile.exists()) {
                    Image logo = new Image(logoFile.toURI().toString());
                    logoView.setImage(logo);
                    System.out.println("✅ Logo chargé: " + logoFile.getName());
                } else {
                    System.err.println("❌ Logo introuvable: " + logoFile.getAbsolutePath());
                    chargerLogoParDefaut(logoView);
                }
            } else {
                // Aucun logo défini dans la base de données
                System.out.println("ℹ️ Aucun logo défini pour: " + magasin.getNomMagasin());
                chargerLogoParDefaut(logoView);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement logo: " + e.getMessage());
            chargerLogoParDefaut(logoView);
        }
    }

    /**
     * ✅ Charge un logo par défaut depuis le dossier images/logos
     */
    private void chargerLogoParDefaut(ImageView logoView) {
        try {
            File projetDir = new File(System.getProperty("user.dir"));
            File defaultLogoFile = new File(projetDir, "images/logos/default-store.png");

            if (defaultLogoFile.exists()) {
                Image defaultLogo = new Image(defaultLogoFile.toURI().toString());
                logoView.setImage(defaultLogo);
                System.out.println("✅ Logo par défaut chargé");
            } else {
                // Fallback: utiliser une icône
                System.out.println("⚠️ Logo par défaut introuvable, utilisation d'une icône");
                creerPlaceholderLogo(logoView);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement logo par défaut: " + e.getMessage());
            creerPlaceholderLogo(logoView);
        }
    }

    /**
     * ✅ Crée un placeholder élégant si aucun logo n'est disponible
     */
    private void creerPlaceholderLogo(ImageView logoView) {
        try {
            // Créer un Canvas pour générer une image placeholder
            javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(160, 160);
            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

            // Fond circulaire
            gc.setFill(javafx.scene.paint.Color.web("#667eea"));
            gc.fillOval(20, 20, 120, 120);

            // Icône magasin
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 48));
            gc.fillText("🏪", 60, 90);

            // Convertir en Image
            javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
            params.setFill(javafx.scene.paint.Color.TRANSPARENT);
            javafx.scene.image.WritableImage image = canvas.snapshot(params, null);

            logoView.setImage(image);
        } catch (Exception e) {
            // Dernier recours: Label avec emoji
            System.err.println("❌ Impossible de créer le placeholder: " + e.getMessage());
        }
    }
}