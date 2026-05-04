package Genex.entities;

import java.time.LocalDate;

public class Tutorial {
    private int id;
    private String title;
    private String description;
    private String videoUrl;
    private String category;
    private String difficulty;
    private LocalDate createdAt;

    public Tutorial() {}

    public Tutorial(String title, String description, String videoUrl, String category, String difficulty, LocalDate createdAt) {
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.category = category;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
    }

    public Tutorial(int id, String title, String description, String videoUrl, String category, String difficulty, LocalDate createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.videoUrl = videoUrl;
        this.category = category;
        this.difficulty = difficulty;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Tutorial{id=" + id + ", title='" + title + "', category='" + category + "', difficulty='" + difficulty + "'}";
    }
}