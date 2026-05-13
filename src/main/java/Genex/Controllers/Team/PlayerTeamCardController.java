package Genex.Controllers.Team;

import Genex.entities.Game;
import Genex.entities.Team;
import Genex.services.CrudGame;
import Genex.services.CrudTeamMember;
import Genex.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

import java.io.File;

public class PlayerTeamCardController {

    @FXML private Text teamName;
    @FXML private Text teamGame;
    @FXML private Text teamContact;
    @FXML private Text memberCount;
    @FXML private ImageView teamLogo;
    @FXML private Text teamIconFallback;
    @FXML private Button btnView;
    @FXML private Button btnJoin;

    private Team team;
    private PlayerTeamBrowserController browserController;
    private CrudTeamMember crudTeamMember;
    private String currentUserId;

    @FXML
    public void initialize() {
        crudTeamMember = new CrudTeamMember();
        currentUserId = SessionManager.getInstance().getCurrentUserId();
    }

    public void setTeam(Team team) {
        this.team = team;
        populateFields();
        loadLogo();
        updateJoinButton();
    }

    public void setBrowserController(PlayerTeamBrowserController controller) {
        this.browserController = controller;
    }

    /**
     * Hides the join button when in read-only mode (Autres Équipes tab).
     */
    public void setReadOnly(boolean readOnly) {
        btnJoin.setVisible(!readOnly);
        btnJoin.setManaged(!readOnly);
    }

    // ── Private helpers ──────────────────────────────────────────────

    private void populateFields() {
        if (team == null) return;
        teamName.setText(team.getName());
        teamContact.setText(team.getContact() != null ? team.getContact() : "—");

        // Resolve game name
        if (team.getGameId() != null) {
            String name = getGameNameById(team.getGameId());
            teamGame.setText(name != null ? name : "—");
        } else {
            teamGame.setText("—");
        }

        // Member count badge
        int count = crudTeamMember.getMemberCount(team.getId());
        memberCount.setText(count + " / " + CrudTeamMember.MAX_MEMBERS);
        memberCount.setStyle("-fx-fill: #ff4444; -fx-font-weight: bold; -fx-font-size: 13px;");
    }

    private void loadLogo() {
        if (team != null && team.getLogoImage() != null && !team.getLogoImage().isEmpty()) {
            try {
                File f = new File(team.getLogoImage());
                if (f.exists()) {
                    teamLogo.setImage(new Image(f.toURI().toString()));
                    teamLogo.setVisible(true);
                    teamIconFallback.setVisible(false);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error loading logo: " + e.getMessage());
            }
        }
        teamLogo.setVisible(false);
        teamIconFallback.setVisible(true);
    }

    private void updateJoinButton() {
        if (team == null) return;

        boolean alreadyMember = crudTeamMember.isMember(team.getId(), currentUserId);
        int count = crudTeamMember.getMemberCount(team.getId());
        boolean isCreator = team.getCreatedBy() != null && team.getCreatedBy().equals(currentUserId);

        if (alreadyMember) {
            btnJoin.setText("Membre");
            btnJoin.setDisable(true);
        } else if (isCreator) {
            btnJoin.setText("Votre équipe");
            btnJoin.setDisable(true);
        } else if (count >= CrudTeamMember.MAX_MEMBERS) {
            btnJoin.setText("Complet");
            btnJoin.setDisable(true);
        } else {
            btnJoin.setText("Rejoindre");
            btnJoin.setDisable(false);
        }
    }

    private String getGameNameById(String gameId) {
        try {
            for (Game g : new CrudGame().getgames()) {
                if (g.getId() != null && g.getId().equals(gameId)) return g.getNom();
            }
        } catch (Exception e) {
            System.err.println("Error fetching game name: " + e.getMessage());
        }
        return null;
    }

    // ── FXML handlers ────────────────────────────────────────────────

    @FXML
    private void handleView() {
        if (browserController != null && team != null) {
            // Check if the current player has a team
            String currentUserId = SessionManager.getInstance().getCurrentUserId();
            boolean playerHasTeam = crudTeamMember.getTeamByPlayer(currentUserId) != null;
            if (playerHasTeam) {
                browserController.showTeamDetail(team);
            } else {
                browserController.showTeamDetailViewOnly(team);
            }
        }
    }

    @FXML
    private void handleJoin() {
        if (team == null || currentUserId == null) return;
        try {
            crudTeamMember.addMember(team.getId(), currentUserId);
            // After joining, navigate directly to the team detail view
            if (browserController != null) {
                browserController.showTeamDetail(team);
            }
        } catch (IllegalStateException e) {
            // Team is full
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Équipe complète");
            alert.setHeaderText(null);
            alert.setContentText("Cette équipe est complète (5/5 membres).");
            alert.showAndWait();
            updateJoinButton(); // refresh button state
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue lors de la tentative de rejoindre l'équipe.");
            alert.showAndWait();
            System.err.println("Error joining team: " + e.getMessage());
        }
    }
}
