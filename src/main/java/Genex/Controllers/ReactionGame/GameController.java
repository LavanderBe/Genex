package Genex.Controllers.ReactionGame;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.*;

public class GameController {

    @FXML private GridPane dataGrid;
    @FXML private MediaView mediaView;
    @FXML private StackPane rootPane;
    @FXML private Label slot0, slot1, slot2, slot3, slot4, slot5, slot6, slot7;
    @FXML private Label timeLabel, wordcountLabel, playerLabel;

    private static final String[] COLORS = {"red", "blue", "yellow", "black", "green"};
    private static final Map<String, Color> COLOR_MAP = new HashMap<>() {{put("red", Color.RED); put("blue", Color.BLUE); put("yellow", Color.YELLOW);put("black", Color.BLACK); put("green", Color.GREEN);}};

    private final String SUCCESS_SOUND = getClass().getResource("/Music/success.wav").toExternalForm();
    private final String ERROR_SOUND = getClass().getResource("/Music/faillure.wav").toExternalForm();

    private String targetColor;
    private int score = 0;
    private final int WINNING_SCORE = 20;
    private volatile boolean running = true;

    private long startTimeMillis;
    private AnimationTimer gameTimer;

    @FXML
    public void initialize() {
        List<Label> allSlots = Arrays.asList(slot0, slot1, slot2, slot3, slot4, slot5, slot6, slot7);
        Thread gameThread = new Thread(() -> {
            runGameLogic(allSlots);
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }

    private void runGameLogic(List<Label> allSlots) {
        String modelPath = "src/main/resources/Speech";
        Random random = new Random();
        try (Model model = new Model(modelPath)) {
            String grammar = "[\"red\", \"blue\", \"yellow\", \"black\",\"green\", \"[unknown]\"]";

            try (Recognizer recognizer = new Recognizer(model, 16000f, grammar)) {
                AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("Microphone not supported");
                    return;
                }

                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();
                Platform.runLater(() -> setupNextTurn(allSlots, random));
                startTimer();

                byte[] buffer = new byte[4096];
                int bytesRead;

                while (score < WINNING_SCORE && running) {
                    bytesRead = line.read(buffer, 0, buffer.length);
                    if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                        String resultJson = recognizer.getResult();
                        String spokenWord = getWordFromJson(resultJson);
                        if (spokenWord != null) {
                            if (spokenWord.equals(targetColor)) {
                                score++;
                                //play success soundeffect
                                Platform.runLater(() -> {
                                    playSound(SUCCESS_SOUND);
                                    wordcountLabel.setText("SCORE: " + score + "/20");
                                    clearSlots(allSlots);
                                    if (score < WINNING_SCORE) {
                                        setupNextTurn(allSlots, random);
                                    }
                                });
                            } else {
                                Platform.runLater(() -> {
                                    playSound(ERROR_SOUND);
                                });
                            }
                        }
                    }
                }

                line.stop();
                line.close();
                System.out.println(gameTimer);
                gameTimer.stop();
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Congratulations!");
                    alert.setHeaderText("You Win! 🎉");
                    alert.setContentText(timeLabel.getText());
                    alert.showAndWait();
                    try {
                        Parent root = FXMLLoader.load(getClass().getResource("/Fxml/ReactionGame/Rules.fxml"));
                        Stage stage = (Stage) rootPane.getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.setFullScreen(true);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupNextTurn(List<Label> allSlots, Random random) {
        targetColor = COLORS[random.nextInt(COLORS.length)];
        int randomIndex = random.nextInt(allSlots.size());
        Label targetLabel = allSlots.get(randomIndex);
        targetLabel.setText(COLORS[random.nextInt(COLORS.length)]);
        targetLabel.setTextFill(COLOR_MAP.get(targetColor));
    }

    private void clearSlots(List<Label> allSlots) {
        for (Label l : allSlots) l.setText("");
    }

    private String getWordFromJson(String json) {
        for (String color : COLORS) {
            if (json.contains("\"" + color + "\"")) {
                return color;
            }
        }
        return null;
    }

    private void playSound(String soundPath) {
        try {
            AudioClip clip = new AudioClip(soundPath);
            clip.play();
        } catch (Exception e) {
            System.err.println("Could not play sound: " + e.getMessage());
        }
    }

    private void startTimer() {
        startTimeMillis = System.currentTimeMillis();
        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsedMillis = System.currentTimeMillis() - startTimeMillis;
                double seconds = elapsedMillis / 1000.0;
                timeLabel.setText(String.format("TIME: %.2fs", seconds));
            }
        };
        gameTimer.start();
    }
}