package com.example.vehiclegestion.common.service;

import com.example.vehiclegestion.common.dao.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchService {

    private static SearchService instance;
    private Connection connection;

    private SearchService() {
        try {
            this.connection = DatabaseConnection.getConnection();
            System.out.println("✅ SearchService connecté à la base de données");

            // Test de connexion
            if (this.connection != null && !this.connection.isClosed()) {
                System.out.println("✅ Connexion BD active");
            } else {
                System.err.println("❌ Connexion BD fermée ou null");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur connexion BD SearchService: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static SearchService getInstance() {
        if (instance == null) {
            instance = new SearchService();
        }
        return instance;
    }

    /**
     * Recherche globale dans toutes les catégories
     */

    /**
     * Recherche d'articles/vehicules
     */
    public List<SearchResultData> rechercherArticles(String query) {
        List<SearchResultData> results = new ArrayList<>();

        if (connection == null) {
            return results;
        }

        String sql = "SELECT a.id_article, a.titre, a.description, a.prix, " +
                "a.categorie, a.etat " +
                "FROM Article a " +
                "WHERE (LOWER(a.titre) LIKE LOWER(?) " +
                "   OR LOWER(a.description) LIKE LOWER(?) " +
                "   OR LOWER(a.categorie) LIKE LOWER(?)) " +
                "AND a.etat != 'vendu' " +
                "ORDER BY a.date_ajout DESC " +
                "LIMIT 10";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            System.out.println("🔍 Recherche articles avec pattern: " + searchPattern);

            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_article");
                String titre = rs.getString("titre");
                String description = rs.getString("description");
                double prix = rs.getDouble("prix");
                String categorie = rs.getString("categorie");
                String etat = rs.getString("etat");

                // Raccourcir la description si trop longue
                String shortDescription = (description != null && description.length() > 100) ?
                        description.substring(0, 100) + "..." : description;

                results.add(new SearchResultData(
                        getIconForCategory(categorie),
                        "Article",
                        titre,
                        String.format("%s - %.2f DH - %s", categorie, prix, etat),
                        "ARTICLE",
                        id
                ));

                System.out.println("🔍 Article trouvé: " + titre);
            }

            System.out.println("✅ Articles trouvés: " + results.size());

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche articles: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Recherche de magasins
     */


    /**
     * Recherche de vendeurs
     */
    public List<SearchResultData> rechercherVendeurs(String query) {
        List<SearchResultData> results = new ArrayList<>();

        if (connection == null) {
            return results;
        }

        String sql = "SELECT u.id_utilisateur, u.nom, u.prenom, v.nom_magasin " +
                "FROM Utilisateur u " +
                "LEFT JOIN Vendeur v ON u.id_utilisateur = v.id_vendeur " +
                "WHERE u.role = 'vendeur' " +
                "   AND (LOWER(u.nom) LIKE LOWER(?) " +
                "   OR LOWER(u.prenom) LIKE LOWER(?) " +
                "   OR LOWER(v.nom_magasin) LIKE LOWER(?)) " +
                "LIMIT 5";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_utilisateur");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String nomMagasin = rs.getString("nom_magasin");

                String displayName = nomMagasin != null ? nomMagasin : (nom + " " + prenom);

                results.add(new SearchResultData(
                        "👨‍💼",
                        "Vendeur",
                        displayName,
                        "Vendeur",
                        "SELLER",
                        id
                ));
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche vendeurs: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Recherche de catégories/actions suggérées
     */
    public List<SearchResultData> rechercherCategories(String query) {
        List<SearchResultData> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        // Suggestions de navigation basées sur les mots-clés
        if (lowerQuery.contains("réservation") || lowerQuery.contains("reservation") ||
                lowerQuery.contains("historique") || lowerQuery.contains("history")) {
            results.add(new SearchResultData(
                    "📋",
                    "Action",
                    "Mes Réservations",
                    "Voir l'historique de vos réservations",
                    "ACTION",
                    0,
                    "RESERVATIONS"
            ));
        }

        if (lowerQuery.contains("favori") || lowerQuery.contains("favorite") ||
                lowerQuery.contains("coeur") || lowerQuery.contains("heart") ||
                lowerQuery.contains("aime")) {
            results.add(new SearchResultData(
                    "❤️",
                    "Action",
                    "Mes Favoris",
                    "Voir vos articles favoris",
                    "ACTION",
                    0,
                    "FAVORITES"
            ));
        }

        if (lowerQuery.contains("profil") || lowerQuery.contains("profile") ||
                lowerQuery.contains("compte") || lowerQuery.contains("account") ||
                lowerQuery.contains("parametre")) {
            results.add(new SearchResultData(
                    "👤",
                    "Action",
                    "Mon Profil",
                    "Gérer votre profil et paramètres",
                    "ACTION",
                    0,
                    "PROFILE"
            ));
        }

        if (lowerQuery.contains("message") || lowerQuery.contains("chat") ||
                lowerQuery.contains("conversation") || lowerQuery.contains("discuter")) {
            results.add(new SearchResultData(
                    "💬",
                    "Action",
                    "Messages",
                    "Accéder à vos conversations",
                    "ACTION",
                    0,
                    "MESSAGES"
            ));
        }

        if (lowerQuery.contains("article") || lowerQuery.contains("vehicule") ||
                lowerQuery.contains("vehicle") || lowerQuery.contains("voiture") ||
                lowerQuery.contains("car")) {
            results.add(new SearchResultData(
                    "🚗",
                    "Action",
                    "Tous les Articles",
                    "Parcourir le catalogue complet",
                    "ACTION",
                    0,
                    "ARTICLES"
            ));
        }

        return results;
    }

    /**
     * Méthode pour obtenir l'icône appropriée selon la catégorie
     */
    private String getIconForCategory(String categorie) {
        if (categorie == null) return "📦";

        String lowerCat = categorie.toLowerCase();
        if (lowerCat.contains("voiture") || lowerCat.contains("vehicule") || lowerCat.contains("car")) {
            return "🚗";
        } else if (lowerCat.contains("moto") || lowerCat.contains("motocycle")) {
            return "🏍️";
        } else if (lowerCat.contains("velo") || lowerCat.contains("bicycle")) {
            return "🚲";
        } else if (lowerCat.contains("camion") || lowerCat.contains("truck")) {
            return "🚚";
        } else if (lowerCat.contains("electrique") || lowerCat.contains("electric")) {
            return "⚡";
        } else if (lowerCat.contains("luxe") || lowerCat.contains("luxury")) {
            return "💎";
        } else {
            return "📦";
        }
    }

    /**
     * Test de connexion à la base de données
     */
    public boolean testConnexion() {
        try {
            if (connection != null && !connection.isClosed()) {
                String sql = "SELECT 1";
                try (Statement stmt = connection.createStatement()) {
                    ResultSet rs = stmt.executeQuery(sql);
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Test connexion échoué: " + e.getMessage());
        }
        return false;
    }

    // ============================================
    // CLASSE DE DONNÉES POUR LES RÉSULTATS
    // ============================================

    public static class SearchResultData {
        private final String icon;
        private final String category;
        private final String title;
        private final String description;
        private final String type;
        private final int id;
        private final String actionType;

        public SearchResultData(String icon, String category, String title,
                                String description, String type, int id) {
            this(icon, category, title, description, type, id, null);
        }

        public SearchResultData(String icon, String category, String title,
                                String description, String type, int id, String actionType) {
            this.icon = icon;
            this.category = category;
            this.title = title;
            this.description = description;
            this.type = type;
            this.id = id;
            this.actionType = actionType;
        }

        // Getters
        public String getIcon() { return icon; }
        public String getCategory() { return category; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getType() { return type; }
        public int getId() { return id; }
        public String getActionType() { return actionType; }

        @Override
        public String toString() {
            return String.format("%s %s: %s - %s", icon, category, title, description);
        }
    }



    /**
     * ✅ AJOUT DE LA RECHERCHE DE MAGASINS dans SearchService
     * À ajouter dans votre classe SearchService existante
     */

    /**
     * Recherche de magasins - NOUVELLE MÉTHODE
     */






































    /**
     * ✅ Recherche de magasins - VERSION AMÉLIORÉE avec recherche par ville
     */
    public List<SearchResultData> rechercherMagasins(String query) {
        List<SearchResultData> results = new ArrayList<>();

        if (connection == null) {
            return results;
        }

        // ✅ Requête améliorée pour mieux chercher par localisation/ville
        String sql = "SELECT m.id_magasin, m.nom_magasin, m.adresse, m.localisation, " +
                "m.categorie, m.description " +
                "FROM Magasin m " +
                "WHERE (LOWER(m.nom_magasin) LIKE LOWER(?) " +
                "   OR LOWER(m.adresse) LIKE LOWER(?) " +
                "   OR LOWER(m.localisation) LIKE LOWER(?) " +
                "   OR LOWER(m.categorie) LIKE LOWER(?) " +
                "   OR LOWER(m.description) LIKE LOWER(?)) " +
                "ORDER BY " +
                "  CASE " +
                "    WHEN LOWER(m.localisation) LIKE LOWER(?) THEN 1 " +  // Priorité 1: ville exacte
                "    WHEN LOWER(m.adresse) LIKE LOWER(?) THEN 2 " +       // Priorité 2: adresse
                "    WHEN LOWER(m.nom_magasin) LIKE LOWER(?) THEN 3 " +   // Priorité 3: nom
                "    ELSE 4 " +
                "  END, " +
                "  m.nom_magasin " +
                "LIMIT 20";  // ✅ Augmenté à 20 pour les recherches par ville

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + query + "%";
            System.out.println("🔍 Recherche magasins avec pattern: " + searchPattern);

            // Paramètres pour WHERE
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            stmt.setString(5, searchPattern);

            // Paramètres pour ORDER BY
            stmt.setString(6, searchPattern);
            stmt.setString(7, searchPattern);
            stmt.setString(8, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_magasin");
                String nom = rs.getString("nom_magasin");
                String adresse = rs.getString("adresse");
                String localisation = rs.getString("localisation");
                String categorie = rs.getString("categorie");
                String description = rs.getString("description");

                // ✅ Description enrichie avec localisation en évidence
                StringBuilder displayDesc = new StringBuilder();

                // Afficher la localisation en premier si elle correspond à la recherche
                if (localisation != null && !localisation.isEmpty()) {
                    displayDesc.append("📍 ").append(localisation);
                }

                if (categorie != null && !categorie.isEmpty()) {
                    if (displayDesc.length() > 0) displayDesc.append(" • ");
                    displayDesc.append(categorie);
                }

                if (adresse != null && !adresse.isEmpty() && displayDesc.length() < 60) {
                    if (displayDesc.length() > 0) displayDesc.append(" • ");
                    // Raccourcir l'adresse si trop longue
                    String shortAdresse = adresse.length() > 30 ?
                            adresse.substring(0, 30) + "..." : adresse;
                    displayDesc.append(shortAdresse);
                }

                results.add(new SearchResultData(
                        "🏪",
                        "Magasin",
                        nom,
                        displayDesc.toString(),
                        "STORE",
                        id
                ));

                System.out.println("🔍 Magasin trouvé: " + nom + " (" + localisation + ")");
            }

            System.out.println("✅ Magasins trouvés: " + results.size());

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche magasins: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Recherche spécifique par ville
     * Cette méthode est appelée quand on détecte qu'il s'agit d'une recherche de ville
     */
    public List<SearchResultData> rechercherMagasinsParVille(String ville) {
        List<SearchResultData> results = new ArrayList<>();

        if (connection == null) {
            return results;
        }

        System.out.println("🏙️ Recherche magasins dans la ville: " + ville);

        String sql = "SELECT m.id_magasin, m.nom_magasin, m.adresse, m.localisation, " +
                "m.categorie, m.description, " +
                "COUNT(a.id_article) as nb_articles " +
                "FROM Magasin m " +
                "LEFT JOIN Article a ON m.id_vendeur = a.id_vendeur " +
                "WHERE LOWER(m.localisation) LIKE LOWER(?) " +
                "GROUP BY m.id_magasin, m.nom_magasin, m.adresse, m.localisation, " +
                "         m.categorie, m.description " +
                "ORDER BY nb_articles DESC, m.nom_magasin " +
                "LIMIT 30";  // Plus de résultats pour les villes

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + ville + "%";
            stmt.setString(1, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id_magasin");
                String nom = rs.getString("nom_magasin");
                String adresse = rs.getString("adresse");
                String localisation = rs.getString("localisation");
                String categorie = rs.getString("categorie");
                int nbArticles = rs.getInt("nb_articles");

                // Description avec nombre d'articles
                StringBuilder displayDesc = new StringBuilder();
                displayDesc.append("📍 ").append(localisation);

                if (categorie != null && !categorie.isEmpty()) {
                    displayDesc.append(" • ").append(categorie);
                }

                if (nbArticles > 0) {
                    displayDesc.append(" • ").append(nbArticles).append(" article(s)");
                }

                results.add(new SearchResultData(
                        "🏪",
                        "Magasin",
                        nom,
                        displayDesc.toString(),
                        "STORE",
                        id
                ));
            }

            System.out.println("✅ Trouvé " + results.size() + " magasin(s) à " + ville);

        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche magasins par ville: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Détecte si la recherche est une ville marocaine
     */
    private boolean estUneVilleMarocaine(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }

        String lowerQuery = query.toLowerCase().trim();

        // Liste des principales villes marocaines
        String[] villesMarocaines = {
                "rabat", "casablanca", "fès", "fes", "marrakech", "tanger", "agadir",
                "meknès", "meknes", "oujda", "kenitra", "tétouan", "tetouan", "safi",
                "mohammedia", "khouribga", "beni mellal", "el jadida", "taza", "nador",
                "settat", "ksar el kebir", "larache", "khemisset", "guelmim", "berrechid",
                "taourirt", "berkane", "sidi slimane", "errachidia", "sale", "salé",
                "sidi kacem", "khenifra", "tiznit", "tan-tan", "ouarzazate", "sefrou"
        };

        for (String ville : villesMarocaines) {
            if (lowerQuery.equals(ville) || lowerQuery.startsWith(ville + " ")) {
                return true;
            }
        }

        return false;
    }

    /**
     * ✅ MODIFIER rechercheGlobale() pour détecter les recherches de villes
     */
    public List<SearchResultData> rechercheGlobale(String query) {
        List<SearchResultData> results = new ArrayList<>();

        if (connection == null) {
            System.err.println("❌ Connection est null dans rechercheGlobale");
            return results;
        }

        System.out.println("🔍 Début recherche globale pour: '" + query + "'");

        // ✅ DÉTECTER SI C'EST UNE RECHERCHE DE VILLE
        if (estUneVilleMarocaine(query)) {
            System.out.println("🏙️ Recherche de ville détectée: " + query);

            // Chercher UNIQUEMENT les magasins dans cette ville
            List<SearchResultData> magasinsVille = rechercherMagasinsParVille(query);

            if (!magasinsVille.isEmpty()) {
                // Ajouter un résumé en premier
                results.add(new SearchResultData(
                        "🏙️",
                        "Ville",
                        magasinsVille.size() + " magasin(s) à " + capitalizeFirstLetter(query),
                        "Cliquez pour voir tous les magasins de cette ville",
                        "CITY_SUMMARY",
                        0,
                        query  // Stocker la ville dans actionType
                ));

                // Ajouter tous les magasins de la ville
                results.addAll(magasinsVille);

                System.out.println("✅ Recherche ville terminée: " + magasinsVille.size() + " résultats");
                return results;
            }
        }

        // ✅ Recherche normale si ce n'est pas une ville ou aucun résultat
        results.addAll(rechercherArticles(query));
        results.addAll(rechercherMagasins(query));
        results.addAll(rechercherVendeurs(query));
        results.addAll(rechercherCategories(query));

        System.out.println("🔍 Recherche globale terminée: " + results.size() + " résultats trouvés");

        return results;
    }

    /**
     * ✅ Méthode utilitaire pour capitaliser la première lettre
     */
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

}