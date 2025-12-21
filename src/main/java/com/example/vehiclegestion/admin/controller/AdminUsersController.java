package com.example.vehiclegestion.admin.controller;

import com.example.vehiclegestion.admin.service.AdminService;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.auth.utils.SessionManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller JavaFX pour la gestion des utilisateurs (Admin)
 */
public class AdminUsersController {

    // ========== COMPOSANTS FXML ==========

    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, Integer> idColumn;
    @FXML private TableColumn<Utilisateur, String> nomColumn;
    @FXML private TableColumn<Utilisateur, String> prenomColumn;
    @FXML private TableColumn<Utilisateur, String> emailColumn;
    @FXML private TableColumn<Utilisateur, String> roleColumn;
    @FXML private TableColumn<Utilisateur, String> statutColumn;
    @FXML private TableColumn<Utilisateur, String> dateCreationColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> roleFilterCombo;
    @FXML private ComboBox<String> statutFilterCombo;

    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button activateButton;
    @FXML private Button deactivateButton;
    @FXML private Button refreshButton;

    @FXML private Label totalUsersLabel;
    @FXML private Label selectedUserLabel;

    // ========== SERVICES ==========

    private final AdminService adminService;
    private final ObservableList<Utilisateur> usersList;

    public AdminUsersController() {
        this.adminService = new AdminService();
        this.usersList = FXCollections.observableArrayList();
    }

    /**
     * Initialisation du controller
     */
    @FXML
    public void initialize() {
        System.out.println("👥 Initialisation Admin Users...");

        // Configuration des colonnes
        setupTableColumns();

        // Configuration des filtres
        setupFilters();

        // Charger les données
        loadUsers();

        // Listener de sélection
        usersTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> updateSelectedUserInfo(newSelection)
        );

        System.out.println("✅ Admin Users initialisé");
    }

    /**
     * Configurer les colonnes de la table
     */
    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // Formater la date de création
        dateCreationColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateCreation() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String formatted = cellData.getValue().getDateCreation().format(formatter);
                return new SimpleStringProperty(formatted);
            }
            return new SimpleStringProperty("");
        });

        // Style pour le statut
        statutColumn.setCellFactory(column -> new TableCell<Utilisateur, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("actif".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Style pour le rôle
        roleColumn.setCellFactory(column -> new TableCell<Utilisateur, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    switch (item.toLowerCase()) {
                        case "admin":
                            setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                            break;
                        case "vendeur":
                            setStyle("-fx-text-fill: #1976d2; -fx-font-weight: bold;");
                            break;
                        case "client":
                            setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");
                            break;
                    }
                }
            }
        });

        usersTable.setItems(usersList);
    }

    /**
     * Configurer les filtres
     */
    private void setupFilters() {
        // ComboBox rôles
        roleFilterCombo.setItems(FXCollections.observableArrayList(
                "Tous", "Admin", "Vendeur", "Client"
        ));
        roleFilterCombo.setValue("Tous");

        // ComboBox statuts
        statutFilterCombo.setItems(FXCollections.observableArrayList(
                "Tous", "Actif", "Inactif"
        ));
        statutFilterCombo.setValue("Tous");

        // Listeners pour filtrage automatique
        searchField.textProperty().addListener((obs, old, newVal) -> handleSearch());
        roleFilterCombo.valueProperty().addListener((obs, old, newVal) -> handleSearch());
        statutFilterCombo.valueProperty().addListener((obs, old, newVal) -> handleSearch());
    }

    /**
     * Charger tous les utilisateurs
     */
    public void loadUsers() {
        try {
            List<Utilisateur> users = adminService.getAllUsers();
            usersList.clear();
            usersList.addAll(users);
            totalUsersLabel.setText("Total: " + users.size() + " utilisateur(s)");
            System.out.println("✅ " + users.size() + " utilisateurs chargés");
        } catch (Exception e) {
            showError("Erreur", "Impossible de charger les utilisateurs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Rechercher/Filtrer les utilisateurs
     */
    @FXML
    public void handleSearch() {
        String searchTerm = searchField.getText();
        String role = getRoleFilter();
        String statut = getStatutFilter();

        try {
            List<Utilisateur> users = adminService.searchUsers(searchTerm, role, statut);
            usersList.clear();
            usersList.addAll(users);
            totalUsersLabel.setText("Résultats: " + users.size() + " utilisateur(s)");
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    /**
     * Ajouter un utilisateur
     */
    @FXML
    public void handleAdd() {
        try {
            UserFormDialog dialog = new UserFormDialog();
            Optional<Utilisateur> result = dialog.showAndWait();

            if (result.isPresent()) {
                Utilisateur newUser = result.get();
                String adminEmail = SessionManager.getInstance().getUtilisateurConnecte().getEmail();

                boolean success = adminService.createUser(newUser, adminEmail);

                if (success) {
                    showSuccess("Utilisateur créé avec succès");
                    loadUsers();
                } else {
                    showError("Erreur", "Impossible de créer l'utilisateur");
                }
            }
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de la création: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Modifier un utilisateur
     */
    @FXML
    public void handleEdit() {
        Utilisateur selected = usersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un utilisateur à modifier");
            return;
        }

        try {
            UserFormDialog dialog = new UserFormDialog(selected);
            Optional<Utilisateur> result = dialog.showAndWait();

            if (result.isPresent()) {
                Utilisateur updatedUser = result.get();
                String adminEmail = SessionManager.getInstance().getUtilisateurConnecte().getEmail();

                boolean success = adminService.updateUser(updatedUser, adminEmail);

                if (success) {
                    showSuccess("Utilisateur mis à jour avec succès");
                    loadUsers();
                } else {
                    showError("Erreur", "Impossible de mettre à jour l'utilisateur");
                }
            }
        } catch (Exception e) {
            showError("Erreur", "Erreur lors de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Supprimer un utilisateur
     */
    @FXML
    public void handleDelete() {
        Utilisateur selected = usersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un utilisateur à supprimer");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " +
                selected.getPrenom() + " " + selected.getNom() + " ?");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String adminEmail = SessionManager.getInstance().getUtilisateurConnecte().getEmail();
                boolean success = adminService.deleteUser(selected.getIdUtilisateur(), adminEmail);

                if (success) {
                    showSuccess("Utilisateur supprimé avec succès");
                    loadUsers();
                } else {
                    showError("Erreur", "Impossible de supprimer l'utilisateur");
                }
            } catch (Exception e) {
                showError("Erreur", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    /**
     * Activer un utilisateur
     */
    @FXML
    public void handleActivate() {
        Utilisateur selected = usersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un utilisateur");
            return;
        }

        try {
            String adminEmail = SessionManager.getInstance().getUtilisateurConnecte().getEmail();
            boolean success = adminService.activateUser(selected.getIdUtilisateur(), adminEmail);

            if (success) {
                showSuccess("Utilisateur activé avec succès");
                loadUsers();
            } else {
                showError("Erreur", "Impossible d'activer l'utilisateur");
            }
        } catch (Exception e) {
            showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    /**
     * Désactiver un utilisateur
     */
    @FXML
    public void handleDeactivate() {
        Utilisateur selected = usersTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Aucune sélection", "Veuillez sélectionner un utilisateur");
            return;
        }

        try {
            String adminEmail = SessionManager.getInstance().getUtilisateurConnecte().getEmail();
            boolean success = adminService.deactivateUser(selected.getIdUtilisateur(), adminEmail);

            if (success) {
                showSuccess("Utilisateur désactivé avec succès");
                loadUsers();
            } else {
                showError("Erreur", "Impossible de désactiver l'utilisateur");
            }
        } catch (Exception e) {
            showError("Erreur", "Erreur: " + e.getMessage());
        }
    }

    /**
     * Rafraîchir la liste
     */
    @FXML
    public void handleRefresh() {
        searchField.clear();
        roleFilterCombo.setValue("Tous");
        statutFilterCombo.setValue("Tous");
        loadUsers();
    }

    /**
     * Mettre à jour les infos de l'utilisateur sélectionné
     */
    private void updateSelectedUserInfo(Utilisateur user) {
        if (user != null) {
            selectedUserLabel.setText("Sélectionné: " + user.getPrenom() + " " +
                    user.getNom() + " (" + user.getEmail() + ")");
        } else {
            selectedUserLabel.setText("Aucun utilisateur sélectionné");
        }
    }

    /**
     * Récupérer le filtre rôle
     */
    private String getRoleFilter() {
        String value = roleFilterCombo.getValue();
        return (value == null || value.equals("Tous")) ? null : value.toLowerCase();
    }

    /**
     * Récupérer le filtre statut
     */
    private String getStatutFilter() {
        String value = statutFilterCombo.getValue();
        return (value == null || value.equals("Tous")) ? null : value.toLowerCase();
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