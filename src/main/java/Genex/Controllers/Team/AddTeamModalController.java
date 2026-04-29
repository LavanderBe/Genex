package Genex.Controllers.Team;

import Genex.entities.Team;
import Genex.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class AddTeamModalController {

    @FXML
    private Text modalTitle;

    @FXML
    private TextField txtName;

    @FXML
    private TextField txtGameId;

    @FXML
    private TextField txtContact;

    @FXML
    private Button btnUploadLogo;

    @FXML
    private Text txtLogoFileName;

    @FXML
    private ChoiceBox<Team.Status> choiceStatus;

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
    private Text errorGameId;

    @FXML
    private Text errorContact;

    private Consumer<Team> onSaveCallback;
    private Team teamToEdit;
    private String logoImagePath;

    @FXML
    public void initialize() {
        System.out.println("AddTeamModalController initialized");

        // Setup status choice box
        if (choiceStatus != null) {
            choiceStatus.getItems().addAll(Team.Status.values());
            choiceStatus.setValue(Team.Status.ACTIVE);
        }

        // Setup validation listeners
        setupValidation();
    }

    private void setupValidation() {
        // Clear errors on input
        if (txtName != null) txtName.textProperty().addListener((obs, old, val) -> hideError(errorName));
        if (txtGameId != null) txtGameId.textProperty().addListener((obs, old, val) -> hideError(errorGameId));
        if (txtContact != null) txtContact.textProperty().addListener((obs, old, val) -> hideError(errorContact));
    }

    @FXML
    private void handleUploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un logo");

        // Set extension filters
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        // Show open file dialog
        Stage stage = (Stage) btnUploadLogo.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Create uploads directory if it doesn't exist
                Path uploadsDir = Paths.get("uploads", "team-logos");
                Files.createDirectories(uploadsDir);

                // Generate unique filename
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = uploadsDir.resolve(fileName);

                // Copy file to uploads directory
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Store the relative path
                logoImagePath = "uploads/team-logos/" + fileName;

                // Update UI
                txtLogoFileName.setText(selectedFile.getName());
                txtLogoFileName.setStyle("-fx-fill: #4ade80;"); // Green color for success

                System.out.println("Logo uploaded: " + logoImagePath);

            } catch (IOException e) {
                System.err.println("Error uploading logo: " + e.getMessage());
                txtLogoFileName.setText("Erreur lors du téléchargement");
                txtLogoFileName.setStyle("-fx-fill: #ff6b6b;"); // Red color for error
                e.printStackTrace();
            }
        }
    }

    public void setTeam(Team team) {
        this.teamToEdit = team;
        modalTitle.setText("Modifier l'Équipe");

        // Fill form with team data
        txtName.setText(team.getName());
        txtGameId.setText(team.getGameId());
        txtContact.setText(team.getContact());

        if (team.getLogoImage() != null && !team.getLogoImage().isEmpty()) {
            logoImagePath = team.getLogoImage();
            // Extract filename from path
            String fileName = Paths.get(team.getLogoImage()).getFileName().toString();
            txtLogoFileName.setText(fileName);
            txtLogoFileName.setStyle("-fx-fill: #4ade80;");
        }

        if (team.getStatus() != null) {
            choiceStatus.setValue(team.getStatus());
        }
    }

    public void setOnSaveCallback(Consumer<Team> callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void closeModal() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void saveTeam() {
        if (!validateForm()) {
            return;
        }

        try {
            // Check if user is logged in
            if (!SessionManager.getInstance().isLoggedIn()) {
                System.err.println("Error: No user logged in!");
                showError(errorName, "Session expirée. Veuillez vous reconnecter.");
                return;
            }

            // Create or update team
            Team team = teamToEdit != null ? teamToEdit : new Team();

            team.setName(txtName.getText().trim());
            team.setGameId(txtGameId.getText().trim());
            team.setContact(txtContact.getText().trim());
            team.setLogoImage(logoImagePath); // Use uploaded file path
            team.setStatus(choiceStatus.getValue());

            // Set createdBy from logged-in user
            if (teamToEdit == null) {
                String currentUserId = SessionManager.getInstance().getCurrentUserId();
                team.setCreatedBy(currentUserId);
                System.out.println("Setting createdBy to: " + currentUserId);
            }

            System.out.println("Team saved: " + team.getName());

            // Call callback
            if (onSaveCallback != null) {
                onSaveCallback.accept(team);
            }

            closeModal();

        } catch (Exception e) {
            System.err.println("Error saving team");
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

        // Validate game ID
        if (txtGameId.getText().trim().isEmpty()) {
            showError(errorGameId, "Le jeu est requis");
            valid = false;
        }

        // Validate contact
        if (txtContact.getText().trim().isEmpty()) {
            showError(errorContact, "Le contact est requis");
            valid = false;
        }

        return valid;
    }

    private void showError(Text errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void hideError(Text errorLabel) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }
}