package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.vendeur.model.Article;
import com.example.vehiclegestion.vendeur.model.Commentaire;
import com.example.vehiclegestion.vendeur.dao.CommentaireDAO;
import com.example.vehiclegestion.utils.DataReceiver;
import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.client.model.Vehicle;
import com.example.vehiclegestion.vendeur.controller.MagasinDetailsController;
import com.example.vehiclegestion.vendeur.model.Magasin;
import com.example.vehiclegestion.vendeur.dao.MagasinDAO;
import com.example.vehiclegestion.common.dao.ChatDAO;
import com.example.vehiclegestion.common.model.Conversation;
import com.example.vehiclegestion.common.controller.ChatWindowController;
import com.example.vehiclegestion.utils.DatabaseConnection;

// âœ… IMPORTS POUR LE LOGGING
import com.example.vehiclegestion.logging.util.LoggerUtil;
import com.example.vehiclegestion.logging.service.ElasticLogService;

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

public class VehiDetaiCo implements DataReceiver {

    // âœ… SERVICE DE LOGGING ELASTICSEARCH
    private final ElasticLogService elasticLogger = new ElasticLogService();

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
    private VBox commentairesSection;
    @FXML
    private VBox sellerSection;
    @FXML
    private Button contactBtn;
    @FXML
    private Button backBtn;
    @FXML
    private Button visiterMagasinBtn;

    private Article article;
    private CommentaireDAO commentaireDAO = new CommentaireDAO();
    private boolean articleCharge = false;

    @Override
    public void receiveData(Object data) {
        LoggerUtil.info(VehiDetaiCo.class, "=== DÃ‰BUT receiveData ===");
        elasticLogger.sendLog("INFO", "VehiDetaiCo - receiveData appelÃ©");

        if (data instanceof Article) {
            this.article = (Article) data;
            this.articleCharge = true;

            LoggerUtil.info(VehiDetaiCo.class, "Article chargÃ© avec succÃ¨s",
                    "ID=" + article.getId(),
                    "Titre=" + article.getTitre());
            elasticLogger.sendLog("INFO",
                    "Article chargÃ© - ID: " + article.getId() + ", Titre: " + article.getTitre());

            displayArticleDetails();
            loadCommentaires();

            LoggerUtil.debug(VehiDetaiCo.class, "receiveData complÃ©tÃ© avec succÃ¨s");
        } else {
            LoggerUtil.error(VehiDetaiCo.class, "DonnÃ©es reÃ§ues invalides",
                    "Type=" + (data != null ? data.getClass().getName() : "null"));
            elasticLogger.sendLog("ERROR", "receiveData - DonnÃ©es invalides");

            this.articleCharge = false;
            showError("Erreur", "Impossible de charger les donnÃ©es du vÃ©hicule");
        }
    }

    @FXML
    private void initialize() {
        LoggerUtil.info(VehiDetaiCo.class, "Initialisation du contrÃ´leur VehicleDetail");
        elasticLogger.sendLog("INFO", "VehiDetaiCo - Initialisation");

        try {
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: #f5f5f5;");

            if (backBtn != null) {
                backBtn.setOnAction(e -> goBack());
                backBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
                LoggerUtil.debug(VehiDetaiCo.class, "Bouton 'Retour' configurÃ©");
            }

            if (contactBtn != null) {
                contactBtn.setOnAction(e -> handleContact());
                contactBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                        "-fx-padding: 12 30; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
                LoggerUtil.debug(VehiDetaiCo.class, "Bouton 'Contact' configurÃ©");
            }

            if (visiterMagasinBtn != null) {
                visiterMagasinBtn.setOnAction(e -> handleVisiterMagasin());
                visiterMagasinBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-background-radius: 8; " +
                        "-fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 14px;");
                LoggerUtil.debug(VehiDetaiCo.class, "Bouton 'Visiter Magasin' configurÃ©");
            }

            LoggerUtil.info(VehiDetaiCo.class, "Initialisation terminÃ©e avec succÃ¨s");
            elasticLogger.sendLog("INFO", "VehiDetaiCo - Initialisation complÃ©tÃ©e");

        } catch (Exception e) {
            LoggerUtil.error(VehiDetaiCo.class, "Erreur lors de l'initialisation",
                    "Message=" + e.getMessage());
            elasticLogger.sendLog("ERROR",
                    "Erreur initialisation VehiDetaiCo: " + e.getMessage());
        }
    }

    private void goBack() {
        LoggerUtil.info(VehiDetaiCo.class, "Action: Retour Ã  la liste");
        elasticLogger.sendLog("INFO", "VehiDetaiCo - Retour arriÃ¨re");

        try {
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.close();
            LoggerUtil.debug(VehiDetaiCo.class, "FenÃªtre fermÃ©e avec succÃ¨s");
        } catch (Exception e) {
            LoggerUtil.error(VehiDetaiCo.class, "Erreur lors de la fermeture",
                    "Message=" + e.getMessage());
        }
    }

    private void displayArticleDetails() {
        LoggerUtil.info(VehiDetaiCo.class, "=== DÃ‰BUT displayArticleDetails ===");

        if (!articleCharge || article == null) {
            LoggerUtil.error(VehiDetaiCo.class, "Impossible d'afficher: article non chargÃ©");
            elasticLogger.sendLog("ERROR", "displayArticleDetails - Article null");
            return;
        }

        try {
            LoggerUtil.debug(VehiDetaiCo.class, "Affichage des dÃ©tails",
                    "ArticleID=" + article.getId(),
                    "Titre=" + article.getTitre());

            loadMainImage();

            titleLabel.setText(article.getTitre());
            priceLabel.setText(String.format("%,.0f DH", article.getPrix()));
            locationLabel.setText("ðŸ“ " + getRandomCity());
            dateLabel.setText("PubliÃ©e le " + LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy")));

            displayCharacteristics();
            displayDescription();
            displaySellerSection();

            LoggerUtil.info(VehiDetaiCo.class, "DÃ©tails de l'article affichÃ©s avec succÃ¨s");
            elasticLogger.sendLog("INFO",
                    "Article affichÃ© - ID: " + article.getId() + ", Titre: " + article.getTitre());

        } catch (Exception e) {
            LoggerUtil.error(VehiDetaiCo.class, "Erreur affichage dÃ©tails article",
                    "Message=" + e.getMessage());
            elasticLogger.sendLog("ERROR",
                    "Erreur displayArticleDetails: " + e.getMessage());
        }
    }

    private void loadMainImage() {
        LoggerUtil.debug(VehiDetaiCo.class, "Chargement de l'image principale");

        if (article.getImage() != null && !article.getImage().isEmpty()) {
            try {
                File file = new File(article.getImage());
                Image image = new Image(file.toURI().toString(), true);
                mainImageView.setImage(image);
                mainImageView.setFitWidth(650);
                mainImageView.setFitHeight(450);
                mainImageView.setPreserveRatio(true);

                LoggerUtil.debug(VehiDetaiCo.class, "Image chargÃ©e",
                        "Chemin=" + article.getImage());
            } catch (Exception e) {
                LoggerUtil.warn(VehiDetaiCo.class, "Erreur chargement image, utilisation fallback",
                        "Chemin=" + article.getImage());
                setFallbackImage();
            }
        } else {
            LoggerUtil.debug(VehiDetaiCo.class, "Aucune image disponible, utilisation placeholder");
            setFallbackImage();
        }
    }

    private void setFallbackImage() {
        LoggerUtil.debug(VehiDetaiCo.class, "Affichage image placeholder");

        StackPane placeholder = new StackPane();
        placeholder.setStyle("-fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%); " +
                "-fx-background-radius: 8;");
        placeholder.setPrefSize(650, 450);
        Label icon = new Label("ðŸš—");
        icon.setStyle("-fx-font-size: 80px;");
        placeholder.getChildren().add(icon);

        if (mainImageView.getParent() instanceof Pane) {
            Pane parent = (Pane) mainImageView.getParent();
            parent.getChildren().remove(mainImageView);
            parent.getChildren().add(placeholder);
        }
    }

    private void displayCharacteristics() {
        LoggerUtil.debug(VehiDetaiCo.class, "Affichage des caractÃ©ristiques");

        characteristicsContainer.getChildren().clear();
        characteristicsContainer.setStyle("-fx-spacing: 12; -fx-padding: 20; -fx-background-color: white; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label sectionTitle = new Label("CaractÃ©ristiques");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");
        characteristicsContainer.getChildren().add(sectionTitle);

        addCharacteristic("ðŸ“… AnnÃ©e-ModÃ¨le", String.valueOf(article.getAnnee()));
        addCharacteristic("ðŸ“ KilomÃ©trage", String.valueOf(article.getKilometrage()) + " km");
        if (article.getTransmission() != null)
            addCharacteristic("âš™ï¸ BoÃ®te de vitesses", article.getTransmission());
        if (article.getCarburant() != null)
            addCharacteristic("â›½ Type de carburant", article.getCarburant());
        if (article.getMarque() != null)
            addCharacteristic("ðŸš— Marque", article.getMarque());
        if (article.getModele() != null)
            addCharacteristic("ðŸ·ï¸ ModÃ¨le", article.getModele());
        if (article.getPuissance() > 0)
            addCharacteristic("ðŸ”‹ Puissance", article.getPuissance() + " ch");
        addCharacteristic("ðŸŒ Origine", "Maroc");
        if (article.getEtat() != null)
            addCharacteristic("â­ Ã‰tat", article.getEtat());
        if (article.getCategorie() != null)
            addCharacteristic("ðŸ“‚ CatÃ©gorie", article.getCategorie());

        LoggerUtil.debug(VehiDetaiCo.class, "CaractÃ©ristiques affichÃ©es");
    }

    private void addCharacteristic(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8 0; -fx-border-color: #f0f0f0; -fx-border-width: 0 0 1 0;");

        Label labelField = new Label(label);
        labelField.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");
        labelField.setPrefWidth(200);

        Label valueField = new Label(value != null ? value : "Non spÃ©cifiÃ©");
        valueField.setStyle("-fx-text-fill: #333; -fx-font-weight: bold; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(labelField, spacer, valueField);
        characteristicsContainer.getChildren().add(row);
    }

    private void displayDescription() {
        LoggerUtil.debug(VehiDetaiCo.class, "Affichage de la description");

        descriptionLabel.setText(article.getDescription() != null && !article.getDescription().isEmpty()
                ? article.getDescription()
                : "VÃ©hicule en excellent Ã©tat, bien entretenu. Toutes les rÃ©visions effectuÃ©es Ã  temps. " +
                "VÃ©hicule non fumeur. Disponible pour essai routier.");
        descriptionLabel.setWrapText(true);
    }

    private void loadCommentaires() {
        LoggerUtil.info(VehiDetaiCo.class, "=== DÃ‰BUT chargement commentaires ===");
        elasticLogger.sendLog("INFO", "Chargement commentaires - Article ID: " +
                (article != null ? article.getId() : "null"));

        if (!articleCharge || article == null) {
            LoggerUtil.error(VehiDetaiCo.class, "Article non chargÃ© pour les commentaires");
            elasticLogger.sendLog("ERROR", "loadCommentaires - Article null");
            return;
        }

        try {
            commentairesSection.getChildren().clear();
            commentairesSection.setStyle("-fx-spacing: 15; -fx-padding: 20; -fx-background-color: white; " +
                    "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

            HBox header = createCommentaireHeader();
            commentairesSection.getChildren().add(header);

            boolean estConnecte = SessionManager.getInstance().estConnecte();
            LoggerUtil.debug(VehiDetaiCo.class, "Session connectÃ©e", "Status=" + estConnecte);

            if (estConnecte) {
                int userId = SessionManager.getInstance().getUserId();
                boolean dejaCommente = commentaireDAO.aDejaCommente(userId, article.getId());

                LoggerUtil.debug(VehiDetaiCo.class, "VÃ©rification commentaire utilisateur",
                        "UserID=" + userId,
                        "DÃ©jÃ CommentÃ©=" + dejaCommente);

                if (dejaCommente) {
                    Commentaire monCommentaire = commentaireDAO.getCommentaireUtilisateur(
                            userId, article.getId());
                    if (monCommentaire != null) {
                        VBox myCommentCard = createMyCommentCard(monCommentaire);
                        commentairesSection.getChildren().add(myCommentCard);
                        LoggerUtil.debug(VehiDetaiCo.class, "Commentaire utilisateur affichÃ©");
                    } else {
                        VBox alreadyCommented = createAlreadyCommentedMessage();
                        commentairesSection.getChildren().add(alreadyCommented);
                    }
                } else {
                    VBox addCommentForm = createAddCommentForm();
                    commentairesSection.getChildren().add(addCommentForm);
                    LoggerUtil.debug(VehiDetaiCo.class, "Formulaire commentaire affichÃ©");
                }
            } else {
                VBox loginPrompt = createLoginPrompt();
                commentairesSection.getChildren().add(loginPrompt);
                LoggerUtil.debug(VehiDetaiCo.class, "Prompt de connexion affichÃ©");
            }

            commentairesSection.getChildren().add(new Separator());

            List<Commentaire> commentaires = commentaireDAO.getCommentairesByArticle(article.getId());
            LoggerUtil.info(VehiDetaiCo.class, "Commentaires rÃ©cupÃ©rÃ©s",
                    "Nombre=" + commentaires.size());
            elasticLogger.sendLog("INFO",
                    "Commentaires chargÃ©s - Nombre: " + commentaires.size() +
                            " pour Article ID: " + article.getId());

            if (commentaires.isEmpty()) {
                Label noComments = new Label(
                        "Aucun commentaire pour le moment. Soyez le premier Ã  donner votre avis !");
                noComments.setStyle("-fx-text-fill: #999; -fx-font-style: italic; -fx-padding: 20 0;");
                commentairesSection.getChildren().add(noComments);
            } else {
                for (Commentaire c : commentaires) {
                    VBox commentCard = createCommentCard(c);
                    commentairesSection.getChildren().add(commentCard);
                }
            }

            LoggerUtil.info(VehiDetaiCo.class, "Chargement commentaires terminÃ© avec succÃ¨s");

        } catch (Exception e) {
            LoggerUtil.error(VehiDetaiCo.class, "Erreur lors du chargement des commentaires",
                    "Message=" + e.getMessage());
            elasticLogger.sendLog("ERROR",
                    "Erreur loadCommentaires: " + e.getMessage());
        }
    }

    private VBox createMyCommentCard(Commentaire commentaire) {
        LoggerUtil.debug(VehiDetaiCo.class, "CrÃ©ation carte 'Mon Commentaire'");

        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 20; -fx-background-radius: 8; " +
                "-fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8;");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("âœ“");
        icon.setStyle("-fx-font-size: 24px; -fx-text-fill: #4CAF50;");

        Label title = new Label("Votre avis");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        header.getChildren().addAll(icon, title);

        HBox ratingBox = new HBox(5);
        ratingBox.setAlignment(Pos.CENTER_LEFT);

        int noteEntiere = (int) Math.round(commentaire.getNote());
        for (int i = 0; i < 5; i++) {
            Label star = new Label(i < noteEntiere ? "â­" : "â˜†");
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

        Label dateLabel = new Label("PubliÃ© " + formatDate(commentaire.getDateCommentaire()));
        dateLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 12px; -fx-font-style: italic;");

        card.getChildren().addAll(header, noteContainer, commentText, dateLabel);
        return card;
    }

    private HBox createCommentaireHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("ðŸ’¬ Avis des utilisateurs");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #333;");

        double avgNote = commentaireDAO.getNoteMoyenne(article.getId());
        int totalComments = commentaireDAO.getNombreCommentaires(article.getId());

        if (totalComments > 0) {
            HBox stats = new HBox(8);
            stats.setAlignment(Pos.CENTER_LEFT);
            stats.setStyle("-fx-background-color: #FFF8E1; -fx-padding: 8 15; -fx-background-radius: 20;");

            Label starLabel = new Label("â­");
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
            ToggleButton star = new ToggleButton("â­");
            star.setUserData(i);
            star.setToggleGroup(ratingGroup);
            star.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand;");
            star.selectedProperty().addListener((obs, old, selected) -> {
                if (selected) {
                    star.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; " +
                            "-fx-cursor: hand; -fx-text-fill: #FFB300;");
                } else {
                    star.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand;");
                }
            });
            stars.getChildren().add(star);
        }
        ratingBox.getChildren().addAll(ratingLabel, stars);

        TextArea commentText = new TextArea();
        commentText.setPromptText("Partagez votre expÃ©rience avec ce vÃ©hicule...");
        commentText.setPrefRowCount(3);
        commentText.setWrapText(true);

        Button submitBtn = new Button("âœ“ Publier mon avis");
        submitBtn.setStyle("-fx-background-color: #0066FF; -fx-text-fill: white; " +
                "-fx-padding: 10 25; -fx-background-radius: 6; -fx-font-weight: bold; -fx-cursor: hand;");
        submitBtn.setOnAction(e -> {
            LoggerUtil.info(VehiDetaiCo.class, "Tentative de publication d'avis");

            if (!articleCharge || article == null) {
                LoggerUtil.error(VehiDetaiCo.class, "Impossible d'ajouter commentaire - Article null");
                showError("Erreur", "Article non chargÃ© - Impossible d'ajouter un commentaire");
                return;
            }

            ToggleButton selectedStar = (ToggleButton) ratingGroup.getSelectedToggle();
            if (selectedStar == null) {
                LoggerUtil.warn(VehiDetaiCo.class, "Validation Ã©chouÃ©e - Aucune note sÃ©lectionnÃ©e");
                showWarning("Attention", "Veuillez sÃ©lectionner une note");
                return;
            }
            if (commentText.getText().trim().isEmpty()) {
                LoggerUtil.warn(VehiDetaiCo.class, "Validation Ã©chouÃ©e - Commentaire vide");
                showWarning("Attention", "Veuillez Ã©crire un commentaire");
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

        Label icon = new Label("âœ“");
        icon.setStyle("-fx-font-size: 32px; -fx-text-fill: #4CAF50;");

        Label text = new Label("Vous avez dÃ©jÃ  laissÃ© un avis pour cet article");
        text.setStyle("-fx-font-size: 14px; -fx-text-fill: #2E7D32; -fx-font-weight: bold;");

        message.getChildren().addAll(icon, text);
        return message;
    }

    private VBox createLoginPrompt() {
        VBox prompt = new VBox(10);
        prompt.setAlignment(Pos.CENTER);
        prompt.setStyle("-fx-background-color: #F0F4FF; -fx-padding: 25; -fx-background-radius: 8;");

        Label icon = new Label("ðŸ”’");
        icon.setStyle("-fx-font-size: 32px;");

        Label message = new Label("Connectez-vous pour laisser un avis");
        message.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Button loginBtn = new Button("Se connecter");
        loginBtn.setStyle("-fx-background-color: #0066FF; -fx-text-fill: white; " +
                "-fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        loginBtn.setOnAction(e -> {
            LoggerUtil.info(VehiDetaiCo.class, "Redirection vers page de connexion");
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/auth/login.fxml"));
                Parent root = loader.load();
                Stage loginStage = new Stage();
                loginStage.setTitle("Connexion");
                loginStage.setScene(new Scene(root, 400, 500));
                loginStage.show();
            } catch (IOException ex) {
                LoggerUtil.error(VehiDetaiCo.class, "Erreur ouverture login",
                        "Message=" + ex.getMessage());
            }
        });

        prompt.getChildren().addAll(icon, message, loginBtn);
        return prompt;
    }

    // âœ… SUITE DU FICHIER VehiDetaiCo.java

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
            Label star = new Label(i < noteEntiere ? "â­" : "â˜†");
            star.setStyle("-fx-text-fill: #FFB300;");
            noteBox.getChildren().add(star);
        }

        Label dateLabel = new Label("â€¢ " + formatDate(c.getDateCommentaire()));
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
        if (nomClient.contains("ðŸª")) return "#FF9800";
        if (nomClient.contains("â­")) return "#F44336";
        return "#2196F3";
    }

    private void saveCommentaire(int note, String texte) {
        LoggerUtil.info(VehiDetaiCo.class, "=== DÃ‰BUT saveCommentaire ===");
        elasticLogger.sendLog("INFO", "Tentative d'ajout commentaire - Note: " + note);

        if (!articleCharge || article == null) {
            LoggerUtil.error(VehiDetaiCo.class, "ERREUR CRITIQUE - Article non chargÃ©");
            elasticLogger.sendLog("ERROR", "saveCommentaire - Article null");
            showError("Erreur", "Article non chargÃ© - Veuillez rÃ©essayer");
            return;
        }

        SessionManager session = SessionManager.getInstance();
        if (!session.estConnecte()) {
            LoggerUtil.error(VehiDetaiCo.class, "Session non active");
            elasticLogger.sendLog("ERROR", "saveCommentaire - Utilisateur non connectÃ©");
            showError("Erreur", "Vous devez Ãªtre connectÃ©");
            return;
        }

        int idUtilisateur = session.getUserId();
        LoggerUtil.info(VehiDetaiCo.class, "DonnÃ©es commentaire",
                "UserID=" + idUtilisateur,
                "ArticleID=" + article.getId(),
                "Note=" + note);

        boolean success = commentaireDAO.ajouterCommentaire(idUtilisateur, article.getId(), note, texte);

        if (success) {
            LoggerUtil.info(VehiDetaiCo.class, "Commentaire sauvegardÃ© avec succÃ¨s",
                    "UserID=" + idUtilisateur,
                    "ArticleID=" + article.getId());
            elasticLogger.sendLog("INFO",
                    "Commentaire ajoutÃ© - User: " + idUtilisateur +
                            ", Article: " + article.getId() + ", Note: " + note);

            showSuccess("SuccÃ¨s", "Votre avis a Ã©tÃ© publiÃ© avec succÃ¨s !");
            loadCommentaires();
        } else {
            LoggerUtil.error(VehiDetaiCo.class, "Ã‰chec sauvegarde commentaire");
            elasticLogger.sendLog("ERROR",
                    "Ã‰chec ajout commentaire - User: " + idUtilisateur +
                            ", Article: " + article.getId());
            showError("Erreur", "Impossible de publier votre avis. Veuillez rÃ©essayer.");
        }
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

    private void displaySellerSection() {
        LoggerUtil.debug(VehiDetaiCo.class, "Affichage section vendeur");

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
        Label sellerBadge = new Label("â­ Membre depuis 2020");
        sellerBadge.setStyle("-fx-text-fill: #FF9800;");
        sellerInfo.getChildren().addAll(sellerName, sellerBadge);
        sellerHeader.getChildren().addAll(avatar, sellerInfo);

        VBox warningBox = new VBox(8);
        warningBox.setStyle("-fx-background-color: #FFF3E0; -fx-padding: 12; -fx-background-radius: 6;");
        Label warningIcon = new Label("âš ï¸ Important");
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

        Button rendezvousBtn = new Button("ðŸ“… Prendre rendez-vous");
        rendezvousBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-font-size: 14px;");
        rendezvousBtn.setMaxWidth(Double.MAX_VALUE);
        rendezvousBtn.setOnAction(e -> handleRendezvous());

        Button chatBtn = new Button("ðŸ’¬ Contacter le Vendeur");
        chatBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-font-size: 14px;");
        chatBtn.setMaxWidth(Double.MAX_VALUE);
        chatBtn.setOnAction(e -> handleContact());

        Button callBtn = new Button("ðŸ“ž Appeler le Vendeur");
        callBtn.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-font-size: 14px;");
        callBtn.setMaxWidth(Double.MAX_VALUE);
        callBtn.setOnAction(e -> {
            LoggerUtil.info(VehiDetaiCo.class, "Action: Appeler le vendeur");
            showInfo("Appel", "NumÃ©ro: +212 6XX XXX XXX\n(FonctionnalitÃ© en dÃ©veloppement)");
        });

        Button magasinBtn = new Button("ðŸª Visiter Magasin");
        magasinBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; " +
                "-fx-padding: 12 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand; " +
                "-fx-font-size: 14px;");
        magasinBtn.setMaxWidth(Double.MAX_VALUE);
        magasinBtn.setOnAction(e -> handleVisiterMagasin());

        buttonsContainer.getChildren().addAll(rendezvousBtn, chatBtn, callBtn, magasinBtn);
        return buttonsContainer;
    }

    @FXML
    private void handleVisiterMagasin() {
        LoggerUtil.info(VehiDetaiCo.class, "=== DÃ‰BUT handleVisiterMagasin ===");
        elasticLogger.sendLog("INFO", "Action: Visiter magasin - Article ID: " +
                (article != null ? article.getId() : "null"));

        if (article == null) {
            LoggerUtil.error(VehiDetaiCo.class, "Aucun vÃ©hicule sÃ©lectionnÃ©");
            showError("Erreur", "Aucun vÃ©hicule sÃ©lectionnÃ©");
            return;
        }

        int vendeurId = getVendeurIdFromDatabase(article.getId());
        LoggerUtil.info(VehiDetaiCo.class, "Vendeur ID rÃ©cupÃ©rÃ©", "VendeurID=" + vendeurId);

        if (vendeurId <= 0) {
            LoggerUtil.error(VehiDetaiCo.class, "Impossible de trouver le vendeur");
            elasticLogger.sendLog("ERROR", "handleVisiterMagasin - Vendeur non trouvÃ©");
            showError("Erreur", "Informations du magasin non disponibles");
            return;
        }

        Magasin magasin = getMagasinDetails(vendeurId);

        if (magasin == null) {
            LoggerUtil.warn(VehiDetaiCo.class, "Magasin non configurÃ©", "VendeurID=" + vendeurId);
            elasticLogger.sendLog("WARN", "Magasin non configurÃ© pour vendeur ID: " + vendeurId);
            showInfo("Magasin", "Ce vendeur n'a pas encore configurÃ© son magasin.\n\n" +
                    "Souhaitez-vous le contacter directement ?", vendeurId);
            return;
        }

        LoggerUtil.info(VehiDetaiCo.class, "Ouverture page magasin",
                "MagasinNom=" + magasin.getNomMagasin());
        elasticLogger.sendLog("INFO", "Ouverture magasin: " + magasin.getNomMagasin());

        ouvrirFenetreMagasin(magasin);
    }

    private void ouvrirFenetreMagasin(Magasin magasin) {
        try {
            LoggerUtil.info(VehiDetaiCo.class, "Tentative ouverture fenÃªtre magasin",
                    "Magasin=" + magasin.getNomMagasin());

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
                    LoggerUtil.debug(VehiDetaiCo.class, "Essai chemin FXML", "Chemin=" + chemin);
                    URL url = getClass().getResource(chemin);
                    if (url != null) {
                        loader = new FXMLLoader(url);
                        root = loader.load();
                        cheminTrouve = chemin;
                        LoggerUtil.info(VehiDetaiCo.class, "FXML chargÃ©", "Chemin=" + chemin);
                        break;
                    }
                } catch (Exception e) {
                    LoggerUtil.debug(VehiDetaiCo.class, "Ã‰chec chemin", "Chemin=" + chemin);
                }
            }

            if (root == null) {
                LoggerUtil.warn(VehiDetaiCo.class, "FXML non trouvÃ©, crÃ©ation fenÃªtre simple");
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

            LoggerUtil.info(VehiDetaiCo.class, "FenÃªtre magasin ouverte",
                    "Chemin=" + cheminTrouve);
            elasticLogger.sendLog("INFO",
                    "FenÃªtre magasin ouverte: " + magasin.getNomMagasin());

        } catch (Exception e) {
            LoggerUtil.error(VehiDetaiCo.class, "Erreur ouverture fenÃªtre magasin",
                    "Message=" + e.getMessage());
            elasticLogger.sendLog("ERROR",
                    "Erreur ouverture magasin: " + e.getMessage());
            creerFenetreMagasinSimple(magasin);
        }
    }

    private void creerFenetreMagasinSimple(Magasin magasin) {
        try {
            LoggerUtil.info(VehiDetaiCo.class, "CrÃ©ation fenÃªtre magasin simple");

            Stage stage = new Stage();
            stage.setTitle("Magasin - " + magasin.getNomMagasin());

            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setFitToWidth(true);
            scrollPane.setStyle("-fx-background-color: #f5f7fa;");

            VBox content = new VBox(20);
            content.setStyle("-fx-padding: 30;");

            // Header
            HBox header = new HBox(20);
            header.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12;");

            StackPane logoPlaceholder = new StackPane();
            logoPlaceholder.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 8; " +
                    "-fx-min-width: 100; -fx-min-height: 100;");
            Label logoLabel = new Label("ðŸª");
            logoLabel.setStyle("-fx-font-size: 40px;");
            logoPlaceholder.getChildren().add(logoLabel);

            VBox infos = new VBox(10);
            Label nomLabel = new Label(magasin.getNomMagasin());
            nomLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

            Label categorieLabel = new Label(magasin.getCategorie());
            categorieLabel.setStyle("-fx-text-fill: #666;");

            infos.getChildren().addAll(nomLabel, categorieLabel);
            header.getChildren().addAll(logoPlaceholder, infos);

            // DÃ©tails
            VBox details = new VBox(15);
            details.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12;");

            if (magasin.getAdresse() != null && !magasin.getAdresse().isEmpty()) {
                HBox adresseBox = new HBox(10);
                adresseBox.getChildren().addAll(new Label("ðŸ“"), new Label(magasin.getAdresse()));
                details.getChildren().add(adresseBox);
            }

            if (magasin.getTelephone() != null && !magasin.getTelephone().isEmpty()) {
                HBox telBox = new HBox(10);
                telBox.getChildren().addAll(new Label("ðŸ“ž"), new Label(magasin.getTelephone()));
                details.getChildren().add(telBox);
            }

            if (magasin.getEmailContact() != null && !magasin.getEmailContact().isEmpty()) {
                HBox emailBox = new HBox(10);
                emailBox.getChildren().addAll(new Label("ðŸ“§"), new Label(magasin.getEmailContact()));
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

            // Boutons
            HBox boutons = new HBox(15);
            boutons.setStyle("-fx-padding: 20 0 0 0;");

            Button btnContact = new Button("ðŸ“ž Contacter");
            btnContact.setOnAction(e -> {
                LoggerUtil.info(VehiDetaiCo.class, "Contact magasin",
                        "Magasin=" + magasin.getNomMagasin());
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

            LoggerUtil.info(VehiDetaiCo.class, "FenÃªtre magasin simple crÃ©Ã©e avec succÃ¨s");
            elasticLogger.sendLog("INFO", "FenÃªtre magasin simple crÃ©Ã©e");

        } catch (Exception e) {
            LoggerUtil.error(VehiDetaiCo.class, "Erreur crÃ©ation fenÃªtre simple",
                    "Message=" + e.getMessage());
            afficherInfosMagasinSimple(magasin);
        }
    }

    private void afficherInfosMagasinSimple(Magasin magasin) {
        LoggerUtil.debug(VehiDetaiCo.class, "Affichage infos magasin via Alert");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Magasin - " + magasin.getNomMagasin());
        alert.setHeaderText("DÃ©tails du Magasin");

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        Label nomLabel = new Label("ðŸª " + magasin.getNomMagasin());
        nomLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label adresseLabel = new Label("ðŸ“ " + magasin.getAdresse());
        Label telLabel = new Label("ðŸ“ž " + magasin.getTelephone());
        Label emailLabel = new Label("ðŸ“§ " + magasin.getEmailContact());
        Label siteLabel = new Label("ðŸŒ " + magasin.getSiteWeb());

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
            LoggerUtil.debug(VehiDetaiCo.class, "RÃ©cupÃ©ration dÃ©tails magasin",
                    "VendeurID=" + vendeurId);

            MagasinDAO magasinDAO = new MagasinDAO();
            Magasin magasin = magasinDAO.getMagasinByVendeur(vendeurId);

            if (magasin != null) {
                LoggerUtil.info(VehiDetaiCo.class, "Magasin trouvÃ©",
                        "Nom=" + magasin.getNomMagasin());
            } else {
                LoggerUtil.warn(VehiDetaiCo.class, "Aucun magasin trouvÃ©",
                        "VendeurID=" + vendeurId);
            }

            return magasin;
        } catch (Exception e) {
            LoggerUtil.error(VehiDetaiCo.class, "Erreur rÃ©cupÃ©ration magasin",
                    "VendeurID=" + vendeurId,
                    "Message=" + e.getMessage());
            elasticLogger.sendLog("ERROR",
                    "Erreur getMagasinDetails pour vendeur ID: " + vendeurId +
                            " - " + e.getMessage());
            return null;
        }
    }

    private int getVendeurIdFromDatabase(int articleId) {
        LoggerUtil.debug(VehiDetaiCo.class, "RÃ©cupÃ©ration ID vendeur", "ArticleID=" + articleId);

        String sql = "SELECT id_vendeur FROM Article WHERE id_article = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, articleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int idVendeur = rs.getInt("id_vendeur");
                LoggerUtil.info(VehiDetaiCo.class, "ID vendeur trouvÃ©",
                        "ArticleID=" + articleId,
                        "VendeurID=" + idVendeur);
                elasticLogger.sendLog("INFO",
                        "Vendeur ID trouvÃ©: " + idVendeur + " pour article: " + articleId);
                return idVendeur;
            } else {
                LoggerUtil.warn(VehiDetaiCo.class, "Aucun vendeur trouvÃ©",
                        "ArticleID=" + articleId);
                elasticLogger.sendLog("WARN", "Aucun vendeur pour article ID: " + articleId);
            }
        } catch (SQLException e) {
            LoggerUtil.error(VehiDetaiCo.class, "Erreur SQL rÃ©cupÃ©ration vendeur",
                    "ArticleID=" + articleId,
                    "Message=" + e.getMessage());
            elasticLogger.sendLog("ERROR",
                    "Erreur SQL getVendeurId: " + e.getMessage());
        }
        return 0;
    }

    @FXML
    private void handleRendezvous() {
        LoggerUtil.info(VehiDetaiCo.class, "=== DÃ‰BUT handleRendezvous ===");
        elasticLogger.sendLog("INFO", "Action: Prendre rendez-vous");

        if (article == null) {
            LoggerUtil.error(VehiDetaiCo.class, "Aucun vÃ©hicule sÃ©lectionnÃ©");
            showError("Erreur", "Aucun vÃ©hicule sÃ©lectionnÃ©");
            return;
        }

        int idVendeur = getVendeurIdFromDatabase(article.getId());
        LoggerUtil.info(VehiDetaiCo.class, "DonnÃ©es rendez-vous",
                "ArticleID=" + article.getId(),
                "VendeurID=" + idVendeur);

        if (idVendeur <= 0) {
            LoggerUtil.error(VehiDetaiCo.class, "Vendeur non trouvÃ©");
            elasticLogger.sendLog("ERROR", "handleRendezvous - Vendeur non trouvÃ©");
            showError("Erreur", "Impossible de trouver le vendeur de ce vÃ©hicule");
            return;
        }

        article.setIdVendeur(idVendeur);
        Vehicle vehicle = convertArticleToVehicle(article);
        LoggerUtil.info(VehiDetaiCo.class, "Vehicle crÃ©Ã©", "SellerID=" + vehicle.getSellerId());

        boolean formulaireOuvert = openRendezVousForm();
        if (!formulaireOuvert) {
            LoggerUtil.debug(VehiDetaiCo.class, "Ouverture formulaire intÃ©grÃ©");
            openRendezVousFormIntegre();
        }
    }
    private boolean openRendezVousForm() {
        LoggerUtil.info(VehiDetaiCo.class, "=== TENTATIVE OUVERTURE FORMULAIRE RENDEZ-VOUS ===");
        elasticLogger.sendLog("INFO", "Tentative ouverture formulaire rendez-vous");

        try {
            String[] cheminsFXML = {
                    "/view/client/rendezvous-form.fxml",
                    "/com/example/vehiclegestion/view/client/rendezvous-form.fxml",
                    "/view/rendezvous-form.fxml"
            };

            FXMLLoader loader = null;
            Parent root = null;
            String cheminTrouve = null;

            for (String chemin : cheminsFXML) {
                try {
                    LoggerUtil.debug(VehiDetaiCo.class, "Essai chemin FXML", "Chemin=" + chemin);
                    loader = new FXMLLoader(getClass().getResource(chemin));
                    root = loader.load();
                    cheminTrouve = chemin;
                    LoggerUtil.info(VehiDetaiCo.class, "FXML chargÃ© avec succÃ¨s", "Chemin=" + chemin);
                    break;
                } catch (Exception e) {
                    LoggerUtil.debug(VehiDetaiCo.class, "Ã‰chec chemin FXML",
                            "Chemin=" + chemin, "Message=" + e.getMessage());
                }
            }

            if (root == null) {
                LoggerUtil.warn(VehiDetaiCo.class, "Aucun fichier FXML trouvÃ©");
                elasticLogger.sendLog("WARN", "openRendezVousForm - FXML introuvable");
                return false;
            }

            RendezVousFormController controller = loader.getController();
            Vehicle vehicle = convertArticleToVehicle(article);
            controller.setVehicle(vehicle);

            controller.setOnRendezVousCreated(() -> {
                LoggerUtil.info(VehiDetaiCo.class, "Rendez-vous crÃ©Ã© avec succÃ¨s",
                        "Article=" + article.getTitre());
                elasticLogger.sendLog("INFO", "Rendez-vous crÃ©Ã© pour article: " + article.getId());
                showSuccess("SuccÃ¨s", "Votre rendez-vous a Ã©tÃ© planifiÃ© avec succÃ¨s !");
            });

            Stage stage = new Stage();
            stage.setTitle("Prendre un Rendez-vous - " + article.getTitre());
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

            LoggerUtil.info(VehiDetaiCo.class, "Formulaire rendez-vous ouvert avec succÃ¨s",
                    "Chemin=" + cheminTrouve);
            elasticLogger.sendLog("INFO",
                    "Formulaire rendez-vous ouvert - Article: " + article.getTitre());

            return true;

        } catch (Exception e) {
            LoggerUtil.error(VehiDetaiCo.class, "Erreur ouverture formulaire FXML",
                    "Message=" + e.getMessage(), "ArticleID=" + article.getId());
            elasticLogger.sendLog("ERROR",
                    "Erreur openRendezVousForm: " + e.getMessage());
            return false;
        }
    }

    private void openRendezVousFormIntegre() {
        LoggerUtil.info(VehiDetaiCo.class, "=== OUVERTURE FORMULAIRE INTÃ‰GRÃ‰ RENDEZ-VOUS ===");
        elasticLogger.sendLog("INFO", "Ouverture formulaire rendez-vous intÃ©grÃ©");

        try {
            Stage stage = new Stage();
            stage.setTitle("Prendre un Rendez-vous - " + article.getTitre());

            VBox formContainer = new VBox(20);
            formContainer.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-border-radius: 10;");
            formContainer.setPrefSize(500, 600);

            Label titleLabel = new Label("ðŸ“… Prendre un Rendez-vous");
            titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            Label vehicleLabel = new Label("VÃ©hicule: " + article.getTitre());
            vehicleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

            VBox form = new VBox(15);

            Label dateLabel = new Label("Date souhaitÃ©e:");
            DatePicker datePicker = new DatePicker();
            datePicker.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10;");

            Label timeLabel = new Label("Heure souhaitÃ©e:");
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
            descArea.setPromptText("PrÃ©cisez vos besoins...");
            descArea.setPrefRowCount(3);

            HBox buttons = new HBox(15);
            buttons.setAlignment(Pos.CENTER_RIGHT);

            Button cancelBtn = new Button("Annuler");
            cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10 20;");
            cancelBtn.setOnAction(e -> {
                LoggerUtil.info(VehiDetaiCo.class, "Rendez-vous annulÃ© par l'utilisateur");
                stage.close();
            });

            Button confirmBtn = new Button("Confirmer");
            confirmBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 10 20;");
            confirmBtn.setOnAction(e -> {
                String date = datePicker.getValue() != null ? datePicker.getValue().toString() : "non spÃ©cifiÃ©e";
                String heure = timeCombo.getValue();
                String type = typeCombo.getValue();

                LoggerUtil.info(VehiDetaiCo.class, "Rendez-vous demandÃ©",
                        "Date=" + date, "Heure=" + heure, "Type=" + type);
                elasticLogger.sendLog("INFO",
                        "Rendez-vous demandÃ© - Date: " + date + ", Heure: " + heure + ", Type: " + type);

                showSuccess("SuccÃ¨s", "Rendez-vous demandÃ© pour le " + date + " Ã  " + heure);
                stage.close();
            });

            buttons.getChildren().addAll(cancelBtn, confirmBtn);

            form.getChildren().addAll(dateLabel, datePicker, timeLabel, timeCombo,
                    typeLabel, typeCombo, descLabel, descArea, buttons);

            formContainer.getChildren().addAll(titleLabel, vehicleLabel, form);

            Scene scene = new Scene(formContainer);
            stage.setScene(scene);
            stage.show();

            LoggerUtil.info(VehiDetaiCo.class, "Formulaire intÃ©grÃ© ouvert avec succÃ¨s");
            elasticLogger.sendLog("INFO", "Formulaire rendez-vous intÃ©grÃ© ouvert");

        } catch (Exception e) {
            LoggerUtil.error(VehiDetaiCo.class, "Erreur formulaire intÃ©grÃ©",
                    "Message=" + e.getMessage());
            elasticLogger.sendLog("ERROR",
                    "Erreur openRendezVousFormIntegre: " + e.getMessage());
            showError("Erreur", "Impossible d'ouvrir le formulaire de rendez-vous.");
        }
    }

    private Vehicle convertArticleToVehicle(Article article) {
        LoggerUtil.debug(VehiDetaiCo.class, "Conversion Article â†’ Vehicle");

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

        LoggerUtil.info(VehiDetaiCo.class, "Conversion terminÃ©e",
                "ArticleID=" + article.getId(),
                "VehicleSellerID=" + vehicle.getSellerId());

        return vehicle;
    }

    @FXML
    private void handleContact() {
        LoggerUtil.info(VehiDetaiCo.class, "=== DÃ‰BUT handleContact ===");
        elasticLogger.sendLog("INFO", "Action: Contacter vendeur");

        if (article == null) {
            LoggerUtil.error(VehiDetaiCo.class, "Aucun vÃ©hicule sÃ©lectionnÃ©");
            elasticLogger.sendLog("ERROR", "handleContact - Article null");
            showError("Erreur", "Aucun vÃ©hicule sÃ©lectionnÃ©");
            return;
        }

        SessionManager session = SessionManager.getInstance();
        if (!session.estConnecte()) {
            LoggerUtil.warn(VehiDetaiCo.class, "Utilisateur non connectÃ©");
            elasticLogger.sendLog("WARN", "handleContact - Utilisateur non connectÃ©");
            showWarning("Connexion requise", "Vous devez Ãªtre connectÃ© pour contacter le vendeur");
            return;
        }

        int clientId = session.getUserId();
        String clientRole = session.getUserRole();
        int vendeurId = getVendeurIdFromDatabase(article.getId());

        LoggerUtil.info(VehiDetaiCo.class, "DonnÃ©es contact",
                "ClientID=" + clientId, "ClientRole=" + clientRole,
                "VendeurID=" + vendeurId, "ArticleID=" + article.getId());

        if (vendeurId <= 0) {
            LoggerUtil.error(VehiDetaiCo.class, "Vendeur non trouvÃ©");
            elasticLogger.sendLog("ERROR", "handleContact - Vendeur non trouvÃ©");
            showError("Erreur", "Impossible de trouver le vendeur de ce vÃ©hicule");
            return;
        }

        if (clientId == vendeurId) {
            LoggerUtil.warn(VehiDetaiCo.class, "Tentative contact propre annonce",
                    "UserID=" + clientId, "VendeurID=" + vendeurId);
            elasticLogger.sendLog("WARN",
                    "Tentative contact propre annonce - User: " + clientId);
            showWarning("Action non autorisÃ©e", "Vous ne pouvez pas contacter votre propre annonce");
            return;
        }

        openChatWindow(vendeurId, clientId, article);
    }

    private void openChatWindow(int vendeurId, int clientId, Article article) {
        LoggerUtil.info(VehiDetaiCo.class, "=== OUVERTURE FENÃŠTRE CHAT ===");
        elasticLogger.sendLog("INFO",
                "Ouverture chat - Vendeur: " + vendeurId + ", Client: " + clientId);

        try {
            String[] possiblePaths = {
                    "/com/example/vehiclegestion/view/common/ChatWindow.fxml",
                    "/view/common/ChatWindow.fxml",
                    "view/common/ChatWindow.fxml",
                    "/ChatWindow.fxml"
            };

            FXMLLoader loader = null;
            Parent chatRoot = null;
            String cheminTrouve = null;

            for (String path : possiblePaths) {
                try {
                    LoggerUtil.debug(VehiDetaiCo.class, "Essai chemin chat FXML", "Chemin=" + path);
                    URL url = getClass().getResource(path);
                    if (url != null) {
                        loader = new FXMLLoader(url);
                        chatRoot = loader.load();
                        cheminTrouve = path;
                        LoggerUtil.info(VehiDetaiCo.class, "FXML chat chargÃ©", "Chemin=" + path);
                        break;
                    }
                } catch (Exception e) {
                    LoggerUtil.debug(VehiDetaiCo.class, "Ã‰chec chemin chat",
                            "Chemin=" + path, "Message=" + e.getMessage());
                }
            }

            if (chatRoot == null || loader == null) {
                LoggerUtil.error(VehiDetaiCo.class, "Fichier ChatWindow.fxml introuvable");
                elasticLogger.sendLog("ERROR", "openChatWindow - FXML introuvable");
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
                LoggerUtil.error(VehiDetaiCo.class, "Ã‰chec crÃ©ation conversation");
                elasticLogger.sendLog("ERROR", "openChatWindow - Conversation non crÃ©Ã©e");
                showError("Erreur", "Impossible de crÃ©er la conversation");
                return;
            }

            LoggerUtil.info(VehiDetaiCo.class, "Conversation crÃ©Ã©e/rÃ©cupÃ©rÃ©e",
                    "ConversationID=" + conversation.getIdConversation(),
                    "Article=" + article.getTitre());

            Stage chatStage = new Stage();
            chatStage.setTitle("Chat avec le vendeur - " + article.getTitre());
            chatStage.setScene(new Scene(chatRoot, 1000, 700));
            chatStage.setMinWidth(800);
            chatStage.setMinHeight(600);

            chatStage.setOnShown(e -> {
                Platform.runLater(() -> {
                    LoggerUtil.debug(VehiDetaiCo.class, "Ouverture conversation spÃ©cifique",
                            "ConversationID=" + conversation.getIdConversation());
                    chatController.openSpecificConversation(conversation.getIdConversation());
                });
            });

            chatStage.setOnCloseRequest(e -> {
                LoggerUtil.info(VehiDetaiCo.class, "Fermeture fenÃªtre chat");
                if (chatController != null) {
                    chatController.cleanup();
                }
            });

            chatStage.show();

            LoggerUtil.info(VehiDetaiCo.class, "FenÃªtre chat ouverte avec succÃ¨s",
                    "Chemin=" + cheminTrouve);
            elasticLogger.sendLog("INFO",
                    "FenÃªtre chat ouverte - Conversation: " + conversation.getIdConversation());

        } catch (Exception e) {
            LoggerUtil.error(VehiDetaiCo.class, "Erreur ouverture fenÃªtre chat",
                    "Message=" + e.getMessage());
            elasticLogger.sendLog("ERROR",
                    "Erreur openChatWindow: " + e.getMessage());
            showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddToFavorites() {
        LoggerUtil.info(VehiDetaiCo.class, "=== AJOUT AUX FAVORIS ===");
        elasticLogger.sendLog("INFO", "Ajout aux favoris - Article: " +
                (article != null ? article.getId() : "null"));

        if (article == null) {
            LoggerUtil.warn(VehiDetaiCo.class, "Impossible d'ajouter aux favoris - Article null");
            showWarning("Attention", "Aucun vÃ©hicule sÃ©lectionnÃ©");
            return;
        }

        // Logique d'ajout aux favoris ici...
        showSuccess("Favoris", "VÃ©hicule ajoutÃ© Ã  vos favoris !");

        LoggerUtil.info(VehiDetaiCo.class, "VÃ©hicule ajoutÃ© aux favoris",
                "Article=" + article.getTitre());
        elasticLogger.sendLog("INFO",
                "Article ajoutÃ© aux favoris - ID: " + article.getId());
    }

    // MÃ©thodes d'affichage des messages avec logging
    private void showError(String title, String message) {
        LoggerUtil.error(VehiDetaiCo.class, "Affichage erreur",
                "Titre=" + title, "Message=" + message);
        elasticLogger.sendLog("ERROR", "Erreur affichÃ©e - " + title + ": " + message);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        LoggerUtil.warn(VehiDetaiCo.class, "Affichage avertissement",
                "Titre=" + title, "Message=" + message);
        elasticLogger.sendLog("WARN", "Avertissement affichÃ© - " + title + ": " + message);

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        LoggerUtil.info(VehiDetaiCo.class, "Affichage succÃ¨s",
                "Titre=" + title, "Message=" + message);
        elasticLogger.sendLog("INFO", "SuccÃ¨s affichÃ© - " + title + ": " + message);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        LoggerUtil.debug(VehiDetaiCo.class, "Affichage information",
                "Titre=" + title, "Message=" + message);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setArticle(Article article) {
        LoggerUtil.info(VehiDetaiCo.class, "=== SET ARTICLE ===");

        this.article = article;
        this.articleCharge = (article != null);

        LoggerUtil.info(VehiDetaiCo.class, "Article dÃ©fini",
                "ID=" + (article != null ? article.getId() : "null"),
                "ChargÃ©=" + articleCharge);

        if (articleCharge) {
            displayArticleDetails();
            loadCommentaires();
        }
    }

    private String getRandomCity() {
        String[] cities = {"Tanger", "Casablanca", "Marrakech", "Rabat", "FÃ¨s"};
        String city = cities[(int)(Math.random() * cities.length)];
        LoggerUtil.debug(VehiDetaiCo.class, "Ville alÃ©atoire gÃ©nÃ©rÃ©e", "Ville=" + city);
        return city;
    }

    public void testCommentaireSystem() {
        LoggerUtil.info(VehiDetaiCo.class, "ðŸ§ª === TEST SYSTÃˆME COMMENTAIRES ===");
        elasticLogger.sendLog("INFO", "DÃ©marrage test systÃ¨me commentaires");

        SessionManager session = SessionManager.getInstance();
        LoggerUtil.debug(VehiDetaiCo.class, "Session - ConnectÃ©", "Status=" + session.estConnecte());
        LoggerUtil.debug(VehiDetaiCo.class, "Session - User ID", "Value=" + session.getUserId());
        LoggerUtil.debug(VehiDetaiCo.class, "Session - Role", "Value=" + session.getUserRole());

        LoggerUtil.debug(VehiDetaiCo.class, "Article - ChargÃ©", "Status=" + articleCharge);
        if (article != null) {
            LoggerUtil.debug(VehiDetaiCo.class, "Article - ID", "Value=" + article.getId());
            LoggerUtil.debug(VehiDetaiCo.class, "Article - Titre", "Value=" + article.getTitre());
        }

        CommentaireDAO dao = new CommentaireDAO();
        LoggerUtil.debug(VehiDetaiCo.class, "DAO - Instance", "Status=" + (dao != null));

        if (session.estConnecte() && article != null) {
            boolean dejaCommente = dao.aDejaCommente(session.getUserId(), article.getId());
            LoggerUtil.debug(VehiDetaiCo.class, "DÃ©jÃ  commentÃ©", "Status=" + dejaCommente);
        }

        if (article != null) {
            int nbComments = dao.getNombreCommentaires(article.getId());
            LoggerUtil.debug(VehiDetaiCo.class, "Nombre commentaires", "Count=" + nbComments);
        }

        LoggerUtil.info(VehiDetaiCo.class, "ðŸ§ª === FIN TEST ===");
        elasticLogger.sendLog("INFO", "Test systÃ¨me commentaires terminÃ©");
    }

}