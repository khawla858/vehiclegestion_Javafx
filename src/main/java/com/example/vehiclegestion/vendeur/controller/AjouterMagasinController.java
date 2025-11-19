package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.dao.MagasinDAO;
import com.example.vehiclegestion.vendeur.model.Magasin;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.File;                // pour File
import java.io.IOException;         // pour IOException
import java.nio.file.Files;         // pour Files.copy
import java.nio.file.Path;          // pour Path
import java.nio.file.StandardCopyOption; // pour REPLACE_EXISTING
import javafx.stage.FileChooser;    // pour FileChooser
import javafx.stage.Window;         // pour la fenêtre parent de FileChooser
import javafx.scene.image.Image;    // pour Image
import javafx.scene.image.ImageView;// pour ImageView
import javafx.fxml.FXML;            // pour @FXML
import javafx.scene.control.TextField; // pour TextField
import javafx.scene.layout.VBox;




public class AjouterMagasinController {

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtAdresse;

    @FXML
    private TextField txtLocalisation;

    @FXML
    private TextArea txtDescription;

    @FXML
    private TextField logoField;
    @FXML
    private ImageView logoPreview;
    @FXML
    private ImageView imagePreview;

    @FXML
    private VBox imagePreviewContainer;
    @FXML
    private TextField txtTelephone;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtSiteWeb;

    @FXML
    private TextField txtFacebook;

    @FXML
    private TextField txtInstagram;

    @FXML
    private TextField txtCategorie;



    private int vendeurId = 1; // TODO: mettre l’id du vendeur connecté

    @FXML
    public void ajouterMagasin() {
        Magasin m = new Magasin();

        m.setNomMagasin(txtNom.getText());
        m.setAdresse(txtAdresse.getText());
        m.setLocalisation(txtLocalisation.getText());
        m.setDescription(txtDescription.getText());
        m.setIdVendeur(vendeurId);

        m.setTelephone(txtTelephone.getText());
        m.setEmailContact(txtEmail.getText());
        m.setSiteWeb(txtSiteWeb.getText());
        m.setFacebook(txtFacebook.getText());
        m.setInstagram(txtInstagram.getText());
        m.setCategorie(txtCategorie.getText());

        m.setLogoMagasin(logoField.getText());
        m.setNbVentesMensuelles(0); // par défaut

        MagasinDAO dao = new MagasinDAO();
        dao.addMagasin(m);

        // Fermer fenêtre
        Stage s = (Stage) txtNom.getScene().getWindow();
        s.close();
    }

    @FXML
    private void browseLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un logo pour le magasin");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(logoField.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Path logosDir = Path.of(System.getProperty("user.dir"), "images", "logos");
                if (!Files.exists(logosDir)) Files.createDirectories(logosDir);

                String ext = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));
                String newName = "logo_magasin_" + System.currentTimeMillis() + ext;

                Path dest = logosDir.resolve(newName);
                Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                String logoRelativePath = "images/logos/" + newName;
                logoField.setText(logoRelativePath);

                Image logoImage = new Image(dest.toUri().toString(), true);
                logoPreview.setImage(logoImage);

                imagePreview.setImage(logoImage);
                imagePreviewContainer.setVisible(true);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void closeWindow() {
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }
    @FXML
    public void openLocalisation() {
        String loc = txtLocalisation.getText();

        if (loc == null || loc.isEmpty()) return;

        // Encoder la localisation pour URL
        String url = "https://www.google.com/maps/search/?api=1&query=" + loc.replace(" ", "+");

        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void saveMagasin() {
        String logoPath = logoField.getText();
        MagasinDAO dao = new MagasinDAO();

        // Exemple : mise à jour du logo pour un magasin existant
        int idMagasin = 1; // récupéré depuis la sélection ou le formulaire
        boolean success = dao.updateLogoMagasin(idMagasin, logoPath);

        if (success) {
            System.out.println("Logo enregistré avec succès !");
        } else {
            System.out.println("Erreur lors de l'enregistrement du logo.");
        }
    }

}
