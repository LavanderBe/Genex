package Genex.Controllers.Team;

import Genex.entities.Team;
import Genex.services.CrudTeam;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;

public class TeamHubController {

    @FXML
    private StackPane rootStackPane;

    @FXML
    private VBox contentArea;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnAddTeam;

    @FXML
    private FlowPane teamCardsContainer;

    private CrudTeam crudTeam;
    private List<Team> allTeams;
    private javafx.scene.layout.Pane mainContentContainer; // Reference to main content area

    @FXML
    public void initialize() {
        System.out.println("TeamHubController initialized");

        // Initialize CRUD service
        crudTeam = new CrudTeam();

        // Setup search listener
        setupSearchListener();

        // Load teams from database
        loadTeamsFromDatabase();
    }

    /**
     * Set the main content container from Dashboard
     */
    public void setContentContainer(javafx.scene.layout.Pane contentContainer) {
        this.mainContentContainer = contentContainer;
        System.out.println("Content container set in TeamHubController");

        // Reload teams to pass the container to cards
        if (allTeams != null && !allTeams.isEmpty()) {
            displayTeams(allTeams);
        }
    }

    private void findMainContentContainer() {
        // This method is no longer needed but kept for backward compatibility
        if (mainContentContainer != null) {
            return; // Already set by MainController
        }

        try {
            // Navigate up the scene graph to find the main content container
            javafx.scene.Node node = teamCardsContainer;
            while (node != null) {
                if (node instanceof StackPane && node.getId() != null && node.getId().equals("contentContainer")) {
                    mainContentContainer = (StackPane) node;
                    System.out.println("Found main content container!");
                    break;
                }
                node = node.getParent();
            }
            if (mainContentContainer == null) {
                System.out.println("Main content container not found - will use fallback navigation");
            }
        } catch (Exception e) {
            System.err.println("Error finding main content container: " + e.getMessage());
        }
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTeams(newValue);
        });
    }

    private void filterTeams(String searchText) {
        teamCardsContainer.getChildren().clear();

        if (searchText == null || searchText.trim().isEmpty()) {
            // Show all teams
            displayTeams(allTeams);
        } else {
            // Filter teams by name or contact
            String search = searchText.toLowerCase();
            List<Team> filtered = allTeams.stream()
                    .filter(t -> t.getName().toLowerCase().contains(search) ||
                            (t.getContact() != null && t.getContact().toLowerCase().contains(search)))
                    .toList();
            displayTeams(filtered);
        }
    }

    private void updateEmptyState() {
        // Empty state handling removed to match Player interface
        // Teams will always show in the grid, even if empty
    }

    @FXML
    private void openAddTeamModal() {
        System.out.println("Opening Add Team Modal...");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTeamModal.fxml"));
            Parent addTeamForm = loader.load();

            // 1. Apply Blur effect to the background
            javafx.scene.effect.GaussianBlur blur = new javafx.scene.effect.GaussianBlur(15);
            contentArea.setEffect(blur);
            contentArea.setDisable(true); // Prevent clicking background items

            // 2. Wrap the form in a darkening overlay (dimmer)
            VBox overlay = new VBox(addTeamForm);
            overlay.setAlignment(javafx.geometry.Pos.CENTER);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Dim background

            // 3. Add to the stack
            rootStackPane.getChildren().add(overlay);

            // 4. Pass a "Close" callback to the AddTeamModalController
            AddTeamModalController controller = loader.getController();
            controller.setOnSaveCallback(newTeam -> {
                System.out.println("Saving new team: " + newTeam.getName());

                // Save to database
                crudTeam.addEntity(newTeam);

                // Reload teams
                loadTeamsFromDatabase();
            });

            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(overlay); // Remove form
                contentArea.setEffect(null);                // Remove blur
                contentArea.setDisable(false);              // Re-enable content
            });

        } catch (Exception e) {
            System.err.println("Error opening add team modal");
            e.printStackTrace();
        }
    }

    private void loadTeamsFromDatabase() {
        try {
            System.out.println("Loading teams from database...");

            // Get all teams from database
            allTeams = crudTeam.getAll();

            System.out.println("Loaded " + allTeams.size() + " teams");

            // Display teams
            displayTeams(allTeams);

        } catch (Exception e) {
            System.err.println("Error loading teams from database");
            e.printStackTrace();
        }
    }

    private void displayTeams(List<Team> teams) {
        teamCardsContainer.getChildren().clear();

        System.out.println("=== Displaying " + teams.size() + " teams ===");
        System.out.println("Main content container: " + (mainContentContainer != null ? "AVAILABLE" : "NULL"));

        for (Team team : teams) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/TeamCard.fxml"));
                Parent card = loader.load();

                TeamCardController cardController = loader.getController();
                cardController.setTeam(team);

                // Pass the main content container reference
                if (mainContentContainer != null) {
                    cardController.setContentContainer(mainContentContainer);
                    System.out.println("✓ Passed content container to card: " + team.getName());
                } else {
                    System.out.println("✗ No content container to pass to card: " + team.getName());
                }

                // Pass rootStackPane and contentArea for overlay modals
                if (rootStackPane != null) {
                    cardController.setRootStackPane(rootStackPane);
                }
                if (contentArea != null) {
                    cardController.setContentArea(contentArea);
                }

                // Set callback to reload teams when card is updated/deleted
                cardController.setOnUpdateCallback(this::loadTeamsFromDatabase);

                teamCardsContainer.getChildren().add(card);

            } catch (Exception e) {
                System.err.println("Error creating team card for: " + team.getName());
                e.printStackTrace();
            }
        }

        updateEmptyState();
        System.out.println("=== Teams displayed ===");
    }

    public void addTeamCard(Parent card) {
        teamCardsContainer.getChildren().add(card);
        updateEmptyState();
    }

    public void clearTeams() {
        teamCardsContainer.getChildren().clear();
        updateEmptyState();
    }
}