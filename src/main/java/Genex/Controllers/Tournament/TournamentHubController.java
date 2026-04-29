package Genex.Controllers.Tournament;

import Genex.entities.Tounament;
import Genex.services.CrudTournament;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

public class TournamentHubController {

    @FXML
    private TextField searchField;

    @FXML
    private Button btnAddTournament;

    @FXML
    private FlowPane tournamentCardsContainer;

    @FXML
    private VBox emptyState;

    private CrudTournament crudTournament;
    private List<Tounament> allTournaments;

    @FXML
    public void initialize() {
        System.out.println("TournamentHubController initialized");

        // Initialize CRUD service
        crudTournament = new CrudTournament();

        // Setup search listener
        setupSearchListener();

        // Load tournaments from database
        loadTournamentsFromDatabase();
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTournaments(newValue);
        });
    }

    private void filterTournaments(String searchText) {
        tournamentCardsContainer.getChildren().clear();

        if (searchText == null || searchText.trim().isEmpty()) {
            // Show all tournaments
            displayTournaments(allTournaments);
        } else {
            // Filter tournaments by name or format
            String search = searchText.toLowerCase();
            List<Tounament> filtered = allTournaments.stream()
                    .filter(t -> t.getTournamentName().toLowerCase().contains(search) ||
                            t.getFormat().toLowerCase().contains(search))
                    .toList();
            displayTournaments(filtered);
        }
    }

    private void updateEmptyState() {
        boolean isEmpty = tournamentCardsContainer.getChildren().isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);
    }

    @FXML
    private void openAddTournamentModal() {
        try {
            System.out.println("Opening Add Tournament Modal...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tournament/AddTournamentModal.fxml"));
            Parent modalRoot = loader.load();

            // Create modal stage
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle("Nouveau Tournoi");

            Scene scene = new Scene(modalRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modalStage.setScene(scene);

            // Get controller and set callback
            AddTournamentModalController controller = loader.getController();
            controller.setOnSaveCallback(tournament -> {
                System.out.println("Saving tournament: " + tournament.getTournamentName());

                // Save to database
                crudTournament.addEntity(tournament);

                // Reload tournaments from database
                loadTournamentsFromDatabase();

                modalStage.close();
            });

            modalStage.showAndWait();

        } catch (Exception e) {
            System.err.println("Error opening Add Tournament Modal");
            e.printStackTrace();
        }
    }

    private void loadTournamentsFromDatabase() {
        try {
            System.out.println("Loading tournaments from database...");

            // Get all tournaments from database
            allTournaments = crudTournament.getAll();

            System.out.println("Loaded " + allTournaments.size() + " tournaments");

            // Display tournaments
            displayTournaments(allTournaments);

        } catch (Exception e) {
            System.err.println("Error loading tournaments from database");
            e.printStackTrace();
        }
    }

    private void displayTournaments(List<Tounament> tournaments) {
        tournamentCardsContainer.getChildren().clear();

        for (Tounament tournament : tournaments) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tournament/TournamentCard.fxml"));
                Parent card = loader.load();

                TournamentCardController cardController = loader.getController();
                cardController.setTournament(tournament);

                // Set callback to reload tournaments when card is updated/deleted
                cardController.setOnUpdateCallback(this::loadTournamentsFromDatabase);

                tournamentCardsContainer.getChildren().add(card);

            } catch (Exception e) {
                System.err.println("Error creating tournament card for: " + tournament.getTournamentName());
                e.printStackTrace();
            }
        }

        updateEmptyState();
    }

    public void addTournamentCard(Parent card) {
        tournamentCardsContainer.getChildren().add(card);
        updateEmptyState();
    }

    public void clearTournaments() {
        tournamentCardsContainer.getChildren().clear();
        updateEmptyState();
    }
}
