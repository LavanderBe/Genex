package Genex.Controllers.Player;

import Genex.entities.Player;
import Genex.services.CrudPlayer;
import Genex.services.CrudUser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class AddPlayerController {
    @FXML
    private Label birthdayError;

    @FXML
    private DatePicker birthdayPicker;

    @FXML
    private Label cinError;

    @FXML
    private TextField cinField;

    @FXML
    private Label cityError;

    @FXML
    private TextField cityField;

    @FXML
    private Label emailError;

    @FXML
    private TextField emailField;

    @FXML
    private Label nationalityError;

    @FXML
    private TextField nationalityField;

    @FXML
    private Label nicknameError;

    @FXML
    private TextField nicknameField;

    @FXML
    private Label nomError;

    @FXML
    private TextField nomField;

    @FXML
    private Label passwordError;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label prenomError;

    @FXML
    private TextField prenomField;

    @FXML
    private Label usernameError;

    @FXML
    private TextField usernameField;

    private Runnable onCloseCallback;

    final private CrudPlayer cp=new CrudPlayer();
    final private CrudUser cu=new CrudUser();

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    void cancel(ActionEvent event) {

    }

    @FXML
    void savePlayer(ActionEvent event) {

        clearErrors();

        boolean isValid = true;

        // 2. Validate String fields
        if (usernameField.getText().trim().isEmpty()) {
            showError(usernameError, "Le nom d'utilisateur est requis.");
            isValid = false;
        }

        if (emailField.getText().trim().isEmpty()) {
            showError(emailError, "L'email est requis.");
            isValid = false;
        }

        if (passwordField.getText().trim().isEmpty()) {
            showError(passwordError, "Le mot de passe est requis.");
            isValid = false;
        }

        if (prenomField.getText().trim().isEmpty()) {
            showError(prenomError, "Le prénom est requis.");
            isValid = false;
        }

        if (nomField.getText().trim().isEmpty()) {
            showError(nomError, "Le nom est requis.");
            isValid = false;
        }

        if (nicknameField.getText().trim().isEmpty()) {
            showError(nicknameError, "Le pseudo est requis.");
            isValid = false;
        }

        if (cinField.getText().trim().isEmpty()) {
            showError(cinError, "Le CIN est requis.");
            isValid = false;
        }

        if (nationalityField.getText().trim().isEmpty()) {
            showError(nationalityError, "La nationalité est requise.");
            isValid = false;
        }

        if (cityField.getText().trim().isEmpty()) {
            showError(cityError, "La ville est requise.");
            isValid = false;
        }

        // 3. Validate DatePicker (Birthday)
        if (birthdayPicker.getValue() == null) {
            showError(birthdayError, "La date de naissance est requise.");
            isValid = false;
        }
        if (isValid) {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String role = "player";
            String nickname = nicknameField.getText();
            String nom = nomField.getText();
            String prenom = prenomField.getText();
            String cin = cinField.getText();
            String nationalite = nationalityField.getText();
            String ville = cityField.getText();
            LocalDate birthday = birthdayPicker.getValue();
            boolean valid2=true;
            if (cu.check_Username(username)){
                showError(usernameError, "Le nom d'utilisateur existe .");
                valid2=false;
            }
            if (cu.check_email(email)){
                showError(emailError, "L'addresse mail existe .");
                valid2=false;
            }
            if (cp.check_cin_exists(cin))
            {
                valid2=false;
            }
            if (cp.check_nickname_exists(nickname)){
                valid2=false;
            }
            if (valid2){
                Player p = new Player(username, email, password, role, prenom, nom, nickname, cin, birthday, nationalite, ville);
                cp.addPlayer_admin(p);
                System.out.println("KESAAA7");
            }


        }
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void clearErrors()
    {
        Label[] errorLabels = {
                usernameError, emailError, passwordError, prenomError,
                nomError, nicknameError, cinError, birthdayError,
                nationalityError, cityError
        };

        for (Label label : errorLabels) {
            if (label != null) {
                label.setVisible(false);
                label.setManaged(false);
            }
        }
    }
}



