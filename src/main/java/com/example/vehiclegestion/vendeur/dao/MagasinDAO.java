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

    public List<Magasin> getAllMagasins() {
        List<Magasin> magasins = new ArrayList<>();
        String sql = "SELECT * FROM Magasin";

        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                magasins.add(new Magasin(
                        rs.getInt("id_magasin"),
                        rs.getString("nom_magasin"),
                        rs.getString("adresse"),
                        rs.getString("localisation"),
                        rs.getString("description"),
                        rs.getInt("id_vendeur")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return magasins;
    }

    public void addMagasin(Magasin m) {
        String sql = "INSERT INTO Magasin(nom_magasin, adresse, localisation, description, id_vendeur) VALUES(?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, m.getNomMagasin());
            stmt.setString(2, m.getAdresse());
            stmt.setString(3, m.getLocalisation());
            stmt.setString(4, m.getDescription());
            stmt.setInt(5, m.getIdVendeur());
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

}
