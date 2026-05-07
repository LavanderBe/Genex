package Genex.Controllers.Login;

import Genex.entities.User;
import Genex.services.CrudUser;
import Genex.services.UserControl;
import Genex.utils.SessionManager;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class Login {

    @FXML
    private TextField EmailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label Errormail;

    @FXML
    private Label Errorpassword;

    @FXML
    private MediaView mediaView;

    @FXML
    private Button loginBtn;

    @FXML
    private StackPane rootPane;
    private MediaPlayer mediaPlayer;

    @FXML
    public void initialize() {
        try {
            String mediaPath = resolveLoginVideoPath();
            if (mediaPath == null) {
                System.err.println("CRITICAL: Video file not found at /Videos/Login.mp4");
                rootPane.setStyle("-fx-background-color: #050508;");
                return;
            }
            Media media = new Media(mediaPath);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true);
            mediaView.setMediaPlayer(mediaPlayer);
            mediaView.fitWidthProperty().bind(rootPane.widthProperty());
            mediaView.fitHeightProperty().bind(rootPane.heightProperty());
        } catch (Exception e) {
            System.err.println("Error initializing media: " + e.getMessage());
        }
    }

    private String resolveLoginVideoPath() {
        var resource = getClass().getResource("/Videos/Login.mp4");
        if (resource != null) {
            return resource.toExternalForm();
        }

        Path localPath = Path.of(System.getProperty("user.dir"), "src", "main", "resources", "Videos", "Login.mp4");
        File localFile = localPath.toFile();
        if (localFile.exists()) {
            return localFile.toURI().toString();
        }
        return null;
    }

    @FXML private void handleSignIn(ActionEvent event) {

        boolean valid = true;
        showError(Errorpassword, "");
        showError(Errormail, "");
        String email = EmailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            showError(Errormail, "L'email est requis");
            valid = false;
        } else if (!UserControl.isValidEmail(email)) {
            showError(Errormail, "Format d'email invalide");
            valid = false;
        }

        if (password.isEmpty()) {
            showError(Errorpassword, "Le mot de passe est requis");
            valid = false;
        }

        if (!valid) {
            shakeNode(rootPane.lookup("#loginCard"));
            return;
        }

        System.out.println("Login attempt: " + email);

        loginBtn.setText("Connexion...");
        loginBtn.setDisable(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        CrudUser cu=new CrudUser();
        if (cu.check_email(email)){
            User u=cu.getUser_withmail(email);
            if (u.verifyPassword(password)){
                SessionManager.getInstance().setCurrentUser(u);
                if (u.getRole().equals("admin"))
                {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Dashboard/dashboard.fxml"));
                    Parent root = null;
                    try {
                        root = loader.load();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Stage stage = (Stage) rootPane.getScene().getWindow();
                    double width = stage.getScene().getWidth();
                    double height = stage.getScene().getHeight();
                    Scene scene = new Scene(root, width, height);
                    scene.setFill(Color.TRANSPARENT);
                    stage.setScene(scene);
                    stage.setMaximized(true);
                    stage.show();
                }
                else{
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Dashboard/Player_dashboard.fxml"));
                    Parent root = null;
                    try {
                        root = loader.load();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Stage stage = (Stage) rootPane.getScene().getWindow();
                    double width = stage.getScene().getWidth();
                    double height = stage.getScene().getHeight();
                    Scene scene = new Scene(root, width, height);
                    scene.setFill(Color.TRANSPARENT);
                    stage.setScene(scene);
                    stage.setMaximized(true);
                    stage.show();
                }
            }
            else {
                showError(Errorpassword, "L'e-mail ou le mot de passe fourni est incorrect");
            }
        }
        else {
            showError(Errorpassword, "L'e-mail ou le mot de passe fourni est incorrect");
        }
        pause.setOnFinished(e -> {
            loginBtn.setText("Se connecter");
            loginBtn.setDisable(false);
        });
        pause.play();
    }
    @FXML private void handleForgotPassword(ActionEvent event) {  }
    @FXML void handlesignup(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Inscription/Inscription.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setAutoReverse(true);
        tt.setCycleCount(6);
        node.setRotate(0.5);
        tt.setOnFinished(e -> {
            node.setTranslateX(0);
            node.setRotate(0);});
        tt.play();
    }
    @FXML
    private void handleGoogleLogin(ActionEvent event) {
        System.out.println("Initiating Google OAuth...");
        // Logic:
        // 1. Open Browser: Desktop.getDesktop().browse(new URI("google_auth_url"));
        // 2. Start a temporary local server to catch the callback token
    }

    @FXML
    private void handleDiscordLogin(ActionEvent event) {
        System.out.println("Initiating Discord OAuth...");
    }
}
