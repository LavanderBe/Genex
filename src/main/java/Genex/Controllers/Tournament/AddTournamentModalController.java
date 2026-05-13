package Genex.Controllers.Tournament;

import Genex.entities.Center;
import Genex.entities.Game;
import Genex.entities.Tounament;
import Genex.services.CrudCenter;
import Genex.services.CrudGame;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.function.Consumer;

public class AddTournamentModalController {

    @FXML
    private Label modalTitle;

    @FXML
    private TextField txtName;

    @FXML
    private ComboBox<String> comboFormat;

    @FXML
    private ComboBox<String> comboType;

    @FXML
    private ComboBox<Game> comboGame;

    @FXML
    private ComboBox<Center> comboCenter;

    @FXML
    private ComboBox<String> comboState;

    @FXML
    private ComboBox<Integer> comboMaxPlayers;

    @FXML
    private DatePicker dateStart;

    @FXML
    private DatePicker dateEnd;

    @FXML
    private TextField txtPrizePool;



    // Error labels
    @FXML
    private Label errorName;

    @FXML
    private Label errorFormat;

    @FXML
    private Label errorType;

    @FXML
    private Label errorGame;

    @FXML
    private Label errorCenter;

    @FXML
    private Label errorState;

    @FXML
    private Label errorMaxPlayers;

    @FXML
    private Label errorStartDate;

    @FXML
    private Label errorEndDate;

    @FXML
    private Label errorPrizePool;

    private Consumer<Tounament> onSaveCallback;
    private Runnable onCloseCallback;
    private Tounament tournamentToEdit;

    private CrudGame crudGame = new CrudGame();
    private CrudCenter crudCenter = new CrudCenter();

    @FXML
    public void initialize() {
        System.out.println("AddTournamentModalController initialized");

        // Set default title for new tournament
        if (modalTitle != null) {
            modalTitle.setText("NOUVEAU TOURNOI");
        }

        // Populate combo boxes
        comboFormat.getItems().addAll("Round Robin", "Single Elimination", "Double Elimination");
        comboType.getItems().addAll("Solo", "Team");
        
        // Populate max players combo box
        comboMaxPlayers.getItems().addAll( 8, 16, 32, 64);
        comboMaxPlayers.setValue(32); // Default value
        
        // Populate state combo box
        for (Tounament.TournamentState state : Tounament.TournamentState.values()) {
            comboState.getItems().add(state.getDisplayName());
        }
        // Set default state to REGISTRATION_OPEN
        comboState.setValue(Tounament.TournamentState.REGISTRATION_OPEN.getDisplayName());

        // Load games and centers
        loadGames();
        loadCenters();

        // Setup validation listeners
        setupValidation();
    }

    private void loadGames() {
        try {
            List<Game> games = crudGame.getgames();
            comboGame.getItems().addAll(games);

            // Set custom string converter to display game name
            comboGame.setConverter(new StringConverter<Game>() {
                @Override
                public String toString(Game game) {
                    return game != null ? game.getNom() : "";
                }

                @Override
                public Game fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading games: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCenters() {
        try {
            List<Center> centers = crudCenter.getAll();
            comboCenter.getItems().addAll(centers);

            // Set custom string converter to display center name and city
            comboCenter.setConverter(new StringConverter<Center>() {
                @Override
                public String toString(Center center) {
                    return center != null ? center.getName() + " - " + center.getCity() : "";
                }

                @Override
                public Center fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading centers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupValidation() {
        // Clear errors on input
        txtName.textProperty().addListener((obs, old, val) -> hideError(errorName));
        comboFormat.valueProperty().addListener((obs, old, val) -> hideError(errorFormat));
        comboType.valueProperty().addListener((obs, old, val) -> hideError(errorType));
        comboGame.valueProperty().addListener((obs, old, val) -> hideError(errorGame));
        comboCenter.valueProperty().addListener((obs, old, val) -> hideError(errorCenter));
        comboState.valueProperty().addListener((obs, old, val) -> hideError(errorState));
        comboMaxPlayers.valueProperty().addListener((obs, old, val) -> hideError(errorMaxPlayers));
        dateStart.valueProperty().addListener((obs, old, val) -> hideError(errorStartDate));
        dateEnd.valueProperty().addListener((obs, old, val) -> hideError(errorEndDate));
        txtPrizePool.textProperty().addListener((obs, old, val) -> hideError(errorPrizePool));
    }

    public void setTournament(Tounament tournament) {
        this.tournamentToEdit = tournament;

        // Change title to "Modifier"
        if (modalTitle != null) {
            modalTitle.setText("MODIFIER TOURNOI");
        }

        // Fill form with tournament data
        txtName.setText(tournament.getTournamentName());
        comboFormat.setValue(tournament.getFormat());
        comboType.setValue(tournament.getParticipant_type());

        // Select the game by ID
        if (tournament.getGame_id() != null) {
            for (Game game : comboGame.getItems()) {
                if (game.getId().equals(tournament.getGame_id())) {
                    comboGame.setValue(game);
                    break;
                }
            }
        }

        // Select the center by ID
        if (tournament.getCenter_id() != null) {
            for (Center center : comboCenter.getItems()) {
                if (center.getCenterId().equals(tournament.getCenter_id())) {
                    comboCenter.setValue(center);
                    break;
                }
            }
        }

        if (tournament.getStarts_at() != null) {
            dateStart.setValue(tournament.getStarts_at().toLocalDate());
        }
        if (tournament.getEnds_at() != null) {
            dateEnd.setValue(tournament.getEnds_at().toLocalDate());
        }

        txtPrizePool.setText(String.valueOf(tournament.getPrize_pool()));
        
        // Set max players
        if (tournament.getMaxPlayers() > 0) {
            comboMaxPlayers.setValue(tournament.getMaxPlayers());
        }
        
        // Set state
        if (tournament.getState() != null) {
            try {
                Tounament.TournamentState state = Tounament.TournamentState.valueOf(tournament.getState());
                comboState.setValue(state.getDisplayName());
            } catch (IllegalArgumentException e) {
                comboState.setValue(Tounament.TournamentState.REGISTRATION_OPEN.getDisplayName());
            }
        }
    }

    public void setOnSaveCallback(Consumer<Tounament> callback) {
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
    private void saveTournament() {
        if (!validateForm()) {
            return;
        }

        try {
            // Create or update tournament
            Tounament tournament = tournamentToEdit != null ? tournamentToEdit : new Tounament();

            tournament.setTournamentName(txtName.getText().trim());
            tournament.setFormat(comboFormat.getValue());
            tournament.setParticipant_type(comboType.getValue());
            tournament.setGame_id(comboGame.getValue().getId());
            tournament.setCenter_id(comboCenter.getValue().getCenterId());

            // Convert dates to LocalDateTime
            if (dateStart.getValue() != null) {
                tournament.setStarts_at(LocalDateTime.of(dateStart.getValue(), LocalTime.of(0, 0)));
            }
            if (dateEnd.getValue() != null) {
                tournament.setEnds_at(LocalDateTime.of(dateEnd.getValue(), LocalTime.of(23, 59)));
            }

            tournament.setPrize_pool(Double.parseDouble(txtPrizePool.getText().trim()));
            
            // Set max players
            tournament.setMaxPlayers(comboMaxPlayers.getValue());
            
            // Set state - convert display name back to enum name
            String stateDisplayName = comboState.getValue();
            for (Tounament.TournamentState state : Tounament.TournamentState.values()) {
                if (state.getDisplayName().equals(stateDisplayName)) {
                    tournament.setState(state.name());
                    break;
                }
            }

            System.out.println("Tournament saved: " + tournament.getTournamentName());

            // Call callback
            if (onSaveCallback != null) {
                onSaveCallback.accept(tournament);
            }

        } catch (Exception e) {
            System.err.println("Error saving tournament");
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        // Validate name
        if (txtName.getText().trim().isEmpty()) {
            showError(errorName, "Le nom est requis");
            valid = false;
        }

        // Validate format
        if (comboFormat.getValue() == null) {
            showError(errorFormat, "Le format est requis");
            valid = false;
        }

        // Validate type
        if (comboType.getValue() == null) {
            showError(errorType, "Le type est requis");
            valid = false;
        }

        // Validate game
        if (comboGame.getValue() == null) {
            showError(errorGame, "Le jeu est requis");
            valid = false;
        }

        // Validate center
        if (comboCenter.getValue() == null) {
            showError(errorCenter, "Le centre est requis");
            valid = false;
        }

        // Validate state
        if (comboState.getValue() == null) {
            showError(errorState, "L'état est requis");
            valid = false;
        }

        // Validate max players
        if (comboMaxPlayers.getValue() == null) {
            showError(errorMaxPlayers, "Le nombre max de joueurs est requis");
            valid = false;
        }

        // Validate start date
        if (dateStart.getValue() == null) {
            showError(errorStartDate, "La date de début est requise");
            valid = false;
        }

        // Validate end date
        if (dateEnd.getValue() == null) {
            showError(errorEndDate, "La date de fin est requise");
            valid = false;
        } else if (dateStart.getValue() != null && dateEnd.getValue().isBefore(dateStart.getValue())) {
            showError(errorEndDate, "La date de fin doit être après la date de début");
            valid = false;
        }

        // Validate prize pool
        if (txtPrizePool.getText().trim().isEmpty()) {
            showError(errorPrizePool, "Le prize pool est requis");
            valid = false;
        } else {
            try {
                double prize = Double.parseDouble(txtPrizePool.getText().trim());
                if (prize < 0) {
                    showError(errorPrizePool, "Le prize pool doit être positif");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                showError(errorPrizePool, "Le prize pool doit être un nombre valide");
                valid = false;
            }
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
