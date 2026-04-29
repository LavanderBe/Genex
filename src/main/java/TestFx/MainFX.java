package TestFx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Forum/Forum.fxml"));
        Scene scene = new Scene(root, 1280, 720);
        primaryStage.setTitle("GENEX - Forum");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    /*hazebaheba*/
    public static void main(String[] args) { launch(args); }
}
