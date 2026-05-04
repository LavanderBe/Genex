package Genex.Controllers.Team;

import Genex.entities.Game;
import Genex.entities.Team;
import Genex.services.CrudGame;
import Genex.services.CrudTeam;
import Genex.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class TeamHubController {

    // ── Left panel ──────────────────────────────────────────────────
    @FXML private StackPane rootStackPane;
    @FXML private VBox contentArea;
    @FXML private TextField searchField;
    @FXML private Button btnAddTeam;
    @FXML private FlowPane teamCardsContainer;

    // ── Right form panel ────────────────────────────────────────────
    @FXML private VBox formPanel;
    @FXML private Label formPanelTitle;
    @FXML private TextField txtName;
    @FXML private ChoiceBox<Game> choiceGame;
    @FXML private TextField txtContact;
    @FXML private TextField txtLogoFileName;
    @FXML private ChoiceBox<Team.Status> choiceStatus;
    @FXML private Button btnSave;
    @FXML private Label errorName;
    @FXML private Label errorGameId;
    @FXML private Label errorContact;

    // ── State ───────────────────────────────────────────────────────
    private CrudTeam crudTeam;
    private List<Team> allTeams;
    private Team teamToEdit;
    private String logoImagePath;
    private javafx.scene.layout.Pane mainContentContainer;

    @FXML
    public void initialize() {
        crudTeam = new CrudTeam();
        setupSearchListener();
        loadGames();
        setupStatusChoiceBox();
        loadTeamsFromDatabase();
    }

    // ── Content container (set by MainController / Dashboard) ───────
    public void setContentContainer(javafx.scene.layout.Pane contentContainer) {
        this.mainContentContainer = contentContainer;
        if (allTeams != null && !allTeams.isEmpty()) displayTeams(allTeams);
    }

    // ── Form panel toggle ────────────────────────────────────────────
    @FXML
    private void toggleFormPanel() {
        boolean nowVisible = !formPanel.isVisible();
        formPanel.setVisible(nowVisible);
        formPanel.setManaged(nowVisible);
        if (!nowVisible) clearForm();
    }

    @FXML
    private void cancelForm() {
        formPanel.setVisible(false);
        formPanel.setManaged(false);
        clearForm();
    }

    private void clearForm() {
        teamToEdit = null;
        logoImagePath = null;
        if (txtName != null) txtName.clear();
        if (txtContact != null) txtContact.clear();
        if (txtLogoFileName != null) txtLogoFileName.clear();
        if (choiceStatus != null && !choiceStatus.getItems().isEmpty())
            choiceStatus.setValue(Team.Status.ACTIVE);
        if (choiceGame != null && !choiceGame.getItems().isEmpty())
            choiceGame.setValue(choiceGame.getItems().get(0));
        hideError(errorName);
        hideError(errorGameId);
        hideError(errorContact);
        if (formPanelTitle != null) formPanelTitle.setText("Nouvelle équipe");
        if (btnSave != null) btnSave.setText("ENREGISTRER");
    }

    // ── Called by TeamCardController to open edit mode ───────────────
    public void openEditForm(Team team) {
        teamToEdit = team;
        formPanelTitle.setText("Modifier l'équipe");
        btnSave.setText("ENREGISTRER");

        txtName.setText(team.getName());
        txtContact.setText(team.getContact() != null ? team.getContact() : "");

        if (team.getGameId() != null) {
            for (Game g : choiceGame.getItems()) {
                if (g.getId().equals(team.getGameId())) { choiceGame.setValue(g); break; }
            }
        }
        if (team.getStatus() != null) choiceStatus.setValue(team.getStatus());
        if (team.getLogoImage() != null && !team.getLogoImage().isEmpty()) {
            logoImagePath = team.getLogoImage();
            txtLogoFileName.setText(Paths.get(team.getLogoImage()).getFileName().toString());
        }

        formPanel.setVisible(true);
        formPanel.setManaged(true);
    }

    // ── Logo picker ──────────────────────────────────────────────────
    @FXML
    private void handleUploadLogo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir un logo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Stage stage = (Stage) btnAddTeam.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            try {
                Path dir = Paths.get("uploads", "team-logos");
                Files.createDirectories(dir);
                String fileName = System.currentTimeMillis() + "_" + file.getName();
                Files.copy(file.toPath(), dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                logoImagePath = "uploads/team-logos/" + fileName;
                txtLogoFileName.setText(file.getName());
            } catch (IOException e) {
                txtLogoFileName.setText("Erreur upload");
                e.printStackTrace();
            }
        }
    }

    // ── Save ─────────────────────────────────────────────────────────
    @FXML
    private void saveTeam() {
        if (!validateForm()) return;
        try {
            Team team = teamToEdit != null ? teamToEdit : new Team();
            team.setName(txtName.getText().trim());
            Game selectedGame = choiceGame.getValue();
            if (selectedGame != null) team.setGameId(selectedGame.getId());
            team.setContact(txtContact.getText().trim());
            team.setLogoImage(logoImagePath);
            team.setStatus(choiceStatus.getValue());

            if (teamToEdit == null) {
                String userId = SessionManager.getInstance().getCurrentUserId();
                team.setCreatedBy(userId);
                crudTeam.addEntity(team);
            } else {
                crudTeam.updateEntity(team, teamToEdit.getId());
            }

            cancelForm();
            loadTeamsFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        if (txtName.getText().trim().isEmpty()) { showError(errorName, "Le nom est requis"); valid = false; }
        if (choiceGame.getValue() == null) { showError(errorGameId, "Le jeu est requis"); valid = false; }
        if (txtContact.getText().trim().isEmpty()) { showError(errorContact, "Le contact est requis"); valid = false; }
        return valid;
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
    private void loadGames() {
        if (choiceGame == null) return;
        try {
            List<Game> games = new CrudGame().getgames();
            choiceGame.getItems().addAll(games);
            choiceGame.setConverter(new StringConverter<>() {
                @Override public String toString(Game g) { return g != null ? g.getNom() : ""; }
                @Override public Game fromString(String s) { return null; }
            });
            if (!games.isEmpty()) choiceGame.setValue(games.get(0));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupStatusChoiceBox() {
        if (choiceStatus == null) return;
        choiceStatus.getItems().addAll(Team.Status.values());
        choiceStatus.setValue(Team.Status.ACTIVE);
    }

    private void loadTeamsFromDatabase() {
        try {
            allTeams = crudTeam.getAll();
            displayTeams(allTeams);
        } catch (Exception e) { e.printStackTrace(); }
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
                // Pass hub reference so card can open edit form inline
                cc.setTeamHubController(this);
                teamCardsContainer.getChildren().add(card);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private void showError(Label lbl, String msg) {
        if (lbl != null) { lbl.setText(msg); lbl.setVisible(true); lbl.setManaged(true); }
    }

    private void hideError(Label lbl) {
        if (lbl != null) { lbl.setVisible(false); lbl.setManaged(false); }
    }

    public void addTeamCard(Parent card) { teamCardsContainer.getChildren().add(card); }
    public void clearTeams() { teamCardsContainer.getChildren().clear(); }
}
