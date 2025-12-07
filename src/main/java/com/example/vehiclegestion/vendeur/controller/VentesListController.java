package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.VenteDAO;
import com.example.vehiclegestion.vendeur.model.Vente;
import com.example.vehiclegestion.auth.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class VentesListController implements Initializable {

    @FXML private TableView<Vente> ventesTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterStatutComboBox;
    @FXML private ComboBox<String> filterMagasinComboBox;
    @FXML private ComboBox<String> filterDateComboBox;

    @FXML private Label totalVentesLabel;
    @FXML private Label chiffreAffairesLabel;
    @FXML private Label ventesEnCoursLabel;
    @FXML private Label ventesTermineesLabel;
    @FXML private Label countLabel;

    @FXML private ListView<String> topArticlesList;
    @FXML private ListView<String> recentVentesList;

    private VenteDAO venteDAO = new VenteDAO();
    private ObservableList<Vente> ventesList = FXCollections.observableArrayList();
    private int vendeurId;
    private SessionManager session = SessionManager.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚀 Initialisation de la gestion des ventes...");

        // Récupération du vendeur connecté
        if (!session.estConnecte() || !session.estVendeur()) {
            System.err.println("❌ Aucun vendeur connecté");
            showAlert("Accès refusé", "Connectez-vous en tant que vendeur");
            return;
        }

        // ✅ Récupérer l'ID du vendeur
        vendeurId = session.getUserId();

        setupTable();
        setupFilters();
        loadVentes();
        updateStatistics();
        loadTopArticles();
        loadRecentVentes();
    }

    private void setupTable() {
        ventesTable.setItems(ventesList);

        // Configuration des colonnes
        TableColumn<Vente, Integer> colId = (TableColumn<Vente, Integer>) ventesTable.getColumns().get(0);
        colId.setCellValueFactory(new PropertyValueFactory<>("idVente"));

        TableColumn<Vente, String> colDate = (TableColumn<Vente, String>) ventesTable.getColumns().get(1);
        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDateVenteFormatted()));

        TableColumn<Vente, String> colClient = (TableColumn<Vente, String>) ventesTable.getColumns().get(2);
        colClient.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getClientComplet()));

        TableColumn<Vente, String> colArticle = (TableColumn<Vente, String>) ventesTable.getColumns().get(3);
        colArticle.setCellValueFactory(new PropertyValueFactory<>("nomArticle"));

        TableColumn<Vente, String> colMagasin = (TableColumn<Vente, String>) ventesTable.getColumns().get(4);
        colMagasin.setCellValueFactory(new PropertyValueFactory<>("nomMagasin"));

        TableColumn<Vente, String> colMontant = (TableColumn<Vente, String>) ventesTable.getColumns().get(5);
        colMontant.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMontantFormatted()));

        TableColumn<Vente, String> colPaiement = (TableColumn<Vente, String>) ventesTable.getColumns().get(6);
        colPaiement.setCellValueFactory(new PropertyValueFactory<>("moyenPaiement"));

        // Colonne Statut avec badges colorés
        TableColumn<Vente, String> colStatut = (TableColumn<Vente, String>) ventesTable.getColumns().get(7);
        colStatut.setCellFactory(column -> new TableCell<Vente, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Vente vente = getTableRow().getItem();
                    Label badge = createStatutBadge(vente.getStatutVente());
                    setGraphic(badge);
                    setText(null);
                }
            }
        });
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statutVente"));

        setupActionsColumn();
        setupContextMenu();
    }

    private Label createStatutBadge(String statut) {
        Label badge = new Label();
        badge.setStyle("-fx-padding: 5 12; -fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        if (statut == null) {
            badge.setText("❓ Inconnu");
            badge.setStyle(badge.getStyle() + "-fx-background-color: #e2e8f0; -fx-text-fill: #64748b;");
        } else {
            switch (statut.toLowerCase()) {
                case "terminée":
                case "terminee":
                    badge.setText("✅ Terminée");
                    badge.setStyle(badge.getStyle() + "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a;");
                    break;
                case "en cours":
                    badge.setText("⏳ En cours");
                    badge.setStyle(badge.getStyle() + "-fx-background-color: #fef3c7; -fx-text-fill: #ca8a04;");
                    break;
                case "annulée":
                case "annulee":
                    badge.setText("❌ Annulée");
                    badge.setStyle(badge.getStyle() + "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626;");
                    break;
                default:
                    badge.setText("❓ " + statut);
                    badge.setStyle(badge.getStyle() + "-fx-background-color: #e2e8f0; -fx-text-fill: #64748b;");
            }
        }

        return badge;
    }

    private void setupActionsColumn() {
        TableColumn<Vente, Void> actionsColumn = (TableColumn<Vente, Void>) ventesTable.getColumns().get(8);

        actionsColumn.setCellFactory(param -> new TableCell<Vente, Void>() {
            private final Button viewBtn = new Button();
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox hbox = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                // Icônes
                FontIcon viewIcon = new FontIcon("fas-eye");
                viewIcon.setIconSize(14);
                viewIcon.setIconColor(Color.WHITE);

                FontIcon editIcon = new FontIcon("fas-edit");
                editIcon.setIconSize(14);
                editIcon.setIconColor(Color.WHITE);

                FontIcon deleteIcon = new FontIcon("fas-trash");
                deleteIcon.setIconSize(14);
                deleteIcon.setIconColor(Color.WHITE);

                viewBtn.setGraphic(viewIcon);
                editBtn.setGraphic(editIcon);
                deleteBtn.setGraphic(deleteIcon);

                // Tooltips
                viewBtn.setTooltip(new Tooltip("Voir détails"));
                editBtn.setTooltip(new Tooltip("Modifier statut"));
                deleteBtn.setTooltip(new Tooltip("Supprimer"));

                // Styles
                viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                        "-fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; " +
                        "-fx-min-width: 30; -fx-min-height: 30;");
                editBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; " +
                        "-fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; " +
                        "-fx-min-width: 30; -fx-min-height: 30;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; " +
                        "-fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; " +
                        "-fx-min-width: 30; -fx-min-height: 30;");

                hbox.setAlignment(Pos.CENTER);

                // Actions
                viewBtn.setOnAction(event -> {
                    Vente vente = getTableView().getItems().get(getIndex());
                    viewVenteDetails(vente);
                });

                editBtn.setOnAction(event -> {
                    Vente vente = getTableView().getItems().get(getIndex());
                    editVenteStatut(vente);
                });

                deleteBtn.setOnAction(event -> {
                    Vente vente = getTableView().getItems().get(getIndex());
                    deleteVente(vente);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : hbox);
            }
        });
    }

    private void setupFilters() {
        // Options de filtre statut
        ObservableList<String> statutOptions = FXCollections.observableArrayList(
                "Tous les statuts", "terminée", "en cours", "annulée"
        );
        filterStatutComboBox.setItems(statutOptions);
        filterStatutComboBox.setValue("Tous les statuts");

        // Options de filtre magasin (dynamique)
        List<String> magasins = venteDAO.getMagasinsByVendeur(vendeurId);
        ObservableList<String> magasinOptions = FXCollections.observableArrayList("Tous les magasins");
        magasinOptions.addAll(magasins);
        filterMagasinComboBox.setItems(magasinOptions);
        filterMagasinComboBox.setValue("Tous les magasins");

        // Options de filtre date
        ObservableList<String> dateOptions = FXCollections.observableArrayList(
                "Toutes les dates", "Aujourd'hui", "Cette semaine", "Ce mois", "Cette année"
        );
        filterDateComboBox.setItems(dateOptions);
        filterDateComboBox.setValue("Toutes les dates");

        // Listeners
        filterStatutComboBox.setOnAction(e -> applyFilters());
        filterMagasinComboBox.setOnAction(e -> applyFilters());
        filterDateComboBox.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem viewItem = new MenuItem("Voir détails");
        FontIcon viewIcon = new FontIcon("fas-eye");
        viewIcon.setIconSize(14);
        viewItem.setGraphic(viewIcon);
        viewItem.setOnAction(e -> {
            Vente selected = ventesTable.getSelectionModel().getSelectedItem();
            if (selected != null) viewVenteDetails(selected);
        });

        MenuItem editItem = new MenuItem("Modifier statut");
        FontIcon editIcon = new FontIcon("fas-edit");
        editIcon.setIconSize(14);
        editItem.setGraphic(editIcon);
        editItem.setOnAction(e -> {
            Vente selected = ventesTable.getSelectionModel().getSelectedItem();
            if (selected != null) editVenteStatut(selected);
        });

        MenuItem deleteItem = new MenuItem("Supprimer");
        FontIcon deleteIcon = new FontIcon("fas-trash");
        deleteIcon.setIconSize(14);
        deleteItem.setGraphic(deleteIcon);
        deleteItem.setOnAction(e -> {
            Vente selected = ventesTable.getSelectionModel().getSelectedItem();
            if (selected != null) deleteVente(selected);
        });

        contextMenu.getItems().addAll(viewItem, editItem, new SeparatorMenuItem(), deleteItem);
        ventesTable.setContextMenu(contextMenu);
    }

    private void loadVentes() {
        try {
            List<Vente> ventes = venteDAO.getVentesByVendeur(vendeurId);
            ventesList.setAll(ventes);
            countLabel.setText("Total: " + ventes.size() + " ventes");
            System.out.println("✅ " + ventes.size() + " ventes chargées");
        } catch (Exception e) {
            showError("Erreur de chargement", "Erreur lors du chargement des ventes: " + e.getMessage());
        }
    }

    private void applyFilters() {
        String statut = filterStatutComboBox.getValue();
        String magasin = filterMagasinComboBox.getValue();
        String periode = filterDateComboBox.getValue();
        String searchTerm = searchField.getText();

        try {
            List<Vente> ventes = venteDAO.getVentesWithFilters(vendeurId, statut, magasin, periode, searchTerm);
            ventesList.setAll(ventes);
            countLabel.setText("Total: " + ventes.size() + " ventes");
            updateStatistics();
        } catch (Exception e) {
            showError("Erreur de filtrage", "Erreur lors du filtrage: " + e.getMessage());
        }
    }

    private void updateStatistics() {
        Map<String, Object> stats = venteDAO.getStatistiques(vendeurId);

        totalVentesLabel.setText(String.valueOf(stats.get("total_ventes")));
        chiffreAffairesLabel.setText(String.format("%.0f DH", stats.get("chiffre_affaires")));
        ventesEnCoursLabel.setText(String.valueOf(stats.get("ventes_en_cours")));
        ventesTermineesLabel.setText(String.valueOf(stats.get("ventes_terminees")));
    }

    private void loadTopArticles() {
        List<Map<String, Object>> topArticles = venteDAO.getTopArticles(vendeurId);
        ObservableList<String> items = FXCollections.observableArrayList();

        for (Map<String, Object> article : topArticles) {
            String item = String.format("🏆 %s - %d ventes (%.0f DH)",
                    article.get("titre"),
                    article.get("nb_ventes"),
                    article.get("total_ca"));
            items.add(item);
        }

        topArticlesList.setItems(items);
    }

    private void loadRecentVentes() {
        List<Vente> recentVentes = venteDAO.getRecentVentes(vendeurId, 10);
        ObservableList<String> items = FXCollections.observableArrayList();

        for (Vente vente : recentVentes) {
            String item = String.format("💰 %s - %s (%s)",
                    vente.getClientComplet(),
                    vente.getMontantFormatted(),
                    vente.getDateVenteFormatted());
            items.add(item);
        }

        recentVentesList.setItems(items);
    }

    @FXML
    private void refreshVentes() {
        System.out.println("🔄 Actualisation des ventes...");
        loadVentes();
        updateStatistics();
        loadTopArticles();
        loadRecentVentes();
        showSuccessNotification("Actualisation réussie", "Les données ont été actualisées");
    }

    @FXML
    private void exportVentes() {
        System.out.println("📤 Export des ventes...");
        showAlert("Export", "Fonctionnalité d'export à implémenter (CSV, PDF, Excel)");
    }

    private void viewVenteDetails(Vente vente) {
        String details = String.format(
                "📋 DÉTAILS DE LA VENTE #%d\n\n" +
                        "👤 Client: %s\n" +
                        "📧 Email: %s\n" +
                        "🛒 Nombre de ventes: %d\n\n" +
                        "🚗 Article: %s\n" +
                        "🏪 Magasin: %s\n\n" +
                        "💰 Montant: %s\n" +
                        "💳 Paiement: %s\n" +
                        "📊 Statut: %s\n" +
                        "📅 Date: %s",
                vente.getIdVente(),
                vente.getClientComplet(),
                vente.getEmailClient(),
                vente.getNbVentesClient(),
                vente.getNomArticle(),
                vente.getNomMagasin(),
                vente.getMontantFormatted(),
                vente.getMoyenPaiement(),
                vente.getStatutVente(),
                vente.getDateVenteFormatted()
        );

        showAlert("Détails de la vente", details);
    }

    private void editVenteStatut(Vente vente) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("en cours", "terminée", "en cours", "annulée");
        dialog.setTitle("Modifier le statut");
        dialog.setHeaderText("Vente #" + vente.getIdVente());
        dialog.setContentText("Nouveau statut:");

        dialog.showAndWait().ifPresent(statut -> {
            if (venteDAO.updateStatutVente(vente.getIdVente(), statut)) {
                refreshVentes();
                showSuccessNotification("Mise à jour réussie", "Le statut a été modifié");
            } else {
                showError("Erreur", "Impossible de mettre à jour le statut");
            }
        });
    }

    private void deleteVente(Vente vente) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer la vente #" + vente.getIdVente());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette vente ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            if (venteDAO.deleteVente(vente.getIdVente())) {
                refreshVentes();
                showSuccessNotification("Suppression réussie", "La vente a été supprimée");
            } else {
                showError("Erreur", "Impossible de supprimer la vente");
            }
        }
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

    private void showSuccessNotification(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        FontIcon successIcon = new FontIcon("fas-check-circle");
        successIcon.setIconSize(24);
        successIcon.setIconColor(Color.GREEN);
        alert.setGraphic(successIcon);

        alert.showAndWait();
    }
}