package Genex.Controllers.Team;

import Genex.entities.Team;
import Genex.entities.TrainingSession;
import Genex.services.CrudTrainingSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.text.SimpleDateFormat;
import java.util.List;

public class TeamDetailController {

    @FXML
    private Button btnBack;

    @FXML
    private Text teamNameTitle;

    @FXML
    private Text teamTag;

    @FXML
    private Text teamGame;

    @FXML
    private Text teamType;

    @FXML
    private Text teamFounded;

    @FXML
    private Button btnAddSession;

    @FXML
    private FlowPane sessionsContainer;

    @FXML
    private VBox emptyState;

    private Team team;
    private CrudTrainingSession crudTrainingSession;

    @FXML
    public void initialize() {
        System.out.println("TeamDetailController initialized");
        crudTrainingSession = new CrudTrainingSession();
    }

    public void setTeam(Team team) {
        this.team = team;
        updateTeamInfo();
        loadTrainingSessions();
    }

    private void updateTeamInfo() {
        if (team != null) {
            teamNameTitle.setText(team.getName());

            // Display status
            if (team.getStatus() != null) {
                teamTag.setText("[" + team.getStatus().name() + "]");
            } else {
                teamTag.setText("");
            }

            // Display game ID
            if (team.getGameId() != null) {
                teamGame.setText("Game ID: " + team.getGameId());
            } else {
                teamGame.setText("");
            }

            // Display contact
            if (team.getContact() != null) {
                teamType.setText("Contact: " + team.getContact());
            } else {
                teamType.setText("");
            }

            // Display created at
            if (team.getCreatedAt() != null) {
                teamFounded.setText("Created: " + team.getCreatedAt().toLocalDate().toString());
            } else {
                teamFounded.setText("");
            }
        }
    }

    private void loadTrainingSessions() {
        try {
            System.out.println("Loading training sessions for team: " + team.getId());

            List<TrainingSession> sessions = crudTrainingSession.getSessionsByTeam(team.getId());

            System.out.println("Loaded " + sessions.size() + " training sessions");

            displaySessions(sessions);

        } catch (Exception e) {
            System.err.println("Error loading training sessions");
            e.printStackTrace();
        }
    }

    private void displaySessions(List<TrainingSession> sessions) {
        sessionsContainer.getChildren().clear();

        for (TrainingSession session : sessions) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/TrainingSessionCard.fxml"));
                Parent card = loader.load();

                TrainingSessionCardController cardController = loader.getController();
                cardController.setSession(session);

                // Set callback to reload sessions when card is updated/deleted
                cardController.setOnUpdateCallback(this::loadTrainingSessions);

                sessionsContainer.getChildren().add(card);

            } catch (Exception e) {
                System.err.println("Error creating session card for: " + session.getTitle());
                e.printStackTrace();
            }
        }

        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean isEmpty = sessionsContainer.getChildren().isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);
    }

    @FXML
    private void handleBack() {
        System.out.println("Navigating back to Team Hub...");

        try {
            // Find the main content container
            javafx.scene.Node node = btnBack;
            StackPane contentContainer = null;

            while (node != null) {
                if (node instanceof StackPane && node.getId() != null && node.getId().equals("contentContainer")) {
                    contentContainer = (StackPane) node;
                    break;
                }
                node = node.getParent();
            }

            if (contentContainer != null) {
                // Load Team Hub back into the content container
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/TeamHub.fxml"));
                Parent teamHub = loader.load();

                // IMPORTANT: Pass the content container to the new TeamHubController
                TeamHubController hubController = loader.getController();
                hubController.setContentContainer(contentContainer);

                contentContainer.getChildren().clear();
                contentContainer.getChildren().add(teamHub);

                System.out.println("Successfully navigated back to Team Hub");
            } else {
                // Fallback: close the window (old behavior)
                System.out.println("Content container not found, closing window...");
                Stage stage = (Stage) btnBack.getScene().getWindow();
                stage.close();
            }

        } catch (Exception e) {
            System.err.println("Error navigating back to Team Hub");
            e.printStackTrace();

            // Fallback: try to close the window
            try {
                Stage stage = (Stage) btnBack.getScene().getWindow();
                stage.close();
            } catch (Exception ex) {
                System.err.println("Could not close window either");
            }
        }
    }

    @FXML
    private void openAddSessionModal() {
        System.out.println("Opening Add Training Session Modal...");

        if (team == null) {
            System.err.println("Cannot add session: team is null");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTrainingSessionModal.fxml"));
            Parent modalRoot = loader.load();

            // Create modal stage
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle("Ajouter une Session");

            Scene scene = new Scene(modalRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modalStage.setScene(scene);

            // Get controller and set team ID
            AddTrainingSessionModalController controller = loader.getController();
            controller.setTeamId(team.getId());
            controller.setOnSaveCallback(newSession -> {
                System.out.println("Saving new session: " + newSession.getTitle());

                // Save to database
                crudTrainingSession.addSession(newSession);

                // Reload sessions
                loadTrainingSessions();

                modalStage.close();
            });

            modalStage.showAndWait();

        } catch (Exception e) {
            System.err.println("Error opening add session modal");
            e.printStackTrace();
        }
    }
}