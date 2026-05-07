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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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
    @FXML private Button navMainButton;
    @FXML private Button navTeamsButton;
    @FXML private Button navTournamentsButton;
    @FXML private Button navTutorialsButton;
    @FXML private Button navForumsButton;
    @FXML private Button navBoutiqueButton;
    @FXML private Button navProfileButton;

    private boolean isSidebarVisible = true;

    @FXML
    public void initialize() {
        //gotta make a usersessionclass and replace the profile topleft thing with the current user
        startPingAnimation();
        showMain();
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
        setActiveNav(navMainButton);
        contentArea.getChildren().clear();
    }

    @FXML private void showTeams() {
        setActiveNav(navTeamsButton);
        loadModule("PlayerTeams.fxml");
    }

    @FXML private void showTournaments() {
        setActiveNav(navTournamentsButton);
        loadModule("PlayerTournaments.fxml");
    }

    @FXML private void showTutorials() {
        setActiveNav(navTutorialsButton);
        loadModule("/Fxml/Player/PlayerHub.fxml");
    }

    @FXML private void showForums() {
        setActiveNav(navForumsButton);
        loadModule("PlayerForums.fxml");
    }

    @FXML private void showProfile() {
        setActiveNav(navProfileButton);
        loadModule("PlayerProfile.fxml");
    }

    @FXML private void showBoutique() {
        setActiveNav(navBoutiqueButton);
        loadModule("Boutique.fxml");
    }

    private void loadModule(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                showModuleError("Module introuvable : " + fxmlPath);
                return;
            }
            Parent module = FXMLLoader.load(resource);
            contentArea.getChildren().setAll(module);
        } catch (Exception e) {
            showModuleError("Impossible de charger le module : " + fxmlPath + "\n" + e.getMessage());
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
