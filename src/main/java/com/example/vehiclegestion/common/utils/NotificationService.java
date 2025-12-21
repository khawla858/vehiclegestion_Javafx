package com.example.vehiclegestion.common.utils;

import com.example.vehiclegestion.common.dao.NotificationDAO;
import com.example.vehiclegestion.common.model.Notification;
import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO;
    private static NotificationService instance;

    private NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    public static NotificationService getInstance() {
        if (instance == null) {
            instance = new NotificationService();
        }
        return instance;
    }

    // 🔔 NOTIFICATIONS POUR CLIENTS
    public void notifierNouveauVehiculeCorrespondant(int idClient, int idArticle, String titreVehicule) {
        System.out.println("🚗 Notification: Nouveau véhicule pour client " + idClient);

        Notification notif = new Notification(
                idClient,
                "client",
                "🚗 Nouveau véhicule correspondant",
                "Le véhicule \"" + titreVehicule + "\" correspond à vos critères de recherche",
                "nouveau_vehicule_correspondant",
                "vehicule"
        );
        notif.setIdSource(idArticle);
        notif.setTypeSource("article");
        notif.setLienAction("/vehicules/" + idArticle);

        creerEtNotifier(notif);
    }

    public void notifierBaissePrix(int idClient, int idArticle, String titreVehicule, double ancienPrix, double nouveauPrix) {
        System.out.println("💰 Notification: Baisse prix pour client " + idClient);

        double reduction = ((ancienPrix - nouveauPrix) / ancienPrix) * 100;

        Notification notif = new Notification(
                idClient,
                "client",
                "💰 Prix réduit sur un favori",
                String.format("Le véhicule \"%s\" a baissé de %.2f%% (%.2f€ → %.2f€)",
                        titreVehicule, reduction, ancienPrix, nouveauPrix),
                "baisse_prix_favori",
                "favori"
        );
        notif.setIdSource(idArticle);
        notif.setTypeSource("article");
        notif.setLienAction("/vehicules/" + idArticle);

        creerEtNotifier(notif);
    }

    public void notifierReservationConfirmee(int idClient, int idReservation, String details) {
        System.out.println("✅ Notification: Réservation confirmée pour client " + idClient);

        Notification notif = new Notification(
                idClient,
                "client",
                "✅ Réservation confirmée",
                "Votre réservation a été confirmée. " + details,
                "reservation_confirmee",
                "transaction"
        );
        notif.setIdSource(idReservation);
        notif.setTypeSource("reservation");
        notif.setPriorite("haute");
        notif.setLienAction("/reservations/" + idReservation);

        creerEtNotifier(notif);
    }

    public void notifierRappelRendezVous(int idClient, int idRdv, String dateRdv, String heureRdv) {
        System.out.println("📅 Notification: Rappel RDV pour client " + idClient);

        Notification notif = new Notification(
                idClient,
                "client",
                "📅 Rappel de rendez-vous",
                "Vous avez un rendez-vous le " + dateRdv + " à " + heureRdv + " pour un essai routier",
                "rappel_rendezvous",
                "transaction"
        );
        notif.setIdSource(idRdv);
        notif.setTypeSource("rendezvous");
        notif.setPriorite("haute");
        notif.setLienAction("/rendezvous/" + idRdv);

        creerEtNotifier(notif);
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

        creerEtNotifier(notif);
    }

    // 👔 NOTIFICATIONS POUR VENDEURS
    public void notifierNouvelleReservationVendeur(int idVendeur, int idReservation, String nomClient, String titreVehicule) {
        System.out.println("🛒 Notification vendeur: Nouvelle réservation");

        Notification notif = new Notification(
                idVendeur,
                "vendeur",
                "🛒 Nouvelle demande de réservation",
                nomClient + " souhaite réserver le véhicule \"" + titreVehicule + "\"",
                "nouvelle_reservation_vendeur",
                "transaction"
        );
        notif.setIdSource(idReservation);
        notif.setTypeSource("reservation");
        notif.setPriorite("haute");
        notif.setLienAction("/vendeur/reservations/" + idReservation);

        creerEtNotifier(notif);
    }

    public void notifierDemandeEssai(int idVendeur, int idRdv, String nomClient, String dateEssai) {
        System.out.println("🚗 Notification: Demande d'essai pour vendeur " + idVendeur);

        Notification notif = new Notification(
                idVendeur,
                "vendeur",
                "🚗 Demande d'essai routier",
                nomClient + " demande un essai routier le " + dateEssai,
                "demande_essai",
                "transaction"
        );
        notif.setIdSource(idRdv);
        notif.setTypeSource("rendezvous");
        notif.setPriorite("haute");
        notif.setLienAction("/vendeur/rendezvous/" + idRdv);

        creerEtNotifier(notif);
    }

    // 📱 MÉTHODES UTILITAIRES
    public List<Notification> getNotificationsUtilisateur(int idUtilisateur, boolean nonLuesSeulement) {
        System.out.println("📋 Récupération notifications pour user " + idUtilisateur);
        List<Notification> notifs = notificationDAO.getNotificationsUtilisateur(idUtilisateur, nonLuesSeulement);
        System.out.println("✅ " + notifs.size() + " notifications trouvées");
        return notifs;
    }

    public int getNombreNotificationsNonLues(int idUtilisateur) {
        int count = notificationDAO.getNombreNotificationsNonLues(idUtilisateur);
        System.out.println("🔢 Notifications non lues pour user " + idUtilisateur + ": " + count);
        return count;
    }

    public void marquerCommeLue(int idNotification) {
        System.out.println("📖 Marquer notification " + idNotification + " comme lue");
        notificationDAO.marquerCommeLue(idNotification);

        NotificationManager.getInstance().notifyNotificationRead(idNotification);
    }

    public void marquerToutesCommeLues(int idUtilisateur) {
        System.out.println("📖 Marquer toutes les notifications comme lues pour user " + idUtilisateur);
        notificationDAO.marquerToutesCommeLues(idUtilisateur);

        NotificationManager.getInstance().notifyNewNotification(idUtilisateur);
    }

    public void supprimerNotification(int idNotification) {
        System.out.println("🗑 Supprimer notification " + idNotification);
        notificationDAO.supprimerNotification(idNotification);
    }

    // 🔄 MÉTHODE PRIVÉE POUR CRÉER ET NOTIFIER
    public void creerEtNotifier(Notification notification) {
        try {
            System.out.println("🔔 Création notification:");
            System.out.println("   User ID: " + notification.getIdUtilisateur());
            System.out.println("   Rôle: " + notification.getRoleDestinataire());
            System.out.println("   Titre: " + notification.getTitre());
            System.out.println("   Message: " + notification.getMessage());

            // 1. Créer la notification en base
            notificationDAO.creerNotification(notification);

            System.out.println("✅ Notification créée avec ID: " + notification.getIdNotification());

            // 2. Notifier tous les écouteurs
            NotificationManager.getInstance().notifyNewNotification(notification.getIdUtilisateur());

            System.out.println("📢 Notification envoyée au NotificationManager");

        } catch (Exception e) {
            System.err.println("❌ Erreur création notification: " + e.getMessage());
            e.printStackTrace();
        }
    }



    // 📩 NOTIFICATION POUR VENDEUR : Nouvelle demande de RDV
    public void notifierNouvelleDemandeRendezVous(int idVendeur, int idRdv,
                                                  String nomClient, String titreVehicule,
                                                  String date, String heure) {
        System.out.println("📨 Notification vendeur: Nouvelle demande RDV");

        Notification notif = new Notification(
                idVendeur,
                "vendeur",
                "📅 Nouvelle demande de rendez-vous",
                nomClient + " demande un rendez-vous pour le véhicule \"" + titreVehicule +
                        "\" le " + date + " à " + heure,
                "nouvelle_demande_rdv",
                "transaction"
        );
        notif.setIdSource(idRdv);
        notif.setTypeSource("rendezvous");
        notif.setPriorite("haute");
        notif.setLienAction("/vendeur/rendezvous/" + idRdv + "/traiter");

        creerEtNotifier(notif);
    }

    // ✅ NOTIFICATION POUR CLIENT : Demande envoyée
    public void notifierConfirmationDemandeClient(int idClient, int idRdv, String message) {
        System.out.println("📨 Notification client: Demande envoyée");

        Notification notif = new Notification(
                idClient,
                "client",
                "📩 Demande de rendez-vous envoyée",
                message,
                "demande_rdv_envoyee",
                "transaction"
        );
        notif.setIdSource(idRdv);
        notif.setTypeSource("rendezvous");
        notif.setPriorite("normale");
        notif.setLienAction("/client/rendezvous/" + idRdv);

        creerEtNotifier(notif);
    }

    // ✅ NOTIFICATION POUR CLIENT : RDV accepté par vendeur
    public void notifierRendezVousAccepteClient(int idClient, int idRdv,
                                                String message, String date, String heure) {
        System.out.println("✅ Notification client: RDV accepté");

        Notification notif = new Notification(
                idClient,
                "client",
                "✅ Rendez-vous accepté",
                message + "\nDate: " + date + " à " + heure,
                "rdv_accepte",
                "transaction"
        );
        notif.setIdSource(idRdv);
        notif.setTypeSource("rendezvous");
        notif.setPriorite("haute");
        notif.setLienAction("/client/rendezvous/" + idRdv + "/confirmer");

        creerEtNotifier(notif);
    }

    // ❌ NOTIFICATION POUR CLIENT : RDV refusé par vendeur
    public void notifierRendezVousRefuseClient(int idClient, int idRdv,
                                               String message, String date, String heure) {
        System.out.println("❌ Notification client: RDV refusé");

        Notification notif = new Notification(
                idClient,
                "client",
                "❌ Rendez-vous refusé",
                message + "\nDate demandée: " + date + " à " + heure,
                "rdv_refuse",
                "transaction"
        );
        notif.setIdSource(idRdv);
        notif.setTypeSource("rendezvous");
        notif.setPriorite("normale");
        notif.setLienAction("/client/rendezvous/" + idRdv);

        creerEtNotifier(notif);
    }

    // 👍 NOTIFICATION POUR VENDEUR : Client confirme RDV
    public void notifierClientConfirmeRendezVous(int idVendeur, int idRdv,
                                                 String nomClient, String message) {
        System.out.println("👍 Notification vendeur: Client confirme RDV");

        Notification notif = new Notification(
                idVendeur,
                "vendeur",
                "👍 Rendez-vous confirmé",
                nomClient + " a confirmé le rendez-vous. " + message,
                "rdv_confirme_par_client",
                "transaction"
        );
        notif.setIdSource(idRdv);
        notif.setTypeSource("rendezvous");
        notif.setPriorite("normale");
        notif.setLienAction("/vendeur/rendezvous/" + idRdv);

        creerEtNotifier(notif);
    }

    // 🚫 NOTIFICATION POUR VENDEUR : Client annule RDV
    public void notifierClientAnnuleRendezVous(int idVendeur, int idRdv,
                                               String nomClient, String message) {
        System.out.println("🚫 Notification vendeur: Client annule RDV");

        Notification notif = new Notification(
                idVendeur,
                "vendeur",
                "🚫 Rendez-vous annulé",
                nomClient + " a annulé le rendez-vous. " + message,
                "rdv_annule_par_client",
                "transaction"
        );
        notif.setIdSource(idRdv);
        notif.setTypeSource("rendezvous");
        notif.setPriorite("urgente");
        notif.setLienAction("/vendeur/rendezvous/" + idRdv);

        creerEtNotifier(notif);
    }
}