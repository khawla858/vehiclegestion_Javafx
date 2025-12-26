package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.auth.utils.SessionManager;
import com.example.vehiclegestion.vendeur.dao.MagasinDAO;
import com.example.vehiclegestion.vendeur.dao.DemandeMagasinDAO;
import com.example.vehiclegestion.vendeur.model.Magasin;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import com.example.vehiclegestion.utils.NavigationManager;
import java.util.List;

public class MagasinListController {
    private NavigationManager nav = NavigationManager.getInstance();

    @FXML private FlowPane magasinContainer;
    @FXML private Button btnAjouterMagasin;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label totalMagasinsLabel;

    private SessionManager session = SessionManager.getInstance();
    private int idVendeurConnecte;
    private List<Magasin> allMagasins;

    @FXML
    public void initialize() {
        System.out.println("\n🏪 === Initialisation MagasinListController ===");

        if (!session.estConnecte() || !session.estVendeur()) {
            System.err.println("❌ Accès refusé : utilisateur non connecté ou non vendeur");
            showErrorAlert("Accès refusé", "Connectez-vous en tant que vendeur");
            return;
        }

        idVendeurConnecte = session.getUserId();
        System.out.println("✅ Vendeur connecté - ID: " + idVendeurConnecte);

        setupFilters();
        chargerMagasins();
        setupButtonEffects();

        System.out.println("🏪 === Fin initialisation ===\n");
    }

    private void setupFilters() {
        if (filterComboBox != null) {
            filterComboBox.getItems().addAll(
                    "Tous les magasins",
                    "Actifs uniquement",
                    "Plus récents",
                    "Plus populaires"
            );
            filterComboBox.setValue("Tous les magasins");
            filterComboBox.setOnAction(e -> filterMagasins());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, old, newVal) -> searchMagasins(newVal));
        }
    }

    private void setupButtonEffects() {
        if (btnAjouterMagasin != null) {
            btnAjouterMagasin.setOnAction(e -> ouvrirAjouterMagasin());

            String normalStyle = "-fx-background-color: linear-gradient(to bottom, #3b82f6, #2563eb); " +
                    "-fx-text-fill: white; -fx-padding: 14 28; -fx-background-radius: 10; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.4), 10, 0, 0, 3);";

            String hoverStyle = "-fx-background-color: linear-gradient(to bottom, #2563eb, #1d4ed8); " +
                    "-fx-text-fill: white; -fx-padding: 14 28; -fx-background-radius: 10; " +
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.6), 15, 0, 0, 5);";

            btnAjouterMagasin.setStyle(normalStyle);
            btnAjouterMagasin.setOnMouseEntered(e -> btnAjouterMagasin.setStyle(hoverStyle));
            btnAjouterMagasin.setOnMouseExited(e -> btnAjouterMagasin.setStyle(normalStyle));
        }
    }

    private void filterMagasins() {
        if (allMagasins == null || allMagasins.isEmpty()) return;

        String filter = filterComboBox.getValue();
        List<Magasin> filtered = allMagasins;

        // Appliquer les filtres selon la sélection
        afficherMagasins(filtered);
    }

    private void searchMagasins(String query) {
        if (allMagasins == null || query == null) return;

        if (query.trim().isEmpty()) {
            afficherMagasins(allMagasins);
            return;
        }

        List<Magasin> results = allMagasins.stream()
                .filter(m -> m.getNomMagasin().toLowerCase().contains(query.toLowerCase()) ||
                        (m.getDescription() != null && m.getDescription().toLowerCase().contains(query.toLowerCase())))
                .toList();

        afficherMagasins(results);
    }

    private void chargerMagasins() {
        System.out.println("\n🔄 === CHARGEMENT DES MAGASINS ===");

        if (magasinContainer == null) {
            System.err.println("❌ magasinContainer est null!");
            return;
        }

        magasinContainer.getChildren().clear();
        MagasinDAO dao = new MagasinDAO();
        allMagasins = dao.getAllMagasinsByVendeur(idVendeurConnecte);

        System.out.println("🔍 Résultat: " + allMagasins.size() + " magasin(s) trouvé(s)");

        if (totalMagasinsLabel != null) {
            totalMagasinsLabel.setText(allMagasins.size() + " magasin" + (allMagasins.size() > 1 ? "s" : ""));
        }

        afficherMagasins(allMagasins);
    }

    private void afficherMagasins(List<Magasin> magasins) {
        magasinContainer.getChildren().clear();

        if (magasins.isEmpty()) {
            showEmptyState();
            return;
        }

        for (Magasin magasin : magasins) {
            VBox card = createMagasinCardPro(magasin);
            magasinContainer.getChildren().add(card);
        }
    }

    private VBox createMagasinCardPro(Magasin magasin) {
        VBox card = new VBox(0);
        card.setPrefWidth(320);
        card.setMaxWidth(320);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4); " +
                "-fx-cursor: hand;");

        VBox.setMargin(card, new Insets(12));

        // ========== IMAGE CONTAINER ==========
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(240);
        imageContainer.setStyle("-fx-background-color: #f1f5f9; " +
                "-fx-background-radius: 16 16 0 0;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(320);
        imageView.setFitHeight(240);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        chargerLogoMagasin(magasin, imageView);

        // Overlay gradient sur l'image
        Region gradientOverlay = new Region();
        gradientOverlay.setPrefSize(320, 240);
        gradientOverlay.setStyle("-fx-background-color: linear-gradient(to bottom, " +
                "transparent 60%, rgba(0,0,0,0.4));");

        // Badge catégorie en haut à gauche
        Label categoryBadge = new Label(magasin.getCategorie() != null ?
                magasin.getCategorie() : "Magasin");
        categoryBadge.setStyle("-fx-background-color: rgba(255,255,255,0.95); " +
                "-fx-text-fill: #1e293b; " +
                "-fx-padding: 6 12; " +
                "-fx-background-radius: 8; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold;");
        StackPane.setAlignment(categoryBadge, Pos.TOP_LEFT);
        StackPane.setMargin(categoryBadge, new Insets(12));

        // Badge "Nouveau" si récent
        Label newBadge = new Label("✨ Nouveau");
        newBadge.setStyle("-fx-background-color: #3b82f6; " +
                "-fx-text-fill: white; " +
                "-fx-padding: 6 12; " +
                "-fx-background-radius: 8; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold;");
        StackPane.setAlignment(newBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(newBadge, new Insets(12));

        imageContainer.getChildren().addAll(imageView, gradientOverlay, categoryBadge, newBadge);

        // Click sur l'image pour ouvrir les détails
        imageContainer.setOnMouseClicked(e -> {
            System.out.println("🖱️ Ouverture détails: " + magasin.getNomMagasin());
            ouvrirDetailMagasin(magasin);
        });

        // Effet hover sur l'image
        imageContainer.setOnMouseEntered(e -> {
            imageView.setOpacity(0.9);
            imageContainer.setStyle("-fx-background-color: #f1f5f9; " +
                    "-fx-background-radius: 16 16 0 0; " +
                    "-fx-scale-x: 1.02; -fx-scale-y: 1.02;");
        });
        imageContainer.setOnMouseExited(e -> {
            imageView.setOpacity(1.0);
            imageContainer.setStyle("-fx-background-color: #f1f5f9; " +
                    "-fx-background-radius: 16 16 0 0;");
        });

        // ========== INFO CONTAINER ==========
        VBox infoContainer = new VBox(12);
        infoContainer.setPadding(new Insets(16));

        // Titre du magasin
        Label nomLabel = new Label(magasin.getNomMagasin());
        nomLabel.setStyle("-fx-font-size: 18px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #0f172a;");
        nomLabel.setWrapText(true);
        nomLabel.setMaxWidth(280);

        // Rating et avis
        HBox ratingBox = new HBox(8);
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        Label starsLabel = new Label("★★★★☆");
        starsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #fbbf24;");

        Label ratingValue = new Label("4.2");
        ratingValue.setStyle("-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #0f172a;");

        Label reviewCount = new Label("(128 avis)");
        reviewCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        ratingBox.getChildren().addAll(starsLabel, ratingValue, reviewCount);

        // Description courte
        String desc = magasin.getDescription();
        if (desc != null && desc.length() > 85) {
            desc = desc.substring(0, 85) + "...";
        }
        Label descLabel = new Label(desc != null ? desc : "Découvrez ce magasin");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(280);
        descLabel.setStyle("-fx-font-size: 13px; " +
                "-fx-text-fill: #475569; " +
                "-fx-line-spacing: 2px;");

        // Localisation
        HBox locationBox = new HBox(6);
        locationBox.setAlignment(Pos.CENTER_LEFT);
        Label locationIcon = new Label("📍");
        locationIcon.setStyle("-fx-font-size: 14px;");
        Label locationLabel = new Label(magasin.getLocalisation() != null ?
                magasin.getLocalisation() : "Non spécifié");
        locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        locationBox.getChildren().addAll(locationIcon, locationLabel);

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #e2e8f0;");

        // Informations additionnelles
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(Pos.CENTER_LEFT);

        VBox vehiculesBox = createStatBox("🚗", magasin.getNbCommentaires() + "", "Véhicules");
        VBox visitsBox = createStatBox("👁️", "1.2k", "Vues");
        VBox statusBox = createStatBox("✅", "Actif", "Statut");

        statsBox.getChildren().addAll(vehiculesBox, visitsBox, statusBox);

        // Boutons d'action
        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setPadding(new Insets(8, 0, 0, 0));

        Button detailsBtn = new Button("Voir les détails");
        detailsBtn.setStyle("-fx-background-color: #3b82f6; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 8; " +
                "-fx-cursor: hand;");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(detailsBtn, Priority.ALWAYS);
        detailsBtn.setOnAction(e -> ouvrirDetailMagasin(magasin));

        Button editBtn = createIconButton("✏️");
        editBtn.setOnAction(e -> {
            e.consume();
            System.out.println("Modifier: " + magasin.getNomMagasin());
            showStyledAlert(Alert.AlertType.INFORMATION, "Modification",
                    "Fonctionnalité de modification à implémenter");
        });

        Button deleteBtn = createIconButton("🗑️");
        deleteBtn.setOnAction(e -> {
            e.consume();
            supprimerMagasin(magasin);
        });

        actionBox.getChildren().addAll(detailsBtn, editBtn, deleteBtn);

        infoContainer.getChildren().addAll(
                nomLabel, ratingBox, descLabel, locationBox,
                separator, statsBox, actionBox
        );

        card.getChildren().addAll(imageContainer, infoContainer);

        // Effet hover sur toute la carte
        setupCardHoverEffect(card);

        return card;
    }

    private VBox createStatBox(String emoji, String value, String label) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);

        HBox valueBox = new HBox(4);
        valueBox.setAlignment(Pos.CENTER);
        Label icon = new Label(emoji);
        icon.setStyle("-fx-font-size: 14px;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        valueBox.getChildren().addAll(icon, val);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        box.getChildren().addAll(valueBox, lbl);
        return box;
    }

    private Button createIconButton(String emoji) {
        Button btn = new Button(emoji);
        btn.setStyle("-fx-background-color: #f1f5f9; " +
                "-fx-padding: 10 12; " +
                "-fx-background-radius: 8; " +
                "-fx-font-size: 14px; " +
                "-fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #e2e8f0; " +
                        "-fx-padding: 10 12; " +
                        "-fx-background-radius: 8; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand;"));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #f1f5f9; " +
                        "-fx-padding: 10 12; " +
                        "-fx-background-radius: 8; " +
                        "-fx-font-size: 14px; " +
                        "-fx-cursor: hand;"));

        return btn;
    }

    private void setupCardHoverEffect(VBox card) {
        String baseStyle = "-fx-background-color: white; " +
                "-fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4); " +
                "-fx-cursor: hand;";

        String hoverStyle = "-fx-background-color: white; " +
                "-fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 20, 0, 0, 8); " +
                "-fx-cursor: hand; " +
                "-fx-translate-y: -4;";

        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));
    }

    private void supprimerMagasin(Magasin magasin) {
        if (magasin.getIdVendeur() != idVendeurConnecte) {
            showStyledAlert(Alert.AlertType.ERROR, "Action non autorisée",
                    "Vous ne pouvez supprimer que vos propres magasins");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer " + magasin.getNomMagasin() + " ?");
        confirm.setContentText("Cette action est irréversible. Tous les véhicules associés seront également supprimés.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            MagasinDAO dao = new MagasinDAO();
            dao.deleteMagasin(magasin.getIdMagasin());
            System.out.println("✅ Magasin supprimé: " + magasin.getNomMagasin());
            chargerMagasins();
            showStyledAlert(Alert.AlertType.INFORMATION, "Succès", "Magasin supprimé avec succès");
        }
    }

    private void showEmptyState() {
        VBox empty = new VBox(25);
        empty.setAlignment(Pos.CENTER);
        empty.setPadding(new Insets(100));
        empty.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 3);");

        Label icon = new Label("🏪");
        icon.setStyle("-fx-font-size: 80px;");

        Label title = new Label("Aucun magasin pour le moment");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        Label subtitle = new Label("Créez votre premier magasin pour commencer à vendre vos véhicules");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(400);
        subtitle.setAlignment(Pos.CENTER);

        Button createBtn = new Button("➕ Créer mon premier magasin");
        createBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #3b82f6, #2563eb); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 15px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 14 28; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand;");
        createBtn.setOnAction(e -> ouvrirAjouterMagasin());

        empty.getChildren().addAll(icon, title, subtitle, createBtn);
        magasinContainer.getChildren().add(empty);
    }

    private void ouvrirDetailMagasin(Magasin magasin) {
        System.out.println("🖱️ Navigation vers détails: " + magasin.getNomMagasin());
        nav.navigateWithData("/view/vendeur/MagasinDetails.fxml", magasin);
    }

    private void ouvrirAjouterMagasin() {
        System.out.println("\n➕ === Ouverture formulaire ajout magasin ===");

        if (!session.estConnecte() || !session.estVendeur()) {
            showErrorAlert("Session expirée", "Veuillez vous reconnecter");
            return;
        }

        MagasinDAO magasinDAO = new MagasinDAO();
        DemandeMagasinDAO demandeDAO = new DemandeMagasinDAO();

        if (!magasinDAO.hasMagasin(idVendeurConnecte)) {
            if (demandeDAO.hasDemandePending(idVendeurConnecte)) {
                showStyledAlert(Alert.AlertType.INFORMATION, "Demande en attente",
                        "Vous avez déjà une demande de création de magasin en attente.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Première création de magasin");
            alert.setHeaderText("Vous ne pouvez créer qu'un seul magasin");
            alert.setContentText("Voulez-vous envoyer une demande à l'admin ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                ouvrirFormulaireDemandeMagasin(idVendeurConnecte);
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/AjouterMagasin.fxml"));
                Parent form = loader.load();

                Stage stage = new Stage();
                stage.setTitle("Ajouter un Magasin");
                stage.setScene(new Scene(form));
                stage.setOnHidden(e -> chargerMagasins());
                stage.showAndWait();

            } catch (IOException ex) {
                ex.printStackTrace();
                showStyledAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible d'ouvrir le formulaire d'ajout");
            }
        }
    }

    private void ouvrirFormulaireDemandeMagasin(int vendeurId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/AjouterDemandeMagasin.fxml"));
            Parent root = loader.load();
            AjouterDemandeMagasinController controller = loader.getController();
            // Plus besoin de setVendeurId car récupéré automatiquement depuis la session
            // controller.setVendeurId(vendeurId);

            Stage stage = new Stage();
            stage.setTitle("Demande de création de magasin");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> {
                // Rafraîchir la liste après fermeture
                chargerMagasins();
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showStyledAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le formulaire de demande");
        }
    }

    private void chargerLogoMagasin(Magasin magasin, ImageView logoView) {
        try {
            String logoPath = magasin.getLogoMagasin();

            if (logoPath != null && !logoPath.isEmpty()) {
                File projetDir = new File(System.getProperty("user.dir"));
                File logosDir = new File(projetDir, "images/logos");

                File logoFile;
                if (logoPath.contains("images/logos")) {
                    logoFile = new File(projetDir, logoPath);
                } else {
                    logoFile = new File(logosDir, logoPath);
                }

                if (logoFile.exists()) {
                    Image logo = new Image(logoFile.toURI().toString());
                    logoView.setImage(logo);
                    System.out.println("✅ Logo chargé: " + logoFile.getName());
                } else {
                    chargerLogoParDefaut(logoView);
                }
            } else {
                chargerLogoParDefaut(logoView);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement logo: " + e.getMessage());
            chargerLogoParDefaut(logoView);
        }
    }

    private void chargerLogoParDefaut(ImageView logoView) {
        try {
            File projetDir = new File(System.getProperty("user.dir"));
            File defaultLogoFile = new File(projetDir, "images/logos/default-store.png");

            if (defaultLogoFile.exists()) {
                Image defaultLogo = new Image(defaultLogoFile.toURI().toString());
                logoView.setImage(defaultLogo);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur logo par défaut: " + e.getMessage());
        }
    }

    private void showErrorAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showStyledAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}