package Genex.Controllers.SponsorTournament;

import Genex.entities.Sponsor;
import Genex.entities.SponsorTournament;
import Genex.entities.SponsorTournament.SponsorMethod;
import Genex.entities.Tounament;
import Genex.services.CrudSponsor;
import Genex.services.CrudSponsorTournament;
import Genex.services.CrudTournament;
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

public class SponsorTournamentController {

    // ── Table ──────────────────────────────────────────────────────────────
    @FXML private TableView<SponsorTournament>               table;
    @FXML private TableColumn<SponsorTournament, String>     colSponsor;
    @FXML private TableColumn<SponsorTournament, String>     colTournament;
    @FXML private TableColumn<SponsorTournament, String>     colMethod;
    @FXML private TableColumn<SponsorTournament, String>     colBudget;
    @FXML private TableColumn<SponsorTournament, String>     colStart;
    @FXML private TableColumn<SponsorTournament, String>     colEnd;
    @FXML private TableColumn<SponsorTournament, Void>       colActions;

    // ── Stats ──────────────────────────────────────────────────────────────
    @FXML private Label statTotal;
    @FXML private Label statBudget;

    // ── Search ─────────────────────────────────────────────────────────────
    @FXML private TextField searchField;

    // ── Drawer ─────────────────────────────────────────────────────────────
    @FXML private StackPane drawerOverlay;
    @FXML private Label     formTitle;

    // ── Form fields ────────────────────────────────────────────────────────
    @FXML private ComboBox<Sponsor>       combSponsor;
    @FXML private ComboBox<Tounament>     combTournament;
    @FXML private ComboBox<SponsorMethod> combMethod;
    @FXML private TextField               fieldBudget;
    @FXML private DatePicker              dateStart;
    @FXML private DatePicker              dateEnd;
    @FXML private TextField               fieldNotes;
    @FXML private Button                  btnSave;
    @FXML private Label                   errSponsor;
    @FXML private Label                   errTournament;
    @FXML private Label                   errBudget;

    // ── State ──────────────────────────────────────────────────────────────
    private CrudSponsorTournament service;
    private CrudSponsor           sponsorService;
    private CrudTournament        tournamentService;
    private final ObservableList<SponsorTournament> data = FXCollections.observableArrayList();
    private SponsorTournament editingTarget = null;

    @FXML
    public void initialize() {
        try {
            service           = new CrudSponsorTournament();
            sponsorService    = new CrudSponsor();
            tournamentService = new CrudTournament();
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
        colSponsor.setCellValueFactory(cd    -> new SimpleStringProperty(cd.getValue().getSponsorName()));
        colTournament.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTournamentName()));
        colMethod.setCellValueFactory(cd     -> new SimpleStringProperty(cd.getValue().getMethodLabel()));
        colBudget.setCellValueFactory(cd     -> new SimpleStringProperty(
                cd.getValue().getBudgetAmount() != null
                        ? cd.getValue().getBudgetAmount().toPlainString() + " TND" : "—"));
        colStart.setCellValueFactory(cd      -> new SimpleStringProperty(
                cd.getValue().getStartDate() != null ? cd.getValue().getStartDate().toString() : "—"));
        colEnd.setCellValueFactory(cd        -> new SimpleStringProperty(
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
        try {
            ObservableList<Sponsor> sponsors = FXCollections.observableArrayList(sponsorService.getAll());
            combSponsor.setConverter(new StringConverter<>() {
                @Override public String toString(Sponsor s)   { return s == null ? "" : s.getName(); }
                @Override public Sponsor fromString(String s) { return null; }
            });
            combSponsor.setItems(sponsors);
        } catch (Exception ignored) {}

        try {
            ObservableList<Tounament> tournaments = FXCollections.observableArrayList(tournamentService.getAll());
            combTournament.setConverter(new StringConverter<>() {
                @Override public String toString(Tounament t)   { return t == null ? "" : t.getTournamentName(); }
                @Override public Tounament fromString(String s) { return null; }
            });
            combTournament.setItems(tournaments);
        } catch (Exception ignored) {}

        combMethod.setItems(FXCollections.observableArrayList(SponsorMethod.values()));
    }

    private void filterTable(String q) {
        if (q == null || q.isBlank()) { table.setItems(data); return; }
        String lq = q.toLowerCase();
        table.setItems(data.filtered(st ->
                st.getSponsorName().toLowerCase().contains(lq) ||
                st.getTournamentName().toLowerCase().contains(lq) ||
                st.getMethodLabel().toLowerCase().contains(lq)
        ));
    }

    private void updateStats() {
        statTotal.setText(String.valueOf(data.size()));
        BigDecimal total = data.stream()
                .filter(st -> st.getBudgetAmount() != null)
                .map(SponsorTournament::getBudgetAmount)
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
            SponsorTournament st = buildFromForm();
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
    private void populateForm(SponsorTournament st) {
        editingTarget = st;
        formTitle.setText("Modifier le contrat");
        combSponsor.getItems().stream()
                .filter(s -> s.getId().equals(st.getSponsorId()))
                .findFirst().ifPresent(combSponsor::setValue);
        combTournament.getItems().stream()
                .filter(t -> t.getTournamentId().equals(st.getTournamentId()))
                .findFirst().ifPresent(combTournament::setValue);
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
        formTitle.setText("Nouveau contrat tournoi");
        combSponsor.setValue(null);
        combTournament.setValue(null);
        combMethod.setValue(null);
        fieldBudget.clear();
        dateStart.setValue(null);
        dateEnd.setValue(null);
        fieldNotes.clear();
        btnSave.setText("Enregistrer");
        hideErr(errSponsor); hideErr(errTournament); hideErr(errBudget);
    }

    private SponsorTournament buildFromForm() {
        SponsorTournament st = new SponsorTournament();
        st.setSponsor(combSponsor.getValue());
        st.setTournament(combTournament.getValue());
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
        hideErr(errSponsor); hideErr(errTournament); hideErr(errBudget);
        if (combSponsor.getValue()    == null) { showErr(errSponsor,    "Sponsor obligatoire.");   ok = false; }
        if (combTournament.getValue() == null) { showErr(errTournament, "Tournoi obligatoire.");   ok = false; }
        try { new BigDecimal(fieldBudget.getText().trim()); }
        catch (NumberFormatException e) { showErr(errBudget, "Montant invalide."); ok = false; }
        return ok;
    }

    // ── Action column ──────────────────────────────────────────────────────
    private void addActionColumn() {
        Callback<TableColumn<SponsorTournament, Void>, TableCell<SponsorTournament, Void>> factory = col -> new TableCell<>() {
            private final Button editBtn   = new Button("✏");
            private final Button deleteBtn = new Button("🗑");
            private final HBox   box       = new HBox(4, editBtn, deleteBtn);
            {
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");
                editBtn.setOnAction(e -> populateForm(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> {
                    SponsorTournament st = getTableView().getItems().get(getIndex());
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
