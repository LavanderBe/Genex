package Genex.Controllers.Tournament;

import Genex.entities.*;
import Genex.services.*;
import Genex.utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.util.Duration;

import java.awt.Desktop;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TournamentDetailController {

    @FXML
    private Button btnBack;

    @FXML
    private Button btnJoinLeave;
    
    @FXML
    private Button btnStart;
    
    @FXML
    private Button btnSync;
    
    @FXML
    private Button btnReset;
    
    @FXML
    private Hyperlink linkChallonge;

    @FXML
    private Text txtTournamentName;

    @FXML
    private Text txtGameName;

    @FXML
    private StackPane statusBadge;

    @FXML
    private Text txtStatus;

    @FXML
    private Text txtPrize;

    @FXML
    private Text txtFormat;

    @FXML
    private Text txtDates;

    @FXML
    private Text txtCenter;
    
    @FXML
    private VBox bracketSection;
    
    @FXML
    private WebView bracketWebView;

    @FXML
    private VBox participantsSection;

    @FXML
    private Text txtParticipantCount;

    @FXML
    private VBox participantsList;

    @FXML
    private VBox emptyParticipants;
    
    @FXML
    private VBox matchesSection;
    
    @FXML
    private Button btnRefreshMatches;
    
    @FXML
    private VBox matchesList;
    
    @FXML
    private VBox emptyMatches;

    // Player status section
    @FXML private VBox playerStatusSection;
    @FXML private HBox statusCard;
    @FXML private Text txtStatusIcon;
    @FXML private Text txtPlayerStatus;
    @FXML private Text txtStatusDetail;

    // Rankings section
    @FXML private VBox rankingsSection;
    @FXML private VBox rankingsList;

    private Tounament tournament;
    private User currentUser;
    private boolean isPlayerJoined = false;
    private StackPane rootStackPane;

    private CrudGame crudGame = new CrudGame();
    private CrudCenter crudCenter = new CrudCenter();
    private CrudTournamentParticipant crudParticipant = new CrudTournamentParticipant();
    private CrudPlayer crudPlayer = new CrudPlayer();
    private CrudTeam crudTeam = new CrudTeam();
    private CrudTournament crudTournament = new CrudTournament();
    private CrudTournamentMatch crudMatch = new CrudTournamentMatch();
    private ChallongeService challongeService = new ChallongeService();

    @FXML
    public void initialize() {
        System.out.println("TournamentDetailController initialized");
        
        // Get current user
        currentUser = SessionManager.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            System.err.println("No user logged in!");
            return;
        }

        System.out.println("Current user role: " + currentUser.getRole());
    }

    public void setTournament(Tounament tournament) {
        this.tournament = tournament;
        loadTournamentDetails();
        setupRoleBasedUI();
    }

    public void setRootStackPane(StackPane rootStackPane) {
        this.rootStackPane = rootStackPane;
    }

    private void loadTournamentDetails() {
        if (tournament == null) return;

        // Set tournament name
        txtTournamentName.setText(tournament.getTournamentName());

        // Set game name
        if (tournament.getGame_id() != null) {
            try {
                Game game = crudGame.getgames().stream()
                        .filter(g -> g.getId().equals(tournament.getGame_id()))
                        .findFirst()
                        .orElse(null);
                
                if (game != null) {
                    txtGameName.setText(game.getNom());
                } else {
                    txtGameName.setText("Jeu inconnu");
                }
            } catch (Exception e) {
                txtGameName.setText("Jeu inconnu");
                e.printStackTrace();
            }
        }

        // Set format with participant type
        String formatText = tournament.getFormat();
        if (tournament.getParticipant_type() != null) {
            formatText += " • " + tournament.getParticipant_type();
        }
        txtFormat.setText(formatText);

        // Set prize pool
        txtPrize.setText(String.format("%.0f €", tournament.getPrize_pool()));

        // Set dates
        if (tournament.getStarts_at() != null && tournament.getEnds_at() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            String startDate = tournament.getStarts_at().format(DateTimeFormatter.ofPattern("dd MMM"));
            String endDate = tournament.getEnds_at().format(formatter);
            txtDates.setText(startDate + " - " + endDate);
        }

        // Set center
        if (tournament.getCenter_id() != null) {
            try {
                Center center = crudCenter.getAll().stream()
                        .filter(c -> c.getCenterId().equals(tournament.getCenter_id()))
                        .findFirst()
                        .orElse(null);
                
                if (center != null) {
                    txtCenter.setText(center.getName() + " - " + center.getCity());
                } else {
                    txtCenter.setText("Centre inconnu");
                }
            } catch (Exception e) {
                txtCenter.setText("Centre inconnu");
                e.printStackTrace();
            }
        }

        // Set state badge
        updateStateBadge();
    }

    private void updateStateBadge() {
        if (tournament == null || tournament.getState() == null) {
            txtStatus.setText("INCONNU");
            return;
        }

        try {
            Tounament.TournamentState state = Tounament.TournamentState.valueOf(tournament.getState());
            txtStatus.setText(state.getDisplayName().toUpperCase());
            
            // Remove all state classes first
            statusBadge.getStyleClass().removeAll("state-registration-open", "state-registration-closed", 
                    "state-in-progress", "state-completed", "state-cancelled");
            
            // Add appropriate state class
            switch (state) {
                case REGISTRATION_OPEN:
                    statusBadge.getStyleClass().add("state-registration-open");
                    break;
                case REGISTRATION_CLOSED:
                    statusBadge.getStyleClass().add("state-registration-closed");
                    break;
                case IN_PROGRESS:
                    statusBadge.getStyleClass().add("state-in-progress");
                    break;
                case COMPLETED:
                    statusBadge.getStyleClass().add("state-completed");
                    break;
                case CANCELLED:
                    statusBadge.getStyleClass().add("state-cancelled");
                    break;
            }
        } catch (IllegalArgumentException e) {
            txtStatus.setText("INCONNU");
            System.err.println("Invalid tournament state: " + tournament.getState());
        }
    }

    private void setupRoleBasedUI() {
        if (currentUser == null || tournament == null) return;

        String role = currentUser.getRole();

        if ("player".equalsIgnoreCase(role)) {
            btnJoinLeave.setVisible(true);
            btnJoinLeave.setManaged(true);
            participantsSection.setVisible(false);
            participantsSection.setManaged(false);
            btnSync.setVisible(false);
            btnSync.setManaged(false);
            btnStart.setVisible(false);
            btnStart.setManaged(false);
            btnReset.setVisible(false);
            btnReset.setManaged(false);

            checkPlayerJoinStatus();

            if (tournament.isSynced()) {
                showBracket();
            } else {
                hideBracket();
            }

            // Show player status if tournament started
            String playerId = getPlayerIdFromUserId(currentUser.getId());
            if (playerId != null) {
                if (tournament.isStarted()) {
                    loadPlayerStatus(playerId);
                } else {
                    playerStatusSection.setVisible(false);
                    playerStatusSection.setManaged(false);
                }
            }

            // Show rankings if tournament completed
            if (Tounament.TournamentState.COMPLETED.name().equals(tournament.getState())) {
                loadRankings();
            } else {
                rankingsSection.setVisible(false);
                rankingsSection.setManaged(false);
            }

        } else {
            // Admin view: Hide join button, show participants section
            btnJoinLeave.setVisible(false);
            btnJoinLeave.setManaged(false);
            participantsSection.setVisible(true);
            participantsSection.setManaged(true);

            // Show/hide sync and start buttons based on tournament state
            if (!tournament.isSynced()) {
                // Not synced yet: show sync button, hide start button and bracket
                btnSync.setVisible(true);
                btnSync.setManaged(true);
                btnStart.setVisible(false);
                btnStart.setManaged(false);
                btnReset.setVisible(false);
                btnReset.setManaged(false);
                hideBracket();
                hideMatches();
            } else if (!tournament.isStarted()) {
                // Synced but not started: hide sync button, show start button and bracket
                btnSync.setVisible(false);
                btnSync.setManaged(false);
                btnStart.setVisible(true);
                btnStart.setManaged(true);
                btnReset.setVisible(true);
                btnReset.setManaged(true);
                showBracket();
                hideMatches();
            } else {
                // Started: hide both buttons, show bracket and matches
                btnSync.setVisible(false);
                btnSync.setManaged(false);
                btnStart.setVisible(false);
                btnStart.setManaged(false);
                btnReset.setVisible(true);
                btnReset.setManaged(true);
                showBracket();
                showMatches();
            }

            // Show rankings if completed
            if (Tounament.TournamentState.COMPLETED.name().equals(tournament.getState())) {
                loadRankings();
            } else {
                rankingsSection.setVisible(false);
                rankingsSection.setManaged(false);
            }

            // Load participants
            loadParticipants();
        }
        
        // Setup Challonge link
        if (tournament.isSynced() && tournament.getChallongeUrl() != null) {
            linkChallonge.setVisible(true);
            linkChallonge.setManaged(true);
        } else {
            linkChallonge.setVisible(false);
            linkChallonge.setManaged(false);
        }
    }

    private void checkPlayerJoinStatus() {
        try {
            String playerId = getPlayerIdFromUserId(currentUser.getId());
            
            if (playerId == null) {
                System.err.println("Player ID not found for user: " + currentUser.getUsername());
                isPlayerJoined = false;
                updateJoinButton();
                return;
            }

            // Check participation based on tournament type
            TournamentParticipants participation = null;
            
            if ("TEAM".equalsIgnoreCase(tournament.getParticipant_type())) {
                // For team tournaments, check if player's team is participating
                CrudTeamMember crudTeamMember = new CrudTeamMember();
                Team playerTeam = crudTeamMember.getTeamByPlayer(playerId);
                
                if (playerTeam != null) {
                    // Check if the team is participating
                    participation = crudParticipant.getPlayerParticipation(
                            tournament.getTournamentId(), playerTeam.getId());
                }
            } else {
                // For solo tournaments, check player participation
                participation = crudParticipant.getPlayerParticipation(
                        tournament.getTournamentId(), playerId);
            }

            if (participation != null && participation.isActive()) {
                isPlayerJoined = true;
            } else {
                isPlayerJoined = false;
            }

            updateJoinButton();
        } catch (Exception e) {
            System.err.println("Error checking player join status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateJoinButton() {
        if (currentUser == null || tournament == null) return;
        String playerId = getPlayerIdFromUserId(currentUser.getId());
        if (playerId == null) return;

        TournamentParticipants participation = null;
        
        // Check participation based on tournament type
        if ("TEAM".equalsIgnoreCase(tournament.getParticipant_type())) {
            // For team tournaments, check if player's team is participating
            CrudTeamMember crudTeamMember = new CrudTeamMember();
            Team playerTeam = crudTeamMember.getTeamByPlayer(playerId);
            
            if (playerTeam != null) {
                participation = crudParticipant.getPlayerParticipation(
                        tournament.getTournamentId(), playerTeam.getId());
            }
        } else {
            // For solo tournaments, check player participation
            participation = crudParticipant.getPlayerParticipation(
                    tournament.getTournamentId(), playerId);
        }

        if (participation == null) {
            // Never joined - only allow joining if registration is open
            if (Tounament.TournamentState.REGISTRATION_OPEN.name().equals(tournament.getState())) {
                btnJoinLeave.setText("REJOINDRE");
                btnJoinLeave.setDisable(false);
                btnJoinLeave.getStyleClass().removeAll("joined", "eliminated");
            } else {
                btnJoinLeave.setText("INSCRIPTIONS FERMÉES");
                btnJoinLeave.setDisable(true);
                btnJoinLeave.getStyleClass().removeAll("joined");
                if (!btnJoinLeave.getStyleClass().contains("eliminated")) {
                    btnJoinLeave.getStyleClass().add("eliminated");
                }
            }
            return;
        }

        if (participation.isEliminated()) {
            // Withdrew or lost - show disabled button
            if (participation.withdrewFromTournament()) {
                btnJoinLeave.setText("RETIRÉ");
            } else {
                btnJoinLeave.setText("ÉLIMINÉ");
            }
            btnJoinLeave.setDisable(true);
            btnJoinLeave.getStyleClass().removeAll("joined");
            if (!btnJoinLeave.getStyleClass().contains("eliminated")) {
                btnJoinLeave.getStyleClass().add("eliminated");
            }
        } else if (participation.isWinner()) {
            btnJoinLeave.setText("🏆 VAINQUEUR");
            btnJoinLeave.setDisable(true);
            btnJoinLeave.getStyleClass().removeAll("joined", "eliminated");
        } else {
            // ACTIVE - still competing
            btnJoinLeave.setText(tournament.isStarted() ? "SE RETIRER" : "QUITTER");
            btnJoinLeave.setDisable(false);
            btnJoinLeave.getStyleClass().removeAll("eliminated");
            if (!btnJoinLeave.getStyleClass().contains("joined")) {
                btnJoinLeave.getStyleClass().add("joined");
            }
        }
    }

    @FXML
    private void handleJoinLeave() {
        if (currentUser == null || tournament == null) return;

        try {
            String playerId = getPlayerIdFromUserId(currentUser.getId());
            if (playerId == null) {
                showAlert("Erreur", "Profil joueur introuvable.", Alert.AlertType.ERROR);
                return;
            }

            if (isPlayerJoined) {
                // Check if tournament has started
                if (tournament.isStarted()) {
                    // Tournament in progress → WITHDRAW flow
                    handleWithdraw(playerId);
                } else {
                    // Tournament not started → normal leave
                    // Determine participant ID based on tournament type
                    String participantId = playerId;
                    String successMessage = "Vous avez quitté le tournoi.";
                    
                    if ("TEAM".equalsIgnoreCase(tournament.getParticipant_type())) {
                        // For team tournaments, get team ID
                        CrudTeamMember crudTeamMember = new CrudTeamMember();
                        Team playerTeam = crudTeamMember.getTeamByPlayer(playerId);
                        
                        if (playerTeam != null) {
                            // Check if player is team owner
                            if (!playerId.equals(playerTeam.getCreatedBy())) {
                                showAlert("Propriétaire requis", 
                                    "Seul le propriétaire de l'équipe peut retirer l'équipe du tournoi.", 
                                    Alert.AlertType.WARNING);
                                return;
                            }
                            participantId = playerTeam.getId();
                            successMessage = "L'équipe \"" + playerTeam.getName() + "\" a quitté le tournoi.";
                        }
                    }
                    
                    crudParticipant.removePlayerFromTournament(tournament.getTournamentId(), participantId);
                    isPlayerJoined = false;
                    // Auto-reopen registration if space available
                    checkAndUpdateTournamentState();
                    showAlert("Succès", successMessage, Alert.AlertType.INFORMATION);
                    updateJoinButton();
                }
            } else {
                // Check if player was previously eliminated or withdrew
                TournamentParticipants existing = crudParticipant.getPlayerParticipation(
                        tournament.getTournamentId(), playerId);
                // Only block if they have an ELIMINATED record (withdrew or lost)
                // If no record exists (deleted manually or never joined), allow joining
                if (existing != null && existing.isEliminated()) {
                    showAlert("Impossible", "Vous ne pouvez pas rejoindre ce tournoi car vous avez été éliminé ou retiré.", Alert.AlertType.WARNING);
                    return;
                }

                // Check tournament type and handle accordingly
                if ("TEAM".equalsIgnoreCase(tournament.getParticipant_type())) {
                    // TEAM TOURNAMENT - Check team ownership
                    handleTeamTournamentJoin(playerId);
                } else {
                    // SOLO TOURNAMENT - Join as individual player
                    handleSoloTournamentJoin(playerId);
                }
            }

        } catch (Exception e) {
            System.err.println("Error joining/leaving tournament: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue.", Alert.AlertType.ERROR);
        }
    }

    private void handleSoloTournamentJoin(String playerId) {
        try {
            int currentCount = crudParticipant.getParticipantCount(tournament.getTournamentId());
            TournamentParticipants participant = TournamentParticipants.solo(
                    tournament.getTournamentId(), playerId, currentCount + 1);
            crudParticipant.addEntity(participant);
            isPlayerJoined = true;
            // Auto-close if now full
            checkAndUpdateTournamentState();
            showAlert("Succès", "Vous avez rejoint le tournoi!", Alert.AlertType.INFORMATION);
            updateJoinButton();
        } catch (Exception e) {
            System.err.println("Error joining solo tournament: " + e.getMessage());
            throw e;
        }
    }

    private void handleTeamTournamentJoin(String playerId) {
        try {
            // Check if player has a team
            CrudTeamMember crudTeamMember = new CrudTeamMember();
            Team playerTeam = crudTeamMember.getTeamByPlayer(playerId);

            if (playerTeam == null) {
                // Case 3: Player has no team
                showAlert("Équipe requise", 
                    "Vous devez rejoindre ou créer une équipe avant de participer à un tournoi par équipe.", 
                    Alert.AlertType.WARNING);
                return;
            }

            // Check if player is the team owner
            if (!playerId.equals(playerTeam.getCreatedBy())) {
                // Case 2: Player has team but is not the owner
                showAlert("Propriétaire requis", 
                    "Vous ne pouvez pas participer car vous n'êtes pas le propriétaire de l'équipe \"" + playerTeam.getName() + "\".\n\n" +
                    "Seul le créateur de l'équipe peut inscrire l'équipe au tournoi.", 
                    Alert.AlertType.WARNING);
                return;
            }

            // Case 1: Player is the team owner - allow joining with team
            int currentCount = crudParticipant.getParticipantCount(tournament.getTournamentId());
            TournamentParticipants participant = new TournamentParticipants();
            participant.setTournamentId(tournament.getTournamentId());
            participant.setParticipantId(playerTeam.getId()); // Use TEAM ID, not player ID
            participant.setTournamentType("TEAM");
            participant.setSeed(currentCount + 1);
            participant.setStatus(TournamentParticipants.Status.ACTIVE);

            crudParticipant.addEntity(participant);
            isPlayerJoined = true;
            
            // Auto-close if now full
            checkAndUpdateTournamentState();
            
            showAlert("Succès", 
                "L'équipe \"" + playerTeam.getName() + "\" a rejoint le tournoi!\n\n" +
                "Tous les membres de l'équipe sont maintenant inscrits.", 
                Alert.AlertType.INFORMATION);
            updateJoinButton();

        } catch (Exception e) {
            System.err.println("Error joining team tournament: " + e.getMessage());
            throw e;
        }
    }

    private void handleWithdraw(String playerId) {
        // Determine if this is a team or solo tournament
        boolean isTeamTournament = "TEAM".equalsIgnoreCase(tournament.getParticipant_type());
        String participantId = playerId;
        String participantName = "Vous";
        
        if (isTeamTournament) {
            // Get player's team
            CrudTeamMember crudTeamMember = new CrudTeamMember();
            Team playerTeam = crudTeamMember.getTeamByPlayer(playerId);
            
            if (playerTeam == null) {
                showAlert("Erreur", "Équipe introuvable.", Alert.AlertType.ERROR);
                return;
            }
            
            // Check if player is team owner
            if (!playerId.equals(playerTeam.getCreatedBy())) {
                showAlert("Propriétaire requis", 
                    "Seul le propriétaire de l'équipe peut retirer l'équipe du tournoi.", 
                    Alert.AlertType.WARNING);
                return;
            }
            
            participantId = playerTeam.getId();
            participantName = "L'équipe \"" + playerTeam.getName() + "\"";
        }
        
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer le retrait");
        confirm.setHeaderText("Quitter le tournoi en cours");
        
        String withdrawMessage = isTeamTournament 
            ? "⚠️ Le tournoi est en cours!\n\n" +
              "Si vous retirez l'équipe maintenant:\n" +
              "• Toute l'équipe sera marquée comme RETIRÉE\n" +
              "• L'adversaire actuel gagne par forfait (3-0)\n" +
              "• L'équipe ne pourra pas rejoindre ce tournoi\n\n" +
              "Voulez-vous vraiment retirer l'équipe?"
            : "⚠️ Le tournoi est en cours!\n\n" +
              "Si vous quittez maintenant:\n" +
              "• Vous serez marqué comme RETIRÉ\n" +
              "• Votre adversaire actuel gagne par forfait (3-0)\n" +
              "• Vous ne pourrez pas rejoindre ce tournoi\n\n" +
              "Voulez-vous vraiment vous retirer?";
        
        confirm.setContentText(withdrawMessage);

        final String finalParticipantId = participantId;
        final String finalParticipantName = participantName;
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Get the participant's Challonge participant ID
                    TournamentParticipants participation = crudParticipant.getPlayerParticipation(
                            tournament.getTournamentId(), finalParticipantId);
                    String challongeParticipantId = participation != null 
                            ? participation.getChallongeParticipantId() : null;

                    System.out.println("Withdrawing participant: " + finalParticipantId + " (Challonge ID: " + challongeParticipantId + ")");

                    // Find ALL pending matches involving this participant
                    List<TournamentMatch> allMatches = crudMatch.getAllByTournament(tournament.getTournamentId());
                    int withdrawRound = 0;
                    int forfeitsReported = 0;

                    for (TournamentMatch match : allMatches) {
                        if (match.getStatus() != TournamentMatch.MatchStatus.PENDING) continue;

                        // Check by local participant ID
                        boolean isPlayer1 = finalParticipantId.equals(match.getPlayer1Id());
                        boolean isPlayer2 = finalParticipantId.equals(match.getPlayer2Id());

                        // Also check by Challonge participant ID (for TBD matches)
                        if (!isPlayer1 && !isPlayer2 && challongeParticipantId != null) {
                            isPlayer1 = challongeParticipantId.equals(match.getChallongePlayer1Id());
                            isPlayer2 = challongeParticipantId.equals(match.getChallongePlayer2Id());
                        }

                        if (!isPlayer1 && !isPlayer2) continue;

                        System.out.println("Found pending match for withdrawn participant: Round " + match.getRound() + " Match " + match.getMatchNumber());

                        // Determine opponent IDs
                        String opponentLocalId = isPlayer1 ? match.getPlayer2Id() : match.getPlayer1Id();
                        String challongeWinnerId = isPlayer1
                                ? match.getChallongePlayer2Id()
                                : match.getChallongePlayer1Id();

                        // Forfeit scores: withdrawn participant gets 0, opponent gets 3
                        int p1Score = isPlayer1 ? 0 : 3;
                        int p2Score = isPlayer1 ? 3 : 0;

                        // Update local DB - mark as COMPLETED with forfeit score
                        crudMatch.updateMatchResult(match.getId(), p1Score, p2Score, opponentLocalId);
                        System.out.println("  Forfeit set: " + p1Score + "-" + p2Score + ", winner: " + opponentLocalId);

                        // Update Challonge
                        if (tournament.getChallongeUrlSlug() != null
                                && match.getChallongeMatchId() != null
                                && challongeWinnerId != null) {
                            try {
                                challongeService.updateMatchResult(
                                        tournament.getChallongeUrlSlug(),
                                        match.getChallongeMatchId(),
                                        p1Score, p2Score,
                                        challongeWinnerId);
                                System.out.println("  Challonge updated for match " + match.getChallongeMatchId());
                            } catch (Exception e) {
                                System.err.println("  Warning: Challonge forfeit failed: " + e.getMessage());
                            }
                        }

                        if (withdrawRound == 0) withdrawRound = match.getRound();
                        forfeitsReported++;
                    }

                    System.out.println("Total forfeits reported: " + forfeitsReported);

                    // Mark participant as WITHDREW in participants table
                    crudParticipant.withdrawPlayer(tournament.getTournamentId(), finalParticipantId, withdrawRound);

                    isPlayerJoined = false;

                    // Refresh matches from Challonge to get updated next rounds
                    refreshMatchesFromChallonge();

                    // Update UI
                    updateJoinButton();
                    loadPlayerStatus(playerId);
                    loadMatches();
                    showBracket();

                    showAlert("Retrait confirmé",
                            finalParticipantName + " a été retiré du tournoi.\n" +
                            forfeitsReported + " match(s) résolu(s) par forfait (3-0).",
                            Alert.AlertType.INFORMATION);

                } catch (Exception e) {
                    System.err.println("Error withdrawing: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Erreur", "Échec du retrait: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    private String getPlayerIdFromUserId(String userId) {
        // The players table uses user_id as the primary key
        // So the user_id from the session IS the player_id we need
        // Just verify the player exists in the players table
        try {
            String query = "SELECT user_id FROM players WHERE user_id = ?";
            java.sql.PreparedStatement pst = Genex.utils.Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, userId);
            java.sql.ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getString("user_id");
            }
            
            System.err.println("No player record found for user_id: " + userId);
            return null;
        } catch (Exception e) {
            System.err.println("Error verifying player: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void loadParticipants() {
        if (tournament == null) return;

        try {
            List<TournamentParticipants> participants = crudParticipant.getAll(tournament.getTournamentId());
            
            // Update count
            txtParticipantCount.setText(String.valueOf(participants.size()));

            // Clear list
            participantsList.getChildren().clear();

            if (participants.isEmpty()) {
                emptyParticipants.setVisible(true);
                emptyParticipants.setManaged(true);
            } else {
                emptyParticipants.setVisible(false);
                emptyParticipants.setManaged(false);

                // Add participant cards
                for (TournamentParticipants participant : participants) {
                    HBox card = createParticipantCard(participant);
                    participantsList.getChildren().add(card);
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading participants: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private HBox createParticipantCard(TournamentParticipants participant) {
        HBox card = new HBox();
        card.getStyleClass().add("participant-card");
        card.setSpacing(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Determine if this is a team or solo tournament
        boolean isTeamTournament = "TEAM".equalsIgnoreCase(participant.getTournamentType()) 
                                    || "TEAM".equalsIgnoreCase(tournament.getParticipant_type());

        // Icon - different for team vs solo
        Text icon = new Text(isTeamTournament ? "👥" : "👤");
        icon.getStyleClass().add("participant-icon");

        // Get participant name (team or player)
        String participantName = "Joueur inconnu";
        try {
            if (isTeamTournament) {
                // Get team name
                Team team = crudTeam.getEntity(participant.getParticipantId());
                if (team != null) {
                    participantName = team.getName();
                }
            } else {
                // Get player name
                List<Player> players = crudPlayer.getEntities();
                Player player = players.stream()
                        .filter(p -> p.getId() != null && p.getId().equals(participant.getParticipantId()))
                        .findFirst()
                        .orElse(null);
                
                if (player != null) {
                    participantName = player.getNickname() != null && !player.getNickname().isEmpty() 
                            ? player.getNickname() 
                            : player.getUsername();
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting participant name: " + e.getMessage());
            e.printStackTrace();
        }

        // Name
        Text name = new Text(participantName);
        name.getStyleClass().add("participant-name");

        // Seed
        Text seed = new Text("Seed #" + participant.getSeed());
        seed.getStyleClass().add("participant-seed");

        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        card.getChildren().addAll(icon, name, spacer, seed);

        return card;
    }

    @FXML
    private void handleBack() {
        try {
            if (rootStackPane == null) {
                System.err.println("Root stack pane not set");
                return;
            }

            // Navigate back to tournament hub
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tournament/TournamentHub.fxml"));
            Node tournamentHub = loader.load();

            // Replace content in rootStackPane with fade transition
            rootStackPane.getChildren().clear();
            rootStackPane.getChildren().add(tournamentHub);
            
            // Apply fade transition
            FadeTransition ft = new FadeTransition(Duration.millis(300), tournamentHub);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

        } catch (Exception e) {
            System.err.println("Error navigating back to tournament hub");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSync() {
        if (tournament == null || currentUser == null) return;
        
        // Check if admin
        if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
            showAlert("Erreur", "Seuls les administrateurs peuvent synchroniser les tournois.", Alert.AlertType.ERROR);
            return;
        }
        
        // Check if already synced
        if (tournament.isSynced()) {
            showAlert("Information", "Ce tournoi est déjà synchronisé avec Challonge.", Alert.AlertType.INFORMATION);
            return;
        }
        
        try {
            // Get participants
            List<TournamentParticipants> participants = crudParticipant.getAll(tournament.getTournamentId());
            
            if (participants.isEmpty()) {
                showAlert("Erreur", "Impossible de synchroniser un tournoi sans participants.", Alert.AlertType.ERROR);
                return;
            }
            
            // CHECK: Participants must equal max_players to generate bracket
            int maxPlayers = tournament.getMaxPlayers();
            if (participants.size() != maxPlayers) {
                showAlert("Erreur", 
                    "Le tournoi doit avoir exactement " + maxPlayers + " participants pour générer le bracket.\n" +
                    "Participants actuels: " + participants.size() + "/" + maxPlayers, 
                    Alert.AlertType.ERROR);
                return;
            }
            
            // Create tournament on Challonge
            ChallongeService.ChallongeResponse response = challongeService.createTournament(
                    tournament.getTournamentName(),
                    tournament.getFormat(),
                    participants
            );
            
            // Update tournament with Challonge data
            tournament.setChallongeId(response.getChallongeId());
            tournament.setChallongeUrl(response.getPublicUrl());
            tournament.setChallongeUrlSlug(response.getUrlSlug());
            tournament.setSynced(true);
            
            // Save to database
            crudTournament.updateEntity(tournament, tournament.getTournamentId());
            
            // Update UI
            setupRoleBasedUI();
            
            showAlert("Succès", "Tournoi synchronisé avec Challonge!\nLes brackets sont maintenant visibles.", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            System.err.println("Error syncing tournament to Challonge: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Échec de la synchronisation avec Challonge: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleStart() {
        if (tournament == null || currentUser == null) return;
        
        // Check if admin
        if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
            showAlert("Erreur", "Seuls les administrateurs peuvent démarrer les tournois.", Alert.AlertType.ERROR);
            return;
        }
        
        // Check if synced
        if (!tournament.isSynced()) {
            showAlert("Erreur", "Le tournoi doit être synchronisé avant de pouvoir être démarré.", Alert.AlertType.ERROR);
            return;
        }
        
        // Check if already started
        if (tournament.isStarted()) {
            showAlert("Information", "Ce tournoi est déjà démarré.", Alert.AlertType.INFORMATION);
            return;
        }
        
        try {
            // Start tournament on Challonge using URL slug
            String urlSlug = tournament.getChallongeUrlSlug();
            if (urlSlug == null || urlSlug.isEmpty()) {
                // Fallback: extract from URL if slug not stored
                String url = tournament.getChallongeUrl();
                if (url != null && url.contains("challonge.com/")) {
                    urlSlug = url.substring(url.lastIndexOf("/") + 1);
                } else {
                    showAlert("Erreur", "URL Challonge invalide.", Alert.AlertType.ERROR);
                    return;
                }
            }
            
            challongeService.startTournament(urlSlug);
            
            // Update tournament status
            tournament.setStarted(true);
            tournament.setState(Tounament.TournamentState.IN_PROGRESS.name());
            
            // Save to database
            crudTournament.updateEntity(tournament, tournament.getTournamentId());
            
            // Sync matches from Challonge to local database
            syncMatchesFromChallonge();
            
            // Update UI
            updateStateBadge();
            setupRoleBasedUI();
            
            showAlert("Succès", "Tournoi démarré! Les brackets sont maintenant en direct.", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            System.err.println("Error starting tournament on Challonge: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Échec du démarrage du tournoi: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleReset() {
        if (tournament == null || currentUser == null) return;
        
        // Check if admin
        if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
            showAlert("Erreur", "Seuls les administrateurs peuvent réinitialiser les tournois.", Alert.AlertType.ERROR);
            return;
        }
        
        // Confirm reset
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmer la réinitialisation");
        confirmAlert.setHeaderText("Réinitialiser le tournoi");
        confirmAlert.setContentText("Cela va:\n" +
                "- Remettre le tournoi à l'état 'Inscription Fermée'\n" +
                "- Supprimer tous les matches locaux\n" +
                "- Réinitialiser les flags de sync/start\n" +
                "- Le bracket Challonge restera sur le site\n\n" +
                "Pour supprimer complètement le bracket Challonge,\n" +
                "allez sur challonge.com et supprimez-le manuellement.\n\n" +
                "Continuer?");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    // Delete all matches
                    List<TournamentMatch> matches = crudMatch.getAllByTournament(tournament.getTournamentId());
                    for (TournamentMatch match : matches) {
                        crudMatch.deleteEntity(match);
                    }
                    System.out.println("Deleted " + matches.size() + " matches");

                    // DELETE ALL PARTICIPANTS (so they can rejoin)
                    List<TournamentParticipants> participants = crudParticipant.getAll(tournament.getTournamentId());
                    for (TournamentParticipants p : participants) {
                        crudParticipant.deleteEntity(p);
                        System.out.println("Removed participant: " + p.getParticipantId());
                    }
                    System.out.println("Removed all " + participants.size() + " participants");

                    // Reset tournament state completely
                    tournament.setStarted(false);
                    tournament.setSynced(false);
                    tournament.setChallongeId(null);
                    tournament.setChallongeUrl(null);
                    tournament.setChallongeUrlSlug(null);
                    tournament.setState(Tounament.TournamentState.REGISTRATION_OPEN.name());

                    crudTournament.updateEntity(tournament, tournament.getTournamentId());
                    updateStateBadge();
                    setupRoleBasedUI();
                    
                    // Refresh participant list and player status
                    loadParticipants();
                    
                    // Refresh player status card if player view
                    if ("player".equalsIgnoreCase(currentUser.getRole())) {
                        String playerId = getPlayerIdFromUserId(currentUser.getId());
                        if (playerId != null) {
                            loadPlayerStatus(playerId);
                        }
                    }

                    showAlert("Succès", "Tournoi réinitialisé! Les joueurs retirés peuvent rejoindre à nouveau.", Alert.AlertType.INFORMATION);

                } catch (Exception e) {
                    System.err.println("Error resetting tournament: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Erreur", "Échec de la réinitialisation: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }
    
    @FXML
    private void handleChallongeLink() {
        if (tournament == null || tournament.getChallongeUrl() == null) return;
        
        try {
            // Open Challonge URL in default browser
            Desktop.getDesktop().browse(new URI(tournament.getChallongeUrl()));
        } catch (Exception e) {
            System.err.println("Error opening Challonge URL: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le lien Challonge.", Alert.AlertType.ERROR);
        }
    }
    
    private void showBracket() {
        if (tournament == null || !tournament.isSynced()) return;
        
        bracketSection.setVisible(true);
        bracketSection.setManaged(true);
        
        // Extract tournament URL slug from full URL
        String tournamentUrl = tournament.getChallongeUrl();
        if (tournamentUrl != null && tournamentUrl.contains("challonge.com/")) {
            String slug = tournamentUrl.substring(tournamentUrl.lastIndexOf("/") + 1);
            String embedUrl = challongeService.getEmbedUrl(slug);
            
            // Load bracket in WebView
            bracketWebView.getEngine().load(embedUrl);
        }
    }
    
    private void hideBracket() {
        bracketSection.setVisible(false);
        bracketSection.setManaged(false);
    }
    
    private void showMatches() {
        matchesSection.setVisible(true);
        matchesSection.setManaged(true);
        loadMatches();
    }
    
    private void hideMatches() {
        matchesSection.setVisible(false);
        matchesSection.setManaged(false);
    }
    
    @FXML
    private void handleRefreshMatches() {
        // Pull latest match data from Challonge and update local DB
        refreshMatchesFromChallonge();
        // Check if tournament is now complete
        checkAndCompleteTournament(null);
        // Reload UI
        loadMatches();
        updateStateBadge();
    }
    
    private void loadMatches() {
        if (tournament == null) return;

        try {
            List<TournamentMatch> matches = crudMatch.getAllByTournament(tournament.getTournamentId());
            matchesList.getChildren().clear();

            if (matches.isEmpty()) {
                emptyMatches.setVisible(true);
                emptyMatches.setManaged(true);
                return;
            }

            emptyMatches.setVisible(false);
            emptyMatches.setManaged(false);

            // Group by absolute round number
            // Winners: round 1,2,3... Losers: round -1,-2,-3...
            // We group them as: Round 1 (winners R1 + losers R1), Round 2 (winners R2 + losers R2)...
            Map<Integer, List<TournamentMatch>> winnersByRound = new HashMap<>();
            Map<Integer, List<TournamentMatch>> losersByRound = new HashMap<>();

            for (TournamentMatch match : matches) {
                if (match.getRound() > 0) {
                    winnersByRound.computeIfAbsent(match.getRound(), k -> new java.util.ArrayList<>()).add(match);
                } else {
                    // Store losers by absolute round number
                    int absRound = Math.abs(match.getRound());
                    losersByRound.computeIfAbsent(absRound, k -> new java.util.ArrayList<>()).add(match);
                }
            }

            // Get all unique round numbers (union of winners and losers rounds)
            java.util.Set<Integer> allRoundNumbers = new java.util.TreeSet<>();
            allRoundNumbers.addAll(winnersByRound.keySet());
            allRoundNumbers.addAll(losersByRound.keySet());

            for (Integer roundNum : allRoundNumbers) {
                List<TournamentMatch> wMatches = winnersByRound.getOrDefault(roundNum, new java.util.ArrayList<>());
                List<TournamentMatch> lMatches = losersByRound.getOrDefault(roundNum, new java.util.ArrayList<>());

                // Combine all matches for this round number
                List<TournamentMatch> allRoundMatches = new java.util.ArrayList<>();
                allRoundMatches.addAll(wMatches);
                allRoundMatches.addAll(lMatches);

                // Determine if this is the current active round
                boolean isCurrentRound = allRoundMatches.stream()
                        .anyMatch(m -> m.getStatus() == TournamentMatch.MatchStatus.PENDING)
                        && isPreviousRoundDone(roundNum, winnersByRound, losersByRound);

                // Create round section
                VBox roundSection = new VBox();
                roundSection.setSpacing(12);
                roundSection.getStyleClass().add("round-section");
                if (isCurrentRound) roundSection.getStyleClass().add("round-section-active");

                // Round header
                HBox header = new HBox();
                header.setSpacing(12);
                header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Text roundTitle = new Text("Round " + roundNum);
                roundTitle.getStyleClass().add("round-title");
                header.getChildren().add(roundTitle);

                if (isCurrentRound) {
                    StackPane activeBadge = new StackPane();
                    activeBadge.getStyleClass().add("active-round-badge");
                    Text activeText = new Text("● ROUND ACTUEL");
                    activeText.getStyleClass().add("active-round-text");
                    activeBadge.getChildren().add(activeText);
                    header.getChildren().add(activeBadge);
                } else if (allRoundMatches.stream().allMatch(m -> m.getStatus() == TournamentMatch.MatchStatus.COMPLETED)) {
                    StackPane doneBadge = new StackPane();
                    doneBadge.getStyleClass().add("done-round-badge");
                    Text doneText = new Text("✓ TERMINÉ");
                    doneText.getStyleClass().add("done-round-text");
                    doneBadge.getChildren().add(doneText);
                    header.getChildren().add(doneBadge);
                }

                roundSection.getChildren().add(header);

                // Winners matches first
                if (!wMatches.isEmpty()) {
                    if (!lMatches.isEmpty()) {
                        // Only show label if there are both brackets
                        Text wLabel = new Text("▲ Winners");
                        wLabel.getStyleClass().add("sub-bracket-label");
                        roundSection.getChildren().add(wLabel);
                    }
                    for (TournamentMatch m : wMatches) {
                        roundSection.getChildren().add(createMatchCard(m));
                    }
                }

                // Losers matches after
                if (!lMatches.isEmpty()) {
                    if (!wMatches.isEmpty()) {
                        Text lLabel = new Text("▼ Losers");
                        lLabel.getStyleClass().add("sub-bracket-label-losers");
                        roundSection.getChildren().add(lLabel);
                    }
                    for (TournamentMatch m : lMatches) {
                        roundSection.getChildren().add(createMatchCard(m));
                    }
                }

                matchesList.getChildren().add(roundSection);
            }

            // Auto-detect tournament completion when all matches are done
            if (!Tounament.TournamentState.COMPLETED.name().equals(tournament.getState())) {
                boolean allDone = matches.stream()
                        .allMatch(m -> m.getStatus() == TournamentMatch.MatchStatus.COMPLETED);
                if (allDone && !matches.isEmpty()) {
                    checkAndCompleteTournament(null);
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading matches: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isPreviousRoundDone(int roundNum,
                                         Map<Integer, List<TournamentMatch>> winnersByRound,
                                         Map<Integer, List<TournamentMatch>> losersByRound) {
        if (roundNum == 1) return true; // First round is always available
        int prevRound = roundNum - 1;

        List<TournamentMatch> prevWinners = winnersByRound.getOrDefault(prevRound, new java.util.ArrayList<>());
        List<TournamentMatch> prevLosers = losersByRound.getOrDefault(prevRound, new java.util.ArrayList<>());

        List<TournamentMatch> prevAll = new java.util.ArrayList<>();
        prevAll.addAll(prevWinners);
        prevAll.addAll(prevLosers);

        if (prevAll.isEmpty()) return true;
        return prevAll.stream().allMatch(m -> m.getStatus() == TournamentMatch.MatchStatus.COMPLETED);
    }
    
    private VBox createRoundSection(int round, List<TournamentMatch> matches, boolean isCurrentRound) {
        VBox roundSection = new VBox();
        roundSection.setSpacing(12);
        roundSection.getStyleClass().add("round-section");
        if (isCurrentRound) roundSection.getStyleClass().add("round-section-active");
        
        // Round header
        HBox header = new HBox();
        header.setSpacing(12);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        String roundLabel;
        if (round > 0) {
            roundLabel = "Round " + round;
        } else {
            roundLabel = "Losers Round " + Math.abs(round);
        }
        
        Text roundTitle = new Text(roundLabel);
        roundTitle.getStyleClass().add("round-title");
        header.getChildren().add(roundTitle);
        
        // "ROUND ACTUEL" badge
        if (isCurrentRound) {
            StackPane activeBadge = new StackPane();
            activeBadge.getStyleClass().add("active-round-badge");
            Text activeText = new Text("● ROUND ACTUEL");
            activeText.getStyleClass().add("active-round-text");
            activeBadge.getChildren().add(activeText);
            header.getChildren().add(activeBadge);
        }
        
        // Check if all matches in this round are done
        boolean allDone = matches.stream().allMatch(m -> m.getStatus() == TournamentMatch.MatchStatus.COMPLETED);
        if (allDone && !isCurrentRound) {
            StackPane doneBadge = new StackPane();
            doneBadge.getStyleClass().add("done-round-badge");
            Text doneText = new Text("✓ TERMINÉ");
            doneText.getStyleClass().add("done-round-text");
            doneBadge.getChildren().add(doneText);
            header.getChildren().add(doneBadge);
        }
        
        roundSection.getChildren().add(header);
        
        // Match cards
        for (TournamentMatch match : matches) {
            HBox matchCard = createMatchCard(match);
            roundSection.getChildren().add(matchCard);
        }
        
        return roundSection;
    }
    
    private HBox createMatchCard(TournamentMatch match) {
        HBox card = new HBox();
        card.setSpacing(15);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.getStyleClass().add("match-card");

        // Match number
        VBox matchInfo = new VBox();
        matchInfo.setSpacing(5);
        Text matchLabel = new Text("Match " + match.getMatchNumber());
        matchLabel.getStyleClass().add("match-label");
        matchInfo.getChildren().add(matchLabel);

        // Player names
        String player1Name = getPlayerName(match.getPlayer1Id());
        String player2Name = getPlayerName(match.getPlayer2Id());

        boolean isPending = match.getStatus() == TournamentMatch.MatchStatus.PENDING;

        if (isPending) {
            // ── Inline score entry layout ──────────────────────────────
            // [P1 name]  [score spinner]  VS  [score spinner]  [P2 name]  [CONFIRMER]

            // Player 1 name
            Text p1Text = new Text(player1Name);
            p1Text.getStyleClass().add("player-name");

            // Score spinner for player 1
            javafx.scene.control.Spinner<Integer> sp1 = new javafx.scene.control.Spinner<>(0, 99, match.getPlayer1Score());
            sp1.setEditable(true);
            sp1.setPrefWidth(70);
            sp1.getStyleClass().add("score-spinner");

            Text vsText = new Text("VS");
            vsText.getStyleClass().add("vs-text");

            // Score spinner for player 2
            javafx.scene.control.Spinner<Integer> sp2 = new javafx.scene.control.Spinner<>(0, 99, match.getPlayer2Score());
            sp2.setEditable(true);
            sp2.setPrefWidth(70);
            sp2.getStyleClass().add("score-spinner");

            // Player 2 name
            Text p2Text = new Text(player2Name);
            p2Text.getStyleClass().add("player-name");

            // Spacer
            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            // Confirm button
            Button btnConfirm = new Button("✔ CONFIRMER");
            btnConfirm.getStyleClass().add("btn-confirm-inline");
            btnConfirm.setOnAction(e -> {
                int s1 = sp1.getValue();
                int s2 = sp2.getValue();
                if (s1 == s2) {
                    showAlert("Erreur", "Les scores ne peuvent pas être égaux.", Alert.AlertType.ERROR);
                    return;
                }
                String winnerId = s1 > s2 ? match.getPlayer1Id() : match.getPlayer2Id();
                String challongeWinnerId = s1 > s2 ? match.getChallongePlayer1Id() : match.getChallongePlayer2Id();
                submitMatchResult(match, s1, s2, winnerId, challongeWinnerId);
            });

            card.getChildren().addAll(matchInfo, p1Text, sp1, vsText, sp2, p2Text, spacer, btnConfirm);

        } else {
            // ── Completed match: show names + scores + status badge ────
            HBox playersBox = new HBox();
            playersBox.setSpacing(20);
            playersBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            VBox player1Box = new VBox();
            player1Box.setSpacing(5);
            player1Box.setAlignment(javafx.geometry.Pos.CENTER);
            Text player1Icon = new Text("👤");
            player1Icon.getStyleClass().add("player-icon");
            Text player1Text = new Text(player1Name);
            player1Text.getStyleClass().add("player-name");
            Text player1Score = new Text("[" + match.getPlayer1Score() + "]");
            player1Score.getStyleClass().add("player-score");
            player1Box.getChildren().addAll(player1Icon, player1Text, player1Score);

            Text vsText = new Text("VS");
            vsText.getStyleClass().add("vs-text");

            VBox player2Box = new VBox();
            player2Box.setSpacing(5);
            player2Box.setAlignment(javafx.geometry.Pos.CENTER);
            Text player2Icon = new Text("👤");
            player2Icon.getStyleClass().add("player-icon");
            Text player2Text = new Text(player2Name);
            player2Text.getStyleClass().add("player-name");
            Text player2Score = new Text("[" + match.getPlayer2Score() + "]");
            player2Score.getStyleClass().add("player-score");
            player2Box.getChildren().addAll(player2Icon, player2Text, player2Score);

            playersBox.getChildren().addAll(player1Box, vsText, player2Box);

            javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

            StackPane statusBadge = new StackPane();
            statusBadge.getStyleClass().add("match-status-badge");
            statusBadge.getStyleClass().add("status-" + match.getStatus().name().toLowerCase());
            Text statusText = new Text(match.getStatus().getDisplayName());
            statusText.getStyleClass().add("match-status-text");
            statusBadge.getChildren().add(statusText);

            card.getChildren().addAll(matchInfo, playersBox, spacer, statusBadge);
        }

        return card;
    }

    private void submitMatchResult(TournamentMatch match, int p1Score, int p2Score,
                                   String winnerId, String challongeWinnerId) {
        try {
            // Save to local DB
            crudMatch.updateMatchResult(match.getId(), p1Score, p2Score, winnerId);

            // Push to Challonge
            if (tournament.getChallongeUrlSlug() != null
                    && match.getChallongeMatchId() != null
                    && challongeWinnerId != null) {
                try {
                    challongeService.updateMatchResult(
                            tournament.getChallongeUrlSlug(),
                            match.getChallongeMatchId(),
                            p1Score, p2Score,
                            challongeWinnerId);
                } catch (Exception e) {
                    System.err.println("Warning: Challonge update failed: " + e.getMessage());
                }
            }

            // Eliminate loser if appropriate
            String loserId = winnerId.equals(match.getPlayer1Id()) ? match.getPlayer2Id() : match.getPlayer1Id();
            if (loserId != null) {
                boolean isDoubleElim = tournament.getFormat() != null
                        && tournament.getFormat().toUpperCase().contains("DOUBLE");
                boolean isLosersMatch = match.getRound() < 0;
                if (!isDoubleElim || isLosersMatch) {
                    try {
                        crudParticipant.eliminatePlayer(tournament.getTournamentId(), loserId, Math.abs(match.getRound()));
                    } catch (Exception e) {
                        System.err.println("Warning: eliminatePlayer failed: " + e.getMessage());
                    }
                }
            }

            // Refresh UI
            refreshMatchesFromChallonge();
            checkAndCompleteTournament(winnerId);
            loadMatches();
            showBracket();
            updateStateBadge();

            if ("player".equalsIgnoreCase(currentUser.getRole())) {
                String playerId = getPlayerIdFromUserId(currentUser.getId());
                if (playerId != null) {
                    loadPlayerStatus(playerId);
                    updateJoinButton();
                }
            }

        } catch (Exception e) {
            System.err.println("Error submitting match result: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Échec de l'enregistrement: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Checks if all matches are done and, if so, marks the tournament COMPLETED,
     * sets the winner's status to WINNER, and hides the matches section controls.
     */
    private void checkAndCompleteTournament(String lastWinnerId) {
        try {
            List<TournamentMatch> allMatches = crudMatch.getAllByTournament(tournament.getTournamentId());
            if (allMatches.isEmpty()) return;

            boolean allDone = allMatches.stream()
                    .allMatch(m -> m.getStatus() == TournamentMatch.MatchStatus.COMPLETED);

            if (!allDone) return;

            // All matches completed — find the overall winner:
            // the player who won the last match (highest match number)
            TournamentMatch finalMatch = allMatches.stream()
                    .max(java.util.Comparator.comparingInt(TournamentMatch::getMatchNumber))
                    .orElse(null);

            String overallWinnerId = (finalMatch != null && finalMatch.getWinnerId() != null)
                    ? finalMatch.getWinnerId()
                    : lastWinnerId;

            // Mark winner in participants table
            if (overallWinnerId != null) {
                String query = "UPDATE tournament_participants SET status='WINNER', final_placement=1 " +
                        "WHERE tournament_id=? AND participant_id=?";
                java.sql.PreparedStatement pst = Genex.utils.Myconnection.getInstance().getCnx()
                        .prepareStatement(query);
                pst.setString(1, tournament.getTournamentId());
                pst.setString(2, overallWinnerId);
                pst.executeUpdate();
                System.out.println("Tournament winner set: " + overallWinnerId);
            }

            // Mark tournament as COMPLETED
            tournament.setState(Tounament.TournamentState.COMPLETED.name());
            crudTournament.updateEntity(tournament, tournament.getTournamentId());
            System.out.println("Tournament marked as COMPLETED");

            // Refresh UI
            updateStateBadge();
            setupRoleBasedUI();

        } catch (Exception e) {
            System.err.println("Error completing tournament: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String getPlayerName(String playerId) {
        if (playerId == null || playerId.isEmpty()) {
            return "TBD";
        }
        
        try {
            List<Player> players = crudPlayer.getEntities();
            Player player = players.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(playerId))
                    .findFirst()
                    .orElse(null);
            
            if (player != null) {
                return player.getNickname() != null && !player.getNickname().isEmpty() 
                        ? player.getNickname() 
                        : player.getUsername();
            }
        } catch (Exception e) {
            System.err.println("Error getting player name: " + e.getMessage());
        }
        
        return "Joueur inconnu";
    }
    
    /**
     * Called on first start - inserts all matches from Challonge into local DB
     */
    private void syncMatchesFromChallonge() {
        if (tournament == null || tournament.getChallongeUrlSlug() == null) {
            System.err.println("Cannot sync matches: tournament or URL slug is null");
            return;
        }
        
        try {
            System.out.println("Fetching matches from Challonge for: " + tournament.getChallongeUrlSlug());
            List<ChallongeService.ChallongeMatch> challongeMatches = 
                    challongeService.fetchMatches(tournament.getChallongeUrlSlug());
            System.out.println("Fetched " + challongeMatches.size() + " matches from Challonge");
            
            Map<String, String> participantMap = createParticipantMap();
            
            int matchNumber = 1;
            for (ChallongeService.ChallongeMatch cMatch : challongeMatches) {
                TournamentMatch localMatch = new TournamentMatch();
                localMatch.setTournamentId(tournament.getTournamentId());
                localMatch.setChallongeMatchId(cMatch.getId());
                localMatch.setRound(cMatch.getRound());
                localMatch.setMatchNumber(matchNumber++);
                
                if (cMatch.getPlayer1Id() != null) {
                    localMatch.setPlayer1Id(participantMap.get(cMatch.getPlayer1Id()));
                    localMatch.setChallongePlayer1Id(cMatch.getPlayer1Id());
                }
                if (cMatch.getPlayer2Id() != null) {
                    localMatch.setPlayer2Id(participantMap.get(cMatch.getPlayer2Id()));
                    localMatch.setChallongePlayer2Id(cMatch.getPlayer2Id());
                }
                
                if (cMatch.isCompleted()) {
                    localMatch.setPlayer1Score(cMatch.getPlayer1Score());
                    localMatch.setPlayer2Score(cMatch.getPlayer2Score());
                    if (cMatch.getWinnerId() != null) {
                        localMatch.setWinnerId(participantMap.get(cMatch.getWinnerId()));
                    }
                    localMatch.setStatus(TournamentMatch.MatchStatus.COMPLETED);
                    localMatch.setCompletedTime(java.time.LocalDateTime.now());
                } else {
                    localMatch.setStatus(TournamentMatch.MatchStatus.PENDING);
                }
                
                try {
                    crudMatch.addEntity(localMatch);
                } catch (Exception e) {
                    System.err.println("Error saving match: " + e.getMessage());
                }
            }
            System.out.println("Successfully synced " + challongeMatches.size() + " matches");
            
        } catch (Exception e) {
            System.err.println("Error syncing matches: " + e.getMessage());
            e.printStackTrace();
            showAlert("Avertissement", "Les matches n'ont pas pu être synchronisés.", Alert.AlertType.WARNING);
        }
    }
    
    /**
     * Called on refresh - updates existing matches from Challonge (players advancing, scores)
     */
    private void refreshMatchesFromChallonge() {
        if (tournament == null || tournament.getChallongeUrlSlug() == null) return;
        
        try {
            List<ChallongeService.ChallongeMatch> challongeMatches = 
                    challongeService.fetchMatches(tournament.getChallongeUrlSlug());
            
            Map<String, String> participantMap = createParticipantMap();
            
            // Get existing local matches indexed by challonge_match_id
            List<TournamentMatch> localMatches = crudMatch.getAllByTournament(tournament.getTournamentId());
            Map<String, TournamentMatch> localByChallongeId = new HashMap<>();
            for (TournamentMatch lm : localMatches) {
                if (lm.getChallongeMatchId() != null) {
                    localByChallongeId.put(lm.getChallongeMatchId(), lm);
                }
            }
            
            for (ChallongeService.ChallongeMatch cMatch : challongeMatches) {
                TournamentMatch localMatch = localByChallongeId.get(cMatch.getId());
                if (localMatch == null) continue; // shouldn't happen
                
                boolean changed = false;
                
                // Update player1 if it was TBD and now has a value
                if (cMatch.getPlayer1Id() != null && localMatch.getPlayer1Id() == null) {
                    localMatch.setPlayer1Id(participantMap.get(cMatch.getPlayer1Id()));
                    localMatch.setChallongePlayer1Id(cMatch.getPlayer1Id());
                    changed = true;
                }
                // Update player2 if it was TBD and now has a value
                if (cMatch.getPlayer2Id() != null && localMatch.getPlayer2Id() == null) {
                    localMatch.setPlayer2Id(participantMap.get(cMatch.getPlayer2Id()));
                    localMatch.setChallongePlayer2Id(cMatch.getPlayer2Id());
                    changed = true;
                }
                
                // Update scores and winner if completed
                if (cMatch.isCompleted() && localMatch.getStatus() != TournamentMatch.MatchStatus.COMPLETED) {
                    localMatch.setPlayer1Score(cMatch.getPlayer1Score());
                    localMatch.setPlayer2Score(cMatch.getPlayer2Score());
                    String localWinnerId = null;
                    if (cMatch.getWinnerId() != null) {
                        localWinnerId = participantMap.get(cMatch.getWinnerId());
                        localMatch.setWinnerId(localWinnerId);
                    }
                    localMatch.setStatus(TournamentMatch.MatchStatus.COMPLETED);
                    localMatch.setCompletedTime(java.time.LocalDateTime.now());
                    changed = true;

                    // Also eliminate the loser in tournament_participants
                    if (localWinnerId != null) {
                        String loserId = localWinnerId.equals(localMatch.getPlayer1Id())
                                ? localMatch.getPlayer2Id() : localMatch.getPlayer1Id();
                        if (loserId != null) {
                            boolean isDoubleElim = tournament.getFormat() != null
                                    && tournament.getFormat().toUpperCase().contains("DOUBLE");
                            boolean isLosersMatch = localMatch.getRound() < 0;
                            if (!isDoubleElim || isLosersMatch) {
                                try {
                                    crudParticipant.eliminatePlayer(
                                            tournament.getTournamentId(), loserId,
                                            Math.abs(localMatch.getRound()));
                                } catch (Exception ex) {
                                    System.err.println("Warning: eliminatePlayer in refresh failed: " + ex.getMessage());
                                }
                            }
                        }
                    }
                }
                
                if (changed) {
                    crudMatch.updateEntity(localMatch, localMatch.getId());
                    System.out.println("Updated match: Round " + localMatch.getRound() + " Match " + localMatch.getMatchNumber());
                }
            }
            
            System.out.println("Matches refreshed from Challonge");
            
        } catch (Exception e) {
            System.err.println("Error refreshing matches: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Map<String, String> createParticipantMap() {
        // Map: Challonge participant ID → Local player ID
        Map<String, String> map = new HashMap<>();
        
        try {
            List<TournamentParticipants> localParticipants = crudParticipant.getAll(tournament.getTournamentId());
            
            for (TournamentParticipants p : localParticipants) {
                if (p.getChallongeParticipantId() != null && !p.getChallongeParticipantId().isEmpty()) {
                    map.put(p.getChallongeParticipantId(), p.getParticipantId());
                    System.out.println("Mapped Challonge ID " + p.getChallongeParticipantId() + " → Player " + p.getParticipantId());
                }
            }
            
            System.out.println("Participant map created with " + map.size() + " entries");
            
        } catch (Exception e) {
            System.err.println("Error creating participant map: " + e.getMessage());
            e.printStackTrace();
        }
        
        return map;
    }

    private void loadPlayerStatus(String playerId) {
        try {
            TournamentParticipants participation = crudParticipant.getPlayerParticipation(
                    tournament.getTournamentId(), playerId);

            playerStatusSection.setVisible(true);
            playerStatusSection.setManaged(true);
            statusCard.getStyleClass().removeAll("status-active", "status-eliminated", "status-withdrew", "status-winner");

            if (participation == null) {
                txtStatusIcon.setText("⏳");
                txtPlayerStatus.setText("NON INSCRIT");
                txtStatusDetail.setText("");
                statusCard.getStyleClass().add("status-eliminated");
                return;
            }

            switch (participation.getStatus()) {
                case ACTIVE:
                    // Cross-check match results — the DB status may be stale
                    // if the admin reported a result but eliminatePlayer wasn't called
                    if (isActuallyEliminated(playerId)) {
                        // Fix the DB silently and fall through to ELIMINATED display
                        int lastLossRound = getLastLossRound(playerId);
                        crudParticipant.eliminatePlayer(
                                tournament.getTournamentId(), playerId, lastLossRound);
                        txtStatusIcon.setText("🔴");
                        txtPlayerStatus.setText("ÉLIMINÉ");
                        String round = lastLossRound > 0 ? " au Round " + lastLossRound : "";
                        txtStatusDetail.setText("Vous avez été éliminé" + round);
                        statusCard.getStyleClass().add("status-eliminated");
                    } else {
                        txtStatusIcon.setText("🟢");
                        txtPlayerStatus.setText("EN COMPÉTITION");
                        txtStatusDetail.setText("Vous êtes toujours en lice!");
                        statusCard.getStyleClass().add("status-active");
                    }
                    break;
                case WINNER:
                    txtStatusIcon.setText("🏆");
                    txtPlayerStatus.setText("VAINQUEUR!");
                    txtStatusDetail.setText("Félicitations! Vous avez remporté le tournoi!");
                    statusCard.getStyleClass().add("status-winner");
                    break;
                case ELIMINATED:
                    if (participation.withdrewFromTournament()) {
                        txtStatusIcon.setText("⚪");
                        txtPlayerStatus.setText("RETIRÉ");
                        String round = participation.getEliminatedAtRound() != null
                                ? " au Round " + participation.getEliminatedAtRound() : "";
                        txtStatusDetail.setText("Vous vous êtes retiré" + round);
                        statusCard.getStyleClass().add("status-withdrew");
                    } else {
                        txtStatusIcon.setText("🔴");
                        txtPlayerStatus.setText("ÉLIMINÉ");
                        String round = participation.getEliminatedAtRound() != null
                                ? " au Round " + participation.getEliminatedAtRound() : "";
                        txtStatusDetail.setText("Vous avez été éliminé" + round);
                        statusCard.getStyleClass().add("status-eliminated");
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error loading player status: " + e.getMessage());
        }
    }

    /**
     * Returns true if the player has been eliminated based on match results,
     * regardless of what the tournament_participants.status column says.
     * - Single elim: 1 loss = eliminated
     * - Double elim: 2 losses = eliminated, OR 1 loss with no pending matches left
     */
    private boolean isActuallyEliminated(String playerId) {
        try {
            int losses = crudMatch.getLossesInTournament(tournament.getTournamentId(), playerId);
            boolean isDoubleElim = tournament.getFormat() != null
                    && tournament.getFormat().toUpperCase().contains("DOUBLE");

            if (!isDoubleElim) {
                // Single elim: any loss = out
                return losses >= 1;
            } else {
                // Double elim: 2 losses = out
                if (losses >= 2) return true;
                // 1 loss but no pending match remaining = also out
                if (losses == 1) {
                    TournamentMatch pending = crudMatch.getActiveMatchForPlayer(
                            tournament.getTournamentId(), playerId);
                    return pending == null;
                }
            }
        } catch (Exception e) {
            System.err.println("Error checking elimination: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns the round number of the player's most recent loss.
     */
    private int getLastLossRound(String playerId) {
        try {
            List<TournamentMatch> matches = crudMatch.getAllByTournament(tournament.getTournamentId());
            int lastRound = 0;
            for (TournamentMatch m : matches) {
                if (m.getStatus() != TournamentMatch.MatchStatus.COMPLETED) continue;
                boolean isLoser = (playerId.equals(m.getPlayer1Id()) || playerId.equals(m.getPlayer2Id()))
                        && !playerId.equals(m.getWinnerId());
                if (isLoser) {
                    lastRound = Math.max(lastRound, Math.abs(m.getRound()));
                }
            }
            return lastRound;
        } catch (Exception e) {
            System.err.println("Error getting last loss round: " + e.getMessage());
        }
        return 0;
    }

    private void loadRankings() {
        try {
            rankingsSection.setVisible(true);
            rankingsSection.setManaged(true);
            rankingsList.getChildren().clear();

            List<TournamentParticipants> participants = crudParticipant.getAll(tournament.getTournamentId());

            // Sort: winners first, then by final_placement, then by wins
            participants.sort((a, b) -> {
                if (a.getFinalPlacement() != null && b.getFinalPlacement() != null)
                    return a.getFinalPlacement() - b.getFinalPlacement();
                if (a.getFinalPlacement() != null) return -1;
                if (b.getFinalPlacement() != null) return 1;
                int winsA = crudMatch.getWinsInTournament(tournament.getTournamentId(), a.getParticipantId());
                int winsB = crudMatch.getWinsInTournament(tournament.getTournamentId(), b.getParticipantId());
                return winsB - winsA;
            });

            int placement = 1;
            for (TournamentParticipants p : participants) {
                HBox row = createRankingRow(placement, p);
                rankingsList.getChildren().add(row);
                placement++;
            }
        } catch (Exception e) {
            System.err.println("Error loading rankings: " + e.getMessage());
        }
    }

    private HBox createRankingRow(int placement, TournamentParticipants p) {
        HBox row = new HBox();
        row.setSpacing(15);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().add("ranking-row");
        if (placement <= 3) row.getStyleClass().add("ranking-top-" + placement);

        // Placement — emoji for top 3, styled number for the rest
        Text placementText;
        if (placement == 1) {
            placementText = new Text("🥇");
        } else if (placement == 2) {
            placementText = new Text("🥈");
        } else if (placement == 3) {
            placementText = new Text("🥉");
        } else {
            placementText = new Text(placement + ".");
            // Explicit white fill so it doesn't go black on dark background
            placementText.setStyle("-fx-fill: rgba(255,255,255,0.7);");
        }
        placementText.getStyleClass().add("ranking-placement");

        // Player name — explicit white
        String playerName = getPlayerName(p.getParticipantId());
        Text nameText = new Text(playerName);
        nameText.getStyleClass().add("ranking-name");
        nameText.setStyle("-fx-fill: white;");

        // Wins / Losses — explicit light color
        int wins = crudMatch.getWinsInTournament(tournament.getTournamentId(), p.getParticipantId());
        int losses = crudMatch.getLossesInTournament(tournament.getTournamentId(), p.getParticipantId());
        Text wlText = new Text(wins + "W - " + losses + "L");
        wlText.getStyleClass().add("ranking-wl");
        wlText.setStyle("-fx-fill: rgba(255,255,255,0.75);");

        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Status badge
        StackPane badge = new StackPane();
        badge.getStyleClass().add("ranking-badge");
        String badgeText;
        if (p.isWinner()) {
            badgeText = "🏆 VAINQUEUR";
            badge.getStyleClass().add("badge-winner");
        } else if (p.withdrewFromTournament()) {
            badgeText = "RETIRÉ";
            badge.getStyleClass().add("badge-withdrew");
        } else {
            badgeText = "ÉLIMINÉ";
            badge.getStyleClass().add("badge-eliminated");
        }
        Text badgeLabel = new Text(badgeText);
        badgeLabel.getStyleClass().add("ranking-badge-text");
        badgeLabel.setStyle("-fx-fill: white;");
        badge.getChildren().add(badgeLabel);

        row.getChildren().addAll(placementText, nameText, spacer, wlText, badge);
        return row;
    }

    private void checkAndUpdateTournamentState() {
        try {
            // Only auto-update registration states, not IN_PROGRESS/COMPLETED/CANCELLED
            String state = tournament.getState();
            boolean isRegistrationState =
                Tounament.TournamentState.REGISTRATION_OPEN.name().equals(state) ||
                Tounament.TournamentState.REGISTRATION_CLOSED.name().equals(state);

            if (!isRegistrationState) return;

            int count = crudParticipant.getParticipantCount(tournament.getTournamentId());
            int max = tournament.getMaxPlayers();

            if (count >= max && Tounament.TournamentState.REGISTRATION_OPEN.name().equals(state)) {
                tournament.setState(Tounament.TournamentState.REGISTRATION_CLOSED.name());
                crudTournament.updateEntity(tournament, tournament.getTournamentId());
                updateStateBadge();
                updateJoinButton();
            } else if (count < max && Tounament.TournamentState.REGISTRATION_CLOSED.name().equals(state)) {
                tournament.setState(Tounament.TournamentState.REGISTRATION_OPEN.name());
                crudTournament.updateEntity(tournament, tournament.getTournamentId());
                updateStateBadge();
                updateJoinButton();
                System.out.println("Registration reopened - space available");
            }
        } catch (Exception e) {
            System.err.println("Error checking tournament state: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
