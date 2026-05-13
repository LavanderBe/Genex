package Genex.services;

import Genex.entities.TeamRankingEntry;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeamRankingService {

    public List<TeamRankingEntry> getTeamRankings() {
        List<TeamRankingEntry> rankings = new ArrayList<>();
        String query = "SELECT t.id, t.name, " +
                "COUNT(DISTINCT tp.tournament_id) AS tournaments, " +
                "SUM(CASE WHEN tm.status='COMPLETED' AND tm.winner_id=t.id THEN 1 ELSE 0 END) AS wins, " +
                "SUM(CASE WHEN tm.status='COMPLETED' " +
                "    AND (tm.player1_id=t.id OR tm.player2_id=t.id) " +
                "    AND tm.winner_id IS NOT NULL AND tm.winner_id<>t.id THEN 1 ELSE 0 END) AS losses " +
                "FROM teams t " +
                "LEFT JOIN tournament_participants tp ON tp.participant_id=t.id AND tp.tournament_type='TEAM' " +
                "LEFT JOIN tournament_matches tm ON tm.tournament_id=tp.tournament_id " +
                "    AND (tm.player1_id=t.id OR tm.player2_id=t.id) " +
                "GROUP BY t.id, t.name " +
                "ORDER BY wins DESC, (wins / GREATEST(wins + losses, 1)) DESC, tournaments DESC, t.name ASC";

        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query)) {
            ResultSet rs = pst.executeQuery();
            int rank = 1;
            while (rs.next()) {
                TeamRankingEntry entry = new TeamRankingEntry();
                entry.setRank(rank++);
                entry.setTeamId(rs.getString("id"));
                entry.setTeamName(rs.getString("name"));
                entry.setTournaments(rs.getInt("tournaments"));
                entry.setWins(rs.getInt("wins"));
                entry.setLosses(rs.getInt("losses"));
                rankings.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("Error loading team rankings: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return rankings;
    }
}
