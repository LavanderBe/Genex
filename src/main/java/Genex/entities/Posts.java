package Genex.entities;

import java.time.LocalDateTime;

public class Posts {
    private String id;
    private String forumId;
    private String authorId;
    private String title;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Posts() {}

    public Posts(String forumId, String authorId, String title, String body) {
        this.forumId = forumId;
        this.authorId = authorId;
        this.title = title;
        this.body = body;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getForumId() { return forumId; }
    public void setForumId(String forumId) { this.forumId = forumId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Posts{id='" + id + "', title='" + title + "', forumId='" + forumId + "'}";
    }
}