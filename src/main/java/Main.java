import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        AsciiArtistController primaryCont = new AsciiArtistController(primaryStage);
        GridPane root = primaryCont.initGetGrid();

        Scene mainScene = new Scene(root, 1100, 800);
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/resources/icon.png")));
        primaryStage.getIcons().add(icon);
        primaryStage.setTitle("ASCIIArtist");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
