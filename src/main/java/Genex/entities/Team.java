package Genex.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Team {

    private String id;
    private String createdBy;
    private String gameId;
    private String name;
    private LocalDate date;
    private String logoImage;
    private String contact;
    private Status status;
    private LocalDateTime createdAt;

    public enum Status {
        ACTIVE, INACTIVE
    }

    // Default constructor
    public Team() {}

    // Full constructor
    public Team(String createdBy, String gameId, String name, LocalDate date,
                String logoImage, String contact, Status status) {
        this.createdBy = createdBy;
        this.gameId    = gameId;
        this.name      = name;
        this.date      = date;
        this.logoImage = logoImage;
        this.contact   = contact;
        this.status    = status;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getLogoImage() { return logoImage; }
    public void setLogoImage(String logoImage) { this.logoImage = logoImage; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Team{" +
                "id='"         + id         + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", gameId='"    + gameId    + '\'' +
                ", name='"      + name      + '\'' +
                ", date="       + date             +
                ", logoImage='" + logoImage + '\'' +
                ", contact='"   + contact   + '\'' +
                ", status="     + status          +
                ", createdAt="  + createdAt        +
                '}';
    }
}