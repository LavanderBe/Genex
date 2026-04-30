package Genex.entities;

import java.time.LocalDateTime;

public class Posts {
    private String id;
    private String forumId;
    private String authorId;
    private String title;
    private String body;
    private String mediaType;
    private String mediaUrl;
    private String postType;
    private String tag;
    private String postStatus;
    private String moderationStatus;
    private int views;
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

    public Posts(String forumId, String authorId, String title, String body, String mediaType, String mediaUrl) {
        this.forumId = forumId;
        this.authorId = authorId;
        this.title = title;
        this.body = body;
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl;
        this.postType = "text";
        this.postStatus = "published";
        this.moderationStatus = "visible";
        this.views = 0;
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

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getPostType() { return postType; }
    public void setPostType(String postType) { this.postType = postType; }

    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }

    public String getPostStatus() { return postStatus; }
    public void setPostStatus(String postStatus) { this.postStatus = postStatus; }

    public String getModerationStatus() { return moderationStatus; }
    public void setModerationStatus(String moderationStatus) { this.moderationStatus = moderationStatus; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "Posts{id='" + id + "', title='" + title + "', forumId='" + forumId + "'}";
    }
}
