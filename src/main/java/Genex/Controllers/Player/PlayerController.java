package Genex.Controllers.Player;

import Genex.entities.Game;
import Genex.entities.Player;
import Genex.services.CrudPlayer;
import Genex.services.CrudPlayer_Game;
import Genex.services.CrudUser;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlayerController {
    @FXML private StackPane rootStackPane;
    @FXML private VBox contentArea, playersList;
    @FXML private TextField searchField;

    private final CrudPlayer_Game cpg=new CrudPlayer_Game();
    private final CrudPlayer cp = new CrudPlayer();
    private final CrudUser cu=new CrudUser();

    private List<Player> allPlayersMaster = FXCollections.observableArrayList();

    private HBox selectedBlade = null;

    @FXML
    public void initialize() {

        allPlayersMaster = cp.getEntities();
        loadAllPlayers();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {filterBlades(newValue.toLowerCase());});

    }

    @FXML
    void HandleaddNewPlayer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Player/AddPlayer.fxml"));
            Parent addPlayerForm = loader.load();
            AddPlayerController formController = loader.getController();
            StackPane overlay = new StackPane(addPlayerForm);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0);"); // Start transparent
            overlay.setOpacity(0);
            rootStackPane.getChildren().add(overlay);
            GaussianBlur blur = new GaussianBlur(0);
            contentArea.setEffect(blur);
            addPlayerForm.setTranslateY(-700);
            addPlayerForm.setScaleX(0.9);
            addPlayerForm.setScaleY(0.9);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), overlay);
            fadeIn.setToValue(1);
            TranslateTransition slideDown = new TranslateTransition(Duration.millis(500), addPlayerForm);
            slideDown.setToY(0);
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(500), addPlayerForm);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);
            Timeline blurTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
                    new KeyFrame(Duration.millis(400), new KeyValue(blur.radiusProperty(), 15))
            );
            ParallelTransition entrance = new ParallelTransition(fadeIn, slideDown, scaleUp, blurTimeline);
            entrance.play();
            formController.setOnCloseCallback(() -> {
                slideDown.setRate(-1);
                scaleUp.setRate(-1);
                fadeIn.setRate(-1);

                Timeline unblurTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 15)),
                        new KeyFrame(Duration.millis(400), new KeyValue(blur.radiusProperty(), 0))
                );

                ParallelTransition exit = new ParallelTransition(fadeIn, slideDown, scaleUp, unblurTimeline);
                exit.setOnFinished(e -> {
                    rootStackPane.getChildren().remove(overlay);
                    contentArea.setEffect(null);
                    loadAllPlayers();
                });
                exit.play();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //loading the entire player database
    private void loadAllPlayers() {
        playersList.getChildren().clear();
        List<Player> players = cp.getEverythingPlayers();

        for (int i = 0; i < players.size(); i++) {
            HBox row = createPlayerRow(players.get(i), i);
            playersList.getChildren().add(row);
        }
    }

    //creating a blade with animation
    private HBox createPlayerRow(Player player, int index) {
        HBox blade = new HBox(30);
        blade.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        blade.getStyleClass().add("player-blade");
        blade.setPrefHeight(70);

        StackPane avatar = new StackPane();
        avatar.setPrefSize(45, 45);
        avatar.getStyleClass().add("avatar-slot");

        String url = player.getAvatar_url();

        if (url != null && !url.isEmpty()) {
            ImageView iv = new ImageView();
            Image avatarImg = new Image(url, true);

            iv.setImage(avatarImg);
            iv.setFitWidth(40);
            iv.setFitHeight(40);
            iv.setPreserveRatio(true);
            iv.setOpacity(0.5);
            avatarImg.progressProperty().addListener((obs, old, progress) -> {
                if (progress.doubleValue() == 1.0) iv.setOpacity(1.0);
            });

            avatar.getChildren().add(iv);
        } else {
            Label initial = new Label(player.getNickname().substring(0, 1).toUpperCase());
            initial.setTextFill(Color.WHITE);
            initial.getStyleClass().add("avatar-initial-label");
            avatar.getChildren().add(initial);
        }



        VBox identity = new VBox(2);
        Label nick = new Label(player.getNickname().toUpperCase());
        nick.getStyleClass().add("blade-nick");

        Label fullName = new Label(player.getPrenom() + " " + player.getNom());
        fullName.getStyleClass().add("blade-name");
        identity.getChildren().addAll(nick, fullName);

        VBox specs = new VBox(5);
        specs.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label role = new Label("PROTOCOLE: JOUEUR");
        role.setId("role");
        role.setStyle("-fx-text-fill: #5c7cfa; -fx-font-family: 'Consolas'; -fx-font-size: 10px;");
        if (player.getRole().equals("coach")){
            role.setText("PROTOCOLE: COACH");
            role.setStyle("-fx-text-fill: #ffbb33;");
        }

        Label level = new Label("NOM: "+ player.getNom() + " "+player.getPrenom());
        level.setStyle("-fx-text-fill: #444466; -fx-font-family: 'Consolas'; -fx-font-size: 10px;");
        specs.getChildren().addAll(role, level);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(3);
        actions.getStyleClass().add("actions-container");
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        Button modifyBtn = new Button("MODIFIER ");
        modifyBtn.getStyleClass().add("blade-btn-edit");

        Button terminateBtn = new Button("SUPPRIMER");
        terminateBtn.getStyleClass().add("blade-btn-delete");
        terminateBtn.setOnAction(e -> handleTerminate(player, blade));

        if (player.getRole().equals("player")){
            Button promoteBtn = new Button("PROMOUVOIR");
            promoteBtn.getStyleClass().add("blade-btn-promote");
            promoteBtn.setId("promotionBtn");
            promoteBtn.setOnAction(e -> {
                e.consume();
                handlePromotion(player, blade);
            });
            actions.getChildren().addAll(promoteBtn, modifyBtn, terminateBtn);
        }
        else {
            Button promoteBtn = new Button("RETROGRADER");
            promoteBtn.getStyleClass().add("blade-btn-promote");
            promoteBtn.setId("promotionBtn");
            promoteBtn.setOnAction(e -> {
                e.consume();
                handlePromotion(player, blade);
            });
            actions.getChildren().addAll(promoteBtn, modifyBtn, terminateBtn);
        }
        FlowPane gamesLinked = new FlowPane();
        gamesLinked.setHgap(8);
        gamesLinked.setVgap(5);
        gamesLinked.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        gamesLinked.setPrefWidth(250);

        List<String> gameNames = new ArrayList<String>();
        List<Game> l=cpg.get_GamesPlayed(player);
        for (Game g:l){
            gameNames.add(g.getNom());
        }

        for (String gName : gameNames) {
            Label gameTag = new Label("[ " + gName + " ]");
            gameTag.getStyleClass().add("game-data-tag");
            gamesLinked.getChildren().add(gameTag);
            Tooltip.install(gameTag, new Tooltip("STABLE_CONNECTION: " + gName));
        }

        blade.getChildren().addAll(avatar,identity,specs,gamesLinked,spacer,actions);

        blade.setOpacity(0);
        blade.setTranslateX(-100);

        double delay = index * 0.1;
        TranslateTransition tt = new TranslateTransition(Duration.millis(400), blade);
        tt.setToX(0);
        tt.setDelay(Duration.seconds(delay));
        tt.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0));

        FadeTransition ft = new FadeTransition(Duration.millis(300), blade);
        ft.setToValue(1);
        ft.setDelay(Duration.seconds(delay));

        new ParallelTransition(tt, ft).play();

        blade.setOnMouseClicked(event -> {
            System.out.println("Selecting Entity: " + player.getNickname());
            handleBladeSelection(blade, player);
        });

        modifyBtn.setOnMouseClicked(event -> {
            event.consume();
            handleModify(player);
        });

        terminateBtn.setOnMouseClicked(event -> {
            event.consume();
            handleTerminate(player, blade);
        });

        return blade;
    }

    private void handlePromotion(Player player, HBox blade) {

        if (player.getRole().equals("player")){
            cu.promoteToCoach(player);
            player.setRole("coach");
            Label role=(Label) blade.lookup("#role");
            Button promote=(Button) blade.lookup("#promotionBtn");

            FadeTransition flash = new FadeTransition(Duration.millis(100), blade);
            flash.setFromValue(1.0);
            flash.setToValue(0.3);
            flash.setCycleCount(4);
            flash.setAutoReverse(true);
            flash.setOnFinished(e -> {
                role.setText("PROTOCOLE: COACH");
                role.setStyle("-fx-text-fill: #ffbb33; -fx-font-family: 'Consolas'; -fx-font-size: 10px;");
                promote.setText("RETROGRADER");
            });
            flash.play();
        }
        else {
            cu.demoteToPlayer(player);
            cp.deleteRequest(player);
            player.setRole("player");
            Label role=(Label) blade.lookup("#role");
            Button promote=(Button) blade.lookup("#promotionBtn");

            FadeTransition flash = new FadeTransition(Duration.millis(100), blade);
            flash.setFromValue(1.0);
            flash.setToValue(0.3);
            flash.setCycleCount(4);
            flash.setAutoReverse(true);
            flash.setOnFinished(e -> {
                role.setText("PROTOCOLE: JOUEUR");
                role.setStyle("-fx-text-fill: #5c7cfa; -fx-font-family: 'Consolas'; -fx-font-size: 10px;");
                promote.setText("PROMOUVOIR");
            });
            flash.play();
        }


    }

    private void handleModify(Player p) {
        try {
            System.out.println("updating player with this id "+p.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Player/AddPlayer.fxml"));
            Parent form = loader.load();
            AddPlayerController controller = loader.getController();
            controller.setPlayerData(p);

            StackPane overlay = new StackPane(form);
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0);"); // Start transparent
            overlay.setOpacity(0);
            rootStackPane.getChildren().add(overlay);

            // 2. Setup Blur Effect
            GaussianBlur blur = new GaussianBlur(0);
            contentArea.setEffect(blur);

            // 3. Setup the Form Panel start position (Slide in from top)
            form.setTranslateY(-700);
            form.setScaleX(0.9);
            form.setScaleY(0.9);

            // 4. ANIMATION SEQUENCE
            // A. Fade in the darkness
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), overlay);
            fadeIn.setToValue(1);

            // B. Slide & Scale the form shard
            TranslateTransition slideDown = new TranslateTransition(Duration.millis(500), form);
            slideDown.setToY(0);

            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(500), form);
            scaleUp.setToX(1.0);
            scaleUp.setToY(1.0);

            // C. Animate the Blur radius (Timeline is needed for the radius property)
            Timeline blurTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 0)),
                    new KeyFrame(Duration.millis(400), new KeyValue(blur.radiusProperty(), 15))
            );

            ParallelTransition entrance = new ParallelTransition(fadeIn, slideDown, scaleUp, blurTimeline);
            entrance.play();

            // 5. CALLBACK FOR CLOSING
            controller.setOnCloseCallback(() -> {
                // Reverse Animations
                slideDown.setRate(-1);
                scaleUp.setRate(-1);
                fadeIn.setRate(-1);

                Timeline unblurTimeline = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(blur.radiusProperty(), 15)),
                        new KeyFrame(Duration.millis(400), new KeyValue(blur.radiusProperty(), 0))
                );

                ParallelTransition exit = new ParallelTransition(fadeIn, slideDown, scaleUp, unblurTimeline);
                exit.setOnFinished(e -> {
                    rootStackPane.getChildren().remove(overlay);
                    contentArea.setEffect(null);
                    loadAllPlayers(); // Refresh the list
                });
                exit.play();
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleTerminate(Player p, HBox node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setToX(0);
        FadeTransition ft = new FadeTransition(Duration.millis(200), node);
        ft.setToValue(0);
        ParallelTransition pt = new ParallelTransition(st, ft);
        pt.setOnFinished(e -> {
            cp.deleteEntity(p.getId());
            playersList.getChildren().remove(node);
        });
        pt.play();
    }

    private void filterBlades(String query) {
        if (query == null || query.isEmpty()) {
            renderBlades(allPlayersMaster);
            return;
        }
        System.out.println(allPlayersMaster);

        List<Player> filteredList = allPlayersMaster.stream().filter(p -> p.getNom().toLowerCase().contains(query) || p.getNickname().toLowerCase().contains(query)).toList();

        renderBlades(filteredList);
    }

    private void renderBlades(List<Player> playersToDisplay) {
        // 1. Clear the visual container
        playersList.getChildren().clear();

        // 2. If no results, show a "System Warning"
        if (playersToDisplay.isEmpty()) {
            Label emptyLabel = new Label(">> [!] NO_ENTITY_MATCHES_QUERY");
            emptyLabel.setStyle("-fx-text-fill: #8B0D0D; -fx-font-family: 'Consolas'; -fx-padding: 50;");
            playersList.getChildren().add(emptyLabel);
            return;
        }

        // 3. Loop and recreate the blades (Triggering the pop-up animations)
        for (int i = 0; i < playersToDisplay.size(); i++) {
            HBox row = createPlayerRow(playersToDisplay.get(i), i);
            playersList.getChildren().add(row);
        }
    }

    private void handleBladeSelection(HBox blade, Player player) {
        // Remove highlight from previous selection
        if (selectedBlade != null) {
            selectedBlade.getStyleClass().remove("selected-blade");
        }

        // Add highlight to new selection
        selectedBlade = blade;
        selectedBlade.getStyleClass().add("selected-blade");

        // Optional: Load more details about the player in a side panel
    }


}