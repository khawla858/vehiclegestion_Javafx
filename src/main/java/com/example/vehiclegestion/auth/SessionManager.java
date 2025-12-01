package com.example.vehiclegestion.auth;

import com.example.vehiclegestion.auth.model.Utilisateur;

public class SessionManager {
    // Solution Eager Initialization pour éviter les problèmes
    private static final SessionManager INSTANCE = new SessionManager();

    private Utilisateur utilisateurConnecte;
    private boolean sessionActive;

    private SessionManager() {
        System.out.println("✅ SessionManager initialisé: " + this.hashCode());
    }

    public String getUserRole() {
        return utilisateurConnecte != null ? utilisateurConnecte.getRole() : null;
    }
    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void demarrerSession(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        this.sessionActive = true;
        System.out.println("🔐 Session démarrée pour: " + utilisateur.getEmail());
        debugSession();
    }

    public void fermerSession() {
        if (utilisateurConnecte != null) {
            System.out.println("🔒 Session fermée pour: " + utilisateurConnecte.getEmail());
        }
        this.utilisateurConnecte = null;
        this.sessionActive = false;
        debugSession();
    }

    public boolean estConnecte() {
        return sessionActive && utilisateurConnecte != null;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public int getUserId() {
        return utilisateurConnecte != null ? utilisateurConnecte.getIdUtilisateur() : -1;
    }

    public boolean estClient() {
        return estConnecte() && "client".equals(utilisateurConnecte.getRole());
    }

    public boolean estVendeur() {
        return estConnecte() && "vendeur".equals(utilisateurConnecte.getRole());
    }

    public boolean estAdmin() {
        return estConnecte() && "admin".equals(utilisateurConnecte.getRole());
    }

    // Méthode de debug
    public void debugSession() {
        System.out.println("=== DEBUG SESSION ===");
        System.out.println("Instance: " + this.hashCode());
        System.out.println("Session active: " + sessionActive);
        System.out.println("Utilisateur: " + (utilisateurConnecte != null ?
                utilisateurConnecte.getEmail() + " (" + utilisateurConnecte.getRole() + ")" : "null"));
        System.out.println("=====================");
    }
}