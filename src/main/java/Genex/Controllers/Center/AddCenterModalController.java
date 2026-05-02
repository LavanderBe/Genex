package Genex.Controllers.Center;

import Genex.entities.Center;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class AddCenterModalController {

    @FXML
    private Label modalTitle;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtAddress;

    @FXML
    private TextField txtCity;

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtMapUrl;



    // Error labels
    @FXML
    private Label errorName;

    @FXML
    private Label errorAddress;

    @FXML
    private Label errorCity;

    @FXML
    private Label errorEmail;

    private Consumer<Center> onSaveCallback;
    private Runnable onCloseCallback;
    private Center centerToEdit;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @FXML
    public void initialize() {
        System.out.println("AddCenterModalController initialized");
        
        // Set default title for new center
        if (modalTitle != null) {
            modalTitle.setText("NOUVEAU CENTRE");
        }
        
        // Setup validation listeners
        setupValidation();
    }

    private void setupValidation() {
        // Clear errors on input
        txtName.textProperty().addListener((obs, old, val) -> hideError(errorName));
        txtAddress.textProperty().addListener((obs, old, val) -> hideError(errorAddress));
        txtCity.textProperty().addListener((obs, old, val) -> hideError(errorCity));
        txtEmail.textProperty().addListener((obs, old, val) -> hideError(errorEmail));
    }

    public void setCenter(Center center) {
        this.centerToEdit = center;
        
        // Change title to "Modifier"
        if (modalTitle != null) {
            modalTitle.setText("MODIFIER CENTRE");
        }
        
        // Fill form with center data
        txtName.setText(center.getName());
        txtAddress.setText(center.getAddress());
        txtCity.setText(center.getCity());
        txtEmail.setText(center.getContactEmail());
        txtMapUrl.setText(center.getMapUrl());
    }

    public void setOnSaveCallback(Consumer<Center> callback) {
        this.onSaveCallback = callback;
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    private void closeModal() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    @FXML
    private void saveCenter() {
        if (!validateForm()) {
            return;
        }

        try {
            // Create or update center
            Center center = centerToEdit != null ? centerToEdit : new Center();
            
            center.setName(txtName.getText().trim());
            center.setAddress(txtAddress.getText().trim());
            center.setCity(txtCity.getText().trim());
            center.setContactEmail(txtEmail.getText().trim());
            center.setMapUrl(txtMapUrl.getText().trim());
            
            System.out.println("Center saved: " + center.getName());
            
            // Call callback
            if (onSaveCallback != null) {
                onSaveCallback.accept(center);
            }

        } catch (Exception e) {
            System.err.println("Error saving center");
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        // Validate name
        if (txtName.getText().trim().isEmpty()) {
            showError(errorName, "Le nom est requis");
            valid = false;
        }

        // Validate address
        if (txtAddress.getText().trim().isEmpty()) {
            showError(errorAddress, "L'adresse est requise");
            valid = false;
        }

        // Validate city
        if (txtCity.getText().trim().isEmpty()) {
            showError(errorCity, "La ville est requise");
            valid = false;
        }

        // Validate email
        if (txtEmail.getText().trim().isEmpty()) {
            showError(errorEmail, "L'email est requis");
            valid = false;
        } else if (!EMAIL_PATTERN.matcher(txtEmail.getText().trim()).matches()) {
            showError(errorEmail, "L'email n'est pas valide");
            valid = false;
        }

        return valid;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
