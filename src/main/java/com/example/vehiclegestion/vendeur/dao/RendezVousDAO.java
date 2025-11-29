package com.example.vehiclegestion.vendeur.dao;
import com.example.vehiclegestion.vendeur.dao.ReservationDAO; // ⬅️ AJOUTE


import com.example.vehiclegestion.vendeur.model.RendezVous;
import com.example.vehiclegestion.common.dao.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {

    private Connection connection;
    private ReservationDAO reservationDAO; // ⬅️ AJOUTE


    public RendezVousDAO() {
        try {
            connection = DatabaseConnection.getConnection();
            reservationDAO = new ReservationDAO(); // ⬅️ AJOUTE

            System.out.println("✅ RendezVousDAO: Connexion établie");

            if (connection == null || connection.isClosed()) {
                System.err.println("❌ ERREUR: La connexion est fermée!");
            }
        } catch (SQLException e) {
            System.err.println("❌ ERREUR: Connexion impossible");
            e.printStackTrace();
        }
    }

    /**
     * ✅ Récupérer tous les RDV d'un vendeur avec filtres
     */
    public List<RendezVous> getRendezVousByVendeur(int idVendeur, String statut, LocalDate dateDebut, LocalDate dateFin) {
        System.out.println("\n🔍 === getRendezVousByVendeur ===");
        System.out.println("   Vendeur ID: " + idVendeur);
        System.out.println("   Filtre statut: " + (statut != null ? statut : "Tous"));

        List<RendezVous> rdvList = new ArrayList<>();

        // ✅ Requête SQL - description utilisé comme type_rdv
        String query = "SELECT " +
                "r.id_rdv, r.id_client, r.id_vendeur, r.id_article, " +
                "u.nom || ' ' || u.prenom AS nom_client, " +
                "c.telephone, u.email, " +
                "COALESCE(a.titre, 'Aucun véhicule') AS titre_article, " +
                "r.date_rdv, r.heure_rdv, r.statut, " +
                "COALESCE(r.description, 'Visite véhicule') AS type_rdv, " +
                "COALESCE(r.duree, 60) AS duree " +
                "FROM RendezVous r " +
                "JOIN Client c ON r.id_client = c.id_client " +
                "JOIN Utilisateur u ON c.id_client = u.id_utilisateur " +
                "LEFT JOIN Article a ON r.id_article = a.id_article " +
                "WHERE r.id_vendeur = ?";

        List<Object> params = new ArrayList<>();
        params.add(idVendeur);

        if (statut != null && !statut.isEmpty() && !statut.equals("Tous")) {
            query += " AND r.statut = ?";
            params.add(statut);
        }
        if (dateDebut != null) {
            query += " AND r.date_rdv >= ?";
            params.add(Date.valueOf(dateDebut));
        }
        if (dateFin != null) {
            query += " AND r.date_rdv <= ?";
            params.add(Date.valueOf(dateFin));
        }
        query += " ORDER BY r.date_rdv DESC, r.heure_rdv DESC";

        System.out.println("   📝 SQL: " + query);

        try {
            if (connection == null || connection.isClosed()) {
                System.err.println("❌ Reconnexion...");
                connection = DatabaseConnection.getConnection();
            }

            PreparedStatement stmt = connection.prepareStatement(query);

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
                System.out.println("   📝 Param " + (i+1) + ": " + params.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                RendezVous rdv = new RendezVous();
                rdv.setIdRdv(rs.getInt("id_rdv"));
                rdv.setIdClient(rs.getInt("id_client"));
                rdv.setIdVendeur(rs.getInt("id_vendeur"));
                rdv.setNomClient(rs.getString("nom_client"));
                rdv.setTelephoneClient(rs.getString("telephone"));
                rdv.setEmailClient(rs.getString("email"));

                // Gérer id_article qui peut être NULL
                int idArticle = rs.getInt("id_article");
                rdv.setIdArticle(rs.wasNull() ? 0 : idArticle);
                rdv.setTitreArticle(rs.getString("titre_article"));

                rdv.setDateRdv(rs.getDate("date_rdv").toLocalDate());
                rdv.setHeureRdv(rs.getTime("heure_rdv").toLocalTime());
                rdv.setStatut(rs.getString("statut"));
                rdv.setTypeRdv(rs.getString("type_rdv")); // description → typeRdv
                rdv.setDuree(rs.getInt("duree"));

                rdvList.add(rdv);
                count++;
            }

            System.out.println("   ✅ " + count + " rendez-vous trouvé(s)");

            if (count > 0) {
                System.out.println("\n   📋 Liste:");
                for (RendezVous rdv : rdvList) {
                    System.out.println("      • " + rdv.getDateRdv() + " " +
                            rdv.getHeureRdv() + " - " + rdv.getNomClient() +
                            " (" + rdv.getStatut() + ")");
                }
            }

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   SQLState: " + e.getSQLState());
            e.printStackTrace();
        }

        System.out.println("🔍 === Fin ===\n");
        return rdvList;
    }

    public boolean addRendezVous(RendezVous rdv) {
        // ✅ Requête - on stocke typeRdv dans description
        String query = "INSERT INTO RendezVous (id_client, id_vendeur, id_article, date_rdv, heure_rdv, statut, description, duree) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            if (connection == null || connection.isClosed()) {
                connection = DatabaseConnection.getConnection();
            }

            PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, rdv.getIdClient());
            stmt.setInt(2, rdv.getIdVendeur());

            // Gérer id_article NULL
            if (rdv.getIdArticle() > 0) {
                stmt.setInt(3, rdv.getIdArticle());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setDate(4, Date.valueOf(rdv.getDateRdv()));
            stmt.setTime(5, Time.valueOf(rdv.getHeureRdv()));
            stmt.setString(6, rdv.getStatut());
            stmt.setString(7, rdv.getTypeRdv()); // typeRdv → description en BD
            stmt.setInt(8, rdv.getDuree());

            int result = stmt.executeUpdate();

            if (result > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    rdv.setIdRdv(keys.getInt(1));
                }
                System.out.println("✅ RDV ajouté (ID: " + rdv.getIdRdv() + ")");
                stmt.close();
                return true;
            }
            stmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean confirmRendezVous(int idRdv) {
        Connection conn = null;
        PreparedStatement stmtRdv = null;
        PreparedStatement stmtReserv = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // ✅ 1. Récupérer les infos du RDV
            String queryRdv = "SELECT id_client, id_article, date_rdv, heure_rdv FROM RendezVous WHERE id_rdv = ?";
            stmtRdv = conn.prepareStatement(queryRdv);
            stmtRdv.setInt(1, idRdv);
            rs = stmtRdv.executeQuery();

            if (!rs.next()) {
                System.err.println("⚠️ RDV introuvable (ID: " + idRdv + ")");
                conn.rollback();
                return false;
            }

            int idClient = rs.getInt("id_client");
            int idArticle = rs.getInt("id_article");
            java.sql.Date dateRdv = rs.getDate("date_rdv");
            java.sql.Time heureRdv = rs.getTime("heure_rdv");

            // ⚠️ Si pas d'article lié, on confirme quand même le RDV
            if (idArticle == 0) {
                String updateRdv = "UPDATE RendezVous SET statut = 'confirmé' WHERE id_rdv = ?";
                PreparedStatement stmt = conn.prepareStatement(updateRdv);
                stmt.setInt(1, idRdv);
                stmt.executeUpdate();
                conn.commit();
                System.out.println("✅ RDV confirmé (sans véhicule associé)");
                return true;
            }

            // ✅ 2. Créer une réservation qui COMMENCE le jour du RDV
            // La date_reservation = date du RDV (pas maintenant)
            // La durée = 24h, donc elle expire 24h après le RDV
            String queryReserv = "INSERT INTO reservation (id_client, id_vehicule, date_reservation, statut, duree_limite) " +
                    "VALUES (?, ?, ?::timestamp, 'confirmée', INTERVAL '24 hours') " +
                    "RETURNING id_reservation";
            stmtReserv = conn.prepareStatement(queryReserv);
            stmtReserv.setInt(1, idClient);
            stmtReserv.setInt(2, idArticle);

            // 🎯 Combiner date_rdv + heure_rdv en timestamp
            java.sql.Timestamp timestampRdv = new java.sql.Timestamp(
                    dateRdv.getTime() + heureRdv.getTime()
            );
            stmtReserv.setTimestamp(3, timestampRdv);

            ResultSet rsReserv = stmtReserv.executeQuery();
            int idReservation = 0;
            if (rsReserv.next()) {
                idReservation = rsReserv.getInt("id_reservation");
            }
            rsReserv.close();

            // ✅ 3. L'article reste 'disponible' pour l'instant
            // (il deviendra 'reserve' automatiquement le jour du RDV)

            // ✅ 4. Lier la réservation au RDV et confirmer
            String updateRdv = "UPDATE RendezVous SET statut = 'confirmé', id_reservation = ? WHERE id_rdv = ?";
            PreparedStatement stmtUpdate = conn.prepareStatement(updateRdv);
            stmtUpdate.setInt(1, idReservation);
            stmtUpdate.setInt(2, idRdv);
            stmtUpdate.executeUpdate();

            conn.commit();

            System.out.println("✅ RDV confirmé (ID: " + idRdv + ")");
            System.out.println("✅ Réservation programmée pour le: " + dateRdv + " " + heureRdv);
            System.out.println("📅 Article sera réservé du " + dateRdv + " jusqu'au lendemain");

            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur confirmation RDV: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmtRdv != null) stmtRdv.close();
                if (stmtReserv != null) stmtReserv.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean cancelRendezVous(int idRdv, String raison) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Récupérer l'id_reservation et id_article
            String queryGet = "SELECT id_reservation, id_article FROM RendezVous WHERE id_rdv = ?";
            PreparedStatement stmtGet = conn.prepareStatement(queryGet);
            stmtGet.setInt(1, idRdv);
            ResultSet rs = stmtGet.executeQuery();

            Integer idReservation = null;
            Integer idArticle = null;
            if (rs.next()) {
                idReservation = rs.getInt("id_reservation");
                if (rs.wasNull()) idReservation = null;

                idArticle = rs.getInt("id_article");
                if (rs.wasNull()) idArticle = null;
            }
            rs.close();

            // 2. Annuler la réservation si elle existe
            if (idReservation != null) {
                String cancelReserv = "UPDATE reservation SET statut = 'annulée' WHERE id_reservation = ?";
                PreparedStatement stmtReserv = conn.prepareStatement(cancelReserv);
                stmtReserv.setInt(1, idReservation);
                stmtReserv.executeUpdate();
            }

            // 3. Remettre l'article en 'disponible' si nécessaire
            if (idArticle != null) {
                String updateArticle = "UPDATE Article SET statut_vehicule = 'disponible' WHERE id_article = ?";
                PreparedStatement stmtArticle = conn.prepareStatement(updateArticle);
                stmtArticle.setInt(1, idArticle);
                stmtArticle.executeUpdate();
            }

            // 4. Annuler le RDV
            String updateRdv = "UPDATE RendezVous SET statut = 'annulé' WHERE id_rdv = ?";
            PreparedStatement stmtRdv = conn.prepareStatement(updateRdv);
            stmtRdv.setInt(1, idRdv);
            boolean result = stmtRdv.executeUpdate() > 0;

            conn.commit();
            System.out.println("✅ RDV annulé (raison: " + raison + ")");
            return result;

        } catch (SQLException e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            System.err.println("❌ Erreur annulation: " + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {}
        }
    }

    public boolean deleteRendezVous(int idRdv) {
        String query = "DELETE FROM RendezVous WHERE id_rdv = ?";
        try {
            if (connection == null || connection.isClosed()) {
                connection = DatabaseConnection.getConnection();
            }
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, idRdv);
            boolean result = stmt.executeUpdate() > 0;
            stmt.close();
            System.out.println(result ? "✅ RDV supprimé" : "⚠️ RDV non trouvé");
            return result;
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return false;
        }
    }
}