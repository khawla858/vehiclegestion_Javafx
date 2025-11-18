package com.example.vehiclegestion.client.doa;

import com.example.vehiclegestion.client.model.Vehicle;
import com.example.vehiclegestion.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        // Remplacer le text block par une string concaténée
        String sql = "SELECT a.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom " +
                "FROM Article a " +
                "JOIN Vendeur v ON a.id_vendeur = v.id_vendeur " +
                "JOIN Utilisateur u ON v.id_vendeur = u.id_utilisateur " +
                "WHERE a.etat != 'vendu' " +
                "ORDER BY a.date_ajout DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

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
                        rs.getString("vendeur_prenom") + " " + rs.getString("vendeur_nom")
                );
                vehicles.add(vehicle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    public List<Vehicle> getFavoriteVehicles(int clientId) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT a.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom " +
                "FROM Article a " +
                "JOIN Vendeur v ON a.id_vendeur = v.id_vendeur " +
                "JOIN Utilisateur u ON v.id_vendeur = u.id_utilisateur " +
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
                        rs.getString("vendeur_prenom") + " " + rs.getString("vendeur_nom")
                );
                vehicles.add(vehicle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }

    public int getTotalVehiclesCount() {
        String sql = "SELECT COUNT(*) as total FROM Article WHERE etat != 'vendu'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Vehicle> searchVehicles(String keyword, String category, String state) {
        List<Vehicle> vehicles = new ArrayList<>();

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT a.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom ")
                .append("FROM Article a ")
                .append("JOIN Vendeur v ON a.id_vendeur = v.id_vendeur ")
                .append("JOIN Utilisateur u ON v.id_vendeur = u.id_utilisateur ")
                .append("WHERE a.etat != 'vendu' ");

        List<Object> parameters = new ArrayList<>();

        if (keyword != null && !keyword.isEmpty()) {
            sqlBuilder.append("AND (a.titre ILIKE ? OR a.description ILIKE ?) ");
            parameters.add("%" + keyword + "%");
            parameters.add("%" + keyword + "%");
        }

        if (category != null && !category.isEmpty()) {
            sqlBuilder.append("AND a.categorie = ? ");
            parameters.add(category);
        }

        if (state != null && !state.isEmpty()) {
            sqlBuilder.append("AND a.etat = ? ");
            parameters.add(state);
        }

        sqlBuilder.append("ORDER BY a.date_ajout DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }

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
                        rs.getString("vendeur_prenom") + " " + rs.getString("vendeur_nom")
                );
                vehicles.add(vehicle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vehicles;
    }
}