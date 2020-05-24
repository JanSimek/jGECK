package xyz.simek.jgeck.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class ImagePane extends BorderPane {

    public ImagePane(Image image) {

        ImageView view = new ImageView();
        view.setImage(image);
        setCenter(view);
    }
}
