package com.example.vehiclegestion.client.doa;

import com.example.vehiclegestion.client.model.RendezVs;
import com.example.vehiclegestion.utils.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
        System.out.println("   ID Vendeur: " + rdv.getIdVendeur() + " ⚠");
        System.out.println("   ID Article: " + rdv.getIdArticle());
        System.out.println("   Type: " + rdv.getTypeRdv());
        System.out.println("   Date: " + rdv.getDateRdv());
        System.out.println("   Heure: " + rdv.getHeureRdv());

        // Vérification critique
        if (rdv.getIdVendeur() <= 0) {
            System.err.println("❌❌❌ ERREUR CRITIQUE: ID VENDEUR INVALIDE: " + rdv.getIdVendeur());
        }
    }

    // Dans votre RendezVsDAO.java, ajoutez cette méthode :

    public int getLastInsertedId() {
        System.out.println("🔍 Récupération du dernier ID inséré...");

        String sql = "SELECT LAST_INSERT_ID()";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int id = rs.getInt(1);
                System.out.println("✅ Dernier ID inséré: " + id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération dernier ID: " + e.getMessage());
        }

        return -1;
    }

    public int getRendezVousIdByDetails(int idClient, int idVendeur, LocalDate date, LocalTime heure) {
        System.out.println("🔍 Recherche RDV par détails...");
        System.out.println("   Client: " + idClient + ", Vendeur: " + idVendeur);
        System.out.println("   Date: " + date + ", Heure: " + heure);

        String sql = "SELECT id_rdv FROM RendezVous  WHERE id_client = ? AND id_vendeur = ? " +
                "AND date_rdv = ? AND heure_rdv = ? ORDER BY id_rdv DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idClient);
            pstmt.setInt(2, idVendeur);
            pstmt.setDate(3, java.sql.Date.valueOf(date));
            pstmt.setTime(4, java.sql.Time.valueOf(heure));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id_rdv");
                    System.out.println("✅ RDV trouvé avec ID: " + id);
                    return id;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche RDV par détails: " + e.getMessage());
        }

        System.out.println("⚠ Aucun RDV trouvé avec ces détails");
        return -1;
    }
    /**
     * Crée un rendez-vous et une notification en une seule transaction
     */
    /**
     * Crée un rendez-vous et une notification en une seule transaction
     */
    public boolean creerRendezVousAvecNotification(RendezVs rendezVs) {
        System.out.println("📅 CRÉATION RENDEZ-VOUS AVEC NOTIFICATION");

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Transaction

            // 1. Insérer le rendez-vous
            String rdvQuery = "INSERT INTO RendezVous  (" +
                    "id_client, id_vendeur, id_article, " +
                    "date_rdv, heure_rdv, duree, " +
                    "type_rdv, description, statut" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement rdvStmt = conn.prepareStatement(rdvQuery, Statement.RETURN_GENERATED_KEYS);
            rdvStmt.setInt(1, rendezVs.getIdClient());
            rdvStmt.setInt(2, rendezVs.getIdVendeur());
            rdvStmt.setInt(3, rendezVs.getIdArticle());
            rdvStmt.setDate(4, java.sql.Date.valueOf(rendezVs.getDateRdv()));
            rdvStmt.setTime(5, java.sql.Time.valueOf(rendezVs.getHeureRdv()));
            rdvStmt.setInt(6, rendezVs.getDuree());
            rdvStmt.setString(7, rendezVs.getTypeRdv());
            rdvStmt.setString(8, rendezVs.getDescription());
            rdvStmt.setString(9, rendezVs.getStatut());

            int rdvRows = rdvStmt.executeUpdate();

            if (rdvRows > 0) {
                System.out.println("✅ Rendez-vous créé");

                // Récupérer l'ID du rendez-vous créé
                int rdvId = -1;
                try (ResultSet rs = rdvStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        rdvId = rs.getInt(1);
                        System.out.println("🎯 ID du rendez-vous: " + rdvId);
                    }
                }

                // 2. NOTIFICATION POUR LE CLIENT (rappel)
                // Formater la date et l'heure
                String dateStr = rendezVs.getDateRdv().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String heureStr = rendezVs.getHeureRdv().format(DateTimeFormatter.ofPattern("HH:mm"));
                String typeRdv = convertTypeToString(rendezVs.getTypeRdv());

                String notifClientQuery = "INSERT INTO Notification (" +
                        "id_utilisateur, role_destinataire, id_source, type_source, " +
                        "titre, message, type_notification, categorie, est_lue, " +
                        "date_creation, priorite, lien_action" +
                        ") VALUES (?, 'client', ?, 'rendezvous', " +
                        "'📅 Rendez-vous programmé', " +
                        "'Vous avez un rendez-vous le " + dateStr + " à " + heureStr + " pour un " + typeRdv + "', " +
                        "'rappel_rendezvous', 'transaction', false, " +
                        "NOW(), 'haute', '/rendezvous')";

                PreparedStatement notifClientStmt = conn.prepareStatement(notifClientQuery);
                notifClientStmt.setInt(1, rendezVs.getIdClient());
                notifClientStmt.setInt(2, rdvId);

                int clientNotifRows = notifClientStmt.executeUpdate();
                if (clientNotifRows > 0) {
                    System.out.println("🔔 Notification client créée");
                }

                // 3. NOTIFICATION POUR LE VENDEUR (demande)
                // Récupérer le nom du client
                String nomClient = getNomUtilisateur(rendezVs.getIdClient(), conn);
                if (nomClient == null || nomClient.isEmpty()) {
                    nomClient = "Un client";
                }

                String notifVendeurQuery = "INSERT INTO Notification (" +
                        "id_utilisateur, role_destinataire, id_source, type_source, " +
                        "titre, message, type_notification, categorie, est_lue, " +
                        "date_creation, priorite, lien_action" +
                        ") VALUES (?, 'vendeur', ?, 'rendezvous', " +
                        "'🚗 Demande d'essai', " +
                        "'" + nomClient + " demande un " + typeRdv + " le " + dateStr + " à " + heureStr + "', " +
                        "'demande_essai', 'transaction', false, " +
                        "NOW(), 'haute', '/vendeur/rendezvous')";

                // Échapper l'apostrophe dans le titre
                notifVendeurQuery = notifVendeurQuery.replace("d'essai", "d''essai");

                PreparedStatement notifVendeurStmt = conn.prepareStatement(notifVendeurQuery);
                notifVendeurStmt.setInt(1, rendezVs.getIdVendeur());
                notifVendeurStmt.setInt(2, rdvId);

                int vendeurNotifRows = notifVendeurStmt.executeUpdate();
                if (vendeurNotifRows > 0) {
                    System.out.println("🔔 Notification vendeur créée");
                }

                // 4. Valider la transaction
                conn.commit();
                System.out.println("✅ Transaction complète: RDV + 2 notifications créés");

                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ ERREUR création RDV: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.err.println("❌ Erreur rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur fermeture connexion: " + e.getMessage());
            }
        }

        return false;
    }
    /**
     * Convertit le type de rendez-vous en texte lisible
     */
    private String convertTypeToString(String type) {
        switch (type) {
            case "essai": return "essai routier";
            case "consultation": return "consultation";
            case "visite": return "visite de vérification";
            case "expertise": return "expertise détaillée";
            default: return "rendez-vous";
        }
    }

    /**
     * Récupère le nom d'un utilisateur
     */
    private String getNomUtilisateur(int idUtilisateur, Connection conn) throws SQLException {
        String query = "SELECT prenom, nom FROM Utilisateur WHERE id_utilisateur = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, idUtilisateur);

        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            String prenom = rs.getString("prenom");
            String nom = rs.getString("nom");
            return (prenom != null ? prenom + " " : "") + (nom != null ? nom : "");
        }

        return null;
    }
}