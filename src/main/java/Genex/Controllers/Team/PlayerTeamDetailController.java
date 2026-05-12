package Genex.Controllers.Team;

import Genex.entities.Game;
import Genex.entities.Player;
import Genex.entities.Team;
import Genex.entities.TrainingSession;
import Genex.services.CrudGame;
import Genex.services.CrudPlayer;
import Genex.services.CrudTeam;
import Genex.services.CrudTeamMember;
import Genex.services.CrudTrainingSession;
import Genex.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerTeamDetailController {

    // ── FXML fields ──────────────────────────────────────────────────
    @FXML private HBox navBar;
    @FXML private VBox mainContent;
    @FXML private Label teamNameLabel;
    @FXML private Button btnMonEquipe;
    @FXML private Button btnAutresEquipes;
    @FXML private Button btnQuitTeam;
    @FXML private HBox splitPanel;
    @FXML private VBox leftPanel;
    @FXML private VBox rightPanel;
    @FXML private ImageView teamLogo;
    @FXML private ImageView teamJersey;
    @FXML private Text teamIconFallback;
    @FXML private Text jerseyIconFallback;
    @FXML private Label infoGame;
    @FXML private Label infoContact;
    @FXML private Label infoStatus;
    @FXML private Label infoDate;
    @FXML private Label memberCountLabel;
    @FXML private VBox membersContainer;
    @FXML private StackPane calendarViewContainer;
    @FXML private StackPane innerContainer;
    @FXML private StackPane swapPane;
    @FXML private HBox scheduleManagementButtons;

    // ── State ────────────────────────────────────────────────────────
    private Team team;
    private boolean isCreator;
    private boolean isMember;
    private CrudTeamMember crudTeamMember;
    private CrudTrainingSession crudTrainingSession;
    private PlayerTeamBrowserController browserController;
    private TeamChatPanelController chatController;
    private CalendarViewController calendarViewController;
    private javafx.stage.Popup searchPopup;
    private javafx.scene.control.ListView<Team> searchSuggestions;
    private javafx.scene.control.TextField otherTeamsSearchField;

    @FXML
    public void initialize() {
        crudTeamMember = new CrudTeamMember();
        crudTrainingSession = new CrudTrainingSession();
    }

    // ── Public API ───────────────────────────────────────────────────

    public void setTeam(Team team) {
        setTeamInternal(team, false);
    }

    public void setTeamViewOnly(Team team) {
        setTeamInternal(team, true);
    }

    public void setBrowserController(PlayerTeamBrowserController controller) {
        this.browserController = controller;
    }

    private void loadChatPanel() {
        // Chat is now opened via modal from browser controller
        // This method is kept for compatibility but does nothing
    }

    public void openEditSessionModal(TrainingSession session) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTrainingSessionModal.fxml"));
            StackPane modalOverlay = loader.load();
            swapPane.getChildren().add(modalOverlay);
            AddTrainingSessionModalController controller = loader.getController();
            controller.setSession(session);
            controller.setOnSaveCallback(updatedSession -> {
                crudTrainingSession.updateSession(updatedSession);
                swapPane.getChildren().remove(modalOverlay);
                if (calendarViewController != null) {
                    calendarViewController.refresh();
                }
            });
            controller.setOnCloseCallback(() -> swapPane.getChildren().remove(modalOverlay));
        } catch (Exception e) {
            System.err.println("Error opening edit session modal");
            e.printStackTrace();
        }
    }

    // ── Tab handlers ─────────────────────────────────────────────────

    @FXML
    private void handleMonEquipe() {
        if (browserController != null) {
            String currentUserId = SessionManager.getInstance().getCurrentUserId();
            try {
                Team myTeam = crudTeamMember.getTeamByPlayer(currentUserId);
                if (myTeam != null) {
                    browserController.showTeamDetail(myTeam);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        showMainContent();
    }

    @FXML
    private void handleAutresEquipes() {
        mainContent.setVisible(false);
        mainContent.setManaged(false);
        innerContainer.setVisible(true);
        innerContainer.setManaged(true);
        setActiveTab(btnAutresEquipes, btnMonEquipe);
        teamNameLabel.setText("Autres équipes");
        loadOtherTeams();
    }

    @FXML
    private void handleQuitTeam() {
        if (team == null) return;
        String currentUserId = SessionManager.getInstance().getCurrentUserId();
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Quitter l'équipe");
        confirm.setHeaderText("Quitter \"" + team.getName() + "\" ?");
        confirm.setContentText("Vous ne serez plus membre de cette équipe.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    crudTeamMember.removeMember(team.getId(), currentUserId);
                    if (browserController != null) browserController.showTeamList();
                } catch (Exception e) {
                    System.err.println("Error quitting team: " + e.getMessage());
                    javafx.scene.control.Alert error = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setHeaderText(null);
                    error.setContentText("Impossible de quitter l'équipe.");
                    error.showAndWait();
                }
            }
        });
    }

    @FXML
    private void handleDeleteTeam() {
        if (team == null || !isCreator) return;

        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer l'équipe");
        confirm.setHeaderText("Supprimer \"" + team.getName() + "\" définitivement ?");
        confirm.setContentText("Tous les membres seront retirés et l'équipe sera supprimée. Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    new Genex.services.CrudTeam().deleteEntity(team);
                    // Navigate back to the team list
                    if (browserController != null) browserController.showTeamList();
                } catch (Exception e) {
                    System.err.println("Error deleting team: " + e.getMessage());
                    javafx.scene.control.Alert error = new javafx.scene.control.Alert(
                            javafx.scene.control.Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setHeaderText(null);
                    error.setContentText("Impossible de supprimer l'équipe.");
                    error.showAndWait();
                }
            }
        });
    }

    @FXML
    private void openAddSessionModal() {
        // This method is no longer used - sessions are added through the calendar view
        // Kept for compatibility
    }

    // ── Open Generate Schedule Modal ─────────────────────────────────
    @FXML
    private void openGenerateScheduleModal() {
        if (!isCreator) {
            System.out.println("Only team creator can generate schedule");
            return;
        }

        try {
            System.out.println("Opening Generate Schedule Modal...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/GenerateScheduleModal.fxml"));
            StackPane modalOverlay = loader.load();

            // Add modal overlay to the stack
            swapPane.getChildren().add(modalOverlay);

            // Get controller and set team
            GenerateScheduleModalController controller = loader.getController();
            controller.setTeam(team);

            controller.setOnSaveCallback(sessions -> {
                System.out.println("✅ Schedule saved: " + sessions.size() + " sessions");

                // Remove modal overlay and refresh calendar
                swapPane.getChildren().remove(modalOverlay);
                if (calendarViewController != null) {
                    calendarViewController.refresh();
                }
            });

            controller.setOnCloseCallback(() -> {
                swapPane.getChildren().remove(modalOverlay);
            });

        } catch (Exception e) {
            System.err.println("Error opening Generate Schedule Modal");
            e.printStackTrace();
        }
    }

    // ── Clear All Training Sessions ──────────────────────────────────
    @FXML
    private void clearAllTrainingSessions() {
        if (team == null || !isCreator) return;

        try {
            // Get all sessions for this team
            List<TrainingSession> sessions = crudTrainingSession.getSessionsByTeam(team.getId());

            if (sessions.isEmpty()) {
                javafx.scene.control.Alert info = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.INFORMATION);
                info.setTitle("Information");
                info.setHeaderText(null);
                info.setContentText("Aucune session d'entraînement à supprimer.");
                info.showAndWait();
                return;
            }

            // Confirmation dialog
            javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmer la suppression");
            confirm.setHeaderText("⚠️ ATTENTION : Action Irréversible");
            confirm.setContentText(
                    "Vous êtes sur le point de supprimer TOUTES les sessions d'entraînement de cette équipe.\n\n" +
                            "📊 " + sessions.size() + " session(s) seront supprimées définitivement.\n\n" +
                            "Cette action ne peut pas être annulée. Continuer ?"
            );

            // Custom button labels
            javafx.scene.control.ButtonType btnConfirm = new javafx.scene.control.ButtonType(
                    "Oui, tout supprimer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
            javafx.scene.control.ButtonType btnCancel = new javafx.scene.control.ButtonType(
                    "Annuler", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(btnConfirm, btnCancel);

            confirm.showAndWait().ifPresent(response -> {
                if (response == btnConfirm) {
                    try {
                        System.out.println("Deleting all training sessions for team: " + team.getName());

                        // Delete all sessions
                        int deletedCount = 0;
                        for (TrainingSession session : sessions) {
                            crudTrainingSession.deleteSession(session.getId());
                            deletedCount++;
                        }

                        System.out.println("✅ Deleted " + deletedCount + " training sessions");

                        // Refresh calendar
                        if (calendarViewController != null) {
                            calendarViewController.refresh();
                        }

                        // Show success message
                        javafx.scene.control.Alert success = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION);
                        success.setTitle("Succès");
                        success.setHeaderText("✅ Planning Effacé");
                        success.setContentText(deletedCount + " session(s) d'entraînement ont été supprimées.");
                        success.showAndWait();

                    } catch (Exception e) {
                        System.err.println("Error deleting training sessions: " + e.getMessage());
                        e.printStackTrace();

                        javafx.scene.control.Alert error = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR);
                        error.setTitle("Erreur");
                        error.setHeaderText(null);
                        error.setContentText("Impossible de supprimer les sessions d'entraînement.");
                        error.showAndWait();
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("Error in clearAllTrainingSessions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ── Private helpers ──────────────────────────────────────────────

    private void setTeamInternal(Team team, boolean viewOnly) {
        this.team = team;
        String currentUserId = SessionManager.getInstance().getCurrentUserId();
        isCreator = team.getCreatedBy() != null && team.getCreatedBy().equals(currentUserId);
        isMember = crudTeamMember.isMember(team.getId(), currentUserId);

        teamNameLabel.setText(team.getName());
        populateRightPanel();
        loadMembers();

        if (viewOnly) {
            navBar.setVisible(false);
            navBar.setManaged(false);
            calendarViewContainer.setVisible(false);
            calendarViewContainer.setManaged(false);
            addBackButton();
        } else {
            navBar.setVisible(true);
            navBar.setManaged(true);
            boolean showCalendar = isMember || isCreator;
            calendarViewContainer.setVisible(showCalendar);
            calendarViewContainer.setManaged(showCalendar);
            if (showCalendar) {
                loadCalendarView();
            }
            // Set active tab based on whether this is the user's team or another team
            if (isMember || isCreator) {
                setActiveTab(btnMonEquipe, btnAutresEquipes);
            } else {
                setActiveTab(btnAutresEquipes, btnMonEquipe);
            }
            boolean showQuit = isMember && !isCreator;
            btnQuitTeam.setVisible(showQuit);
            btnQuitTeam.setManaged(showQuit);
            
            // Schedule management buttons only for creator
            if (scheduleManagementButtons != null) {
                scheduleManagementButtons.setVisible(isCreator);
                scheduleManagementButtons.setManaged(isCreator);
            }
        }

        showMainContent();
    }

    private void addBackButton() {
        Button btnBack = new Button("← Retour aux équipes");
        btnBack.setStyle(
            "-fx-background-color: rgba(255,255,255,0.06);" +
            "-fx-text-fill: rgba(255,255,255,0.85);" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-shape: 'M 15 0 L 155 0 L 155 26 L 140 36 L 0 36 L 0 10 Z';" +
            "-fx-padding: 9 0;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: rgba(255,255,255,0.25);" +
            "-fx-border-width: 1;"
        );
        btnBack.setOnAction(e -> {
            if (browserController != null) browserController.showTeamList();
        });
        HBox backBar = new HBox(btnBack);
        backBar.setPadding(new Insets(12, 20, 4, 20));
        mainContent.getChildren().add(0, backBar);
    }

    private void showMainContent() {
        mainContent.setVisible(true);
        mainContent.setManaged(true);
        innerContainer.setVisible(false);
        innerContainer.setManaged(false);
    }

    private void populateRightPanel() {
        if (team == null) return;
        loadLogo();
        loadJersey();
        if (team.getGameId() != null) {
            String name = getGameNameById(team.getGameId());
            infoGame.setText(name != null ? name : "—");
        } else {
            infoGame.setText("—");
        }
        infoContact.setText(team.getContact() != null ? team.getContact() : "—");
        infoStatus.setText(team.getStatus() != null ? team.getStatus().name() : "—");
        infoDate.setText(team.getCreatedAt() != null
                ? team.getCreatedAt().toLocalDate().toString() : "—");
    }

    private void loadLogo() {
        if (team.getLogoImage() != null && !team.getLogoImage().isEmpty()) {
            try {
                File f = new File(team.getLogoImage());
                if (f.exists()) {
                    teamLogo.setImage(new Image(f.toURI().toString()));
                    teamLogo.setVisible(true);
                    teamIconFallback.setVisible(false);
                    return;
                }
            } catch (Exception e) {
                System.err.println("Error loading logo: " + e.getMessage());
            }
        }
        teamLogo.setVisible(false);
        teamIconFallback.setVisible(true);
    }

    private void loadJersey() {
        if (team.getJerseyImage() != null && !team.getJerseyImage().isEmpty()) {
            try {
                File f = new File(team.getJerseyImage());
                System.out.println("Loading jersey for team: " + team.getName());
                System.out.println("Jersey path: " + team.getJerseyImage());
                System.out.println("File exists: " + f.exists());
                if (f.exists()) {
                    teamJersey.setImage(new Image(f.toURI().toString()));
                    teamJersey.setVisible(true);
                    jerseyIconFallback.setVisible(false);
                    System.out.println("✅ Jersey loaded successfully");
                    return;
                } else {
                    System.out.println("⚠️ Jersey file not found");
                }
            } catch (Exception e) {
                System.err.println("❌ Error loading jersey: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("ℹ️ No jersey path set for team: " + (team != null ? team.getName() : "null"));
        }
        teamJersey.setVisible(false);
        jerseyIconFallback.setVisible(true);
    }

    private void loadMembers() {
        membersContainer.getChildren().clear();
        List<Player> members = crudTeamMember.getMembersByTeam(team.getId());
        memberCountLabel.setText(members.size() + " / " + CrudTeamMember.MAX_MEMBERS + " membres");

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
                    
                    // Add kick button (only visible to creator)
                    if (isCreator) {
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
                }
                
                membersContainer.getChildren().add(row);
            } else {
                // Empty slot
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER);
                row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8; -fx-padding: 8 12 8 12; -fx-border-color: rgba(255,255,255,0.1); -fx-border-width: 1; -fx-border-style: dashed; -fx-border-radius: 8;");

                if (isCreator) {
                    // Show circular "+" button for creator
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
                } else {
                    // Show empty slot text for non-creators
                    Label emptyLabel = new Label("Emplacement libre");
                    emptyLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.25); -fx-font-style: italic; -fx-font-size: 11px;");
                    row.getChildren().add(emptyLabel);
                }
                
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
            swapPane.getChildren().add(modalOverlay);
            
            AddPlayerToTeamModalController controller = loader.getController();
            controller.setTeam(team);
            controller.setOnPlayerAddedCallback(player -> {
                loadMembers(); // Refresh the members list
            });
            controller.setOnCloseCallback(() -> 
                swapPane.getChildren().remove(modalOverlay));
            
        } catch (Exception e) {
            System.err.println("Error opening add player modal: " + e.getMessage());
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ouvrir le modal d'ajout de joueur.");
            alert.showAndWait();
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

    private void loadCalendarView() {
        calendarViewContainer.getChildren().clear();
        if (team == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/CalendarView.fxml"));
            VBox calendarView = loader.load();
            
            calendarViewController = loader.getController();
            calendarViewController.setTeamId(team.getId());
            calendarViewController.setRootStackPane(swapPane);
            calendarViewController.setIsCreator(isCreator);
            
            calendarViewContainer.getChildren().add(calendarView);
            
        } catch (Exception e) {
            System.err.println("Error loading calendar view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadOtherTeams() {
        System.out.println("=== Loading Other Teams ===");
        innerContainer.getChildren().clear();
        try {
            VBox wrapper = new VBox(16);
            wrapper.setPadding(new Insets(20));
            wrapper.setStyle("-fx-background-color: #0d0d1a;");

            // Title and Search Bar
            HBox headerBox = new HBox(16);
            headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Label title = new Label("Autres équipes");
            title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            
            // Search field
            otherTeamsSearchField = new javafx.scene.control.TextField();
            otherTeamsSearchField.setPromptText("Rechercher une équipe...");
            otherTeamsSearchField.setPrefWidth(300);
            otherTeamsSearchField.setStyle(
                "-fx-background-color: #1a1a2e;" +
                "-fx-text-fill: white;" +
                "-fx-prompt-text-fill: rgba(255,255,255,0.4);" +
                "-fx-border-color: rgba(139,13,13,0.4);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 12;"
            );
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            
            headerBox.getChildren().addAll(title, spacer, otherTeamsSearchField);
            wrapper.getChildren().add(headerBox);

            FlowPane flow = new FlowPane();
            flow.setHgap(16);
            flow.setVgap(16);
            flow.setPrefWrapLength(1100);

            List<Team> allTeams = new CrudTeam().getAll();
            System.out.println("Total teams found: " + allTeams.size());
            
            // Get current user's team to exclude it
            String currentUserId = SessionManager.getInstance().getCurrentUserId();
            Team myTeam = crudTeamMember.getTeamByPlayer(currentUserId);
            System.out.println("Current user's team: " + (myTeam != null ? myTeam.getName() : "none"));

            List<Team> otherTeamsList = new java.util.ArrayList<>();
            for (Team t : allTeams) {
                System.out.println("Processing team: " + t.getName() + " (Status: " + t.getStatus() + ")");
                // Exclude the user's own team from the list
                if (myTeam != null && t.getId().equals(myTeam.getId())) {
                    System.out.println("  Skipping - user's own team");
                    continue;
                }
                otherTeamsList.add(t);
            }

            // Display all other teams initially
            displayOtherTeamsCards(flow, otherTeamsList);

            if (otherTeamsList.isEmpty()) {
                System.out.println("No other teams available");
                Label empty = new Label("Aucune autre équipe disponible.");
                empty.setStyle("-fx-text-fill: rgba(255,255,255,0.35); -fx-font-style: italic;");
                wrapper.getChildren().add(empty);
            } else {
                System.out.println("Adding " + otherTeamsList.size() + " team cards to view");
                wrapper.getChildren().add(flow);
                
                // Setup search autocomplete
                setupOtherTeamsSearch(otherTeamsSearchField, otherTeamsList, flow);
            }

            ScrollPane scroll = new ScrollPane(wrapper);
            scroll.setFitToWidth(true);
            scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
            StackPane.setAlignment(scroll, Pos.TOP_LEFT);
            innerContainer.getChildren().add(scroll);
            System.out.println("✅ Other teams loaded successfully");
        } catch (Exception e) {
            System.err.println("❌ Error loading other teams");
            e.printStackTrace();
        }
    }
    
    private void setupOtherTeamsSearch(javafx.scene.control.TextField searchField, List<Team> allTeams, FlowPane flow) {
        // Create suggestions dropdown
        searchSuggestions = new javafx.scene.control.ListView<>();
        searchSuggestions.setPrefWidth(searchField.getWidth());
        searchSuggestions.setPrefHeight(200);
        searchSuggestions.setMaxHeight(200);
        searchSuggestions.setStyle(
            "-fx-background-color: #1a1a2e;" +
            "-fx-border-color: #8B0D0D;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0, 0, 5);"
        );
        
        // Create popup
        searchPopup = new javafx.stage.Popup();
        searchPopup.setAutoHide(true);
        searchPopup.getContent().add(searchSuggestions);
        
        // Custom cell factory
        searchSuggestions.setCellFactory(lv -> new javafx.scene.control.ListCell<Team>() {
            @Override
            protected void updateItem(Team team, boolean empty) {
                super.updateItem(team, empty);
                if (empty || team == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox container = new VBox(4);
                    container.setStyle("-fx-padding: 8;");
                    
                    Label nameLabel = new Label(team.getName());
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
                    
                    Label contactLabel = new Label(team.getContact() != null ? team.getContact() : "—");
                    contactLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 11px;");
                    
                    container.getChildren().addAll(nameLabel, contactLabel);
                    setGraphic(container);
                    setText(null);
                    
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                    setOnMouseEntered(e -> setStyle("-fx-background-color: rgba(139,13,13,0.3); -fx-padding: 0;"));
                    setOnMouseExited(e -> setStyle("-fx-background-color: transparent; -fx-padding: 0;"));
                }
            }
        });
        
        // Handle selection
        searchSuggestions.setOnMouseClicked(e -> {
            Team selected = searchSuggestions.getSelectionModel().getSelectedItem();
            if (selected != null && browserController != null) {
                browserController.showTeamDetailViewOnly(selected);
                searchPopup.hide();
                searchField.clear();
            }
        });
        
        // Listen to text changes
        searchField.textProperty().addListener((obs, old, val) -> {
            if (val == null || val.trim().isEmpty()) {
                searchPopup.hide();
            } else {
                String searchText = val.toLowerCase();
                List<Team> matches = allTeams.stream()
                    .filter(t -> t.getName().toLowerCase().startsWith(searchText))
                    .limit(5)
                    .toList();
                
                if (!matches.isEmpty()) {
                    searchSuggestions.getItems().setAll(matches);
                    
                    if (!searchPopup.isShowing()) {
                        javafx.geometry.Bounds bounds = searchField.localToScreen(searchField.getBoundsInLocal());
                        if (bounds != null) {
                            searchPopup.show(searchField, bounds.getMinX(), bounds.getMaxY());
                        }
                    }
                } else {
                    searchPopup.hide();
                }
            }
        });
        
        // Update popup width
        searchField.widthProperty().addListener((obs, old, newWidth) -> {
            searchSuggestions.setPrefWidth(newWidth.doubleValue());
        });
    }
    
    private void displayOtherTeamsCards(FlowPane flow, List<Team> teams) {
        flow.getChildren().clear();
        for (Team t : teams) {
            try {
                System.out.println("  Loading card for team: " + t.getName());
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/Fxml/Team/PlayerTeamCard.fxml"));
                Parent card = loader.load();
                PlayerTeamCardController cc = loader.getController();
                cc.setTeam(t);
                cc.setBrowserController(browserController);
                cc.setReadOnly(true);
                flow.getChildren().add(card);
            } catch (Exception e) {
                System.err.println("Error loading team card: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void setActiveTab(Button active, Button inactive) {
        active.getStyleClass().remove("player-tab-btn");
        if (!active.getStyleClass().contains("player-tab-btn-active"))
            active.getStyleClass().add("player-tab-btn-active");
        inactive.getStyleClass().remove("player-tab-btn-active");
        if (!inactive.getStyleClass().contains("player-tab-btn"))
            inactive.getStyleClass().add("player-tab-btn");
    }

    private String getGameNameById(String gameId) {
        try {
            for (Game g : new CrudGame().getgames())
                if (g.getId() != null && g.getId().equals(gameId)) return g.getNom();
        } catch (Exception e) {
            System.err.println("Error fetching game name: " + e.getMessage());
        }
        return null;
    }
}
