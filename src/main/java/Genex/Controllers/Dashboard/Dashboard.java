package Genex.Controllers.Dashboard;

import Genex.utils.PingService;
import Genex.utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dashboard {

    // ==================== FXML Fields ====================
    @FXML private AnchorPane contentArea;
    @FXML private BorderPane mainPane;
    @FXML private VBox       sidebarVbox;
    @FXML private Circle pingDot;
    @FXML private Label sessionUser;
    @FXML private Label pingLabel;

    /** ComboBox inside the Finance content area for page selection */
    @FXML private ComboBox<String> financePageSelector;

    private PingService pingService;


    @FXML
    public void initialize() {
        //sessionUser.setText(SessionManager.getInstance().getCurrentUser().getUsername());
        startPingAnimation();
        startPingService();
        // hook cleanup to window close
        pingLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obs2, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.setOnCloseRequest(e -> stopPingService());
                    }
                });
            }
        });
    }

    private void startPingAnimation() {
        FadeTransition fade = new FadeTransition(Duration.seconds(1), pingDot);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }

    private void startPingService() {
        pingService = new PingService();

        pingService.setOnSucceeded(e -> {
            long ping = (long) e.getSource().getValue();
            Platform.runLater(() -> updatePingLabel(ping));
        });

        pingService.setOnFailed(e -> {
            System.out.println("[PingService] Task failed : "
                    + e.getSource().getException().getMessage());
            Platform.runLater(() -> {
                pingLabel.setText("Ping: N/A");
                pingLabel.setStyle("-fx-text-fill: gray;");
            });
        });

        pingService.start();
        System.out.println("[PingService] Service started.");
    }

    private void updatePingLabel(long ping) {
        if (ping == -1) {
            pingLabel.setText("Ping: Timeout");
            pingLabel.setStyle("-fx-text-fill: red;");
            System.out.println("[PingService] Ping timeout.");
            return;
        }

        String color;
        String quality;

        if (ping < 50) {
            color = "#00ff00"; // green
            quality = "EXCELLENT";
        } else if (ping < 100) {
            color = "#ffff00"; // yellow
            quality = "GOOD";
        } else if (ping < 150) {
            color = "#ff8800"; // orange
            quality = "FAIR";
        } else {
            color = "#ff0000"; // red
            quality = "POOR";
        }
        pingDot.setFill(Color.web(color));
        pingLabel.setText("Ping: " + ping + " ms");
        pingLabel.setStyle("-fx-text-fill: " + color + ";");
        System.out.println("[PingService] Ping: " + ping + " ms - " + quality);
    }

    public void stopPingService() {
        if (pingService != null && pingService.isRunning()) {
            pingService.cancel();
            pingService = null;
            System.out.println("[PingService] Service stopped.");
        }
    }

    // ==================== Navigation Methods ====================

    @FXML
    private void navDashboardBtn(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        restoreDefaultContent();
    }
    private void loadModule(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node node = loader.load();
            contentArea.getChildren().clear();
            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);
            contentArea.getChildren().add(node);
            FadeTransition ft = new FadeTransition(Duration.millis(300), node);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void navAccountBtn (ActionEvent event){
        setActiveButton((Button) event.getSource());
        loadModule("/Fxml/Accounts/Accounts.fxml");
    }

    @FXML
    void navPlayerBtn(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadModule("/Fxml/Player/Player.fxml");
    }

    @FXML
    void navGamesBtn(ActionEvent event){
        setActiveButton((Button) event.getSource());
        loadModule("/Fxml/Games/Games_admin.fxml");
    }

    /**
     * Called when the FINANCES sidebar button is clicked.
     * Loads the Finance hub (ComboBox + sub-view) into the content area.
     */
    @FXML
    private void navFinancesBtn(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("/Fxml/Finance/Finance.fxml");
    }

    // ==================== Helpers ====================

    private void restoreDefaultContent() {
        // Reload the default dashboard overview
        loadView("/Fxml/Dashboard/DashboardHome.fxml");
    }

    @FXML
    void navTournamentBtn(ActionEvent event) {
        try {
            setActiveButton((Button) event.getSource());
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Tournament/TournamentHub.fxml"));

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error loading Tournament Hub: " + ex.getMessage());
        }
    }

    @FXML
    void navCenterBtn(ActionEvent event) {
        try {
            setActiveButton((Button) event.getSource());
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Center/CenterHub.fxml"));

            contentArea.getChildren().clear();
            contentArea.getChildren().add(root);

            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error loading Center Hub: " + ex.getMessage());
        }
    }
    @FXML
    private void navForumBtn(ActionEvent event) {
        setActiveButton((Button) event.getSource());
        loadView("/Fxml/Forum/Forum.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newView = loader.load();
            contentArea.getChildren().setAll(newView);
            AnchorPane.setTopAnchor(newView, 0.0);
            AnchorPane.setBottomAnchor(newView, 0.0);
            AnchorPane.setLeftAnchor(newView, 0.0);
            AnchorPane.setRightAnchor(newView, 0.0);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxmlPath);
        }
    }

    private void setActiveButton(Button clickedButton) {
        for (Node node : sidebarVbox.getChildren()) {
            if (node instanceof Button btn) {
                btn.getStyleClass().remove("nav-button-active");
                if (!btn.getStyleClass().contains("nav-button"))
                    btn.getStyleClass().add("nav-button");
            }
        }
        clickedButton.getStyleClass().remove("nav-button");
        clickedButton.getStyleClass().add("nav-button-active");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            stopPingService();
            SessionManager.getInstance().logout();
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Login/Login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Login screen: " + e.getMessage());
        }
    }
}
