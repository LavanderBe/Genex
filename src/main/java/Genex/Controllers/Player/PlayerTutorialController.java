package Genex.Controllers.Player;

import Genex.entities.Tutorial;
import Genex.services.TutorialService;
import Genex.Controllers.Player.VideoPlayerController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class PlayerTutorialController {

    @FXML private FlowPane tutorialCardsPane;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private TutorialService tutorialService;
    private List<Tutorial> allTutorials = new ArrayList<>();

    public void initialize() {
        configureSearch();
        loadModules();
    }

    private void loadModules() {
        tutorialCardsPane.getChildren().clear();
        setStatus(null);
        try {
            if (tutorialService == null) {
                tutorialService = new TutorialService();
            }
            allTutorials = tutorialService.getAllTutorials();
            if (allTutorials.isEmpty()) {
                setStatus("NO TRAINING MODULES AVAILABLE YET.");
                return;
            }
            displayTutorials(allTutorials);
        } catch (IllegalStateException e) {
            setStatus("FAILED TO LOAD TRAINING MODULES.");
            showLoadError(e);
        }
    }

    private void displayTutorials(List<Tutorial> tutorials) {
        tutorialCardsPane.getChildren().clear();
        for (Tutorial tutorial : tutorials) {
            VBox card = createModuleCard(tutorial);
            tutorialCardsPane.getChildren().add(card);
        }
    }

    private VBox createModuleCard(Tutorial tutorial) {
        VBox card = new VBox(0);
        card.getStyleClass().add("tutorial-card");
        card.setPrefWidth(320);
        card.setMinWidth(320);

        ImageView header = null;
        try {
            header = new ImageView(new Image(getClass().getResourceAsStream("/Images/tutorial.jpg")));
            header.setFitWidth(320);
            header.setFitHeight(180);
            header.setPreserveRatio(false);
        } catch (Exception e) {}

        StackPane imagePane = new StackPane();
        imagePane.getStyleClass().add("card-image-pane");
        imagePane.setPrefHeight(180);
        if (header != null) {
            imagePane.getChildren().add(header);
        }

        Label categoryBadge = new Label(tutorial.getCategory() != null ? tutorial.getCategory().toUpperCase() : "GENERAL");
        categoryBadge.getStyleClass().add("category-badge");

        imagePane.getChildren().add(categoryBadge);
        StackPane.setAlignment(categoryBadge, Pos.TOP_LEFT);
        StackPane.setMargin(categoryBadge, new Insets(10, 0, 0, 10));

        VBox details = new VBox(6);
        details.setPadding(new Insets(14, 14, 10, 14));
        details.getStyleClass().add("card-info-box");

        String title = tutorial.getTitle() != null && !tutorial.getTitle().isBlank()
                ? tutorial.getTitle().toUpperCase()
                : "UNTITLED MODULE";
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        titleLabel.setWrapText(true);
        titleLabel.setPrefHeight(46);

        Label descLabel = new Label(tutorial.getDescription() != null ? tutorial.getDescription() : "");
        descLabel.getStyleClass().add("card-desc");
        descLabel.setWrapText(true);
        descLabel.setMinHeight(52);

        StackPane launchIcon = new StackPane();
        Rectangle launchBox = new Rectangle(18, 14);
        launchBox.getStyleClass().add("launch-icon");
        Polygon playTriangle = new Polygon(4.0, 3.0, 4.0, 11.0, 12.0, 7.0);
        playTriangle.getStyleClass().add("launch-triangle");
        launchIcon.getChildren().addAll(launchBox, playTriangle);

        Label launchLabel = new Label("LAUNCH PROTOCOL");
        launchLabel.getStyleClass().add("launch-text");

        HBox launchRow = new HBox(8, launchIcon, launchLabel);
        launchRow.getStyleClass().add("launch-protocol");
        launchRow.setMaxWidth(Double.MAX_VALUE);
        launchRow.setCursor(javafx.scene.Cursor.HAND);
        launchRow.setOnMouseClicked(event -> launchVideoPlayer(tutorial));

        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label dateLabel = new Label(tutorial.getCreatedAt() != null ? tutorial.getCreatedAt().toString() : "");
        dateLabel.getStyleClass().add("card-date");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label diffLabel = new Label(tutorial.getDifficulty() != null ? tutorial.getDifficulty().toUpperCase() : "UNDEFINED");
        String diff = tutorial.getDifficulty() != null ? tutorial.getDifficulty().toLowerCase() : "";
        if (diff.contains("expert") || diff.contains("hard")) {
            diffLabel.getStyleClass().add("diff-expert");
        } else if (diff.contains("intermediate") || diff.contains("medium")) {
            diffLabel.getStyleClass().add("diff-intermediate");
        } else {
            diffLabel.getStyleClass().add("diff-beginner");
        }
        footer.getChildren().addAll(dateLabel, spacer, diffLabel);

        double progress = computeProgress(tutorial);
        Label progressLabel = new Label("PROGRESS: " + (int) (progress * 100) + "%");
        progressLabel.getStyleClass().add("progress-text");
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.getStyleClass().add("module-progress");
        progressBar.setPrefWidth(Double.MAX_VALUE);

        details.getChildren().addAll(titleLabel, descLabel, progressLabel, progressBar, footer, launchRow);
        card.getChildren().addAll(imagePane, details);

        return card;
    }

    private void setStatus(String message) {
        if (statusLabel == null) {
            return;
        }
        boolean hasMessage = message != null && !message.isBlank();
        statusLabel.setVisible(hasMessage);
        statusLabel.setManaged(hasMessage);
        statusLabel.setText(hasMessage ? message : "");
    }

    private void showLoadError(IllegalStateException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("LOAD FAILURE");
        alert.setHeaderText("Could not fetch tutorials");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    private void configureSearch() {
        if (searchField == null) {
            return;
        }
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applySearch(newVal));
    }

    private void applySearch(String input) {
        if (allTutorials == null || allTutorials.isEmpty()) {
            return;
        }
        String query = input != null ? input.trim().toLowerCase() : "";
        if (query.isBlank()) {
            displayTutorials(allTutorials);
            setStatus(null);
            return;
        }

        List<Tutorial> filtered = new ArrayList<>();
        for (Tutorial tutorial : allTutorials) {
            String title = tutorial.getTitle() != null ? tutorial.getTitle().toLowerCase() : "";
            String category = tutorial.getCategory() != null ? tutorial.getCategory().toLowerCase() : "";
            String difficulty = tutorial.getDifficulty() != null ? tutorial.getDifficulty().toLowerCase() : "";
            if (title.contains(query) || category.contains(query) || difficulty.contains(query)) {
                filtered.add(tutorial);
            }
        }
        displayTutorials(filtered);
        setStatus(filtered.isEmpty() ? "NO MODULE MATCHES YOUR FILTER." : null);
    }

    private double computeProgress(Tutorial tutorial) {
        String difficulty = tutorial.getDifficulty() != null ? tutorial.getDifficulty().toLowerCase() : "";
        if (difficulty.contains("beginner")) {
            return 0.35;
        }
        if (difficulty.contains("intermediate")) {
            return 0.6;
        }
        if (difficulty.contains("expert") || difficulty.contains("hard")) {
            return 0.85;
        }
        return 0.25;
    }

    private void launchVideoPlayer(Tutorial tutorial) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Player/VideoPlayer.fxml"));
            Parent root = loader.load();
            VideoPlayerController controller = loader.getController();

            Stage videoStage = new Stage();
            videoStage.setTitle(tutorial.getTitle() != null ? tutorial.getTitle() : "Video Player");
            videoStage.setScene(new Scene(root, 1280, 720));
            videoStage.setResizable(true);
            videoStage.setOnShown(event -> controller.setTutorial(tutorial, videoStage));
            videoStage.show();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("VIDEO PLAYER ERROR");
            alert.setHeaderText("Could not open video player");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}
