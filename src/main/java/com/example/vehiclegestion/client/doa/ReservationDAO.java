package com.example.vehiclegestion.client.doa;

import com.example.vehiclegestion.client.model.Reservation;
import com.example.vehiclegestion.common.dao.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.example.vehiclegestion.common.dao.DatabaseConnection;
import com.example.vehiclegestion.common.dao.DatabaseConnection;

public class ReservationDAO {
    private Connection connection;

    public ReservationDAO() {
        try {
            this.connection = DatabaseConnection.getConnection();
            System.out.println("✅ ReservationDAO connecté à la base de données");
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion ReservationDAO: " + e.getMessage());
            throw new RuntimeException("Erreur de connexion à la base de données", e);
        }
    }

    // 📌 Créer une nouvelle réservation (version corrigée)
    public boolean createReservation(int clientId, int vehiculeId) {
        String sql = "INSERT INTO reservation (id_client, id_vehicule, date_reservation, statut) VALUES (?, ?, CURRENT_TIMESTAMP, 'en attente')";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, vehiculeId);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Réservation créée - Client: " + clientId + ", Véhicule: " + vehiculeId);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur création réservation: " + e.getMessage());
            // Vérifier si c'est une violation de contrainte d'unicité
            if (e.getMessage().contains("unique constraint") || e.getMessage().contains("duplicate key")) {
                System.err.println("⚠️ Le client a déjà réservé ce véhicule");
            }
            return false;
        }
    }

    // 📌 Vérifier si un véhicule est déjà réservé (version corrigée)
    public boolean isVehiculeReserved(int vehiculeId) {
        // Version plus compatible avec différentes bases de données
        String sql = "SELECT COUNT(*) as count FROM reservation " +
                "WHERE id_vehicule = ? " +
                "AND statut IN ('en attente', 'confirmée') " +
                "AND (date_expiration > CURRENT_TIMESTAMP OR date_expiration IS NULL)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, vehiculeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                System.out.println("🔍 Véhicule " + vehiculeId + " - Réservations actives: " + count);
                return count > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification réservation véhicule " + vehiculeId + ": " + e.getMessage());
        }
        return false;
    }

    // 📌 Obtenir les réservations d'un client (version corrigée)
    public List<Reservation> getClientReservations(int clientId) {
        List<Reservation> reservations = new ArrayList<>();

        // Requête simplifiée et plus compatible
        String sql = "SELECT r.id_reservation, r.id_client, r.id_vehicule, " +
                "a.titre, a.prix, r.date_reservation, r.statut, r.date_expiration " +
                "FROM reservation r " +
                "JOIN article a ON r.id_vehicule = a.id_article " +
                "WHERE r.id_client = ? " +
                "ORDER BY r.date_reservation DESC";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reservation reservation = new Reservation(
                        rs.getInt("id_reservation"),
                        rs.getInt("id_client"),
                        rs.getInt("id_vehicule"),
                        rs.getString("titre"),
                        rs.getDouble("prix"),
                        rs.getTimestamp("date_reservation").toLocalDateTime(),
                        rs.getString("statut"),
                        rs.getTimestamp("date_expiration") != null ?
                                rs.getTimestamp("date_expiration").toLocalDateTime() : null
                );
                reservations.add(reservation);
            }
            System.out.println("✅ " + reservations.size() + " réservations trouvées pour client " + clientId);

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération réservations client " + clientId + ": " + e.getMessage());
        }

        return reservations;
    }

    // 📌 Vérifier si le client a déjà réservé ce véhicule (version corrigée)
    public boolean hasClientReservedVehicule(int clientId, int vehiculeId) {
        String sql = "SELECT COUNT(*) as count FROM reservation " +
                "WHERE id_client = ? AND id_vehicule = ? " +
                "AND statut IN ('en attente', 'confirmée')";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, vehiculeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                boolean hasReserved = rs.getInt("count") > 0;
                System.out.println("🔍 Client " + clientId + " a réservé véhicule " + vehiculeId + ": " + hasReserved);
                return hasReserved;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification réservation client " + clientId + " véhicule " + vehiculeId + ": " + e.getMessage());
        }
        return false;
    }

    // 📌 Obtenir le nombre de réservations actives pour un véhicule (version corrigée)
    public int getActiveReservationsCount(int vehiculeId) {
        String sql = "SELECT COUNT(*) as count FROM reservation " +
                "WHERE id_vehicule = ? " +
                "AND statut IN ('en attente', 'confirmée') " +
                "AND (date_expiration > CURRENT_TIMESTAMP OR date_expiration IS NULL)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, vehiculeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage réservations véhicule " + vehiculeId + ": " + e.getMessage());
        }
        return 0;
    }

    // 📌 Annuler une réservation (version corrigée)
    public boolean cancelReservation(int reservationId) {
        String sql = "UPDATE reservation SET statut = 'annulée' WHERE id_reservation = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            boolean success = stmt.executeUpdate() > 0;
            if (success) {
                System.out.println("✅ Réservation " + reservationId + " annulée");
            }
            return success;
        } catch (SQLException e) {
            System.err.println("❌ Erreur annulation réservation " + reservationId + ": " + e.getMessage());
            return false;
        }
    }

    // 📌 Mettre à jour les réservations expirées (version corrigée)
    public void updateExpiredReservations() {
        // Version compatible avec différentes bases de données
        String sql = "UPDATE reservation SET statut = 'expirée' " +
                "WHERE date_expiration < CURRENT_TIMESTAMP " +
                "AND statut = 'en attente'";

        try (Statement stmt = connection.createStatement()) {
            int updated = stmt.executeUpdate(sql);
            if (updated > 0) {
                System.out.println("🔄 " + updated + " réservations expirées mises à jour");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour réservations expirées: " + e.getMessage());
        }
    }

    // 📌 Méthode utilitaire pour tester la connexion
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Test connexion échoué: " + e.getMessage());
            return false;
        }
    }

    // 📌 Fermer la connexion
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Connexion ReservationDAO fermée");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur fermeture connexion: " + e.getMessage());
        }
    }
}