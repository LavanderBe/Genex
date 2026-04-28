package Genex.entities;

import java.time.LocalDateTime;

public class TrainingSession {

    private int sessionId;
    private int teamId;
    private int createdBy;

    private String titre;
    private String type; // scrim, aim_training, strategy, team_practice

    private LocalDateTime sessionDateTime;
    private int durationMinutes;

    private String locationType; // online / centre
    private Integer centreId;    // nullable
    // optional text (discord link, room...)

    private String notes;
    private String statut; // planifie, en_cours, termine, annule

    // Constructors
    public TrainingSession() {}

    public TrainingSession(int teamId, int createdBy, String titre, String type,
                           LocalDateTime sessionDateTime, int durationMinutes,
                           String locationType, Integer centreId,
                           String notes, String statut) {

        this.teamId = teamId;
        this.createdBy = createdBy;
        this.titre = titre;
        this.type = type;
        this.sessionDateTime = sessionDateTime;
        this.durationMinutes = durationMinutes;
        this.locationType = locationType;
        this.centreId = centreId;

        this.notes = notes;
        this.statut = statut;
    }

    // Getters & Setters
    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public int getTeamId() { return teamId; }
    public void setTeamId(int teamId) { this.teamId = teamId; }

    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDateTime getSessionDateTime() { return sessionDateTime; }
    public void setSessionDateTime(LocalDateTime sessionDateTime) { this.sessionDateTime = sessionDateTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getLocationType() { return locationType; }
    public void setLocationType(String locationType) { this.locationType = locationType; }

    public Integer getCentreId() { return centreId; }
    public void setCentreId(Integer centreId) { this.centreId = centreId; }



    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}