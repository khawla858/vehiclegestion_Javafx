package com.example.vehiclegestion.admin.controller;

import com.example.vehiclegestion.admin.service.AdminService;
import com.example.vehiclegestion.vendeur.model.Magasin;
import com.example.vehiclegestion.auth.utils.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.Optional;

/**
 * Controller JavaFX pour la gestion des magasins (Admin)
 */
public class AdminMagasinsController {

    // ========== COMPOSANTS FXML ==========

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

    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button viewDetailsButton;
    @FXML private Button refreshButton;

    @FXML private Label totalMagasinsLabel;
    @FXML private Label selectedMagasinLabel;

    // ========== SERVICES ==========

    private final AdminService adminService;
    private final ObservableList<Magasin> magasinsList;

    public AdminMagasinsController() {
        this.adminService = new AdminService();
        this.magasinsList = FXCollections.observableArrayList();
    }

    /**
     * Initialisation du controller
     */
    @FXML
    public void initialize() {
        System.out.println("🏪 Initialisation Admin Magasins...");

        // Configuration des colonnes
        setupTableColumns();

        // Configuration des filtres
        setupFilters();

        // Charger les données
        loadMagasins();

        // Listener de sélection
        magasinsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> updateSelectedMagasinInfo(newSelection)
        );

        System.out.println("✅ Admin Magasins initialisé");
    }

    /**
     * Configurer les colonnes de la table
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

        // Style pour la catégorie
        categorieColumn.setCellFactory(column -> new TableCell<Magasin, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText("Non catégorisé");
                    setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold;");
                }
            }
        });

        magasinsTable.setItems(magasinsList);
    }

    /**
     * Configurer les filtres
     */
    private void setupFilters() {
        // ComboBox catégories
        categorieFilterCombo.setItems(FXCollections.observableArrayList(
                "Toutes", "Voitures neuves", "Voitures d'occasion", "Voitures de luxe",
                "Voitures sportives", "4x4/SUV", "Utilitaires", "Motos", "Pièces détachées"
        ));
        categorieFilterCombo.setValue("Toutes");

        // Listeners pour filtrage automatique
        searchField.textProperty().addListener((obs, old, newVal) -> handleSearch());
        categorieFilterCombo.valueProperty().addListener((obs, old, newVal) -> handleSearch());
    }

    /**
     * Charger tous les magasins
     */
    public void loadMagasins() {
        try {
            List<Magasin> magasins = adminService.getAllMagasins();
            magasinsList.clear();
            magasinsList.addAll(magasins);
            totalMagasinsLabel.setText("Total: " + magasins.size() + " magasin(s)");
            System.out.println("✅ " + magasins.size() + " magasins chargés");
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les magasins: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rechercher/Filtrer les magasins
     */
    @FXML
    public void handleSearch() {
        String searchTerm = searchField.getText();
        String categorie = getCategorieFilter();

        try {
            List<Magasin> magasins = adminService.searchMagasins(searchTerm, categorie);
            magasinsList.clear();
            magasinsList.addAll(magasins);
            totalMagasinsLabel.setText("Résultats: " + magasins.size() + " magasin(s)");
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    /**
     * Modifier un magasin
     */
    @FXML
    public void handleEdit() {
        Magasin selected = magasinsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un magasin à modifier");
            return;
        }

        try {
            MagasinFormDialog dialog = new MagasinFormDialog(selected);
            Optional<Magasin> result = dialog.showAndWait();

            if (result.isPresent()) {
                Magasin updatedMagasin = result.get();
                String adminEmail = SessionManager.getInstance().getUtilisateurConnecte().getEmail();

                boolean success = adminService.updateMagasin(updatedMagasin, adminEmail);

                if (success) {
                    showSuccess("Magasin mis à jour avec succès");
                    loadMagasins();
                } else {
                    showError("Erreur", "Impossible de mettre à jour le magasin");
                }
            }
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Supprimer un magasin
     */
    @FXML
    public void handleDelete() {
        Magasin selected = magasinsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un magasin à supprimer");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le magasin ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" +
                selected.getNomMagasin() + "\" ?\n\n" +
                "⚠️ ATTENTION : Tous les véhicules associés seront également supprimés !");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String adminEmail = SessionManager.getInstance().getUtilisateurConnecte().getEmail();
                boolean success = adminService.deleteMagasin(selected.getIdMagasin(), adminEmail);

                if (success) {
                    showSuccess("Magasin supprimé avec succès");
                    loadMagasins();
                } else {
                    showError("Erreur", "Impossible de supprimer le magasin");
                }
            } catch (Exception e) {
                showError("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    /**
     * Voir les détails d'un magasin
     */
    @FXML
    public void handleViewDetails() {
        Magasin selected = magasinsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un magasin");
            return;
        }

        // Créer une fenêtre de détails
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Détails du magasin");
        details.setHeaderText(selected.getNomMagasin());

        StringBuilder content = new StringBuilder();
        content.append("ID: ").append(selected.getIdMagasin()).append("\n");
        content.append("Nom: ").append(selected.getNomMagasin()).append("\n");
        content.append("Adresse: ").append(selected.getAdresse()).append("\n");
        content.append("Localisation: ").append(selected.getLocalisation()).append("\n");
        content.append("Catégorie: ").append(selected.getCategorie() != null ? selected.getCategorie() : "Non définie").append("\n");
        content.append("Téléphone: ").append(selected.getTelephone() != null ? selected.getTelephone() : "Non renseigné").append("\n");
        content.append("Email: ").append(selected.getEmailContact() != null ? selected.getEmailContact() : "Non renseigné").append("\n");
        content.append("Site web: ").append(selected.getSiteWeb() != null ? selected.getSiteWeb() : "Non renseigné").append("\n");
        content.append("Facebook: ").append(selected.getFacebook() != null ? selected.getFacebook() : "Non renseigné").append("\n");
        content.append("Instagram: ").append(selected.getInstagram() != null ? selected.getInstagram() : "Non renseigné").append("\n");
        content.append("Description: ").append(selected.getDescription() != null ? selected.getDescription() : "Aucune description").append("\n");
        content.append("Horaires: ").append(selected.getHoraires() != null ? selected.getHoraires() : "Non renseignés").append("\n");
        content.append("\n");
        content.append("ID Vendeur: ").append(selected.getIdVendeur()).append("\n");

        details.setContentText(content.toString());
        details.showAndWait();
    }

    /**
     * Rafraîchir la liste
     */
    @FXML
    public void handleRefresh() {
        searchField.clear();
        categorieFilterCombo.setValue("Toutes");
        loadMagasins();
    }

    /**
     * Mettre à jour les infos du magasin sélectionné
     */
    private void updateSelectedMagasinInfo(Magasin magasin) {
        if (magasin != null) {
            selectedMagasinLabel.setText("Sélectionné: " + magasin.getNomMagasin() +
                    " (" + magasin.getLocalisation() + ")");
        } else {
            selectedMagasinLabel.setText("Aucun magasin sélectionné");
        }
    }

    /**
     * Récupérer le filtre catégorie
     */
    private String getCategorieFilter() {
        String value = categorieFilterCombo.getValue();
        return (value == null || value.equals("Toutes")) ? null : value;
    }

    // ========== ALERTES ==========

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
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

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}