package Genex.Controllers.Dashboard;

import Genex.utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
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

    /** ComboBox inside the Finance content area for page selection */
    @FXML private ComboBox<String> financePageSelector;



    @FXML
    public void initialize() {
        //gotta make a usersessionclass and replace the profile topleft thing with the current user
        startPingAnimation();
    }

    private void startPingAnimation() {
        FadeTransition fade = new FadeTransition(Duration.seconds(1), pingDot);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
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

        } catch (IOException e) {
            e.printStackTrace();
        }
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
