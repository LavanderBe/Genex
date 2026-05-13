package Genex.Controllers.Login;

import Genex.Server.LocalHttpServer;
import Genex.entities.Player;
import Genex.entities.User;
import Genex.services.CrudPlayer;
import Genex.services.CrudUser;
import Genex.services.GoogleAuthService;
import Genex.services.UserControl;
import Genex.utils.HcaptchaVerifier;
import Genex.utils.SessionManager;
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
import javafx.scene.shape.SVGPath;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class Login {

    int nb_errors=3;


    @FXML private SVGPath captchabackground;

    @FXML private WebView captchaWebView;
    @FXML private Label Errorcaptcha;

    @FXML
    private TextField EmailField;

    @FXML private PasswordField passwordField;

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

    @FXML
    public void initialize() {
        try {
            // Start local server
            LocalHttpServer.start();
            String captchaUrl = "http://localhost:7654/captcha.html";
            captchaWebView.getEngine().load(captchaUrl);

            // 1. Try to get the resource
            var resource = getClass().getResource("/Videos/test.mp4");

            if (resource == null) {
                // This is what is happening now.
                System.err.println("CRITICAL: Video file not found at /Genex/Videos/background.mp4");
                // Optionally set a static background color so the app still runs
                rootPane.setStyle("-fx-background-color: #050508;");
                return;
            }

            String path = resource.toExternalForm();
            Media media = new Media(path);
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            mediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
            mediaPlayer.setMute(true);
            mediaPlayer.play();

            // Stretches video to fill screen
            mediaView.fitWidthProperty().bind(rootPane.widthProperty());
            mediaView.fitHeightProperty().bind(rootPane.heightProperty());

            mediaView.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                        if (newWindow != null) {
                            newWindow.setOnCloseRequest(e -> cleanup());
                        }
                    });
                }
            });

        } catch (Exception e) {
            System.err.println("Error initializing media: " + e.getMessage());
        }
    }

    @FXML private void handleSignIn(ActionEvent event) {
        boolean valid = true;
        showError(Errorpassword, "");
        showError(Errormail, "");
        String email = EmailField.getText().trim();
        String password = passwordField.getText();
        String token = (String) captchaWebView.getEngine().executeScript("getCaptchaToken()");

        if (token == null || token.isBlank()) {
            Errorcaptcha.setText("Veuillez compléter la vérification captcha");
            Errorcaptcha.setVisible(true);
            shakeNode(rootPane.lookup("#loginCard"));
            return;
        }

        boolean captchaValid = HcaptchaVerifier.verify(token, null);
        Errorcaptcha.setVisible(false);
        System.out.println("Captcha passed");
        if (nb_errors>0) {
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
                nb_errors--;
                return;
            }
            System.out.println("Login attempt: " + email);
            loginBtn.setText("Connexion...");
            loginBtn.setDisable(true);

            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            CrudUser cu = new CrudUser();
            if (cu.check_email(email)) {
                User u = cu.getUser_withmail(email);
                if (u.verifyPassword(password)) {
                    if (u.getRole().equals("admin")) {
                        SessionManager.getInstance().setCurrentUser(u);
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
                        cleanup();
                    } else {
                        Player p=new CrudPlayer().getPlayerInfo(u.getId());
                        SessionManager.getInstance().setCurrentUser(p);
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
                        cleanup();
                    }
                } else {
                    showError(Errorpassword, "L'e-mail ou le mot de passe fourni est incorrect");
                    shakeNode(rootPane.lookup("#loginCard"));
                    nb_errors--;
                }
            } else {
                showError(Errorpassword, "L'e-mail ou le mot de passe fourni est incorrect");
                shakeNode(rootPane.lookup("#loginCard"));
                nb_errors--;
            }
            pause.setOnFinished(e -> {
                loginBtn.setText("Se connecter");
                loginBtn.setDisable(false);
            });
            pause.play();
            if (nb_errors <= 0) {
                loginBtn.setDisable(true);
            }
        }
        if (nb_errors <= 0) {
            loginBtn.setDisable(true);
            loginBtn.setText("Session Bloquée");
        }
    }

    @FXML private void handleForgotPassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Login/ForgotPassword.fxml"));
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

    @FXML private void handleGoogleLogin(ActionEvent event) {
        loginBtn.setDisable(true);
        loginBtn.setText("BROWSER_UPLINK_OPEN...");

        new Thread(() -> {
            try {
                GoogleAuthService service = new GoogleAuthService();
                var googleUser = service.getUserInfo();

                // Jump back to the UI thread once we have the data
                javafx.application.Platform.runLater(() -> {
                    syncWithDatabase(googleUser.getEmail(), googleUser.getGivenName());
                });

            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> loginBtn.setDisable(false));
            }
        }).start();
    }

    private void syncWithDatabase(String email, String name) {
        System.out.println(email+" "+name);
        CrudUser cu = new CrudUser();
        User u;
        Player p;
        if (cu.check_email(email)) {
            u=cu.getUser_withmail(email);
            p=new CrudPlayer().getPlayerInfo(u.getId());
        } else {
            p=new Player();
            p.setUsername(name);
            p.setEmail(email);
            p.setRole("player");
            p.setNickname(name);
            p.setCin("000000000");
            p.setPassword_hash("google auth");
            p.setSalt("google auth");
            p.setBirthday(LocalDate.now());
            p.setNom(name);
            p.setPrenom(name);
            p.setCreated_at(LocalDateTime.now());
            new CrudPlayer().addPlayer_admin(p);
        }
        SessionManager.getInstance().setCurrentUser(p);
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
        cleanup();
    }

    @FXML private void handleDiscordLogin(ActionEvent event) {
        System.out.println("Initiating Discord OAuth...");
    }

    public void cleanup() {
        MediaPlayer player = mediaView.getMediaPlayer();
        if (player != null) {
            player.stop();
            player.dispose();
            mediaView.setMediaPlayer(null);
            System.out.println("[Cleanup Mediaview] Mediaview stopped.");
        }
        if (captchaWebView != null) {
            captchaWebView.getEngine().load(null);
            captchaWebView.getEngine().executeScript("if(window.hcaptcha) hcaptcha.reset();");
            System.out.println("[Cleanup Webview] Mediaview stopped.");
        }
    }

    @FXML
    void handleCaptcha(ActionEvent event) {
        captchaWebView.setVisible(true);
        captchabackground.setVisible(true);
    }
}