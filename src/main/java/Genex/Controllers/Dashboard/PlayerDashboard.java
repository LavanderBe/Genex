package Genex.Controllers.Dashboard;

import Genex.entities.User;
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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.net.URL;

public class PlayerDashboard {

    @FXML private BorderPane mainPane;
    @FXML private VBox sidebarContainer;
    @FXML private AnchorPane contentArea;
    @FXML private Label sessionUser;
    @FXML private Circle pingDot;
    @FXML private Label pingLabel;

    @FXML private Button navMainButton;
    @FXML private Button navTeamsButton;
    @FXML private Button navTournamentsButton;
    @FXML private Button navTutorialsButton;
    @FXML private Button navForumsButton;
    @FXML private Button navBoutiqueButton;
    @FXML private Button navProfileButton;

    private boolean isSidebarVisible = true;

    private PingService pingService;

    @FXML
    public void initialize() {
        sessionUser.setText(SessionManager.getInstance().getCurrentUser().getUsername().toUpperCase());
        startPingAnimation();
        startPingService();
        checkTrainingNotifications();
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
    private void checkTrainingNotifications() {
        javafx.application.Platform.runLater(() -> {
            try {
                System.out.println("=== CHECKING FOR TRAINING NOTIFICATIONS (PLAYER) ===");
                // Show Windows system notifications
                Genex.utils.TrainingNotificationHelper.checkAndShowNotifications();
                System.out.println("=== NOTIFICATION CHECK COMPLETE (PLAYER) ===");
            } catch (Exception e) {
                System.err.println("Error checking notifications in PlayerDashboard: " + e.getMessage());
                e.printStackTrace();
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
        loadModule("PlayerProfile.fxml");
    }

    @FXML private void showMain() {
        setActiveNav(navMainButton);
        contentArea.getChildren().clear();
    }

    @FXML private void showTeams() {
        setActiveNav(navTeamsButton);
        loadModule("/Fxml/Team/PlayerTeams.fxml");
    }

    @FXML private void showTournaments() {
        setActiveNav(navTournamentsButton);
        loadModule("/Fxml/Tournament/TournamentHub.fxml");
    }

    @FXML private void showTutorials() {
        setActiveNav(navTutorialsButton);
        loadModule("/Fxml/Player/PlayerHub.fxml");
    }

    @FXML private void showForums() {
        setActiveNav(navForumsButton);
        loadModule("/Fxml/Forum/Forum.fxml");
    }

    @FXML private void showProfile() {
        setActiveNav(navProfileButton);
        loadModule("PlayerProfile.fxml");
    }

    @FXML private void showBoutique() {
        setActiveNav(navBoutiqueButton);
        loadModule("/Fxml/Boutique/Boutique.fxml");
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

    private void showModuleError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERREUR DE CHARGEMENT DU MODULE");
        alert.setHeaderText("Impossible d'ouvrir le module sélectionné");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setActiveNav(Button activeButton) {
        Button[] buttons = {
                navMainButton, navTeamsButton, navTournamentsButton,
                navTutorialsButton, navForumsButton, navBoutiqueButton, navProfileButton
        };
        for (Button button : buttons) {
            if (button == null) {
                continue;
            }
            button.getStyleClass().removeAll("nav-button", "nav-button-active");
            button.getStyleClass().add(button == activeButton ? "nav-button-active" : "nav-button");
        }
    }

    private void activateSidebarButton(ActionEvent event) {
        if (event.getSource() instanceof Button clickedButton) {
            for (Node node : sidebarContainer.lookupAll(".nav-button, .nav-button-active")) {
                if (node instanceof Button btn) {
                    btn.getStyleClass().remove("nav-button-active");
                    if (!btn.getStyleClass().contains("nav-button")) {
                        btn.getStyleClass().add("nav-button");
                    }
                }
            }
            clickedButton.getStyleClass().remove("nav-button");
            if (!clickedButton.getStyleClass().contains("nav-button-active")) {
                clickedButton.getStyleClass().add("nav-button-active");
            }
        }
    }

    private void loadAbsoluteModule(String fxmlPath) {
        try {
            Parent module = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(module);
            AnchorPane.setTopAnchor(module, 0.0);
            AnchorPane.setBottomAnchor(module, 0.0);
            AnchorPane.setLeftAnchor(module, 0.0);
            AnchorPane.setRightAnchor(module, 0.0);
        } catch (IOException e) {
            System.err.println("Module not found: " + fxmlPath);
        }
    }

    private void loadModuleAbsolute(String absolutePath) {
        try {
            Parent module = FXMLLoader.load(getClass().getResource(absolutePath));
            contentArea.getChildren().setAll(module);
            // Anchor to fill the content area
            javafx.scene.layout.AnchorPane.setTopAnchor(module, 0.0);
            javafx.scene.layout.AnchorPane.setBottomAnchor(module, 0.0);
            javafx.scene.layout.AnchorPane.setLeftAnchor(module, 0.0);
            javafx.scene.layout.AnchorPane.setRightAnchor(module, 0.0);
        } catch (IOException e) {
            System.err.println("Module not found: " + absolutePath);
            e.printStackTrace();
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
