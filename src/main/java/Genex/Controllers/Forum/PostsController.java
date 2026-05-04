package Genex.Controllers.Forum;

import Genex.entities.Posts;
import Genex.services.CrudPosts;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostsController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> typeFilterField;
    @FXML
    private ComboBox<String> statusFilterField;
    @FXML
    private ComboBox<String> moderationFilterField;
    @FXML
    private TextField forumIdField;
    @FXML
    private TextField authorIdField;
    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<String> postTypeField;
    @FXML
    private ComboBox<String> postStatusField;
    @FXML
    private TextField tagField;
    @FXML
    private TextArea bodyArea;
    @FXML
    private TextField videoPathField;
    @FXML
    private Label featuredTitleLabel;
    @FXML
    private Label featuredMoodLabel;
    @FXML
    private Label featuredTrendLabel;
    @FXML
    private Label featuredMetaLabel;
    @FXML
    private Label featuredDescLabel;
    @FXML
    private FlowPane postCardsContainer;

    private final CrudPosts crudPosts = new CrudPosts();
    private final List<Posts> posts = new ArrayList<>();
    private String selectedPostId;

    @FXML
    private void openForum(ActionEvent event) {
        switchView("/Fxml/Forum/Forum.fxml");
    }

    @FXML
    private void openPosts(ActionEvent event) {
        switchView("/Fxml/Forum/Posts.fxml");
    }

    @FXML
    public void initialize() {
        setupCombos();
        setupListeners();
        loadPosts();
        clearFormFields();
    }

    @FXML
    private void handleSelectVideo(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sélectionner une vidéo");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Vidéos", "*.mp4", "*.mov", "*.mkv", "*.avi"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        File selected = chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (selected != null) {
            videoPathField.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void handleClearVideo(ActionEvent event) {
        videoPathField.clear();
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
        clearFormFields();
    }

    @FXML
    private void handleAddPost(ActionEvent event) {
        if (!validatePostForm()) {
            return;
        }
        try {
            Posts post = new Posts(
                forumIdField.getText().trim(),
                authorIdField.getText().trim(),
                titleField.getText().trim(),
                bodyArea.getText() == null ? "" : bodyArea.getText().trim()
            );
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            crudPosts.addEntity(post);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Post créé avec succès.");
            clearFormFields();
            loadPosts();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer le post: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdatePost(ActionEvent event) {
        if (selectedPostId == null || selectedPostId.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionne un post à modifier.");
            return;
        }
        if (!validatePostForm()) {
            return;
        }
        try {
            Posts post = new Posts();
            post.setTitle(titleField.getText().trim());
            post.setBody(bodyArea.getText() == null ? "" : bodyArea.getText().trim());
            post.setUpdatedAt(LocalDateTime.now());
            crudPosts.updateEntity(post, selectedPostId);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Post modifié avec succès.");
            clearFormFields();
            loadPosts();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier le post: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeletePost(ActionEvent event) {
        if (selectedPostId == null || selectedPostId.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionne un post à supprimer.");
            return;
        }
        try {
            Posts post = new Posts();
            post.setId(selectedPostId);
            crudPosts.deleteEntity(post);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Post supprimé avec succès.");
            clearFormFields();
            loadPosts();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le post: " + e.getMessage());
        }
    }

    @FXML
    private void handleReportPost(ActionEvent event) {
        moderationFilterField.setValue("SIGNALÉ");
        showAlert(Alert.AlertType.INFORMATION, "Info", "Signalement local appliqué.");
    }

    @FXML
    private void handleHidePost(ActionEvent event) {
        moderationFilterField.setValue("MASQUÉ");
        showAlert(Alert.AlertType.INFORMATION, "Info", "Masquage local appliqué.");
    }

    @FXML
    private void handleRestorePost(ActionEvent event) {
        moderationFilterField.setValue("TOUS");
        showAlert(Alert.AlertType.INFORMATION, "Info", "Restauration locale appliquée.");
    }

    private void setupCombos() {
        postTypeField.getItems().setAll("DISCUSSION", "QUESTION", "GUIDE", "ANNONCE");
        postStatusField.getItems().setAll("ACTIF", "RÉSOLU", "ARCHIVÉ");
        typeFilterField.getItems().setAll("TOUS", "DISCUSSION", "QUESTION", "GUIDE", "ANNONCE");
        statusFilterField.getItems().setAll("TOUS", "ACTIF", "RÉSOLU", "ARCHIVÉ");
        moderationFilterField.getItems().setAll("TOUS", "VISIBLE", "MASQUÉ", "SIGNALÉ");

        postTypeField.setValue("DISCUSSION");
        postStatusField.setValue("ACTIF");
        typeFilterField.setValue("TOUS");
        statusFilterField.setValue("TOUS");
        moderationFilterField.setValue("TOUS");
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldV, newV) -> refreshPostCards());
        typeFilterField.valueProperty().addListener((obs, oldV, newV) -> refreshPostCards());
        statusFilterField.valueProperty().addListener((obs, oldV, newV) -> refreshPostCards());
        moderationFilterField.valueProperty().addListener((obs, oldV, newV) -> refreshPostCards());
    }

    private void loadPosts() {
        posts.clear();
        posts.addAll(crudPosts.getAllPosts());
        refreshPostCards();
    }

    private void refreshPostCards() {
        postCardsContainer.getChildren().clear();
        List<Posts> filtered = posts.stream().filter(this::matchesFilters).toList();
        for (Posts post : filtered) {
            postCardsContainer.getChildren().add(createPostCard(post));
        }
        updateFeatured(filtered);
    }

    private boolean matchesFilters(Posts post) {
        String search = normalize(searchField.getText());
        if (!search.isBlank()) {
            String haystack = normalize(post.getTitle()) + " " + normalize(post.getBody()) + " " + normalize(post.getAuthorId()) + " " + normalize(post.getForumId());
            if (!haystack.contains(search)) {
                return false;
            }
        }
        String typeFilter = valueOrDefault(typeFilterField.getValue(), "TOUS");
        if (!"TOUS".equals(typeFilter)) {
            String haystack = normalize(post.getTitle()) + " " + normalize(post.getBody());
            if (!haystack.contains(typeFilter.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }

    private VBox createPostCard(Posts post) {
        VBox card = new VBox(8);
        card.getStyleClass().add("forum-card");
        card.setPrefWidth(305);
        card.setPadding(new Insets(16));
        card.setOnMouseClicked(e -> selectPost(post));

        Label title = new Label(post.getTitle());
        title.getStyleClass().add("forum-card-title");
        title.setWrapText(true);

        Label body = new Label(post.getBody() == null || post.getBody().isBlank() ? "Aucun contenu." : post.getBody());
        body.getStyleClass().add("forum-card-desc");
        body.setWrapText(true);
        body.setMaxWidth(280);

        Label meta = new Label("Forum " + safe(post.getForumId()) + " • Auteur " + safe(post.getAuthorId()) + " • ID " + safe(post.getId()));
        meta.getStyleClass().add("forum-card-meta");

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().addAll("action-button", "secondary-button");
        editBtn.setOnAction(e -> {
            e.consume();
            selectPost(post);
        });

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().addAll("action-button", "danger-button");
        deleteBtn.setOnAction(e -> {
            e.consume();
            selectedPostId = post.getId();
            handleDeletePost(new ActionEvent());
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8, editBtn, spacer, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(title, body, meta, actions);
        return card;
    }

    private void selectPost(Posts post) {
        selectedPostId = post.getId();
        forumIdField.setText(safe(post.getForumId()));
        authorIdField.setText(safe(post.getAuthorId()));
        titleField.setText(safe(post.getTitle()));
        bodyArea.setText(safe(post.getBody()));
    }

    private void updateFeatured(List<Posts> filtered) {
        if (filtered.isEmpty()) {
            featuredTitleLabel.setText("Aucun post pour le moment");
            featuredMoodLabel.setText("🔥 ACTIF");
            featuredTrendLabel.setText("TENDANCE 0");
            featuredMetaLabel.setText("Forum - • Par -");
            featuredDescLabel.setText("Créez votre premier post pour le voir mis en avant ici.");
            return;
        }
        Posts featured = filtered.get(0);
        featuredTitleLabel.setText(safe(featured.getTitle()));
        featuredMoodLabel.setText("🔥 ACTIF");
        featuredTrendLabel.setText("TENDANCE " + filtered.size());
        featuredMetaLabel.setText("Forum " + safe(featured.getForumId()) + " • Par " + safe(featured.getAuthorId()));
        featuredDescLabel.setText(safe(featured.getBody()).isBlank() ? "Aucun contenu." : safe(featured.getBody()));
    }

    private boolean validatePostForm() {
        if (isBlank(forumIdField.getText())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "L'ID forum est obligatoire.");
            return false;
        }
        if (isBlank(authorIdField.getText())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "L'ID auteur est obligatoire.");
            return false;
        }
        if (isBlank(titleField.getText())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le titre est obligatoire.");
            return false;
        }
        return true;
    }

    private void clearFormFields() {
        selectedPostId = null;
        forumIdField.clear();
        authorIdField.clear();
        titleField.clear();
        bodyArea.clear();
        tagField.clear();
        videoPathField.clear();
        postTypeField.setValue("DISCUSSION");
        postStatusField.setValue("ACTIF");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String safe(String value) {
        return value == null ? "" : value;
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
