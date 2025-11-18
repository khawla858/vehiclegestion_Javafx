package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.common.dao.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class VendeurDAO {

    private final int vendeurId;

    public VendeurDAO(int vendeurId) {
        this.vendeurId = vendeurId;
    }

    public int getTotalSales() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vente WHERE id_vendeur = ? AND statut_vente = 'terminée'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendeurId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant_total), 0) FROM Vente WHERE id_vendeur = ? AND statut_vente = 'terminée'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendeurId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    public int getActiveClientsCount() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT id_client) FROM Vente WHERE id_vendeur = ? AND statut_vente = 'terminée'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendeurId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getPendingOrdersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Vente WHERE id_vendeur = ? AND statut_vente = 'en cours'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendeurId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Map<String, Integer> getSalesPerMonth() throws SQLException {
        Map<String, Integer> salesMap = new LinkedHashMap<>();
        String sql = "SELECT TO_CHAR(date_vente, 'Mon') AS mois, COUNT(*) "
                + "FROM Vente "
                + "WHERE id_vendeur = ? AND statut_vente = 'terminée' "
                + "GROUP BY TO_CHAR(date_vente, 'Mon'), EXTRACT(MONTH FROM date_vente) "
                + "ORDER BY EXTRACT(MONTH FROM date_vente)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendeurId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                salesMap.put(rs.getString("mois"), rs.getInt(2));
            }
        }

        // Remplir les mois manquants avec 0
        String[] allMonths = {"Jan", "Fév", "Mar", "Avr", "Mai", "Jun", "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc"};
        Map<String, Integer> completeMap = new LinkedHashMap<>();
        for (String month : allMonths) {
            completeMap.put(month, salesMap.getOrDefault(month, 0));
        }

        return completeMap;
    }

    public Map<String, Integer> getProductDistribution() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();
        String sql = "SELECT categorie, COUNT(*) as count "
                + "FROM Article "
                + "WHERE id_vendeur = ? AND etat = 'vendu' "
                + "GROUP BY categorie "
                + "ORDER BY count DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendeurId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                data.put(rs.getString("categorie"), rs.getInt("count"));
            }
        }
        return data;
    }

    // 🔹 Nouvelles méthodes supplémentaires
    public int getTotalArticles() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Article WHERE id_vendeur = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendeurId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getAvailableArticles() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Article WHERE id_vendeur = ? AND etat IN ('neuf', 'occasion')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendeurId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double getAverageRating() throws SQLException {
        String sql = "SELECT COALESCE(AVG(note), 0) FROM Commentaire c " +
                "JOIN Article a ON c.id_article = a.id_article " +
                "WHERE a.id_vendeur = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendeurId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        }
    }


    // Dans VendeurDAO.java - Ajoutez ces méthodes :

    public List<Map<String, String>> getRecentActivities() throws SQLException {
        List<Map<String, String>> activities = new ArrayList<>();

        String sql = "SELECT 'VENTE' as type, " +
                "'Vente terminée - ' || a.titre as description, " +
                "v.date_vente as date_activite, " +
                "'COMPLETED' as statut " +
                "FROM Vente v " +
                "JOIN Article a ON v.id_article = a.id_article " +
                "WHERE v.id_vendeur = ? AND v.statut_vente = 'terminée' " +
                "UNION ALL " +
                "SELECT 'COMMENTAIRE' as type, " +
                "'Nouveau commentaire - Note: ' || c.note || '/5' as description, " +
                "c.date_commentaire as date_activite, " +
                "'PENDING' as statut " +
                "FROM Commentaire c " +
                "JOIN Article a ON c.id_article = a.id_article " +
                "WHERE a.id_vendeur = ? " +
                "UNION ALL " +
                "SELECT 'RDV' as type, " +
                "'Rendez-vous client - ' || TO_CHAR(r.date_rdv, 'DD/MM') as description, " +
                "(r.date_rdv + r.heure_rdv) as date_activite, " +
                "CASE WHEN r.statut = 'confirmé' THEN 'PENDING' ELSE 'COMPLETED' END as statut " +
                "FROM RendezVous r " +
                "WHERE r.id_vendeur = ? " +
                "ORDER BY date_activite DESC " +
                "LIMIT 6";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendeurId);
            stmt.setInt(2, vendeurId);
            stmt.setInt(3, vendeurId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, String> activity = new HashMap<>();
                activity.put("type", rs.getString("type"));
                activity.put("description", rs.getString("description"));
                activity.put("date", rs.getTimestamp("date_activite").toString());
                activity.put("status", rs.getString("statut"));
                activities.add(activity);
            }
        }
        return activities;
    }

    public Map<String, String> getSystemStatus() throws SQLException {
        Map<String, String> status = new HashMap<>();

        String sql = "SELECT " +
                "(SELECT COUNT(*) FROM Vente WHERE id_vendeur = ? AND statut_vente = 'en cours') as ventes_en_cours, " +
                "(SELECT COUNT(*) FROM RendezVous WHERE id_vendeur = ? AND statut = 'confirmé') as rdv_confirmes, " +
                "(SELECT COALESCE(AVG(note), 0) FROM Commentaire c " +
                " JOIN Article a ON c.id_article = a.id_article WHERE a.id_vendeur = ?) as note_moyenne";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vendeurId);
            stmt.setInt(2, vendeurId);
            stmt.setInt(3, vendeurId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int ventesEnCours = rs.getInt("ventes_en_cours");
                int rdvConfirmes = rs.getInt("rdv_confirmes");
                double noteMoyenne = rs.getDouble("note_moyenne");

                // Déterminer le statut système
                if (noteMoyenne >= 4.0 && ventesEnCours <= 5) {
                    status.put("statut", "OPTIMUM");
                    status.put("couleur", "#27ae60");
                    status.put("description", "Performances excellentes, continuez comme ça !");
                } else if (noteMoyenne >= 3.0) {
                    status.put("statut", "BON");
                    status.put("couleur", "#f39c12");
                    status.put("description", "Bonnes performances, quelques améliorations possibles.");
                } else {
                    status.put("statut", "ATTENTION");
                    status.put("couleur", "#e74c3c");
                    status.put("description", "Attention nécessaire sur la satisfaction client.");
                }

                status.put("ventes_en_cours", String.valueOf(ventesEnCours));
                status.put("rdv_confirmes", String.valueOf(rdvConfirmes));
                status.put("note_moyenne", String.format("%.1f", noteMoyenne));
            }
        }
        return status;
    }
}