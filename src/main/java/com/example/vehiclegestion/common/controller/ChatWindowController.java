package com.example.vehiclegestion.common.controller;

import com.example.vehiclegestion.auth.utils.SessionManager;
import com.example.vehiclegestion.common.dao.ChatDAO;
import com.example.vehiclegestion.common.model.Conversation;
import com.example.vehiclegestion.common.model.Message;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
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
    @FXML private VBox chatContainer;  // ← AJOUT
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚀 Initialisation du chat...");

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
        loadConversations();
        setupSearchFilter();
        startAutoRefresh();
        updateNotificationBadge();
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
                    conversationsListContainer.getChildren().add(createConversationItem(conv));
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement conversations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Crée un item de conversation pour la liste (style moderne)
     */
    private HBox createConversationItem(Conversation conv) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 15, 12, 15));

        // Style de base
        String baseStyle = "-fx-background-color: white; -fx-cursor: hand;";
        if (conv.hasUnreadMessages()) {
            baseStyle = "-fx-background-color: #f0f9ff; -fx-cursor: hand;";
        }
        item.setStyle(baseStyle);

        // Avatar (initiales)
        StackPane avatar = new StackPane();
        avatar.setPrefSize(50, 50);
        avatar.setStyle("-fx-background-color: #0084ff; -fx-background-radius: 25;");

        String initiales = getInitiales(conv.getInterlocuteurComplet());
        Label avatarText = new Label(initiales);
        avatarText.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        avatar.getChildren().add(avatarText);

        // Informations conversation
        VBox infoBox = new VBox(4);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nomLabel = new Label(conv.getInterlocuteurComplet());
        nomLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1c1e21;");

        Label messageLabel = new Label(conv.getDernierMessagePreview());
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #65676b;");
        messageLabel.setMaxWidth(200);

        infoBox.getChildren().addAll(nomLabel, messageLabel);

        // Informations droite (date + badge)
        VBox rightBox = new VBox(5);
        rightBox.setAlignment(Pos.TOP_RIGHT);

        Label dateLabel = new Label(conv.getDernierMessageDateFormatted());
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a8d91;");

        // Badge messages non lus
        if (conv.hasUnreadMessages()) {
            Label badge = new Label(String.valueOf(conv.getNbMessagesNonLus()));
            badge.setStyle("-fx-background-color: #0084ff; -fx-text-fill: white; " +
                    "-fx-background-radius: 10; -fx-padding: 2 6; " +
                    "-fx-font-size: 11px; -fx-font-weight: bold;");
            rightBox.getChildren().addAll(dateLabel, badge);
        } else {
            rightBox.getChildren().add(dateLabel);
        }

        item.getChildren().addAll(avatar, infoBox, rightBox);

        // Hover effect
        final String finalBaseStyle = baseStyle;
        item.setOnMouseEntered(e -> {
            if (currentConversation == null || currentConversation.getIdConversation() != conv.getIdConversation()) {
                item.setStyle("-fx-background-color: #f2f3f5; -fx-cursor: hand;");
            }
        });

        item.setOnMouseExited(e -> {
            if (currentConversation != null && currentConversation.getIdConversation() == conv.getIdConversation()) {
                item.setStyle("-fx-background-color: #e7f3ff; -fx-cursor: hand;");
            } else {
                item.setStyle(finalBaseStyle);
            }
        });

        // Click pour ouvrir la conversation
        item.setOnMouseClicked(e -> {
            openConversation(conv);
            // Mettre à jour le style de la conversation sélectionnée
            conversationsListContainer.getChildren().forEach(node -> {
                if (node instanceof HBox) {
                    HBox hbox = (HBox) node;
                    if (hbox == item) {
                        hbox.setStyle("-fx-background-color: #e7f3ff; -fx-cursor: hand;");
                    } else {
                        hbox.setStyle("-fx-background-color: white; -fx-cursor: hand;");
                    }
                }
            });
        });

        return item;
    }

    /**
     * Ouvre une conversation et charge ses messages
     */
    private void openConversation(Conversation conv) {
        System.out.println("\n========================================");
        System.out.println("📂 OUVERTURE CONVERSATION");
        System.out.println("========================================");
        System.out.println("ID Conversation: " + conv.getIdConversation());
        System.out.println("Interlocuteur: " + conv.getInterlocuteurComplet());
        System.out.println("ID Vendeur: " + conv.getIdVendeur());
        System.out.println("ID Client: " + conv.getIdClient());
        System.out.println("Utilisateur actuel: ID=" + currentUserId + ", Role=" + currentUserRole);
        System.out.println("========================================\n");

        // IMPORTANT: Sauvegarder la conversation actuelle
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
        loadConversations();
    }

    /**
     * Charge les messages d'une conversation
     */
    private void loadMessages(int idConversation) {
        try {
            System.out.println("\n🔄 Chargement messages pour conversation: " + idConversation);

            // VÉRIFICATION CRITIQUE: S'assurer qu'on charge bien la bonne conversation
            if (currentConversation == null || currentConversation.getIdConversation() != idConversation) {
                System.err.println("⚠️ ATTENTION: idConversation ne correspond pas à currentConversation!");
                System.err.println("   Demandé: " + idConversation);
                System.err.println("   Actuel: " + (currentConversation != null ? currentConversation.getIdConversation() : "NULL"));
                return;
            }

            List<Message> messages = chatDAO.getMessagesByConversation(idConversation);

            // Ne recharger que si le nombre de messages a changé
            if (messages.size() == lastMessageCount && !messagesContainer.getChildren().isEmpty()) {
                System.out.println("✓ Nombre de messages inchangé (" + messages.size() + "), skip reload");
                return;
            }

            System.out.println("💬 " + messages.size() + " messages chargés pour conversation " + idConversation);
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
     * Crée une bulle de message (style WhatsApp/Messenger)
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
            System.out.println("⚠️ Message vide, non envoyé");
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
                System.out.println("✅ Message envoyé: " + contenu);

                // Vider le champ
                messageInputField.clear();

                // Recharger les messages immédiatement
                lastMessageCount = 0; // Force le rechargement
                loadMessages(currentConversation.getIdConversation());

                // Rafraîchir la liste des conversations
                loadConversations();

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
                // Charger les nouveaux messages sans perturber l'utilisateur
                loadMessages(currentConversation.getIdConversation());
            }
            updateNotificationBadge();

            // Rafraîchir la liste des conversations en arrière-plan
            if (currentConversation == null) {
                loadConversations();
            }
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
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
        // Ajout d'emojis rapides
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
}