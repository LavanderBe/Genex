package Genex.Controllers.Tutorial;

import Genex.entities.Tutorial;
import Genex.services.TutorialService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.geometry.Pos;

import java.net.URL;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TutorialController implements Initializable {

    @FXML private TextField searchField;
    @FXML private FlowPane tutorialContainer;

    private final TutorialService tutorialService = new TutorialService();
    private List<Tutorial> allTutorials;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        refreshTutorialList();
        searchField.textProperty().addListener((obs, old, nv) -> filterTutorials(nv));
    }

    public void refreshTutorialList() {
        allTutorials = tutorialService.getAll();
        displayTutorials(allTutorials);
    }

    private void displayTutorials(List<Tutorial> tutorials) {
        tutorialContainer.getChildren().clear();
        for (Tutorial t : tutorials) {
            tutorialContainer.getChildren().add(createTutorialCard(t));
        }
    }

    private void filterTutorials(String query) {
        List<Tutorial> filtered = allTutorials.stream()
                .filter(t -> t.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        t.getCategory().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        displayTutorials(filtered);
    }

    private VBox createTutorialCard(Tutorial t) {
        VBox card = new VBox();
        card.getStyleClass().add("tutorial-card");
        card.setPrefWidth(320);

        // Top Image Layer
        StackPane topLayer = new StackPane();
        topLayer.getStyleClass().add("card-image-box");
        topLayer.setPrefHeight(180);

        try {
            // Using placeholder logic or the user-added cardimage
            Image img = new Image(getClass().getResourceAsStream("/Images/cardimage.jpg"));
            ImageView iv = new ImageView(img);
            iv.setFitWidth(320);
            iv.setFitHeight(180);
            iv.setPreserveRatio(false);

            // Clip image to match card's top rounded corners
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(320, 180);
            clip.setArcWidth(50);
            clip.setArcHeight(50);
            iv.setClip(clip);

            topLayer.getChildren().add(iv);
        } catch (Exception e) {
            topLayer.setStyle("-fx-background-color: #1a1e2e; -fx-background-radius: 25 25 0 0;");
        }

        Label catTag = new Label(t.getCategory().toUpperCase());
        catTag.getStyleClass().add("card-category-tag");
        StackPane.setAlignment(catTag, Pos.TOP_LEFT);
        StackPane.setMargin(catTag, new javafx.geometry.Insets(15));

        // Actions Overlay (Top Right)
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.TOP_RIGHT);
        actions.setPadding(new javafx.geometry.Insets(15));

        javafx.scene.control.Button editBtn = new javafx.scene.control.Button();
        editBtn.getStyleClass().add("card-action-icon");
        javafx.scene.shape.SVGPath editIcon = new javafx.scene.shape.SVGPath();
        editIcon.setContent("M12 20h9M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4 12.5-12.5z");
        editIcon.setScaleX(0.85);
        editIcon.setScaleY(0.85);
        editIcon.getStyleClass().add("svg-icon");
        editBtn.setGraphic(editIcon);
        editBtn.setOnAction(e -> openModal(t));

        javafx.scene.control.Button deleteBtn = new javafx.scene.control.Button();
        deleteBtn.getStyleClass().add("card-action-icon");
        javafx.scene.shape.SVGPath deleteIcon = new javafx.scene.shape.SVGPath();
        deleteIcon.setContent("M3 6h18m-2 0v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6m3 0V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2m-6 5v6m4-6v6");
        deleteIcon.setScaleX(0.85);
        deleteIcon.setScaleY(0.85);
        deleteIcon.getStyleClass().add("svg-icon");
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.setOnAction(e -> {
            tutorialService.deleteEntity(t);
            refreshTutorialList();
        });

        actions.getChildren().addAll(editBtn, deleteBtn);
        StackPane.setAlignment(actions, Pos.TOP_RIGHT);

        topLayer.getChildren().addAll(catTag, actions);

        // Content
        VBox content = new VBox(10);
        content.getStyleClass().add("card-content");

        Label title = new Label(t.getTitle());
        title.getStyleClass().add("card-title");
        title.setWrapText(true);

        Label desc = new Label(t.getDescription());
        desc.getStyleClass().add("card-description");
        desc.setWrapText(true);
        desc.setMinHeight(40);

        content.getChildren().addAll(title, desc);

        // Footer
        HBox footer = new HBox(10);
        footer.getStyleClass().add("card-footer");
        footer.setAlignment(Pos.CENTER_LEFT);

        String dateStr = "3/20/2024";
        if (t.getCreated_at() != null) {
            java.time.LocalDateTime ldt = t.getCreated_at().toLocalDateTime();
            dateStr = ldt.format(java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy"));
        } else {
            // If null, use today's date formatted similarly
            dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy"));
        }

        Label date = new Label("🕘 " + dateStr);
        date.getStyleClass().add("card-meta-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label diff = new Label("📶 " + t.getDifficulty().toUpperCase());
        diff.getStyleClass().addAll("card-meta-text", "difficulty-" + t.getDifficulty().toLowerCase());

        footer.getChildren().addAll(date, spacer, diff);

        card.getChildren().addAll(topLayer, content, footer);
        return card;
    }

    @FXML private AnchorPane rootPane;

    @FXML
    private void goToQuizzes(javafx.event.ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Quiz/Quiz.fxml"));
            Scene scene = ((javafx.scene.Node)event.getSource()).getScene();
            scene.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddTutorial() {
        openModal(null);
    }

    @FXML
    private void openModal(Tutorial t) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tutorial/AddTutorial.fxml"));
            Parent root = loader.load();

            AddTutorialController controller = loader.getController();
            controller.setParentController(this);
            if (t != null) controller.setEditData(t);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

