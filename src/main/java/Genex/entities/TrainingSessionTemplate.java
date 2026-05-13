package Genex.entities;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Template for generating training sessions
 * Not stored in database - used only for schedule generation
 */
public class TrainingSessionTemplate {
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private TrainingSession.Type type;
    private String title;
    private String notes;

    public TrainingSessionTemplate() {}

    public TrainingSessionTemplate(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                                   TrainingSession.Type type, String title, String notes) {
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.title = title;
        this.notes = notes;
    }

    // Getters and Setters
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public TrainingSession.Type getType() {
        return type;
    }

    public void setType(TrainingSession.Type type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getDurationMinutes() {
        if (startTime != null && endTime != null) {
            return (int) java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + startTime + "-" + endTime + " : " + type + " - " + title;
    }
}
