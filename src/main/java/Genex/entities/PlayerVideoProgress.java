package Genex.entities;

import java.time.LocalDateTime;

public class PlayerVideoProgress {
    private String playerId;
    private int tutorialVideoId;
    private boolean completed;
    private LocalDateTime completedAt;

    public PlayerVideoProgress() {}

    public PlayerVideoProgress(String playerId, int tutorialVideoId, boolean completed, LocalDateTime completedAt) {
        this.playerId = playerId;
        this.tutorialVideoId = tutorialVideoId;
        this.completed = completed;
        this.completedAt = completedAt;
    }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public int getTutorialVideoId() { return tutorialVideoId; }
    public void setTutorialVideoId(int tutorialVideoId) { this.tutorialVideoId = tutorialVideoId; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
