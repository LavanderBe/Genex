package Genex.Controllers.Player;

import Genex.entities.Player;
import Genex.services.CrudPlayer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PlayerController {
    @FXML private StackPane rootStackPane; // The root container
    @FXML private VBox contentArea;

    @FXML
    private Button btnAddPlayer;

    @FXML
    private Button btnSearch;

    @FXML
    private FlowPane playersContainer;

    @FXML
    private TextField searchField;

    private final CrudPlayer cp = new CrudPlayer();


    @FXML
    void initialize(){
        loadAllPlayers();
    }



    private void loadAllPlayers() {
        playersContainer.getChildren().clear();

        List<Player> players = cp.getEntities();

        for (Player player : players) {
            VBox card = createPlayerCard(player);
            playersContainer.getChildren().add(card);
        }
    }

    private VBox createPlayerCard(Player player) {
        VBox card = new VBox();
        card.getStyleClass().add("player-card"); // <--- MATCHES CSS
        card.setSpacing(8);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);

        Label nameLabel = new Label(player.getPrenom() + " " + player.getNom());
        nameLabel.getStyleClass().add("name-label"); // <--- MATCHES CSS

        Label nickLabel = new Label("🎮 " + player.getNickname());
        nickLabel.getStyleClass().add("nick-label"); // <--- MATCHES CSS

        Label roleLabel = new Label("👤 " + player.getRole());
        roleLabel.getStyleClass().add("role-label"); // <--- MATCHES CSS

        // Action buttons
        HBox actionBox = new HBox(10);
        actionBox.setPadding(new Insets(10, 0, 0, 0));

        Button editBtn = new Button("✏");
        editBtn.getStyleClass().add("card-edit-btn");

        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("card-delete-btn");

        actionBox.getChildren().addAll(editBtn, deleteBtn);
        card.getChildren().addAll(nameLabel, nickLabel, roleLabel, actionBox);

        return card;
    }

    @FXML
    private void addNewPlayer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Player/AddPlayer.fxml"));
            Parent addPlayerForm = loader.load();

            // 1. Apply Blur effect to the background
            GaussianBlur blur = new GaussianBlur(15);
            contentArea.setEffect(blur);
            contentArea.setDisable(true); // Prevent clicking background items

            // 2. Wrap the form in a darkening overlay (dimmer)
            VBox overlay = new VBox(addPlayerForm);
            overlay.setAlignment(javafx.geometry.Pos.CENTER);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Dim background

            // 3. Add to the stack
            rootStackPane.getChildren().add(overlay);

            // 4. Pass a "Close" callback to the AddPlayerController
            AddPlayerController controller = loader.getController();
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(overlay); // Remove form
                contentArea.setEffect(null);                // Remove blur
                contentArea.setDisable(false);              // Re-enable dashboard
                loadAllPlayers();                           // Refresh list
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void searchPlayers(ActionEvent event) {
        String query = searchField.getText().trim().toLowerCase();
        // TODO: Implement search
        loadAllPlayers(); // For now just reload all
    }

    private void showPlayerDetails(Player player) {
        System.out.println("Showing details for: ");
        // We will implement modal later
    }

    private void editPlayer(Player player) {
        System.out.println("Editing player: ");
    }

    private void deletePlayer(Player player) {
        System.out.println("Deleting player: " + player.getNickname());
        // Add confirmation + delete logic later
        loadAllPlayers(); // refresh
    }

}
