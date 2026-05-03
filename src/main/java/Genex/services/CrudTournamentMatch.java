package Genex.services;

import Genex.entities.TournamentMatch;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CrudTournamentMatch implements ICrud<TournamentMatch> {

    public CrudTournamentMatch() {}

    @Override
    public void addEntity(TournamentMatch match) {
        String query = "INSERT INTO tournament_matches " +
                "(tournament_id, challonge_match_id, round, match_number, " +
                "player1_id, player2_id, winner_id, player1_score, player2_score, status, " +
                "scheduled_time, completed_time) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            pst.setString(1, match.getTournamentId());
            pst.setString(2, match.getChallongeMatchId());
            pst.setInt(3, match.getRound());
            pst.setInt(4, match.getMatchNumber());
            pst.setString(5, match.getPlayer1Id());
            pst.setString(6, match.getPlayer2Id());
            pst.setString(7, match.getWinnerId());
            pst.setInt(8, match.getPlayer1Score());
            pst.setInt(9, match.getPlayer2Score());
            pst.setString(10, match.getStatus().name());
            pst.setTimestamp(11, match.getScheduledTime() != null ? Timestamp.valueOf(match.getScheduledTime()) : null);
            pst.setTimestamp(12, match.getCompletedTime() != null ? Timestamp.valueOf(match.getCompletedTime()) : null);
            pst.executeUpdate();
            
            // Get the auto-generated ID
            ResultSet rs = pst.getGeneratedKeys();
            if (rs.next()) {
                match.setId(rs.getString(1));
            }
            
            System.out.println("Match added successfully with ID: " + match.getId());
        } catch (SQLException e) {
            throw new RuntimeException("Error adding match", e);
        }
    }

    @Override
    public void updateEntity(TournamentMatch match, String id) {
        String query = "UPDATE tournament_matches SET " +
                "tournament_id=?, challonge_match_id=?, round=?, match_number=?, " +
                "player1_id=?, player2_id=?, winner_id=?, player1_score=?, player2_score=?, " +
                "status=?, scheduled_time=?, completed_time=? WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, match.getTournamentId());
            pst.setString(2, match.getChallongeMatchId());
            pst.setInt(3, match.getRound());
            pst.setInt(4, match.getMatchNumber());
            pst.setString(5, match.getPlayer1Id());
            pst.setString(6, match.getPlayer2Id());
            pst.setString(7, match.getWinnerId());
            pst.setInt(8, match.getPlayer1Score());
            pst.setInt(9, match.getPlayer2Score());
            pst.setString(10, match.getStatus().name());
            pst.setTimestamp(11, match.getScheduledTime() != null ? Timestamp.valueOf(match.getScheduledTime()) : null);
            pst.setTimestamp(12, match.getCompletedTime() != null ? Timestamp.valueOf(match.getCompletedTime()) : null);
            pst.setString(13, id);
            pst.executeUpdate();
            System.out.println("Match updated successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Error updating match", e);
        }
    }

    @Override
    public void deleteEntity(TournamentMatch match) {
        String query = "DELETE FROM tournament_matches WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, match.getId());
            pst.executeUpdate();
            System.out.println("Match deleted successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting match", e);
        }
    }

    @Override
    public void getEntity(TournamentMatch match) {
        String query = "SELECT * FROM tournament_matches WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, match.getId());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                populateMatchFromResultSet(match, rs);
                System.out.println("Match loaded");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading match", e);
        }
    }

    // Get all matches for a tournament
    public List<TournamentMatch> getAllByTournament(String tournamentId) {
        List<TournamentMatch> matches = new ArrayList<>();
        String query = "SELECT * FROM tournament_matches WHERE tournament_id=? ORDER BY round, match_number";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, tournamentId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                TournamentMatch match = new TournamentMatch();
                populateMatchFromResultSet(match, rs);
                matches.add(match);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading matches for tournament", e);
        }

        return matches;
    }

    // Get matches by round
    public List<TournamentMatch> getMatchesByRound(String tournamentId, int round) {
        List<TournamentMatch> matches = new ArrayList<>();
        String query = "SELECT * FROM tournament_matches WHERE tournament_id=? AND round=? ORDER BY match_number";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, tournamentId);
            pst.setInt(2, round);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                TournamentMatch match = new TournamentMatch();
                populateMatchFromResultSet(match, rs);
                matches.add(match);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading matches for round", e);
        }

        return matches;
    }

    // Get pending matches
    public List<TournamentMatch> getPendingMatches(String tournamentId) {
        List<TournamentMatch> matches = new ArrayList<>();
        String query = "SELECT * FROM tournament_matches WHERE tournament_id=? AND status='PENDING' ORDER BY round, match_number";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, tournamentId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                TournamentMatch match = new TournamentMatch();
                populateMatchFromResultSet(match, rs);
                matches.add(match);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading pending matches", e);
        }

        return matches;
    }

    // Update match score and winner
    public void updateMatchResult(String matchId, int player1Score, int player2Score, String winnerId) {
        String query = "UPDATE tournament_matches SET " +
                "player1_score=?, player2_score=?, winner_id=?, " +
                "status='COMPLETED', completed_time=NOW() WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, player1Score);
            pst.setInt(2, player2Score);
            pst.setString(3, winnerId);
            pst.setString(4, matchId);
            pst.executeUpdate();
            System.out.println("Match result updated successfully");
        } catch (SQLException e) {
            throw new RuntimeException("Error updating match result", e);
        }
    }

    // Helper method to populate match from ResultSet
    private void populateMatchFromResultSet(TournamentMatch match, ResultSet rs) throws SQLException {
        match.setId(rs.getString("id"));
        match.setTournamentId(rs.getString("tournament_id"));
        match.setChallongeMatchId(rs.getString("challonge_match_id"));
        match.setRound(rs.getInt("round"));
        match.setMatchNumber(rs.getInt("match_number"));
        match.setPlayer1Id(rs.getString("player1_id"));
        match.setPlayer2Id(rs.getString("player2_id"));
        match.setWinnerId(rs.getString("winner_id"));
        match.setPlayer1Score(rs.getInt("player1_score"));
        match.setPlayer2Score(rs.getInt("player2_score"));
        match.setStatus(TournamentMatch.MatchStatus.valueOf(rs.getString("status")));
        
        Timestamp scheduled = rs.getTimestamp("scheduled_time");
        if (scheduled != null) match.setScheduledTime(scheduled.toLocalDateTime());
        
        Timestamp completed = rs.getTimestamp("completed_time");
        if (completed != null) match.setCompletedTime(completed.toLocalDateTime());
        
        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) match.setCreatedAt(created.toLocalDateTime());
        
        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) match.setUpdatedAt(updated.toLocalDateTime());
    }
}
