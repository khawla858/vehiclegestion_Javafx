package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.MagasinDAO;
import com.example.vehiclegestion.vendeur.model.Magasin;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AjouterMagasinController {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtAdresse;

    @FXML
    private TextField txtLocalisation;

    @FXML
    private TextArea txtDescription;

    private int vendeurId = 1; // TODO: mettre l’id du vendeur connecté

    @FXML
    public void ajouterMagasin() {

        String nom = txtNom.getText();
        String adr = txtAdresse.getText();
        String loc = txtLocalisation.getText();
        String desc = txtDescription.getText();

        Magasin m = new Magasin(nom, adr, loc, desc, vendeurId);

        MagasinDAO dao = new MagasinDAO();
        dao.addMagasin(m);

        // Fermer fenêtre
        Stage s = (Stage) txtNom.getScene().getWindow();
        s.close();
    }

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
}
