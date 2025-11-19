package com.example.vehiclegestion.vendeur.dao;

import com.example.vehiclegestion.common.dao.DatabaseConnection;
import com.example.vehiclegestion.vendeur.model.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    private Connection connection;
    private final int VENDEUR_ID = 1; // ID du vendeur connecté

    public ClientDAO() {
        try {
            connection = DatabaseConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🔹 Récupérer tous les clients du vendeur
    public List<Client> getClientsByVendeur(int vendeurId) throws SQLException {
        List<Client> clients = new ArrayList<>();

        String query = "SELECT c.id_client, u.nom, u.prenom, u.email, c.telephone, c.adresse, c.statut_client, " +
                "COUNT(v.id_vente) AS nb_ventes, " +
                "COALESCE(SUM(v.montant_total), 0) AS total_depense, " +
                "MAX(v.date_vente) AS dernier_achat " +
                "FROM Client c " +
                "JOIN Utilisateur u ON u.id_utilisateur = c.id_client " +
                "JOIN Vente v ON v.id_client = c.id_client AND v.id_vendeur = ? " +
                "GROUP BY c.id_client, u.nom, u.prenom, u.email, c.telephone, c.adresse, c.statut_client";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, vendeurId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Client client = new Client();
                    client.setId(rs.getInt("id_client"));
                    client.setNom(rs.getString("nom"));
                    client.setPrenom(rs.getString("prenom"));
                    client.setEmail(rs.getString("email"));
                    client.setTelephone(rs.getString("telephone"));
                    client.setAdresse(rs.getString("adresse"));
                    client.setStatutClient(rs.getString("statut_client"));
                    client.setVendeurId(vendeurId);

                    client.setNbVentes(rs.getInt("nb_ventes"));
                    client.setTotalDepense(rs.getDouble("total_depense"));
                    if (rs.getDate("dernier_achat") != null)
                        client.setDernierAchat(rs.getDate("dernier_achat").toLocalDate());

                    clients.add(client);
                }
            }
        }
        return clients;
    }



    // 🔹 Récupérer un client par son ID
    public Client getClientById(int clientId) throws SQLException {
        String query = "SELECT * FROM Client WHERE id_client = ? AND id_vendeur = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, VENDEUR_ID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClient(rs);
                }
            }
        }
        return null;
    }

    // 🔹 Supprimer un client (avec suppression des ventes associées)
    // 🔹 Supprimer toutes les ventes d'un client pour ce vendeur
    public boolean deleteClientVentes(int clientId) throws SQLException {
        String deleteVentesQuery = "DELETE FROM Vente WHERE id_client = ? AND id_vendeur = ?";

        try (PreparedStatement stmt = connection.prepareStatement(deleteVentesQuery)) {
            stmt.setInt(1, clientId);
            stmt.setInt(2, VENDEUR_ID);
            int ventesDeleted = stmt.executeUpdate();
            System.out.println("🗑️ " + ventesDeleted + " vente(s) du client supprimée(s)");
            return ventesDeleted >= 0;
        }
    }


    // 🔹 Ajouter un nouveau client
    public boolean addClient(Client client) throws SQLException {
        String query = "INSERT INTO Client (nom, prenom, email, telephone, adresse, status, id_vendeur) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getEmail());
            stmt.setString(4, client.getTelephone());
            stmt.setString(5, client.getAdresse());
            stmt.setString(6, client.getStatus());
            stmt.setInt(7, VENDEUR_ID);

            int rowsAffected = stmt.executeUpdate();

            // Récupérer l'ID généré
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        client.setId(generatedKeys.getInt(1));
                    }
                }
            }

            return rowsAffected > 0;
        }
    }

    // 🔹 Mettre à jour un client
    public boolean updateClient(Client client) throws SQLException {
        String query = "UPDATE Client SET nom = ?, prenom = ?, email = ?, telephone = ?, adresse = ?, status = ?, dernier_contact = ? WHERE id_client = ? AND id_vendeur = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, client.getNom());
            stmt.setString(2, client.getPrenom());
            stmt.setString(3, client.getEmail());
            stmt.setString(4, client.getTelephone());
            stmt.setString(5, client.getAdresse());
            stmt.setString(6, client.getStatus());
            stmt.setDate(7, java.sql.Date.valueOf(java.time.LocalDate.now()));
            stmt.setInt(8, client.getId());
            stmt.setInt(9, VENDEUR_ID);

            return stmt.executeUpdate() > 0;
        }
    }

    // 🔹 Méthode utilitaire pour mapper ResultSet vers Client
    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setId(rs.getInt("id_client"));
        client.setNom(rs.getString("nom"));
        client.setPrenom(rs.getString("prenom"));
        client.setEmail(rs.getString("email"));
        client.setTelephone(rs.getString("telephone"));
        client.setAdresse(rs.getString("adresse"));
        client.setStatus(rs.getString("status"));
        client.setDateCreation(rs.getDate("date_creation").toLocalDate());

        Date dernierContact = rs.getDate("dernier_contact");
        if (dernierContact != null) {
            client.setLastContact(dernierContact.toLocalDate());
        }

        client.setVendeurId(rs.getInt("id_vendeur"));
        client.setBuyer(rs.getBoolean("is_acheteur"));

        return client;
    }

    // 🔹 Rechercher des clients
    public List<Client> searchClients(String searchTerm) throws SQLException {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT * FROM Client WHERE id_vendeur = ? AND (nom LIKE ? OR prenom LIKE ? OR email LIKE ? OR telephone LIKE ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, VENDEUR_ID);
            String likeTerm = "%" + searchTerm + "%";
            stmt.setString(2, likeTerm);
            stmt.setString(3, likeTerm);
            stmt.setString(4, likeTerm);
            stmt.setString(5, likeTerm);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Client client = mapResultSetToClient(rs);
                    clients.add(client);
                }
            }
        }
        return clients;
    }

    // 🔹 Compter le nombre total de clients
    public int countClientsByVendeur(int vendeurId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Client WHERE id_vendeur = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, vendeurId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
    // 🔹 Rechercher et filtrer des clients dynamiquement
    public List<Client> getClientsByVendeurWithFilter(int vendeurId, String filter, String searchTerm) throws SQLException {
        List<Client> clients = new ArrayList<>();

        StringBuilder query = new StringBuilder(
                "SELECT c.*, u.nom, u.prenom, u.email " +
                        "FROM Client c " +
                        "JOIN Utilisateur u ON u.id_utilisateur = c.id_client " +
                        "WHERE c.id_vendeur = ?"
        );

        // Filtre par statut
        if (filter != null && !filter.equalsIgnoreCase("Tous les clients")) {
            query.append(" AND c.status = ?");
        }

        // Recherche texte
        if (searchTerm != null && !searchTerm.isEmpty()) {
            query.append(" AND (u.nom ILIKE ? OR u.prenom ILIKE ? OR u.email ILIKE ? OR c.telephone ILIKE ?)");
        }

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int idx = 1;
            stmt.setInt(idx++, vendeurId);

            if (filter != null && !filter.equalsIgnoreCase("Tous les clients")) {
                stmt.setString(idx++, filter);
            }

            if (searchTerm != null && !searchTerm.isEmpty()) {
                String like = "%" + searchTerm + "%";
                stmt.setString(idx++, like);
                stmt.setString(idx++, like);
                stmt.setString(idx++, like);
                stmt.setString(idx++, like);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clients.add(mapResultSetToClient(rs));
                }
            }
        }

        return clients;
    }

    public List<String> getDistinctStatutsByVendeur(int vendeurId) throws SQLException {
        List<String> statuts = new ArrayList<>();
        String query = "SELECT DISTINCT statut_client FROM Client WHERE id_vendeur = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, vendeurId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    statuts.add(rs.getString("statut_client"));
                }
            }
        }
        return statuts;
    }



}