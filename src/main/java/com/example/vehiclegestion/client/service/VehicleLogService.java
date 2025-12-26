package com.example.vehiclegestion.client.service;

import com.example.vehiclegestion.logging.model.LogEntry;
import com.example.vehiclegestion.logging.service.ElasticLogService;
import com.example.vehiclegestion.auth.utils.SessionManager;

/**
 * Service de logs pour les opérations CLIENT (véhicules, filtres, favoris…)
 * ✅ VERSION CORRIGÉE - Envoie les vrais logs à Elasticsearch
 */
public class VehicleLogService {

    private final ElasticLogService elasticLogService;

    public VehicleLogService() {
        this.elasticLogService = new ElasticLogService();
    }

    // ========== MÉTHODES DE DÉLÉGATION CORRIGÉES ==========

    /**
     * ✅ CORRECTION: Envoie le log réel au lieu d'un log générique
     */
    public void sendLog(String level, String message) {
        LogEntry logEntry;

        switch (level.toUpperCase()) {
            case "ERROR":
                logEntry = LogEntry.error("VEHICLE_ERROR", getUserEmail(), message);
                break;
            case "WARN":
            case "WARNING":
                logEntry = LogEntry.warning("VEHICLE_WARNING", getUserEmail(), message);
                break;
            case "INFO":
            default:
                logEntry = LogEntry.info("VEHICLE_INFO", getUserEmail(), message);
                break;
        }

        logEntry.setModule(LogEntry.MODULE_CLIENT);
        enrichWithUserData(logEntry);
        elasticLogService.sendLog(logEntry);
    }

    /**
     * ✅ CORRECTION: Envoie le LogEntry réel au lieu d'un log générique
     */
    public void sendLog(LogEntry logEntry) {
        if (logEntry != null) {
            logEntry.setModule(LogEntry.MODULE_CLIENT);
            enrichWithUserData(logEntry);
            elasticLogService.sendLog(logEntry);
        }
    }

    /**
     * Enrichit le log avec les données de l'utilisateur connecté
     */
    private void enrichWithUserData(LogEntry logEntry) {
        SessionManager session = SessionManager.getInstance();
        if (session.estConnecte()) {
            logEntry.setUserId((long) session.getUserId());
            logEntry.setUserRole(session.getUserRole());
            logEntry.setUserEmail(session.getEmail());
        }
    }

    /**
     * Récupère l'email de l'utilisateur connecté
     */
    private String getUserEmail() {
        SessionManager session = SessionManager.getInstance();
        return session.estConnecte() ? session.getEmail() : "anonymous";
    }

    // ========== LOGS DE CHARGEMENT ==========

    public void logVehicleLoadSuccess(int vehicleCount) {
        LogEntry log = LogEntry.success(
                LogEntry.ACTION_VEHICLE_LOAD_SUCCESS,
                getUserEmail(),
                "Chargement réussi de " + vehicleCount + " véhicules"
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.addMetadata("vehicleCount", vehicleCount);
        enrichWithUserData(log);
        sendLog(log);
    }

    public void logVehicleLoadError(String errorMessage) {
        LogEntry log = LogEntry.error(
                LogEntry.ACTION_VEHICLE_LOAD_ERROR,
                getUserEmail(),
                "Erreur lors du chargement des véhicules : " + errorMessage
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.addMetadata("error", errorMessage);
        enrichWithUserData(log);
        sendLog(log);
    }

    public void logNoVehiclesFound() {
        LogEntry log = LogEntry.warning(
                LogEntry.ACTION_NO_VEHICLES_FOUND,
                getUserEmail(),
                "Aucun véhicule trouvé dans la base de données"
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        enrichWithUserData(log);
        sendLog(log);
    }

    // ========== LOGS DE FILTRES ==========

    public void logFilterApplied(String clientEmail, Long clientId,
                                 String filterType, String filterValue) {
        LogEntry log = LogEntry.info(
                LogEntry.ACTION_FILTER_APPLIED,
                clientEmail,
                "Filtre appliqué : " + filterType + " = " + filterValue
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        log.addMetadata("filterType", filterType);
        log.addMetadata("filterValue", filterValue);
        sendLog(log);
    }

    public void logFiltersReset(String clientEmail, Long clientId) {
        LogEntry log = LogEntry.info(
                LogEntry.ACTION_FILTERS_RESET,
                clientEmail,
                "Réinitialisation de tous les filtres"
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        sendLog(log);
    }

    // ========== LOGS DE TRI ==========

    public void logSortApplied(String clientEmail, Long clientId, String sortBy) {
        LogEntry log = LogEntry.success(
                LogEntry.ACTION_SORT_APPLIED,
                clientEmail,
                "Tri appliqué : " + sortBy
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        log.addMetadata("sortBy", sortBy);
        sendLog(log);
    }

    // ========== LOGS D'INTERACTION CLIENT ==========

    public void logVehicleView(String clientEmail, Long clientId,
                               int vehicleId, String vehicleTitle) {
        LogEntry log = LogEntry.info(
                LogEntry.ACTION_VEHICLE_VIEW,
                clientEmail,
                "Consultation du véhicule : " + vehicleTitle
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        log.addMetadata("vehicleId", vehicleId);
        log.addMetadata("vehicleTitle", vehicleTitle);
        sendLog(log);
    }

    public void logFavoriteToggle(String clientEmail, Long clientId,
                                  int vehicleId, String vehicleTitle,
                                  boolean added) {
        LogEntry log = LogEntry.success(
                added ? LogEntry.ACTION_FAVORITE_ADDED
                        : LogEntry.ACTION_FAVORITE_REMOVED,
                clientEmail,
                added ? "Véhicule ajouté aux favoris : " + vehicleTitle
                        : "Véhicule retiré des favoris : " + vehicleTitle
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        log.addMetadata("vehicleId", vehicleId);
        log.addMetadata("vehicleTitle", vehicleTitle);
        log.addMetadata("action", added ? "add" : "remove");
        sendLog(log);
    }

    public void logFavoriteError(String clientEmail, Long clientId,
                                 int vehicleId, String error) {
        LogEntry log = LogEntry.error(
                LogEntry.ACTION_FAVORITE_ERROR,
                clientEmail,
                "Erreur lors de la gestion des favoris : " + error
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        log.addMetadata("vehicleId", vehicleId);
        log.addMetadata("error", error);
        sendLog(log);
    }

    // ========== LOGS D'IMAGES ==========

    public void logImageLoadError(int vehicleId, String imagePath, String error) {
        LogEntry log = LogEntry.error(
                LogEntry.ACTION_IMAGE_LOAD_ERROR,
                getUserEmail(),
                "Erreur de chargement d'image pour le véhicule " + vehicleId
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.addMetadata("vehicleId", vehicleId);
        log.addMetadata("imagePath", imagePath);
        log.addMetadata("error", error);
        enrichWithUserData(log);
        sendLog(log);
    }

    // ========== LOGS DE PERFORMANCE ==========

    public void logDisplayPerformance(int vehicleCount,
                                      long displayTimeMs,
                                      boolean filtersVisible) {
        LogEntry log = LogEntry.info(
                LogEntry.ACTION_DISPLAY_PERFORMANCE,
                getUserEmail(),
                "Affichage de " + vehicleCount + " véhicules en " + displayTimeMs + " ms"
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.addMetadata("vehicleCount", vehicleCount);
        log.addMetadata("displayTimeMs", displayTimeMs);
        log.addMetadata("filtersVisible", filtersVisible);
        enrichWithUserData(log);
        sendLog(log);
    }

    // ========== VISIBILITÉ DES FILTRES ==========

    public void logFiltersVisibilityToggle(String clientEmail,
                                           Long clientId,
                                           boolean visible) {
        LogEntry log = LogEntry.info(
                LogEntry.ACTION_FILTERS_TOGGLE,
                clientEmail,
                visible ? "Filtres affichés" : "Filtres masqués"
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        log.addMetadata("visible", visible);
        sendLog(log);
    }

    // ========== LOGS DE RECHERCHE ==========

    public void logSearch(String clientEmail, Long clientId,
                          String searchQuery, int resultsCount) {
        LogEntry log = LogEntry.success(
                "VEHICLE_SEARCH",
                clientEmail,
                "Recherche effectuée : " + searchQuery
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        log.addMetadata("searchQuery", searchQuery);
        log.addMetadata("resultsCount", resultsCount);
        sendLog(log);
    }

    // ========== LOGS DE RÉSERVATION ==========

    public void logReservationAttempt(String clientEmail, Long clientId,
                                      int vehicleId, String vehicleTitle) {
        LogEntry log = LogEntry.info(
                "RESERVATION_ATTEMPT",
                clientEmail,
                "Tentative de réservation : " + vehicleTitle
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        log.addMetadata("vehicleId", vehicleId);
        log.addMetadata("vehicleTitle", vehicleTitle);
        sendLog(log);
    }

    public void logReservationSuccess(String clientEmail, Long clientId,
                                      int vehicleId, String vehicleTitle) {
        LogEntry log = LogEntry.success(
                "RESERVATION_SUCCESS",
                clientEmail,
                "Réservation confirmée : " + vehicleTitle
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        log.addMetadata("vehicleId", vehicleId);
        log.addMetadata("vehicleTitle", vehicleTitle);
        sendLog(log);
    }

    public void logReservationError(String clientEmail, Long clientId,
                                    int vehicleId, String error) {
        LogEntry log = LogEntry.error(
                "RESERVATION_ERROR",
                clientEmail,
                "Erreur de réservation : " + error
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        log.addMetadata("vehicleId", vehicleId);
        log.addMetadata("error", error);
        sendLog(log);
    }

    // ========== LOGS DE NAVIGATION ==========

    public void logPageView(String clientEmail, Long clientId, String pageName) {
        LogEntry log = LogEntry.info(
                "PAGE_VIEW",
                clientEmail,
                "Navigation vers : " + pageName
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.setUserId(clientId);
        log.addMetadata("pageName", pageName);
        sendLog(log);
    }

    // ========== LOGS D'ERREURS GÉNÉRIQUES ==========

    public void logError(String action, String errorMessage, Exception e) {
        LogEntry log = LogEntry.error(
                action,
                getUserEmail(),
                errorMessage
        );
        log.setModule(LogEntry.MODULE_CLIENT);
        log.addMetadata("error", errorMessage);
        if (e != null) {
            log.addMetadata("exception", e.getClass().getName());
            log.addMetadata("exceptionMessage", e.getMessage());
        }
        enrichWithUserData(log);
        sendLog(log);
    }

    // ========== SHUTDOWN ==========

    public void shutdown() {
        elasticLogService.shutdown();
    }
}