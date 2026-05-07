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
        //gotta make a usersessionclass and replace the profile topleft thing with the current user
        startPingAnimation();
        
        // Check for training notifications
        checkTrainingNotifications();
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

    @FXML private void showMain() { loadModule("PlayerMain.fxml"); }
    @FXML private void showTeams() { loadModuleAbsolute("/Fxml/Team/PlayerTeams.fxml"); }
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
        //gotta make a user session class for better security will work on that later on
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