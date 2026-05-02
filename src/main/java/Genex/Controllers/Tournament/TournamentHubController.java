package Genex.Controllers.Tournament;

import Genex.entities.Game;
import Genex.entities.Tounament;
import Genex.services.CrudGame;
import Genex.services.CrudTournament;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;

public class TournamentHubController {

    @FXML
    private StackPane rootStackPane;

    @FXML
    private VBox contentArea;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<Game> comboGameFilter;

    @FXML
    private Button btnAddTournament;

    @FXML
    private VBox tournamentCardsContainer;

    @FXML
    private VBox emptyState;

    private CrudTournament crudTournament;
    private CrudGame crudGame;
    private List<Tounament> allTournaments;

    @FXML
    public void initialize() {
        System.out.println("TournamentHubController initialized");

        // Initialize CRUD services
        crudTournament = new CrudTournament();
        crudGame = new CrudGame();

        // Load games for filter
        loadGamesFilter();

        // Setup search listener
        setupSearchListener();

        // Setup game filter listener
        setupGameFilterListener();

        // Load tournaments from database
        loadTournamentsFromDatabase();
    }

    private void loadGamesFilter() {
        try {
            List<Game> games = crudGame.getgames();
            
            // Add "All Games" option
            Game allGamesOption = new Game();
            allGamesOption.setId("ALL");
            allGamesOption.setNom("Tous les jeux");
            
            comboGameFilter.getItems().add(allGamesOption);
            comboGameFilter.getItems().addAll(games);
            
            // Set default selection to "All Games"
            comboGameFilter.setValue(allGamesOption);

            // Set custom string converter to display game name
            comboGameFilter.setConverter(new StringConverter<Game>() {
                @Override
                public String toString(Game game) {
                    return game != null ? game.getNom() : "";
                }

                @Override
                public Game fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading games for filter: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTournaments();
        });
    }

    private void setupGameFilterListener() {
        comboGameFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterTournaments();
        });
    }

    private void filterTournaments() {
        tournamentCardsContainer.getChildren().clear();

        String searchText = searchField.getText();
        Game selectedGame = comboGameFilter.getValue();

        List<Tounament> filtered = allTournaments.stream()
                .filter(t -> {
                    // Filter by search text
                    boolean matchesSearch = searchText == null || searchText.trim().isEmpty() ||
                            t.getTournamentName().toLowerCase().contains(searchText.toLowerCase()) ||
                            t.getFormat().toLowerCase().contains(searchText.toLowerCase());

                    // Filter by game
                    boolean matchesGame = selectedGame == null || 
                            "ALL".equals(selectedGame.getId()) ||
                            (t.getGame_id() != null && t.getGame_id().equals(selectedGame.getId()));

                    return matchesSearch && matchesGame;
                })
                .toList();

        displayTournaments(filtered);
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
            Parent addTournamentForm = loader.load();

            // 1. Apply Blur effect to the background
            GaussianBlur blur = new GaussianBlur(15);
            contentArea.setEffect(blur);
            contentArea.setDisable(true); // Prevent clicking background items

            // 2. Wrap the form in a darkening overlay (dimmer)
            VBox overlay = new VBox(addTournamentForm);
            overlay.setAlignment(javafx.geometry.Pos.CENTER);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Dim background

            // 3. Add to the stack
            rootStackPane.getChildren().add(overlay);

            // 4. Pass a "Close" callback to the AddTournamentModalController
            AddTournamentModalController controller = loader.getController();
            controller.setOnSaveCallback(tournament -> {
                System.out.println("Saving tournament: " + tournament.getTournamentName());

                // Save to database
                crudTournament.addEntity(tournament);

                // Remove overlay and reload
                rootStackPane.getChildren().remove(overlay);
                contentArea.setEffect(null);
                contentArea.setDisable(false);
                loadTournamentsFromDatabase();
            });

            // Also handle close without saving
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(overlay);
                contentArea.setEffect(null);
                contentArea.setDisable(false);
            });

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

            // Apply current filters
            filterTournaments();

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
                cardController.setRootStackPane(rootStackPane, contentArea);

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
