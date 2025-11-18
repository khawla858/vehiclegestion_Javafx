package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.vendeur.model.RendezVous;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RendezVousDAO {

    private Connection getConnection() throws SQLException {
        // Remplacez par votre méthode de connexion (ex. : DatabaseConnection.getInstance().getConnection())
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/your_db", "user", "password");
    }

    // Récupérer tous les RDV d'un vendeur avec filtres
    public List<RendezVous> getRendezVousByVendeur(int idVendeur, String statut, LocalDate dateDebut, LocalDate dateFin) {
        List<RendezVous> rdvList = new ArrayList<>();
        String query = "SELECT r.id_rdv, r.id_client, u.nom || ' ' || u.prenom AS nom_client, c.telephone, u.email, " +
                "r.id_article, a.titre AS titre_article, r.date_rdv, r.heure_rdv, r.statut, r.description, r.duree, r.commentaire " +
                "FROM RendezVous r " +
                "JOIN Client cl ON r.id_client = cl.id_client " +
                "JOIN Utilisateur u ON cl.id_client = u.id_utilisateur " +
                "LEFT JOIN Article a ON r.id_article = a.id_article " +
                "WHERE r.id_vendeur = ?";

        List<Object> params = new ArrayList<>();
        params.add(idVendeur);

        if (statut != null && !statut.isEmpty()) {
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
        query += " ORDER BY r.date_rdv, r.heure_rdv";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                RendezVous rdv = new RendezVous();
                rdv.setIdRdv(rs.getInt("id_rdv"));
                rdv.setIdClient(rs.getInt("id_client"));
                rdv.setNomClient(rs.getString("nom_client"));
                rdv.setTelephoneClient(rs.getString("telephone"));
                rdv.setEmailClient(rs.getString("email"));
                rdv.setIdArticle(rs.getInt("id_article"));
                rdv.setTitreArticle(rs.getString("titre_article"));
                rdv.setDateRdv(rs.getDate("date_rdv").toLocalDate());
                rdv.setHeureRdv(rs.getTime("heure_rdv").toLocalTime());
                rdv.setStatut(rs.getString("statut"));
                rdv.setDescription(rs.getString("description"));
                rdv.setDuree(rs.getInt("duree"));
                rdv.setCommentaire(rs.getString("commentaire"));
                rdvList.add(rdv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rdvList;
    }

    // Ajouter un RDV
    public boolean addRendezVous(RendezVous rdv) {
        String query = "INSERT INTO RendezVous (id_client, id_vendeur, id_article, date_rdv, heure_rdv, statut, description, duree, commentaire) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, rdv.getIdClient());
            stmt.setInt(2, rdv.getIdVendeur());
            stmt.setInt(3, rdv.getIdArticle());
            stmt.setDate(4, Date.valueOf(rdv.getDateRdv()));
            stmt.setTime(5, Time.valueOf(rdv.getHeureRdv()));
            stmt.setString(6, rdv.getStatut());
            stmt.setString(7, rdv.getDescription());
            stmt.setInt(8, rdv.getDuree());
            stmt.setString(9, rdv.getCommentaire());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Modifier un RDV
    public boolean updateRendezVous(RendezVous rdv) {
        String query = "UPDATE RendezVous SET id_client = ?, id_article = ?, date_rdv = ?, heure_rdv = ?, statut = ?, " +
                "description = ?, duree = ?, commentaire = ? WHERE id_rdv = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, rdv.getIdClient());
            stmt.setInt(2, rdv.getIdArticle());
            stmt.setDate(3, Date.valueOf(rdv.getDateRdv()));
            stmt.setTime(4, Time.valueOf(rdv.getHeureRdv()));
            stmt.setString(5, rdv.getStatut());
            stmt.setString(6, rdv.getDescription());
            stmt.setInt(7, rdv.getDuree());
            stmt.setString(8, rdv.getCommentaire());
            stmt.setInt(9, rdv.getIdRdv());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Annuler un RDV
    public boolean cancelRendezVous(int idRdv, String raison) {
        String query = "UPDATE RendezVous SET statut = 'annulé', commentaire = CONCAT(commentaire, ' - Annulé: ', ?) WHERE id_rdv = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, raison);
            stmt.setInt(2, idRdv);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Confirmer un RDV
    public boolean confirmRendezVous(int idRdv) {
        String query = "UPDATE RendezVous SET statut = 'confirmé' WHERE id_rdv = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idRdv);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Marquer comme terminé
    public boolean completeRendezVous(int idRdv, String commentaire) {
        String query = "UPDATE RendezVous SET statut = 'terminé', commentaire = CONCAT(commentaire, ' - Terminé: ', ?) WHERE id_rdv = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, commentaire);
            stmt.setInt(2, idRdv);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Supprimer un RDV (si nécessaire)
    public boolean deleteRendezVous(int idRdv) {
        String query = "DELETE FROM RendezVous WHERE id_rdv = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idRdv);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}