package Genex.entities;

import java.time.LocalDateTime;

public class TrainingAttendance {

    public enum Status {
        PRESENT, ABSENT
    }

    private String id;
    private String sessionId;
    private String teamId;
    private String playerId;
    private Status status;
    private LocalDateTime recordedAt;

    public TrainingAttendance() {}

    public TrainingAttendance(String sessionId, String teamId, String playerId, Status status) {
        this.sessionId = sessionId;
        this.teamId = teamId;
        this.playerId = playerId;
        this.status = status;
        this.recordedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
