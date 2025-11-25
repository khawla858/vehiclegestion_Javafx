package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.MagasinDAO;
import com.example.vehiclegestion.vendeur.dao.DemandeMagasinDAO;
import com.example.vehiclegestion.vendeur.model.Magasin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

// Ikonli
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

public class MagasinListController {

    @FXML
    private VBox magasinContainer;

    @FXML
    private Button btnAjouterMagasin;

    @FXML
    public void initialize() {
        chargerMagasins();

        btnAjouterMagasin.setOnAction(e -> ouvrirAjouterMagasin());
    }

    /**
     * Ouvre le formulaire d'ajout d'un magasin ou demande admin si première création
     */
    private void ouvrirAjouterMagasin() {
        MagasinDAO magasinDAO = new MagasinDAO();
        DemandeMagasinDAO demandeDAO = new DemandeMagasinDAO();

        int vendeurId = 1; // ID vendeur connecté

        // Vérifier si le vendeur a déjà un magasin
        if (!magasinDAO.hasMagasin(vendeurId)) {

            // Vérifier s'il y a déjà une demande en attente
            if (demandeDAO.hasDemandePending(vendeurId)) {
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Demande en attente");
                info.setHeaderText(null);
                info.setContentText("Vous avez déjà une demande de création de magasin en attente.");
                info.showAndWait();
                return;
            }

            // Première création → demander confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Première création de magasin");
            alert.setHeaderText("Vous ne pouvez créer  !");
            alert.setContentText("Voulez-vous envoyer une demande à l'admin pour créer votre premier magasin ?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                ouvrirFormulaireDemandeMagasin(vendeurId);
            }

        } else {
            // Déjà un magasin → ouvrir directement formulaire d'ajout
            try {
                Parent form = FXMLLoader.load(getClass().getResource("/view/vendeur/AjouterMagasin.fxml"));
                Stage stage = new Stage();
                stage.setTitle("Ajouter un Magasin");
                stage.setScene(new Scene(form));
                stage.show();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Ouvre le formulaire pour envoyer une demande de création de magasin
     */
    private void ouvrirFormulaireDemandeMagasin(int vendeurId) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/vendeur/AjouterDemandeMagasin.fxml"));
            Parent root = loader.load();

            // Passer l'ID vendeur au contrôleur
            AjouterDemandeMagasinController controller = loader.getController();
            controller.setVendeurId(vendeurId);

            Stage stage = new Stage();
            stage.setTitle("Envoyer une demande de création de magasin");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Charge tous les magasins depuis la DB et les affiche
     */
    private void chargerMagasins() {
        magasinContainer.getChildren().clear();

        MagasinDAO dao = new MagasinDAO();
        List<Magasin> magasins = dao.getAllMagasins();

        for (Magasin m : magasins) {

            VBox card = new VBox(10);
            card.setPadding(new Insets(15));
            card.setStyle(
                    "-fx-background-color: #ffffff; " +
                            "-fx-background-radius: 15;" +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 8, 0, 0, 3);"
            );

            // -----------------------
            // TOP: ICONES EDIT + DELETE
            // -----------------------
            FontIcon editIcon = new FontIcon("fas-edit");
            editIcon.setIconSize(18);
            editIcon.setCursor(Cursor.HAND);

            FontIcon deleteIcon = new FontIcon("fas-trash");
            deleteIcon.setIconSize(18);
            deleteIcon.setCursor(Cursor.HAND);

            editIcon.setOnMouseClicked(e -> {
                System.out.println("Modifier magasin : " + m.getNomMagasin());
                // TODO : ouvrir popup modification
            });

            deleteIcon.setOnMouseClicked(e -> {
                dao.deleteMagasin(m.getIdMagasin());
                chargerMagasins();
            });

            HBox topButtons = new HBox(12);
            topButtons.setAlignment(Pos.TOP_RIGHT);
            topButtons.getChildren().addAll(editIcon, deleteIcon);

            // -----------------------
            // INFORMATIONS MAGASIN
            // -----------------------
            Label nomLabel = new Label(m.getNomMagasin());
            nomLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

            Label adresseLabel = new Label(" " + m.getAdresse());
            adresseLabel.setGraphic(new FontIcon("fas-map-marker-alt"));
            adresseLabel.setStyle("-fx-font-size: 14px;");

            Label localLabel = new Label(" " + m.getLocalisation());
            localLabel.setGraphic(new FontIcon("fas-globe"));
            localLabel.setStyle("-fx-font-size: 14px;");

            Label descLabel = new Label(" " + m.getDescription());
            descLabel.setGraphic(new FontIcon("fas-info-circle"));
            descLabel.setStyle("-fx-font-size: 14px;");

            // -----------------------
            // LIEN "Voir Articles"
            // -----------------------
            Label viewArticles = new Label("Voir Articles");
            viewArticles.setStyle("-fx-text-fill: #ff1493; -fx-underline: true; -fx-font-size: 14px;");
            viewArticles.setCursor(Cursor.HAND);
            viewArticles.setOnMouseClicked(e -> {
                System.out.println("Voir articles pour : " + m.getNomMagasin());
                // TODO : ouvrir la liste des articles de ce magasin
            });

            // -----------------------
            // AJOUTER TOUT À LA CARTE
            // -----------------------
            card.getChildren().addAll(topButtons, nomLabel, adresseLabel, localLabel, descLabel, viewArticles);

            // Ajouter la carte au container
            magasinContainer.getChildren().add(card);
        }
    }
}
