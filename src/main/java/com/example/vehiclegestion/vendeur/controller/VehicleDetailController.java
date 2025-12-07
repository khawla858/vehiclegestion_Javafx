package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.auth.utils.SessionManager;
import javafx.scene.control.Alert;
import com.example.vehiclegestion.vendeur.model.Article;
import com.example.vehiclegestion.vendeur.model.Commentaire;
import com.example.vehiclegestion.vendeur.dao.CommentaireDAO;
import com.example.vehiclegestion.utils.DataReceiver;
import com.example.vehiclegestion.utils.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.geometry.Pos;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class VehicleDetailController implements DataReceiver {

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

    private Article article;
    private NavigationManager nav = NavigationManager.getInstance();
    private CommentaireDAO commentaireDAO = new CommentaireDAO();
    private boolean articleCharge = false;

    @Override
    public void receiveData(Object data) {
        System.out.println("📥 === RECEIVE DATA CALLED ===");

        if (data instanceof Article) {
            this.article = (Article) data;
            this.articleCharge = true;

            System.out.println("✅ ARTICLE CHARGÉ - ID: " + article.getId() + ", Titre: " + article.getTitre());

            // 🧪 EXÉCUTER LE TEST
            testCommentaireSystem();

            displayArticleDetails();
            loadCommentaires();
        } else {
            System.err.println("❌ Données non valides");
            this.articleCharge = false;
            showError("Erreur", "Impossible de charger les données du véhicule");
        }
    }

    @FXML
    private void initialize() {
        System.out.println("🔧 VehicleDetailController initialisé");

        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f5f5f5;");

        if (backBtn != null) {
            backBtn.setOnAction(e -> nav.goBack());
            backBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                    "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        }

        if (contactBtn != null) {
            contactBtn.setOnAction(e -> handleContact());
            contactBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                    "-fx-padding: 12 30; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
        }
    }

    private void displayArticleDetails() {
        if (!articleCharge || article == null) {
            System.err.println("❌ Impossible d'afficher: article non chargé");
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

    // ==================== SECTION COMMENTAIRES ====================

    private void loadCommentaires() {
        System.out.println("🔄 === DÉBUT CHARGEMENT COMMENTAIRES ===");

        if (!articleCharge || article == null) {
            System.err.println("❌ ERREUR: Article non chargé pour les commentaires");
            return;
        }

        System.out.println("📌 Article ID: " + article.getId());

        commentairesSection.getChildren().clear();
        commentairesSection.setStyle("-fx-spacing: 15; -fx-padding: 20; -fx-background-color: white; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        HBox header = createCommentaireHeader();
        commentairesSection.getChildren().add(header);

        boolean estConnecte = SessionManager.getInstance().estConnecte();
        System.out.println("🔐 Session status: " + estConnecte);

        if (estConnecte) {
            int userId = SessionManager.getInstance().getUserId();
            boolean dejaCommente = commentaireDAO.aDejaCommente(userId, article.getId());
            System.out.println("📝 Déjà commenté: " + dejaCommente);

            if (dejaCommente) {
                // ✅ AFFICHER LE COMMENTAIRE DE L'UTILISATEUR AU LIEU DU MESSAGE
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
        System.out.println("📋 Commentaires récupérés: " + commentaires.size());

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

    /**
     * Crée une carte spéciale pour le commentaire de l'utilisateur connecté
     */
    private VBox createMyCommentCard(Commentaire commentaire) {
        VBox card = new VBox(15);
        card.setStyle("-fx-background-color: #E8F5E9; -fx-padding: 20; -fx-background-radius: 8; " +
                "-fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 8;");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("✓");
        icon.setStyle("-fx-font-size: 24px; -fx-text-fill: #4CAF50;");

        Label title = new Label("Votre avis");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E7D32;");

        header.getChildren().addAll(icon, title);

        // Note
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

        // Commentaire
        Label commentText = new Label(commentaire.getTexteCommentaire());
        commentText.setWrapText(true);
        commentText.setStyle("-fx-text-fill: #333; -fx-font-size: 14px; -fx-line-spacing: 1.3;");

        // Date
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
        loginBtn.setOnAction(e -> nav.navigateTo("/view/auth/login.fxml"));

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

    // ==================== GESTION DES COMMENTAIRES ====================

    private void saveCommentaire(int note, String texte) {
        System.out.println("\n💾 === DÉBUT SAVE COMMENTAIRE ===");

        if (!articleCharge || article == null) {
            System.err.println("❌ ERREUR CRITIQUE: Article non chargé !");
            showError("Erreur", "Article non chargé - Veuillez réessayer");
            return;
        }

        SessionManager session = SessionManager.getInstance();
        if (!session.estConnecte()) {
            System.err.println("❌ ERREUR: Session non active !");
            showError("Erreur", "Vous devez être connecté");
            return;
        }

        int idUtilisateur = session.getUserId();
        System.out.println("👤 User ID: " + idUtilisateur);
        System.out.println("📄 Article ID: " + article.getId());

        boolean success = commentaireDAO.ajouterCommentaire(idUtilisateur, article.getId(), note, texte);

        if (success) {
            System.out.println("✅ Commentaire sauvegardé avec succès !");
            showSuccess("Succès", "Votre avis a été publié avec succès !");
            loadCommentaires();
        } else {
            System.err.println("❌ Échec sauvegarde commentaire");
            showError("Erreur", "Impossible de publier votre avis. Veuillez réessayer.");
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

        sellerSection.getChildren().addAll(sellerHeader, warningBox);
    }

    @FXML
    private void handleContact() {
        System.out.println("📞 Contacter le vendeur pour: " + article.getTitre());
        showInfo("Contact", "Fonctionnalité de contact bientôt disponible !");
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

    private String getRandomCity() {
        String[] cities = {"Tanger", "Casablanca", "Marrakech", "Rabat", "Fès"};
        return cities[(int)(Math.random() * cities.length)];
    }

    public void testCommentaireSystem() {
        System.out.println("🧪 === TEST SYSTÈME COMMENTAIRES ===");

        // 1. Test session
        SessionManager session = SessionManager.getInstance();
        System.out.println("1. SESSION - Connecté: " + session.estConnecte());
        System.out.println("   User ID: " + session.getUserId());
        System.out.println("   Role: " + session.getUserRole());

        // 2. Test article
        System.out.println("2. ARTICLE - Chargé: " + articleCharge);
        if (article != null) {
            System.out.println("   ID: " + article.getId());
            System.out.println("   Titre: " + article.getTitre());
        }

        // 3. Test DAO
        CommentaireDAO dao = new CommentaireDAO();
        System.out.println("3. DAO - Instance: " + (dao != null ? "✅" : "❌"));

        // 4. Test déjà commenté
        if (session.estConnecte() && article != null) {
            boolean dejaCommente = dao.aDejaCommente(session.getUserId(), article.getId());
            System.out.println("4. DÉJÀ COMMENTÉ: " + dejaCommente);
        }

        // 5. Test nombre commentaires
        if (article != null) {
            int nbComments = dao.getNombreCommentaires(article.getId());
            System.out.println("5. NOMBRE COMMENTAIRES: " + nbComments);
        }

        System.out.println("🧪 === FIN TEST ===");
    }
}