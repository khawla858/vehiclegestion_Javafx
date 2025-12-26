package com.example.vehiclegestion.vendeur.controller;
import com.example.vehiclegestion.auth.utils.SessionManager;
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
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;

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
    @FXML private WebView mapView;
    // ✅ ImageView au lieu de WebView
    @FXML private Label etageInfo;
    @FXML private Button btnItineraire;
    @FXML private Button btnVoirCarte;
    @FXML private Button btnRetour;
    @FXML private Button btnVoirVehicules;

    private Magasin magasin;
    private MagasinDAO magasinDAO;
    private NavigationManager nav = NavigationManager.getInstance();

    @FXML
    public void initialize() {
        try {
            magasinDAO = new MagasinDAO();
            configurerActions();

            if (mapView != null) {
                mapView.getEngine().setJavaScriptEnabled(true);
                System.out.println("✅ WebView initialisé pour la carte");
            }

            if (btnRetour != null) {
                btnRetour.setOnAction(e -> nav.goBack());
                btnRetour.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
            }
            if (btnVoirVehicules != null) {
                btnVoirVehicules.setOnAction(e -> voirVehiculesMagasin());
                btnVoirVehicules.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; " +
                        "-fx-padding: 10 20; -fx-background-radius: 8; -fx-font-weight: bold; -fx-cursor: hand;");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur d'initialisation : " + e.getMessage());
        }
    }

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

    private void voirVehiculesMagasin() {
        if (magasin == null) {
            showError("Aucun magasin sélectionné");
            return;
        }

        System.out.println("🚗 Navigation vers véhicules du magasin: " + magasin.getNomMagasin());
        SessionManager.getInstance().setCurrentMagasinId(magasin.getIdMagasin());
        SessionManager.getInstance().setCurrentMagasinNom(magasin.getNomMagasin());
        NavigationManager.getInstance().navigateWithData("/view/vendeur/VendeurVehicle.fxml", null);
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
        chargerCarteReelle();
        // ✅ Méthode originale pour ImageView
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

    // Remplacez ENTIÈREMENT la méthode chargerCarteReelle() par celle-ci :

    private void chargerCarteReelle() {
        if (magasin == null) {
            System.err.println("❌ Magasin null");
            return;
        }

        try {
            // Coordonnées par défaut pour Rabat, Maroc
            double defaultLat = 34.0209;
            double defaultLon = -6.8416;

            String nomMagasin = magasin.getNomMagasin() != null ? magasin.getNomMagasin() : "Magasin";
            String adresseAffichage = magasin.getAdresse() != null ? magasin.getAdresse() : "";

            System.out.println("🗺️ Chargement carte pour : " + nomMagasin);

            // HTML avec Leaflet - CARTE PURE SANS INTERFACE
            String htmlContent =
                    "<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<head>\n" +
                            "    <meta charset='utf-8'/>\n" +
                            "    <title>Carte</title>\n" +
                            "    <link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css' " +
                            "          integrity='sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=' crossorigin=''/>\n" +
                            "    <script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js' " +
                            "            integrity='sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=' crossorigin=''></script>\n" +
                            "    <style>\n" +
                            "        * { \n" +
                            "            margin: 0; \n" +
                            "            padding: 0; \n" +
                            "            box-sizing: border-box; \n" +
                            "        }\n" +
                            "        html, body { \n" +
                            "            width: 100%; \n" +
                            "            height: 100%; \n" +
                            "            overflow: hidden; \n" +
                            "        }\n" +
                            "        #map { \n" +
                            "            width: 100%; \n" +
                            "            height: 100%; \n" +
                            "            position: absolute;\n" +
                            "            top: 0;\n" +
                            "            left: 0;\n" +
                            "        }\n" +
                            "        /* Masquer le logo Leaflet et les attributions */\n" +
                            "        .leaflet-control-attribution,\n" +
                            "        .leaflet-control-zoom a {\n" +
                            "            font-size: 10px !important;\n" +
                            "        }\n" +
                            "    </style>\n" +
                            "</head>\n" +
                            "<body>\n" +
                            "    <div id='map'></div>\n" +
                            "    <script>\n" +
                            "        try {\n" +
                            "            // Créer la carte avec contrôles minimaux\n" +
                            "            var map = L.map('map', {\n" +
                            "                zoomControl: true,\n" +
                            "                attributionControl: false,  // Masquer les attributions\n" +
                            "                scrollWheelZoom: true,\n" +
                            "                doubleClickZoom: true,\n" +
                            "                boxZoom: true,\n" +
                            "                keyboard: true,\n" +
                            "                dragging: true\n" +
                            "            }).setView([" + defaultLat + ", " + defaultLon + "], 15);\n" +
                            "            \n" +
                            "            // Ajouter les tuiles OpenStreetMap\n" +
                            "            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                            "                maxZoom: 19,\n" +
                            "                minZoom: 10\n" +
                            "            }).addTo(map);\n" +
                            "            \n" +
                            "            // Créer une icône personnalisée pour le marqueur\n" +
                            "            var customIcon = L.icon({\n" +
                            "                iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',\n" +
                            "                shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',\n" +
                            "                iconSize: [25, 41],\n" +
                            "                iconAnchor: [12, 41],\n" +
                            "                popupAnchor: [1, -34],\n" +
                            "                shadowSize: [41, 41]\n" +
                            "            });\n" +
                            "            \n" +
                            "            // Ajouter un marqueur\n" +
                            "            var marker = L.marker([" + defaultLat + ", " + defaultLon + "], {icon: customIcon}).addTo(map);\n" +
                            "            \n" +
                            "            // Popup avec infos du magasin\n" +
                            "            marker.bindPopup(\n" +
                            "                '<div style=\"text-align: center; font-family: Arial;\">' +\n" +
                            "                '<b style=\"font-size: 16px; color: #1e293b;\">" + nomMagasin.replace("'", "\\'") + "</b><br>' +\n" +
                            "                '<span style=\"font-size: 12px; color: #64748b;\">" + adresseAffichage.replace("'", "\\'") + "</span>' +\n" +
                            "                '</div>'\n" +
                            "            ).openPopup();\n" +
                            "            \n" +
                            "            // Forcer le redimensionnement après chargement\n" +
                            "            setTimeout(function() {\n" +
                            "                map.invalidateSize();\n" +
                            "                console.log('✅ Carte redimensionnée');\n" +
                            "            }, 200);\n" +
                            "            \n" +
                            "            console.log('✅ Carte Leaflet chargée avec succès');\n" +
                            "            \n" +
                            "        } catch(e) {\n" +
                            "            console.error('❌ Erreur carte:', e);\n" +
                            "        }\n" +
                            "    </script>\n" +
                            "</body>\n" +
                            "</html>";

            // Activer JavaScript pour le WebView
            mapView.getEngine().setJavaScriptEnabled(true);

            // Charger le contenu HTML
            mapView.getEngine().loadContent(htmlContent);

            // Ajouter un listener pour suivre le chargement
            mapView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    System.out.println("✅ WebView - Carte chargée avec succès");
                    // Forcer le redimensionnement du WebView
                    javafx.application.Platform.runLater(() -> {
                        mapView.getEngine().executeScript("if(typeof map !== 'undefined') map.invalidateSize();");
                    });
                } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                    System.err.println("❌ WebView - Échec du chargement");
                }
            });

            System.out.println("✅ Initialisation carte terminée");

        } catch (Exception e) {
            System.err.println("❌ Erreur chargement carte : " + e.getMessage());
            e.printStackTrace();
        }
    }



}