package Genex.Controllers.Dashboard;

//import Genex.services.UserSession;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        // 1. Set User from Session
       // if (UserSession.getInstance() != null) {
       //     sessionUser.setText(UserSession.getInstance().getUser().getNom().toUpperCase());
        //}

        // 2. Start Ping Pulse Animation
        startPingAnimation();
    }

    @FXML
    private void toggleSidebar() {
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), sidebarContainer);

        if (isSidebarVisible) {
            // Slide Out
            slide.setToX(-180);
            slide.setOnFinished(e -> {
                mainPane.setLeft(null); // Remove from layout so center expands
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
        showProfile();
    }

    // --- NAVIGATION LOGIC ---
    @FXML private void showMain() { loadModule("PlayerMain.fxml"); }
    @FXML private void showTeams() { loadModule("PlayerTeams.fxml"); }
    @FXML private void showTournaments() { loadModule("PlayerTournaments.fxml"); }
    @FXML private void showTutorials() { loadModule("PlayerTutorials.fxml"); }
    @FXML private void showForums() { loadModule("PlayerForums.fxml"); }
    @FXML private void showProfile() { loadModule("PlayerProfile.fxml"); }

    private void loadModule(String fxmlName) {
        try {
            // This replaces the content in the center area
            Parent module = FXMLLoader.load(getClass().getResource("/Fxml/Dashboard/Modules/" + fxmlName));
            contentArea.getChildren().setAll(module);
        } catch (IOException e) {
            System.err.println("Module not found: " + fxmlName);
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
       // UserSession.getInstance().cleanUserSession();
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