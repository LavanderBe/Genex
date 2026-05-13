package Genex.Controllers.Forum;

import Genex.entities.Forum;
import Genex.entities.User;
import Genex.services.CrudForum;
import Genex.services.GroqService;
import Genex.services.NewsService;
import Genex.services.TelegramNotificationService;
import Genex.services.TemperatureService;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ForumController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilterField;
    @FXML
    private ComboBox<String> statusFilterField;
    @FXML
    private ComboBox<String> moderationFilterField;
    @FXML
    private ComboBox<String> sortFilterField;
    @FXML
    private ComboBox<String> densityFilterField;
    @FXML
    private TextField titleField;
    @FXML
    private TextField createdByField;
    @FXML
    private ComboBox<String> categoryField;
    @FXML
    private ComboBox<String> topicStatusField;
    @FXML
    private CheckBox pinnedField;
    @FXML
    private TextArea descriptionArea;
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
    private FlowPane forumCardsContainer;
    @FXML
    private Label forumTotalCountLabel;
    @FXML
    private Label forumShownCountLabel;
    @FXML
    private Label forumAuthorsCountLabel;
    @FXML
    private VBox forumEmptyStateBox;
    @FXML
    private Label forumEmptyStateLabel;
    @FXML
    private Button forumEmptyCreateButton;
    @FXML
    private VBox forumActionsSidebarCard;
    @FXML
    private Button newForumButton;
    @FXML
    private Button addForumButton;
    @FXML
    private Button updateForumButton;
    @FXML
    private Button deleteForumButton;
    @FXML
    private Button pinForumButton;
    @FXML
    private Button resolveForumButton;
    @FXML
    private Button reportForumButton;
    @FXML
    private Button hideForumButton;
    @FXML
    private Button restoreForumButton;
    @FXML
    private Button generateDescButton;
    @FXML
    private VBox forumManagementCard;

    private final CrudForum crudForum = new CrudForum();
    private final TemperatureService temperatureService = new TemperatureService();
    private final NewsService newsService = new NewsService();
    private final GroqService groqService = new GroqService();
    private final TelegramNotificationService telegramNotificationService = new TelegramNotificationService();
    private final List<Forum> forums = new ArrayList<>();
    private final Map<String, String> forumStatusById = new HashMap<>();
    private boolean adminMode;
    private String selectedForumId;
    
    // Limites de caractères
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 10000;
    private static final int MAX_CREATED_BY_LENGTH = 100;

    @FXML
    public void initialize() {
        resolveCurrentRole();
        setupCombos();
        setupListeners();
        loadForums();
        clearFormFields();
        applyRolePermissions();
        loadHeaderInfo();
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
    private void handleClearForm(ActionEvent event) {
        clearFormFields();
    }

    @FXML
    private void handleRefreshHeaderData(ActionEvent event) {
        loadHeaderInfo();
    }

    @FXML
    private void handleAddForum(ActionEvent event) {
        if (!requireAdminAction("créer un forum")) {
            return;
        }
        if (!validateForumForm()) {
            return;
        }
        try {
            Forum forum = new Forum(
                    titleField.getText().trim(),
                    descriptionArea.getText() == null ? "" : descriptionArea.getText().trim(),
                    createdByField.getText().trim()
            );
            forum.setCreatedAt(LocalDateTime.now());
            crudForum.addEntity(forum);
            String telegramStatus = notifyForumCreationOnTelegram(forum);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Forum créé avec succès." + telegramStatus);
            clearFormFields();
            loadForums();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer le forum: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateDescription(ActionEvent event) {
        if (titleField.getText() == null || titleField.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le titre du forum est requis pour générer une description.");
            return;
        }
        
        String title = titleField.getText().trim();
        generateDescButton.setDisable(true);
        generateDescButton.setText("⏳ EN COURS...");
        
        CompletableFuture.supplyAsync(() -> {
            System.out.println("[GROQ] Demande de génération pour: " + title);
            return groqService.generateForumDescription(title);
        })
        .thenAccept(generatedDesc -> {
            Platform.runLater(() -> {
                System.out.println("[GROQ] Réponse reçue: " + generatedDesc);
                descriptionArea.setText(generatedDesc);
                generateDescButton.setDisable(false);
                generateDescButton.setText("✨ GÉNÉRER");
                showAlert(Alert.AlertType.INFORMATION, "Succès ✓", "Description générée par IA Groq !");
            });
        })
        .exceptionally(e -> {
            Platform.runLater(() -> {
                System.err.println("[GROQ] Erreur: " + e.getMessage());
                e.printStackTrace();
                generateDescButton.setDisable(false);
                generateDescButton.setText("✨ GÉNÉRER");
                showAlert(Alert.AlertType.ERROR, "Erreur Groq", "Erreur: " + e.getMessage());
            });
            return null;
        });
    }

    @FXML
    private void handleUpdateForum(ActionEvent event) {
        if (!requireAdminAction("modifier un forum")) {
            return;
        }
        if (selectedForumId == null || selectedForumId.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionne un forum à modifier.");
            return;
        }
        if (!validateForumForm()) {
            return;
        }
        try {
            Forum forum = new Forum();
            forum.setTitle(titleField.getText().trim());
            forum.setDescription(descriptionArea.getText() == null ? "" : descriptionArea.getText().trim());
            forum.setCreatedBy(createdByField.getText().trim());
            crudForum.updateEntity(forum, selectedForumId);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Forum modifié avec succès.");
            clearFormFields();
            loadForums();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de modifier le forum: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteForum(ActionEvent event) {
        if (!requireAdminAction("supprimer un forum")) {
            return;
        }
        if (selectedForumId == null || selectedForumId.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionne un forum à supprimer.");
            return;
        }
        try {
            Forum selectedForum = findForumById(selectedForumId);
            Forum forum = new Forum();
            forum.setId(selectedForumId);
            crudForum.deleteEntity(forum);
            String telegramStatus = notifyForumDeletionOnTelegram(selectedForum);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Forum supprimé avec succès." + telegramStatus);
            clearFormFields();
            loadForums();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le forum: " + e.getMessage());
        }
    }

    @FXML
    private void handleTogglePin(ActionEvent event) {
        if (!requireAdminAction("épingler un forum")) {
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Info", "Le statut Pin est visuel pour le moment.");
    }

    @FXML
    private void handleMarkResolved(ActionEvent event) {
        if (!requireAdminAction("marquer un forum résolu")) {
            return;
        }
        topicStatusField.setValue("RÉSOLU");
        if (!isBlank(selectedForumId)) {
            forumStatusById.put(selectedForumId, "RÉSOLU");
            refreshForumCards();
        }
        showAlert(Alert.AlertType.INFORMATION, "Info", "Statut local réglé sur RÉSOLU.");
    }

    @FXML
    private void handleReportForum(ActionEvent event) {
        moderationFilterField.setValue("SIGNALÉ");
        showAlert(Alert.AlertType.INFORMATION, "Info", "Signalement local appliqué.");
    }

    @FXML
    private void handleHideForum(ActionEvent event) {
        if (!requireAdminAction("masquer un forum")) {
            return;
        }
        moderationFilterField.setValue("MASQUÉ");
        showAlert(Alert.AlertType.INFORMATION, "Info", "Masquage local appliqué.");
    }

    @FXML
    private void handleRestoreForum(ActionEvent event) {
        if (!requireAdminAction("restaurer un forum")) {
            return;
        }
        moderationFilterField.setValue("TOUS");
        showAlert(Alert.AlertType.INFORMATION, "Info", "Restauration locale appliquée.");
    }

    private void setupCombos() {
        categoryField.getItems().setAll("GÉNÉRAL", "SUPPORT", "STRATÉGIE", "ACTUALITÉS");
        topicStatusField.getItems().setAll("OUVERT", "RÉSOLU", "ARCHIVÉ");
        categoryFilterField.getItems().setAll("TOUS", "GÉNÉRAL", "SUPPORT", "STRATÉGIE", "ACTUALITÉS");
        statusFilterField.getItems().setAll("TOUS", "OUVERT", "RÉSOLU", "ARCHIVÉ");
        moderationFilterField.getItems().setAll("TOUS", "VISIBLE", "MASQUÉ", "SIGNALÉ");
        sortFilterField.getItems().setAll("PLUS RÉCENT", "A-Z", "PLUS ACTIF");
        densityFilterField.getItems().setAll("CONFORT", "COMPACT");

        categoryField.setValue("GÉNÉRAL");
        topicStatusField.setValue("OUVERT");
        categoryFilterField.setValue("TOUS");
        statusFilterField.setValue("TOUS");
        moderationFilterField.setValue("TOUS");
        sortFilterField.setValue("PLUS RÉCENT");
        densityFilterField.setValue("CONFORT");
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
        searchField.textProperty().addListener((obs, oldV, newV) -> refreshForumCards());
        categoryFilterField.valueProperty().addListener((obs, oldV, newV) -> refreshForumCards());
        statusFilterField.valueProperty().addListener((obs, oldV, newV) -> refreshForumCards());
        moderationFilterField.valueProperty().addListener((obs, oldV, newV) -> refreshForumCards());
        sortFilterField.valueProperty().addListener((obs, oldV, newV) -> refreshForumCards());
        densityFilterField.valueProperty().addListener((obs, oldV, newV) -> refreshForumCards());
    }

    private void loadForums() {
        forums.clear();
        forums.addAll(crudForum.getAllForums());
        for (Forum forum : forums) {
            if (!isBlank(forum.getId())) {
                forumStatusById.putIfAbsent(forum.getId(), "OUVERT");
            }
        }
        forumStatusById.keySet().removeIf(id -> forums.stream().noneMatch(forum -> id.equals(forum.getId())));
        refreshForumCards();
    }

    private void refreshForumCards() {
        forumCardsContainer.getChildren().clear();
        List<Forum> filtered = forums.stream()
                .filter(this::matchesFilters)
                .sorted(forumComparator())
                .toList();

        for (Forum forum : filtered) {
            forumCardsContainer.getChildren().add(createForumCard(forum));
        }
        updateFeatured(filtered);
        updateSidebarStats(filtered);
        updateEmptyState(filtered);
    }

    private boolean matchesFilters(Forum forum) {
        String search = normalize(searchField.getText());
        if (!search.isBlank()) {
            String haystack = normalize(forum.getTitle()) + " " + normalize(forum.getDescription()) + " " + normalize(forum.getCreatedBy());
            if (!haystack.contains(search)) {
                return false;
            }
        }
        String category = valueOrDefault(categoryFilterField.getValue(), "TOUS");
        if (!"TOUS".equals(category)) {
            String haystack = normalize(forum.getTitle()) + " " + normalize(forum.getDescription());
            if (!haystack.contains(category.toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        String status = valueOrDefault(statusFilterField.getValue(), "TOUS");
        if (!"TOUS".equals(status) && !status.equals(statusLabel(forum))) {
            return false;
        }
        return true;
    }

    private VBox createForumCard(Forum forum) {
        boolean compactMode = "COMPACT".equals(valueOrDefault(densityFilterField.getValue(), "CONFORT"));
        VBox card = new VBox(compactMode ? 6 : 8);
        card.getStyleClass().add("forum-card");
        if (compactMode) {
            card.getStyleClass().add("forum-card-compact");
        }
        if (!isBlank(selectedForumId) && selectedForumId.equals(forum.getId())) {
            card.getStyleClass().add("forum-card-selected");
        }
        card.setPrefWidth(compactMode ? 285 : 320);
        card.setPadding(new Insets(compactMode ? 13 : 16));
        card.setOnMouseClicked(e -> selectForum(forum));

        Label title = new Label(forum.getTitle());
        title.getStyleClass().add("forum-card-title");
        title.getStyleClass().add("forum-card-title-main");
        title.setWrapText(true);

        Label desc = new Label(forum.getDescription() == null || forum.getDescription().isBlank() ? "Aucune description." : forum.getDescription());
        desc.getStyleClass().add("forum-card-desc");
        desc.setWrapText(true);
        desc.setMaxWidth(compactMode ? 250 : 295);

        Label meta = new Label("Par " + safe(forum.getCreatedBy()) + " • ID " + safe(forum.getId()));
        meta.getStyleClass().addAll("forum-card-meta", "forum-card-meta-muted");
        
        HBox badges = new HBox(8,
                createBadge(statusLabel(forum), "status-badge"),
                createBadge("ACTIF", "moderation-badge"));

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().addAll("action-button", "secondary-button");
        editBtn.setOnAction(e -> {
            e.consume();
            selectForum(forum);
        });

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().addAll("action-button", "danger-button");
        deleteBtn.setOnAction(e -> {
            e.consume();
            selectedForumId = forum.getId();
            handleDeleteForum(new ActionEvent());
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(8);
        if (adminMode) {
            actions.getChildren().addAll(editBtn, spacer, deleteBtn);
        } else {
            Label readOnlyBadge = new Label("Lecture seule");
            readOnlyBadge.getStyleClass().add("forum-card-meta");
            actions.getChildren().add(readOnlyBadge);
        }
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(title, badges, desc, meta, actions);
        return card;
    }

    @FXML
    private void handleSelectFirstForum(ActionEvent event) {
        List<Forum> filtered = forums.stream().filter(this::matchesFilters).sorted(forumComparator()).toList();
        if (!filtered.isEmpty()) {
            selectForum(filtered.get(0));
        }
    }

    @FXML
    private void handleResetForumFilters(ActionEvent event) {
        searchField.clear();
        categoryFilterField.setValue("TOUS");
        statusFilterField.setValue("TOUS");
        moderationFilterField.setValue("TOUS");
        sortFilterField.setValue("PLUS RÉCENT");
        densityFilterField.setValue("CONFORT");
    }

    private void selectForum(Forum forum) {
        selectedForumId = forum.getId();
        titleField.setText(safe(forum.getTitle()));
        descriptionArea.setText(safe(forum.getDescription()));
        createdByField.setText(safe(forum.getCreatedBy()));
        refreshForumCards();
    }

    private void updateFeatured(List<Forum> filtered) {
        if (filtered.isEmpty()) {
            featuredTitleLabel.setText("Aucun forum pour le moment");
            featuredMoodLabel.setText("🔥 ACTIF");
            featuredTrendLabel.setText("TENDANCE 0");
            featuredMetaLabel.setText("Créé par -");
            featuredDescLabel.setText("Créez votre premier forum pour le voir mis en avant ici.");
            return;
        }
        Forum featured = filtered.get(0);
        featuredTitleLabel.setText(safe(featured.getTitle()));
        featuredMoodLabel.setText("🔥 ACTIF");
        featuredTrendLabel.setText("TENDANCE " + filtered.size());
        featuredMetaLabel.setText("Créé par " + safe(featured.getCreatedBy()));
        featuredDescLabel.setText(safe(featured.getDescription()).isBlank() ? "Aucune description." : safe(featured.getDescription()));
    }

    private void updateSidebarStats(List<Forum> filtered) {
        forumTotalCountLabel.setText("Forums: " + forums.size());
        forumShownCountLabel.setText("Affichés: " + filtered.size());
        long uniqueAuthors = filtered.stream()
                .map(Forum::getCreatedBy)
                .map(this::safe)
                .filter(name -> !name.isBlank())
                .distinct()
                .count();
        forumAuthorsCountLabel.setText("Auteurs uniques: " + uniqueAuthors);
    }

    private void updateEmptyState(List<Forum> filtered) {
        boolean visible = filtered.isEmpty();
        forumEmptyStateBox.setVisible(visible);
        forumEmptyStateBox.setManaged(visible);
        if (!visible) {
            return;
        }
        if (forums.isEmpty()) {
            forumEmptyStateLabel.setText("Aucun forum disponible. Créez un sujet pour démarrer.");
            return;
        }
        forumEmptyStateLabel.setText("Aucun résultat avec ces filtres. Réinitialisez pour retrouver les discussions.");
    }

    private Comparator<Forum> forumComparator() {
        String sortMode = valueOrDefault(sortFilterField.getValue(), "PLUS RÉCENT");
        return switch (sortMode) {
            case "A-Z" -> Comparator.comparing(forum -> normalize(forum.getTitle()));
            case "PLUS ACTIF" -> Comparator
                    .comparingInt((Forum forum) -> statusPriority(statusLabel(forum)))
                    .thenComparing((Forum forum) -> forum.getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder()));
            default -> Comparator.comparing((Forum forum) -> forum.getCreatedAt(), Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    private int statusPriority(String status) {
        return switch (safe(status).toUpperCase(Locale.ROOT)) {
            case "OUVERT" -> 3;
            case "RÉSOLU" -> 2;
            default -> 1;
        };
    }

    private String statusLabel(Forum forum) {
        if (forum == null || isBlank(forum.getId())) {
            return "OUVERT";
        }
        return forumStatusById.getOrDefault(forum.getId(), "OUVERT");
    }

    private Label createBadge(String text, String variantClass) {
        Label badge = new Label(safe(text).isBlank() ? "-" : text);
        badge.getStyleClass().addAll("card-badge", variantClass);
        return badge;
    }

    private boolean validateForumForm() {
        if (isBlank(titleField.getText())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le titre est obligatoire.");
            return false;
        }
        if (titleField.getText().length() > MAX_TITLE_LENGTH) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le titre ne doit pas dépasser " + MAX_TITLE_LENGTH + " caractères.");
            return false;
        }
        if (isBlank(createdByField.getText())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le champ « Créé par » est obligatoire.");
            return false;
        }
        if (createdByField.getText().length() > MAX_CREATED_BY_LENGTH) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le champ « Créé par » ne doit pas dépasser " + MAX_CREATED_BY_LENGTH + " caractères.");
            return false;
        }
        if (!isBlank(descriptionArea.getText()) && descriptionArea.getText().length() > MAX_DESCRIPTION_LENGTH) {
            showAlert(Alert.AlertType.WARNING, "Validation", "La description ne doit pas dépasser " + MAX_DESCRIPTION_LENGTH + " caractères.");
            return false;
        }
        return true;
    }

    private void clearFormFields() {
        selectedForumId = null;
        titleField.clear();
        createdByField.clear();
        descriptionArea.clear();
        categoryField.setValue("GÉNÉRAL");
        topicStatusField.setValue("OUVERT");
        pinnedField.setSelected(false);
        if (forumCardsContainer != null) {
            refreshForumCards();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String notifyForumCreationOnTelegram(Forum forum) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        try {
            telegramNotificationService.sendForumCreatedNotification(forum, currentUser);
            return "\nNotification Telegram envoyée.";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "\nForum créé, mais la notification Telegram a été interrompue.";
        } catch (IOException | IllegalStateException e) {
            return "\nForum créé, mais notification Telegram non envoyée: " + e.getMessage();
        }
    }

    private String notifyForumDeletionOnTelegram(Forum forum) {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        try {
            telegramNotificationService.sendForumDeletedNotification(forum == null ? new Forum() : forum, currentUser);
            return "\nNotification Telegram envoyée.";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "\nForum supprimé, mais la notification Telegram a été interrompue.";
        } catch (IOException | IllegalStateException e) {
            return "\nForum supprimé, mais notification Telegram non envoyée: " + e.getMessage();
        }
    }

    private Forum findForumById(String forumId) {
        for (Forum forum : forums) {
            if (forumId.equals(forum.getId())) {
                return forum;
            }
        }
        return null;
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
    }

    private void applyRolePermissions() {
        if (adminMode) {
            return;
        }
        hideNode(addForumButton);
        hideNode(updateForumButton);
        hideNode(deleteForumButton);
        hideNode(newForumButton);
        hideNode(forumManagementCard);
        hideNode(pinForumButton);
        hideNode(resolveForumButton);
        hideNode(reportForumButton);
        hideNode(hideForumButton);
        hideNode(restoreForumButton);
        hideNode(forumActionsSidebarCard);
        hideNode(forumEmptyCreateButton);
        titleField.setEditable(false);
        createdByField.setEditable(false);
        descriptionArea.setEditable(false);
        categoryField.setDisable(true);
        topicStatusField.setDisable(true);
        pinnedField.setDisable(true);
    }

    private boolean requireAdminAction(String actionLabel) {
        if (adminMode) {
            return true;
        }
        showAlert(Alert.AlertType.WARNING, "Permission refusée", "Action réservée à l'administrateur: " + actionLabel + ".");
        return false;
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
}
