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

    private static final String PAGE_OVERVIEW            = "📊  Vue d'ensemble";
    private static final String PAGE_SPONSORS            = "🏆  Sponsors";
    private static final String PAGE_BUDGET              = "💰  Budgets";
    private static final String PAGE_SPONSOR_TEAM        = "👥  Sponsors Equipes";
    private static final String PAGE_SPONSOR_TOURNAMENT  = "🎮  Sponsors Tournois";

    @FXML
    public void initialize() {
        pageSelector.setItems(FXCollections.observableArrayList(
                PAGE_OVERVIEW,
                PAGE_SPONSORS,
                PAGE_BUDGET,
                PAGE_SPONSOR_TEAM,
                PAGE_SPONSOR_TOURNAMENT
        ));
        pageSelector.setValue(PAGE_OVERVIEW);
        loadPage(PAGE_OVERVIEW);
    }

    @FXML
    private void handlePageChange() {
        String selected = pageSelector.getValue();
        if (selected != null) loadPage(selected);
    }

    private void loadPage(String page) {
        String fxmlPath = switch (page) {
            case PAGE_OVERVIEW           -> "/Fxml/Finance/FinanceOverview.fxml";
            case PAGE_SPONSORS           -> "/Fxml/Sponsors/Sponsors.fxml";
            case PAGE_BUDGET             -> "/Fxml/Budget/Budget.fxml";
            case PAGE_SPONSOR_TEAM       -> "/Fxml/SponsorTeam/SponsorTeam.fxml";
            case PAGE_SPONSOR_TOURNAMENT -> "/Fxml/SponsorTournament/SponsorTournament.fxml";
            default                      -> "/Fxml/Finance/FinanceOverview.fxml";
        };

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
