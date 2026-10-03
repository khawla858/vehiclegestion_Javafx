package com.example.vehiclegestion.common.dao;

import com.example.vehiclegestion.common.model.Notification;
import com.example.vehiclegestion.common.dao.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public void creerNotification(Notification notification) {
        System.out.println("📝 DEBUT creerNotification");
        System.out.println("   User ID: " + notification.getIdUtilisateur());
        System.out.println("   Titre: " + notification.getTitre());
        System.out.println("   Message: " + notification.getMessage());

        String sql = "INSERT INTO Notification (id_utilisateur, role_destinataire, id_source, type_source, " +
                "titre, message, type_notification, categorie, est_lue, date_creation, " +
                "priorite, lien_action, data_context) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("✅ Connexion à la base établie");

            pstmt.setInt(1, notification.getIdUtilisateur());
            pstmt.setString(2, notification.getRoleDestinataire());

            if (notification.getIdSource() != null && notification.getIdSource() > 0) {
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
            pstmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(11, notification.getPriorite());
            pstmt.setString(12, notification.getLienAction());
            pstmt.setString(13, notification.getDataContext());

            System.out.println("🚀 Exécution de l'insertion...");
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✅ Lignes affectées: " + rowsAffected);

            // Récupérer l'ID généré
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    notification.setIdNotification(generatedId);
                    System.out.println("🎯 ID généré: " + generatedId);
                } else {
                    System.out.println("⚠ Aucun ID généré");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL lors de la création de notification:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   Code SQL: " + e.getErrorCode());
            System.err.println("   SQL State: " + e.getSQLState());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ ERREUR générale lors de la création de notification:");
            e.printStackTrace();
        }

        System.out.println("📝 FIN creerNotification");
    }

    public List<Notification> getNotificationsUtilisateur(int idUtilisateur, boolean nonLuesSeulement) {
        System.out.println("🔍 DEBUT getNotificationsUtilisateur pour user: " + idUtilisateur);

        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE id_utilisateur = ? ";

        if (nonLuesSeulement) {
            sql += "AND est_lue = false ";
        }

        sql += "ORDER BY date_creation DESC LIMIT 50";

        System.out.println("📋 Requête SQL: " + sql);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUtilisateur);
            System.out.println("✅ Paramètre user ID: " + idUtilisateur);

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    notifications.add(mapResultSetToNotification(rs));
                }
                System.out.println("📊 " + count + " notifications trouvées");
            }

        } catch (SQLException e) {
            System.err.println("❌ ERREUR récupération notifications: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("🔍 FIN getNotificationsUtilisateur");
        return notifications;
    }

    public int getNombreNotificationsNonLues(int idUtilisateur) {
        String sql = "SELECT COUNT(*) FROM Notification WHERE id_utilisateur = ? AND est_lue = false";
        System.out.println("🔢 Comptage notifications non lues pour user: " + idUtilisateur);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUtilisateur);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("📊 Nombre non lues: " + count);
                    return count;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ ERREUR compte notifications non lues: " + e.getMessage());
        }

        return 0;
    }

    public void marquerCommeLue(int idNotification) {
        System.out.println("📖 Marquer notification " + idNotification + " comme lue");

        String sql = "UPDATE Notification SET est_lue = true, date_lecture = NOW() WHERE id_notification = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idNotification);
            int rows = pstmt.executeUpdate();
            System.out.println("✅ Lignes mises à jour: " + rows);

        } catch (SQLException e) {
            System.err.println("❌ ERREUR marquer notification comme lue: " + e.getMessage());
        }
    }

    public void marquerToutesCommeLues(int idUtilisateur) {
        System.out.println("📖 Marquer toutes notifications comme lues pour user: " + idUtilisateur);

        String sql = "UPDATE Notification SET est_lue = true, date_lecture = NOW() " +
                "WHERE id_utilisateur = ? AND est_lue = false";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUtilisateur);
            int rows = pstmt.executeUpdate();
            System.out.println("✅ Lignes mises à jour: " + rows);

        } catch (SQLException e) {
            System.err.println("❌ ERREUR marquer toutes notifications comme lues: " + e.getMessage());
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

    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setIdNotification(rs.getInt("id_notification"));
        notification.setIdUtilisateur(rs.getInt("id_utilisateur"));
        notification.setRoleDestinataire(rs.getString("role_destinataire"));

        int idSource = rs.getInt("id_source");
        if (!rs.wasNull()) {
            notification.setIdSource(idSource);
        }

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

        System.out.println("   → Notification ID " + notification.getIdNotification() +
                " - " + notification.getTitre() +
                " (lue: " + notification.isEstLue() + ")");

        return notification;
    }

    // Méthode de test pour vérifier la connexion
    public boolean testerConnexion() {
        System.out.println("🔧 Test de connexion à la base de données...");

        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connexion à la base OK");

                // Tester si la table Notification existe
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet tables = meta.getTables(null, null, "Notification", null);
                if (tables.next()) {
                    System.out.println("✅ Table Notification existe");
                } else {
                    System.err.println("❌ Table Notification n'existe pas!");
                    return false;
                }

                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
        }

        return false;
    }
}