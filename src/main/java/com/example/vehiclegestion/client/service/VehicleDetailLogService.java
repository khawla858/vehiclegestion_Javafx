package com.example.vehiclegestion.client.service;

import com.example.vehiclegestion.logging.model.LogEntry;
import com.example.vehiclegestion.logging.service.ElasticLogService;
import com.example.vehiclegestion.vendeur.model.Magasin;
import com.example.vehiclegestion.vendeur.model.Article;
import com.example.vehiclegestion.auth.SessionManager;

/**
 * Service de logs pour les opérations sur les détails des véhicules
 */
public class VehicleDetailLogService {

    private final ElasticLogService elasticLogService;

    public VehicleDetailLogService() {
        this.elasticLogService = new ElasticLogService();
    }

    // ========== LOGS D'OUVERTURE/FERMETURE ==========

    public void logVehicleDetailsOpened(Article article, SessionManager session) {
        LogEntry logEntry = LogEntry.success(
                        LogEntry.ACTION_VEHICLE_DETAILS_OPENED,
                        session.estConnecte() ? session.getUserEmail() : "anonymous",
                        "Ouverture des détails du véhicule: " + article.getTitre()
                )
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre())
                .addMetadata("price", article.getPrix())
                .addMetadata("category", article.getCategorie());

        if (session.estConnecte()) {
            logEntry.setUserId((long) session.getUserId());
            logEntry.setUserRole(session.getUserRole());
        }

        sendLog(logEntry);
    }

    public void logVehicleDetailsClosed(Article article, String userEmail) {
        LogEntry logEntry = LogEntry.success(
                "VEHICLE_DETAILS_CLOSED",
                userEmail,
                "Fermeture des détails du véhicule"
        );

        if (article != null) {
            logEntry.addMetadata("articleId", article.getId())
                    .addMetadata("articleTitle", article.getTitre());
        }

        sendLog(logEntry);
    }

    public void logDataLoadFailed(Object data, String userEmail) {
        LogEntry errorLog = LogEntry.error(
                "VEHICLE_DETAILS_LOAD_FAILED",
                userEmail,
                "Échec du chargement des détails du véhicule"
        ).addMetadata("dataType", data != null ? data.getClass().getName() : "null");

        sendLog(errorLog);
    }

    // ========== LOGS DE COMMENTAIRES ==========

    public void logCommentsViewed(Article article, String userEmail) {
        LogEntry logEntry = LogEntry.success(
                        "COMMENTS_VIEWED",
                        userEmail,
                        "Consultation des avis du véhicule"
                )
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre());

        sendLog(logEntry);
    }

    public void logCommentCreated(SessionManager session, Article article, int note, int commentLength) {
        LogEntry logEntry = LogEntry.success(
                        "COMMENT_CREATED",
                        session.getUserEmail(),
                        "Nouvel avis publié"
                )
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre())
                .addMetadata("rating", note)
                .addMetadata("commentLength", commentLength);

        logEntry.setUserId((long) session.getUserId());
        logEntry.setUserRole(session.getUserRole());

        sendLog(logEntry);
    }

    public void logCommentSaveError(SessionManager session, int articleId, int userId) {
        LogEntry logEntry = LogEntry.error(
                        "COMMENT_SAVE_ERROR",
                        session.getUserEmail(),
                        "Erreur lors de la sauvegarde du commentaire"
                )
                .addMetadata("articleId", articleId)
                .addMetadata("userId", userId);

        sendLog(logEntry);
    }

    public void logCommentSaveFailed(String userEmail, String reason) {
        sendLog(LogEntry.error(
                "COMMENT_SAVE_FAILED",
                userEmail,
                "Tentative de commentaire: " + reason
        ));
    }

    public void logUnauthorizedCommentAttempt(int articleId) {
        sendLog(LogEntry.warning(
                "COMMENT_UNAUTHORIZED",
                "anonymous",
                "Tentative de commentaire sans être connecté"
        ).addMetadata("articleId", articleId));
    }

    // ========== LOGS DE MAGASIN ==========

    public void logStoreVisited(Magasin magasin, Article article, SessionManager session) {
        LogEntry logEntry = LogEntry.success(
                        "STORE_VISITED",
                        session.estConnecte() ? session.getUserEmail() : "anonymous",
                        "Visite du magasin depuis les détails du véhicule"
                )
                .addMetadata("magasinId", magasin.getIdMagasin())
                .addMetadata("magasinNom", magasin.getNomMagasin())
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre());

        if (session.estConnecte()) {
            logEntry.setUserId((long) session.getUserId());
            logEntry.setUserRole(session.getUserRole());
        }

        sendLog(logEntry);
    }

    // ========== LOGS DE CONTACT ==========

    public void logContactInitiated(SessionManager session, Article article, int vendeurId) {
        LogEntry logEntry = LogEntry.success(
                        "CONTACT_INITIATED",
                        session.getUserEmail(),
                        "Démarrage du contact avec le vendeur"
                )
                .addMetadata("clientId", session.getUserId())
                .addMetadata("vendeurId", vendeurId)
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre());

        logEntry.setUserId((long) session.getUserId());
        logEntry.setUserRole(session.getUserRole());

        sendLog(logEntry);
    }

    public void logChatOpened(SessionManager session, int vendeurId, Article article) {
        LogEntry logEntry = LogEntry.success(
                        "CHAT_OPENED",
                        session.getUserEmail(),
                        "Ouverture du chat avec le vendeur"
                )
                .addMetadata("clientId", session.getUserId())
                .addMetadata("vendeurId", vendeurId)
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre());

        logEntry.setUserId((long) session.getUserId());
        logEntry.setUserRole(session.getUserRole());

        sendLog(logEntry);
    }

    public void logUnauthorizedContactAttempt(int articleId) {
        sendLog(LogEntry.warning(
                "CONTACT_UNAUTHORIZED",
                "anonymous",
                "Tentative de contact sans être connecté"
        ).addMetadata("articleId", articleId));
    }

    public void logSelfContactBlocked(SessionManager session, int articleId) {
        sendLog(LogEntry.warning(
                        "SELF_CONTACT_BLOCKED",
                        session.getUserEmail(),
                        "Tentative de contacter sa propre annonce"
                )
                .addMetadata("userId", session.getUserId())
                .addMetadata("articleId", articleId));
    }

    // ========== LOGS DE RENDEZ-VOUS ==========

    public void logRendezVousRequested(SessionManager session, Article article, String date, String time) {
        LogEntry logEntry = LogEntry.success(
                        "APPOINTMENT_REQUESTED",
                        session.getUserEmail(),
                        "Demande de rendez-vous pour le véhicule"
                )
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre())
                .addMetadata("date", date)
                .addMetadata("time", time);

        logEntry.setUserId((long) session.getUserId());
        logEntry.setUserRole(session.getUserRole());

        sendLog(logEntry);
    }

    public void logRendezVousCreated(SessionManager session, Article article) {
        LogEntry logEntry = LogEntry.success(
                        "APPOINTMENT_CREATED",
                        session.getUserEmail(),
                        "Rendez-vous créé avec succès"
                )
                .addMetadata("articleId", article.getId())
                .addMetadata("articleTitle", article.getTitre());

        logEntry.setUserId((long) session.getUserId());
        logEntry.setUserRole(session.getUserRole());

        sendLog(logEntry);
    }

    // ========== LOGS D'ERREURS D'IMAGE ==========

    public void logImageLoadError(int articleId, String imagePath, String error) {
        sendLog(LogEntry.error(
                        "IMAGE_LOAD_ERROR",
                        null,
                        "Erreur lors du chargement de l'image pour l'article " + articleId
                )
                .addMetadata("articleId", articleId)
                .addMetadata("imagePath", imagePath)
                .addMetadata("error", error));
    }

    // ========== LOGS DE PERFORMANCE ==========

    public void logPageLoadTime(long loadTimeMs, String pageType) {
        sendLog(LogEntry.info(
                        "PAGE_LOAD_PERFORMANCE",
                        null,
                        "Chargement de la page " + pageType + " en " + loadTimeMs + "ms"
                )
                .addMetadata("loadTimeMs", loadTimeMs)
                .addMetadata("pageType", pageType));
    }

    // ========== MÉTHODE UTILITAIRE ==========

    private void sendLog(LogEntry logEntry) {
        elasticLogService.sendLog(logEntry);
    }

    public void sendLog(String level, String message) {
        elasticLogService.sendLog(level, message);
    }

    public void shutdown() {
        elasticLogService.shutdown();
    }
}