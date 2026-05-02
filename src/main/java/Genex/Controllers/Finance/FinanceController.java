package Genex.Controllers.Finance;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class FinanceController {

    @FXML private ComboBox<String> pageSelector;
    @FXML private StackPane        financeContent;
    @FXML private Label            breadcrumb;

    private static final String PAGE_SPONSORS = "🏆  Sponsors";
    private static final String PAGE_BUDGET   = "💰  Budgets";

    @FXML
    public void initialize() {
        pageSelector.setItems(FXCollections.observableArrayList(PAGE_SPONSORS, PAGE_BUDGET));
        // Default: load Sponsors
        pageSelector.setValue(PAGE_SPONSORS);
        loadPage(PAGE_SPONSORS);
    }

    @FXML
    private void handlePageChange() {
        String selected = pageSelector.getValue();
        if (selected != null) loadPage(selected);
    }

    private void loadPage(String page) {
        String fxmlPath = PAGE_SPONSORS.equals(page)
                ? "/Fxml/Sponsors/Sponsors.fxml"
                : "/Fxml/Budget/Budget.fxml";

        breadcrumb.setText("FINANCES  /  " + page.replaceAll("^[^a-zA-Z]+", "").trim().toUpperCase());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            financeContent.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("FinanceController: failed to load " + fxmlPath);
        }
    }
}
