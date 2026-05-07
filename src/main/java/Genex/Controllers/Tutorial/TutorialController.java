package Genex.Controllers.Tutorial;

import Genex.Controllers.Quiz.AddQuizController;
import Genex.entities.Quiz;
import Genex.entities.Tutorial;
import Genex.services.QuizService;
import Genex.services.TutorialService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TutorialController implements Initializable {

    @FXML private FlowPane tutorialCardsPane;
    @FXML private FlowPane quizCardsPane;
    @FXML private TextField searchTutorialField;
    @FXML private TextField searchQuizField;
    @FXML private Button tabTutorial;
    @FXML private Button tabQuiz;
    @FXML private VBox tutorialSection;
    @FXML private VBox quizSection;
    @FXML private VBox emptyQuizLabel;

    private TutorialService tutorialService = new TutorialService();
    private QuizService quizService = new QuizService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadTutorialCards(null);
        loadQuizCards(null);
        showTutorialTab();
    }

    // ===================== TAB SWITCHING =====================

    @FXML
    private void showTutorialTab() {
        tutorialSection.setVisible(true);
        tutorialSection.setManaged(true);
        quizSection.setVisible(false);
        quizSection.setManaged(false);
        tabTutorial.getStyleClass().removeAll("tab-active", "tab-inactive");
        tabTutorial.getStyleClass().add("tab-active");
        tabQuiz.getStyleClass().removeAll("tab-active", "tab-inactive");
        tabQuiz.getStyleClass().add("tab-inactive");
    }

    @FXML
    private void showQuizTab() {
        tutorialSection.setVisible(false);
        tutorialSection.setManaged(false);
        quizSection.setVisible(true);
        quizSection.setManaged(true);
        tabQuiz.getStyleClass().removeAll("tab-active", "tab-inactive");
        tabQuiz.getStyleClass().add("tab-active");
        tabTutorial.getStyleClass().removeAll("tab-active", "tab-inactive");
        tabTutorial.getStyleClass().add("tab-inactive");
    }

    // ===================== TUTORIAL CARDS =====================

    private void loadTutorialCards(String keyword) {
        tutorialCardsPane.getChildren().clear();
        List<Tutorial> tutorials = (keyword == null || keyword.isBlank())
                ? tutorialService.getAllTutorials()
                : tutorialService.searchTutorials(keyword);

        for (Tutorial t : tutorials) {
            tutorialCardsPane.getChildren().add(buildTutorialCard(t));
        }
    }

    private VBox buildTutorialCard(Tutorial tutorial) {
        VBox card = new VBox(0);
        card.setPrefWidth(320);
        card.setPrefHeight(280);
        card.getStyleClass().add("tutorial-card");

        // Image section
        StackPane imagePane = new StackPane();
        imagePane.setPrefHeight(160);
        imagePane.getStyleClass().add("card-image-pane");

        ImageView imgView = new ImageView();
        imgView.setFitWidth(320);
        imgView.setFitHeight(160);
        imgView.setPreserveRatio(false);
        try {
            imgView.setImage(new Image(getClass().getResourceAsStream("/Images/tutorial.jpg")));
        } catch (Exception ignored) {}

        // Category badge
        Label categoryBadge = new Label(localizeCategory(tutorial.getCategory()));
        categoryBadge.getStyleClass().add("category-badge");

        imagePane.getChildren().addAll(imgView, categoryBadge);
        StackPane.setAlignment(categoryBadge, javafx.geometry.Pos.TOP_LEFT);
        StackPane.setMargin(categoryBadge, new Insets(10, 0, 0, 10));

        // Bottom section
        VBox infoBox = new VBox(6);
        infoBox.setPadding(new Insets(14, 14, 10, 14));
        infoBox.getStyleClass().add("card-info-box");

        Label titleLabel = new Label(tutorial.getTitle());
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);

        String descText = tutorial.getDescription() != null ? tutorial.getDescription() : "";
        if (descText.length() > 70) descText = descText.substring(0, 70) + "...";
        Label descLabel = new Label(descText.toUpperCase());
        descLabel.getStyleClass().add("card-desc");
        descLabel.setWrapText(true);

        HBox footer = new HBox(10);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label dateLabel = new Label(tutorial.getCreatedAt() != null ? tutorial.getCreatedAt().toString() : "");
        dateLabel.getStyleClass().add("card-date");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Difficulty color
        Label diffLabel = new Label(localizeDifficulty(tutorial.getDifficulty()));
        String diff = tutorial.getDifficulty() != null ? tutorial.getDifficulty().toLowerCase() : "";
        if (diff.contains("expert") || diff.contains("hard")) {
            diffLabel.getStyleClass().add("diff-expert");
        } else if (diff.contains("intermediate") || diff.contains("medium")) {
            diffLabel.getStyleClass().add("diff-intermediate");
        } else {
            diffLabel.getStyleClass().add("diff-beginner");
        }

        footer.getChildren().addAll(dateLabel, spacer, diffLabel);
        infoBox.getChildren().addAll(titleLabel, descLabel, footer);

        // Action buttons overlay
        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(0, 14, 10, 14));

        Button editBtn = new Button("MODIFIER");
        editBtn.getStyleClass().add("card-edit-btn");
        editBtn.setOnAction(e -> openEditTutorial(tutorial));

        Button deleteBtn = new Button("SUPPRIMER");
        deleteBtn.getStyleClass().add("card-delete-btn");
        deleteBtn.setOnAction(e -> deleteTutorial(tutorial));

        // ADDED: Tooltip for info
        Tooltip tooltip = new Tooltip("Double-cliquez pour afficher tous les détails");
        Tooltip.install(card, tooltip);

        actions.getChildren().addAll(editBtn, deleteBtn);

        card.getChildren().addAll(imagePane, infoBox, actions);
        return card;
    }

    // ===================== QUIZ CARDS =====================

    private void loadQuizCards(String keyword) {
        quizCardsPane.getChildren().clear();
        List<Quiz> quizzes = (keyword == null || keyword.isBlank())
                ? quizService.getAllQuizzes()
                : quizService.searchQuizzes(keyword);

        if (quizzes.isEmpty()) {
            emptyQuizLabel.setVisible(true);
            emptyQuizLabel.setManaged(true);
        } else {
            emptyQuizLabel.setVisible(false);
            emptyQuizLabel.setManaged(false);
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

        // Linked tutorial
        Label linkedLabel = new Label("MODULE : " + (quiz.getTutorialTitle() != null ? quiz.getTutorialTitle().toUpperCase() : "INCONNU"));
        linkedLabel.getStyleClass().add("quiz-linked-label");

        Label questionLabel = new Label(quiz.getQuestion());
        questionLabel.getStyleClass().add("quiz-question");
        questionLabel.setWrapText(true);

        // Options grid
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

        Button editBtn = new Button("MODIFIER");
        editBtn.getStyleClass().add("card-edit-btn");
        editBtn.setOnAction(e -> openEditQuiz(quiz));

        Button deleteBtn = new Button("SUPPRIMER");
        deleteBtn.getStyleClass().add("card-delete-btn");
        deleteBtn.setOnAction(e -> deleteQuiz(quiz));

        footer.getChildren().addAll(spacer, editBtn, deleteBtn);
        card.getChildren().addAll(linkedLabel, questionLabel, optGrid, footer);
        return card;
    }

    // ===================== SEARCH =====================

    @FXML
    private void onSearchTutorial() {
        loadTutorialCards(searchTutorialField.getText());
    }

    @FXML
    private void onSearchQuiz() {
        loadQuizCards(searchQuizField.getText());
    }

    // ===================== ADD / EDIT / DELETE TUTORIALS =====================

    @FXML
    private void openAddTutorial() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tutorial/AddTutorial.fxml"));
            Parent root = loader.load();
            AddTutorialController controller = loader.getController();
            controller.setMode(false, null);
            controller.setOnSaveCallback(() -> loadTutorialCards(null));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("NOUVEAU MODULE D'ENTRAÎNEMENT");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openEditTutorial(Tutorial tutorial) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tutorial/AddTutorial.fxml"));
            Parent root = loader.load();
            AddTutorialController controller = loader.getController();
            controller.setMode(true, tutorial);
            controller.setOnSaveCallback(() -> loadTutorialCards(null));

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("MODIFIER LE MODULE D'ENTRAÎNEMENT");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteTutorial(Tutorial tutorial) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("SUPPRIMER LE MODULE");
        alert.setHeaderText("Confirmer la suppression de : " + tutorial.getTitle());
        alert.setContentText("Cela supprimera également toutes les questions de quiz liées.");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                tutorialService.deleteEntity(tutorial);
                loadTutorialCards(null);
            }
        });
    }

    // ===================== ADD / EDIT / DELETE QUIZ =====================

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
            stage.setTitle("NOUVELLE ENTRÉE DE PROTOCOLE");
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
            stage.setTitle("MODIFIER L'ENTRÉE DE PROTOCOLE");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteQuiz(Quiz quiz) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("SUPPRIMER LE PROTOCOLE");
        alert.setHeaderText("Supprimer cette question ?");
        alert.setContentText(quiz.getQuestion());
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                quizService.deleteEntity(quiz);
                loadQuizCards(null);
            }
        });
    }

    private String localizeCategory(String category) {
        if (category == null || category.isBlank()) {
            return "GÉNÉRAL";
        }

        return switch (category.trim().toUpperCase()) {
            case "MACRO" -> "MACRO";
            case "OBJECTIVE" -> "OBJECTIF";
            case "MECHANICS" -> "MÉCANIQUE";
            case "STRATEGY" -> "STRATÉGIE";
            case "TEAMPLAY" -> "JEU D'ÉQUIPE";
            case "VISION" -> "VISION";
            default -> category.toUpperCase();
        };
    }

    private String localizeDifficulty(String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            return "";
        }

        return switch (difficulty.trim().toUpperCase()) {
            case "BEGINNER" -> "DÉBUTANT";
            case "INTERMEDIATE" -> "INTERMÉDIAIRE";
            case "EXPERT", "HARD" -> "EXPERT";
            default -> difficulty.toUpperCase();
        };
    }
}
