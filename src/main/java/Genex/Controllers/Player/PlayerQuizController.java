package Genex.Controllers.Player;

import Genex.entities.Quiz;
import Genex.services.QuizService;
import Genex.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.SVGPath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerQuizController {

    private static final String SEARCH_HINT = "ANALYSER LES ÉVALUATIONS...";
    private static final int QUIZ_XP_REWARD = 50;

    @FXML private VBox quizContainer;
    @FXML private TextField scanField;
    private QuizService quizService;
    private final Map<Integer, Character> selectedAnswers = new HashMap<>();
    private Map<Integer, QuizService.QuizAttemptResult> savedAttempts = new HashMap<>();
    private List<Quiz> allQuizzes = new ArrayList<>();

    public void initialize() {
        configureSearchField();
        loadQuizzes();
    }

    private void loadQuizzes() {
        if (quizService == null) {
            try {
                quizService = new QuizService();
            } catch (IllegalStateException e) {
                quizContainer.getChildren().clear();
                Label errorLabel = new Label("ÉCHEC DU CHARGEMENT DES QUIZ.");
                errorLabel.getStyleClass().add("result-wrong");
                quizContainer.getChildren().add(errorLabel);
                return;
            }
        }

        String userId = SessionManager.getInstance().getCurrentUserId();
        if (userId != null && !userId.isBlank()) {
            savedAttempts = quizService.getUserQuizAttempts(userId);
        } else {
            savedAttempts = Collections.emptyMap();
        }
        allQuizzes = quizService.getAllQuizzes();
        renderQuizzes(allQuizzes);
    }

    private void renderQuizzes(List<Quiz> quizzes) {
        quizContainer.getChildren().clear();

        if (quizzes.isEmpty()) {
            Label emptyLabel = new Label("AUCUNE ÉVALUATION DE QUIZ DISPONIBLE.");
            emptyLabel.getStyleClass().add("module-subtitle");
            quizContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Quiz quiz : quizzes) {
            VBox quizBox = new VBox(12);
            quizBox.getStyleClass().add("quiz-card");

            Label linkedLabel = new Label("MODULE : " + (quiz.getTutorialTitle() != null
                    ? quiz.getTutorialTitle().toUpperCase() : "PROTOCOLE GÉNÉRAL"));
            linkedLabel.getStyleClass().add("quiz-linked-label");

            HBox topBar = new HBox(8);
            topBar.setAlignment(Pos.CENTER_LEFT);
            SVGPath expBolt = new SVGPath();
            expBolt.setContent("M12 2 L6 13 H11 L9 22 L18 10 H13 L15 2 Z");
            expBolt.getStyleClass().add("exp-bolt");
            Label expText = new Label("+50 EXP");
            expText.getStyleClass().add("exp-text");
            HBox expBadge = new HBox(6, expBolt, expText);
            expBadge.getStyleClass().add("exp-badge");

            Region topSpacer = new Region();
            HBox.setHgrow(topSpacer, Priority.ALWAYS);
            topBar.getChildren().addAll(topSpacer, expBadge);

            Label questionLabel = new Label(quiz.getQuestion() != null ? quiz.getQuestion() : "");
            questionLabel.getStyleClass().add("quiz-title");
            questionLabel.setWrapText(true);

            Button authenticateButton = new Button("VALIDER LA RÉPONSE");
            authenticateButton.getStyleClass().add("auth-button");
            authenticateButton.setDisable(true);

            VBox optionsBox = new VBox(10);

            char[] options = {'A', 'B', 'C', 'D'};
            String[] values = {quiz.getOptionA(), quiz.getOptionB(), quiz.getOptionC(), quiz.getOptionD()};

            for (int i = 0; i < 4; i++) {
                if (values[i] != null && !values[i].isEmpty()) {
                    final char currentLabel = options[i];
                    Button optBtn = new Button(currentLabel + ". " + values[i]);
                    optBtn.setPrefWidth(Double.MAX_VALUE);
                    optBtn.setUserData(currentLabel);
                    optBtn.getStyleClass().add("option-button");
                    optBtn.setOnAction(e -> {
                        selectedAnswers.put(quiz.getId(), currentLabel);
                        styleSelectedOption(optionsBox, currentLabel);
                        authenticateButton.setDisable(false);
                    });
                    optionsBox.getChildren().add(optBtn);
                }
            }

            Label resultLabel = new Label("");
            resultLabel.getStyleClass().add("result-label");

            HBox footer = new HBox(12);
            footer.setAlignment(Pos.CENTER_LEFT);
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            footer.getChildren().addAll(authenticateButton, spacer, resultLabel);

            authenticateButton.setOnAction(e -> authenticateAnswer(quiz, optionsBox, resultLabel, authenticateButton));

            QuizService.QuizAttemptResult previousAttempt = savedAttempts.get(quiz.getId());
            if (previousAttempt != null) {
                revealAnswerState(optionsBox, quiz.getCorrectAnswer(), previousAttempt.getSelectedAnswer());
                authenticateButton.setDisable(true);
                setAuthenticateResultState(authenticateButton, previousAttempt.isCorrect());
                resultLabel.getStyleClass().removeAll("result-correct", "result-wrong");
                resultLabel.getStyleClass().add(previousAttempt.isCorrect() ? "result-correct" : "result-wrong");
                resultLabel.setText(previousAttempt.isCorrect()
                        ? "DÉJÀ RÉSOLU : AUCUN XP SUPPLÉMENTAIRE"
                        : "DÉJÀ SOUMIS : AUCUN XP SUPPLÉMENTAIRE // ROUGE = VOTRE CHOIX, VERT = CORRECT");
            }

            quizBox.getChildren().addAll(linkedLabel, topBar, questionLabel, optionsBox, footer);
            quizContainer.getChildren().add(quizBox);
        }
    }

    private void authenticateAnswer(Quiz quiz, VBox optionsBox, Label resultLabel, Button authenticateButton) {
        String userId = SessionManager.getInstance().getCurrentUserId();
        Character selectedAnswer = selectedAnswers.get(quiz.getId());
        if (selectedAnswer == null) {
            return;
        }
        if (savedAttempts.containsKey(quiz.getId())) {
            return;
        }

        boolean isCorrect = Character.toUpperCase(quiz.getCorrectAnswer()) == Character.toUpperCase(selectedAnswer);
        int earnedXp = isCorrect ? QUIZ_XP_REWARD : 0;
        QuizService.QuizSubmissionStatus status = quizService != null
                ? quizService.submitQuizResultOnce(userId, quiz.getId(), selectedAnswer, quiz.getCorrectAnswer(), QUIZ_XP_REWARD)
                : QuizService.QuizSubmissionStatus.FAILED;

        if (status == QuizService.QuizSubmissionStatus.ALREADY_SUBMITTED && userId != null && !userId.isBlank()) {
            QuizService.QuizAttemptResult existing = quizService.getUserQuizAttempt(userId, quiz.getId());
            if (existing != null) {
                savedAttempts.put(quiz.getId(), existing);
                revealAnswerState(optionsBox, quiz.getCorrectAnswer(), existing.getSelectedAnswer());
                authenticateButton.setDisable(true);
                setAuthenticateResultState(authenticateButton, existing.isCorrect());
                resultLabel.getStyleClass().removeAll("result-correct", "result-wrong");
                resultLabel.getStyleClass().add(existing.isCorrect() ? "result-correct" : "result-wrong");
                resultLabel.setText(existing.isCorrect()
                        ? "DÉJÀ RÉSOLU : AUCUN XP SUPPLÉMENTAIRE"
                        : "DÉJÀ SOUMIS : AUCUN XP SUPPLÉMENTAIRE // ROUGE = VOTRE CHOIX, VERT = CORRECT");
                return;
            }
        }

        if (status == QuizService.QuizSubmissionStatus.SESSION_MISSING) {
            resultLabel.getStyleClass().removeAll("result-correct", "result-wrong");
            resultLabel.getStyleClass().add("result-wrong");
            resultLabel.setText("SESSION MANQUANTE : VEUILLEZ VOUS RECONNECTER");
            return;
        }

        if (status == QuizService.QuizSubmissionStatus.FAILED) {
            resultLabel.getStyleClass().removeAll("result-correct", "result-wrong");
            resultLabel.getStyleClass().add("result-wrong");
            resultLabel.setText("ÉCHEC D'ENREGISTREMENT : STATS NON MISES À JOUR");
            return;
        }

        savedAttempts.put(quiz.getId(), new QuizService.QuizAttemptResult(selectedAnswer, isCorrect));
        revealAnswerState(optionsBox, quiz.getCorrectAnswer(), selectedAnswer);
        authenticateButton.setDisable(true);
        setAuthenticateResultState(authenticateButton, isCorrect);
        resultLabel.getStyleClass().removeAll("result-correct", "result-wrong");
        resultLabel.getStyleClass().add(isCorrect ? "result-correct" : "result-wrong");
        resultLabel.setText(isCorrect
                ? "CORRECT : +" + earnedXp + " XP"
                : "FAUX : +" + earnedXp + " XP // ROUGE = VOTRE CHOIX, VERT = CORRECT");
    }

    private void styleSelectedOption(VBox optionsBox, char selectedAnswer) {
        for (int i = 0; i < optionsBox.getChildren().size(); i++) {
            if (!(optionsBox.getChildren().get(i) instanceof Button optionButton)) {
                continue;
            }
            char optionLetter = resolveOptionLetter(optionButton, i);
            optionButton.getStyleClass().removeAll("option-selected", "option-correct", "option-wrong");
            if (Character.toUpperCase(optionLetter) == Character.toUpperCase(selectedAnswer)) {
                optionButton.getStyleClass().add("option-selected");
            }
        }
    }

    private void revealAnswerState(VBox optionsBox, char correctAnswer, char selectedAnswer) {
        for (int i = 0; i < optionsBox.getChildren().size(); i++) {
            if (!(optionsBox.getChildren().get(i) instanceof Button optionButton)) {
                continue;
            }
            char optionLetter = resolveOptionLetter(optionButton, i);
            optionButton.setDisable(true);
            optionButton.getStyleClass().removeAll("option-selected", "option-correct", "option-wrong");
            if (Character.toUpperCase(optionLetter) == Character.toUpperCase(correctAnswer)) {
                optionButton.getStyleClass().add("option-correct");
            } else if (Character.toUpperCase(optionLetter) == Character.toUpperCase(selectedAnswer)) {
                optionButton.getStyleClass().add("option-wrong");
            }
        }
    }

    private char resolveOptionLetter(Button optionButton, int fallbackIndex) {
        Object userData = optionButton.getUserData();
        if (userData instanceof Character character) {
            return character;
        }
        return (char) ('A' + fallbackIndex);
    }

    private void setAuthenticateResultState(Button authenticateButton, boolean isCorrect) {
        authenticateButton.getStyleClass().removeAll("auth-button-correct", "auth-button-wrong", "auth-button-locked");
        authenticateButton.getStyleClass().add(isCorrect ? "auth-button-correct" : "auth-button-wrong");
        authenticateButton.getStyleClass().add("auth-button-locked");
    }

    private void configureSearchField() {
        if (scanField == null) {
            return;
        }
        scanField.setText(SEARCH_HINT);
        scanField.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (focused && SEARCH_HINT.equals(scanField.getText())) {
                scanField.clear();
            } else if (!focused && scanField.getText().isBlank()) {
                scanField.setText(SEARCH_HINT);
            }
            applySearchFilter();
        });
        scanField.textProperty().addListener((obs, oldVal, newVal) -> applySearchFilter());
    }

    private void applySearchFilter() {
        if (scanField == null) {
            return;
        }
        String raw = scanField.getText();
        if (raw == null || raw.isBlank() || SEARCH_HINT.equals(raw)) {
            renderQuizzes(allQuizzes);
            return;
        }
        String query = raw.toLowerCase();
        List<Quiz> filtered = new ArrayList<>();
        for (Quiz quiz : allQuizzes) {
            String question = quiz.getQuestion() != null ? quiz.getQuestion().toLowerCase() : "";
            String tutorial = quiz.getTutorialTitle() != null ? quiz.getTutorialTitle().toLowerCase() : "";
            if (question.contains(query) || tutorial.contains(query)) {
                filtered.add(quiz);
            }
        }
        renderQuizzes(filtered);
    }

    @FXML
    private void handleBack() {
        // Implementation for back button if needed
    }
}
