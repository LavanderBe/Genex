package Genex.Controllers.Tutorial;

import Genex.entities.Tutorial;
import Genex.entities.TutorialVideo;
import Genex.entities.User;
import Genex.services.CrudTutorialVideo;
import Genex.services.CrudUser;
import Genex.services.TutorialService;
import Genex.utils.EmailSystem;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AddTutorialController implements Initializable {

    @FXML private Label dialogTitle;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private VBox videosContainer;
    @FXML private Label videosHintLabel;

    private final TutorialService tutorialService = new TutorialService();
    private final CrudTutorialVideo videoService = new CrudTutorialVideo();
    private boolean isEditMode = false;
    private Tutorial currentTutorial;
    private Runnable onSaveCallback;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        categoryCombo.getItems().addAll("MACRO", "OBJECTIVE", "MECHANICS", "STRATEGY", "TEAMPLAY", "VISION");
        difficultyCombo.getItems().addAll("BEGINNER", "INTERMEDIATE", "EXPERT");
        categoryCombo.setValue("MACRO");
        difficultyCombo.setValue("INTERMEDIATE");

        // Mode creation : on commence avec une ligne vide pour guider l'utilisateur.
        addVideoRow("", "");
    }

    public void setMode(boolean editMode, Tutorial tutorial) {
        this.isEditMode = editMode;
        this.currentTutorial = tutorial;
        if (editMode && tutorial != null) {
            dialogTitle.setText("MODIFIER LE MODULE D'ENTRAÎNEMENT");
            titleField.setText(tutorial.getTitle());
            descriptionField.setText(tutorial.getDescription());
            categoryCombo.setValue(tutorial.getCategory() != null ? tutorial.getCategory() : "MACRO");
            difficultyCombo.setValue(tutorial.getDifficulty() != null ? tutorial.getDifficulty() : "INTERMEDIATE");

            // Recharge les videos existantes.
            videosContainer.getChildren().clear();
            List<TutorialVideo> existing = videoService.getByTutorial(tutorial.getId());
            if (existing.isEmpty()) {
                // Fallback : si la migration n'a rien produit, retombe sur le video_url legacy.
                if (tutorial.getVideoUrl() != null && !tutorial.getVideoUrl().isBlank()) {
                    addVideoRow("Vidéo 1", tutorial.getVideoUrl());
                } else {
                    addVideoRow("", "");
                }
            } else {
                for (TutorialVideo v : existing) {
                    addVideoRow(v.getTitle(), v.getVideoUrl());
                }
            }
        } else {
            dialogTitle.setText("NOUVEAU MODULE D'ENTRAÎNEMENT");
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleAddVideo(ActionEvent event) {
        addVideoRow("", "");
    }

    private void addVideoRow(String title, String url) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label pos = new Label();
        pos.getStyleClass().add("field-label");
        pos.setMinWidth(28);

        TextField titleInput = new TextField(title == null ? "" : title);
        titleInput.setPromptText("TITRE DE LA VIDÉO");
        titleInput.getStyleClass().add("dialog-field");
        titleInput.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(titleInput, Priority.SOMETIMES);

        TextField urlInput = new TextField(url == null ? "" : url);
        urlInput.setPromptText("HTTPS://YOUTUBE.COM/...");
        urlInput.getStyleClass().add("dialog-field");
        urlInput.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(urlInput, Priority.ALWAYS);

        Button upBtn = new Button("▲");
        upBtn.getStyleClass().add("abort-btn");
        upBtn.setOnAction(e -> moveRow(row, -1));

        Button downBtn = new Button("▼");
        downBtn.getStyleClass().add("abort-btn");
        downBtn.setOnAction(e -> moveRow(row, +1));

        Button removeBtn = new Button("✕");
        removeBtn.getStyleClass().add("abort-btn");
        removeBtn.setOnAction(e -> {
            videosContainer.getChildren().remove(row);
            refreshPositions();
        });

        row.getChildren().addAll(pos, titleInput, urlInput, upBtn, downBtn, removeBtn);
        row.setPadding(new Insets(2));
        row.setUserData(new String[]{"titleInput", "urlInput"}); // marker
        // On stocke les inputs comme proprietes accessibles via getRowInputs.
        row.getProperties().put("titleInput", titleInput);
        row.getProperties().put("urlInput", urlInput);

        videosContainer.getChildren().add(row);
        refreshPositions();
    }

    private void moveRow(HBox row, int delta) {
        int idx = videosContainer.getChildren().indexOf(row);
        int newIdx = idx + delta;
        if (idx < 0 || newIdx < 0 || newIdx >= videosContainer.getChildren().size()) return;
        videosContainer.getChildren().remove(idx);
        videosContainer.getChildren().add(newIdx, row);
        refreshPositions();
    }

    private void refreshPositions() {
        int i = 1;
        for (javafx.scene.Node node : videosContainer.getChildren()) {
            if (node instanceof HBox row && !row.getChildren().isEmpty() && row.getChildren().get(0) instanceof Label lbl) {
                lbl.setText("#" + i++);
            }
        }
    }

    private List<TutorialVideo> collectVideos() {
        List<TutorialVideo> out = new ArrayList<>();
        int pos = 1;
        for (javafx.scene.Node node : videosContainer.getChildren()) {
            if (!(node instanceof HBox row)) continue;
            TextField titleInput = (TextField) row.getProperties().get("titleInput");
            TextField urlInput = (TextField) row.getProperties().get("urlInput");
            if (titleInput == null || urlInput == null) continue;
            String t = titleInput.getText() == null ? "" : titleInput.getText().trim();
            String u = urlInput.getText() == null ? "" : urlInput.getText().trim();
            if (t.isEmpty() && u.isEmpty()) continue;
            TutorialVideo v = new TutorialVideo();
            v.setTitle(t.isEmpty() ? ("Vidéo " + pos) : t);
            v.setVideoUrl(u);
            v.setPosition(pos++);
            out.add(v);
        }
        return out;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String description = descriptionField.getText() == null ? "" : descriptionField.getText().trim();
        String category = categoryCombo.getValue();
        String difficulty = difficultyCombo.getValue();

        if (title.isEmpty()) { showAlert("ERREUR DE VALIDATION", "Le titre tactique est requis."); return; }
        if (description.isEmpty()) { showAlert("ERREUR DE VALIDATION", "Les données de briefing sont requises."); return; }
        if (category == null || category.isBlank()) { showAlert("ERREUR DE VALIDATION", "Veuillez sélectionner un secteur."); return; }
        if (difficulty == null || difficulty.isBlank()) { showAlert("ERREUR DE VALIDATION", "Veuillez sélectionner une intensité."); return; }

        List<TutorialVideo> videos = collectVideos();
        if (videos.isEmpty()) { showAlert("ERREUR DE VALIDATION", "Au moins une vidéo est requise."); return; }
        for (TutorialVideo v : videos) {
            if (v.getVideoUrl() == null || v.getVideoUrl().isBlank()) {
                showAlert("ERREUR DE VALIDATION", "Toutes les vidéos doivent avoir une URL.");
                return;
            }
        }

        Tutorial tutorial = new Tutorial();
        tutorial.setTitle(title);
        tutorial.setDescription(description);
        // On conserve la colonne legacy video_url avec la premiere video (utile pour les ecrans qui n'ont pas encore migre).
        tutorial.setVideoUrl(videos.get(0).getVideoUrl());
        tutorial.setCategory(category);
        tutorial.setDifficulty(difficulty);
        tutorial.setCreatedAt(LocalDate.now());

        int tutorialId;
        if (isEditMode && currentTutorial != null) {
            tutorial.setId(currentTutorial.getId());
            tutorialService.updateEntity(tutorial, String.valueOf(currentTutorial.getId()));
            tutorialId = currentTutorial.getId();
            videoService.replaceAll(tutorialId, videos);
        } else {
            tutorialService.addEntity(tutorial);
            tutorialId = resolveNewTutorialId(title);
            if (tutorialId > 0) {
                for (TutorialVideo v : videos) {
                    v.setTutorialId(tutorialId);
                    videoService.addEntity(v);
                }
            }
            notifyUsersOfNewTutorial(title);
        }

        if (onSaveCallback != null) onSaveCallback.run();
        closeWindow();
    }

    // TutorialService.addEntity ne renvoie pas l'id genere : on le relit par titre + date.
    private int resolveNewTutorialId(String title) {
        for (Tutorial t : tutorialService.getAllTutorials()) {
            if (title.equals(t.getTitle())) return t.getId();
        }
        return -1;
    }

    private void notifyUsersOfNewTutorial(String tutorialTitle) {
        try {
            List<User> users = new CrudUser().getNormalUsers();
            List<String[]> recipients = new ArrayList<>();
            for (User u : users) {
                if (u.getEmail() != null && !u.getEmail().isBlank()) {
                    recipients.add(new String[]{u.getEmail(), u.getUsername()});
                }
            }
            EmailSystem.broadcastNewTutorial(recipients, tutorialTitle);
            System.out.println("Diffusion notification nouveau tutoriel a " + recipients.size() + " utilisateurs");
        } catch (Exception ex) {
            System.err.println("Echec diffusion notification tutoriel: " + ex.getMessage());
        }
    }

    @FXML
    private void handleAbort(ActionEvent event) { closeWindow(); }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
