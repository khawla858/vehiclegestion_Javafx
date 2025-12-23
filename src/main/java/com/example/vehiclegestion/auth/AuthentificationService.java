package com.example.vehiclegestion.auth;

import com.example.vehiclegestion.auth.dao.UtilisateurDAO;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.auth.utils.SessionManager;
import com.example.vehiclegestion.auth.service.AuthLogService;

/**
 * Service d'authentification avec logs intégrés
 */
public class AuthentificationService {

    private final UtilisateurDAO utilisateurDAO;
    private final AuthLogService authLogService;

    public AuthentificationService() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.authLogService = new AuthLogService();
    }

    // ========== CONNEXION ==========

    /**
     * Connexion d'un utilisateur
     */
    public Utilisateur seConnecter(String email, String password) {
        System.out.println("🔐 Tentative de connexion pour: " + email);

        try {
            // 1. Vérifier dans la BD
            Utilisateur user = utilisateurDAO.seConnecter(email, password);

            if (user != null) {
                System.out.println("✅ Connexion réussie pour: " + email + " (" + user.getRole() + ")");

                // 2. Démarrer la session
                SessionManager.getInstance().demarrerSession(user);

                // 3. Log de connexion réussie
                authLogService.logSuccessfulLogin(user.getEmail(), user.getRole());

                return user;
            } else {
                System.out.println("❌ Échec connexion pour: " + email);

                // Log de connexion échouée
                authLogService.logFailedLogin(email, "Email ou mot de passe incorrect");

                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur technique lors de la connexion: " + e.getMessage());
            authLogService.logTechnicalError("CONNEXION", email, e.getMessage());
            throw e;
        }
    }

    // ========== INSCRIPTION ==========

    /**
     * Inscription d'un nouvel utilisateur
     */
    public boolean inscrire(Utilisateur user) {
        System.out.println("📝 Tentative d'inscription pour: " + user.getEmail());

        try {
            // 1. Vérifier si l'email existe déjà
            if (utilisateurDAO.emailExists(user.getEmail())) {
                System.out.println("❌ Email déjà utilisé: " + user.getEmail());
                authLogService.logFailedRegistration(user.getEmail(), "Email déjà utilisé");
                return false;
            }

            // 2. Créer l'utilisateur
            boolean success = utilisateurDAO.inscrire(user);

            if (success) {
                System.out.println("✅ Inscription réussie pour: " + user.getEmail());
                authLogService.logSuccessfulRegistration(user.getEmail(), user.getRole());
            } else {
                System.out.println("❌ Échec inscription pour: " + user.getEmail());
                authLogService.logFailedRegistration(user.getEmail(), "Erreur lors de la sauvegarde");
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ Erreur technique lors de l'inscription: " + e.getMessage());
            authLogService.logTechnicalError("INSCRIPTION", user.getEmail(), e.getMessage());
            throw e;
        }
    }

    // ========== DÉCONNEXION ==========

    /**
     * Déconnexion - ferme la session et log l'action
     */
    public void seDeconnecter() {
        // La méthode fermerSession() de SessionManager va automatiquement logger
        SessionManager.getInstance().fermerSession();
        System.out.println("🔓 Déconnexion réussie");
    }

    // ========== VÉRIFICATIONS ==========

    /**
     * Vérifier si un utilisateur est connecté
     */
    public boolean estConnecte() {
        return SessionManager.getInstance().estConnecte();
    }

    /**
     * Récupérer l'utilisateur connecté
     */
    public Utilisateur getUtilisateurConnecte() {
        return SessionManager.getInstance().getUtilisateurConnecte();
    }

    /**
     * Récupérer le rôle de l'utilisateur connecté
     */
    public String getRoleUtilisateurConnecte() {
        return SessionManager.getInstance().getUserRole();
    }

    /**
     * Vérifier si l'utilisateur est admin
     */
    public boolean estAdmin() {
        return SessionManager.getInstance().estAdmin();
    }

    /**
     * Vérifier si l'utilisateur est vendeur
     */
    public boolean estVendeur() {
        return SessionManager.getInstance().estVendeur();
    }

    /**
     * Vérifier si l'utilisateur est client
     */
    public boolean estClient() {
        return SessionManager.getInstance().estClient();
    }

    // ========== GESTION DU MAGASIN ==========

    /**
     * Définir le magasin courant pour le vendeur connecté
     */
    public void setCurrentMagasin(int magasinId, String magasinNom) {
        SessionManager.getInstance().setCurrentMagasinId(magasinId);
        SessionManager.getInstance().setCurrentMagasinNom(magasinNom);
        System.out.println("🏪 Magasin défini: " + magasinNom + " (ID: " + magasinId + ")");
    }

    /**
     * Récupérer l'ID du magasin courant
     */
    public Integer getCurrentMagasinId() {
        return SessionManager.getInstance().getCurrentMagasinId();
    }

    /**
     * Récupérer le nom du magasin courant
     */
    public String getCurrentMagasinNom() {
        return SessionManager.getInstance().getCurrentMagasinNom();
    }

    /**
     * Effacer le magasin courant (déconnexion ou changement)
     */
    public void clearCurrentMagasin() {
        SessionManager.getInstance().clearCurrentMagasin();
        System.out.println("🧹 Magasin courant effacé");
    }

    // ========== RÉINITIALISATION DE MOT DE PASSE ==========

    /**
     * Demander une réinitialisation de mot de passe
     */
    public boolean demanderReinitialisationMotDePasse(String email) {
        try {
            Utilisateur utilisateur = utilisateurDAO.findByEmail(email);

            if (utilisateur == null) {
                authLogService.logAccountNotFound(email);
                return false;
            }

            // Log de la demande
            authLogService.logPasswordResetRequest(email);

            // TODO: Envoyer email de réinitialisation
            System.out.println("📧 Email de réinitialisation à envoyer à: " + email);
            return true;

        } catch (Exception e) {
            authLogService.logTechnicalError("PASSWORD_RESET_REQUEST", email, e.getMessage());
            throw e;
        }
    }

    /**
     * Réinitialiser le mot de passe
     */
    public boolean reinitialiserMotDePasse(String email, String nouveauMotDePasse) {
        try {
            // Le DAO va hasher le mot de passe
            boolean success = utilisateurDAO.updatePassword(email, nouveauMotDePasse);

            if (success) {
                authLogService.logPasswordResetSuccess(email);
            } else {
                authLogService.logTechnicalError("PASSWORD_RESET", email, "Échec de mise à jour");
            }

            return success;

        } catch (Exception e) {
            authLogService.logTechnicalError("PASSWORD_RESET", email, e.getMessage());
            throw e;
        }
    }
}