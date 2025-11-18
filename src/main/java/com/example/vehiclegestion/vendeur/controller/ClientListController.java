package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.ClientDAO;
import com.example.vehiclegestion.vendeur.model.Client;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.List;

public class ClientListController implements Initializable {

    @FXML private TableView<Client> clientsTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label totalClientsLabel;
    @FXML private Label activeClientsLabel;
    @FXML private Label buyersLabel;

    private ClientDAO clientDAO;
    private ObservableList<Client> clientsList = FXCollections.observableArrayList();
    private final int VENDEUR_ID = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚀 Initialisation de la liste des clients...");

        // ✅ Initialiser ClientDAO avec gestion d'exception
        try {
            clientDAO = new ClientDAO();
            System.out.println("✅ Connexion à la base de données établie");
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
            e.printStackTrace();
            showError("Impossible de se connecter à la base de données.\nVérifiez votre configuration.");
            return; // Arrêter l'initialisation si la connexion échoue
        }

        setupTable();
        setupFilters();
        loadClients();
        updateStatistics();
    }

    private void setupTable() {
        clientsTable.setItems(clientsList);
        setupContextMenu();
    }

    private void setupFilters() {
        ObservableList<String> filterOptions = FXCollections.observableArrayList(
                "Tous les clients",
                "Actif",
                "Inactif",
                "Prospect",
                "Acheteur"
        );
        filterComboBox.setItems(filterOptions);
        filterComboBox.setValue("Tous les clients");

        filterComboBox.setOnAction(e -> filterClients());
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchClients());
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem viewItem = new MenuItem("👁️ Voir détails");
        viewItem.setOnAction(e -> viewClientDetails());

        MenuItem editItem = new MenuItem("✏️ Modifier");
        editItem.setOnAction(e -> editClient());

        MenuItem contactItem = new MenuItem("📞 Contacter");
        contactItem.setOnAction(e -> contactClient());

        MenuItem deleteItem = new MenuItem("🗑️ Supprimer");
        deleteItem.setOnAction(e -> deleteClient());

        contextMenu.getItems().addAll(viewItem, editItem, contactItem, new SeparatorMenuItem(), deleteItem);
        clientsTable.setContextMenu(contextMenu);
    }

    private void loadClients() {
        // ✅ Vérifier que clientDAO est initialisé
        if (clientDAO == null) {
            showError("La connexion à la base de données n'est pas disponible.");
            return;
        }

        try {
            List<Client> clients = clientDAO.getClientsByVendeur(VENDEUR_ID);
            clientsList.setAll(clients);
            System.out.println("✅ " + clients.size() + " clients chargés");
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors du chargement des clients: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur de chargement des clients: " + e.getMessage());
        }
    }

    private void filterClients() {
        String filter = filterComboBox.getValue();
        if (filter == null || filter.equals("Tous les clients")) {
            loadClients();
        } else {
            // Implémentez le filtrage selon le critère choisi
            System.out.println("Filtrage par: " + filter);
        }
    }

    private void searchClients() {
        String searchTerm = searchField.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadClients();
        } else {
            // ✅ Vérifier que clientDAO est initialisé
            if (clientDAO == null) {
                showError("La connexion à la base de données n'est pas disponible.");
                return;
            }

            try {
                List<Client> results = clientDAO.searchClients(searchTerm);
                clientsList.setAll(results);
                System.out.println("🔍 Recherche: " + searchTerm + " - " + results.size() + " résultats");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la recherche: " + e.getMessage());
                e.printStackTrace();
                showError("Erreur de recherche: " + e.getMessage());
            }
        }
    }

    private void updateStatistics() {
        int total = clientsList.size();
        int active = (int) clientsList.stream()
                .filter(c -> c.getStatutClient() != null &&
                        c.getStatutClient().equalsIgnoreCase("actif"))
                .count();
        int buyers = (int) clientsList.stream()
                .filter(c -> c.getStatutClient() != null &&
                        c.getStatutClient().equalsIgnoreCase("acheteur"))
                .count();

        totalClientsLabel.setText(String.valueOf(total));
        activeClientsLabel.setText(String.valueOf(active));
        buyersLabel.setText(String.valueOf(buyers));
    }

    @FXML
    private void addNewClient() {
        System.out.println("➕ Ajouter un nouveau client");
        // TODO: Ouvrir un formulaire d'ajout de client
        showAlert("Information", "Fonctionnalité à implémenter: Ajouter un client");
    }

    @FXML
    private void refreshClients() {
        System.out.println("🔄 Actualisation de la liste des clients...");
        loadClients();
        updateStatistics();
    }

    private void viewClientDetails() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("🔍 Voir détails: " + selected.getFullName());
            showAlert("Détails Client", "Détails de: " + selected.getFullName());
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un client");
        }
    }

    private void editClient() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("✏️ Modifier: " + selected.getFullName());
            showAlert("Modification", "Modifier: " + selected.getFullName());
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un client à modifier");
        }
    }

    private void contactClient() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("📞 Contacter: " + selected.getFullName());
            showAlert("Contact", "Contacter: " + selected.getFullName() + "\nTél: " + selected.getTelephone());
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un client à contacter");
        }
    }

    private void deleteClient() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation suppression");
            alert.setHeaderText("Supprimer le client");
            alert.setContentText("Êtes-vous sûr de vouloir supprimer " + selected.getFullName() + "?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                // ✅ Vérifier que clientDAO est initialisé
                if (clientDAO == null) {
                    showError("La connexion à la base de données n'est pas disponible.");
                    return;
                }

                try {
                    boolean success = clientDAO.deleteClient(selected.getId());
                    if (success) {
                        System.out.println("✅ Client supprimé: " + selected.getFullName());
                        loadClients();
                        updateStatistics();
                        showAlert("Succès", "Client supprimé avec succès");
                    } else {
                        showAlert("Erreur", "Impossible de supprimer le client");
                    }
                } catch (SQLException e) {
                    System.err.println("❌ Erreur lors de la suppression: " + e.getMessage());
                    e.printStackTrace();
                    showError("Erreur lors de la suppression: " + e.getMessage());
                }
            }
        } else {
            showAlert("Aucune sélection", "Veuillez sélectionner un client à supprimer");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Erreur de base de données");
        alert.setContentText(message);
        alert.showAndWait();
    }
}