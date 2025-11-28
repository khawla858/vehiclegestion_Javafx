package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.model.Magasin;
import com.example.vehiclegestion.vendeur.dao.MagasinDAO;
import com.example.vehiclegestion.utils.DataReceiver;
import com.example.vehiclegestion.utils.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.File;

public class MagasinDetailsController implements DataReceiver {

    @FXML private ImageView logoImage;
    @FXML private Text nomMagasin;
    @FXML private Text categorieMagasin;
    @FXML private Label localisationBadge;
    @FXML private Label adresseMagasin;
    @FXML private Label telephoneMagasin;
    @FXML private Hyperlink emailMagasin;
    @FXML private Hyperlink lienSiteWeb;
    @FXML private VBox horairesContainer;
    @FXML private Text descriptionMagasin;
    @FXML private Button btnFacebook;
    @FXML private Button btnInstagram;
    @FXML private ImageView carteImage;
    @FXML private Label etageInfo;
    @FXML private Button btnItineraire;
    @FXML private Button btnVoirCarte;
    @FXML private Button btnRetour;

    private Magasin magasin;
    private MagasinDAO magasinDAO;
    private NavigationManager nav = NavigationManager.getInstance();

    @FXML
    public void initialize() {
        try {
            magasinDAO = new MagasinDAO();
            configurerActions();

            // ✅ Configuration du bouton retour
            if (btnRetour != null) {
                btnRetour.setOnAction(e -> nav.goBack());
                btnRetour.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation : " + e.getMessage());
        }
    }

    /**
     * ✅ Implémentation de DataReceiver pour recevoir le magasin
     */
    @Override
    public void receiveData(Object data) {
        if (data instanceof Magasin) {
            this.magasin = (Magasin) data;
            System.out.println("✅ Magasin reçu dans MagasinDetailsController: " + magasin.getNomMagasin());
            afficherDonneesMagasin();
        } else {
            System.err.println("❌ Données reçues non valides dans MagasinDetailsController");
            showError("Erreur", "Impossible de charger les données du magasin");
        }
    }

    private void afficherDonneesMagasin() {
        if (magasin == null) {
            System.err.println("❌ Aucun magasin à afficher");
            return;
        }

        nomMagasin.setText(magasin.getNomMagasin() != null ? magasin.getNomMagasin() : "Nom du magasin");
        categorieMagasin.setText(magasin.getCategorie() != null ? magasin.getCategorie() : "Catégorie non définie");

        if (localisationBadge != null) {
            localisationBadge.setText(magasin.getLocalisation() != null ? magasin.getLocalisation() : "Localisation");
        }

        adresseMagasin.setText(magasin.getAdresse() != null ? magasin.getAdresse() : "Adresse non renseignée");
        telephoneMagasin.setText(magasin.getTelephone() != null ? magasin.getTelephone() : "Téléphone non renseigné");

        if (emailMagasin != null) {
            emailMagasin.setText(magasin.getEmailContact() != null ? magasin.getEmailContact() : "Email non renseigné");
        }

        if (lienSiteWeb != null) {
            lienSiteWeb.setText(magasin.getSiteWeb() != null ? magasin.getSiteWeb() : "www.votresite.com");
        }

        descriptionMagasin.setText(magasin.getDescription() != null ? magasin.getDescription() :
                "Découvrez notre magasin et profitez de nos services de qualité.");

        chargerLogo();
        chargerHoraires();
        chargerImageCarte();
    }

    private void chargerLogo() {
        try {
            if (magasin.getLogoMagasin() != null && !magasin.getLogoMagasin().isEmpty()) {
                String logoPath = magasin.getLogoMagasin();
                File logoFile = new File(logoPath);

                if (logoFile.exists()) {
                    Image logo = new Image(logoFile.toURI().toString());
                    logoImage.setImage(logo);
                    System.out.println("✅ Logo chargé : " + logoPath);
                } else {
                    System.err.println("❌ Logo introuvable : " + logoPath);
                    chargerLogoParDefaut();
                }
            } else {
                chargerLogoParDefaut();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur chargement logo : " + e.getMessage());
            chargerLogoParDefaut();
        }
    }

    private void chargerLogoParDefaut() {
        try {
            Image defaultLogo = new Image(getClass().getResourceAsStream("/images/default-store.png"));
            logoImage.setImage(defaultLogo);
        } catch (Exception e) {
            System.out.println("ℹ️ Aucun logo disponible");
        }
    }

    private void chargerImageCarte() {
        try {
            File carteFile = new File("images/logos/carte-localisation.jpg");
            if (carteFile.exists()) {
                Image carte = new Image(carteFile.toURI().toString());
                carteImage.setImage(carte);
                System.out.println("✅ Carte locale chargée : " + carteFile.getAbsolutePath());
            } else {
                chargerCarteDepuisResources();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erreur chargement carte locale : " + e.getMessage());
            chargerCarteDepuisResources();
        }
    }

    private void chargerCarteDepuisResources() {
        try {
            Image carte = new Image(getClass().getResourceAsStream("/images/logos/carte-localisation.png"));
            carteImage.setImage(carte);
            System.out.println("✅ Carte chargée depuis resources");
        } catch (Exception e) {
            System.err.println("⚠️ Carte introuvable dans resources, génération placeholder...");
            genererPlaceholderCarte();
        }
    }

    private void genererPlaceholderCarte() {
        try {
            javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(400, 500);
            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

            javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
                    0, 0, 0, 500, false, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#f0f9ff")),
                    new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#e0f2fe"))
            );
            gc.setFill(gradient);
            gc.fillRect(0, 0, 400, 500);

            gc.setStroke(javafx.scene.paint.Color.web("#bae6fd"));
            gc.setLineWidth(3);
            gc.strokeRect(0, 0, 400, 500);

            gc.setFill(javafx.scene.paint.Color.web("#38bdf8"));
            gc.fillOval(130, 150, 140, 140);

            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", 70));
            gc.fillText("🗺️", 160, 240);

            gc.setFill(javafx.scene.paint.Color.web("#0c4a6e"));
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 22));
            gc.fillText("Plan de localisation", 90, 340);

            gc.setFill(javafx.scene.paint.Color.web("#075985"));
            gc.setFont(javafx.scene.text.Font.font("Arial", 16));
            gc.fillText("Cliquez sur 'Voir en plein écran'", 70, 380);
            gc.fillText("pour ouvrir Google Maps", 90, 410);

            javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
            params.setFill(javafx.scene.paint.Color.TRANSPARENT);
            javafx.scene.image.WritableImage image = canvas.snapshot(params, null);

            carteImage.setImage(image);
            System.out.println("✅ Placeholder carte généré");

        } catch (Exception e) {
            System.err.println("❌ Impossible de générer le placeholder : " + e.getMessage());
        }
    }

    private void chargerHoraires() {
        horairesContainer.getChildren().clear();

        if (magasin.getHorairesOuverture() != null && !magasin.getHorairesOuverture().isEmpty()) {
            magasin.getHorairesOuverture().forEach((jour, horaire) -> {
                HBox ligneHoraire = creerLigneHoraire(jour, horaire);
                horairesContainer.getChildren().add(ligneHoraire);
            });
        } else {
            horairesContainer.getChildren().addAll(
                    creerLigneHoraire("Du Dimanche au Jeudi", "de 10h à 20h"),
                    creerLigneHoraire("Vendredi et Samedi", "de 10h à 20h")
            );
        }
    }

    private HBox creerLigneHoraire(String jour, String horaire) {
        HBox hbox = new HBox(15);
        hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label lblJour = new Label(jour + " :");
        lblJour.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569; -fx-min-width: 180;");

        Label lblHoraire = new Label(horaire);
        lblHoraire.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 14px;");

        hbox.getChildren().addAll(lblJour, lblHoraire);
        return hbox;
    }

    private void configurerActions() {
        if (emailMagasin != null) {
            emailMagasin.setOnAction(e -> {
                if (magasin != null && magasin.getEmailContact() != null) {
                    ouvrirLien("mailto:" + magasin.getEmailContact());
                }
            });
        }

        if (lienSiteWeb != null) {
            lienSiteWeb.setOnAction(e -> {
                if (magasin != null && magasin.getSiteWeb() != null) {
                    ouvrirLien("https://" + magasin.getSiteWeb());
                }
            });
        }

        if (btnFacebook != null) {
            btnFacebook.setOnAction(e -> {
                if (magasin != null && magasin.getFacebook() != null) {
                    ouvrirLien("https://facebook.com/" + magasin.getFacebook());
                }
            });
        }

        if (btnInstagram != null) {
            btnInstagram.setOnAction(e -> {
                if (magasin != null && magasin.getInstagram() != null) {
                    String instagram = magasin.getInstagram().startsWith("@") ?
                            magasin.getInstagram().substring(1) : magasin.getInstagram();
                    ouvrirLien("https://instagram.com/" + instagram);
                }
            });
        }

        if (btnItineraire != null) {
            btnItineraire.setOnAction(e -> ouvrirItineraire());
        }

        if (btnVoirCarte != null) {
            btnVoirCarte.setOnAction(e -> ouvrirCarteComplete());
        }
    }

    private void ouvrirItineraire() {
        if (magasin == null || magasin.getAdresse() == null) {
            showError("Adresse du magasin non disponible");
            return;
        }

        try {
            String adresse = construireAdresseComplete();
            String adresseEncodee = URLEncoder.encode(adresse, StandardCharsets.UTF_8);
            String urlItineraire = "https://www.google.com/maps/dir/?api=1&destination=" + adresseEncodee;
            ouvrirLien(urlItineraire);
            System.out.println("✅ Itinéraire ouvert vers : " + adresse);

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture itinéraire : " + e.getMessage());
            showError("Impossible d'ouvrir l'itinéraire");
        }
    }

    private void ouvrirCarteComplete() {
        if (magasin == null || magasin.getAdresse() == null) {
            showError("Adresse du magasin non disponible");
            return;
        }

        try {
            String adresse = construireAdresseComplete();
            String adresseEncodee = URLEncoder.encode(adresse, StandardCharsets.UTF_8);
            String urlCarte = "https://www.google.com/maps/search/?api=1&query=" + adresseEncodee;
            ouvrirLien(urlCarte);
            System.out.println("✅ Carte ouverte pour : " + adresse);

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture carte : " + e.getMessage());
            showError("Impossible d'ouvrir la carte");
        }
    }

    private String construireAdresseComplete() {
        StringBuilder adresse = new StringBuilder();

        if (magasin.getAdresse() != null && !magasin.getAdresse().isEmpty()) {
            adresse.append(magasin.getAdresse());
        }

        if (magasin.getLocalisation() != null && !magasin.getLocalisation().isEmpty()) {
            if (adresse.length() > 0) adresse.append(", ");
            adresse.append(magasin.getLocalisation());
        }

        if (adresse.length() > 0) {
            adresse.append(", Maroc");
        }

        return adresse.toString();
    }

    private void ouvrirLien(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.err.println("❌ Impossible d'ouvrir le lien : " + url);
            e.printStackTrace();
            showError("Impossible d'ouvrir le lien");
        }
    }

    // ✅ Gardez cette méthode pour la compatibilité
    public void setMagasin(Magasin magasin) {
        this.magasin = magasin;
        afficherDonneesMagasin();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}