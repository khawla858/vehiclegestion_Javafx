package com.example.vehiclegestion.logging.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Modèle représentant une entrée de log pour l'authentification
 * Ce modèle sera indexé dans Elasticsearch
 */
public class LogEntry {

    // ========== ATTRIBUTS PRINCIPAUX ==========

    /**
     * Horodatage du log (format ISO 8601)
     * Exemple: "2024-03-15T14:30:45.123"
     */
    private String timestamp;

    /**
     * Niveau de sévérité du log
     * Valeurs: INFO, WARN, ERROR, DEBUG
     */
    private String level;

    /**
     * Type d'action effectuée
     * Exemples: LOGIN_SUCCESS, LOGIN_FAILED, REGISTER_SUCCESS, LOGOUT, etc.
     */
    private String action;

    /**
     * Email de l'utilisateur concerné
     */
    private String userEmail;

    /**
     * Rôle de l'utilisateur (client, vendeur, admin)
     */
    private String userRole;

    /**
     * ID de l'utilisateur (null si non connecté)
     */
    private Long userId;

    /**
     * Message descriptif du log
     */
    private String message;

    /**
     * Adresse IP de l'utilisateur (optionnel)
     */
    private String ipAddress;

    /**
     * Données supplémentaires sous forme de clé-valeur
     * Exemple: {errorCode: "AUTH_001", attemptNumber: 3}
     */
    private Map<String, Object> metadata;

    /**
     * Nom de l'application/module
     */
    private String applicationName;

    /**
     * Environnement (dev, prod, test)
     */
    private String environment;

    // ========== CONSTRUCTEURS ==========

    /**
     * Constructeur par défaut
     */
    public LogEntry() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.metadata = new HashMap<>();
        this.applicationName = "VehicleGestion";
        this.environment = "production";
    }

    /**
     * Constructeur pour les logs d'authentification
     */
    public LogEntry(String level, String action, String userEmail, String message) {
        this();
        this.level = level;
        this.action = action;
        this.userEmail = userEmail;
        this.message = message;
    }

    /**
     * Constructeur complet
     */
    public LogEntry(String level, String action, String userEmail, String userRole,
                    Long userId, String message, String ipAddress) {
        this(level, action, userEmail, message);
        this.userRole = userRole;
        this.userId = userId;
        this.ipAddress = ipAddress;
    }

    // ========== MÉTHODES UTILITAIRES ==========

    /**
     * Ajouter des métadonnées au log
     */
    public LogEntry addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    /**
     * Convertir le log en Map pour Elasticsearch
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", timestamp);
        map.put("level", level);
        map.put("action", action);
        map.put("userEmail", userEmail);
        map.put("userRole", userRole);
        map.put("userId", userId);
        map.put("message", message);
        map.put("ipAddress", ipAddress);
        map.put("metadata", metadata);
        map.put("applicationName", applicationName);
        map.put("environment", environment);
        return map;
    }

    /**
     * Créer un log de succès (alias de info)
     */
    public static LogEntry success(String action, String userEmail, String message) {
        return new LogEntry("INFO", action, userEmail, message);
    }

    /**
     * Créer un log d'information
     */
    public static LogEntry info(String action, String userEmail, String message) {
        return new LogEntry("INFO", action, userEmail, message);
    }

    /**
     * Créer un log d'erreur
     */
    public static LogEntry error(String action, String userEmail, String message) {
        return new LogEntry("ERROR", action, userEmail, message);
    }

    /**
     * Créer un log d'avertissement
     */
    public static LogEntry warning(String action, String userEmail, String message) {
        return new LogEntry("WARN", action, userEmail, message);
    }

    // ========== GETTERS ET SETTERS ==========

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    // ========== MÉTHODE TOSTRING ==========

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | User: %s | %s",
                timestamp, level, action, userEmail, message);
    }

    // ========== CONSTANTES D'ACTIONS ==========


        // Authentification
        public static final String ACTION_LOGIN_SUCCESS = "LOGIN_SUCCESS";
        public static final String ACTION_LOGIN_FAILED = "LOGIN_FAILED";
        public static final String ACTION_REGISTER_SUCCESS = "REGISTER_SUCCESS";
        public static final String ACTION_REGISTER_FAILED = "REGISTER_FAILED";
        public static final String ACTION_LOGOUT = "LOGOUT";

        // Véhicules (liste)
        public static final String ACTION_VEHICLE_LOAD_SUCCESS = "VEHICLE_LOAD_SUCCESS";
        public static final String ACTION_VEHICLE_LOAD_ERROR = "VEHICLE_LOAD_ERROR";
        public static final String ACTION_NO_VEHICLES_FOUND = "NO_VEHICLES_FOUND";
        public static final String ACTION_FILTER_APPLIED = "FILTER_APPLIED";
        public static final String ACTION_FILTERS_RESET = "FILTERS_RESET";
        public static final String ACTION_SORT_APPLIED = "SORT_APPLIED";
        public static final String ACTION_VEHICLE_VIEW = "VEHICLE_VIEW";
        public static final String ACTION_FAVORITE_ADDED = "FAVORITE_ADDED";
        public static final String ACTION_FAVORITE_REMOVED = "FAVORITE_REMOVED";
        public static final String ACTION_FAVORITE_ERROR = "FAVORITE_ERROR";
        public static final String ACTION_IMAGE_LOAD_ERROR = "IMAGE_LOAD_ERROR";
        public static final String ACTION_DISPLAY_PERFORMANCE = "DISPLAY_PERFORMANCE";
        public static final String ACTION_FILTERS_TOGGLE = "FILTERS_TOGGLE";

        // Détails des véhicules (à AJOUTER)
        public static final String ACTION_VEHICLE_DETAILS_OPENED = "VEHICLE_DETAILS_OPENED";
        public static final String ACTION_VEHICLE_DETAILS_CLOSED = "VEHICLE_DETAILS_CLOSED";
        public static final String ACTION_VEHICLE_DETAILS_LOAD_FAILED = "VEHICLE_DETAILS_LOAD_FAILED";
        public static final String ACTION_COMMENTS_VIEWED = "COMMENTS_VIEWED";
        public static final String ACTION_COMMENT_CREATED = "COMMENT_CREATED";
        public static final String ACTION_COMMENT_SAVE_ERROR = "COMMENT_SAVE_ERROR";
        public static final String ACTION_COMMENT_SAVE_FAILED = "COMMENT_SAVE_FAILED";
        public static final String ACTION_COMMENT_UNAUTHORIZED = "COMMENT_UNAUTHORIZED";
        public static final String ACTION_STORE_VISITED = "STORE_VISITED";
        public static final String ACTION_CONTACT_INITIATED = "CONTACT_INITIATED";
        public static final String ACTION_CHAT_OPENED = "CHAT_OPENED";
        public static final String ACTION_CONTACT_UNAUTHORIZED = "CONTACT_UNAUTHORIZED";
        public static final String ACTION_SELF_CONTACT_BLOCKED = "SELF_CONTACT_BLOCKED";
        public static final String ACTION_APPOINTMENT_REQUESTED = "APPOINTMENT_REQUESTED";
        public static final String ACTION_APPOINTMENT_CREATED = "APPOINTMENT_CREATED";
        public static final String ACTION_PAGE_LOAD_PERFORMANCE = "PAGE_LOAD_PERFORMANCE";

        // ... existing code ...
    }
