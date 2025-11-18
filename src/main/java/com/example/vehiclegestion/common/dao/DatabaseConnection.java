package com.example.vehiclegestion.common.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/vehiclegestion";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Aamer1512";

    private static Connection connection = null;

    public static Connection getConnection() throws SQLException {
        if (connection == null) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Connexion à la base de données réussie");
            } catch (ClassNotFoundException e) {
                System.err.println("❌ Driver PostgreSQL introuvable");
                e.printStackTrace();
                throw new SQLException("Driver PostgreSQL introuvable", e);
            } catch (SQLException e) {
                System.err.println("❌ Erreur de connexion à la base de données");
                e.printStackTrace();
                throw e; // Relancer l'exception
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; // Réinitialiser à null après fermeture
                System.out.println("✅ Connexion fermée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la fermeture de la connexion");
                e.printStackTrace();
            }
        }
    }
}