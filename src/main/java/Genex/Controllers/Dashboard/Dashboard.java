package Genex.Controllers.Dashboard;

import Genex.Controllers.Team.TeamHubController;
import Genex.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Dashboard {

    // ==================== FXML Fields ====================
    @FXML
    private AnchorPane contentArea;

    @FXML
    private BorderPane mainPane;

    @FXML
    private VBox sidebarVbox;


    // ==================== Navigation Methods ====================
    @FXML
    private void navDashboardBtn(ActionEvent event){
        setActiveButton((Button) event.getSource());
    }

    @FXML
    void navPlayerBtn(ActionEvent event) {
        try {
            setActiveButton((Button) event.getSource());
            // 1. Load the new FXML file
            // Note: Adjust the path if your FXML files are in a different folder
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Player/Player.fxml"));

            // 2. Clear the existing content
            contentArea.getChildren().clear();

            // 3. Add the new content
            contentArea.getChildren().add(root);

            // 4. Ensure the new content scales to fit the AnchorPane
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error loading FXML: ");
        }
    }

    @FXML
    void navTeamBtn(ActionEvent event) {
        try {
            setActiveButton((Button) event.getSource());
            
            // Load the Team Hub FXML with loader to get controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Team/TeamHub.fxml"));
            Parent root = loader.load();
            
            // Get the controller and pass the contentArea as container
            TeamHubController teamHubController = loader.getController();
            teamHubController.setContentContainer(contentArea);

            // Clear the existing content
            contentArea.getChildren().clear();

            // Add the new content
            contentArea.getChildren().add(root);

            // Ensure the new content scales to fit the AnchorPane
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error loading Team Hub FXML: " + ex.getMessage());
        }
    }


    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent newView = loader.load();
            contentArea.getChildren().setAll(newView);

        } catch (IOException e) {
            e.printStackTrace();
            // You can show an error alert here later
            System.err.println("Error loading view: " + fxmlPath);
        }
    }



    private void setActiveButton(Button clickedButton) {
        // Loop through all nodes in the VBox
        for (Node node : sidebarVbox.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;

                // Remove the active class and ensure default class is present
                btn.getStyleClass().remove("nav-button-active");
                if (!btn.getStyleClass().contains("nav-button")) {
                    btn.getStyleClass().add("nav-button");
                }
            }
        }

        // Apply active class to the clicked button
        clickedButton.getStyleClass().remove("nav-button");
        clickedButton.getStyleClass().add("nav-button-active");
    }


    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Clear the session
            SessionManager.getInstance().logout();
            System.out.println("User logged out");
            
            Parent root = FXMLLoader.load(getClass().getResource("/Fxml/Login/Login.fxml"));
            Scene scene = new Scene(root);

            // Get the current stage
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