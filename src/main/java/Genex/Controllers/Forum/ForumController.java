package Genex.Controllers.Forum;

import Genex.entities.Forum;
import Genex.services.CrudForum;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
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
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ForumController {
    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField titleField;
    @FXML
    private TextField createdByField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<String> categoryField;
    @FXML
    private ComboBox<String> topicStatusField;
    @FXML
    private CheckBox pinnedField;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> categoryFilterField;
    @FXML
    private ComboBox<String> statusFilterField;
    @FXML
    private ComboBox<String> moderationFilterField;
    @FXML
    private FlowPane forumCardsContainer;
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

    private final CrudForum crudForum = new CrudForum();
    private List<Forum> forums = new ArrayList<>();
    private Forum selectedForum;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int TITLE_MIN = 5;
    private static final int TITLE_MAX = 120;
    private static final int DESCRIPTION_MIN = 10;
    private static final int DESCRIPTION_MAX = 1200;
    private static final int AUTHOR_MIN = 3;
    private static final int AUTHOR_MAX = 60;
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("^[\\p{L}\\p{N} .'-]+$");

    @FXML
    public void initialize() {
        initializeChoiceBoxes();
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        categoryFilterField.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        statusFilterField.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        moderationFilterField.valueProperty().addListener((obs, oldValue, newValue) -> applyFilter());

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventHandler(KeyEvent.KEY_PRESSED, this::handleShortcuts);
            }
        });

        refreshForums();
    }

    private void initializeChoiceBoxes() {
        categoryField.setItems(FXCollections.observableArrayList("General", "Annonce", "Aide", "Feedback", "Off-topic"));
        topicStatusField.setItems(FXCollections.observableArrayList("open", "resolved"));
        categoryFilterField.setItems(FXCollections.observableArrayList("All", "General", "Annonce", "Aide", "Feedback", "Off-topic"));
        statusFilterField.setItems(FXCollections.observableArrayList("All", "open", "resolved"));
        moderationFilterField.setItems(FXCollections.observableArrayList("All", "visible", "reported", "hidden"));

        categoryField.setValue("General");
        topicStatusField.setValue("open");
        categoryFilterField.setValue("All");
        statusFilterField.setValue("All");
        moderationFilterField.setValue("All");
    }

    @FXML
    private void handleAddForum() {
        ValidationResult validation = validateForm();
        if (!validation.valid()) {
            showAlert(Alert.AlertType.WARNING, "Saisie invalide", validation.message());
            return;
        }

        try {
            Forum forum = new Forum(validation.title(), validation.description(), validation.createdBy());
            forum.setCategory(validation.category());
            forum.setTopicStatus(validation.topicStatus());
            forum.setPinned(validation.pinned());
            forum.setModerationStatus("visible");
            crudForum.addEntity(forum);
            refreshForums();
            handleClearForm();
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de l'ajout", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateForum() {
        if (selectedForum == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un forum à modifier.");
            return;
        }

        ValidationResult validation = validateForm();
        if (!validation.valid()) {
            showAlert(Alert.AlertType.WARNING, "Saisie invalide", validation.message());
            return;
        }

        try {
            Forum updatedForum = new Forum();
            updatedForum.setTitle(validation.title());
            updatedForum.setDescription(validation.description());
            updatedForum.setCreatedBy(validation.createdBy());
            updatedForum.setCategory(validation.category());
            updatedForum.setTopicStatus(validation.topicStatus());
            updatedForum.setPinned(validation.pinned());
            updatedForum.setModerationStatus(defaultString(selectedForum.getModerationStatus(), "visible"));
            crudForum.updateEntity(updatedForum, selectedForum.getId());
            refreshForums();
            handleClearForm();
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la modification", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteForum() {
        if (selectedForum == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un forum à supprimer.");
            return;
        }

        try {
            crudForum.deleteEntity(selectedForum);
            refreshForums();
            handleClearForm();
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur lors de la suppression", e.getMessage());
        }
    }

    @FXML
    private void handleTogglePin() {
        if (selectedForum == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un forum.");
            return;
        }
        selectedForum.setPinned(!selectedForum.isPinned());
        persistSelectedForum("Mise à jour pin impossible.");
    }

    @FXML
    private void handleMarkResolved() {
        if (selectedForum == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un forum.");
            return;
        }
        selectedForum.setTopicStatus("resolved");
        persistSelectedForum("Mise à jour du statut impossible.");
    }

    @FXML
    private void handleReportForum() {
        if (selectedForum == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un forum.");
            return;
        }
        selectedForum.setModerationStatus("reported");
        persistSelectedForum("Signalement impossible.");
    }

    @FXML
    private void handleHideForum() {
        if (selectedForum == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un forum.");
            return;
        }
        selectedForum.setModerationStatus("hidden");
        persistSelectedForum("Masquage impossible.");
    }

    @FXML
    private void handleRestoreForum() {
        if (selectedForum == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionnez un forum.");
            return;
        }
        selectedForum.setModerationStatus("visible");
        persistSelectedForum("Restauration impossible.");
    }

    @FXML
    public void handleClearForm() {
        titleField.clear();
        descriptionArea.clear();
        createdByField.clear();
        categoryField.setValue("General");
        topicStatusField.setValue("open");
        pinnedField.setSelected(false);
        selectedForum = null;
    }

    private void refreshForums() {
        try {
            forums = crudForum.getAllForums();
            applyFilter();
        } catch (IllegalStateException e) {
            forums = new ArrayList<>();
            renderCards(forums);
            updateFeaturedCard(forums);
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", e.getMessage());
        }
    }

    private void applyFilter() {
        String query = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        String categoryFilter = defaultString(categoryFilterField.getValue(), "All");
        String statusFilter = defaultString(statusFilterField.getValue(), "All");
        String moderationFilter = defaultString(moderationFilterField.getValue(), "All");

        List<Forum> filtered = forums.stream()
                .filter(forum -> query.isEmpty()
                        || containsIgnoreCase(forum.getTitle(), query)
                        || containsIgnoreCase(forum.getDescription(), query)
                        || containsIgnoreCase(forum.getCreatedBy(), query))
                .filter(forum -> "All".equalsIgnoreCase(categoryFilter)
                        || defaultString(forum.getCategory(), "General").equalsIgnoreCase(categoryFilter))
                .filter(forum -> "All".equalsIgnoreCase(statusFilter)
                        || defaultString(forum.getTopicStatus(), "open").equalsIgnoreCase(statusFilter))
                .filter(forum -> "All".equalsIgnoreCase(moderationFilter)
                        || defaultString(forum.getModerationStatus(), "visible").equalsIgnoreCase(moderationFilter))
                .sorted(Comparator
                        .comparing(Forum::isPinned).reversed()
                        .thenComparing((Forum f) -> "reported".equalsIgnoreCase(defaultString(f.getModerationStatus(), "visible"))).reversed()
                        .thenComparing(Forum::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        renderCards(filtered);
        updateFeaturedCard(filtered);
    }

    private boolean containsIgnoreCase(String text, String query) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(query);
    }

    private void renderCards(List<Forum> items) {
        forumCardsContainer.getChildren().clear();

        if (items.isEmpty()) {
            Label emptyLabel = new Label("Aucun forum trouvé.");
            emptyLabel.getStyleClass().add("table-placeholder");
            forumCardsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Forum forum : items) {
            VBox card = new VBox(8);
            card.setPrefWidth(300);
            card.getStyleClass().add("forum-card");

            Label title = new Label((forum.isPinned() ? "📌 " : "") + defaultString(forum.getTitle(), "Untitled"));
            title.getStyleClass().add("forum-card-title");

            Label description = new Label(truncate(defaultString(forum.getDescription(), ""), 120));
            description.setWrapText(true);
            description.getStyleClass().add("forum-card-desc");

            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            Label meta = new Label(buildMetaText(forum));
            meta.getStyleClass().add("forum-card-meta");

            card.getChildren().addAll(title, description, spacer, meta);
            card.setOnMouseClicked(event -> selectForum(forum));
            forumCardsContainer.getChildren().add(card);
        }
    }

    private String buildMetaText(Forum forum) {
        String author = defaultString(forum.getCreatedBy(), "-");
        String created = forum.getCreatedAt() == null ? "-" : DATE_FORMATTER.format(forum.getCreatedAt());
        String category = defaultString(forum.getCategory(), "General");
        String topicStatus = defaultString(forum.getTopicStatus(), "open");
        String moderationStatus = defaultString(forum.getModerationStatus(), "visible");
        return category + " • " + topicStatus + " • " + moderationStatus + " • By " + author + " • " + created;
    }

    private void updateFeaturedCard(List<Forum> filteredForums) {
        if (selectedForum != null && filteredForums.stream().anyMatch(f -> f.getId().equals(selectedForum.getId()))) {
            renderFeatured(selectedForum);
            return;
        }

        if (filteredForums.isEmpty()) {
            featuredTitleLabel.setText("No forum yet");
            featuredMoodLabel.setText("🔥 ACTIVE");
            featuredMoodLabel.getStyleClass().setAll("mood-badge", "mood-active");
            featuredTrendLabel.setText("TREND 0");
            featuredMetaLabel.setText("Created by -");
            featuredDescLabel.setText("Create your first forum to see it highlighted here.");
            return;
        }

        renderFeatured(filteredForums.getFirst());
    }

    private void selectForum(Forum forum) {
        selectedForum = forum;
        titleField.setText(defaultString(forum.getTitle(), ""));
        descriptionArea.setText(defaultString(forum.getDescription(), ""));
        createdByField.setText(defaultString(forum.getCreatedBy(), ""));
        categoryField.setValue(defaultString(forum.getCategory(), "General"));
        topicStatusField.setValue(defaultString(forum.getTopicStatus(), "open"));
        pinnedField.setSelected(forum.isPinned());
        renderFeatured(forum);
    }

    private void renderFeatured(Forum forum) {
        String pinPrefix = forum.isPinned() ? "📌 " : "";
        featuredTitleLabel.setText(pinPrefix + defaultString(forum.getTitle(), "Untitled"));
        featuredDescLabel.setText(defaultString(forum.getDescription(), "No description"));
        featuredMetaLabel.setText(
                defaultString(forum.getCategory(), "General")
                        + " • "
                        + defaultString(forum.getTopicStatus(), "open")
                        + " • "
                        + defaultString(forum.getModerationStatus(), "visible")
                        + " • By "
                        + defaultString(forum.getCreatedBy(), "-")
        );

        int trend = Math.min(99, Math.max(1, forum.getDescription() == null ? 1 : forum.getDescription().length() / 12));
        featuredTrendLabel.setText("TREND " + trend);

        String moodClass;
        String moodText;
        if ("reported".equalsIgnoreCase(defaultString(forum.getModerationStatus(), "visible"))) {
            moodClass = "mood-help";
            moodText = "🚩 REPORTED";
        } else if ("resolved".equalsIgnoreCase(defaultString(forum.getTopicStatus(), "open"))) {
            moodClass = "mood-update";
            moodText = "✅ RESOLVED";
        } else if (forum.isPinned()) {
            moodClass = "mood-strategy";
            moodText = "📌 PINNED";
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
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
        String createdBy = createdByField.getText() == null ? "" : createdByField.getText().trim();
        String category = defaultString(categoryField.getValue(), "General");
        String topicStatus = defaultString(topicStatusField.getValue(), "open");
        boolean pinned = pinnedField.isSelected();

        if (title.isBlank() || description.isBlank() || createdBy.isBlank()) {
            return ValidationResult.invalid("Tous les champs sont obligatoires.");
        }
        if (title.length() < TITLE_MIN || title.length() > TITLE_MAX) {
            return ValidationResult.invalid("Le titre doit contenir entre " + TITLE_MIN + " et " + TITLE_MAX + " caractères.");
        }
        if (description.length() < DESCRIPTION_MIN || description.length() > DESCRIPTION_MAX) {
            return ValidationResult.invalid("La description doit contenir entre " + DESCRIPTION_MIN + " et " + DESCRIPTION_MAX + " caractères.");
        }
        if (createdBy.length() < AUTHOR_MIN || createdBy.length() > AUTHOR_MAX) {
            return ValidationResult.invalid("Le champ 'created by' doit contenir entre " + AUTHOR_MIN + " et " + AUTHOR_MAX + " caractères.");
        }
        if (!AUTHOR_PATTERN.matcher(createdBy).matches()) {
            return ValidationResult.invalid("Le champ 'created by' contient des caractères non autorisés.");
        }
        if (category.isBlank() || topicStatus.isBlank()) {
            return ValidationResult.invalid("La catégorie et le statut sont obligatoires.");
        }

        return ValidationResult.valid(title, description, createdBy, category, topicStatus, pinned);
    }

    private void persistSelectedForum(String errorMessage) {
        try {
            Forum updatedForum = new Forum();
            updatedForum.setTitle(defaultString(selectedForum.getTitle(), ""));
            updatedForum.setDescription(defaultString(selectedForum.getDescription(), ""));
            updatedForum.setCreatedBy(defaultString(selectedForum.getCreatedBy(), ""));
            updatedForum.setCategory(defaultString(selectedForum.getCategory(), "General"));
            updatedForum.setTopicStatus(defaultString(selectedForum.getTopicStatus(), "open"));
            updatedForum.setModerationStatus(defaultString(selectedForum.getModerationStatus(), "visible"));
            updatedForum.setPinned(selectedForum.isPinned());
            updatedForum.setCreatedAt(selectedForum.getCreatedAt() == null ? LocalDateTime.now() : selectedForum.getCreatedAt());
            crudForum.updateEntity(updatedForum, selectedForum.getId());
            refreshForums();
            selectForum(findForumById(selectedForum.getId()));
        } catch (IllegalStateException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", defaultString(e.getMessage(), errorMessage));
        }
    }

    private Forum findForumById(String id) {
        return forums.stream()
                .filter(forum -> id != null && id.equals(forum.getId()))
                .findFirst()
                .orElse(selectedForum);
    }

    private void handleShortcuts(KeyEvent event) {
        if (!event.isControlDown()) {
            return;
        }
        if (event.getCode() == KeyCode.S) {
            if (selectedForum == null) {
                handleAddForum();
            } else {
                handleUpdateForum();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.N) {
            handleClearForm();
            event.consume();
        } else if (event.getCode() == KeyCode.P) {
            handleTogglePin();
            event.consume();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void openForum() {
        // Déjà sur la page Forum.
    }

    @FXML
    private void openPosts() {
        switchScene("/Fxml/Forum/Posts.fxml", "GENEX - Posts");
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

    private record ValidationResult(boolean valid, String title, String description, String createdBy,
                                    String category, String topicStatus, boolean pinned, String message) {
        private static ValidationResult valid(String title, String description, String createdBy,
                                              String category, String topicStatus, boolean pinned) {
            return new ValidationResult(true, title, description, createdBy, category, topicStatus, pinned, "");
        }

        private static ValidationResult invalid(String message) {
            return new ValidationResult(false, "", "", "", "", "", false, message);
        }
    }
}
