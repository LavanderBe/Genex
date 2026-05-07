package Genex.Controllers.Team;

import Genex.entities.TrainingSession;
import Genex.services.CrudTrainingSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class SessionDetailsPanelController {

    @FXML private Label dateLabel;
    @FXML private Button btnClose;
    @FXML private Button btnAddSession;
    @FXML private VBox sessionsContainer;

    private LocalDate date;
    private List<TrainingSession> sessions;
    private String teamId;
    private boolean isCreator;
    private Runnable onCloseCallback;
    private StackPane rootStackPane;
    private CrudTrainingSession crudTrainingSession;

    @FXML
    public void initialize() {
        crudTrainingSession = new CrudTrainingSession();
    }

    public void setDate(LocalDate date) {
        this.date = date;
        updateDateLabel();
    }

    public void setSessions(List<TrainingSession> sessions) {
        this.sessions = sessions;
        displaySessions();
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public void setIsCreator(boolean isCreator) {
        this.isCreator = isCreator;
        btnAddSession.setVisible(isCreator);
        btnAddSession.setManaged(isCreator);
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    public void setRootStackPane(StackPane rootStackPane) {
        this.rootStackPane = rootStackPane;
    }

    @FXML
    private void handleClose() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    @FXML
    private void handleAddSession() {
        if (teamId == null || rootStackPane == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTrainingSessionModal.fxml"));
            StackPane modalOverlay = loader.load();
            
            AddTrainingSessionModalController controller = loader.getController();
            controller.setTeamId(teamId);
            controller.setPreselectedDate(date); // Pre-fill with the selected date
            
            controller.setOnSaveCallback(session -> {
                // Save the session
                System.out.println("=== SAVING TRAINING SESSION ===");
                System.out.println("Session ID: " + session.getId());
                System.out.println("Session Title: " + session.getTitle());
                System.out.println("Team ID: " + session.getTeamId());
                System.out.println("Date: " + session.getSessionDatetime());
                
                if (session.getId() == null) {
                    System.out.println("Adding NEW session...");
                    crudTrainingSession.addSession(session);
                    System.out.println("Session added successfully!");
                } else {
                    System.out.println("Updating EXISTING session...");
                    crudTrainingSession.updateSession(session);
                    System.out.println("Session updated successfully!");
                }
                
                System.out.println("=== SESSION SAVE COMPLETE ===");
                
                // Remove modal and refresh this panel
                rootStackPane.getChildren().remove(modalOverlay);
                refreshSessions();
            });
            
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(modalOverlay);
            });
            
            rootStackPane.getChildren().add(modalOverlay);
            
        } catch (Exception e) {
            System.err.println("Error opening add session modal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateDateLabel() {
        if (date == null) return;
        
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        
        String formattedDate = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1) + 
                              " " + date.getDayOfMonth() + " " + 
                              monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + 
                              " " + date.getYear();
        
        dateLabel.setText("Séances du " + formattedDate);
    }

    private void displaySessions() {
        sessionsContainer.getChildren().clear();
        
        if (sessions == null || sessions.isEmpty()) {
            Label emptyLabel = new Label("Aucune séance planifiée pour ce jour.");
            emptyLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.4); " +
                "-fx-font-style: italic; " +
                "-fx-font-size: 13px;"
            );
            emptyLabel.setAlignment(Pos.CENTER);
            VBox.setMargin(emptyLabel, new Insets(20, 0, 20, 0));
            sessionsContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (TrainingSession session : sessions) {
            VBox sessionCard = createSessionCard(session);
            sessionsContainer.getChildren().add(sessionCard);
        }
    }

    private VBox createSessionCard(TrainingSession session) {
        VBox card = new VBox(10);
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05); " +
            "-fx-border-color: " + getColorForType(session.getType()) + "; " +
            "-fx-border-width: 0 0 0 4; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 12;"
        );
        
        // Title and Type
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label(session.getTitle());
        titleLabel.setStyle(
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold;"
        );
        
        Label typeLabel = new Label(session.getType().toString());
        typeLabel.setStyle(
            "-fx-background-color: " + getColorForType(session.getType()) + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 10px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 3 8; " +
            "-fx-background-radius: 4;"
        );
        
        headerRow.getChildren().addAll(titleLabel, typeLabel);
        card.getChildren().add(headerRow);
        
        // Time and Duration
        HBox timeRow = new HBox(16);
        timeRow.setAlignment(Pos.CENTER_LEFT);
        
        Label timeLabel = new Label("🕐 " + session.getStartTime() + " - " + session.getEndTime());
        timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");
        
        Label durationLabel = new Label("⏱ " + session.getFormattedDuration());
        durationLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");
        
        timeRow.getChildren().addAll(timeLabel, durationLabel);
        card.getChildren().add(timeRow);
        
        // Location
        if (session.getLocation() != null && !session.getLocation().isEmpty()) {
            Label locationLabel = new Label("📍 " + session.getLocation());
            locationLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");
            card.getChildren().add(locationLabel);
        }
        
        // Notes
        if (session.getNotes() != null && !session.getNotes().isEmpty()) {
            Label notesLabel = new Label(session.getNotes());
            notesLabel.setWrapText(true);
            notesLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.6); " +
                "-fx-font-size: 11px; " +
                "-fx-font-style: italic;"
            );
            card.getChildren().add(notesLabel);
        }
        
        // Add to Google Calendar button (for all users)
        Button btnAddToCalendar = new Button("📅 Ajouter à mon Google Calendar");
        btnAddToCalendar.setStyle(
            "-fx-background-color: rgba(66,133,244,0.3); " +
            "-fx-text-fill: #4285F4; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 6 12; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: rgba(66,133,244,0.5); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6;"
        );
        btnAddToCalendar.setOnAction(e -> openGoogleCalendarLink(session));
        
        HBox calendarButtonRow = new HBox();
        calendarButtonRow.setAlignment(Pos.CENTER_LEFT);
        calendarButtonRow.getChildren().add(btnAddToCalendar);
        card.getChildren().add(calendarButtonRow);
        
        // Action buttons (only for creator)
        if (isCreator) {
            HBox buttonRow = new HBox(8);
            buttonRow.setAlignment(Pos.CENTER_RIGHT);
            
            Button btnEdit = new Button("✏ Modifier");
            btnEdit.setStyle(
                "-fx-background-color: rgba(74,144,226,0.3); " +
                "-fx-text-fill: #4A90E2; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 6 12; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: rgba(74,144,226,0.5); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 6;"
            );
            btnEdit.setOnAction(e -> handleEditSession(session));
            
            Button btnDelete = new Button("🗑 Supprimer");
            btnDelete.setStyle(
                "-fx-background-color: rgba(226,74,74,0.3); " +
                "-fx-text-fill: #E24A4A; " +
                "-fx-font-size: 11px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 6 12; " +
                "-fx-background-radius: 6; " +
                "-fx-cursor: hand; " +
                "-fx-border-color: rgba(226,74,74,0.5); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 6;"
            );
            btnDelete.setOnAction(e -> handleDeleteSession(session));
            
            buttonRow.getChildren().addAll(btnEdit, btnDelete);
            card.getChildren().add(buttonRow);
        }
        
        return card;
    }

    private void openGoogleCalendarLink(TrainingSession session) {
        try {
            String googleCalendarUrl = generateGoogleCalendarUrl(session);
            
            // Open in default browser
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                    desktop.browse(new java.net.URI(googleCalendarUrl));
                    System.out.println("Opening Google Calendar link: " + googleCalendarUrl);
                }
            }
        } catch (Exception e) {
            System.err.println("Error opening Google Calendar link: " + e.getMessage());
            e.printStackTrace();
            
            // Show error alert
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible d'ouvrir Google Calendar");
            alert.setContentText("Veuillez vérifier votre navigateur par défaut.");
            alert.showAndWait();
        }
    }

    private String generateGoogleCalendarUrl(TrainingSession session) {
        // Google Calendar URL format:
        // https://calendar.google.com/calendar/render?action=TEMPLATE&text=TITLE&dates=START/END&details=DESCRIPTION&location=LOCATION
        
        try {
            String title = java.net.URLEncoder.encode(session.getTitle(), "UTF-8");
            
            // Format dates as yyyyMMddTHHmmss (Google Calendar format)
            java.time.LocalDateTime startDateTime = session.getSessionDatetime().with(session.getStartTime());
            java.time.LocalDateTime endDateTime = session.getSessionDatetime().with(session.getEndTime());
            
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
            String startDate = startDateTime.format(formatter);
            String endDate = endDateTime.format(formatter);
            
            // Build description
            StringBuilder description = new StringBuilder();
            description.append("Type: ").append(session.getType()).append("\\n");
            description.append("Durée: ").append(session.getFormattedDuration()).append("\\n");
            if (session.getNotes() != null && !session.getNotes().isEmpty()) {
                description.append("\\nNotes:\\n").append(session.getNotes());
            }
            String details = java.net.URLEncoder.encode(description.toString(), "UTF-8");
            
            String location = "";
            if (session.getLocation() != null && !session.getLocation().isEmpty()) {
                location = java.net.URLEncoder.encode(session.getLocation(), "UTF-8");
            }
            
            // Build URL
            String url = "https://calendar.google.com/calendar/render?action=TEMPLATE" +
                        "&text=" + title +
                        "&dates=" + startDate + "/" + endDate +
                        "&details=" + details +
                        "&location=" + location;
            
            return url;
            
        } catch (Exception e) {
            System.err.println("Error generating Google Calendar URL: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    private void handleEditSession(TrainingSession session) {
        if (rootStackPane == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/AddTrainingSessionModal.fxml"));
            StackPane modalOverlay = loader.load();
            
            AddTrainingSessionModalController controller = loader.getController();
            controller.setSession(session);
            
            controller.setOnSaveCallback(updatedSession -> {
                crudTrainingSession.updateSession(updatedSession);
                rootStackPane.getChildren().remove(modalOverlay);
                refreshSessions();
            });
            
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(modalOverlay);
            });
            
            rootStackPane.getChildren().add(modalOverlay);
            
        } catch (Exception e) {
            System.err.println("Error opening edit session modal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDeleteSession(TrainingSession session) {
        // Confirm deletion
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer la séance");
        confirm.setHeaderText("Supprimer \"" + session.getTitle() + "\" ?");
        confirm.setContentText("Cette action est irréversible.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    crudTrainingSession.deleteSession(session.getId());
                    refreshSessions();
                } catch (Exception e) {
                    System.err.println("Error deleting session: " + e.getMessage());
                    e.printStackTrace();
                    
                    javafx.scene.control.Alert error = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.ERROR);
                    error.setTitle("Erreur");
                    error.setHeaderText(null);
                    error.setContentText("Impossible de supprimer la séance.");
                    error.showAndWait();
                }
            }
        });
    }

    private void refreshSessions() {
        // Reload sessions for this date
        try {
            System.out.println("=== REFRESHING SESSIONS FOR DATE: " + date + " ===");
            List<TrainingSession> allSessions = crudTrainingSession.getSessionsByTeam(teamId);
            System.out.println("Total sessions for team: " + allSessions.size());
            
            sessions = allSessions.stream()
                .filter(s -> s.getSessionDatetime().toLocalDate().equals(date))
                .toList();
            
            System.out.println("Sessions for " + date + ": " + sessions.size());
            for (TrainingSession s : sessions) {
                System.out.println("  - " + s.getTitle() + " at " + s.getStartTime());
            }
            
            displaySessions();
            System.out.println("=== REFRESH COMPLETE ===");
        } catch (Exception e) {
            System.err.println("Error refreshing sessions: " + e.getMessage());
            e.printStackTrace();
        }
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
}
