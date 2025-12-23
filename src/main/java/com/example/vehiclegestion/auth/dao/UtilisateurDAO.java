package com.example.vehiclegestion.auth.dao;

import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.auth.utils.PasswordUtils;
import com.example.vehiclegestion.common.dao.DatabaseConnection;

import java.sql.*;

/**
 * DAO pour la gestion des utilisateurs avec logs intégrés
 */
public class UtilisateurDAO {

    // ========== INSCRIPTION ==========

    /**
     * Inscrit un nouvel utilisateur
     * IMPORTANT: Le mot de passe dans l'objet user DOIT être en clair,
     * cette méthode s'occupe de le hasher
     */
    public boolean inscrire(Utilisateur user) {
        String sql = "INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role, statut) VALUES (?, ?, ?, ?, ?, 'actif')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());

            // Hash du mot de passe (s'il n'est pas déjà hashé)
            String motDePasse = user.getMotDePasse();
            if (motDePasse != null && !motDePasse.isEmpty()) {
                // Vérifier si c'est déjà un hash (longueur 64 pour SHA-256)
                if (motDePasse.length() != 64) {
                    motDePasse = PasswordUtils.hashPassword(motDePasse);
                }
            }
            stmt.setString(4, motDePasse);
            stmt.setString(5, user.getRole());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                // Récupérer l'ID généré
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    user.setIdUtilisateur(userId); // Important : Mettre à jour l'ID

                    // Si c'est un vendeur ou client, créer l'entrée correspondante
                    if ("vendeur".equals(user.getRole())) {
                        creerVendeur(userId);
                    } else if ("client".equals(user.getRole())) {
                        creerClient(userId);
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }



    // ========== CONNEXION ==========

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
                    return mapResultSetToUtilisateur(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la connexion: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ========== RECHERCHE ==========

    /**
     * Trouver un utilisateur par email
     * ✅ NOUVELLE MÉTHODE AJOUTÉE
     */
    public Utilisateur findByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche par email: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Trouver un utilisateur par ID
     * ✅ NOUVELLE MÉTHODE AJOUTÉE
     */
    public Utilisateur findById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE id_utilisateur = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la recherche par ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // ========== VÉRIFICATIONS ==========

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
            System.err.println("❌ Erreur lors de la vérification email: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ========== MISE À JOUR ==========

    /**
     * Mettre à jour le mot de passe d'un utilisateur
     * ✅ NOUVELLE MÉTHODE AJOUTÉE
     * IMPORTANT: Le nouveauMotDePasse peut être en clair ou déjà hashé
     * La méthode détecte et hash si nécessaire
     */
    public boolean updatePassword(String email, String nouveauMotDePasse) {
        String sql = "UPDATE utilisateur SET mot_de_passe = ? WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Hash le mot de passe s'il n'est pas déjà hashé
            String motDePasseHash = nouveauMotDePasse;
            if (nouveauMotDePasse != null && nouveauMotDePasse.length() != 64) {
                motDePasseHash = PasswordUtils.hashPassword(nouveauMotDePasse);
            }

            stmt.setString(1, motDePasseHash);
            stmt.setString(2, email);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du mot de passe: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mettre à jour le statut d'un utilisateur
     * ✅ NOUVELLE MÉTHODE AJOUTÉE
     */
    public boolean updateStatut(String email, String statut) {
        String sql = "UPDATE utilisateur SET statut = ? WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statut);
            stmt.setString(2, email);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Bloquer un utilisateur
     * ✅ NOUVELLE MÉTHODE AJOUTÉE
     */
    public boolean bloquerUtilisateur(String email) {
        return updateStatut(email, "bloque");
    }

    /**
     * Débloquer un utilisateur
     * ✅ NOUVELLE MÉTHODE AJOUTÉE
     */
    public boolean debloquerUtilisateur(String email) {
        return updateStatut(email, "actif");
    }

    // ========== SUPPRESSION ==========

    /**
     * Supprimer un utilisateur (soft delete)
     * ✅ NOUVELLE MÉTHODE AJOUTÉE
     */
    public boolean supprimerUtilisateur(String email) {
        return updateStatut(email, "supprime");
    }

    // ========== MÉTHODES PRIVÉES ==========

    /**
     * Crée une entrée vendeur
     */
    private void creerVendeur(int userId) {
        String sql = "INSERT INTO vendeur (id_vendeur, statut_vendeur) VALUES (?, 'actif')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            System.out.println("✅ Entrée vendeur créée pour l'utilisateur ID: " + userId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création du vendeur: " + e.getMessage());
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
            System.out.println("✅ Entrée client créée pour l'utilisateur ID: " + userId);
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création du client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Mapper un ResultSet vers un objet Utilisateur
     * ✅ NOUVELLE MÉTHODE UTILITAIRE
     */
    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur user = new Utilisateur();
        user.setIdUtilisateur(rs.getInt("id_utilisateur"));
        user.setNom(rs.getString("nom"));
        user.setPrenom(rs.getString("prenom"));
        user.setEmail(rs.getString("email"));
        user.setMotDePasse(rs.getString("mot_de_passe")); // Hash stocké
        user.setRole(rs.getString("role"));
        user.setStatut(rs.getString("statut"));

        // Récupérer dateCreation si la colonne existe
        try {
            Timestamp timestamp = rs.getTimestamp("date_creation");
            if (timestamp != null) {
                user.setDateCreation(timestamp.toLocalDateTime());
            }
        } catch (SQLException e) {
            // Colonne date_creation n'existe pas, ignorer
        }

        return user;
    }
}