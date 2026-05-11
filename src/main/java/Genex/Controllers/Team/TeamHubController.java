package Genex.Controllers.Team;

import Genex.entities.Team;
import Genex.services.CrudTeam;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
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
    @FXML private FlowPane teamCardsContainer;

    private CrudTeam crudTeam;
    private List<Team> allTeams;
    private javafx.scene.layout.Pane mainContentContainer;

    @FXML
    public void initialize() {
        System.out.println("TeamHubController initialized");
        
        crudTeam = new CrudTeam();
        setupSearchListener();
        loadTeamsFromDatabase();
    }

    // ── Content container (set by MainController / Dashboard) ───────
    public void setContentContainer(javafx.scene.layout.Pane contentContainer) {
        this.mainContentContainer = contentContainer;
        if (allTeams != null && !allTeams.isEmpty()) displayTeams(allTeams);
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

    // ── Search ───────────────────────────────────────────────────────
    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, old, val) -> filterTeams(val));
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
