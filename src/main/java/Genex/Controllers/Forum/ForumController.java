package Genex.Controllers.Forum;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class ForumController {

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
    private void handleClearForm(ActionEvent event) {
    }

    @FXML
    private void handleAddForum(ActionEvent event) {
    }

    @FXML
    private void handleUpdateForum(ActionEvent event) {
    }

    @FXML
    private void handleDeleteForum(ActionEvent event) {
    }

    @FXML
    private void handleTogglePin(ActionEvent event) {
    }

    @FXML
    private void handleMarkResolved(ActionEvent event) {
    }

    @FXML
    private void handleReportForum(ActionEvent event) {
    }

    @FXML
    private void handleHideForum(ActionEvent event) {
    }

    @FXML
    private void handleRestoreForum(ActionEvent event) {
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
