package com.example.vehiclegestion.vendeur.controller.layout;

import com.example.vehiclegestion.vendeur.model.Article;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ArticleCardController {

    @FXML private ImageView imageView;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label descLabel;

    private Article article;

    public void setData(Article article) {
        this.article = article;
        titleLabel.setText(article.getTitre());
        priceLabel.setText(String.format("%.2f MAD", article.getPrix()));
        descLabel.setText(article.getDescription());

        // Charger l'image depuis les ressources
        String imagePath = "/com/example/vehiclegestion/images/vehicules" + article.getImage();
        imageView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
    }

    @FXML
    private void editArticle() {
        System.out.println("Modifier article: " + article.getId());
    }

    @FXML
    private void deleteArticle() {
        System.out.println("Supprimer article: " + article.getId());
    }
}
