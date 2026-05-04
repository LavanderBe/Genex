package Genex.Controllers.Team;

import Genex.entities.Game;
import Genex.entities.Team;
import Genex.services.CrudGame;
import Genex.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Consumer;

public class AddTeamModalController {

    @FXML private Label modalTitle;
    @FXML private TextField txtName;
    @FXML private ChoiceBox<Game> choiceGame;
    @FXML private TextField txtContact;
    @FXML private TextField txtLogoFileName;
    @FXML private ChoiceBox<Team.Status> choiceStatus;

    // Error labels
    @FXML private Label errorName;
    @FXML private Label errorGameId;
    @FXML private Label errorContact;

    private Consumer<Team> onSaveCallback;
    private Runnable onCloseCallback;
    private Team teamToEdit;
    private String logoImagePath;

    private CrudGame crudGame = new CrudGame();

    @FXML
    public void initialize() {
        System.out.println("AddTeamModalController initialized");

        // Set default title
        if (modalTitle != null) {
            modalTitle.setText("NOUVELLE ÉQUIPE");
        }

        // Load games
        loadGames();

        // Setup status choice box
        setupStatusChoiceBox();

        // Setup validation listeners
        setupValidation();
    }

    private void loadGames() {
        try {
            List<Game> games = crudGame.getgames();
            choiceGame.getItems().addAll(games);

            // Set custom string converter
            choiceGame.setConverter(new StringConverter<Game>() {
                @Override
                public String toString(Game game) {
                    return game != null ? game.getNom() : "";
                }

                @Override
                public Game fromString(String string) {
                    return null;
                }
            });

            if (!games.isEmpty()) {
                choiceGame.setValue(games.get(0));
            }
        } catch (Exception e) {
            System.err.println("Error loading games: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupStatusChoiceBox() {
        choiceStatus.getItems().addAll(Team.Status.values());
        choiceStatus.setValue(Team.Status.ACTIVE);
    }

    private void setupValidation() {
        txtName.textProperty().addListener((obs, old, val) -> hideError(errorName));
        choiceGame.valueProperty().addListener((obs, old, val) -> hideError(errorGameId));
        txtContact.textProperty().addListener((obs, old, val) -> hideError(errorContact));
    }

    public void setTeam(Team team) {
        this.teamToEdit = team;

        // Change title
        if (modalTitle != null) {
            modalTitle.setText("MODIFIER L'ÉQUIPE");
        }

        // Fill form
        txtName.setText(team.getName());
        txtContact.setText(team.getContact() != null ? team.getContact() : "");

        // Select game
        if (team.getGameId() != null) {
            for (Game g : choiceGame.getItems()) {
                if (g.getId().equals(team.getGameId())) {
                    choiceGame.setValue(g);
                    break;
                }
            }
        }

        // Select status
        if (team.getStatus() != null) {
            choiceStatus.setValue(team.getStatus());
        }

        // Logo
        if (team.getLogoImage() != null && !team.getLogoImage().isEmpty()) {
            logoImagePath = team.getLogoImage();
            txtLogoFileName.setText(Paths.get(team.getLogoImage()).getFileName().toString());
        }
    }

    public void setOnSaveCallback(Consumer<Team> callback) {
        this.onSaveCallback = callback;
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    private void closeModal() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    @FXML
    private void handleUploadLogo() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir un logo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        
        // Get stage from any node
        Stage stage = (Stage) txtName.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        
        if (file != null) {
            try {
                Path dir = Paths.get("uploads", "team-logos");
                Files.createDirectories(dir);
                String fileName = System.currentTimeMillis() + "_" + file.getName();
                Files.copy(file.toPath(), dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                logoImagePath = "uploads/team-logos/" + fileName;
                txtLogoFileName.setText(file.getName());
            } catch (Exception e) {
                txtLogoFileName.setText("Erreur upload");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void saveTeam() {
        if (!validateForm()) {
            return;
        }

        try {
            Team team = teamToEdit != null ? teamToEdit : new Team();

            team.setName(txtName.getText().trim());
            
            Game selectedGame = choiceGame.getValue();
            if (selectedGame != null) {
                team.setGameId(selectedGame.getId());
            }
            
            team.setContact(txtContact.getText().trim());
            team.setLogoImage(logoImagePath);
            team.setStatus(choiceStatus.getValue());

            if (teamToEdit == null) {
                String userId = SessionManager.getInstance().getCurrentUserId();
                team.setCreatedBy(userId);
            }

            System.out.println("Team saved: " + team.getName());

            // Call callback
            if (onSaveCallback != null) {
                onSaveCallback.accept(team);
            }

        } catch (Exception e) {
            System.err.println("Error saving team");
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        if (txtName.getText().trim().isEmpty()) {
            showError(errorName, "Le nom est requis");
            valid = false;
        }

        if (choiceGame.getValue() == null) {
            showError(errorGameId, "Le jeu est requis");
            valid = false;
        }

        if (txtContact.getText().trim().isEmpty()) {
            showError(errorContact, "Le contact est requis");
            valid = false;
        }

        return valid;
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
