
package com.example.vehiclegestion.auth;

import com.example.vehiclegestion.auth.model.Utilisateur;

public class SessionManagerr {
    private static SessionManagerr instance;
    private Utilisateur utilisateurConnecte;
    private boolean sessionActive;

    private SessionManagerr() {
        // Constructeur privé pour le singleton
    }

    public static SessionManagerr getInstance() {
        if (instance == null) {
            instance = new SessionManagerr();
        }
        return instance;
    }

    /**
     * Démarrer une session utilisateur
     */
    public void demarrerSession(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        this.sessionActive = true;
        System.out.println("🔐 Session démarrée pour: " + utilisateur.getEmail() + " (ID: " + utilisateur.getIdUtilisateur() + ")");
    }

    /**
     * Fermer la session
     */
    public void fermerSession() {
        if (utilisateurConnecte != null) {
            System.out.println("🔒 Session fermée pour: " + utilisateurConnecte.getEmail());
        }
        this.utilisateurConnecte = null;
        this.sessionActive = false;
    }

    /**
     * Vérifier si une session est active
     */
    public boolean estConnecte() {
        return sessionActive && utilisateurConnecte != null;
    }

    /**
     * Obtenir l'utilisateur connecté
     */
    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    /**
     * Obtenir l'ID de l'utilisateur connecté
     */
    public int getUserId() {
        return utilisateurConnecte != null ? utilisateurConnecte.getIdUtilisateur() : -1;
    }

    /**
     * Vérifier le rôle de l'utilisateur connecté
     */
    public boolean estClient() {
        return estConnecte() && "client".equals(utilisateurConnecte.getRole());
    }

    public boolean estVendeur() {
        return estConnecte() && "vendeur".equals(utilisateurConnecte.getRole());
    }

    public boolean estAdmin() {
        return estConnecte() && "admin".equals(utilisateurConnecte.getRole());
    }

    /**
     * Méthode manquante - utilisée dans votre LoginController
     * Remplacer getUtilisateurActuel() par getUtilisateurConnecte()
     */
    public Utilisateur getUtilisateurActuel() {
        return getUtilisateurConnecte();
    }
}