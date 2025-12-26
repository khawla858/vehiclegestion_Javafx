package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.auth.utils.SessionManager;
import com.example.vehiclegestion.vendeur.dao.RendezVousDAO;
import com.example.vehiclegestion.vendeur.model.RendezVous;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import com.example.vehiclegestion.logging.service.ElasticLogService;
import com.example.vehiclegestion.logging.util.LoggerUtil;
import org.slf4j.Logger;
import java.util.HashMap;
import java.util.Map;


public class RendezVousListController {

    @FXML private TableView<RendezVous> rdvTable;
    @FXML private TableColumn<RendezVous, Integer> colId;
    @FXML private TableColumn<RendezVous, String> colClient;
    @FXML private TableColumn<RendezVous, String> colArticle;
    @FXML private TableColumn<RendezVous, LocalDate> colDate;
    @FXML private TableColumn<RendezVous, String> colHeure;
    @FXML private TableColumn<RendezVous, String> colStatut;
    @FXML private TableColumn<RendezVous, Void> colActions;

    @FXML private ComboBox<String> statutFilter;
    @FXML private DatePicker dateFilter;
    @FXML private TextField clientFilter;
    @FXML private DatePicker calendarPicker;
    @FXML private ListView<String> calendarList;
    @FXML private Label statsTotal;
    @FXML private Label statsEnAttente;
    @FXML private Label statsConfirme;
    @FXML private Label statsTermine;
    @FXML private TableColumn<RendezVous, String> colType;


    private RendezVousDAO rendezVousDAO;
    private ObservableList<RendezVous> allRdv;
    private ObservableList<RendezVous> filteredRdv;
    private SessionManager session = SessionManager.getInstance();
    private int idVendeurConnecte;

    // 🔑 Logger pour console et Elastic
    private static final Logger logger = LoggerUtil.getLogger(RendezVousListController.class);
    private static final ElasticLogService elasticLogService = new ElasticLogService();


    @FXML
    public void initialize() {
        System.out.println("\n📅 === INITIALISATION RendezVousListController ===");

        // ✅ Vérifier la session
        if (!session.estConnecte() || !session.estVendeur()) {
            System.err.println("❌ Aucun vendeur connecté");
            showAlert("Accès refusé", "Connectez-vous en tant que vendeur", Alert.AlertType.ERROR);
            return;
        }

        // ✅ Récupérer l'ID du vendeur
        idVendeurConnecte = session.getUserId();
        Utilisateur user = session.getUtilisateurConnecte();

        System.out.println("✅ Vendeur:");
        System.out.println("   ID: " + idVendeurConnecte);
        System.out.println("   Email: " + user.getEmail());
        System.out.println("   Nom: " + user.getNom() + " " + user.getPrenom());

        // ✅ Initialiser
        rendezVousDAO = new RendezVousDAO();
        allRdv = FXCollections.observableArrayList();
        filteredRdv = FXCollections.observableArrayList();
        rdvTable.setItems(filteredRdv);

        // ✅ Configurer les colonnes
        configureTableColumns();

        // ✅ Configurer les filtres
        if (statutFilter != null) {
            statutFilter.setItems(FXCollections.observableArrayList(
                    "Tous", "en attente", "confirmé", "annulé", "terminé"
            ));
            statutFilter.getSelectionModel().selectFirst();
            statutFilter.setOnAction(e -> refreshRendezVous());
        }

        if (dateFilter != null) {
            dateFilter.setOnAction(e -> refreshRendezVous());
        }

        if (clientFilter != null) {
            clientFilter.textProperty().addListener((obs, old, newVal) -> refreshRendezVous());
        }

        /*if (calendarPicker != null) {
            calendarPicker.valueProperty().addListener((obs, old, newDate) -> filterByCalendar(newDate));
        }*/

        // ✅ Charger les données
        loadRendezVousFromDB();

        System.out.println("📅 === FIN INITIALISATION ===\n");
    }

    private void configureTableColumns() {
        System.out.println("🔧 Configuration des colonnes...");

        // ID
        if (colId != null) {
            colId.setCellValueFactory(new PropertyValueFactory<>("idRdv"));
            colId.setStyle("-fx-alignment: CENTER;");
        }

        // Client
        if (colClient != null) {
            colClient.setCellValueFactory(new PropertyValueFactory<>("nomClient"));
            colClient.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        RendezVous rdv = getTableView().getItems().get(getIndex());
                        setText(item + "\n📞 " + rdv.getTelephoneClient());
                        setStyle("-fx-font-size: 13px; -fx-padding: 8;");
                    }
                }
            });
        }

        // Article
        if (colArticle != null) {
            colArticle.setCellValueFactory(new PropertyValueFactory<>("titreArticle"));
        }

        // Date
        if (colDate != null) {
            colDate.setCellValueFactory(new PropertyValueFactory<>("dateRdv"));
            colDate.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            });
        }

        // Heure
        if (colHeure != null) {
            colHeure.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(
                            cellData.getValue().getHeureRdv().toString()
                    )
            );
            colHeure.setStyle("-fx-alignment: CENTER;");
        }
        if (colType != null) {
            colType.setCellValueFactory(new PropertyValueFactory<>("typeRdv"));
            colType.setStyle("-fx-alignment: CENTER_LEFT; -fx-font-size: 13px;");
        }

        // Statut avec badges
        if (colStatut != null) {
            colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
            colStatut.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        Label badge = new Label(item.toUpperCase());
                        badge.setStyle(getStatutStyle(item));
                        badge.setAlignment(Pos.CENTER);
                        badge.setMaxWidth(Double.MAX_VALUE);
                        setGraphic(badge);
                    }
                }
            });
        }

        // Actions
        if (colActions != null) {
            colActions.setCellFactory(col -> new TableCell<>() {
                private final Button btnConfirmer = new Button("✓");
                private final Button btnAnnuler = new Button("✗");
                private final Button btnSupprimer = new Button("🗑");

                {
                    btnConfirmer.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10; -fx-cursor: hand;");
                    btnAnnuler.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10; -fx-cursor: hand;");
                    btnSupprimer.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 5 10; -fx-cursor: hand;");

                    btnConfirmer.setOnAction(e -> {
                        RendezVous rdv = getTableView().getItems().get(getIndex());
                        confirmerRendezVous(rdv);
                    });

                    btnAnnuler.setOnAction(e -> {
                        RendezVous rdv = getTableView().getItems().get(getIndex());
                        annulerRendezVous(rdv);
                    });

                    btnSupprimer.setOnAction(e -> {
                        RendezVous rdv = getTableView().getItems().get(getIndex());
                        supprimerRendezVous(rdv);
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        HBox actions = new HBox(5, btnConfirmer, btnAnnuler, btnSupprimer);
                        actions.setAlignment(Pos.CENTER);
                        setGraphic(actions);
                    }
                }
            });
        }

        System.out.println("✅ Colonnes configurées");
    }

    private String getStatutStyle(String statut) {
        switch (statut.toLowerCase()) {
            case "en attente":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-padding: 6 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 12px;";
            case "confirmé":
                return "-fx-background-color: #d1fae5; -fx-text-fill: #065f46; -fx-padding: 6 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 12px;";
            case "annulé":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 6 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 12px;";
            case "terminé":
                return "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-padding: 6 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 12px;";
            default:
                return "-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-padding: 6 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 12px;";
        }
    }

    private void loadRendezVousFromDB() {
        System.out.println("\n🔄 === CHARGEMENT DES RENDEZ-VOUS ===");
        System.out.println("   Vendeur ID: " + idVendeurConnecte);

        try {
            List<RendezVous> rdvs = rendezVousDAO.getRendezVousByVendeur(idVendeurConnecte, null, null, null);

            System.out.println("   ✅ " + rdvs.size() + " RDV chargés");

            allRdv.setAll(rdvs);
            filteredRdv.setAll(rdvs);
            updateStatistics();

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les rendez-vous", Alert.AlertType.ERROR);
        }

        System.out.println("🔄 === FIN CHARGEMENT ===\n");
    }

    private void updateStatistics() {
        if (statsTotal != null) {
            statsTotal.setText(String.valueOf(allRdv.size()));
        }
        if (statsEnAttente != null) {
            long count = allRdv.stream().filter(r -> "en attente".equals(r.getStatut())).count();
            statsEnAttente.setText(String.valueOf(count));
        }
        if (statsConfirme != null) {
            long count = allRdv.stream().filter(r -> "confirmé".equals(r.getStatut())).count();
            statsConfirme.setText(String.valueOf(count));
        }
        if (statsTermine != null) {
            long count = allRdv.stream().filter(r -> "terminé".equals(r.getStatut())).count();
            statsTermine.setText(String.valueOf(count));
        }
    }

    @FXML
    private void refreshRendezVous() {
        String statut = statutFilter != null ? statutFilter.getValue() : "Tous";
        LocalDate date = dateFilter != null ? dateFilter.getValue() : null;
        String client = clientFilter != null ? clientFilter.getText().toLowerCase() : "";

        List<RendezVous> filtered = allRdv.stream()
                .filter(r -> statut.equals("Tous") || r.getStatut().equalsIgnoreCase(statut))
                .filter(r -> date == null || r.getDateRdv().equals(date))
                .filter(r -> client.isEmpty() || r.getNomClient().toLowerCase().contains(client))
                .collect(Collectors.toList());

        filteredRdv.setAll(filtered);
        System.out.println("✅ " + filtered.size() + " résultat(s)");
    }

    /*private void filterByCalendar(LocalDate date) {
        if (calendarList == null) return;

        if (date != null) {
            List<String> rdvs = allRdv.stream()
                    .filter(r -> r.getDateRdv().equals(date))
                    .map(r -> r.getHeureRdv() + " - " + r.getNomClient() +
                            " (" + r.getTitreArticle() + ") [" + r.getStatut() + "]")
                    .collect(Collectors.toList());

            calendarList.setItems(FXCollections.observableArrayList(rdvs));
        } else {
            calendarList.getItems().clear();
        }
    }*/

    private void confirmerRendezVous(RendezVous rdv) {
        if (rendezVousDAO.confirmRendezVous(rdv.getIdRdv())) {
            showAlert("Succès", "Rendez-vous confirmé !", Alert.AlertType.INFORMATION);
            // Après confirmation réussie
            logger.info("RDV confirmé ID {} par vendeur {}", rdv.getIdRdv(), idVendeurConnecte);

            Map<String, Object> details = new HashMap<>();
            details.put("rdvId", rdv.getIdRdv());
            details.put("vendeurId", idVendeurConnecte);
            details.put("action", "confirmer");
            elasticLogService.sendLog("INFO", "RDV confirmé", details);

            loadRendezVousFromDB();
        } else {
            showAlert("Erreur", "Impossible de confirmer", Alert.AlertType.ERROR);
        }
    }

    private void annulerRendezVous(RendezVous rdv) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Annulation");
        dialog.setHeaderText("Annuler le RDV avec " + rdv.getNomClient());
        dialog.setContentText("Raison:");

        dialog.showAndWait().ifPresent(raison -> {
            if (rendezVousDAO.cancelRendezVous(rdv.getIdRdv(), raison)) {
                showAlert("Succès", "Rendez-vous annulé", Alert.AlertType.INFORMATION);
                // Après annulation réussie
                logger.warn("RDV annulé ID {} par vendeur {} | raison: {}", rdv.getIdRdv(), idVendeurConnecte, raison);

                Map<String, Object> details = new HashMap<>();
                details.put("rdvId", rdv.getIdRdv());
                details.put("vendeurId", idVendeurConnecte);
                details.put("raison", raison);
                elasticLogService.sendLog("WARN", "RDV annulé", details);

                loadRendezVousFromDB();
            } else {
                showAlert("Erreur", "Impossible d'annuler", Alert.AlertType.ERROR);
            }
        });
    }

    private void supprimerRendezVous(RendezVous rdv) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le rendez-vous ?");
        confirm.setContentText("Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (rendezVousDAO.deleteRendezVous(rdv.getIdRdv())) {
                    showAlert("Succès", "Rendez-vous supprimé", Alert.AlertType.INFORMATION);
                    // Après suppression réussie
                    logger.error("RDV supprimé ID {} par vendeur {}", rdv.getIdRdv(), idVendeurConnecte);

                    Map<String, Object> details = new HashMap<>();
                    details.put("rdvId", rdv.getIdRdv());
                    details.put("vendeurId", idVendeurConnecte);
                    elasticLogService.sendLog("ERROR", "RDV supprimé", details);

                    loadRendezVousFromDB();
                } else {
                    showAlert("Erreur", "Impossible de supprimer", Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void addRendezVous() {
        System.out.println("\n➕ === OUVERTURE FORMULAIRE AJOUT RDV ===");

        try {
            // Charger le FXML du formulaire
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/ajouter_rendezvous.fxml"));
            Parent root = loader.load();

            // Créer une nouvelle fenêtre
            Stage stage = new Stage();
            stage.setTitle("📅 Nouveau Rendez-vous");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Fenêtre modale
            stage.setResizable(false);

            // Afficher et attendre la fermeture
            stage.showAndWait();

            // Recharger les RDV après ajout
            System.out.println("🔄 Rechargement des RDV après ajout...");
            loadRendezVousFromDB();

        } catch (IOException e) {
            System.err.println("❌ Erreur ouverture formulaire: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void exportRendezVous() {
        showAlert("Info", "Export (à implémenter)", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}