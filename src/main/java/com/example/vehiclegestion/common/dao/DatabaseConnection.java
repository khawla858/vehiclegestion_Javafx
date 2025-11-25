package com.example.vehiclegestion.common.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/Java_Projet";
    private static final String USER = "postgres";
    private static final String PASSWORD = "khadija12345aalla";

    /**
     * Crée une NOUVELLE connexion à chaque appel
     * (Meilleure pratique pour les applications desktop)
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Nouvelle connexion à la base de données créée");
            return connection;
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver PostgreSQL introuvable");
            throw new SQLException("Driver PostgreSQL introuvable", e);
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Teste la connexion sans la garder ouverte
     */
    public static boolean testConnection() {
        System.out.println("🧪 Test de connexion à la base de données...");
        try (Connection testConn = getConnection()) {
            if (testConn != null && !testConn.isClosed()) {
                System.out.println("✅ Test de connexion réussi !");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Test de connexion échoué: " + e.getMessage());
        }
        return false;
    }
}