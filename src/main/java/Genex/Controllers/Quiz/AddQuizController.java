package Genex.Controllers.Quiz;

import Genex.entities.Quiz;
import Genex.entities.Tutorial;
import Genex.services.QuizService;
import Genex.services.TutorialService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AddQuizController implements Initializable {

    @FXML private Label dialogTitle;
    @FXML private ComboBox<Tutorial> tutorialCombo;
    @FXML private TextArea questionField;
    @FXML private TextField optionAField;
    @FXML private TextField optionBField;
    @FXML private TextField optionCField;
    @FXML private TextField optionDField;
    @FXML private ToggleButton answerA;
    @FXML private ToggleButton answerB;
    @FXML private ToggleButton answerC;
    @FXML private ToggleButton answerD;

    private ToggleGroup answerGroup = new ToggleGroup();
    private QuizService quizService = new QuizService();
    private TutorialService tutorialService = new TutorialService();
    private boolean isEditMode = false;
    private Quiz currentQuiz;
    private Runnable onSaveCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        answerA.setToggleGroup(answerGroup);
        answerB.setToggleGroup(answerGroup);
        answerC.setToggleGroup(answerGroup);
        answerD.setToggleGroup(answerGroup);
        answerA.setSelected(true);

        // Load tutorials into combo
        List<Tutorial> tutorials = tutorialService.getAllTutorials();
        tutorialCombo.getItems().addAll(tutorials);

        // Display tutorial title in combo
        tutorialCombo.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Tutorial item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle().toUpperCase());
            }
        });
        tutorialCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Tutorial item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getTitle().toUpperCase());
            }
        });

        if (!tutorials.isEmpty()) tutorialCombo.getSelectionModel().selectFirst();
    }

    public void setMode(boolean editMode, Quiz quiz) {
        this.isEditMode = editMode;
        this.currentQuiz = quiz;
        if (editMode && quiz != null) {
            dialogTitle.setText("MODIFIER L'ENTRÉE DE PROTOCOLE");
            questionField.setText(quiz.getQuestion());
            optionAField.setText(quiz.getOptionA());
            optionBField.setText(quiz.getOptionB());
            optionCField.setText(quiz.getOptionC());
            optionDField.setText(quiz.getOptionD());

            // Select correct tutorial
            tutorialCombo.getItems().stream()
                    .filter(t -> t.getId() == quiz.getTutorialId())
                    .findFirst()
                    .ifPresent(tutorialCombo.getSelectionModel()::select);

            // Select correct answer button
            char ca = Character.toUpperCase(quiz.getCorrectAnswer());
            if (ca == 'A') answerA.setSelected(true);
            else if (ca == 'B') answerB.setSelected(true);
            else if (ca == 'C') answerC.setSelected(true);
            else if (ca == 'D') answerD.setSelected(true);
        } else {
            dialogTitle.setText("NOUVELLE ENTRÉE DE PROTOCOLE");
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private char getSelectedAnswer() {
        Toggle selected = answerGroup.getSelectedToggle();
        if (selected == answerA) return 'A';
        if (selected == answerB) return 'B';
        if (selected == answerC) return 'C';
        if (selected == answerD) return 'D';
        return 'A';
    }

    @FXML
    private void handleSave(ActionEvent event) {
        Tutorial selectedTutorial = tutorialCombo.getValue();
        String question = questionField.getText().trim();
        String optA = optionAField.getText().trim();
        String optB = optionBField.getText().trim();
        String optC = optionCField.getText().trim();
        String optD = optionDField.getText().trim();
        char correct = getSelectedAnswer();

        if (selectedTutorial == null) {
            showAlert("ERREUR DE VALIDATION", "Veuillez sélectionner un module stratégique lié.");
            return;
        }
        if (question.isEmpty()) {
            showAlert("ERREUR DE VALIDATION", "La question tactique est requise.");
            return;
        }
        if (optA.isEmpty() || optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
            showAlert("ERREUR DE VALIDATION", "Toutes les réponses (A à D) sont requises.");
            return;
        }

        Quiz quiz = new Quiz();
        quiz.setTutorialId(selectedTutorial.getId());
        quiz.setQuestion(question);
        quiz.setOptionA(optA);
        quiz.setOptionB(optB);
        quiz.setOptionC(optC);
        quiz.setOptionD(optD);
        quiz.setCorrectAnswer(correct);

        if (isEditMode && currentQuiz != null) {
            quiz.setId(currentQuiz.getId());
            quizService.updateEntity(quiz, String.valueOf(currentQuiz.getId()));
        } else {
            quizService.addEntity(quiz);
        }

        if (onSaveCallback != null) onSaveCallback.run();
        closeWindow();
    }

    @FXML
    private void handleAbort(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) questionField.getScene().getWindow();
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
