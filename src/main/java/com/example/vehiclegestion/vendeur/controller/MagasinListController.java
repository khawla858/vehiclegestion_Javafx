package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import com.example.vehiclegestion.utils.NavigationManager;


public class MagasinListController {
    private NavigationManager nav = NavigationManager.getInstance();


    @FXML private VBox magasinContainer;
    @FXML private Button btnAjouterMagasin;

    // ✅ Session Manager et ID vendeur
    private SessionManager session = SessionManager.getInstance();
    private int idVendeurConnecte; // ✅ Nom cohérent

    @FXML
    public void initialize() {
        System.out.println("\n🏪 === Initialisation MagasinListController ===");

        // ✅ Vérification de la session
        if (!session.estConnecte() || !session.estVendeur()) {
            System.err.println("❌ Accès refusé : utilisateur non connecté ou non vendeur");
            showErrorAlert("Accès refusé", "Connectez-vous en tant que vendeur");
            return;
        }

        // ✅ Récupérer l'ID du vendeur connecté
        idVendeurConnecte = session.getUserId();
        System.out.println("✅ Vendeur connecté - ID: " + idVendeurConnecte);
        System.out.println("   - Email: " + session.getUtilisateurConnecte().getEmail());
        session.debugSession();

        // Charger les magasins du vendeur connecté
        chargerMagasins();

        // Configuration du bouton d'ajout
        btnAjouterMagasin.setOnAction(e -> ouvrirAjouterMagasin());

        // Effets hover sur le bouton
        btnAjouterMagasin.setOnMouseEntered(e ->
                btnAjouterMagasin.setStyle("-fx-font-size: 15px; -fx-background-color: #e02849; " +
                        "-fx-text-fill: white; -fx-padding: 12 25; -fx-background-radius: 10; " +
                        "-fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(255,45,85,0.4), 12, 0, 0, 4); " +
                        "-fx-cursor: hand;")
        );
        btnAjouterMagasin.setOnMouseExited(e ->
                btnAjouterMagasin.setStyle("-fx-font-size: 15px; -fx-background-color: #ff2d55; " +
                        "-fx-text-fill: white; -fx-padding: 12 25; -fx-background-radius: 10; " +
                        "-fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(255,45,85,0.3), 10, 0, 0, 3); " +
                        "-fx-cursor: hand;")
        );

        System.out.println("🏪 === Fin initialisation ===\n");
    }

    private void showErrorAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void ouvrirAjouterMagasin() {
        System.out.println("\n➕ === Ouverture formulaire ajout magasin ===");

        // ✅ VÉRIFICATION SESSION (protection supplémentaire)
        if (!session.estConnecte() || !session.estVendeur()) {
            System.err.println("❌ Session expirée ou invalide");
            showErrorAlert("Session expirée", "Veuillez vous reconnecter");
            return;
        }

        MagasinDAO magasinDAO = new MagasinDAO();
        DemandeMagasinDAO demandeDAO = new DemandeMagasinDAO();

        // ✅ CORRECTION : Utiliser idVendeurConnecte (pas vendeurId)
        System.out.println("🔍 Vérification pour vendeur ID: " + idVendeurConnecte);

        // Vérifier si le vendeur a déjà un magasin
        if (!magasinDAO.hasMagasin(idVendeurConnecte)) {
            System.out.println("ℹ️ Le vendeur n'a pas encore de magasin");

            // Vérifier s'il a déjà une demande en attente
            if (demandeDAO.hasDemandePending(idVendeurConnecte)) {
                System.out.println("⚠️ Demande déjà en attente pour ce vendeur");
                showStyledAlert(Alert.AlertType.INFORMATION, "Demande en attente",
                        "Vous avez déjà une demande de création de magasin en attente.");
                return;
            }

            // Proposer d'envoyer une demande
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Première création de magasin");
            alert.setHeaderText("Vous ne pouvez créer qu'un seul magasin !");
            alert.setContentText("Voulez-vous envoyer une demande à l'admin pour créer votre premier magasin ?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                System.out.println("✅ Ouverture du formulaire de demande");
                ouvrirFormulaireDemandeMagasin(idVendeurConnecte); // ✅ Utiliser idVendeurConnecte
            }
        } else {
            System.out.println("✅ Le vendeur a déjà un magasin, ouverture du formulaire d'ajout");

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/AjouterMagasin.fxml"));
                Parent form = loader.load();

                // ✅ IMPORTANT : Le controller récupère automatiquement l'ID depuis SessionManager
                // Pas besoin de setVendeurId() ici car AjouterMagasinController le fait dans initialize()
                AjouterMagasinController controller = loader.getController();

                Stage stage = new Stage();
                stage.setTitle("Ajouter un Magasin");
                stage.setScene(new Scene(form));

                // ✅ Rafraîchir la liste après fermeture du formulaire
                stage.setOnHidden(e -> {
                    System.out.println("🔄 Fenêtre fermée - Rechargement de la liste...");
                    chargerMagasins();
                });

                stage.showAndWait(); // Attendre la fermeture

            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("❌ Erreur ouverture formulaire: " + ex.getMessage());
                showStyledAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible d'ouvrir le formulaire d'ajout");
            }
        }

        System.out.println("➕ === Fin ouverture formulaire ===\n");
    }

    private void chargerMagasins() {
        System.out.println("\n🔄 === RECHARGEMENT DES MAGASINS ===");
        System.out.println("📍 ID Vendeur connecté: " + idVendeurConnecte);

        magasinContainer.getChildren().clear();
        MagasinDAO dao = new MagasinDAO();

        // ✅ Récupérer LE magasin du vendeur connecté (un seul autorisé)
        Magasin magasin = dao.getMagasinByVendeur(idVendeurConnecte);

        // ✅ Logs détaillés pour débogage
        System.out.println("🔍 Résultat de la recherche:");
        if (magasin != null) {
            System.out.println("   ✅ Magasin trouvé:");
            System.out.println("      - ID Magasin: " + magasin.getIdMagasin());
            System.out.println("      - Nom: " + magasin.getNomMagasin());
            System.out.println("      - Adresse: " + magasin.getAdresse());
            System.out.println("      - ID Vendeur: " + magasin.getIdVendeur());
            System.out.println("      - Logo: " + magasin.getLogoMagasin());

            // Afficher la carte du magasin
            HBox card = createMagasinCard(magasin, dao);
            magasinContainer.getChildren().add(card);
            System.out.println("   ✅ Carte magasin créée et ajoutée à l'interface");
        } else {
            System.out.println("   ⚠️ AUCUN magasin trouvé pour ce vendeur");
            System.out.println("   💡 Vérifiez avec: SELECT * FROM Magasin WHERE id_vendeur = " + idVendeurConnecte + ";");
            showEmptyState();
        }

        System.out.println("🔄 === FIN RECHARGEMENT ===\n");
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

    private HBox createMagasinCard(Magasin magasin, MagasinDAO dao) {
        // ✅ VÉRIFICATION : Le magasin appartient bien au vendeur connecté
        if (magasin.getIdVendeur() != idVendeurConnecte) {
            System.err.println("⚠️ SÉCURITÉ : Tentative d'affichage d'un magasin non autorisé");
            return new HBox(); // Retourner une carte vide
        }

        HBox card = new HBox(0);
        card.setStyle("-fx-background-radius: 16; " +
                "-fx-background-color: white; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3); " +
                "-fx-padding: 0;");
        card.setPrefHeight(220);
        card.setMaxHeight(220);

        card.setCursor(Cursor.HAND);

        card.setOnMouseClicked(e -> {
            if (e.getTarget() instanceof Button) {
                return;
            }
            System.out.println("🖱️ Clic sur la carte du magasin: " + magasin.getNomMagasin());
            ouvrirDetailMagasin(magasin);
        });

        // ========== PARTIE GAUCHE : IMAGE ==========
        StackPane imagePane = new StackPane();
        imagePane.setPrefWidth(280);
        imagePane.setMaxWidth(280);
        imagePane.setStyle("-fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");

        ImageView logoView = new ImageView();
        logoView.setFitWidth(260);
        logoView.setFitHeight(260);
        logoView.setPreserveRatio(true);
        logoView.setSmooth(true);

        chargerLogoMagasin(magasin, logoView);

        StackPane.setAlignment(logoView, Pos.CENTER);

        Button likeBtn = new Button("🤍");
        likeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 50%; " +
                "-fx-font-size: 20px; -fx-padding: 8; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 4, 0, 0, 2);");
        likeBtn.setOnAction(e -> {
            e.consume();
            if (likeBtn.getText().equals("🤍")) {
                likeBtn.setText("❤️");
                showStyledAlert(Alert.AlertType.INFORMATION, "Favori ajouté",
                        magasin.getNomMagasin() + " a été ajouté à vos favoris !");
            } else {
                likeBtn.setText("🤍");
            }
        });
        StackPane.setAlignment(likeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(likeBtn, new Insets(15));

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

        imagePane.getChildren().addAll(logoView, likeBtn, dotsIndicator);

        // ========== PARTIE DROITE : INFORMATIONS ==========
        VBox infoPane = new VBox(12);
        infoPane.setPadding(new Insets(20, 20, 20, 25));
        HBox.setHgrow(infoPane, Priority.ALWAYS);

        HBox titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        Label numero = new Label("1.");
        numero.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #95a5a6;");
        Label nomLabel = new Label(magasin.getNomMagasin());
        nomLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleBox.getChildren().addAll(numero, nomLabel);

        HBox ratingBox = new HBox(8);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label rating = new Label("4.1");
        rating.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label stars = new Label("★★★★☆");
        stars.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700;");
        Label reviewCount = new Label("(2,443)");
        reviewCount.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        ratingBox.getChildren().addAll(rating, stars, reviewCount);

        String categorieText = magasin.getCategorie() != null ?
                magasin.getCategorie() : "Marchés aux puces et marchés de rue";
        Label categoryLabel = new Label(categorieText);
        categoryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");

        HBox statusBox = new HBox(8);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        Label statusLabel = new Label("Ouvert");
        statusLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7f8c8d;");
        statusBox.getChildren().add(statusLabel);

        HBox descBox = new HBox(10);
        descBox.setAlignment(Pos.TOP_LEFT);
        Label authorIcon = new Label("👤");
        authorIcon.setStyle("-fx-font-size: 16px;");
        VBox textBox = new VBox(4);

        Utilisateur user = session.getUtilisateurConnecte();
        Label authorLabel = new Label("Par " + user.getPrenom() + " " + user.getNom());
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

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox actionBox = new HBox(12);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button voirVehiculesBtn = new Button("Voir les véhicules");
        voirVehiculesBtn.setStyle("-fx-background-color: #00aa6c; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 20; " +
                "-fx-background-radius: 20; -fx-cursor: hand;");
        voirVehiculesBtn.setOnAction(e -> {
            e.consume();
            System.out.println("Voir véhicules pour : " + magasin.getNomMagasin());
        });

        Button voirDetailsBtn = new Button("Plus de détails");
        voirDetailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 20; " +
                "-fx-background-radius: 20; -fx-cursor: hand;");
        voirDetailsBtn.setOnAction(e -> {
            e.consume();
            ouvrirDetailMagasin(magasin);
        });

        int nbComments = magasin.getNbCommentaires();
        String commentText = formatCommentCount(nbComments);
        Label commentBadge = new Label(commentText);
        commentBadge.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1976d2; " +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8; " +
                "-fx-background-radius: 10;");

        actionBox.getChildren().addAll(voirVehiculesBtn, voirDetailsBtn, commentBadge);

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

            // ✅ SÉCURITÉ : Vérifier que le magasin appartient au vendeur
            if (magasin.getIdVendeur() != idVendeurConnecte) {
                showStyledAlert(Alert.AlertType.ERROR, "Action non autorisée",
                        "Vous ne pouvez supprimer que votre propre magasin");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer la suppression");
            confirm.setHeaderText("Supprimer le magasin ?");
            confirm.setContentText("Cette action est irréversible.");
            Optional<ButtonType> result = confirm.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                dao.deleteMagasin(magasin.getIdMagasin());
                System.out.println("✅ Magasin supprimé");
                chargerMagasins();
            }
        });

        Button locBtn = createSmallActionButton("📍", "#9b59b6");
        locBtn.setOnAction(e -> {
            e.consume();
            ouvrirLocalisation(magasin.getLocalisation());
        });

        editDeleteBox.getChildren().addAll(editBtn, deleteBtn, locBtn);

        infoPane.getChildren().addAll(titleBox, ratingBox, categoryLabel, statusBox, descBox, spacer, actionBox);

        HBox topRightOverlay = new HBox();
        topRightOverlay.setAlignment(Pos.TOP_RIGHT);
        topRightOverlay.setPadding(new Insets(10, 10, 0, 0));
        topRightOverlay.getChildren().add(editDeleteBox);

        StackPane.setAlignment(topRightOverlay, Pos.TOP_RIGHT);

        StackPane rightStack = new StackPane();
        rightStack.getChildren().addAll(infoPane, topRightOverlay);
        HBox.setHgrow(rightStack, Priority.ALWAYS);

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
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 4, 0, 0, 2);"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: rgba(255,255,255,0.95); -fx-background-radius: 6; " +
                "-fx-padding: 6 10; -fx-font-size: 14px; -fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 3, 0, 0, 1);"));
        return btn;
    }

    private void setupCardHoverEffect(HBox card) {
        String baseStyle = "-fx-background-radius: 16; -fx-background-color: white; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3); -fx-padding: 0;";
        String hoverStyle = "-fx-background-radius: 16; -fx-background-color: white; " +
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
            System.out.println("🖱️ Navigation vers détails du magasin: " + magasin.getNomMagasin());

            // ✅ Utiliser NavigationManager pour naviguer vers les détails avec les données
            nav.navigateWithData("/view/vendeur/MagasinDetails.fxml", magasin);
        }


    private void ouvrirLocalisation(String localisation) {
        if (localisation == null || localisation.isEmpty()) {
            showStyledAlert(Alert.AlertType.WARNING, "Localisation manquante",
                    "Aucune localisation n'est définie pour ce magasin.");
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
                System.out.println("ℹ️ Aucun logo défini pour: " + magasin.getNomMagasin());
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
                System.out.println("✅ Logo par défaut chargé");
            } else {
                System.out.println("⚠️ Logo par défaut introuvable, utilisation d'une icône");
                creerPlaceholderLogo(logoView);
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement logo par défaut: " + e.getMessage());
            creerPlaceholderLogo(logoView);
        }
    }

    private void creerPlaceholderLogo(ImageView logoView) {
        try {
            javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(160, 160);
            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

            gc.setFill(javafx.scene.paint.Color.web("#667eea"));
            gc.fillOval(20, 20, 120, 120);

            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 48));
            gc.fillText("🏪", 60, 90);

            javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
            params.setFill(javafx.scene.paint.Color.TRANSPARENT);
            javafx.scene.image.WritableImage image = canvas.snapshot(params, null);

            logoView.setImage(image);
        } catch (Exception e) {
            System.err.println("❌ Impossible de créer le placeholder: " + e.getMessage());
        }
    }
}