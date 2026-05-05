package Genex.Controllers.Main;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.geometry.Pos;

public class NotificationPanelController {

    @FXML
    private VBox notificationList;

    private Runnable onCloseCallback;

    @FXML
    public void initialize() {
        System.out.println("NotificationPanelController initialized");
        // Don't load any mock notifications - panel will be empty
    }

    @FXML
    private void closePanel() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    public int getUnreadCount() {
        return (int) notificationList.getChildren().stream()
                .filter(node -> node.getStyleClass().contains("unread"))
                .count();
    }
}