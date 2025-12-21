package com.example.vehiclegestion.utils;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


public class EX1 extends Application {
    @Override
  public void start(Stage stage ){
        TextField tf1 = new TextField();
        TextField tf2 = new TextField();

        Button btnCopier = new Button("Copier");
        Button btnCouper = new Button("Couper");
        Button btnFermer = new Button("Fermer");

        btnCopier.setOnAction(e -> {
            tf2.setText(tf1.getText());
        });

        btnCouper.setOnAction( e->{
            tf2.setText(tf1.getText());
            tf1.clear();
        });
        btnFermer.setOnAction(e->{
            stage.close();
        });

        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        grid.add(new Label("Texte1"),0,0);
        grid.add(tf1,1,0);

        grid.add(new Label("Texte2"),0,1);
        grid.add(tf2,1,1);

        grid.add(btnCopier,0,2);
        grid.add(btnCouper,1,2);
        grid.add(btnFermer,2,2);

        Scene scene = new Scene(grid , 400 , 200);
        stage.setScene(scene);
        stage.setTitle("Copier / Couper ");
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}