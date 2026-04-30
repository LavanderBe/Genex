package Genex.Controllers.Quiz;

import Genex.entities.Quiz;
import Genex.services.QuizService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class QuizController implements Initializable {
    @FXML private VBox rootBox;
    @FXML private FlowPane quizFlowPane;
    private QuizService service = new QuizService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        refreshQuizList();
    }

    public void refreshQuizList() {
        quizFlowPane.getChildren().clear();
        List<Quiz> quizzes = service.getAllEntities();
        System.out.println("DEBUG: Loaded " + quizzes.size() + " quizzes from database.");
        for (Quiz q : quizzes) {
            quizFlowPane.getChildren().add(createQuizCard(q));
        }
    }

    private VBox createQuizCard(Quiz q) {
        VBox card = new VBox(15);
        card.getStyleClass().add("quiz-card");
        card.setPrefWidth(550);

        // Header with Tutorial Tag and Action buttons
        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Actions Overlay (Top Right like tutorial)
        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.TOP_RIGHT);

        javafx.scene.control.Button editBtn = new javafx.scene.control.Button();
        editBtn.getStyleClass().add("card-action-icon");
        javafx.scene.shape.SVGPath editIcon = new javafx.scene.shape.SVGPath();
        editIcon.setContent("M12 20h9M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z");
        editIcon.setScaleX(0.85);
        editIcon.setScaleY(0.85);
        editIcon.getStyleClass().add("svg-icon");
        editBtn.setGraphic(editIcon);
        editBtn.setOnAction(e -> handleEditQuiz(q));

        javafx.scene.control.Button delBtn = new javafx.scene.control.Button();
        delBtn.getStyleClass().add("card-action-icon");
        javafx.scene.shape.SVGPath deleteIcon = new javafx.scene.shape.SVGPath();
        deleteIcon.setContent("M3 6h18m-2 0v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6m3 0V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2m-6 5v6m4-6v6");
        deleteIcon.setScaleX(0.85);
        deleteIcon.setScaleY(0.85);
        deleteIcon.getStyleClass().add("svg-icon");
        delBtn.setGraphic(deleteIcon);
        delBtn.setOnAction(e -> {
            service.deleteEntity(String.valueOf(q.getId()));
            refreshQuizList();
        });
        actions.getChildren().addAll(editBtn, delBtn);

        Label tutTag = new Label(q.getTutorial_name() != null ? q.getTutorial_name().toUpperCase() : "GENERAL");
        tutTag.getStyleClass().add("card-category-tag");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(actions, spacer, tutTag);

        Label qLabel = new Label(q.getQuestion());
        qLabel.getStyleClass().add("card-title");
        qLabel.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(12);

        grid.add(createOptionBox("A", q.getOption_a(), "A".equals(q.getCorrect_option())), 0, 0);
        grid.add(createOptionBox("B", q.getOption_b(), "B".equals(q.getCorrect_option())), 1, 0);
        grid.add(createOptionBox("C", q.getOption_c(), "C".equals(q.getCorrect_option())), 0, 1);
        grid.add(createOptionBox("D", q.getOption_d(), "D".equals(q.getCorrect_option())), 1, 1);

        card.getChildren().addAll(header, qLabel, grid);
        return card;
    }

    private HBox createOptionBox(String letter, String text, boolean isCorrect) {
        HBox box = new HBox(12);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label l = new Label(letter);
        l.getStyleClass().add("quiz-opt-letter");
        if (isCorrect) l.getStyleClass().add("opt-correct");

        Label t = new Label(text);
        t.getStyleClass().add("quiz-opt-text");
        if (isCorrect) t.setStyle("-fx-text-fill: #00e676;");

        box.getChildren().addAll(l, t);
        return box;
    }

    @FXML
    private void handleAddQuiz() {
        showQuizModal(null);
    }

    private void handleEditQuiz(Quiz q) {
        showQuizModal(q);
    }

    private void showQuizModal(Quiz q) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Quiz/AddQuiz.fxml"));
            Parent root = loader.load();

            // Safer controller retrieval
            Object controllerObj = loader.getController();
            if (controllerObj instanceof AddQuizController) {
                AddQuizController ctrl = (AddQuizController) controllerObj;
                ctrl.setParentController(this);
                if (q != null) ctrl.setQuiz(q);
            } else {
                System.err.println("CRITICAL ERROR: FXML controller is not AddQuizController but " + (controllerObj == null ? "null" : controllerObj.getClass().getName()));
            }

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToTutorials(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Tutorial/Tutorial.fxml"));
            Scene scene = ((javafx.scene.Node)event.getSource()).getScene();
            scene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
