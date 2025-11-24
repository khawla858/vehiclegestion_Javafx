package com.example.vehiclegestion.client.doa;

import com.example.vehiclegestion.client.model.Vehicle;
import com.example.vehiclegestion.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDAO {

    /**
     * Ajouter un véhicule aux favoris
     */
    public boolean addFavorite(int idClient, int idArticle) {
        String query = "INSERT INTO favorites (id_client, id_article) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idClient);
            stmt.setInt(2, idArticle);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Favori ajouté: Client " + idClient + ", Article " + idArticle);
            return rowsAffected > 0;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("ℹ️ Ce véhicule est déjà dans les favoris");
            return false;
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout aux favoris: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retirer un véhicule des favoris
     */
    public boolean removeFavorite(int idClient, int idArticle) {
        String query = "DELETE FROM favorites WHERE id_client = ? AND id_article = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idClient);
            stmt.setInt(2, idArticle);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Favori retiré: Client " + idClient + ", Article " + idArticle);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression du favori: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifier si un véhicule est dans les favoris
     */
    public boolean isFavorite(int idClient, int idArticle) {
        String query = "SELECT COUNT(*) FROM favorites WHERE id_client = ? AND id_article = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idClient);
            stmt.setInt(2, idArticle);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la vérification du favori: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Récupérer tous les favoris d'un client avec les détails des véhicules
     */
    public List<Vehicle> getFavoriteVehicles(int idClient) {
        List<Vehicle> favorites = new ArrayList<>();

        String query = "SELECT a.*, " +
                "v.nom_magasin, " +
                "u.nom, u.prenom, u.email, " +
                "f.date_added as favorite_date " +
                "FROM favorites f " +
                "INNER JOIN article a ON f.id_article = a.id_article " +
                "LEFT JOIN vendeur v ON a.id_vendeur = v.id_vendeur " +
                "LEFT JOIN utilisateur u ON a.id_vendeur = u.id_utilisateur " +
                "WHERE f.id_client = ? " +
                "ORDER BY f.date_added DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idClient);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vehicle vehicle = new Vehicle();

                // Informations de l'article
                vehicle.setId(rs.getInt("id_article"));
                vehicle.setTitle(rs.getString("titre"));
                vehicle.setDescription(rs.getString("description"));
                vehicle.setPrice(rs.getDouble("prix"));
                vehicle.setCategory(rs.getString("categorie"));
                vehicle.setImage(rs.getString("image"));

                // Informations du vendeur (nom du magasin)
                String nomMagasin = rs.getString("nom_magasin");
                vehicle.setSellerName(nomMagasin != null ? nomMagasin : "Vendeur");

                // Informations de l'utilisateur vendeur
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String email = rs.getString("email");

                // Si pas de nom de magasin, utiliser le nom et prénom
                if (nomMagasin == null && nom != null && prenom != null) {
                    vehicle.setSellerName(nom + " " + prenom);
                }

                // Si votre modèle Vehicle a ces champs, décommentez ces lignes
                // vehicle.setSellerEmail(email != null ? email : "");
                // vehicle.setSellerPhone("");

                // Date d'ajout aux favoris
                Timestamp dateAdded = rs.getTimestamp("favorite_date");
                if (dateAdded != null) {
                    vehicle.setDateAdded(dateAdded.toLocalDateTime());
                }

                favorites.add(vehicle);
            }

            System.out.println("✅ " + favorites.size() + " favoris récupérés pour le client " + idClient);

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la récupération des favoris: " + e.getMessage());
            e.printStackTrace();
        }

        return favorites;
    }

    /**
     * Récupérer le nombre de favoris d'un client
     */
    public int getFavoriteCount(int idClient) {
        String query = "SELECT COUNT(*) FROM favorites WHERE id_client = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idClient);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors du comptage des favoris: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Supprimer tous les favoris d'un client
     */
    public boolean clearAllFavorites(int idClient) {
        String query = "DELETE FROM favorites WHERE id_client = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idClient);
            int rowsAffected = stmt.executeUpdate();

            System.out.println("✅ " + rowsAffected + " favoris supprimés pour le client " + idClient);
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression des favoris: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}