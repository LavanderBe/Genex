package Genex.Controllers.Sponsors;

import Genex.entities.Sponsor;
import Genex.services.CrudSponsor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class Sponsors {

    // ── Table ──────────────────────────────────────────────────────────────
    @FXML private TableView<Sponsor> sponsorTable;
    @FXML private TableColumn<Sponsor, String> colName;
    @FXML private TableColumn<Sponsor, String> colIndustry;
    @FXML private TableColumn<Sponsor, String> colEmail;
    @FXML private TableColumn<Sponsor, String> colWebsite;
    @FXML private TableColumn<Sponsor, Void>   colActions;

    // ── Form ───────────────────────────────────────────────────────────────
    @FXML private TextField fieldName;
    @FXML private TextField fieldIndustry;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldWebsite;
    @FXML private TextField fieldLogo;
    @FXML private Button    btnSave;
    @FXML private Button    btnClear;
    @FXML private Label     formTitle;

    // ── Search ─────────────────────────────────────────────────────────────
    @FXML private TextField searchField;

    // ── State ──────────────────────────────────────────────────────────────
    private final CrudSponsor service = new CrudSponsor();
    private final ObservableList<Sponsor> data = FXCollections.observableArrayList();
    private Sponsor editingTarget = null;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colIndustry.setCellValueFactory(new PropertyValueFactory<>("industry"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        colWebsite.setCellValueFactory(new PropertyValueFactory<>("websiteUrl"));
        addActionColumn();
        loadData();

        searchField.textProperty().addListener((obs, old, val) -> filterTable(val));
    }

    // ── Data ───────────────────────────────────────────────────────────────
    private void loadData() {
        data.setAll(service.getAll());
        sponsorTable.setItems(data);
    }

    private void filterTable(String query) {
        if (query == null || query.isBlank()) {
            sponsorTable.setItems(data);
            return;
        }
        String q = query.toLowerCase();
        ObservableList<Sponsor> filtered = data.filtered(s ->
                (s.getName() != null && s.getName().toLowerCase().contains(q)) ||
                (s.getIndustry() != null && s.getIndustry().toLowerCase().contains(q)) ||
                (s.getContactEmail() != null && s.getContactEmail().toLowerCase().contains(q))
        );
        sponsorTable.setItems(filtered);
    }

    // ── Form actions ───────────────────────────────────────────────────────
    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        Sponsor s = buildFromForm();

        if (editingTarget == null) {
            service.addEntity(s);
        } else {
            service.updateEntity(s, editingTarget.getId());
        }

        clearForm();
        loadData();
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    @FXML
    private void goToBudget() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Budget/Budget.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) sponsorTable.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir la gestion des budgets.");
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private void populateForm(Sponsor s) {
        editingTarget = s;
        formTitle.setText("Modifier le sponsor");
        fieldName.setText(s.getName());
        fieldIndustry.setText(s.getIndustry());
        fieldEmail.setText(s.getContactEmail());
        fieldWebsite.setText(s.getWebsiteUrl());
        fieldLogo.setText(s.getLogoUrl());
        btnSave.setText("Mettre à jour");
    }

    private void clearForm() {
        editingTarget = null;
        formTitle.setText("Nouveau sponsor");
        fieldName.clear();
        fieldIndustry.clear();
        fieldEmail.clear();
        fieldWebsite.clear();
        fieldLogo.clear();
        btnSave.setText("Enregistrer");
    }

    private Sponsor buildFromForm() {
        Sponsor s = new Sponsor();
        s.setName(fieldName.getText().trim());
        s.setIndustry(fieldIndustry.getText().trim());
        s.setContactEmail(fieldEmail.getText().trim());
        s.setWebsiteUrl(fieldWebsite.getText().trim());
        s.setLogoUrl(fieldLogo.getText().trim());
        return s;
    }

    private boolean validateForm() {
        if (fieldName.getText().isBlank()) {
            showAlert("Validation", "Le nom du sponsor est obligatoire.");
            return false;
        }
        return true;
    }

    private void addActionColumn() {
        Callback<TableColumn<Sponsor, Void>, TableCell<Sponsor, Void>> factory = col -> new TableCell<>() {
            private final Button editBtn   = new Button("✏");
            private final Button deleteBtn = new Button("🗑");
            private final HBox   box       = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");

                editBtn.setOnAction(e -> {
                    Sponsor s = getTableView().getItems().get(getIndex());
                    populateForm(s);
                });

                deleteBtn.setOnAction(e -> {
                    Sponsor s = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Supprimer \"" + s.getName() + "\" ?", ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.YES) {
                            service.deleteEntity(s);
                            loadData();
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        };
        colActions.setCellFactory(factory);
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
