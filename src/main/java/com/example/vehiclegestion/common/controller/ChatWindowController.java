package com.example.vehiclegestion.common.controller;

import com.example.vehiclegestion.auth.utils.SessionManager;
import com.example.vehiclegestion.common.dao.ChatDAO;
import com.example.vehiclegestion.common.model.Conversation;
import com.example.vehiclegestion.common.model.Message;

import com.example.vehiclegestion.common.utils.NotificationService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ChatWindowController implements Initializable {

    @FXML private VBox conversationsListContainer;
    @FXML private TextField searchConversationField;
    @FXML private Label badgeNotifications;

    @FXML private VBox placeholderView;
    @FXML private VBox chatContainer;
    @FXML private HBox chatHeader;
    @FXML private Label avatarLabel;
    @FXML private Label interlocuteurNomLabel;
    @FXML private Label interlocuteurStatusLabel;

    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private HBox messageInputArea;
    @FXML private TextField messageInputField;
    @FXML private Button sendButton;

    private ChatDAO chatDAO = new ChatDAO();
    private int currentUserId;
    private String currentUserRole;
    private Conversation currentConversation;
    private Timeline autoRefreshTimeline;
    private int lastMessageCount = 0;
    private boolean fromVehicleDetails = false; // Nouveau flag

    // AJOUTER CET ATTRIBUT
    private NotificationService notificationService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("\n🚀 === INITIALISATION CHAT WINDOW ===");

        // AJOUTER CETTE LIGNE - Initialiser NotificationService
        notificationService = NotificationService.getInstance();

        // Récupérer l'utilisateur connecté
        if (SessionManager.getInstance().estConnecte()) {
            currentUserId = SessionManager.getInstance().getUserId();
            currentUserRole = SessionManager.getInstance().getUserRole();
            System.out.println("✅ Utilisateur connecté: ID=" + currentUserId + ", Role=" + currentUserRole);
        } else {
            System.err.println("❌ Aucun utilisateur connecté !");
            showError("Erreur", "Vous devez être connecté pour accéder au chat");
            return;
        }

        setupMessageInput();
        setupSearchFilter();
        startAutoRefresh();
        updateNotificationBadge();

        // NE PAS charger les conversations ici immédiatement
        // Elles seront chargées par initializeFromMenu() ou openSpecificConversation()

        // Par défaut, afficher le placeholder
        Platform.runLater(() -> {
            showPlaceholder();
        });
    }

    /**
     * Affiche le placeholder (quand on vient du menu)
     */
    public void showPlaceholder() {
        System.out.println("📭 Affichage du placeholder (mode menu)");
        placeholderView.setVisible(true);
        placeholderView.setManaged(true);
        chatContainer.setVisible(false);
        chatContainer.setManaged(false);
        currentConversation = null;
    }

    /**
     * Configure le champ de saisie des messages
     */
    private void setupMessageInput() {
        // Envoi avec Entrée
        messageInputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        // Activer/désactiver le bouton d'envoi selon le contenu
        messageInputField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (sendButton != null) {
                sendButton.setDisable(newVal.trim().isEmpty());
            }
        });

        // Style focus
        messageInputField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                messageInputField.setStyle(
                        "-fx-padding: 12 15; " +
                                "-fx-background-radius: 20; " +
                                "-fx-border-radius: 20; " +
                                "-fx-background-color: #f0f2f5; " +
                                "-fx-border-color: #0084ff; " +
                                "-fx-border-width: 2; " +
                                "-fx-font-size: 14px;"
                );
            } else {
                messageInputField.setStyle(
                        "-fx-padding: 12 15; " +
                                "-fx-background-radius: 20; " +
                                "-fx-border-radius: 20; " +
                                "-fx-background-color: #f0f2f5; " +
                                "-fx-border-color: transparent; " +
                                "-fx-font-size: 14px;"
                );
            }
        });
    }

    /**
     * Charge toutes les conversations de l'utilisateur
     */
    private void loadConversations() {
        try {
            List<Conversation> conversations = chatDAO.getConversationsByUser(currentUserId, currentUserRole);
            System.out.println("📋 " + conversations.size() + " conversations chargées");

            conversationsListContainer.getChildren().clear();

            if (conversations.isEmpty()) {
                Label emptyLabel = new Label("Aucune conversation");
                emptyLabel.setStyle("-fx-text-fill: #8a8d91; -fx-padding: 20; -fx-font-size: 14px;");
                conversationsListContainer.getChildren().add(emptyLabel);
            } else {
                for (Conversation conv : conversations) {
                    HBox item = createConversationItem(conv);
                    conversationsListContainer.getChildren().add(item);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement conversations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crée un item de conversation pour la liste
     */
    private HBox createConversationItem(Conversation conv) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));
        item.setStyle("-fx-background-color: white; -fx-cursor: hand;");

        // STOCKER L'ID DE LA CONVERSATION
        item.setUserData(conv.getIdConversation());

        // Ajouter l'événement de clic
        item.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                openConversation(conv);
                highlightConversationInList(conv.getIdConversation());
            }
        });

        // Avatar
        StackPane avatar = new StackPane();
        avatar.setPrefSize(50, 50);
        avatar.setStyle("-fx-background-color: #0084ff; -fx-background-radius: 25;");

        String initiales = getInitiales(conv.getInterlocuteurComplet());
        Label avatarLabel = new Label(initiales);
        avatarLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        avatar.getChildren().add(avatarLabel);

        // Contenu
        VBox content = new VBox(4);
        content.setPrefWidth(200);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(conv.getInterlocuteurComplet());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1c1e21; -fx-font-size: 14px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLabel = new Label(conv.getDernierMessageDateFormatted());
        timeLabel.setStyle("-fx-text-fill: #8a8d91; -fx-font-size: 12px;");

        header.getChildren().addAll(nameLabel, spacer, timeLabel);

        Label lastMsgLabel = new Label(conv.getDernierMessagePreview());
        lastMsgLabel.setStyle("-fx-text-fill: #8a8d91; -fx-font-size: 13px;");
        lastMsgLabel.setWrapText(true);
        lastMsgLabel.setMaxWidth(200);

        content.getChildren().addAll(header, lastMsgLabel);

        // Badge notifications non lues
        if (conv.getNbMessagesNonLus() > 0) {
            StackPane badge = new StackPane();
            badge.setPrefSize(20, 20);
            badge.setStyle("-fx-background-color: #fa3e3e; -fx-background-radius: 10;");

            Label badgeText = new Label(String.valueOf(conv.getNbMessagesNonLus()));
            badgeText.setStyle("-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
            badge.getChildren().add(badgeText);

            item.getChildren().addAll(avatar, content, badge);
        } else {
            item.getChildren().addAll(avatar, content);
        }

        return item;
    }

    /**
     * Met en surbrillance une conversation dans la liste
     */
    private void highlightConversationInList(int idConversation) {
        for (javafx.scene.Node node : conversationsListContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                if (hbox.getUserData() != null && hbox.getUserData().equals(idConversation)) {
                    // Style de sélection
                    hbox.setStyle("-fx-background-color: #e7f3ff; -fx-border-color: #0084ff; -fx-border-width: 0 0 0 3; -fx-cursor: hand;");
                } else {
                    hbox.setStyle("-fx-background-color: white; -fx-border-width: 0; -fx-cursor: hand;");
                }
            }
        }
    }

    /**
     * Rafraîchit la liste en gardant la sélection
     */
    private void refreshConversationsListKeepingSelection() {
        Integer selectedId = currentConversation != null ? currentConversation.getIdConversation() : null;

        // Sauvegarder les items sélectionnés
        List<Object> selectedItems = new java.util.ArrayList<>();
        for (javafx.scene.Node node : conversationsListContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                if (hbox.getStyle().contains("#e7f3ff")) {
                    selectedItems.add(hbox.getUserData());
                }
            }
        }

        // Recharger
        loadConversations();

        // Restaurer la sélection
        if (selectedId != null) {
            highlightConversationInList(selectedId);
        }
    }

    /**
     * Ouvre une conversation et charge ses messages
     */
    public void openConversation(Conversation conv) {
        System.out.println("\n========================================");
        System.out.println("📂 OUVERTURE CONVERSATION");
        System.out.println("========================================");
        System.out.println("ID Conversation: " + conv.getIdConversation());
        System.out.println("Interlocuteur: " + conv.getInterlocuteurComplet());
        System.out.println("ID Vendeur: " + conv.getIdVendeur());
        System.out.println("ID Client: " + conv.getIdClient());
        System.out.println("Utilisateur actuel: ID=" + currentUserId + ", Role=" + currentUserRole);
        System.out.println("========================================\n");

        currentConversation = conv;
        lastMessageCount = 0;

        // Masquer placeholder, afficher chat
        placeholderView.setVisible(false);
        placeholderView.setManaged(false);

        chatContainer.setVisible(true);
        chatContainer.setManaged(true);

        // Mettre à jour l'en-tête
        avatarLabel.setText(getInitiales(conv.getInterlocuteurComplet()));
        interlocuteurNomLabel.setText(conv.getInterlocuteurComplet());
        interlocuteurStatusLabel.setText(conv.getTypeConversationLabel());

        // Charger les messages
        loadMessages(conv.getIdConversation());

        // Marquer les messages comme lus
        chatDAO.markMessagesAsRead(conv.getIdConversation(), currentUserId);

        // Focus sur le champ de saisie
        Platform.runLater(() -> messageInputField.requestFocus());

        // Rafraîchir la liste des conversations
        refreshConversationsListKeepingSelection();
    }

    /**
     * Charge les messages d'une conversation
     */
    private void loadMessages(int idConversation) {
        try {
            System.out.println("\n🔄 Chargement messages pour conversation: " + idConversation);

            List<Message> messages = chatDAO.getMessagesByConversation(idConversation);

            // Ne recharger que si le nombre de messages a changé
            if (messages.size() == lastMessageCount && !messagesContainer.getChildren().isEmpty()) {
                System.out.println("✓ Nombre de messages inchangé (" + messages.size() + "), skip reload");
                return;
            }

            System.out.println("💬 " + messages.size() + " messages chargés");
            lastMessageCount = messages.size();

            messagesContainer.getChildren().clear();

            if (messages.isEmpty()) {
                Label emptyLabel = new Label("Aucun message. Commencez la conversation ! 💬");
                emptyLabel.setStyle("-fx-text-fill: #8a8d91; -fx-padding: 20; -fx-font-size: 14px; -fx-text-alignment: center;");
                emptyLabel.setWrapText(true);
                messagesContainer.getChildren().add(emptyLabel);
            } else {
                for (Message msg : messages) {
                    messagesContainer.getChildren().add(createMessageBubble(msg));
                }
            }

            // Scroll vers le bas
            Platform.runLater(() -> {
                messagesScrollPane.layout();
                messagesScrollPane.setVvalue(1.0);
            });

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement messages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crée une bulle de message
     */
    private HBox createMessageBubble(Message msg) {
        HBox container = new HBox();
        container.setPadding(new Insets(5, 10, 5, 10));

        boolean isMyMessage = msg.getIdExpediteur() == currentUserId;

        // Bulle de message
        VBox bubble = new VBox(5);
        bubble.setPadding(new Insets(10, 15, 10, 15));
        bubble.setMaxWidth(400);

        if (isMyMessage) {
            // Message envoyé (droite, bleu)
            bubble.setStyle(
                    "-fx-background-color: #0084ff; " +
                            "-fx-background-radius: 18 18 4 18; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
            container.setAlignment(Pos.CENTER_RIGHT);
        } else {
            // Message reçu (gauche, gris)
            bubble.setStyle(
                    "-fx-background-color: #e4e6eb; " +
                            "-fx-background-radius: 18 18 18 4; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
            container.setAlignment(Pos.CENTER_LEFT);

            // Ajouter le nom de l'expéditeur pour les messages reçus
            if (msg.getPrenomExpediteur() != null) {
                Label senderLabel = new Label(msg.getPrenomExpediteur() + " " + msg.getNomExpediteur());
                senderLabel.setStyle(
                        "-fx-text-fill: #0084ff; " +
                                "-fx-font-size: 11px; " +
                                "-fx-font-weight: bold;"
                );
                bubble.getChildren().add(senderLabel);
            }
        }

        // Contenu du message
        Label contentLabel = new Label(msg.getContenu());
        contentLabel.setWrapText(true);
        contentLabel.setStyle(
                "-fx-text-fill: " + (isMyMessage ? "white" : "#1c1e21") + "; " +
                        "-fx-font-size: 14px; " +
                        "-fx-line-spacing: 2px;"
        );

        // Heure d'envoi
        Label timeLabel = new Label(msg.getHeureEnvoi());
        timeLabel.setStyle(
                "-fx-text-fill: " + (isMyMessage ? "rgba(255,255,255,0.7)" : "#65676b") + "; " +
                        "-fx-font-size: 11px;"
        );

        bubble.getChildren().addAll(contentLabel, timeLabel);
        container.getChildren().add(bubble);

        return container;
    }

    /**
     * Envoie un message
     */
    @FXML
    private void sendMessage() {
        String contenu = messageInputField.getText().trim();

        if (contenu.isEmpty()) {
            System.out.println("⚠ Message vide, non envoyé");
            return;
        }

        if (currentConversation == null) {
            showError("Erreur", "Aucune conversation sélectionnée");
            return;
        }

        try {
            // Désactiver temporairement le champ de saisie
            messageInputField.setDisable(true);

            boolean success = chatDAO.sendMessage(
                    currentConversation.getIdConversation(),
                    currentUserId,
                    currentUserRole,
                    contenu
            );

            if (success) {
                System.out.println("\n✅ === MESSAGE ENVOYÉ DANS L'INTERFACE ===");
                System.out.println("   Contenu: " + contenu);

                // ================================================
                // AJOUTER CE CODE POUR LA NOTIFICATION
                // ================================================
                try {
                    // Déterminer qui est le destinataire
                    int destinataireId;

                    if (currentUserRole.equals("client")) {
                        // Si le client envoie, le destinataire est le vendeur
                        destinataireId = currentConversation.getIdVendeur();
                        System.out.println("📤 Client → Vendeur (ID: " + destinataireId + ")");
                    } else {
                        // Si le vendeur envoie, le destinataire est le client
                        destinataireId = currentConversation.getIdClient();
                        System.out.println("📤 Vendeur → Client (ID: " + destinataireId + ")");
                    }

                    // Obtenir le nom de l'expéditeur
                    String nomExpediteur = SessionManager.getInstance().getUserFullName();
                    if (nomExpediteur == null || nomExpediteur.isEmpty()) {
                        nomExpediteur = "Utilisateur";
                    }

                    // Créer la notification
                    if (destinataireId > 0) {
                        notificationService.notifierNouveauMessage(
                                destinataireId,
                                currentUserId,
                                nomExpediteur
                        );
                        System.out.println("📨 Notification envoyée à l'utilisateur ID: " + destinataireId);
                    }
                } catch (Exception e) {
                    System.err.println("⚠ Erreur lors de l'envoi de la notification: " + e.getMessage());
                }
                // ================================================

                // Vider le champ
                messageInputField.clear();

                // Recharger les messages immédiatement
                lastMessageCount = 0;
                loadMessages(currentConversation.getIdConversation());

                // Rafraîchir la liste des conversations
                refreshConversationsListKeepingSelection();

                // Feedback visuel
                showSuccessNotification("Message envoyé ✓");
            } else {
                showError("Erreur", "Impossible d'envoyer le message");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur envoi message: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Erreur lors de l'envoi: " + e.getMessage());
        } finally {
            // Réactiver le champ de saisie
            messageInputField.setDisable(false);
            messageInputField.requestFocus();
        }
    }

    /**
     * Affiche une notification de succès temporaire
     */
    private void showSuccessNotification(String message) {
        Label notification = new Label(message);
        notification.setStyle(
                "-fx-background-color: #00c851; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10 20; " +
                        "-fx-background-radius: 5; " +
                        "-fx-font-size: 12px; " +
                        "-fx-font-weight: bold;"
        );

        // Ajouter au conteneur de messages temporairement
        messagesContainer.getChildren().add(notification);

        // Supprimer après 2 secondes
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            messagesContainer.getChildren().remove(notification);
        }));
        timeline.play();
    }

    /**
     * Filtre de recherche dans les conversations
     */
    private void setupSearchFilter() {
        searchConversationField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                loadConversations();
                return;
            }

            try {
                List<Conversation> allConversations = chatDAO.getConversationsByUser(currentUserId, currentUserRole);
                String searchTerm = newVal.toLowerCase().trim();

                conversationsListContainer.getChildren().clear();

                boolean found = false;
                for (Conversation conv : allConversations) {
                    if (conv.getInterlocuteurComplet().toLowerCase().contains(searchTerm) ||
                            (conv.getDernierMessage() != null && conv.getDernierMessage().toLowerCase().contains(searchTerm))) {
                        conversationsListContainer.getChildren().add(createConversationItem(conv));
                        found = true;
                    }
                }

                if (!found) {
                    Label noResultLabel = new Label("Aucun résultat pour \"" + newVal + "\"");
                    noResultLabel.setStyle("-fx-text-fill: #8a8d91; -fx-padding: 20; -fx-font-size: 14px;");
                    conversationsListContainer.getChildren().add(noResultLabel);
                }

            } catch (Exception e) {
                System.err.println("❌ Erreur recherche: " + e.getMessage());
            }
        });
    }

    /**
     * Met à jour le badge de notifications
     */
    private void updateNotificationBadge() {
        int unreadCount = chatDAO.countUnreadMessages(currentUserId, currentUserRole);

        if (unreadCount > 0) {
            badgeNotifications.setText(String.valueOf(unreadCount));
            badgeNotifications.setVisible(true);
        } else {
            badgeNotifications.setVisible(false);
        }
    }

    /**
     * Actualisation automatique toutes les 3 secondes
     */
    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
            if (currentConversation != null) {
                // Charger les nouveaux messages
                loadMessages(currentConversation.getIdConversation());
            }
            updateNotificationBadge();

            // Rafraîchir la liste
            refreshConversationsListKeepingSelection();
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    /**
     * Ouvre une conversation spécifique par son ID
     */
    public void openSpecificConversation(int idConversation) {
        System.out.println("\n🎯 === OUVERTURE AUTOMATIQUE CONVERSATION ===");
        System.out.println("   ID Conversation: " + idConversation);

        try {
            // 1. Recharger TOUTES les conversations d'abord
            loadConversations();

            // 2. Récupérer la conversation spécifique
            Conversation conversation = chatDAO.getConversationById(idConversation);

            if (conversation != null) {
                System.out.println("✅ Conversation trouvée: " + conversation.getInterlocuteurComplet());

                // 3. Vérifier si la conversation est déjà dans la liste
                boolean conversationInList = false;
                for (javafx.scene.Node node : conversationsListContainer.getChildren()) {
                    if (node instanceof HBox) {
                        HBox hbox = (HBox) node;
                        if (hbox.getUserData() != null &&
                                hbox.getUserData().equals(idConversation)) {
                            conversationInList = true;
                            break;
                        }
                    }
                }

                // 4. Si pas dans la liste, l'ajouter
                if (!conversationInList) {
                    System.out.println("📌 Conversation absente de la liste, ajout...");
                    HBox newItem = createConversationItem(conversation);
                    conversationsListContainer.getChildren().add(0, newItem);
                }

                // 5. Ouvrir la conversation
                openConversation(conversation);

                System.out.println("✅ Conversation " + idConversation + " ouverte automatiquement");
            } else {
                System.err.println("❌ Conversation " + idConversation + " introuvable");
                showError("Erreur", "La conversation n'a pas pu être chargée");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture conversation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Définit le mode "depuis les détails du véhicule"
     */
    public void setFromVehicleDetails(boolean fromVehicleDetails) {
        this.fromVehicleDetails = fromVehicleDetails;
        System.out.println("🎯 Mode: " + (fromVehicleDetails ? "Depuis détails véhicule" : "Depuis menu"));
    }

    /**
     * Actions supplémentaires
     */
    @FXML
    private void attachFile() {
        System.out.println("📎 Joindre un fichier (à implémenter)");
        showAlert("Fonction en développement", "L'envoi de fichiers sera bientôt disponible.");
    }

    @FXML
    private void showEmojiPicker() {
        System.out.println("😊 Sélecteur d'emoji (à implémenter)");
        String currentText = messageInputField.getText();
        messageInputField.setText(currentText + "😊");
        messageInputField.positionCaret(messageInputField.getText().length());
    }

    @FXML
    private void callInterlocuteur() {
        System.out.println("📞 Appel (à implémenter)");
        showAlert("Fonction en développement", "Les appels vocaux seront bientôt disponibles.");
    }

    @FXML
    private void showInfos() {
        if (currentConversation != null) {
            showAlert("Informations sur la conversation",
                    "Conversation ID: " + currentConversation.getIdConversation() + "\n" +
                            "Type: " + currentConversation.getTypeConversationLabel() + "\n" +
                            "Interlocuteur: " + currentConversation.getInterlocuteurComplet() + "\n" +
                            "Statut: " + currentConversation.getStatut() + "\n" +
                            "Date création: " + (currentConversation.getDateCreation() != null ?
                            currentConversation.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A"));
        }
    }

    /**
     * Utilitaires
     */
    private String getInitiales(String nom) {
        if (nom == null || nom.isEmpty()) return "?";
        String[] parts = nom.split(" ");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        }
        return nom.substring(0, Math.min(2, nom.length())).toUpperCase();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void cleanup() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
    }

    /**
     * Initialise le chat en mode "depuis le menu"
     * (appelé quand on ouvre depuis le menu Messages)
     */
    public void initializeFromMenu() {
        System.out.println("\n📭 === MODE MENU ACTIVÉ ===");

        // Par défaut, montrer le placeholder (pas de conversation ouverte)
        Platform.runLater(() -> {
            placeholderView.setVisible(true);
            placeholderView.setManaged(true);
            chatContainer.setVisible(false);
            chatContainer.setManaged(false);
            currentConversation = null;

            // Désactiver le champ de message
            if (messageInputField != null) {
                messageInputField.setDisable(true);
                messageInputField.clear();
                messageInputField.setPromptText("Sélectionnez une conversation pour envoyer un message");
            }

            if (sendButton != null) {
                sendButton.setDisable(true);
            }
        });

        // Charger les conversations
        loadConversations();
    }
    public void setUserInfo(int userId, String userRole) {
        this.currentUserId = userId;
        this.currentUserRole = userRole;
        System.out.println("✅ Chat - Utilisateur défini: ID=" + userId + ", Role=" + userRole);

        if (userId > 0) {
            loadConversations();
        }
    }
}