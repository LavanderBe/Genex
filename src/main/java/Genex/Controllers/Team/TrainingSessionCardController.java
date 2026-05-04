package Genex.Controllers.Team;

import Genex.entities.TrainingSession;
import Genex.services.CrudTrainingSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TrainingSessionCardController {

    @FXML
    private Text sessionTitle;

    @FXML
    private Text sessionType;

    @FXML
    private Text sessionStatus;

    @FXML
    private Text sessionDate;

    @FXML
    private Text sessionDuration;

    @FXML
    private Text sessionDurationText;

    @FXML
    private Text sessionLocation;

    @FXML
    private VBox notesContainer;

    @FXML
    private Text sessionNotes;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    private TrainingSession session;
    private Runnable onUpdateCallback;
    private javafx.scene.layout.StackPane rootStackPane; // Reference to root stack pane
    private javafx.scene.layout.VBox contentArea; // Reference to content area

    public void setSession(TrainingSession session) {
        this.session = session;
        updateUI();
    }

    public void setRootStackPane(javafx.scene.layout.StackPane rootStackPane) {
        this.rootStackPane = rootStackPane;
    }

    public void setContentArea(javafx.scene.layout.VBox contentArea) {
        this.contentArea = contentArea;
    }

    private void updateUI() {
        if (session != null) {
            sessionTitle.setText(session.getTitle());

            // Display type
            if (session.getType() != null) {
                sessionType.setText(session.getType().name());
            } else {
                sessionType.setText("");
            }

            // Display status
            if (session.getStatus() != null) {
                sessionStatus.setText(session.getStatus().name());
            } else {
                sessionStatus.setText("");
            }

            // Format date and time
            if (session.getSessionDatetime() != null) {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                sessionDate.setText(session.getSessionDatetime().format(dateFormatter));
            }

            // Duration with start and end time
            if (session.getStartTime() != null && session.getEndTime() != null) {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                String timeRange = session.getStartTime().format(timeFormatter) + " - " +
                        session.getEndTime().format(timeFormatter);
                sessionDuration.setText(timeRange);
                sessionDurationText.setText(session.getFormattedDuration());
            } else {
                sessionDuration.setText("--:-- - --:--");
                sessionDurationText.setText(session.getFormattedDuration());
            }

            // Location - display from session or default
            if (session.getLocation() != null && !session.getLocation().trim().isEmpty()) {
                sessionLocation.setText(session.getLocation());
            } else {
                sessionLocation.setText("Non spécifié");
            }

            // Notes (optional) - only if the fields exist in FXML
            if (notesContainer != null && sessionNotes != null) {
                if (session.getNotes() != null && !session.getNotes().trim().isEmpty()) {
                    sessionNotes.setText(session.getNotes());
                    notesContainer.setVisible(true);
                    notesContainer.setManaged(true);
                } else {
                    notesContainer.setVisible(false);
                    notesContainer.setManaged(false);
                }
            }
        }
    }

    @FXML
    private void handleEdit() {
        System.out.println("Edit session: " + (session != null ? session.getTitle() : "null"));

        if (session == null) return;

        // Call the modal in TeamDetailController (like Tournament)
        if (teamDetailController != null) {
            teamDetailController.openEditSessionModal(session);
        } else {
            System.err.println("TeamDetailController not set!");
        }
    }

    private void openEditModalAsOverlay() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTrainingSessionModal.fxml"));
            Parent editSessionForm = loader.load();

            // 1. Apply Blur effect to the background
            javafx.scene.effect.GaussianBlur blur = new javafx.scene.effect.GaussianBlur(15);
            contentArea.setEffect(blur);
            contentArea.setDisable(true); // Prevent clicking background items

            // 2. Wrap the form in a darkening overlay (dimmer)
            VBox overlay = new VBox(editSessionForm);
            overlay.setAlignment(javafx.geometry.Pos.CENTER);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Dim background

            // 3. Add to the stack
            rootStackPane.getChildren().add(overlay);

            // 4. Pass a "Close" callback to the AddTrainingSessionModalController
            AddTrainingSessionModalController controller = loader.getController();
            controller.setSession(session);
            controller.setOnSaveCallback(updatedSession -> {
                System.out.println("Updating session: " + updatedSession.getTitle());

                // Update in database
                CrudTrainingSession crudSession = new CrudTrainingSession();
                crudSession.updateSession(updatedSession);

                // Refresh the detail page
                if (onUpdateCallback != null) {
                    onUpdateCallback.run();
                }
            });

            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(overlay); // Remove form
                contentArea.setEffect(null);                // Remove blur
                contentArea.setDisable(false);              // Re-enable content
            });

        } catch (Exception e) {
            System.err.println("Error opening edit modal");
            e.printStackTrace();
        }
    }

    private void openEditModalAsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTrainingSessionModal.fxml"));
            Parent modalRoot = loader.load();

            // Create modal stage
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle("Modifier la Session");

            Scene scene = new Scene(modalRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modalStage.setScene(scene);

            // Get controller and set session data
            AddTrainingSessionModalController controller = loader.getController();
            controller.setSession(session);
            controller.setOnSaveCallback(updatedSession -> {
                System.out.println("Updating session: " + updatedSession.getTitle());

                // Update in database
                CrudTrainingSession crudSession = new CrudTrainingSession();
                crudSession.updateSession(updatedSession);

                // Refresh the detail page
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
        System.out.println("Delete session: " + (session != null ? session.getTitle() : "null"));

        if (session == null) return;

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer la session \"" + session.getTitle() + "\" ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete from database
                CrudTrainingSession crudSession = new CrudTrainingSession();
                crudSession.deleteSession(session.getId());

                System.out.println("Session deleted: " + session.getTitle());

                // Refresh the detail page
                if (onUpdateCallback != null) {
                    onUpdateCallback.run();
                }

            } catch (Exception e) {
                System.err.println("Error deleting session");
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer la session.", Alert.AlertType.ERROR);
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

    private TeamDetailController teamDetailController;

    public void setTeamDetailController(TeamDetailController controller) {
        this.teamDetailController = controller;
    }
}