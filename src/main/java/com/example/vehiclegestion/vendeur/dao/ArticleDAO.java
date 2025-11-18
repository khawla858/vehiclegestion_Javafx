package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.common.dao.DatabaseConnection;
import com.example.vehiclegestion.vendeur.model.Article;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArticleDAO {

    private Connection connection;

    public ArticleDAO() {
        try {
            // ✅ Utilise la classe commune pour obtenir la connexion
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Récupérer tous les articles d'un vendeur
    public List<Article> getArticlesByVendeur(int vendeurId) throws SQLException {
        List<Article> articles = new ArrayList<>();
        String query = "SELECT * FROM Article WHERE id_vendeur = ?";

        System.out.println("🔍 Recherche articles pour vendeur ID: " + vendeurId);

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, vendeurId);
            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    Article article = new Article();
                    article.setId(rs.getInt("id_article"));
                    article.setTitre(rs.getString("titre"));
                    article.setDescription(rs.getString("description"));
                    article.setPrix(rs.getDouble("prix"));
                    article.setCategorie(rs.getString("categorie"));
                    article.setEtat(rs.getString("etat"));
                    article.setImage(rs.getString("image"));
                    articles.add(article);
                    count++;

                    System.out.println("📄 Article " + count + ": " + article.getTitre() + " - " + article.getPrix() + " MAD");
                }
                System.out.println("✅ Total articles récupérés: " + count);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans getArticlesByVendeur: " + e.getMessage());
            throw e;
        }

        return articles;
    }

    // 🔹 Méthode pour ajouter un article
    public boolean addArticle(Article article, int vendeurId) throws SQLException {
        String query = "INSERT INTO Article (titre, description, prix, categorie, etat, image, id_vendeur) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, article.getTitre());
            stmt.setString(2, article.getDescription());
            stmt.setDouble(3, article.getPrix());
            stmt.setString(4, article.getCategorie());
            stmt.setString(5, article.getEtat());
            stmt.setString(6, article.getImage());
            stmt.setInt(7, vendeurId);

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Article ajouté - Lignes affectées: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL dans addArticle: " + e.getMessage());
            throw e;
        }
    }

    // 🔹 Méthode pour supprimer un article
    public boolean deleteArticle(int articleId) throws SQLException {
        String query = "DELETE FROM Article WHERE id_article = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, articleId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 🔹 Méthode pour mettre à jour un article
    public boolean updateArticle(Article article) throws SQLException {
        String query = "UPDATE Article SET titre = ?, description = ?, prix = ?, categorie = ?, etat = ?, image = ? WHERE id_article = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, article.getTitre());
            stmt.setString(2, article.getDescription());
            stmt.setDouble(3, article.getPrix());
            stmt.setString(4, article.getCategorie());
            stmt.setString(5, article.getEtat());
            stmt.setString(6, article.getImage());
            stmt.setInt(7, article.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 🔹 Méthode pour récupérer un article par son ID
    public Article getArticleById(int articleId) throws SQLException {
        String query = "SELECT * FROM Article WHERE id_article = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, articleId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Article article = new Article();
                    article.setId(rs.getInt("id_article"));
                    article.setTitre(rs.getString("titre"));
                    article.setDescription(rs.getString("description"));
                    article.setPrix(rs.getDouble("prix"));
                    article.setCategorie(rs.getString("categorie"));
                    article.setEtat(rs.getString("etat"));
                    article.setImage(rs.getString("image"));
                    return article;
                }
            }
        }
        return null;
    }

    // 🔹 Méthode pour compter les articles d'un vendeur
    public int countArticlesByVendeur(int vendeurId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Article WHERE id_vendeur = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, vendeurId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}