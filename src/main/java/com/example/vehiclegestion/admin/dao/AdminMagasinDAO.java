package com.example.vehiclegestion.admin.dao;

import com.example.vehiclegestion.common.dao.DatabaseConnection;
import com.example.vehiclegestion.vendeur.model.Magasin;
import org.postgresql.util.PGobject;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminMagasinDAO {

    // ====================== GET ALL MAGASINS ======================
    public static List<Magasin> getAllMagasins() {
        List<Magasin> magasins = new ArrayList<>();
        String query = "SELECT * FROM Magasin ORDER BY id_magasin DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                magasins.add(extractMagasinFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération magasins: " + e.getMessage());
        }

        return magasins;
    }

    // ====================== GET BY ID ======================
    public Magasin getMagasinById(int idMagasin) {
        String query = "SELECT * FROM Magasin WHERE id_magasin = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idMagasin);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractMagasinFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println("Erreur récupération magasin: " + e.getMessage());
        }

        return null;
    }

    // ====================== ADD ======================
    public static boolean addMagasin(Magasin magasin) {
        String query = "INSERT INTO Magasin (nom_magasin, adresse, localisation, description, " +
                "id_vendeur, telephone, email_contact, site_web, facebook, instagram, categorie, " +
                "logo_magasin, nb_ventes_mensuelles, horaires_ouverture) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, magasin.getNomMagasin());
            stmt.setString(2, magasin.getAdresse());
            stmt.setString(3, magasin.getLocalisation());
            stmt.setString(4, magasin.getDescription());
            stmt.setObject(5, magasin.getIdVendeur() == 0 ? null : magasin.getIdVendeur());
            stmt.setString(6, magasin.getTelephone());
            stmt.setString(7, magasin.getEmailContact());
            stmt.setString(8, magasin.getSiteWeb());
            stmt.setString(9, magasin.getFacebook());
            stmt.setString(10, magasin.getInstagram());
            stmt.setString(11, magasin.getCategorie());
            stmt.setString(12, magasin.getLogoMagasin());
            stmt.setInt(13, magasin.getNbVentesMensuelles());
            stmt.setObject(14, magasin.getHoraires(), Types.OTHER);


            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) magasin.setIdMagasin(rs.getInt(1));
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Erreur ajout magasin: " + e.getMessage());
        }

        return false;
    }

    // ====================== UPDATE ======================
    public boolean updateMagasin(Magasin magasin) {
        String query = "UPDATE Magasin SET nom_magasin = ?, adresse = ?, localisation = ?, " +
                "description = ?, id_vendeur = ?, telephone = ?, email_contact = ?, " +
                "site_web = ?, facebook = ?, instagram = ?, categorie = ?, logo_magasin = ?, " +
                "nb_ventes_mensuelles = ?, horaires_ouverture = ?::jsonb " +
                "WHERE id_magasin = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, magasin.getNomMagasin());
            stmt.setString(2, magasin.getAdresse());
            stmt.setString(3, magasin.getLocalisation());
            stmt.setString(4, magasin.getDescription());
            stmt.setObject(5, magasin.getIdVendeur() == 0 ? null : magasin.getIdVendeur());
            stmt.setString(6, magasin.getTelephone());
            stmt.setString(7, magasin.getEmailContact());
            stmt.setString(8, magasin.getSiteWeb());
            stmt.setString(9, magasin.getFacebook());
            stmt.setString(10, magasin.getInstagram());
            stmt.setString(11, magasin.getCategorie());
            stmt.setString(12, magasin.getLogoMagasin());
            stmt.setInt(13, magasin.getNbVentesMensuelles());
            stmt.setObject(14, magasin.getHoraires(), Types.OTHER);

            stmt.setInt(15, magasin.getIdMagasin());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur update magasin: " + e.getMessage());
        }

        return false;
    }

    // ====================== DELETE ======================
    public boolean deleteMagasin(int idMagasin) {
        String query = "DELETE FROM Magasin WHERE id_magasin = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idMagasin);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erreur suppression magasin: " + e.getMessage());
        }

        return false;
    }

    // ====================== SEARCH (nom, adresse, localisation) ======================
    public List<Magasin> searchMagasins(String searchTerm) {
        List<Magasin> magasins = new ArrayList<>();
        String query = "SELECT * FROM Magasin WHERE " +
                "LOWER(nom_magasin) LIKE ? OR LOWER(adresse) LIKE ? OR LOWER(localisation) LIKE ? " +
                "ORDER BY id_magasin DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String s = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, s);
            stmt.setString(2, s);
            stmt.setString(3, s);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) magasins.add(extractMagasinFromResultSet(rs));

        } catch (SQLException e) {
            System.err.println("Erreur search magasin: " + e.getMessage());
        }

        return magasins;
    }

    // ====================== SEARCH WITH CATEGORIE (compatibilité AdminService) ======================
    public List<Magasin> searchMagasins(String searchTerm, String categorie) {
        List<Magasin> magasins = new ArrayList<>();
        String query = "SELECT * FROM Magasin WHERE " +
                "(LOWER(nom_magasin) LIKE ? OR LOWER(adresse) LIKE ? OR LOWER(localisation) LIKE ?) " +
                "AND (? IS NULL OR categorie = ?) " +
                "ORDER BY id_magasin DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String s = "%" + searchTerm.toLowerCase() + "%";
            stmt.setString(1, s);
            stmt.setString(2, s);
            stmt.setString(3, s);
            stmt.setObject(4, categorie);
            stmt.setObject(5, categorie);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) magasins.add(extractMagasinFromResultSet(rs));

        } catch (SQLException e) {
            System.err.println("Erreur search + categorie: " + e.getMessage());
        }

        return magasins;
    }

    // ====================== MAGASINS PAR CATEGORIE ======================
    public static Map<String, Integer> getMagasinCountByCategorie() {
        Map<String, Integer> map = new HashMap<>();
        String query = "SELECT categorie, COUNT(*) FROM Magasin GROUP BY categorie";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getString(1), rs.getInt(2));
            }

        } catch (SQLException e) {
            System.err.println("Erreur stats catégories: " + e.getMessage());
        }

        return map;
    }

    // ====================== TOP N MAGASINS BY VEHICLES ======================
    public List<Magasin> getTopMagasinsByVehicules(int limit) {
        List<Magasin> top = new ArrayList<>();
        String query = "SELECT * FROM Magasin ORDER BY nb_ventes_mensuelles DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) top.add(extractMagasinFromResultSet(rs));

        } catch (SQLException e) {
            System.err.println("Erreur top magasins: " + e.getMessage());
        }

        return top;
    }

    // ====================== COUNT ======================
    public static int getTotalMagasins() {
        String query = "SELECT COUNT(*) FROM Magasin";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Erreur count magasins: " + e.getMessage());
        }

        return 0;
    }

    // ====================== EXTRACTOR ======================
    private static Magasin extractMagasinFromResultSet(ResultSet rs) throws SQLException {
        Magasin magasin = new Magasin();

        magasin.setIdMagasin(rs.getInt("id_magasin"));
        magasin.setNomMagasin(rs.getString("nom_magasin"));
        magasin.setAdresse(rs.getString("adresse"));
        magasin.setLocalisation(rs.getString("localisation"));
        magasin.setDescription(rs.getString("description"));

        int idVendeur = rs.getInt("id_vendeur");
        if (!rs.wasNull()) magasin.setIdVendeur(idVendeur);

        magasin.setTelephone(rs.getString("telephone"));
        magasin.setEmailContact(rs.getString("email_contact"));
        magasin.setSiteWeb(rs.getString("site_web"));
        magasin.setFacebook(rs.getString("facebook"));
        magasin.setInstagram(rs.getString("instagram"));
        magasin.setCategorie(rs.getString("categorie"));
        magasin.setLogoMagasin(rs.getString("logo_magasin"));
        magasin.setNbVentesMensuelles(rs.getInt("nb_ventes_mensuelles"));

        Object horaires = rs.getObject("horaires_ouverture");
        if (horaires != null) {
            if (horaires instanceof PGobject) {
                magasin.setHoraires(((PGobject) horaires).getValue());
            } else {
                magasin.setHoraires(horaires.toString());
            }
        }

        return magasin;
    }


    public static List<Magasin> getMagasinsByVendeur(int idVendeur) {
        List<Magasin> magasins = new ArrayList<>();
        String query = "SELECT * FROM Magasin WHERE id_vendeur = ? ORDER BY id_magasin DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idVendeur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                magasins.add(extractMagasinFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println("Erreur getMagasinsByVendeur: " + e.getMessage());
        }

        return magasins;
    }
    public static int getTotalMagasinsCount() {
        String query = "SELECT COUNT(*) FROM Magasin";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("Erreur getTotalMagasinsCount: " + e.getMessage());
        }

        return 0;
    }

}
