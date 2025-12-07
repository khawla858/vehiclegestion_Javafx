package com.example.vehiclegestion.vendeur.controller;

import com.example.vehiclegestion.auth.SessionManager;
import com.example.vehiclegestion.vendeur.dao.UtilisateurDAO; // ✅ Importer le DAO
import com.example.vehiclegestion.vendeur.dao.DemandeMagasinDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

public class AjouterDemandeMagasinController {

    @FXML
    private TextField nomMagasin, adresse, localisation, photoProfil;
    @FXML
    private TextField nomVendeur, emailVendeur, telVendeur;
    @FXML
    private TextArea description;
    @FXML
    private Button btnEnvoyer;
    @FXML
    private Label lblTitre, lblSousTitre;

    private SessionManager session;
    private int idVendeur;

    @FXML
    public void initialize() {
        System.out.println("🏪 Initialisation formulaire demande magasin");

        session = SessionManager.getInstance();

        // Vérifier la session
        if (!session.estConnecte() || !session.estVendeur()) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Vous devez être connecté en tant que vendeur pour créer une demande.");
            return;
        }

        idVendeur = session.getUserId();

        // Configurer les styles
        setupStyles();

        // Pré-remplir les informations du vendeur depuis la base de données
        preRemplirInfosVendeur();

        System.out.println("✅ Formulaire initialisé pour vendeur ID: " + idVendeur);
    }

    private void setupStyles() {
        if (lblTitre != null) {
            lblTitre.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        }

        if (lblSousTitre != null) {
            lblSousTitre.setStyle("-fx-font-size: 14px; -fx-text-fill: #4b5563;");
        }

        // Style pour les champs du vendeur (lecture seule)
        nomVendeur.setStyle("-fx-background-color: #f3f4f6; -fx-border-color: #d1d5db; -fx-border-radius: 5;");
        emailVendeur.setStyle("-fx-background-color: #f3f4f6; -fx-border-color: #d1d5db; -fx-border-radius: 5;");
        telVendeur.setStyle("-fx-background-color: #f3f4f6; -fx-border-color: #d1d5db; -fx-border-radius: 5;");

        // Style pour le bouton
        btnEnvoyer.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        btnEnvoyer.setOnMouseEntered(e -> btnEnvoyer.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"));
        btnEnvoyer.setOnMouseExited(e -> btnEnvoyer.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;"));
    }

    private void preRemplirInfosVendeur() {
        System.out.println("🔄 Pré-remplissage infos vendeur depuis la base de données...");

        try {
            // Récupérer les informations complètes depuis la base de données
            UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
            com.example.vehiclegestion.auth.model.Utilisateur utilisateur =
                    utilisateurDAO.getUtilisateurById(idVendeur);

            if (utilisateur == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Impossible de récupérer les informations du vendeur.");
                return;
            }

            System.out.println("📋 Infos récupérées de la DB:");
            System.out.println("   👤 Nom: " + utilisateur.getNom());
            System.out.println("   👤 Prénom: " + utilisateur.getPrenom());
            System.out.println("   📧 Email: " + utilisateur.getEmail());
            System.out.println("   📱 Téléphone: " + utilisateur.getTelephone());

            // Nom complet
            String nomComplet = utilisateur.getPrenom() + " " + utilisateur.getNom();
            nomVendeur.setText(nomComplet);

            // Email
            emailVendeur.setText(utilisateur.getEmail());

            // Téléphone - Vérifier si null ou vide
            String telephone = utilisateur.getTelephone();
            if (telephone == null || telephone.trim().isEmpty()) {
                telephone = "Non renseigné";
                System.out.println("⚠️ Téléphone non renseigné pour l'utilisateur");
            }
            telVendeur.setText(telephone);

            // Rendre les champs en lecture seule
            nomVendeur.setEditable(false);
            emailVendeur.setEditable(false);
            telVendeur.setEditable(false);

            System.out.println("✅ Infos vendeur pré-remplies depuis la DB");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des infos vendeur: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de charger les informations du vendeur depuis la base de données.");
        }
    }

    @FXML
    public void envoyerDemande() {
        System.out.println("📤 Envoi de la demande magasin...");

        // Vérifier la session
        if (!session.estConnecte() || !session.estVendeur()) {
            showAlert(Alert.AlertType.ERROR, "Session expirée",
                    "Veuillez vous reconnecter pour envoyer une demande.");
            return;
        }

        // Validation des champs
        String nom = nomMagasin.getText().trim();
        String adr = adresse.getText().trim();
        String loc = localisation.getText().trim();
        String desc = description.getText().trim();
        String photo = photoProfil.getText().trim();

        if (nom.isEmpty() || adr.isEmpty() || loc.isEmpty() || desc.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Champs obligatoires",
                    "Veuillez remplir tous les champs obligatoires (*)");

            // Mettre en évidence les champs vides
            if (nom.isEmpty()) nomMagasin.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            if (adr.isEmpty()) adresse.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            if (loc.isEmpty()) localisation.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");
            if (desc.isEmpty()) description.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2;");

            return;
        }

        try {
            // Vérifier si le vendeur a déjà une demande en attente
            DemandeMagasinDAO demandeDAO = new DemandeMagasinDAO();
            if (demandeDAO.hasDemandePending(idVendeur)) {
                showAlert(Alert.AlertType.WARNING, "Demande existante",
                        "Vous avez déjà une demande de magasin en attente de validation.\n" +
                                "Veuillez patienter la réponse de l'administrateur.");
                return;
            }

            // Envoyer la demande
            boolean success = demandeDAO.addDemande(idVendeur, nom, adr, loc, desc, photo);

            if (success) {
                System.out.println("✅ Demande envoyée avec succès pour: " + nom);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Demande envoyée");
                successAlert.setHeaderText("✅ Demande enregistrée avec succès !");
                successAlert.setContentText("Votre demande pour créer le magasin \"" + nom + "\" a été envoyée.\n" +
                        "Vous serez notifié par email une fois la demande traitée.");
                successAlert.showAndWait();

                // Réinitialiser le formulaire
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue lors de l'envoi de la demande.\n" +
                                "Veuillez réessayer ou contacter l'administrateur.");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi de la demande: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur technique",
                    "Erreur: " + e.getMessage());
        }
    }

    private void clearForm() {
        nomMagasin.clear();
        adresse.clear();
        localisation.clear();
        description.clear();
        photoProfil.clear();

        // Réinitialiser les styles
        nomMagasin.setStyle("");
        adresse.setStyle("");
        localisation.setStyle("");
        description.setStyle("");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ✅ Ajouter cette méthode si elle n'existe pas
    public void setVendeurId(int idVendeur) {
        this.idVendeur = idVendeur;
    }
}