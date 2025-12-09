package com.example.vehiclegestion.admin.controller;

import com.example.vehiclegestion.logging.model.LogEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller pour la gestion et visualisation des logs système
 */
public class AdminLogsController {

    @FXML private TableView<LogEntryDisplay> logsTable;
    @FXML private TableColumn<LogEntryDisplay, String> timestampColumn;
    @FXML private TableColumn<LogEntryDisplay, String> levelColumn;
    @FXML private TableColumn<LogEntryDisplay, String> actionColumn;
    @FXML private TableColumn<LogEntryDisplay, String> userColumn;
    @FXML private TableColumn<LogEntryDisplay, String> messageColumn;

    @FXML private ComboBox<String> levelFilterCombo;
    @FXML private ComboBox<String> actionFilterCombo;
    @FXML private TextField searchField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    @FXML private Label totalLogsLabel;
    @FXML private Label errorCountLabel;
    @FXML private Label warningCountLabel;
    @FXML private Label infoCountLabel;

    private ObservableList<LogEntryDisplay> allLogs;
    private ObservableList<LogEntryDisplay> filteredLogs;

    /**
     * Initialisation du controller
     */
    @FXML
    public void initialize() {
        System.out.println("📋 Initialisation AdminLogsController...");

        // Initialiser les listes
        allLogs = FXCollections.observableArrayList();
        filteredLogs = FXCollections.observableArrayList();

        // Configurer les colonnes du tableau
        setupTableColumns();

        // Configurer les filtres
        setupFilters();

        // Charger les logs
        loadLogs();

        System.out.println("✅ AdminLogsController initialisé");
    }

    /**
     * Configurer les colonnes du tableau
     */
    private void setupTableColumns() {
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        levelColumn.setCellValueFactory(new PropertyValueFactory<>("level"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("action"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));

        // Style personnalisé pour la colonne Level
        levelColumn.setCellFactory(column -> new TableCell<LogEntryDisplay, String>() {
            @Override
            protected void updateItem(String level, boolean empty) {
                super.updateItem(level, empty);
                if (empty || level == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(level);
                    switch (level.toUpperCase()) {
                        case "ERROR":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        case "WARN":
                        case "WARNING":
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "INFO":
                            setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                            break;
                        case "DEBUG":
                            setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });

        // Rendre la colonne message wrappable
        messageColumn.setCellFactory(column -> new TableCell<LogEntryDisplay, String>() {
            @Override
            protected void updateItem(String message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                    setText(message);
                    setWrapText(true);
                }
            }
        });

        logsTable.setItems(filteredLogs);
    }

    /**
     * Configurer les filtres
     */
    private void setupFilters() {
        // Niveaux de log
        levelFilterCombo.setItems(FXCollections.observableArrayList(
                "Tous", "ERROR", "WARN", "INFO", "DEBUG"
        ));
        levelFilterCombo.setValue("Tous");

        // Actions
        actionFilterCombo.setItems(FXCollections.observableArrayList(
                "Toutes", "LOGIN", "LOGOUT", "REGISTER", "UPDATE", "DELETE"
        ));
        actionFilterCombo.setValue("Toutes");

        // Écouteurs pour les filtres
        levelFilterCombo.setOnAction(e -> applyFilters());
        actionFilterCombo.setOnAction(e -> applyFilters());
        searchField.textProperty().addListener((obs, old, newVal) -> applyFilters());
        startDatePicker.setOnAction(e -> applyFilters());
        endDatePicker.setOnAction(e -> applyFilters());
    }

    /**
     * Charger les logs depuis les fichiers
     */
    private void loadLogs() {
        allLogs.clear();

        // Charger depuis les différents fichiers de log
        loadLogsFromFile("logs/app.log");
        loadLogsFromFile("logs/error.log");
        loadLogsFromFile("logs/debug.log");

        // Trier par timestamp décroissant
        allLogs.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        // Appliquer les filtres
        applyFilters();

        // Mettre à jour les statistiques
        updateStatistics();
    }

    /**
     * Charger les logs depuis un fichier spécifique
     */
    private void loadLogsFromFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("⚠️ Fichier log introuvable: " + filePath);
                return;
            }

            List<String> lines = Files.readAllLines(Paths.get(filePath));
            System.out.println("📄 Lecture de " + lines.size() + " lignes depuis " + filePath);

            for (String line : lines) {
                LogEntryDisplay log = parseLogLine(line);
                if (log != null) {
                    allLogs.add(log);
                }
            }

        } catch (IOException e) {
            System.err.println("❌ Erreur lecture fichier: " + filePath);
            e.printStackTrace();
        }
    }

    /**
     * Parser une ligne de log
     */
    private LogEntryDisplay parseLogLine(String line) {
        try {
            // Format attendu: [timestamp] LEVEL | ACTION | User: email | message
            if (line.contains("|")) {
                String[] parts = line.split("\\|");

                // Extraire timestamp et level
                String timestampLevel = parts[0].trim();
                String timestamp = "";
                String level = "";

                if (timestampLevel.contains("[") && timestampLevel.contains("]")) {
                    timestamp = timestampLevel.substring(
                            timestampLevel.indexOf("[") + 1,
                            timestampLevel.indexOf("]")
                    );
                    level = timestampLevel.substring(timestampLevel.indexOf("]") + 1).trim();
                }

                // Extraire action
                String action = parts.length > 1 ? parts[1].trim() : "";

                // Extraire user
                String user = parts.length > 2 ?
                        parts[2].replace("User:", "").trim() : "";

                // Extraire message
                String message = parts.length > 3 ? parts[3].trim() : "";

                return new LogEntryDisplay(timestamp, level, action, user, message);
            }

            // Format simple si pas de séparateurs
            return new LogEntryDisplay(
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    "INFO",
                    "SYSTEM",
                    "system",
                    line
            );

        } catch (Exception e) {
            System.err.println("⚠️ Erreur parsing log: " + line);
            return null;
        }
    }

    /**
     * Appliquer les filtres
     */
    private void applyFilters() {
        filteredLogs.clear();

        String levelFilter = levelFilterCombo.getValue();
        String actionFilter = actionFilterCombo.getValue();
        String searchText = searchField.getText().toLowerCase();

        filteredLogs.addAll(allLogs.stream()
                .filter(log -> {
                    // Filtre par niveau
                    if (!"Tous".equals(levelFilter) &&
                            !log.getLevel().equalsIgnoreCase(levelFilter)) {
                        return false;
                    }

                    // Filtre par action
                    if (!"Toutes".equals(actionFilter) &&
                            !log.getAction().toUpperCase().contains(actionFilter)) {
                        return false;
                    }

                    // Filtre par recherche texte
                    if (!searchText.isEmpty()) {
                        String fullText = (log.getTimestamp() + " " +
                                log.getLevel() + " " +
                                log.getAction() + " " +
                                log.getUser() + " " +
                                log.getMessage()).toLowerCase();
                        if (!fullText.contains(searchText)) {
                            return false;
                        }
                    }

                    // Filtre par date (si implémenté)
                    // TODO: Ajouter filtrage par date si nécessaire

                    return true;
                })
                .collect(Collectors.toList())
        );

        updateStatistics();
    }

    /**
     * Mettre à jour les statistiques
     */
    private void updateStatistics() {
        totalLogsLabel.setText(String.valueOf(filteredLogs.size()));

        long errorCount = filteredLogs.stream()
                .filter(log -> "ERROR".equalsIgnoreCase(log.getLevel()))
                .count();
        errorCountLabel.setText(String.valueOf(errorCount));

        long warningCount = filteredLogs.stream()
                .filter(log -> "WARN".equalsIgnoreCase(log.getLevel()) ||
                        "WARNING".equalsIgnoreCase(log.getLevel()))
                .count();
        warningCountLabel.setText(String.valueOf(warningCount));

        long infoCount = filteredLogs.stream()
                .filter(log -> "INFO".equalsIgnoreCase(log.getLevel()))
                .count();
        infoCountLabel.setText(String.valueOf(infoCount));
    }

    /**
     * Rafraîchir les logs
     */
    @FXML
    public void handleRefresh() {
        System.out.println("🔄 Rafraîchissement des logs...");
        loadLogs();
    }

    /**
     * Réinitialiser les filtres
     */
    @FXML
    public void handleResetFilters() {
        levelFilterCombo.setValue("Tous");
        actionFilterCombo.setValue("Toutes");
        searchField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        applyFilters();
    }

    /**
     * Exporter les logs
     */
    @FXML
    public void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les logs");
        fileChooser.setInitialFileName("logs_export_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(logsTable.getScene().getWindow());
        if (file != null) {
            exportToCSV(file);
        }
    }

    /**
     * Exporter vers CSV
     */
    private void exportToCSV(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            // En-tête
            writer.println("Timestamp,Level,Action,User,Message");

            // Données
            for (LogEntryDisplay log : filteredLogs) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        log.getTimestamp(),
                        log.getLevel(),
                        log.getAction(),
                        log.getUser(),
                        log.getMessage().replace("\"", "\"\"")
                );
            }

            showInfo("Export réussi", "Les logs ont été exportés vers:\n" + file.getAbsolutePath());

        } catch (IOException e) {
            showError("Erreur d'export", "Impossible d'exporter les logs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Supprimer tous les logs (avec confirmation)
     */
    @FXML
    public void handleClearLogs() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer tous les logs ?");
        confirmation.setContentText("Cette action est irréversible !");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            clearLogFiles();
        }
    }

    /**
     * Vider les fichiers de logs
     */
    private void clearLogFiles() {
        try {
            Files.write(Paths.get("logs/app.log"), new byte[0]);
            Files.write(Paths.get("logs/error.log"), new byte[0]);
            Files.write(Paths.get("logs/debug.log"), new byte[0]);

            allLogs.clear();
            filteredLogs.clear();
            updateStatistics();

            showInfo("Succès", "Tous les logs ont été supprimés");

        } catch (IOException e) {
            showError("Erreur", "Impossible de supprimer les logs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== CLASSES INTERNES ==========

    /**
     * Classe pour l'affichage des logs dans le TableView
     */
    public static class LogEntryDisplay {
        private String timestamp;
        private String level;
        private String action;
        private String user;
        private String message;

        public LogEntryDisplay(String timestamp, String level, String action,
                               String user, String message) {
            this.timestamp = timestamp;
            this.level = level;
            this.action = action;
            this.user = user;
            this.message = message;
        }

        public String getTimestamp() { return timestamp; }
        public String getLevel() { return level; }
        public String getAction() { return action; }
        public String getUser() { return user; }
        public String getMessage() { return message; }
    }

    // ========== MÉTHODES UTILITAIRES ==========

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}