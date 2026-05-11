package Genex.Controllers.Team;

import Genex.entities.Player;
import Genex.entities.Team;
import Genex.entities.TeamLevel;
import Genex.entities.TrainingSession;
import Genex.services.CrudTeamMember;
import Genex.services.CrudTrainingSession;
import Genex.services.OptimalScheduleGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class GenerateScheduleModalController {

    @FXML private Label modalTitle;
    @FXML private ComboBox<TeamLevel> choiceLevel;
    @FXML private ComboBox<String> choiceRepeatCount;
    @FXML private DatePicker dateStart;
    @FXML private Label lblEndDate;
    @FXML private TextArea txtPreview;
    @FXML private Label lblSuggestedLevel;
    @FXML private Button btnGenerate;
    @FXML private Button btnReset;
    @FXML private Button btnSave;
    @FXML private Button btnCloseModal;

    private Team team;
    private Runnable onCloseCallback;
    private Consumer<List<TrainingSession>> onSaveCallback;
    private OptimalScheduleGenerator generator;
    private CrudTeamMember crudTeamMember;
    private CrudTrainingSession crudTrainingSession;
    private List<TrainingSession> generatedSessions;
    private LocalDate calculatedEndDate;

    @FXML
    public void initialize() {
        System.out.println("GenerateScheduleModalController initialized");

        generator = new OptimalScheduleGenerator();
        crudTeamMember = new CrudTeamMember();
        crudTrainingSession = new CrudTrainingSession();

        setupLevelChoiceBox();
        setupRepeatCountChoiceBox();
        setupDatePickers();
        setupButtons();

        btnSave.setDisable(true); // Disabled until schedule is generated
    }

    private void setupLevelChoiceBox() {
        choiceLevel.getItems().addAll(TeamLevel.values());

        choiceLevel.setConverter(new StringConverter<TeamLevel>() {
            @Override
            public String toString(TeamLevel level) {
                return level != null ? level.toString() : "";
            }

            @Override
            public TeamLevel fromString(String string) {
                return null;
            }
        });
    }

    private void setupRepeatCountChoiceBox() {
        choiceRepeatCount.getItems().addAll(
                "1 semaine",
                "2 semaines",
                "3 semaines",
                "4 semaines",
                "6 semaines",
                "8 semaines",
                "12 semaines (3 mois)"
        );
        choiceRepeatCount.setValue("4 semaines"); // Default

        // Update end date when repeat count changes
        choiceRepeatCount.setOnAction(e -> updateEndDateLabel());
    }

    private void updateEndDateLabel() {
        if (dateStart.getValue() != null && choiceRepeatCount.getValue() != null) {
            String selected = choiceRepeatCount.getValue();
            int weeks = extractWeeksFromString(selected);
            calculatedEndDate = dateStart.getValue().plusWeeks(weeks);
            
            // Update label
            String formattedDate = calculatedEndDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            lblEndDate.setText("→ Fin: " + formattedDate);
            lblEndDate.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
    }

    private int extractWeeksFromString(String text) {
        if (text.contains("1 semaine")) return 1;
        if (text.contains("2 semaines")) return 2;
        if (text.contains("3 semaines")) return 3;
        if (text.contains("4 semaines")) return 4;
        if (text.contains("6 semaines")) return 6;
        if (text.contains("8 semaines")) return 8;
        if (text.contains("12 semaines")) return 12;
        return 4; // Default
    }

    private void setupDatePickers() {
        // Set default dates: start today
        dateStart.setValue(LocalDate.now());
        calculatedEndDate = LocalDate.now().plusWeeks(4);
        
        // Update label initially
        updateEndDateLabel();

        // Update end date when start date changes
        dateStart.setOnAction(e -> updateEndDateLabel());
    }

    private void setupButtons() {
        if (btnCloseModal != null) {
            btnCloseModal.setOnAction(e -> closeModal());
        }
        if (btnGenerate != null) {
            btnGenerate.setOnAction(e -> handleGenerate());
        }
        if (btnReset != null) {
            btnReset.setOnAction(e -> handleReset());
        }
        if (btnSave != null) {
            btnSave.setOnAction(e -> handleSave());
        }
    }

    @FXML
    private void handleReset() {
        System.out.println("Resetting form...");

        // Reset to defaults
        dateStart.setValue(LocalDate.now());
        choiceRepeatCount.setValue("4 semaines");
        calculatedEndDate = LocalDate.now().plusWeeks(4);
        updateEndDateLabel();
        
        txtPreview.clear();
        txtPreview.setPromptText("Cliquez sur 'Générer le Planning' pour voir la prévisualisation...");
        generatedSessions = null;
        btnSave.setDisable(true);

        // Recalculate suggested level
        if (team != null) {
            List<Player> members = crudTeamMember.getMembersByTeam(team.getId());
            List<TrainingSession> pastSessions = crudTrainingSession.getSessionsByTeam(team.getId());
            TeamLevel suggestedLevel = generator.calculateSuggestedLevel(team, members, pastSessions);
            choiceLevel.setValue(suggestedLevel);
        }

        System.out.println("✅ Form reset");
    }

    public void setTeam(Team team) {
        this.team = team;

        // Get team members
        List<Player> members = crudTeamMember.getMembersByTeam(team.getId());
        
        // Get past training sessions for better level calculation
        List<TrainingSession> pastSessions = crudTrainingSession.getSessionsByTeam(team.getId());

        // Calculate suggested level with training history
        TeamLevel suggestedLevel = generator.calculateSuggestedLevel(team, members, pastSessions);

        // Display suggestion with more details
        String suggestionText = String.format("Niveau suggéré: %s (basé sur %d membres, %d sessions passées)",
                suggestedLevel.getDisplayName(),
                members.size(),
                pastSessions.size());
        
        lblSuggestedLevel.setText(suggestionText);
        lblSuggestedLevel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");

        // Pre-select suggested level
        choiceLevel.setValue(suggestedLevel);
    }

    @FXML
    private void handleGenerate() {
        if (!validateForm()) {
            return;
        }

        try {
            TeamLevel selectedLevel = choiceLevel.getValue();
            LocalDate startDate = dateStart.getValue();
            LocalDate endDate = calculatedEndDate; // Use calculated end date

            System.out.println("Generating schedule for team: " + team.getName());
            System.out.println("Level: " + selectedLevel);
            System.out.println("Period: " + startDate + " to " + endDate);

            // Generate schedule
            generatedSessions = generator.generateSchedule(
                    team.getId(),
                    selectedLevel,
                    startDate,
                    endDate
            );

            // Check for conflicts with existing sessions
            List<TrainingSession> existingSessions = crudTrainingSession.getSessionsByTeam(team.getId());
            List<OptimalScheduleGenerator.ConflictInfo> conflicts = 
                    generator.detectConflicts(generatedSessions, existingSessions);

            // Get statistics
            OptimalScheduleGenerator.ScheduleStatistics stats =
                    generator.getScheduleStatistics(generatedSessions);

            // Analyze balance
            OptimalScheduleGenerator.BalanceAnalysis balance =
                    generator.analyzeTrainingBalance(generatedSessions);

            // Display preview
            StringBuilder preview = new StringBuilder();
            preview.append("═══════════════════════════════════════\n");
            preview.append("  PLANNING GÉNÉRÉ - ").append(team.getName()).append("\n");
            preview.append("═══════════════════════════════════════\n\n");
            
            // Statistics
            preview.append("📊 STATISTIQUES:\n");
            preview.append(stats.toString()).append("\n\n");

            // Balance analysis
            preview.append(balance.toString()).append("\n");

            // Conflicts warning
            if (!conflicts.isEmpty()) {
                preview.append("⚠️ CONFLITS DÉTECTÉS:\n");
                preview.append(conflicts.size()).append(" session(s) en conflit avec le planning existant\n");
                for (OptimalScheduleGenerator.ConflictInfo conflict : conflicts) {
                    preview.append("  • ").append(conflict.toString()).append("\n");
                }
                preview.append("\n");
            } else {
                preview.append("✅ Aucun conflit avec le planning existant\n\n");
            }

            preview.append("───────────────────────────────────────\n");
            preview.append("DÉTAILS DES SESSIONS:\n");
            preview.append("───────────────────────────────────────\n\n");

            for (TrainingSession session : generatedSessions) {
                preview.append(String.format("📅 %s (%s)\n",
                        session.getSessionDatetime().toLocalDate(),
                        session.getSessionDatetime().getDayOfWeek()));
                preview.append(String.format("   ⏰ %s - %s (%s)\n",
                        session.getStartTime(),
                        session.getEndTime(),
                        session.getFormattedDuration()));
                preview.append(String.format("   🎯 %s: %s\n",
                        session.getType(),
                        session.getTitle()));
                preview.append(String.format("   💡 %s\n\n",
                        session.getNotes()));
            }

            txtPreview.setText(preview.toString());

            // Enable save button
            btnSave.setDisable(false);

            System.out.println("✅ Schedule generated: " + generatedSessions.size() + " sessions");
            if (!conflicts.isEmpty()) {
                System.out.println("⚠️ " + conflicts.size() + " conflicts detected");
            }

        } catch (Exception e) {
            System.err.println("Error generating schedule: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de générer le planning.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSave() {
        if (generatedSessions == null || generatedSessions.isEmpty()) {
            showAlert("Erreur", "Aucun planning à sauvegarder.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Save all sessions to database
            for (TrainingSession session : generatedSessions) {
                crudTrainingSession.addSession(session);
            }

            System.out.println("✅ Saved " + generatedSessions.size() + " training sessions");

            // Show success message
            showAlert("Succès",
                    generatedSessions.size() + " sessions d'entraînement ont été créées!",
                    Alert.AlertType.INFORMATION);

            // Call callback if set
            if (onSaveCallback != null) {
                onSaveCallback.accept(generatedSessions);
            }

            // Close modal
            closeModal();

        } catch (Exception e) {
            System.err.println("Error saving schedule: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible de sauvegarder le planning.", Alert.AlertType.ERROR);
        }
    }

    private boolean validateForm() {
        if (choiceLevel.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner un niveau d'équipe.", Alert.AlertType.WARNING);
            return false;
        }

        if (dateStart.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner la date de début.", Alert.AlertType.WARNING);
            return false;
        }

        if (choiceRepeatCount.getValue() == null) {
            showAlert("Erreur", "Veuillez sélectionner la durée du planning.", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void closeModal() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    public void setOnSaveCallback(Consumer<List<TrainingSession>> callback) {
        this.onSaveCallback = callback;
    }

    // Hover effects for buttons
    @FXML
    private void handleCloseHover() {
        if (btnCloseModal != null) {
            btnCloseModal.setStyle("-fx-background-color: rgba(255,0,0,0.3); -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10; -fx-border-color: rgba(255,0,0,0.6); -fx-border-width: 2; -fx-shape: 'M 0 5 L 5 0 L 15 0 L 20 5 L 20 15 L 15 20 L 5 20 L 0 15 Z';");
        }
    }

    @FXML
    private void handleCloseExit() {
        if (btnCloseModal != null) {
            btnCloseModal.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 10; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2; -fx-shape: 'M 0 5 L 5 0 L 15 0 L 20 5 L 20 15 L 15 20 L 5 20 L 0 15 Z';");
        }
    }

    @FXML
    private void handleGenerateHover() {
        if (btnGenerate != null) {
            btnGenerate.setStyle("-fx-background-color: #A01010; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.4); -fx-border-width: 2; -fx-shape: 'M 5 0 L 95 0 L 100 5 L 100 35 L 95 40 L 5 40 L 0 35 L 0 5 Z';");
        }
    }

    @FXML
    private void handleGenerateExit() {
        if (btnGenerate != null) {
            btnGenerate.setStyle("-fx-background-color: #8B0D0D; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.2); -fx-border-width: 2; -fx-shape: 'M 5 0 L 95 0 L 100 5 L 100 35 L 95 40 L 5 40 L 0 35 L 0 5 Z';");
        }
    }

    @FXML
    private void handleResetHover() {
        if (btnReset != null) {
            btnReset.setStyle("-fx-background-color: rgba(255,165,0,0.4); -fx-text-fill: #FFB84D; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12; -fx-cursor: hand; -fx-border-color: rgba(255,165,0,0.6); -fx-border-width: 2; -fx-shape: 'M 5 0 L 95 0 L 100 5 L 100 35 L 95 40 L 5 40 L 0 35 L 0 5 Z';");
        }
    }

    @FXML
    private void handleResetExit() {
        if (btnReset != null) {
            btnReset.setStyle("-fx-background-color: rgba(255,165,0,0.2); -fx-text-fill: #FFA500; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12; -fx-cursor: hand; -fx-border-color: rgba(255,165,0,0.4); -fx-border-width: 2; -fx-shape: 'M 5 0 L 95 0 L 100 5 L 100 35 L 95 40 L 5 40 L 0 35 L 0 5 Z';");
        }
    }

    @FXML
    private void handleSaveHover() {
        if (btnSave != null && !btnSave.isDisabled()) {
            btnSave.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.5); -fx-border-width: 2; -fx-shape: 'M 5 0 L 95 0 L 100 5 L 100 30 L 95 35 L 5 35 L 0 30 L 0 5 Z';");
        }
    }

    @FXML
    private void handleSaveExit() {
        if (btnSave != null && !btnSave.isDisabled()) {
            btnSave.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.3); -fx-border-width: 2; -fx-shape: 'M 5 0 L 95 0 L 100 5 L 100 30 L 95 35 L 5 35 L 0 30 L 0 5 Z';");
        }
    }
}
