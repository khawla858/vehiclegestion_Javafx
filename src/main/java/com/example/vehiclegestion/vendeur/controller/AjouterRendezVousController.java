package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.vendeur.dao.RendezVousDAO;
import com.example.vehiclegestion.vendeur.dao.ClientDAO;
import com.example.vehiclegestion.vendeur.dao.ArticleDAO;
import com.example.vehiclegestion.vendeur.model.RendezVous;
import com.example.vehiclegestion.vendeur.model.Client;
import com.example.vehiclegestion.vendeur.model.Article;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AjouterRendezVousController implements Initializable {

    @FXML private ComboBox<Client> clientCombo;
    @FXML private ComboBox<Article> vehiculeCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> heureCombo;
    @FXML private ComboBox<String> dureeCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextArea descriptionArea; // Sera retiré de l'interface car utilisé pour le type

    @FXML private VBox clientInfoBox;
    @FXML private VBox vehiculeInfoBox;
    @FXML private Label clientNomLabel;
    @FXML private Label clientEmailLabel;
    @FXML private Label clientTelLabel;
    @FXML private Label clientAdresseLabel;
    @FXML private Label vehiculeTitreLabel;
    @FXML private Label vehiculePrixLabel;
    @FXML private Label vehiculeMarqueLabel;
    @FXML private Label vehiculeAnneeLabel;

    private RendezVousDAO rendezVousDAO;
    private ClientDAO clientDAO;
    private ArticleDAO articleDAO;
    private SessionManager session = SessionManager.getInstance();
    private int idVendeurConnecte;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("📝 Initialisation formulaire ajout RDV");

        idVendeurConnecte = session.getUserId();
        rendezVousDAO = new RendezVousDAO();
        clientDAO = new ClientDAO();
        articleDAO = new ArticleDAO();

        // Configuration initiale
        setupForm();
        loadClients();
        loadVehicules();
        setupListeners();
    }

    private void setupForm() {
        // Date par défaut = aujourd'hui
        datePicker.setValue(LocalDate.now());

        // Heures disponibles (9h-18h)
        List<String> heures = IntStream.range(9, 18)
                .mapToObj(i -> String.format("%02d:00", i))
                .collect(Collectors.toList());
        heureCombo.setItems(FXCollections.observableArrayList(heures));
        heureCombo.getSelectionModel().selectFirst();

        // Durées possibles
        dureeCombo.setItems(FXCollections.observableArrayList(
                "30 minutes", "60 minutes", "90 minutes", "120 minutes"
        ));
        dureeCombo.getSelectionModel().select(1); // 60 minutes par défaut

        // Types de rendez-vous (stockés dans description)
        typeCombo.setItems(FXCollections.observableArrayList(
                "Visite véhicule", "Essai routier", "Négociation prix",
                "Signature contrat", "Livraison véhicule", "Service après-vente"
        ));
        typeCombo.getSelectionModel().selectFirst();

        // Masquer descriptionArea si présent (car maintenant c'est le type)
        if (descriptionArea != null) {
            descriptionArea.setVisible(false);
            descriptionArea.setManaged(false);
        }
    }

    private void loadClients() {
        try {
            List<Client> clients = clientDAO.getClientsByVendeur(idVendeurConnecte);
            clientCombo.setItems(FXCollections.observableArrayList(clients));

            // Personnaliser l'affichage des clients dans la ComboBox
            clientCombo.setCellFactory(param -> new ListCell<Client>() {
                @Override
                protected void updateItem(Client client, boolean empty) {
                    super.updateItem(client, empty);
                    if (empty || client == null) {
                        setText(null);
                    } else {
                        setText(client.getNomComplet() + " - " + client.getTelephone());
                    }
                }
            });

            clientCombo.setButtonCell(new ListCell<Client>() {
                @Override
                protected void updateItem(Client client, boolean empty) {
                    super.updateItem(client, empty);
                    if (empty || client == null) {
                        setText("Sélectionner un client");
                    } else {
                        setText(client.getNomComplet());
                    }
                }
            });

            System.out.println("✅ " + clients.size() + " clients chargés");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement clients: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger les clients", Alert.AlertType.ERROR);
        }
    }

    private void loadVehicules() {
        try {
            List<Article> vehicules = articleDAO.getArticlesByVendeur(idVendeurConnecte);
            vehiculeCombo.setItems(FXCollections.observableArrayList(vehicules));

            // Personnaliser l'affichage des véhicules
            vehiculeCombo.setCellFactory(param -> new ListCell<Article>() {
                @Override
                protected void updateItem(Article article, boolean empty) {
                    super.updateItem(article, empty);
                    if (empty || article == null) {
                        setText(null);
                    } else {
                        setText(article.getTitre() + " - " + String.format("%,.0f €", article.getPrix()));
                    }
                }
            });

            vehiculeCombo.setButtonCell(new ListCell<Article>() {
                @Override
                protected void updateItem(Article article, boolean empty) {
                    super.updateItem(article, empty);
                    if (empty || article == null) {
                        setText("Sélectionner un véhicule (optionnel)");
                    } else {
                        setText(article.getTitre());
                    }
                }
            });

            System.out.println("✅ " + vehicules.size() + " véhicules chargés");
        } catch (Exception e) {
            System.err.println("❌ Erreur chargement véhicules: " + e.getMessage());
        }
    }

    private void setupListeners() {
        // Quand un client est sélectionné
        clientCombo.valueProperty().addListener((obs, oldClient, newClient) -> {
            if (newClient != null) {
                updateClientInfo(newClient);
                clientInfoBox.setVisible(true);
            } else {
                clientInfoBox.setVisible(false);
            }
        });

        // Quand un véhicule est sélectionné
        vehiculeCombo.valueProperty().addListener((obs, oldVehicule, newVehicule) -> {
            if (newVehicule != null) {
                updateVehiculeInfo(newVehicule);
                vehiculeInfoBox.setVisible(true);
            } else {
                vehiculeInfoBox.setVisible(false);
            }
        });
    }

    private void updateClientInfo(Client client) {
        clientNomLabel.setText("Nom: " + client.getNomComplet());
        clientEmailLabel.setText("Email: " + client.getEmail());
        clientTelLabel.setText("Téléphone: " + client.getTelephone());
        clientAdresseLabel.setText("Adresse: " + (client.getAdresse() != null ? client.getAdresse() : "Non renseignée"));

        // Pré-remplir les champs
        telephoneField.setText(client.getTelephone());
        emailField.setText(client.getEmail());
    }

    private void updateVehiculeInfo(Article vehicule) {
        vehiculeTitreLabel.setText("Modèle: " + vehicule.getTitre());
        vehiculePrixLabel.setText("Prix: " + String.format("%,.0f €", vehicule.getPrix()));
        vehiculeMarqueLabel.setText("Marque: " + vehicule.getMarque());
        vehiculeAnneeLabel.setText("Année: " + vehicule.getAnnee());
    }

    @FXML
    private void enregistrer() {
        System.out.println("\n💾 Tentative d'enregistrement RDV...");

        // Validation
        if (!validateForm()) {
            return;
        }

        try {
            RendezVous rdv = new RendezVous();

            // Données du RDV
            Client client = clientCombo.getValue();
            rdv.setIdClient(client.getId());
            rdv.setIdVendeur(idVendeurConnecte);

            Article vehicule = vehiculeCombo.getValue();
            if (vehicule != null) {
                rdv.setIdArticle(vehicule.getId());
                rdv.setTitreArticle(vehicule.getTitre());
            } else {
                rdv.setIdArticle(0);
                rdv.setTitreArticle("Aucun véhicule spécifié");
            }

            rdv.setNomClient(client.getNomComplet());
            rdv.setTelephoneClient(telephoneField.getText());
            rdv.setEmailClient(emailField.getText());
            rdv.setDateRdv(datePicker.getValue());
            rdv.setHeureRdv(LocalTime.parse(heureCombo.getValue() + ":00"));

            // ✅ Le type est stocké dans typeRdv (qui sera sauvegardé dans description en BD)
            rdv.setTypeRdv(typeCombo.getValue());
            rdv.setStatut("en attente");
            rdv.setDuree(Integer.parseInt(dureeCombo.getValue().split(" ")[0]));

            // Sauvegarde
            if (rendezVousDAO.addRendezVous(rdv)) {
                System.out.println("✅ RDV enregistré avec succès! ID: " + rdv.getIdRdv());
                showAlert("Succès", "Rendez-vous créé avec succès!", Alert.AlertType.INFORMATION);
                fermerFenetre();
            } else {
                throw new Exception("Erreur lors de l'insertion en base");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur enregistrement: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de créer le rendez-vous: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (clientCombo.getValue() == null) {
            errors.append("• Sélectionnez un client\n");
        }
        if (datePicker.getValue() == null) {
            errors.append("• Sélectionnez une date\n");
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            errors.append("• La date ne peut pas être dans le passé\n");
        }
        if (heureCombo.getValue() == null) {
            errors.append("• Sélectionnez une heure\n");
        }
        if (dureeCombo.getValue() == null) {
            errors.append("• Sélectionnez une durée\n");
        }
        if (typeCombo.getValue() == null) {
            errors.append("• Sélectionnez un type de rendez-vous\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation", "Veuillez corriger les erreurs suivantes:\n\n" + errors, Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    @FXML
    private void annuler() {
        System.out.println("❌ Annulation création RDV");
        fermerFenetre();
    }

    private void fermerFenetre() {
        Stage stage = (Stage) clientCombo.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}