package Genex.Controllers.Forum;

import Genex.entities.Forum;
import Genex.services.CrudForum;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private FlowPane forumCardsContainer;

    private final CrudForum crudForum = new CrudForum();
    private final List<Forum> forums = new ArrayList<>();
    private String selectedForumId;

    @FXML
    public void initialize() {
        setupCombos();
        setupListeners();
        loadForums();
        clearFormFields();
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
    private void handleAddForum(ActionEvent event) {
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
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Forum créé avec succès.");
            clearFormFields();
            loadForums();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer le forum: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdateForum(ActionEvent event) {
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
        if (selectedForumId == null || selectedForumId.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Sélectionne un forum à supprimer.");
            return;
        }
        try {
            Forum forum = new Forum();
            forum.setId(selectedForumId);
            crudForum.deleteEntity(forum);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Forum supprimé avec succès.");
            clearFormFields();
            loadForums();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de supprimer le forum: " + e.getMessage());
        }
    }

    @FXML
    private void handleTogglePin(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Info", "Le statut Pin est visuel pour le moment.");
    }

    @FXML
    private void handleMarkResolved(ActionEvent event) {
        topicStatusField.setValue("RÉSOLU");
        showAlert(Alert.AlertType.INFORMATION, "Info", "Statut local réglé sur RÉSOLU.");
    }

    @FXML
    private void handleReportForum(ActionEvent event) {
        moderationFilterField.setValue("SIGNALÉ");
        showAlert(Alert.AlertType.INFORMATION, "Info", "Signalement local appliqué.");
    }

    @FXML
    private void handleHideForum(ActionEvent event) {
        moderationFilterField.setValue("MASQUÉ");
        showAlert(Alert.AlertType.INFORMATION, "Info", "Masquage local appliqué.");
    }

    @FXML
    private void handleRestoreForum(ActionEvent event) {
        moderationFilterField.setValue("TOUS");
        showAlert(Alert.AlertType.INFORMATION, "Info", "Restauration locale appliquée.");
    }

    private void setupCombos() {
        categoryField.getItems().setAll("GÉNÉRAL", "SUPPORT", "STRATÉGIE", "ACTUALITÉS");
        topicStatusField.getItems().setAll("OUVERT", "RÉSOLU", "ARCHIVÉ");
        categoryFilterField.getItems().setAll("TOUS", "GÉNÉRAL", "SUPPORT", "STRATÉGIE", "ACTUALITÉS");
        statusFilterField.getItems().setAll("TOUS", "OUVERT", "RÉSOLU", "ARCHIVÉ");
        moderationFilterField.getItems().setAll("TOUS", "VISIBLE", "MASQUÉ", "SIGNALÉ");

        categoryField.setValue("GÉNÉRAL");
        topicStatusField.setValue("OUVERT");
        categoryFilterField.setValue("TOUS");
        statusFilterField.setValue("TOUS");
        moderationFilterField.setValue("TOUS");
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, oldV, newV) -> refreshForumCards());
        categoryFilterField.valueProperty().addListener((obs, oldV, newV) -> refreshForumCards());
        statusFilterField.valueProperty().addListener((obs, oldV, newV) -> refreshForumCards());
        moderationFilterField.valueProperty().addListener((obs, oldV, newV) -> refreshForumCards());
    }

    private void loadForums() {
        forums.clear();
        forums.addAll(crudForum.getAllForums());
        refreshForumCards();
    }

    private void refreshForumCards() {
        forumCardsContainer.getChildren().clear();
        List<Forum> filtered = forums.stream().filter(this::matchesFilters).toList();

        for (Forum forum : filtered) {
            forumCardsContainer.getChildren().add(createForumCard(forum));
        }
        updateFeatured(filtered);
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
        return true;
    }

    private VBox createForumCard(Forum forum) {
        VBox card = new VBox(8);
        card.getStyleClass().add("forum-card");
        card.setPrefWidth(305);
        card.setPadding(new Insets(16));
        card.setOnMouseClicked(e -> selectForum(forum));

        Label title = new Label(forum.getTitle());
        title.getStyleClass().add("forum-card-title");
        title.setWrapText(true);

        Label desc = new Label(forum.getDescription() == null || forum.getDescription().isBlank() ? "Aucune description." : forum.getDescription());
        desc.getStyleClass().add("forum-card-desc");
        desc.setWrapText(true);
        desc.setMaxWidth(280);

        Label meta = new Label("Par " + safe(forum.getCreatedBy()) + " • ID " + safe(forum.getId()));
        meta.getStyleClass().add("forum-card-meta");

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

        HBox actions = new HBox(8, editBtn, spacer, deleteBtn);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(title, desc, meta, actions);
        return card;
    }

    private void selectForum(Forum forum) {
        selectedForumId = forum.getId();
        titleField.setText(safe(forum.getTitle()));
        descriptionArea.setText(safe(forum.getDescription()));
        createdByField.setText(safe(forum.getCreatedBy()));
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

    private boolean validateForumForm() {
        if (isBlank(titleField.getText())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le titre est obligatoire.");
            return false;
        }
        if (isBlank(createdByField.getText())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Le champ « Créé par » est obligatoire.");
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
