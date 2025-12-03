package com.example.vehiclegestion.common.dao;

import com.example.vehiclegestion.common.model.Notification;
import com.example.vehiclegestion.common.dao.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public void creerNotification(Notification notification) {
        String sql = "INSERT INTO Notification (id_utilisateur, role_destinataire, id_source, type_source, " +
                "titre, message, type_notification, categorie, est_lue, date_creation, " +
                "priorite, lien_action, data_context) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, notification.getIdUtilisateur());
            pstmt.setString(2, notification.getRoleDestinataire());
            if (notification.getIdSource() != null) {
                pstmt.setInt(3, notification.getIdSource());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setString(4, notification.getTypeSource());
            pstmt.setString(5, notification.getTitre());
            pstmt.setString(6, notification.getMessage());
            pstmt.setString(7, notification.getTypeNotification());
            pstmt.setString(8, notification.getCategorie());
            pstmt.setBoolean(9, notification.isEstLue());
            pstmt.setTimestamp(10, Timestamp.valueOf(notification.getDateCreation()));
            pstmt.setString(11, notification.getPriorite());
            pstmt.setString(12, notification.getLienAction());
            pstmt.setString(13, notification.getDataContext());

            pstmt.executeUpdate();

            // Récupérer l'ID généré
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    notification.setIdNotification(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur création notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Notification> getNotificationsUtilisateur(int idUtilisateur, boolean nonLuesSeulement) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE id_utilisateur = ? ";

        if (nonLuesSeulement) {
            sql += "AND est_lue = false ";
        }

        sql += "ORDER BY date_creation DESC LIMIT 50";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUtilisateur);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToNotification(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération notifications: " + e.getMessage());
        }

        return notifications;
    }

    public int getNombreNotificationsNonLues(int idUtilisateur) {
        String sql = "SELECT COUNT(*) FROM Notification WHERE id_utilisateur = ? AND est_lue = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUtilisateur);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur compte notifications non lues: " + e.getMessage());
        }

        return 0;
    }

    public void marquerCommeLue(int idNotification) {
        String sql = "UPDATE Notification SET est_lue = true, date_lecture = NOW() WHERE id_notification = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idNotification);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur marquer notification comme lue: " + e.getMessage());
        }
    }

    public void marquerToutesCommeLues(int idUtilisateur) {
        String sql = "UPDATE Notification SET est_lue = true, date_lecture = NOW() " +
                "WHERE id_utilisateur = ? AND est_lue = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUtilisateur);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur marquer toutes notifications comme lues: " + e.getMessage());
        }
    }

    public void supprimerNotification(int idNotification) {
        String sql = "DELETE FROM Notification WHERE id_notification = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idNotification);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur suppression notification: " + e.getMessage());
        }
    }

    public void supprimerNotificationsAnciennes(int idUtilisateur, int jours) {
        String sql = "DELETE FROM Notification WHERE id_utilisateur = ? " +
                "AND date_creation < NOW() - INTERVAL '? days'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUtilisateur);
            pstmt.setInt(2, jours);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur suppression notifications anciennes: " + e.getMessage());
        }
    }

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setIdNotification(rs.getInt("id_notification"));
        notification.setIdUtilisateur(rs.getInt("id_utilisateur"));
        notification.setRoleDestinataire(rs.getString("role_destinataire"));
        notification.setIdSource(rs.getInt("id_source"));
        notification.setTypeSource(rs.getString("type_source"));
        notification.setTitre(rs.getString("titre"));
        notification.setMessage(rs.getString("message"));
        notification.setTypeNotification(rs.getString("type_notification"));
        notification.setCategorie(rs.getString("categorie"));
        notification.setEstLue(rs.getBoolean("est_lue"));

        Timestamp tsCreation = rs.getTimestamp("date_creation");
        if (tsCreation != null) {
            notification.setDateCreation(tsCreation.toLocalDateTime());
        }

        Timestamp tsLecture = rs.getTimestamp("date_lecture");
        if (tsLecture != null) {
            notification.setDateLecture(tsLecture.toLocalDateTime());
        }

        notification.setPriorite(rs.getString("priorite"));
        notification.setLienAction(rs.getString("lien_action"));
        notification.setDataContext(rs.getString("data_context"));

        return notification;
    }
}