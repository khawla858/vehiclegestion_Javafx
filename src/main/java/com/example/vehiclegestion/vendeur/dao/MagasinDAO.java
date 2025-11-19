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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * ✅ CORRIGÉ : Récupère TOUS les champs incluant le logo
     */
    public List<Magasin> getAllMagasins() {
        List<Magasin> magasins = new ArrayList<>();
        String sql = "SELECT * FROM Magasin";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Magasin m = new Magasin();

                // Champs de base
                m.setIdMagasin(rs.getInt("id_magasin"));
                m.setNomMagasin(rs.getString("nom_magasin"));
                m.setAdresse(rs.getString("adresse"));
                m.setLocalisation(rs.getString("localisation"));
                m.setDescription(rs.getString("description"));
                m.setIdVendeur(rs.getInt("id_vendeur"));
                m.setNbVentesMensuelles(rs.getInt("nb_ventes_mensuelles"));

                // ✅ NOUVEAUX CHAMPS incluant le logo
                m.setLogoMagasin(rs.getString("logo_magasin"));
                m.setTelephone(rs.getString("telephone"));
                m.setEmailContact(rs.getString("email_contact"));
                m.setSiteWeb(rs.getString("site_web"));
                m.setFacebook(rs.getString("facebook"));
                m.setInstagram(rs.getString("instagram"));
                m.setCategorie(rs.getString("categorie"));

                magasins.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return magasins;
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Récupérer un magasin par ID vendeur
     */
    public Magasin getMagasinByVendeur(int idVendeur) {
        String sql = "SELECT * FROM Magasin WHERE id_vendeur = ? LIMIT 1";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idVendeur);
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

                return m;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Ajouter un magasin
     */
    public void addMagasin(Magasin m) {
        String sql = "INSERT INTO Magasin (" +
                "nom_magasin, adresse, localisation, description, id_vendeur, " +
                "telephone, email_contact, site_web, facebook, instagram, categorie, logo_magasin, nb_ventes_mensuelles" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
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

            stmt.executeUpdate();
            System.out.println("✅ Magasin ajouté avec succès!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Supprimer un magasin
     */
    public boolean deleteMagasin(int idMagasin) {
        String sql = "DELETE FROM Magasin WHERE id_magasin = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idMagasin);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifier si un vendeur a un magasin
     */
    public boolean hasMagasin(int idVendeur) {
        String sql = "SELECT COUNT(*) FROM Magasin WHERE id_vendeur = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idVendeur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
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
     * ✅ NOUVELLE MÉTHODE : Mettre à jour toutes les infos du magasin
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