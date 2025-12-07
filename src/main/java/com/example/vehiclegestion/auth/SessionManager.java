package com.example.vehiclegestion.auth;

import com.example.vehiclegestion.auth.model.Utilisateur;

public class SessionManager {
    // Solution Eager Initialization pour éviter les problèmes
    private static final SessionManager INSTANCE = new SessionManager();

    private Utilisateur utilisateurConnecte;
    private boolean sessionActive;
    private String userFullName;


    private SessionManager() {
        System.out.println("✅ SessionManager initialisé: " + this.hashCode());
    }

    public static SessionManager getInstance() {
        return INSTANCE;
    }
    public void demarrerSession(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
        this.sessionActive = true;
        System.out.println("🔐 Session démarrée pour: " + utilisateur.getEmail() +
                " (Téléphone: " + utilisateur.getTelephone() + ")");
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
    public String getUserRole() {
        return utilisateurConnecte != null ? utilisateurConnecte.getRole() : null;
    }
    // ✅ NOUVEAUX champs pour stocker le magasin courant
    private Integer currentMagasinId = null;
    private String currentMagasinNom = null;

    // ... vos méthodes existantes ...

    // ✅ MÉTHODES pour gérer le magasin courant
    public void setCurrentMagasinId(Integer magasinId) {
        this.currentMagasinId = magasinId;
        System.out.println("✅ SessionManager - Magasin ID défini: " + magasinId);
    }

    public Integer getCurrentMagasinId() {
        return currentMagasinId;
    }

    public void setCurrentMagasinNom(String magasinNom) {
        this.currentMagasinNom = magasinNom;
        System.out.println("✅ SessionManager - Magasin Nom défini: " + magasinNom);
    }

    public String getCurrentMagasinNom() {
        return currentMagasinNom;
    }

    public void clearCurrentMagasin() {
        this.currentMagasinId = null;
        this.currentMagasinNom = null;
        System.out.println("✅ SessionManager - Magasin courant effacé");
    }

    public String getUserFullName() {
        return userFullName;
    }
    public String getUserTelephone() {
        if (utilisateurConnecte != null) {
            return utilisateurConnecte.getTelephone();
        }
        return null;
    }
}