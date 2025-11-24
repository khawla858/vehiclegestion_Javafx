package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.vendeur.model.Magasin;
import com.example.vehiclegestion.common.dao.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MagasinDAO {

    private Connection connection;

    public MagasinDAO() {
        try {
            connection = DatabaseConnection.getConnection();
            System.out.println("✅ MagasinDAO: Connexion établie");

            // ✅ VÉRIFIER QUE LA CONNEXION EST VALIDE
            if (connection == null || connection.isClosed()) {
                System.err.println("❌ ERREUR: La connexion à la base de données est fermée!");
            }
        } catch (SQLException e) {
            System.err.println("❌ ERREUR: Impossible de se connecter à la base de données");
            e.printStackTrace();
        }
    }

    /**
     * ✅ MÉTHODE CORRIGÉE : Récupérer un magasin par ID vendeur avec logs détaillés
     */
    public Magasin getMagasinByVendeur(int idVendeur) {
        System.out.println("\n🔍 === getMagasinByVendeur ===");
        System.out.println("   Recherche pour vendeur ID: " + idVendeur);

        String sql = "SELECT * FROM Magasin WHERE id_vendeur = ? LIMIT 1";

        try {
            // ✅ VÉRIFIER LA CONNEXION
            if (connection == null || connection.isClosed()) {
                System.err.println("❌ ERREUR: Connexion fermée, tentative de reconnexion...");
                connection = DatabaseConnection.getConnection();
            }

            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, idVendeur);

            System.out.println("   📝 Requête SQL: " + sql);
            System.out.println("   📝 Paramètre: id_vendeur = " + idVendeur);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Magasin m = new Magasin();

                m.setIdMagasin(rs.getInt("id_magasin"));
                m.setNomMagasin(rs.getString("nom_magasin"));
                m.setAdresse(rs.getString("adresse"));
                m.setLocalisation(rs.getString("localisation"));
                m.setDescription(rs.getString("description"));
                m.setIdVendeur(rs.getInt("id_vendeur"));
                m.setNbVentesMensuelles(rs.getInt("nb_ventes_mensuelles"));

                // Nouveaux champs
                m.setLogoMagasin(rs.getString("logo_magasin"));
                m.setTelephone(rs.getString("telephone"));
                m.setEmailContact(rs.getString("email_contact"));
                m.setSiteWeb(rs.getString("site_web"));
                m.setFacebook(rs.getString("facebook"));
                m.setInstagram(rs.getString("instagram"));
                m.setCategorie(rs.getString("categorie"));

                System.out.println("   ✅ MAGASIN TROUVÉ:");
                System.out.println("      - ID Magasin: " + m.getIdMagasin());
                System.out.println("      - Nom: " + m.getNomMagasin());
                System.out.println("      - Adresse: " + m.getAdresse());
                System.out.println("      - ID Vendeur: " + m.getIdVendeur());
                System.out.println("      - Logo: " + m.getLogoMagasin());

                rs.close();
                ps.close();
                return m;
            } else {
                System.out.println("   ⚠️ AUCUN RÉSULTAT trouvé dans la base de données");
                System.out.println("   💡 Vérifiez avec: SELECT * FROM Magasin WHERE id_vendeur = " + idVendeur + ";");

                rs.close();
                ps.close();
            }
        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL dans getMagasinByVendeur:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   SQLState: " + e.getSQLState());
            System.err.println("   ErrorCode: " + e.getErrorCode());
            e.printStackTrace();
        }

        System.out.println("🔍 === Fin getMagasinByVendeur ===\n");
        return null;
    }

    /**
     * ✅ MÉTHODE CORRIGÉE : Ajouter un magasin avec logs détaillés
     */
    public void addMagasin(Magasin m) {
        System.out.println("\n💾 === addMagasin ===");
        System.out.println("   Ajout du magasin: " + m.getNomMagasin());
        System.out.println("   Pour le vendeur ID: " + m.getIdVendeur());

        String sql = "INSERT INTO Magasin (" +
                "nom_magasin, adresse, localisation, description, id_vendeur, " +
                "telephone, email_contact, site_web, facebook, instagram, categorie, logo_magasin, nb_ventes_mensuelles" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            // ✅ VÉRIFIER LA CONNEXION
            if (connection == null || connection.isClosed()) {
                System.err.println("❌ ERREUR: Connexion fermée, tentative de reconnexion...");
                connection = DatabaseConnection.getConnection();
            }

            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, m.getNomMagasin());
            stmt.setString(2, m.getAdresse());
            stmt.setString(3, m.getLocalisation());
            stmt.setString(4, m.getDescription());
            stmt.setInt(5, m.getIdVendeur());
            stmt.setString(6, m.getTelephone());
            stmt.setString(7, m.getEmailContact());
            stmt.setString(8, m.getSiteWeb());
            stmt.setString(9, m.getFacebook());
            stmt.setString(10, m.getInstagram());
            stmt.setString(11, m.getCategorie());
            stmt.setString(12, m.getLogoMagasin());
            stmt.setInt(13, m.getNbVentesMensuelles());

            System.out.println("   📝 Exécution de l'insertion...");
            int rowsAffected = stmt.executeUpdate();

            System.out.println("   ✅ Insertion réussie! Lignes affectées: " + rowsAffected);

            // ✅ RÉCUPÉRER L'ID GÉNÉRÉ
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int idGenere = generatedKeys.getInt(1);
                m.setIdMagasin(idGenere);
                System.out.println("   ✅ ID magasin généré: " + idGenere);
            }

            generatedKeys.close();
            stmt.close();

            // ✅ VÉRIFICATION IMMÉDIATE
            System.out.println("   🔍 Vérification de l'insertion...");
            Magasin verification = getMagasinByVendeur(m.getIdVendeur());
            if (verification != null) {
                System.out.println("   ✅ VÉRIFICATION OK: Le magasin est bien en base!");
            } else {
                System.err.println("   ❌ ERREUR: Le magasin n'a pas été trouvé après insertion!");
            }

        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL dans addMagasin:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   SQLState: " + e.getSQLState());
            System.err.println("   ErrorCode: " + e.getErrorCode());
            e.printStackTrace();
        }

        System.out.println("💾 === Fin addMagasin ===\n");
    }

    /**
     * Récupérer tous les magasins
     */
    public List<Magasin> getAllMagasins() {
        List<Magasin> magasins = new ArrayList<>();
        String sql = "SELECT * FROM Magasin";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Magasin m = new Magasin();
                m.setIdMagasin(rs.getInt("id_magasin"));
                m.setNomMagasin(rs.getString("nom_magasin"));
                m.setAdresse(rs.getString("adresse"));
                m.setLocalisation(rs.getString("localisation"));
                m.setDescription(rs.getString("description"));
                m.setIdVendeur(rs.getInt("id_vendeur"));
                m.setNbVentesMensuelles(rs.getInt("nb_ventes_mensuelles"));
                m.setLogoMagasin(rs.getString("logo_magasin"));
                m.setTelephone(rs.getString("telephone"));
                m.setEmailContact(rs.getString("email_contact"));
                m.setSiteWeb(rs.getString("site_web"));
                m.setFacebook(rs.getString("facebook"));
                m.setInstagram(rs.getString("instagram"));
                m.setCategorie(rs.getString("categorie"));

                magasins.add(m);
            }

            System.out.println("✅ getAllMagasins: " + magasins.size() + " magasin(s) trouvé(s)");
        } catch (SQLException e) {
            System.err.println("❌ ERREUR dans getAllMagasins: " + e.getMessage());
            e.printStackTrace();
        }
        return magasins;
    }

    /**
     * Supprimer un magasin
     */
    public boolean deleteMagasin(int idMagasin) {
        System.out.println("🗑️ Suppression du magasin ID: " + idMagasin);
        String sql = "DELETE FROM Magasin WHERE id_magasin = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idMagasin);
            int rows = ps.executeUpdate();
            System.out.println(rows > 0 ? "✅ Magasin supprimé" : "⚠️ Aucun magasin supprimé");
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ ERREUR lors de la suppression: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifier si un vendeur a un magasin
     */
    public boolean hasMagasin(int idVendeur) {
        System.out.println("🔍 Vérification: Le vendeur " + idVendeur + " a-t-il un magasin?");
        String sql = "SELECT COUNT(*) FROM Magasin WHERE id_vendeur = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idVendeur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean hasMagasin = rs.getInt(1) > 0;
                System.out.println(hasMagasin ? "✅ OUI" : "❌ NON");
                return hasMagasin;
            }
        } catch (SQLException e) {
            System.err.println("❌ ERREUR dans hasMagasin: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Mettre à jour le logo du magasin
     */
    public boolean updateLogoMagasin(int idMagasin, String logoPath) {
        String sql = "UPDATE Magasin SET logo_magasin = ? WHERE id_magasin = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, logoPath);
            ps.setInt(2, idMagasin);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Mettre à jour toutes les infos du magasin
     */
    public boolean updateMagasin(Magasin m) {
        String sql = "UPDATE Magasin SET " +
                "nom_magasin=?, adresse=?, localisation=?, description=?, " +
                "telephone=?, email_contact=?, site_web=?, facebook=?, instagram=?, " +
                "categorie=?, logo_magasin=?, nb_ventes_mensuelles=? " +
                "WHERE id_magasin=?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, m.getNomMagasin());
            ps.setString(2, m.getAdresse());
            ps.setString(3, m.getLocalisation());
            ps.setString(4, m.getDescription());
            ps.setString(5, m.getTelephone());
            ps.setString(6, m.getEmailContact());
            ps.setString(7, m.getSiteWeb());
            ps.setString(8, m.getFacebook());
            ps.setString(9, m.getInstagram());
            ps.setString(10, m.getCategorie());
            ps.setString(11, m.getLogoMagasin());
            ps.setInt(12, m.getNbVentesMensuelles());
            ps.setInt(13, m.getIdMagasin());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}