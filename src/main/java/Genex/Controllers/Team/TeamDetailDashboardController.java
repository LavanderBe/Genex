package Genex.Controllers.Team;

import Genex.entities.Game;
import Genex.entities.Team;
import Genex.entities.TeamMember;
import Genex.entities.TrainingSession;
import Genex.services.*;
import Genex.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class TeamDetailDashboardController {

    @FXML private StackPane rootStackPane;
    @FXML private VBox contentArea;
    @FXML private Button btnBack;
    @FXML private Label teamNameTitle;
    @FXML private Button btnEditTeam;
    
    // Left Panel - Team Members
    @FXML private FlowPane teamMembersContainer;
    @FXML private Label lblMemberCount;
    
    // Right Panel - Team Info
    @FXML private ImageView teamLogoImage;
    @FXML private Label lblGameName;
    @FXML private Label lblStatus;
    @FXML private Label lblContact;
    
    // Calendar
    @FXML private WebView calendarWebView;
    @FXML private Label lblCurrentMonth;
    @FXML private Button btnPrevMonth;
    @FXML private Button btnNextMonth;
    
    // Sessions List
    @FXML private VBox sessionsListContainer;
    @FXML private Label lblSelectedDate;
    @FXML private Button btnAddSessionQuick;

    private Team team;
    private CrudTrainingSession crudTrainingSession;
    private CrudTeamMember crudTeamMember;
    private YearMonth currentMonth;
    private String currentUserId;
    private boolean isAdmin;
    private javafx.beans.value.ChangeListener<javafx.concurrent.Worker.State> webViewListener;
    private LocalDate selectedDate; // Track selected calendar date
    private List<TrainingSession> cachedSessions; // Cache sessions to avoid repeated DB queries

    @FXML
    public void initialize() {
        System.out.println("TeamDetailDashboardController initialized");
        crudTrainingSession = new CrudTrainingSession();
        crudTeamMember = new CrudTeamMember();
        
        currentMonth = YearMonth.now();
        currentUserId = SessionManager.getInstance().getCurrentUserId();
        isAdmin = SessionManager.getInstance().isAdmin();
    }

    public void setTeam(Team team) {
        this.team = team;
        updateTeamInfo();
        loadTeamMembers();
        loadCalendar();
        updateAddSessionButton();
    }

    // ══════════════════════════════════════════════════════════════════
    // TEAM INFO DISPLAY
    // ══════════════════════════════════════════════════════════════════

    private void updateTeamInfo() {
        if (team == null) return;
        
        teamNameTitle.setText(team.getName());
        
        // Load team logo
        if (team.getLogoImage() != null && !team.getLogoImage().isEmpty()) {
            File logoFile = new File(team.getLogoImage());
            if (logoFile.exists()) {
                teamLogoImage.setImage(new Image(logoFile.toURI().toString()));
            }
        }
        
        // Game name
        if (team.getGameId() != null) {
            String gameName = getGameNameById(team.getGameId());
            lblGameName.setText("Game: " + (gameName != null ? gameName : "Unknown"));
        }
        
        // Status
        lblStatus.setText("Status: " + (team.getStatus() != null ? team.getStatus().name() : "N/A"));
        
        // Contact
        lblContact.setText("Contact: " + (team.getContact() != null ? team.getContact() : "N/A"));
    }

    private String getGameNameById(String gameId) {
        try {
            for (Game g : new CrudGame().getgames()) {
                if (g.getId() != null && g.getId().equals(gameId)) {
                    return g.getNom();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════════
    // TEAM MEMBERS
    // ══════════════════════════════════════════════════════════════════

    private void loadTeamMembers() {
        teamMembersContainer.getChildren().clear();
        
        try {
            List<TeamMember> members = crudTeamMember.getMembersByTeam(team.getId());
            
            // Update member count
            lblMemberCount.setText("Members: " + members.size() + "/5");
            
            for (TeamMember member : members) {
                HBox memberCard = createMemberCard(member);
                teamMembersContainer.getChildren().add(memberCard);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HBox createMemberCard(TeamMember member) {
        HBox card = new HBox(10);
        card.setStyle("-fx-padding: 12; -fx-background-color: rgba(255, 255, 255, 0.05); " +
                     "-fx-background-radius: 10; -fx-border-color: rgba(255, 255, 255, 0.08); " +
                     "-fx-border-radius: 10; -fx-border-width: 1; -fx-min-width: 180; -fx-pref-width: 180; -fx-min-height: 50;");
        card.setAlignment(Pos.CENTER_LEFT);
        
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER_LEFT);
        
        Label nameLabel = new Label("👤 " + member.getPlayerName());
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        content.getChildren().add(nameLabel);
        
        // If admin or current user is the member, show remove button
        if (isAdmin || member.getPlayerId().equals(currentUserId)) {
            Button btnRemove = new Button("Remove");
            btnRemove.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                             "-fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 5; -fx-cursor: hand;");
            btnRemove.setOnAction(e -> removeMember(member));
            content.getChildren().add(btnRemove);
        }
        
        card.getChildren().add(content);
        HBox.setHgrow(content, javafx.scene.layout.Priority.ALWAYS);
        
        return card;
    }

    private void updateAddSessionButton() {
        // Show Add Session button only for admin
        if (isAdmin) {
            btnAddSessionQuick.setVisible(true);
            btnAddSessionQuick.setManaged(true);
        } else {
            btnAddSessionQuick.setVisible(false);
            btnAddSessionQuick.setManaged(false);
        }
    }

    private void removeMember(TeamMember member) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Member");
        confirm.setHeaderText("Remove " + member.getPlayerName() + " from the team?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    crudTeamMember.removeMember(member.getPlayerId());
                    loadTeamMembers();
                } catch (Exception e) {
                    showAlert("Error", e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════
    // CALENDAR
    // ══════════════════════════════════════════════════════════════════

    private void loadCalendar() {
        lblCurrentMonth.setText(currentMonth.getMonth().name() + " " + currentMonth.getYear());
        
        // Cache sessions for the team (load once, reuse for filtering)
        cachedSessions = crudTrainingSession.getSessionsByTeam(team.getId());
        
        // Generate calendar HTML with training sessions
        String calendarHTML = generateCalendarHTML();
        
        WebEngine engine = calendarWebView.getEngine();
        
        // Remove old listener if exists
        if (webViewListener != null) {
            engine.getLoadWorker().stateProperty().removeListener(webViewListener);
        }
        
        // Create and add new listener
        webViewListener = (obs, old, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaApp", new JavaScriptBridge());
                } catch (Exception e) {
                    System.err.println("Error attaching JavaScript bridge: " + e.getMessage());
                }
            }
        };
        engine.getLoadWorker().stateProperty().addListener(webViewListener);
        
        // Load content
        engine.loadContent(calendarHTML);
    }

    private String generateCalendarHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>");
        html.append("body { font-family: Arial; margin: 0; padding: 20px; background: white; }");
        html.append("table { width: 100%; border-collapse: collapse; }");
        html.append("th, td { border: 1px solid #ddd; padding: 10px; text-align: center; height: 80px; vertical-align: top; }");
        html.append("th { background-color: #3498db; color: white; }");
        html.append("td { cursor: pointer; }");
        html.append("td:hover { background-color: #ecf0f1; }");
        html.append(".day-number { font-weight: bold; margin-bottom: 5px; }");
        html.append(".session { font-size: 10px; padding: 2px; margin: 2px 0; border-radius: 3px; color: white; }");
        html.append(".scrim { background-color: #3498db; }");
        html.append(".aim { background-color: #e74c3c; }");
        html.append(".strategy { background-color: #2ecc71; }");
        html.append(".practice { background-color: #f39c12; }");
        html.append(".other { background-color: #95a5a6; }");
        html.append("</style></head><body>");
        
        html.append("<table>");
        html.append("<tr><th>Mon</th><th>Tue</th><th>Wed</th><th>Thu</th><th>Fri</th><th>Sat</th><th>Sun</th></tr>");
        
        // Use cached sessions
        List<TrainingSession> sessions = cachedSessions != null ? cachedSessions : List.of();
        
        LocalDate firstDay = currentMonth.atDay(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        int daysInMonth = currentMonth.lengthOfMonth();
        
        html.append("<tr>");
        
        // Empty cells before first day
        for (int i = 1; i < dayOfWeek; i++) {
            html.append("<td></td>");
        }
        
        // Days of month
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            html.append("<td onclick='javaApp.onDayClick(\"").append(date).append("\")'>"); html.append("<div class='day-number'>").append(day).append("</div>");
            
            // Add sessions for this day
            for (TrainingSession session : sessions) {
                if (session.getSessionDatetime() != null && 
                    session.getSessionDatetime().toLocalDate().equals(date)) {
                    String cssClass = getSessionCssClass(session.getType());
                    html.append("<div class='session ").append(cssClass).append("'>")
                        .append(session.getTitle())
                        .append("</div>");
                }
            }
            
            html.append("</td>");
            
            // New row after Sunday
            if ((dayOfWeek + day - 1) % 7 == 0) {
                html.append("</tr><tr>");
            }
        }
        
        html.append("</tr></table>");
        html.append("</body></html>");
        
        return html.toString();
    }

    private String getSessionCssClass(TrainingSession.Type type) {
        if (type == null) return "other";
        return switch (type) {
            case SCRIM -> "scrim";
            case AIM_TRAINING -> "aim";
            case STRATEGY -> "strategy";
            case TEAM_PRACTICE -> "practice";
            case OTHER -> "other";
        };
    }

    public class JavaScriptBridge {
        public void onDayClick(String dateStr) {
            long startTime = System.currentTimeMillis();
            Platform.runLater(() -> {
                try {
                    LocalDate date = LocalDate.parse(dateStr);
                    handleDayClick(date);
                    long endTime = System.currentTimeMillis();
                    System.out.println("⏱️ Day click handled in: " + (endTime - startTime) + "ms");
                } catch (Exception e) {
                    System.err.println("Error handling day click: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    private void handleDayClick(LocalDate date) {
        long startHandle = System.currentTimeMillis();
        
        // Store selected date
        selectedDate = date;
        
        // Update selected date label
        lblSelectedDate.setText("Selected: " + date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        
        System.out.println("⏱️ Label updated in: " + (System.currentTimeMillis() - startHandle) + "ms");
        
        // Load sessions for this day
        long startLoad = System.currentTimeMillis();
        loadSessionsForDate(date);
        System.out.println("⏱️ Sessions loaded in: " + (System.currentTimeMillis() - startLoad) + "ms");
    }

    private void loadSessionsForDate(LocalDate date) {
        long startClear = System.currentTimeMillis();
        sessionsListContainer.getChildren().clear();
        System.out.println("⏱️ Container cleared in: " + (System.currentTimeMillis() - startClear) + "ms");
        
        // Use cached sessions (much faster than DB query)
        long startCache = System.currentTimeMillis();
        if (cachedSessions == null) {
            cachedSessions = crudTrainingSession.getSessionsByTeam(team.getId());
            System.out.println("⏱️ Cache loaded from DB in: " + (System.currentTimeMillis() - startCache) + "ms");
        } else {
            System.out.println("⏱️ Using cached sessions (instant)");
        }
        
        // Filter sessions for this day (in-memory filtering is very fast)
        long startFilter = System.currentTimeMillis();
        List<TrainingSession> sessionsOnDay = cachedSessions.stream()
                .filter(s -> s.getSessionDatetime() != null && 
                            s.getSessionDatetime().toLocalDate().equals(date))
                .toList();
        System.out.println("⏱️ Filtered " + sessionsOnDay.size() + " sessions in: " + (System.currentTimeMillis() - startFilter) + "ms");
        
        long startUI = System.currentTimeMillis();
        if (sessionsOnDay.isEmpty()) {
            Label noSessions = new Label("No sessions scheduled");
            noSessions.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.5); -fx-font-style: italic; -fx-padding: 20;");
            sessionsListContainer.getChildren().add(noSessions);
        } else {
            for (TrainingSession session : sessionsOnDay) {
                VBox sessionCard = createSessionCard(session);
                sessionsListContainer.getChildren().add(sessionCard);
            }
        }
        System.out.println("⏱️ UI created in: " + (System.currentTimeMillis() - startUI) + "ms");
    }

    private VBox createSessionCard(TrainingSession session) {
        VBox card = new VBox(8);
        card.setStyle("-fx-padding: 12; -fx-background-color: rgba(255, 255, 255, 0.05); " +
                     "-fx-background-radius: 10; -fx-border-color: rgba(255, 255, 255, 0.08); " +
                     "-fx-border-radius: 10; -fx-border-width: 1;");
        
        // Color indicator based on type
        String colorIndicator = getColorIndicator(session.getType());
        
        // Title with color indicator
        Label lblTitle = new Label(colorIndicator + " " + session.getTitle());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: white;");
        
        // Time range
        Label lblTime = new Label(session.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + 
                                 " - " + 
                                 session.getEndTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        lblTime.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.7); -fx-font-size: 12px;");
        
        // Type label
        Label lblType = new Label(session.getType() != null ? session.getType().name() : "");
        lblType.setStyle("-fx-text-fill: rgba(255, 255, 255, 0.5); -fx-font-size: 11px;");
        
        card.getChildren().addAll(lblTitle, lblTime, lblType);
        
        // Action buttons (only for admin)
        if (isAdmin) {
            HBox actions = new HBox(8);
            actions.setAlignment(Pos.CENTER_LEFT);
            actions.setStyle("-fx-padding: 5 0 0 0;");
            
            Button btnEdit = new Button("✏️ Edit");
            btnEdit.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 11px; " +
                           "-fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> openEditSessionModal(session, selectedDate));
            
            Button btnDelete = new Button("🗑️ Delete");
            btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; " +
                             "-fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
            btnDelete.setOnAction(e -> deleteSession(session));
            
            actions.getChildren().addAll(btnEdit, btnDelete);
            card.getChildren().add(actions);
        }
        
        return card;
    }

    private String getColorIndicator(TrainingSession.Type type) {
        if (type == null) return "⚪";
        return switch (type) {
            case SCRIM -> "🟦";
            case AIM_TRAINING -> "🔴";
            case STRATEGY -> "🟢";
            case TEAM_PRACTICE -> "🟡";
            case OTHER -> "⚪";
        };
    }

    @FXML
    private void handleAddSessionQuick() {
        if (selectedDate != null) {
            openAddSessionModal(selectedDate);
        } else {
            openAddSessionModal(LocalDate.now());
        }
    }

    private void openEditSessionModal(TrainingSession session, LocalDate date) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTrainingSessionModal.fxml"));
            StackPane modalOverlay = loader.load();
            rootStackPane.getChildren().add(modalOverlay);
            
            AddTrainingSessionModalController controller = loader.getController();
            controller.setSession(session); // This will pre-fill the form
            
            controller.setOnSaveCallback(updatedSession -> {
                // Update in database
                crudTrainingSession.updateSession(updatedSession);
                rootStackPane.getChildren().remove(modalOverlay);
                // Invalidate cache and reload
                cachedSessions = null;
                loadCalendar();
                // Refresh sessions list if a date is selected
                if (selectedDate != null) {
                    loadSessionsForDate(selectedDate);
                }
            });
            
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(modalOverlay);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteSession(TrainingSession session) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Session");
        confirm.setHeaderText("Delete training session?");
        confirm.setContentText("Are you sure you want to delete \"" + session.getTitle() + "\"?\nThis action cannot be undone.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    crudTrainingSession.deleteSession(session.getId());
                    showAlert("Success", "Training session deleted successfully.");
                    // Invalidate cache and reload
                    cachedSessions = null;
                    loadCalendar();
                    // Refresh sessions list if a date is selected
                    if (selectedDate != null) {
                        loadSessionsForDate(selectedDate);
                    }
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete session: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void openAddSessionModal(LocalDate date) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTrainingSessionModal.fxml"));
            StackPane modalOverlay = loader.load();
            rootStackPane.getChildren().add(modalOverlay);
            
            AddTrainingSessionModalController controller = loader.getController();
            controller.setTeamId(team.getId());
            
            // Pre-fill date if provided
            if (date != null) {
                controller.setPrefilledDate(date);
            }
            
            controller.setOnSaveCallback(session -> {
                // Add to database
                crudTrainingSession.addSession(session);
                rootStackPane.getChildren().remove(modalOverlay);
                // Invalidate cache and reload
                cachedSessions = null;
                loadCalendar();
                // Refresh sessions list if a date is selected
                if (selectedDate != null) {
                    loadSessionsForDate(selectedDate);
                }
            });
            
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(modalOverlay);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePrevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        loadCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        loadCalendar();
    }

    // ══════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════════════

    @FXML
    private void handleBack() {
        try {
            javafx.scene.Node node = btnBack;
            javafx.scene.layout.Pane container = null;
            while (node != null) {
                if (node instanceof javafx.scene.layout.AnchorPane) {
                    container = (javafx.scene.layout.Pane) node;
                    break;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditTeam() {
        // TODO: Open edit team modal
        showAlert("Edit Team", "Edit team functionality coming soon!");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
