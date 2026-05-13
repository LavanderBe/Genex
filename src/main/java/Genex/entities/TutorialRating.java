package Genex.entities;

import java.time.LocalDateTime;

public class TutorialRating {
    private String playerId;
    private int tutorialId;
    private int stars;
    private String comment;
    private LocalDateTime createdAt;

    public TutorialRating() {}

    public TutorialRating(String playerId, int tutorialId, int stars, String comment) {
        this.playerId = playerId;
        this.tutorialId = tutorialId;
        this.stars = stars;
        this.comment = comment;
    }

    public TutorialRating(String playerId, int tutorialId, int stars, String comment, LocalDateTime createdAt) {
        this.playerId = playerId;
        this.tutorialId = tutorialId;
        this.stars = stars;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public int getTutorialId() { return tutorialId; }
    public void setTutorialId(int tutorialId) { this.tutorialId = tutorialId; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
