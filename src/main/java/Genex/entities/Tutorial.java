package Genex.entities;

import java.sql.Timestamp;

public class Tutorial {
    private int id;
    private String title;
    private String description;
    private String video_url;
    private String category;
    private String difficulty;
    private Timestamp created_at;

    public Tutorial() {}

    public Tutorial(int id, String title, String description, String video_url, String category, String difficulty) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.video_url = video_url;
        this.category = category;
        this.difficulty = difficulty;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVideo_url() { return video_url; }
    public void setVideo_url(String video_url) { this.video_url = video_url; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }
}
