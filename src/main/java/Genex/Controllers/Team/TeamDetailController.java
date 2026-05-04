package Genex.Controllers.Team;

import Genex.entities.Game;
import Genex.entities.Team;
import Genex.entities.TrainingSession;
import Genex.services.CrudGame;
import Genex.services.CrudTrainingSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TeamDetailController {

    // ── Left panel ──────────────────────────────────────────────────
    @FXML private StackPane rootStackPane;
    @FXML private VBox contentArea;
    @FXML private Button btnBack;
    @FXML private Label teamNameTitle;
    @FXML private Text teamTag;
    @FXML private Text teamGame;
    @FXML private Text teamType;
    @FXML private Text teamFounded;
    @FXML private Button btnAddSession;
    @FXML private FlowPane sessionsContainer;

    // ── Right form panel ────────────────────────────────────────────
    @FXML private VBox sessionFormPanel;
    @FXML private Label sessionFormTitle;
    @FXML private TextField txtTitle;
    @FXML private ChoiceBox<TrainingSession.Type> choiceType;
    @FXML private ChoiceBox<TrainingSession.Status> choiceStatus;
    @FXML private DatePicker dateSession;
    @FXML private TextField txtStartTime;
    @FXML private TextField txtEndTime;
    @FXML private TextField txtLocation;
    @FXML private TextArea txtNotes;
    @FXML private Button btnSaveSession;

    // ── State ───────────────────────────────────────────────────────
    private Team team;
    private TrainingSession sessionToEdit;
    private CrudTrainingSession crudTrainingSession;

    @FXML
    public void initialize() {
        crudTrainingSession = new CrudTrainingSession();
        setupChoiceBoxes();
        setupValidationListeners();
    }

    public void setTeam(Team team) {
        this.team = team;
        updateTeamInfo();
        loadTrainingSessions();
    }

    // ── Form panel toggle ────────────────────────────────────────────
    @FXML
    private void toggleSessionForm() {
        boolean nowVisible = !sessionFormPanel.isVisible();
        sessionFormPanel.setVisible(nowVisible);
        sessionFormPanel.setManaged(nowVisible);
        if (!nowVisible) clearSessionForm();
    }

    @FXML
    private void cancelSessionForm() {
        sessionFormPanel.setVisible(false);
        sessionFormPanel.setManaged(false);
        clearSessionForm();
    }

    private void clearSessionForm() {
        sessionToEdit = null;
        if (txtTitle != null) txtTitle.clear();
        if (txtStartTime != null) txtStartTime.clear();
        if (txtEndTime != null) txtEndTime.clear();
        if (txtLocation != null) txtLocation.clear();
        if (txtNotes != null) txtNotes.clear();
        if (dateSession != null) dateSession.setValue(null);
        if (choiceType != null && !choiceType.getItems().isEmpty())
            choiceType.setValue(TrainingSession.Type.TEAM_PRACTICE);
        if (choiceStatus != null && !choiceStatus.getItems().isEmpty())
            choiceStatus.setValue(TrainingSession.Status.PLANNED);
        if (sessionFormTitle != null) sessionFormTitle.setText("Nouvelle session");
        if (btnSaveSession != null) btnSaveSession.setText("ENREGISTRER");
    }

    // ── Called by TrainingSessionCardController to open edit mode ────
    public void openEditSessionForm(TrainingSession session) {
        sessionToEdit = session;
        sessionFormTitle.setText("Modifier la session");
        btnSaveSession.setText("ENREGISTRER");

        txtTitle.setText(session.getTitle());
        if (session.getType() != null) choiceType.setValue(session.getType());
        if (session.getStatus() != null) choiceStatus.setValue(session.getStatus());
        if (session.getSessionDatetime() != null)
            dateSession.setValue(session.getSessionDatetime().toLocalDate());
        if (session.getStartTime() != null)
            txtStartTime.setText(session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        if (session.getEndTime() != null)
            txtEndTime.setText(session.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        if (session.getLocation() != null) txtLocation.setText(session.getLocation());
        if (session.getNotes() != null) txtNotes.setText(session.getNotes());

        sessionFormPanel.setVisible(true);
        sessionFormPanel.setManaged(true);
    }

    // ── Time buttons ─────────────────────────────────────────────────
    @FXML private void incrementStartTime() { adjustTime(txtStartTime, 15); }
    @FXML private void decrementStartTime() { adjustTime(txtStartTime, -15); }
    @FXML private void incrementEndTime()   { adjustTime(txtEndTime, 15); }
    @FXML private void decrementEndTime()   { adjustTime(txtEndTime, -15); }

    private void adjustTime(TextField field, int minutes) {
        try {
            String text = field.getText().trim();
            LocalTime t = text.isEmpty()
                    ? LocalTime.now().withSecond(0).withNano(0)
                    : LocalTime.parse(text, DateTimeFormatter.ofPattern("HH:mm"));
            field.setText(t.plusMinutes(minutes).format(DateTimeFormatter.ofPattern("HH:mm")));
        } catch (Exception e) {
            field.setText(LocalTime.now().withSecond(0).withNano(0)
                    .format(DateTimeFormatter.ofPattern("HH:mm")));
        }
    }

    // ── Save session ─────────────────────────────────────────────────
    @FXML
    private void saveSession() {
        if (!validateSessionForm()) return;
        try {
            LocalDate date = dateSession.getValue();
            LocalTime start = LocalTime.parse(txtStartTime.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime end   = LocalTime.parse(txtEndTime.getText().trim(),   DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime dt = LocalDateTime.of(date, start);

            String excludeId = sessionToEdit != null ? sessionToEdit.getId() : null;
            if (crudTrainingSession.hasTimeConflict(team.getId(), dt, start, end, excludeId)) {
                List<TrainingSession> conflicts = crudTrainingSession.getConflictingSessions(team.getId(), dt, start, end, excludeId);
                StringBuilder msg = new StringBuilder("Sessions en conflit:\n");
                for (TrainingSession c : conflicts)
                    msg.append("• ").append(c.getTitle()).append(" (")
                       .append(c.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                       .append(" - ").append(c.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                       .append(")\n");
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Conflit d'horaire");
                alert.setHeaderText("Une session existe déjà à cet horaire");
                alert.setContentText(msg.toString());
                alert.showAndWait();
                return;
            }

            TrainingSession session = sessionToEdit != null ? sessionToEdit : new TrainingSession();
            session.setTitle(txtTitle.getText().trim());
            session.setType(choiceType.getValue());
            session.setSessionDatetime(dt);
            session.setStartTime(start);
            session.setEndTime(end);
            session.setStatus(choiceStatus.getValue());
            session.setLocation(txtLocation.getText().trim());
            session.setNotes(txtNotes.getText().trim());
            session.setTeamId(team.getId());

            if (sessionToEdit == null) crudTrainingSession.addSession(session);
            else                       crudTrainingSession.updateSession(session);

            cancelSessionForm();
            loadTrainingSessions();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean validateSessionForm() {
        boolean valid = true;
        if (txtTitle.getText().trim().isEmpty()) {
            showAlert("Validation", "Le titre est requis");
            valid = false;
        }
        if (dateSession.getValue() == null) {
            showAlert("Validation", "La date est requise");
            valid = false;
        }
        if (txtStartTime.getText().trim().isEmpty()) {
            showAlert("Validation", "L'heure de début est requise");
            valid = false;
        } else {
            try { LocalTime.parse(txtStartTime.getText().trim(), DateTimeFormatter.ofPattern("HH:mm")); }
            catch (Exception e) {
                showAlert("Validation", "Format d'heure invalide (HH:MM)");
                valid = false;
            }
        }
        if (txtEndTime.getText().trim().isEmpty()) {
            showAlert("Validation", "L'heure de fin est requise");
            valid = false;
        } else {
            try {
                LocalTime end = LocalTime.parse(txtEndTime.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                if (!txtStartTime.getText().trim().isEmpty()) {
                    LocalTime start = LocalTime.parse(txtStartTime.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                    if (!end.isAfter(start)) {
                        showAlert("Validation", "L'heure de fin doit être après l'heure de début");
                        valid = false;
                    }
                }
            } catch (Exception e) {
                showAlert("Validation", "Format d'heure invalide (HH:MM)");
                valid = false;
            }
        }
        return valid;
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
                // Pass detail controller so card can open edit form inline
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

    // ── Choice boxes setup ───────────────────────────────────────────
    private void setupChoiceBoxes() {
        if (choiceType != null) {
            choiceType.getItems().addAll(TrainingSession.Type.values());
            choiceType.setValue(TrainingSession.Type.TEAM_PRACTICE);
        }
        if (choiceStatus != null) {
            choiceStatus.getItems().addAll(TrainingSession.Status.values());
            choiceStatus.setValue(TrainingSession.Status.PLANNED);
        }
    }

    private void setupValidationListeners() {
        // No inline validation - using alerts instead
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
