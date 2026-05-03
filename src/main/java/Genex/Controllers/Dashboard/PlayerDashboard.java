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
    
    // Navigation buttons
    @FXML private Button btnMain;
    @FXML private Button btnTeams;
    @FXML private Button btnTournaments;
    @FXML private Button btnTutorials;
    @FXML private Button btnForums;
    @FXML private Button btnBoutique;
    @FXML private Button btnProfile;

    private boolean isSidebarVisible = true;

    @FXML
    public void initialize() {
        //gotta make a usersessionclass and replace the profile topleft thing with the current user
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
        showProfile();
    }

    @FXML private void showMain() { 
        setActiveButton(btnMain);
        loadModule("PlayerMain.fxml"); 
    }
    
    @FXML private void showTeams() { 
        setActiveButton(btnTeams);
        loadModule("PlayerTeams.fxml"); 
    }
    
    @FXML private void showTournaments() { 
        setActiveButton(btnTournaments);
        loadModule("/Fxml/Tournament/TournamentHub.fxml"); 
    }
    
    @FXML private void showTutorials() { 
        setActiveButton(btnTutorials);
        loadModule("PlayerTutorials.fxml"); 
    }
    
    @FXML private void showForums() { 
        setActiveButton(btnForums);
        loadModule("PlayerForums.fxml"); 
    }
    
    @FXML private void showProfile() { 
        setActiveButton(btnProfile);
        loadModule("PlayerProfile.fxml"); 
    }
    
    @FXML private void showBoutique() {
        setActiveButton(btnBoutique);
        loadModule("Boutique.fxml");
    }

    private void loadModule(String fxmlName) {
        try {
            Parent module = FXMLLoader.load(getClass().getResource(fxmlName));
            
            // Set anchor constraints to make the module fill the entire content area
            AnchorPane.setTopAnchor(module, 0.0);
            AnchorPane.setRightAnchor(module, 0.0);
            AnchorPane.setBottomAnchor(module, 0.0);
            AnchorPane.setLeftAnchor(module, 0.0);
            
            contentArea.getChildren().setAll(module);
        } catch (IOException e) {
            System.err.println("Module not found: " + fxmlName);
            e.printStackTrace();
        }
    }
    
    private void setActiveButton(Button clickedButton) {
        // Remove active class from all navigation buttons and ensure nav-button class is present
        if (btnMain != null) {
            btnMain.getStyleClass().removeAll("nav-button-active");
            if (!btnMain.getStyleClass().contains("nav-button")) {
                btnMain.getStyleClass().add("nav-button");
            }
        }
        if (btnTeams != null) {
            btnTeams.getStyleClass().removeAll("nav-button-active");
            if (!btnTeams.getStyleClass().contains("nav-button")) {
                btnTeams.getStyleClass().add("nav-button");
            }
        }
        if (btnTournaments != null) {
            btnTournaments.getStyleClass().removeAll("nav-button-active");
            if (!btnTournaments.getStyleClass().contains("nav-button")) {
                btnTournaments.getStyleClass().add("nav-button");
            }
        }
        if (btnTutorials != null) {
            btnTutorials.getStyleClass().removeAll("nav-button-active");
            if (!btnTutorials.getStyleClass().contains("nav-button")) {
                btnTutorials.getStyleClass().add("nav-button");
            }
        }
        if (btnForums != null) {
            btnForums.getStyleClass().removeAll("nav-button-active");
            if (!btnForums.getStyleClass().contains("nav-button")) {
                btnForums.getStyleClass().add("nav-button");
            }
        }
        if (btnBoutique != null) {
            btnBoutique.getStyleClass().removeAll("nav-button-active");
            if (!btnBoutique.getStyleClass().contains("nav-button")) {
                btnBoutique.getStyleClass().add("nav-button");
            }
        }
        if (btnProfile != null) {
            btnProfile.getStyleClass().removeAll("nav-button-active");
            if (!btnProfile.getStyleClass().contains("nav-button")) {
                btnProfile.getStyleClass().add("nav-button");
            }
        }
        
        // Add active class to clicked button
        if (clickedButton != null && !clickedButton.getStyleClass().contains("nav-button-active")) {
            clickedButton.getStyleClass().add("nav-button-active");
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