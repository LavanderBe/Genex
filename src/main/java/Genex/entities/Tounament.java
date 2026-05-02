package Genex.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Tounament {

    private String tournamentId;
    private String tournamentName;
    private String game_id;
    private String center_id;
    private String format;
    private String participant_type;
    private LocalDateTime starts_at;
    private LocalDateTime ends_at;
    private double prize_pool;
    private String state;
    private List<TournamentParticipants> participants;

    public enum Format {
        ROUND_ROBIN, SINGLE_ELIM, DOUBLE_ELIM
    }

    public enum ParticipantType {
        SOLO, TEAM
    }

    public enum TournamentState {
        REGISTRATION_OPEN("Inscription Ouverte"),
        REGISTRATION_CLOSED("Inscription Fermée"),
        IN_PROGRESS("En Cours"),
        COMPLETED("Terminé"),
        CANCELLED("Annulé");

        private final String displayName;

        TournamentState(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Default constructor
    public Tounament() {
        this.participants = new ArrayList<>();
    }

    // Full constructor
    public Tounament(String name, String game_id, String center_id, String format,
                     String participant_type, LocalDateTime starts_at,
                     LocalDateTime ends_at, double prize_pool) {
        this.tournamentName = name;
        this.game_id = game_id;
        this.center_id = center_id;
        this.format = format;
        this.participant_type = participant_type;
        this.starts_at = starts_at;
        this.ends_at = ends_at;
        this.prize_pool = prize_pool;
        this.state = TournamentState.REGISTRATION_OPEN.name();
        this.participants = new ArrayList<>();
    }

    // Add participant to the list
    public void addParticipant(TournamentParticipants participant) {
        this.participants.add(participant);
    }

    // Getters & Setters
    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }

    public String getTournamentName() { return tournamentName; }
    public void setTournamentName(String tournamentName) { this.tournamentName = tournamentName; }

    public String getGame_id() { return game_id; }
    public void setGame_id(String game_id) { this.game_id = game_id; }

    public String getCenter_id() { return center_id; }
    public void setCenter_id(String center_id) { this.center_id = center_id; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getParticipant_type() { return participant_type; }
    public void setParticipant_type(String participant_type) { this.participant_type = participant_type; }

    public LocalDateTime getStarts_at() { return starts_at; }
    public void setStarts_at(LocalDateTime starts_at) { this.starts_at = starts_at; }

    public LocalDateTime getEnds_at() { return ends_at; }
    public void setEnds_at(LocalDateTime ends_at) { this.ends_at = ends_at; }

    public double getPrize_pool() { return prize_pool; }
    public void setPrize_pool(double prize_pool) { this.prize_pool = prize_pool; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public List<TournamentParticipants> getParticipants() { return participants; }
    public void setParticipants(List<TournamentParticipants> participants) { this.participants = participants; }

    @Override
    public String toString() {
        return "Tounament{" +
                "tournamentId='" + tournamentId + '\'' +
                ", tournamentName='" + tournamentName + '\'' +
                ", game_id='" + game_id + '\'' +
                ", center_id='" + center_id + '\'' +
                ", format='" + format + '\'' +
                ", participant_type='" + participant_type + '\'' +
                ", starts_at=" + starts_at +
                ", ends_at=" + ends_at +
                ", prize_pool=" + prize_pool +
                ", state='" + state + '\'' +
                ", participants=" + participants +
                '}';
    }
}