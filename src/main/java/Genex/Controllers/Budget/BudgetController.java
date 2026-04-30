package Genex.Controllers.Budget;

import Genex.entities.Budget;
import Genex.entities.Sponsor;
import Genex.services.CrudBudget;
import Genex.services.CrudSponsor;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;

public class BudgetController {

    // ── Table ──────────────────────────────────────────────────────────────
    @FXML private TableView<Budget>               budgetTable;
    @FXML private TableColumn<Budget, String>     colCenter;
    @FXML private TableColumn<Budget, String>     colSponsor;
    @FXML private TableColumn<Budget, Integer>    colYear;
    @FXML private TableColumn<Budget, BigDecimal> colAllocated;
    @FXML private TableColumn<Budget, BigDecimal> colSpent;
    @FXML private TableColumn<Budget, BigDecimal> colRemaining;
    @FXML private TableColumn<Budget, Void>       colActions;

    // ── Stat labels ────────────────────────────────────────────────────────
    @FXML private Label statTotal;
    @FXML private Label statAllocated;
    @FXML private Label statSpent;
    @FXML private Label statRemaining;

    // ── Chart — coming soon ────────────────────────────────────────────────

    // ── Search ─────────────────────────────────────────────────────────────
    @FXML private TextField searchField;

    // ── Drawer ─────────────────────────────────────────────────────────────
    @FXML private StackPane drawerOverlay;

    // ── Form fields ────────────────────────────────────────────────────────
    @FXML private Label             formTitle;
    @FXML private TextField         fieldCenterId;
    @FXML private ComboBox<Sponsor> combSponsor;
    @FXML private TextField         fieldYear;
    @FXML private TextField         fieldAllocated;
    @FXML private TextField         fieldSpent;
    @FXML private TextField         fieldDoc;
    @FXML private Button            btnSave;
    @FXML private Label             errCenter;
    @FXML private Label             errYear;
    @FXML private Label             errAllocated;

    // ── State ──────────────────────────────────────────────────────────────
    private CrudBudget  budgetService;
    private CrudSponsor sponsorService;
    private final ObservableList<Budget> data = FXCollections.observableArrayList();
    private Budget editingTarget = null;

    @FXML
    public void initialize() {
        try {
            budgetService  = new CrudBudget();
            sponsorService = new CrudSponsor();
        } catch (Exception e) {
            showAlert("Erreur DB", rootCause(e));
            return;
        }

        colCenter.setCellValueFactory(new PropertyValueFactory<>("centerId"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("fiscalYear"));
        colAllocated.setCellValueFactory(new PropertyValueFactory<>("allocatedAmount"));
        colSpent.setCellValueFactory(new PropertyValueFactory<>("spentAmount"));
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remainingAmount"));
        colSponsor.setCellValueFactory(cd -> {
            Sponsor s = cd.getValue().getSponsor();
            return new SimpleStringProperty(s != null ? s.getName() : "—");
        });

        addActionColumn();
        loadSponsors();
        loadData();
        searchField.textProperty().addListener((obs, o, v) -> filterTable(v));
        budgetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }
    private void loadData() {
        try {
            data.setAll(budgetService.getAll());
            budgetTable.setItems(data);
            updateStats();
        } catch (Exception e) {
            showAlert("Erreur", rootCause(e));
        }
    }

    private void loadSponsors() {
        try {
            ObservableList<Sponsor> sponsors = FXCollections.observableArrayList(sponsorService.getAll());
            combSponsor.setConverter(new StringConverter<>() {
                @Override public String toString(Sponsor s)   { return s == null ? "— Fonds internes —" : s.getName(); }
                @Override public Sponsor fromString(String s) { return null; }
            });
            sponsors.add(0, null);
            combSponsor.setItems(sponsors);
        } catch (Exception e) {
            showAlert("Erreur", rootCause(e));
        }
    }

    private void filterTable(String q) {
        if (q == null || q.isBlank()) { budgetTable.setItems(data); return; }
        String lq = q.toLowerCase();
        budgetTable.setItems(data.filtered(b ->
                (b.getCenterId() != null && b.getCenterId().toLowerCase().contains(lq)) ||
                String.valueOf(b.getFiscalYear()).contains(lq) ||
                (b.getSponsor() != null && b.getSponsor().getName().toLowerCase().contains(lq))
        ));
    }

    private void updateStats() {
        long count = data.size();
        BigDecimal totalAlloc = data.stream().map(Budget::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSpent = data.stream().map(Budget::getSpentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRem   = totalAlloc.subtract(totalSpent);

        statTotal.setText(String.valueOf(count));
        statAllocated.setText(totalAlloc.toPlainString() + " TND");
        statSpent.setText(totalSpent.toPlainString() + " TND");
        statRemaining.setText(totalRem.toPlainString() + " TND");
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

    // ── File picker ────────────────────────────────────────────────────────
    @FXML
    private void handlePickDoc() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir un justificatif");
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images (PNG, JPEG)", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(fieldDoc.getScene().getWindow());
        if (f != null) fieldDoc.setText(f.getAbsolutePath());
    }

    // ── Form actions ───────────────────────────────────────────────────────
    @FXML
    private void handleSave() {
        if (!validateForm()) return;
        try {
            Budget b = buildFromForm();
            if (editingTarget == null) {
                budgetService.addEntity(b);
            } else {
                budgetService.updateEntity(b, editingTarget.getId());
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

    // ── Navigation ─────────────────────────────────────────────────────────
    @FXML
    private void goToSponsors() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Sponsors/Sponsors.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) budgetTable.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur navigation", rootCause(e));
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private void populateForm(Budget b) {
        editingTarget = b;
        formTitle.setText("Modifier le budget");
        fieldCenterId.setText(b.getCenterId() != null ? b.getCenterId() : "");
        fieldYear.setText(String.valueOf(b.getFiscalYear()));
        fieldAllocated.setText(b.getAllocatedAmount().toPlainString());
        fieldSpent.setText(b.getSpentAmount().toPlainString());
        combSponsor.getItems().stream()
                .filter(s -> s != null && s.getId().equals(b.getSponsorId()))
                .findFirst().ifPresent(combSponsor::setValue);
        btnSave.setText("Mettre à jour");
        openDrawer();
    }

    private void clearForm() {
        editingTarget = null;
        formTitle.setText("Nouveau budget");
        fieldCenterId.clear();
        fieldYear.clear();
        fieldAllocated.clear();
        fieldSpent.clear();
        fieldDoc.clear();
        combSponsor.setValue(null);
        btnSave.setText("Enregistrer");
        hideErr(errCenter); hideErr(errYear); hideErr(errAllocated);
    }

    private Budget buildFromForm() {
        Budget b = new Budget();
        b.setCenterId(fieldCenterId.getText().trim());
        b.setFiscalYear(Integer.parseInt(fieldYear.getText().trim()));
        b.setAllocatedAmount(new BigDecimal(fieldAllocated.getText().trim()));
        String spent = fieldSpent.getText().trim();
        b.setSpentAmount(spent.isEmpty() ? BigDecimal.ZERO : new BigDecimal(spent));
        b.setSponsor(combSponsor.getValue());
        return b;
    }

    private boolean validateForm() {
        boolean ok = true;
        hideErr(errCenter); hideErr(errYear); hideErr(errAllocated);

        if (fieldCenterId.getText().isBlank()) {
            showErr(errCenter, "L'ID du centre est obligatoire.");
            ok = false;
        }
        try { Integer.parseInt(fieldYear.getText().trim()); }
        catch (NumberFormatException e) {
            showErr(errYear, "Année invalide.");
            ok = false;
        }
        try { new BigDecimal(fieldAllocated.getText().trim()); }
        catch (NumberFormatException e) {
            showErr(errAllocated, "Montant invalide.");
            ok = false;
        }
        return ok;
    }

    private void addActionColumn() {
        Callback<TableColumn<Budget, Void>, TableCell<Budget, Void>> factory = col -> new TableCell<>() {
            private final Button editBtn   = new Button("✏");
            private final Button deleteBtn = new Button("🗑");
            private final HBox   box       = new HBox(4, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");
                editBtn.setOnAction(e -> populateForm(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> {
                    Budget b = getTableView().getItems().get(getIndex());
                    Alert c = new Alert(Alert.AlertType.CONFIRMATION,
                            "Supprimer ce budget (" + b.getFiscalYear() + ") ?",
                            ButtonType.YES, ButtonType.NO);
                    c.setHeaderText(null);
                    c.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.YES) {
                            try { budgetService.deleteEntity(b); loadData(); }
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
        Throwable c = e;
        while (c.getCause() != null) c = c.getCause();
        return c.getClass().getSimpleName() + ": " + c.getMessage();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
