package com.example.vehiclegestion.client.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.client.service.HistoriqueService;
import com.example.vehiclegestion.client.model.HistoriqueItem;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HistoriqueController implements Initializable {

    // ==================== COMPOSANTS FXML ====================
    @FXML private BorderPane mainContainer;
    @FXML private ComboBox<String> periodeCombo;
    @FXML private ComboBox<String> typeCombo;
    @FXML private Button btnRefresh;
    @FXML private Button btnExport;
    @FXML private TextField searchField;

    // Containers
    @FXML private VBox historiqueContainer;
    @FXML private ScrollPane scrollContainer;
    @FXML private VBox tableSection;
    @FXML private StackPane tableContainer;
    @FXML private Button btnHideTable;

    // Cartes statistiques
    @FXML private Label lblTotalActions;
    @FXML private Label lblTotalRDV;
    @FXML private Label lblTotalVentes;
    @FXML private Label lblTotalAvis;
    @FXML private Label lblTotalDepense;
    @FXML private Label lblMoyenneNotes;

    // Tableaux
    @FXML private TableView<HistoriqueItem> rdvTableView;
    @FXML private TableView<HistoriqueItem> ventesTableView;
    @FXML private TableView<HistoriqueItem> commentairesTableView;

    // Colonnes pour RDV
    @FXML private TableColumn<HistoriqueItem, String> rdvDateCol;
    @FXML private TableColumn<HistoriqueItem, String> rdvHeureCol;
    @FXML private TableColumn<HistoriqueItem, String> rdvArticleCol;
    @FXML private TableColumn<HistoriqueItem, String> rdvVendeurCol;
    @FXML private TableColumn<HistoriqueItem, String> rdvStatutCol;
    @FXML private TableColumn<HistoriqueItem, String> rdvNotesCol;

    // Colonnes pour Ventes
    @FXML private TableColumn<HistoriqueItem, String> venteDateCol;
    @FXML private TableColumn<HistoriqueItem, String> venteArticleCol;
    @FXML private TableColumn<HistoriqueItem, Double> venteMontantCol;
    @FXML private TableColumn<HistoriqueItem, String> venteStatutCol;
    @FXML private TableColumn<HistoriqueItem, String> ventePaiementCol;
    @FXML private TableColumn<HistoriqueItem, String> venteDetailsCol;

    // Colonnes pour Commentaires
    @FXML private TableColumn<HistoriqueItem, String> comDateCol;
    @FXML private TableColumn<HistoriqueItem, String> comArticleCol;
    @FXML private TableColumn<HistoriqueItem, Integer> comNoteCol;
    // Dans HistoriqueController.java, ajoutez cette variable
    @FXML private VBox historiqueContent;
    @FXML private TableColumn<HistoriqueItem, String> comTitreCol;
    @FXML private TableColumn<HistoriqueItem, String> comTexteCol;
    @FXML private TableColumn<HistoriqueItem, String> comStatutCol;

    // Labels des tableaux
    @FXML private Label tableTitle;
    @FXML private Label tableSubtitle;
    @FXML private Label tableCount;

    // ==================== VARIABLES ====================
    private SessionManager sessionManager;
    private HistoriqueService historiqueService;
    private Timeline autoRefreshTimeline;
    private String currentPeriode = "Toutes périodes";
    private String currentType = "Tous";
    private int clientId;
    private List<HistoriqueItem> currentHistorique;

    // Données pour tableaux
    private ObservableList<HistoriqueItem> rdvTableData = FXCollections.observableArrayList();
    private ObservableList<HistoriqueItem> ventesTableData = FXCollections.observableArrayList();
    private ObservableList<HistoriqueItem> commentairesTableData = FXCollections.observableArrayList();

    // Couleurs
    private static final String COLOR_PRIMARY = "#3498db";
    private static final String COLOR_SUCCESS = "#2ecc71";
    private static final String COLOR_WARNING = "#f39c12";
    private static final String COLOR_DANGER = "#e74c3c";
    private static final String COLOR_INFO = "#9b59b6";
    private static final String COLOR_DARK = "#2c3e50";
    private static final String COLOR_LIGHT = "#ecf0f1";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("🚀 Initialisation HistoriqueController");

        sessionManager = SessionManager.getInstance();
        historiqueService = new HistoriqueService();

        if (!sessionManager.estConnecte()) {
            showError("Veuillez vous connecter");
            return;
        }

        clientId = sessionManager.getUtilisateurConnecte().getIdUtilisateur();

        setupUI();
        setupTableData();
        setupListeners();
        loadInitialData();
        setupAutoRefresh();


        mainContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (tableSection.isVisible()) {
                adjustTableHeight();
            }
        });
    }

    private void setupUI() {
        // Configuration des filtres
        periodeCombo.setItems(FXCollections.observableArrayList(
                "Toutes périodes", "Aujourd'hui", "Cette semaine",
                "Ce mois", "3 derniers mois", "6 derniers mois", "Cette année"
        ));
        periodeCombo.setValue(currentPeriode);

        typeCombo.setItems(FXCollections.observableArrayList(
                "Tous", "rendez_vous", "vente", "commentaire"
        ));
        typeCombo.setValue(currentType);

        // Boutons
        btnRefresh.setStyle(getButtonStyle(COLOR_PRIMARY));
        btnExport.setStyle(getButtonStyle(COLOR_SUCCESS));

        // Bouton masquer tableau
        // Bouton masquer tableau
        btnHideTable.setOnAction(e -> {
            hideTable();
            typeCombo.setValue("Tous");
        });
        // Champ de recherche
        searchField.setPromptText("Rechercher...");

        // Container historique
        historiqueContainer.setSpacing(15);
        historiqueContainer.setPadding(new Insets(20));

        // ScrollPane
        scrollContainer.setFitToWidth(true);
    }

    private void setupTableData() {
        // Configuration du tableau Rendez-vous
        rdvDateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        rdvHeureCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            if (item.getDateAction() != null) {
                LocalDateTime dateTime = item.getDateAction().toLocalDateTime();
                return new SimpleStringProperty(dateTime.format(DateTimeFormatter.ofPattern("HH:mm")));
            }
            return new SimpleStringProperty("");
        });

        rdvArticleCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            String article = item.getArticleTitre();
            if (article == null || article.isEmpty()) {
                article = "Non spécifié";
            }
            return new SimpleStringProperty(article);
        });

        rdvVendeurCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            return new SimpleStringProperty(item.getVendeurNom() != null ? item.getVendeurNom() : "");
        });

        rdvStatutCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            return new SimpleStringProperty(item.getStatut() != null ? item.getStatut() : "");
        });

        rdvNotesCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            return new SimpleStringProperty(item.getDescription() != null ? item.getDescription() : "");
        });

        // Configuration du tableau Ventes
        venteDateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        venteArticleCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            String article = item.getArticleTitre();
            if (article == null || article.isEmpty()) {
                article = "Article inconnu";
            }
            return new SimpleStringProperty(article);
        });

        venteMontantCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            Double montant = item.getMontant();
            if (montant == null) montant = 0.0;
            return new SimpleObjectProperty<>(montant);
        });

        venteStatutCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            return new SimpleStringProperty(item.getStatut() != null ? item.getStatut() : "");
        });

        ventePaiementCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            String details = item.getDetails();
            if (details != null && details.contains("carte")) {
                return new SimpleStringProperty("Carte bancaire");
            } else if (details != null && details.contains("espèces")) {
                return new SimpleStringProperty("Espèces");
            } else if (details != null && details.contains("virement")) {
                return new SimpleStringProperty("Virement");
            }
            return new SimpleStringProperty("Non spécifié");
        });

        venteDetailsCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            StringBuilder details = new StringBuilder();
            if (item.getArticleMarque() != null) {
                details.append(item.getArticleMarque()).append(" ");
            }
            if (item.getArticleModele() != null) {
                details.append(item.getArticleModele());
            }
            return new SimpleStringProperty(details.toString());
        });

        // Configuration du tableau Commentaires
        comDateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getFormattedDate()));

        comArticleCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            String article = item.getArticleTitre();
            if (article == null || article.isEmpty()) {
                article = "Article inconnu";
            }
            return new SimpleStringProperty(article);
        });

        comNoteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        comTitreCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            String details = item.getDetails();
            if (details != null && details.length() > 0) {
                return new SimpleStringProperty("Avis client");
            }
            return new SimpleStringProperty("Avis");
        });

        comTexteCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            String description = item.getDescription();
            if (description != null && description.length() > 50) {
                description = description.substring(0, 47) + "...";
            }
            return new SimpleStringProperty(description != null ? description : "");
        });

        comStatutCol.setCellValueFactory(cellData -> {
            HistoriqueItem item = cellData.getValue();
            String statut = item.getStatut();
            if (statut == null || statut.isEmpty()) {
                statut = "Publié";
            }
            return new SimpleStringProperty(statut);
        });

        // Assigner les données aux tableaux
        rdvTableView.setItems(rdvTableData);
        ventesTableView.setItems(ventesTableData);
        commentairesTableView.setItems(commentairesTableData);

        // Configurer les styles des cellules
        setupTableCellStyles();
    }

    private void setupListeners() {
        // Filtres
        periodeCombo.setOnAction(e -> {
            currentPeriode = periodeCombo.getValue();
            loadData();
        });

        typeCombo.setOnAction(e -> {
            currentType = typeCombo.getValue();
            loadData();
            updateTableVisibility();
        });

        // Boutons
        btnRefresh.setOnAction(e -> {
            animateRefreshButton();
            loadData();
            loadStatistics();
            showToast("✅ Données actualisées", COLOR_SUCCESS);
        });

        btnExport.setOnAction(e -> {
            exportTableData();
        });

        // Recherche
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterHistorique(newVal);
        });
    }

    private void loadInitialData() {
        Platform.runLater(() -> {
            loadData();
            loadStatistics();
        });
    }

    private void loadData() {
        VBox loadingContainer = createLoadingState();
        historiqueContainer.getChildren().clear();
        historiqueContainer.getChildren().add(loadingContainer);

        new Thread(() -> {
            currentHistorique =
                    historiqueService.getHistoriqueClient(clientId, currentPeriode, currentType);

            Platform.runLater(() -> {
                historiqueContainer.getChildren().clear();

                if (currentHistorique.isEmpty()) {
                    showEmptyState();
                    hideTable();
                    return;
                }

                // Grouper par date
                Map<String, List<HistoriqueItem>> groupedByDate = groupByDate(currentHistorique);

                // Afficher les groupes
                for (Map.Entry<String, List<HistoriqueItem>> entry : groupedByDate.entrySet()) {
                    addDateSection(entry.getKey(), entry.getValue());
                }

                // Animation d'apparition
                animateItemsAppearance();

                // Mettre à jour les tableaux
                updateTableVisibility();
            });
        }).start();
    }

    private void updateTableVisibility() {
        if (currentType.equals("Tous")) {
            hideTable();
            return;
        }

        // Vider tous les tableaux d'abord
        rdvTableData.clear();
        ventesTableData.clear();
        commentairesTableData.clear();

        // Remplir le tableau correspondant
        if (currentHistorique != null) {
            for (HistoriqueItem item : currentHistorique) {
                if (currentType.equals(item.getTypeAction())) {
                    switch (item.getTypeAction()) {
                        case "rendez_vous":
                            rdvTableData.add(item);
                            break;
                        case "vente":
                            ventesTableData.add(item);
                            break;
                        case "commentaire":
                            commentairesTableData.add(item);
                            break;
                    }
                }
            }
        }

        // Cacher tous les tableaux
        rdvTableView.setVisible(false);
        rdvTableView.setManaged(false);
        ventesTableView.setVisible(false);
        ventesTableView.setManaged(false);
        commentairesTableView.setVisible(false);
        commentairesTableView.setManaged(false);

        // Afficher le tableau correspondant
        switch (currentType) {
            case "rendez_vous":
                rdvTableView.setVisible(true);
                rdvTableView.setManaged(true);
                tableTitle.setText("📅 DÉTAILS DES RENDEZ-VOUS");
                tableSubtitle.setText("Liste complète de tous vos rendez-vous");
                tableCount.setText(rdvTableData.size() + " rendez-vous");
                break;

            case "vente":
                ventesTableView.setVisible(true);
                ventesTableView.setManaged(true);
                tableTitle.setText("💰 DÉTAILS DES TRANSACTIONS");
                tableSubtitle.setText("Historique détaillé de vos achats");
                tableCount.setText(ventesTableData.size() + " transactions");
                break;

            case "commentaire":
                commentairesTableView.setVisible(true);
                commentairesTableView.setManaged(true);
                tableTitle.setText("⭐ DÉTAILS DES AVIS");
                tableSubtitle.setText("Tous les avis et commentaires publiés");
                tableCount.setText(commentairesTableData.size() + " avis");
                break;
        }

        // Ajuster la hauteur du tableau
        adjustTableHeight();

        // Afficher la section tableau si il y a des données
        if ((currentType.equals("rendez_vous") && !rdvTableData.isEmpty()) ||
                (currentType.equals("vente") && !ventesTableData.isEmpty()) ||
                (currentType.equals("commentaire") && !commentairesTableData.isEmpty())) {
            showTable();
        } else {
            hideTable();
        }
    }


























    private void hideTable() {
        // Animation de disparition
        FadeTransition fade = new FadeTransition(Duration.millis(300), tableSection);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            // Masquer le tableau
            tableSection.setVisible(false);
            tableSection.setManaged(false);

            // Réafficher l'historique
            historiqueContent.setVisible(true);
            historiqueContent.setManaged(true);

            // Réinitialiser le filtre type
            typeCombo.setValue("Tous");
            currentType = "Tous";
        });
        fade.play();
    }


    private void loadStatistics() {
        new Thread(() -> {
            Map<String, Object> stats = historiqueService.getStatistiquesClient(clientId);

            Platform.runLater(() -> {
                // Mettre à jour les labels
                lblTotalActions.setText(String.valueOf(stats.getOrDefault("total_actions", 0)));
                lblTotalRDV.setText(String.valueOf(stats.getOrDefault("rendez_vous", 0)));
                lblTotalVentes.setText(String.valueOf(stats.getOrDefault("ventes", 0)));
                lblTotalAvis.setText(String.valueOf(stats.getOrDefault("commentaires", 0)));

                // Formater les nombres
                double totalDepense = (double) stats.getOrDefault("total_depense", 0.0);
                lblTotalDepense.setText(String.format("%,.2f €", totalDepense));

                double moyenneNotes = (double) stats.getOrDefault("moyenne_notes", 0.0);
                lblMoyenneNotes.setText(String.format("%.1f/5 ⭐", moyenneNotes));
            });
        }).start();
    }

    private void addDateSection(String date, List<HistoriqueItem> items) {
        // Section de date
        VBox dateSection = new VBox(10);
        dateSection.setPadding(new Insets(0, 0, 20, 0));

        // Titre de section
        Label sectionTitle = new Label(date);
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                "-fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");

        dateSection.getChildren().add(sectionTitle);

        // Items
        for (HistoriqueItem item : items) {
            dateSection.getChildren().add(createHistoriqueCard(item));
        }

        historiqueContainer.getChildren().add(dateSection);
    }

    private Pane createHistoriqueCard(HistoriqueItem item) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");

        // En-tête de la carte légèrement plus compacte
        HBox header = new HBox(12); // Espacement réduit de 15 à 12
        header.setPadding(new Insets(15, 18, 12, 18)); // Padding réduit
        header.setAlignment(Pos.CENTER_LEFT);

        // Icône légèrement plus petite
        StackPane iconContainer = new StackPane();
        iconContainer.setStyle(
                "-fx-background-color: " + item.getCouleur() + "15; " +
                        "-fx-background-radius: 8; " +
                        "-fx-pref-width: 45; " +    // Réduit de 50 à 45
                        "-fx-pref-height: 45;"      // Réduit de 50 à 45
        );

        Label iconLabel = new Label(item.getIcone());
        iconLabel.setStyle("-fx-font-size: 22px;"); // Réduit de 24 à 22
        iconContainer.getChildren().add(iconLabel);

        // Informations principales
        VBox infoBox = new VBox(6); // Espacement réduit de 8 à 6
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label titleLabel = new Label(item.getTypeDisplay());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; " + // Réduit de 16 à 15
                "-fx-text-fill: " + COLOR_DARK + ";");

        Label descLabel = new Label(item.getDescription());
        descLabel.setStyle("-fx-text-fill: #555; -fx-font-size: 13px;"); // Réduit de 14 à 13
        descLabel.setWrapText(true);

        // Métadonnées
        HBox metaBox = new HBox(12); // Espacement réduit de 15 à 12
        metaBox.setAlignment(Pos.CENTER_LEFT);

        Label timeLabel = new Label("🕒 " + item.getShortDate());
        timeLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;"); // Réduit de 12 à 11

        if (item.getVendeurNom() != null && !item.getVendeurNom().isEmpty()) {
            Label vendorLabel = new Label("👤 " + item.getVendeurNom());
            vendorLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;"); // Réduit de 12 à 11
            metaBox.getChildren().add(vendorLabel);
        }

        metaBox.getChildren().add(timeLabel);

        infoBox.getChildren().addAll(titleLabel, descLabel, metaBox);

        // Bouton pour voir les détails dans le tableau
        Button detailsBtn = new Button("Voir détails ➔");
        detailsBtn.setStyle(getButtonStyleSmall(item.getCouleur()));
        detailsBtn.setOnAction(e -> {
            // Filtrer pour afficher seulement ce type
            typeCombo.setValue(item.getTypeAction());
            currentType = item.getTypeAction();
            loadData();
        });

        header.getChildren().addAll(iconContainer, infoBox, detailsBtn);

        // Séparateur
        Separator separator = new Separator();
        separator.setPadding(new Insets(0, 18, 0, 18));

        card.getChildren().addAll(header, separator);

        // Animation au survol
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle().replace("rgba(0,0,0,0.08)", "rgba(0,0,0,0.12)"));
        });

        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace("rgba(0,0,0,0.12)", "rgba(0,0,0,0.08)"));
        });

        return card;
    }
    private Map<String, List<HistoriqueItem>> groupByDate(List<HistoriqueItem> items) {
        Map<String, List<HistoriqueItem>> grouped = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy");

        for (HistoriqueItem item : items) {
            if (item.getDateAction() != null) {
                LocalDateTime dateTime = item.getDateAction().toLocalDateTime();
                String dateKey = dateTime.toLocalDate().format(formatter);

                // Capitaliser premier caractère
                dateKey = dateKey.substring(0, 1).toUpperCase() + dateKey.substring(1);

                grouped.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(item);
            }
        }

        return grouped;
    }

    private void filterHistorique(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadData();
            return;
        }

        new Thread(() -> {
            List<HistoriqueItem> allItems =
                    historiqueService.getHistoriqueClient(clientId, currentPeriode, currentType);

            List<HistoriqueItem> filtered = new ArrayList<>();
            String searchLower = query.toLowerCase();

            for (HistoriqueItem item : allItems) {
                if ((item.getDescription() != null && item.getDescription().toLowerCase().contains(searchLower)) ||
                        (item.getTypeDisplay() != null && item.getTypeDisplay().toLowerCase().contains(searchLower)) ||
                        (item.getVendeurNom() != null && item.getVendeurNom().toLowerCase().contains(searchLower)) ||
                        (item.getArticleTitre() != null && item.getArticleTitre().toLowerCase().contains(searchLower))) {
                    filtered.add(item);
                }
            }

            Platform.runLater(() -> {
                historiqueContainer.getChildren().clear();

                if (filtered.isEmpty()) {
                    showNoResultsState(query);
                    hideTable();
                    return;
                }

                Map<String, List<HistoriqueItem>> grouped = groupByDate(filtered);
                for (Map.Entry<String, List<HistoriqueItem>> entry : grouped.entrySet()) {
                    addDateSection(entry.getKey(), entry.getValue());
                }

                // Mettre à jour le tableau avec les données filtrées
                currentHistorique = filtered;
                updateTableVisibility();
            });
        }).start();
    }

    private void exportTableData() {
        // Logique d'export selon le type sélectionné
        switch (currentType) {
            case "rendez_vous":
                showToast("📤 Export des rendez-vous en PDF", COLOR_SUCCESS);
                break;
            case "vente":
                showToast("📤 Export des transactions en Excel", COLOR_SUCCESS);
                break;
            case "commentaire":
                showToast("📤 Export des avis en PDF", COLOR_SUCCESS);
                break;
            default:
                showToast("📤 Export de l'historique complet", COLOR_INFO);
                break;
        }
    }

    // ==================== ÉTATS VISUELS ====================
    private VBox createLoadingState() {
        VBox loadingBox = new VBox(20);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPadding(new Insets(50));

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setStyle("-fx-progress-color: " + COLOR_PRIMARY + ";");

        Label loadingLabel = new Label("Chargement de votre historique...");
        loadingLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 14px;");

        loadingBox.getChildren().addAll(spinner, loadingLabel);

        return loadingBox;
    }

    private void showEmptyState() {
        VBox emptyState = new VBox(20);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(80, 20, 80, 20));

        Label emojiLabel = new Label("📭");
        emojiLabel.setStyle("-fx-font-size: 60px;");

        Label titleLabel = new Label("Aucune activité trouvée");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #555;");

        Label descLabel = new Label(
                currentPeriode.equals("Toutes périodes") && currentType.equals("Tous") ?
                        "Vous n'avez encore effectué aucune action" :
                        "Aucune activité ne correspond à vos filtres"
        );
        descLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");
        descLabel.setAlignment(Pos.CENTER);

        Button resetButton = new Button("Réinitialiser les filtres");
        resetButton.setStyle(getButtonStyle(COLOR_PRIMARY));
        resetButton.setOnAction(e -> {
            periodeCombo.setValue("Toutes périodes");
            typeCombo.setValue("Tous");
            searchField.clear();
        });

        emptyState.getChildren().addAll(emojiLabel, titleLabel, descLabel, resetButton);
        historiqueContainer.getChildren().add(emptyState);
    }

    private void showNoResultsState(String query) {
        VBox noResults = new VBox(20);
        noResults.setAlignment(Pos.CENTER);
        noResults.setPadding(new Insets(80, 20, 80, 20));

        Label emojiLabel = new Label("🔍");
        emojiLabel.setStyle("-fx-font-size: 60px;");

        Label titleLabel = new Label("Aucun résultat");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #555;");

        Label descLabel = new Label(
                String.format("Aucune activité ne correspond à \"%s\"", query)
        );
        descLabel.setStyle("-fx-text-fill: #888; -fx-font-size: 14px;");

        Button clearButton = new Button("Effacer la recherche");
        clearButton.setStyle(getButtonStyle(COLOR_PRIMARY));
        clearButton.setOnAction(e -> searchField.clear());

        noResults.getChildren().addAll(emojiLabel, titleLabel, descLabel, clearButton);
        historiqueContainer.getChildren().add(noResults);
    }

    private void showError(String message) {
        VBox errorBox = new VBox(20);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setPadding(new Insets(50));

        Label emojiLabel = new Label("⚠️");
        emojiLabel.setStyle("-fx-font-size: 60px;");

        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: " + COLOR_DANGER + "; -fx-font-size: 16px;");

        errorBox.getChildren().addAll(emojiLabel, errorLabel);
        historiqueContainer.getChildren().add(errorBox);
    }

    // ==================== ANIMATIONS ====================
    private void animateRefreshButton() {
        RotateTransition rotate = new RotateTransition(Duration.millis(500), btnRefresh);
        rotate.setByAngle(360);
        rotate.setCycleCount(1);
        rotate.play();
    }

    private void animateItemsAppearance() {
        for (int i = 0; i < historiqueContainer.getChildren().size(); i++) {
            Pane item = (Pane) historiqueContainer.getChildren().get(i);
            item.setOpacity(0);
            item.setTranslateY(20);

            FadeTransition fade = new FadeTransition(Duration.millis(300), item);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.setDelay(Duration.millis(i * 50));

            TranslateTransition translate = new TranslateTransition(Duration.millis(300), item);
            translate.setFromY(20);
            translate.setToY(0);
            translate.setDelay(Duration.millis(i * 50));

            ParallelTransition parallel = new ParallelTransition(fade, translate);
            parallel.play();
        }
    }

    private void showToast(String message, String color) {
        Label toast = new Label(message);
        toast.setStyle(
                "-fx-background-color: " + color + "20; " +
                        "-fx-background-radius: 20; " +
                        "-fx-border-color: " + color + "; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 20; " +
                        "-fx-text-fill: " + color + "; " +
                        "-fx-padding: 10 20; " +
                        "-fx-font-weight: bold;"
        );

        StackPane toastContainer = new StackPane(toast);
        StackPane.setAlignment(toast, Pos.TOP_CENTER);
        StackPane.setMargin(toast, new Insets(20, 0, 0, 0));

        StackPane rootPane = new StackPane(mainContainer, toastContainer);
        Scene scene = mainContainer.getScene();
        if (scene != null) {
            scene.setRoot(rootPane);
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> {
            if (scene != null) {
                scene.setRoot(mainContainer);
            }
        });
        pause.play();
    }

    // ==================== UTILITAIRES ====================
    private String getButtonStyle(String color) {
        return "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 8; " +
                "-fx-padding: 10 20; " +
                "-fx-cursor: hand; " +
                "-fx-font-size: 14px;";
    }

    private String getButtonStyleSmall(String color) {
        return "-fx-background-color: " + color + "20; " +
                "-fx-text-fill: " + color + "; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 6; " +
                "-fx-padding: 6 12; " +
                "-fx-cursor: hand; " +
                "-fx-font-size: 12px; " +
                "-fx-border-color: " + color + "; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 6;";
    }

    private String getBadgeStyle(String color) {
        return "-fx-background-color: " + color + "20; " +
                "-fx-text-fill: " + color + "; " +
                "-fx-padding: 4 12; " +
                "-fx-background-radius: 12; " +
                "-fx-font-size: 12px; " +
                "-fx-font-weight: bold;";
    }

    private String getStatusBadgeStyle(String statut) {
        String color = COLOR_INFO;
        if (statut != null) {
            String statutLower = statut.toLowerCase();
            if (statutLower.contains("confirmé") || statutLower.contains("terminé") || statutLower.contains("validé")) {
                color = COLOR_SUCCESS;
            } else if (statutLower.contains("attente") || statutLower.contains("en cours")) {
                color = COLOR_WARNING;
            } else if (statutLower.contains("annulé") || statutLower.contains("refusé")) {
                color = COLOR_DANGER;
            }
        }
        return getBadgeStyle(color);
    }

    private String getNoteStyle(int note) {
        if (note >= 4) return "-fx-font-weight: bold; -fx-text-fill: #2ecc71;";
        if (note >= 3) return "-fx-font-weight: bold; -fx-text-fill: #f39c12;";
        return "-fx-font-weight: bold; -fx-text-fill: #e74c3c;";
    }

    // ==================== GESTION AUTO-REFRESH ====================
    private void setupAutoRefresh() {
        autoRefreshTimeline = new Timeline(
                new KeyFrame(Duration.minutes(5), e -> {
                    System.out.println("🔄 Rafraîchissement automatique de l'historique");
                    loadData();
                    loadStatistics();
                })
        );
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    public void cleanup() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
    }


    // Dans la méthode setupUI() ou une nouvelle méthode
    private void setupTableSizes() {
        // Définir des tailles plus grandes pour les tableaux
        rdvTableView.setPrefHeight(400);
        ventesTableView.setPrefHeight(400);
        commentairesTableView.setPrefHeight(400);

        // Permettre le redimensionnement des colonnes
        rdvTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ventesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        commentairesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // Modifiez la méthode showTable() pour ajuster la taille
    private void showTable() {
        // Masquer l'historique
        historiqueContent.setVisible(false);
        historiqueContent.setManaged(false);

        // Afficher le tableau
        tableSection.setVisible(true);
        tableSection.setManaged(true);

        // Le tableau prend tout l'espace
        tableSection.prefHeightProperty().bind(mainContainer.heightProperty().subtract(150)); // Hauteur adaptative

        // Ajuster la hauteur du tableau
        adjustTableHeight();

        // Animation d'apparition
        FadeTransition fade = new FadeTransition(Duration.millis(300), tableSection);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        // Forcer le rafraîchissement du layout
        Platform.runLater(() -> {
            rdvTableView.refresh();
            ventesTableView.refresh();
            commentairesTableView.refresh();
        });
    }


    // Modifiez adjustTableHeight() pour que le tableau prenne toute la hauteur
    private void adjustTableHeight() {
        Platform.runLater(() -> {
            // Calculer la hauteur disponible
            double availableHeight = mainContainer.getHeight() - 150; // Moins la hauteur de l'en-tête

            // Ajuster la hauteur des tableaux
            rdvTableView.setPrefHeight(availableHeight - 100); // Moins l'en-tête du tableau
            ventesTableView.setPrefHeight(availableHeight - 100);
            commentairesTableView.setPrefHeight(availableHeight - 100);

            // Forcer le rafraîchissement
            rdvTableView.requestLayout();
            ventesTableView.requestLayout();
            commentairesTableView.requestLayout();
        });
    }





    // Modifiez setupTableCellStyles() pour améliorer l'affichage
    private void setupTableCellStyles() {
        // Style pour toutes les cellules
        rdvTableView.setStyle("-fx-font-size: 14px;");
        ventesTableView.setStyle("-fx-font-size: 14px;");
        commentairesTableView.setStyle("-fx-font-size: 14px;");

        // Style pour la colonne Statut RDV avec meilleur padding
        rdvStatutCol.setCellFactory(column -> new TableCell<HistoriqueItem, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut.toUpperCase());
                    String style = getStatusBadgeStyle(statut) +
                            "-fx-padding: 6px 12px;" +
                            "-fx-font-size: 13px;";
                    setStyle(style);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Style pour la colonne Montant avec meilleur affichage
        venteMontantCol.setCellFactory(column -> new TableCell<HistoriqueItem, Double>() {
            @Override
            protected void updateItem(Double montant, boolean empty) {
                super.updateItem(montant, empty);
                if (empty || montant == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%,.2f €", montant));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #2ecc71; -fx-font-size: 14px;");
                    setAlignment(Pos.CENTER_RIGHT);
                }
            }
        });

        // Style pour la colonne Note avec meilleur affichage
        comNoteCol.setCellFactory(column -> new TableCell<HistoriqueItem, Integer>() {
            @Override
            protected void updateItem(Integer note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText(null);
                    setStyle("");
                } else {
                    StringBuilder stars = new StringBuilder();
                    for (int i = 0; i < 5; i++) {
                        if (i < note) {
                            stars.append("★");
                        } else {
                            stars.append("☆");
                        }
                    }
                    setText(stars.toString() + " (" + note + "/5)");
                    setStyle(getNoteStyle(note) + " -fx-font-size: 14px; -fx-padding: 5px;");
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Style pour la colonne Commentaire (wrap text)
        comTexteCol.setCellFactory(column -> new TableCell<HistoriqueItem, String>() {
            @Override
            protected void updateItem(String texte, boolean empty) {
                super.updateItem(texte, empty);
                if (empty || texte == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(texte);
                    setWrapText(true);
                    setStyle("-fx-font-size: 13px; -fx-padding: 8px;");
                }
            }
        });
    }
}