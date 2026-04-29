package Genex.Controllers.Main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.text.Text;
import javafx.animation.TranslateTransition;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

public class MainController {

    @FXML
    private StackPane contentContainer;

    @FXML
    private StackPane notificationPanelContainer;

    @FXML
    private StackPane notificationBackdrop;

    @FXML
    private TextField searchField;

    @FXML
    private Button btnTournaments;

    @FXML
    private Button btnFinances;

    @FXML
    private Button btnCommunity;

    @FXML
    private Button btnTutorials;

    @FXML
    private Button btnCenters;

    @FXML
    private Button btnNotification;

    @FXML
    private StackPane notificationBadge;

    @FXML
    private Text notificationCount;

    private Parent notificationPanel;
    private NotificationPanelController notificationPanelController;
    private boolean isNotificationPanelOpen = false;

    @FXML
    public void initialize() {
        System.out.println("MainController initialized");

        // Load notification panel
        loadNotificationPanel();

        // Don't load any page by default - just show empty content area
        System.out.println("Main interface ready");
    }

    private void loadNotificationPanel() {
        try {
            System.out.println("=== Loading notification panel ===");
            System.out.println("Container exists: " + (notificationPanelContainer != null));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Main/NotificationPanel.fxml"));
            notificationPanel = loader.load();
            notificationPanelController = loader.getController();

            System.out.println("Notification panel FXML loaded successfully");
            System.out.println("Panel object: " + (notificationPanel != null));
            System.out.println("Controller: " + (notificationPanelController != null));

            // Set close callback
            notificationPanelController.setOnCloseCallback(this::closeNotificationPanel);

            // Add to container
            notificationPanelContainer.getChildren().add(notificationPanel);

            System.out.println("Panel added to container. Children count: " + notificationPanelContainer.getChildren().size());

            // Make it not block mouse events when closed
            notificationPanelContainer.setPickOnBounds(false);
            notificationPanel.setPickOnBounds(true);

            System.out.println("Panel container translateX: " + notificationPanelContainer.getTranslateX());
            System.out.println("Panel visible: " + notificationPanel.isVisible());
            System.out.println("=== Notification panel loaded successfully ===");
        } catch (Exception e) {
            System.err.println("=== ERROR loading notification panel ===");
            e.printStackTrace();
        }
    }

    @FXML
    private void loadTournaments() {
        System.out.println("Loading Tournaments...");
        animateButtonClick(btnTournaments);
        setActiveButton(btnTournaments);
        // Load the Tournament Hub page
        loadPage("/Fxml/Tournament/TournamentHub.fxml");
    }

    @FXML
    private void loadFinances() {
        System.out.println("Loading Finances...");
        animateButtonClick(btnFinances);
        setActiveButton(btnFinances);
        // Don't load any page - just activate the button
        System.out.println("Finances section - No page loaded yet");
    }

    @FXML
    private void loadCommunity() {
        System.out.println("Loading Community...");
        animateButtonClick(btnCommunity);
        setActiveButton(btnCommunity);
        // Don't load any page - just activate the button
        System.out.println("Community section - No page loaded yet");
    }

    @FXML
    private void loadTutorials() {
        System.out.println("Loading Tutorials...");
        animateButtonClick(btnTutorials);
        setActiveButton(btnTutorials);
        // Don't load any page - just activate the button
        System.out.println("Tutorials section - No page loaded yet");
    }

    @FXML
    private void loadCenters() {
        System.out.println("Loading Centers...");
        animateButtonClick(btnCenters);
        setActiveButton(btnCenters);
        // Load the Center Hub page
        loadPage("/Fxml/Center/CenterHub.fxml");
    }

    @FXML
    private void handleNotificationClick() {
        System.out.println("=== Notification button clicked ===");
        System.out.println("Current panel state - isOpen: " + isNotificationPanelOpen);
        System.out.println("Panel container: " + (notificationPanelContainer != null ? "exists" : "null"));
        System.out.println("Panel: " + (notificationPanel != null ? "exists" : "null"));
        System.out.println("Backdrop: " + (notificationBackdrop != null ? "exists" : "null"));

        // Add a simple scale animation to show the button was clicked
        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(Duration.millis(100), btnNotification);
        scale.setToX(0.9);
        scale.setToY(0.9);
        scale.setCycleCount(2);
        scale.setAutoReverse(true);
        scale.play();

        if (isNotificationPanelOpen) {
            System.out.println("Closing notification panel...");
            closeNotificationPanel();
        } else {
            System.out.println("Opening notification panel...");
            openNotificationPanel();
        }
    }

    @FXML
    private void closeNotificationPanel() {
        if (!isNotificationPanelOpen || notificationPanelContainer == null) return;

        System.out.println("Closing notification panel...");

        // Hide backdrop
        if (notificationBackdrop != null) {
            notificationBackdrop.setVisible(false);
            notificationBackdrop.setManaged(false);
        }

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), notificationPanelContainer);
        slideOut.setToX(280);

        slideOut.setOnFinished(e -> {
            isNotificationPanelOpen = false;
            notificationPanelContainer.setPickOnBounds(false);
            System.out.println("Panel closed!");
        });
        slideOut.play();
    }

    private void openNotificationPanel() {
        System.out.println("=== openNotificationPanel called ===");

        if (isNotificationPanelOpen) {
            System.out.println("Panel already open, skipping");
            return;
        }

        if (notificationPanelContainer == null) {
            System.err.println("ERROR: notificationPanelContainer is null!");
            return;
        }

        if (notificationPanel == null) {
            System.err.println("ERROR: notificationPanel is null!");
            return;
        }

        System.out.println("Starting slide-in animation...");
        System.out.println("Current translateX: " + notificationPanelContainer.getTranslateX());
        System.out.println("Panel visible: " + notificationPanel.isVisible());
        System.out.println("Container visible: " + notificationPanelContainer.isVisible());

        // Show backdrop
        if (notificationBackdrop != null) {
            notificationBackdrop.setVisible(true);
            notificationBackdrop.setManaged(true);
            System.out.println("Backdrop shown");
        } else {
            System.err.println("WARNING: notificationBackdrop is null!");
        }

        notificationPanelContainer.setPickOnBounds(true);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), notificationPanelContainer);
        slideIn.setToX(0);

        slideIn.setOnFinished(e -> {
            isNotificationPanelOpen = true;
            System.out.println("=== Panel opened! Final translateX: " + notificationPanelContainer.getTranslateX() + " ===");
        });

        System.out.println("Playing animation...");
        slideIn.play();

        // Update badge
        updateNotificationBadge();
    }

    private void updateNotificationBadge() {
        if (notificationPanelController != null) {
            int unreadCount = notificationPanelController.getUnreadCount();
            if (unreadCount > 0) {
                notificationCount.setText(String.valueOf(unreadCount));
                notificationBadge.setVisible(true);
            } else {
                notificationBadge.setVisible(false);
            }
        }
    }

    private void animateButtonClick(Button button) {
        // Skip animation if it's the notification button to avoid conflicts
        if (button == btnNotification) {
            return;
        }

        TranslateTransition transition = new TranslateTransition(Duration.millis(100), button);
        transition.setFromX(0);
        transition.setToX(5);
        transition.setCycleCount(2);
        transition.setAutoReverse(true);
        transition.play();
    }

    private void loadPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent page = loader.load();

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(page);
            System.out.println("Successfully loaded: " + fxmlPath);
        } catch (Exception e) {
            System.err.println("Error loading page: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeButton) {
        btnTournaments.getStyleClass().remove("sidebar-button-active");
        btnFinances.getStyleClass().remove("sidebar-button-active");
        btnCommunity.getStyleClass().remove("sidebar-button-active");
        btnTutorials.getStyleClass().remove("sidebar-button-active");
        btnCenters.getStyleClass().remove("sidebar-button-active");

        if (!activeButton.getStyleClass().contains("sidebar-button-active")) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }

        updateIconStyles();
    }

    private void updateIconStyles() {
        resetIconStyle(btnTournaments);
        resetIconStyle(btnFinances);
        resetIconStyle(btnCommunity);
        resetIconStyle(btnTutorials);
        resetIconStyle(btnCenters);

        if (btnTournaments.getStyleClass().contains("sidebar-button-active")) {
            setActiveIconStyle(btnTournaments);
        } else if (btnFinances.getStyleClass().contains("sidebar-button-active")) {
            setActiveIconStyle(btnFinances);
        } else if (btnCommunity.getStyleClass().contains("sidebar-button-active")) {
            setActiveIconStyle(btnCommunity);
        } else if (btnTutorials.getStyleClass().contains("sidebar-button-active")) {
            setActiveIconStyle(btnTutorials);
        } else if (btnCenters.getStyleClass().contains("sidebar-button-active")) {
            setActiveIconStyle(btnCenters);
        }
    }

    private void resetIconStyle(Button button) {
        if (button.getGraphic() != null) {
            button.getGraphic().getStyleClass().remove("nav-icon-active");
            if (!button.getGraphic().getStyleClass().contains("nav-icon")) {
                button.getGraphic().getStyleClass().add("nav-icon");
            }
        }
    }

    private void setActiveIconStyle(Button button) {
        if (button.getGraphic() != null) {
            button.getGraphic().getStyleClass().remove("nav-icon");
            if (!button.getGraphic().getStyleClass().contains("nav-icon-active")) {
                button.getGraphic().getStyleClass().add("nav-icon-active");
            }
        }
    }
}
