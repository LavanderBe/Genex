package Genex.entities;

import java.time.LocalDateTime;

public class TeamMember {

    private String id;
    private String teamId;
    private String playerId;
    private String playerName;
    private LocalDateTime joinedAt;

    // Default constructor
    public TeamMember() {}

    // Full constructor
    public TeamMember(String teamId, String playerId, String playerName) {
        this.teamId = teamId;
        this.playerId = playerId;
        this.playerName = playerName;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    @Override
    public String toString() {
        return "TeamMember{" +
                "id='" + id + '\'' +
                ", teamId='" + teamId + '\'' +
                ", playerId='" + playerId + '\'' +
                ", playerName='" + playerName + '\'' +
                ", joinedAt=" + joinedAt +
                '}';
    }
}
