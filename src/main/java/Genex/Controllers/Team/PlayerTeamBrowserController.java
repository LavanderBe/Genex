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
    private Button chatBubbleBtn;
    private Team currentTeam;
    private StackPane currentChatModal;

    @FXML
    public void initialize() {
        crudTeamMember = new CrudTeamMember();
        currentUserId = SessionManager.getInstance().getCurrentUserId();

        // Create floating chat bubble button
        createFloatingChatButton();

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

    private void createFloatingChatButton() {
        chatBubbleBtn = new Button("💬");
        chatBubbleBtn.setStyle(
            "-fx-background-color: #8B0D0D;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 32px;" +
            "-fx-min-width: 70px;" +
            "-fx-min-height: 70px;" +
            "-fx-max-width: 70px;" +
            "-fx-max-height: 70px;" +
            "-fx-background-radius: 35px;" +
            "-fx-border-radius: 35px;" +
            "-fx-border-color: rgba(255,255,255,0.3);" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,13,13,0.6), 15, 0, 0, 5);"
        );
        
        chatBubbleBtn.setOnMouseEntered(e -> chatBubbleBtn.setStyle(
            "-fx-background-color: #A01010;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 32px;" +
            "-fx-min-width: 70px;" +
            "-fx-min-height: 70px;" +
            "-fx-max-width: 70px;" +
            "-fx-max-height: 70px;" +
            "-fx-background-radius: 35px;" +
            "-fx-border-radius: 35px;" +
            "-fx-border-color: rgba(255,255,255,0.5);" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,13,13,0.8), 20, 0, 0, 7);" +
            "-fx-scale-x: 1.05;" +
            "-fx-scale-y: 1.05;"
        ));
        
        chatBubbleBtn.setOnMouseExited(e -> chatBubbleBtn.setStyle(
            "-fx-background-color: #8B0D0D;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 32px;" +
            "-fx-min-width: 70px;" +
            "-fx-min-height: 70px;" +
            "-fx-max-width: 70px;" +
            "-fx-max-height: 70px;" +
            "-fx-background-radius: 35px;" +
            "-fx-border-radius: 35px;" +
            "-fx-border-color: rgba(255,255,255,0.3);" +
            "-fx-border-width: 2;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(139,13,13,0.6), 15, 0, 0, 5);"
        ));
        
        chatBubbleBtn.setOnAction(e -> openChatModal());
        chatBubbleBtn.setVisible(false);
        chatBubbleBtn.setManaged(false);
        
        StackPane.setAlignment(chatBubbleBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(chatBubbleBtn, new Insets(0, 30, 30, 0));
        
        rootStackPane.getChildren().add(chatBubbleBtn);
    }

    private void openChatModal() {
        // Prevent opening multiple modals
        if (currentChatModal != null) {
            return;
        }
        
        if (currentTeam == null) {
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Fxml/Team/TeamChatModal.fxml"));
            currentChatModal = loader.load();
            
            TeamChatPanelController controller = loader.getController();
            controller.setTeam(currentTeam.getId());
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(currentChatModal);
                currentChatModal = null;
                chatBubbleBtn.setVisible(true);
            });
            
            // Hide bubble when modal opens
            chatBubbleBtn.setVisible(false);
            
            rootStackPane.getChildren().add(currentChatModal);
            
        } catch (Exception e) {
            System.err.println("Error loading chat modal: " + e.getMessage());
            e.printStackTrace();
            currentChatModal = null;
            chatBubbleBtn.setVisible(true);
        }
    }

    // ── Public navigation API ────────────────────────────────────────

    public void showTeamDetail(Team team) {
        try {
            currentTeam = team;
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Fxml/Team/PlayerTeamDetail.fxml"));
            Parent detailRoot = loader.load();
            PlayerTeamDetailController controller = loader.getController();
            controller.setTeam(team);
            controller.setBrowserController(this);
            innerContainer.getChildren().setAll(detailRoot);
            
            // Show chat bubble for team members
            boolean isMember = crudTeamMember.isMember(team.getId(), currentUserId);
            boolean isCreator = team.getCreatedBy() != null && team.getCreatedBy().equals(currentUserId);
            chatBubbleBtn.setVisible(isMember || isCreator);
            chatBubbleBtn.setManaged(isMember || isCreator);
        } catch (Exception e) {
            System.err.println("Error loading team detail view");
            e.printStackTrace();
            showError("Impossible de charger les détails de l'équipe.");
        }
    }

    public void showTeamDetailViewOnly(Team team) {
        try {
            currentTeam = null;
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Fxml/Team/PlayerTeamDetail.fxml"));
            Parent detailRoot = loader.load();
            PlayerTeamDetailController controller = loader.getController();
            controller.setTeamViewOnly(team);
            controller.setBrowserController(this);
            innerContainer.getChildren().setAll(detailRoot);
            
            // Hide chat bubble when viewing other teams
            chatBubbleBtn.setVisible(false);
            chatBubbleBtn.setManaged(false);
        } catch (Exception e) {
            System.err.println("Error loading team detail view (view-only)");
            e.printStackTrace();
            showError("Impossible de charger les détails de l'équipe.");
        }
    }

    public void showTeamList() {
        try {
            currentTeam = null;
            // Hide chat bubble in team list
            chatBubbleBtn.setVisible(false);
            chatBubbleBtn.setManaged(false);
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

            Button btnCreate = new Button("＋ Créer une équipe");
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

            if (SessionManager.getInstance().getCurrentPlayer().getRole().equals("player")){
                btnCreate.setDisable(true);
                btnCreate.setVisible(false);
            }
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
                    System.out.println("✅ Team created: " + team.getName() + " (ID: " + team.getId() + ")");

                    // 2. Fetch the saved team to get its generated ID
                    Team savedTeam = getLastCreatedTeamByUser(currentUserId);

                    // 3. Auto-add creator as a member
                    if (savedTeam != null) {
                        System.out.println("🔄 Adding creator as team member...");
                        try {
                            crudTeamMember.addMember(savedTeam.getId(), currentUserId);
                            System.out.println("✅ Creator added as team member");
                        } catch (IllegalStateException ise) {
                            // Player already in another team
                            System.err.println("⚠️ Creator already in another team: " + ise.getMessage());
                            // Delete the newly created team since creator can't join
                            crudTeam.deleteEntity(savedTeam);
                            rootStackPane.getChildren().remove(modalOverlay);
                            Alert error = new Alert(Alert.AlertType.ERROR);
                            error.setTitle("Erreur");
                            error.setHeaderText("Vous êtes déjà membre d'une équipe");
                            error.setContentText("Quittez votre équipe actuelle avant d'en créer une nouvelle.");
                            error.showAndWait();
                            showTeamList();
                            return;
                        }
                    }

                    // 4. Close modal
                    rootStackPane.getChildren().remove(modalOverlay);

                    // 5. Navigate to the new team detail
                    if (savedTeam != null) {
                        System.out.println("✅ Navigating to new team detail");
                        showTeamDetail(savedTeam);
                    } else {
                        System.err.println("⚠️ Could not find saved team, returning to list");
                        showTeamList();
                    }
                } catch (Exception ex) {
                    System.err.println("❌ Error creating team: " + ex.getMessage());
                    ex.printStackTrace();
                    rootStackPane.getChildren().remove(modalOverlay);
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setHeaderText(null);
                    error.setContentText("Impossible de créer l'équipe: " + ex.getMessage());
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
