package com.example.vehiclegestion.logging.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class LogEntry {

    // ===== MODULES =====
    public static final String MODULE_AUTH = "AUTH";
    public static final String MODULE_CLIENT = "CLIENT";
    public static final String MODULE_SYSTEM = "SYSTEM";

    //zeugzejhdb
    public static final String ACTION_VEHICLE_LOAD_SUCCESS = "VEHICLE_LOAD_SUCCESS";
    public static final String ACTION_VEHICLE_LOAD_ERROR = "VEHICLE_LOAD_ERROR";
    public static final String ACTION_NO_VEHICLES_FOUND = "NO_VEHICLES_FOUND";

    public static final String ACTION_FILTER_APPLIED = "FILTER_APPLIED";
    public static final String ACTION_FILTERS_RESET = "FILTERS_RESET";
    public static final String ACTION_SORT_APPLIED = "SORT_APPLIED";
    public static final String ACTION_FILTERS_TOGGLE = "FILTERS_TOGGLE";

    public static final String ACTION_VEHICLE_VIEW = "VEHICLE_VIEW";

    public static final String ACTION_FAVORITE_ADDED = "FAVORITE_ADDED";
    public static final String ACTION_FAVORITE_REMOVED = "FAVORITE_REMOVED";
    public static final String ACTION_FAVORITE_ERROR = "FAVORITE_ERROR";

    public static final String ACTION_IMAGE_LOAD_ERROR = "IMAGE_LOAD_ERROR";
    public static final String ACTION_DISPLAY_PERFORMANCE = "DISPLAY_PERFORMANCE";

    // ========== À AJOUTER DANS LogEntry.java ==========
// Section: ACTIONS CLIENT - VÉHICULES




    public static final String ACTION_VEHICLE_DETAILS_OPENED = "VEHICLE_DETAILS_OPENED";
    public static final String ACTION_VEHICLE_DETAILS_CLOSED = "VEHICLE_DETAILS_CLOSED";
    public static final String ACTION_VEHICLE_DETAILS_LOAD_FAILED = "VEHICLE_DETAILS_LOAD_FAILED";





    // Section: COMMENTAIRES
    public static final String COMMENT_CREATED = "COMMENT_CREATED";
    public static final String COMMENT_SAVE_ERROR = "COMMENT_SAVE_ERROR";
    public static final String COMMENT_SAVE_FAILED = "COMMENT_SAVE_FAILED";
    public static final String COMMENT_UNAUTHORIZED = "COMMENT_UNAUTHORIZED";
    public static final String COMMENTS_VIEWED = "COMMENTS_VIEWED";

    // Section: MAGASIN
    public static final String STORE_VISIT_INITIATED = "STORE_VISIT_INITIATED";
    public static final String STORE_NOT_FOUND = "STORE_NOT_FOUND";
    public static final String STORE_OPENED = "STORE_OPENED";

    // Section: RENDEZ-VOUS
    public static final String APPOINTMENT_REQUEST_INITIATED = "APPOINTMENT_REQUEST_INITIATED";

    // Section: CHAT
    public static final String CHAT_OPENED = "CHAT_OPENED";
    public static final String CONTACT_UNAUTHORIZED = "CONTACT_UNAUTHORIZED";
    public static final String SELF_CONTACT_BLOCKED = "SELF_CONTACT_BLOCKED";


    // ===== CHAMPS =====
    private String level;
    private String action;
    private String module;
    private String userEmail;
    private Long userId;
    private String userRole;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, Object> metadata = new HashMap<>();

    // ===== CONSTRUCTEUR PRIVÉ =====
    private LogEntry(String level, String action, String module, String userEmail, String message) {
        this.level = level;
        this.action = action;
        this.module = module;
        this.userEmail = userEmail;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // ===== FACTORY METHODS =====
    public static LogEntry success(String action, String userEmail, String message) {
        return new LogEntry("SUCCESS", action, MODULE_SYSTEM, userEmail, message);
    }

    public static LogEntry error(String action, String userEmail, String message) {
        return new LogEntry("ERROR", action, MODULE_SYSTEM, userEmail, message);
    }

    public static LogEntry warning(String action, String userEmail, String message) {
        return new LogEntry("WARNING", action, MODULE_SYSTEM, userEmail, message);
    }

    // ===== FLUENT SETTERS =====
    public LogEntry setModule(String module) {
        this.module = module;
        return this;
    }

    public LogEntry setUserId(Long userId) {
        this.userId = userId;
        return this;
    }

    public LogEntry setUserRole(String userRole) {
        this.userRole = userRole;
        return this;
    }

    public LogEntry addMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }

    // ===== GETTERS =====
    public String getLevel() { return level; }
    public String getAction() { return action; }
    public String getModule() { return module; }
    public String getUserEmail() { return userEmail; }
    public Long getUserId() { return userId; }
    public String getUserRole() { return userRole; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, Object> getMetadata() { return metadata; }

    // ===== ELASTICSEARCH MAP =====
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("level", level);
        map.put("action", action);
        map.put("module", module);
        map.put("userEmail", userEmail);
        map.put("userId", userId);
        map.put("userRole", userRole);
        map.put("message", message);
        map.put("timestamp", timestamp.toString());
        map.put("metadata", metadata);
        return map;
    }
    public static LogEntry info(String action, String userEmail, String message) {
        return new LogEntry("INFO", action, MODULE_SYSTEM, userEmail, message);
    }
    public LogEntry setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        return this;
    }
    // Constructeur par défaut
    public LogEntry() {
        this.timestamp = LocalDateTime.now();
        this.metadata = new HashMap<>();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setLevel(String level) {
        this.level = level;
    }
    public void setAction(String action) {
        this.action = action;
    }







}
