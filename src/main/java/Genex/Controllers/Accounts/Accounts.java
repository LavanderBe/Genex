package Genex.Controllers.Accounts;

import Genex.entities.User;
import Genex.services.CrudUser;
import Genex.services.UserControl;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Date;

public class Accounts {


    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, String> colUsername, colEmail, colRole;
    @FXML private TableColumn<User, Date> colDate;

    @FXML private Label sideUsername, hashLabel, saltLabel, uidLabel, secLevelLabel;
    @FXML private Label rankLabel, winRateLabel;

    @FXML private TextField searchField;
    @FXML private VBox detailsShard, playerDataBox;
    @FXML private Label statusLabel;

    @FXML private StackPane formOverlay;
    @FXML private AnchorPane formPanel;
    @FXML private Label formTitle;
    @FXML private TextField usernameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleCombo;

    private boolean isEditMode = false;

    private ObservableList<User> masterData = FXCollections.observableArrayList();
    private final CrudUser crudUser = new CrudUser();


    @FXML
    public void initialize() {
        setupTableColumns();
        loadUserData();
        setupSearchFilter();
        setupSelectionListener();
        setupRowFactory();
        roleCombo.getItems().addAll("ADMIN", "PLAYER");
    }

    private void setupTableColumns() {
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("created_at"));
    }

    private void loadUserData() {
        System.out.println(crudUser.SelectEntities());
        masterData.setAll(crudUser.SelectEntities());
        userTable.setItems(masterData);
    }

    private void setupSearchFilter() {
        FilteredList<User> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (user.getUsername().toLowerCase().contains(lowerCaseFilter)) return true;
                if (user.getEmail().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        userTable.setItems(filteredData);
    }

    private void setupSelectionListener() {
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // 1. Animate the panel (Glitch effect)
                detailsShard.setOpacity(0.5);
                FadeTransition ft = new FadeTransition(Duration.millis(1500), detailsShard);
                ft.setToValue(1.0);
                ft.play();

                // 2. Fill General Info
                User u=crudUser.getUser_withmail(newVal.getEmail());
                sideUsername.setText(u.getUsername());
                hashLabel.setText(u.getPassword_hash());
                uidLabel.setText((u.getId()).toUpperCase());

                // Assuming your User entity has getSalt() and getId()
                saltLabel.setText(u.getSalt());

                // 3. Security Level Logic
                if ("ADMIN".equals(u.getRole())) {
                    secLevelLabel.setText("LEVEL_MAX");
                    secLevelLabel.setTextFill(Color.web("#8B0D0D"));
                } else {
                    secLevelLabel.setText("LEVEL_STANDARD");
                    secLevelLabel.setTextFill(Color.web("#22c55e"));
                }

                // 4. Role-Based Visibility (Show extra stats for players)
                boolean isPlayer = "PLAYER".equals(u.getRole());
                playerDataBox.setVisible(isPlayer);
                playerDataBox.setManaged(isPlayer);

                if (isPlayer) {
                    // rankLabel.setText(newVal.getRank());
                }
            }
        });
    }

    private void setupRowFactory() {
        userTable.setRowFactory(tv -> new TableRow<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    getStyleClass().removeAll("row-admin", "row-player");
                } else {
                    if ("ADMIN".equalsIgnoreCase(item.getRole())) {
                        if (!getStyleClass().contains("row-admin")) getStyleClass().add("row-admin");
                        getStyleClass().remove("row-player");
                    } else {
                        if (!getStyleClass().contains("row-player")) getStyleClass().add("row-player");
                        getStyleClass().remove("row-admin");
                    }
                }
            }
        });
    }

    @FXML
    void handleAddUser(ActionEvent event) {
        isEditMode = false;
        formTitle.setText("INITIALISATION DE COMPTE");
        formTitle.setTextFill(Color.web("#5C7CFA")); // Blue for add
        clearFields();
        openForm();
    }

    @FXML
    void handleUpdate(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            isEditMode = true;
            formTitle.setText("RECALIBRATION");
            formTitle.setTextFill(Color.web("#8B0D0D")); // Red for update

            usernameField.setText(selected.getUsername());
            usernameField.setDisable(true);
            emailField.setText(selected.getEmail());
            roleCombo.setValue(selected.getRole());

            openForm();
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText("PURGE_PROTOCOL_REQUESTED");
            alert.setContentText("Proceed with permanent deletion of " + selected.getUsername() + "?");
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(getClass().getResource("/Fxml/Dashboard/alert_style.css").toExternalForm());
            dialogPane.getStyleClass().add("cyber-alert");
            alert.setGraphic(null);
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.getIcons().add(new Image("Images/logo.png"));
            SVGPath warningIcon = new SVGPath();
            warningIcon.setContent("M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z");
            warningIcon.setFill(Color.web("#8B0D0D"));
            warningIcon.setScaleX(2.0);
            warningIcon.setScaleY(2.0);
            alert.setGraphic(warningIcon);
            if (alert.showAndWait().get() == ButtonType.OK) {
                masterData.remove(selected);
                crudUser.deleteEntity(selected);
            }
        }
    }

    private void openForm() {
        formOverlay.setVisible(true);
        formOverlay.setManaged(true);
        double y=formPanel.getTranslateY();

        // Slide down animation
        formPanel.setTranslateY(-500); // Start off-screen
        TranslateTransition slide = new TranslateTransition(Duration.millis(400), formPanel);
        slide.setToY(y);
        slide.play();
    }

    @FXML
    private void closeForm() {
        double y=formPanel.getTranslateY();
        TranslateTransition slide = new TranslateTransition(Duration.millis(300), formPanel);
        slide.setToY(-500);
        slide.setOnFinished(e -> {
            formOverlay.setVisible(false);
            formOverlay.setManaged(false);
            clearFields();
            formPanel.setTranslateY(y);
        });
        slide.play();
        isEditMode=false;
    }

    @FXML
    private void handleSaveUser(){
        String username=usernameField.getText().trim();
        String password=passwordField.getText();
        String role=roleCombo.getValue();
        String email=emailField.getText().trim();
        if (isEditMode){
            if (username.isEmpty()||password.isEmpty()||role.isEmpty()||email.isEmpty()){
                shakeNode(formOverlay);
                return;
            }
            if (!UserControl.isValidEmail(email)){
                shakeNode(formOverlay);
                return;
            }
            if (crudUser.check_email(email)){
                shakeNode(formOverlay);
                return;
            }
            User u=new User(username,email,password,role);
            String id=crudUser.getUser_Id(username);
            crudUser.updateEntity(u,id);
            closeForm();
            loadUserData();
        }else{
            if (username.isEmpty()||password.isEmpty()||role.isEmpty()||email.isEmpty()){
                shakeNode(formOverlay);
                return;
            }
            if (!UserControl.isValidEmail(email)){
                shakeNode(formOverlay);
                return;
            }
            if (crudUser.check_email(email)||crudUser.check_Username(username)){
                shakeNode(formOverlay);
                return;
            }
            User u=new User(username,email,password,role);
            crudUser.addEntity(u);
            closeForm();
            loadUserData();
        }
    }

    private void shakeNode(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setAutoReverse(true);
        tt.setCycleCount(6);
        node.setRotate(0.5);
        tt.setOnFinished(e -> {
            node.setTranslateX(0);
            node.setRotate(0);});
        tt.play();
    }

    private void clearFields() {
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        roleCombo.setValue(null);
    }

}
