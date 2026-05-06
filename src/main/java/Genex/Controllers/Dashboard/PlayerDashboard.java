package Genex.Controllers.Dashboard;

import Genex.entities.User;
import Genex.utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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

    private boolean isSidebarVisible = true;

    @FXML
    public void initialize() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && sessionUser != null) {
            sessionUser.setText(currentUser.getUsername());
        }
        startPingAnimation();
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

    private void startPingAnimation() {
        FadeTransition fade = new FadeTransition(Duration.seconds(1), pingDot);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }

    @FXML
    private void handleProfileClick() {
        System.out.println("Switching to Profile Module...");
        loadModule("PlayerProfile.fxml");
    }

    @FXML private void showMain(ActionEvent event) { activateSidebarButton(event); loadModule("PlayerMain.fxml"); }
    @FXML private void showTeams(ActionEvent event) { activateSidebarButton(event); loadModule("PlayerTeams.fxml"); }
    @FXML private void showTournaments(ActionEvent event) { activateSidebarButton(event); loadModule("PlayerTournaments.fxml"); }
    @FXML private void showTutorials(ActionEvent event) { activateSidebarButton(event); loadModule("PlayerTutorials.fxml"); }
    @FXML private void showForums(ActionEvent event) { activateSidebarButton(event); loadAbsoluteModule("/Fxml/Forum/Forum.fxml"); }
    @FXML private void showProfile(ActionEvent event) { activateSidebarButton(event); loadModule("PlayerProfile.fxml"); }
    @FXML private void showBoutique(ActionEvent event) { activateSidebarButton(event); loadModule("Boutique.fxml"); }

    private void loadModule(String fxmlName) {
        try {
            Parent module = FXMLLoader.load(getClass().getResource(fxmlName));
            contentArea.getChildren().setAll(module);
            AnchorPane.setTopAnchor(module, 0.0);
            AnchorPane.setBottomAnchor(module, 0.0);
            AnchorPane.setLeftAnchor(module, 0.0);
            AnchorPane.setRightAnchor(module, 0.0);
        } catch (IOException e) {
            System.err.println("Module not found: " + fxmlName);
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

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            SessionManager.getInstance().logout();
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Login/Login.fxml"));
            Stage stage = (Stage) mainPane.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
