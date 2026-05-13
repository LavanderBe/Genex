package Genex.Controllers.ReactionGame;

import javafx.animation.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;

public class RulesController {

    @FXML private StackPane rootPane;
    @FXML private MediaView mediaView;
    @FXML private VBox rulesContaine;
    @FXML private Label rulesLabel;
    @FXML private Label neonTitle;
    @FXML private ProgressBar syncProgress;
    @FXML private Label gameNameLabel;
    @FXML private Button btnStart;

    private MediaPlayer bgMusic;
    private MediaPlayer bgVideo;

    @FXML
    public void initialize() {
        btnStart.setDisable(true);
        setupBackgroundMedia();
        startAmbientMusic();
        startTypewriterEffect();
        startFloatingAnimation();
        simulateNeuralSync();
        startNeonBreathing();

    }

    private void setupBackgroundMedia() {
        try {
            URL resource = getClass().getResource("/Videos/Briefing.mp4");
            if (resource != null) {
                bgVideo = new MediaPlayer(new Media(resource.toExternalForm()));
                mediaView.setMediaPlayer(bgVideo);
                bgVideo.setCycleCount(MediaPlayer.INDEFINITE);
                bgVideo.setMute(true);
                bgVideo.play();
                mediaView.fitWidthProperty().bind(rootPane.widthProperty());
                mediaView.fitHeightProperty().bind(rootPane.heightProperty());
            }
        } catch (Exception e) {
            System.err.println("GENEX // VIDEO_FAILURE: " + e.getMessage());
        }
    }

    private void startAmbientMusic() {
        try {
            URL resource = getClass().getResource("/Music/Rules.mp3");
            if (resource != null) {
                bgMusic = new MediaPlayer(new Media(resource.toExternalForm()));
                bgMusic.setCycleCount(MediaPlayer.INDEFINITE);
                bgMusic.setVolume(0.4); // Ambient level
                bgMusic.play();
                System.out.println("GENEX // AUDIO_LINK_ESTABLISHED");
            }
        } catch (Exception e) {
            System.err.println("GENEX // AUDIO_FAILURE");
        }
    }

    private void startTypewriterEffect() {
        String fullText = "> SCANNING POUR LES CHEATS... [DONE]\n" +
                "> VERIFICATION DE L'IDENTITE DU JOUEUR... [DONE]\n" +
                "> RULE 01: BEHAVIORAL CODE 104 ACTIVE.\n" +
                "> RULE 02: LATENCY SPIKES RESULT IN DISCONNECT.\n" +
                "> RULE 03: USE OF EXPLOITS IS PERMANENT BAN.\n" +
                "> RULE 04: HAVE FUN OR BE TERMINATED.";

        final IntegerProperty i = new SimpleIntegerProperty(0);
        Timeline timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(30), event -> {
            if (i.get() > fullText.length()) {
                timeline.stop();
            } else {
                rulesLabel.setText(fullText.substring(0, i.get()));
                i.set(i.get() + 1);
            }
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(fullText.length() + 1);
        timeline.play();
    }

    private void startFloatingAnimation() {
        // Subtle mechanical bobbing for the rules box
        TranslateTransition floating = new TranslateTransition(Duration.seconds(2.5), rulesContaine);
        floating.setFromY(0);
        floating.setToY(-12);
        floating.setAutoReverse(true);
        floating.setCycleCount(Animation.INDEFINITE);
        floating.setInterpolator(Interpolator.EASE_BOTH);
        floating.play();
    }


    private void startNeonBreathing() {
        Color dimRed = Color.web("#4a0707");
        Color brightRed = Color.web("#ff1a1a");
        Timeline colorTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(neonTitle.textFillProperty(), dimRed)),
                new KeyFrame(Duration.seconds(2.5), new KeyValue(neonTitle.textFillProperty(), brightRed))
        );
        colorTimeline.setAutoReverse(true);
        colorTimeline.setCycleCount(Animation.INDEFINITE);
        colorTimeline.play();
    }

    private void simulateNeuralSync() {
        syncProgress.setProgress(0);
        Timeline task = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(syncProgress.progressProperty(), 0)),
                new KeyFrame(Duration.seconds(8), new KeyValue(syncProgress.progressProperty(), 1.0))
        );
        task.setOnFinished(e -> {
            System.out.println("GENEX // SYSTEM_SYNC_COMPLETE");
            btnStart.setDisable(false);
        });
        task.play();
    }

    @FXML
    private void handleBack() {
        terminateNeuralLink();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Dashboard/Player_dashboard.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            terminateNeuralLink();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleStart() {
        terminateNeuralLink();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/ReactionGame/Game.fxml"));
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void terminateNeuralLink() {
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.dispose();
            System.out.println("[CLEANUP SYSTEM] MUSIC DISPOSED");
        }
        if (bgVideo != null) {
            bgVideo.stop();
            bgVideo.dispose();
            System.out.println("[CLEANUP SYSTEM] IMAGE DISPOSED");
        }
    }

}
