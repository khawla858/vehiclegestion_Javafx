package com.example.vehiclegestion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
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
    }
    private void initializeDatabase(){
        try{
            String url = "jdbc:postgresql://localhost:5432/Java_Project" ;
            String user = "postgres";
            String password = "Aamer1512";

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
}