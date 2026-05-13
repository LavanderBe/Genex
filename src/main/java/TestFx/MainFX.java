package TestFx;

import Genex.Server.LocalHttpServer;
import Genex.utils.Myconnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Dashboard/dashboard.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setTitle("GENEX");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
            //primaryStage.setFullScreen(true);
        primaryStage.getIcons().add(new Image("Images/logo.png"));
        primaryStage.show();
    }
    @Override
    public void stop() throws Exception{
        Myconnection.closeConnection();
        LocalHttpServer.stop();
        super.stop();
        System.exit(0);
    }
    public static void main(String[] args) { launch(args); }
}