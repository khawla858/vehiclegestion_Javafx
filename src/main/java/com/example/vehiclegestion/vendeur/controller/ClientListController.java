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
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import com.example.vehiclegestion.auth.SessionManager;
import org.kordamp.ikonli.javafx.FontIcon;

public class ClientListController implements Initializable {

    @FXML private TableView<Client> clientsTable;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private Label totalClientsLabel;
    @FXML private Label activeClientsLabel;
    @FXML private Label buyersLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label countLabel;
    @FXML private ListView<String> topClientsList;
    @FXML private ListView<String> recentActivityList;

    private ClientDAO clientDAO = new ClientDAO();
    private ObservableList<Client> clientsList = FXCollections.observableArrayList();
    private int vendeurId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚀 Initialisation de la liste des clients...");

        // Récupération du vendeur connecté
        if (SessionManager.getInstance().estVendeur()) {
            vendeurId = SessionManager.getInstance().getUserId();
            System.out.println("🟢 Vendeur connecté ID = " + vendeurId);
        } else {
            System.err.println("❌ Erreur : Aucun vendeur connecté !");
            showError("Erreur d'authentification", "Aucun vendeur connecté !");
            return;
        }

        setupTable();
        setupFilters();
        loadClients();
        updateStatistics();
        loadTopClients();
        loadRecentActivity();
    }

    private void setupTable() {
        clientsTable.setItems(clientsList);
        setupContextMenu();
        setupActionsColumn();
    }

    private void setupActionsColumn() {
        TableColumn<Client, Void> actionsColumn = (TableColumn<Client, Void>) clientsTable.getColumns().get(7);

        actionsColumn.setCellFactory(param -> new TableCell<Client, Void>() {
            private final Button viewBtn = new Button();
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            private final HBox hbox = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                // Configuration des icônes FontAwesome
                FontIcon viewIcon = new FontIcon("fas-eye");
                viewIcon.setIconSize(14);
                viewIcon.setIconColor(javafx.scene.paint.Color.WHITE);

                FontIcon editIcon = new FontIcon("fas-edit");
                editIcon.setIconSize(14);
                editIcon.setIconColor(javafx.scene.paint.Color.WHITE);

                FontIcon deleteIcon = new FontIcon("fas-trash");
                deleteIcon.setIconSize(14);
                deleteIcon.setIconColor(javafx.scene.paint.Color.WHITE);

                viewBtn.setGraphic(viewIcon);
                editBtn.setGraphic(editIcon);
                deleteBtn.setGraphic(deleteIcon);

                // Tooltips
                viewBtn.setTooltip(new Tooltip("Voir détails"));
                editBtn.setTooltip(new Tooltip("Modifier"));
                deleteBtn.setTooltip(new Tooltip("Supprimer"));

                // Styles améliorés
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

                // Hover effects
                viewBtn.setOnMouseEntered(e -> viewBtn.setStyle(
                        "-fx-background-color: #2563eb; -fx-text-fill: white; " +
                                "-fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; " +
                                "-fx-min-width: 30; -fx-min-height: 30;"));
                viewBtn.setOnMouseExited(e -> viewBtn.setStyle(
                        "-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                                "-fx-padding: 6 10; -fx-background-radius: 6; -fx-cursor: hand; " +
                                "-fx-min-width: 30; -fx-min-height: 30;"));

                // Actions
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
        // Configuration de l'icône pour le champ de recherche
        FontIcon searchIcon = new FontIcon("fas-search");
        searchIcon.setIconSize(14);
        searchIcon.setIconColor(javafx.scene.paint.Color.GRAY);

        HBox searchContainer = new HBox(5);
        searchContainer.setAlignment(Pos.CENTER_LEFT);
        searchContainer.getChildren().addAll(searchIcon, searchField);

        // Options de filtrage avec icônes
        ObservableList<String> filterOptions = FXCollections.observableArrayList(
                "Tous les clients",
                "Actif",
                "Inactif",
                "Prospect",
                "Acheteur"
        );
        filterComboBox.setItems(filterOptions);
        filterComboBox.setValue("Tous les clients");

        // Options de tri avec icônes
        ObservableList<String> sortOptions = FXCollections.observableArrayList(
                "Nom (A-Z)",
                "Nom (Z-A)",
                "Date récente",
                "Plus de ventes",
                "Plus dépensé"
        );
        sortComboBox.setItems(sortOptions);
        sortComboBox.setValue("Nom (A-Z)");

        // Listeners
        filterComboBox.setOnAction(e -> applyFilterAndSearch());
        sortComboBox.setOnAction(e -> applySorting());
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilterAndSearch());
    }

    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem viewItem = new MenuItem("Voir détails");
        FontIcon viewIcon = new FontIcon("fas-eye");
        viewIcon.setIconSize(14);
        viewItem.setGraphic(viewIcon);
        viewItem.setOnAction(e -> {
            Client selected = clientsTable.getSelectionModel().getSelectedItem();
            if (selected != null) viewClientDetails(selected);
            else showAlert("Aucune sélection", "Veuillez sélectionner un client");
        });

        MenuItem editItem = new MenuItem("Modifier");
        FontIcon editIcon = new FontIcon("fas-edit");
        editIcon.setIconSize(14);
        editItem.setGraphic(editIcon);
        editItem.setOnAction(e -> {
            Client selected = clientsTable.getSelectionModel().getSelectedItem();
            if (selected != null) editClient(selected);
            else showAlert("Aucune sélection", "Veuillez sélectionner un client à modifier");
        });

        MenuItem contactItem = new MenuItem("Contacter");
        FontIcon contactIcon = new FontIcon("fas-phone");
        contactIcon.setIconSize(14);
        contactItem.setGraphic(contactIcon);
        contactItem.setOnAction(e -> contactClient());

        MenuItem deleteItem = new MenuItem("Supprimer");
        FontIcon deleteIcon = new FontIcon("fas-trash");
        deleteIcon.setIconSize(14);
        deleteItem.setGraphic(deleteIcon);
        deleteItem.setOnAction(e -> {
            Client selected = clientsTable.getSelectionModel().getSelectedItem();
            if (selected != null) deleteClient(selected);
            else showAlert("Aucune sélection", "Veuillez sélectionner un client à supprimer");
        });

        contextMenu.getItems().addAll(viewItem, editItem, contactItem, new SeparatorMenuItem(), deleteItem);
        clientsTable.setContextMenu(contextMenu);
    }

    private void loadClients() {
        try {
            List<Client> clients = clientDAO.getClientsByVendeur(vendeurId);
            clientsList.setAll(clients);
            countLabel.setText("Total: " + clients.size() + " clients");
            System.out.println("✅ " + clients.size() + " clients chargés");
        } catch (Exception e) {
            showError("Erreur de chargement", "Erreur lors du chargement des clients : " + e.getMessage());
        }
    }

    private void applyFilterAndSearch() {
        String filter = filterComboBox.getValue();
        String searchTerm = searchField.getText();

        try {
            List<Client> clients = clientDAO.getClientsByVendeurWithFilter(vendeurId, filter, searchTerm);
            clientsList.setAll(clients);
            countLabel.setText("Total: " + clients.size() + " clients");
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur de filtrage", "Erreur lors de la récupération : " + e.getMessage());
        }
    }

    private void applySorting() {
        String sortOption = sortComboBox.getValue();

        if (sortOption.equals("Nom (A-Z)")) {
            clientsList.sort((c1, c2) -> c1.getFullName().compareToIgnoreCase(c2.getFullName()));
        } else if (sortOption.equals("Nom (Z-A)")) {
            clientsList.sort((c1, c2) -> c2.getFullName().compareToIgnoreCase(c1.getFullName()));
        } else if (sortOption.equals("Plus de ventes")) {
            clientsList.sort((c1, c2) -> Integer.compare(c2.getNbVentes(), c1.getNbVentes()));
        } else if (sortOption.equals("Plus dépensé")) {
            clientsList.sort((c1, c2) -> Double.compare(c2.getTotalDepense(), c1.getTotalDepense()));
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

        double totalRevenue = clientsList.stream()
                .mapToDouble(Client::getTotalDepense)
                .sum();

        totalClientsLabel.setText(String.valueOf(total));
        activeClientsLabel.setText(String.valueOf(active));
        buyersLabel.setText(String.valueOf(buyers));
        totalRevenueLabel.setText(String.format("%.0f DH", totalRevenue));
    }

    private void loadTopClients() {
        ObservableList<String> topClients = FXCollections.observableArrayList();
        clientsList.stream()
                .sorted((c1, c2) -> Double.compare(c2.getTotalDepense(), c1.getTotalDepense()))
                .limit(5)
                .forEach(c -> topClients.add(
                        String.format("🏆 %s - %.0f DH (%d ventes)",
                                c.getFullName(), c.getTotalDepense(), c.getNbVentes())
                ));

        if (topClientsList != null) {
            topClientsList.setItems(topClients);
        }
    }

    private void loadRecentActivity() {
        ObservableList<String> activities = FXCollections.observableArrayList(
                "📝 Client ajouté récemment",
                "💰 Nouvelle vente enregistrée",
                "✏️ Informations client mises à jour",
                "📞 Contact client effectué",
                "🔔 Rappel de suivi"
        );

        if (recentActivityList != null) {
            recentActivityList.setItems(activities);
        }
    }

    @FXML
    private void addNewClient() {
        System.out.println("➕ Ajouter un nouveau client");
        showAlert("Information", "Fonctionnalité à implémenter: Ajouter un client");
    }

    @FXML
    private void refreshClients() {
        System.out.println("🔄 Actualisation de la liste des clients...");
        loadClients();
        updateStatistics();
        loadTopClients();
        loadRecentActivity();

        // Afficher une notification de succès
        showSuccessNotification("Liste actualisée", "Les données clients ont été actualisées avec succès");
    }

    private void viewClientDetails(Client client) {
        System.out.println("🔍 Voir détails: " + client.getFullName());
        showAlert("Détails Client",
                "Nom: " + client.getFullName() + "\n" +
                        "Email: " + client.getEmail() + "\n" +
                        "Statut: " + client.getStatutClient() + "\n" +
                        "Ventes: " + client.getNbVentes() + "\n" +
                        "Total dépensé: " + client.getTotalDepense() + " DH");
    }

    private void editClient(Client client) {
        System.out.println("✏️ Modifier: " + client.getFullName());
        showAlert("Modification", "Modifier: " + client.getFullName());
    }

    private void deleteClient(Client client) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation suppression");
        alert.setHeaderText("Supprimer les ventes du client");
        alert.setContentText("Voulez-vous supprimer toutes les ventes de " +
                client.getFullName() + " pour ce vendeur ?");

        // Ajouter une icône à l'alerte
        FontIcon warningIcon = new FontIcon("fas-exclamation-triangle");
        warningIcon.setIconSize(24);
        warningIcon.setIconColor(javafx.scene.paint.Color.ORANGE);
        alert.setGraphic(warningIcon);

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                boolean success = clientDAO.deleteClientVentes(client.getId(), vendeurId);
                if (success) {
                    System.out.println("✅ Ventes supprimées: " + client.getFullName());
                    refreshClients();
                    showSuccessNotification("Suppression réussie", "Toutes les ventes du client ont été supprimées");
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Erreur de suppression", "Erreur lors de la suppression: " + e.getMessage());
            }
        }
    }

    private void contactClient() {
        Client selected = clientsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("📞 Contacter: " + selected.getFullName());
            showAlert("Contact", "Contacter: " + selected.getFullName() +
                    "\nEmail: " + selected.getEmail());
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
        successIcon.setIconColor(javafx.scene.paint.Color.GREEN);
        alert.setGraphic(successIcon);

        alert.showAndWait();
    }
}