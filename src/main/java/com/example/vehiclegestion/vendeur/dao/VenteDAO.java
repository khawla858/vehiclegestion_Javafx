package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.vendeur.model.Vente;
import com.example.vehiclegestion.common.dao.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class VenteDAO {

    // -----------------------------------------------------------
    // 1️⃣ Récupération des ventes d’un vendeur
    // -----------------------------------------------------------
    public List<Vente> getVentesByVendeur(int idVendeur) {
        List<Vente> ventes = new ArrayList<>();

        String sql =
                "SELECT v.id_vente, v.id_client, v.id_vendeur, v.id_article, v.date_vente, " +
                        "v.montant_total, v.moyen_paiement, v.statut_vente, " +
                        "u.nom AS nom_client, u.prenom AS prenom_client, u.email AS email_client, " +
                        "a.titre AS nom_article, COALESCE(m.nom_magasin, 'Magasin non défini') AS nom_magasin, " +
                        "(SELECT COUNT(*) FROM Vente WHERE id_client = v.id_client) AS nb_ventes_client " +
                        "FROM Vente v " +
                        "JOIN Client c ON v.id_client = c.id_client " +
                        "JOIN Utilisateur u ON c.id_client = u.id_utilisateur " +
                        "JOIN Article a ON v.id_article = a.id_article " +
                        "LEFT JOIN Magasin m ON a.id_magasin = m.id_magasin " +
                        "WHERE v.id_vendeur = ? " +
                        "ORDER BY v.date_vente DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVendeur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ventes.add(mapResultSetToVente(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ventes;
    }

    // -----------------------------------------------------------
    // 2️⃣ Récupération avec filtres avancés
    // -----------------------------------------------------------
    public List<Vente> getVentesWithFilters(int idVendeur, String statut, String magasin,
                                            String periode, String searchTerm) {

        List<Vente> ventes = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT v.id_vente, v.id_client, v.id_vendeur, v.id_article, v.date_vente, " +
                        "v.montant_total, v.moyen_paiement, v.statut_vente, " +
                        "u.nom AS nom_client, u.prenom AS prenom_client, u.email AS email_client, " +
                        "a.titre AS nom_article, COALESCE(m.nom_magasin, 'Magasin non défini') AS nom_magasin, " +
                        "(SELECT COUNT(*) FROM Vente WHERE id_client = v.id_client) AS nb_ventes_client " +
                        "FROM Vente v " +
                        "JOIN Client c ON v.id_client = c.id_client " +
                        "JOIN Utilisateur u ON c.id_client = u.id_utilisateur " +
                        "JOIN Article a ON v.id_article = a.id_article " +
                        "JOIN Magasin m ON a.id_magasin = m.id_magasin " +
                        "WHERE v.id_vendeur = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(idVendeur);

        if (statut != null && !statut.equals("Tous les statuts")) {
            sql.append("AND v.statut_vente = ? ");
            params.add(statut);
        }

        if (magasin != null && !magasin.equals("Tous les magasins")) {
            sql.append("AND m.nom_magasin = ? ");
            params.add(magasin);
        }

        if (periode != null) {
            switch (periode) {
                case "Aujourd'hui" ->
                        sql.append("AND DATE(v.date_vente) = CURRENT_DATE ");
                case "Cette semaine" ->
                        sql.append("AND v.date_vente >= CURRENT_DATE - INTERVAL '7 days' ");
                case "Ce mois" ->
                        sql.append("AND EXTRACT(MONTH FROM v.date_vente) = EXTRACT(MONTH FROM CURRENT_DATE) ")
                                .append("AND EXTRACT(YEAR FROM v.date_vente) = EXTRACT(YEAR FROM CURRENT_DATE) ");
                case "Cette année" ->
                        sql.append("AND EXTRACT(YEAR FROM v.date_vente) = EXTRACT(YEAR FROM CURRENT_DATE) ");
            }
        }

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append("AND (LOWER(u.nom) LIKE ? OR LOWER(u.prenom) LIKE ? OR LOWER(a.titre) LIKE ?) ");
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append("ORDER BY v.date_vente DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) ventes.add(mapResultSetToVente(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ventes;
    }

    // -----------------------------------------------------------
    // 3️⃣ Statistiques générales
    // -----------------------------------------------------------
    public Map<String, Object> getStatistiques(int idVendeur) {
        Map<String, Object> stats = new HashMap<>();

        String sql =
                "SELECT COUNT(*) AS total_ventes, " +
                        "COALESCE(SUM(montant_total), 0) AS chiffre_affaires, " +
                        "COUNT(CASE WHEN statut_vente = 'en cours' THEN 1 END) AS ventes_en_cours, " +
                        "COUNT(CASE WHEN statut_vente = 'terminée' THEN 1 END) AS ventes_terminees " +
                        "FROM Vente WHERE id_vendeur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVendeur);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                stats.put("total_ventes", rs.getInt("total_ventes"));
                stats.put("chiffre_affaires", rs.getDouble("chiffre_affaires"));
                stats.put("ventes_en_cours", rs.getInt("ventes_en_cours"));
                stats.put("ventes_terminees", rs.getInt("ventes_terminees"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    // -----------------------------------------------------------
    // 4️⃣ Magasins d’un vendeur
    // -----------------------------------------------------------
    public List<String> getMagasinsByVendeur(int idVendeur) {
        List<String> magasins = new ArrayList<>();

        String sql =
                "SELECT DISTINCT nom_magasin FROM Magasin " +
                        "WHERE id_vendeur = ? ORDER BY nom_magasin";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVendeur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) magasins.add(rs.getString("nom_magasin"));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return magasins;
    }

    // -----------------------------------------------------------
    // 5️⃣ Top 5 articles vendus
    // -----------------------------------------------------------
    public List<Map<String, Object>> getTopArticles(int idVendeur) {
        List<Map<String, Object>> result = new ArrayList<>();

        String sql =
                "SELECT a.titre, COUNT(*) AS nb_ventes, SUM(v.montant_total) AS total_ca " +
                        "FROM Vente v " +
                        "JOIN Article a ON v.id_article = a.id_article " +
                        "WHERE v.id_vendeur = ? " +
                        "GROUP BY a.titre " +
                        "ORDER BY nb_ventes DESC " +
                        "LIMIT 5";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVendeur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("titre", rs.getString("titre"));
                map.put("nb_ventes", rs.getInt("nb_ventes"));
                map.put("total_ca", rs.getDouble("total_ca"));
                result.add(map);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    // -----------------------------------------------------------
    // 6️⃣ Dernières ventes
    // -----------------------------------------------------------
    public List<Vente> getRecentVentes(int idVendeur, int limit) {
        List<Vente> ventes = new ArrayList<>();

        String sql =
                "SELECT v.id_vente, v.id_client, v.id_vendeur, v.id_article, v.date_vente, " +
                        "v.montant_total, v.moyen_paiement, v.statut_vente, " +
                        "u.nom AS nom_client, u.prenom AS prenom_client, u.email AS email_client, " +
                        "a.titre AS nom_article, COALESCE(m.nom_magasin, 'Magasin non défini') AS nom_magasin, " +
                        "(SELECT COUNT(*) FROM Vente WHERE id_client = v.id_client) AS nb_ventes_client " +
                        "FROM Vente v " +
                        "JOIN Client c ON v.id_client = c.id_client " +
                        "JOIN Utilisateur u ON c.id_client = u.id_utilisateur " +
                        "JOIN Article a ON v.id_article = a.id_article " +
                        "JOIN Magasin m ON a.id_magasin = m.id_magasin " +
                        "WHERE v.id_vendeur = ? " +
                        "ORDER BY v.date_vente DESC " +
                        "LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVendeur);
            stmt.setInt(2, limit);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) ventes.add(mapResultSetToVente(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ventes;
    }

    // -----------------------------------------------------------
    // 7️⃣ Mise à jour statut
    // -----------------------------------------------------------
    public boolean creerVente(Vente vente) throws SQLException {
        String sql = "INSERT INTO Vente (id_client, id_vendeur, id_article, date_vente, montant_total, moyen_paiement, statut_vente) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Transaction

            // 1️⃣ Insérer la vente
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, vente.getIdClient());
            stmt.setInt(2, vente.getIdVendeur());
            stmt.setInt(3, vente.getIdArticle());

            // Convertir LocalDate en Timestamp pour date_vente
            if (vente.getDateVente() != null) {
                stmt.setTimestamp(4, Timestamp.valueOf(vente.getDateVente()));
            } else {
                stmt.setTimestamp(4, Timestamp.valueOf(java.time.LocalDateTime.now()));
            }

            stmt.setDouble(5, vente.getMontantTotal());
            stmt.setString(6, vente.getMoyenPaiement());
            stmt.setString(7, vente.getStatutVente());

            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted == 0) {
                conn.rollback();
                return false;
            }

            // 2️⃣ Récupérer l'ID généré
            generatedKeys = stmt.getGeneratedKeys();
            int idVenteGeneree = 0;
            if (generatedKeys.next()) {
                idVenteGeneree = generatedKeys.getInt(1);
                vente.setIdVente(idVenteGeneree);
            }

            // 3️⃣ Mettre à jour le statut de l'article selon le statut de la vente
            String statutArticle = "disponible";
            if ("terminée".equalsIgnoreCase(vente.getStatutVente()) ||
                    "terminee".equalsIgnoreCase(vente.getStatutVente())) {
                statutArticle = "vendu";
            } else if ("en cours".equalsIgnoreCase(vente.getStatutVente())) {
                statutArticle = "reserve";
            }

            String sqlUpdateArticle = "UPDATE Article SET statut_vehicule = ? WHERE id_article = ?";
            try (PreparedStatement stmtArticle = conn.prepareStatement(sqlUpdateArticle)) {
                stmtArticle.setString(1, statutArticle);
                stmtArticle.setInt(2, vente.getIdArticle());
                stmtArticle.executeUpdate();
            }

            // 4️⃣ Vérifier si le client existe dans la table Client
            String checkClient = "SELECT COUNT(*) FROM Client WHERE id_client = ?";
            try (PreparedStatement stmtCheck = conn.prepareStatement(checkClient)) {
                stmtCheck.setInt(1, vente.getIdClient());
                ResultSet rs = stmtCheck.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    // Le client n'existe pas, l'insérer
                    String insertClient = "INSERT INTO Client (id_client, statut_client) VALUES (?, 'actif')";
                    try (PreparedStatement stmtInsert = conn.prepareStatement(insertClient)) {
                        stmtInsert.setInt(1, vente.getIdClient());
                        stmtInsert.executeUpdate();
                    }
                }
            }

            conn.commit(); // Valider la transaction

            System.out.println("✅ Vente créée avec succès ! ID: " + idVenteGeneree);
            System.out.println("   - Client: " + vente.getIdClient());
            System.out.println("   - Article: " + vente.getIdArticle());
            System.out.println("   - Montant: " + vente.getMontantTotal());
            System.out.println("   - Statut article: " + statutArticle);

            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création de la vente: " + e.getMessage());
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;

        } finally {
            if (generatedKeys != null) generatedKeys.close();
            if (stmt != null) stmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }
    // -----------------------------------------------------------
// 7️⃣ Mise à jour statut avec gestion automatique de l'article
// -----------------------------------------------------------
    public boolean updateStatutVente(int idVente, String statut) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // ✅ Transaction

            // 1️⃣ Mettre à jour le statut de la vente
            String sqlVente = "UPDATE Vente SET statut_vente = ? WHERE id_vente = ?";
            try (PreparedStatement stmtVente = conn.prepareStatement(sqlVente)) {
                stmtVente.setString(1, statut);
                stmtVente.setInt(2, idVente);

                int rowsUpdated = stmtVente.executeUpdate();
                if (rowsUpdated == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 2️⃣ Récupérer l'ID de l'article concerné
            String sqlGetArticle = "SELECT id_article FROM Vente WHERE id_vente = ?";
            int idArticle = 0;
            try (PreparedStatement stmtGet = conn.prepareStatement(sqlGetArticle)) {
                stmtGet.setInt(1, idVente);
                try (ResultSet rs = stmtGet.executeQuery()) {
                    if (rs.next()) {
                        idArticle = rs.getInt("id_article");
                    }
                }
            }

            // 3️⃣ Mettre à jour SEULEMENT statut_vehicule
            if (idArticle > 0) {
                String sqlArticle = "UPDATE Article SET statut_vehicule = ? WHERE id_article = ?";
                try (PreparedStatement stmtArticle = conn.prepareStatement(sqlArticle)) {

                    if (statut.equalsIgnoreCase("terminée") || statut.equalsIgnoreCase("terminee")) {
                        // ✅ Vente terminée → Article VENDU
                        stmtArticle.setString(1, "vendu");
                        System.out.println("✅ Article #" + idArticle + " marqué comme VENDU");

                    } else if (statut.equalsIgnoreCase("annulée") || statut.equalsIgnoreCase("annulee")) {
                        // 🔄 Vente annulée → Article DISPONIBLE
                        stmtArticle.setString(1, "disponible");
                        System.out.println("🔄 Article #" + idArticle + " remis DISPONIBLE");

                    } else if (statut.equalsIgnoreCase("en cours")) {
                        // ⏳ Vente en cours → Article RÉSERVÉ
                        stmtArticle.setString(1, "reserve");
                        System.out.println("⏳ Article #" + idArticle + " mis en RÉSERVÉ");
                    }

                    stmtArticle.setInt(2, idArticle);
                    stmtArticle.executeUpdate();
                }
            }

            conn.commit(); // Valider transaction
            System.out.println("✅ Statut vente #" + idVente + " mis à jour : " + statut);
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut: " + e.getMessage());
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // -----------------------------------------------------------
    // 8️⃣ Supprimer une vente
    // -----------------------------------------------------------
    public boolean deleteVente(int idVente) {
        String sql = "DELETE FROM Vente WHERE id_vente = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idVente);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -----------------------------------------------------------
    // 🔁 Mapper ResultSet → Objet Vente
    // -----------------------------------------------------------
    private Vente mapResultSetToVente(ResultSet rs) throws SQLException {
        Vente v = new Vente();

        v.setIdVente(rs.getInt("id_vente"));
        v.setIdClient(rs.getInt("id_client"));
        v.setIdVendeur(rs.getInt("id_vendeur"));
        v.setIdArticle(rs.getInt("id_article"));
        v.setMontantTotal(rs.getDouble("montant_total"));
        v.setMoyenPaiement(rs.getString("moyen_paiement"));
        v.setStatutVente(rs.getString("statut_vente"));
        v.setNomClient(rs.getString("nom_client"));
        v.setPrenomClient(rs.getString("prenom_client"));
        v.setEmailClient(rs.getString("email_client"));
        v.setNomArticle(rs.getString("nom_article"));
        v.setNomMagasin(rs.getString("nom_magasin"));
        v.setNbVentesClient(rs.getInt("nb_ventes_client"));

        Timestamp ts = rs.getTimestamp("date_vente");
        if (ts != null) v.setDateVente(ts.toLocalDateTime());

        return v;
    }

}