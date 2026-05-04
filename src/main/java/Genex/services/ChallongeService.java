package Genex.services;

import Genex.entities.TournamentParticipants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Service for integrating with Challonge API
 * Handles tournament creation, participant management, and bracket generation
 */
public class ChallongeService {

    private final String apiKey;
    private final String username;
    private final String baseUrl;
    private final String authHeader;

    public ChallongeService() {
        // Load configuration from properties file
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/challonge.properties"));
            this.apiKey = props.getProperty("challonge.api.key");
            this.username = props.getProperty("challonge.api.username");
            this.baseUrl = props.getProperty("challonge.base.url");
            
            // Create Basic Auth header: username:api_key encoded in Base64
            String credentials = username + ":" + apiKey;
            this.authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Challonge configuration", e);
        }
    }

    /**
     * Create a tournament on Challonge with participants
     * 
     * @param tournamentName Name of the tournament
     * @param tournamentType Tournament format (single_elimination, double_elimination)
     * @param participants List of participants to add
     * @return Challonge tournament ID
     */
    public ChallongeResponse createTournament(String tournamentName, String tournamentType, 
                                             List<TournamentParticipants> participants) {
        try {
            // Step 1: Create tournament
            String tournamentUrl = createUniqueTournamentUrl(tournamentName);
            TournamentCreationResult result = createTournamentOnChallonge(tournamentName, tournamentUrl, tournamentType);
            
            // Step 2: Add participants
            addParticipantsToChallonge(result.urlSlug, participants);
            
            // Step 3: Generate URLs
            String publicUrl = "https://challonge.com/" + result.urlSlug;
            String embedUrl = publicUrl + "/module";
            
            // Return both the numeric ID and URL slug
            return new ChallongeResponse(result.id, result.urlSlug, publicUrl, embedUrl);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create tournament on Challonge", e);
        }
    }

    /**
     * Start the tournament and generate brackets
     * 
     * @param challongeId Challonge tournament ID
     */
    public void startTournament(String challongeId) {
        try {
            // Challonge API requires the tournament ID in the URL
            URL url = new URL(baseUrl + "/tournaments/" + challongeId + "/start.json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", authHeader);
            conn.setRequestProperty("Content-Type", "application/json");
            
            // POST request needs a body, even if empty
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(new byte[0]);
            }
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                System.out.println("Tournament started successfully on Challonge");
                return;
            }
            
            // Try to read error details
            String error = "";
            try {
                if (conn.getErrorStream() != null) {
                    error = readErrorResponse(conn);
                }
            } catch (Exception e) {
                error = "Could not read error details";
            }
            
            // Common Challonge errors
            String errorMsg = "Failed to start tournament. Response code: " + responseCode;
            if (!error.isEmpty()) {
                errorMsg += ", Error: " + error;
            }
            
            if (responseCode == 400) {
                errorMsg += ". Common causes: Tournament already started, or not enough participants (minimum 2 required).";
            } else if (responseCode == 401) {
                errorMsg += ". Authentication failed - check API credentials.";
            } else if (responseCode == 404) {
                errorMsg += ". Tournament not found on Challonge.";
            }
            
            throw new RuntimeException(errorMsg);
            
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Failed to start tournament on Challonge: " + e.getMessage(), e);
        }
    }

    /**
     * Get the public bracket URL
     * 
     * @param tournamentUrl Tournament URL slug
     * @return Public bracket URL
     */
    public String getTournamentUrl(String tournamentUrl) {
        return "https://challonge.com/" + tournamentUrl;
    }

    /**
     * Get the embed iframe URL for WebView
     * 
     * @param tournamentUrl Tournament URL slug
     * @return Embed URL for iframe
     */
    public String getEmbedUrl(String tournamentUrl) {
        return "https://challonge.com/" + tournamentUrl + "/module";
    }
    
    /**
     * Fetch all matches from Challonge for a tournament
     * 
     * @param tournamentUrlSlug Tournament URL slug
     * @return List of matches from Challonge
     */
    public List<ChallongeMatch> fetchMatches(String tournamentUrlSlug) {
        try {
            URL url = new URL(baseUrl + "/tournaments/" + tournamentUrlSlug + "/matches.json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", authHeader);
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("Failed to fetch matches. Response code: " + responseCode);
            }
            
            String response = readResponse(conn);
            JSONArray matchesArray = new JSONArray(response);
            
            List<ChallongeMatch> matches = new ArrayList<>();
            for (int i = 0; i < matchesArray.length(); i++) {
                JSONObject matchObj = matchesArray.getJSONObject(i).getJSONObject("match");
                ChallongeMatch match = parseChallongeMatch(matchObj);
                matches.add(match);
            }
            
            return matches;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch matches from Challonge", e);
        }
    }
    
    /**
     * Update match result on Challonge
     * 
     * @param tournamentUrlSlug Tournament URL slug
     * @param matchId Challonge match ID
     * @param player1Score Player 1 score
     * @param player2Score Player 2 score
     * @param winnerId Winner participant ID
     */
    public void updateMatchResult(String tournamentUrlSlug, String matchId, 
                                  int player1Score, int player2Score, String winnerId) {
        try {
            URL url = new URL(baseUrl + "/tournaments/" + tournamentUrlSlug + "/matches/" + matchId + ".json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Authorization", authHeader);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // Build score CSV (e.g., "2-1" or "3-0")
            String scoresCsv = player1Score + "-" + player2Score;
            
            JSONObject match = new JSONObject();
            match.put("scores_csv", scoresCsv);
            match.put("winner_id", winnerId);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("match", match);
            
            // Send request
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                String error = readErrorResponse(conn);
                throw new RuntimeException("Failed to update match. Response: " + error);
            }
            
            System.out.println("Match result updated on Challonge");
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to update match on Challonge", e);
        }
    }
    
    private ChallongeMatch parseChallongeMatch(JSONObject matchObj) {
        ChallongeMatch match = new ChallongeMatch();
        match.setId(matchObj.get("id").toString());
        match.setRound(matchObj.getInt("round"));
        
        // Get participant IDs (can be null if TBD)
        Object p1 = matchObj.get("player1_id");
        Object p2 = matchObj.get("player2_id");
        match.setPlayer1Id(p1 != JSONObject.NULL ? p1.toString() : null);
        match.setPlayer2Id(p2 != JSONObject.NULL ? p2.toString() : null);
        
        // Get winner ID
        Object winner = matchObj.get("winner_id");
        match.setWinnerId(winner != JSONObject.NULL ? winner.toString() : null);
        
        // Parse scores
        String scoresCsv = matchObj.optString("scores_csv", "");
        if (!scoresCsv.isEmpty() && scoresCsv.contains("-")) {
            String[] scores = scoresCsv.split("-");
            if (scores.length == 2) {
                try {
                    match.setPlayer1Score(Integer.parseInt(scores[0].trim()));
                    match.setPlayer2Score(Integer.parseInt(scores[1].trim()));
                } catch (NumberFormatException e) {
                    // Invalid scores, keep defaults
                }
            }
        }
        
        // Determine status
        String state = matchObj.optString("state", "pending");
        match.setCompleted("complete".equals(state));
        
        return match;
    }

    // ─── Private Helper Methods ──────────────────────────────────────────────

    private String createUniqueTournamentUrl(String tournamentName) {
        // Create URL-safe slug from tournament name
        String slug = tournamentName.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_|_$", "");
        
        // Add timestamp to ensure uniqueness
        return slug + "_" + System.currentTimeMillis();
    }

    private TournamentCreationResult createTournamentOnChallonge(String name, String url, String tournamentType) throws Exception {
        URL apiUrl = new URL(baseUrl + "/tournaments.json");
        HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", authHeader);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Map tournament type
        String challongeType = mapTournamentType(tournamentType);

        // Build JSON request body
        JSONObject tournament = new JSONObject();
        tournament.put("name", name);
        tournament.put("url", url);
        tournament.put("tournament_type", challongeType);
        tournament.put("open_signup", false);  // Closed registration, we add participants manually
        tournament.put("show_rounds", true);
        tournament.put("private", false);  // Public bracket

        JSONObject requestBody = new JSONObject();
        requestBody.put("tournament", tournament);

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Read response
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String error = readErrorResponse(conn);
            throw new RuntimeException("Failed to create tournament. Response: " + error);
        }

        String response = readResponse(conn);
        JSONObject jsonResponse = new JSONObject(response);
        JSONObject tournamentObj = jsonResponse.getJSONObject("tournament");
        
        // Extract both ID and URL from response
        Object idObj = tournamentObj.get("id");
        String urlSlug = tournamentObj.getString("url");
        
        return new TournamentCreationResult(idObj.toString(), urlSlug);
    }
    
    // Helper class to return both ID and URL slug
    private static class TournamentCreationResult {
        final String id;
        final String urlSlug;
        
        TournamentCreationResult(String id, String urlSlug) {
            this.id = id;
            this.urlSlug = urlSlug;
        }
    }

    private void addParticipantsToChallonge(String challongeId, List<TournamentParticipants> participants) throws Exception {
        CrudPlayer crudPlayer = new CrudPlayer();
        CrudTournamentParticipant crudParticipant = new CrudTournamentParticipant();
        
        for (TournamentParticipants participant : participants) {
            // Get player name
            String playerName = crudPlayer.getEntities().stream()
                    .filter(p -> p.getId() != null && p.getId().equals(participant.getParticipantId()))
                    .findFirst()
                    .map(p -> p.getNickname() != null && !p.getNickname().isEmpty() 
                            ? p.getNickname() 
                            : p.getUsername())
                    .orElse("Player " + participant.getSeed());

            // Add participant to Challonge and get their Challonge ID
            String challongeParticipantId = addParticipantToChallonge(challongeId, playerName);
            
            // Save Challonge participant ID back to local database
            if (challongeParticipantId != null) {
                participant.setChallongeParticipantId(challongeParticipantId);
                crudParticipant.updateChallongeParticipantId(participant.getId(), challongeParticipantId);
            }
        }
    }

    private String addParticipantToChallonge(String challongeId, String participantName) throws Exception {
        URL url = new URL(baseUrl + "/tournaments/" + challongeId + "/participants.json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", authHeader);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // Build JSON request body (no seed - let Challonge auto-assign)
        JSONObject participant = new JSONObject();
        participant.put("name", participantName);

        JSONObject requestBody = new JSONObject();
        requestBody.put("participant", participant);

        // Send request
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            String error = readErrorResponse(conn);
            throw new RuntimeException("Failed to add participant: " + participantName + ". Response: " + error);
        }

        // Parse response to get Challonge participant ID
        String response = readResponse(conn);
        JSONObject jsonResponse = new JSONObject(response);
        Object idObj = jsonResponse.getJSONObject("participant").get("id");
        String challongeParticipantId = idObj.toString();
        
        System.out.println("Added participant to Challonge: " + participantName + " (ID: " + challongeParticipantId + ")");
        return challongeParticipantId;
    }

    private String mapTournamentType(String internalType) {
        if (internalType == null) return "single elimination";
        
        switch (internalType.toUpperCase()) {
            case "SINGLE_ELIM":
            case "SINGLE ELIMINATION":
                return "single elimination";
            case "DOUBLE_ELIM":
            case "DOUBLE ELIMINATION":
                return "double elimination";
            default:
                return "single elimination";
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    private String readErrorResponse(HttpURLConnection conn) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } catch (Exception e) {
            // If error stream is not available, return empty string
            return "";
        }
    }

    // ─── Response Class ──────────────────────────────────────────────────────

    public static class ChallongeResponse {
        private final String challongeId;
        private final String urlSlug;
        private final String publicUrl;
        private final String embedUrl;

        public ChallongeResponse(String challongeId, String urlSlug, String publicUrl, String embedUrl) {
            this.challongeId = challongeId;
            this.urlSlug = urlSlug;
            this.publicUrl = publicUrl;
            this.embedUrl = embedUrl;
        }

        public String getChallongeId() { return challongeId; }
        public String getUrlSlug() { return urlSlug; }
        public String getPublicUrl() { return publicUrl; }
        public String getEmbedUrl() { return embedUrl; }
    }
    
    // ─── Match Data Class ────────────────────────────────────────────────────
    
    public static class ChallongeMatch {
        private String id;
        private int round;
        private String player1Id;
        private String player2Id;
        private String winnerId;
        private int player1Score;
        private int player2Score;
        private boolean completed;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public int getRound() { return round; }
        public void setRound(int round) { this.round = round; }
        
        public String getPlayer1Id() { return player1Id; }
        public void setPlayer1Id(String player1Id) { this.player1Id = player1Id; }
        
        public String getPlayer2Id() { return player2Id; }
        public void setPlayer2Id(String player2Id) { this.player2Id = player2Id; }
        
        public String getWinnerId() { return winnerId; }
        public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
        
        public int getPlayer1Score() { return player1Score; }
        public void setPlayer1Score(int player1Score) { this.player1Score = player1Score; }
        
        public int getPlayer2Score() { return player2Score; }
        public void setPlayer2Score(int player2Score) { this.player2Score = player2Score; }
        
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}
