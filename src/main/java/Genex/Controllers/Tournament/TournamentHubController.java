package Genex.Controllers.Tournament;

import Genex.entities.Game;
import Genex.entities.Tounament;
import Genex.services.CrudGame;
import Genex.services.CrudTournament;
import Genex.services.CrudTournamentMatch;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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

    // Player record section
    @FXML private VBox playerRecordSection;
    @FXML private Text txtTournamentsPlayed;
    @FXML private Text txtTotalWins;
    @FXML private Text txtTotalLosses;
    @FXML private Text txtWinRatio;
    @FXML private Text txtBestPlacement;

    private CrudTournament crudTournament;
    private CrudGame crudGame;
    private CrudTournamentMatch crudMatch = new CrudTournamentMatch();
    private List<Tounament> allTournaments;
    private boolean showOnlyMyTournaments = false;

    @FXML
    public void initialize() {
        System.out.println("TournamentHubController initialized");

        // Initialize CRUD services
        crudTournament = new CrudTournament();
        crudGame = new CrudGame();

        // Setup role-based UI
        setupRoleBasedUI();

        // Load games for filter
        loadGamesFilter();

        // Setup search listener
        setupSearchListener();

        // Setup game filter listener
        setupGameFilterListener();

        // Load tournaments from database
        loadTournamentsFromDatabase();
    }

    private void setupRoleBasedUI() {
        Genex.entities.User currentUser = Genex.utils.SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null && "player".equalsIgnoreCase(currentUser.getRole())) {
            btnAddTournament.setText("Mes Tournois");
            btnAddTournament.setOnAction(event -> toggleMyTournaments());
            // Show player record
            loadPlayerRecord(currentUser.getId());
        } else {
            btnAddTournament.setText("+ Nouveau Tournoi");
            btnAddTournament.setOnAction(event -> openAddTournamentModal());
            // Hide record for admin
            if (playerRecordSection != null) {
                playerRecordSection.setVisible(false);
                playerRecordSection.setManaged(false);
            }
        }
    }

    private void loadPlayerRecord(String playerId) {
        try {
            playerRecordSection.setVisible(true);
            playerRecordSection.setManaged(true);

            int tournamentsPlayed = crudMatch.getTotalTournamentsPlayed(playerId);
            int totalWins = crudMatch.getTotalWins(playerId);
            int totalLosses = crudMatch.getTotalLosses(playerId);
            Integer bestPlacement = crudMatch.getBestPlacement(playerId);

            int total = totalWins + totalLosses;
            int ratio = total > 0 ? (int) ((totalWins * 100.0) / total) : 0;

            txtTournamentsPlayed.setText(String.valueOf(tournamentsPlayed));
            txtTotalWins.setText(String.valueOf(totalWins));
            txtTotalLosses.setText(String.valueOf(totalLosses));
            txtWinRatio.setText(ratio + "%");

            if (bestPlacement != null) {
                String medal = bestPlacement == 1 ? "🥇 1er" :
                               bestPlacement == 2 ? "🥈 2ème" :
                               bestPlacement == 3 ? "🥉 3ème" :
                               bestPlacement + "ème";
                txtBestPlacement.setText(medal);
            } else {
                txtBestPlacement.setText("-");
            }
        } catch (Exception e) {
            System.err.println("Error loading player record: " + e.getMessage());
        }
    }

    private void toggleMyTournaments() {
        showOnlyMyTournaments = !showOnlyMyTournaments;
        
        if (showOnlyMyTournaments) {
            btnAddTournament.setText("Tous les Tournois");
            btnAddTournament.getStyleClass().add("active-filter");
        } else {
            btnAddTournament.setText("Mes Tournois");
            btnAddTournament.getStyleClass().remove("active-filter");
        }
        
        filterTournaments();
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
                    // Filter by search text - using startsWith for more precise filtering
                    boolean matchesSearch = searchText == null || searchText.trim().isEmpty() ||
                            t.getTournamentName().toLowerCase().startsWith(searchText.toLowerCase()) ||
                            t.getFormat().toLowerCase().startsWith(searchText.toLowerCase());

                    // Filter by game
                    boolean matchesGame = selectedGame == null || 
                            "ALL".equals(selectedGame.getId()) ||
                            (t.getGame_id() != null && t.getGame_id().equals(selectedGame.getId()));

                    // Filter by player's joined tournaments (if player and filter active)
                    boolean matchesMyTournaments = true;
                    if (showOnlyMyTournaments) {
                        Genex.entities.User currentUser = Genex.utils.SessionManager.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            String playerId = currentUser.getId();
                            Genex.services.CrudTournamentParticipant crudParticipant = new Genex.services.CrudTournamentParticipant();
                            matchesMyTournaments = crudParticipant.isPlayerParticipating(t.getTournamentId(), playerId);
                        }
                    }

                    return matchesSearch && matchesGame && matchesMyTournaments;
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
            System.out.println("Opening Add Tournament Drawer...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tournament/AddTournamentModal.fxml"));
            StackPane drawerOverlay = loader.load();

            // Add drawer overlay to the stack
            rootStackPane.getChildren().add(drawerOverlay);

            // Get controller and set callbacks
            AddTournamentModalController controller = loader.getController();
            controller.setOnSaveCallback(tournament -> {
                System.out.println("Saving tournament: " + tournament.getTournamentName());

                // Save to database
                crudTournament.addEntity(tournament);

                // Remove drawer overlay and reload
                rootStackPane.getChildren().remove(drawerOverlay);
                loadTournamentsFromDatabase();
            });

            // Handle close without saving
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(drawerOverlay);
            });

        } catch (Exception e) {
            System.err.println("Error opening Add Tournament Drawer");
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
