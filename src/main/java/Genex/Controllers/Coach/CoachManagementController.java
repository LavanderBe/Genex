package Genex.Controllers.Coach;

import Genex.entities.Player;
import Genex.services.CrudPlayer;
import Genex.services.CrudUser;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class CoachManagementController {

    @FXML private VBox coachesList, requestsList;
    @FXML private Label queueCount;
    private final CrudPlayer cp = new CrudPlayer();
    private final CrudUser cu=new CrudUser();



    @FXML
    public void initialize() {
        refreshProtocol();
    }
    private void refreshProtocol() {
        coachesList.getChildren().clear();
        requestsList.getChildren().clear();
        List<Player> coaches = cp.getCoaches();
        for (int i = 0; i < coaches.size(); i++) {
            coachesList.getChildren().add(createCoachBlade(coaches.get(i), i));
        }
        List<Player> requesters = cp.getPromotionRequesters();
        List<Player> accepted=new ArrayList<>();
        List<Player> refused=new ArrayList<>();
        List<Player> pending=new ArrayList<>();
        for (Player p:requesters){
            if (p.getStatus().equals("pending")){
                pending.add(p);
            }
            if (p.getStatus().equals("accepted")){
                accepted.add(p);
            }
            else{
                refused.add(p);
            }
        }
        queueCount.setText(String.valueOf(pending.size()));
        for (int i = 0; i < pending.size(); i++) {
            requestsList.getChildren().add(createRequestBlade(pending.get(i), i));
        }
    }

    private HBox createCoachBlade(Player p, int index) {
        HBox blade = new HBox(20);
        blade.getStyleClass().add("coach-blade");
        blade.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox info = new VBox(2);
        Label nick = new Label(p.getNickname().toUpperCase());
        nick.setStyle("-fx-text-fill: white; -fx-font-family: 'Arial Black'; -fx-font-size: 14px;");
        Label details = new Label("COACH_CERTIFIE // EMAIL: " + p.getEmail());
        details.setStyle("-fx-text-fill: #5c7cfa; -fx-font-family: 'Consolas'; -fx-font-size: 9px;");
        info.getChildren().addAll(nick, details);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button demoteBtn = new Button("RETROGRADER");
        demoteBtn.getStyleClass().add("btn-dock-right");
        demoteBtn.setStyle("-fx-translate-x: 0;"); // Reset docking for single button
        demoteBtn.setOnAction(e -> handleAction(p, blade, "demote"));

        blade.getChildren().addAll(info, spacer, demoteBtn);

        animateEntrance(blade, index);
        return blade;
    }

    private VBox createRequestBlade(Player p, int index) {
        VBox blade = new VBox(12);
        blade.getStyleClass().add("request-blade");

        Label tag = new Label(">> INCOMING_UPLINK_REQUEST");
        tag.setStyle("-fx-text-fill: #8B0D0D; -fx-font-family: 'Consolas'; -fx-font-size: 9px; -fx-font-weight: bold;");

        Label nick = new Label(p.getNickname().toUpperCase());
        nick.setStyle("-fx-text-fill: white; -fx-font-family: 'Impact'; -fx-font-size: 18px; -fx-letter-spacing: 1px;");
        HBox actions = new HBox(0);
        Button approve = new Button("ACCEPTER");
        approve.getStyleClass().add("btn-dock-left");
        approve.setOnAction(e -> handleAction(p, blade, "approve"));

        Button reject = new Button("REFUSER");
        reject.getStyleClass().add("btn-dock-right");
        reject.setOnAction(e -> handleAction(p, blade, "reject"));

        actions.getChildren().addAll(approve, reject);

        blade.getChildren().addAll(tag, nick, actions);

        animateEntrance(blade, index);
        return blade;
    }

    private void handleAction(Player p, Pane node, String type) {
        node.setDisable(true);
        String glowColor = type.equals("approve") ? "#22c55e" : "#ff5252";
        node.setStyle("-fx-border-color: " + glowColor + "; -fx-border-width: 2;");
        Timeline flash = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(node.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(50), new KeyValue(node.opacityProperty(), 0.4)),
                new KeyFrame(Duration.millis(100), new KeyValue(node.opacityProperty(), 1.0))
        );
        flash.setCycleCount(2);
        flash.setOnFinished(finishFlash -> {
            animateExit(node, () -> {
                new Thread(() -> {
                    try {
                        if ("approve".equals(type)) {
                            cu.promoteToCoach(p);
                            cp.acceptRequest(p);
                        } else if ("reject".equals(type)) {
                            cp.rejectRequest(p);
                        } else if ("demote".equals(type)) {
                            cu.demoteToPlayer(p);
                            cp.deleteRequest(p);
                        }
                        Platform.runLater(this::refreshProtocol);
                    } catch (Exception ex) {
                        System.err.println("GENEX // DATABASE_LINK_ERROR");
                        Platform.runLater(() -> node.setDisable(false));
                    }
                }).start();
            });
        });

        flash.play();
    }


    private void animateEntrance(Pane node, int index) {
        node.setOpacity(0);
        node.setTranslateY(20);
        double delay = index * 0.1;
        TranslateTransition tt = new TranslateTransition(Duration.millis(400), node);
        tt.setToY(0);
        tt.setDelay(Duration.seconds(delay));
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setToValue(1.0);
        ft.setDelay(Duration.seconds(delay));
        new ParallelTransition(tt, ft).play();
    }

    private void animateExit(Pane node, Runnable callback) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setToX(0);
        FadeTransition ft = new FadeTransition(Duration.millis(200), node);
        ft.setToValue(0);

        ParallelTransition pt = new ParallelTransition(st, ft);
        pt.setOnFinished(e -> callback.run());
        pt.play();
    }

}
