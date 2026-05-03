package Genex.Controllers.Tournament;

import Genex.entities.*;
import Genex.services.*;
import Genex.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

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

    private Tounament tournament;
    private User currentUser;
    private boolean isPlayerJoined = false;
    private StackPane rootStackPane;

    private CrudGame crudGame = new CrudGame();
    private CrudCenter crudCenter = new CrudCenter();
    private CrudTournamentParticipant crudParticipant = new CrudTournamentParticipant();
    private CrudPlayer crudPlayer = new CrudPlayer();
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
            // Player view: Show join/leave button, hide participants section and sync button
            btnJoinLeave.setVisible(true);
            btnJoinLeave.setManaged(true);
            participantsSection.setVisible(false);
            participantsSection.setManaged(false);
            btnSync.setVisible(false);
            btnSync.setManaged(false);
            btnStart.setVisible(false);
            btnStart.setManaged(false);

            // Check if player is already joined
            checkPlayerJoinStatus();
            
            // Show bracket if synced
            if (tournament.isSynced()) {
                showBracket();
            } else {
                hideBracket();
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
            // Get the player ID from the players table using the user ID
            String playerId = getPlayerIdFromUserId(currentUser.getId());
            
            if (playerId == null) {
                System.err.println("Player ID not found for user: " + currentUser.getUsername());
                isPlayerJoined = false;
                updateJoinButton();
                return;
            }

            isPlayerJoined = crudParticipant.isPlayerParticipating(
                    tournament.getTournamentId(), 
                    playerId
            );

            updateJoinButton();
        } catch (Exception e) {
            System.err.println("Error checking player join status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateJoinButton() {
        if (isPlayerJoined) {
            btnJoinLeave.setText("QUITTER");
            // Keep btn-join class for polygon shape, add joined for red color
            if (!btnJoinLeave.getStyleClass().contains("joined")) {
                btnJoinLeave.getStyleClass().add("joined");
            }
        } else {
            btnJoinLeave.setText("REJOINDRE");
            // Remove joined class to show green color
            btnJoinLeave.getStyleClass().remove("joined");
        }
    }

    @FXML
    private void handleJoinLeave() {
        if (currentUser == null || tournament == null) return;

        try {
            // Get the player ID from the players table using the user ID
            String playerId = getPlayerIdFromUserId(currentUser.getId());
            
            if (playerId == null) {
                showAlert("Erreur", "Profil joueur introuvable.", Alert.AlertType.ERROR);
                return;
            }

            if (isPlayerJoined) {
                // Leave tournament
                crudParticipant.removePlayerFromTournament(
                        tournament.getTournamentId(), 
                        playerId
                );
                isPlayerJoined = false;
                showAlert("Succès", "Vous avez quitté le tournoi.", Alert.AlertType.INFORMATION);
            } else {
                // Join tournament
                int currentCount = crudParticipant.getParticipantCount(tournament.getTournamentId());
                
                TournamentParticipants participant = TournamentParticipants.solo(
                        tournament.getTournamentId(),
                        playerId,
                        currentCount + 1  // seed based on join order
                );
                
                crudParticipant.addEntity(participant);
                isPlayerJoined = true;
                showAlert("Succès", "Vous avez rejoint le tournoi!", Alert.AlertType.INFORMATION);
            }

            updateJoinButton();

        } catch (Exception e) {
            System.err.println("Error joining/leaving tournament: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Une erreur est survenue.", Alert.AlertType.ERROR);
        }
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

        // Icon
        Text icon = new Text("👤");
        icon.getStyleClass().add("participant-icon");

        // Get player name
        String playerName = "Joueur inconnu";
        try {
            List<Player> players = crudPlayer.getEntities();
            Player player = players.stream()
                    .filter(p -> p.getId() != null && p.getId().equals(participant.getParticipantId()))
                    .findFirst()
                    .orElse(null);
            
            if (player != null) {
                playerName = player.getNickname() != null && !player.getNickname().isEmpty() 
                        ? player.getNickname() 
                        : player.getUsername();
            }
        } catch (Exception e) {
            System.err.println("Error getting player name: " + e.getMessage());
        }

        // Name
        Text name = new Text(playerName);
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
            Parent tournamentHub = loader.load();

            // Replace content in rootStackPane
            rootStackPane.getChildren().clear();
            rootStackPane.getChildren().add(tournamentHub);

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
                    // Delete all matches for this tournament
                    List<TournamentMatch> matches = crudMatch.getAllByTournament(tournament.getTournamentId());
                    for (TournamentMatch match : matches) {
                        crudMatch.deleteEntity(match);
                    }
                    System.out.println("Deleted " + matches.size() + " matches from database");
                    
                    // Reset tournament state completely
                    tournament.setStarted(false);
                    tournament.setSynced(false);
                    tournament.setChallongeId(null);
                    tournament.setChallongeUrl(null);
                    tournament.setChallongeUrlSlug(null);
                    tournament.setState(Tounament.TournamentState.REGISTRATION_CLOSED.name());
                    
                    // Save to database
                    crudTournament.updateEntity(tournament, tournament.getTournamentId());
                    
                    // Update UI
                    updateStateBadge();
                    setupRoleBasedUI();
                    
                    showAlert("Succès", 
                            "Tournoi réinitialisé!\n\n" +
                            "Vous pouvez maintenant:\n" +
                            "1. Cliquer 'SYNCHRONISER AVEC CHALLONGE' pour créer un nouveau bracket\n" +
                            "2. Ou aller sur challonge.com pour supprimer l'ancien bracket d'abord", 
                            Alert.AlertType.INFORMATION);
                    
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
        // Sync matches from Challonge
        syncMatchesFromChallonge();
        // Then load them
        loadMatches();
        showAlert("Succès", "Matches actualisés!", Alert.AlertType.INFORMATION);
    }
    
    private void loadMatches() {
        if (tournament == null) return;
        
        try {
            // Get all matches for this tournament
            List<TournamentMatch> matches = crudMatch.getAllByTournament(tournament.getTournamentId());
            
            // Clear matches list
            matchesList.getChildren().clear();
            
            if (matches.isEmpty()) {
                emptyMatches.setVisible(true);
                emptyMatches.setManaged(true);
                return;
            }
            
            emptyMatches.setVisible(false);
            emptyMatches.setManaged(false);
            
            // Group matches by round
            Map<Integer, List<TournamentMatch>> matchesByRound = new HashMap<>();
            for (TournamentMatch match : matches) {
                matchesByRound.computeIfAbsent(match.getRound(), k -> new java.util.ArrayList<>()).add(match);
            }
            
            // Create UI for each round
            List<Integer> rounds = new java.util.ArrayList<>(matchesByRound.keySet());
            java.util.Collections.sort(rounds);
            
            for (Integer round : rounds) {
                VBox roundSection = createRoundSection(round, matchesByRound.get(round));
                matchesList.getChildren().add(roundSection);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading matches: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private VBox createRoundSection(int round, List<TournamentMatch> matches) {
        VBox roundSection = new VBox();
        roundSection.setSpacing(12);
        roundSection.getStyleClass().add("round-section");
        
        // Round header
        Text roundTitle = new Text("Round " + round);
        roundTitle.getStyleClass().add("round-title");
        roundSection.getChildren().add(roundTitle);
        
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
        
        // Players section
        HBox playersBox = new HBox();
        playersBox.setSpacing(20);
        playersBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // Player 1
        VBox player1Box = new VBox();
        player1Box.setSpacing(5);
        player1Box.setAlignment(javafx.geometry.Pos.CENTER);
        Text player1Icon = new Text("👤");
        player1Icon.getStyleClass().add("player-icon");
        String player1Name = getPlayerName(match.getPlayer1Id());
        Text player1Text = new Text(player1Name);
        player1Text.getStyleClass().add("player-name");
        Text player1Score = new Text("[" + match.getPlayer1Score() + "]");
        player1Score.getStyleClass().add("player-score");
        player1Box.getChildren().addAll(player1Icon, player1Text, player1Score);
        
        // VS
        Text vsText = new Text("VS");
        vsText.getStyleClass().add("vs-text");
        
        // Player 2
        VBox player2Box = new VBox();
        player2Box.setSpacing(5);
        player2Box.setAlignment(javafx.geometry.Pos.CENTER);
        Text player2Icon = new Text("👤");
        player2Icon.getStyleClass().add("player-icon");
        String player2Name = getPlayerName(match.getPlayer2Id());
        Text player2Text = new Text(player2Name);
        player2Text.getStyleClass().add("player-name");
        Text player2Score = new Text("[" + match.getPlayer2Score() + "]");
        player2Score.getStyleClass().add("player-score");
        player2Box.getChildren().addAll(player2Icon, player2Text, player2Score);
        
        playersBox.getChildren().addAll(player1Box, vsText, player2Box);
        
        // Spacer
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Status badge
        StackPane statusBadge = new StackPane();
        statusBadge.getStyleClass().add("match-status-badge");
        statusBadge.getStyleClass().add("status-" + match.getStatus().name().toLowerCase());
        Text statusText = new Text(match.getStatus().getDisplayName());
        statusText.getStyleClass().add("match-status-text");
        statusBadge.getChildren().add(statusText);
        
        // Report button (only for pending matches)
        Button btnReport = new Button("REPORTER RÉSULTAT");
        btnReport.getStyleClass().add("btn-report");
        btnReport.setOnAction(e -> handleReportResult(match));
        btnReport.setVisible(match.getStatus() == TournamentMatch.MatchStatus.PENDING);
        btnReport.setManaged(match.getStatus() == TournamentMatch.MatchStatus.PENDING);
        
        card.getChildren().addAll(matchInfo, playersBox, spacer, statusBadge, btnReport);
        
        return card;
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
    
    private void handleReportResult(TournamentMatch match) {
        try {
            // Load modal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tournament/MatchReportModal.fxml"));
            Parent modalRoot = loader.load();
            
            // Get controller and set data
            MatchReportModalController modalController = loader.getController();
            modalController.setMatch(
                    match,
                    getPlayerName(match.getPlayer1Id()),
                    getPlayerName(match.getPlayer2Id()),
                    tournament.getChallongeUrlSlug()
            );
            
            // Set callback to refresh matches after submission
            modalController.setOnSuccess(() -> {
                loadMatches();
                showBracket(); // Refresh bracket
            });
            
            // Create and show modal stage
            javafx.stage.Stage modalStage = new javafx.stage.Stage();
            modalStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            modalStage.initOwner(txtTournamentName.getScene().getWindow());
            modalStage.setTitle("Reporter le résultat");
            modalStage.setScene(new javafx.scene.Scene(modalRoot));
            modalStage.setResizable(false);
            modalStage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Error opening match report modal: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire de rapport.", Alert.AlertType.ERROR);
        }
    }
    
    private void syncMatchesFromChallonge() {
        if (tournament == null || tournament.getChallongeUrlSlug() == null) {
            System.err.println("Cannot sync matches: tournament or URL slug is null");
            return;
        }
        
        try {
            System.out.println("Fetching matches from Challonge for: " + tournament.getChallongeUrlSlug());
            
            // Fetch matches from Challonge
            List<ChallongeService.ChallongeMatch> challongeMatches = 
                    challongeService.fetchMatches(tournament.getChallongeUrlSlug());
            
            System.out.println("Fetched " + challongeMatches.size() + " matches from Challonge");
            
            // Create a map of participant IDs: Challonge ID → Local Player ID
            Map<String, String> participantMap = createParticipantMap();
            
            // Save matches to local database
            int matchNumber = 1;
            for (ChallongeService.ChallongeMatch cMatch : challongeMatches) {
                System.out.println("Processing match: Round " + cMatch.getRound() + 
                                 ", Player1: " + cMatch.getPlayer1Id() + 
                                 ", Player2: " + cMatch.getPlayer2Id());
                
                TournamentMatch localMatch = new TournamentMatch();
                localMatch.setTournamentId(tournament.getTournamentId());
                localMatch.setChallongeMatchId(cMatch.getId());
                localMatch.setRound(cMatch.getRound());
                localMatch.setMatchNumber(matchNumber++);
                
                // Map Challonge participant IDs to local player IDs
                if (cMatch.getPlayer1Id() != null) {
                    String localId = participantMap.get(cMatch.getPlayer1Id());
                    localMatch.setPlayer1Id(localId);
                    System.out.println("  Player1 mapped: " + cMatch.getPlayer1Id() + " -> " + localId);
                }
                if (cMatch.getPlayer2Id() != null) {
                    String localId = participantMap.get(cMatch.getPlayer2Id());
                    localMatch.setPlayer2Id(localId);
                    System.out.println("  Player2 mapped: " + cMatch.getPlayer2Id() + " -> " + localId);
                }
                
                // Set scores and winner if match is completed
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
                
                // Save to database
                try {
                    crudMatch.addEntity(localMatch);
                    System.out.println("  Match saved to database with ID: " + localMatch.getId());
                } catch (Exception e) {
                    System.err.println("  Error saving match: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Successfully synced " + challongeMatches.size() + " matches from Challonge");
            
        } catch (Exception e) {
            System.err.println("Error syncing matches from Challonge: " + e.getMessage());
            e.printStackTrace();
            showAlert("Avertissement", "Les matches n'ont pas pu être synchronisés automatiquement. Vous pouvez les actualiser manuellement.", Alert.AlertType.WARNING);
        }
    }
    
    private Map<String, String> createParticipantMap() {
        // Create a mapping between Challonge participant IDs and local player IDs
        // We need to fetch participants from Challonge and match them with local participants
        Map<String, String> map = new HashMap<>();
        
        try {
            // Get local participants
            List<TournamentParticipants> localParticipants = crudParticipant.getAll(tournament.getTournamentId());
            
            // For now, we'll use a simple approach:
            // The order of participants in Challonge should match the order they were added locally
            // This is a temporary solution until we implement proper Challonge participant ID storage
            
            // TODO: Store Challonge participant IDs when creating participants
            // For now, matches will be created but player IDs might be null
            // The matches will still work, just player names might show as "TBD"
            
            System.out.println("Participant mapping: " + localParticipants.size() + " local participants found");
            
        } catch (Exception e) {
            System.err.println("Error creating participant map: " + e.getMessage());
            e.printStackTrace();
        }
        
        return map;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
