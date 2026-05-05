package Genex.Controllers.Dashboard;

//import Genex.services.UserSession;
import Genex.utils.PingService;
import Genex.utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class PlayerDashboard {

    @FXML private BorderPane mainPane;
    @FXML private VBox sidebarContainer;
    @FXML private AnchorPane contentArea;
    @FXML private Label sessionUser;
    @FXML private Circle pingDot;
    @FXML private Label pingLabel;

    private boolean isSidebarVisible = true;

    private PingService pingService;

    @FXML
    public void initialize() {
        sessionUser.setText(SessionManager.getInstance().getCurrentUser().getUsername().toUpperCase());
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

    @FXML
    private void toggleSidebar() {
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sidebarContainer);

        if (isSidebarVisible) {
            slide.setToX(-180);
            slide.setOnFinished(e -> {
                mainPane.setLeft(null);
                isSidebarVisible = false;
            });
        } else {
            // Slide In
            mainPane.setLeft(sidebarContainer);
            sidebarContainer.setTranslateX(-180);
            slide.setToX(0);
            isSidebarVisible = true;
        }
        slide.play();
    }


    //============================== PING SERVICE =========================================


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

    private void startPingAnimation() {
        FadeTransition fade = new FadeTransition(Duration.seconds(1), pingDot);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }

    //======================================================================================

    @FXML
    private void handleProfileClick() {
        System.out.println("Switching to Profile Module...");
        showProfile();
    }

    @FXML private void showMain() { loadModule("PlayerMain.fxml"); }
    @FXML private void showTeams() { loadModule("PlayerTeams.fxml"); }
    @FXML private void showTournaments() { loadModule("PlayerTournaments.fxml"); }
    @FXML private void showTutorials() { loadModule("PlayerTutorials.fxml"); }
    @FXML private void showForums() { loadModule("PlayerForums.fxml"); }
    @FXML private void showProfile() { loadModule("PlayerProfile.fxml"); }
    @FXML private void showBoutique() {loadModule("Boutique.fxml");}

    private void loadModule(String fxmlName) {
        try {
            Parent module = FXMLLoader.load(getClass().getResource(fxmlName));
            contentArea.getChildren().setAll(module);
        } catch (IOException e) {
            System.err.println("Module not found: " + fxmlName);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        stopPingService();
        SessionManager.getInstance().logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Login/Login.fxml"));
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}