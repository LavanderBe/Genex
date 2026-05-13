package Genex.entities;

public class TeamRankingEntry {

    private int rank;
    private String teamId;
    private String teamName;
    private int tournaments;
    private int wins;
    private int losses;

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public int getTournaments() { return tournaments; }
    public void setTournaments(int tournaments) { this.tournaments = tournaments; }

    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }

    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }

    public double getWinRate() {
        int total = wins + losses;
        return total == 0 ? 0 : (wins * 100.0) / total;
    }
}
