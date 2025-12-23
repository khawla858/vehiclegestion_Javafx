package com.example.vehiclegestion.client.service;

import com.example.vehiclegestion.logging.model.LogEntry;
import com.example.vehiclegestion.logging.service.ElasticLogService;

/**
 * Service de logs pour les opérations sur les véhicules
 */
public class VehicleLogService {

    private final ElasticLogService elasticLogService;

    public VehicleLogService() {
        this.elasticLogService = new ElasticLogService();
    }

    // ========== MÉTHODE DE DÉLÉGATION ==========

    /**
     * Envoyer un log simple (pour backward compatibility)
     */
    public void sendLog(String level, String message) {
        elasticLogService.sendLog(level, message);
    }

    /**
     * Envoyer un LogEntry complet
     */
    public void sendLog(LogEntry logEntry) {
        elasticLogService.sendLog(logEntry);
    }

    // ========== LOGS DE CHARGEMENT ==========

    public void logVehicleLoadSuccess(int vehicleCount) {
        LogEntry log = LogEntry.success(
                "VEHICLE_LOAD_SUCCESS",
                null,
                "Chargement réussi de " + vehicleCount + " véhicules"
        );
        log.addMetadata("vehicleCount", vehicleCount);
        sendLog(log);
    }

    public void logVehicleLoadError(String errorMessage) {
        LogEntry log = LogEntry.error(
                "VEHICLE_LOAD_ERROR",
                null,
                "Erreur lors du chargement des véhicules: " + errorMessage
        );
        sendLog(log);
    }

    public void logNoVehiclesFound() {
        LogEntry log = LogEntry.warning(
                "NO_VEHICLES_FOUND",
                null,
                "Aucun véhicule trouvé dans la base de données"
        );
        sendLog(log);
    }

    // ========== LOGS DE FILTRES ==========

    public void logFilterApplied(String clientEmail, Long clientId, String filterType, String filterValue) {
        LogEntry log = LogEntry.info(
                "FILTER_APPLIED",
                clientEmail,
                "Filtre appliqué: " + filterType + " = " + filterValue
        );
        log.setUserId(clientId);
        log.addMetadata("filterType", filterType);
        log.addMetadata("filterValue", filterValue);
        sendLog(log);
    }

    public void logFiltersReset(String clientEmail, Long clientId) {
        LogEntry log = LogEntry.info(
                "FILTERS_RESET",
                clientEmail,
                "Réinitialisation de tous les filtres"
        );
        log.setUserId(clientId);
        sendLog(log);
    }

    // ========== LOGS DE TRI ==========

    public void logSortApplied(String clientEmail, Long clientId, String sortBy) {
        LogEntry log = LogEntry.success(
                "SORT_APPLIED",
                clientEmail,
                "Tri appliqué: " + sortBy
        );
        log.setUserId(clientId);
        log.addMetadata("sortBy", sortBy);
        sendLog(log);
    }

    // ========== LOGS D'INTERACTION ==========

    public void logVehicleView(String clientEmail, Long clientId, int vehicleId, String vehicleTitle) {
        LogEntry log = LogEntry.info(
                "VEHICLE_VIEW",
                clientEmail,
                "Consultation du véhicule: " + vehicleTitle
        );
        log.setUserId(clientId);
        log.addMetadata("vehicleId", vehicleId);
        log.addMetadata("vehicleTitle", vehicleTitle);
        sendLog(log);
    }

    public void logFavoriteToggle(String clientEmail, Long clientId, int vehicleId,
                                  String vehicleTitle, boolean added) {
        LogEntry log = LogEntry.success(
                added ? "FAVORITE_ADDED" : "FAVORITE_REMOVED",
                clientEmail,
                added ? "Véhicule ajouté aux favoris" : "Véhicule retiré des favoris"
        );
        log.setUserId(clientId);
        log.addMetadata("vehicleId", vehicleId);
        log.addMetadata("vehicleTitle", vehicleTitle);
        log.addMetadata("action", added ? "add" : "remove");
        sendLog(log);
    }

    public void logFavoriteError(String clientEmail, Long clientId, int vehicleId, String error) {
        LogEntry log = LogEntry.error(
                "FAVORITE_ERROR",
                clientEmail,
                "Erreur lors de la gestion des favoris: " + error
        );
        log.setUserId(clientId);
        log.addMetadata("vehicleId", vehicleId);
        log.addMetadata("error", error);
        sendLog(log);
    }

    // ========== LOGS D'ERREUR D'IMAGE ==========

    public void logImageLoadError(int vehicleId, String imagePath, String error) {
        LogEntry log = LogEntry.error(
                "IMAGE_LOAD_ERROR",
                null,
                "Erreur lors du chargement de l'image pour le véhicule " + vehicleId
        );
        log.addMetadata("vehicleId", vehicleId);
        log.addMetadata("imagePath", imagePath);
        log.addMetadata("error", error);
        sendLog(log);
    }

    // ========== LOGS DE PERFORMANCE ==========

    public void logDisplayPerformance(int vehicleCount, long displayTimeMs, boolean filtersVisible) {
        LogEntry log = LogEntry.info(
                "DISPLAY_PERFORMANCE",
                null,
                "Affichage de " + vehicleCount + " véhicules en " + displayTimeMs + "ms"
        );
        log.addMetadata("vehicleCount", vehicleCount);
        log.addMetadata("displayTimeMs", displayTimeMs);
        log.addMetadata("filtersVisible", filtersVisible);
        sendLog(log);
    }

    // ========== LOGS D'ETAT DE FILTRES ==========

    public void logFiltersVisibilityToggle(String clientEmail, Long clientId, boolean visible) {
        LogEntry log = LogEntry.info(
                "FILTERS_TOGGLE",
                clientEmail,
                visible ? "Filtres affichés" : "Filtres masqués"
        );
        log.setUserId(clientId);
        log.addMetadata("visible", visible);
        sendLog(log);
    }

    public void shutdown() {
        elasticLogService.shutdown();
    }
}