package Genex.Controllers.Tournament;

import Genex.entities.TournamentMatch;
import Genex.services.ChallongeService;
import Genex.services.CrudTournamentMatch;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MatchReportModalController {

    @FXML
    private Text txtMatchInfo;

    @FXML
    private Text txtPlayer1Name;

    @FXML
    private Text txtPlayer2Name;

    @FXML
    private ComboBox<Integer> cmbPlayer1Score;

    @FXML
    private ComboBox<Integer> cmbPlayer2Score;

    @FXML
    private RadioButton radioPlayer1;

    @FXML
    private RadioButton radioPlayer2;

    @FXML
    private ToggleGroup winnerGroup;

    private TournamentMatch match;
    private String player1Name;
    private String player2Name;
    private String challongeUrlSlug;
    private Runnable onSuccess;

    private CrudTournamentMatch crudMatch = new CrudTournamentMatch();
    private ChallongeService challongeService = new ChallongeService();

    @FXML
    public void initialize() {
        // Populate score dropdowns (0-10)
        for (int i = 0; i <= 10; i++) {
            cmbPlayer1Score.getItems().add(i);
            cmbPlayer2Score.getItems().add(i);
        }

        // Set default scores
        cmbPlayer1Score.setValue(0);
        cmbPlayer2Score.setValue(0);

        // Auto-select winner based on score changes
        cmbPlayer1Score.setOnAction(e -> autoSelectWinner());
        cmbPlayer2Score.setOnAction(e -> autoSelectWinner());
    }

    public void setMatch(TournamentMatch match, String player1Name, String player2Name, String challongeUrlSlug) {
        this.match = match;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.challongeUrlSlug = challongeUrlSlug;

        // Update UI
        String roundLabel = match.getRound() > 0 
                ? "Winners - Round " + match.getRound() 
                : "Losers - Round " + Math.abs(match.getRound());
        txtMatchInfo.setText(roundLabel + " - Match " + match.getMatchNumber());
        txtPlayer1Name.setText(player1Name);
        txtPlayer2Name.setText(player2Name);
        radioPlayer1.setText(player1Name);
        radioPlayer2.setText(player2Name);

        // Set current scores if any
        cmbPlayer1Score.setValue(match.getPlayer1Score());
        cmbPlayer2Score.setValue(match.getPlayer2Score());
    }

    public void setOnSuccess(Runnable onSuccess) {
        this.onSuccess = onSuccess;
    }

    private void autoSelectWinner() {
        int score1 = cmbPlayer1Score.getValue();
        int score2 = cmbPlayer2Score.getValue();

        if (score1 > score2) {
            radioPlayer1.setSelected(true);
        } else if (score2 > score1) {
            radioPlayer2.setSelected(true);
        }
    }

    @FXML
    private void handleSubmit() {
        // Validate inputs
        if (cmbPlayer1Score.getValue() == null || cmbPlayer2Score.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner les scores.", Alert.AlertType.ERROR);
            return;
        }

        if (winnerGroup.getSelectedToggle() == null) {
            showAlert("Erreur", "Veuillez sélectionner le gagnant.", Alert.AlertType.ERROR);
            return;
        }

        int player1Score = cmbPlayer1Score.getValue();
        int player2Score = cmbPlayer2Score.getValue();

        // Determine winner (local player ID)
        String winnerId = radioPlayer1.isSelected() ? match.getPlayer1Id() : match.getPlayer2Id();
        
        // Determine Challonge winner ID (Challonge participant ID)
        String challongeWinnerId = radioPlayer1.isSelected() 
                ? match.getChallongePlayer1Id() 
                : match.getChallongePlayer2Id();

        try {
            // Update local database
            crudMatch.updateMatchResult(match.getId(), player1Score, player2Score, winnerId);

            // Update Challonge if we have the match ID and winner's Challonge ID
            if (challongeUrlSlug != null && !challongeUrlSlug.isEmpty() 
                    && match.getChallongeMatchId() != null 
                    && challongeWinnerId != null) {
                try {
                    challongeService.updateMatchResult(
                            challongeUrlSlug,
                            match.getChallongeMatchId(),
                            player1Score,
                            player2Score,
                            challongeWinnerId
                    );
                } catch (Exception e) {
                    System.err.println("Warning: Failed to update Challonge: " + e.getMessage());
                }
            }

            showAlert("Succès", "Résultat enregistré avec succès!", Alert.AlertType.INFORMATION);

            if (onSuccess != null) onSuccess.run();
            closeModal();

        } catch (Exception e) {
            System.err.println("Error submitting match result: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        closeModal();
    }

    private void closeModal() {
        Stage stage = (Stage) txtMatchInfo.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
