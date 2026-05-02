package Genex.Controllers.Center;

import Genex.entities.Center;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.function.Consumer;
import java.util.regex.Pattern;

public class AddCenterModalController {

    @FXML
    private Text modalTitle;

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

    @FXML
    private Button btnClose;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    // Error labels
    @FXML
    private Text errorName;

    @FXML
    private Text errorAddress;

    @FXML
    private Text errorCity;

    @FXML
    private Text errorEmail;

    private Consumer<Center> onSaveCallback;
    private Center centerToEdit;

    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @FXML
    public void initialize() {
        System.out.println("AddCenterModalController initialized");
        
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
        modalTitle.setText("Modifier le Centre");
        
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

    @FXML
    private void closeModal() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
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
            
            closeModal();
            
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

    private void showError(Text errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Text errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
