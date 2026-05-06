package Genex.Controllers.Forum;

import Genex.entities.Forum;
import Genex.entities.Posts;
import Genex.entities.User;
import Genex.services.CrudForum;
import Genex.services.CrudPosts;
import Genex.services.CrudUser;
import Genex.services.NewsService;
import Genex.utils.SessionManager;
import javafx.application.Platform;
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
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

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
    private ComboBox<String> forumNameField;
    @FXML
    private ComboBox<String> authorNameField;
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
    private HBox newsFeedContainer;
    @FXML
    private FlowPane postCardsContainer;
    @FXML
    private Button newPostButton;
    @FXML
    private Button addPostButton;
    @FXML
    private Button updatePostButton;
    @FXML
    private Button deletePostButton;
    @FXML
    private Button reportPostButton;
    @FXML
    private Button hidePostButton;
    @FXML
    private Button restorePostButton;
    @FXML
    private Button selectVideoButton;
    @FXML
    private Button clearVideoButton;
    @FXML
    private TextField imagePathField;
    @FXML
    private Button selectImageButton;
    @FXML
    private Button clearImageButton;

    private final CrudPosts crudPosts = new CrudPosts();
    private final CrudForum crudForum = new CrudForum();
    private final CrudUser crudUser = new CrudUser();
    private final NewsService newsService = new NewsService();
    private final List<Posts> posts = new ArrayList<>();
    private final List<Forum> forums = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final Map<String, ModerationState> moderationByPostId = new HashMap<>();
    private final Map<String, ReactionCounter> reactionsByPostId = new HashMap<>();
    private final Map<String, String> forumNameById = new HashMap<>();
    private final Map<String, String> forumIdByName = new LinkedHashMap<>();
    private final Map<String, String> authorNameById = new HashMap<>();
    private final Map<String, String> authorIdByName = new LinkedHashMap<>();
    private final Map<String, Long> userPostTimestamps = new HashMap<>();
    private final Map<String, Long> userBanUntil = new HashMap<>();
    private static final int CONSECUTIVE_POSTS_LIMIT = 3;
    private static final long BAN_DURATION_MINUTES = 30;
    private static final Set<String> SPAM_KEYWORDS = Set.of(
        "free", "giveaway", "urgent", "winner", "bitcoin", "promo", "click", "offer"
    );
    private static final Set<String> FILTERED_SPEECH_KEYWORDS = Set.of(
        "idiot", "imbecile", "stupid", "hate", "racist", "violent", "con", "merde",
        "putain", "connard", "salaud", "débile", "nul", "pourri", "minables",
        "fuck", "shit", "damn", "asshole", "bastard", "bitch", "crap", "dickhead",
        "fucker", "motherfucker", "whore", "slut", "faggot", "retard", "ugly",
        "pédé", "pute", "saloperie", "ordure", "enculé", "ducon", "biatch"
    );
    private boolean adminMode;
    private String currentUserId;
    private String currentUserName;
    private String selectedPostId;

    private enum ModerationState {
        VISIBLE("VISIBLE"),
        MASQUE("MASQUÉ"),
        SIGNALE("SIGNALÉ"),
        SPAM("SPAM");

        private final String label;

        ModerationState(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    private enum ReactionType {
        LIKE,
        HEART,
        LAUGH
    }

    private static final class ReactionCounter {
        private int likes;
        private int hearts;
        private int laughs;

        private void increment(ReactionType type) {
            switch (type) {
                case LIKE -> likes++;
                case HEART -> hearts++;
                case LAUGH -> laughs++;
            }
        }

        private String summary() {
            return "👍 " + likes + " • ❤️ " + hearts + " • 😂 " + laughs;
        }
    }

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
        resolveCurrentRole();
        setupCombos();
        loadForumCatalog();
        loadAuthorCatalog();
        setupListeners();
        loadPosts();
        clearFormFields();
        applyRolePermissions();
        loadHeaderInfo();
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
    private void handleSelectImage(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Sélectionner une image");
        chooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        File selected = chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (selected != null) {
            imagePathField.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void handleClearImage(ActionEvent event) {
        imagePathField.clear();
    }

    @FXML
    private void handleClearForm(ActionEvent event) {
        clearFormFields();
    }

    @FXML
    private void handleRefreshHeaderData(ActionEvent event) {
        loadNews();
    }

    @FXML
    private void handleAddPost(ActionEvent event) {
        if (!validatePostForm()) {
            return;
        }

        String authorId = resolveAuthorForCreate();
        if (isUserBanned(authorId)) {
            showAlert(Alert.AlertType.WARNING, "Accès refusé", "Cet auteur est banni temporairement pour spam (30 min).");
            return;
        }

        int consecutiveCount = countConsecutivePostsByAuthor(authorId);
        if (consecutiveCount >= CONSECUTIVE_POSTS_LIMIT) {
            banUserTemporarily(authorId);
            showAlert(Alert.AlertType.WARNING, "SPAM DÉTECTÉ", "Auteur banni pour 30 minutes : 3 posts consécutifs détectés.");
            return;
        }

        try {
            String rawTitle = titleField.getText().trim();
            String rawBody = bodyArea.getText() == null ? "" : bodyArea.getText().trim();
            boolean hadFilteredSpeech = containsFilteredSpeech(rawTitle) || containsFilteredSpeech(rawBody);
            int badwordCount = countBadwords(rawTitle) + countBadwords(rawBody);
            
            String cleanTitle = sanitizeSpeech(rawTitle);
            String cleanBody = sanitizeSpeech(rawBody);
            boolean isSpam = isSpamContent(cleanTitle, cleanBody);

            Posts post = new Posts(
                resolveForumIdForCreate(),
                authorId,
                cleanTitle,
                cleanBody,
                imagePathField.getText().isEmpty() ? null : imagePathField.getText(),
                "image"
            );
            post.setTag(tagField.getText().isEmpty() ? null : tagField.getText());
            post.setPostType(postTypeField.getValue() != null ? postTypeField.getValue() : "text");
            post.setPostStatus(postStatusField.getValue() != null ? postStatusField.getValue() : "published");
            post.setModerationStatus(isSpam || hadFilteredSpeech ? "flagged" : "visible");
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            crudPosts.addEntity(post);

            recordUserPostTimestamp(authorId);

            if (!isBlank(post.getId())) {
                moderationByPostId.put(post.getId(), isSpam ? ModerationState.SPAM : ModerationState.VISIBLE);
                reactionsByPostId.putIfAbsent(post.getId(), new ReactionCounter());
            }

            StringBuilder message = new StringBuilder("Post créé avec succès.");
            if (isSpam) {
                message.append(" Le post est marqué SPAM.");
            }
            if (hadFilteredSpeech) {
                message.append(" " + badwordCount + " gros mot(s) détecté(s) et filtré(s).");
            }
            showAlert(Alert.AlertType.INFORMATION, "Succès", message.toString());
            clearFormFields();
            loadPosts();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer le post: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdatePost(ActionEvent event) {
        if (!canManageSelectedPost("modifier ce post")) {
            return;
        }
        if (selectedPostId == null || selectedPostId.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionne un post à modifier.");
            return;
        }
        if (!validatePostForm()) {
            return;
        }
        try {
            String rawTitle = titleField.getText().trim();
            String rawBody = bodyArea.getText() == null ? "" : bodyArea.getText().trim();
            boolean hadFilteredSpeech = containsFilteredSpeech(rawTitle) || containsFilteredSpeech(rawBody);
            int badwordCount = countBadwords(rawTitle) + countBadwords(rawBody);
            String cleanTitle = sanitizeSpeech(rawTitle);
            String cleanBody = sanitizeSpeech(rawBody);
            boolean isSpam = isSpamContent(cleanTitle, cleanBody);

            Posts post = new Posts();
            post.setTitle(cleanTitle);
            post.setBody(cleanBody);
            post.setMediaUrl(imagePathField.getText().isEmpty() ? null : imagePathField.getText());
            post.setMediaType(imagePathField.getText().isEmpty() ? null : "image");
            post.setTag(tagField.getText().isEmpty() ? null : tagField.getText());
            post.setPostType(postTypeField.getValue() != null ? postTypeField.getValue() : "text");
            post.setPostStatus(postStatusField.getValue() != null ? postStatusField.getValue() : "published");
            post.setModerationStatus(isSpam || hadFilteredSpeech ? "flagged" : "visible");
            post.setUpdatedAt(LocalDateTime.now());
            crudPosts.updateEntity(post, selectedPostId);

            moderationByPostId.put(selectedPostId, isSpam ? ModerationState.SPAM : ModerationState.VISIBLE);
            reactionsByPostId.putIfAbsent(selectedPostId, new ReactionCounter());

            StringBuilder message = new StringBuilder("Post modifié avec succès.");
            if (isSpam) {
                message.append(" Le post est marqué SPAM.");
            }
            if (hadFilteredSpeech) {
                message.append(" " + badwordCount + " gros mot(s) détecté(s) et filtré(s).");
            }
            showAlert(Alert.AlertType.INFORMATION, "Succès", message.toString());
            clearFormFields();
            loadPosts();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier le post: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeletePost(ActionEvent event) {
        if (!canManageSelectedPost("supprimer ce post")) {
            return;
        }
        if (selectedPostId == null || selectedPostId.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionne un post à supprimer.");
            return;
        }
        try {
            Posts post = new Posts();
            post.setId(selectedPostId);
            crudPosts.deleteEntity(post);
            moderationByPostId.remove(selectedPostId);
            reactionsByPostId.remove(selectedPostId);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Post supprimé avec succès.");
            clearFormFields();
            loadPosts();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le post: " + e.getMessage());
        }
    }

    @FXML
    private void handleReportPost(ActionEvent event) {
        if (isBlank(selectedPostId)) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionne un post à signaler.");
            return;
        }
        moderationByPostId.put(selectedPostId, ModerationState.SIGNALE);
        refreshPostCards();
        showAlert(Alert.AlertType.INFORMATION, "Info", "Post signalé.");
    }

    @FXML
    private void handleHidePost(ActionEvent event) {
        if (!requireAdminAction("masquer un post")) {
            return;
        }
        if (isBlank(selectedPostId)) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionne un post à masquer.");
            return;
        }
        moderationByPostId.put(selectedPostId, ModerationState.MASQUE);
        refreshPostCards();
        showAlert(Alert.AlertType.INFORMATION, "Info", "Post masqué.");
    }

    @FXML
    private void handleRestorePost(ActionEvent event) {
        if (!requireAdminAction("restaurer un post")) {
            return;
        }
        if (isBlank(selectedPostId)) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionne un post à restaurer.");
            return;
        }
        moderationByPostId.put(selectedPostId, ModerationState.VISIBLE);
        refreshPostCards();
        showAlert(Alert.AlertType.INFORMATION, "Info", "Post restauré.");
    }

    private void setupCombos() {
        postTypeField.getItems().setAll("DISCUSSION", "QUESTION", "GUIDE", "ANNONCE");
        postStatusField.getItems().setAll("ACTIF", "RÉSOLU", "ARCHIVÉ");
        typeFilterField.getItems().setAll("TOUS", "DISCUSSION", "QUESTION", "GUIDE", "ANNONCE");
        statusFilterField.getItems().setAll("TOUS", "ACTIF", "RÉSOLU", "ARCHIVÉ");
        moderationFilterField.getItems().setAll("TOUS", "VISIBLE", "MASQUÉ", "SIGNALÉ", "SPAM");

        postTypeField.setValue("DISCUSSION");
        postStatusField.setValue("ACTIF");
        typeFilterField.setValue("TOUS");
        statusFilterField.setValue("TOUS");
        moderationFilterField.setValue("TOUS");

        authorNameField.setEditable(true);
    }

    private void loadHeaderInfo() {
        loadNews();
    }

    private void loadNews() {
        newsFeedContainer.getChildren().clear();
        newsFeedContainer.getChildren().add(buildLoadingNewsCard());

        CompletableFuture.runAsync(() -> {
            try {
                List<NewsService.NewsItem> items = newsService.getGamingFeed(4);
                Platform.runLater(() -> {
                    renderNewsFeed(items);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    renderNewsError();
                });
            } catch (IOException | IllegalStateException e) {
                Platform.runLater(() -> {
                    renderNewsError();
                });
            }
        });
    }

    private void renderNewsFeed(List<NewsService.NewsItem> items) {
        newsFeedContainer.getChildren().clear();
        for (NewsService.NewsItem item : items) {
            newsFeedContainer.getChildren().add(createNewsCard(item));
        }
    }

    private VBox buildLoadingNewsCard() {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("forum-card", "news-card");
        card.setPrefWidth(260);
        card.setMinHeight(150);
        card.setMaxHeight(150);
        card.setPadding(new Insets(14));

        Label title = new Label("Chargement des actualités...");
        title.getStyleClass().add("forum-card-title");
        title.setWrapText(true);

        Label meta = new Label("Source: Reddit r/gaming");
        meta.getStyleClass().add("forum-card-meta");
        card.getChildren().addAll(title, meta);
        return card;
    }

    private void renderNewsError() {
        newsFeedContainer.getChildren().clear();
        VBox card = new VBox(8);
        card.getStyleClass().addAll("forum-card", "news-card");
        card.setPrefWidth(260);
        card.setMinHeight(150);
        card.setMaxHeight(150);
        card.setPadding(new Insets(14));

        Label title = new Label("Flux news indisponible.");
        title.getStyleClass().add("forum-card-title");
        title.setWrapText(true);

        Label meta = new Label("Réessayez avec ↻");
        meta.getStyleClass().add("forum-card-meta");
        card.getChildren().addAll(title, meta);
        newsFeedContainer.getChildren().add(card);
    }

    private VBox createNewsCard(NewsService.NewsItem item) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("forum-card", "news-card");
        card.setPrefWidth(260);
        card.setMinHeight(150);
        card.setMaxHeight(150);
        card.setPadding(new Insets(12));

        Node mediaNode = createNewsMediaNode(item.imageUrl());

        Label title = new Label(item.title());
        title.getStyleClass().addAll("forum-card-title", "news-title");
        title.setWrapText(true);
        title.setMaxWidth(236);

        Label meta = new Label(item.source());
        meta.getStyleClass().add("forum-card-meta");

        card.getChildren().addAll(mediaNode, title, meta);
        return card;
    }

    private Node createNewsMediaNode(String imageUrl) {
        if (!isBlank(imageUrl)) {
            try {
                Image image = new Image(imageUrl, false);
                if (!image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(236);
                    imageView.setFitHeight(82);
                    imageView.setPreserveRatio(false);
                    imageView.getStyleClass().add("news-image");
                    return imageView;
                }
            } catch (IllegalArgumentException ignored) {
                // fallback placeholder below
            }
        }

        Label placeholder = new Label("📰 ACTU");
        placeholder.getStyleClass().add("news-image-placeholder");
        placeholder.setMinSize(236, 82);
        placeholder.setPrefSize(236, 82);
        placeholder.setMaxSize(236, 82);
        return placeholder;
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
        synchronizePostMetadata();
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
            String haystack = normalize(post.getTitle()) + " " + normalize(post.getBody()) + " " + normalize(authorDisplayName(post)) + " " + normalize(forumDisplayName(post));
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
        String moderationFilter = valueOrDefault(moderationFilterField.getValue(), "TOUS");
        if (!"TOUS".equals(moderationFilter) && !moderationLabel(post).equals(moderationFilter)) {
            return false;
        }
        return true;
    }

    private VBox createPostCard(Posts post) {
        VBox card = new VBox(8);
        card.getStyleClass().add("forum-card");
        card.setPrefWidth(420);
        card.setPadding(new Insets(18));
        card.setOnMouseClicked(e -> selectPost(post));

        Label title = new Label(post.getTitle());
        title.getStyleClass().add("forum-card-title");
        title.setWrapText(true);

        // Afficher l'image si elle existe
        if (post.getMediaUrl() != null && !post.getMediaUrl().isBlank() && "image".equals(post.getMediaType())) {
            try {
                ImageView imageView = new ImageView();
                imageView.setImage(new Image("file:" + post.getMediaUrl()));
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(380);
                imageView.setFitHeight(220);
                card.getChildren().add(imageView);
            } catch (Exception e) {
                System.out.println("Erreur chargement image: " + e.getMessage());
            }
        }

        Label body = new Label(post.getBody() == null || post.getBody().isBlank() ? "Aucun contenu." : post.getBody());
        body.getStyleClass().add("forum-card-desc");
        body.setWrapText(true);
        body.setMaxWidth(390);

        Label meta = new Label("Forum " + forumDisplayName(post) + " • Auteur " + authorDisplayName(post) + " • ID " + safe(post.getId()));
        meta.getStyleClass().add("forum-card-meta");

        Label moderation = new Label("Modération: " + moderationLabel(post));
        moderation.getStyleClass().add("forum-card-meta");

        Label reactions = new Label(reactionSummary(post));
        reactions.getStyleClass().add("forum-card-meta");

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

        Button likeBtn = createReactionButton("👍", post, ReactionType.LIKE, "reaction-like");
        Button heartBtn = createReactionButton("❤️", post, ReactionType.HEART, "reaction-heart");
        Button laughBtn = createReactionButton("😂", post, ReactionType.LAUGH, "reaction-laugh");

        Button repostXBtn = createRepostButton("𝕏", post, "X", "social-x");
        Button repostFacebookBtn = createRepostButton("f", post, "FACEBOOK", "social-facebook");
        Button repostLinkedInBtn = createRepostButton("in", post, "LINKEDIN", "social-linkedin");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8);
        if (canManagePost(post)) {
            actions.getChildren().addAll(editBtn, spacer, deleteBtn);
        } else {
            Label readOnlyBadge = new Label("Lecture seule");
            readOnlyBadge.getStyleClass().add("forum-card-meta");
            actions.getChildren().add(readOnlyBadge);
        }
        actions.setAlignment(Pos.CENTER_LEFT);
        FlowPane reactionActions = new FlowPane(8, 8, likeBtn, heartBtn, laughBtn);
        reactionActions.getStyleClass().add("post-actions-row");
        FlowPane repostActions = new FlowPane(14, 10, repostXBtn, repostFacebookBtn, repostLinkedInBtn);
        repostActions.getStyleClass().addAll("post-actions-row", "social-actions-row");

        card.getChildren().addAll(title, body, meta, moderation, reactions, reactionActions, repostActions, actions);
        return card;
    }

    private void selectPost(Posts post) {
        selectedPostId = post.getId();
        forumNameField.setValue(forumNameForId(post.getForumId()));
        authorNameField.setValue(authorNameForId(post.getAuthorId()));
        titleField.setText(safe(post.getTitle()));
        bodyArea.setText(safe(post.getBody()));
        tagField.setText(safe(post.getTag()));
        postTypeField.setValue(safe(post.getPostType()));
        postStatusField.setValue(safe(post.getPostStatus()));
        if (post.getMediaUrl() != null && !post.getMediaUrl().isBlank()) {
            imagePathField.setText(post.getMediaUrl());
        } else {
            imagePathField.clear();
        }
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
        featuredMetaLabel.setText("Forum " + forumDisplayName(featured) + " • Par " + authorDisplayName(featured));
        featuredDescLabel.setText(safe(featured.getBody()).isBlank() ? "Aucun contenu." : safe(featured.getBody()));
    }

    private void synchronizePostMetadata() {
        Set<String> existingIds = new HashSet<>();
        for (Posts post : posts) {
            if (isBlank(post.getId())) {
                continue;
            }
            existingIds.add(post.getId());
            reactionsByPostId.putIfAbsent(post.getId(), new ReactionCounter());
            moderationByPostId.putIfAbsent(post.getId(), ModerationState.VISIBLE);
            if (isSpamContent(post.getTitle(), post.getBody()) && moderationByPostId.get(post.getId()) == ModerationState.VISIBLE) {
                moderationByPostId.put(post.getId(), ModerationState.SPAM);
            }
        }
        reactionsByPostId.keySet().removeIf(id -> !existingIds.contains(id));
        moderationByPostId.keySet().removeIf(id -> !existingIds.contains(id));
    }

    private String reactionSummary(Posts post) {
        if (isBlank(post.getId())) {
            return "👍 0 • ❤️ 0 • 😂 0";
        }
        return reactionsByPostId.computeIfAbsent(post.getId(), key -> new ReactionCounter()).summary();
    }

    private String moderationLabel(Posts post) {
        if (isBlank(post.getId())) {
            return ModerationState.VISIBLE.label();
        }
        return moderationByPostId.computeIfAbsent(post.getId(), key -> ModerationState.VISIBLE).label();
    }

    private Button createReactionButton(String text, Posts post, ReactionType reactionType, String variantClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-button", "reaction-emoji-button", variantClass);
        button.setMinWidth(52);
        button.setOnAction(e -> {
            e.consume();
            if (isBlank(post.getId())) {
                return;
            }
            reactionsByPostId.computeIfAbsent(post.getId(), key -> new ReactionCounter()).increment(reactionType);
            refreshPostCards();
        });
        return button;
    }

    private Button createRepostButton(String text, Posts post, String network, String logoClass) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-button", "social-logo-button", logoClass);
        button.setMinWidth(44);
        button.setOnAction(e -> {
            e.consume();
            repostToSocial(post, network);
        });
        return button;
    }

    private void repostToSocial(Posts post, String network) {
        String postRef = "https://genex.app/posts/" + safe(post.getId());
        String shareText = safe(post.getTitle()).isBlank() ? "Nouveau post sur Genex" : safe(post.getTitle());
        String encodedUrl = URLEncoder.encode(postRef, StandardCharsets.UTF_8);
        String encodedText = URLEncoder.encode(shareText, StandardCharsets.UTF_8);

        String shareUrl;
        switch (network) {
            case "FACEBOOK" -> shareUrl = "https://www.facebook.com/sharer/sharer.php?u=" + encodedUrl + "&quote=" + encodedText;
            case "LINKEDIN" -> shareUrl = "https://www.linkedin.com/sharing/share-offsite/?url=" + encodedUrl;
            default -> shareUrl = "https://x.com/intent/tweet?text=" + encodedText + "&url=" + encodedUrl;
        }

        try {
            openExternalUrl(shareUrl);
            showAlert(Alert.AlertType.INFORMATION, "Repost", "Ouverture du partage " + network + "...");
        } catch (IOException | URISyntaxException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le lien de partage.\n" + shareUrl);
        }
    }

    private void openExternalUrl(String url) throws IOException, URISyntaxException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(new URI(url));
            return;
        }

        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("linux")) {
            if (tryLaunchUrl("xdg-open", url) || tryLaunchUrl("gio", "open", url)) {
                return;
            }
        } else if (os.contains("mac")) {
            if (tryLaunchUrl("open", url)) {
                return;
            }
        } else if (os.contains("win")) {
            if (tryLaunchUrl("rundll32", "url.dll,FileProtocolHandler", url)) {
                return;
            }
        }
        throw new IOException("No browser launcher available");
    }

    private boolean tryLaunchUrl(String... command) {
        try {
            new ProcessBuilder(command).start();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isSpamContent(String title, String body) {
        String text = normalize(title) + " " + normalize(body);
        int keywordHits = 0;
        for (String keyword : SPAM_KEYWORDS) {
            if (text.contains(keyword)) {
                keywordHits++;
            }
        }
        int urlHits = text.split("https?://|www\\.", -1).length - 1;
        boolean repetitive = Pattern.compile("(.)\\1{6,}").matcher(text).find();
        return keywordHits >= 2 || urlHits >= 2 || repetitive;
    }

    private boolean containsFilteredSpeech(String value) {
        String normalized = normalize(value).toLowerCase();
        for (String keyword : FILTERED_SPEECH_KEYWORDS) {
            if (normalized.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private int countBadwords(String value) {
        String normalized = normalize(value).toLowerCase();
        int count = 0;
        for (String keyword : FILTERED_SPEECH_KEYWORDS) {
            String pattern = "(?i)\\b" + Pattern.quote(keyword) + "\\b";
            int matches = (int) Pattern.compile(pattern).matcher(normalized).results().count();
            count += matches;
        }
        return count;
    }

    private String sanitizeSpeech(String value) {
        String sanitized = safe(value);
        for (String keyword : FILTERED_SPEECH_KEYWORDS) {
            String replacement = "*".repeat(Math.min(6, keyword.length()));
            sanitized = sanitized.replaceAll("(?i)\\b" + Pattern.quote(keyword) + "\\b", replacement);
        }
        return sanitized;
    }

    private boolean validatePostForm() {
        if (isBlank(forumNameField.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nom du forum est obligatoire.");
            return false;
        }
        if (isBlank(resolveForumIdForCreate())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le forum sélectionné est introuvable.");
            return false;
        }
        if (isBlank(authorNameField.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le nom auteur est obligatoire.");
            return false;
        }
        if (isBlank(resolveAuthorForCreate())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "L'auteur sélectionné est introuvable.");
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
        if (!forumNameField.getItems().isEmpty()) {
            forumNameField.setValue(forumNameField.getItems().get(0));
        } else {
            forumNameField.setValue(null);
        }
        if (adminMode) {
            if (!authorNameField.getItems().isEmpty()) {
                authorNameField.setValue(authorNameField.getItems().get(0));
            } else {
                authorNameField.setValue(null);
            }
        } else {
            authorNameField.setValue(resolveAuthorNameForCurrentUser());
        }
        titleField.clear();
        bodyArea.clear();
        tagField.clear();
        videoPathField.clear();
        imagePathField.clear();
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

    private void resolveCurrentRole() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        adminMode = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
        currentUserId = currentUser != null ? currentUser.getId() : null;
        currentUserName = currentUser != null ? currentUser.getUsername() : null;
    }

    private void applyRolePermissions() {
        if (adminMode) {
            return;
        }
        hideNode(hidePostButton);
        hideNode(restorePostButton);
        forumNameField.setDisable(false);
        authorNameField.setDisable(true);
        authorNameField.setValue(resolveAuthorNameForCurrentUser());
    }

    private boolean requireAdminAction(String actionLabel) {
        if (adminMode) {
            return true;
        }
        showAlert(Alert.AlertType.WARNING, "Permission refusée", "Action réservée à l'administrateur: " + actionLabel + ".");
        return false;
    }

    private boolean canManageSelectedPost(String actionLabel) {
        if (isBlank(selectedPostId)) {
            return true;
        }
        Posts selectedPost = findPostById(selectedPostId);
        if (selectedPost == null) {
            return true;
        }
        if (canManagePost(selectedPost)) {
            return true;
        }
        showAlert(Alert.AlertType.WARNING, "Permission refusée", "Vous n'avez pas la permission de " + actionLabel + ".");
        return false;
    }

    private Posts findPostById(String postId) {
        for (Posts post : posts) {
            if (postId.equals(post.getId())) {
                return post;
            }
        }
        return null;
    }

    private boolean canManagePost(Posts post) {
        if (adminMode) {
            return true;
        }
        return currentUserId != null && currentUserId.equals(post.getAuthorId());
    }

    private String resolveAuthorForCreate() {
        String selectedAuthorName = authorNameField.getValue();
        if (isBlank(selectedAuthorName)) {
            return "";
        }

        if (adminMode) {
            String authorId = authorIdByName.get(selectedAuthorName);
            if (!isBlank(authorId)) {
                return authorId;
            }
            if (selectedAuthorName.length() > 2) {
                return selectedAuthorName;
            }
            return "";
        }

        if (!isBlank(currentUserId)) {
            return currentUserId;
        }
        if (!isBlank(currentUserName)) {
            return safe(authorIdByName.get(currentUserName));
        }
        return "";
    }

    private void loadForumCatalog() {
        forums.clear();
        forumNameById.clear();
        forumIdByName.clear();

        forums.addAll(crudForum.getAllForums());
        for (Forum forum : forums) {
            if (isBlank(forum.getId()) || isBlank(forum.getTitle())) {
                continue;
            }
            forumNameById.put(forum.getId(), forum.getTitle());
            forumIdByName.putIfAbsent(forum.getTitle(), forum.getId());
        }

        forumNameField.getItems().setAll(forumIdByName.keySet());
    }

    private void loadAuthorCatalog() {
        users.clear();
        authorNameById.clear();
        authorIdByName.clear();

        users.addAll(crudUser.getAllUsers());
        for (User user : users) {
            if (isBlank(user.getId()) || isBlank(user.getUsername())) {
                continue;
            }
            authorNameById.put(user.getId(), user.getUsername());
            authorIdByName.putIfAbsent(user.getUsername(), user.getId());
        }
        authorNameField.getItems().setAll(authorIdByName.keySet());
    }

    private String resolveForumIdForCreate() {
        String selectedForumName = forumNameField.getValue();
        if (isBlank(selectedForumName)) {
            return "";
        }
        return safe(forumIdByName.get(selectedForumName));
    }

    private String forumDisplayName(Posts post) {
        String name = forumNameForId(post.getForumId());
        if (!isBlank(name)) {
            return name;
        }
        return "Forum inconnu";
    }

    private String authorDisplayName(Posts post) {
        String name = authorNameForId(post.getAuthorId());
        if (!isBlank(name)) {
            return name;
        }
        return "Auteur inconnu";
    }

    private String forumNameForId(String forumId) {
        if (isBlank(forumId)) {
            return "";
        }
        String name = forumNameById.get(forumId);
        if (!isBlank(name)) {
            return name;
        }
        return "";
    }

    private String authorNameForId(String authorId) {
        if (isBlank(authorId)) {
            return "";
        }
        String name = authorNameById.get(authorId);
        if (!isBlank(name)) {
            return name;
        }
        if (authorIdByName.containsKey(authorId)) {
            return authorId;
        }
        return "";
    }

    private String resolveAuthorNameForCurrentUser() {
        if (!isBlank(currentUserName)) {
            return currentUserName;
        }
        if (!isBlank(currentUserId)) {
            return authorNameForId(currentUserId);
        }
        return null;
    }

    private void hideNode(Node node) {
        if (node == null) {
            return;
        }
        node.setVisible(false);
        node.setManaged(false);
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

    private boolean isUserBanned(String authorId) {
        if (isBlank(authorId)) {
            return false;
        }
        Long banUntil = userBanUntil.get(authorId);
        if (banUntil == null) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (now > banUntil) {
            userBanUntil.remove(authorId);
            return false;
        }
        return true;
    }

    private void banUserTemporarily(String authorId) {
        if (isBlank(authorId)) {
            return;
        }
        long banUntil = System.currentTimeMillis() + (BAN_DURATION_MINUTES * 60 * 1000);
        userBanUntil.put(authorId, banUntil);
    }

    private int countConsecutivePostsByAuthor(String authorId) {
        if (isBlank(authorId)) {
            return 0;
        }
        int count = 0;
        for (int i = posts.size() - 1; i >= 0 && count < CONSECUTIVE_POSTS_LIMIT; i--) {
            Posts post = posts.get(i);
            if (authorId.equals(post.getAuthorId())) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    private void recordUserPostTimestamp(String authorId) {
        if (!isBlank(authorId)) {
            userPostTimestamps.put(authorId, System.currentTimeMillis());
        }
    }
}
