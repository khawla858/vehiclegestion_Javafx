package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.auth.utils.PasswordUtils;
import com.example.vehiclegestion.common.dao.DatabaseConnection;

import java.sql.*;

public class UtilisateurDAO {

    /**
     * Inscrit un nouvel utilisateur
     */
    public boolean inscrire(Utilisateur user, String password, String telephone) {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, telephone, mot_de_passe, role, statut) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, telephone); // ✅ Ajout du téléphone
            stmt.setString(5, PasswordUtils.hashPassword(password));
            stmt.setString(6, user.getRole());
            stmt.setString(7, "actif");

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Récupérer l'ID généré
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);

                    // Créer l'entrée dans la table spécifique selon le rôle
                    if ("vendeur".equals(user.getRole())) {
                        creerVendeur(userId);
                    } else if ("client".equals(user.getRole())) {
                        creerClient(userId);
                    }
                }
                System.out.println("✅ Utilisateur inséré avec succès dans la base de données");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Connexion d'un utilisateur
     */
    public Utilisateur seConnecter(String email, String password) {
        String sql = "SELECT * FROM utilisateur WHERE email = ? AND statut = 'actif'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("mot_de_passe");

                if (PasswordUtils.verifyPassword(password, hashedPassword)) {
                    Utilisateur user = new Utilisateur();
                    user.setIdUtilisateur(rs.getInt("id_utilisateur"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setTelephone(rs.getString("telephone")); // ✅ Récupérer téléphone
                    user.setRole(rs.getString("role"));
                    user.setStatut(rs.getString("statut"));
                    return user;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la connexion: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer un utilisateur par ID
     */
    public Utilisateur getUtilisateurById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Utilisateur user = new Utilisateur();
                user.setIdUtilisateur(rs.getInt("id_utilisateur"));
                user.setNom(rs.getString("nom"));
                user.setPrenom(rs.getString("prenom"));
                user.setEmail(rs.getString("email"));
                user.setTelephone(rs.getString("telephone")); // ✅ Récupérer téléphone
                user.setRole(rs.getString("role"));
                user.setStatut(rs.getString("statut"));
                return user;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void creerVendeur(int userId) {
        String sql = "INSERT INTO vendeur (id_vendeur, statut_vendeur) VALUES (?, 'actif')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println("✅ Vendeur créé avec ID: " + userId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur création vendeur: " + e.getMessage());
        }
    }

    private void creerClient(int userId) {
        String sql = "INSERT INTO client (id_client, statut_client) VALUES (?, 'actif')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println("✅ Client créé avec ID: " + userId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur création client: " + e.getMessage());
        }
    }

    /**
     * Mettre à jour le téléphone d'un utilisateur
     */
    public boolean updateTelephone(int userId, String telephone) {
        String sql = "UPDATE utilisateur SET telephone = ? WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, telephone);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("📱 Téléphone mis à jour pour utilisateur ID: " + userId);
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour téléphone: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


}