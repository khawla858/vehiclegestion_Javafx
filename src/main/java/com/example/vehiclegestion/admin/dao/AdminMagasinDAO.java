package com.example.vehiclegestion.admin.dao;

import com.example.vehiclegestion.vendeur.model.Magasin;
import com.example.vehiclegestion.common.dao.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO pour la gestion administrative des magasins
 */
public class AdminMagasinDAO {

    // ==================== LISTE & RECHERCHE ====================

    /**
     * Récupérer tous les magasin
     */
    public List<Magasin> getAllmagasins() {
        List<Magasin> magasins = new ArrayList<>();
        String sql = "SELECT m.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom, u.email as vendeur_email " +
                "FROM magasin m " +
                "LEFT JOIN utilisateurs u ON m.id_vendeur = u.id_utilisateur " +
                "ORDER BY m.id_magasin DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                magasins.add(mapResultSetToMagasin(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération magasins: " + e.getMessage());
        }

        return magasins;
    }

    /**
     * Rechercher des magasins par critères
     */
    public List<Magasin> searchMagasins(String searchTerm, String categorie) {
        List<Magasin> magasins = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT m.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom, u.email as vendeur_email " +
                        "FROM magasin m " +
                        "LEFT JOIN utilisateurs u ON m.id_vendeur = u.id_utilisateur " +
                        "WHERE 1=1"
        );

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append(" AND (LOWER(m.nom_magasin) LIKE ? OR LOWER(m.adresse) LIKE ? OR LOWER(m.localisation) LIKE ?)");
        }
        if (categorie != null && !categorie.isEmpty()) {
            sql.append(" AND m.categorie = ?");
        }

        sql.append(" ORDER BY m.id_magasin DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String search = "%" + searchTerm.toLowerCase() + "%";
                stmt.setString(paramIndex++, search);
                stmt.setString(paramIndex++, search);
                stmt.setString(paramIndex++, search);
            }
            if (categorie != null && !categorie.isEmpty()) {
                stmt.setString(paramIndex++, categorie);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                magasins.add(mapResultSetToMagasin(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche magasins: " + e.getMessage());
        }

        return magasins;
    }

    /**
     * Récupérer un magasin par ID avec détails complets
     */
    public Magasin getMagasinById(int magasinId) {
        String sql = "SELECT m.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom, u.email as vendeur_email " +
                "FROM magasin m " +
                "LEFT JOIN utilisateurs u ON m.id_vendeur = u.id_utilisateur " +
                "WHERE m.id_magasin = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, magasinId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMagasin(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération magasin: " + e.getMessage());
        }

        return null;
    }

    /**
     * Récupérer les magasins d'un vendeur
     */
    public List<Magasin> getMagasinsByVendeur(int vendeurId) {
        List<Magasin> magasins = new ArrayList<>();
        String sql = "SELECT * FROM magasin WHERE id_vendeur = ? ORDER BY id_magasin DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vendeurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                magasins.add(mapResultSetToMagasin(rs));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }

        return magasins;
    }

    // ==================== MODIFICATION ====================

    /**
     * Mettre à jour un magasin
     */
    public boolean updateMagasin(Magasin magasin) {
        String sql = "UPDATE magasin SET " +
                "nom_magasin = ?, adresse = ?, localisation = ?, description = ?, " +
                "telephone = ?, email_contact = ?, site_web = ?, " +
                "facebook = ?, instagram = ?, categorie = ?, horaires = ? " +
                "WHERE id_magasin = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, magasin.getNomMagasin());
            stmt.setString(2, magasin.getAdresse());
            stmt.setString(3, magasin.getLocalisation());
            stmt.setString(4, magasin.getDescription());
            stmt.setString(5, magasin.getTelephone());
            stmt.setString(6, magasin.getEmailContact());
            stmt.setString(7, magasin.getSiteWeb());
            stmt.setString(8, magasin.getFacebook());
            stmt.setString(9, magasin.getInstagram());
            stmt.setString(10, magasin.getCategorie());
            stmt.setString(11, magasin.getHoraires());
            stmt.setInt(12, magasin.getIdMagasin());

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Magasin mis à jour: " + magasin.getNomMagasin());
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour magasin: " + e.getMessage());
        }

        return false;
    }

    /**
     * Supprimer un magasin (attention: supprime aussi les véhicules associés)
     */
    public boolean deleteMagasin(int magasinId) {
        String sql = "DELETE FROM magasin WHERE id_magasin = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, magasinId);
            int rowsAffected = stmt.executeUpdate();

            System.out.println("✅ Magasin supprimé: ID " + magasinId);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression magasin: " + e.getMessage());
        }

        return false;
    }

    // ==================== STATISTIQUES ====================

    /**
     * Compter le nombre total de magasins
     */
    public int getTotalMagasinsCount() {
        String sql = "SELECT COUNT(*) FROM magasin";
        return getCount(sql);
    }

    /**
     * Compter les magasins par catégorie
     */
    public Map<String, Integer> getMagasinCountByCategorie() {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT categorie, COUNT(*) as count FROM magasin GROUP BY categorie";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String cat = rs.getString("categorie");
                if (cat == null || cat.isEmpty()) {
                    cat = "Non catégorisé";
                }
                counts.put(cat, rs.getInt("count"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur stats catégories: " + e.getMessage());
        }

        return counts;
    }

    /**
     * Récupérer les magasins les plus actifs (par nombre de véhicules)
     */
    public List<Map<String, Object>> getTopMagasinsByVehicules(int limit) {
        List<Map<String, Object>> topMagasins = new ArrayList<>();
        String sql = "SELECT m.id_magasin, m.nom_magasin, COUNT(a.id_article) as nb_vehicules " +
                "FROM magasin m " +
                "LEFT JOIN article a ON m.id_magasin = a.id_magasin " +
                "GROUP BY m.id_magasin, m.nom_magasin " +
                "ORDER BY nb_vehicules DESC " +
                "LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> magasin = new HashMap<>();
                magasin.put("id", rs.getInt("id_magasin"));
                magasin.put("nom", rs.getString("nom_magasin"));
                magasin.put("nbVehicules", rs.getInt("nb_vehicules"));
                topMagasins.add(magasin);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur top magasins: " + e.getMessage());
        }

        return topMagasins;
    }

    /**
     * Compter les véhicules par magasin
     */
    public int getVehiculesCountByMagasin(int magasinId) {
        String sql = "SELECT COUNT(*) FROM vehicules WHERE id_magasin = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, magasinId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage véhicules: " + e.getMessage());
        }

        return 0;
    }

    // ==================== UTILITAIRES ====================

    /**
     * Mapper ResultSet vers Magasin
     */
    private Magasin mapResultSetToMagasin(ResultSet rs) throws SQLException {
        Magasin magasin = new Magasin();
        magasin.setIdMagasin(rs.getInt("id_magasin"));
        magasin.setNomMagasin(rs.getString("nom_magasin"));
        magasin.setAdresse(rs.getString("adresse"));
        magasin.setLocalisation(rs.getString("localisation"));
        magasin.setDescription(rs.getString("description"));
        magasin.setIdVendeur(rs.getInt("id_vendeur"));
        magasin.setTelephone(rs.getString("telephone"));
        magasin.setEmailContact(rs.getString("email_contact"));
        magasin.setSiteWeb(rs.getString("site_web"));
        magasin.setFacebook(rs.getString("facebook"));
        magasin.setInstagram(rs.getString("instagram"));
        magasin.setCategorie(rs.getString("categorie"));
        magasin.setLogoMagasin(rs.getString("logo_magasin"));
        magasin.setHoraires(rs.getString("horaires"));

        // Nombre de ventes mensuelles si disponible
        try {
            magasin.setNbVentesMensuelles(rs.getInt("nb_ventes_mensuelles"));
        } catch (SQLException e) {
            // Colonne non présente dans cette requête
        }

        return magasin;
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
}