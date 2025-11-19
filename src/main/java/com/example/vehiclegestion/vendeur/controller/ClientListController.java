package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.ClientDAO;
import com.example.vehiclegestion.vendeur.model.Client;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.List;
import javafx.scene.layout.HBox;           // ✅ AJOUTE CETTE LIGNE
import javafx.geometry.Pos;

public class ClientListController implements Initializable {

    @FXML private TableView<Client> clientsTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private Label totalClientsLabel;
    @FXML private Label activeClientsLabel;
    @FXML private Label buyersLabel;

    private ClientDAO clientDAO = new ClientDAO();
    private ObservableList<Client> clientsList = FXCollections.observableArrayList();
    private final int VENDEUR_ID = 1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚀 Initialisation de la liste des clients...");
        setupTable();
        setupFilters();
        loadClients();
        updateStatistics();
    }

    private void setupTable() {
        clientsTable.setItems(clientsList);
        setupContextMenu();
        setupActionsColumn(); // ✅ AJOUTE CETTE LIGNE


    }
    private void setupActionsColumn() {
        // Trouve la colonne Actions (la dernière colonne)
        TableColumn<Client, Void> actionsColumn = (TableColumn<Client, Void>) clientsTable.getColumns().get(9);

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("👁️");
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox hbox = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                // Style des boutons
                viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");

                hbox.setAlignment(Pos.CENTER);
                // Actions des boutons
                viewBtn.setOnAction(event -> {
                    Client client = getTableView().getItems().get(getIndex());
                    viewClientDetails(client);
                });

                editBtn.setOnAction(event -> {
                    Client client = getTableView().getItems().get(getIndex());
                    editClient(client);
                });

                deleteBtn.setOnAction(event -> {
                    Client client = getTableView().getItems().get(getIndex());
                    deleteClient(client);
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
        viewItem.setOnAction(e -> {
            Client selected = clientsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                viewClientDetails(selected);  // ✅ Passe le client
            } else {
                showAlert("Aucune sélection", "Veuillez sélectionner un client");
            }
        });

        MenuItem editItem = new MenuItem("✏️ Modifier");
        editItem.setOnAction(e -> {
            Client selected = clientsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                editClient(selected);  // ✅ Passe le client
            } else {
                showAlert("Aucune sélection", "Veuillez sélectionner un client à modifier");
            }
        });

        MenuItem contactItem = new MenuItem("📞 Contacter");
        contactItem.setOnAction(e -> contactClient());

        MenuItem deleteItem = new MenuItem("🗑️ Supprimer");
        deleteItem.setOnAction(e -> {
            Client selected = clientsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteClient(selected);  // ✅ Passe le client
            } else {
                showAlert("Aucune sélection", "Veuillez sélectionner un client à supprimer");
            }
        });

        contextMenu.getItems().addAll(viewItem, editItem, contactItem, new SeparatorMenuItem(), deleteItem);
        clientsTable.setContextMenu(contextMenu);
    }

    private void loadClients() {
        try {
            List<Client> clients = clientDAO.getClientsByVendeur(VENDEUR_ID);
            clientsList.setAll(clients);
            System.out.println("✅ " + clients.size() + " clients chargés");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de chargement des clients: " + e.getMessage());
        }
    }

    private void filterClients() {
        applyFilterAndSearch();
    }

    private void searchClients() {
        applyFilterAndSearch();
    }

    private void applyFilterAndSearch() {
        String filter = filterComboBox.getValue();
        String searchTerm = searchField.getText();

        try {
            List<Client> clients = clientDAO.getClientsByVendeurWithFilter(VENDEUR_ID, filter, searchTerm);
            clientsList.setAll(clients);
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de la récupération des clients : " + e.getMessage());
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

    private void viewClientDetails(Client client) {
        System.out.println("🔍 Voir détails: " + client.getFullName());
        showAlert("Détails Client", "Détails de: " + client.getFullName());
    }

    private void editClient(Client client) {
        System.out.println("✏️ Modifier: " + client.getFullName());
        showAlert("Modification", "Modifier: " + client.getFullName());
    }

    private void deleteClient(Client client) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation suppression");
        alert.setHeaderText("Supprimer les ventes du client");
        alert.setContentText("Voulez-vous supprimer toutes les ventes de " + client.getFullName() + " pour ce vendeur ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                boolean success = clientDAO.deleteClientVentes(client.getId());
                if (success) {
                    System.out.println("✅ Ventes du client supprimées: " + client.getFullName());
                    loadClients(); // recharge la table
                    updateStatistics();
                    showAlert("Succès", "Toutes les ventes du client ont été supprimées avec succès");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur lors de la suppression des ventes: " + e.getMessage());
            }
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
        alert.setHeaderText("Erreur de chargement");
        alert.setContentText(message);
        alert.showAndWait();
    }
}