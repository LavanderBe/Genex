package Genex.Controllers.Player;

import Genex.entities.Tutorial;
import Genex.entities.TutorialRating;
import Genex.entities.TutorialVideo;
import Genex.services.CrudPlayerVideoProgress;
import Genex.services.CrudTutorialRating;
import Genex.services.CrudTutorialVideo;
import Genex.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Set;

public class TutorialDetailController {

    @FXML private Label titleLabel;
    @FXML private Label categoryLabel;
    @FXML private Label difficultyLabel;
    @FXML private Label dateLabel;
    @FXML private Label avgRatingLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label videosCountLabel;
    @FXML private VBox videosList;
    @FXML private VBox ratingBlock;
    @FXML private Label ratingHint;
    @FXML private HBox starsRow;
    @FXML private TextArea commentField;
    @FXML private Button submitRatingBtn;
    @FXML private Label ratingStatusLabel;

    private final CrudTutorialVideo videoService = new CrudTutorialVideo();
    private final CrudPlayerVideoProgress progressService = new CrudPlayerVideoProgress();
    private final CrudTutorialRating ratingService = new CrudTutorialRating();

    private Tutorial tutorial;
    private Stage parentStage;
    private List<TutorialVideo> videos;
    private Set<Integer> completedIds;
    private int currentStars = 0;

    public void setTutorial(Tutorial tutorial, Stage parentStage) {
        this.tutorial = tutorial;
        this.parentStage = parentStage;
        renderHeader();
        reloadAndRender();
    }

    private void renderHeader() {
        if (tutorial == null) return;
        titleLabel.setText(tutorial.getTitle() != null ? tutorial.getTitle().toUpperCase() : "MODULE");
        categoryLabel.setText("CATÉGORIE : " + (tutorial.getCategory() != null ? tutorial.getCategory() : "N/D"));
        difficultyLabel.setText("INTENSITÉ : " + (tutorial.getDifficulty() != null ? tutorial.getDifficulty() : "N/D"));
        dateLabel.setText("PUBLIÉ : " + (tutorial.getCreatedAt() != null ? tutorial.getCreatedAt().toString() : "N/D"));
        descriptionLabel.setText(tutorial.getDescription() != null ? tutorial.getDescription() : "");

        double avg = ratingService.getAverage(tutorial.getId());
        int count = ratingService.getCount(tutorial.getId());
        if (count > 0) {
            avgRatingLabel.setText(String.format("★ %.1f / 5  (%d AVIS)", avg, count));
        } else {
            avgRatingLabel.setText("AUCUN AVIS POUR LE MOMENT");
        }
    }

    private void reloadAndRender() {
        videos = videoService.getByTutorial(tutorial.getId());
        String playerId = SessionManager.getInstance().getCurrentUserId();
        completedIds = playerId != null ? progressService.getCompletedVideoIds(playerId, tutorial.getId()) : Set.of();

        renderVideosList();
        renderProgress();
        renderRatingBlock();
    }

    private void renderVideosList() {
        videosList.getChildren().clear();
        if (videos == null || videos.isEmpty()) {
            Label empty = new Label("Aucune vidéo dans ce module pour le moment.");
            empty.setStyle("-fx-text-fill: #8e94af; -fx-font-style: italic; -fx-padding: 14;");
            videosList.getChildren().add(empty);
            return;
        }
        int idx = 1;
        for (TutorialVideo v : videos) {
            videosList.getChildren().add(buildVideoRow(idx++, v));
        }
    }

    private HBox buildVideoRow(int displayIndex, TutorialVideo v) {
        boolean done = completedIds != null && completedIds.contains(v.getId());

        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        String borderColor = done ? "#00C851" : "#1f2937";
        row.setStyle("-fx-background-color: #0b0f14; -fx-border-color: " + borderColor + "; -fx-border-width: 1;");

        Label num = new Label("#" + displayIndex);
        num.setStyle("-fx-text-fill: #5c7cfa; -fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 32;");

        VBox titleBox = new VBox(2);
        Label name = new Label(v.getTitle() != null ? v.getTitle() : ("Vidéo " + displayIndex));
        name.setStyle("-fx-text-fill: white; -fx-font-family: 'Arial Black'; -fx-font-style: italic; -fx-font-size: 14px;");
        name.setWrapText(true);
        Label urlPreview = new Label(v.getVideoUrl());
        urlPreview.setStyle("-fx-text-fill: #5e6480; -fx-font-family: 'Courier New'; -fx-font-size: 10px;");
        urlPreview.setWrapText(true);
        titleBox.getChildren().addAll(name, urlPreview);

        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label status = new Label(done ? "TERMINÉ" : "EN ATTENTE");
        status.setStyle("-fx-text-fill: " + (done ? "#00C851" : "#8e94af") + "; -fx-font-family: 'Courier New'; -fx-font-size: 10px; -fx-letter-spacing: 0.25em; -fx-font-weight: bold;");

        Button watchBtn = new Button("▶ REGARDER");
        watchBtn.setStyle("-fx-background-color: #1a56f5; -fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-letter-spacing: 0.2em; -fx-padding: 8 14 8 14;");
        watchBtn.setOnAction(e -> launchVideoPlayer(v));

        Button toggleBtn = new Button(done ? "ANNULER" : "MARQUER TERMINÉ");
        String toggleColor = done ? "#5f0b0b" : "#8B0D0D";
        toggleBtn.setStyle("-fx-background-color: " + toggleColor + "; -fx-text-fill: white; -fx-font-family: 'Courier New'; -fx-font-weight: bold; -fx-letter-spacing: 0.2em; -fx-padding: 8 14 8 14;");
        toggleBtn.setOnAction(e -> toggleCompletion(v, !done));

        row.getChildren().addAll(num, titleBox, status, watchBtn, toggleBtn);
        return row;
    }

    private void toggleCompletion(TutorialVideo v, boolean completed) {
        String playerId = SessionManager.getInstance().getCurrentUserId();
        if (playerId == null) {
            new Alert(Alert.AlertType.WARNING, "Vous devez être connecté pour marquer la progression.").showAndWait();
            return;
        }
        progressService.setCompleted(playerId, v.getId(), completed);
        reloadAndRender();
    }

    private void renderProgress() {
        int total = videos == null ? 0 : videos.size();
        int done = completedIds == null ? 0 : completedIds.size();
        double pct = total <= 0 ? 0 : (double) done / total;
        progressBar.setProgress(pct);
        progressLabel.setText(((int) Math.round(pct * 100)) + "%");
        videosCountLabel.setText(done + " / " + total + " VIDÉOS COMPLÉTÉES");
    }

    private void renderRatingBlock() {
        starsRow.getChildren().clear();
        int total = videos == null ? 0 : videos.size();
        int doneCount = completedIds == null ? 0 : completedIds.size();
        boolean unlocked = total > 0 && doneCount == total;

        String playerId = SessionManager.getInstance().getCurrentUserId();
        TutorialRating existing = playerId != null ? ratingService.getByPlayer(playerId, tutorial.getId()) : null;
        currentStars = existing != null ? existing.getStars() : 0;
        if (existing != null && existing.getComment() != null) {
            commentField.setText(existing.getComment());
        }

        // Construit 5 etoiles cliquables.
        for (int i = 1; i <= 5; i++) {
            final int starValue = i;
            Button star = new Button("★");
            star.setStyle(buildStarStyle(i <= currentStars, unlocked));
            star.setDisable(!unlocked);
            star.setOnAction(e -> {
                currentStars = starValue;
                refreshStars(unlocked);
            });
            starsRow.getChildren().add(star);
        }

        if (unlocked) {
            ratingHint.setText(existing != null
                    ? "Vous avez déjà noté ce module. Vous pouvez ajuster votre évaluation."
                    : "Toutes les vidéos sont terminées. Donnez votre verdict !");
            ratingHint.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 12px;");
            submitRatingBtn.setDisable(false);
            commentField.setDisable(false);
        } else {
            ratingHint.setText("Terminez toutes les vidéos pour débloquer la notation. (" + doneCount + "/" + total + ")");
            ratingHint.setStyle("-fx-text-fill: #8e94af; -fx-font-size: 12px; -fx-font-style: italic;");
            submitRatingBtn.setDisable(true);
            commentField.setDisable(true);
        }
        ratingStatusLabel.setText("");
    }

    private void refreshStars(boolean unlocked) {
        for (int i = 0; i < starsRow.getChildren().size(); i++) {
            if (starsRow.getChildren().get(i) instanceof Button b) {
                b.setStyle(buildStarStyle(i < currentStars, unlocked));
            }
        }
    }

    private String buildStarStyle(boolean filled, boolean unlocked) {
        String color = !unlocked ? "#3a3f55" : (filled ? "#ffcc33" : "#4a4f65");
        return "-fx-background-color: transparent; -fx-text-fill: " + color + "; -fx-font-size: 28px; -fx-padding: 2 6 2 6; -fx-cursor: " + (unlocked ? "hand" : "default") + ";";
    }

    @FXML
    private void handleSubmitRating() {
        if (currentStars < 1 || currentStars > 5) {
            ratingStatusLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            ratingStatusLabel.setText("Sélectionnez au moins 1 étoile.");
            return;
        }
        String playerId = SessionManager.getInstance().getCurrentUserId();
        if (playerId == null) {
            ratingStatusLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-family: 'Courier New'; -fx-font-size: 11px;");
            ratingStatusLabel.setText("Session expirée.");
            return;
        }
        TutorialRating r = new TutorialRating(playerId, tutorial.getId(), currentStars,
                commentField.getText() == null ? null : commentField.getText().trim());
        ratingService.upsert(r);
        ratingStatusLabel.setStyle("-fx-text-fill: #00C851; -fx-font-family: 'Courier New'; -fx-font-size: 11px; -fx-letter-spacing: 0.2em;");
        ratingStatusLabel.setText("ÉVALUATION ENREGISTRÉE");
        renderHeader(); // met a jour la moyenne affichee
    }

    private void launchVideoPlayer(TutorialVideo v) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Player/VideoPlayer.fxml"));
            Parent root = loader.load();
            VideoPlayerController controller = loader.getController();

            // Construit un Tutorial "synthetique" pour reutiliser le lecteur existant
            // sans avoir a le refondre : seuls le titre et l'URL changent par video.
            Tutorial proxy = new Tutorial();
            proxy.setId(tutorial.getId());
            proxy.setTitle(v.getTitle());
            proxy.setDescription(tutorial.getDescription());
            proxy.setVideoUrl(v.getVideoUrl());
            proxy.setCategory(tutorial.getCategory());
            proxy.setDifficulty(tutorial.getDifficulty());
            proxy.setCreatedAt(tutorial.getCreatedAt());

            Stage stage = new Stage();
            stage.setTitle(v.getTitle() != null ? v.getTitle() : "Lecteur vidéo");
            stage.setScene(new Scene(root, 1280, 720));
            stage.setResizable(true);
            stage.setOnShown(event -> controller.setTutorial(proxy, stage));
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERREUR DU LECTEUR VIDÉO");
            alert.setHeaderText("Impossible d'ouvrir la vidéo");
            alert.setContentText("Erreur : " + ex.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleClose() {
        if (parentStage != null) parentStage.close();
    }
}
