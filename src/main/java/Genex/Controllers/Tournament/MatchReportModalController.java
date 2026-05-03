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
        txtMatchInfo.setText("Round " + match.getRound() + " - Match " + match.getMatchNumber());
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

        // Determine winner
        String winnerId;
        if (radioPlayer1.isSelected()) {
            winnerId = match.getPlayer1Id();
        } else {
            winnerId = match.getPlayer2Id();
        }

        try {
            // Update local database
            crudMatch.updateMatchResult(match.getId(), player1Score, player2Score, winnerId);

            // Update Challonge if tournament is synced
            if (challongeUrlSlug != null && !challongeUrlSlug.isEmpty() && match.getChallongeMatchId() != null) {
                try {
                    // Get Challonge participant ID for winner
                    // For now, we'll use the match's challonge IDs
                    String challongeWinnerId = radioPlayer1.isSelected() 
                            ? getChallongeParticipantId(match.getPlayer1Id())
                            : getChallongeParticipantId(match.getPlayer2Id());
                    
                    challongeService.updateMatchResult(
                            challongeUrlSlug,
                            match.getChallongeMatchId(),
                            player1Score,
                            player2Score,
                            challongeWinnerId
                    );
                } catch (Exception e) {
                    System.err.println("Warning: Failed to update Challonge: " + e.getMessage());
                    // Continue anyway - local update succeeded
                }
            }

            showAlert("Succès", "Résultat enregistré avec succès!", Alert.AlertType.INFORMATION);

            // Call success callback
            if (onSuccess != null) {
                onSuccess.run();
            }

            // Close modal
            closeModal();

        } catch (Exception e) {
            System.err.println("Error submitting match result: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'enregistrement du résultat: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String getChallongeParticipantId(String playerId) {
        // TODO: Implement proper mapping between local player IDs and Challonge participant IDs
        // For now, return the player ID as-is
        return playerId;
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
