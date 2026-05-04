package Genex.Controllers.Quiz;

import Genex.entities.Quiz;
import Genex.services.QuizService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class QuizController implements Initializable {

    @FXML private FlowPane quizCardsPane;
    @FXML private TextField searchQuizField;
    @FXML private VBox emptyBox;
    @FXML private Button tabTutorial;
    @FXML private Button tabQuiz;

    private QuizService quizService = new QuizService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadQuizCards(null);
        setActiveTab(false);
    }

    // ===================== LOAD CARDS =====================

    private void loadQuizCards(String keyword) {
        quizCardsPane.getChildren().clear();

        List<Quiz> quizzes = (keyword == null || keyword.isBlank())
                ? quizService.getAllQuizzes()
                : quizService.searchQuizzes(keyword);

        if (quizzes.isEmpty()) {
            emptyBox.setVisible(true);
            emptyBox.setManaged(true);
        } else {
            emptyBox.setVisible(false);
            emptyBox.setManaged(false);
            for (Quiz q : quizzes) {
                quizCardsPane.getChildren().add(buildQuizCard(q));
            }
        }
    }

    private VBox buildQuizCard(Quiz quiz) {
        VBox card = new VBox(8);
        card.setPrefWidth(500);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("quiz-card");

        Label linkedLabel = new Label("MODULE: " + (quiz.getTutorialTitle() != null
                ? quiz.getTutorialTitle().toUpperCase() : "UNKNOWN"));
        linkedLabel.getStyleClass().add("quiz-linked-label");

        Label questionLabel = new Label(quiz.getQuestion());
        questionLabel.getStyleClass().add("quiz-question");
        questionLabel.setWrapText(true);

        GridPane optGrid = new GridPane();
        optGrid.setHgap(10);
        optGrid.setVgap(8);
        optGrid.setPadding(new Insets(6, 0, 6, 0));

        String[] opts = {quiz.getOptionA(), quiz.getOptionB(), quiz.getOptionC(), quiz.getOptionD()};
        char[] labels = {'A', 'B', 'C', 'D'};
        for (int i = 0; i < 4; i++) {
            Label optLabel = new Label(labels[i] + ". " + (opts[i] != null ? opts[i] : ""));
            optLabel.getStyleClass().add(labels[i] == quiz.getCorrectAnswer() ? "option-correct" : "option-label");
            optLabel.setMaxWidth(200);
            optGrid.add(optLabel, i % 2, i / 2);
        }

        HBox footer = new HBox(8);
        footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("EDIT");
        editBtn.getStyleClass().add("card-edit-btn");
        editBtn.setOnAction(e -> openEditQuiz(quiz));

        Button deleteBtn = new Button("DELETE");
        deleteBtn.getStyleClass().add("card-delete-btn");
        deleteBtn.setOnAction(e -> deleteQuiz(quiz));

        footer.getChildren().addAll(spacer, editBtn, deleteBtn);
        card.getChildren().addAll(linkedLabel, questionLabel, optGrid, footer);
        return card;
    }

    // ===================== TAB SWITCHING =====================

    @FXML
    private void showTutorialTab() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Tutorial/Tutorial.fxml"));
            Scene scene = tabTutorial.getScene();
            if (scene != null) {
                scene.setRoot(root);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showQuizTab() {
        setActiveTab(false);
    }

    private void setActiveTab(boolean tutorialActive) {
        if (tabTutorial == null || tabQuiz == null) {
            return;
        }
        tabTutorial.getStyleClass().removeAll("tab-active", "tab-inactive");
        tabQuiz.getStyleClass().removeAll("tab-active", "tab-inactive");
        if (tutorialActive) {
            tabTutorial.getStyleClass().add("tab-active");
            tabQuiz.getStyleClass().add("tab-inactive");
        } else {
            tabQuiz.getStyleClass().add("tab-active");
            tabTutorial.getStyleClass().add("tab-inactive");
        }
    }

    // ===================== SEARCH =====================

    @FXML
    private void onSearchQuiz() {
        loadQuizCards(searchQuizField.getText());
    }

    // ===================== ADD / EDIT / DELETE =====================

    @FXML
    private void openAddQuiz() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Quiz/AddQuiz.fxml"));
            Parent root = loader.load();
            AddQuizController controller = loader.getController();
            controller.setMode(false, null);
            controller.setOnSaveCallback(() -> loadQuizCards(null));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("NEW PROTOCOL ENTRY");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEditQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Quiz/AddQuiz.fxml"));
            Parent root = loader.load();
            AddQuizController controller = loader.getController();
            controller.setMode(true, quiz);
            controller.setOnSaveCallback(() -> loadQuizCards(null));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("EDIT PROTOCOL ENTRY");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteQuiz(Quiz quiz) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("TERMINATE PROTOCOL");
        alert.setHeaderText("Delete this question?");
        alert.setContentText(quiz.getQuestion());
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                quizService.deleteEntity(quiz);
                loadQuizCards(null);
            }
        });
    }
}