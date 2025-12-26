package com.example.vehiclegestion.client.doa;

import com.example.vehiclegestion.common.dao.DatabaseConnection;
import com.example.vehiclegestion.client.model.HistoriqueItem;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class HistoriqueClientDAO {

    private static final String BASE_QUERY = """
        SELECT 
            hc.id_historique,
            hc.id_client,
            hc.type_action,
            hc.date_action,
            hc.details,
            hc.id_article,
            hc.id_vendeur,
            a.titre as article_titre,
            a.marque as article_marque,
            a.modele as article_modele,
            a.prix as article_prix,
            vend.nom_magasin as vendeur_nom,  -- CORRECTION ICI: vend au lieu de v
            u.prenom as vendeur_prenom,
            u.nom as vendeur_nom_complet,
            rdv.statut as rdv_statut,
            rdv.date_rdv,
            rdv.heure_rdv,
            rdv.description as rdv_description,
            vent.montant_total,
            vent.statut_vente,
            vent.date_vente,
            com.note,
            com.texte_commentaire,
            com.date_commentaire
        FROM HistoriqueClient hc
        LEFT JOIN Article a ON hc.id_article = a.id_article
        LEFT JOIN Vendeur vend ON hc.id_vendeur = vend.id_vendeur
        LEFT JOIN Utilisateur u ON vend.id_vendeur = u.id_utilisateur
        LEFT JOIN RendezVous rdv ON hc.id_rendez_vous = rdv.id_rdv
        LEFT JOIN Vente vent ON hc.id_vente = vent.id_vente
        LEFT JOIN Commentaire com ON hc.id_commentaire = com.id_commentaire
        WHERE hc.id_client = ?
        """;

    public List<HistoriqueItem> getHistoriqueComplet(int idClient, String periode, String typeFiltre) {
        List<HistoriqueItem> historique = new ArrayList<>();

        StringBuilder sql = new StringBuilder(BASE_QUERY);
        List<Object> params = new ArrayList<>();
        params.add(idClient);

        // Filtre par type
        if (typeFiltre != null && !"Tous".equals(typeFiltre)) {
            sql.append(" AND hc.type_action = ?");
            params.add(typeFiltre);
        }

        // Filtre par période
        if (periode != null && !"Toutes périodes".equals(periode)) {
            sql.append(" AND hc.date_action >= ?");
            params.add(java.sql.Date.valueOf(getDateDebut(periode)));
        }

        sql.append(" ORDER BY hc.date_action DESC");

        System.out.println("📊 SQL Historique: " + sql.toString());
        System.out.println("📊 Params: " + params);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("✅ Requête exécutée avec succès");
                int count = 0;
                while (rs.next()) {
                    count++;
                    historique.add(mapResultSetToHistoriqueItem(rs));
                }
                System.out.println("📋 " + count + " résultats trouvés");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur DAO HistoriqueComplet: " + e.getMessage());
            System.err.println("SQL: " + sql.toString());
            e.printStackTrace();
        }

        System.out.println("📊 Résultats retournés: " + historique.size());
        return historique;
    }

    public Map<String, Object> getStatistiques(int idClient) {
        Map<String, Object> stats = new HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total actions depuis HistoriqueClient
            String sqlTotal = "SELECT COUNT(*) as total FROM HistoriqueClient WHERE id_client = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlTotal)) {
                pstmt.setInt(1, idClient);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.put("total_actions", rs.getInt("total"));
                    System.out.println("📊 Total actions: " + rs.getInt("total"));
                }
            }

            // Par type depuis HistoriqueClient
            String sqlTypes = """
                SELECT type_action, COUNT(*) as count 
                FROM HistoriqueClient 
                WHERE id_client = ? 
                GROUP BY type_action
                """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlTypes)) {
                pstmt.setInt(1, idClient);
                ResultSet rs = pstmt.executeQuery();

                int rdvCount = 0, venteCount = 0, commentCount = 0;
                while (rs.next()) {
                    String type = rs.getString("type_action");
                    int count = rs.getInt("count");

                    if ("rendez_vous".equals(type)) {
                        rdvCount = count;
                    } else if ("vente".equals(type)) {
                        venteCount = count;
                    } else if ("commentaire".equals(type)) {
                        commentCount = count;
                    }
                }

                stats.put("rendez_vous", rdvCount);
                stats.put("ventes", venteCount);
                stats.put("commentaires", commentCount);

                System.out.println("📊 RDV: " + rdvCount + ", Ventes: " + venteCount + ", Commentaires: " + commentCount);
            }

            // Total dépensé depuis Vente
            String sqlDepenses = """
                SELECT COALESCE(SUM(montant_total), 0) as total_depense
                FROM Vente 
                WHERE id_client = ? AND statut_vente = 'terminée'
                """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlDepenses)) {
                pstmt.setInt(1, idClient);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.put("total_depense", rs.getDouble("total_depense"));
                    System.out.println("📊 Total dépensé: " + rs.getDouble("total_depense"));
                }
            }

            // Moyenne des notes depuis HistoriqueClient + Commentaire
            String sqlNotes = """
                SELECT COALESCE(AVG(c.note), 0) as moyenne_notes
                FROM HistoriqueClient hc
                JOIN Commentaire c ON hc.id_commentaire = c.id_commentaire
                WHERE hc.id_client = ? AND hc.type_action = 'commentaire'
                """;
            try (PreparedStatement pstmt = conn.prepareStatement(sqlNotes)) {
                pstmt.setInt(1, idClient);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    stats.put("moyenne_notes", rs.getDouble("moyenne_notes"));
                    System.out.println("📊 Moyenne notes: " + rs.getDouble("moyenne_notes"));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur statistiques: " + e.getMessage());
            e.printStackTrace();

            // Valeurs par défaut en cas d'erreur
            stats.put("total_actions", 0);
            stats.put("rendez_vous", 0);
            stats.put("ventes", 0);
            stats.put("commentaires", 0);
            stats.put("total_depense", 0.0);
            stats.put("moyenne_notes", 0.0);
        }

        return stats;
    }

    private HistoriqueItem mapResultSetToHistoriqueItem(ResultSet rs) throws SQLException {
        HistoriqueItem item = new HistoriqueItem();

        try {
            item.setIdHistorique(rs.getInt("id_historique"));
            item.setIdClient(rs.getInt("id_client"));
            item.setTypeAction(rs.getString("type_action"));
            item.setDateAction(rs.getTimestamp("date_action"));

            // Détails JSON
            String details = rs.getString("details");
            item.setDetails(details != null ? details : "{}");

            // Informations article
            item.setArticleTitre(rs.getString("article_titre"));
            item.setArticleMarque(rs.getString("article_marque"));
            item.setArticleModele(rs.getString("article_modele"));

            // Vendeur - plusieurs façons de récupérer le nom
            String vendeurPrenom = rs.getString("vendeur_prenom");
            String vendeurNomComplet = rs.getString("vendeur_nom_complet");
            if (vendeurPrenom != null && vendeurNomComplet != null) {
                item.setVendeurNom(vendeurPrenom + " " + vendeurNomComplet);
            } else {
                String nomVendeur = rs.getString("vendeur_nom");
                item.setVendeurNom(nomVendeur != null ? nomVendeur : "");
            }

            // Montant
            double montant = rs.getDouble("montant_total");
            if (!rs.wasNull()) {
                item.setMontant(montant);
            }

            // Note
            int note = rs.getInt("note");
            if (!rs.wasNull()) {
                item.setNote(note);
            }

            // Statut - vérifier d'abord RDV, puis Vente
            String statut = rs.getString("rdv_statut");
            if (statut == null || statut.isEmpty()) {
                statut = rs.getString("statut_vente");
            }
            if (statut == null) {
                statut = "";
            }
            item.setStatut(statut);

        } catch (SQLException e) {
            System.err.println("❌ Erreur mapping HistoriqueItem: " + e.getMessage());
            throw e;
        }

        return item;
    }

    private LocalDate getDateDebut(String periode) {
        LocalDate today = LocalDate.now();
        if (periode == null) return today.minusYears(10);

        switch (periode) {
            case "Aujourd'hui": return today;
            case "Cette semaine": return today.minusDays(7);
            case "Ce mois": return today.minusMonths(1);
            case "3 derniers mois": return today.minusMonths(3);
            case "6 derniers mois": return today.minusMonths(6);
            case "Cette année": return today.minusYears(1);
            default: return today.minusYears(10);
        }
    }

    // Méthode pour tester la table HistoriqueClient
    public boolean testerTableHistoriqueClient(int idClient) {
        String sql = "SELECT COUNT(*) as count FROM HistoriqueClient WHERE id_client = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idClient);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("🔍 HistoriqueClient pour client " + idClient + ": " + count + " entrées");
                return count > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur test HistoriqueClient: " + e.getMessage());
        }

        return false;
    }

    // Méthode pour insérer des données de test si la table est vide
    public void insererDonneesDeTest(int idClient) {
        System.out.println("🔄 Insertion de données de test pour client " + idClient);

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Vérifier si la table HistoriqueClient contient déjà des données
            String checkSql = "SELECT COUNT(*) FROM HistoriqueClient WHERE id_client = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, idClient);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("✅ HistoriqueClient contient déjà des données");
                    return;
                }
            }

            // Insérer des rendez-vous de test
            String insertRdv = """
                INSERT INTO HistoriqueClient (
                    id_client, type_action, id_rendez_vous, 
                    id_article, id_vendeur, details, date_action
                ) 
                SELECT 
                    rdv.id_client, 
                    'rendez_vous', 
                    rdv.id_rdv, 
                    rdv.id_article, 
                    rdv.id_vendeur, 
                    jsonb_build_object(
                        'date_rdv', rdv.date_rdv,
                        'heure_rdv', rdv.heure_rdv,
                        'statut', rdv.statut,
                        'description', COALESCE(rdv.description, 'Essai routier')
                    ),
                    CURRENT_TIMESTAMP - INTERVAL '1 day' * random() * 30
                FROM RendezVous rdv 
                WHERE rdv.id_client = ?
                """;

            try (PreparedStatement pstmt = conn.prepareStatement(insertRdv)) {
                pstmt.setInt(1, idClient);
                int rdvInserted = pstmt.executeUpdate();
                System.out.println("✅ " + rdvInserted + " rendez-vous insérés");
            }

            // Insérer des ventes de test
            String insertVente = """
                INSERT INTO HistoriqueClient (
                    id_client, type_action, id_vente, 
                    id_article, id_vendeur, details, date_action
                ) 
                SELECT 
                    v.id_client, 
                    'vente', 
                    v.id_vente, 
                    v.id_article, 
                    v.id_vendeur, 
                    jsonb_build_object(
                        'montant_total', v.montant_total,
                        'statut_vente', v.statut_vente,
                        'date_vente', v.date_vente,
                        'moyen_paiement', COALESCE(v.moyen_paiement, 'Carte bancaire')
                    ),
                    v.date_vente
                FROM Vente v 
                WHERE v.id_client = ?
                """;

            try (PreparedStatement pstmt = conn.prepareStatement(insertVente)) {
                pstmt.setInt(1, idClient);
                int ventesInserted = pstmt.executeUpdate();
                System.out.println("✅ " + ventesInserted + " ventes insérées");
            }

            // Insérer des commentaires de test
            String insertCommentaire = """
                INSERT INTO HistoriqueClient (
                    id_client, type_action, id_commentaire, 
                    id_article, id_vendeur, details, date_action
                ) 
                SELECT 
                    c.id_utilisateur, 
                    'commentaire', 
                    c.id_commentaire, 
                    c.id_article, 
                    a.id_vendeur, 
                    jsonb_build_object(
                        'note', c.note,
                        'texte', c.texte_commentaire,
                        'date', c.date_commentaire
                    ),
                    c.date_commentaire
                FROM Commentaire c
                JOIN Article a ON c.id_article = a.id_article
                WHERE c.id_utilisateur = ?
                """;

            try (PreparedStatement pstmt = conn.prepareStatement(insertCommentaire)) {
                pstmt.setInt(1, idClient);
                int commentairesInserted = pstmt.executeUpdate();
                System.out.println("✅ " + commentairesInserted + " commentaires insérés");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur insertion données test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}