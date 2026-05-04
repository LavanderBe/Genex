package Genex.Controllers.Team;

import Genex.entities.Game;
import Genex.entities.Team;
import Genex.entities.TrainingSession;
import Genex.services.CrudGame;
import Genex.services.CrudTrainingSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

public class TeamDetailController {

    @FXML private StackPane rootStackPane;
    @FXML private VBox contentArea;
    @FXML private Button btnBack;
    @FXML private Label teamNameTitle;
    @FXML private Text teamTag;
    @FXML private Text teamGame;
    @FXML private Text teamType;
    @FXML private Text teamFounded;
    @FXML private Button btnAddSession;
    @FXML private VBox sessionsContainer;

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

    // ── Open Add Session Modal (Tournament pattern) ──────────────────
    @FXML
    private void openAddSessionModal() {
        try {
            System.out.println("Opening Add Training Session Modal...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTrainingSessionModal.fxml"));
            StackPane modalOverlay = loader.load();

            // Add modal overlay to the stack
            rootStackPane.getChildren().add(modalOverlay);

            // Get controller and set callbacks
            AddTrainingSessionModalController controller = loader.getController();
            controller.setTeamId(team.getId());
            
            controller.setOnSaveCallback(session -> {
                System.out.println("Saving session: " + session.getTitle());

                // Save to database
                if (session.getId() == null) {
                    crudTrainingSession.addSession(session);
                } else {
                    crudTrainingSession.updateSession(session);
                }

                // Remove modal overlay and reload
                rootStackPane.getChildren().remove(modalOverlay);
                loadTrainingSessions();
            });

            // Handle close without saving
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(modalOverlay);
            });

        } catch (Exception e) {
            System.err.println("Error opening Add Training Session Modal");
            e.printStackTrace();
        }
    }

    // ── Called by TrainingSessionCardController to open edit modal ────
    public void openEditSessionModal(TrainingSession session) {
        try {
            System.out.println("Opening Edit Training Session Modal...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTrainingSessionModal.fxml"));
            StackPane modalOverlay = loader.load();

            // Add modal overlay to the stack
            rootStackPane.getChildren().add(modalOverlay);

            // Get controller and set session data
            AddTrainingSessionModalController controller = loader.getController();
            controller.setSession(session);
            
            controller.setOnSaveCallback(updatedSession -> {
                System.out.println("Updating session: " + updatedSession.getTitle());

                // Update in database
                crudTrainingSession.updateSession(updatedSession);

                // Remove modal overlay and reload
                rootStackPane.getChildren().remove(modalOverlay);
                loadTrainingSessions();
            });

            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(modalOverlay);
            });

        } catch (Exception e) {
            System.err.println("Error opening Edit Training Session Modal");
            e.printStackTrace();
        }
    }

    // ── Team info display ────────────────────────────────────────────
    private void updateTeamInfo() {
        if (team == null) return;
        teamNameTitle.setText(team.getName());
        teamTag.setText(team.getStatus() != null ? "[" + team.getStatus().name() + "]" : "");
        if (team.getGameId() != null) {
            String name = getGameNameById(team.getGameId());
            teamGame.setText(name != null ? name : "Unknown Game");
        }
        teamType.setText(team.getContact() != null ? "Contact: " + team.getContact() : "");
        teamFounded.setText(team.getCreatedAt() != null
                ? "Created: " + team.getCreatedAt().toLocalDate() : "");
    }

    private String getGameNameById(String gameId) {
        try {
            for (Game g : new CrudGame().getgames())
                if (g.getId() != null && g.getId().equals(gameId)) return g.getNom();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ── Sessions display ─────────────────────────────────────────────
    private void loadTrainingSessions() {
        try {
            displaySessions(crudTrainingSession.getSessionsByTeam(team.getId()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void displaySessions(List<TrainingSession> sessions) {
        sessionsContainer.getChildren().clear();
        for (TrainingSession session : sessions) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/TrainingSessionCard.fxml"));
                Parent card = loader.load();
                TrainingSessionCardController cc = loader.getController();
                cc.setSession(session);
                if (rootStackPane != null) cc.setRootStackPane(rootStackPane);
                if (contentArea != null)   cc.setContentArea(contentArea);
                cc.setOnUpdateCallback(this::loadTrainingSessions);
                // Pass detail controller so card can open edit modal
                cc.setTeamDetailController(this);
                sessionsContainer.getChildren().add(card);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ── Back navigation ──────────────────────────────────────────────
    @FXML
    private void handleBack() {
        try {
            javafx.scene.Node node = btnBack;
            javafx.scene.layout.Pane container = null;
            while (node != null) {
                if (node instanceof javafx.scene.layout.AnchorPane) {
                    container = (javafx.scene.layout.Pane) node; break;
                }
                node = node.getParent();
            }
            if (container != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/TeamHub.fxml"));
                Parent hub = loader.load();
                TeamHubController hc = loader.getController();
                hc.setContentContainer(container);
                container.getChildren().clear();
                container.getChildren().add(hub);
                if (container instanceof javafx.scene.layout.AnchorPane ap) {
                    javafx.scene.layout.AnchorPane.setTopAnchor(hub, 0.0);
                    javafx.scene.layout.AnchorPane.setBottomAnchor(hub, 0.0);
                    javafx.scene.layout.AnchorPane.setLeftAnchor(hub, 0.0);
                    javafx.scene.layout.AnchorPane.setRightAnchor(hub, 0.0);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
