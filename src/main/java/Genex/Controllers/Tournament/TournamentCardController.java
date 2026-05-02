package Genex.Controllers.Tournament;

import Genex.entities.Tounament;
import Genex.services.CrudTournament;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TournamentCardController {

    @FXML
    private Text txtName;

    @FXML
    private StackPane statusBadge;

    @FXML
    private Text txtStatus;

    @FXML
    private Text txtFormat;

    @FXML
    private Text txtPrize;

    @FXML
    private Text txtDates;

    @FXML
    private Button btnView;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    private Tounament tournament;
    private Runnable onUpdateCallback;

    @FXML
    public void initialize() {
        System.out.println("TournamentCardController initialized");
    }

    public void setTournament(Tounament tournament) {
        this.tournament = tournament;
        updateCard();
    }

    private void updateCard() {
        if (tournament == null) return;

        // Set tournament name
        txtName.setText(tournament.getTournamentName());

        // Set format
        txtFormat.setText(tournament.getFormat());

        // Set prize pool
        txtPrize.setText(String.format("%.0f €", tournament.getPrize_pool()));

        // Set dates
        if (tournament.getStarts_at() != null && tournament.getEnds_at() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
            String startDate = tournament.getStarts_at().format(formatter);
            String endDate = tournament.getEnds_at().format(formatter);
            txtDates.setText(startDate + " - " + endDate);
        }

        // Set status (for now, just set as "ACTIF")
        txtStatus.setText("ACTIF");
        statusBadge.getStyleClass().removeAll("upcoming", "finished");
        // You can add logic here to determine status based on dates
    }

    @FXML
    private void handleView() {
        System.out.println("View tournament: " + tournament.getTournamentName());
        // TODO: Open tournament details view
    }

    @FXML
    private void handleEdit() {
        System.out.println("Edit tournament: " + (tournament != null ? tournament.getTournamentName() : "null"));

        if (tournament == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tournament/AddTournamentModal.fxml"));
            Parent modalRoot = loader.load();

            // Create modal stage
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle("Modifier le Tournoi");

            Scene scene = new Scene(modalRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modalStage.setScene(scene);

            // Get controller and set tournament data
            AddTournamentModalController controller = loader.getController();
            controller.setTournament(tournament);
            controller.setOnSaveCallback(updatedTournament -> {
                System.out.println("Updating tournament: " + updatedTournament.getTournamentName());

                // Update in database
                CrudTournament crudTournament = new CrudTournament();
                crudTournament.updateEntity(updatedTournament, tournament.getTournamentId());

                // Refresh the hub
                if (onUpdateCallback != null) {
                    onUpdateCallback.run();
                }

                modalStage.close();
            });

            modalStage.showAndWait();

        } catch (Exception e) {
            System.err.println("Error opening edit modal");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        System.out.println("Delete tournament: " + (tournament != null ? tournament.getTournamentName() : "null"));

        if (tournament == null) return;

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer le tournoi \"" + tournament.getTournamentName() + "\" ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete from database
                CrudTournament crudTournament = new CrudTournament();
                crudTournament.deleteEntity(tournament);

                System.out.println("Tournament deleted: " + tournament.getTournamentName());

                // Refresh the hub
                if (onUpdateCallback != null) {
                    onUpdateCallback.run();
                }

            } catch (Exception e) {
                System.err.println("Error deleting tournament");
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer le tournoi.", Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setOnUpdateCallback(Runnable callback) {
        this.onUpdateCallback = callback;
    }
}
