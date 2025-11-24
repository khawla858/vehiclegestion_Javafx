// ========================================
// 2. ReservationDAO.java (DAO)
// ========================================
package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.vendeur.model.Reservation;
import com.example.vehiclegestion.common.dao.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private Connection connection;

    public ReservationDAO() throws SQLException {
        connection = DatabaseConnection.getConnection();
    }

    // 📌 Récupérer toutes les réservations pour un vendeur
    public List<Reservation> getReservationsByVendeur(int idVendeur) throws SQLException {
        List<Reservation> list = new ArrayList<>();

        String sql =
                "SELECT r.id_reservation, r.id_client, " +
                        "u.nom || ' ' || u.prenom AS nom_client, " +
                        "r.id_vehicule, a.titre, a.prix, " +
                        "r.date_reservation, r.statut, r.date_expiration " +
                        "FROM reservation r " +
                        "JOIN client c ON r.id_client = c.id_client " +
                        "JOIN utilisateur u ON c.id_client = u.id_utilisateur " +
                        "JOIN article a ON r.id_vehicule = a.id_article " +
                        "WHERE a.id_vendeur = ? " +
                        "ORDER BY r.date_reservation DESC";


        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idVendeur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Reservation(
                        rs.getInt("id_reservation"),
                        rs.getInt("id_client"),
                        rs.getString("nom_client"),
                        rs.getInt("id_vehicule"),
                        rs.getString("titre"),
                        rs.getDouble("prix"),
                        rs.getTimestamp("date_reservation").toLocalDateTime(),
                        rs.getString("statut"),
                        rs.getTimestamp("date_expiration").toLocalDateTime()
                ));
            }
        }

        return list;
    }

    // 📌 Confirmer une réservation
    public boolean confirmerReservation(int idReservation) throws SQLException {
        String sql = "UPDATE reservation SET statut='confirmée' WHERE id_reservation=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReservation);
            return stmt.executeUpdate() > 0;
        }
    }

    // 📌 Refuser une réservation
    public boolean refuserReservation(int idReservation) throws SQLException {
        String sql = "UPDATE reservation SET statut='annulée' WHERE id_reservation=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idReservation);
            return stmt.executeUpdate() > 0;
        }
    }

    // 📌 Marquer les réservations expirées
    public void updateExpiredReservations() throws SQLException {
        String sql = "UPDATE reservation SET statut='expirée' WHERE date_expiration < NOW() AND statut='en attente'";

        try (Statement stmt = connection.createStatement()) {
            int updated = stmt.executeUpdate(sql);
            System.out.println("✅ " + updated + " réservations expirées");
        }
    }

    // 📌 Compter les réservations par statut
    public int countReservationsByStatus(int idVendeur, String statut) throws SQLException {
        String sql =
                "SELECT COUNT(*) FROM reservation r " +
                        "JOIN article a ON r.id_vehicule = a.id_article " +
                        "WHERE a.id_vendeur = ? AND r.statut = ?";


        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idVendeur);
            stmt.setString(2, statut);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}