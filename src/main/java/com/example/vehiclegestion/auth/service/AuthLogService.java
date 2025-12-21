package com.example.vehiclegestion.auth.service;

import com.example.vehiclegestion.logging.model.LogEntry;
import com.example.vehiclegestion.logging.service.ElasticLogService;

/**
 * Service dédié aux logs d'authentification et d'administration
 * Simplifie l'envoi de logs depuis les contrôleurs
 */
public class AuthLogService {

    private final ElasticLogService elasticLogService;

    public AuthLogService() {
        this.elasticLogService = new ElasticLogService();
    }

    // ========== LOGS DE CONNEXION ==========

    /**
     * Log d'une connexion réussie
     */
    public void logSuccessfulLogin(String email, String role) {
        LogEntry log = LogEntry.success("LOGIN_SUCCESS", email,
                        "Connexion réussie pour l'utilisateur " + email + " avec le rôle " + role)
                .addMetadata("role", role)
                .addMetadata("loginMethod", "email_password");

        log.setUserRole(role);
        elasticLogService.sendLog(log);
    }

    /**
     * Log d'une connexion échouée
     */
    public void logFailedLogin(String email, String reason) {
        LogEntry log = LogEntry.error("LOGIN_FAILED", email,
                        "Échec de connexion pour " + email + " - Raison: " + reason)
                .addMetadata("failureReason", reason)
                .addMetadata("loginMethod", "email_password");

        elasticLogService.sendLog(log);
    }

    /**
     * Log quand un compte n'est pas trouvé
     */
    public void logAccountNotFound(String email) {
        LogEntry log = LogEntry.warning("ACCOUNT_NOT_FOUND", email,
                        "Tentative de connexion avec un email inexistant: " + email)
                .addMetadata("errorType", "ACCOUNT_NOT_FOUND");

        elasticLogService.sendLog(log);
    }

    // ========== LOGS D'INSCRIPTION ==========

    /**
     * Log d'une inscription réussie
     */
    public void logSuccessfulRegistration(String email, String role) {
        LogEntry log = LogEntry.success("REGISTER_SUCCESS", email,
                        "Inscription réussie pour " + email + " en tant que " + role)
                .addMetadata("role", role)
                .addMetadata("registrationMethod", "email_password");

        log.setUserRole(role);
        elasticLogService.sendLog(log);
    }

    /**
     * Log d'une inscription échouée
     */
    public void logFailedRegistration(String email, String reason) {
        LogEntry log = LogEntry.error("REGISTER_FAILED", email,
                        "Échec d'inscription pour " + email + " - Raison: " + reason)
                .addMetadata("failureReason", reason);

        elasticLogService.sendLog(log);
    }

    // ========== LOGS DE DÉCONNEXION ==========

    /**
     * Log d'une déconnexion
     */
    public void logLogout(String email, String role, Long userId) {
        LogEntry log = LogEntry.success("LOGOUT", email,
                "Déconnexion de l'utilisateur " + email);

        log.setUserRole(role);
        log.setUserId(userId);
        elasticLogService.sendLog(log);
    }

    // ========== LOGS DE RÉCUPÉRATION DE MOT DE PASSE ==========

    /**
     * Log d'une demande de récupération de mot de passe
     */
    public void logPasswordResetRequest(String email) {
        LogEntry log = LogEntry.success("PASSWORD_RESET_REQUEST", email,
                        "Demande de réinitialisation de mot de passe pour " + email)
                .addMetadata("resetMethod", "email");

        elasticLogService.sendLog(log);
    }

    /**
     * Log d'un changement de mot de passe réussi
     */
    public void logPasswordResetSuccess(String email) {
        LogEntry log = LogEntry.success("PASSWORD_RESET_SUCCESS", email,
                "Mot de passe réinitialisé avec succès pour " + email);

        elasticLogService.sendLog(log);
    }

    // ========== LOGS D'ADMINISTRATION ==========

    /**
     * Log d'une action administrative
     * Utilisé pour toutes les actions de gestion admin (CRUD users, magasins, etc.)
     */
    public void logAdminAction(String adminEmail, String action, String description) {
        LogEntry log = LogEntry.success("ADMIN_ACTION", adminEmail,
                        description)
                .addMetadata("action", action)
                .addMetadata("adminEmail", adminEmail)
                .addMetadata("actionType", "ADMINISTRATION");

        log.setUserRole("admin");
        elasticLogService.sendLog(log);
    }

    /**
     * Log d'une action administrative avec des détails supplémentaires
     * @param targetEntity Type d'entité ciblée (USER, MAGASIN, etc.)
     * @param targetId ID de l'entité ciblée
     */
    public void logAdminAction(String adminEmail, String action, String description,
                               String targetEntity, String targetId) {
        LogEntry log = LogEntry.success("ADMIN_ACTION", adminEmail,
                        description)
                .addMetadata("action", action)
                .addMetadata("adminEmail", adminEmail)
                .addMetadata("actionType", "ADMINISTRATION")
                .addMetadata("targetEntity", targetEntity)
                .addMetadata("targetId", targetId);

        log.setUserRole("admin");
        elasticLogService.sendLog(log);
    }

    /**
     * Log d'une action administrative qui a échoué
     */
    public void logAdminActionFailed(String adminEmail, String action, String reason) {
        LogEntry log = LogEntry.error("ADMIN_ACTION_FAILED", adminEmail,
                        "Échec de l'action admin: " + action + " - Raison: " + reason)
                .addMetadata("action", action)
                .addMetadata("adminEmail", adminEmail)
                .addMetadata("actionType", "ADMINISTRATION")
                .addMetadata("failureReason", reason);

        log.setUserRole("admin");
        elasticLogService.sendLog(log);
    }

    // ========== LOGS DE SÉCURITÉ ==========

    /**
     * Log d'une tentative de connexion suspecte
     */
    public void logSuspiciousActivity(String email, String reason) {
        LogEntry log = LogEntry.warning("SUSPICIOUS_ACTIVITY", email,
                        "Activité suspecte détectée pour " + email + " - " + reason)
                .addMetadata("securityAlert", true)
                .addMetadata("reason", reason);

        elasticLogService.sendLog(log);
    }

    /**
     * Log d'un blocage de compte
     */
    public void logAccountBlocked(String email, String reason) {
        LogEntry log = LogEntry.error("ACCOUNT_BLOCKED", email,
                        "Compte bloqué pour " + email + " - Raison: " + reason)
                .addMetadata("securityAlert", true)
                .addMetadata("blockReason", reason);

        elasticLogService.sendLog(log);
    }

    // ========== LOGS D'ERREURS TECHNIQUES ==========

    /**
     * Log d'une erreur technique
     */
    public void logTechnicalError(String action, String email, String errorMessage) {
        LogEntry log = LogEntry.error("TECHNICAL_ERROR", email,
                        "Erreur technique lors de " + action + " - " + errorMessage)
                .addMetadata("errorType", "TECHNICAL")
                .addMetadata("errorMessage", errorMessage);

        elasticLogService.sendLog(log);
    }

    // ========== LOGS DE VALIDATION ==========

    /**
     * Log d'une erreur de validation
     */
    public void logValidationError(String email, String field, String error) {
        LogEntry log = LogEntry.warning("VALIDATION_ERROR", email,
                        "Erreur de validation pour " + email + " - Champ: " + field + ", Erreur: " + error)
                .addMetadata("field", field)
                .addMetadata("validationError", error);

        elasticLogService.sendLog(log);
    }
}