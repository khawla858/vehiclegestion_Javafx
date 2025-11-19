package com.example.vehiclegestion.client.doa;

import com.example.vehiclegestion.client.model.Client;
import com.example.vehiclegestion.client.model.Vehicle;
import com.example.vehiclegestion.utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public Client authenticate(String email, String password) {
        // Remplacer les text blocks par des strings concaténées
        String sql = "SELECT u.id_utilisateur, u.nom, u.prenom, u.email, u.date_creation, " +
                "c.adresse, c.telephone, c.moyen_paiement, c.centre_interet, " +
                "c.photo_profil, c.statut_client " +
                "FROM Utilisateur u " +
                "JOIN Client c ON u.id_utilisateur = c.id_client " +
                "WHERE u.email = ? AND u.mot_de_passe = ? AND u.role = 'client' AND u.statut = 'actif'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Client(
                        rs.getInt("id_utilisateur"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("moyen_paiement"),
                        rs.getString("centre_interet"),
                        rs.getString("photo_profil"),
                        rs.getString("statut_client"),
                        rs.getTimestamp("date_creation").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean registerClient(String nom, String prenom, String email, String password,
                                  String telephone, String adresse) {
        String sqlUser = "INSERT INTO Utilisateur (nom, prenom, email, mot_de_passe, role, statut) " +
                "VALUES (?, ?, ?, ?, 'client', 'actif')";

        String sqlClient = "INSERT INTO Client (id_client, adresse, telephone) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement stmtUser = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            stmtUser.setString(1, nom);
            stmtUser.setString(2, prenom);
            stmtUser.setString(3, email);
            stmtUser.setString(4, password);

            int affectedRows = stmtUser.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }

            ResultSet generatedKeys = stmtUser.getGeneratedKeys();
            int clientId = -1;
            if (generatedKeys.next()) {
                clientId = generatedKeys.getInt(1);
            }

            PreparedStatement stmtClient = conn.prepareStatement(sqlClient);
            stmtClient.setInt(1, clientId);
            stmtClient.setString(2, adresse);
            stmtClient.setString(3, telephone);

            stmtClient.executeUpdate();
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Vehicle> getFavoriteVehicles(int clientId) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT a.*, v.nom_magasin " +
                "FROM Article a " +
                "JOIN Commentaire c ON a.id_article = c.id_article " +
                "WHERE c.id_client = ? AND c.note >= 4 " +
                "ORDER BY c.note DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vehicle vehicle = new Vehicle(
                        rs.getInt("id_article"),
                        rs.getString("titre"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getString("categorie"),
                        rs.getString("etat"),
                        rs.getString("image"),
                        rs.getTimestamp("date_ajout").toLocalDateTime(),
                        rs.getInt("id_vendeur"),
                        rs.getString("nom_magasin")
                );
                vehicles.add(vehicle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    public boolean updateClientProfile(Client client) {
        String sqlUser = "UPDATE Utilisateur SET nom = ?, prenom = ?, email = ? WHERE id_utilisateur = ?";
        String sqlClient = "UPDATE Client SET adresse = ?, telephone = ?, moyen_paiement = ? WHERE id_client = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement stmtUser = conn.prepareStatement(sqlUser);
            stmtUser.setString(1, client.getLastName());
            stmtUser.setString(2, client.getFirstName());
            stmtUser.setString(3, client.getEmail());
            stmtUser.setInt(4, client.getId());
            stmtUser.executeUpdate();

            PreparedStatement stmtClient = conn.prepareStatement(sqlClient);
            stmtClient.setString(1, client.getAddress());
            stmtClient.setString(2, client.getPhone());
            stmtClient.setString(3, client.getPaymentMethod());
            stmtClient.setInt(4, client.getId());
            stmtClient.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}