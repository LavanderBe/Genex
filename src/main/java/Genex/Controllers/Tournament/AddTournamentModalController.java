package Genex.Controllers.Tournament;

import Genex.entities.Tounament;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class AddTournamentModalController {

    @FXML
    private Text modalTitle;

    @FXML
    private TextField txtName;

    @FXML
    private ComboBox<String> comboFormat;

    @FXML
    private ComboBox<String> comboType;

    @FXML
    private TextField txtGameId;

    @FXML
    private TextField txtCenterId;

    @FXML
    private DatePicker dateStart;

    @FXML
    private DatePicker dateEnd;

    @FXML
    private TextField txtPrizePool;

    @FXML
    private Button btnClose;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    // Error labels
    @FXML
    private Text errorName;

    @FXML
    private Text errorFormat;

    @FXML
    private Text errorType;

    @FXML
    private Text errorStartDate;

    @FXML
    private Text errorEndDate;

    @FXML
    private Text errorPrizePool;

    private Consumer<Tounament> onSaveCallback;
    private Tounament tournamentToEdit;

    @FXML
    public void initialize() {
        System.out.println("AddTournamentModalController initialized");
        
        // Populate combo boxes
        comboFormat.getItems().addAll("Round Robin", "Single Elimination", "Double Elimination");
        comboType.getItems().addAll("Solo", "Team");
        
        // Setup validation listeners
        setupValidation();
    }

    private void setupValidation() {
        // Clear errors on input
        txtName.textProperty().addListener((obs, old, val) -> hideError(errorName));
        comboFormat.valueProperty().addListener((obs, old, val) -> hideError(errorFormat));
        comboType.valueProperty().addListener((obs, old, val) -> hideError(errorType));
        dateStart.valueProperty().addListener((obs, old, val) -> hideError(errorStartDate));
        dateEnd.valueProperty().addListener((obs, old, val) -> hideError(errorEndDate));
        txtPrizePool.textProperty().addListener((obs, old, val) -> hideError(errorPrizePool));
    }

    public void setTournament(Tounament tournament) {
        this.tournamentToEdit = tournament;
        modalTitle.setText("Modifier le Tournoi");
        
        // Fill form with tournament data
        txtName.setText(tournament.getTournamentName());
        comboFormat.setValue(tournament.getFormat());
        comboType.setValue(tournament.getParticipant_type());
        txtGameId.setText(tournament.getGame_id());
        txtCenterId.setText(tournament.getCenter_id());
        
        if (tournament.getStarts_at() != null) {
            dateStart.setValue(tournament.getStarts_at().toLocalDate());
        }
        if (tournament.getEnds_at() != null) {
            dateEnd.setValue(tournament.getEnds_at().toLocalDate());
        }
        
        txtPrizePool.setText(String.valueOf(tournament.getPrize_pool()));
    }

    public void setOnSaveCallback(Consumer<Tounament> callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void closeModal() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
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
            tournament.setGame_id(txtGameId.getText().trim());
            tournament.setCenter_id(txtCenterId.getText().trim());
            
            // Convert dates to LocalDateTime
            if (dateStart.getValue() != null) {
                tournament.setStarts_at(LocalDateTime.of(dateStart.getValue(), LocalTime.of(0, 0)));
            }
            if (dateEnd.getValue() != null) {
                tournament.setEnds_at(LocalDateTime.of(dateEnd.getValue(), LocalTime.of(23, 59)));
            }
            
            tournament.setPrize_pool(Double.parseDouble(txtPrizePool.getText().trim()));
            
            System.out.println("Tournament saved: " + tournament.getTournamentName());
            
            // Call callback
            if (onSaveCallback != null) {
                onSaveCallback.accept(tournament);
            }
            
            closeModal();
            
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

    private void showError(Text errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Text errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
