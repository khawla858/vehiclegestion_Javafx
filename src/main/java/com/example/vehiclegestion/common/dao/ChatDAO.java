package com.example.vehiclegestion.common.dao;

import com.example.vehiclegestion.common.model.Conversation;
import com.example.vehiclegestion.common.model.Message;
import com.example.vehiclegestion.common.dao.DatabaseConnection;
import com.example.vehiclegestion.auth.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {

    // ==========================================
    // GESTION DES CONVERSATIONS
    // ==========================================

    /**
     * Récupère ou crée une conversation entre vendeur et client
     */
    public Conversation getOrCreateConversationVendeurClient(int idVendeur, int idClient, Integer idArticle, String sujet) {
        System.out.println("\n🔍 === RECHERCHE/CREATION CONVERSATION ===");
        System.out.println("   Vendeur: " + idVendeur);
        System.out.println("   Client: " + idClient);
        System.out.println("   Article: " + idArticle);
        System.out.println("   Sujet: " + sujet);

        // Vérifier d'abord avec l'article
        if (idArticle != null) {
            Conversation existingWithArticle = getConversationVendeurClientWithArticle(idVendeur, idClient, idArticle);
            if (existingWithArticle != null) {
                System.out.println("✅ Conversation existante avec article trouvée: ID=" + existingWithArticle.getIdConversation());
                return existingWithArticle;
            }
        }

        // Sinon, vérifier sans article
        Conversation existing = getConversationVendeurClient(idVendeur, idClient);
        if (existing != null) {
            System.out.println("✅ Conversation existante sans article trouvée: ID=" + existing.getIdConversation());
            return existing;
        }

        // Créer nouvelle
        System.out.println("🆕 Création nouvelle conversation...");
        String query = """
            INSERT INTO Conversation (type_conversation, id_vendeur, id_client, id_article, sujet, statut)
            VALUES ('vendeur_client', ?, ?, ?, ?, 'active')
            RETURNING id_conversation
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idVendeur);
            stmt.setInt(2, idClient);
            if (idArticle != null) {
                stmt.setInt(3, idArticle);
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, sujet != null ? sujet : "Nouvelle conversation");

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int idConversation = rs.getInt("id_conversation");
                System.out.println("✅ Nouvelle conversation créée: ID=" + idConversation);

                // Récupérer la conversation complète
                Conversation newConv = getConversationById(idConversation);
                if (newConv != null) {
                    System.out.println("✅ Conversation complète chargée: " + newConv.getInterlocuteurComplet());
                    return newConv;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur création conversation: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Récupère une conversation vendeur-client avec article spécifique
     */
    private Conversation getConversationVendeurClientWithArticle(int idVendeur, int idClient, int idArticle) {
        String query = """
            SELECT * FROM Conversation 
            WHERE id_vendeur = ? AND id_client = ? AND id_article = ?
            AND type_conversation = 'vendeur_client'
            ORDER BY date_creation DESC 
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idVendeur);
            stmt.setInt(2, idClient);
            stmt.setInt(3, idArticle);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Conversation conv = mapConversation(rs);
                System.out.println("🔍 Conversation avec article trouvée: ID=" + conv.getIdConversation());
                return conv;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération conversation avec article: " + e.getMessage());
        }

        return null;
    }

    /**
     * Récupère une conversation vendeur-client existante
     */
    private Conversation getConversationVendeurClient(int idVendeur, int idClient) {
        String query = """
            SELECT * FROM Conversation 
            WHERE id_vendeur = ? AND id_client = ? 
            AND type_conversation = 'vendeur_client'
            ORDER BY date_creation DESC 
            LIMIT 1
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idVendeur);
            stmt.setInt(2, idClient);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Conversation conv = mapConversation(rs);
                System.out.println("🔍 Conversation trouvée: ID=" + conv.getIdConversation() +
                        " (Vendeur:" + idVendeur + ", Client:" + idClient + ")");
                return conv;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération conversation: " + e.getMessage());
        }

        return null;
    }

    /**
     * Récupère une conversation par ID
     */
    public Conversation getConversationById(int idConversation) {
        String query = """
        SELECT 
            c.*,
            CASE 
                WHEN c.id_vendeur IS NOT NULL AND c.id_client IS NOT NULL THEN 
                    CASE 
                        WHEN ? = 'vendeur' THEN 
                            COALESCE(CONCAT(u_client.prenom, ' ', u_client.nom), 'Client #' || c.id_client)
                        WHEN ? = 'client' THEN 
                            COALESCE(CONCAT(u_vendeur.prenom, ' ', u_vendeur.nom), 'Vendeur #' || c.id_vendeur)
                        ELSE 'Utilisateur'
                    END
                ELSE 'Support'
            END AS nom_complet_interlocuteur,
            CASE 
                WHEN c.id_vendeur IS NOT NULL AND c.id_client IS NOT NULL THEN 
                    CASE 
                        WHEN ? = 'vendeur' THEN u_client.prenom
                        WHEN ? = 'client' THEN u_vendeur.prenom
                        ELSE 'Utilisateur'
                    END
                ELSE 'Support'
            END AS prenom_interlocuteur,
            CASE 
                WHEN c.id_vendeur IS NOT NULL AND c.id_client IS NOT NULL THEN 
                    CASE 
                        WHEN ? = 'vendeur' THEN u_client.nom
                        WHEN ? = 'client' THEN u_vendeur.nom
                        ELSE ''
                    END
                ELSE ''
            END AS nom_interlocuteur
        FROM Conversation c
        LEFT JOIN Utilisateur u_client ON c.id_client = u_client.id_utilisateur
        LEFT JOIN Utilisateur u_vendeur ON c.id_vendeur = u_vendeur.id_utilisateur
        WHERE c.id_conversation = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            // Récupérer le rôle de l'utilisateur connecté depuis SessionManager
            SessionManager session = SessionManager.getInstance();
            String currentRole = session.estConnecte() ? session.getUserRole() : "client";

            stmt.setString(1, currentRole);
            stmt.setString(2, currentRole);
            stmt.setString(3, currentRole);
            stmt.setString(4, currentRole);
            stmt.setString(5, currentRole);
            stmt.setString(6, currentRole);
            stmt.setInt(7, idConversation);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Conversation conv = mapConversation(rs);
                conv.setPrenomInterlocuteur(rs.getString("prenom_interlocuteur"));
                conv.setNomInterlocuteur(rs.getString("nom_interlocuteur"));
                return conv;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération conversation par ID: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    /**
     * Récupère toutes les conversations d'un utilisateur (selon son rôle)
     */
    public List<Conversation> getConversationsByUser(int idUtilisateur, String role) {
        List<Conversation> conversations = new ArrayList<>();

        String query = """
            SELECT 
                c.*,
                CASE 
                    WHEN ? = 'vendeur' THEN 
                        COALESCE(CONCAT(u_client.prenom, ' ', u_client.nom), 'Client #' || c.id_client)
                    WHEN ? = 'client' THEN 
                        COALESCE(CONCAT(u_vendeur.prenom, ' ', u_vendeur.nom), 'Vendeur #' || c.id_vendeur)
                    ELSE 'Admin Support'
                END AS nom_complet_interlocuteur,
                CASE 
                    WHEN ? = 'vendeur' THEN COALESCE(u_client.prenom, 'Client')
                    WHEN ? = 'client' THEN COALESCE(u_vendeur.prenom, 'Vendeur')
                    ELSE 'Admin'
                END AS prenom_interlocuteur,
                CASE 
                    WHEN ? = 'vendeur' THEN COALESCE(u_client.nom, '')
                    WHEN ? = 'client' THEN COALESCE(u_vendeur.nom, '')
                    ELSE 'Support'
                END AS nom_interlocuteur,
                (SELECT contenu FROM Message WHERE id_conversation = c.id_conversation ORDER BY date_envoi DESC LIMIT 1) AS dernier_message,
                (SELECT COUNT(*) FROM Message m WHERE m.id_conversation = c.id_conversation 
                 AND m.est_lu = FALSE AND m.id_expediteur != ?) AS nb_non_lus
            FROM Conversation c
            LEFT JOIN Utilisateur u_client ON c.id_client = u_client.id_utilisateur
            LEFT JOIN Utilisateur u_vendeur ON c.id_vendeur = u_vendeur.id_utilisateur
            WHERE 
                (? = 'vendeur' AND c.id_vendeur = ?) OR
                (? = 'client' AND c.id_client = ?) OR
                (? = 'admin' AND c.id_admin = ?)
            ORDER BY COALESCE(c.dernier_message_date, c.date_creation) DESC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, role);
            stmt.setString(2, role);
            stmt.setString(3, role);
            stmt.setString(4, role);
            stmt.setString(5, role);
            stmt.setString(6, role);
            stmt.setInt(7, idUtilisateur);
            stmt.setString(8, role);
            stmt.setInt(9, idUtilisateur);
            stmt.setString(10, role);
            stmt.setInt(11, idUtilisateur);
            stmt.setString(12, role);
            stmt.setInt(13, idUtilisateur);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Conversation conv = mapConversation(rs);
                conv.setPrenomInterlocuteur(rs.getString("prenom_interlocuteur"));
                conv.setNomInterlocuteur(rs.getString("nom_interlocuteur"));
                conv.setDernierMessage(rs.getString("dernier_message"));
                conv.setNbMessagesNonLus(rs.getInt("nb_non_lus"));

                System.out.println("📋 Conversation chargée: ID=" + conv.getIdConversation() +
                        " | Interlocuteur: " + conv.getInterlocuteurComplet() +
                        " | Messages non lus: " + conv.getNbMessagesNonLus());

                conversations.add(conv);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération conversations: " + e.getMessage());
            e.printStackTrace();
        }

        return conversations;
    }

    // ==========================================
    // GESTION DES MESSAGES
    // ==========================================

    /**
     * Envoie un message dans une conversation
     */
    public boolean sendMessage(int idConversation, int idExpediteur, String roleExpediteur, String contenu) {
        String query = """
            INSERT INTO Message (id_conversation, id_expediteur, role_expediteur, contenu, type_message)
            VALUES (?, ?, ?, ?, 'texte')
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idConversation);
            stmt.setInt(2, idExpediteur);
            stmt.setString(3, roleExpediteur);
            stmt.setString(4, contenu);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("\n✅ === MESSAGE ENVOYÉ ===");
                System.out.println("   Conversation: " + idConversation);
                System.out.println("   Expéditeur: " + idExpediteur + " (" + roleExpediteur + ")");
                System.out.println("   Contenu: " + contenu);

                // Mettre à jour la date du dernier message
                updateLastMessageDate(idConversation);

                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur envoi message: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Met à jour la date du dernier message d'une conversation
     */
    private void updateLastMessageDate(int idConversation) {
        String query = "UPDATE Conversation SET dernier_message_date = NOW() WHERE id_conversation = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idConversation);
            stmt.executeUpdate();
            System.out.println("📅 Date dernier message mise à jour pour conversation " + idConversation);

        } catch (SQLException e) {
            System.err.println("⚠️ Erreur mise à jour date dernier message: " + e.getMessage());
        }
    }

    /**
     * Récupère tous les messages d'une conversation
     */
    public List<Message> getMessagesByConversation(int idConversation) {
        List<Message> messages = new ArrayList<>();

        String query = """
            SELECT 
                m.*,
                COALESCE(u.prenom, 'Utilisateur') AS prenom_expediteur,
                COALESCE(u.nom, '') AS nom_expediteur
            FROM Message m
            LEFT JOIN Utilisateur u ON m.id_expediteur = u.id_utilisateur
            WHERE m.id_conversation = ?
            ORDER BY m.date_envoi ASC
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idConversation);

            System.out.println("\n🔍 Chargement des messages pour conversation ID=" + idConversation);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Message msg = mapMessage(rs);
                msg.setPrenomExpediteur(rs.getString("prenom_expediteur"));
                msg.setNomExpediteur(rs.getString("nom_expediteur"));

                System.out.println("   💬 Message ID=" + msg.getIdMessage() +
                        " | Expéditeur=" + msg.getIdExpediteur() +
                        " | Contenu: " + msg.getContenu());

                messages.add(msg);
            }

            System.out.println("✅ Total: " + messages.size() + " messages chargés");

        } catch (SQLException e) {
            System.err.println("❌ Erreur récupération messages: " + e.getMessage());
            e.printStackTrace();
        }

        return messages;
    }

    /**
     * Marque tous les messages d'une conversation comme lus
     */
    public boolean markMessagesAsRead(int idConversation, int idUtilisateur) {
        String query = """
            UPDATE Message 
            SET est_lu = TRUE, date_lecture = NOW()
            WHERE id_conversation = ? 
            AND id_expediteur != ? 
            AND est_lu = FALSE
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idConversation);
            stmt.setInt(2, idUtilisateur);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✅ " + rows + " messages marqués comme lus");
            }

            return true;

        } catch (SQLException e) {
            System.err.println("❌ Erreur marquage messages lus: " + e.getMessage());
            return false;
        }
    }

    /**
     * Compte le nombre total de messages non lus pour un utilisateur
     */
    public int countUnreadMessages(int idUtilisateur, String role) {
        String query = """
            SELECT COUNT(*) AS total
            FROM Message m
            JOIN Conversation c ON m.id_conversation = c.id_conversation
            WHERE m.est_lu = FALSE 
            AND m.id_expediteur != ?
            AND (
                (? = 'vendeur' AND c.id_vendeur = ?) OR
                (? = 'client' AND c.id_client = ?) OR
                (? = 'admin' AND c.id_admin = ?)
            )
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idUtilisateur);
            stmt.setString(2, role);
            stmt.setInt(3, idUtilisateur);
            stmt.setString(4, role);
            stmt.setInt(5, idUtilisateur);
            stmt.setString(6, role);
            stmt.setInt(7, idUtilisateur);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage messages non lus: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Supprime une conversation et tous ses messages
     */
    public boolean deleteConversation(int idConversation) {
        String query = "DELETE FROM Conversation WHERE id_conversation = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idConversation);
            int rows = stmt.executeUpdate();

            System.out.println("✅ Conversation " + idConversation + " supprimée");
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression conversation: " + e.getMessage());
            return false;
        }
    }

    // ==========================================
    // MÉTHODES UTILITAIRES
    // ==========================================

    private Conversation mapConversation(ResultSet rs) throws SQLException {
        Conversation conv = new Conversation();
        conv.setIdConversation(rs.getInt("id_conversation"));
        conv.setTypeConversation(rs.getString("type_conversation"));

        conv.setIdVendeur((Integer) rs.getObject("id_vendeur"));
        conv.setIdClient((Integer) rs.getObject("id_client"));
        conv.setIdAdmin((Integer) rs.getObject("id_admin"));

        conv.setIdArticle((Integer) rs.getObject("id_article"));
        conv.setIdPlainte((Integer) rs.getObject("id_plainte"));

        conv.setSujet(rs.getString("sujet"));
        conv.setStatut(rs.getString("statut"));

        Timestamp dateCreation = rs.getTimestamp("date_creation");
        if (dateCreation != null) {
            conv.setDateCreation(dateCreation.toLocalDateTime());
        }

        Timestamp dernierMessage = rs.getTimestamp("dernier_message_date");
        if (dernierMessage != null) {
            conv.setDernierMessageDate(dernierMessage.toLocalDateTime());
        }

        return conv;
    }

    private Message mapMessage(ResultSet rs) throws SQLException {
        Message msg = new Message();
        msg.setIdMessage(rs.getInt("id_message"));
        msg.setIdConversation(rs.getInt("id_conversation"));
        msg.setIdExpediteur(rs.getInt("id_expediteur"));
        msg.setRoleExpediteur(rs.getString("role_expediteur"));
        msg.setContenu(rs.getString("contenu"));
        msg.setTypeMessage(rs.getString("type_message"));
        msg.setFichierUrl(rs.getString("fichier_url"));
        msg.setEstLu(rs.getBoolean("est_lu"));

        Timestamp dateEnvoi = rs.getTimestamp("date_envoi");
        if (dateEnvoi != null) {
            msg.setDateEnvoi(dateEnvoi.toLocalDateTime());
        }

        Timestamp dateLecture = rs.getTimestamp("date_lecture");
        if (dateLecture != null) {
            msg.setDateLecture(dateLecture.toLocalDateTime());
        }

        return msg;
    }

    public void testDatabaseConnection() {
        System.out.println("🔍 TEST CONNEXION DATABASE");
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null) {
                System.out.println("✅ Connexion DB réussie!");

                // Tester si les tables existent
                String testQuery = "SELECT COUNT(*) FROM Conversation";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(testQuery)) {
                    if (rs.next()) {
                        System.out.println("✅ Table Conversation existe: " + rs.getInt(1) + " enregistrements");
                    }
                } catch (SQLException e) {
                    System.err.println("❌ Table Conversation non trouvée: " + e.getMessage());
                }
            } else {
                System.err.println("❌ Connexion DB échouée!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion DB: " + e.getMessage());
        }
    }
    /**
     * Envoie un message dans une conversation ET crée une notification
     */
    public boolean sendMessageWithNotification(int idConversation, int idExpediteur, String roleExpediteur, String contenu) {
        System.out.println("📤 ENVOI MESSAGE AVEC NOTIFICATION");

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Démarrer une transaction

            // 1. Insérer le message
            String messageQuery = """
            INSERT INTO Message (id_conversation, id_expediteur, role_expediteur, contenu, type_message)
            VALUES (?, ?, ?, ?, 'texte')
        """;

            PreparedStatement messageStmt = conn.prepareStatement(messageQuery, Statement.RETURN_GENERATED_KEYS);
            messageStmt.setInt(1, idConversation);
            messageStmt.setInt(2, idExpediteur);
            messageStmt.setString(3, roleExpediteur);
            messageStmt.setString(4, contenu);

            int messageRows = messageStmt.executeUpdate();

            if (messageRows > 0) {
                System.out.println("✅ Message inséré");

                // 2. Récupérer l'ID du destinataire
                int destinataireId = -1;
                String roleDestinataire = "";

                String destinataireQuery = """
                SELECT 
                    CASE 
                        WHEN ? = 'client' THEN id_vendeur
                        WHEN ? = 'vendeur' THEN id_client
                    END as id_destinataire,
                    CASE 
                        WHEN ? = 'client' THEN 'vendeur'
                        WHEN ? = 'vendeur' THEN 'client'
                    END as role_destinataire
                FROM Conversation 
                WHERE id_conversation = ?
            """;

                PreparedStatement destinataireStmt = conn.prepareStatement(destinataireQuery);
                destinataireStmt.setString(1, roleExpediteur);
                destinataireStmt.setString(2, roleExpediteur);
                destinataireStmt.setString(3, roleExpediteur);
                destinataireStmt.setString(4, roleExpediteur);
                destinataireStmt.setInt(5, idConversation);

                ResultSet rs = destinataireStmt.executeQuery();

                if (rs.next()) {
                    destinataireId = rs.getInt("id_destinataire");
                    roleDestinataire = rs.getString("role_destinataire");
                    System.out.println("🎯 Destinataire trouvé: ID=" + destinataireId + ", Role=" + roleDestinataire);
                }

                // 3. Si destinataire existe, créer la notification
                if (destinataireId > 0) {
                    // Récupérer le nom de l'expéditeur
                    String nomExpediteur = getNomUtilisateur(idExpediteur, conn);
                    if (nomExpediteur == null || nomExpediteur.isEmpty()) {
                        nomExpediteur = "Un " + (roleExpediteur.equals("client") ? "client" : "vendeur");
                    }

                    // Insérer la notification
                    String notificationQuery = """
                    INSERT INTO Notification (
                        id_utilisateur, 
                        role_destinataire, 
                        id_source, 
                        type_source, 
                        titre, 
                        message, 
                        type_notification, 
                        categorie, 
                        est_lue, 
                        date_creation, 
                        priorite, 
                        lien_action
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?)
                """;

                    PreparedStatement notifStmt = conn.prepareStatement(notificationQuery);
                    notifStmt.setInt(1, destinataireId);
                    notifStmt.setString(2, roleDestinataire);
                    notifStmt.setInt(3, idConversation);
                    notifStmt.setString(4, "conversation");
                    notifStmt.setString(5, "💬 Nouveau message");
                    notifStmt.setString(6, nomExpediteur + " vous a envoyé un message");
                    notifStmt.setString(7, "nouveau_message");
                    notifStmt.setString(8, "message");
                    notifStmt.setBoolean(9, false);
                    notifStmt.setString(10, "haute");
                    notifStmt.setString(11, "/messages");

                    int notifRows = notifStmt.executeUpdate();

                    if (notifRows > 0) {
                        System.out.println("🔔 NOTIFICATION CRÉÉE pour user " + destinataireId);
                    } else {
                        System.out.println("⚠️ Notification non créée");
                    }

                    notifStmt.close();
                } else {
                    System.out.println("⚠️ Aucun destinataire trouvé pour la conversation");
                }

                // 4. Mettre à jour la date du dernier message
                updateLastMessageDateInTransaction(idConversation, conn);

                // 5. Valider la transaction
                conn.commit();
                System.out.println("✅ Transaction validée - Message + Notification créés");

                messageStmt.close();
                if (destinataireStmt != null) destinataireStmt.close();

                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ ERREUR transaction: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("↩️ Transaction annulée");
                }
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
            stmt.close();
            return (prenom != null ? prenom + " " : "") + (nom != null ? nom : "");
        }

        stmt.close();
        return null;
    }

    /**
     * Mettre à jour la date du dernier message dans une transaction
     */
    private void updateLastMessageDateInTransaction(int idConversation, Connection conn) throws SQLException {
        String query = "UPDATE Conversation SET dernier_message_date = NOW() WHERE id_conversation = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setInt(1, idConversation);
        stmt.executeUpdate();
        stmt.close();
        System.out.println("📅 Date dernier message mise à jour");
    }
}