package com.example.vehiclegestion.admin.dao;

import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.common.dao.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO pour la gestion administrative des utilisateurs
 */
public class AdminUserDAO {

    // ==================== LISTE & RECHERCHE ====================

    /**
     * Récupérer tous les utilisateurs avec pagination
     */
    public List<Utilisateur> getAllUsers(int page, int pageSize) {
        List<Utilisateur> users = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY date_creation DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des utilisateur: " + e.getMessage());
            e.printStackTrace();
        }

        return users;
    }

    /**
     * Récupérer tous les utilisateurs sans pagination
     */
    public List<Utilisateur> getAllUsers() {
        List<Utilisateur> users = new ArrayList<>();
        String sql = "SELECT * FROM utilisateur ORDER BY date_creation DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }

        return users;
    }

    /**
     * Rechercher des utilisateurs par critères
     */
    public List<Utilisateur> searchUsers(String searchTerm, String role, String statut) {
        List<Utilisateur> users = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM utilisateur WHERE 1=1");

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(email) LIKE ?)");
        }
        if (role != null && !role.isEmpty()) {
            sql.append(" AND role = ?");
        }
        if (statut != null && !statut.isEmpty()) {
            sql.append(" AND statut = ?");
        }

        sql.append(" ORDER BY date_creation DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String search = "%" + searchTerm.toLowerCase() + "%";
                stmt.setString(paramIndex++, search);
                stmt.setString(paramIndex++, search);
                stmt.setString(paramIndex++, search);
            }
            if (role != null && !role.isEmpty()) {
                stmt.setString(paramIndex++, role);
            }
            if (statut != null && !statut.isEmpty()) {
                stmt.setString(paramIndex++, statut);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche: " + e.getMessage());
        }

        return users;
    }

    /**
     * Récupérer un utilisateur par ID
     */
    public Utilisateur getUserById(int userId) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }

        return null;
    }

    // ==================== CRÉATION & MODIFICATION ====================

    /**
     * Créer un nouvel utilisateur (par admin)
     */
    public boolean createUser(Utilisateur user) {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role, statut, date_creation) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getMotDePasse()); // Doit être hashé avant
            stmt.setString(5, user.getRole());
            stmt.setString(6, user.getStatut());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    user.setIdUtilisateur(generatedKeys.getInt(1));
                }
                System.out.println("✅ Utilisateur créé avec succès: " + user.getEmail());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur création utilisateur: " + e.getMessage());
        }

        return false;
    }

    /**
     * Mettre à jour un utilisateur
     */
    public boolean updateUser(Utilisateur user) {
        String sql = "UPDATE utilisateur SET nom = ?, prenom = ?, email = ?, role = ?, statut = ? " +
                "WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getStatut());
            stmt.setInt(6, user.getIdUtilisateur());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Utilisateur mis à jour: " + user.getEmail());
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour: " + e.getMessage());
        }

        return false;
    }

    /**
     * Supprimer un utilisateur
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM utilisateur WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();

            System.out.println("✅ Utilisateur supprimé: ID " + userId);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression: " + e.getMessage());
        }

        return false;
    }

    // ==================== ACTIVATION / DÉSACTIVATION ====================

    /**
     * Changer le statut d'un utilisateur
     */
    public boolean changeUserStatus(int userId, String newStatus) {
        String sql = "UPDATE utilisateur SET statut = ? WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Statut changé pour user ID " + userId + " -> " + newStatus);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur changement statut: " + e.getMessage());
        }

        return false;
    }

    /**
     * Activer un utilisateur
     */
    public boolean activateUser(int userId) {
        return changeUserStatus(userId, "actif");
    }

    /**
     * Désactiver un utilisateur
     */
    public boolean deactivateUser(int userId) {
        return changeUserStatus(userId, "inactif");
    }

    // ==================== STATISTIQUES ====================

    /**
     * Compter le nombre total d'utilisateurs
     */
    public int getTotalUsersCount() {
        String sql = "SELECT COUNT(*) FROM utilisateur";
        return getCount(sql);
    }

    /**
     * Compter les utilisateurs par rôle
     */
    public Map<String, Integer> getUserCountByRole() {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT role, COUNT(*) as count FROM utilisateur GROUP BY role";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString("role"), rs.getInt("count"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur stats rôles: " + e.getMessage());
        }

        return counts;
    }

    /**
     * Compter les utilisateurs par statut
     */
    public Map<String, Integer> getUserCountByStatus() {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT statut, COUNT(*) as count FROM utilisateur GROUP BY statut";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString("statut"), rs.getInt("count"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur stats statuts: " + e.getMessage());
        }

        return counts;
    }

    /**
     * Compter les nouveaux utilisateurs (derniers 30 jours)
     */
    public int getNewUsersCount(int days) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE date_creation >= NOW() - INTERVAL '" + days + " days'";
        return getCount(sql);
    }

    // ==================== UTILITAIRES ====================

    /**
     * Mapper ResultSet vers Utilisateur
     */
    private Utilisateur mapResultSetToUser(ResultSet rs) throws SQLException {
        Utilisateur user = new Utilisateur();
        user.setIdUtilisateur(rs.getInt("id_utilisateur"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setMotDePasse(rs.getString("mot_de_passe"));
        user.setRole(rs.getString("role"));
        user.setStatut(rs.getString("statut"));

        Timestamp timestamp = rs.getTimestamp("date_creation");
        if (timestamp != null) {
            user.setDateCreation(timestamp.toLocalDateTime());
        }

        return user;
    }

    /**
     * Méthode helper pour les comptages
     */
    private int getCount(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage: " + e.getMessage());
        }

        return 0;
    }
    public void testConnection() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✅ Connexion BD OK");

            String sql = "SELECT COUNT(*) FROM utilisateur";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("✅ utilisateur dans la BD: " + count);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion BD: " + e.getMessage());
            e.printStackTrace();
        }
    }
}