package Genex.Controllers.Sponsors;

import Genex.entities.Sponsor;
import Genex.services.CrudSponsor;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.util.regex.Pattern;

public class Sponsors {

    // ── Table ──────────────────────────────────────────────────────────────
    @FXML private TableView<Sponsor>           sponsorTable;
    @FXML private TableColumn<Sponsor, Void>   colLogo;
    @FXML private TableColumn<Sponsor, String> colName;
    @FXML private TableColumn<Sponsor, String> colIndustry;
    @FXML private TableColumn<Sponsor, String> colType;
    @FXML private TableColumn<Sponsor, String> colEmail;
    @FXML private TableColumn<Sponsor, Void>   colActions;

    // ── Stat labels ────────────────────────────────────────────────────────
    @FXML private Label statTotal;
    @FXML private Label statTournament;
    @FXML private Label statTeam;
    @FXML private Label statCenter;

    // ── Search ─────────────────────────────────────────────────────────────
    @FXML private TextField searchField;

    // ── Drawer ─────────────────────────────────────────────────────────────
    @FXML private StackPane drawerOverlay;

    // ── Form fields ────────────────────────────────────────────────────────
    @FXML private Label     formTitle;
    @FXML private TextField fieldName;
    @FXML private TextField fieldIndustry;
    @FXML private TextField fieldEmail;
    @FXML private TextField fieldWebsite;
    @FXML private TextField fieldLogo;
    @FXML private CheckBox  chkTournament;
    @FXML private CheckBox  chkTeam;
    @FXML private CheckBox  chkCenter;
    @FXML private Button    btnSave;
    @FXML private Label     errName;
    @FXML private Label     errEmail;

    // ── State ──────────────────────────────────────────────────────────────
    private CrudSponsor service;
    private final ObservableList<Sponsor> data = FXCollections.observableArrayList();
    private Sponsor editingTarget = null;

    // Requires the form  xxxx@xxx.xxx  — local part, @, domain label(s), dot, TLD
    private static final Pattern EMAIL_RE =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    // ── Init ───────────────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        try {
            service = new CrudSponsor();
        } catch (Exception e) {
            showAlert("Erreur DB", rootCause(e));
            return;
        }
        setupTable();
        loadData();
        sponsorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        searchField.textProperty().addListener((obs, o, v) -> filterTable(v));
    }

    // ── Table setup ────────────────────────────────────────────────────────
    private void setupTable() {
        // Logo thumbnail
        colLogo.setCellFactory(col -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            { iv.setFitWidth(28); iv.setFitHeight(28); iv.setPreserveRatio(true); }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Sponsor s = getTableView().getItems().get(getIndex());
                String path = s.getLogoUrl();
                if (path != null && !path.isBlank()) {
                    try {
                        File f = new File(path);
                        iv.setImage(f.exists() ? new Image(f.toURI().toString()) : null);
                    } catch (Exception ex) { iv.setImage(null); }
                } else { iv.setImage(null); }
                setGraphic(iv);
            }
        });

        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colIndustry.setCellValueFactory(new PropertyValueFactory<>("industry"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));

        // Type column — coloured badges
        colType.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getSponsorType()));

        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null || type.isBlank()) { setGraphic(null); setText(null); return; }
                HBox box = new HBox(4);
                for (String t : type.split(",")) {
                    Label badge = new Label(t.trim());
                    switch (t.trim().toLowerCase()) {
                        case "tournoi" -> badge.getStyleClass().add("badge-tournament");
                        case "équipe"  -> badge.getStyleClass().add("badge-team");
                        case "centre"  -> badge.getStyleClass().add("badge-center");
                        default        -> badge.getStyleClass().add("badge-tournament");
                    }
                    box.getChildren().add(badge);
                }
                setGraphic(box);
                setText(null);
            }
        });

        addActionColumn();
    }

    // ── Data ───────────────────────────────────────────────────────────────
    private void loadData() {
        try {
            data.setAll(service.getAll());
            sponsorTable.setItems(data);
            updateStats();
        } catch (Exception e) {
            showAlert("Erreur", rootCause(e));
        }
    }

    private void filterTable(String q) {
        if (q == null || q.isBlank()) { sponsorTable.setItems(data); return; }
        String lq = q.toLowerCase();
        sponsorTable.setItems(data.filtered(s ->
                (s.getName() != null && s.getName().toLowerCase().contains(lq)) ||
                (s.getIndustry() != null && s.getIndustry().toLowerCase().contains(lq)) ||
                (s.getContactEmail() != null && s.getContactEmail().toLowerCase().contains(lq)) ||
                (s.getSponsorType() != null && s.getSponsorType().toLowerCase().contains(lq))
        ));
    }

    private void updateStats() {
        statTotal.setText(String.valueOf(data.size()));
        statTournament.setText(String.valueOf(
                data.stream().filter(s -> s.getSponsorType() != null && s.getSponsorType().contains("Tournoi")).count()));
        statTeam.setText(String.valueOf(
                data.stream().filter(s -> s.getSponsorType() != null && s.getSponsorType().contains("Équipe")).count()));
        statCenter.setText(String.valueOf(
                data.stream().filter(s -> s.getSponsorType() != null && s.getSponsorType().contains("Centre")).count()));
    }

    // ── Drawer ─────────────────────────────────────────────────────────────
    @FXML
    private void handleAdd() {
        clearForm();
        openDrawer();
    }

    private void openDrawer() {
        drawerOverlay.setVisible(true);
        drawerOverlay.setManaged(true);
    }

    // ── Logo file picker — uploads from PC ────────────────────────────────
    @FXML
    private void handlePickLogo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir un logo");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images (PNG, JPEG)", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(fieldLogo.getScene().getWindow());
        if (f != null) fieldLogo.setText(f.getAbsolutePath());
    }

    // ── Form save ──────────────────────────────────────────────────────────
    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        try {
            Sponsor s = buildFromForm();
            if (editingTarget == null) {
                service.addEntity(s);
            } else {
                service.updateEntity(s, editingTarget.getId());
            }
            clearForm();
            drawerOverlay.setVisible(false);
            drawerOverlay.setManaged(false);
            loadData();
        } catch (Exception e) {
            showAlert("Erreur", rootCause(e));
        }
    }

    @FXML
    private void handleClear() {
        clearForm();
        drawerOverlay.setVisible(false);
        drawerOverlay.setManaged(false);
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private void populateForm(Sponsor s) {
        editingTarget = s;
        formTitle.setText("Modifier le sponsor");
        fieldName.setText(s.getName());
        fieldIndustry.setText(s.getIndustry() != null ? s.getIndustry() : "");
        fieldEmail.setText(s.getContactEmail() != null ? s.getContactEmail() : "");
        fieldWebsite.setText(s.getWebsiteUrl() != null ? s.getWebsiteUrl() : "");
        fieldLogo.setText(s.getLogoUrl() != null ? s.getLogoUrl() : "");
        String type = s.getSponsorType() != null ? s.getSponsorType() : "";
        chkTournament.setSelected(type.contains("Tournoi"));
        chkTeam.setSelected(type.contains("Équipe"));
        chkCenter.setSelected(type.contains("Centre"));
        btnSave.setText("Mettre à jour");
        openDrawer();
    }

    private void clearForm() {
        editingTarget = null;
        formTitle.setText("Nouveau sponsor");
        fieldName.clear();
        fieldIndustry.clear();
        fieldEmail.clear();
        fieldWebsite.clear();
        fieldLogo.clear();
        chkTournament.setSelected(false);
        chkTeam.setSelected(false);
        chkCenter.setSelected(false);
        btnSave.setText("Enregistrer");
        hideErr(errName);
        hideErr(errEmail);
    }

    private Sponsor buildFromForm() {
        Sponsor s = new Sponsor();
        s.setName(fieldName.getText().trim());
        s.setIndustry(fieldIndustry.getText().trim());
        s.setContactEmail(fieldEmail.getText().trim());
        s.setWebsiteUrl(fieldWebsite.getText().trim());
        s.setLogoUrl(fieldLogo.getText().trim());
        StringBuilder type = new StringBuilder();
        if (chkTournament.isSelected()) { if (type.length() > 0) type.append(", "); type.append("Tournoi"); }
        if (chkTeam.isSelected())       { if (type.length() > 0) type.append(", "); type.append("Équipe"); }
        if (chkCenter.isSelected())     { if (type.length() > 0) type.append(", "); type.append("Centre"); }
        s.setSponsorType(type.toString());
        return s;
    }

    private boolean validateForm() {
        boolean ok = true;
        hideErr(errName);
        hideErr(errEmail);
        if (fieldName.getText().isBlank()) {
            showErr(errName, "Le nom est obligatoire.");
            ok = false;
        }
        String email = fieldEmail.getText().trim();
        if (!email.isBlank() && !EMAIL_RE.matcher(email).matches()) {
            showErr(errEmail, "Format invalide — attendu : xxxx@xxx.xxx");
            ok = false;
        }
        return ok;
    }

    private void addActionColumn() {
        Callback<TableColumn<Sponsor, Void>, TableCell<Sponsor, Void>> factory = col -> new TableCell<>() {
            private final Button editBtn   = new Button("✏");
            private final Button deleteBtn = new Button("🗑");
            private final HBox   box       = new HBox(4, editBtn, deleteBtn);
            {
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");
                editBtn.setOnAction(e -> populateForm(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> {
                    Sponsor s = getTableView().getItems().get(getIndex());
                    Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                            "Supprimer \"" + s.getName() + "\" ?", ButtonType.YES, ButtonType.NO);
                    c.setHeaderText(null);
                    c.showAndWait().ifPresent(b -> {
                        if (b == ButtonType.YES) {
                            try { service.deleteEntity(s); loadData(); }
                            catch (Exception ex) { showAlert("Erreur", rootCause(ex)); }
                        }
                    });
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        };
        colActions.setCellFactory(factory);
    }

    private void showErr(Label l, String msg) { l.setText(msg); l.setVisible(true); l.setManaged(true); }
    private void hideErr(Label l)             { l.setVisible(false); l.setManaged(false); }

    private String rootCause(Throwable e) {
        Throwable c = e; while (c.getCause() != null) c = c.getCause();
        return c.getClass().getSimpleName() + ": " + c.getMessage();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
