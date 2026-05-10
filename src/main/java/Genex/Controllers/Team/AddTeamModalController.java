package Genex.Controllers.Team;

import Genex.entities.Game;
import Genex.entities.Team;
import Genex.services.CrudGame;
import Genex.services.GeminiImageGeneratorService;
import Genex.utils.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
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
    @FXML private ComboBox<Game> choiceGame;
    @FXML private TextField txtContact;
    @FXML private TextField txtLogoFileName;
    @FXML private TextField txtLogoDescription;
    @FXML private TextField txtJerseyFileName;
    @FXML private ComboBox<Team.Status> choiceStatus;
    @FXML private javafx.scene.control.Button btnCloseModal;
    @FXML private javafx.scene.control.Button btnGenerateLogo;
    @FXML private javafx.scene.control.Button btnGenerateJersey;

    // Error labels
    @FXML private Label errorName;
    @FXML private Label errorGameId;
    @FXML private Label errorContact;

    private Consumer<Team> onSaveCallback;
    private Runnable onCloseCallback;
    private Team teamToEdit;
    private String logoImagePath;
    private String jerseyImagePath;

    private CrudGame crudGame = new CrudGame();
    private GeminiImageGeneratorService aiService = new GeminiImageGeneratorService();

    @FXML
    public void initialize() {
        System.out.println("AddTeamModalController initialized");

        // Set default title
        if (modalTitle != null) {
            modalTitle.setText("NOUVELLE ÉQUIPE");
        }

        // Setup close button
        if (btnCloseModal != null) {
            btnCloseModal.setOnAction(e -> closeModal());
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

        // Jersey
        if (team.getJerseyImage() != null && !team.getJerseyImage().isEmpty()) {
            jerseyImagePath = team.getJerseyImage();
            txtJerseyFileName.setText(Paths.get(team.getJerseyImage()).getFileName().toString());
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
    private void handleUploadJersey() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir un maillot");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        
        Stage stage = (Stage) txtName.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        
        if (file != null) {
            try {
                Path dir = Paths.get("uploads", "team-jerseys");
                Files.createDirectories(dir);
                String fileName = System.currentTimeMillis() + "_" + file.getName();
                Files.copy(file.toPath(), dir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                jerseyImagePath = "uploads/team-jerseys/" + fileName;
                txtJerseyFileName.setText(file.getName());
            } catch (Exception e) {
                txtJerseyFileName.setText("Erreur upload");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleGenerateLogo() {
        String teamName = txtName.getText().trim();
        Game selectedGame = choiceGame.getValue();
        String customDescription = txtLogoDescription.getText().trim();
        
        if (teamName.isEmpty()) {
            showError(errorName, "Entrez le nom de l'équipe d'abord");
            return;
        }
        
        if (selectedGame == null) {
            showError(errorGameId, "Sélectionnez un jeu d'abord");
            return;
        }
        
        // Disable button and show loading
        btnGenerateLogo.setDisable(true);
        txtLogoFileName.setText("🎨 Génération en cours...");
        
        // Run in background thread
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return aiService.generateTeamLogo(teamName, selectedGame.getNom(), customDescription);
            }
        };
        
        task.setOnSucceeded(e -> {
            String imagePath = task.getValue();
            if (imagePath != null) {
                logoImagePath = imagePath;
                txtLogoFileName.setText("✅ Logo généré avec AI");
                System.out.println("✅ Logo generated: " + imagePath);
            } else {
                txtLogoFileName.setText("❌ Échec de la génération");
                System.err.println("❌ Failed to generate logo");
            }
            btnGenerateLogo.setDisable(false);
        });
        
        task.setOnFailed(e -> {
            txtLogoFileName.setText("❌ Erreur de génération");
            System.err.println("❌ Error: " + task.getException().getMessage());
            task.getException().printStackTrace();
            btnGenerateLogo.setDisable(false);
        });
        
        new Thread(task).start();
    }

    @FXML
    private void handleGenerateJersey() {
        String teamName = txtName.getText().trim();
        Game selectedGame = choiceGame.getValue();
        String logoDescription = txtLogoDescription.getText().trim();
        
        if (teamName.isEmpty()) {
            showError(errorName, "Entrez le nom de l'équipe d'abord");
            return;
        }
        
        if (selectedGame == null) {
            showError(errorGameId, "Sélectionnez un jeu d'abord");
            return;
        }
        
        // Disable button and show loading
        btnGenerateJersey.setDisable(true);
        txtJerseyFileName.setText("👕 Génération en cours...");
        
        // Run in background thread
        Task<String> task = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return aiService.generateTeamJersey(teamName, selectedGame.getNom(), logoDescription);
            }
        };
        
        task.setOnSucceeded(e -> {
            String imagePath = task.getValue();
            if (imagePath != null) {
                jerseyImagePath = imagePath;
                txtJerseyFileName.setText("✅ Maillot généré avec AI");
                System.out.println("✅ Jersey generated: " + imagePath);
            } else {
                txtJerseyFileName.setText("❌ Échec de la génération");
                System.err.println("❌ Failed to generate jersey");
            }
            btnGenerateJersey.setDisable(false);
        });
        
        task.setOnFailed(e -> {
            txtJerseyFileName.setText("❌ Erreur de génération");
            System.err.println("❌ Error: " + task.getException().getMessage());
            task.getException().printStackTrace();
            btnGenerateJersey.setDisable(false);
        });
        
        new Thread(task).start();
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
            team.setJerseyImage(jerseyImagePath);
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
