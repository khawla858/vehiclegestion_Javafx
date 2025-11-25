package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.common.dao.DatabaseConnection;
import com.example.vehiclegestion.vendeur.model.DemandeMagasin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DemandeMagasinDAO {

    private Connection connection;

    public DemandeMagasinDAO() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addDemande(int idVendeur, String nom, String adresse, String localisation,
                              String description, String photo) {
        String sql = "INSERT INTO DemandeMagasin (id_vendeur, nom_magasin, adresse, localisation, description, photo_profil) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idVendeur);
            ps.setString(2, nom);
            ps.setString(3, adresse);
            ps.setString(4, localisation);
            ps.setString(5, description);
            ps.setString(6, photo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasDemandePending(int vendeurId) {
        String sql = "SELECT COUNT(*) FROM DemandeMagasin WHERE id_vendeur = ? AND statut = 'en attente'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, vendeurId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<DemandeMagasin> getAllDemandes() {
        List<DemandeMagasin> demandes = new ArrayList<>();
        String sql = "SELECT * FROM DemandeMagasin";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                demandes.add(new DemandeMagasin(
                        rs.getInt("id_demande"),
                        rs.getInt("id_vendeur"),
                        rs.getString("nom_magasin"),
                        rs.getString("adresse"),
                        rs.getString("localisation"),
                        rs.getString("description"),
                        rs.getString("photo_profil"),
                        rs.getString("statut"),
                        rs.getTimestamp("date_demande")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return demandes;
    }
}
