package Genex.entities;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Team {

    private String teamId;
    private String name;
    private String gameId;
    private LocalDate foundationDate;
    private String logoImage;
    private String contact;
    private Status status;
    private List<Training> trainings; // ← training list

    public enum Status {
        ACTIVE, INACTIVE, DISBANDED
    }

    // Default constructor
    public Team() {
        this.trainings = new ArrayList<>();
    }

    // Full constructor
    public Team(String name, String gameId, LocalDate foundationDate,
                String logoImage, String contact, Status status) {
        this.name = name;
        this.gameId = gameId;
        this.foundationDate = foundationDate;
        this.logoImage = logoImage;
        this.contact = contact;
        this.status = status;
        this.trainings = new ArrayList<>();
    }

    // Add training to list
    public void addTraining(Training training) {
        this.trainings.add(training);
    }

    // Getters & Setters
    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }

    public LocalDate getFoundationDate() { return foundationDate; }
    public void setFoundationDate(LocalDate foundationDate) { this.foundationDate = foundationDate; }

    public String getLogoImage() { return logoImage; }
    public void setLogoImage(String logoImage) { this.logoImage = logoImage; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public List<Training> getTrainings() { return trainings; }
    public void setTrainings(List<Training> trainings) { this.trainings = trainings; }

    @Override
    public String toString() {
        return "Team{" +
                "teamId='" + teamId + '\'' +
                ", name='" + name + '\'' +
                ", gameId='" + gameId + '\'' +
                ", foundationDate=" + foundationDate +
                ", logoImage='" + logoImage + '\'' +
                ", contact='" + contact + '\'' +
                ", status=" + status +
                ", trainings=" + trainings +
                '}';
    }
}