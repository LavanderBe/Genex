package Genex.Controllers.Login;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.regex.Pattern;

public class Login {

    @FXML private StackPane rootPane;
    @FXML private Pane bgPane;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private Button togglePasswordBtn;
    @FXML private Button loginBtn;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Button toggleSignUp;
    @FXML private Button toggleSignIn;
    @FXML private ImageView bgImage;

    private boolean passwordVisible = false;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,}$"
    );

    @FXML
    public void initialize() {
        setupFieldListeners();
        loadBackgroundImage();
    }

    private void loadBackgroundImage() {
        try {
            Image img = new Image(getClass().getResourceAsStream("/Images/esports-arena.jpg"));
            if (!img.isError()) {
                bgImage.setImage(img);
                // Bind image dimensions to parent container for responsive resizing
                bgImage.fitWidthProperty().bind(bgPane.widthProperty());
                bgImage.fitHeightProperty().bind(bgPane.heightProperty());
            }
        } catch (Exception e) {
            bgImage.setVisible(false);
        }
    }

    private void setupFieldListeners() {
        emailField.textProperty().addListener((obs, old, val) -> {
            if (!val.isEmpty()) hideError(emailError);
        });
        passwordField.textProperty().addListener((obs, old, val) -> {
            if (!val.isEmpty()) hideError(passwordError);
        });
        passwordVisibleField.textProperty().addListener((obs, old, val) -> {
            passwordField.setText(val);
        });
    }

    @FXML
    private void handleLogin() {
        boolean valid = true;
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            showError(emailError, "L'email est requis");
            valid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(emailError, "Format d'email invalide");
            valid = false;
        }

        if (password.isEmpty()) {
            showError(passwordError, "Le mot de passe est requis");
            valid = false;
        } else if (password.length() < 6) {
            showError(passwordError, "Minimum 6 caractères");
            valid = false;
        }

        if (!valid) {
            shakeNode(rootPane.lookup(".glass-card"));
            return;
        }

        System.out.println("Login attempt: " + email);

        loginBtn.setText("Connexion...");
        loginBtn.setDisable(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            System.out.println("Connected successfully!");
            loginBtn.setText("Se connecter");
            loginBtn.setDisable(false);
        });
        pause.play();
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordVisibleField.setText(passwordField.getText());
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("🙈");
        } else {
            passwordField.setText(passwordVisibleField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            togglePasswordBtn.setText("👁");
        }
    }

    @FXML
    private void goToInscription() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Inscription/Inscription.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene currentScene = stage.getScene();
            double width = currentScene.getWidth();
            double height = currentScene.getHeight();
            Scene scene = new Scene(root, width, height);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la page d'inscription");
        }
    }

    @FXML
    private void handleForgotPassword() {
        System.out.println("Forgot password clicked");
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
