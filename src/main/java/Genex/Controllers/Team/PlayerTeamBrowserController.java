package Genex.Controllers.Team;

import Genex.entities.Team;
import Genex.services.CrudTeam;
import Genex.services.CrudTeamMember;
import Genex.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.List;

public class PlayerTeamBrowserController {

    @FXML private StackPane rootStackPane;
    @FXML private StackPane innerContainer;

    private CrudTeamMember crudTeamMember;
    private String currentUserId;

    @FXML
    public void initialize() {
        crudTeamMember = new CrudTeamMember();
        currentUserId = SessionManager.getInstance().getCurrentUserId();

        if (currentUserId == null) {
            showError("Session expirée. Veuillez vous reconnecter.");
            return;
        }

        try {
            Team existingTeam = crudTeamMember.getTeamByPlayer(currentUserId);
            if (existingTeam != null) {
                showTeamDetail(existingTeam);
            } else {
                showTeamList();
            }
        } catch (Exception e) {
            System.err.println("Error during team routing: " + e.getMessage());
            e.printStackTrace();
            showTeamList();
        }
    }

    // ── Public navigation API ────────────────────────────────────────

    public void showTeamDetail(Team team) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Fxml/Team/PlayerTeamDetail.fxml"));
            Parent detailRoot = loader.load();
            PlayerTeamDetailController controller = loader.getController();
            controller.setTeam(team);
            controller.setBrowserController(this);
            innerContainer.getChildren().setAll(detailRoot);
        } catch (Exception e) {
            System.err.println("Error loading team detail view");
            e.printStackTrace();
            showError("Impossible de charger les détails de l'équipe.");
        }
    }

    public void showTeamDetailViewOnly(Team team) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Fxml/Team/PlayerTeamDetail.fxml"));
            Parent detailRoot = loader.load();
            PlayerTeamDetailController controller = loader.getController();
            controller.setTeamViewOnly(team);
            controller.setBrowserController(this);
            innerContainer.getChildren().setAll(detailRoot);
        } catch (Exception e) {
            System.err.println("Error loading team detail view (view-only)");
            e.printStackTrace();
            showError("Impossible de charger les détails de l'équipe.");
        }
    }

    public void showTeamList() {
        try {
            List<Team> allTeams = new CrudTeam().getAll();

            // ── Outer wrapper ────────────────────────────────────────
            VBox wrapper = new VBox(20);
            wrapper.setPadding(new Insets(25));
            wrapper.setStyle("-fx-background-color: #0d0d1a;");

            // ── Header row: title + "Créer une équipe" button ────────
            HBox header = new HBox();
            header.setAlignment(Pos.CENTER_LEFT);

            Label title = new Label("Équipes disponibles");
            title.setStyle("-fx-font-size: 20px; -fx-text-fill: white; -fx-font-weight: bold;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnCreate = new Button("＋  Créer une équipe");
            btnCreate.setStyle(
                "-fx-background-color: #8B0D0D;" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-shape: 'M 0 0 L 140 0 L 152 10 L 152 36 L 12 36 L 0 26 Z';" +
                "-fx-padding: 9 0;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(139,13,13,0.4), 8, 0, 0, 3);"
            );
            btnCreate.setOnAction(e -> openCreateTeamModal());

            header.getChildren().addAll(title, spacer, btnCreate);
            wrapper.getChildren().add(header);

            // ── Team cards grid ──────────────────────────────────────
            FlowPane flow = new FlowPane();
            flow.setHgap(20);
            flow.setVgap(20);
            flow.setPrefWrapLength(1100);

            boolean hasTeams = false;
            for (Team team : allTeams) {
                if (team.getStatus() != Team.Status.ACTIVE) continue;
                hasTeams = true;
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/Fxml/Team/PlayerTeamCard.fxml"));
                Parent card = loader.load();
                PlayerTeamCardController cc = loader.getController();
                cc.setTeam(team);
                cc.setBrowserController(this);
                cc.setReadOnly(false);
                flow.getChildren().add(card);
            }

            if (!hasTeams) {
                Label empty = new Label("Aucune équipe disponible pour le moment.");
                empty.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-size: 14px;");
                wrapper.getChildren().add(empty);
            } else {
                wrapper.getChildren().add(flow);
            }

            ScrollPane scroll = new ScrollPane(wrapper);
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

            innerContainer.getChildren().setAll(scroll);

        } catch (Exception e) {
            System.err.println("Error loading team list");
            e.printStackTrace();
            showError("Impossible de charger la liste des équipes.");
        }
    }

    public StackPane getRootStackPane() {
        return rootStackPane;
    }

    // ── Create team ──────────────────────────────────────────────────

    /**
     * Opens the AddTeamModal as an overlay.
     * If the player already has a team, warns them to leave first.
     */
    public void openCreateTeamModal() {
        // Check if player already has a team
        try {
            Team existing = crudTeamMember.getTeamByPlayer(currentUserId);
            if (existing != null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Déjà membre d'une équipe");
                alert.setHeaderText("Vous êtes déjà membre de \"" + existing.getName() + "\".");
                alert.setContentText("Quittez votre équipe actuelle avant d'en créer une nouvelle.");
                alert.showAndWait();
                return;
            }
        } catch (Exception e) {
            System.err.println("Error checking existing team: " + e.getMessage());
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Fxml/Team/AddTeamModal.fxml"));
            StackPane modalOverlay = loader.load();
            rootStackPane.getChildren().add(modalOverlay);

            AddTeamModalController controller = loader.getController();

            controller.setOnSaveCallback(team -> {
                try {
                    // 1. Save team to DB
                    CrudTeam crudTeam = new CrudTeam();
                    crudTeam.addEntity(team);

                    // 2. Fetch the saved team to get its generated ID
                    Team savedTeam = getLastCreatedTeamByUser(currentUserId);

                    // 3. Auto-add creator as a member
                    if (savedTeam != null) {
                        crudTeamMember.addMember(savedTeam.getId(), currentUserId);
                    }

                    // 4. Close modal
                    rootStackPane.getChildren().remove(modalOverlay);

                    // 5. Navigate to the new team detail
                    if (savedTeam != null) {
                        showTeamDetail(savedTeam);
                    } else {
                        showTeamList();
                    }
                } catch (Exception ex) {
                    System.err.println("Error creating team: " + ex.getMessage());
                    ex.printStackTrace();
                    rootStackPane.getChildren().remove(modalOverlay);
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setHeaderText(null);
                    error.setContentText("Impossible de créer l'équipe.");
                    error.showAndWait();
                }
            });

            controller.setOnCloseCallback(() ->
                    rootStackPane.getChildren().remove(modalOverlay));

        } catch (Exception e) {
            System.err.println("Error opening create team modal");
            e.printStackTrace();
        }
    }

    // ── Private helpers ──────────────────────────────────────────────

    /**
     * Fetches the most recently created team by this user (right after insert).
     */
    private Team getLastCreatedTeamByUser(String userId) {
        try {
            List<Team> all = new CrudTeam().getAll(); // ordered by created_at DESC
            for (Team t : all) {
                if (userId.equals(t.getCreatedBy())) return t;
            }
        } catch (Exception e) {
            System.err.println("Error fetching last created team: " + e.getMessage());
        }
        return null;
    }

    private void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 14px; -fx-padding: 40px;");
        innerContainer.getChildren().setAll(errorLabel);
    }
}
