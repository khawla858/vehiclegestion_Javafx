package com.example.vehiclegestion.client.service;
//main
import com.example.vehiclegestion.logging.model.LogEntry;
import com.example.vehiclegestion.logging.service.ElasticLogService;
import  com.example.vehiclegestion.auth.utils.SessionManager;

/**
 * Service dédié aux logs des actions du client
 */
public class ClientLogService {

    private final ElasticLogService elasticLogService;

    public ClientLogService() {
        this.elasticLogService = new ElasticLogService();
    }

    // ========== LOGS DE NAVIGATION ==========

    /**
     * Log de navigation de l'utilisateur
     */
    public void logUserNavigation(String destination, int userId) {
        LogEntry log = LogEntry.info("USER_NAVIGATION", String.valueOf(userId),
                        "Navigation vers: " + destination)
                .addMetadata("destination", destination)
                .addMetadata("userId", String.valueOf(userId));

        elasticLogService.sendLog(log);
    }

    /**
     * Log de visualisation d'un véhicule
     */
    public void logVehicleView(int vehicleId, String viewType) {
        LogEntry log = LogEntry.info("VEHICLE_VIEW", "anonymous",
                        "Visualisation du véhicule ID: " + vehicleId)
                .addMetadata("vehicleId", String.valueOf(vehicleId))
                .addMetadata("viewType", viewType);

        elasticLogService.sendLog(log);
    }

    /**
     * Log de visualisation d'un magasin
     */
    public void logStoreView(int storeId, String viewType) {
        LogEntry log = LogEntry.info("STORE_VIEW", "anonymous",
                        "Visualisation du magasin ID: " + storeId)
                .addMetadata("storeId", String.valueOf(storeId))
                .addMetadata("viewType", viewType);

        elasticLogService.sendLog(log);
    }

    /**
     * Log de visualisation d'un vendeur
     */
    public void logSellerView(int sellerId, String viewType) {
        LogEntry log = LogEntry.info("SELLER_VIEW", "anonymous",
                        "Visualisation du vendeur ID: " + sellerId)
                .addMetadata("sellerId", String.valueOf(sellerId))
                .addMetadata("viewType", viewType);

        elasticLogService.sendLog(log);
    }

    /**
     * Log de visualisation d'une ville
     */
    public void logCityView(String cityName, String viewType) {
        LogEntry log = LogEntry.info("CITY_VIEW", "anonymous",
                        "Visualisation de la ville: " + cityName)
                .addMetadata("cityName", cityName)
                .addMetadata("viewType", viewType);

        elasticLogService.sendLog(log);
    }

    // ========== LOGS DE RECHERCHE ==========

    /**
     * Log d'une recherche
     */
    public void logSearchEvent(String query, String searchType) {
        logSearchEvent(query, searchType, 0);
    }

    /**
     * Log d'une recherche avec ID utilisateur
     */
    public void logSearchEvent(String query, String searchType, int userId) {
        String user = userId > 0 ? String.valueOf(userId) : "anonymous";

        LogEntry log = LogEntry.info("SEARCH_PERFORMED", user,
                        "Recherche: \"" + query + "\"")
                .addMetadata("query", query)
                .addMetadata("searchType", searchType)
                .addMetadata("userId", String.valueOf(userId));

        elasticLogService.sendLog(log);
    }

    /**
     * Log des résultats de recherche
     */
    public void logSearchResults(String query, int resultCount) {
        LogEntry log = LogEntry.info("SEARCH_RESULTS", "anonymous",
                        "Résultats pour \"" + query + "\": " + resultCount + " trouvés")
                .addMetadata("query", query)
                .addMetadata("resultCount", String.valueOf(resultCount));

        elasticLogService.sendLog(log);
    }

    /**
     * Log d'aucun résultat de recherche
     */
    public void logSearchNoResults(String query) {
        LogEntry log = LogEntry.warning("SEARCH_NO_RESULTS", "anonymous",
                        "Aucun résultat pour \"" + query + "\"")
                .addMetadata("query", query);

        elasticLogService.sendLog(log);
    }

    /**
     * Log de clic sur un résultat de recherche
     */
    public void logSearchResultClick(String resultType, int resultId, String resultTitle) {
        LogEntry log = LogEntry.info("SEARCH_RESULT_CLICK", "anonymous",
                        "Clic sur résultat: " + resultTitle)
                .addMetadata("resultType", resultType)
                .addMetadata("resultId", String.valueOf(resultId))
                .addMetadata("resultTitle", resultTitle);

        elasticLogService.sendLog(log);
    }

    // ========== LOGS D'ACTIONS UTILISATEUR ==========

    /**
     * Log d'une action utilisateur
     */
    public void logUserAction(String action, String details) {
        LogEntry log = LogEntry.info("USER_ACTION", "anonymous",
                        "Action: " + action + " - " + details)
                .addMetadata("action", action)
                .addMetadata("details", details);

        elasticLogService.sendLog(log);
    }

    /**
     * Log d'activité utilisateur
     */
    public void logUserActivity(String activityType, int userId, String userEmail, String details) {
        LogEntry log = LogEntry.info("USER_ACTIVITY", userEmail,
                        "Activité: " + activityType + " - " + details)
                .addMetadata("activityType", activityType)
                .addMetadata("userId", String.valueOf(userId))
                .addMetadata("details", details);

        elasticLogService.sendLog(log);
    }

    /**
     * Log de déconnexion
     */
    public void logUserLogout(int userId, String userEmail) {
        LogEntry log = LogEntry.info("USER_LOGOUT", userEmail,
                        "Déconnexion de l'utilisateur")
                .addMetadata("userId", String.valueOf(userId));

        elasticLogService.sendLog(log);
    }

    // ========== LOGS DE NOTIFICATIONS ==========

    /**
     * Log de chargement des notifications
     */
    public void logNotificationLoad(int userId, int notificationCount) {
        LogEntry log = LogEntry.info("NOTIFICATIONS_LOAD", String.valueOf(userId),
                        "Chargement de " + notificationCount + " notification(s)")
                .addMetadata("userId", String.valueOf(userId))
                .addMetadata("notificationCount", String.valueOf(notificationCount));

        elasticLogService.sendLog(log);
    }

    /**
     * Log de clic sur notification
     */
    public void logNotificationClick(int notificationId, String category, String title) {
        LogEntry log = LogEntry.info("NOTIFICATION_CLICK", "anonymous",
                        "Clic sur notification: " + title)
                .addMetadata("notificationId", String.valueOf(notificationId))
                .addMetadata("category", category)
                .addMetadata("title", title);

        elasticLogService.sendLog(log);
    }

    /**
     * Log de navigation depuis notification
     */
    public void logNotificationNavigation(int notificationId, String category, String title) {
        LogEntry log = LogEntry.info("NOTIFICATION_NAVIGATION", "anonymous",
                        "Navigation depuis notification: " + title)
                .addMetadata("notificationId", String.valueOf(notificationId))
                .addMetadata("category", category)
                .addMetadata("title", title);

        elasticLogService.sendLog(log);
    }

    // ========== LOGS D'ÉVÉNEMENTS UI ==========

    /**
     * Log d'un événement UI
     */
    public void logUIEvent(String eventType, String details) {
        LogEntry log = LogEntry.info("UI_EVENT", "anonymous",
                        "Événement UI: " + eventType + " - " + details)
                .addMetadata("eventType", eventType)
                .addMetadata("details", details);

        elasticLogService.sendLog(log);
    }

    /**
     * Log de chargement de contenu
     */
    public void logContentLoad(String fxmlPath) {
        LogEntry log = LogEntry.info("CONTENT_LOAD", "anonymous",
                        "Chargement du contenu: " + fxmlPath)
                .addMetadata("fxmlPath", fxmlPath);

        elasticLogService.sendLog(log);
    }

    /**
     * Log de succès UI
     */
    public void logUISuccess(String action, String details) {
        LogEntry log = LogEntry.success("UI_SUCCESS", "anonymous",
                        action + " - " + details)
                .addMetadata("action", action)
                .addMetadata("details", details);

        elasticLogService.sendLog(log);
    }

    // ========== LOGS D'ERREURS ==========

    /**
     * Log d'une erreur
     */
    public void logError(String errorType, String errorMessage) {
        LogEntry log = LogEntry.error("CLIENT_ERROR", "anonymous",
                        "Erreur: " + errorType + " - " + errorMessage)
                .addMetadata("errorType", errorType)
                .addMetadata("errorMessage", errorMessage);

        elasticLogService.sendLog(log);
    }

    /**
     * Log d'erreur de session
     */
    public void logSessionExpired(String context) {
        LogEntry log = LogEntry.warning("SESSION_EXPIRED", "anonymous",
                        "Session expirée dans le contexte: " + context)
                .addMetadata("context", context);

        elasticLogService.sendLog(log);
    }

    /**
     * Log d'erreur de base de données
     */
    public void logDatabaseError(String operation, String error) {
        LogEntry log = LogEntry.error("DATABASE_ERROR", "anonymous",
                        "Erreur BD lors de " + operation + " - " + error)
                .addMetadata("operation", operation)
                .addMetadata("error", error);

        elasticLogService.sendLog(log);
    }

    // ========== LOGS DE RÉCUPÉRATION DE DONNÉES ==========

    /**
     * Log de récupération de données
     */
    public void logDataRetrieval(String dataType, Object identifier, boolean success) {
        logDataRetrieval(dataType, identifier, success ? 1 : 0);
    }

    /**
     * Log de récupération de données avec count
     */
    public void logDataRetrieval(String dataType, Object identifier, int count) {
        String status = count > 0 ? "succès" : "échec";

        LogEntry log = LogEntry.info("DATA_RETRIEVAL", "anonymous",
                        "Récupération " + dataType + " ID: " + identifier + " - " + status)
                .addMetadata("dataType", dataType)
                .addMetadata("identifier", identifier.toString())
                .addMetadata("count", String.valueOf(count))
                .addMetadata("status", status);

        elasticLogService.sendLog(log);
    }

    // ========== LOGS D'INFORMATION ==========

    /**
     * Log d'information générale
     */
    public void logInfo(String infoType, String details) {
        LogEntry log = LogEntry.info("CLIENT_INFO", "anonymous",
                        infoType + " - " + details)
                .addMetadata("infoType", infoType)
                .addMetadata("details", details);

        elasticLogService.sendLog(log);
    }
}