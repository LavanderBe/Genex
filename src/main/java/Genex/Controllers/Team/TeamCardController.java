package Genex.Controllers.Team;

import Genex.entities.Team;
import Genex.services.CrudTeam;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.util.Optional;

public class TeamCardController {

    @FXML
    private Text teamName;

    @FXML
    private Text teamTag;

    @FXML
    private Text teamGame;

    @FXML
    private Text teamType;

    @FXML
    private ImageView teamLogo;

    @FXML
    private Text teamIconFallback;

    @FXML
    private Button btnView;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    private Team team;
    private Runnable onUpdateCallback;
    private StackPane contentContainer; // Reference to main content container

    public void setTeam(Team team) {
        this.team = team;
        updateUI();
    }

    public void setContentContainer(StackPane contentContainer) {
        this.contentContainer = contentContainer;
    }

    private void updateUI() {
        if (team != null) {
            teamName.setText(team.getName());

            // Display status as tag
            if (team.getStatus() != null) {
                teamTag.setText("[" + team.getStatus().name() + "]");
            } else {
                teamTag.setText("");
            }

            // Display game ID (or you could fetch game name from database)
            if (team.getGameId() != null) {
                teamGame.setText("Game ID: " + team.getGameId());
            } else {
                teamGame.setText("");
            }

            // Display contact
            if (team.getContact() != null) {
                teamType.setText(team.getContact());
            } else {
                teamType.setText("");
            }

            // Load and display team logo
            loadTeamLogo();
        }
    }

    private void loadTeamLogo() {
        if (team != null && team.getLogoImage() != null && !team.getLogoImage().isEmpty()) {
            try {
                File logoFile = new File(team.getLogoImage());

                if (logoFile.exists()) {
                    Image logoImage = new Image(logoFile.toURI().toString());
                    teamLogo.setImage(logoImage);
                    teamLogo.setVisible(true);
                    teamIconFallback.setVisible(false);
                    System.out.println("Loaded logo for team: " + team.getName());
                } else {
                    // File doesn't exist, show fallback
                    System.out.println("Logo file not found: " + team.getLogoImage());
                    showFallbackIcon();
                }
            } catch (Exception e) {
                System.err.println("Error loading team logo: " + e.getMessage());
                showFallbackIcon();
            }
        } else {
            // No logo path, show fallback
            showFallbackIcon();
        }
    }

    private void showFallbackIcon() {
        teamLogo.setVisible(false);
        teamIconFallback.setVisible(true);
    }

    @FXML
    private void handleCardClick() {
        System.out.println("=== Team card clicked ===");
        System.out.println("Team: " + (team != null ? team.getName() : "null"));
        System.out.println("Content container: " + (contentContainer != null ? "SET" : "NULL"));
        // Open team detail page
        handleView();
    }

    @FXML
    private void handleView() {
        System.out.println("=== handleView called ===");
        System.out.println("View team: " + (team != null ? team.getName() : "null"));
        System.out.println("Content container available: " + (contentContainer != null));

        if (team == null) {
            System.err.println("ERROR: team is null!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/TeamDetail.fxml"));
            Parent detailRoot = loader.load();

            // Get controller and set team
            TeamDetailController controller = loader.getController();
            controller.setTeam(team);

            // If we have a content container reference, load in same window
            if (contentContainer != null) {
                System.out.println("✓ Loading team detail in SAME WINDOW...");
                contentContainer.getChildren().clear();
                contentContainer.getChildren().add(detailRoot);
                System.out.println("✓ Team detail loaded successfully in same window!");
            } else {
                // Fallback: Open in new window (old behavior)
                System.out.println("✗ Content container is NULL - opening in NEW WINDOW (fallback)...");
                Stage detailStage = new Stage();
                detailStage.initModality(Modality.APPLICATION_MODAL);
                detailStage.setTitle(team.getName() + " - Détails");

                Scene scene = new Scene(detailRoot);
                detailStage.setScene(scene);
                detailStage.setMaximized(true);

                detailStage.showAndWait();

                // Refresh after closing detail page
                if (onUpdateCallback != null) {
                    onUpdateCallback.run();
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR opening team detail page");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEdit() {
        System.out.println("Edit team: " + (team != null ? team.getName() : "null"));

        if (team == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTeamModal.fxml"));
            Parent modalRoot = loader.load();

            // Create modal stage
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initStyle(StageStyle.TRANSPARENT);
            modalStage.setTitle("Modifier l'Équipe");

            Scene scene = new Scene(modalRoot);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            modalStage.setScene(scene);

            // Get controller and set team data
            AddTeamModalController controller = loader.getController();
            controller.setTeam(team);
            controller.setOnSaveCallback(updatedTeam -> {
                System.out.println("Updating team: " + updatedTeam.getName());

                // Update in database
                CrudTeam crudTeam = new CrudTeam();
                crudTeam.updateEntity(updatedTeam, team.getId());

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
        System.out.println("Delete team: " + (team != null ? team.getName() : "null"));

        if (team == null) return;

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer l'équipe \"" + team.getName() + "\" ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete from database
                CrudTeam crudTeam = new CrudTeam();
                crudTeam.deleteEntity(team);

                System.out.println("Team deleted: " + team.getName());

                // Refresh the hub
                if (onUpdateCallback != null) {
                    onUpdateCallback.run();
                }

            } catch (Exception e) {
                System.err.println("Error deleting team");
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer l'équipe.", Alert.AlertType.ERROR);
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