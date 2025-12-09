package com.example.vehiclegestion.admin.controller;

import com.example.vehiclegestion.admin.dao.AdminMagasinDAO;
import com.example.vehiclegestion.vendeur.model.Magasin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class AdminMagasinsController {

    @FXML private TableView<Magasin> magasinsTable;
    @FXML private TableColumn<Magasin, Integer> idColumn;
    @FXML private TableColumn<Magasin, String> nomColumn;
    @FXML private TableColumn<Magasin, String> adresseColumn;
    @FXML private TableColumn<Magasin, String> localisationColumn;
    @FXML private TableColumn<Magasin, String> categorieColumn;
    @FXML private TableColumn<Magasin, String> telephoneColumn;
    @FXML private TableColumn<Magasin, String> emailColumn;
    @FXML private TableColumn<Magasin, Integer> vendeurColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categorieFilterCombo;
    @FXML private Label totalMagasinsLabel;
    @FXML private Label selectedMagasinLabel;

    @FXML private Button addMagasinButton;
    @FXML private Button refreshButton;
    @FXML private Button viewDetailsButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private AdminMagasinDAO magasinDAO;
    private ObservableList<Magasin> magasinsList;
    private ObservableList<Magasin> filteredMagasinsList;

    @FXML
    public void initialize() {
        magasinDAO = new AdminMagasinDAO();
        magasinsList = FXCollections.observableArrayList();
        filteredMagasinsList = FXCollections.observableArrayList();

        setupTableColumns();
        setupFilters();
        setupTableSelection();
        loadMagasins();
        updateButtonStates();
    }

    /**
     * Configuration des colonnes du tableau
     */
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idMagasin"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nomMagasin"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        localisationColumn.setCellValueFactory(new PropertyValueFactory<>("localisation"));
        categorieColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("emailContact"));
        vendeurColumn.setCellValueFactory(new PropertyValueFactory<>("idVendeur"));

        // Style des cellules pour la localisation GPS
        localisationColumn.setCellFactory(column -> new TableCell<Magasin, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("📍 " + item);
                    setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
                    setTooltip(new Tooltip("Coordonnées GPS: " + item));
                }
            }
        });

        // Double-clic pour voir les détails
        magasinsTable.setRowFactory(tv -> {
            TableRow<Magasin> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewDetails();
                }
            });
            return row;
        });
    }

    /**
     * Configuration des filtres
     */
    private void setupFilters() {
        // Remplir le ComboBox des catégories
        categorieFilterCombo.getItems().add("Toutes les catégories");
        categorieFilterCombo.getItems().addAll(
                "Voitures neuves", "Voitures d'occasion", "Voitures de luxe",
                "Voitures sportives", "4x4/SUV", "Utilitaires", "Motos",
                "Pièces détachées", "Services & Réparations"
        );
        categorieFilterCombo.setValue("Toutes les catégories");

        // Listeners pour les filtres
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterMagasins());
        categorieFilterCombo.valueProperty().addListener((obs, oldVal, newVal) -> filterMagasins());
    }

    /**
     * Configuration de la sélection dans le tableau
     */
    private void setupTableSelection() {
        magasinsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            updateButtonStates();
            if (newSelection != null) {
                selectedMagasinLabel.setText("Sélectionné: " + newSelection.getNomMagasin());
            } else {
                selectedMagasinLabel.setText("Aucun magasin sélectionné");
            }
        });
    }

    /**
     * Charger les magasins depuis la base de données
     */
    private void loadMagasins() {
        try {
            magasinsList.clear();
            magasinsList.addAll(AdminMagasinDAO.getAllMagasins());
            filterMagasins();
            totalMagasinsLabel.setText("Total: " + magasinsList.size() + " magasin(s)");
        } catch (Exception e) {
            showAlert("Erreur lors du chargement des magasins: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Filtrer les magasins selon les critères de recherche
     */
    private void filterMagasins() {
        filteredMagasinsList.clear();

        String searchText = searchField.getText().toLowerCase().trim();
        String selectedCategorie = categorieFilterCombo.getValue();

        for (Magasin magasin : magasinsList) {
            boolean matchesSearch = searchText.isEmpty() ||
                    magasin.getNomMagasin().toLowerCase().contains(searchText) ||
                    magasin.getAdresse().toLowerCase().contains(searchText) ||
                    (magasin.getLocalisation() != null && magasin.getLocalisation().toLowerCase().contains(searchText));

            boolean matchesCategorie = selectedCategorie.equals("Toutes les catégories") ||
                    (magasin.getCategorie() != null && magasin.getCategorie().equals(selectedCategorie));

            if (matchesSearch && matchesCategorie) {
                filteredMagasinsList.add(magasin);
            }
        }

        magasinsTable.setItems(filteredMagasinsList);
    }

    /**
     * Mettre à jour l'état des boutons
     */
    private void updateButtonStates() {
        boolean hasSelection = magasinsTable.getSelectionModel().getSelectedItem() != null;
        viewDetailsButton.setDisable(!hasSelection);
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    /**
     * Gérer l'ajout d'un nouveau magasin
     */
    @FXML
    private void handleAddMagasin() {
        MagasinFormDialog dialog = new MagasinFormDialog();
        Optional<Magasin> result = dialog.showAndWait();

        result.ifPresent(magasin -> {
            try {
                boolean success = AdminMagasinDAO.addMagasin(magasin);
                if (success) {
                    showAlert("Magasin créé avec succès!", Alert.AlertType.INFORMATION);
                    loadMagasins();
                } else {
                    showAlert("Erreur lors de la création du magasin", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Gérer le rafraîchissement des données
     */
    @FXML
    private void handleRefresh() {
        loadMagasins();
        showAlert("Données rafraîchies avec succès!", Alert.AlertType.INFORMATION);
    }

    /**
     * Gérer l'affichage des détails
     */
    @FXML
    private void handleViewDetails() {
        Magasin selectedMagasin = magasinsTable.getSelectionModel().getSelectedItem();
        if (selectedMagasin == null) {
            showAlert("Veuillez sélectionner un magasin", Alert.AlertType.WARNING);
            return;
        }

        // Créer un dialog pour afficher les détails
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du magasin");
        alert.setHeaderText(selectedMagasin.getNomMagasin());

        StringBuilder details = new StringBuilder();
        details.append("📋 ID: ").append(selectedMagasin.getIdMagasin()).append("\n\n");
        details.append("📍 Adresse: ").append(selectedMagasin.getAdresse()).append("\n");
        details.append("🌍 Localisation GPS: ").append(selectedMagasin.getLocalisation()).append("\n\n");
        details.append("📦 Catégorie: ").append(selectedMagasin.getCategorie()).append("\n");
        details.append("📞 Téléphone: ").append(selectedMagasin.getTelephone()).append("\n");

        if (selectedMagasin.getEmailContact() != null) {
            details.append("📧 Email: ").append(selectedMagasin.getEmailContact()).append("\n");
        }

        if (selectedMagasin.getSiteWeb() != null) {
            details.append("🌐 Site web: ").append(selectedMagasin.getSiteWeb()).append("\n");
        }

        if (selectedMagasin.getHoraires() != null) {
            details.append("🕐 Horaires: ").append(selectedMagasin.getHoraires()).append("\n");
        }

        if (selectedMagasin.getDescription() != null) {
            details.append("\n📝 Description:\n").append(selectedMagasin.getDescription());
        }

        alert.setContentText(details.toString());

        // Styliser l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e293b;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }

    /**
     * Gérer la modification d'un magasin
     */
    @FXML
    private void handleEdit() {
        Magasin selectedMagasin = magasinsTable.getSelectionModel().getSelectedItem();
        if (selectedMagasin == null) {
            showAlert("Veuillez sélectionner un magasin à modifier", Alert.AlertType.WARNING);
            return;
        }

        MagasinFormDialog dialog = new MagasinFormDialog(selectedMagasin);
        Optional<Magasin> result = dialog.showAndWait();

        result.ifPresent(magasin -> {
            try {
                boolean success = magasinDAO.updateMagasin(magasin);
                if (success) {
                    showAlert("Magasin modifié avec succès!", Alert.AlertType.INFORMATION);
                    loadMagasins();
                } else {
                    showAlert("Erreur lors de la modification du magasin", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Gérer la suppression d'un magasin
     */
    @FXML
    private void handleDelete() {
        Magasin selectedMagasin = magasinsTable.getSelectionModel().getSelectedItem();
        if (selectedMagasin == null) {
            showAlert("Veuillez sélectionner un magasin à supprimer", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText("Supprimer le magasin: " + selectedMagasin.getNomMagasin());
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer ce magasin?\nCette action est irréversible.");

        // Styliser l'alerte
        DialogPane dialogPane = confirmAlert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e293b;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = magasinDAO.deleteMagasin(selectedMagasin.getIdMagasin());
                if (success) {
                    showAlert("Magasin supprimé avec succès!", Alert.AlertType.INFORMATION);
                    loadMagasins();
                } else {
                    showAlert("Erreur lors de la suppression du magasin", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                showAlert("Erreur: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Afficher une alerte
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Erreur" :
                type == Alert.AlertType.WARNING ? "Attention" : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Styliser l'alerte
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #1e293b;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: white;");

        alert.showAndWait();
    }

}