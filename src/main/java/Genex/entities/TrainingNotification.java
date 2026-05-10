package Genex.entities;

import java.time.LocalDateTime;

public class TrainingNotification {
    private String id;
    private String userId;
    private String trainingSessionId;
    private String teamId;
    private LocalDateTime createdAt;
    private boolean isRead;
    private boolean remindOnLogin;
    private NotificationType type;

    public enum NotificationType {
        NEW_SESSION,
        SESSION_UPDATED,
        SESSION_CANCELLED,
        SESSION_REMINDER
    }

    // Constructors
    public TrainingNotification() {
        this.id = java.util.UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.remindOnLogin = false;
        this.type = NotificationType.NEW_SESSION;
    }

    public TrainingNotification(String userId, String trainingSessionId, String teamId, NotificationType type) {
        this();
        this.userId = userId;
        this.trainingSessionId = trainingSessionId;
        this.teamId = teamId;
        this.type = type;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTrainingSessionId() {
        return trainingSessionId;
    }

    public void setTrainingSessionId(String trainingSessionId) {
        this.trainingSessionId = trainingSessionId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isRemindOnLogin() {
        return remindOnLogin;
    }

    public void setRemindOnLogin(boolean remindOnLogin) {
        this.remindOnLogin = remindOnLogin;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }
}
