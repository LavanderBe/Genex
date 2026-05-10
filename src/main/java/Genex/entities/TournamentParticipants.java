package Genex.entities;

public class TournamentParticipants {

    private String id;
    private String tournamentId;
    private String participantId; // player_id OR team_id
    private String tournamentType; // "SOLO" or "TEAM"
    private int seed;
    private Status status;
    private String challongeParticipantId;
    private String eliminationReason;  // LOST or WITHDREW
    private Integer finalPlacement;    // 1st, 2nd, 3rd...
    private Integer eliminatedAtRound; // which round they lost/withdrew

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

    public String getChallongeParticipantId() { return challongeParticipantId; }
    public void setChallongeParticipantId(String challongeParticipantId) { this.challongeParticipantId = challongeParticipantId; }

    public String getEliminationReason() { return eliminationReason; }
    public void setEliminationReason(String eliminationReason) { this.eliminationReason = eliminationReason; }

    public Integer getFinalPlacement() { return finalPlacement; }
    public void setFinalPlacement(Integer finalPlacement) { this.finalPlacement = finalPlacement; }

    public Integer getEliminatedAtRound() { return eliminatedAtRound; }
    public void setEliminatedAtRound(Integer eliminatedAtRound) { this.eliminatedAtRound = eliminatedAtRound; }

    // Helper: is player still competing?
    public boolean isActive() { return status == Status.ACTIVE; }
    public boolean isEliminated() { return status == Status.ELIMINATED; }
    public boolean isWinner() { return status == Status.WINNER; }
    public boolean withdrewFromTournament() { return "WITHDREW".equals(eliminationReason); }
    public boolean lostInTournament() { return "LOST".equals(eliminationReason); }

    @Override
    public String toString() {
        return "TournamentParticipants{id='" + id + "', participantId='" + participantId + "', status=" + status + '}';
    }
}
