package Genex.Controllers.Player;

import Genex.entities.Tutorial;
import Genex.services.VideoService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class VideoPlayerController {

    @FXML private WebView webView;
    @FXML private StackPane videoPlayerPane;
    @FXML private Button closeButton;
    @FXML private Button playButton;
    @FXML private Button pauseButton;
    @FXML private Label videoTitleLabel;
    @FXML private Label videoDescriptionLabel;
    @FXML private Label videoCategoryLabel;
    @FXML private Label videoDifficultyLabel;
    @FXML private Label videoDateLabel;
    @FXML private Label errorLabel;
    @FXML private Label loadingLabel;

    private VideoService videoService;
    private Tutorial currentTutorial;
    private Stage parentStage;

    public void initialize() {
        videoService = new VideoService();
    }

    /**
     * Sets the tutorial and loads the video.
     */
    public void setTutorial(Tutorial tutorial, Stage parentStage) {
        this.currentTutorial = tutorial;
        this.parentStage = parentStage;
        loadVideo();
    }

    /**
     * Loads the video from the tutorial URL into the WebView.
     */
    private void loadVideo() {
        if (currentTutorial == null) {
            showError("No tutorial data provided.");
            return;
        }

        // Update labels with tutorial info
        videoTitleLabel.setText(currentTutorial.getTitle() != null ? currentTutorial.getTitle().toUpperCase() : "UNTITLED");
        videoDescriptionLabel.setText(currentTutorial.getDescription() != null ? currentTutorial.getDescription() : "");
        videoCategoryLabel.setText("CATEGORY: " + (currentTutorial.getCategory() != null ? currentTutorial.getCategory() : "N/A"));
        videoDifficultyLabel.setText("DIFFICULTY: " + (currentTutorial.getDifficulty() != null ? currentTutorial.getDifficulty() : "N/A"));
        videoDateLabel.setText("PUBLISHED: " + (currentTutorial.getCreatedAt() != null ? currentTutorial.getCreatedAt() : "N/A"));

        String videoUrl = currentTutorial.getVideoUrl();
        if (videoUrl == null || videoUrl.isBlank()) {
            showError("No video URL found for this tutorial.");
            return;
        }

        // Extract video ID and validate
        String videoId = videoService.extractVideoId(videoUrl);
        if (videoId == null || videoId.isBlank()) {
            showError("Invalid video URL format.");
            return;
        }

        // Create embedded player HTML
        String embeddedUrl = videoService.getEmbeddedUrl(videoId);
        String html = createPlayerHtml(embeddedUrl);

        // Load HTML into WebView
        try {
            loadingLabel.setVisible(false);
            webView.getEngine().loadContent(html);
        } catch (Exception e) {
            showError("Error loading video: " + e.getMessage());
        }
    }

    /**
     * Creates HTML content for embedding the YouTube player.
     */
    private String createPlayerHtml(String embeddedUrl) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        * { margin: 0; padding: 0; }\n" +
                "        body { background: #000; height: 100vh; width: 100%; display: flex; align-items: center; justify-content: center; }\n" +
                "        .container { width: 100%; height: 100%; display: flex; }\n" +
                "        iframe { width: 100%; height: 100%; border: none; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <iframe src=\"" + embeddedUrl + "\" allowfullscreen></iframe>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * Displays an error message to the user.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: #ff4444;");
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        loadingLabel.setVisible(false);
        if (webView != null) {
            webView.setVisible(false);
            webView.setManaged(false);
        }
    }

    /**
     * Closes the video player window.
     */
    @FXML
    private void handleClose() {
        if (parentStage != null) {
            parentStage.close();
        }
    }

    @FXML
    private void handlePlay() {
    }

    @FXML
    private void handlePause() {
    }
}

