package Genex.Controllers.Forum;

import Genex.entities.Forum;
import Genex.services.CrudForum;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ForumController {
    @FXML
    private TextField titleField;
    @FXML
    private TextField createdByField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField searchField;
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
        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter(newValue));
        refreshForums();
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
    public void handleClearForm() {
        titleField.clear();
        descriptionArea.clear();
        createdByField.clear();
        selectedForum = null;
    }

    private void refreshForums() {
        try {
            forums = crudForum.getAllForums();
            applyFilter(searchField.getText());
        } catch (IllegalStateException e) {
            forums = new ArrayList<>();
            renderCards(forums);
            updateFeaturedCard(forums);
            showAlert(Alert.AlertType.ERROR, "Erreur de chargement", e.getMessage());
        }
    }

    private void applyFilter(String searchText) {
        String query = searchText == null ? "" : searchText.trim().toLowerCase(Locale.ROOT);
        List<Forum> filtered = forums.stream()
                .filter(forum -> query.isEmpty() ||
                        containsIgnoreCase(forum.getTitle(), query) ||
                        containsIgnoreCase(forum.getDescription(), query) ||
                        containsIgnoreCase(forum.getCreatedBy(), query))
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

            Label title = new Label(forum.getTitle());
            title.getStyleClass().add("forum-card-title");

            Label description = new Label(truncate(forum.getDescription(), 120));
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
        String author = forum.getCreatedBy() == null ? "-" : forum.getCreatedBy();
        String created = forum.getCreatedAt() == null ? "-" : DATE_FORMATTER.format(forum.getCreatedAt());
        return "By " + author + " • " + created;
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
        titleField.setText(forum.getTitle());
        descriptionArea.setText(forum.getDescription());
        createdByField.setText(forum.getCreatedBy());
        renderFeatured(forum);
    }

    private void renderFeatured(Forum forum) {
        featuredTitleLabel.setText(defaultString(forum.getTitle(), "Untitled"));
        featuredDescLabel.setText(defaultString(forum.getDescription(), "No description"));
        featuredMetaLabel.setText("Created by " + defaultString(forum.getCreatedBy(), "-"));

        int trend = Math.min(99, Math.max(1, forum.getDescription() == null ? 1 : forum.getDescription().length() / 12));
        featuredTrendLabel.setText("TREND " + trend);

        String moodClass;
        String moodText;
        int titleLength = forum.getTitle() == null ? 0 : forum.getTitle().length();
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
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        String description = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
        String createdBy = createdByField.getText() == null ? "" : createdByField.getText().trim();

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

        return ValidationResult.valid(title, description, createdBy);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private record ValidationResult(boolean valid, String title, String description, String createdBy, String message) {
        private static ValidationResult valid(String title, String description, String createdBy) {
            return new ValidationResult(true, title, description, createdBy, "");
        }

        private static ValidationResult invalid(String message) {
            return new ValidationResult(false, "", "", "", message);
        }
    }
}
