package com.example.vehiclegestion.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // ⚠️ CONFIGURATION EXACTEMENT COMME VOTRE pgAdmin
    private static final String DEFAULT_DB = "postgres";        // Maintenance database
    private static final String TARGET_DB = "Java_Project";     // Votre base de données cible
    private static final String HOST = "localhost";             // Host (standard)
    private static final String PORT = "5432";                  // Port exact de votre capture
    private static final String USER = "postgres";              // Username de votre capture
    private static final String PASSWORD = "Aamer1512";         // ⚠️ METTEZ VOTRE VRAI MOT DE PASSE ICI

    private static final String URL_DEFAULT = String.format("jdbc:postgresql://%s:%s/%s",
            HOST, PORT, DEFAULT_DB);
    private static final String URL_TARGET = String.format("jdbc:postgresql://%s:%s/%s",
            HOST, PORT, TARGET_DB);

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");

            // Essayer d'abord de se connecter à la base cible
            try {
                Connection conn = DriverManager.getConnection(URL_TARGET, USER, PASSWORD);
                System.out.println("✅ Connexion réussie à: " + TARGET_DB);
                return conn;
            } catch (SQLException e) {
                System.out.println("⚠️ Base " + TARGET_DB + " non trouvée, création...");

                // Se connecter à postgres pour créer Java_Project
                Connection defaultConn = DriverManager.getConnection(URL_DEFAULT, USER, PASSWORD);
                createDatabase(defaultConn);
                defaultConn.close();

                // Se reconnecter à la nouvelle base
                Connection conn = DriverManager.getConnection(URL_TARGET, USER, PASSWORD);
                System.out.println("✅ Base " + TARGET_DB + " créée et connectée");
                return conn;
            }

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL non trouvé", e);
        }
    }

    private static void createDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE \"" + TARGET_DB + "\"");
            System.out.println("📦 Base '" + TARGET_DB + "' créée");
        } catch (SQLException e) {
            System.err.println("❌ Erreur création base: " + e.getMessage());
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("🔌 Connexion fermée");
            } catch (SQLException e) {
                System.err.println("Erreur fermeture: " + e.getMessage());
            }
        }
    }

    // Méthode de test
    public static void testConnection() {
        try {
            Connection conn = getConnection();
            System.out.println("🎉 Test de connexion RÉUSSI !");

            // Vérifier si la table Utilisateur existe
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT 1 FROM Utilisateur LIMIT 1");
                System.out.println("✅ Table Utilisateur trouvée");
            } catch (SQLException e) {
                System.out.println("⚠️ Table Utilisateur non trouvée, exécutez vos scripts SQL");
            }

            closeConnection(conn);
        } catch (SQLException e) {
            System.err.println("❌ Échec connexion: " + e.getMessage());
            System.err.println("URL essayée: " + URL_TARGET);
            System.err.println("User: " + USER);
            System.err.println("Vérifiez: 1) PostgreSQL démarré, 2) Bon mot de passe, 3) Port 5432");
        }
    }

    // Main pour tester
    public static void main(String[] args) {
        testConnection();
    }
}