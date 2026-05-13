package Genex.entities;

import java.time.LocalDateTime;

public class TutorialVideo {
    private int id;
    private int tutorialId;
    private int position;
    private String title;
    private String videoUrl;
    private LocalDateTime createdAt;

    public TutorialVideo() {}

    public TutorialVideo(int tutorialId, int position, String title, String videoUrl) {
        this.tutorialId = tutorialId;
        this.position = position;
        this.title = title;
        this.videoUrl = videoUrl;
    }

    public TutorialVideo(int id, int tutorialId, int position, String title, String videoUrl, LocalDateTime createdAt) {
        this.id = id;
        this.tutorialId = tutorialId;
        this.position = position;
        this.title = title;
        this.videoUrl = videoUrl;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTutorialId() { return tutorialId; }
    public void setTutorialId(int tutorialId) { this.tutorialId = tutorialId; }

    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "TutorialVideo{id=" + id + ", tutorialId=" + tutorialId + ", position=" + position + ", title='" + title + "'}";
    }
}
