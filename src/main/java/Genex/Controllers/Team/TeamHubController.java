package Genex.Controllers.Team;

import Genex.entities.Team;
import Genex.entities.TeamRankingEntry;
import Genex.services.CrudTeam;
import Genex.services.TeamRankingService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class TeamHubController {

    @FXML private StackPane rootStackPane;
    @FXML private VBox contentArea;
    @FXML private TextField searchField;
    @FXML private Button btnAddTeam;
    @FXML private Button btnRanking;
    @FXML private Button btnBackFromRanking;
    @FXML private FlowPane teamCardsContainer;

    private CrudTeam crudTeam;
    private List<Team> allTeams;
    private javafx.scene.layout.Pane mainContentContainer;
    private javafx.stage.Popup searchPopup;
    private javafx.scene.control.ListView<Team> searchSuggestions;

    @FXML
    public void initialize() {
        System.out.println("TeamHubController initialized");
        
        crudTeam = new CrudTeam();
        setupSearchAutocomplete();
        loadTeamsFromDatabase();
    }

    // ── Content container (set by MainController / Dashboard) ───────
    public void setContentContainer(javafx.scene.layout.Pane contentContainer) {
        this.mainContentContainer = contentContainer;
        if (allTeams != null && !allTeams.isEmpty()) displayTeams(allTeams);
    }

    @FXML
    private void backToTeamList() {
        loadTeamsFromDatabase();
    }

    // ── Open Add Team Modal (Tournament pattern) ────────────────────
    @FXML
    private void openAddTeamDrawer() {
        try {
            System.out.println("Opening Add Team Modal...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTeamModal.fxml"));
            StackPane modalOverlay = loader.load();

            // Add modal overlay to the stack
            rootStackPane.getChildren().add(modalOverlay);

            // Get controller and set callbacks
            AddTeamModalController controller = loader.getController();
            controller.setOnSaveCallback(team -> {
                System.out.println("Saving team: " + team.getName());
                System.out.println("Logo path: " + team.getLogoImage());
                System.out.println("Jersey path: " + team.getJerseyImage());

                // Save to database
                crudTeam.addEntity(team);

                // Remove modal overlay and reload
                rootStackPane.getChildren().remove(modalOverlay);
                
                // Force reload from database to get fresh data
                loadTeamsFromDatabase();
            });

            // Handle close without saving
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(modalOverlay);
            });

        } catch (Exception e) {
            System.err.println("Error opening Add Team Modal");
            e.printStackTrace();
        }
    }

    // ── Called by TeamCardController to open edit modal ─────────────
    public void openEditDrawer(Team team) {
        try {
            System.out.println("Opening Edit Team Modal...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTeamModal.fxml"));
            StackPane modalOverlay = loader.load();

            // Add modal overlay to the stack
            rootStackPane.getChildren().add(modalOverlay);

            // Get controller and set team data
            AddTeamModalController controller = loader.getController();
            controller.setTeam(team);
            
            controller.setOnSaveCallback(updatedTeam -> {
                System.out.println("Updating team: " + updatedTeam.getName());

                // Update in database
                crudTeam.updateEntity(updatedTeam, team.getId());

                // Remove modal overlay and reload
                rootStackPane.getChildren().remove(modalOverlay);
                loadTeamsFromDatabase();
            });

            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(modalOverlay);
            });

        } catch (Exception e) {
            System.err.println("Error opening Edit Team Modal");
            e.printStackTrace();
        }
    }

    // ── Search with Autocomplete ─────────────────────────────────────
    @FXML
    private void openRankingView() {
        teamCardsContainer.getChildren().clear();
        showRankingHeader();

        VBox rankingBoard = new VBox(10);
        rankingBoard.setPrefWidth(900);
        rankingBoard.setStyle("-fx-background-color: rgba(20,20,40,0.75); -fx-border-color: rgba(139,13,13,0.35); -fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 18;");

        Label title = new Label("Ranking des equipes");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        rankingBoard.getChildren().add(title);

        List<TeamRankingEntry> rankings = new TeamRankingService().getTeamRankings();
        if (rankings.isEmpty()) {
            Label empty = new Label("Aucune equipe a classer pour le moment.");
            empty.setStyle("-fx-text-fill: rgba(255,255,255,0.45); -fx-font-style: italic;");
            rankingBoard.getChildren().add(empty);
        } else {
            for (TeamRankingEntry entry : rankings) {
                rankingBoard.getChildren().add(createRankingRow(entry));
            }
        }

        teamCardsContainer.getChildren().add(rankingBoard);
    }

    private void showRankingHeader() {
        if (btnBackFromRanking != null) {
            btnBackFromRanking.setVisible(true);
            btnBackFromRanking.setManaged(true);
        }
        btnAddTeam.setVisible(false);
        btnAddTeam.setManaged(false);
        btnRanking.setVisible(false);
        btnRanking.setManaged(false);
        searchField.setVisible(false);
        searchField.setManaged(false);
    }

    private void showTeamListHeader() {
        if (btnBackFromRanking != null) {
            btnBackFromRanking.setVisible(false);
            btnBackFromRanking.setManaged(false);
        }
        btnAddTeam.setVisible(true);
        btnAddTeam.setManaged(true);
        btnRanking.setVisible(true);
        btnRanking.setManaged(true);
        searchField.setVisible(true);
        searchField.setManaged(true);
    }

    private javafx.scene.layout.HBox createRankingRow(TeamRankingEntry entry) {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(18);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 12 14;");

        Label rank = new Label("#" + entry.getRank());
        rank.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 16px; -fx-font-weight: bold;");
        rank.setMinWidth(50);

        Label name = new Label(entry.getTeamName());
        name.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        javafx.scene.layout.HBox.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);

        Label stats = new Label("W " + entry.getWins() + "   L " + entry.getLosses() +
                "   WR " + String.format("%.0f%%", entry.getWinRate()) +
                "   Tournois " + entry.getTournaments());
        stats.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");

        row.getChildren().addAll(rank, name, stats);
        return row;
    }

    private void setupSearchAutocomplete() {
        // Create suggestions dropdown
        searchSuggestions = new javafx.scene.control.ListView<>();
        searchSuggestions.setPrefWidth(searchField.getWidth());
        searchSuggestions.setPrefHeight(200);
        searchSuggestions.setMaxHeight(200);
        searchSuggestions.setStyle(
            "-fx-background-color: #1a1a2e;" +
            "-fx-border-color: #8B0D0D;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0, 0, 5);"
        );
        
        // Create popup
        searchPopup = new javafx.stage.Popup();
        searchPopup.setAutoHide(true);
        searchPopup.getContent().add(searchSuggestions);
        
        // Custom cell factory for team suggestions
        searchSuggestions.setCellFactory(lv -> new javafx.scene.control.ListCell<Team>() {
            @Override
            protected void updateItem(Team team, boolean empty) {
                super.updateItem(team, empty);
                if (empty || team == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    javafx.scene.layout.VBox container = new javafx.scene.layout.VBox(4);
                    container.setStyle("-fx-padding: 8;");
                    
                    javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(team.getName());
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
                    
                    javafx.scene.control.Label contactLabel = new javafx.scene.control.Label(
                        team.getContact() != null ? team.getContact() : "—"
                    );
                    contactLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 11px;");
                    
                    container.getChildren().addAll(nameLabel, contactLabel);
                    setGraphic(container);
                    setText(null);
                    
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                    setOnMouseEntered(e -> setStyle("-fx-background-color: rgba(139,13,13,0.3); -fx-padding: 0;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: transparent; -fx-padding: 0;"));
                }
            }
        });
        
        // Handle selection
        searchSuggestions.setOnMouseClicked(e -> {
            Team selected = searchSuggestions.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openTeamDetail(selected);
                searchPopup.hide();
                searchField.clear();
            }
        });
        
        // Listen to text changes
        searchField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.trim().isEmpty()) {
                searchPopup.hide();
            } else {
                String searchText = val.toLowerCase();
                List<Team> matches = allTeams.stream()
                    .filter(t -> t.getName().toLowerCase().startsWith(searchText))
                    .limit(5) // Show max 5 suggestions
                    .toList();
                
                if (!matches.isEmpty()) {
                    searchSuggestions.getItems().setAll(matches);
                    
                    // Position popup below search field
                    if (!searchPopup.isShowing()) {
                        javafx.geometry.Bounds bounds = searchField.localToScreen(searchField.getBoundsInLocal());
                        searchPopup.show(searchField, bounds.getMinX(), bounds.getMaxY());
                    }
                } else {
                    searchPopup.hide();
                }
            }
        });
        
        // Update popup width when search field width changes
        searchField.widthProperty().addListener((obs, old, newWidth) -> {
            searchSuggestions.setPrefWidth(newWidth.doubleValue());
        });
    }
    
    private void openTeamDetail(Team team) {
        try {
            System.out.println("Opening team detail for: " + team.getName());
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/TeamDetail.fxml"));
            Parent teamDetail = loader.load();
            
            TeamDetailController controller = loader.getController();
            controller.setTeam(team);
            
            // Replace content
            if (mainContentContainer != null) {
                mainContentContainer.getChildren().clear();
                mainContentContainer.getChildren().add(teamDetail);
                
                if (mainContentContainer instanceof javafx.scene.layout.AnchorPane) {
                    javafx.scene.layout.AnchorPane.setTopAnchor(teamDetail, 0.0);
                    javafx.scene.layout.AnchorPane.setBottomAnchor(teamDetail, 0.0);
                    javafx.scene.layout.AnchorPane.setLeftAnchor(teamDetail, 0.0);
                    javafx.scene.layout.AnchorPane.setRightAnchor(teamDetail, 0.0);
                }
            }
        } catch (Exception e) {
            System.err.println("Error opening team detail: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void filterTeams(String text) {
        teamCardsContainer.getChildren().clear();
        if (text == null || text.trim().isEmpty()) {
            displayTeams(allTeams);
        } else {
            String s = text.toLowerCase();
            displayTeams(allTeams.stream()
                    .filter(t -> t.getName().toLowerCase().contains(s) ||
                            (t.getContact() != null && t.getContact().toLowerCase().contains(s)))
                    .toList());
        }
    }

    // ── Data ─────────────────────────────────────────────────────────
    private void loadTeamsFromDatabase() {
        try {
            System.out.println("Loading teams from database...");
            showTeamListHeader();
            allTeams = crudTeam.getAll();
            System.out.println("Loaded " + allTeams.size() + " teams");
            displayTeams(allTeams);
        } catch (Exception e) {
            System.err.println("Error loading teams from database");
            e.printStackTrace();
        }
    }

    private void displayTeams(List<Team> teams) {
        teamCardsContainer.getChildren().clear();
        for (Team team : teams) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/TeamCard.fxml"));
                Parent card = loader.load();
                TeamCardController cc = loader.getController();
                cc.setTeam(team);
                if (mainContentContainer != null) cc.setContentContainer(mainContentContainer);
                if (rootStackPane != null) cc.setRootStackPane(rootStackPane);
                if (contentArea != null) cc.setContentArea(contentArea);
                cc.setOnUpdateCallback(this::loadTeamsFromDatabase);
                // Pass hub reference so card can open edit drawer
                cc.setTeamHubController(this);
                teamCardsContainer.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("Error creating team card for: " + team.getName());
                e.printStackTrace();
            }
        }
    }

    public void addTeamCard(Parent card) { teamCardsContainer.getChildren().add(card); }
    public void clearTeams() { teamCardsContainer.getChildren().clear(); }
}
