package Genex.Controllers.Quiz;

import Genex.entities.Quiz;
import Genex.entities.Tutorial;
import Genex.services.QuizService;
import Genex.services.TutorialService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AddQuizController implements Initializable {
    @FXML private ComboBox<String> tutorialCombo;
    @FXML private TextArea questionArea;
    @FXML private TextField optAField, optBField, optCField, optDField;
    @FXML private ToggleButton btnA, btnB, btnC, btnD;

    private QuizService service = new QuizService();
    private TutorialService tutorialService = new TutorialService();
    private QuizController parentController;
    private Quiz currentQuiz;
    private boolean isEditMode = false;
    private ToggleGroup group = new ToggleGroup();
    private List<Tutorial> tutorialList;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            tutorialList = tutorialService.getAll();
            if (tutorialList != null && !tutorialList.isEmpty()) {
                for (Tutorial t : tutorialList) {
                    tutorialCombo.getItems().add(t.getTitle());
                }
            }
        } catch (Exception e) {
            System.err.println("Error initializing AddQuizController: " + e.getMessage());
        }

        btnA.setToggleGroup(group);
        btnB.setToggleGroup(group);
        btnC.setToggleGroup(group);
        btnD.setToggleGroup(group);
        btnA.setSelected(true);

        // Ensure buttons have text corresponding to options
        btnA.setText("A");
        btnB.setText("B");
        btnC.setText("C");
        btnD.setText("D");
    }

    public void setParentController(QuizController pc) {
        this.parentController = pc;
    }

    public void setQuiz(Quiz q) {
        this.currentQuiz = q;
        this.isEditMode = true;
        tutorialCombo.setValue(q.getTutorial_name());
        questionArea.setText(q.getQuestion());
        optAField.setText(q.getOption_a());
        optBField.setText(q.getOption_b());
        optCField.setText(q.getOption_c());
        optDField.setText(q.getOption_d());

        if (q.getCorrect_option() != null) {
            switch (q.getCorrect_option()) {
                case "A": btnA.setSelected(true); break;
                case "B": btnB.setSelected(true); break;
                case "C": btnC.setSelected(true); break;
                case "D": btnD.setSelected(true); break;
            }
        }
    }

    @FXML
    private void handleSave() {
        if (tutorialCombo.getValue() == null || questionArea.getText().isEmpty() ||
                optAField.getText().isEmpty() || optBField.getText().isEmpty() ||
                optCField.getText().isEmpty() || optDField.getText().isEmpty()) {
            showAlert("Error", "Information Missing", "Please select a tutorial and fill in all fields.");
            return;
        }

        ToggleButton selected = (ToggleButton) group.getSelectedToggle();
        String correct = selected != null ? selected.getText() : "A";

        if (isEditMode) {
            updateCurrentQuiz(correct);
            service.updateEntity(currentQuiz, String.valueOf(currentQuiz.getId()));
        } else {
            Quiz q = new Quiz();
            String selectedTitle = tutorialCombo.getValue();
            q.setTutorial_name(selectedTitle);
            q.setTutorial_id(getTutorialIdByTitle(selectedTitle));
            q.setQuestion(questionArea.getText());
            q.setOption_a(optAField.getText());
            q.setOption_b(optBField.getText());
            q.setOption_c(optCField.getText());
            q.setOption_d(optDField.getText());
            q.setCorrect_option(correct);
            service.addEntity(q);
        }

        if (parentController != null) {
            parentController.refreshQuizList();
        }
        handleClose();
    }

    private void updateCurrentQuiz(String correct) {
        String selectedTitle = tutorialCombo.getValue();
        currentQuiz.setTutorial_name(selectedTitle);
        currentQuiz.setTutorial_id(getTutorialIdByTitle(selectedTitle));
        currentQuiz.setQuestion(questionArea.getText());
        currentQuiz.setOption_a(optAField.getText());
        currentQuiz.setOption_b(optBField.getText());
        currentQuiz.setOption_c(optCField.getText());
        currentQuiz.setOption_d(optDField.getText());
        currentQuiz.setCorrect_option(correct);
    }

    private int getTutorialIdByTitle(String title) {
        if (tutorialList != null && title != null) {
            for (Tutorial t : tutorialList) {
                if (t.getTitle().equals(title)) {
                    return t.getId();
                }
            }
        }
        return 0;
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleClose() {
        if (tutorialCombo.getScene() != null && tutorialCombo.getScene().getWindow() != null) {
            ((Stage) tutorialCombo.getScene().getWindow()).close();
        }
    }
}
