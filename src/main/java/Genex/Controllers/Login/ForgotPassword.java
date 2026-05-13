package Genex.Controllers.Login;

import Genex.entities.User;
import Genex.services.CrudUser;
import Genex.services.UserControl;
import Genex.utils.EmailSystem;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class ForgotPassword {
    @FXML private StackPane rootPane;
    @FXML
    private AnchorPane mainCard;
    @FXML private MediaView mediaView;

    @FXML private Button resendBtn;

    private int cooldownSeconds = 60;
    private Timeline cooldownTimeline;

    // Form Elements
    @FXML private TextField emailField;
    @FXML private VBox otpSection;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button actionBtn;
    @FXML private Label statusLabel;

    private String generatedCode;
    private boolean isCodeSent = false;
    private final CrudUser crudUser = new CrudUser();

    @FXML
    public void initialize() {
        setupBackgroundVideo();
    }

    private void setupBackgroundVideo() {
        try {
            var resource = getClass().getResource("/Videos/test.mp4");
            if (resource != null) {
                Media media = new Media(resource.toExternalForm());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaView.setMediaPlayer(mediaPlayer);
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                mediaPlayer.setMute(true);
                mediaPlayer.play();
                mediaView.fitWidthProperty().bind(rootPane.widthProperty());
                mediaView.fitHeightProperty().bind(rootPane.heightProperty());
            }
        } catch (Exception e) {
            rootPane.setStyle("-fx-background-color: #050508;");
        }
    }

    @FXML
    private void handleAction(ActionEvent event) {
        statusLabel.setVisible(false);
        String email = emailField.getText().trim();

        if (!isCodeSent) {
            // --- PHASE 1: SEND CODE ---
            if (email.isEmpty() || !UserControl.isValidEmail(email)) {
                showStatus("FORMAT D'EMAIL INVALIDE", true);
                shakeNode(mainCard);
                return;
            }
            if (!crudUser.check_email(email)) {
                showStatus("IDENTIFIANT NON RECONNU DANS LA DATABASE", true);
                shakeNode(mainCard);
                return;
            }
            if (crudUser.getUser_withmail(email).getRole().equals("admin")){
                showStatus("IDENTIFIANT NON RECONNU DANS LA DATABASE", true);
                shakeNode(mainCard);
                return;
            }
            generatedCode = String.valueOf((int) (Math.random() * 900000) + 100000);
            System.out.println(generatedCode);
            actionBtn.setDisable(true);
            actionBtn.setText("ENVOI DU CODE...");
            new Thread(() -> {
                EmailSystem.SendForgotPassword(email,generatedCode);
                Platform.runLater(() -> {
                    showStatus("CODE DE DÉCRYPTAGE ENVOYÉ À L'ADRESSE", false);
                    switchToResetMode();
                });
            }).start();

        } else {
            // --- PHASE 2: RESET PASSWORD ---
            String enteredCode = codeField.getText().trim();
            String newPwd = newPasswordField.getText();
            String confirmPwd = confirmPasswordField.getText();

            if (!enteredCode.equals(generatedCode)) {
                showStatus("CODE DE DÉCRYPTAGE INCORRECT", true);
                shakeNode(mainCard);
                return;
            }

            if (newPwd.isEmpty() || newPwd.length() < 6) {
                showStatus("L'ACCESS_KEY DOIT CONTENIR AU MOINS 6 CARACTÈRES", true);
                shakeNode(mainCard);
                return;
            }

            if (!newPwd.equals(confirmPwd)) {
                showStatus("LES ACCESS_KEYS NE CORRESPONDENT PAS", true);
                shakeNode(mainCard);
                return;
            }

            // Update Database
            User user = crudUser.getUser_withmail(email);
            user.updatepassword(newPwd);
            crudUser.updateEntity(user, user.getId());
            System.out.println("user: "+user.getUsername()+" password updated");
            showStatus("ACCÈS RÉINITIALISÉ. REDIRECTION...", false);
            actionBtn.setDisable(true);
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException e) {}
                Platform.runLater(this::handleBackToLogin);
            }).start();
        }
    }

    private void startResendTimer() {
        resendBtn.setDisable(true);
        cooldownSeconds = 60;
        if (cooldownTimeline != null) cooldownTimeline.stop();
        cooldownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            cooldownSeconds--;
            if (cooldownSeconds > 0) {
                resendBtn.setText("RENVOYER DANS " + cooldownSeconds + "S");
            } else {
                resendBtn.setText("RENVOYER LE CODE");
                resendBtn.setDisable(false);
                cooldownTimeline.stop();
            }
        }));
        cooldownTimeline.setCycleCount(Animation.INDEFINITE);
        cooldownTimeline.play();
    }

    @FXML
    private void handleResendCode(ActionEvent event) {
        String email = emailField.getText().trim();
        generatedCode = String.valueOf((int) (Math.random() * 900000) + 100000);
        System.out.println(generatedCode);
        showStatus("NOUVEAU CODE DE DÉCRYPTAGE GÉNÉRÉ", false);
        new Thread(() -> {
            EmailSystem.SendForgotPassword(email,generatedCode);
        }).start();
        startResendTimer();
    }

    private void switchToResetMode() {
        isCodeSent = true;
        emailField.setEditable(false);
        emailField.setOpacity(0.6);

        otpSection.setManaged(true);
        otpSection.setVisible(true);

        actionBtn.setDisable(false);
        actionBtn.setText("RÉINITIALISER L'ACCÈS");

        startResendTimer();

        FadeTransition ft = new FadeTransition(Duration.millis(500), otpSection);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.web("#FF5252") : Color.web("#22c55e"));
        statusLabel.setVisible(true);
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setAutoReverse(true);
        tt.setCycleCount(6);
        tt.play();
    }

    @FXML
    private void handleBackToLogin() {
        try {
            cleanup();
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Login/Login.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cleanup() {
        MediaPlayer player = mediaView.getMediaPlayer();
        if (player != null) {
            player.stop();
            player.dispose();
            mediaView.setMediaPlayer(null);
            System.out.println("[Cleanup Mediaview] Mediaview stopped.");
        }
        if (cooldownTimeline != null) {
            cooldownTimeline.stop();
            System.out.println("[Cleanup Timer] Timer Stopped.");
        }
    }

}
