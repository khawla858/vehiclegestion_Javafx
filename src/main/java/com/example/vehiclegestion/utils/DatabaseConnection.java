package com.example.vehiclegestion.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DEFAULT_DB = "postgres";
    private static final String TARGET_DB = "Java_Projet";
    private static final String URL_DEFAULT = "jdbc:postgresql://localhost:5432/" + DEFAULT_DB;
    private static final String URL_TARGET = "jdbc:postgresql://localhost:5432/" + TARGET_DB;
    private static final String USER = "postgres";
    private static final String PASSWORD = "khadija12345aalla"; // ⚠️ REMPLACEZ CE MOT DE PASSE

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");

            // Essayer d'abord de se connecter à la base cible
            try {
                Connection conn = DriverManager.getConnection(URL_TARGET, USER, PASSWORD);
                System.out.println("✅ Connexion réussie à la base: " + TARGET_DB);
                return conn;
            } catch (SQLException e) {
                System.out.println("⚠️ Base " + TARGET_DB + " non trouvée, tentative de création...");

                // Se connecter à la base par défaut pour créer la base cible
                Connection defaultConn = DriverManager.getConnection(URL_DEFAULT, USER, PASSWORD);
                createDatabase(defaultConn);
                defaultConn.close();

                // Se reconnecter à la nouvelle base
                Connection conn = DriverManager.getConnection(URL_TARGET, USER, PASSWORD);
                System.out.println("✅ Base " + TARGET_DB + " créée et connexion établie");
                return conn;
            }

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL non trouvé", e);
        }
    }

    private static void createDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Créer la base de données
            stmt.executeUpdate("CREATE DATABASE \"" + TARGET_DB + "\"");
            System.out.println("📦 Base de données '" + TARGET_DB + "' créée avec succès");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création de la base: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        // Fermeture normale
    }
}