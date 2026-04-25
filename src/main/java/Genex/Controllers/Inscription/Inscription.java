package Genex.Controllers.Inscription;

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

public class Inscription {

    @FXML
    private ImageView bgImage;

    @FXML
    private Pane bgPane;

    @FXML
    private Button closeBtn;

    @FXML
    private Label confirmPasswordError;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField confirmVisibleField;

    @FXML
    private Label emailError;

    @FXML
    private TextField emailField;

    @FXML
    private Label nomError;

    @FXML
    private TextField nomField;

    @FXML
    private Label passwordError;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ProgressBar passwordStrengthBar;

    @FXML
    private Label passwordStrengthLabel;

    @FXML
    private TextField passwordVisibleField;

    @FXML
    private Label prenomError;

    @FXML
    private TextField prenomField;

    @FXML
    private StackPane rootPane;

    @FXML
    private Button submitBtn;

    @FXML
    private Label telephoneError;

    @FXML
    private TextField telephoneField;

    @FXML
    private Button toggleConfirmBtn;

    @FXML
    private Button togglePasswordBtn;

    @FXML
    private Label usernameError;

    @FXML
    private TextField usernameField;

    private boolean passwordVisible = false;
    private boolean confirmVisible = false;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+33|0)[1-9](\\d{2}){4}$"
    );

    @FXML
    public void initialize() {
        setupFieldListeners();
        loadBackgroundImage();
        updatePasswordStrength();
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
        prenomField.textProperty().addListener((obs, old, val) -> {
            if (!val.isEmpty()) hideError(prenomError);
        });
        nomField.textProperty().addListener((obs, old, val) -> {
            if (!val.isEmpty()) hideError(nomError);
        });
        usernameField.textProperty().addListener((obs, old, val) -> {
            if (!val.isEmpty()) hideError(usernameError);
        });
        emailField.textProperty().addListener((obs, old, val) -> {
            if (!val.isEmpty()) hideError(emailError);
        });
        telephoneField.textProperty().addListener((obs, old, val) -> {
            if (!val.isEmpty()) hideError(telephoneError);
        });
        passwordField.textProperty().addListener((obs, old, val) -> {
            if (!val.isEmpty()) hideError(passwordError);
            updatePasswordStrength();
        });
        passwordVisibleField.textProperty().addListener((obs, old, val) -> {
            passwordField.setText(val);
            updatePasswordStrength();
        });
        confirmPasswordField.textProperty().addListener((obs, old, val) -> {
            if (!val.isEmpty()) hideError(confirmPasswordError);
        });
        confirmVisibleField.textProperty().addListener((obs, old, val) -> {
            confirmPasswordField.setText(val);
        });
    }


    private void updatePasswordStrength() {
        String password = passwordField.getText();
        double strength = calculatePasswordStrength(password);
        passwordStrengthBar.setProgress(strength);

        if (strength < 0.3) {
            passwordStrengthLabel.setText("Faible");
            passwordStrengthBar.setStyle("-fx-accent: #ff4444;");
        } else if (strength < 0.7) {
            passwordStrengthLabel.setText("Moyen");
            passwordStrengthBar.setStyle("-fx-accent: #ffaa00;");
        } else {
            passwordStrengthLabel.setText("Fort");
            passwordStrengthBar.setStyle("-fx-accent: #44ff44;");
        }
    }

    private double calculatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0.0;

        double strength = 0.0;
        if (password.length() >= 8) strength += 0.2;
        if (password.matches(".*[a-z].*")) strength += 0.2;
        if (password.matches(".*[A-Z].*")) strength += 0.2;
        if (password.matches(".*\\d.*")) strength += 0.2;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strength += 0.2;

        return Math.min(strength, 1.0);
    }

    @FXML
    void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Login/Login.fxml"));
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
            showAlert("Erreur", "Impossible d'ouvrir la page de connexion");
        }
    }

    @FXML
    void handleClose() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }

    @FXML
    void handleInscription() {
        boolean valid = true;

        String prenom = prenomField.getText().trim();
        String nom = nomField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (prenom.isEmpty()) {
            showError(prenomError, "Le prénom est requis");
            valid = false;
        }

        if (nom.isEmpty()) {
            showError(nomError, "Le nom est requis");
            valid = false;
        }

        if (username.isEmpty()) {
            showError(usernameError, "Le nom d'utilisateur est requis");
            valid = false;
        } else if (username.length() < 3) {
            showError(usernameError, "Minimum 3 caractères");
            valid = false;
        }

        if (email.isEmpty()) {
            showError(emailError, "L'email est requis");
            valid = false;
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError(emailError, "Format d'email invalide");
            valid = false;
        }

        if (telephone.isEmpty()) {
            showError(telephoneError, "Le téléphone est requis");
            valid = false;
        } else if (!PHONE_PATTERN.matcher(telephone).matches()) {
            showError(telephoneError, "Format de téléphone invalide");
            valid = false;
        }

        if (password.isEmpty()) {
            showError(passwordError, "Le mot de passe est requis");
            valid = false;
        } else if (password.length() < 6) {
            showError(passwordError, "Minimum 6 caractères");
            valid = false;
        }

        if (confirmPassword.isEmpty()) {
            showError(confirmPasswordError, "La confirmation est requise");
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            showError(confirmPasswordError, "Les mots de passe ne correspondent pas");
            valid = false;
        }

        if (!valid) {
            shakeNode(rootPane.lookup(".glass-card"));
            return;
        }

        System.out.println("Inscription attempt: " + email);

        submitBtn.setText("Inscription...");
        submitBtn.setDisable(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            System.out.println("Inscription successful!");
            submitBtn.setText("Créer un compte");
            submitBtn.setDisable(false);
            // Optionally, go to login after successful inscription
            goToLogin();
        });
        pause.play();
    }

    @FXML
    void toggleConfirmVisibility() {
        confirmVisible = !confirmVisible;
        if (confirmVisible) {
            confirmVisibleField.setText(confirmPasswordField.getText());
            confirmVisibleField.setVisible(true);
            confirmVisibleField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            toggleConfirmBtn.setText("🙈");
        } else {
            confirmPasswordField.setText(confirmVisibleField.getText());
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            confirmVisibleField.setVisible(false);
            confirmVisibleField.setManaged(false);
            toggleConfirmBtn.setText("👁");
        }
    }

    @FXML
    void togglePasswordVisibility() {
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
