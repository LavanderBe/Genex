package Genex.Controllers.Team;

import Genex.entities.Game;
import Genex.entities.Player;
import Genex.entities.Team;
import Genex.entities.TrainingSession;
import Genex.services.CrudGame;
import Genex.services.CrudTeamMember;
import Genex.services.CrudTrainingSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.util.List;

public class TeamDetailController {

    @FXML private StackPane rootStackPane;
    @FXML private VBox contentArea;
    @FXML private Button btnBack;
    @FXML private Label teamNameTitle;
    @FXML private Text teamTag;
    @FXML private Label teamGame;
    @FXML private Label teamType;
    @FXML private Text teamStatus;
    @FXML private Text teamFounded;
    @FXML private Label memberCountLabel;
    @FXML private VBox membersContainer;
    @FXML private StackPane calendarViewContainer;
    @FXML private javafx.scene.image.ImageView teamLogoImage;
    @FXML private Text teamLogoFallback;
    @FXML private javafx.scene.image.ImageView teamJerseyImage;
    @FXML private Text teamJerseyFallback;

    private Team team;
    private CrudTrainingSession crudTrainingSession;
    private CrudTeamMember crudTeamMember;
    private CalendarViewController calendarViewController;

    @FXML
    public void initialize() {
        System.out.println("TeamDetailController initialized");
        crudTrainingSession = new CrudTrainingSession();
        crudTeamMember = new CrudTeamMember();
    }

    public void setTeam(Team team) {
        this.team = team;
        updateTeamInfo();
        loadTeamVisuals();
        loadMembers();
        loadCalendarView();
    }

    // ── Open Add Session Modal (Tournament pattern) ──────────────────
    @FXML
    private void openAddSessionModal() {
        // This method is no longer used - sessions are added through the calendar view
        // Kept for compatibility
    }

    // ── Called by TrainingSessionCardController to open edit modal ────
    public void openEditSessionModal(TrainingSession session) {
        try {
            System.out.println("Opening Edit Training Session Modal...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTrainingSessionModal.fxml"));
            StackPane modalOverlay = loader.load();

            // Add modal overlay to the stack
            rootStackPane.getChildren().add(modalOverlay);

            // Get controller and set session data
            AddTrainingSessionModalController controller = loader.getController();
            controller.setSession(session);

            controller.setOnSaveCallback(updatedSession -> {
                System.out.println("Updating session: " + updatedSession.getTitle());

                // Update in database
                crudTrainingSession.updateSession(updatedSession);

                // Remove modal overlay and reload
                rootStackPane.getChildren().remove(modalOverlay);
                if (calendarViewController != null) {
                    calendarViewController.refresh();
                }
            });

            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(modalOverlay);
            });

        } catch (Exception e) {
            System.err.println("Error opening Edit Training Session Modal");
            e.printStackTrace();
        }
    }

    // ── Team info display ────────────────────────────────────────────
    private void updateTeamInfo() {
        if (team == null) return;
        teamNameTitle.setText(team.getName());
        teamTag.setText(team.getStatus() != null ? "[" + team.getStatus().name() + "]" : "[ACTIVE]");
        if (team.getGameId() != null) {
            String name = getGameNameById(team.getGameId());
            teamGame.setText(name != null ? name : "Unknown Game");
        } else {
            teamGame.setText("—");
        }
        teamType.setText(team.getContact() != null ? team.getContact() : "—");
        teamStatus.setText(team.getStatus() != null ? team.getStatus().name() : "ACTIVE");
        teamFounded.setText(team.getCreatedAt() != null
                ? team.getCreatedAt().toLocalDate().toString() : "—");
    }

    // ── Team visuals (logo and jersey) ──────────────────────────────
    private void loadTeamVisuals() {
        if (team == null) return;

        // Load team logo
        if (team.getLogoImage() != null && !team.getLogoImage().isEmpty()) {
            try {
                java.io.File logoFile = new java.io.File(team.getLogoImage());
                if (logoFile.exists()) {
                    javafx.scene.image.Image logoImage = new javafx.scene.image.Image(logoFile.toURI().toString());
                    teamLogoImage.setImage(logoImage);
                    teamLogoImage.setVisible(true);
                    teamLogoFallback.setVisible(false);
                    System.out.println("✅ Loaded logo for team: " + team.getName() + " from " + team.getLogoImage());
                } else {
                    System.out.println("⚠️ Logo file not found: " + team.getLogoImage());
                    showLogoFallback();
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading team logo: " + e.getMessage());
                e.printStackTrace();
                showLogoFallback();
            }
        } else {
            System.out.println("ℹ️ No logo path set for team: " + team.getName());
            showLogoFallback();
        }

        // Load team jersey
        if (team.getJerseyImage() != null && !team.getJerseyImage().isEmpty()) {
            try {
                java.io.File jerseyFile = new java.io.File(team.getJerseyImage());
                if (jerseyFile.exists()) {
                    javafx.scene.image.Image jerseyImage = new javafx.scene.image.Image(jerseyFile.toURI().toString());
                    teamJerseyImage.setImage(jerseyImage);
                    teamJerseyImage.setVisible(true);
                    teamJerseyFallback.setVisible(false);
                    System.out.println("✅ Loaded jersey for team: " + team.getName() + " from " + team.getJerseyImage());
                } else {
                    System.out.println("⚠️ Jersey file not found: " + team.getJerseyImage());
                    showJerseyFallback();
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading team jersey: " + e.getMessage());
                e.printStackTrace();
                showJerseyFallback();
            }
        } else {
            System.out.println("ℹ️ No jersey path set for team: " + team.getName());
            showJerseyFallback();
        }
    }

    private void showLogoFallback() {
        if (teamLogoImage != null) teamLogoImage.setVisible(false);
        if (teamLogoFallback != null) teamLogoFallback.setVisible(true);
    }

    private void showJerseyFallback() {
        if (teamJerseyImage != null) teamJerseyImage.setVisible(false);
        if (teamJerseyFallback != null) teamJerseyFallback.setVisible(true);
    }

    private String getGameNameById(String gameId) {
        try {
            for (Game g : new CrudGame().getgames())
                if (g.getId() != null && g.getId().equals(gameId)) return g.getNom();
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ── Sessions display ─────────────────────────────────────────────
    private void loadCalendarView() {
        calendarViewContainer.getChildren().clear();
        if (team == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/CalendarView.fxml"));
            VBox calendarView = loader.load();

            calendarViewController = loader.getController();
            calendarViewController.setTeamId(team.getId());
            calendarViewController.setRootStackPane(rootStackPane);
            calendarViewController.setIsCreator(true); // Admin is always creator

            calendarViewContainer.getChildren().add(calendarView);

        } catch (Exception e) {
            System.err.println("Error loading calendar view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Members display ──────────────────────────────────────────────
    private void loadMembers() {
        if (membersContainer == null) return; // Skip if FXML doesn't have members section

        membersContainer.getChildren().clear();
        List<Player> members = crudTeamMember.getMembersByTeam(team.getId());

        if (memberCountLabel != null) {
            memberCountLabel.setText(members.size() + " / " + CrudTeamMember.MAX_MEMBERS + " membres");
        }

        // Always show 5 slots (MAX_MEMBERS)
        for (int i = 0; i < CrudTeamMember.MAX_MEMBERS; i++) {
            if (i < members.size()) {
                // Existing member slot
                Player p = members.get(i);
                boolean isTeamCreator = team.getCreatedBy() != null && team.getCreatedBy().equals(p.getId());

                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);

                // Different background color for creator
                if (isTeamCreator) {
                    row.setStyle("-fx-background-color: rgba(218,165,32,0.15); -fx-background-radius: 8; -fx-padding: 8 12 8 12; -fx-border-color: rgba(218,165,32,0.4); -fx-border-width: 1; -fx-border-radius: 8;");
                } else {
                    row.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-padding: 8 12 8 12;");
                }

                String nickname = p.getNickname() != null ? p.getNickname() : p.getUsername();
                String fullName = ((p.getPrenom() != null ? p.getPrenom() : "") + " " +
                        (p.getNom() != null ? p.getNom() : "")).trim();

                Label nickLabel = new Label(nickname);
                if (isTeamCreator) {
                    nickLabel.setStyle("-fx-text-fill: #DAA520; -fx-font-weight: bold; -fx-font-size: 13px;");
                } else {
                    nickLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
                }

                Label nameLabel = new Label(fullName.isEmpty() ? "" : "— " + fullName);
                if (isTeamCreator) {
                    nameLabel.setStyle("-fx-text-fill: rgba(218,165,32,0.7); -fx-font-size: 11px;");
                } else {
                    nameLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 11px;");
                }

                // Add crown icon for creator
                if (isTeamCreator) {
                    Label crownIcon = new Label("👑");
                    crownIcon.setStyle("-fx-font-size: 14px;");
                    row.getChildren().addAll(crownIcon, nickLabel, nameLabel);
                } else {
                    row.getChildren().addAll(nickLabel, nameLabel);

                    // Add spacer
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    row.getChildren().add(spacer);

                    // Add kick button (admin can kick any non-creator member)
                    Button btnKick = new Button("Kick");
                    btnKick.setStyle(
                            "-fx-background-color: rgba(139,13,13,0.3);" +
                                    "-fx-text-fill: #ff6b6b;" +
                                    "-fx-font-size: 11px;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-padding: 4 12;" +
                                    "-fx-background-radius: 6px;" +
                                    "-fx-border-radius: 6px;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-border-color: rgba(255,107,107,0.3);" +
                                    "-fx-border-width: 1;"
                    );
                    btnKick.setOnMouseEntered(e ->
                            btnKick.setStyle(
                                    "-fx-background-color: #8B0D0D;" +
                                            "-fx-text-fill: white;" +
                                            "-fx-font-size: 11px;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-padding: 4 12;" +
                                            "-fx-background-radius: 6px;" +
                                            "-fx-border-radius: 6px;" +
                                            "-fx-cursor: hand;" +
                                            "-fx-border-color: rgba(255,255,255,0.5);" +
                                            "-fx-border-width: 1;"
                            )
                    );
                    btnKick.setOnMouseExited(e ->
                            btnKick.setStyle(
                                    "-fx-background-color: rgba(139,13,13,0.3);" +
                                            "-fx-text-fill: #ff6b6b;" +
                                            "-fx-font-size: 11px;" +
                                            "-fx-font-weight: bold;" +
                                            "-fx-padding: 4 12;" +
                                            "-fx-background-radius: 6px;" +
                                            "-fx-border-radius: 6px;" +
                                            "-fx-cursor: hand;" +
                                            "-fx-border-color: rgba(255,107,107,0.3);" +
                                            "-fx-border-width: 1;"
                            )
                    );
                    btnKick.setOnAction(e -> kickPlayer(p));
                    row.getChildren().add(btnKick);
                }

                membersContainer.getChildren().add(row);
            } else {
                // Empty slot - admin can add players
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER);
                row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8; -fx-padding: 8 12 8 12; -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-border-style: dashed; -fx-border-radius: 8;");

                // Show circular "+" button for admin
                Button btnAddPlayer = new Button("+");
                btnAddPlayer.setStyle(
                        "-fx-background-color: #8B0D0D;" +
                                "-fx-text-fill: #FFFFFF;" +
                                "-fx-font-size: 24px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-font-family: 'Arial';" +
                                "-fx-min-width: 40px;" +
                                "-fx-min-height: 40px;" +
                                "-fx-max-width: 40px;" +
                                "-fx-max-height: 40px;" +
                                "-fx-pref-width: 40px;" +
                                "-fx-pref-height: 40px;" +
                                "-fx-background-radius: 20px;" +
                                "-fx-border-radius: 20px;" +
                                "-fx-cursor: hand;" +
                                "-fx-border-color: rgba(255,255,255,0.3);" +
                                "-fx-border-width: 1.5;" +
                                "-fx-padding: 0;" +
                                "-fx-alignment: center;"
                );
                btnAddPlayer.setOnMouseEntered(e ->
                        btnAddPlayer.setStyle(
                                "-fx-background-color: #A01010;" +
                                        "-fx-text-fill: #FFFFFF;" +
                                        "-fx-font-size: 24px;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-family: 'Arial';" +
                                        "-fx-min-width: 40px;" +
                                        "-fx-min-height: 40px;" +
                                        "-fx-max-width: 40px;" +
                                        "-fx-max-height: 40px;" +
                                        "-fx-pref-width: 40px;" +
                                        "-fx-pref-height: 40px;" +
                                        "-fx-background-radius: 20px;" +
                                        "-fx-border-radius: 20px;" +
                                        "-fx-cursor: hand;" +
                                        "-fx-border-color: rgba(255,255,255,0.5);" +
                                        "-fx-border-width: 1.5;" +
                                        "-fx-padding: 0;" +
                                        "-fx-alignment: center;"
                        )
                );
                btnAddPlayer.setOnMouseExited(e ->
                        btnAddPlayer.setStyle(
                                "-fx-background-color: #8B0D0D;" +
                                        "-fx-text-fill: #FFFFFF;" +
                                        "-fx-font-size: 24px;" +
                                        "-fx-font-weight: bold;" +
                                        "-fx-font-family: 'Arial';" +
                                        "-fx-min-width: 40px;" +
                                        "-fx-min-height: 40px;" +
                                        "-fx-max-width: 40px;" +
                                        "-fx-max-height: 40px;" +
                                        "-fx-pref-width: 40px;" +
                                        "-fx-pref-height: 40px;" +
                                        "-fx-background-radius: 20px;" +
                                        "-fx-border-radius: 20px;" +
                                        "-fx-cursor: hand;" +
                                        "-fx-border-color: rgba(255,255,255,0.3);" +
                                        "-fx-border-width: 1.5;" +
                                        "-fx-padding: 0;" +
                                        "-fx-alignment: center;"
                        )
                );
                btnAddPlayer.setOnAction(e -> openAddPlayerModal());
                row.getChildren().add(btnAddPlayer);

                membersContainer.getChildren().add(row);
            }
        }
    }

    private void openAddPlayerModal() {
        if (team == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Fxml/Team/AddPlayerToTeamModal.fxml"));
            StackPane modalOverlay = loader.load();
            rootStackPane.getChildren().add(modalOverlay);

            AddPlayerToTeamModalController controller = loader.getController();
            controller.setTeam(team);
            controller.setOnPlayerAddedCallback(player -> {
                loadMembers(); // Refresh the members list
            });
            controller.setOnCloseCallback(() ->
                    rootStackPane.getChildren().remove(modalOverlay));

        } catch (Exception e) {
            System.err.println("Error opening add player modal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void kickPlayer(Player player) {
        if (team == null || player == null) return;

        // Confirm kick action
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Retirer le joueur");
        confirm.setHeaderText("Retirer \"" + (player.getNickname() != null ? player.getNickname() : player.getUsername()) + "\" de l'équipe ?");
        confirm.setContentText("Ce joueur sera retiré de l'équipe et pourra rejoindre une autre équipe.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    crudTeamMember.removeMember(team.getId(), player.getId());
                    loadMembers(); // Refresh the members list

                    // Show success message
                    javafx.scene.control.Alert success = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.INFORMATION);
                    success.setTitle("Succès");
                    success.setHeaderText(null);
                    success.setContentText("Le joueur a été retiré de l'équipe.");
                    success.showAndWait();

                } catch (Exception e) {
                    System.err.println("Error kicking player: " + e.getMessage());
                    e.printStackTrace();
                    javafx.scene.control.Alert error = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setHeaderText(null);
                    error.setContentText("Impossible de retirer le joueur de l'équipe.");
                    error.showAndWait();
                }
            }
        });
    }

    // ── Back navigation ──────────────────────────────────────────────
    @FXML
    private void handleBack() {
        try {
            javafx.scene.Node node = btnBack;
            javafx.scene.layout.Pane container = null;
            while (node != null) {
                if (node instanceof javafx.scene.layout.AnchorPane) {
                    container = (javafx.scene.layout.Pane) node; break;
                }
                node = node.getParent();
            }
            if (container != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/TeamHub.fxml"));
                Parent hub = loader.load();
                TeamHubController hc = loader.getController();
                hc.setContentContainer(container);
                container.getChildren().clear();
                container.getChildren().add(hub);
                if (container instanceof javafx.scene.layout.AnchorPane ap) {
                    javafx.scene.layout.AnchorPane.setTopAnchor(hub, 0.0);
                    javafx.scene.layout.AnchorPane.setBottomAnchor(hub, 0.0);
                    javafx.scene.layout.AnchorPane.setLeftAnchor(hub, 0.0);
                    javafx.scene.layout.AnchorPane.setRightAnchor(hub, 0.0);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}