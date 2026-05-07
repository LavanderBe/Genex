package Genex.Controllers.Player;

import Genex.entities.Player;
import Genex.entities.User;
import Genex.services.QuizService;
import Genex.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.net.URL;

public class PlayerHubController {

    @FXML private Label rankLabel;
    @FXML private Label xpLabel;
    @FXML private Label accuracyLabel;
    @FXML private Label operatorIdLabel;
    @FXML private Button tutorialsButton;
    private QuizService quizService;

    public void initialize() {
        loadPlayerData();
    }

    private void loadPlayerData() {
        String userId = SessionManager.getInstance().getCurrentUserId();
        Player player = SessionManager.getInstance().getCurrentPlayer();
        User currentUser = SessionManager.getInstance().getCurrentUser();
        Player latestStats = null;

        if (quizService == null) {
            try {
                quizService = new QuizService();
            } catch (IllegalStateException ignored) {
                quizService = null;
            }
        }

        if (userId != null && !userId.isBlank() && quizService != null) {
            try {
                latestStats = quizService.getPlayerStatsByUserId(userId);
            } catch (IllegalStateException ignored) {}
        }

        if (userId == null || userId.isBlank()) {
            operatorIdLabel.setText("UNKNOWN // OFFLINE");
            xpLabel.setText("0");
            accuracyLabel.setText("0.0%");
            rankLabel.setText("#N/A");
            return;
        }

        String displayName = "OPERATOR";
        if (player != null) {
            if (player.getNickname() != null && !player.getNickname().isEmpty()) {
                displayName = player.getNickname();
            } else if (player.getPrenom() != null && !player.getPrenom().isEmpty()) {
                displayName = player.getPrenom();
            }
        } else if (currentUser != null && currentUser.getUsername() != null && !currentUser.getUsername().isEmpty()) {
            displayName = currentUser.getUsername();
        }

        operatorIdLabel.setText("OPERATOR // TACTICAL DATA");

        int xp = 0;
        int totalAttempts = 0;
        int correctAnswers = 0;
        if (latestStats != null) {
            xp = latestStats.getTacticalXp();
            totalAttempts = latestStats.getTotalAttempts();
            correctAnswers = latestStats.getCorrectAnswers();
        } else if (player != null) {
            xp = player.getTacticalXp();
            totalAttempts = player.getTotalAttempts();
            correctAnswers = player.getCorrectAnswers();
        }

        double accuracy = totalAttempts == 0 ? 0.0 : ((double) correctAnswers / totalAttempts) * 100.0;
        xpLabel.setText(String.format("%,d", xp));
        accuracyLabel.setText(String.format("%.1f%%", accuracy));

        int rank = 0;
        if (quizService != null) {
            try {
                rank = quizService.getGlobalRankByXp(userId);
            } catch (IllegalStateException ignored) {}
        }
        rankLabel.setText(rank > 0 ? "#" + rank : "#N/A");
    }

    @FXML
    private void handleTutorialsClick(ActionEvent event) {
        switchScene("/Fxml/Player/PlayerTutorial.fxml", event);
    }

    @FXML
    private void handleQuizzesClick(ActionEvent event) {
        switchScene("/Fxml/Player/PlayerQuiz.fxml", event);
    }

    private void switchScene(String fxmlPath, ActionEvent event) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                showNavigationError("FXML not found: " + fxmlPath);
                return;
            }

            Parent root = FXMLLoader.load(resource);
            AnchorPane contentArea = resolveContentArea(event);
            if (contentArea != null) {
                contentArea.getChildren().setAll(root);
                if (root instanceof Pane pane) {
                    AnchorPane.setTopAnchor(pane, 0.0);
                    AnchorPane.setBottomAnchor(pane, 0.0);
                    AnchorPane.setLeftAnchor(pane, 0.0);
                    AnchorPane.setRightAnchor(pane, 0.0);
                }
                return;
            }

            Stage stage = resolveCurrentStage(event);
            if (stage == null) {
                showNavigationError("Could not resolve the current window.");
                return;
            }

            if (stage.getScene() != null) {
                stage.getScene().setRoot(root);
            } else {
                stage.setScene(new Scene(root));
                stage.show();
            }
        } catch (Exception e) {
            showNavigationError("Could not open screen: " + fxmlPath + "\n" + e.getMessage());
        }
    }

    private Stage resolveCurrentStage(ActionEvent event) {
        if (event != null && event.getSource() instanceof Node source
                && source.getScene() != null
                && source.getScene().getWindow() instanceof Stage stage) {
            return stage;
        }
        if (rankLabel != null && rankLabel.getScene() != null && rankLabel.getScene().getWindow() instanceof Stage stage) {
            return stage;
        }
        if (operatorIdLabel != null && operatorIdLabel.getScene() != null && operatorIdLabel.getScene().getWindow() instanceof Stage stage) {
            return stage;
        }
        if (tutorialsButton != null && tutorialsButton.getScene() != null && tutorialsButton.getScene().getWindow() instanceof Stage stage) {
            return stage;
        }
        return null;
    }

    private AnchorPane resolveContentArea(ActionEvent event) {
        if (event == null || !(event.getSource() instanceof Node source)) {
            return null;
        }

        Parent current = source.getParent();
        while (current != null) {
            if (current instanceof AnchorPane pane && "contentArea".equals(pane.getId())) {
                return pane;
            }
            current = current.getParent();
        }
        return null;
    }

    private void showNavigationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("NAVIGATION ERROR");
        alert.setHeaderText("Screen could not be loaded");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
