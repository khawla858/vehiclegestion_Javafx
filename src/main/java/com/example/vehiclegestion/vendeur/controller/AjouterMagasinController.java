package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.auth.model.Utilisateur;
import com.example.vehiclegestion.vendeur.dao.MagasinDAO;
import com.example.vehiclegestion.vendeur.model.Magasin;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AjouterMagasinController {

    @FXML private TextField txtNom;
    @FXML private TextField txtAdresse;
    @FXML private TextField txtLocalisation;
    @FXML private TextArea txtDescription;
    @FXML private TextField logoField;
    @FXML private ImageView logoPreview;
    @FXML private ImageView imagePreview;
    @FXML private VBox imagePreviewContainer;
    @FXML private TextField txtTelephone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtSiteWeb;
    @FXML private TextField txtFacebook;
    @FXML private TextField txtInstagram;
    @FXML private TextField txtCategorie;

    // ✅ Session Manager
    private SessionManager session = SessionManager.getInstance();
    private int vendeurIdConnecte; // ✅ Nom cohérent

    @FXML
    public void initialize() {
        System.out.println("\n➕ === Initialisation AjouterMagasinController ===");

        // ✅ VÉRIFICATION SESSION
        if (!session.estConnecte() || !session.estVendeur()) {
            System.err.println("❌ ERREUR: Aucun vendeur connecté");
            showErrorAndClose("Accès refusé", "Vous devez être connecté en tant que vendeur");
            return;
        }

        // ✅ RÉCUPÉRATION ID VENDEUR
        vendeurIdConnecte = session.getUserId();
        Utilisateur user = session.getUtilisateurConnecte();

        System.out.println("✅ Vendeur connecté:");
        System.out.println("   - ID: " + vendeurIdConnecte);
        System.out.println("   - Nom: " + user.getNom() + " " + user.getPrenom());
        System.out.println("   - Email: " + user.getEmail());

        // Initialiser le preview container comme invisible
        if (imagePreviewContainer != null) {
            imagePreviewContainer.setVisible(false);
            imagePreviewContainer.setManaged(false);
        }

        System.out.println("➕ === Fin initialisation ===\n");
    }

    @FXML
    public void ajouterMagasin() {
        System.out.println("\n💾 === Ajout d'un nouveau magasin ===");

        // ✅ VÉRIFICATION FINALE DE LA SESSION
        if (!session.estConnecte() || !session.estVendeur()) {
            System.err.println("❌ Session expirée");
            showError("Session expirée", "Veuillez vous reconnecter");
            return;
        }

        // ✅ VALIDATION DES CHAMPS
        if (!validateForm()) {
            System.out.println("❌ Validation du formulaire échouée");
            return;
        }

        try {
            // ✅ CRÉER L'OBJET MAGASIN
            Magasin m = new Magasin();
            m.setNomMagasin(txtNom.getText().trim());
            m.setAdresse(txtAdresse.getText().trim());
            m.setLocalisation(txtLocalisation.getText().trim());
            m.setDescription(txtDescription.getText().trim());

            // ✅ IMPORTANT : Utiliser l'ID du vendeur connecté
            m.setIdVendeur(vendeurIdConnecte);

            m.setTelephone(txtTelephone.getText().trim());
            m.setEmailContact(txtEmail.getText().trim());
            m.setSiteWeb(txtSiteWeb.getText().trim());
            m.setFacebook(txtFacebook.getText().trim());
            m.setInstagram(txtInstagram.getText().trim());
            m.setCategorie(txtCategorie.getText().trim());
            m.setLogoMagasin(logoField.getText());
            m.setNbVentesMensuelles(0);

            System.out.println("📋 Données du magasin:");
            System.out.println("   - Nom: " + m.getNomMagasin());
            System.out.println("   - Adresse: " + m.getAdresse());
            System.out.println("   - ID Vendeur: " + m.getIdVendeur());
            System.out.println("   - Logo: " + m.getLogoMagasin());

            // ✅ INSERTION EN BASE DE DONNÉES
            MagasinDAO dao = new MagasinDAO();
            dao.addMagasin(m);

            System.out.println("✅ Magasin ajouté avec succès pour le vendeur ID: " + vendeurIdConnecte);

            // ✅ AFFICHER UN MESSAGE DE SUCCÈS
            showSuccess("Magasin créé", "Votre magasin a été créé avec succès !");

            // ✅ FERMER LA FENÊTRE APRÈS UN DÉLAI
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            javafx.application.Platform.runLater(() -> closeWindow());
                        }
                    }, 1500 // 1.5 secondes
            );

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'ajout du magasin: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur", "Impossible de créer le magasin: " + e.getMessage());
        }

        System.out.println("💾 === Fin ajout magasin ===\n");
    }

    @FXML
    private void browseLogo() {
        System.out.println("🖼️ Sélection d'un logo...");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un logo pour le magasin");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(txtNom.getScene().getWindow());

        if (selectedFile != null) {
            try {
                System.out.println("📁 Fichier sélectionné: " + selectedFile.getName());

                // ✅ Créer le dossier de destination
                Path logosDir = Paths.get(System.getProperty("user.dir"), "images", "logos");
                if (!Files.exists(logosDir)) {
                    Files.createDirectories(logosDir);
                    System.out.println("📂 Dossier créé: " + logosDir);
                }

                // ✅ Générer un nom unique
                String ext = selectedFile.getName().substring(selectedFile.getName().lastIndexOf('.'));
                String newName = "logo_magasin_" + vendeurIdConnecte + "_" + System.currentTimeMillis() + ext;

                // ✅ Copier le fichier
                Path dest = logosDir.resolve(newName);
                Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✅ Logo copié vers: " + dest);

                // ✅ Stocker le chemin relatif
                String logoRelativePath = "images/logos/" + newName;
                logoField.setText(logoRelativePath);

                // ✅ Afficher l'aperçu
                Image logoImage = new Image(dest.toUri().toString(), true);

                if (logoPreview != null) {
                    logoPreview.setImage(logoImage);
                }

                if (imagePreview != null) {
                    imagePreview.setImage(logoImage);
                }

                if (imagePreviewContainer != null) {
                    imagePreviewContainer.setVisible(true);
                    imagePreviewContainer.setManaged(true);
                }

                System.out.println("✅ Aperçu du logo affiché");

            } catch (IOException e) {
                System.err.println("❌ Erreur lors de la copie du logo: " + e.getMessage());
                e.printStackTrace();
                showError("Erreur", "Impossible de copier l'image: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️ Aucun fichier sélectionné");
        }
    }

    @FXML
    public void closeWindow() {
        System.out.println("🚪 Fermeture de la fenêtre d'ajout");
        Stage stage = (Stage) txtNom.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void openLocalisation() {
        String loc = txtLocalisation.getText();
        if (loc == null || loc.isEmpty()) {
            showError("Localisation manquante", "Veuillez saisir une adresse");
            return;
        }

        String url = "https://www.google.com/maps/search/?api=1&query=" + loc.replace(" ", "+");
        try {
            System.out.println("🗺️ Ouverture de Google Maps: " + loc);
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture Google Maps: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ VALIDATION DU FORMULAIRE
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (txtNom.getText().trim().isEmpty()) {
            errors.append("• Le nom du magasin est obligatoire\n");
        }

        if (txtAdresse.getText().trim().isEmpty()) {
            errors.append("• L'adresse est obligatoire\n");
        }

        if (txtLocalisation.getText().trim().isEmpty()) {
            errors.append("• La localisation est obligatoire\n");
        }

        if (txtDescription.getText().trim().isEmpty()) {
            errors.append("• La description est obligatoire\n");
        }

        if (errors.length() > 0) {
            showError("Formulaire incomplet",
                    "Veuillez corriger les erreurs suivantes:\n\n" + errors.toString());
            return false;
        }

        return true;
    }

    // ✅ MÉTHODES D'AFFICHAGE DES MESSAGES
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show(); // Pas showAndWait() pour ne pas bloquer
    }

    private void showErrorAndClose(String title, String message) {
        showError(title, message);
        closeWindow();
    }
}