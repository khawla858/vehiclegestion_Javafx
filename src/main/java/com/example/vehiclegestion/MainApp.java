package com.example.vehiclegestion;

import com.example.vehiclegestion.utils.DatabaseConnection;
import com.example.vehiclegestion.utils.NavigationController;
import javafx.application.Application;
import javafx.stage.Stage;
<<<<<<< HEAD

/**
 * Application principale AutoMarket
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        try {
            MainApp.primaryStage = primaryStage;

            // Initialiser la navigation
            NavigationController.setPrimaryStage(primaryStage);

            // Configuration de la fenêtre
            setupPrimaryStage();

            // Test de la base de données
            testDatabaseConnection();

            // Charger la page de login
            NavigationController.loadLogin();

            System.out.println("✅ Application AutoMarket démarrée avec succès");

        } catch (Exception e) {
            System.err.println("❌ Erreur critique au démarrage: " + e.getMessage());
            e.printStackTrace();
            showErrorDialog("Erreur au démarrage", e.getMessage());
        }
    }

    private void setupPrimaryStage() {
        primaryStage.setTitle("AutoMarket - Connexion");
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        primaryStage.setOnCloseRequest(event -> {
            System.out.println("🔌 Fermeture de l'application AutoMarket");
        });
    }

    private void testDatabaseConnection() {
        try {
            DatabaseConnection.getConnection();
            System.out.println("✅ Connexion à la base de données réussie");
        } catch (Exception e) {
            System.out.println("⚠️ Attention: Base de données non disponible - Mode démo activé");
        }
    }

    private void showErrorDialog(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
=======
import java.sql.Connection;
import java.sql.DriverManager;
import java.io.IOException;

public class MainApp extends Application {
 private Connection connection ;

    @Override
    public void start(Stage stage) throws Exception {

        initializeDatabase();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/vehiclegestion/view/login.fxml"));
        Parent root = loader.load();

        com.example.vehiclegestion.controller.LoginController controller = loader.getController();
        controller.setConnection(connection);

        Scene scene = new Scene(root,600,400);
        stage.setScene(scene);
        stage.setTitle("connextion-gestion véhicules");
        stage.setResizable(false);
        stage.show();
>>>>>>> 6beccf9ab2d9a6f3480a8433f3a542500cff3b44
    }
    private void initializeDatabase(){
        try{
            String url = "jdbc:postgresql://localhost:5432/Java_Project" ;
            String user = "postgres";
            String password = "Aamer1512";

<<<<<<< HEAD
    public static void main(String[] args) {
        System.out.println("🚗 Démarrage d'AutoMarket...");
        launch(args);
    }
=======
            connection = DriverManager.getConnection(url,user,password);
            System.out.println("Connection réussite !");
        }catch(Exception e){
            System.err.println("erreur de connection :"+e.getMessage());
            e.printStackTrace();
        }
    }
public void stop() throws Exception {
    if (connection != null && !connection.isClosed()) {
        connection.close();
        System.out.println("🔌 Connexion BD fermée.");
    }
}

public static void main(String[] args) {
    launch(args);
}
>>>>>>> 6beccf9ab2d9a6f3480a8433f3a542500cff3b44
}