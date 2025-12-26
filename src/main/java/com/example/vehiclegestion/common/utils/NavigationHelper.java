package com.example.vehiclegestion.common.utils;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class NavigationHelper {

    public static void openChat(Stage owner) {
        Platform.runLater(() -> {
            try {
                String[] possiblePaths = {
                        "/view/common/ChatWindow.fxml",
                        "/com/example/vehiclegestion/view/common/ChatWindow.fxml",
                        "view/common/ChatWindow.fxml"
                };

                FXMLLoader loader = null;
                Parent chatRoot = null;

                for (String path : possiblePaths) {
                    try {
                        URL url = NavigationHelper.class.getResource(path);
                        if (url != null) {
                            loader = new FXMLLoader(url);
                            chatRoot = loader.load();
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Échec chemin: " + path);
                    }
                }

                if (chatRoot != null) {
                    Stage chatStage = new Stage();
                    chatStage.setTitle("Messages");
                    chatStage.setScene(new Scene(chatRoot, 1000, 700));
                    chatStage.initOwner(owner);
                    chatStage.show();
                }
            } catch (Exception e) {
                System.err.println("Erreur ouverture chat: " + e.getMessage());
            }
        });
    }
}