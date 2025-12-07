package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.vendeur.dao.ClientDAO;
import com.example.vehiclegestion.vendeur.dao.VenteDAO;
import com.example.vehiclegestion.vendeur.model.Vente;

import com.example.vehiclegestion.vendeur.dao.ArticleDAO;
import com.example.vehiclegestion.vendeur.model.Client;
import com.example.vehiclegestion.vendeur.model.Article;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.time.LocalTime;
import com.example.vehiclegestion.logging.service.ElasticLogService;
import com.example.vehiclegestion.logging.util.LoggerUtil;
import org.slf4j.Logger;

import java.util.Map;
public class FormVenteController {

    // FXML Elements
    @FXML private TextField searchClientField;
    @FXML private VBox clientResultsContainer;
    @FXML private ListView<Client> clientListView;
    @FXML private Label clientCountLabel;
    @FXML private VBox selectedClientBox;
    @FXML private Label clientNomLabel;
    @FXML private Label clientEmailLabel;
    @FXML private Label clientTelLabel;
    @FXML private Label clientNbAchatsLabel;

    @FXML private Label vehiculeTitreLabel;
    @FXML private Label vehiculePrixLabel;
    @FXML private Label vehiculeMarqueLabel;
    @FXML private Label vehiculeModeleLabel;
    @FXML private Label vehiculeAnneeLabel;

    @FXML private TextField montantField;
    @FXML private ComboBox<String> moyenPaiementCombo;
    @FXML private ComboBox<String> statutCombo;
    @FXML private DatePicker dateVentePicker;
    @FXML private TextArea notesArea;
    @FXML private Label errorLabel;

    // DAOs et données
    private ClientDAO clientDAO;
    private VenteDAO venteDAO;
    private ArticleDAO articleDAO;
    private SessionManager sessionManager = SessionManager.getInstance();

    private Article articleAVendre;
    private Client clientSelectionne;
    private Stage dialogStage;
    private int vendeurId;
    private static final Logger logger = LoggerUtil.getLogger(FormVenteController.class);
    private final ElasticLogService elasticLogService = new ElasticLogService();

    @FXML
    public void initialize() {
        try {
            clientDAO = new ClientDAO();
            venteDAO = new VenteDAO();
            articleDAO = new ArticleDAO();

            vendeurId = sessionManager.getUserId();

            setupComboBoxes();
            setupClientListView();
            setupSearchField();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation : " + e.getMessage());
        }
    }

    /**
     * Configure les ComboBox
     */
    private void setupComboBoxes() {
        // Moyens de paiement
        moyenPaiementCombo.setItems(FXCollections.observableArrayList(
                "Espèces",
                "Carte bancaire",
                "Virement",
                "Chèque",
                "Crédit auto"
        ));
        moyenPaiementCombo.setValue("Espèces");

        // Statuts
        statutCombo.setItems(FXCollections.observableArrayList(
                "en cours",
                "terminée"
        ));
        statutCombo.setValue("en cours");

        // Date par défaut = aujourd'hui
        dateVentePicker.setValue(LocalDate.now());
    }

    /**
     * Configure la ListView des clients
     */
    private void setupClientListView() {
        clientListView.setCellFactory(param -> new ListCell<Client>() {
            @Override
            protected void updateItem(Client client, boolean empty) {
                super.updateItem(client, empty);
                if (empty || client == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String display = String.format("👤 %s %s - 📧 %s - 📞 %s",
                            client.getPrenom(),
                            client.getNom(),
                            client.getEmail() != null ? client.getEmail() : "N/A",
                            client.getTelephone() != null ? client.getTelephone() : "N/A"
                    );
                    setText(display);
                    setStyle("-fx-padding: 8; -fx-font-size: 13px;");
                }
            }
        });

        // Sélection d'un client
        clientListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Client selected = clientListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    selectionnerClient(selected);
                }
            }
        });
    }

    /**
     * Configure la recherche en temps réel
     */
    private void setupSearchField() {
        searchClientField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() >= 2) {
                rechercherClient();
            }
        });
    }

    /**
     * Rechercher un client par nom, prénom ou email
     */
    @FXML
    private void rechercherClient() {
        String searchTerm = searchClientField.getText().trim();

        if (searchTerm.isEmpty()) {
            showError("Veuillez saisir un nom, prénom ou email");
            return;
        }

        try {
            // ✅ CORRECTION : Passer vendeurId en 2ème paramètre
            List<Client> clients = clientDAO.searchClients(searchTerm, vendeurId);

            if (clients.isEmpty()) {
                showError("Aucun client trouvé pour : " + searchTerm);
                clientResultsContainer.setVisible(false);
                clientResultsContainer.setManaged(false);
            } else {
                ObservableList<Client> clientsObs = FXCollections.observableArrayList(clients);
                clientListView.setItems(clientsObs);
                clientCountLabel.setText(clients.size() + " client(s) trouvé(s)");

                clientResultsContainer.setVisible(true);
                clientResultsContainer.setManaged(true);

                errorLabel.setText("");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de recherche : " + e.getMessage());
        }
    }

    /**
     * Sélectionner un client
     */
    private void selectionnerClient(Client client) {
        clientSelectionne = client;

        // Afficher les infos du client
        clientNomLabel.setText(client.getFullName());
        clientEmailLabel.setText(client.getEmail() != null ? client.getEmail() : "Non renseigné");
        clientTelLabel.setText(client.getTelephone() != null ? client.getTelephone() : "Non renseigné");
        clientNbAchatsLabel.setText(String.valueOf(client.getNbVentes()));

        // Masquer les résultats, afficher la sélection
        clientResultsContainer.setVisible(false);
        clientResultsContainer.setManaged(false);

        selectedClientBox.setVisible(true);
        selectedClientBox.setManaged(true);

        errorLabel.setText("");

        System.out.println("✅ Client sélectionné : " + client.getFullName());
    }

    /**
     * Changer de client
     */
    @FXML
    private void changerClient() {
        clientSelectionne = null;
        searchClientField.clear();

        selectedClientBox.setVisible(false);
        selectedClientBox.setManaged(false);
    }

    /**
     * Créer un nouveau client
     */
    @FXML
    private void creerNouveauClient() {
        // TODO: Ouvrir un formulaire de création de client
        showError("Fonctionnalité à implémenter : Créer un nouveau client");
    }

    /**
     * Définir l'article à vendre
     */
    public void setArticle(Article article) {
        this.articleAVendre = article;

        if (article != null) {
            vehiculeTitreLabel.setText(article.getTitre());
            vehiculePrixLabel.setText(String.format("%.2f DH", article.getPrix()));
            vehiculeMarqueLabel.setText(article.getMarque() != null ? article.getMarque() : "N/A");
            vehiculeModeleLabel.setText(article.getModele() != null ? article.getModele() : "N/A");
            vehiculeAnneeLabel.setText(String.valueOf(article.getAnnee()));

            // Pré-remplir le montant
            montantField.setText(String.valueOf(article.getPrix()));
        }
    }

    /**
     * Enregistrer la vente
     */
    @FXML
    private void enregistrerVente() {
        errorLabel.setText("");

        // ✅ Validation
        if (clientSelectionne == null) {
            showError("⚠️ Veuillez sélectionner un client");
            return;
        }

        if (articleAVendre == null) {
            showError("⚠️ Aucun véhicule sélectionné");
            return;
        }

        String montantStr = montantField.getText().trim();
        if (montantStr.isEmpty()) {
            showError("⚠️ Veuillez saisir le montant");
            return;
        }

        double montant;
        try {
            montant = Double.parseDouble(montantStr);
            if (montant <= 0) {
                showError("⚠️ Le montant doit être positif");
                return;
            }
        } catch (NumberFormatException e) {
            showError("⚠️ Montant invalide");
            return;
        }

        String moyenPaiement = moyenPaiementCombo.getValue();
        String statut = statutCombo.getValue();
        LocalDate dateVente = dateVentePicker.getValue();
        String notes = notesArea.getText().trim();

        if (moyenPaiement == null || statut == null || dateVente == null) {
            showError("⚠️ Veuillez remplir tous les champs obligatoires");
            return;
        }

        try {
            // ✅ LOG + ELASTIC : début tentative création vente
            logger.info("Tentative création vente | Vendeur={} | Client={} | Article={} | Montant={}",
                    vendeurId, clientSelectionne.getId(), articleAVendre.getId(), montant);

            elasticLogService.sendLog(
                    "INFO",
                    "Tentative création vente",
                    Map.of(
                            "vendeurId", vendeurId,
                            "clientId", clientSelectionne.getId(),
                            "articleId", articleAVendre.getId(),
                            "montant", montant
                    )
            );

            // ✅ Création de la vente
            Vente nouvelleVente = new Vente();
            nouvelleVente.setIdClient(clientSelectionne.getId());
            nouvelleVente.setIdVendeur(vendeurId);
            nouvelleVente.setIdArticle(articleAVendre.getId());
            nouvelleVente.setMontantTotal(montant);
            nouvelleVente.setMoyenPaiement(moyenPaiement);
            nouvelleVente.setStatutVente(statut);

            LocalDateTime dateTimeVente = dateVente.atTime(LocalTime.now());
            nouvelleVente.setDateVente(dateTimeVente);

            nouvelleVente.setNomClient(clientSelectionne.getNom());
            nouvelleVente.setPrenomClient(clientSelectionne.getPrenom());
            nouvelleVente.setEmailClient(clientSelectionne.getEmail());
            nouvelleVente.setNomArticle(articleAVendre.getTitre());

            boolean succes = venteDAO.creerVente(nouvelleVente);

            if (succes) {
                // ✅ LOG + ELASTIC : Vente validée
                logger.info("Vente enregistrée avec succès | VenteID={}", nouvelleVente.getIdVente());

                elasticLogService.sendLog(
                        "INFO",
                        "Vente validée avec succès",
                        Map.of(
                                "venteId", nouvelleVente.getIdVente(),
                                "vendeurId", vendeurId,
                                "clientId", clientSelectionne.getId(),
                                "articleId", articleAVendre.getId(),
                                "montant", montant,
                                "moyenPaiement", moyenPaiement,
                                "statut", statut
                        )
                );

                showSuccess("✅ Vente enregistrée avec succès ! ID: #" + nouvelleVente.getIdVente());

                if ("terminée".equalsIgnoreCase(statut) || "terminee".equalsIgnoreCase(statut)) {
                    articleAVendre.setEtat("vendu");
                }

                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            if (dialogStage != null) {
                                dialogStage.close();
                            }
                        });
                    } catch (InterruptedException e) {
                        logger.error("Erreur lors de la fermeture automatique du formulaire", e);
                    }
                }).start();

            } else {
                // ✅ LOG + ELASTIC : échec vente
                logger.warn("Échec enregistrement vente | Client={} | Article={}",
                        clientSelectionne.getId(), articleAVendre.getId());

                elasticLogService.sendLog(
                        "WARN",
                        "Échec création vente",
                        Map.of(
                                "vendeurId", vendeurId,
                                "clientId", clientSelectionne.getId(),
                                "articleId", articleAVendre.getId()
                        )
                );

                showError("❌ Erreur lors de l'enregistrement de la vente");
            }

        } catch (SQLException e) {
            // ✅ LOG + ELASTIC : Erreur SQL
            logger.error("Erreur SQL lors de la création de la vente", e);

            elasticLogService.sendLog(
                    "ERROR",
                    "Erreur SQL création vente",
                    Map.of(
                            "vendeurId", vendeurId,
                            "message", e.getMessage()
                    )
            );

            showError("❌ Erreur base de données : " + e.getMessage());

        } catch (Exception e) {
            // ✅ LOG + ELASTIC : Erreur critique imprévue
            logger.error("Erreur inattendue lors de la création de la vente", e);

            elasticLogService.sendLog(
                    "ERROR",
                    "Erreur inattendue création vente",
                    Map.of(
                            "vendeurId", vendeurId,
                            "message", e.getMessage()
                    )
            );

            showError("❌ Erreur inattendue : " + e.getMessage());
        }
    }


    /**
     * Annuler
     */
    @FXML
    private void annuler() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Définir le Stage
     */
    public void setDialogStage(Stage stage) {
        this.dialogStage = stage;
    }

    /**
     * Afficher une erreur
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
    }

    /**
     * Afficher un succès
     */
    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
    }
}