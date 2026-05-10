package Genex.Controllers.Player;

import Genex.entities.Game;
import Genex.entities.Player;
import Genex.services.CrudGame;
import Genex.services.CrudPlayer;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

public class AddPlayerController {

    @FXML private TextField prenomField, nomField, pseudoField, cinField, natField, cityField, accountNameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker dobPicker;
    @FXML private VBox gamesToggleContainer;

    private Runnable onCloseCallback;
    private final CrudGame cg = new CrudGame();
    private final CrudPlayer cp = new CrudPlayer();
    private List<CheckBox> gameCheckboxes = new ArrayList<>();

    @FXML
    public void initialize() {
        loadAvailableGames();
    }

    private void loadAvailableGames() {
        gamesToggleContainer.getChildren().clear();
        List<Game> games = cg.getgames(); // Fetches all games from DB

        for (Game game : games) {
            CheckBox cb = new CheckBox(game.getNom().toUpperCase());
            cb.getStyleClass().add("game-checkbox");
            cb.setUserData(game.getId()); // Store ID for database linking
            gameCheckboxes.add(cb);
            gamesToggleContainer.getChildren().add(cb);
        }
    }

    @FXML
    private void handleSave() {
        // 1. Collect Data
        // Player newPlayer = new Player(...);

        // 2. Collect Selected Games
        List<Integer> selectedGameIds = new ArrayList<>();
        for (CheckBox cb : gameCheckboxes) {
            if (cb.isSelected()) {
                selectedGameIds.add((Integer) cb.getUserData());
            }
        }

        // 3. Save to DB logic...
        System.out.println("Executing Protocol for " + pseudoField.getText());

        if (onCloseCallback != null) onCloseCallback.run();
    }

    @FXML
    private void handleCancel() {
        if (onCloseCallback != null) onCloseCallback.run();
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }
}