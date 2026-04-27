package Genex.entities;

public class TournamentParticipants {

    private String id;
    private String tournamentId;
    private String participantId;  //tekho id team wla platyer solo
    private int seed;
    private Status status;

    public TournamentParticipants() {

    }

    public enum Status {
        ACTIVE, ELIMINATED, WINNER
    }

    // Constructor
    public TournamentParticipants(String tournamentId, String participantId, int seed) {
        this.tournamentId = tournamentId;
        this.participantId = participantId;
        this.seed = seed;
        this.status = Status.ACTIVE; // default
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }

    public String getParticipantId() { return participantId; }
    public void setParticipantId(String participantId) { this.participantId = participantId; }

    public int getSeed() { return seed; }
    public void setSeed(int seed) { this.seed = seed; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    @Override
    public String toString() {
        return "TournamentParticipant{" +
                "id='" + id + '\'' +
                ", tournamentId='" + tournamentId + '\'' +
                ", participantId='" + participantId + '\'' +
                ", seed=" + seed +
                ", status=" + status +
                '}';
    }
}
