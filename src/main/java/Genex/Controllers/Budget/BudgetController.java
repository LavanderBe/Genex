package Genex.Controllers.Budget;

import Genex.entities.Budget;
import Genex.entities.Sponsor;
import Genex.services.CrudBudget;
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
import java.math.BigDecimal;

public class BudgetController {

    // ── Table ──────────────────────────────────────────────────────────────
    @FXML private TableView<Budget>          budgetTable;
    @FXML private TableColumn<Budget, String>     colCenter;
    @FXML private TableColumn<Budget, String>     colSponsor;
    @FXML private TableColumn<Budget, Integer>    colYear;
    @FXML private TableColumn<Budget, BigDecimal> colAllocated;
    @FXML private TableColumn<Budget, BigDecimal> colSpent;
    @FXML private TableColumn<Budget, BigDecimal> colRemaining;
    @FXML private TableColumn<Budget, Void>       colActions;

    // ── Form ───────────────────────────────────────────────────────────────
    @FXML private TextField        fieldCenterId;
    @FXML private ComboBox<Sponsor> combSponsor;
    @FXML private TextField        fieldYear;
    @FXML private TextField        fieldAllocated;
    @FXML private TextField        fieldSpent;
    @FXML private Button           btnSave;
    @FXML private Label            formTitle;

    // ── Search ─────────────────────────────────────────────────────────────
    @FXML private TextField searchField;

    // ── State ──────────────────────────────────────────────────────────────
    private final CrudBudget   budgetService  = new CrudBudget();
    private final CrudSponsor  sponsorService = new CrudSponsor();
    private final ObservableList<Budget> data = FXCollections.observableArrayList();
    private Budget editingTarget = null;

    @FXML
    public void initialize() {
        colCenter.setCellValueFactory(new PropertyValueFactory<>("centerId"));
        colYear.setCellValueFactory(new PropertyValueFactory<>("fiscalYear"));
        colAllocated.setCellValueFactory(new PropertyValueFactory<>("allocatedAmount"));
        colSpent.setCellValueFactory(new PropertyValueFactory<>("spentAmount"));
        colRemaining.setCellValueFactory(new PropertyValueFactory<>("remainingAmount"));

        // Sponsor column — show name from nested object
        colSponsor.setCellValueFactory(cd -> {
            Sponsor s = cd.getValue().getSponsor();
            return new javafx.beans.property.SimpleStringProperty(s != null ? s.getName() : "—");
        });

        addActionColumn();
        loadSponsors();
        loadData();

        searchField.textProperty().addListener((obs, old, val) -> filterTable(val));
    }

    // ── Data ───────────────────────────────────────────────────────────────
    private void loadData() {
        data.setAll(budgetService.getAll());
        budgetTable.setItems(data);
    }

    private void loadSponsors() {
        ObservableList<Sponsor> sponsors = FXCollections.observableArrayList(sponsorService.getAll());
        combSponsor.setItems(sponsors);
        combSponsor.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Sponsor s)   { return s == null ? "" : s.getName(); }
            @Override public Sponsor fromString(String s) { return null; }
        });
        // Allow no sponsor (internal budget)
        combSponsor.getItems().add(0, null);
    }

    private void filterTable(String query) {
        if (query == null || query.isBlank()) {
            budgetTable.setItems(data);
            return;
        }
        String q = query.toLowerCase();
        budgetTable.setItems(data.filtered(b ->
                b.getCenterId().toLowerCase().contains(q) ||
                String.valueOf(b.getFiscalYear()).contains(q) ||
                (b.getSponsor() != null && b.getSponsor().getName().toLowerCase().contains(q))
        ));
    }

    // ── Form actions ───────────────────────────────────────────────────────
    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        Budget b = buildFromForm();

        if (editingTarget == null) {
            budgetService.addEntity(b);
        } else {
            budgetService.updateEntity(b, editingTarget.getId());
        }

        clearForm();
        loadData();
    }

    @FXML
    private void handleClear() {
        clearForm();
    }

    @FXML
    private void goToSponsors() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Sponsors/Sponsors.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) budgetTable.getScene().getWindow();
            stage.setScene(new Scene(root, stage.getScene().getWidth(), stage.getScene().getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────
    private void populateForm(Budget b) {
        editingTarget = b;
        formTitle.setText("Modifier le budget");
        fieldCenterId.setText(b.getCenterId());
        fieldYear.setText(String.valueOf(b.getFiscalYear()));
        fieldAllocated.setText(b.getAllocatedAmount().toPlainString());
        fieldSpent.setText(b.getSpentAmount().toPlainString());
        combSponsor.getItems().stream()
                .filter(s -> s != null && s.getId().equals(b.getSponsorId()))
                .findFirst().ifPresent(combSponsor::setValue);
        btnSave.setText("Mettre à jour");
    }

    private void clearForm() {
        editingTarget = null;
        formTitle.setText("Nouveau budget");
        fieldCenterId.clear();
        fieldYear.clear();
        fieldAllocated.clear();
        fieldSpent.clear();
        combSponsor.setValue(null);
        btnSave.setText("Enregistrer");
    }

    private Budget buildFromForm() {
        Budget b = new Budget();
        b.setCenterId(fieldCenterId.getText().trim());
        b.setFiscalYear(Integer.parseInt(fieldYear.getText().trim()));
        b.setAllocatedAmount(new BigDecimal(fieldAllocated.getText().trim()));
        String spentText = fieldSpent.getText().trim();
        b.setSpentAmount(spentText.isEmpty() ? BigDecimal.ZERO : new BigDecimal(spentText));
        b.setSponsor(combSponsor.getValue());
        return b;
    }

    private boolean validateForm() {
        if (fieldCenterId.getText().isBlank()) {
            showAlert("Validation", "L'ID du centre est obligatoire.");
            return false;
        }
        try { Integer.parseInt(fieldYear.getText().trim()); }
        catch (NumberFormatException e) {
            showAlert("Validation", "L'année fiscale doit être un nombre valide.");
            return false;
        }
        try { new BigDecimal(fieldAllocated.getText().trim()); }
        catch (NumberFormatException e) {
            showAlert("Validation", "Le montant alloué doit être un nombre valide.");
            return false;
        }
        return true;
    }

    private void addActionColumn() {
        Callback<TableColumn<Budget, Void>, TableCell<Budget, Void>> factory = col -> new TableCell<>() {
            private final Button editBtn   = new Button("✏");
            private final Button deleteBtn = new Button("🗑");
            private final HBox   box       = new HBox(6, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("action-btn-edit");
                deleteBtn.getStyleClass().add("action-btn-delete");

                editBtn.setOnAction(e -> populateForm(getTableView().getItems().get(getIndex())));

                deleteBtn.setOnAction(e -> {
                    Budget b = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                            "Supprimer ce budget (" + b.getFiscalYear() + ") ?",
                            ButtonType.YES, ButtonType.NO);
                    confirm.setHeaderText(null);
                    confirm.showAndWait().ifPresent(btn -> {
                        if (btn == ButtonType.YES) {
                            budgetService.deleteEntity(b);
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
