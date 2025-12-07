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

    /**
     * Ajouter une demande de magasin
     */
    public boolean addDemande(int idVendeur, String nom, String adresse, String localisation,
                              String description, String photo) {
        String sql = "INSERT INTO DemandeMagasin (id_vendeur, nom_magasin, adresse, localisation, description, photo_profil) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        System.out.println("📝 Tentative d'ajout de demande:");
        System.out.println("   👤 ID Vendeur: " + idVendeur);
        System.out.println("   🏪 Nom magasin: " + nom);
        System.out.println("   📍 Adresse: " + adresse);
        System.out.println("   🗺️ Localisation: " + localisation);
        System.out.println("   📝 Description: " + (description.length() > 50 ? description.substring(0, 50) + "..." : description));
        System.out.println("   🖼️ Photo: " + (photo != null ? photo : "null"));

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idVendeur);
            ps.setString(2, nom);
            ps.setString(3, adresse);
            ps.setString(4, localisation);
            ps.setString(5, description);
            ps.setString(6, photo != null ? photo : ""); // Si null, mettre chaîne vide

            int rowsAffected = ps.executeUpdate();
            System.out.println("✅ " + rowsAffected + " ligne(s) affectée(s) dans DemandeMagasin");

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de l'ajout de la demande:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Vérifier si un vendeur a déjà une demande en attente
     */
    public boolean hasDemandePending(int vendeurId) {
        String sql = "SELECT COUNT(*) FROM DemandeMagasin WHERE id_vendeur = ? AND statut = 'en attente'";

        System.out.println("🔍 Vérification demande en attente pour vendeur ID: " + vendeurId);

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, vendeurId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("📊 Nombre de demandes en attente: " + count);
                return count > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification demande en attente:");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Récupérer toutes les demandes (pour l'admin)
     */
    public List<DemandeMagasin> getAllDemandes() {
        List<DemandeMagasin> demandes = new ArrayList<>();
        String sql = "SELECT dm.*, u.nom, u.prenom, u.email, u.telephone " +
                "FROM DemandeMagasin dm " +
                "JOIN Utilisateur u ON dm.id_vendeur = u.id_utilisateur " +
                "ORDER BY dm.date_demande DESC";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                DemandeMagasin demande = new DemandeMagasin(
                        rs.getInt("id_demande"),
                        rs.getInt("id_vendeur"),
                        rs.getString("nom_magasin"),
                        rs.getString("adresse"),
                        rs.getString("localisation"),
                        rs.getString("description"),
                        rs.getString("photo_profil"),
                        rs.getString("statut"),
                        rs.getTimestamp("date_demande")
                );

                // Ajouter les infos utilisateur si disponibles
                demande.setNomVendeur(rs.getString("prenom") + " " + rs.getString("nom"));
                demande.setEmailVendeur(rs.getString("email"));
                demande.setTelephoneVendeur(rs.getString("telephone"));

                demandes.add(demande);
            }

            System.out.println("📋 " + demandes.size() + " demande(s) récupérée(s)");
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération des demandes:");
            e.printStackTrace();
        }
        return demandes;
    }

    /**
     * Mettre à jour le statut d'une demande
     */
    public boolean updateStatutDemande(int idDemande, String statut) {
        String sql = "UPDATE DemandeMagasin SET statut = ? WHERE id_demande = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, idDemande);

            int rowsAffected = ps.executeUpdate();
            System.out.println("🔄 Statut demande ID " + idDemande + " mis à jour à: " + statut);

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur mise à jour statut:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Récupérer une demande par ID
     */
    public DemandeMagasin getDemandeById(int idDemande) {
        String sql = "SELECT dm.*, u.nom, u.prenom, u.email, u.telephone " +
                "FROM DemandeMagasin dm " +
                "JOIN Utilisateur u ON dm.id_vendeur = u.id_utilisateur " +
                "WHERE dm.id_demande = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idDemande);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                DemandeMagasin demande = new DemandeMagasin(
                        rs.getInt("id_demande"),
                        rs.getInt("id_vendeur"),
                        rs.getString("nom_magasin"),
                        rs.getString("adresse"),
                        rs.getString("localisation"),
                        rs.getString("description"),
                        rs.getString("photo_profil"),
                        rs.getString("statut"),
                        rs.getTimestamp("date_demande")
                );

                demande.setNomVendeur(rs.getString("prenom") + " " + rs.getString("nom"));
                demande.setEmailVendeur(rs.getString("email"));
                demande.setTelephoneVendeur(rs.getString("telephone"));

                return demande;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération demande par ID:");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Récupérer les demandes d'un vendeur spécifique
     */
    public List<DemandeMagasin> getDemandesByVendeur(int idVendeur) {
        List<DemandeMagasin> demandes = new ArrayList<>();
        String sql = "SELECT * FROM DemandeMagasin WHERE id_vendeur = ? ORDER BY date_demande DESC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idVendeur);
            ResultSet rs = ps.executeQuery();

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

            System.out.println("👤 " + demandes.size() + " demande(s) trouvée(s) pour vendeur ID: " + idVendeur);
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération demandes par vendeur:");
            e.printStackTrace();
        }
        return demandes;
    }
}