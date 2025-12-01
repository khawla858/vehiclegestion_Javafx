package com.example.vehiclegestion.client.doa;

import com.example.vehiclegestion.client.model.RendezVs;
import com.example.vehiclegestion.utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class RendezVsDAO {

    public boolean creerRendezVous(RendezVs rdv) {
        // ✅ VÉRIFICATION AVANT INSERTION
        if (rdv.getIdVendeur() <= 0) {
            System.err.println("❌ ERREUR: ID vendeur invalide: " + rdv.getIdVendeur());
            return false;
        }

        String sql = "INSERT INTO RendezVous (id_client, id_vendeur, id_article, type_rdv, date_rdv, heure_rdv, duree, description, statut) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, rdv.getIdClient());
            stmt.setInt(2, rdv.getIdVendeur());

            // Gérer le cas où idArticle pourrait être null
            if (rdv.getIdArticle() > 0) {
                stmt.setInt(3, rdv.getIdArticle());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setString(4, rdv.getTypeRdv());
            stmt.setDate(5, Date.valueOf(rdv.getDateRdv()));
            stmt.setTime(6, Time.valueOf(rdv.getHeureRdv()));
            stmt.setInt(7, rdv.getDuree());

            // Gérer la description qui pourrait être null
            if (rdv.getDescription() != null && !rdv.getDescription().isEmpty()) {
                stmt.setString(8, rdv.getDescription());
            } else {
                stmt.setNull(8, Types.VARCHAR);
            }

            stmt.setString(9, rdv.getStatut());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la création du rendez-vous: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la création du rendez-vous: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Méthode pour déboguer
    public void debugRendezVs(RendezVs rdv) {
        System.out.println("🔍 DEBUG RendezVs CRITIQUE:");
        System.out.println("   ID Client: " + rdv.getIdClient());
        System.out.println("   ID Vendeur: " + rdv.getIdVendeur() + " ⚠️");
        System.out.println("   ID Article: " + rdv.getIdArticle());
        System.out.println("   Type: " + rdv.getTypeRdv());
        System.out.println("   Date: " + rdv.getDateRdv());
        System.out.println("   Heure: " + rdv.getHeureRdv());

        // Vérification critique
        if (rdv.getIdVendeur() <= 0) {
            System.err.println("❌❌❌ ERREUR CRITIQUE: ID VENDEUR INVALIDE: " + rdv.getIdVendeur());
        }
    }
}