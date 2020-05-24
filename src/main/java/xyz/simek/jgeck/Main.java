package xyz.simek.jgeck;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import xyz.simek.jgeck.controller.ViewFactory;

import java.util.Objects;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ViewFactory viewFactory = ViewFactory.defaultFactory;

        Scene scene = viewFactory.getMainScene();

        stage.setScene(scene);
        stage.setTitle("Garden of Eden Creation Kit F2");
        stage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream("geck.png"))));
        stage.show();
    }
}
