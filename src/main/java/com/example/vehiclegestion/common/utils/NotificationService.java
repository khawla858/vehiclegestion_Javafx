package com.example.vehiclegestion.common.utils;

import com.example.vehiclegestion.common.dao.NotificationDAO;
import com.example.vehiclegestion.common.model.Notification;

import java.util.List;

/**
 * Service pour gérer les notifications
 */
public class NotificationService {

    private final NotificationDAO notificationDAO;
    private static NotificationService instance;
    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    /**
     * Créer une notification manuelle
     */
    public void creerNotification(Notification notification) {
        System.out.println("📢 Création notification: " + notification.getTitre());
        notificationDAO.creerNotification(notification);
    }

    /**
     * Récupérer les notifications d'un utilisateur
     */
    public List<Notification> getNotificationsUtilisateur(int idUtilisateur, boolean nonLuesSeulement) {
        return notificationDAO.getNotificationsUtilisateur(idUtilisateur, nonLuesSeulement);
    }

    /**
     * Compter les notifications non lues
     */
    public int compterNotificationsNonLues(int idUtilisateur) {
        return notificationDAO.getNombreNotificationsNonLues(idUtilisateur);
    }

    /**
     * Marquer une notification comme lue
     */
    public void marquerCommeLue(int idNotification) {
        notificationDAO.marquerCommeLue(idNotification);
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    public void marquerToutesCommeLues(int idUtilisateur) {
        notificationDAO.marquerToutesCommeLues(idUtilisateur);
    }

    /**
     * Supprimer une notification
     */
    public void supprimerNotification(int idNotification) {
        notificationDAO.supprimerNotification(idNotification);
    }

    // ==================== NOTIFICATIONS SPÉCIFIQUES ====================

    /**
     * Notifier les admins d'une nouvelle demande de magasin
     */
    public void notifierDemandeMagasin(int idVendeur, String nomMagasin, int idMagasin) {
        System.out.println("🏪 Notification demande magasin: " + nomMagasin);

        Notification notification = new Notification();
        notification.setRoleDestinataire("admin");
        notification.setIdSource(idMagasin);
        notification.setTypeSource("magasin");
        notification.setTitre("🏪 Nouvelle demande de magasin");
        notification.setMessage("Le vendeur (ID: " + idVendeur + ") a créé un nouveau magasin : " + nomMagasin);
        notification.setTypeNotification("demande");
        notification.setCategorie("magasin");
        notification.setPriorite("haute");
        notification.setLienAction("/admin/magasins/" + idMagasin);

        // Cette notification sera créée automatiquement par le trigger
        // Mais on peut aussi la créer manuellement si besoin
    }

    /**
     * Notifier les admins d'une nouvelle plainte
     */
    public void notifierNouvellePlaine(int idPlainte, int idClient, int idVendeur, String description) {
        System.out.println("⚠️ Notification nouvelle plainte ID: " + idPlainte);

        Notification notification = new Notification();
        notification.setRoleDestinataire("admin");
        notification.setIdSource(idPlainte);
        notification.setTypeSource("plainte");
        notification.setTitre("⚠️ Nouvelle plainte reçue");
        notification.setMessage("Une plainte a été déposée par le client (ID: " + idClient + ") contre le vendeur (ID: " + idVendeur + ")");
        notification.setTypeNotification("alerte");
        notification.setCategorie("plainte");
        notification.setPriorite("urgente");
        notification.setLienAction("/admin/plaintes/" + idPlainte);

        // Cette notification sera créée automatiquement par le trigger
    }


    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }
    public int getNombreNotificationsNonLues(int idUtilisateur) {
        int count = notificationDAO.getNombreNotificationsNonLues(idUtilisateur);
        System.out.println("🔢 Notifications non lues pour user " + idUtilisateur + ": " + count);
        return count;
    }
    /**
     * Notifier un vendeur d'une plainte contre lui
     */
    public void notifierVendeurPlaine(int idVendeur, int idPlainte) {
        System.out.println("⚠️ Notification plainte au vendeur ID: " + idVendeur);

        Notification notification = new Notification();
        notification.setIdUtilisateur(idVendeur);
        notification.setRoleDestinataire("vendeur");
        notification.setIdSource(idPlainte);
        notification.setTypeSource("plainte");
        notification.setTitre("⚠️ Plainte reçue");
        notification.setMessage("Une plainte a été déposée contre votre activité. Veuillez consulter les détails.");
        notification.setTypeNotification("alerte");
        notification.setCategorie("plainte");
        notification.setPriorite("haute");
        notification.setLienAction("/vendeur/plaintes/" + idPlainte);

        notificationDAO.creerNotification(notification);
    }

    /**
     * Notifier un client de la réponse à sa plainte
     */
    public void notifierClientReponsePlaine(int idClient, int idPlainte, String statutPlainte) {
        System.out.println("✅ Notification réponse plainte au client ID: " + idClient);

        String emoji = "✅";
        String titre = "Mise à jour de votre plainte";

        if (statutPlainte.equals("résolue")) {
            emoji = "✅";
            titre = "Plainte résolue";
        } else if (statutPlainte.equals("rejetée")) {
            emoji = "❌";
            titre = "Plainte rejetée";
        }

        Notification notification = new Notification();
        notification.setIdUtilisateur(idClient);
        notification.setRoleDestinataire("client");
        notification.setIdSource(idPlainte);
        notification.setTypeSource("plainte");
        notification.setTitre(emoji + " " + titre);
        notification.setMessage("Votre plainte a été " + statutPlainte + " par un administrateur.");
        notification.setTypeNotification("info");
        notification.setCategorie("plainte");
        notification.setPriorite("normale");
        notification.setLienAction("/client/plaintes/" + idPlainte);

        notificationDAO.creerNotification(notification);
    }

    /**
     * Notifier les admins d'une nouvelle inscription
     */
    public void notifierNouvelleInscription(int idUtilisateur, String prenom, String nom, String role) {
        System.out.println("👤 Notification nouvelle inscription: " + prenom + " " + nom);

        Notification notification = new Notification();
        notification.setRoleDestinataire("admin");
        notification.setIdSource(idUtilisateur);
        notification.setTypeSource("utilisateur");
        notification.setTitre("👤 Nouvel utilisateur inscrit");
        notification.setMessage(prenom + " " + nom + " (" + role + ") vient de s'inscrire.");
        notification.setTypeNotification("info");
        notification.setCategorie("utilisateur");
        notification.setPriorite("normale");
        notification.setLienAction("/admin/utilisateurs/" + idUtilisateur);

        // Cette notification sera créée automatiquement par le trigger
    }

    /**
     * Tester la connexion à la base de données
     */
    public boolean testerConnexion() {
        return notificationDAO.testerConnexion();
    }
    public void notifierNouveauMessage(int idClient, int idExpediteur, String nomExpediteur) {
        System.out.println("💬 Notification: Nouveau message pour client " + idClient + " de " + nomExpediteur);

        Notification notif = new Notification(
                idClient,
                "client",
                "💬 Nouveau message",
                "Vous avez reçu un nouveau message de " + nomExpediteur,
                "nouveau_message",
                "message"
        );
        notif.setIdSource(idExpediteur);
        notif.setTypeSource("message");
        notif.setPriorite("haute");
        notif.setLienAction("/messages");

        notificationDAO.creerNotification(notif);    }
}