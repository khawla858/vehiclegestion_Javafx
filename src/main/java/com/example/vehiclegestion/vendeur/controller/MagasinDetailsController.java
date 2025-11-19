package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.vendeur.model.Magasin;
import com.example.vehiclegestion.vendeur.dao.MagasinDAO;
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

public class MagasinDetailsController {

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

    private Magasin magasin;
    private MagasinDAO magasinDAO;
    private final int ID_VENDEUR = 1;

    @FXML
    public void initialize() {
        try {
            magasinDAO = new MagasinDAO();
            chargerMagasinVendeur();
            configurerActions();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation : " + e.getMessage());
        }
    }

    private void chargerMagasinVendeur() {
        try {
            var magasins = magasinDAO.getAllMagasins();

            if (magasins.isEmpty()) {
                afficherMagasinParDefaut();
                return;
            }

            magasin = magasins.stream()
                    .filter(m -> m.getIdVendeur() == ID_VENDEUR)
                    .findFirst()
                    .orElse(null);

            if (magasin != null) {
                afficherDonneesMagasin();
            } else {
                afficherMagasinParDefaut();
            }

        } catch (Exception e) {
            e.printStackTrace();
            afficherMagasinParDefaut();
        }
    }

    private void afficherDonneesMagasin() {
        if (magasin == null) return;

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
        chargerImageCarte(); // ✅ Nouvelle méthode simplifiée
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

    /**
     * ✅ NOUVELLE MÉTHODE SIMPLIFIÉE : Charge une image statique locale
     * L'image est stockée dans images/logos/carte-localisation.png
     */
    private void chargerImageCarte() {
        try {
            // OPTION 1 : Depuis le dossier images/logos (même dossier que les logos)
            File carteFile = new File("images/logos/carte-localisation.jpg");

            if (carteFile.exists()) {
                Image carte = new Image(carteFile.toURI().toString());
                carteImage.setImage(carte);
                System.out.println("✅ Carte locale chargée : " + carteFile.getAbsolutePath());
            } else {
                // OPTION 2 : Depuis resources
                System.out.println("⚠️ Fichier carte non trouvé dans images/logos, essai depuis resources...");
                chargerCarteDepuisResources();
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erreur chargement carte locale : " + e.getMessage());
            chargerCarteDepuisResources();
        }
    }

    /**
     * Charge la carte depuis le dossier resources
     */
    private void chargerCarteDepuisResources() {
        try {
            Image carte = new Image(getClass().getResourceAsStream("images/logos/carte-localisation.png"));
            carteImage.setImage(carte);
            System.out.println("✅ Carte chargée depuis resources");
        } catch (Exception e) {
            System.err.println("⚠️ Carte introuvable dans resources, génération placeholder...");
            genererPlaceholderCarte();
        }
    }

    /**
     * Génère une belle image placeholder si aucune carte n'est disponible
     */
    private void genererPlaceholderCarte() {
        try {
            javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(400, 500);
            javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

            // Fond dégradé élégant
            javafx.scene.paint.LinearGradient gradient = new javafx.scene.paint.LinearGradient(
                    0, 0, 0, 500, false, javafx.scene.paint.CycleMethod.NO_CYCLE,
                    new javafx.scene.paint.Stop(0, javafx.scene.paint.Color.web("#f0f9ff")),
                    new javafx.scene.paint.Stop(1, javafx.scene.paint.Color.web("#e0f2fe"))
            );
            gc.setFill(gradient);
            gc.fillRect(0, 0, 400, 500);

            // Bordure
            gc.setStroke(javafx.scene.paint.Color.web("#bae6fd"));
            gc.setLineWidth(3);
            gc.strokeRect(0, 0, 400, 500);

            // Cercle de fond pour l'icône
            gc.setFill(javafx.scene.paint.Color.web("#38bdf8"));
            gc.fillOval(130, 150, 140, 140);

            // Icône de carte (grand emoji)
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", 70));
            gc.fillText("🗺️", 160, 240);

            // Texte principal
            gc.setFill(javafx.scene.paint.Color.web("#0c4a6e"));
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 22));
            gc.fillText("Plan de localisation", 90, 340);

            // Texte secondaire
            gc.setFill(javafx.scene.paint.Color.web("#075985"));
            gc.setFont(javafx.scene.text.Font.font("Arial", 16));
            gc.fillText("Cliquez sur 'Voir en plein écran'", 70, 380);
            gc.fillText("pour ouvrir Google Maps", 90, 410);

            // Convertir Canvas en Image
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

        // ✅ Bouton Itinéraire - Ouvre Google Maps avec itinéraire
        if (btnItineraire != null) {
            btnItineraire.setOnAction(e -> ouvrirItineraire());
        }

        // ✅ Bouton Voir la carte - Ouvre Google Maps en plein écran
        if (btnVoirCarte != null) {
            btnVoirCarte.setOnAction(e -> ouvrirCarteComplete());
        }
    }

    /**
     * ✅ Ouvre Google Maps avec l'itinéraire vers le magasin
     */
    private void ouvrirItineraire() {
        if (magasin == null || magasin.getAdresse() == null) {
            showError("Adresse du magasin non disponible");
            return;
        }

        try {
            String adresse = construireAdresseComplete();
            String adresseEncodee = URLEncoder.encode(adresse, StandardCharsets.UTF_8);

            // URL Google Maps pour itinéraire
            String urlItineraire = "https://www.google.com/maps/dir/?api=1&destination=" + adresseEncodee;

            ouvrirLien(urlItineraire);
            System.out.println("✅ Itinéraire ouvert vers : " + adresse);

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture itinéraire : " + e.getMessage());
            showError("Impossible d'ouvrir l'itinéraire");
        }
    }

    /**
     * ✅ Ouvre Google Maps en plein écran pour voir la carte complète
     */
    private void ouvrirCarteComplete() {
        if (magasin == null || magasin.getAdresse() == null) {
            showError("Adresse du magasin non disponible");
            return;
        }

        try {
            String adresse = construireAdresseComplete();
            String adresseEncodee = URLEncoder.encode(adresse, StandardCharsets.UTF_8);

            // URL Google Maps normale
            String urlCarte = "https://www.google.com/maps/search/?api=1&query=" + adresseEncodee;

            ouvrirLien(urlCarte);
            System.out.println("✅ Carte ouverte pour : " + adresse);

        } catch (Exception e) {
            System.err.println("❌ Erreur ouverture carte : " + e.getMessage());
            showError("Impossible d'ouvrir la carte");
        }
    }

    /**
     * Construit l'adresse complète du magasin
     */
    private String construireAdresseComplete() {
        StringBuilder adresse = new StringBuilder();

        if (magasin.getAdresse() != null && !magasin.getAdresse().isEmpty()) {
            adresse.append(magasin.getAdresse());
        }

        if (magasin.getLocalisation() != null && !magasin.getLocalisation().isEmpty()) {
            if (adresse.length() > 0) adresse.append(", ");
            adresse.append(magasin.getLocalisation());
        }

        // Ajouter le pays
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

    private void afficherMagasinParDefaut() {
        nomMagasin.setText("Votre Magasin");
        categorieMagasin.setText("Catégorie à définir");
        adresseMagasin.setText("Adresse à renseigner");
        telephoneMagasin.setText("Téléphone à renseigner");
        descriptionMagasin.setText("Complétez les informations de votre magasin dans les paramètres.");

        if (emailMagasin != null) emailMagasin.setText("email@exemple.com");
        if (lienSiteWeb != null) lienSiteWeb.setText("www.votresite.com");
        if (localisationBadge != null) localisationBadge.setText("À définir");
    }

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
}