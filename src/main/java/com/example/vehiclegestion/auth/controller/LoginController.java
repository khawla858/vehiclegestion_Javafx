package com.example.vehiclegestion.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;

public class LoginController{
    @FXML
    private TextField emailField ;
    @FXML
    private PasswordField passwordField ;

    private Connection connection ;

    public void setConnection(Connection connection){
   this.connection = connection ;
   System.out.println("conextion BD injecté dans loginController ");
    }
    @FXML
    private void handleLogin(){
        String email = emailField.getText();
        String password = passwordField.getText();

        System.out.println("tentative de connection "+email);

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }
        if(email.equals("admin@test.com") &&password.equals("addmin")){
            showAlert("Succées" ," connection réussite pour :"+email);
        }else{
            showAlert("Erreur","Email ou mdp incorrect ");
        }
    }
    @FXML
    private void handleReset(){
        emailField.clear();
        passwordField.clear();
        System.out.println("Formulaire réinitialisé");
    }
@FXML
    private void showAlert(String title , String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
}
@FXML
    private void initialize(){
        System.out.println("Initialisation avec succées !");
}
}