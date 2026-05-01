package Genex.Controllers.Dashboard;

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
        setActiveButton((Button) event.getSource());
        loadView("/Fxml/Player/Player.fxml");
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
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, "Error loading view: " + fxmlPath, e);
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
