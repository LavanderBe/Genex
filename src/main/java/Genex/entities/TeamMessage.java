package Genex.entities;

import java.time.LocalDateTime;

public class TeamMessage {
    
    public enum MessageType {
        USER,
        SYSTEM
    }
    
    private String id;
    private String teamId;
    private String senderId;
    private String senderName;
    private String message;
    private MessageType messageType;
    private LocalDateTime sentAt;
    private boolean isEdited;
    private LocalDateTime editedAt;
    
    // Constructors
    public TeamMessage() {
        this.messageType = MessageType.USER;
        this.isEdited = false;
    }
    
    public TeamMessage(String teamId, String senderId, String message) {
        this();
        this.teamId = teamId;
        this.senderId = senderId;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }
    
    public TeamMessage(String id, String teamId, String senderId, String message, 
                      MessageType messageType, LocalDateTime sentAt) {
        this.id = id;
        this.teamId = teamId;
        this.senderId = senderId;
        this.message = message;
        this.messageType = messageType;
        this.sentAt = sentAt;
        this.isEdited = false;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTeamId() {
        return teamId;
    }
    
    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
    
    public String getSenderId() {
        return senderId;
    }
    
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }
    
    public String getSenderName() {
        return senderName;
    }
    
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }
    
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    public boolean isEdited() {
        return isEdited;
    }
    
    public void setEdited(boolean edited) {
        isEdited = edited;
    }
    
    public LocalDateTime getEditedAt() {
        return editedAt;
    }
    
    public void setEditedAt(LocalDateTime editedAt) {
        this.editedAt = editedAt;
    }
    
    @Override
    public String toString() {
        return "TeamMessage{" +
                "id='" + id + '\'' +
                ", teamId='" + teamId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", senderName='" + senderName + '\'' +
                ", message='" + message + '\'' +
                ", messageType=" + messageType +
                ", sentAt=" + sentAt +
                ", isEdited=" + isEdited +
                '}';
    }
}
