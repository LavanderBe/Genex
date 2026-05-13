package Genex.Controllers.Team;

import Genex.entities.Player;
import Genex.entities.Team;
import Genex.services.CrudPlayer;
import Genex.services.CrudTeamMember;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AddPlayerToTeamModalController {

    @FXML private TextField txtSearch;
    @FXML private VBox playersContainer;
    @FXML private Label infoLabel;
    @FXML private Button btnCloseModal;

    private Team team;
    private CrudPlayer crudPlayer;
    private CrudTeamMember crudTeamMember;
    private List<Player> allAvailablePlayers;
    private Consumer<Player> onPlayerAddedCallback;
    private Runnable onCloseCallback;

    @FXML
    public void initialize() {
        crudPlayer = new CrudPlayer();
        crudTeamMember = new CrudTeamMember();
        
        // Setup close button
        if (btnCloseModal != null) {
            btnCloseModal.setOnAction(e -> {
                if (onCloseCallback != null) {
                    onCloseCallback.run();
                }
            });
        }
        
        // Add search listener
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterPlayers(newVal));
    }

    public void setTeam(Team team) {
        this.team = team;
        loadAvailablePlayers();
    }

    public void setOnPlayerAddedCallback(Consumer<Player> callback) {
        this.onPlayerAddedCallback = callback;
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    private void loadAvailablePlayers() {
        try {
            System.out.println("=== Loading Available Players ===");
            
            // Get all players from database
            List<Player> allPlayers = crudPlayer.getEntities();
            System.out.println("Total players in database: " + allPlayers.size());
            
            // Filter out players who already have a team or are already in this team
            allAvailablePlayers = new ArrayList<>();
            List<Player> currentMembers = crudTeamMember.getMembersByTeam(team.getId());
            System.out.println("Current team members: " + currentMembers.size());
            
            for (Player player : allPlayers) {
                System.out.println("Checking player: " + player.getNickname() + " (ID: " + player.getId() + ")");
                
                // Skip if player already in this team
                boolean alreadyInTeam = false;
                for (Player member : currentMembers) {
                    if (member.getId() != null && member.getId().equals(player.getId())) {
                        alreadyInTeam = true;
                        System.out.println("  ❌ Already in this team");
                        break;
                    }
                }
                if (alreadyInTeam) continue;
                
                // Skip if player already has another team
                if (player.getId() != null) {
                    Team playerTeam = crudTeamMember.getTeamByPlayer(player.getId());
                    if (playerTeam != null) {
                        System.out.println("  ❌ Already has team: " + playerTeam.getName());
                        continue;
                    }
                }
                
                System.out.println("  ✅ Available");
                allAvailablePlayers.add(player);
            }
            
            System.out.println("Available players: " + allAvailablePlayers.size());
            
            if (allAvailablePlayers.isEmpty()) {
                infoLabel.setText("Aucun joueur disponible. Tous les joueurs ont déjà une équipe.");
                infoLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 12px;");
            } else {
                infoLabel.setText(allAvailablePlayers.size() + " joueur(s) disponible(s)");
                infoLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");
            }
            
            displayPlayers(allAvailablePlayers);
            
        } catch (Exception e) {
            System.err.println("Error loading available players: " + e.getMessage());
            e.printStackTrace();
            infoLabel.setText("Erreur lors du chargement des joueurs");
            infoLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 12px;");
        }
    }

    private void filterPlayers(String searchText) {
        if (allAvailablePlayers == null) return;
        
        if (searchText == null || searchText.trim().isEmpty()) {
            displayPlayers(allAvailablePlayers);
            return;
        }
        
        String search = searchText.toLowerCase().trim();
        List<Player> filtered = new ArrayList<>();
        
        // Search only by nickname for better performance
        for (Player player : allAvailablePlayers) {
            String nickname = player.getNickname() != null ? player.getNickname().toLowerCase() : "";
            
            if (nickname.startsWith(search)) {
                filtered.add(player);
            }
        }
        
        displayPlayers(filtered);
        
        if (filtered.isEmpty()) {
            infoLabel.setText("Aucun joueur trouvé pour \"" + searchText + "\"");
            infoLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.5); -fx-font-size: 12px;");
        } else {
            infoLabel.setText(filtered.size() + " joueur(s) trouvé(s)");
            infoLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");
        }
    }

    private void displayPlayers(List<Player> players) {
        playersContainer.getChildren().clear();
        
        if (players.isEmpty()) {
            Label emptyLabel = new Label("Aucun joueur disponible");
            emptyLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.4); -fx-font-style: italic; -fx-font-size: 13px;");
            playersContainer.getChildren().add(emptyLabel);
            return;
        }
        
        for (Player player : players) {
            HBox playerCard = createPlayerCard(player);
            playersContainer.getChildren().add(playerCard);
        }
    }

    private HBox createPlayerCard(Player player) {
        HBox card = new HBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 12 16;" +
            "-fx-cursor: hand;"
        );
        
        // Player info
        VBox infoBox = new VBox(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        String displayName = player.getNickname() != null ? player.getNickname() : player.getUsername();
        Label nameLabel = new Label(displayName);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        String fullName = ((player.getPrenom() != null ? player.getPrenom() : "") + " " +
                          (player.getNom() != null ? player.getNom() : "")).trim();
        
        if (!fullName.isEmpty()) {
            Label fullNameLabel = new Label(fullName);
            fullNameLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 12px;");
            infoBox.getChildren().addAll(nameLabel, fullNameLabel);
        } else {
            infoBox.getChildren().add(nameLabel);
        }
        
        // Add button
        Button btnAdd = new Button("＋ Ajouter");
        btnAdd.setStyle(
            "-fx-background-color: #8B0D0D;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: 600;" +
            "-fx-padding: 8 16;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );
        
        btnAdd.setOnMouseEntered(e -> 
            btnAdd.setStyle(
                "-fx-background-color: #A01010;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 8 16;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            )
        );
        
        btnAdd.setOnMouseExited(e -> 
            btnAdd.setStyle(
                "-fx-background-color: #8B0D0D;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: 600;" +
                "-fx-padding: 8 16;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            )
        );
        
        btnAdd.setOnAction(e -> addPlayerToTeam(player));
        
        // Hover effect for card
        card.setOnMouseEntered(e -> 
            card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08);" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 12 16;" +
                "-fx-cursor: hand;"
            )
        );
        
        card.setOnMouseExited(e -> 
            card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05);" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 12 16;" +
                "-fx-cursor: hand;"
            )
        );
        
        card.getChildren().addAll(infoBox, btnAdd);
        return card;
    }

    private void addPlayerToTeam(Player player) {
        try {
            if (player.getId() != null) {
                crudTeamMember.addMember(team.getId(), player.getId());
                
                // Show success message
                Alert success = new Alert(Alert.AlertType.INFORMATION);
                success.setTitle("Succès");
                success.setHeaderText(null);
                success.setContentText(player.getNickname() + " a été ajouté à l'équipe.");
                success.showAndWait();
                
                // Callback and close
                if (onPlayerAddedCallback != null) {
                    onPlayerAddedCallback.accept(player);
                }
                closeModal();
            }
        } catch (IllegalStateException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Équipe complète");
            alert.setHeaderText(null);
            alert.setContentText("L'équipe est complète (5/5 membres).");
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Error adding player: " + e.getMessage());
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Impossible d'ajouter le joueur à l'équipe.");
            alert.showAndWait();
        }
    }

    @FXML
    private void closeModal() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }
}
