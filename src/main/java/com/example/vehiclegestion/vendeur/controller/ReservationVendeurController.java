package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.ReservationDAO;
import com.example.vehiclegestion.vendeur.model.Reservation;
import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class ReservationVendeurController {

    @FXML private TableView<Reservation> reservationsTable;
    @FXML private TableColumn<Reservation, String> colClient;
    @FXML private TableColumn<Reservation, String> colArticle;
    @FXML private TableColumn<Reservation, String> colPrix;
    @FXML private TableColumn<Reservation, String> colStatut;
    @FXML private TableColumn<Reservation, String> colDate;
    @FXML private TableColumn<Reservation, String> colExpiration;

    @FXML private Label lblTotal;
    @FXML private Label lblEnAttente;
    @FXML private Label lblConfirmees;
    @FXML private Label lblAnnulees;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private TextField searchField;

    private ReservationDAO reservationDAO;
    private ObservableList<Reservation> reservations;
    private ObservableList<Reservation> allReservations;
    private SessionManager sessionManager;
    private int idVendeur;

    @FXML
    public void initialize() {
        try {
            sessionManager = SessionManager.getInstance();

            if (!sessionManager.estConnecte() || !sessionManager.estVendeur()) {
                showError("Accès refusé. Vous devez être connecté en tant que vendeur.");
                return;
            }

            idVendeur = sessionManager.getUserId();
            Utilisateur utilisateur = sessionManager.getUtilisateurConnecte();

            System.out.println("🔐 Vendeur connecté: " + utilisateur.getEmail() + " (ID: " + idVendeur + ")");

            reservationDAO = new ReservationDAO();
            setupTable();
            setupFilters();
            chargerReservations();
            updateStatistics();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation : " + e.getMessage());
        }
    }

    private void setupTable() {
        // Configuration des colonnes
        colClient.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomClient()));

        colArticle.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTitreArticle()));

        colPrix.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPrixFormatted()));

        colDate.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDateReservationFormatted()));

        colExpiration.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDateExpirationFormatted()));

        colStatut.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut()));

        // Style des cellules de statut
        colStatut.setCellFactory(column -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    String style = switch (item.toLowerCase()) {
                        case "en attente" -> "-fx-background-color: #fff3cd; -fx-text-fill: #856404; -fx-padding: 5; -fx-background-radius: 5;";
                        case "confirmée" -> "-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 5; -fx-background-radius: 5;";
                        case "annulée" -> "-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 5; -fx-background-radius: 5;";
                        case "expirée" -> "-fx-background-color: #d6d8db; -fx-text-fill: #383d41; -fx-padding: 5; -fx-background-radius: 5;";
                        default -> "";
                    };
                    setStyle(style);
                }
            }
        });
    }

    private void setupFilters() {
        ObservableList<String> filterOptions = FXCollections.observableArrayList(
                "Toutes les réservations",
                "En attente",
                "Confirmées",
                "Annulées",
                "Expirées"
        );
        filterComboBox.setItems(filterOptions);
        filterComboBox.setValue("Toutes les réservations");

        filterComboBox.setOnAction(e -> filterReservations());
        searchField.textProperty().addListener((obs, old, newVal) -> searchReservations());
    }

    private void chargerReservations() {
        try {
            if (!sessionManager.estConnecte() || !sessionManager.estVendeur()) {
                showError("Session expirée. Veuillez vous reconnecter.");
                return;
            }

            reservationDAO.updateExpiredReservations();
            allReservations = FXCollections.observableArrayList(
                    reservationDAO.getReservationsByVendeur(idVendeur)
            );
            reservations = FXCollections.observableArrayList(allReservations);
            reservationsTable.setItems(reservations);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur de chargement : " + e.getMessage());
        }
    }

    private void filterReservations() {
        String filter = filterComboBox.getValue();
        if (filter.equals("Toutes les réservations")) {
            reservations.setAll(allReservations);
        } else {
            String statutFilter = switch (filter) {
                case "En attente" -> "en attente";
                case "Confirmées" -> "confirmée";
                case "Annulées" -> "annulée";
                case "Expirées" -> "expirée";
                default -> "";
            };
            reservations.setAll(allReservations.stream()
                    .filter(r -> r.getStatut().equalsIgnoreCase(statutFilter))
                    .toList());
        }
    }

    private void searchReservations() {
        String search = searchField.getText().toLowerCase();
        if (search.isEmpty()) {
            reservations.setAll(allReservations);
        } else {
            reservations.setAll(allReservations.stream()
                    .filter(r -> r.getNomClient().toLowerCase().contains(search) ||
                            r.getTitreArticle().toLowerCase().contains(search))
                    .toList());
        }
    }

    @FXML
    private void refreshReservations() {
        if (!sessionManager.estConnecte() || !sessionManager.estVendeur()) {
            showError("Session expirée. Veuillez vous reconnecter.");
            return;
        }
        chargerReservations();
        updateStatistics();
    }

    private void updateStatistics() {
        try {
            lblTotal.setText(String.valueOf(allReservations.size()));
            lblEnAttente.setText(String.valueOf(
                    reservationDAO.countReservationsByStatus(idVendeur, "en attente")));
            lblConfirmees.setText(String.valueOf(
                    reservationDAO.countReservationsByStatus(idVendeur, "confirmée")));
            lblAnnulees.setText(String.valueOf(
                    reservationDAO.countReservationsByStatus(idVendeur, "annulée")));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}