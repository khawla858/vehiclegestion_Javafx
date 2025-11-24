package com.example.vehiclegestion.auth.dao;

import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.auth.utils.PasswordUtils;
import com.example.vehiclegestion.common.dao.DatabaseConnection;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class UtilisateurDAO {

    /**
     * Inscrit un nouvel utilisateur
     */
    public boolean inscrire(Utilisateur user) {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role, statut) VALUES (?, ?, ?, ?, ?, 'actif')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, PasswordUtils.hashPassword(user.getMotDePasse()));
            stmt.setString(5, user.getRole());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                // Récupérer l'ID généré
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);

                    // Si c'est un vendeur ou client, créer l'entrée correspondante
                    if (user.getRole().equals("vendeur")) {
                        creerVendeur(userId);
                    } else if (user.getRole().equals("client")) {
                        creerClient(userId);
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Crée une entrée vendeur
     */
    private void creerVendeur(int userId) {
        String sql = "INSERT INTO vendeur (id_vendeur, statut_vendeur) VALUES (?, 'actif')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Crée une entrée client
     */
    private void creerClient(int userId) {
        String sql = "INSERT INTO client (id_client, statut_client) VALUES (?, 'actif')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

                // Vérifier le mot de passe
                if (PasswordUtils.verifyPassword(password, hashedPassword)) {
                    Utilisateur user = new Utilisateur();
                    user.setIdUtilisateur(rs.getInt("id_utilisateur"));
                    user.setNom(rs.getString("nom"));
                    user.setPrenom(rs.getString("prenom"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setStatut(rs.getString("statut"));
                    return user;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Vérifie si un email existe déjà
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM utilisateur WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}