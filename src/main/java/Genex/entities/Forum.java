package Genex.entities;

import java.time.LocalDateTime;

public class Forum {
    private String id;
    private String title;
    private String description;
    private String createdBy;
    private String category;
    private String topicStatus;
    private String moderationStatus;
    private boolean pinned;
    private LocalDateTime createdAt;

    public Forum() {}

    public Forum(String title, String description, String createdBy) {
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.category = "General";
        this.topicStatus = "open";
        this.moderationStatus = "visible";
        this.pinned = false;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTopicStatus() { return topicStatus; }
    public void setTopicStatus(String topicStatus) { this.topicStatus = topicStatus; }

    public String getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(String moderationStatus) { this.moderationStatus = moderationStatus; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Forum{id='" + id + "', title='" + title + "', createdBy='" + createdBy + "'}";
    }
}
