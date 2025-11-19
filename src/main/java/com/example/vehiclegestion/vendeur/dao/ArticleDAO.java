package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.common.dao.DatabaseConnection;
import com.example.vehiclegestion.vendeur.model.Article;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ArticleDAO {

    private Connection connection;

    public ArticleDAO() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Récupérer tous les articles d’un vendeur
    public List<Article> getArticlesByVendeur(int vendeurId) throws SQLException {
        List<Article> articles = new ArrayList<>();
        String query = "SELECT * FROM Article WHERE id_vendeur = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, vendeurId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Article article = new Article(
                            rs.getInt("id_article"),
                            rs.getString("titre"),
                            rs.getString("description"),
                            rs.getDouble("prix"),
                            rs.getDouble("prix_promo"),
                            rs.getInt("reduction"),
                            rs.getString("categorie"),
                            rs.getString("etat"),
                            rs.getString("image"),
                            rs.getInt("id_vendeur"),
                            rs.getString("marque"),
                            rs.getString("modele"),
                            rs.getInt("annee"),
                            rs.getInt("kilometrage"),
                            rs.getString("transmission"),
                            rs.getString("carburant"),
                            rs.getInt("puissance"),
                            rs.getString("couleur")
                    );

                    articles.add(article);
                }
            }
        }
        return articles;
    }

    // Ajouter un article/////////////////////////////////////////////////////////////
    public boolean addArticle(Article article, int vendeurId) throws SQLException {
        String sql = "INSERT INTO Article " +
                "(titre, description, prix, prix_promo, reduction, categorie, etat, image, id_vendeur, " +
                "marque, modele, annee, kilometrage, transmission, carburant, puissance, couleur) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, article.getTitre());
            stmt.setString(2, article.getDescription());
            stmt.setDouble(3, article.getPrix());
            stmt.setDouble(4, article.getPrixPromo());
            stmt.setInt(5, article.getReduction());
            stmt.setString(6, article.getCategorie());
            stmt.setString(7, article.getEtat());
            stmt.setString(8, article.getImage());
            stmt.setInt(9, vendeurId);
            stmt.setString(10, article.getMarque());
            stmt.setString(11, article.getModele());
            stmt.setInt(12, article.getAnnee());
            stmt.setInt(13, article.getKilometrage());
            stmt.setString(14, article.getTransmission());
            stmt.setString(15, article.getCarburant());
            stmt.setInt(16, article.getPuissance());
            stmt.setString(17, article.getCouleur());

            return stmt.executeUpdate() > 0;
        }
    }


    // Supprimer un article/////////////////////////////////////////////////////////////////////////
    public boolean deleteArticle(int articleId) throws SQLException {
        // récupérer le chemin image
        String select = "SELECT image FROM Article WHERE id_article = ?";
        String imagePath = null;
        try (PreparedStatement ps = connection.prepareStatement(select)) {
            ps.setInt(1, articleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) imagePath = rs.getString("image");
            }
        }

        // supprimer la ligne
        String delete = "DELETE FROM Article WHERE id_article = ?";
        try (PreparedStatement ps = connection.prepareStatement(delete)) {
            ps.setInt(1, articleId);
            boolean ok = ps.executeUpdate() > 0;
            if (ok && imagePath != null) {
                // supprimer le fichier physique
                Path p = Path.of(System.getProperty("user.dir")).resolve(imagePath);
                try { Files.deleteIfExists(p); } catch (Exception ex) { ex.printStackTrace(); }
            }
            return ok;
        }
    }


    // Mettre à jour un article////////////////////////////////////////////////////////////////////////
    public boolean updateArticle(Article article) throws SQLException {
        String sql = "UPDATE Article SET titre=?, description=?, prix=?, prix_promo=?, reduction=?, categorie=?, etat=?, image=?, " +
                "marque=?, modele=?, annee=?, kilometrage=?, transmission=?, carburant=?, puissance=?, couleur=? " +
                "WHERE id_article=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, article.getTitre());
            stmt.setString(2, article.getDescription());
            stmt.setDouble(3, article.getPrix());
            stmt.setDouble(4, article.getPrixPromo());
            stmt.setInt(5, article.getReduction());
            stmt.setString(6, article.getCategorie());
            stmt.setString(7, article.getEtat());
            stmt.setString(8, article.getImage());
            stmt.setString(9, article.getMarque());
            stmt.setString(10, article.getModele());
            stmt.setInt(11, article.getAnnee());
            stmt.setInt(12, article.getKilometrage());
            stmt.setString(13, article.getTransmission());
            stmt.setString(14, article.getCarburant());
            stmt.setInt(15, article.getPuissance());
            stmt.setString(16, article.getCouleur());
            stmt.setInt(17, article.getId());

            return stmt.executeUpdate() > 0;
        }
    }
}
