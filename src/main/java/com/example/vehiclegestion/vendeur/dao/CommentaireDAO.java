package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.vendeur.model.Commentaire;
import com.example.vehiclegestion.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireDAO {

    public List<Commentaire> getCommentairesByArticle(int idArticle) {
        List<Commentaire> commentaires = new ArrayList<>();

        // ✅ REQUÊTE avec les bonnes colonnes (sans statut)
        String query = "SELECT c.*, u.nom, u.prenom, u.role " +
                "FROM Commentaire c " +
                "JOIN Utilisateur u ON c.id_client = u.id_utilisateur " +
                "WHERE c.id_article = ? " +  // ✅ Supprimer: AND c.statut = 'actif'
                "ORDER BY c.date_commentaire DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idArticle);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setIdCommentaire(rs.getInt("id_commentaire"));
                c.setIdUtilisateur(rs.getInt("id_client"));
                c.setIdArticle(rs.getInt("id_article"));
                c.setNote(rs.getDouble("note"));
                c.setTexteCommentaire(rs.getString("texte_commentaire"));

                Timestamp timestamp = rs.getTimestamp("date_commentaire");
                if (timestamp != null) {
                    c.setDateCommentaire(timestamp.toLocalDateTime());
                }

                String prenom = rs.getString("prenom");
                String nom = rs.getString("nom");
                String role = rs.getString("role");
                String roleEmoji = getRoleEmoji(role);
                String nomComplet = prenom + " " + nom + " " + roleEmoji;
                c.setNomClient(nomComplet);

                commentaires.add(c);
            }

            System.out.println("📋 Commentaires récupérés: " + commentaires.size());

        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL dans getCommentairesByArticle: " + e.getMessage());
            e.printStackTrace();
        }

        return commentaires;
    }
    public boolean ajouterCommentaire(int idUtilisateur, int idArticle, double note, String texte) {
        System.out.println("\n📝 === DEBUG AJOUT COMMENTAIRE (DAO) ===");
        System.out.println("   👤 ID Utilisateur: " + idUtilisateur);
        System.out.println("   📄 ID Article: " + idArticle);
        System.out.println("   ⭐ Note: " + note);
        System.out.println("   📝 Texte: " + texte);

        // Validation
        if (idUtilisateur <= 0 || idArticle <= 0 || texte == null || texte.trim().isEmpty()) {
            System.err.println("❌ Données invalides");
            return false;
        }

        // ✅ CORRECTION: Utiliser les VRAIES colonnes de votre table
        String query = "INSERT INTO Commentaire (id_client, id_article, note, texte_commentaire) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            if (conn == null) {
                System.err.println("❌ Connexion DB NULL");
                return false;
            }

            stmt.setInt(1, idUtilisateur);
            stmt.setInt(2, idArticle);
            stmt.setDouble(3, note);
            stmt.setString(4, texte);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("   📊 Lignes affectées: " + rowsAffected);

            boolean success = rowsAffected > 0;
            if (success) {
                System.out.println("✅ Commentaire ajouté avec succès !");
            } else {
                System.err.println("❌ Aucune ligne affectée");
            }

            return success;

        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL dans ajouterCommentaire: " + e.getMessage());
            System.err.println("   Code erreur: " + e.getErrorCode());
            System.err.println("   État SQL: " + e.getSQLState());
            e.printStackTrace();
            return false;
        }
    }

    public double getNoteMoyenne(int idArticle) {
        String query = "SELECT AVG(note) as avg_note FROM Commentaire WHERE id_article = ?"; // ✅ Supprimer statut

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idArticle);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double avg = rs.getDouble("avg_note");
                System.out.println("📊 Note moyenne article " + idArticle + ": " + avg);
                return rs.wasNull() ? 0.0 : avg;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur calcul note moyenne: " + e.getMessage());
            e.printStackTrace();
        }

        return 0.0;
    }

    public int getNombreCommentaires(int idArticle) {
        String query = "SELECT COUNT(*) as total FROM Commentaire WHERE id_article = ?"; // ✅ Supprimer statut

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idArticle);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("📊 Nombre commentaires article " + idArticle + ": " + total);
                return total;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage commentaires: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    public boolean aDejaCommente(int idUtilisateur, int idArticle) {
        String query = "SELECT COUNT(*) as total FROM Commentaire " +
                "WHERE id_client = ? AND id_article = ?"; // ✅ Supprimer: AND statut = 'actif'

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idUtilisateur);
            stmt.setInt(2, idArticle);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                System.out.println("📊 Commentaires existants: " + total);
                return total > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ ERREUR dans aDejaCommente: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }


    /**
     * Récupère le commentaire spécifique d'un utilisateur sur un article
     */
    public Commentaire getCommentaireUtilisateur(int idUtilisateur, int idArticle) {
        String query = "SELECT c.*, u.nom, u.prenom, u.role " +
                "FROM Commentaire c " +
                "JOIN Utilisateur u ON c.id_client = u.id_utilisateur " +
                "WHERE c.id_client = ? AND c.id_article = ? " +
                "ORDER BY c.date_commentaire DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idUtilisateur);
            stmt.setInt(2, idArticle);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Commentaire c = new Commentaire();
                c.setIdCommentaire(rs.getInt("id_commentaire"));
                c.setIdUtilisateur(rs.getInt("id_client"));
                c.setIdArticle(rs.getInt("id_article"));
                c.setNote(rs.getDouble("note"));
                c.setTexteCommentaire(rs.getString("texte_commentaire"));

                Timestamp timestamp = rs.getTimestamp("date_commentaire");
                if (timestamp != null) {
                    c.setDateCommentaire(timestamp.toLocalDateTime());
                }

                String prenom = rs.getString("prenom");
                String nom = rs.getString("nom");
                String role = rs.getString("role");
                String roleEmoji = getRoleEmoji(role);
                String nomComplet = prenom + " " + nom + " " + roleEmoji;
                c.setNomClient(nomComplet);

                System.out.println("✅ Commentaire utilisateur trouvé: " + nomComplet);
                return c;
            }

        } catch (SQLException e) {
            System.err.println("❌ ERREUR dans getCommentaireUtilisateur: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }


    private String getRoleEmoji(String role) {
        if (role == null) return "👤";
        switch (role.toLowerCase()) {
            case "client": return "👤";
            case "vendeur": return "🏪";
            case "admin": return "⭐";
            default: return "👤";
        }
    }
}