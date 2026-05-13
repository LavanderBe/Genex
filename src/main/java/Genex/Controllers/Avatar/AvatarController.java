package Genex.Controllers.Avatar;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;
import javafx.util.Duration;

import java.util.Random;

public class AvatarController {

    @FXML private ImageView avatarImage;
    @FXML private TextField seedField;
    @FXML private ComboBox<String> styleCombo;
    @FXML private ColorPicker glowPicker;
    @FXML private StackPane previewContainer;
    @FXML private VBox loadingOverlay;

    private String currentToken = "";
    private String currentStyle = "bottts";
    private Runnable onSaveCallback;


    public interface AvatarListener {
        void onAvatarGenerated(String url);
    }

    private AvatarListener listener;

    public void setAvatarListener(AvatarListener listener) {
        this.listener = listener;
    }


    @FXML
    public void initialize() {
        styleCombo.getItems().addAll("bottts", "adventurer", "avataaars", "pixel-art", "notionists", "micah","personas","thumbs");
        styleCombo.setValue(currentStyle);
        seedField.setText("GENEX_USER_" + new Random().nextInt(9999));
        updateAvatar();
        glowPicker.setValue(Color.web("#5C7CFA"));
    }


    private void updateAvatar() {
        String seed = seedField.getText().trim();
        String style = styleCombo.getValue();
        if (seed.isEmpty()) return;
        String apiUrl = "https://api.dicebear.com/7.x/" + style + "/png?seed=" + seed;

        loadingOverlay.setVisible(true);
        startGlitchAnimation();
        Image img = new Image(apiUrl, true);

        img.progressProperty().addListener((obs, oldV, newV) -> {
            if (newV.doubleValue() == 1.0) {
                avatarImage.setImage(img);
                loadingOverlay.setVisible(false);
                playSuccessFlash();
            }
        });
    }

    @FXML
    private void handleSeedChange() {
        updateAvatar();
    }

    @FXML
    private void handleStyleChange() {
        updateAvatar();
    }

    @FXML
    private void handleRandomize() {
        seedField.setText("GENEX_USER_" + new Random().nextInt(1000000));
        updateAvatar();
    }

    @FXML
    private void handleGlowChange() {
        Color color = glowPicker.getValue();
        DropShadow ds = new DropShadow(25, color);
        ds.setSpread(0.3);
        avatarImage.setEffect(ds);
    }

    @FXML
    private void handleSave() {
        String style = styleCombo.getValue();
        String seed = seedField.getText();
        String color = glowPicker.getValue().toString().substring(2, 8);
        String finalUrl = "https://api.dicebear.com/7.x/" + style + "/png?seed=" + seed + "&backgroundColor=" + color;
        if (listener != null) {
            listener.onAvatarGenerated(finalUrl);
        }
    }

    @FXML
    private void handleCancel() {
        if (onSaveCallback != null) onSaveCallback.run();
    }

    // --- GEEK ANIMATIONS ---

    private void startGlitchAnimation() {
        FadeTransition ft = new FadeTransition(Duration.millis(100), avatarImage);
        ft.setFromValue(1.0);
        ft.setToValue(0.4);
        ft.setCycleCount(Animation.INDEFINITE);
        ft.setAutoReverse(true);
        ft.play();
        loadingOverlay.visibleProperty().addListener((obs, wasV, isV) -> {
            if (!isV) {
                ft.stop();
                avatarImage.setOpacity(1.0);
            }
        });
    }

    private void playSuccessFlash() {
        // A quick green border flash to show connection is stable
        previewContainer.setStyle("-fx-border-color: #22c55e; -fx-border-width: 2;");
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> previewContainer.setStyle("")); // Returns to CSS default
        pause.play();
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
}