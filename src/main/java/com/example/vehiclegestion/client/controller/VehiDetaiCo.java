package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.vendeur.model.Article;
import com.example.vehiclegestion.vendeur.model.Commentaire;
import com.example.vehiclegestion.vendeur.dao.CommentaireDAO;
import com.example.vehiclegestion.utils.DataReceiver;
import com.example.vehiclegestion.auth.utils.SessionManager;
import com.example.vehiclegestion.client.model.Vehicle;
import com.example.vehiclegestion.vendeur.controller.MagasinDetailsController;
import com.example.vehiclegestion.vendeur.model.Magasin;
import com.example.vehiclegestion.vendeur.dao.MagasinDAO;

// ✅ IMPORTS POUR LES LOGS ELASTICSEARCH
import com.example.vehiclegestion.logging.service.ElasticLogService;
import com.example.vehiclegestion.logging.model.LogEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Node;
import javafx.geometry.Pos;
import javafx.application.Platform;
import com.example.vehiclegestion.common.dao.ChatDAO;
import com.example.vehiclegestion.common.model.Conversation;
import com.example.vehiclegestion.common.controller.ChatWindowController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.example.vehiclegestion.utils.DatabaseConnection;

public class VehiDetaiCo implements DataReceiver {

    // ========== LOGGING ==========
    private static final Logger logger = LoggerFactory.getLogger(VehiDetaiCo.class);
    private final ElasticLogService elasticLogService = new ElasticLogService();

    @FXML private ScrollPane scrollPane;
    @FXML private HBox mainContainer;
    @FXML private VBox leftSection;
    @FXML private ImageView mainImageView;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label locationLabel;
    @FXML private Label dateLabel;
    @FXML private VBox characteristicsContainer;
    @FXML private Label descriptionLabel;
    @FXML private VBox commentairesSection;
    @FXML private VBox sellerSection;
    @FXML private Button contactBtn;
    @FXML private Button backBtn;
    @FXML private Button visiterMagasinBtn;

    private Article article;
    private CommentaireDAO commentaireDAO = new CommentaireDAO();
    private boolean articleCharge = false;

    @Override
    public void receiveData(Object data) {
        logger.info("📥 Réception des données du véhicule");

        if (data instanceof Article) {
            this.article = (Article) data;
            this.articleCharge = true;

            // 📊 LOG: Article chargé
            LogEntry logEntry = LogEntry.success(
                            "VEHICLE_DETAILS_OPENED",
                            getUserEmail(),
                            "Ouverture des détails du véhicule: " + article.getTitre()
                    )
                    .addMetadata("articleId", article.getId())
                    .addMetadata("articleTitle", article.getTitre())
                    .addMetadata("price", article.getPrix())
                    .addMetadata("category", article.getCategorie());

            if (SessionManager.getInstance().estConnecte()) {
                logEntry.setUserId((long) SessionManager.getInstance().getUserId());
                logEntry.setUserRole(SessionManager.getInstance().getUserRole());
            }

            elasticLogService.sendLog(logEntry);
            logger.info("✅ Article chargé - ID: {}, Titre: {}", article.getId(), article.getTitre());

            displayArticleDetails();
            loadCommentaires();
        } else {
            // 📊 LOG: Erreur chargement
            LogEntry errorLog = LogEntry.error(
                    "VEHICLE_DETAILS_LOAD_FAILED",
                    getUserEmail(),
                    "Échec du chargement des détails du véhicule"
            ).addMetadata("dataType", data != null ? data.getClass().getName() : "null");

            elasticLogService.sendLog(errorLog);
            logger.error("❌ Données non valides pour l'affichage des détails");

            this.articleCharge = false;
            showError("Erreur", "Impossible de charger les données du véhicule");
        }
    }

    @FXML
    private void initialize() {
        logger.info("🔧 Initialisation du contrôleur VehicleDetail");

        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");

        if (backBtn != null) {
            backBtn.setOnAction(e -> goBack());
            backBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                    "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        }

        if (contactBtn != null) {
            contactBtn.setOnAction(e -> handleContact());
            contactBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                    "-fx-padding: 12 30; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        }

        if (visiterMagasinBtn != null) {
            visiterMagasinBtn.setOnAction(e -> handleVisiterMagasin());
            visiterMagasinBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                    "-fx-padding: 10 20; -fx-background-radius: 8; " +
                    "-fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
        }
    }

    private void goBack() {
        // 📊 LOG: Fermeture de la page
        LogEntry logEntry = LogEntry.success(
                "VEHICLE_DETAILS_CLOSED",
                getUserEmail(),
                "Fermeture des détails du véhicule"
        );

        if (article != null) {
            logEntry.addMetadata("articleId", article.getId())
                    .addMetadata("articleTitle", article.getTitre());
        }

        elasticLogService.sendLog(logEntry);
        logger.info("🔙 Retour à la page précédente");

        Stage stage = (Stage) backBtn.getScene().getWindow();
        stage.close();
    }

    private void loadCommentaires() {
        logger.info("🔄 Chargement des commentaires pour l'article ID: {}",
                article != null ? article.getId() : "null");

        if (!articleCharge || article == null) {
            logger.error("❌ Impossible de charger les commentaires - Article non chargé");
            return;
        }

        // 📊 LOG: Consultation des commentaires
        LogEntry logEntry = LogEntry.success(
                        "COMMENTS_VIEWED",
                        getUserEmail(),
                        "Consultation des avis du véhicule"
                )
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre());

        elasticLogService.sendLog(logEntry);

        commentairesSection.getChildren().clear();
        commentairesSection.setStyle("-fx-spacing: 15; -fx-padding: 20; -fx-background-color: white; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        HBox header = createCommentaireHeader();
        commentairesSection.getChildren().add(header);

        boolean estConnecte = SessionManager.getInstance().estConnecte();
        logger.info("🔐 État de la session: {}", estConnecte ? "Connecté" : "Non connecté");

        if (estConnecte) {
            int userId = SessionManager.getInstance().getUserId();
            boolean dejaCommente = commentaireDAO.aDejaCommente(userId, article.getId());
            logger.info("📝 Utilisateur {} - Déjà commenté: {}", userId, dejaCommente);

            if (dejaCommente) {
                Commentaire monCommentaire = commentaireDAO.getCommentaireUtilisateur(userId, article.getId());
                if (monCommentaire != null) {
                    VBox myCommentCard = createMyCommentCard(monCommentaire);
                    commentairesSection.getChildren().add(myCommentCard);
                } else {
                    VBox alreadyCommented = createAlreadyCommentedMessage();
                    commentairesSection.getChildren().add(alreadyCommented);
                }
            } else {
                VBox addCommentForm = createAddCommentForm();
                commentairesSection.getChildren().add(addCommentForm);
            }
        } else {
            VBox loginPrompt = createLoginPrompt();
            commentairesSection.getChildren().add(loginPrompt);
        }

        commentairesSection.getChildren().add(new Separator());

        List<Commentaire> commentaires = commentaireDAO.getCommentairesByArticle(article.getId());
        logger.info("📋 Nombre de commentaires récupérés: {}", commentaires.size());

        if (commentaires.isEmpty()) {
            Label noComments = new Label("Aucun commentaire pour le moment. Soyez le premier à donner votre avis !");
            noComments.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 20 0;");
            commentairesSection.getChildren().add(noComments);
        } else {
            for (Commentaire c : commentaires) {
                VBox commentCard = createCommentCard(c);
                commentairesSection.getChildren().add(commentCard);
            }
        }
    }

    private void saveCommentaire(int note, String texte) {
        logger.info("💾 Tentative de sauvegarde d'un commentaire");

        if (!articleCharge || article == null) {
            logger.error("❌ Article non chargé - Impossible de sauvegarder le commentaire");

            // 📊 LOG: Échec sauvegarde commentaire
            elasticLogService.sendLog(LogEntry.error(
                    "COMMENT_SAVE_FAILED",
                    getUserEmail(),
                    "Tentative de commentaire sans article chargé"
            ));

            showError("Erreur", "Article non chargé - Veuillez réessayer");
            return;
        }

        SessionManager session = SessionManager.getInstance();
        if (!session.estConnecte()) {
            logger.error("❌ Session non active - Commentaire refusé");

            // 📊 LOG: Tentative sans connexion
            elasticLogService.sendLog(LogEntry.warning(
                    "COMMENT_UNAUTHORIZED",
                    "anonymous",
                    "Tentative de commentaire sans être connecté"
            ).addMetadata("articleId", article.getId()));

            showError("Erreur", "Vous devez être connecté");
            return;
        }

        int idUtilisateur = session.getUserId();
        logger.info("👤 Sauvegarde commentaire - User ID: {}, Article ID: {}, Note: {}",
                idUtilisateur, article.getId(), note);

        boolean success = commentaireDAO.ajouterCommentaire(idUtilisateur, article.getId(), note, texte);

        if (success) {
            // 📊 LOG: Commentaire créé avec succès
            LogEntry logEntry = LogEntry.success(
                            "COMMENT_CREATED",
                            session.getEmail(),
                            "Nouvel avis publié"
                    )
                    .addMetadata("articleId", article.getId())
                    .addMetadata("articleTitle", article.getTitre())
                    .addMetadata("rating", note)
                    .addMetadata("commentLength", texte.length());

            logEntry.setUserId((long) idUtilisateur);
            logEntry.setUserRole(session.getUserRole());

            elasticLogService.sendLog(logEntry);

            logger.info("✅ Commentaire sauvegardé avec succès");
            showSuccess("Succès", "Votre avis a été publié avec succès !");
            loadCommentaires();
        } else {
            // 📊 LOG: Échec sauvegarde
            elasticLogService.sendLog(LogEntry.error(
                            "COMMENT_SAVE_ERROR",
                            session.getEmail(),
                            "Erreur lors de la sauvegarde du commentaire"
                    )
                    .addMetadata("articleId", article.getId())
                    .addMetadata("userId", idUtilisateur));

            logger.error("❌ Échec de la sauvegarde du commentaire");
            showError("Erreur", "Impossible de publier votre avis. Veuillez réessayer.");
        }
    }

    @FXML
    private void handleVisiterMagasin() {
        logger.info("🏪 Demande de visite du magasin");

        if (article == null) {
            logger.error("❌ Aucun article sélectionné");
            showError("Erreur", "Aucun véhicule sélectionné");
            return;
        }

        int vendeurId = getVendeurIdFromDatabase(article.getId());
        logger.info("🔍 ID vendeur récupéré: {}", vendeurId);

        if (vendeurId <= 0) {
            logger.error("❌ ID vendeur invalide");
            showError("Erreur", "Informations du magasin non disponibles");
            return;
        }

        // 📊 LOG: Consultation magasin
        LogEntry logEntry = LogEntry.success(
                        "STORE_VISIT_INITIATED",
                        getUserEmail(),
                        "Tentative de visite du magasin"
                )
                .addMetadata("vendeurId", vendeurId)
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre());

        if (SessionManager.getInstance().estConnecte()) {
            logEntry.setUserId((long) SessionManager.getInstance().getUserId());
        }

        elasticLogService.sendLog(logEntry);

        Magasin magasin = getMagasinDetails(vendeurId);

        if (magasin == null) {
            logger.warn("⚠️ Magasin non configuré pour le vendeur ID: {}", vendeurId);

            // 📊 LOG: Magasin non trouvé
            elasticLogService.sendLog(LogEntry.warning(
                    "STORE_NOT_FOUND",
                    getUserEmail(),
                    "Magasin non configuré"
            ).addMetadata("vendeurId", vendeurId));

            showInfo("Magasin", "Ce vendeur n'a pas encore configuré son magasin.\n\n" +
                    "Souhaitez-vous le contacter directement ?", vendeurId);
            return;
        }

        // 📊 LOG: Magasin ouvert
        elasticLogService.sendLog(LogEntry.success(
                        "STORE_OPENED",
                        getUserEmail(),
                        "Ouverture du magasin: " + magasin.getNomMagasin()
                )
                .addMetadata("magasinId", magasin.getIdMagasin())
                .addMetadata("magasinNom", magasin.getNomMagasin())
                .addMetadata("vendeurId", vendeurId));

        ouvrirFenetreMagasin(magasin);
    }

    @FXML
    private void handleRendezvous() {
        logger.info("📅 Demande de prise de rendez-vous");

        if (article == null) {
            logger.error("❌ Aucun article sélectionné");
            showError("Erreur", "Aucun véhicule sélectionné");
            return;
        }

        int idVendeur = getVendeurIdFromDatabase(article.getId());
        logger.info("🔍 ID vendeur pour RDV: {}", idVendeur);

        if (idVendeur <= 0) {
            logger.error("❌ Impossible de trouver le vendeur");
            showError("Erreur", "Impossible de trouver le vendeur de ce véhicule");
            return;
        }

        // 📊 LOG: Demande rendez-vous
        LogEntry logEntry = LogEntry.success(
                        "APPOINTMENT_REQUEST_INITIATED",
                        getUserEmail(),
                        "Demande de rendez-vous pour: " + article.getTitre()
                )
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre())
                .addMetadata("vendeurId", idVendeur);

        if (SessionManager.getInstance().estConnecte()) {
            logEntry.setUserId((long) SessionManager.getInstance().getUserId());
            logEntry.setUserRole(SessionManager.getInstance().getUserRole());
        }

        elasticLogService.sendLog(logEntry);

        article.setIdVendeur(idVendeur);
        Vehicle vehicle = convertArticleToVehicle(article);

        boolean formulaireOuvert = openRendezVousForm();
        if (!formulaireOuvert) {
            openRendezVousFormIntegre();
        }
    }

    @FXML
    private void handleContact() {
        logger.info("📞 Demande de contact avec le vendeur");

        if (article == null) {
            logger.error("❌ Aucun article sélectionné");
            showError("Erreur", "Aucun véhicule sélectionné");
            return;
        }

        if (!SessionManager.getInstance().estConnecte()) {
            logger.warn("⚠️ Tentative de contact sans connexion");

            // 📊 LOG: Contact refusé
            elasticLogService.sendLog(LogEntry.warning(
                    "CONTACT_UNAUTHORIZED",
                    "anonymous",
                    "Tentative de contact sans être connecté"
            ).addMetadata("articleId", article.getId()));

            showWarning("Connexion requise", "Vous devez être connecté pour contacter le vendeur");
            return;
        }

        int clientId = SessionManager.getInstance().getUserId();
        String clientRole = SessionManager.getInstance().getUserRole();
        int vendeurId = getVendeurIdFromDatabase(article.getId());

        logger.info("👤 Client ID: {} ({}), Vendeur ID: {}", clientId, clientRole, vendeurId);

        if (vendeurId <= 0) {
            logger.error("❌ Vendeur introuvable");
            showError("Erreur", "Impossible de trouver le vendeur de ce véhicule");
            return;
        }

        if (clientId == vendeurId) {
            logger.warn("⚠️ Tentative d'auto-contact");

            // 📊 LOG: Auto-contact bloqué
            elasticLogService.sendLog(LogEntry.warning(
                            "SELF_CONTACT_BLOCKED",
                            SessionManager.getInstance().getEmail(),
                            "Tentative de contacter sa propre annonce"
                    )
                    .addMetadata("userId", clientId)
                    .addMetadata("articleId", article.getId()));

            showWarning("Action non autorisée", "Vous ne pouvez pas contacter votre propre annonce");
            return;
        }

        // 📊 LOG: Ouverture chat
        LogEntry logEntry = LogEntry.success(
                        "CHAT_OPENED",
                        SessionManager.getInstance().getEmail(),
                        "Ouverture du chat avec le vendeur"
                )
                .addMetadata("clientId", clientId)
                .addMetadata("vendeurId", vendeurId)
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre());

        logEntry.setUserId((long) clientId);
        logEntry.setUserRole(clientRole);

        elasticLogService.sendLog(logEntry);

        openChatWindow(vendeurId, clientId, article);
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Récupère l'email de l'utilisateur connecté ou retourne "anonymous"
     */
    private String getUserEmail() {
        if (SessionManager.getInstance().estConnecte()) {
            return SessionManager.getInstance().getEmail();
        }
        return "anonymous";
    }

    /**
     * Fermeture propre du service de logs
     */
    public void cleanup() {
        logger.info("🛑 Fermeture du contrôleur VehicleDetail");
        elasticLogService.shutdown();
    }

    // ========== AFFICHAGE DES DÉTAILS ==========

    private void displayArticleDetails() {
        if (!articleCharge || article == null) {
            logger.error("❌ Impossible d'afficher: article non chargé");
            return;
        }

        loadMainImage();
        titleLabel.setText(article.getTitre());
        priceLabel.setText(String.format("%,.0f DH", article.getPrix()));
        locationLabel.setText("📍 " + getRandomCity());
        dateLabel.setText("Publiée le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        displayCharacteristics();
        displayDescription();
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
            } catch (Exception e) {
                setFallbackImage();
            }
        } else {
            setFallbackImage();
        }
    }

    private void setFallbackImage() {
        StackPane placeholder = new StackPane();
        placeholder.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                "-fx-background-radius: 8;");
        placeholder.setPrefSize(650, 450);
        Label icon = new Label("🚗");
        icon.setStyle("-fx-font-size: 80px;");
        placeholder.getChildren().add(icon);

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

        addCharacteristic("📅 Année-Modèle", String.valueOf(article.getAnnee()));
        addCharacteristic("📏 Kilométrage", String.valueOf(article.getKilometrage()) + " km");
        if (article.getTransmission() != null) addCharacteristic("⚙️ Boîte de vitesses", article.getTransmission());
        if (article.getCarburant() != null) addCharacteristic("⛽ Type de carburant", article.getCarburant());
        if (article.getMarque() != null) addCharacteristic("🚗 Marque", article.getMarque());
        if (article.getModele() != null) addCharacteristic("🏷️ Modèle", article.getModele());
        if (article.getPuissance() > 0) addCharacteristic("🔋 Puissance", article.getPuissance() + " ch");
        addCharacteristic("🌍 Origine", "Maroc");
        if (article.getEtat() != null) addCharacteristic("⭐ État", article.getEtat());
        if (article.getCategorie() != null) addCharacteristic("📂 Catégorie", article.getCategorie());
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
    }

    // ========== GESTION DES COMMENTAIRES - UI ==========

    private VBox createMyCommentCard(Commentaire commentaire) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 20; -fx-background-radius: 8; " +
                "-fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("✓");
        icon.setStyle("-fx-font-size: 24px; -fx-text-fill: #4CAF50;");

        Label title = new Label("Votre avis");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        header.getChildren().addAll(icon, title);

        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        int noteEntiere = (int) Math.round(commentaire.getNote());
        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < noteEntiere ? "⭐" : "☆");
            star.setStyle("-fx-font-size: 16px; -fx-text-fill: #FFB300;");
            ratingBox.getChildren().add(star);
        }

        Label noteText = new Label("Note: " + commentaire.getNote() + "/5");
        noteText.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

        HBox noteContainer = new HBox(10);
        noteContainer.setAlignment(Pos.CENTER_LEFT);
        noteContainer.getChildren().addAll(ratingBox, noteText);

        Label commentText = new Label(commentaire.getTexteCommentaire());
        commentText.setWrapText(true);
        commentText.setStyle("-fx-text-fill: #333; -fx-font-size: 14px; -fx-line-spacing: 1.3;");

        Label dateLabel = new Label("Publié " + formatDate(commentaire.getDateCommentaire()));
        dateLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px; -fx-font-style: italic;");

        card.getChildren().addAll(header, noteContainer, commentText, dateLabel);
        return card;
    }

    private HBox createCommentaireHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("💬 Avis des utilisateurs");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

        double avgNote = commentaireDAO.getNoteMoyenne(article.getId());
        int totalComments = commentaireDAO.getNombreCommentaires(article.getId());

        if (totalComments > 0) {
            HBox stats = new HBox(8);
            stats.setAlignment(Pos.CENTER_LEFT);
            stats.setStyle("-fx-background-color: #FFF8E1; -fx-padding: 8 15; -fx-background-radius: 20;");

            Label starLabel = new Label("⭐");
            Label noteLabel = new Label(String.format("%.1f", avgNote));
            noteLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #F57F17;");

            Label countLabel = new Label("(" + totalComments + " avis)");
            countLabel.setStyle("-fx-text-fill: #999;");

            stats.getChildren().addAll(starLabel, noteLabel, countLabel);
            header.getChildren().addAll(title, stats);
        } else {
            header.getChildren().add(title);
        }

        return header;
    }

    private VBox createAddCommentForm() {
        VBox form = new VBox(12);
        form.setStyle("-fx-background-color: #F8F9FA; -fx-padding: 15; -fx-background-radius: 8;");

        Label formTitle = new Label("Laisser un avis");
        formTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        HBox ratingBox = new HBox(8);
        ratingBox.setAlignment(Pos.CENTER_LEFT);
        Label ratingLabel = new Label("Votre note :");

        HBox stars = new HBox(5);
        ToggleGroup ratingGroup = new ToggleGroup();
        for (int i = 1; i <= 5; i++) {
            ToggleButton star = new ToggleButton("⭐");
            star.setUserData(i);
            star.setToggleGroup(ratingGroup);
            star.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand;");
            star.selectedProperty().addListener((obs, old, selected) -> {
                if (selected) {
                    star.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand; -fx-text-fill: #FFB300;");
                } else {
                    star.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand;");
                }
            });
            stars.getChildren().add(star);
        }
        ratingBox.getChildren().addAll(ratingLabel, stars);

        TextArea commentText = new TextArea();
        commentText.setPromptText("Partagez votre expérience avec ce véhicule...");
        commentText.setPrefRowCount(3);
        commentText.setWrapText(true);

        Button submitBtn = new Button("✓ Publier mon avis");
        submitBtn.setStyle("-fx-background-color: #0066FF; -fx-text-fill: white; " +
                "-fx-padding: 10 25; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            if (!articleCharge || article == null) {
                showError("Erreur", "Article non chargé - Impossible d'ajouter un commentaire");
                return;
            }

            ToggleButton selectedStar = (ToggleButton) ratingGroup.getSelectedToggle();
            if (selectedStar == null) {
                showWarning("Attention", "Veuillez sélectionner une note");
                return;
            }
            if (commentText.getText().trim().isEmpty()) {
                showWarning("Attention", "Veuillez écrire un commentaire");
                return;
            }

            int note = (int) selectedStar.getUserData();
            String texte = commentText.getText().trim();
            saveCommentaire(note, texte);
        });

        form.getChildren().addAll(formTitle, ratingBox, commentText, submitBtn);
        return form;
    }

    private VBox createAlreadyCommentedMessage() {
        VBox message = new VBox(10);
        message.setAlignment(Pos.CENTER);
        message.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 20; -fx-background-radius: 8;");

        Label icon = new Label("✓");
        icon.setStyle("-fx-font-size: 32px; -fx-text-fill: #4CAF50;");

        Label text = new Label("Vous avez déjà laissé un avis pour cet article");
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");

        message.getChildren().addAll(icon, text);
        return message;
    }

    private VBox createLoginPrompt() {
        VBox prompt = new VBox(10);
        prompt.setAlignment(Pos.CENTER);
        prompt.setStyle("-fx-background-color: #F0F4FF; -fx-padding: 25; -fx-background-radius: 8;");

        Label icon = new Label("🔒");
        icon.setStyle("-fx-font-size: 32px;");

        Label message = new Label("Connectez-vous pour laisser un avis");
        message.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Button loginBtn = new Button("Se connecter");
        loginBtn.setStyle("-fx-background-color: #0066FF; -fx-text-fill: white; " +
                "-fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        loginBtn.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
                Parent root = loader.load();
                Stage loginStage = new Stage();
                loginStage.setTitle("Connexion");
                loginStage.setScene(new Scene(root, 400, 500));
                loginStage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        prompt.getChildren().addAll(icon, message, loginBtn);
        return prompt;
    }

    private VBox createCommentCard(Commentaire c) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        String avatarColor = getAvatarColor(c.getNomClient());
        avatar.setStyle("-fx-background-color: " + avatarColor + "; -fx-background-radius: 25; " +
                "-fx-min-width: 50; -fx-min-height: 50;");

        String nomClient = c.getNomClient() != null ? c.getNomClient() : "Utilisateur";
        String initiale = nomClient.substring(0, 1).toUpperCase();

        Label avatarText = new Label(initiale);
        avatarText.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        avatar.getChildren().add(avatarText);

        VBox userInfo = new VBox(4);
        Label userName = new Label(nomClient);
        userName.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");

        HBox metaInfo = new HBox(10);
        metaInfo.setAlignment(Pos.CENTER_LEFT);

        HBox noteBox = new HBox(3);
        int noteEntiere = (int) Math.round(c.getNote());
        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < noteEntiere ? "⭐" : "☆");
            star.setStyle("-fx-text-fill: #FFB300;");
            noteBox.getChildren().add(star);
        }

        Label dateLabel = new Label("• " + formatDate(c.getDateCommentaire()));
        dateLabel.setStyle("-fx-text-fill: #999;");

        metaInfo.getChildren().addAll(noteBox, dateLabel);
        userInfo.getChildren().addAll(userName, metaInfo);
        header.getChildren().addAll(avatar, userInfo);

        Label commentText = new Label(c.getTexteCommentaire());
        commentText.setWrapText(true);
        commentText.setStyle("-fx-text-fill: #555; -fx-line-spacing: 1.3;");

        card.getChildren().addAll(header, commentText);
        return card;
    }

    private String getAvatarColor(String nomClient) {
        if (nomClient.contains("🏪")) return "#FF9800";
        if (nomClient.contains("⭐")) return "#F44336";
        return "#2196F3";
    }

    private String formatDate(LocalDateTime date) {
        if (date == null) return "Date inconnue";
        long daysSince = java.time.temporal.ChronoUnit.DAYS.between(date, LocalDateTime.now());
        if (daysSince == 0) return "Aujourd'hui";
        if (daysSince == 1) return "Hier";
        if (daysSince < 7) return "Il y a " + daysSince + " jours";
        if (daysSince < 30) return "Il y a " + (daysSince / 7) + " semaines";
        return date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    // ========== SECTION VENDEUR ==========

    private void displaySellerSection() {
        sellerSection.getChildren().clear();
        sellerSection.setStyle("-fx-spacing: 15; -fx-padding: 20; -fx-background-color: white; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        HBox sellerHeader = new HBox(12);
        sellerHeader.setAlignment(Pos.CENTER_LEFT);

        StackPane avatar = new StackPane();
        avatar.setStyle("-fx-background-color: #3498db; -fx-background-radius: 30; " +
                "-fx-min-width: 60; -fx-min-height: 60;");
        Label avatarText = new Label("V");
        avatarText.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        avatar.getChildren().add(avatarText);

        VBox sellerInfo = new VBox(5);
        Label sellerName = new Label("Vendeur Professionnel");
        sellerName.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        Label sellerBadge = new Label("⭐ Membre depuis 2020");
        sellerBadge.setStyle("-fx-text-fill: #FF9800;");
        sellerInfo.getChildren().addAll(sellerName, sellerBadge);
        sellerHeader.getChildren().addAll(avatar, sellerInfo);

        VBox warningBox = new VBox(8);
        warningBox.setStyle("-fx-background-color: #FFF3E0; -fx-padding: 12; -fx-background-radius: 6;");
        Label warningIcon = new Label("⚠️ Important");
        warningIcon.setStyle("-fx-font-weight: bold; -fx-text-fill: #F57C00;");
        Label warningText = new Label("Il ne faut jamais envoyer d'argent ni d'avance en cas de transfert.");
        warningText.setWrapText(true);
        warningText.setStyle("-fx-text-fill: #666;");
        warningBox.getChildren().addAll(warningIcon, warningText);

        VBox actionButtons = createActionButtons();
        sellerSection.getChildren().addAll(sellerHeader, warningBox, actionButtons);
    }

    private VBox createActionButtons() {
        VBox buttonsContainer = new VBox(10);
        buttonsContainer.setStyle("-fx-padding: 15 0 0 0;");

        Button rendezvousBtn = new Button("📅 Prendre rendez-vous");
        rendezvousBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-font-size: 14px;");
        rendezvousBtn.setMaxWidth(Double.MAX_VALUE);
        rendezvousBtn.setOnAction(e -> handleRendezvous());

        Button chatBtn = new Button("💬 Contacter le Vendeur");
        chatBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-font-size: 14px;");
        chatBtn.setMaxWidth(Double.MAX_VALUE);
        chatBtn.setOnAction(e -> handleContact());

        Button callBtn = new Button("📞 Appeler le Vendeur");
        callBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-font-size: 14px;");
        callBtn.setMaxWidth(Double.MAX_VALUE);
        callBtn.setOnAction(e -> {
            showInfo("Appel", "Numéro: +212 6XX XXX XXX\n(Fonctionnalité en développement)");
        });

        Button magasinBtn = new Button("🏪 Visiter Magasin");
        magasinBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-font-size: 14px;");
        magasinBtn.setMaxWidth(Double.MAX_VALUE);
        magasinBtn.setOnAction(e -> handleVisiterMagasin());

        buttonsContainer.getChildren().addAll(rendezvousBtn, chatBtn, callBtn, magasinBtn);
        return buttonsContainer;
    }

    // ========== GESTION MAGASIN ==========

    private void ouvrirFenetreMagasin(Magasin magasin) {
        try {
            logger.info("🏪 Ouverture fenêtre magasin pour: {}", magasin.getNomMagasin());

            String[] cheminsFXML = {
                    "/com/example/vehiclegestion/view/vendeur/magasinDetails.fxml",
                    "/view/vendeur/magasinDetails.fxml",
                    "/magasinDetails.fxml"
            };

            FXMLLoader loader = null;
            Parent root = null;
            String cheminTrouve = null;

            for (String chemin : cheminsFXML) {
                try {
                    URL url = getClass().getResource(chemin);
                    if (url != null) {
                        loader = new FXMLLoader(url);
                        root = loader.load();
                        cheminTrouve = chemin;
                        logger.info("✅ FXML chargé: {}", chemin);
                        break;
                    }
                } catch (Exception e) {
                    logger.debug("❌ Échec pour: {}", chemin);
                }
            }

            if (root == null) {
                logger.warn("⚠️ FXML non trouvé, création d'une fenêtre simple...");
                creerFenetreMagasinSimple(magasin);
                return;
            }

            MagasinDetailsController controller = loader.getController();
            controller.setMagasin(magasin);

            Stage magasinStage = new Stage();
            magasinStage.setTitle("Magasin - " + magasin.getNomMagasin());
            magasinStage.setScene(new Scene(root, 1200, 800));

            magasinStage.initModality(Modality.WINDOW_MODAL);
            if (visiterMagasinBtn != null) {
                magasinStage.initOwner(visiterMagasinBtn.getScene().getWindow());
            }

            magasinStage.setMinWidth(900);
            magasinStage.setMinHeight(600);
            magasinStage.show();

            logger.info("✅ Fenêtre magasin ouverte: {}", cheminTrouve);

        } catch (Exception e) {
            logger.error("❌ Erreur ouverture fenêtre magasin: {}", e.getMessage());
            creerFenetreMagasinSimple(magasin);
        }
    }

    private void creerFenetreMagasinSimple(Magasin magasin) {
        try {
            Stage stage = new Stage();
            stage.setTitle("Magasin - " + magasin.getNomMagasin());

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: #f5f7fa;");

            VBox content = new VBox(20);
            content.setStyle("-fx-padding: 30;");

            HBox header = new HBox(20);
            header.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12;");

            StackPane logoPlaceholder = new StackPane();
            logoPlaceholder.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8; -fx-min-width: 100; -fx-min-height: 100;");
            Label logoLabel = new Label("🏪");
            logoLabel.setStyle("-fx-font-size: 40px;");
            logoPlaceholder.getChildren().add(logoLabel);

            VBox infos = new VBox(10);
            Label nomLabel = new Label(magasin.getNomMagasin());
            nomLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            Label categorieLabel = new Label(magasin.getCategorie());
            categorieLabel.setStyle("-fx-text-fill: #666;");

            infos.getChildren().addAll(nomLabel, categorieLabel);
            header.getChildren().addAll(logoPlaceholder, infos);

            VBox details = new VBox(15);
            details.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12;");

            if (magasin.getAdresse() != null && !magasin.getAdresse().isEmpty()) {
                HBox adresseBox = new HBox(10);
                adresseBox.getChildren().addAll(new Label("📍"), new Label(magasin.getAdresse()));
                details.getChildren().add(adresseBox);
            }

            if (magasin.getTelephone() != null && !magasin.getTelephone().isEmpty()) {
                HBox telBox = new HBox(10);
                telBox.getChildren().addAll(new Label("📞"), new Label(magasin.getTelephone()));
                details.getChildren().add(telBox);
            }

            if (magasin.getEmailContact() != null && !magasin.getEmailContact().isEmpty()) {
                HBox emailBox = new HBox(10);
                emailBox.getChildren().addAll(new Label("📧"), new Label(magasin.getEmailContact()));
                details.getChildren().add(emailBox);
            }

            if (magasin.getDescription() != null && !magasin.getDescription().isEmpty()) {
                Label descLabel = new Label("Description:");
                descLabel.setStyle("-fx-font-weight: bold;");
                TextArea descArea = new TextArea(magasin.getDescription());
                descArea.setEditable(false);
                descArea.setWrapText(true);
                descArea.setPrefRowCount(4);
                details.getChildren().addAll(descLabel, descArea);
            }

            HBox boutons = new HBox(15);
            boutons.setStyle("-fx-padding: 20 0 0 0;");

            Button btnContact = new Button("📞 Contacter");
            btnContact.setOnAction(e -> {
                showInfo("Contact", "Contacter " + magasin.getNomMagasin());
            });

            Button btnFermer = new Button("Fermer");
            btnFermer.setOnAction(e -> stage.close());

            boutons.getChildren().addAll(btnContact, btnFermer);

            content.getChildren().addAll(header, details, boutons);
            scrollPane.setContent(content);

            Scene scene = new Scene(scrollPane, 900, 700);
            stage.setScene(scene);
            stage.show();

            logger.info("✅ Fenêtre magasin simple créée");

        } catch (Exception e) {
            logger.error("❌ Erreur création fenêtre simple: {}", e.getMessage());
            afficherInfosMagasinSimple(magasin);
        }
    }

    private void afficherInfosMagasinSimple(Magasin magasin) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Magasin - " + magasin.getNomMagasin());
        alert.setHeaderText("Détails du Magasin");

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        Label nomLabel = new Label("🏪 " + magasin.getNomMagasin());
        nomLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label adresseLabel = new Label("📍 " + magasin.getAdresse());
        Label telLabel = new Label("📞 " + magasin.getTelephone());
        Label emailLabel = new Label("📧 " + magasin.getEmailContact());
        Label siteLabel = new Label("🌐 " + magasin.getSiteWeb());

        content.getChildren().addAll(nomLabel, adresseLabel, telLabel, emailLabel, siteLabel);
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String message, int vendeurId) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType contacterBtn = new ButtonType("Contacter le vendeur", ButtonBar.ButtonData.OK_DONE);
        ButtonType fermerBtn = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(contacterBtn, fermerBtn);

        alert.showAndWait().ifPresent(response -> {
            if (response == contacterBtn) {
                handleContact();
            }
        });
    }

    private Magasin getMagasinDetails(int vendeurId) {
        try {
            MagasinDAO magasinDAO = new MagasinDAO();
            return magasinDAO.getMagasinByVendeur(vendeurId);
        } catch (Exception e) {
            logger.error("❌ Erreur récupération magasin: {}", e.getMessage());
            return null;
        }
    }

    private int getVendeurIdFromDatabase(int articleId) {
        String sql = "SELECT id_vendeur FROM Article WHERE id_article = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, articleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int idVendeur = rs.getInt("id_vendeur");
                logger.info("✅ ID vendeur trouvé en BD: {}", idVendeur);
                return idVendeur;
            } else {
                logger.error("❌ Aucun article trouvé avec ID: {}", articleId);
            }
        } catch (SQLException e) {
            logger.error("❌ Erreur récupération ID vendeur: {}", e.getMessage());
        }
        return 0;
    }

    // ========== RENDEZ-VOUS ==========

    private boolean openRendezVousForm() {
        try {
            logger.info("🔧 Tentative d'ouverture du formulaire FXML...");

            String[] cheminsFXML = {
                    "/view/client/rendezvous-form.fxml",
                    "/com/example/vehiclegestion/view/client/rendezvous-form.fxml",
                    "/view/rendezvous-form.fxml"
            };

            FXMLLoader loader = null;
            Parent root = null;

            for (String chemin : cheminsFXML) {
                try {
                    loader = new FXMLLoader(getClass().getResource(chemin));
                    root = loader.load();
                    logger.info("✅ FXML chargé avec succès: {}", chemin);
                    break;
                } catch (Exception e) {
                    logger.debug("❌ Échec pour: {}", chemin);
                }
            }

            if (root == null) {
                logger.warn("❌ Aucun fichier FXML trouvé");
                return false;
            }

            RendezVousFormController controller = loader.getController();
            Vehicle vehicle = convertArticleToVehicle(article);
            controller.setVehicle(vehicle);

            controller.setOnRendezVousCreated(() -> {
                logger.info("✅ Rendez-vous créé pour: {}", article.getTitre());
                showSuccess("Succès", "Votre rendez-vous a été planifié avec succès !");
            });

            Stage stage = new Stage();
            stage.setTitle("Prendre un Rendez-vous - " + article.getTitre());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

            return true;

        } catch (Exception e) {
            logger.error("❌ Erreur ouverture formulaire FXML: {}", e.getMessage());
            return false;
        }
    }

    private void openRendezVousFormIntegre() {
        try {
            logger.info("🔧 Ouverture du formulaire intégré...");

            Stage stage = new Stage();
            stage.setTitle("Prendre un Rendez-vous - " + article.getTitre());

            VBox formContainer = new VBox(20);
            formContainer.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-border-radius: 10;");
            formContainer.setPrefSize(500, 600);

            Label titleLabel = new Label("📅 Prendre un Rendez-vous");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Label vehicleLabel = new Label("Véhicule: " + article.getTitre());
            vehicleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

            VBox form = new VBox(15);

            Label dateLabel = new Label("Date souhaitée:");
            DatePicker datePicker = new DatePicker();
            datePicker.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10;");

            Label timeLabel = new Label("Heure souhaitée:");
            ComboBox<String> timeCombo = new ComboBox<>();
            timeCombo.getItems().addAll("09:00", "10:00", "11:00", "14:00", "15:00", "16:00");
            timeCombo.setValue("10:00");
            timeCombo.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10;");

            Label typeLabel = new Label("Type de rendez-vous:");
            ComboBox<String> typeCombo = new ComboBox<>();
            typeCombo.getItems().addAll("Essai routier", "Consultation", "Visite");
            typeCombo.setValue("Essai routier");
            typeCombo.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10;");

            Label descLabel = new Label("Message (optionnel):");
            TextArea descArea = new TextArea();
            descArea.setPromptText("Précisez vos besoins...");
            descArea.setPrefRowCount(3);

            HBox buttons = new HBox(15);
            buttons.setAlignment(Pos.CENTER_RIGHT);

            Button cancelBtn = new Button("Annuler");
            cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10 20;");
            cancelBtn.setOnAction(e -> stage.close());

            Button confirmBtn = new Button("Confirmer");
            confirmBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 20;");
            confirmBtn.setOnAction(e -> {
                showSuccess("Succès", "Rendez-vous demandé pour le " +
                        datePicker.getValue() + " à " + timeCombo.getValue());
                stage.close();
            });

            buttons.getChildren().addAll(cancelBtn, confirmBtn);

            form.getChildren().addAll(dateLabel, datePicker, timeLabel, timeCombo,
                    typeLabel, typeCombo, descLabel, descArea, buttons);

            formContainer.getChildren().addAll(titleLabel, vehicleLabel, form);

            Scene scene = new Scene(formContainer);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            logger.error("❌ Erreur formulaire intégré: {}", e.getMessage());
            showError("Erreur", "Impossible d'ouvrir le formulaire de rendez-vous.");
        }
    }

    private Vehicle convertArticleToVehicle(Article article) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(article.getId());
        vehicle.setTitle(article.getTitre());
        vehicle.setDescription(article.getDescription());
        vehicle.setPrice(article.getPrix());
        vehicle.setCategory(article.getCategorie());
        vehicle.setState(article.getEtat());
        vehicle.setSellerId(article.getIdVendeur());

        if (article.getMarque() != null && article.getModele() != null) {
            vehicle.setTitle(article.getMarque() + " " + article.getModele());
        }

        logger.info("🔄 Conversion Article→Vehicle - Vendeur ID: {}", vehicle.getSellerId());
        return vehicle;
    }

    // ========== CHAT ==========

    private void openChatWindow(int vendeurId, int clientId, Article article) {
        try {
            logger.info("💬 Ouverture fenêtre chat - Vendeur: {}, Client: {}", vendeurId, clientId);

            String[] possiblePaths = {
                    "/com/example/vehiclegestion/view/common/ChatWindow.fxml",
                    "/view/common/ChatWindow.fxml",
                    "view/common/ChatWindow.fxml",
                    "/ChatWindow.fxml"
            };

            FXMLLoader loader = null;
            Parent chatRoot = null;

            for (String path : possiblePaths) {
                try {
                    URL url = getClass().getResource(path);
                    if (url != null) {
                        loader = new FXMLLoader(url);
                        chatRoot = loader.load();
                        logger.info("✅ FXML chargé: {}", path);
                        break;
                    }
                } catch (Exception e) {
                    logger.debug("❌ Échec pour: {}", path);
                }
            }

            if (chatRoot == null || loader == null) {
                logger.error("❌ Fichier ChatWindow.fxml introuvable");
                showError("Erreur", "Impossible de charger l'interface de chat");
                return;
            }

            ChatWindowController chatController = loader.getController();
            chatController.setFromVehicleDetails(true);

            ChatDAO chatDAO = new ChatDAO();
            Conversation conversation = chatDAO.getOrCreateConversationVendeurClient(
                    vendeurId,
                    clientId,
                    article.getId(),
                    "Discussion sur: " + article.getTitre()
            );

            if (conversation == null) {
                showError("Erreur", "Impossible de créer la conversation");
                return;
            }

            logger.info("✅ Conversation ID: {}", conversation.getIdConversation());

            Stage chatStage = new Stage();
            chatStage.setTitle("Chat avec le vendeur - " + article.getTitre());
            chatStage.setScene(new Scene(chatRoot, 1000, 700));
            chatStage.setMinWidth(800);
            chatStage.setMinHeight(600);

            chatStage.setOnShown(e -> {
                Platform.runLater(() -> {
                    chatController.openSpecificConversation(conversation.getIdConversation());
                });
            });

            chatStage.setOnCloseRequest(e -> {
                if (chatController != null) {
                    chatController.cleanup();
                }
            });

            chatStage.show();
            logger.info("✅ Fenêtre de chat ouverte");

        } catch (Exception e) {
            logger.error("❌ Erreur ouverture chat: {}", e.getMessage());
            showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    // ========== UTILITAIRES ==========

    private String getRandomCity() {
        String[] cities = {"Tanger", "Casablanca", "Marrakech", "Rabat", "Fès"};
        return cities[(int)(Math.random() * cities.length)];
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
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

    public void setArticle(Article article) {
        this.article = article;
        this.articleCharge = (article != null);
        displayArticleDetails();
        loadCommentaires();
    }

    public void testCommentaireSystem() {
        logger.info("🧪 Test du système de commentaires");
    }
}