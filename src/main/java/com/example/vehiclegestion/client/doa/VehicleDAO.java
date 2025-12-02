package com.example.vehiclegestion.client.doa;

import com.example.vehiclegestion.client.model.Vehicle;
import com.example.vehiclegestion.common.dao.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
public class VehicleDAO {

    public List<Vehicle> getAllVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
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
                        rs.getString("statut_vehicule"),                        rs.getString("image"),
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

    // Ajouter un véhicule aux favoris
    public boolean addToFavorites(int clientId, int vehicleId) {
        String sql = "INSERT INTO Favoris (id_client, id_article) VALUES (?, ?) " +
                "ON CONFLICT (id_client, id_article) DO NOTHING";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, vehicleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur ajout favori: " + e.getMessage());
            return false;
        }
    }

    // Retirer des favoris
    public boolean removeFromFavorites(int clientId, int vehicleId) {
        String sql = "DELETE FROM Favoris WHERE id_client = ? AND id_article = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, vehicleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression favori: " + e.getMessage());
            return false;
        }
    }

    // Vérifier si un véhicule est dans les favoris
    public boolean isFavorite(int clientId, int vehicleId) {
        String sql = "SELECT 1 FROM Favoris WHERE id_client = ? AND id_article = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, vehicleId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification favori: " + e.getMessage());
            return false;
        }
    }

    // Récupérer tous les favoris d'un client
    public List<Vehicle> getFavorites(int clientId) {
        List<Vehicle> favorites = new ArrayList<>();
        String sql = "SELECT a.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom " +
                "FROM Article a " +
                "JOIN Vendeur v ON a.id_vendeur = v.id_vendeur " +
                "JOIN Utilisateur u ON v.id_vendeur = u.id_utilisateur " +
                "JOIN Favoris f ON a.id_article = f.id_article " +
                "WHERE f.id_client = ? " +
                "ORDER BY f.date_ajout DESC";

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
                favorites.add(vehicle);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération favoris: " + e.getMessage());
        }
        return favorites;
    }

    // Récupérer les véhicules par vendeur
    public List<Vehicle> getVehiclesBySeller(int sellerId) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT a.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom " +
                "FROM Article a " +
                "JOIN Vendeur v ON a.id_vendeur = v.id_vendeur " +
                "JOIN Utilisateur u ON v.id_vendeur = u.id_utilisateur " +
                "WHERE a.id_vendeur = ? AND a.etat != 'vendu' " +
                "ORDER BY a.date_ajout DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sellerId);
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

    // Récupérer les véhicules par catégorie
    public List<Vehicle> getVehiclesByCategory(String category) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT a.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom " +
                "FROM Article a " +
                "JOIN Vendeur v ON a.id_vendeur = v.id_vendeur " +
                "JOIN Utilisateur u ON v.id_vendeur = u.id_utilisateur " +
                "WHERE a.categorie = ? AND a.etat != 'vendu' " +
                "ORDER BY a.date_ajout DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
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

    // Récupérer les véhicules par état
    public List<Vehicle> getVehiclesByState(String state) {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT a.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom " +
                "FROM Article a " +
                "JOIN Vendeur v ON a.id_vendeur = v.id_vendeur " +
                "JOIN Utilisateur u ON v.id_vendeur = u.id_utilisateur " +
                "WHERE a.etat = ? " +
                "ORDER BY a.date_ajout DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, state);
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

    // Récupérer un véhicule par son ID
    public Vehicle getVehicleById(int vehicleId) {
        String sql = "SELECT a.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom " +
                "FROM Article a " +
                "JOIN Vendeur v ON a.id_vendeur = v.id_vendeur " +
                "JOIN Utilisateur u ON v.id_vendeur = u.id_utilisateur " +
                "WHERE a.id_article = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vehicleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Vehicle(
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
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Mettre à jour le statut d'un véhicule
    public boolean updateVehicleStatus(int vehicleId, String newStatus) {
        String sql = "UPDATE Article SET etat = ? WHERE id_article = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, vehicleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour statut véhicule: " + e.getMessage());
            return false;
        }
    }

    // Récupérer les véhicules récents (derniers 7 jours)
    public List<Vehicle> getRecentVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        String sql = "SELECT a.*, u.nom as vendeur_nom, u.prenom as vendeur_prenom " +
                "FROM Article a " +
                "JOIN Vendeur v ON a.id_vendeur = v.id_vendeur " +
                "JOIN Utilisateur u ON v.id_vendeur = u.id_utilisateur " +
                "WHERE a.date_ajout >= CURRENT_DATE - INTERVAL '7 days' AND a.etat != 'vendu' " +
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

    // Récupérer les statistiques des véhicules
    public Map<String, Integer> getVehicleStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT etat, COUNT(*) as count FROM Article GROUP BY etat";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stats.put(rs.getString("etat"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }

    // Recherche avancée avec plusieurs critères
    public List<Vehicle> advancedSearch(String keyword, String category, Double minPrice, Double maxPrice, String state) {
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

        if (minPrice != null) {
            sqlBuilder.append("AND a.prix >= ? ");
            parameters.add(minPrice);
        }

        if (maxPrice != null) {
            sqlBuilder.append("AND a.prix <= ? ");
            parameters.add(maxPrice);
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