package Genex.entities;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;

public class TrainingSession {

    private String id;
    private String teamId;
    private String title;
    private Type type;
    private LocalDateTime sessionDatetime;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String notes;
    private Status status;
    private String calendarEventId;

    public enum Type {
        SCRIM, AIM_TRAINING, STRATEGY, TEAM_PRACTICE, OTHER
    }

    public enum Status {
        PLANNED, ONGOING, COMPLETED, CANCELLED
    }

    // Default constructor
    public TrainingSession() {}

    // Full constructor
    public TrainingSession(String teamId, String title, Type type,
                           LocalDateTime sessionDatetime, LocalTime startTime,
                           LocalTime endTime, String location, String notes, Status status) {
        this.teamId          = teamId;
        this.title           = title;
        this.type            = type;
        this.sessionDatetime = sessionDatetime;
        this.startTime       = startTime;
        this.endTime         = endTime;
        this.location        = location;
        this.notes           = notes;
        this.status          = status;
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

    public LocalDateTime getSessionDatetime() { return sessionDatetime; }
    public void setSessionDatetime(LocalDateTime sessionDatetime) { this.sessionDatetime = sessionDatetime; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getCalendarEventId() { return calendarEventId; }
    public void setCalendarEventId(String calendarEventId) { this.calendarEventId = calendarEventId; }

    /**
     * Calculate duration in minutes from start and end time
     */
    public int getDurationMinutes() {
        if (startTime != null && endTime != null) {
            Duration duration = Duration.between(startTime, endTime);
            return (int) duration.toMinutes();
        }
        return 0;
    }

    /**
     * Get formatted duration string (e.g., "2h 30min")
     */
    public String getFormattedDuration() {
        int minutes = getDurationMinutes();
        if (minutes == 0) return "0 min";

        int hours = minutes / 60;
        int mins = minutes % 60;

        if (hours > 0 && mins > 0) {
            return hours + "h " + mins + "min";
        } else if (hours > 0) {
            return hours + "h";
        } else {
            return mins + "min";
        }
    }

    @Override
    public String toString() {
        return "TrainingSession{" +
                "id='"               + id               + '\'' +
                ", teamId='"         + teamId           + '\'' +
                ", title='"          + title            + '\'' +
                ", type="            + type                    +
                ", sessionDatetime=" + sessionDatetime         +
                ", startTime="       + startTime               +
                ", endTime="         + endTime                 +
                ", duration='"       + getFormattedDuration() + '\'' +
                ", location='"       + location         + '\'' +
                ", notes='"          + notes            + '\'' +
                ", status="          + status                  +
                '}';
    }
}