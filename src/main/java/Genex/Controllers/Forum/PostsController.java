package Genex.Controllers.Forum;

import Genex.entities.Posts;
import Genex.services.CrudPosts;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PostsController {
    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField forumIdField;
    @FXML
    private TextField authorIdField;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea bodyArea;
    @FXML
    private TextField videoPathField;
    @FXML
    private TextField tagField;
    @FXML
    private ComboBox<String> postTypeField;
    @FXML
    private ComboBox<String> postStatusField;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> typeFilterField;
    @FXML
    private ComboBox<String> statusFilterField;
    @FXML
    private ComboBox<String> moderationFilterField;
    @FXML
    private FlowPane postCardsContainer;
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

    private final CrudPosts crudPosts = new CrudPosts();
    private List<Posts> posts = new ArrayList<>();
    private Posts selectedPost;
    private static final int FORUM_ID_MIN = 1;
    private static final int FORUM_ID_MAX = 64;
    private static final int AUTHOR_MIN = 3;
    private static final int AUTHOR_MAX = 60;
    private static final int TITLE_MIN = 5;
    private static final int TITLE_MAX = 120;
    private static final int BODY_MIN = 10;
    private static final int BODY_MAX = 3000;
    private static final Pattern VIDEO_EXT_PATTERN = Pattern.compile("(?i).+\\.(mp4|mov|m4v|avi|mkv|webm)$");
    private static final Pattern TAG_PATTERN = Pattern.compile("^[\\p{L}\\p{N}_-]{0,64}$");
    private static final Pattern FORUM_ID_PATTERN = Pattern.compile("^[\\p{L}\\p{N}_-]+$");
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("^[\\p{L}\\p{N} .'-]+$");

    @FXML
    public void initialize() {
        initializeChoiceBoxes();
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        typeFilterField.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        statusFilterField.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        moderationFilterField.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, this::handleShortcuts);
            }
        });

        refreshPosts();
    }

    private void initializeChoiceBoxes() {
        postTypeField.setItems(FXCollections.observableArrayList("text", "video", "question", "poll"));
        postStatusField.setItems(FXCollections.observableArrayList("draft", "published"));
        typeFilterField.setItems(FXCollections.observableArrayList("All", "text", "video", "question", "poll"));
        statusFilterField.setItems(FXCollections.observableArrayList("All", "draft", "published"));
        moderationFilterField.setItems(FXCollections.observableArrayList("All", "visible", "reported", "hidden"));

        postTypeField.setValue("text");
        postStatusField.setValue("published");
        typeFilterField.setValue("All");
        statusFilterField.setValue("All");
        moderationFilterField.setValue("All");
    }

    @FXML
    private void handleAddPost() {
        ValidationResult validation = validateForm();
        if (!validation.valid()) {
            showAlert(Alert.AlertType.WARNING, "Saisie invalide", validation.message());
            return;
        }

        try {
            Posts post = new Posts(
                    validation.forumId(),
                    validation.authorId(),
                    validation.title(),
                    validation.body(),
                    validation.mediaType(),
                    validation.mediaUrl()
            );
            post.setPostType(validation.postType());
            post.setTag(validation.tag());
            post.setPostStatus(validation.postStatus());
            post.setModerationStatus("visible");
            post.setViews(0);
            crudPosts.addEntity(post);
            refreshPosts();
            handleClearForm();
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ajout", e.getMessage());
        }
    }

    @FXML
    private void handleUpdatePost() {
        if (selectedPost == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un post à modifier.");
            return;
        }

        ValidationResult validation = validateForm();
        if (!validation.valid()) {
            showAlert(Alert.AlertType.WARNING, "Saisie invalide", validation.message());
            return;
        }

        try {
            Posts updatedPost = new Posts();
            updatedPost.setForumId(validation.forumId());
            updatedPost.setAuthorId(validation.authorId());
            updatedPost.setTitle(validation.title());
            updatedPost.setBody(validation.body());
            updatedPost.setMediaType(validation.mediaType());
            updatedPost.setMediaUrl(validation.mediaUrl());
            updatedPost.setPostType(validation.postType());
            updatedPost.setTag(validation.tag());
            updatedPost.setPostStatus(validation.postStatus());
            updatedPost.setModerationStatus(defaultString(selectedPost.getModerationStatus(), "visible"));
            updatedPost.setViews(Math.max(0, selectedPost.getViews()));
            updatedPost.setUpdatedAt(LocalDateTime.now());
            crudPosts.updateEntity(updatedPost, selectedPost.getId());
            refreshPosts();
            handleClearForm();
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la modification", e.getMessage());
        }
    }

    @FXML
    private void handleDeletePost() {
        if (selectedPost == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un post à supprimer.");
            return;
        }

        try {
            crudPosts.deleteEntity(selectedPost);
            refreshPosts();
            handleClearForm();
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la suppression", e.getMessage());
        }
    }

    @FXML
    private void handleReportPost() {
        if (selectedPost == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un post.");
            return;
        }
        selectedPost.setModerationStatus("reported");
        persistSelectedPost("Signalement impossible.");
    }

    @FXML
    private void handleHidePost() {
        if (selectedPost == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un post.");
            return;
        }
        selectedPost.setModerationStatus("hidden");
        persistSelectedPost("Masquage impossible.");
    }

    @FXML
    private void handleRestorePost() {
        if (selectedPost == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un post.");
            return;
        }
        selectedPost.setModerationStatus("visible");
        persistSelectedPost("Restauration impossible.");
    }

    @FXML
    public void handleClearForm() {
        forumIdField.clear();
        authorIdField.clear();
        titleField.clear();
        bodyArea.clear();
        videoPathField.clear();
        tagField.clear();
        postTypeField.setValue("text");
        postStatusField.setValue("published");
        selectedPost = null;
    }

    private void refreshPosts() {
        try {
            posts = crudPosts.getAllPosts();
            applyFilter();
        } catch (IllegalStateException e) {
            posts = new ArrayList<>();
            renderCards(posts);
            updateFeaturedCard(posts);
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", e.getMessage());
        }
    }

    private void applyFilter() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String typeFilter = defaultString(typeFilterField.getValue(), "All");
        String statusFilter = defaultString(statusFilterField.getValue(), "All");
        String moderationFilter = defaultString(moderationFilterField.getValue(), "All");

        List<Posts> filtered = posts.stream()
                .filter(post -> query.isEmpty()
                        || containsIgnoreCase(post.getTitle(), query)
                        || containsIgnoreCase(post.getBody(), query)
                        || containsIgnoreCase(extractFileName(post.getMediaUrl()), query)
                        || containsIgnoreCase(post.getTag(), query)
                        || containsIgnoreCase(post.getForumId(), query)
                        || containsIgnoreCase(post.getAuthorId(), query))
                .filter(post -> "All".equalsIgnoreCase(typeFilter)
                        || defaultString(post.getPostType(), "text").equalsIgnoreCase(typeFilter))
                .filter(post -> "All".equalsIgnoreCase(statusFilter)
                        || defaultString(post.getPostStatus(), "published").equalsIgnoreCase(statusFilter))
                .filter(post -> "All".equalsIgnoreCase(moderationFilter)
                        || defaultString(post.getModerationStatus(), "visible").equalsIgnoreCase(moderationFilter))
                .sorted(Comparator
                        .comparing((Posts p) -> "reported".equalsIgnoreCase(defaultString(p.getModerationStatus(), "visible"))).reversed()
                        .thenComparing(Posts::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        renderCards(filtered);
        updateFeaturedCard(filtered);
    }

    private boolean containsIgnoreCase(String text, String query) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(query);
    }

    private void renderCards(List<Posts> items) {
        postCardsContainer.getChildren().clear();

        if (items.isEmpty()) {
            Label emptyLabel = new Label("Aucun post trouvé.");
            emptyLabel.getStyleClass().add("table-placeholder");
            postCardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Posts post : items) {
            VBox card = new VBox(8);
            card.setPrefWidth(300);
            card.getStyleClass().add("forum-card");

            Label title = new Label(defaultString(post.getTitle(), "Untitled"));
            title.getStyleClass().add("forum-card-title");

            Label body = new Label(truncate(defaultString(post.getBody(), ""), 120));
            body.setWrapText(true);
            body.getStyleClass().add("forum-card-desc");

            String mediaUrl = defaultString(post.getMediaUrl(), "");
            Label mediaLabel = new Label();
            if (!mediaUrl.isBlank()) {
                mediaLabel.setText("🎬 " + extractFileName(mediaUrl));
                mediaLabel.getStyleClass().add("forum-card-meta");
            }

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Label meta = new Label(buildMetaText(post));
            meta.getStyleClass().add("forum-card-meta");

            if (mediaUrl.isBlank()) {
                card.getChildren().addAll(title, body, spacer, meta);
            } else {
                card.getChildren().addAll(title, body, mediaLabel, spacer, meta);
            }
            card.setOnMouseClicked(event -> selectPost(post));
            postCardsContainer.getChildren().add(card);
        }
    }

    private String buildMetaText(Posts post) {
        String type = defaultString(post.getPostType(), "text");
        String status = defaultString(post.getPostStatus(), "published");
        String moderation = defaultString(post.getModerationStatus(), "visible");
        String tag = defaultString(post.getTag(), "");
        String tagText = tag.isBlank() ? "" : " • #" + tag;
        return "Forum " + defaultString(post.getForumId(), "-")
                + " • By " + defaultString(post.getAuthorId(), "-")
                + " • " + type
                + " • " + status
                + " • " + moderation
                + tagText;
    }

    private void updateFeaturedCard(List<Posts> filteredPosts) {
        if (selectedPost != null && filteredPosts.stream().anyMatch(p -> p.getId().equals(selectedPost.getId()))) {
            renderFeatured(selectedPost);
            return;
        }

        if (filteredPosts.isEmpty()) {
            featuredTitleLabel.setText("No post yet");
            featuredMoodLabel.setText("🔥 ACTIVE");
            featuredMoodLabel.getStyleClass().setAll("mood-badge", "mood-active");
            featuredTrendLabel.setText("TREND 0");
            featuredMetaLabel.setText("Forum - • By -");
            featuredDescLabel.setText("Create your first post to see it highlighted here.");
            return;
        }

        renderFeatured(filteredPosts.getFirst());
    }

    private void selectPost(Posts post) {
        selectedPost = post;
        post.setViews(Math.max(0, post.getViews()) + 1);
        persistSelectedPost("Impossible de mettre à jour les vues.");
    }

    private void populateForm(Posts post) {
        forumIdField.setText(defaultString(post.getForumId(), ""));
        authorIdField.setText(defaultString(post.getAuthorId(), ""));
        titleField.setText(defaultString(post.getTitle(), ""));
        bodyArea.setText(defaultString(post.getBody(), ""));
        videoPathField.setText(defaultString(post.getMediaUrl(), ""));
        tagField.setText(defaultString(post.getTag(), ""));
        postTypeField.setValue(defaultString(post.getPostType(), "text"));
        postStatusField.setValue(defaultString(post.getPostStatus(), "published"));
        renderFeatured(post);
    }

    private void renderFeatured(Posts post) {
        featuredTitleLabel.setText(defaultString(post.getTitle(), "Untitled"));
        String bodyText = defaultString(post.getBody(), "");
        boolean hasVideo = post.getMediaUrl() != null && !post.getMediaUrl().isBlank();
        featuredDescLabel.setText(bodyText.isBlank() ? (hasVideo ? "Post video" : "No content") : bodyText);
        String metaText = "Forum " + defaultString(post.getForumId(), "-") + " • By " + defaultString(post.getAuthorId(), "-");
        metaText += " • " + defaultString(post.getPostType(), "text");
        metaText += " • " + defaultString(post.getPostStatus(), "published");
        metaText += " • views " + Math.max(0, post.getViews());
        if (hasVideo) {
            metaText += " • 🎬 " + extractFileName(post.getMediaUrl());
        }
        if (post.getTag() != null && !post.getTag().isBlank()) {
            metaText += " • #" + post.getTag();
        }
        featuredMetaLabel.setText(metaText);

        int trend = Math.min(99, Math.max(1, post.getBody() == null ? 1 : post.getBody().length() / 20));
        featuredTrendLabel.setText("TREND " + trend);

        String moodClass;
        String moodText;
        if ("reported".equalsIgnoreCase(defaultString(post.getModerationStatus(), "visible"))) {
            moodClass = "mood-help";
            moodText = "🚩 REPORTED";
        } else if ("draft".equalsIgnoreCase(defaultString(post.getPostStatus(), "published"))) {
            moodClass = "mood-update";
            moodText = "📝 DRAFT";
        } else if ("video".equalsIgnoreCase(defaultString(post.getPostType(), "text"))) {
            moodClass = "mood-strategy";
            moodText = "🎬 VIDEO";
        } else {
            moodClass = "mood-active";
            moodText = "🔥 ACTIVE";
        }

        featuredMoodLabel.setText(moodText);
        featuredMoodLabel.getStyleClass().setAll("mood-badge", moodClass);
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return defaultString(text, "");
        }
        return text.substring(0, maxLength - 1) + "…";
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String extractFileName(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        return new File(path).getName();
    }

    private ValidationResult validateForm() {
        String forumId = forumIdField.getText() == null ? "" : forumIdField.getText().trim();
        String authorId = authorIdField.getText() == null ? "" : authorIdField.getText().trim();
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String body = bodyArea.getText() == null ? "" : bodyArea.getText().trim();
        String videoPath = videoPathField.getText() == null ? "" : videoPathField.getText().trim();
        String tag = tagField.getText() == null ? "" : tagField.getText().trim();
        String postType = defaultString(postTypeField.getValue(), "text");
        String postStatus = defaultString(postStatusField.getValue(), "published");

        if (forumId.isBlank() || authorId.isBlank() || title.isBlank()) {
            return ValidationResult.invalid("Forum id, author id et titre sont obligatoires.");
        }
        if (body.isBlank() && videoPath.isBlank()) {
            return ValidationResult.invalid("Ajoutez un contenu texte ou une vidéo.");
        }
        if (forumId.length() < FORUM_ID_MIN || forumId.length() > FORUM_ID_MAX || !FORUM_ID_PATTERN.matcher(forumId).matches()) {
            return ValidationResult.invalid("Le forum id est invalide.");
        }
        if (authorId.length() < AUTHOR_MIN || authorId.length() > AUTHOR_MAX || !AUTHOR_PATTERN.matcher(authorId).matches()) {
            return ValidationResult.invalid("Le champ author id est invalide.");
        }
        if (title.length() < TITLE_MIN || title.length() > TITLE_MAX) {
            return ValidationResult.invalid("Le titre doit contenir entre " + TITLE_MIN + " et " + TITLE_MAX + " caractères.");
        }
        if (!body.isBlank() && (body.length() < BODY_MIN || body.length() > BODY_MAX)) {
            return ValidationResult.invalid("Le contenu doit contenir entre " + BODY_MIN + " et " + BODY_MAX + " caractères.");
        }
        if (!videoPath.isBlank() && !VIDEO_EXT_PATTERN.matcher(videoPath).matches()) {
            return ValidationResult.invalid("Le fichier vidéo doit être au format mp4, mov, m4v, avi, mkv ou webm.");
        }
        if (!tag.isBlank() && !TAG_PATTERN.matcher(tag).matches()) {
            return ValidationResult.invalid("Le tag accepte uniquement lettres, chiffres, _ et -.");
        }
        if (!postType.equals("text") && videoPath.isBlank() && postType.equals("video")) {
            return ValidationResult.invalid("Un post vidéo doit contenir un fichier vidéo.");
        }

        return ValidationResult.valid(forumId, authorId, title, body, videoPath, postType, postStatus, tag);
    }

    @FXML
    private void handleSelectVideo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une vidéo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers vidéo", "*.mp4", "*.mov", "*.m4v", "*.avi", "*.mkv", "*.webm")
        );

        Stage stage = (Stage) rootPane.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            videoPathField.setText(file.getAbsolutePath());
            postTypeField.setValue("video");
        }
    }

    @FXML
    private void handleClearVideo() {
        videoPathField.clear();
        if ("video".equalsIgnoreCase(defaultString(postTypeField.getValue(), "text"))) {
            postTypeField.setValue("text");
        }
    }

    private void persistSelectedPost(String fallbackMessage) {
        try {
            Posts updatedPost = new Posts();
            updatedPost.setForumId(defaultString(selectedPost.getForumId(), ""));
            updatedPost.setAuthorId(defaultString(selectedPost.getAuthorId(), ""));
            updatedPost.setTitle(defaultString(selectedPost.getTitle(), ""));
            updatedPost.setBody(defaultString(selectedPost.getBody(), ""));
            updatedPost.setMediaType(selectedPost.getMediaUrl() == null || selectedPost.getMediaUrl().isBlank() ? null : "video");
            updatedPost.setMediaUrl(selectedPost.getMediaUrl());
            updatedPost.setPostType(defaultString(selectedPost.getPostType(), "text"));
            updatedPost.setTag(selectedPost.getTag());
            updatedPost.setPostStatus(defaultString(selectedPost.getPostStatus(), "published"));
            updatedPost.setModerationStatus(defaultString(selectedPost.getModerationStatus(), "visible"));
            updatedPost.setViews(Math.max(0, selectedPost.getViews()));
            updatedPost.setUpdatedAt(LocalDateTime.now());
            crudPosts.updateEntity(updatedPost, selectedPost.getId());
            refreshPosts();
            selectedPost = findPostById(selectedPost.getId());
            if (selectedPost != null) {
                populateForm(selectedPost);
            }
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", defaultString(e.getMessage(), fallbackMessage));
        }
    }

    private Posts findPostById(String id) {
        return posts.stream()
                .filter(post -> id != null && id.equals(post.getId()))
                .findFirst()
                .orElse(null);
    }

    private void handleShortcuts(KeyEvent event) {
        if (!event.isControlDown()) {
            return;
        }
        if (event.getCode() == KeyCode.S) {
            if (selectedPost == null) {
                handleAddPost();
            } else {
                handleUpdatePost();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.N) {
            handleClearForm();
            event.consume();
        } else if (event.getCode() == KeyCode.R) {
            handleReportPost();
            event.consume();
        }
    }

    @FXML
    private void openForum() {
        switchScene("/Fxml/Forum/Forum.fxml", "GENEX - Forum");
    }

    @FXML
    private void openPosts() {
        // Déjà sur la page Posts.
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 720));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation impossible", "Impossible d'ouvrir la page demandée.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record ValidationResult(boolean valid, String forumId, String authorId, String title, String body,
                                    String mediaUrl, String postType, String postStatus, String tag, String message) {
        private String mediaType() {
            return mediaUrl == null || mediaUrl.isBlank() ? null : "video";
        }

        private static ValidationResult valid(String forumId, String authorId, String title, String body,
                                              String mediaUrl, String postType, String postStatus, String tag) {
            return new ValidationResult(true, forumId, authorId, title, body, mediaUrl, postType, postStatus, tag, "");
        }

        private static ValidationResult invalid(String message) {
            return new ValidationResult(false, "", "", "", "", "", "", "", "", message);
        }
    }
}
