package com.example.vehiclegestion.auth.utils;

import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.auth.service.AuthLogService;

/**
 * Gestionnaire de session utilisateur (Singleton)
 * Gère l'état de connexion de l'utilisateur courant avec logs intégrés
 */
public class SessionManager {

    // Singleton - Eager Initialization
    private static SessionManager INSTANCE = new SessionManager();

    // Session utilisateur
    private Utilisateur utilisateurConnecte;
    private boolean sessionActive;

    // Magasin courant (pour les vendeurs)
    private Integer currentMagasinId = null;
    private String currentMagasinNom = null;

    // Service de logs
    private final AuthLogService authLogService;

    /**
     * Constructeur privé (Singleton)
     */
    private SessionManager() {
        this.authLogService = new AuthLogService();
        System.out.println("✅ SessionManager initialisé: " + this.hashCode());
    }

    /**
     * Récupérer l'instance unique du SessionManager
     */
    public static synchronized SessionManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager();
        }
        return INSTANCE;
    }

    // ========== MÉTHODES STATIQUES POUR COMPATIBILITÉ ==========

    /**
     * Définir l'utilisateur connecté (méthode statique)
     */
    public static void setCurrentUser(Utilisateur user) {
        getInstance().demarrerSession(user);
    }

    /**
     * Obtenir l'utilisateur connecté (méthode statique)
     */
    public static Utilisateur getCurrentUser() {
        return getInstance().getUtilisateurConnecte();
    }

    /**
     * Vérifier si un utilisateur est connecté (méthode statique)
     */
    public static boolean isLoggedIn() {
        return getInstance().estConnecte();
    }

    /**
     * Vérifier si l'utilisateur connecté est un administrateur (méthode statique)
     */
    public static boolean isAdmin() {
        return getInstance().estAdmin();
    }

    /**
     * Vérifier si l'utilisateur connecté est un gestionnaire (méthode statique)
     */
    public static boolean isGestionnaire() {
        Utilisateur user = getInstance().getUtilisateurConnecte();
        return user != null && "gestionnaire".equalsIgnoreCase(user.getRole());
    }

    /**
     * Vérifier si l'utilisateur connecté est un vendeur (méthode statique)
     */
    public static boolean isVendeur() {
        return getInstance().estVendeur();
    }

    /**
     * Obtenir l'ID de l'utilisateur connecté (méthode statique)
     */
    public static Integer getCurrentUserId() {
        return getInstance().getIdUtilisateur();
    }

    /**
     * Obtenir le nom complet de l'utilisateur connecté (méthode statique)
     */
    public static String getCurrentUserFullName() {
        return getInstance().getUserFullName();
    }

    /**
     * Obtenir le rôle de l'utilisateur connecté (méthode statique)
     */
    public static String getCurrentUserRole() {
        return getInstance().getRole();
    }

    /**
     * Nettoyer la session (déconnexion) (méthode statique)
     */
    public static void clearSession() {
        getInstance().fermerSession();
    }

    /**
     * Afficher les informations de la session (debug) (méthode statique)
     */
    public static void printSessionInfo() {
        getInstance().debugSession();
    }

    // ========== GESTION DE LA SESSION (MÉTHODES D'INSTANCE) ==========

    /**
     * Démarrer une session pour un utilisateur
     */
    public void demarrerSession(Utilisateur utilisateur) {
        if (utilisateur == null) {
            System.err.println("❌ Tentative de démarrer une session avec un utilisateur null");
            return;
        }

        this.utilisateurConnecte = utilisateur;
        this.sessionActive = true;

        System.out.println("🔐 Session démarrée pour: " + utilisateur.getEmail());
        System.out.println("🆔 ID Utilisateur: " + utilisateur.getIdUtilisateur());
        System.out.println("🎭 Rôle: " + utilisateur.getRole());

        debugSession();

        // Pas de log ici car déjà logué dans LoginController ou AuthentificationService
    }

    /**
     * Terminer la session (déconnexion)
     */
    public void fermerSession() {
        if (utilisateurConnecte != null) {
            System.out.println("🔒 Session fermée pour: " + utilisateurConnecte.getEmail());

            // ✅ LOG: Déconnexion
            authLogService.logLogout(
                    utilisateurConnecte.getEmail(),
                    utilisateurConnecte.getRole(),
                    (long) utilisateurConnecte.getIdUtilisateur()
            );
        }

        this.utilisateurConnecte = null;
        this.sessionActive = false;
        this.clearCurrentMagasin(); // Effacer aussi le magasin courant

        debugSession();
    }

    /**
     * Vérifier si un utilisateur est connecté
     */
    public boolean estConnecte() {
        return sessionActive && utilisateurConnecte != null;
    }

    /**
     * Récupérer l'utilisateur connecté
     */
    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    // ========== INFORMATIONS UTILISATEUR ==========

    /**
     * Récupérer l'ID de l'utilisateur connecté
     */
    public int getUserId() {
        return utilisateurConnecte != null ? utilisateurConnecte.getIdUtilisateur() : -1;
    }

    /**
     * Récupérer l'ID de l'utilisateur connecté (retourne Integer pour compatibilité)
     */
    public Integer getIdUtilisateur() {
        return utilisateurConnecte != null ? utilisateurConnecte.getIdUtilisateur() : null;
    }

    /**
     * Récupérer le rôle de l'utilisateur connecté
     */
    public String getRole() {
        return utilisateurConnecte != null ? utilisateurConnecte.getRole() : null;
    }

    /**
     * Récupérer le rôle de l'utilisateur connecté (alias pour compatibilité)
     */
    public String getUserRole() {
        return getRole();
    }

    /**
     * Récupérer l'email de l'utilisateur connecté
     */
    public String getEmail() {
        return utilisateurConnecte != null ? utilisateurConnecte.getEmail() : null;
    }

    /**
     * Vérifier si l'utilisateur a un rôle spécifique
     */
    public boolean hasRole(String role) {
        return utilisateurConnecte != null && role.equals(utilisateurConnecte.getRole());
    }

    /**
     * Vérifier si l'utilisateur est admin
     */
    public boolean estAdmin() {
        return hasRole("admin");
    }
    public String getUserEmail() {
        return utilisateurConnecte != null ? utilisateurConnecte.getEmail() : null;
    }
    /**
     * Vérifier si l'utilisateur est vendeur
     */
    public boolean estVendeur() {
        return hasRole("vendeur");
    }

    /**
     * Vérifier si l'utilisateur est client
     */
    public boolean estClient() {
        return hasRole("client");
    }

    /**
     * Récupérer le nom complet de l'utilisateur connecté
     * Format: "Prénom Nom"
     */
    public String getUserFullName() {
        if (utilisateurConnecte != null) {
            String prenom = utilisateurConnecte.getPrenom() != null ? utilisateurConnecte.getPrenom() : "";
            String nom = utilisateurConnecte.getNom() != null ? utilisateurConnecte.getNom() : "";

            // Nettoyer les espaces et formater
            String fullName = (prenom + " " + nom).trim();

            // Si vide, retourner l'email ou "Utilisateur"
            if (fullName.isEmpty()) {
                return utilisateurConnecte.getEmail() != null ?
                        utilisateurConnecte.getEmail() : "Utilisateur";
            }

            return fullName;
        }
        return "Utilisateur";
    }

    // ========== GESTION DU MAGASIN COURANT ==========

    /**
     * Définir l'ID du magasin courant
     */
    public void setCurrentMagasinId(Integer magasinId) {
        this.currentMagasinId = magasinId;
        System.out.println("✅ SessionManager - Magasin ID défini: " + magasinId);
    }

    /**
     * Récupérer l'ID du magasin courant
     */
    public Integer getCurrentMagasinId() {
        return currentMagasinId;
    }

    /**
     * Définir le nom du magasin courant
     */
    public void setCurrentMagasinNom(String magasinNom) {
        this.currentMagasinNom = magasinNom;
        System.out.println("✅ SessionManager - Magasin Nom défini: " + magasinNom);
    }

    /**
     * Récupérer le nom du magasin courant
     */
    public String getCurrentMagasinNom() {
        return currentMagasinNom;
    }

    /**
     * Effacer le magasin courant
     */
    public void clearCurrentMagasin() {
        this.currentMagasinId = null;
        this.currentMagasinNom = null;
        System.out.println("🧹 SessionManager - Magasin courant effacé");
    }

    // ========== DEBUG ==========

    /**
     * Afficher les informations de session (debug)
     */
    public void debugSession() {
        System.out.println("=== DEBUG SESSION ===");
        System.out.println("Instance: " + this.hashCode());
        System.out.println("Session active: " + sessionActive);

        if (utilisateurConnecte != null) {
            System.out.println("Utilisateur: " + utilisateurConnecte.getEmail() +
                    " (" + utilisateurConnecte.getRole() + ")");
            System.out.println("   - ID: " + utilisateurConnecte.getIdUtilisateur());
            System.out.println("   - Nom: " + utilisateurConnecte.getNom());
            System.out.println("   - Prénom: " + utilisateurConnecte.getPrenom());

            if (currentMagasinId != null) {
                System.out.println("Magasin courant: " + currentMagasinNom +
                        " (ID: " + currentMagasinId + ")");
            }
        } else {
            System.out.println("Utilisateur: null");
        }

        System.out.println("=====================");
    }
}