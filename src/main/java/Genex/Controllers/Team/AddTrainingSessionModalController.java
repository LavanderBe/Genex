package Genex.Controllers.Team;

import Genex.entities.TrainingSession;
import Genex.services.CrudTrainingSession;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

public class AddTrainingSessionModalController {

    @FXML
    private Label modalTitle;

    @FXML
    private TextField txtTitle;

    @FXML
    private ChoiceBox<TrainingSession.Type> choiceType;

    @FXML
    private DatePicker dateSession;

    @FXML
    private TextField txtStartTime;

    @FXML
    private TextField txtEndTime;

    @FXML
    private ChoiceBox<TrainingSession.Status> choiceStatus;

    @FXML
    private TextField txtLocation;

    @FXML
    private TextArea txtNotes;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSave;

    // Error labels
    @FXML
    private Label errorTitle;

    @FXML
    private Label errorType;

    @FXML
    private Label errorDate;

    @FXML
    private Label errorStartTime;

    @FXML
    private Label errorEndTime;

    @FXML
    private Label errorStatus;

    private Consumer<TrainingSession> onSaveCallback;
    private Runnable onCloseCallback;
    private TrainingSession sessionToEdit;
    private String teamId;

    @FXML
    public void initialize() {
        System.out.println("AddTrainingSessionModalController initialized");

        // Setup choice boxes
        if (choiceType != null) {
            choiceType.getItems().addAll(TrainingSession.Type.values());
            choiceType.setValue(TrainingSession.Type.TEAM_PRACTICE);
        }

        if (choiceStatus != null) {
            choiceStatus.getItems().addAll(TrainingSession.Status.values());
            choiceStatus.setValue(TrainingSession.Status.PLANNED);
        }

        // Setup validation listeners
        setupValidation();
    }

    private void setupValidation() {
        // Clear errors on input
        if (txtTitle != null) txtTitle.textProperty().addListener((obs, old, val) -> hideError(errorTitle));
        if (txtStartTime != null) txtStartTime.textProperty().addListener((obs, old, val) -> hideError(errorStartTime));
        if (txtEndTime != null) txtEndTime.textProperty().addListener((obs, old, val) -> hideError(errorEndTime));
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public void setSession(TrainingSession session) {
        this.sessionToEdit = session;
        this.teamId = session.getTeamId();
        modalTitle.setText("Modifier la Session");

        // Fill form with session data
        txtTitle.setText(session.getTitle());

        if (session.getType() != null) {
            choiceType.setValue(session.getType());
        }

        if (session.getSessionDatetime() != null) {
            dateSession.setValue(session.getSessionDatetime().toLocalDate());
        }

        if (session.getStartTime() != null) {
            txtStartTime.setText(session.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        if (session.getEndTime() != null) {
            txtEndTime.setText(session.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        }

        if (session.getStatus() != null) {
            choiceStatus.setValue(session.getStatus());
        }

        if (session.getLocation() != null && !session.getLocation().trim().isEmpty()) {
            txtLocation.setText(session.getLocation());
        }

        if (session.getNotes() != null) {
            txtNotes.setText(session.getNotes());
        }
    }

    public void setOnSaveCallback(Consumer<TrainingSession> callback) {
        this.onSaveCallback = callback;
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    private void closeModal() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    @FXML
    private void incrementStartTime() {
        adjustTime(txtStartTime, 15); // Increment by 15 minutes
    }

    @FXML
    private void decrementStartTime() {
        adjustTime(txtStartTime, -15); // Decrement by 15 minutes
    }

    @FXML
    private void incrementEndTime() {
        adjustTime(txtEndTime, 15); // Increment by 15 minutes
    }

    @FXML
    private void decrementEndTime() {
        adjustTime(txtEndTime, -15); // Decrement by 15 minutes
    }

    private void adjustTime(TextField timeField, int minutes) {
        try {
            String currentText = timeField.getText().trim();
            LocalTime time;

            if (currentText.isEmpty()) {
                // If empty, start from current time rounded to nearest 15 min
                time = LocalTime.now();
                int minuteOfHour = time.getMinute();
                int roundedMinute = ((minuteOfHour + 7) / 15) * 15;
                time = time.withMinute(0).withSecond(0).withNano(0).plusMinutes(roundedMinute);
            } else {
                time = LocalTime.parse(currentText, DateTimeFormatter.ofPattern("HH:mm"));
            }

            // Adjust time
            time = time.plusMinutes(minutes);

            // Update field
            timeField.setText(time.format(DateTimeFormatter.ofPattern("HH:mm")));

        } catch (Exception e) {
            // If parsing fails, set to current time
            LocalTime time = LocalTime.now().withSecond(0).withNano(0);
            timeField.setText(time.format(DateTimeFormatter.ofPattern("HH:mm")));
        }
    }

    @FXML
    private void saveSession() {
        if (!validateForm()) {
            return;
        }

        try {
            // Parse date and time first for conflict check
            LocalDate date = dateSession.getValue();
            LocalTime startTime = LocalTime.parse(txtStartTime.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endTime = LocalTime.parse(txtEndTime.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime dateTime = LocalDateTime.of(date, startTime);

            // Check for time conflicts
            CrudTrainingSession crudSession = new CrudTrainingSession();
            String excludeId = sessionToEdit != null ? sessionToEdit.getId() : null;
            
            boolean hasConflict = crudSession.hasTimeConflict(teamId, dateTime, startTime, endTime, excludeId);
            
            if (hasConflict) {
                // Get conflicting sessions to show details
                List<TrainingSession> conflicts = crudSession.getConflictingSessions(teamId, dateTime, startTime, endTime, excludeId);
                
                StringBuilder conflictMessage = new StringBuilder("⚠️ Conflit d'horaire détecté!\n\n");
                conflictMessage.append("Session(s) en conflit:\n");
                
                for (TrainingSession conflict : conflicts) {
                    conflictMessage.append("• ").append(conflict.getTitle())
                            .append(" (").append(conflict.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                            .append(" - ").append(conflict.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                            .append(")\n");
                }
                
                conflictMessage.append("\nVeuillez choisir un autre horaire.");
                
                showError(errorStartTime, "Conflit d'horaire!");
                showError(errorEndTime, conflictMessage.toString());
                
                // Show alert dialog
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Conflit d'horaire");
                alert.setHeaderText("Une session existe déjà à cet horaire");
                alert.setContentText(conflictMessage.toString());
                alert.showAndWait();
                
                return; // Don't save
            }

            // Create or update session
            TrainingSession session = sessionToEdit != null ? sessionToEdit : new TrainingSession();

            session.setTitle(txtTitle.getText().trim());
            session.setType(choiceType.getValue());
            session.setSessionDatetime(dateTime);
            session.setStartTime(startTime);
            session.setEndTime(endTime);
            session.setStatus(choiceStatus.getValue());
            session.setLocation(txtLocation.getText().trim());
            session.setNotes(txtNotes.getText().trim());
            session.setTeamId(teamId);

            System.out.println("✓ No conflicts - Session saved: " + session.getTitle());
            System.out.println("Duration: " + session.getFormattedDuration());

            // Call callback
            if (onSaveCallback != null) {
                onSaveCallback.accept(session);
            }

            closeModal();

        } catch (Exception e) {
            System.err.println("Error saving session");
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        // Validate title
        if (txtTitle.getText().trim().isEmpty()) {
            showError(errorTitle, "Le titre est requis");
            valid = false;
        }

        // Validate date
        if (dateSession.getValue() == null) {
            showError(errorDate, "La date est requise");
            valid = false;
        }

        // Validate start time
        if (txtStartTime.getText().trim().isEmpty()) {
            showError(errorStartTime, "L'heure de début est requise");
            valid = false;
        } else {
            try {
                LocalTime.parse(txtStartTime.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                showError(errorStartTime, "Format invalide (HH:MM)");
                valid = false;
            }
        }

        // Validate end time
        if (txtEndTime.getText().trim().isEmpty()) {
            showError(errorEndTime, "L'heure de fin est requise");
            valid = false;
        } else {
            try {
                LocalTime endTime = LocalTime.parse(txtEndTime.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));

                // Check if end time is after start time
                if (!txtStartTime.getText().trim().isEmpty()) {
                    LocalTime startTime = LocalTime.parse(txtStartTime.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                    if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
                        showError(errorEndTime, "Doit être après l'heure de début");
                        valid = false;
                    }
                }
            } catch (Exception e) {
                showError(errorEndTime, "Format invalide (HH:MM)");
                valid = false;
            }
        }

        return valid;
    }

    private void showError(Label errorLabel, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
    }

    private void hideError(Label errorLabel) {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }
}