package Genex.Controllers.Dashboard;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Dashboard implements Initializable {

    // ==================== FXML Fields ====================
    @FXML private StackPane rootPane;
    @FXML
    private StackPane contentArea;

    @FXML
    private VBox dashboardContent;

    // Sidebar Buttons
    @FXML
    private Button navDashboard;
    @FXML
    private Button navUsers;
    @FXML
    private Button navGames;
    @FXML
    private Button navPlayers;
    @FXML
    private Button navTeams;
    @FXML
    private Button navTournaments;
    @FXML
    private Button navForum;
    @FXML
    private Button navtutorials;
    @FXML
    private Button navFinance;
    @FXML
    private Button navAccount;

    // ==================== Initialization ====================

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setActiveButton(navDashboard);
    }

    // ==================== Navigation Methods ====================

    @FXML
    private void navDashboardBtn(ActionEvent event) {
        setActiveButton(navDashboard);
        contentArea.getChildren().setAll(dashboardContent);
    }

    @FXML
    private void navPlayersBtn(ActionEvent event) {
        setActiveButton(navPlayers);
        loadView("/Fxml/Player/Player.fxml");
    }

    @FXML
    private void navUsersBtn(ActionEvent event) {
        setActiveButton(navUsers);
        loadView("/Genex/Views/Users/users.fxml");
    }

    @FXML
    private void navGamesBtn(ActionEvent event) {
        setActiveButton(navGames);
        loadView("/Genex/Views/Games/games.fxml");
    }

    @FXML
    private void navTeamsBtn(ActionEvent event) {
        setActiveButton(navTeams);
        loadView("/Genex/Views/Teams/teams.fxml");
    }

    @FXML
    private void navTournamentsBtn(ActionEvent event) {
        setActiveButton(navTournaments);
        loadView("/Genex/Views/Tournaments/tournaments.fxml");
    }

    @FXML
    private void navForumBtn(ActionEvent event) {
        setActiveButton(navForum);
        loadView("/Genex/Views/Forum/forum.fxml");
    }

    @FXML
    private void navtutorialsBtn(ActionEvent event) {
        setActiveButton(navtutorials);
        loadView("/Genex/Views/Tutorials/tutorials.fxml");
    }

    @FXML
    private void navFinanceBtn(ActionEvent event) {
        setActiveButton(navFinance);
        loadView("/Genex/Views/Finance/finance.fxml");
    }

    @FXML
    private void navAccountBtn(ActionEvent event) {
        setActiveButton(navAccount);
        loadView("/Genex/Views/Account/account.fxml");
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

    private void setActiveButton(Button activeButton) {
        // Remove active style from all buttons
        navDashboard.getStyleClass().remove("nav-active");
        navUsers.getStyleClass().remove("nav-active");
        navGames.getStyleClass().remove("nav-active");
        navPlayers.getStyleClass().remove("nav-active");
        navTeams.getStyleClass().remove("nav-active");
        navTournaments.getStyleClass().remove("nav-active");
        navForum.getStyleClass().remove("nav-active");
        navtutorials.getStyleClass().remove("nav-active");
        navFinance.getStyleClass().remove("nav-active");
        navAccount.getStyleClass().remove("nav-active");

        // Add active style to the selected button
        if (activeButton != null) {
            activeButton.getStyleClass().add("nav-active");
        }
    }


    @FXML
    private void logout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Login/Login.fxml"));
            Parent loginRoot = loader.load();

            // Get the current stage
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene loginScene = new Scene(loginRoot);
            loginScene.setFill(Color.TRANSPARENT);   // if you're using transparent style

            stage.setScene(loginScene);
            stage.setMaximized(true);
            stage.centerOnScreen();           // Optional: nice touch
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Login screen: " + e.getMessage());
        }
    }

    // Optional: New Tournament button
    @FXML
    private void newtournement(ActionEvent event) {
        System.out.println("New Tournament clicked");
        // You can open a dialog or switch to tournament creation view
    }
}