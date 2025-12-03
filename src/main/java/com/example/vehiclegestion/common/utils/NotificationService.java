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
                "Vous avez un rendez-vous demain à " + heureRdv + " pour un essai routier",
                "rappel_rendezvous",
                "transaction"
        );
        notif.setIdSource(idRdv);
        notif.setTypeSource("rendezvous");
        notif.setPriorite("urgente");
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

    // 👔 NOTIFICATIONS POUR VENDEURS (gardées pour référence)
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

    // 📱 MÉTHODES UTILITAIRES
    public List<Notification> getNotificationsUtilisateur(int idUtilisateur, boolean nonLuesSeulement) {
        return notificationDAO.getNotificationsUtilisateur(idUtilisateur, nonLuesSeulement);
    }

    public int getNombreNotificationsNonLues(int idUtilisateur) {
        return notificationDAO.getNombreNotificationsNonLues(idUtilisateur);
    }

    public void marquerCommeLue(int idNotification) {
        notificationDAO.marquerCommeLue(idNotification);

        // Notifier que la notification a été lue
        NotificationManager.getInstance().notifyNotificationRead(idNotification);
    }

    public void marquerToutesCommeLues(int idUtilisateur) {
        notificationDAO.marquerToutesCommeLues(idUtilisateur);

        // Notifier que toutes les notifications ont été lues
        NotificationManager.getInstance().notifyNewNotification(idUtilisateur);
    }

    public void supprimerNotification(int idNotification) {
        notificationDAO.supprimerNotification(idNotification);
    }

    // 🔄 MÉTHODE PRIVÉE POUR CRÉER ET NOTIFIER
    private void creerEtNotifier(Notification notification) {
        // 1. Créer la notification en base
        notificationDAO.creerNotification(notification);

        // 2. Notifier tous les écouteurs
        NotificationManager.getInstance().notifyNewNotification(notification.getIdUtilisateur());

        System.out.println("📢 Notification créée et notifiée: ID=" + notification.getIdNotification() +
                ", User=" + notification.getIdUtilisateur());
    }
    // Dans NotificationService.java, ajoutez cette méthode :

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
}
