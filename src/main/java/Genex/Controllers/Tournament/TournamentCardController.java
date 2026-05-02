package Genex.Controllers.Tournament;

import Genex.entities.Center;
import Genex.entities.Game;
import Genex.entities.Tounament;
import Genex.services.CrudCenter;
import Genex.services.CrudGame;
import Genex.services.CrudTournament;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TournamentCardController {

    @FXML
    private Text txtName;

    @FXML
    private Text txtGame;

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
    private Text txtCenter;

    @FXML
    private Button btnView;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    private Tounament tournament;
    private Runnable onUpdateCallback;
    private StackPane rootStackPane;
    private VBox contentArea;
    private CrudGame crudGame = new CrudGame();
    private CrudCenter crudCenter = new CrudCenter();

    @FXML
    public void initialize() {
        System.out.println("TournamentCardController initialized");
    }

    public void setTournament(Tounament tournament) {
        this.tournament = tournament;
        updateCard();
    }

    public void setRootStackPane(StackPane rootStackPane, VBox contentArea) {
        this.rootStackPane = rootStackPane;
        this.contentArea = contentArea;
    }

    private void updateCard() {
        if (tournament == null) return;

        // Set tournament name
        txtName.setText(tournament.getTournamentName());

        // Set game name
        if (tournament.getGame_id() != null) {
            try {
                Game game = crudGame.getgames().stream()
                        .filter(g -> g.getId().equals(tournament.getGame_id()))
                        .findFirst()
                        .orElse(null);
                
                if (game != null) {
                    txtGame.setText(game.getNom());
                } else {
                    txtGame.setText("Jeu inconnu");
                }
            } catch (Exception e) {
                txtGame.setText("Jeu inconnu");
                e.printStackTrace();
            }
        } else {
            txtGame.setText("Jeu non spécifié");
        }

        // Set format with participant type
        String formatText = tournament.getFormat();
        if (tournament.getParticipant_type() != null) {
            formatText += " • " + tournament.getParticipant_type();
        }
        txtFormat.setText(formatText);

        // Set prize pool
        txtPrize.setText(String.format("%.0f €", tournament.getPrize_pool()));

        // Set dates with year
        if (tournament.getStarts_at() != null && tournament.getEnds_at() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            String startDate = tournament.getStarts_at().format(DateTimeFormatter.ofPattern("dd MMM"));
            String endDate = tournament.getEnds_at().format(formatter);
            txtDates.setText(startDate + " - " + endDate);
        }

        // Set center name and city
        if (tournament.getCenter_id() != null) {
            try {
                Center center = crudCenter.getAll().stream()
                        .filter(c -> c.getCenterId().equals(tournament.getCenter_id()))
                        .findFirst()
                        .orElse(null);
                
                if (center != null) {
                    txtCenter.setText(center.getName() + " - " + center.getCity());
                } else {
                    txtCenter.setText("Centre inconnu");
                }
            } catch (Exception e) {
                txtCenter.setText("Centre inconnu");
                e.printStackTrace();
            }
        } else {
            txtCenter.setText("Centre non spécifié");
        }

        // Set state badge
        updateStateBadge();
    }

    private void updateStateBadge() {
        if (tournament == null || tournament.getState() == null) {
            txtStatus.setText("INCONNU");
            statusBadge.getStyleClass().removeAll("state-registration-open", "state-registration-closed", 
                    "state-in-progress", "state-completed", "state-cancelled");
            return;
        }

        try {
            Tounament.TournamentState state = Tounament.TournamentState.valueOf(tournament.getState());
            txtStatus.setText(state.getDisplayName().toUpperCase());
            
            // Remove all state classes first
            statusBadge.getStyleClass().removeAll("state-registration-open", "state-registration-closed", 
                    "state-in-progress", "state-completed", "state-cancelled");
            
            // Add appropriate state class
            switch (state) {
                case REGISTRATION_OPEN:
                    statusBadge.getStyleClass().add("state-registration-open");
                    break;
                case REGISTRATION_CLOSED:
                    statusBadge.getStyleClass().add("state-registration-closed");
                    break;
                case IN_PROGRESS:
                    statusBadge.getStyleClass().add("state-in-progress");
                    break;
                case COMPLETED:
                    statusBadge.getStyleClass().add("state-completed");
                    break;
                case CANCELLED:
                    statusBadge.getStyleClass().add("state-cancelled");
                    break;
            }
        } catch (IllegalArgumentException e) {
            txtStatus.setText("INCONNU");
            System.err.println("Invalid tournament state: " + tournament.getState());
        }
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
            Parent editTournamentForm = loader.load();

            // 1. Apply Blur effect to the background
            GaussianBlur blur = new GaussianBlur(15);
            contentArea.setEffect(blur);
            contentArea.setDisable(true);

            // 2. Wrap the form in a darkening overlay
            VBox overlay = new VBox(editTournamentForm);
            overlay.setAlignment(javafx.geometry.Pos.CENTER);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

            // 3. Add to the stack
            rootStackPane.getChildren().add(overlay);

            // 4. Get controller and set tournament data
            AddTournamentModalController controller = loader.getController();
            controller.setTournament(tournament);
            controller.setOnSaveCallback(updatedTournament -> {
                System.out.println("Updating tournament: " + updatedTournament.getTournamentName());

                // Update in database
                CrudTournament crudTournament = new CrudTournament();
                crudTournament.updateEntity(updatedTournament, tournament.getTournamentId());

                // Remove overlay
                rootStackPane.getChildren().remove(overlay);
                contentArea.setEffect(null);
                contentArea.setDisable(false);

                // Refresh the hub
                if (onUpdateCallback != null) {
                    onUpdateCallback.run();
                }
            });

            // Handle close without saving
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(overlay);
                contentArea.setEffect(null);
                contentArea.setDisable(false);
            });

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
