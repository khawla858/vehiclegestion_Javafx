package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.common.dao.DatabaseConnection;
import com.example.vehiclegestion.vendeur.model.Article;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;

public class ArticleDAO {

    private Connection connection;

    public ArticleDAO() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Article> getArticlesByVendeur(int utilisateurId) throws SQLException {
        System.out.println("\n🔍 === ArticleDAO.getArticlesByVendeur ===");
        System.out.println("   - Utilisateur ID reçu: " + utilisateurId);

        // ✅ CORRECTION : Récupérer id_vendeur depuis la table Vendeur
        int vendeurId = 0;
        String getVendeurId = "SELECT id_vendeur FROM Vendeur WHERE id_vendeur = ?";

        try (PreparedStatement ps = connection.prepareStatement(getVendeurId)) {
            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    vendeurId = rs.getInt("id_vendeur");
                    System.out.println("   ✅ ID Vendeur trouvé: " + vendeurId);
                } else {
                    System.err.println("   ❌ Aucun vendeur trouvé avec id_vendeur = " + utilisateurId);
                    System.err.println("   ⚠️ Cet utilisateur n'existe pas dans la table Vendeur");
                    return new ArrayList<>();
                }
            }
        }

        List<Article> articles = new ArrayList<>();
        String query = "SELECT * FROM Article WHERE id_vendeur = ? ORDER BY date_ajout DESC";

        System.out.println("   - Requête SQL: " + query);
        System.out.println("   - Recherche pour id_vendeur: " + vendeurId);

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, vendeurId);

            System.out.println("   - Exécution de la requête...");

            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
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

                    if (count <= 3) {
                        System.out.println("   ✅ Article #" + count + ": " + article.getTitre() +
                                " (ID: " + article.getId() + ", Prix: " + article.getPrix() + " DH)");
                    }
                }

                System.out.println("   - Total articles trouvés: " + count);

                if (count == 0) {
                    System.out.println("   ⚠️ Aucun article trouvé pour ce vendeur");
                    System.out.println("   💡 Vérifiez avec: SELECT * FROM Article WHERE id_vendeur = " + vendeurId + ";");
                }
            }
        } catch (SQLException e) {
            System.err.println("   ❌ Erreur SQL: " + e.getMessage());
            throw e;
        }

        System.out.println("🔍 === Fin getArticlesByVendeur ===\n");
        return articles;
    }


    public boolean addArticle(Article article, int utilisateurId) throws SQLException {
        System.out.println("\n💾 === ArticleDAO.addArticle ===");
        System.out.println("   - Titre: " + article.getTitre());
        System.out.println("   - Prix: " + article.getPrix() + " DH");
        System.out.println("   - Magasin ID: " + article.getIdMagasin());

        // ✅ CORRECTION: Récupérer id_vendeur depuis la table Vendeur
        int vendeurId = 0;
        String getVendeurId = "SELECT id_vendeur FROM Vendeur WHERE id_vendeur = ?";

        try (PreparedStatement ps = connection.prepareStatement(getVendeurId)) {
            ps.setInt(1, utilisateurId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    vendeurId = rs.getInt("id_vendeur");
                    System.out.println("   ✅ ID Vendeur trouvé: " + vendeurId);
                } else {
                    System.err.println("   ❌ Aucun vendeur trouvé avec id_vendeur = " + utilisateurId);
                    return false;
                }
            }
        }

        // ✅ CORRECTION: Requête SQL complète et correcte
        String sql = "INSERT INTO Article " +
                "(titre, description, prix, prix_promo, reduction, categorie, etat, image, " +
                "id_vendeur, marque, modele, annee, kilometrage, transmission, carburant, puissance, couleur, id_magasin) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, article.getTitre());
            stmt.setString(2, article.getDescription());
            stmt.setDouble(3, article.getPrix());
            stmt.setDouble(4, article.getPrixPromo());
            stmt.setInt(5, article.getReduction());
            stmt.setString(6, article.getCategorie());
            stmt.setString(7, article.getEtat());
            stmt.setString(8, article.getImage());
            stmt.setInt(9, vendeurId); // ✅ id_vendeur
            stmt.setString(10, article.getMarque());
            stmt.setString(11, article.getModele());
            stmt.setInt(12, article.getAnnee());
            stmt.setInt(13, article.getKilometrage());
            stmt.setString(14, article.getTransmission());
            stmt.setString(15, article.getCarburant());
            stmt.setInt(16, article.getPuissance());
            stmt.setString(17, article.getCouleur());
            stmt.setObject(18, article.getIdMagasin()); // ✅ id_magasin

            System.out.println("   - Exécution de l'insertion...");
            int rowsInserted = stmt.executeUpdate();

            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        System.out.println("   ✅ Article inséré avec succès!");
                        System.out.println("   - ID article généré: " + newId);
                        System.out.println("   - ID vendeur: " + vendeurId);
                        System.out.println("   - ID magasin: " + article.getIdMagasin());
                    }
                }
                return true;
            } else {
                System.out.println("   ❌ Aucune ligne insérée!");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("   ❌ Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

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
                // Java 8: remplacer Path.of par Paths.get
                Path p = Paths.get(System.getProperty("user.dir"), imagePath);
                try { Files.deleteIfExists(p); } catch (IOException ex) { ex.printStackTrace(); }
            }
            return ok;
        }
    }

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
    public List<Article> getArticlesByMagasin(int idMagasin) throws SQLException {
        System.out.println("\n🔍 === ArticleDAO.getArticlesByMagasin ===");
        System.out.println("   - Magasin ID: " + idMagasin);

        List<Article> articles = new ArrayList<>();

        // ✅ REQUÊTE: Récupérer les articles du magasin spécifique
        String query = "SELECT * FROM Article WHERE id_magasin = ? ORDER BY date_ajout DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, idMagasin);

            System.out.println("   - Requête SQL: " + query);
            System.out.println("   - Paramètre: id_magasin = " + idMagasin);

            try (ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
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
                    // ✅ SET MAGASIN ID
                    article.setIdMagasin(rs.getInt("id_magasin"));
                    articles.add(article);

                    if (count <= 3) {
                        System.out.println("   ✅ Article #" + count + ": " + article.getTitre() +
                                " (Magasin ID: " + article.getIdMagasin() + ")");
                    }
                }
                System.out.println("   - Total articles trouvés: " + count);
            }
        } catch (SQLException e) {
            System.err.println("   ❌ Erreur SQL: " + e.getMessage());
            throw e;
        }

        System.out.println("🔍 === Fin getArticlesByMagasin ===\n");
        return articles;
    }

}
