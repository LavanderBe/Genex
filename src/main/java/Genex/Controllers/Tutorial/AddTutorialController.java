package Genex.Controllers.Tutorial;

import Genex.entities.Tutorial;
import Genex.services.TutorialService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class AddTutorialController implements Initializable {

    @FXML private Label dialogTitle;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private TextField videoUrlField;

    private TutorialService tutorialService = new TutorialService();
    private boolean isEditMode = false;
    private Tutorial currentTutorial;
    private Runnable onSaveCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryCombo.getItems().addAll("MACRO", "OBJECTIVE", "MECHANICS", "STRATEGY", "TEAMPLAY", "VISION");
        difficultyCombo.getItems().addAll("BEGINNER", "INTERMEDIATE", "EXPERT");
        categoryCombo.setValue("MACRO");
        difficultyCombo.setValue("INTERMEDIATE");
    }

    public void setMode(boolean editMode, Tutorial tutorial) {
        this.isEditMode = editMode;
        this.currentTutorial = tutorial;
        if (editMode && tutorial != null) {
            dialogTitle.setText("MODIFIER LE MODULE D'ENTRAÎNEMENT");
            titleField.setText(tutorial.getTitle());
            descriptionField.setText(tutorial.getDescription());
            videoUrlField.setText(tutorial.getVideoUrl());
            categoryCombo.setValue(tutorial.getCategory() != null ? tutorial.getCategory() : "MACRO");
            difficultyCombo.setValue(tutorial.getDifficulty() != null ? tutorial.getDifficulty() : "INTERMEDIATE");
        } else {
            dialogTitle.setText("NOUVEAU MODULE D'ENTRAÎNEMENT");
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String title = titleField.getText().trim();
        String description = descriptionField.getText().trim();
        String videoUrl = videoUrlField.getText().trim();
        String category = categoryCombo.getValue();
        String difficulty = difficultyCombo.getValue();

        if (title.isEmpty()) {
            showAlert("ERREUR DE VALIDATION", "Le titre tactique est requis.");
            return;
        }
        if (description.isEmpty()) {
            showAlert("ERREUR DE VALIDATION", "Les données de briefing sont requises.");
            return;
        }
        if (category == null || category.isBlank()) {
            showAlert("ERREUR DE VALIDATION", "Veuillez sélectionner un secteur.");
            return;
        }
        if (difficulty == null || difficulty.isBlank()) {
            showAlert("ERREUR DE VALIDATION", "Veuillez sélectionner une intensité.");
            return;
        }
        if (videoUrl.isEmpty()) {
            showAlert("ERREUR DE VALIDATION", "L'URL de la source de renseignements est requise.");
            return;
        }

        Tutorial tutorial = new Tutorial();
        tutorial.setTitle(title);
        tutorial.setDescription(description);
        tutorial.setVideoUrl(videoUrl);
        tutorial.setCategory(category);
        tutorial.setDifficulty(difficulty);
        tutorial.setCreatedAt(LocalDate.now());

        if (isEditMode && currentTutorial != null) {
            tutorial.setId(currentTutorial.getId());
            tutorialService.updateEntity(tutorial, String.valueOf(currentTutorial.getId()));
        } else {
            tutorialService.addEntity(tutorial);
        }

        if (onSaveCallback != null) onSaveCallback.run();
        closeWindow();
    }

    @FXML
    private void handleAbort(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
