package Genex.Controllers.Tutorial;

import Genex.entities.Tutorial;
import Genex.services.TutorialService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AddTutorialController implements Initializable {

    @FXML private TextField titleField;
    @FXML private TextArea descArea;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private TextField videoUrlField;

    private TutorialService service = new TutorialService();
    private boolean isEditMode = false;
    private Tutorial currentTutorial;
    private TutorialController parentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        categoryCombo.getItems().addAll("Macro", "Objective", "Micro", "Laning", "Jungle");
        categoryCombo.setValue("Macro");

        difficultyCombo.getItems().addAll("Beginner", "Intermediate", "Expert");
        difficultyCombo.setValue("Intermediate");
    }

    public void setParentController(TutorialController controller) {
        this.parentController = controller;
    }

    public void setEditData(Tutorial t) {
        this.isEditMode = true;
        this.currentTutorial = t;
        titleField.setText(t.getTitle());
        descArea.setText(t.getDescription());
        categoryCombo.setValue(t.getCategory());
        difficultyCombo.setValue(t.getDifficulty());
        videoUrlField.setText(t.getVideo_url());
    }

    @FXML
    private void handleSave() {
        if (titleField.getText().isEmpty() || videoUrlField.getText().isEmpty() || descArea.getText().isEmpty()) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Missing Information");
            alert.setContentText("Please provide a Title, Description, and Video URL before creating the tutorial.");
            alert.showAndWait();
            return;
        }

        String category = categoryCombo.getValue();
        if (category == null) category = "Macro";

        if (isEditMode) {
            currentTutorial.setTitle(titleField.getText());
            currentTutorial.setDescription(descArea.getText());
            currentTutorial.setCategory(category);
            currentTutorial.setDifficulty(difficultyCombo.getValue());
            currentTutorial.setVideo_url(videoUrlField.getText());
            service.updateEntity(currentTutorial, String.valueOf(currentTutorial.getId()));
        } else {
            Tutorial t = new Tutorial();
            t.setTitle(titleField.getText());
            t.setDescription(descArea.getText());
            t.setCategory(category);
            t.setDifficulty(difficultyCombo.getValue());
            t.setVideo_url(videoUrlField.getText());
            service.addEntity(t);
        }

        if (parentController != null) {
            parentController.refreshTutorialList();
        }
        handleClose();
    }

    @FXML
    private void handleClose() {
        ((Stage) titleField.getScene().getWindow()).close();
    }
}
