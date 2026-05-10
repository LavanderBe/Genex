package Genex.Controllers.Team;

import Genex.entities.TrainingSession;
import Genex.services.CrudTrainingSession;
import Genex.services.GoogleCalendarService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class CalendarViewController {

    @FXML private Label monthYearLabel;
    @FXML private Button btnPrevMonth;
    @FXML private Button btnNextMonth;
    @FXML private GridPane dayHeadersGrid;
    @FXML private GridPane calendarGrid;

    private YearMonth currentMonth;
    private String teamId;
    private CrudTrainingSession crudTrainingSession;
    private GoogleCalendarService googleCalendarService;
    private StackPane rootStackPane;
    private boolean isCreator;
    
    // Callback to refresh when sessions are modified
    private Runnable onRefreshCallback;

    @FXML
    public void initialize() {
        crudTrainingSession = new CrudTrainingSession();
        googleCalendarService = new GoogleCalendarService();
        currentMonth = YearMonth.now();
        setupDayHeaders();
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
        refreshCalendar();
    }

    public void setRootStackPane(StackPane rootStackPane) {
        this.rootStackPane = rootStackPane;
    }

    public void setIsCreator(boolean isCreator) {
        this.isCreator = isCreator;
    }

    public void setOnRefreshCallback(Runnable callback) {
        this.onRefreshCallback = callback;
    }

    @FXML
    private void handlePrevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        refreshCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        refreshCalendar();
    }

    private void setupDayHeaders() {
        String[] dayNames = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        
        for (int i = 0; i < 7; i++) {
            Label dayLabel = new Label(dayNames[i]);
            dayLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.7); " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-alignment: center;"
            );
            dayLabel.setMaxWidth(Double.MAX_VALUE);
            dayLabel.setAlignment(Pos.CENTER);
            GridPane.setHgrow(dayLabel, Priority.ALWAYS);
            dayHeadersGrid.add(dayLabel, i, 0);
        }
    }

    private void refreshCalendar() {
        // Update month/year label
        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthYearLabel.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + currentMonth.getYear());

        // Clear existing calendar cells
        calendarGrid.getChildren().clear();

        // Get training sessions for this month
        List<TrainingSession> sessions = getSessionsForMonth();
        Map<LocalDate, List<TrainingSession>> sessionsByDate = sessions.stream()
            .collect(Collectors.groupingBy(s -> s.getSessionDatetime().toLocalDate()));

        // Get first day of month and calculate starting position
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        int daysInMonth = currentMonth.lengthOfMonth();

        // Create calendar cells
        int row = 0;
        int col = dayOfWeek - 1; // Start at correct day of week

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            List<TrainingSession> daySessions = sessionsByDate.getOrDefault(date, Collections.emptyList());
            
            VBox dayCell = createDayCell(day, date, daySessions);
            calendarGrid.add(dayCell, col, row);
            
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDayCell(int day, LocalDate date, List<TrainingSession> sessions) {
        VBox cell = new VBox(6);
        cell.setAlignment(Pos.TOP_CENTER);
        cell.setMinHeight(120);
        cell.setPrefHeight(120);
        cell.setMaxHeight(120);
        HBox.setHgrow(cell, Priority.ALWAYS);
        GridPane.setHgrow(cell, Priority.ALWAYS);
        cell.setMaxWidth(Double.MAX_VALUE);
        
        // Check if this is today
        boolean isToday = date.equals(LocalDate.now());
        
        // Base style
        final String baseStyle;
        if (isToday) {
            baseStyle = "-fx-background-color: rgba(139,13,13,0.2); " +
                       "-fx-border-color: #8B0D0D; " +
                       "-fx-border-width: 2; " +
                       "-fx-border-radius: 8; " +
                       "-fx-background-radius: 8; " +
                       "-fx-padding: 10; " +
                       "-fx-cursor: hand;";
        } else {
            baseStyle = "-fx-background-color: rgba(255,255,255,0.03); " +
                       "-fx-border-color: rgba(255,255,255,0.1); " +
                       "-fx-border-width: 1; " +
                       "-fx-border-radius: 8; " +
                       "-fx-background-radius: 8; " +
                       "-fx-padding: 10; " +
                       "-fx-cursor: hand;";
        }
        
        cell.setStyle(baseStyle);
        
        // Day number
        Label dayLabel = new Label(String.valueOf(day));
        dayLabel.setStyle(
            "-fx-text-fill: " + (isToday ? "#FFFFFF" : "rgba(255,255,255,0.8)") + "; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: " + (isToday ? "bold" : "normal") + ";"
        );
        cell.getChildren().add(dayLabel);
        
        // Session indicators (colored dots)
        if (!sessions.isEmpty()) {
            VBox.setMargin(dayLabel, new javafx.geometry.Insets(0, 0, 6, 0));
            
            HBox indicators = new HBox(5);
            indicators.setAlignment(Pos.CENTER);
            
            // Show up to 3 session indicators
            int count = Math.min(sessions.size(), 3);
            for (int i = 0; i < count; i++) {
                TrainingSession session = sessions.get(i);
                Region dot = new Region();
                dot.setMinSize(12, 12);
                dot.setMaxSize(12, 12);
                dot.setStyle(
                    "-fx-background-color: " + getColorForType(session.getType()) + "; " +
                    "-fx-background-radius: 6;"
                );
                indicators.getChildren().add(dot);
            }
            
            // If more than 3 sessions, show "+X" label
            if (sessions.size() > 3) {
                Label moreLabel = new Label("+" + (sessions.size() - 3));
                moreLabel.setStyle(
                    "-fx-text-fill: rgba(255,255,255,0.7); " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold;"
                );
                indicators.getChildren().add(moreLabel);
            }
            
            cell.getChildren().add(indicators);
        }
        
        // Click handler to open session details panel
        cell.setOnMouseClicked(e -> openSessionDetailsPanel(date, sessions));
        
        // Hover effect
        final boolean finalIsToday = isToday;
        cell.setOnMouseEntered(e -> {
            if (finalIsToday) {
                cell.setStyle(baseStyle.replace("rgba(139,13,13,0.2)", "rgba(139,13,13,0.35)"));
            } else {
                cell.setStyle(baseStyle.replace("rgba(255,255,255,0.03)", "rgba(255,255,255,0.12)"));
            }
        });
        
        cell.setOnMouseExited(e -> cell.setStyle(baseStyle));
        
        return cell;
    }

    private String getColorForType(TrainingSession.Type type) {
        return switch (type) {
            case SCRIM -> "#4A90E2";           // Blue
            case AIM_TRAINING -> "#E24A4A";    // Red
            case STRATEGY -> "#4AE290";        // Green
            case TEAM_PRACTICE -> "#E2D44A";   // Yellow
            case OTHER -> "#808080";           // Gray
        };
    }

    private List<TrainingSession> getSessionsForMonth() {
        if (teamId == null) return Collections.emptyList();
        
        try {
            // Get all sessions for the team
            List<TrainingSession> allSessions = crudTrainingSession.getSessionsByTeam(teamId);
            
            // Filter sessions for current month
            LocalDate startOfMonth = currentMonth.atDay(1);
            LocalDate endOfMonth = currentMonth.atEndOfMonth();
            
            return allSessions.stream()
                .filter(s -> {
                    LocalDate sessionDate = s.getSessionDatetime().toLocalDate();
                    return !sessionDate.isBefore(startOfMonth) && !sessionDate.isAfter(endOfMonth);
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            System.err.println("Error loading sessions for month: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private void openSessionDetailsPanel(LocalDate date, List<TrainingSession> sessions) {
        if (rootStackPane == null) {
            System.err.println("Root stack pane not set");
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/SessionDetailsPanel.fxml"));
            StackPane panelOverlay = loader.load();
            
            SessionDetailsPanelController controller = loader.getController();
            
            // IMPORTANT: Set these BEFORE setSessions() so displaySessions() has correct values
            controller.setDate(date);
            controller.setTeamId(teamId);
            controller.setIsCreator(isCreator);
            
            // Pass current user ID for attendance tracking
            String currentUserId = Genex.utils.SessionManager.getInstance().getCurrentUserId();
            controller.setCurrentUserId(currentUserId);
            
            System.out.println("=== SESSION DETAILS PANEL DEBUG ===");
            System.out.println("Current User ID: " + currentUserId);
            System.out.println("Is Creator: " + isCreator);
            System.out.println("Team ID: " + teamId);
            System.out.println("Date: " + date);
            System.out.println("Sessions count: " + sessions.size());
            System.out.println("===================================");
            
            // Call setSessions() LAST because it triggers displaySessions()
            controller.setSessions(sessions);
            
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(panelOverlay);
                refreshCalendar(); // Refresh calendar when panel closes
                if (onRefreshCallback != null) {
                    onRefreshCallback.run();
                }
            });
            controller.setRootStackPane(rootStackPane);
            
            rootStackPane.getChildren().add(panelOverlay);
            
        } catch (Exception e) {
            System.err.println("Error opening session details panel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void refresh() {
        refreshCalendar();
    }
}
