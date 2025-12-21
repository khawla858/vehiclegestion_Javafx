package com.example.vehiclegestion.common.utils;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestionnaire centralisé des notifications en temps réel
 */
public class NotificationManager {
    private static NotificationManager instance;
    private final List<NotificationListener> listeners = new ArrayList<>();

    private NotificationManager() {}

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void addListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NotificationListener listener) {
        listeners.remove(listener);
    }

    public void notifyNewNotification(int userId) {
        System.out.println("🔔 NotificationManager: Nouvelle notification pour user " + userId);

        Platform.runLater(() -> {
            for (NotificationListener listener : listeners) {
                try {
                    listener.onNewNotification(userId);
                } catch (Exception e) {
                    System.err.println("Erreur dans listener: " + e.getMessage());
                }
            }
        });
    }

    public void notifyNotificationRead(int notificationId) {
        Platform.runLater(() -> {
            for (NotificationListener listener : listeners) {
                listener.onNotificationRead(notificationId);
            }
        });
    }

    public interface NotificationListener {
        void onNewNotification(int userId);
        void onNotificationRead(int notificationId);
    }
}