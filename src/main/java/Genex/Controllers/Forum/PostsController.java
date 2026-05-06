package Genex.Controllers.Forum;

import Genex.entities.Posts;
import Genex.services.CrudPosts;
import Genex.services.NewsService;
import Genex.services.TemperatureService;
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
    private Label weatherLabel;
    @FXML
    private ImageView weatherIconView;
    @FXML
    private HBox newsFeedContainer;
    @FXML
    private FlowPane postCardsContainer;

    private final CrudPosts crudPosts = new CrudPosts();
    private final TemperatureService temperatureService = new TemperatureService();
    private final NewsService newsService = new NewsService();
    private final List<Posts> posts = new ArrayList<>();
    private final Map<String, ModerationState> moderationByPostId = new HashMap<>();
    private final Map<String, ReactionCounter> reactionsByPostId = new HashMap<>();
    private static final Set<String> SPAM_KEYWORDS = Set.of(
        "free", "giveaway", "urgent", "winner", "bitcoin", "promo", "click", "offer"
    );
    private static final Set<String> FILTERED_SPEECH_KEYWORDS = Set.of(
        "idiot", "imbecile", "stupid", "hate", "racist", "violent", "con", "merde"
    );
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
        LAUGH,
        CLAP
    }

    private static final class ReactionCounter {
        private int likes;
        private int hearts;
        private int laughs;
        private int claps;

        private void increment(ReactionType type) {
            switch (type) {
                case LIKE -> likes++;
                case HEART -> hearts++;
                case LAUGH -> laughs++;
                case CLAP -> claps++;
            }
        }

        private String summary() {
            return "Like " + likes + " • Love " + hearts + " • Haha " + laughs + " • Clap " + claps;
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
        setupCombos();
        setupListeners();
        loadPosts();
        clearFormFields();
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
    private void handleClearForm(ActionEvent event) {
        clearFormFields();
    }

    @FXML
    private void handleRefreshHeaderData(ActionEvent event) {
        loadHeaderInfo();
    }

    @FXML
    private void handleAddPost(ActionEvent event) {
        if (!validatePostForm()) {
            return;
        }
        try {
            String rawTitle = titleField.getText().trim();
            String rawBody = bodyArea.getText() == null ? "" : bodyArea.getText().trim();
            boolean hadFilteredSpeech = containsFilteredSpeech(rawTitle) || containsFilteredSpeech(rawBody);
            String cleanTitle = sanitizeSpeech(rawTitle);
            String cleanBody = sanitizeSpeech(rawBody);
            boolean isSpam = isSpamContent(cleanTitle, cleanBody);

            Posts post = new Posts(
                forumIdField.getText().trim(),
                authorIdField.getText().trim(),
                cleanTitle,
                cleanBody
            );
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            crudPosts.addEntity(post);

            if (!isBlank(post.getId())) {
                moderationByPostId.put(post.getId(), isSpam ? ModerationState.SPAM : ModerationState.VISIBLE);
                reactionsByPostId.putIfAbsent(post.getId(), new ReactionCounter());
            }

            StringBuilder message = new StringBuilder("Post créé avec succès.");
            if (isSpam) {
                message.append(" Le post est marqué SPAM.");
            }
            if (hadFilteredSpeech) {
                message.append(" Le contenu sensible a été filtré.");
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
            String cleanTitle = sanitizeSpeech(rawTitle);
            String cleanBody = sanitizeSpeech(rawBody);
            boolean isSpam = isSpamContent(cleanTitle, cleanBody);

            Posts post = new Posts();
            post.setTitle(cleanTitle);
            post.setBody(cleanBody);
            post.setUpdatedAt(LocalDateTime.now());
            crudPosts.updateEntity(post, selectedPostId);

            moderationByPostId.put(selectedPostId, isSpam ? ModerationState.SPAM : ModerationState.VISIBLE);
            reactionsByPostId.putIfAbsent(selectedPostId, new ReactionCounter());

            StringBuilder message = new StringBuilder("Post modifié avec succès.");
            if (isSpam) {
                message.append(" Le post est marqué SPAM.");
            }
            if (hadFilteredSpeech) {
                message.append(" Le contenu sensible a été filtré.");
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
    }

    private void loadHeaderInfo() {
        loadTemperature();
        loadNews();
    }

    private void loadTemperature() {
        weatherLabel.setText("🌡 Chargement météo...");
        weatherIconView.setImage(null);

        CompletableFuture.runAsync(() -> {
            try {
                TemperatureService.TemperatureSnapshot snapshot = temperatureService.getCurrentForTunis();
                Image weatherImage = new Image(temperatureService.iconUrlForCode(snapshot.weatherCode()), true);
                Platform.runLater(() -> {
                    weatherLabel.setText(temperatureService.formatForUi(snapshot));
                    weatherIconView.setImage(weatherImage);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    weatherLabel.setText("🌡 Météo indisponible");
                    weatherIconView.setImage(null);
                });
            } catch (IOException | IllegalStateException e) {
                Platform.runLater(() -> {
                    weatherLabel.setText("🌡 Météo indisponible");
                    weatherIconView.setImage(null);
                });
            }
        });
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

        Label body = new Label(post.getBody() == null || post.getBody().isBlank() ? "Aucun contenu." : post.getBody());
        body.getStyleClass().add("forum-card-desc");
        body.setWrapText(true);
        body.setMaxWidth(390);

        Label meta = new Label("Forum " + safe(post.getForumId()) + " • Auteur " + safe(post.getAuthorId()) + " • ID " + safe(post.getId()));
        meta.getStyleClass().add("forum-card-meta");

        Label moderation = new Label("Modération: " + moderationLabel(post));
        moderation.getStyleClass().add("forum-card-meta");

        Label reactions = new Label("Réactions: " + reactionSummary(post));
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

        Button likeBtn = createReactionButton("Like", post, ReactionType.LIKE);
        Button heartBtn = createReactionButton("Love", post, ReactionType.HEART);
        Button laughBtn = createReactionButton("Haha", post, ReactionType.LAUGH);
        Button clapBtn = createReactionButton("Clap", post, ReactionType.CLAP);

        Button repostXBtn = createRepostButton("X", post, "X");
        Button repostFacebookBtn = createRepostButton("Facebook", post, "FACEBOOK");
        Button repostLinkedInBtn = createRepostButton("LinkedIn", post, "LINKEDIN");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8, editBtn, spacer, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);
        FlowPane reactionActions = new FlowPane(8, 8, likeBtn, heartBtn, laughBtn, clapBtn);
        reactionActions.getStyleClass().add("post-actions-row");
        FlowPane repostActions = new FlowPane(8, 8, repostXBtn, repostFacebookBtn, repostLinkedInBtn);
        repostActions.getStyleClass().add("post-actions-row");

        card.getChildren().addAll(title, body, meta, moderation, reactions, reactionActions, repostActions, actions);
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
            return "Like 0 • Love 0 • Haha 0 • Clap 0";
        }
        return reactionsByPostId.computeIfAbsent(post.getId(), key -> new ReactionCounter()).summary();
    }

    private String moderationLabel(Posts post) {
        if (isBlank(post.getId())) {
            return ModerationState.VISIBLE.label();
        }
        return moderationByPostId.computeIfAbsent(post.getId(), key -> ModerationState.VISIBLE).label();
    }

    private Button createReactionButton(String text, Posts post, ReactionType reactionType) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-button", "neutral-button");
        button.setMinWidth(92);
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

    private Button createRepostButton(String text, Posts post, String network) {
        Button button = new Button(text);
        button.getStyleClass().addAll("action-button", "secondary-button");
        button.setMinWidth(120);
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
        String normalized = normalize(value);
        for (String keyword : FILTERED_SPEECH_KEYWORDS) {
            if (normalized.contains(keyword)) {
                return true;
            }
        }
        return false;
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
