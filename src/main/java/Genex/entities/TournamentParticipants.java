package Genex.entities;

public class TournamentParticipants {

    private String id;
    private String tournamentId;
    private String participantId; // player_id OR team_id
    private String tournamentType; // "SOLO" or "TEAM"
    private int seed;
    private Status status;

    public enum Status {
        ACTIVE, ELIMINATED, WINNER
    }

    // Default constructor
    public TournamentParticipants() {}

    // Static constructor for SOLO
    public static TournamentParticipants solo(String tournamentId, String playerId, int seed) {
        TournamentParticipants p = new TournamentParticipants();
        p.tournamentId = tournamentId;
        p.participantId = playerId;
        p.tournamentType = "SOLO";
        p.seed = seed;
        p.status = Status.ACTIVE;
        return p;
    }

    // Static constructor for TEAM
    public static TournamentParticipants team(String tournamentId, String teamId, int seed) {
        TournamentParticipants p = new TournamentParticipants();
        p.tournamentId = tournamentId;
        p.participantId = teamId;
        p.tournamentType = "TEAM";
        p.seed = seed;
        p.status = Status.ACTIVE;
        return p;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }

    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public String getTournamentType() { return tournamentType; }
    public void setTournamentType(String tournamentType) { this.tournamentType = tournamentType; }

    public int getSeed() { return seed; }
    public void setSeed(int seed) { this.seed = seed; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return "TournamentParticipants{" +
                "id='" + id + '\'' +
                ", tournamentId='" + tournamentId + '\'' +
                ", participantId='" + participantId + '\'' +
                ", tournamentType='" + tournamentType + '\'' +
                ", seed=" + seed +
                ", status=" + status +
                '}';
    }
}