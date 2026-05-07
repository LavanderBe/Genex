package Genex.Controllers.Notifications;

import Genex.entities.TrainingNotification;
import Genex.entities.TrainingSession;
import Genex.services.CrudTrainingSession;
import Genex.services.TrainingNotificationService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class TrainingNotificationPopupController {

    @FXML private Button btnClose;
    @FXML private VBox notificationsContainer;

    private Runnable onCloseCallback;
    private TrainingNotificationService notificationService;
    private CrudTrainingSession crudTrainingSession;

    @FXML
    public void initialize() {
        notificationService = new TrainingNotificationService();
        crudTrainingSession = new CrudTrainingSession();
    }

    public void setNotifications(List<TrainingNotification> notifications) {
        displayNotifications(notifications);
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    private void handleClose() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    private void displayNotifications(List<TrainingNotification> notifications) {
        notificationsContainer.getChildren().clear();

        if (notifications == null || notifications.isEmpty()) {
            Label emptyLabel = new Label("Aucune nouvelle notification");
            emptyLabel.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.4); " +
                "-fx-font-style: italic; " +
                "-fx-font-size: 13px;"
            );
            emptyLabel.setAlignment(Pos.CENTER);
            VBox.setMargin(emptyLabel, new Insets(20, 0, 20, 0));
            notificationsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (TrainingNotification notification : notifications) {
            VBox notificationCard = createNotificationCard(notification);
            notificationsContainer.getChildren().add(notificationCard);
        }
    }

    private VBox createNotificationCard(TrainingNotification notification) {
        VBox card = new VBox(12);
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05); " +
            "-fx-border-color: rgba(139,13,13,0.4); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 16;"
        );

        // Get training session details
        TrainingSession session = crudTrainingSession.getSessionById(notification.getTrainingSessionId());
        if (session == null) {
            Label errorLabel = new Label("Session introuvable");
            errorLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5);");
            card.getChildren().add(errorLabel);
            return card;
        }

        // Notification type icon and title
        String icon = getIconForType(notification.getType());
        String typeText = getTextForType(notification.getType());
        
        Label titleLabel = new Label(icon + " " + typeText);
        titleLabel.setStyle(
            "-fx-text-fill: #8B0D0D; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold;"
        );
        card.getChildren().add(titleLabel);

        // Session title
        Label sessionTitle = new Label(session.getTitle());
        sessionTitle.setStyle(
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold;"
        );
        card.getChildren().add(sessionTitle);

        // Session details
        VBox detailsBox = new VBox(6);
        
        // Date and time
        String dayOfWeek = session.getSessionDatetime().getDayOfWeek()
            .getDisplayName(TextStyle.FULL, Locale.FRENCH);
        String date = session.getSessionDatetime().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH));
        
        Label dateLabel = new Label("📅 " + dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1) + ", " + date);
        dateLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13px;");
        
        Label timeLabel = new Label("🕐 " + session.getStartTime() + " - " + session.getEndTime() + " (" + session.getFormattedDuration() + ")");
        timeLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13px;");
        
        detailsBox.getChildren().addAll(dateLabel, timeLabel);

        // Location
        if (session.getLocation() != null && !session.getLocation().isEmpty()) {
            Label locationLabel = new Label("📍 " + session.getLocation());
            locationLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 13px;");
            detailsBox.getChildren().add(locationLabel);
        }

        // Time until session
        LocalDateTime sessionStart = session.getSessionDatetime().with(session.getStartTime());
        Duration timeUntil = Duration.between(LocalDateTime.now(), sessionStart);
        
        if (timeUntil.isNegative()) {
            Label pastLabel = new Label("⚠️ Cette séance est déjà passée");
            pastLabel.setStyle("-fx-text-fill: #ff6b6b; -fx-font-size: 12px; -fx-font-weight: bold;");
            detailsBox.getChildren().add(pastLabel);
        } else {
            String timeUntilText = formatDuration(timeUntil);
            Label countdownLabel = new Label("⏰ Commence dans: " + timeUntilText);
            countdownLabel.setStyle("-fx-text-fill: #4AE290; -fx-font-size: 13px; -fx-font-weight: bold;");
            detailsBox.getChildren().add(countdownLabel);
        }

        card.getChildren().add(detailsBox);

        // Action buttons
        HBox buttonRow = new HBox(8);
        buttonRow.setAlignment(Pos.CENTER_RIGHT);

        Button btnMarkAsSeen = new Button("✓ J'ai vu");
        btnMarkAsSeen.setStyle(
            "-fx-background-color: rgba(74,226,144,0.3); " +
            "-fx-text-fill: #4AE290; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: rgba(74,226,144,0.5); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6;"
        );
        btnMarkAsSeen.setOnAction(e -> handleMarkAsSeen(notification, card));

        Button btnRemindLater = new Button("🔔 Me rappeler");
        btnRemindLater.setStyle(
            "-fx-background-color: rgba(226,212,74,0.3); " +
            "-fx-text-fill: #E2D44A; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand; " +
            "-fx-border-color: rgba(226,212,74,0.5); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6;"
        );
        btnRemindLater.setOnAction(e -> handleRemindLater(notification, card));

        buttonRow.getChildren().addAll(btnMarkAsSeen, btnRemindLater);
        card.getChildren().add(buttonRow);

        return card;
    }

    private void handleMarkAsSeen(TrainingNotification notification, VBox card) {
        notificationService.markAsRead(notification.getId());
        notificationService.setRemindOnLogin(notification.getId(), false);
        
        // Remove card with fade animation
        card.setOpacity(0.5);
        card.setDisable(true);
        
        // Remove after short delay
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
        pause.setOnFinished(e -> notificationsContainer.getChildren().remove(card));
        pause.play();
        
        System.out.println("Notification marked as seen");
    }

    private void handleRemindLater(TrainingNotification notification, VBox card) {
        notificationService.setRemindOnLogin(notification.getId(), true);
        notificationService.markAsRead(notification.getId());
        
        // Remove card with fade animation
        card.setOpacity(0.5);
        card.setDisable(true);
        
        // Remove after short delay
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.millis(300));
        pause.setOnFinished(e -> notificationsContainer.getChildren().remove(card));
        pause.play();
        
        System.out.println("Notification set to remind on next login");
    }

    private String getIconForType(TrainingNotification.NotificationType type) {
        return switch (type) {
            case NEW_SESSION -> "🆕";
            case SESSION_UPDATED -> "✏️";
            case SESSION_CANCELLED -> "❌";
            case SESSION_REMINDER -> "⏰";
        };
    }

    private String getTextForType(TrainingNotification.NotificationType type) {
        return switch (type) {
            case NEW_SESSION -> "Nouvelle séance d'entraînement";
            case SESSION_UPDATED -> "Séance modifiée";
            case SESSION_CANCELLED -> "Séance annulée";
            case SESSION_REMINDER -> "Rappel de séance";
        };
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (days > 0) {
            return days + " jour" + (days > 1 ? "s" : "") + " " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "min";
        } else {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        }
    }
}
