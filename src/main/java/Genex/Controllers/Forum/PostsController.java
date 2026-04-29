package Genex.Controllers.Forum;

import Genex.entities.Posts;
import Genex.services.CrudPosts;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private TextField searchField;
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
    private static final Pattern FORUM_ID_PATTERN = Pattern.compile("^[\\p{L}\\p{N}_-]+$");
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("^[\\p{L}\\p{N} .'-]+$");

    @FXML
    public void initialize() {
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter(newValue));
        refreshPosts();
    }

    @FXML
    private void handleAddPost() {
        ValidationResult validation = validateForm();
        if (!validation.valid()) {
            showAlert(Alert.AlertType.WARNING, "Saisie invalide", validation.message());
            return;
        }

        try {
            Posts post = new Posts(validation.forumId(), validation.authorId(), validation.title(), validation.body());
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
    public void handleClearForm() {
        forumIdField.clear();
        authorIdField.clear();
        titleField.clear();
        bodyArea.clear();
        selectedPost = null;
    }

    private void refreshPosts() {
        try {
            posts = crudPosts.getAllPosts();
            applyFilter(searchField.getText());
        } catch (IllegalStateException e) {
            posts = new ArrayList<>();
            renderCards(posts);
            updateFeaturedCard(posts);
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", e.getMessage());
        }
    }

    private void applyFilter(String searchText) {
        String query = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);
        List<Posts> filtered = posts.stream()
                .filter(post -> query.isEmpty()
                        || containsIgnoreCase(post.getTitle(), query)
                        || containsIgnoreCase(post.getBody(), query)
                        || containsIgnoreCase(post.getForumId(), query)
                        || containsIgnoreCase(post.getAuthorId(), query))
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

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Label meta = new Label(buildMetaText(post));
            meta.getStyleClass().add("forum-card-meta");

            card.getChildren().addAll(title, body, spacer, meta);
            card.setOnMouseClicked(event -> selectPost(post));
            postCardsContainer.getChildren().add(card);
        }
    }

    private String buildMetaText(Posts post) {
        return "Forum " + defaultString(post.getForumId(), "-") + " • By " + defaultString(post.getAuthorId(), "-");
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
        forumIdField.setText(defaultString(post.getForumId(), ""));
        authorIdField.setText(defaultString(post.getAuthorId(), ""));
        titleField.setText(defaultString(post.getTitle(), ""));
        bodyArea.setText(defaultString(post.getBody(), ""));
        renderFeatured(post);
    }

    private void renderFeatured(Posts post) {
        featuredTitleLabel.setText(defaultString(post.getTitle(), "Untitled"));
        featuredDescLabel.setText(defaultString(post.getBody(), "No content"));
        featuredMetaLabel.setText("Forum " + defaultString(post.getForumId(), "-") + " • By " + defaultString(post.getAuthorId(), "-"));

        int trend = Math.min(99, Math.max(1, post.getBody() == null ? 1 : post.getBody().length() / 20));
        featuredTrendLabel.setText("TREND " + trend);

        String moodClass;
        String moodText;
        int titleLength = post.getTitle() == null ? 0 : post.getTitle().length();
        if (titleLength >= 18) {
            moodClass = "mood-strategy";
            moodText = "🎯 STRATEGY";
        } else if (titleLength >= 12) {
            moodClass = "mood-update";
            moodText = "✅ UPDATE";
        } else if (titleLength >= 8) {
            moodClass = "mood-help";
            moodText = "🛟 HELP";
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

    private ValidationResult validateForm() {
        String forumId = forumIdField.getText() == null ? "" : forumIdField.getText().trim();
        String authorId = authorIdField.getText() == null ? "" : authorIdField.getText().trim();
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String body = bodyArea.getText() == null ? "" : bodyArea.getText().trim();

        if (forumId.isBlank() || authorId.isBlank() || title.isBlank() || body.isBlank()) {
            return ValidationResult.invalid("Tous les champs sont obligatoires.");
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
        if (body.length() < BODY_MIN || body.length() > BODY_MAX) {
            return ValidationResult.invalid("Le contenu doit contenir entre " + BODY_MIN + " et " + BODY_MAX + " caractères.");
        }

        return ValidationResult.valid(forumId, authorId, title, body);
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
                                    String message) {
        private static ValidationResult valid(String forumId, String authorId, String title, String body) {
            return new ValidationResult(true, forumId, authorId, title, body, "");
        }

        private static ValidationResult invalid(String message) {
            return new ValidationResult(false, "", "", "", "", message);
        }
    }
}
