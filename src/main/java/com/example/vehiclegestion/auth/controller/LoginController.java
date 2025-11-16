<<<<<<< HEAD
package com.example.vehiclegestion.auth.controller;

import com.example.vehiclegestion.client.model.Client;
import com.example.vehiclegestion.utils.NavigationController; // ← IMPORT AJOUTÉ
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.time.LocalDateTime;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;

    @FXML
    public void initialize() {
        System.out.println("✅ LoginController initialisé");

        // Auto-remplir pour tests
        emailField.setText("test@automarket.com");
        passwordField.setText("test123");

        // Enter key pour connexion
        passwordField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        System.out.println("🔄 Tentative de connexion...");

        String email = emailField.getText();
        String password = passwordField.getText();

=======
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

>>>>>>> 6beccf9ab2d9a6f3480a8433f3a542500cff3b44
        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs");
            return;
        }
<<<<<<< HEAD

        try {
            // Simuler une connexion réussie
            System.out.println("✅ Connexion réussie: " + email);

            // Rediriger vers le dashboard
            NavigationController.loadDashboard();

        } catch (Exception e) {
            System.err.println("❌ Erreur connexion: " + e.getMessage());
            showAlert("Erreur", "Échec de la connexion: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        System.out.println("📝 Inscription demandée");
        showAlert("Inscription", "Fonctionnalité d'inscription à implémenter");
    }

    private void showAlert(String title, String message) {
=======
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
>>>>>>> 6beccf9ab2d9a6f3480a8433f3a542500cff3b44
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
<<<<<<< HEAD
    }
=======
}
@FXML
    private void initialize(){
        System.out.println("Initialisation avec succées !");
}
>>>>>>> 6beccf9ab2d9a6f3480a8433f3a542500cff3b44
}