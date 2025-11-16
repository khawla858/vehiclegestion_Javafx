package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.RendezVousDAO;
import com.example.vehiclegestion.vendeur.model.RendezVous;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class RendezVousListController {

    @FXML
    private TableView<RendezVous> rdvTable;

    @FXML
    private ComboBox<String> statutFilter;

    @FXML
    private DatePicker dateFilter;

    @FXML
    private TextField clientFilter;

    @FXML
    private DatePicker calendarPicker;

    @FXML
    private ListView<String> calendarList;

    private RendezVousDAO rendezVousDAO = new RendezVousDAO();

    private ObservableList<RendezVous> allRdv;
    private ObservableList<RendezVous> filteredRdv;

    private int idVendeurConnecte = 1;  // ⚠️ Exemple : à remplacer par vendeur connecté

    @FXML
    public void initialize() {
        allRdv = FXCollections.observableArrayList();
        filteredRdv = FXCollections.observableArrayList();
        rdvTable.setItems(filteredRdv);

        // Charger le statut dans ComboBox
        statutFilter.setItems(FXCollections.observableArrayList("Tous", "en attente", "confirmé", "annulé", "terminé"));
        statutFilter.getSelectionModel().selectFirst();

        loadRendezVousFromDB();

        // Listener du calendrier
        calendarPicker.valueProperty().addListener((obs, oldDate, newDate) -> filterByCalendar(newDate));
    }

    // 🔥 CHARGER LES RENDEZ-VOUS DE LA BASE
    private void loadRendezVousFromDB() {
        try {
            List<RendezVous> rdvs = rendezVousDAO.getRendezVousByVendeur(idVendeurConnecte, null, null, null);
            allRdv.setAll(rdvs);
            filteredRdv.setAll(rdvs);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les rendez-vous.");
        }
    }

    // 🔍 Appliquer les filtres
    @FXML
    private void refreshRendezVous() {
        String statut = statutFilter.getValue();
        LocalDate date = dateFilter.getValue();
        String client = clientFilter.getText().toLowerCase();

        List<RendezVous> filtered = allRdv.stream()
                .filter(r -> statut.equals("Tous") || r.getStatut().equalsIgnoreCase(statut))
                .filter(r -> date == null || r.getDateRdv().equals(date))
                .filter(r -> client.isEmpty() || r.getNomClient().toLowerCase().contains(client))
                .collect(Collectors.toList());

        filteredRdv.setAll(filtered);
    }

    // 📅 Filtrer le calendrier
    private void filterByCalendar(LocalDate date) {
        if (date != null) {
            List<String> rdvs = allRdv.stream()
                    .filter(r -> r.getDateRdv().equals(date))
                    .map(r -> r.getHeureRdv() + " - " + r.getNomClient() + " (" + r.getTitreArticle() + ")")
                    .collect(Collectors.toList());

            calendarList.setItems(FXCollections.observableArrayList(rdvs));
        } else {
            calendarList.getItems().clear();
        }
    }

    @FXML
    private void addRendezVous() {
        showAlert("Info", "Ouvrir la fenêtre d'ajout...");
    }

    @FXML
    private void exportRendezVous() {
        showAlert("Info", "Exporter : à implémenter.");
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
