package TestFx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // UPDATED: Load dashboard.fxml directly (lowercase as you requested)
        Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Dashboard/dashboard.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("GENEX");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}