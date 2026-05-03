package Genex.Controllers.Forum;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class PostsController {

    @FXML
    private AnchorPane rootPane;

    @FXML
    private void openForum(ActionEvent event) {
        switchView("/Fxml/Forum/Forum.fxml");
    }

    @FXML
    private void openPosts(ActionEvent event) {
        switchView("/Fxml/Forum/Posts.fxml");
    }

    @FXML
    private void handleSelectVideo(ActionEvent event) {
    }

    @FXML
    private void handleClearVideo(ActionEvent event) {
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
    }

    @FXML
    private void handleAddPost(ActionEvent event) {
    }

    @FXML
    private void handleUpdatePost(ActionEvent event) {
    }

    @FXML
    private void handleDeletePost(ActionEvent event) {
    }

    @FXML
    private void handleReportPost(ActionEvent event) {
    }

    @FXML
    private void handleHidePost(ActionEvent event) {
    }

    @FXML
    private void handleRestorePost(ActionEvent event) {
    }

    private void switchView(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            rootPane.getChildren().setAll(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load view: " + fxmlPath, e);
        }
    }
}
