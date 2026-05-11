package Genex.Controllers.Inscription;

import Genex.entities.Player;
import Genex.entities.User;
import Genex.services.CrudPlayer;
import Genex.services.CrudUser;
import Genex.services.UserControl;
import Genex.utils.EmailSystem;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class Inscription {

    // --- FXML UI COMPONENTS ---
    @FXML private StackPane rootPane;
    @FXML private AnchorPane mainCard;
    @FXML private Label errorLabel;
    @FXML private MediaView mediaView;


    // Containers for the 3 Stages
    @FXML private VBox step1Box, step2Box, step3Box;

    // Stage 1 Fields
    @FXML private TextField usernameField, emailField;
    @FXML private PasswordField passwordField, confirmPasswordField;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    private final EmailSystem emailsys=new EmailSystem();

    // Stage 2 Fields
    @FXML private TextField otpField;

    // Stage 3 Fields
    @FXML private TextField nomField, prenomField, pseudoField, cinField, natField, cityField;
    @FXML private DatePicker dobPicker;
    @FXML private Button signUpBtn;

    // --- DATA VARIABLES ---
    private String generatedCode;
    private final CrudUser crudUser = new CrudUser();
    private final CrudPlayer crudPlayer=new CrudPlayer();
    private String  username;
    private String email;
    private String pwd;
    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {updateStrengthIndicator(newValue);});
        cinField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("([0-9]*)?") && newText.length() <= 8)
            {
                return change;
            }
            return null;
        }));
        try {
            var resource = getClass().getResource("/Videos/test.mp4");
            if (resource == null) {
                System.err.println("CRITICAL: Video file not found at /Genex/Videos/test.mp4");
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

            mediaView.fitWidthProperty().bind(rootPane.widthProperty());
            mediaView.fitHeightProperty().bind(rootPane.heightProperty());

        } catch (Exception e) {
            System.err.println("Error initializing media: " + e.getMessage());
        }
    }


    @FXML
    private void handleNextToOTP(ActionEvent event) {
        username=usernameField.getText().trim();
        email = emailField.getText().trim();
        pwd = passwordField.getText();

        // 1. Validation
        if (username.isEmpty()|| !UserControl.isValidUsername(username))
        {
            showError(errorLabel,"NOM D'UTILISATEUR INVALIDE");
            shakeNode(mainCard);
            return;
        }

        if (email.isEmpty() || !UserControl.isValidEmail(email)) {
            showError(errorLabel,"E-MAIL INVALIDE");
            shakeNode(mainCard);
            return;
        }
        if (pwd.isEmpty())
        {
            showError(errorLabel,"CHANMPS VIDES");
            shakeNode(mainCard);
            return;
        }
        if (!pwd.equals(confirmPasswordField.getText())) {
            showError(errorLabel,"ACCESS KEYS NE MATCHENT PAS");
            shakeNode(mainCard);
            return;
        }
        if (crudUser.check_email(email))
        {
            showError(errorLabel,"EMAIL ALREADY EXISTS");
            shakeNode(mainCard);
            return;
        }
        if (crudUser.check_Username(username))
        {
            showError(errorLabel,"USERNAME ALREADY EXISTS");
            shakeNode(mainCard);
            return;
        }

        generatedCode = String.valueOf((int)(Math.random() * 900000) + 100000);
        System.out.println(generatedCode);

        Thread.ofVirtual().start(() -> {
            emailsys.sendVerificationEmail(email,username,generatedCode);
        });
        goToStep(step1Box, step2Box);
    }

    @FXML
    private void handleVerifyCode(ActionEvent event) {
        if (otpField.getText().equals(generatedCode)) {
            goToStep(step2Box, step3Box);
        } else {
            showError(errorLabel,"VERIFICATION CODE MISMATCH");
            shakeNode(mainCard);
        }
    }

    @FXML
    private void handleFinalRegistration(ActionEvent event) {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String pseudo = pseudoField.getText();
        String cin = cinField.getText();
        LocalDate dob = dobPicker.getValue();
        String nat=natField.getText();
        String ville=cityField.getText();


        if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty() || dob == null||nat.isEmpty()||ville.isEmpty()) {
            showError(errorLabel,"ALL IDENTITY FIELDS ARE REQUIRED");
            shakeNode(mainCard);
            return;
        }
        if (cin.length()<8||crudPlayer.check_cin_exists(cin)){
            showError(errorLabel,"CIN INVALIDE");
            shakeNode(mainCard);
            return;
        }
        if (crudPlayer.check_nickname_exists(pseudo)){
            showError(errorLabel,"PSEUDO INVALIDE");
            shakeNode(mainCard);
            return;
        }
        Player p=new Player(username,email,pwd,"player",prenom,nom,pseudo,cin,dob,nat,ville);
        crudPlayer.addPlayer_admin(p);
        if (true) {
            signUpBtn.setText("REDIRECTION......");
            signUpBtn.setDisable(true);

            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> {
                handleBackToLogin(null);
            });
            pause.play();

        } else {
            showError(errorLabel,"SYSTEM ERROR: DATABASE REJECTED ENTITY");
        }
    }

    private void goToStep(VBox out, VBox in) {
        out.setVisible(false);
        out.setManaged(false);
        in.setVisible(true);
        in.setManaged(true);
        errorLabel.setVisible(false);
    }

    private void updateStrengthIndicator(String password) {
        if (password.isEmpty()) {
            strengthBar.setProgress(0);
            strengthBar.setStyle("-fx-accent: #1F1E4E;");
            strengthLabel.setText("VIDE");
            return;
        }

        double score = calculateStrength(password);
        strengthBar.setProgress(score);

        if (score <= 0.33) {
            strengthBar.setStyle("-fx-accent: #ff4444;"); // Red
            strengthLabel.setText("FAIBLE");
            strengthLabel.setTextFill(Color.web("#ff4444"));
        } else if (score <= 0.66) {
            strengthBar.setStyle("-fx-accent: #ffbb33;"); // Orange
            strengthLabel.setText("NORMAL");
            strengthLabel.setTextFill(Color.web("#ffbb33"));
        } else {
            strengthBar.setStyle("-fx-accent: #00C851;"); // Green
            strengthLabel.setText("FORT");
            strengthLabel.setTextFill(Color.web("#00C851"));
        }
    }

    private double calculateStrength(String password) {
        double score = 0;
        if (password.length() >= 8) score += 0.33;
        if (password.matches(".*[0-9].*")) score += 0.16;
        if (password.matches(".*[a-z].*")) score += 0.16;
        if (password.matches(".*[A-Z].*")) score += 0.16;
        if (password.matches(".*[@#$%^&+=!].*")) score += 0.19;
        return Math.min(score, 1.0);
    }

    @FXML void handleBackToLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Login/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene currentScene = stage.getScene();
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page de connexion");
        }
    }

    void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Login/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene currentScene = stage.getScene();
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page de connexion");
        }
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(8);
        shake.setAutoReverse(true);
        shake.setCycleCount(8);
        shake.setInterpolator(Interpolator.LINEAR);
        shake.play();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}