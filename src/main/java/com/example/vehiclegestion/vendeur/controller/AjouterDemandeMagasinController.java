package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.DemandeMagasinDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AjouterDemandeMagasinController {

    @FXML
    private TextField nomMagasin, adresse, localisation, photoProfil;
    @FXML
    private TextField nomVendeur, emailVendeur, telVendeur;
    @FXML
    private TextArea description;
    @FXML
    private Button btnEnvoyer;

    private int idVendeur = 1; // À récupérer depuis session / login

    public void setVendeurId(int idVendeur) {
        this.idVendeur = idVendeur;
    }
    @FXML
    public void initialize() {
        // Pré-remplir les infos du vendeur
        // Exemple statique, remplacer par DAO Utilisateur si nécessaire
        nomVendeur.setText("Nom Prénom");
        emailVendeur.setText("vendeur@email.com");
        telVendeur.setText("0600000000");
    }

    @FXML
    public void envoyerDemande() {
        String nom = nomMagasin.getText();
        String adr = adresse.getText();
        String loc = localisation.getText();
        String desc = description.getText();
        String photo = photoProfil.getText();

        if (nom.isEmpty() || adr.isEmpty() || loc.isEmpty() || desc.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champs manquants");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs obligatoires !");
            alert.showAndWait();
            return;
        }

        // Ajouter la demande dans la DB
        DemandeMagasinDAO dao = new DemandeMagasinDAO();
        boolean success = dao.addDemande(idVendeur, nom, adr, loc, desc, photo);

        Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
        alert.setTitle(success ? "Demande envoyée" : "Erreur");
        alert.setHeaderText(null);
        alert.setContentText(success ?
                "Votre demande pour créer le magasin '" + nom + "' a été envoyée !" :
                "Erreur lors de l'envoi de la demande. Veuillez réessayer.");
        alert.showAndWait();
    }
}
