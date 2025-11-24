package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.vendeur.model.RendezVous;
import com.example.vehiclegestion.common.dao.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {

    private Connection connection;

    public RendezVousDAO() {
        try {
            connection = DatabaseConnection.getConnection();
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
        String query = "UPDATE RendezVous SET statut = 'confirmé' WHERE id_rdv = ?";
        try {
            if (connection == null || connection.isClosed()) {
                connection = DatabaseConnection.getConnection();
            }
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, idRdv);
            boolean result = stmt.executeUpdate() > 0;
            stmt.close();
            System.out.println(result ? "✅ RDV confirmé" : "⚠️ RDV non trouvé");
            return result;
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelRendezVous(int idRdv, String raison) {
        // ✅ Annulation - on ne modifie que le statut
        String query = "UPDATE RendezVous SET statut = 'annulé' WHERE id_rdv = ?";
        try {
            if (connection == null || connection.isClosed()) {
                connection = DatabaseConnection.getConnection();
            }
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, idRdv);
            boolean result = stmt.executeUpdate() > 0;
            stmt.close();
            System.out.println(result ? "✅ RDV annulé (raison: " + raison + ")" : "⚠️ RDV non trouvé");
            return result;
        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            return false;
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