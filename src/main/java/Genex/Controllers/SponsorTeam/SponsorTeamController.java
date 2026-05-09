package Genex.Controllers.SponsorTeam;

import Genex.entities.Sponsor;
import Genex.entities.SponsorTeam;
import Genex.entities.SponsorTeam.SponsorMethod;
import Genex.entities.Team;
import Genex.services.CrudSponsor;
import Genex.services.CrudSponsorTeam;
import Genex.services.CrudTeam;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SponsorTeamController {

    // ── Table ──────────────────────────────────────────────────────────────
    @FXML private TableView<SponsorTeam>               table;
    @FXML private TableColumn<SponsorTeam, String>     colSponsor;
    @FXML private TableColumn<SponsorTeam, String>     colTeam;
    @FXML private TableColumn<SponsorTeam, String>     colMethod;
    @FXML private TableColumn<SponsorTeam, String>     colBudget;
    @FXML private TableColumn<SponsorTeam, String>     colStart;
    @FXML private TableColumn<SponsorTeam, String>     colEnd;
    @FXML private TableColumn<SponsorTeam, Void>       colActions;

    // ── Stats ──────────────────────────────────────────────────────────────
    @FXML private Label statTotal;
    @FXML private Label statBudget;

    // ── Search ─────────────────────────────────────────────────────────────
    @FXML private TextField searchField;

    // ── Drawer ─────────────────────────────────────────────────────────────
    @FXML private StackPane drawerOverlay;
    @FXML private Label     formTitle;

    // ── Form fields ────────────────────────────────────────────────────────
    @FXML private ComboBox<Sponsor>      combSponsor;
    @FXML private ComboBox<Team>         combTeam;
    @FXML private ComboBox<SponsorMethod> combMethod;
    @FXML private TextField              fieldBudget;
    @FXML private DatePicker             dateStart;
    @FXML private DatePicker             dateEnd;
    @FXML private TextField              fieldNotes;
    @FXML private Button                 btnSave;
    @FXML private Label                  errSponsor;
    @FXML private Label                  errTeam;
    @FXML private Label                  errBudget;

    // ── State ──────────────────────────────────────────────────────────────
    private CrudSponsorTeam service;
    private CrudSponsor     sponsorService;
    private CrudTeam        teamService;
    private final ObservableList<SponsorTeam> data = FXCollections.observableArrayList();
    private SponsorTeam editingTarget = null;

    @FXML
    public void initialize() {
        try {
            service        = new CrudSponsorTeam();
            sponsorService = new CrudSponsor();
            teamService    = new CrudTeam();
        } catch (Exception e) {
            showAlert("Erreur DB", rootCause(e));
            return;
        }

        setupColumns();
        loadCombos();
        loadData();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        searchField.textProperty().addListener((obs, o, v) -> filterTable(v));
    }

    // ── Table columns ──────────────────────────────────────────────────────
    private void setupColumns() {
        colSponsor.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getSponsorName()));
        colTeam.setCellValueFactory(cd    -> new SimpleStringProperty(cd.getValue().getTeamName()));
        colMethod.setCellValueFactory(cd  -> new SimpleStringProperty(cd.getValue().getMethodLabel()));
        colBudget.setCellValueFactory(cd  -> new SimpleStringProperty(
                cd.getValue().getBudgetAmount() != null
                        ? cd.getValue().getBudgetAmount().toPlainString() + " TND" : "—"));
        colStart.setCellValueFactory(cd   -> new SimpleStringProperty(
                cd.getValue().getStartDate() != null ? cd.getValue().getStartDate().toString() : "—"));
        colEnd.setCellValueFactory(cd     -> new SimpleStringProperty(
                cd.getValue().getEndDate() != null ? cd.getValue().getEndDate().toString() : "—"));
        addActionColumn();
    }

    // ── Data ───────────────────────────────────────────────────────────────
    private void loadData() {
        try {
            data.setAll(service.getAll());
            table.setItems(data);
            updateStats();
        } catch (Exception e) {
            showAlert("Erreur", rootCause(e));
        }
    }

    private void loadCombos() {
        // Sponsors
        try {
            ObservableList<Sponsor> sponsors = FXCollections.observableArrayList(sponsorService.getAll());
            combSponsor.setConverter(new StringConverter<>() {
                @Override public String toString(Sponsor s)   { return s == null ? "" : s.getName(); }
                @Override public Sponsor fromString(String s) { return null; }
            });
            combSponsor.setItems(sponsors);
        } catch (Exception e) { /* DB might not have data yet */ }

        // Teams
        try {
            ObservableList<Team> teams = FXCollections.observableArrayList(teamService.getAll());
            combTeam.setConverter(new StringConverter<>() {
                @Override public String toString(Team t)   { return t == null ? "" : t.getNom_team(); }
                @Override public Team fromString(String s) { return null; }
            });
            combTeam.setItems(teams);
        } catch (Exception e) { /* DB might not have data yet */ }

        // Methods
        combMethod.setItems(FXCollections.observableArrayList(SponsorMethod.values()));
    }

    private void filterTable(String q) {
        if (q == null || q.isBlank()) { table.setItems(data); return; }
        String lq = q.toLowerCase();
        table.setItems(data.filtered(st ->
                st.getSponsorName().toLowerCase().contains(lq) ||
                st.getTeamName().toLowerCase().contains(lq) ||
                st.getMethodLabel().toLowerCase().contains(lq)
        ));
    }

    private void updateStats() {
        statTotal.setText(String.valueOf(data.size()));
        BigDecimal total = data.stream()
                .filter(st -> st.getBudgetAmount() != null)
                .map(SponsorTeam::getBudgetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statBudget.setText(total.toPlainString() + " TND");
    }

    // ── Drawer ─────────────────────────────────────────────────────────────
    @FXML private void handleAdd() { clearForm(); openDrawer(); }

    private void openDrawer() {
        drawerOverlay.setVisible(true);
        drawerOverlay.setManaged(true);
    }

    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        try {
            SponsorTeam st = buildFromForm();
            if (editingTarget == null) service.addEntity(st);
            else                       service.updateEntity(st, editingTarget.getId());
            closeDrawer();
            loadData();
        } catch (Exception e) {
            showAlert("Erreur", rootCause(e));
        }
    }

    @FXML private void handleClear() { closeDrawer(); }

    private void closeDrawer() {
        clearForm();
        drawerOverlay.setVisible(false);
        drawerOverlay.setManaged(false);
    }

    // ── Form helpers ───────────────────────────────────────────────────────
    private void populateForm(SponsorTeam st) {
        editingTarget = st;
        formTitle.setText("Modifier le contrat");
        combSponsor.getItems().stream()
                .filter(s -> s.getId().equals(st.getSponsorId()))
                .findFirst().ifPresent(combSponsor::setValue);
        combTeam.getItems().stream()
                .filter(t -> t.getId_team() == st.getTeamId())
                .findFirst().ifPresent(combTeam::setValue);
        combMethod.setValue(st.getMethod());
        fieldBudget.setText(st.getBudgetAmount() != null ? st.getBudgetAmount().toPlainString() : "");
        dateStart.setValue(st.getStartDate());
        dateEnd.setValue(st.getEndDate());
        fieldNotes.setText(st.getNotes() != null ? st.getNotes() : "");
        btnSave.setText("Mettre à jour");
        openDrawer();
    }

    private void clearForm() {
        editingTarget = null;
        formTitle.setText("Nouveau contrat équipe");
        combSponsor.setValue(null);
        combTeam.setValue(null);
        combMethod.setValue(null);
        fieldBudget.clear();
        dateStart.setValue(null);
        dateEnd.setValue(null);
        fieldNotes.clear();
        btnSave.setText("Enregistrer");
        hideErr(errSponsor); hideErr(errTeam); hideErr(errBudget);
    }

    private SponsorTeam buildFromForm() {
        SponsorTeam st = new SponsorTeam();
        st.setSponsor(combSponsor.getValue());
        st.setTeam(combTeam.getValue());
        st.setMethod(combMethod.getValue());
        String b = fieldBudget.getText().trim();
        st.setBudgetAmount(b.isEmpty() ? BigDecimal.ZERO : new BigDecimal(b));
        st.setStartDate(dateStart.getValue());
        st.setEndDate(dateEnd.getValue());
        st.setNotes(fieldNotes.getText().trim());
        return st;
    }

    private boolean validateForm() {
        boolean ok = true;
        hideErr(errSponsor); hideErr(errTeam); hideErr(errBudget);
        if (combSponsor.getValue() == null) { showErr(errSponsor, "Sponsor obligatoire."); ok = false; }
        if (combTeam.getValue() == null)    { showErr(errTeam,    "Équipe obligatoire.");  ok = false; }
        try { new BigDecimal(fieldBudget.getText().trim()); }
        catch (NumberFormatException e) { showErr(errBudget, "Montant invalide."); ok = false; }
        return ok;
    }

    // ── Action column ──────────────────────────────────────────────────────
    private void addActionColumn() {
        Callback<TableColumn<SponsorTeam, Void>, TableCell<SponsorTeam, Void>> factory = col -> new TableCell<>() {
            private final Button editBtn   = new Button("✏");
            private final Button deleteBtn = new Button("🗑");
            private final HBox   box       = new HBox(4, editBtn, deleteBtn);
            {
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");
                editBtn.setOnAction(e -> populateForm(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> {
                    SponsorTeam st = getTableView().getItems().get(getIndex());
                    Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                            "Supprimer ce contrat ?", ButtonType.YES, ButtonType.NO);
                    c.setHeaderText(null);
                    c.showAndWait().ifPresent(b -> {
                        if (b == ButtonType.YES) {
                            try { service.deleteEntity(st); loadData(); }
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
    private String rootCause(Throwable e)     { Throwable c = e; while (c.getCause() != null) c = c.getCause(); return c.getMessage(); }
    private void showAlert(String t, String m){ Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
}
