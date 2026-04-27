package Genex.entities;

import java.time.LocalDateTime;

public class Training {

    private String id;
    private String teamId;
    private String title;
    private Type type;
    private LocalDateTime scheduledAt;  // date + time of the session
    private int durationMinutes;        // how long in minutes
    private String notes;
    private String opponentTeamId;      // only for SCRIM — which team they play against

    public enum Type {
        SCRIM,            // practice match against another team
        STRATEGY,         // building new strategies
        VOD_REVIEW,       // reviewing past game footage
        AIM_TRAINING,     // mechanical/aim practice
        DRAFT_PRACTICE,   // practicing draft/pick-ban
        BOOTCAMP          // intensive full-day training
    }

    // Default constructor
    public Training() {}

    // Full constructor
    public Training(String teamId, String title, Type type, LocalDateTime scheduledAt,
                    int durationMinutes, String notes, String opponentTeamId) {
        this.teamId = teamId;
        this.title = title;
        this.type = type;
        this.scheduledAt = scheduledAt;
        this.durationMinutes = durationMinutes;
        this.notes = notes;
        this.opponentTeamId = opponentTeamId; // null if not a scrim
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getOpponentTeamId() { return opponentTeamId; }
    public void setOpponentTeamId(String opponentTeamId) { this.opponentTeamId = opponentTeamId; }

    @Override
    public String toString() {
        return "Training{" +
                "id='" + id + '\'' +
                ", teamId='" + teamId + '\'' +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", scheduledAt=" + scheduledAt +
                ", durationMinutes=" + durationMinutes +
                ", notes='" + notes + '\'' +
                ", opponentTeamId='" + opponentTeamId + '\'' +
                '}';
    }
}