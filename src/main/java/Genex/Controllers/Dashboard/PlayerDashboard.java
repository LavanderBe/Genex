package Genex.Controllers.Dashboard;

import Genex.entities.Player;
import Genex.entities.User;
import Genex.services.CrudPlayer;
import Genex.services.CrudUser;
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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;

public class PlayerDashboard {

    @FXML private Label RoleLabel;
    @FXML private StackPane avatarContainer;
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
                User u=new CrudUser().getUser_withmail("maxime@max.co");
                Player p=new CrudPlayer().getPlayerInfo(u.getId());
                SessionManager.getInstance().setCurrentUser(p);
        sessionUser.setText(SessionManager.getInstance().getCurrentUser().getUsername().toUpperCase());
        RoleLabel.setText(SessionManager.getInstance().getCurrentPlayer().getRole().toUpperCase());
        setupAvatar();
        startPingAnimation();
        startPingService();
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
        setActiveNav(navProfileButton);
        loadModule("/Fxml/Profile/Profile.fxml");
        setupAvatar();
    }

    @FXML private void showMain() {
        setActiveNav(navMainButton);
        contentArea.getChildren().clear();
        setupAvatar();
    }

    @FXML private void showTeams() {
        setActiveNav(navTeamsButton);
        loadModule("PlayerTeams.fxml");
        setupAvatar();
    }

    @FXML private void showTournaments() {
        setActiveNav(navTournamentsButton);
        loadModule("PlayerTournaments.fxml");
        setupAvatar();
    }

    @FXML private void showTutorials() {
        setActiveNav(navTutorialsButton);
        loadModule("/Fxml/Player/PlayerHub.fxml");
        setupAvatar();
    }

    @FXML private void showForums() {
        setActiveNav(navForumsButton);
        loadModule("/Fxml/Forum/Forum.fxml");
        setupAvatar();
    }

    @FXML private void showProfile() {
        setActiveNav(navProfileButton);
        loadModule("/Fxml/Profile/Profile.fxml");
        setupAvatar();
    }

    @FXML private void showBoutique() {
        setActiveNav(navBoutiqueButton);
        loadModule("Boutique.fxml");
        setupAvatar();
    }

    private void loadModule(String fxmlPath) {
        try {
            Parent module = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(module);
            AnchorPane.setTopAnchor(module, 0.0);
            AnchorPane.setBottomAnchor(module, 0.0);
            AnchorPane.setLeftAnchor(module, 0.0);
            AnchorPane.setRightAnchor(module, 0.0);
            FadeTransition ft = new FadeTransition(Duration.millis(300), module);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (IOException e) {
            System.err.println("Module not found: " + fxmlPath);
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

    private void setupAvatar(){


        String url=SessionManager.getInstance().getCurrentPlayer().getAvatar_url();
        if (url != null && !url.isEmpty()) {
            ImageView iv = new ImageView(new Image(url, true));
            iv.setFitHeight(28);
            iv.setFitWidth(28);
            iv.setPreserveRatio(true);
            avatarContainer.getChildren().clear();
            avatarContainer.getChildren().add(iv);
        } else {
            Label initial = new Label(SessionManager.getInstance().getCurrentUser().getUsername().substring(0, 1).toUpperCase());
            initial.setTextFill(Color.WHITE);
            initial.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 14px;");
            avatarContainer.getChildren().clear();
            avatarContainer.getChildren().add(initial);
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
