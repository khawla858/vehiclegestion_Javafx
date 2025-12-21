package com.example.vehiclegestion.admin.service;

import com.example.vehiclegestion.admin.dao.AdminUserDAO;
import com.example.vehiclegestion.admin.dao.AdminMagasinDAO;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.vendeur.model.Magasin;
import com.example.vehiclegestion.auth.service.AuthLogService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service principal pour toutes les opérations d'administration
 */
public class AdminService {

    private final AdminUserDAO userDAO;
    private final AdminMagasinDAO magasinDAO;
    private final AuthLogService authLogService;

    public AdminService() {
        this.userDAO = new AdminUserDAO();
        this.magasinDAO = new AdminMagasinDAO();
        this.authLogService = new AuthLogService();
        // TEST DE CONNEXION
        userDAO.testConnection();
    }

    // ==================== GESTION UTILISATEURS ====================

    /**
     * Récupérer tous les utilisateurs
     */
    public List<Utilisateur> getAllUsers() {
        return userDAO.getAllUsers();
    }

    /**
     * Récupérer un utilisateur par ID
     */
    public Utilisateur getUserById(int userId) {
        return userDAO.getUserById(userId);
    }

    /**
     * Rechercher des utilisateurs
     */
    public List<Utilisateur> searchUsers(String searchTerm, String role, String statut) {
        return userDAO.searchUsers(searchTerm, role, statut);
    }

    /**
     * Créer un nouvel utilisateur
     */
    public boolean createUser(Utilisateur user, String adminEmail) {
        try {
            // Validation
            if (!validateUserData(user)) {
                return false;
            }

            // Vérifier si l'email existe déjà
            List<Utilisateur> existing = userDAO.searchUsers(user.getEmail(), null, null);
            if (!existing.isEmpty()) {
                System.out.println("❌ Email déjà utilisé: " + user.getEmail());
                return false;
            }

            // TODO: Hasher le mot de passe avant insertion
            // user.setMotDePasse(PasswordUtil.hash(user.getMotDePasse()));

            boolean success = userDAO.createUser(user);

            if (success) {
                authLogService.logAdminAction(adminEmail, "CREATE_USER",
                        "Création utilisateur: " + user.getEmail() + " (" + user.getRole() + ")");
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ Erreur création utilisateur: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mettre à jour un utilisateur
     */
    public boolean updateUser(Utilisateur user, String adminEmail) {
        try {
            if (!validateUserData(user)) {
                return false;
            }

            boolean success = userDAO.updateUser(user);

            if (success) {
                authLogService.logAdminAction(adminEmail, "UPDATE_USER",
                        "Modification utilisateur: " + user.getEmail());
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprimer un utilisateur
     */
    public boolean deleteUser(int userId, String adminEmail) {
        try {
            Utilisateur user = userDAO.getUserById(userId);
            if (user == null) {
                return false;
            }

            boolean success = userDAO.deleteUser(userId);

            if (success) {
                authLogService.logAdminAction(adminEmail, "DELETE_USER",
                        "Suppression utilisateur: " + user.getEmail());
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ Erreur suppression: " + e.getMessage());
            return false;
        }
    }

    /**
     * Activer un utilisateur
     */
    public boolean activateUser(int userId, String adminEmail) {
        try {
            boolean success = userDAO.activateUser(userId);

            if (success) {
                Utilisateur user = userDAO.getUserById(userId);
                authLogService.logAdminAction(adminEmail, "ACTIVATE_USER",
                        "Activation utilisateur: " + user.getEmail());
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ Erreur activation: " + e.getMessage());
            return false;
        }
    }

    /**
     * Désactiver un utilisateur
     */
    public boolean deactivateUser(int userId, String adminEmail) {
        try {
            boolean success = userDAO.deactivateUser(userId);

            if (success) {
                Utilisateur user = userDAO.getUserById(userId);
                authLogService.logAdminAction(adminEmail, "DEACTIVATE_USER",
                        "Désactivation utilisateur: " + user.getEmail());
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ Erreur désactivation: " + e.getMessage());
            return false;
        }
    }

    // ==================== GESTION MAGASINS ====================

    /**
     * Récupérer tous les magasins
     */
    public List<Magasin> getAllMagasins() {
        return magasinDAO.getAllmagasins();
    }

    /**
     * Récupérer un magasin par ID
     */
    public Magasin getMagasinById(int magasinId) {
        return magasinDAO.getMagasinById(magasinId);
    }

    /**
     * Rechercher des magasins
     */
    public List<Magasin> searchMagasins(String searchTerm, String categorie) {
        return magasinDAO.searchMagasins(searchTerm, categorie);
    }

    /**
     * Mettre à jour un magasin
     */
    public boolean updateMagasin(Magasin magasin, String adminEmail) {
        try {
            boolean success = magasinDAO.updateMagasin(magasin);

            if (success) {
                authLogService.logAdminAction(adminEmail, "UPDATE_MAGASIN",
                        "Modification magasin: " + magasin.getNomMagasin());
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ Erreur mise à jour magasin: " + e.getMessage());
            return false;
        }
    }

    /**
     * Supprimer un magasin
     */
    public boolean deleteMagasin(int magasinId, String adminEmail) {
        try {
            Magasin magasin = magasinDAO.getMagasinById(magasinId);
            if (magasin == null) {
                return false;
            }

            boolean success = magasinDAO.deleteMagasin(magasinId);

            if (success) {
                authLogService.logAdminAction(adminEmail, "DELETE_MAGASIN",
                        "Suppression magasin: " + magasin.getNomMagasin());
            }

            return success;

        } catch (Exception e) {
            System.err.println("❌ Erreur suppression magasin: " + e.getMessage());
            return false;
        }
    }

    /**
     * Récupérer les magasins d'un vendeur
     */
    public List<Magasin> getMagasinsByVendeur(int vendeurId) {
        return magasinDAO.getMagasinsByVendeur(vendeurId);
    }

    // ==================== STATISTIQUES DASHBOARD ====================

    /**
     * Récupérer toutes les statistiques pour le dashboard admin
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Statistiques utilisateurs
            stats.put("totalUsers", userDAO.getTotalUsersCount());
            stats.put("usersByRole", userDAO.getUserCountByRole());
            stats.put("usersByStatus", userDAO.getUserCountByStatus());
            stats.put("newUsersLast30Days", userDAO.getNewUsersCount(30));

            // Statistiques magasins
            stats.put("totalMagasins", magasinDAO.getTotalMagasinsCount());
            stats.put("magasinsByCategorie", magasinDAO.getMagasinCountByCategorie());
            stats.put("topMagasins", magasinDAO.getTopMagasinsByVehicules(5));

            System.out.println("✅ Statistiques dashboard récupérées");

        } catch (Exception e) {
            System.err.println("❌ Erreur récupération stats: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Récupérer les statistiques utilisateurs
     */
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", userDAO.getTotalUsersCount());
        stats.put("byRole", userDAO.getUserCountByRole());
        stats.put("byStatus", userDAO.getUserCountByStatus());
        stats.put("newLast30Days", userDAO.getNewUsersCount(30));
        stats.put("newLast7Days", userDAO.getNewUsersCount(7));

        return stats;
    }

    /**
     * Récupérer les statistiques magasins
     */
    public Map<String, Object> getMagasinStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", magasinDAO.getTotalMagasinsCount());
        stats.put("byCategorie", magasinDAO.getMagasinCountByCategorie());
        stats.put("topByVehicules", magasinDAO.getTopMagasinsByVehicules(10));

        return stats;
    }

    // ==================== VALIDATION ====================

    /**
     * Valider les données d'un utilisateur
     */
    private boolean validateUserData(Utilisateur user) {
        if (user.getNom() == null || user.getNom().trim().isEmpty()) {
            System.out.println("❌ Nom requis");
            return false;
        }

        if (user.getPrenom() == null || user.getPrenom().trim().isEmpty()) {
            System.out.println("❌ Prénom requis");
            return false;
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            System.out.println("❌ Email requis");
            return false;
        }

        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            System.out.println("❌ Email invalide");
            return false;
        }

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            System.out.println("❌ Rôle requis");
            return false;
        }

        List<String> rolesValides = List.of("admin", "vendeur", "client");
        if (!rolesValides.contains(user.getRole().toLowerCase())) {
            System.out.println("❌ Rôle invalide: " + user.getRole());
            return false;
        }

        return true;
    }
}