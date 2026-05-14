package Genex.services;

import Genex.entities.Tounament;
import Genex.entities.TournamentMatch;
import Genex.entities.TournamentParticipants;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to fetch tournament news and updates for dashboard banners
 */
public class TournamentNewsService {

    private final CrudTournament crudTournament;
    private final CrudTournamentMatch crudTournamentMatch;

    public TournamentNewsService() {
        this.crudTournament = new CrudTournament();
        this.crudTournamentMatch = new CrudTournamentMatch();
    }

    /**
     * Get ALL active tournament news for display
     * Returns list of all tournaments (not cancelled)
     */
    public List<TournamentNews> getAllTournamentNews() {
        System.out.println("[TournamentNewsService] Fetching ALL tournament news...");
        List<TournamentNews> allNews = new ArrayList<>();
        
        // Get ALL active tournaments (any state except CANCELLED)
        String query = "SELECT * FROM tournaments " +
                "WHERE state != 'CANCELLED' AND state != 'Annulé' " +
                "ORDER BY starts_at DESC";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                Tounament tournament = mapTournament(rs);
                String state = tournament.getState().toUpperCase();
                
                System.out.println("[TournamentNewsService] Found tournament: " + tournament.getTournamentName() + " | State: " + state);
                
                // Determine news type based on state
                TournamentNewsType newsType;
                
                if (state.contains("REGISTRATION_OPEN") || state.contains("INSCRIPTION") && state.contains("OUVERTE")) {
                    newsType = TournamentNewsType.NEW_TOURNAMENT;
                } else if (state.contains("REGISTRATION_CLOSED") || state.contains("INSCRIPTION") && state.contains("FERMÉE") || state.contains("FERMEE")) {
                    newsType = TournamentNewsType.REGISTRATION_CLOSED;
                } else if (state.contains("IN_PROGRESS") || state.contains("EN COURS") || state.contains("EN_COURS")) {
                    newsType = TournamentNewsType.ONGOING_TOURNAMENT;
                } else if (state.contains("COMPLETED") || state.contains("TERMINÉ") || state.contains("TERMINE")) {
                    newsType = TournamentNewsType.COMPLETED_TOURNAMENT;
                } else {
                    newsType = TournamentNewsType.ONGOING_TOURNAMENT;
                }
                
                allNews.add(new TournamentNews(tournament, newsType));
            }
            
            System.out.println("[TournamentNewsService] Total tournaments found: " + allNews.size());
        } catch (SQLException e) {
            System.err.println("Error fetching tournaments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return allNews;
    }

    /**
     * Get the most recent tournament news for display (DEPRECATED - use getAllTournamentNews)
     * Shows ALL tournament states with appropriate information
     */
    public TournamentNews getTournamentNews() {
        System.out.println("[TournamentNewsService] Fetching tournament news...");
        
        // Get the most recent active tournament (any state except CANCELLED)
        String query = "SELECT * FROM tournaments " +
                "WHERE state != 'CANCELLED' AND state != 'Annulé' " +
                "ORDER BY starts_at DESC LIMIT 1";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                Tounament tournament = mapTournament(rs);
                String state = tournament.getState().toUpperCase();
                
                System.out.println("[TournamentNewsService] Found tournament: " + tournament.getTournamentName() + " | State: " + state);
                
                // Determine news type based on state
                if (state.contains("REGISTRATION_OPEN") || state.contains("INSCRIPTION") && state.contains("OUVERTE")) {
                    // Inscription Ouverte → Show invitation to join
                    return new TournamentNews(tournament, TournamentNewsType.NEW_TOURNAMENT);
                    
                } else if (state.contains("REGISTRATION_CLOSED") || state.contains("INSCRIPTION") && state.contains("FERMÉE") || state.contains("FERMEE")) {
                    // Inscription Fermée → Tournament starting soon
                    return new TournamentNews(tournament, TournamentNewsType.REGISTRATION_CLOSED);
                    
                } else if (state.contains("IN_PROGRESS") || state.contains("EN COURS") || state.contains("EN_COURS")) {
                    // En Cours → Show matches/rounds
                    return new TournamentNews(tournament, TournamentNewsType.ONGOING_TOURNAMENT);
                    
                } else if (state.contains("COMPLETED") || state.contains("TERMINÉ") || state.contains("TERMINE")) {
                    // Terminé → Show final winner
                    return new TournamentNews(tournament, TournamentNewsType.COMPLETED_TOURNAMENT);
                    
                } else {
                    // Default: treat as ongoing
                    System.out.println("[TournamentNewsService] Unknown state, treating as ongoing: " + state);
                    return new TournamentNews(tournament, TournamentNewsType.ONGOING_TOURNAMENT);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tournament: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("[TournamentNewsService] No tournament news found");
        return null;
    }

    /**
     * Get the newest tournament created within the last 24 hours
     * Uses starts_at as a proxy for creation time if created_at doesn't exist
     */
    private Tounament getNewestTournament() {
        // Try with created_at first, fall back to starts_at
        String query = "SELECT * FROM tournaments " +
                "WHERE (state = ? OR state = 'REGISTRATION_OPEN' OR state = 'registration_open' OR state = 'Inscription Ouverte') " +
                "AND starts_at >= NOW() - INTERVAL 7 DAY " +
                "ORDER BY starts_at DESC LIMIT 1";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, Tounament.TournamentState.REGISTRATION_OPEN.name());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Tounament tournament = mapTournament(rs);
                // Check if tournament is relatively new (within 7 days)
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime startsAt = tournament.getStarts_at();
                if (startsAt.isAfter(now.minusDays(7))) {
                    System.out.println("[TournamentNewsService] Found new tournament: " + tournament.getTournamentName());
                    return tournament;
                }
            } else {
                System.out.println("[TournamentNewsService] No new tournament found");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching newest tournament: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    
    /**
     * Debug method to list all tournaments
     */
    public void debugListAllTournaments() {
        String query = "SELECT id, tournament_name, state, starts_at FROM tournaments ORDER BY starts_at DESC";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            
            System.out.println("=== ALL TOURNAMENTS IN DATABASE ===");
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(String.format("%d. %s | State: %s | Starts: %s",
                    count,
                    rs.getString("tournament_name"),
                    rs.getString("state"),
                    rs.getTimestamp("starts_at")
                ));
            }
            System.out.println("=== TOTAL: " + count + " tournaments ===");
        } catch (SQLException e) {
            System.err.println("Error listing tournaments: " + e.getMessage());
        }
    }

    /**
     * Get an ongoing tournament (in progress)
     */
    private Tounament getOngoingTournament() {
        // Try multiple state variations to catch all ongoing tournaments
        String query = "SELECT * FROM tournaments " +
                "WHERE (state = ? OR state = ? OR state = 'IN_PROGRESS' OR state = 'in_progress' OR state = 'En Cours') " +
                "ORDER BY starts_at DESC LIMIT 1";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, Tounament.TournamentState.IN_PROGRESS.name());
            pst.setString(2, "IN_PROGRESS");
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                Tounament tournament = mapTournament(rs);
                System.out.println("[TournamentNewsService] Found ongoing tournament: " + tournament.getTournamentName());
                return tournament;
            } else {
                System.out.println("[TournamentNewsService] No ongoing tournament found");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching ongoing tournament: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get the current round number for a tournament
     * Returns the lowest round number that has pending or in-progress matches
     */
    public int getCurrentRound(String tournamentId) {
        // First, try to find the current round based on pending/in-progress matches
        String query = "SELECT MIN(round) as current_round FROM tournament_matches " +
                "WHERE tournament_id = ? AND status IN ('PENDING', 'IN_PROGRESS')";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, tournamentId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int currentRound = rs.getInt("current_round");
                if (currentRound > 0) {
                    return currentRound;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching current round: " + e.getMessage());
        }

        // Fallback: If all matches are completed, return the highest round
        String fallbackQuery = "SELECT MAX(round) as last_round FROM tournament_matches " +
                "WHERE tournament_id = ?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(fallbackQuery);
            pst.setString(1, tournamentId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int lastRound = rs.getInt("last_round");
                return lastRound > 0 ? lastRound : 1;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching last round: " + e.getMessage());
        }

        return 1; // Default to round 1
    }

    /**
     * Get matches for a specific round
     */
    public List<TournamentMatch> getMatchesByRound(String tournamentId, int round) {
        return crudTournamentMatch.getMatchesByRound(tournamentId, round);
    }

    /**
     * Get every match for a tournament, ordered by round then match number.
     */
    public List<TournamentMatch> getAllMatches(String tournamentId) {
        return crudTournamentMatch.getAllByTournament(tournamentId);
    }

    /**
     * Get completed matches from previous rounds
     */
    public List<TournamentMatch> getPreviousRoundResults(String tournamentId, int currentRound) {
        if (currentRound <= 1) {
            return new ArrayList<>(); // No previous rounds
        }

        List<TournamentMatch> results = new ArrayList<>();
        String query = "SELECT * FROM tournament_matches " +
                "WHERE tournament_id = ? AND round = ? AND status = 'COMPLETED' " +
                "ORDER BY match_number";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, tournamentId);
            pst.setInt(2, currentRound - 1); // Previous round

            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                TournamentMatch match = new TournamentMatch();
                populateMatchFromResultSet(match, rs);
                results.add(match);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching previous round results: " + e.getMessage());
        }

        return results;
    }

    /**
     * Check if user is registered in a tournament
     */
    public boolean isUserRegistered(String tournamentId, String userId) {
        String query = "SELECT COUNT(*) FROM tournament_participants " +
                "WHERE tournament_id = ? AND participant_id = ?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, tournamentId);
            pst.setString(2, userId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking user registration: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get participant count for a tournament
     */
    public int getParticipantCount(String tournamentId) {
        String query = "SELECT COUNT(*) FROM tournament_participants WHERE tournament_id = ?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, tournamentId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching participant count: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Get the winner of a completed tournament
     */
    public String getTournamentWinner(String tournamentId) {
        String query = "SELECT participant_id FROM tournament_participants " +
                "WHERE tournament_id = ? AND status = 'WINNER' LIMIT 1";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, tournamentId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getString("participant_id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tournament winner: " + e.getMessage());
        }
        
        // Fallback: Get the player with most wins
        query = "SELECT winner_id, COUNT(*) as wins FROM tournament_matches " +
                "WHERE tournament_id = ? AND status = 'COMPLETED' AND winner_id IS NOT NULL " +
                "GROUP BY winner_id ORDER BY wins DESC LIMIT 1";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, tournamentId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getString("winner_id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching winner by wins: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Get a participant display name by ID.
     * Match participant IDs can point to either a player user_id or a team id.
     */
    public String getPlayerName(String playerId) {
        if (playerId == null || playerId.isEmpty()) {
            return "TBD";
        }

        String playerQuery = "SELECT p.first_name, p.last_name, p.nickname, u.username " +
                "FROM players p " +
                "LEFT JOIN users u ON p.user_id = u.id " +
                "WHERE p.user_id = ?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(playerQuery);
            pst.setString(1, playerId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String nickname = rs.getString("nickname");
                if (nickname != null && !nickname.trim().isEmpty()) {
                    return nickname.trim();
                }

                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String fullName = ((firstName == null ? "" : firstName) + " " +
                        (lastName == null ? "" : lastName)).trim();
                if (!fullName.isEmpty()) {
                    return fullName;
                }

                String username = rs.getString("username");
                if (username != null && !username.trim().isEmpty()) {
                    return username.trim();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching player name for ID " + playerId + ": " + e.getMessage());
        }

        String teamName = getTeamName(playerId);
        if (teamName != null) {
            return teamName;
        }

        return "Participant #" + shortId(playerId);
    }

    /**
     * Get team name by ID.
     */
    public String getTeamName(String teamId) {
        if (teamId == null || teamId.isEmpty()) {
            return null;
        }

        String query = "SELECT name FROM teams WHERE id = ?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String teamName = rs.getString("name");
                if (teamName != null && !teamName.trim().isEmpty()) {
                    return teamName.trim();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching team name for ID " + teamId + ": " + e.getMessage());
        }

        return null;
    }

    private String shortId(String id) {
        return id == null || id.length() <= 8 ? id : id.substring(0, 8);
    }

    /**
     * Get game name by ID
     */
    public String getGameName(String gameId) {
        if (gameId == null || gameId.trim().isEmpty()) {
            return "Game TBD";
        }

        String query = "SELECT name FROM games WHERE id = ? OR name = ? LIMIT 1";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, gameId);
            pst.setString(2, gameId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String gameName = rs.getString("name");
                if (gameName != null && !gameName.trim().isEmpty()) {
                    return gameName.trim();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching game name by id: " + e.getMessage());
        }

        return gameId;
    }

    /**
     * Get center name by ID.
     */
    public String getCenterName(String centerId) {
        if (centerId == null || centerId.isEmpty()) {
            return "Online";
        }

        String query = "SELECT name FROM centers WHERE id = ?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, centerId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String centerName = rs.getString("name");
                return centerName == null || centerName.trim().isEmpty() ? "Center #" + centerId : centerName;
            }
        } catch (SQLException e) {
            System.err.println("Error fetching center name: " + e.getMessage());
        }

        return "Center #" + centerId;
    }

    // Helper methods
    private Tounament mapTournament(ResultSet rs) throws SQLException {
        Tounament t = new Tounament();
        t.setTournamentId(rs.getString("id"));
        t.setTournamentName(rs.getString("tournament_name"));
        t.setGame_id(rs.getString("game_id"));
        t.setCenter_id(rs.getString("center_id"));
        t.setFormat(rs.getString("format"));
        t.setParticipant_type(rs.getString("participant_type"));
        t.setStarts_at(rs.getTimestamp("starts_at").toLocalDateTime());
        t.setEnds_at(rs.getTimestamp("ends_at").toLocalDateTime());
        t.setPrize_pool(rs.getDouble("prize_pool"));
        t.setState(rs.getString("state"));
        t.setMaxPlayers(rs.getInt("max_players"));
        t.setChallongeId(rs.getString("challonge_id"));
        t.setChallongeUrl(rs.getString("challonge_url"));
        t.setChallongeUrlSlug(rs.getString("challonge_url_slug"));
        t.setSynced(rs.getBoolean("is_synced"));
        t.setStarted(rs.getBoolean("is_started"));
        return t;
    }

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
    }

    // Inner classes
    public static class TournamentNews {
        private final Tounament tournament;
        private final TournamentNewsType type;

        public TournamentNews(Tounament tournament, TournamentNewsType type) {
            this.tournament = tournament;
            this.type = type;
        }

        public Tounament getTournament() {
            return tournament;
        }

        public TournamentNewsType getType() {
            return type;
        }
    }

    public enum TournamentNewsType {
        NEW_TOURNAMENT,           // REGISTRATION_OPEN - Show invitation
        REGISTRATION_CLOSED,      // REGISTRATION_CLOSED - Starting soon
        ONGOING_TOURNAMENT,       // IN_PROGRESS - Show matches/rounds
        COMPLETED_TOURNAMENT      // COMPLETED - Show final winner
    }
}
