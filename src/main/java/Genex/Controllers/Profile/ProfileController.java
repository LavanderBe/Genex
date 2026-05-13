package Genex.Controllers.Profile;

import Genex.Controllers.Avatar.AvatarController;
import Genex.entities.Game;
import Genex.entities.Player;
import Genex.entities.User;
import Genex.services.CrudGame;
import Genex.services.CrudPlayer;
import Genex.services.CrudPlayer_Game;
import Genex.services.CrudUser;
import Genex.utils.SessionManager;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfileController {

    @FXML private Button btnPromote;
    @FXML private StackPane avatarFrame;
    @FXML private TextField cinField;
    @FXML private TextField cityField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label displayRole;
    @FXML private Label displayUsername;
    @FXML private Label usernameLabel;
    @FXML private DatePicker dobPicker;
    @FXML private TextField emailField;
    @FXML private FlowPane gamesFlowPane;
    @FXML private TextField natField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private ImageView profileAvatar;
    @FXML private TextField pseudoField;
    @FXML private Label statusLabel;
    @FXML private ScrollPane mainPane;
    @FXML private AnchorPane maincard;
    @FXML private VBox dataShard;
    @FXML private StackPane gameOverlay;
    @FXML private VBox selectionContainer;

    private final CrudPlayer_Game cpg=new CrudPlayer_Game();
    private final CrudPlayer cp=new CrudPlayer();
    private final CrudUser cu=new CrudUser();
    private final CrudGame cg=new CrudGame();
    private final String oldcin=SessionManager.getInstance().getCurrentPlayer().getCin();
    private String temporaryAvatarUrl=SessionManager.getInstance().getCurrentPlayer().getAvatar_url();
    private List<CheckBox> checkBoxes = new ArrayList<>();

    public void initialize() {
        Player current = SessionManager.getInstance().getCurrentPlayer();
        if (current != null) {
            displayUsername.setText(current.getNickname().toUpperCase());
            usernameLabel.setText(current.getUsername());
            nomField.setText(current.getNom());
            prenomField.setText(current.getPrenom());
            cinField.setText(current.getCin());
            emailField.setText(current.getEmail());
            natField.setText(current.getNationality());
            cityField.setText(current.getCity());
            pseudoField.setText(current.getNickname());
            displayRole.setText("ROLE : "+current.getRole().toUpperCase());
            dobPicker.setValue(current.getBirthday());
            if (current.getAvatar_url() != null) {
                profileAvatar.setImage(new Image(current.getAvatar_url(), true));
            }
            for (Game g : current.getGames_played()) {
                Label tag = new Label("[ " + g.getNom() + " ]");
                tag.getStyleClass().add("game-data-tag");
                gamesFlowPane.getChildren().add(tag);
            }
            if (cp.isPromotionSent(current)){
                btnPromote.setDisable(true);
                btnPromote.setText("DEMANDE_EN_COUR_DE_TRAITEMENT");
            }
            setupAgeRestriction();
            cinField.setTextFormatter(new TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.matches("([0-9]*)?") && newText.length() <= 8)
                {
                    return change;
                }
                return null;
            }));

        }
    }

    @FXML void handleSaveProfile(ActionEvent event) {
        statusLabel.setVisible(false);
        Player current = SessionManager.getInstance().getCurrentPlayer();
        String newNom= nomField.getText();
        String newPrenom= prenomField.getText();
        String newEmail = emailField.getText().trim();
        String newPseudo = pseudoField.getText().trim();
        String newCin = cinField.getText().trim();
        String newCity= cityField.getText();
        String newnat= natField.getText();
        String newPwd = newPasswordField.getText();
        String confirmPwd = confirmPasswordField.getText();
        LocalDate dob = dobPicker.getValue();

        if (newEmail.isEmpty() || newPseudo.isEmpty() || newCin.isEmpty() || dob == null||newNom.isEmpty()||newPrenom.isEmpty() ||newCity.isEmpty() ||newnat.isEmpty()) {
            showStatus("ERREUR_CRITIQUE: TOUT LES CHAMPS SONT REQUIS", true);
            return;
        }
        if (dob.isAfter(LocalDate.now().minusYears(13))) {
            showStatus("ACCESS_REFUSE: AGE MINMUM EST DE 13 ANS", true);
            return;
        }
        if (!newEmail.equals(current.getEmail()) && cu.check_email(newEmail)) {
            showStatus("LINK_ERROR: EMAIL_ALREADY_REGISTERED", true);
            return;
        }
        if (!newPseudo.equals(current.getNickname()) && cp.check_nickname_exists(newPseudo)) {
            showStatus("LINK_ERROR: PSEUDO_ALREADY_IN_USE", true);
            return;
        }

        if (!newPwd.isEmpty()&& !confirmPwd.isEmpty()) {
            if (!newPwd.equals(confirmPwd)) {
                showStatus("SECURITY_ERROR: KEYS_DO_NOT_MATCH", true);
                return;
            }
            current.updatepassword(newPwd);
        }
        else {
            User u=cu.getUser_withId(current.getId());
            current.setPassword_hash(u.getPassword_hash());
            current.setSalt((u.getSalt()));
        }

        current.setNom(nomField.getText().trim());
        current.setPrenom(prenomField.getText().trim());
        current.setNickname(newPseudo);
        current.setEmail(newEmail);
        current.setCin(newCin);
        current.setBirthday(dob);
        current.setNationality(natField.getText().trim());
        current.setCity(cityField.getText().trim());
        current.setAvatar_url(temporaryAvatarUrl);

        statusLabel.setText(">> MODIFICATION......");
        statusLabel.setTextFill(Color.web("#5C7CFA"));
        statusLabel.setVisible(true);

        new Thread(() -> {
            cu.updateEntity(current,current.getId());
            cp.updateEntity(current,oldcin);

            Platform.runLater(() -> {
                if (true) {
                    displayUsername.setText(current.getNickname().toUpperCase());
                    usernameLabel.setText(current.getUsername());
                    nomField.setText(current.getNom());
                    prenomField.setText(current.getPrenom());
                    cinField.setText(current.getCin());
                    emailField.setText(current.getEmail());
                    natField.setText(current.getNationality());
                    cityField.setText(current.getCity());
                    pseudoField.setText(current.getNickname());
                    displayRole.setText("ROLE : "+current.getRole().toUpperCase());
                    dobPicker.setValue(current.getBirthday());
                    showStatus(">> MODIFICATION SAUVEGARDER", false);
                    playSuccessGlitch();
                } else {
                    showStatus("DATABASE_WRITE_FAILURE", true);
                }
            });
        }).start();
    }

    @FXML
    void requestCoachPromotion(ActionEvent event) {
        Player current = SessionManager.getInstance().getCurrentPlayer();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("SYSTEM_ACCESS_UPGRADE");
        alert.setHeaderText("INITIATION // PROTOCOLE_DE_PROMOTION");
        alert.setContentText("Attention : Demande d'accès de niveau supérieur (COACH). \n" +
                "Cette demande sera examinée par les administrateurs du terminal. \n" +
                "Procéder à la liaison neurale ?");
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/Fxml/Dashboard/alert_style.css").toExternalForm());
        dialogPane.getStyleClass().add("cyber-alert");
        SVGPath icon = new SVGPath();
        icon.setContent("M12 2L4.5 20.29l.71.71L12 18l6.79 3 .71-.71L12 2z");
        icon.setFill(Color.web("#ffbb33")); // Gold icon for promotion
        alert.setGraphic(icon);
        Button okBtn = (Button) dialogPane.lookupButton(ButtonType.OK);
        okBtn.setText("ENVOYER");
        Button cancelBtn = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelBtn.setText("ABANDONNER");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            sendRequestToDatabase(current);
        }
    }

    @FXML
    private void handleUploadAvatar(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("GENEX // LINK_LOCAL_ASSET");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            this.temporaryAvatarUrl = selectedFile.toURI().toString();
            applyNewAvatarToUI(this.temporaryAvatarUrl);
        }
    }

    @FXML
    private void handleOpenAvatarCreator(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Player/Avatar/Avatar.fxml"));
            Parent root = loader.load();
            AvatarController controller = loader.getController();
            StackPane overlay = new StackPane(root);
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.8);");
            maincard.getChildren().add(overlay);
            controller.setAvatarListener(generatedUrl -> {
                this.temporaryAvatarUrl = generatedUrl;
                applyNewAvatarToUI(generatedUrl);

                maincard.getChildren().remove(overlay);
            });
            controller.setOnSaveCallback(() -> {
                maincard.getChildren().remove(overlay);
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleEditGames(ActionEvent event) {
        toggleGameOverlay();
        loadSelectionList();
    }


    private void applyNewAvatarToUI(String url) {
        Image img = new Image(url, true);
        FadeTransition glitch = new FadeTransition(Duration.millis(100), profileAvatar);
        glitch.setFromValue(1.0);
        glitch.setToValue(0.3);
        glitch.setCycleCount(4);
        glitch.setAutoReverse(true);

        img.progressProperty().addListener((obs, oldV, newV) -> {
            if (newV.doubleValue() == 1.0) {
                profileAvatar.setImage(img);
                glitch.play();
            }
        });
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(">> " + message.toUpperCase());
        statusLabel.setVisible(true);
        if (isError) {
            statusLabel.setTextFill(Color.web("#FF5252"));
            shakeNode(dataShard);
        } else {
            statusLabel.setTextFill(Color.web("#22c55e"));
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

    private void sendRequestToDatabase(Player player) {
        new Thread(() -> {
            boolean success = cp.sendPromotionRequest(player);
            javafx.application.Platform.runLater(() -> {
                if (success) {
                    btnPromote.setDisable(true);
                    btnPromote.setText("DEMANDE_EN_COUR_DE_TRAITEMENT");
                } else {
                    showStatus("DATABASE_WRITE_FAILURE: LINK_REJECTED", true);
                }
            });
        }).start();
    }

    @FXML
    private void toggleGameOverlay() {
        boolean isVisible = gameOverlay.isVisible();
        gameOverlay.setVisible(!isVisible);
        gameOverlay.setManaged(!isVisible);
    }

    private void loadSelectionList() {
        selectionContainer.getChildren().clear();
        checkBoxes.clear();

        Player current = SessionManager.getInstance().getCurrentPlayer();
        List<Game> allGames = cg.getgames(); // Get everything from DB
        List<Game> playerGames = cpg.get_GamesPlayed(current); // Get current links

        for (Game g : allGames) {
            CheckBox cb = new CheckBox(g.getNom().toUpperCase());
            cb.getStyleClass().add("game-checkbox");
            cb.setUserData(g); // Store the object
            if (playerGames.stream().anyMatch(pg -> pg.getId() == g.getId())) {
                cb.setSelected(true);
            }

            checkBoxes.add(cb);
            selectionContainer.getChildren().add(cb);
        }
    }

    @FXML
    private void handleSaveGameLinks() {
        Player current = SessionManager.getInstance().getCurrentPlayer();
        cpg.deleteAllGames_ForPlayer(current);
        for (CheckBox cb : checkBoxes) {
            if (cb.isSelected()) {
                Game g = (Game) cb.getUserData();
                cpg.addEntity(current, g);
            }
        }
        refreshGameTags(current);
        showStatus("NEURAL_LINKS_SYNCHRONIZED", false);
        toggleGameOverlay();
    }

    private void refreshGameTags(Player p) {
        gamesFlowPane.getChildren().clear();
        List<Game> updatedList = cpg.get_GamesPlayed(p);
        SessionManager.getInstance().getCurrentPlayer().setGames_played(updatedList);
        for (Game g : updatedList) {
            Label tag = new Label("[ " + g.getNom().toUpperCase() + " ]");
            tag.getStyleClass().add("game-data-tag");
            gamesFlowPane.getChildren().add(tag);
        }
    }

    private void setupAgeRestriction() {
        LocalDate maxAllowedDate = LocalDate.now().minusYears(13);
        dobPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isAfter(maxAllowedDate)) {
                    setDisable(true);
                    setStyle("-fx-background-color: #1a0505; -fx-text-fill: #444444;");
                }
            }
        });
    }

    private void playSuccessGlitch() {
        // Make the card "flash" blue briefly to signal success
        FadeTransition ft = new FadeTransition(Duration.millis(100), dataShard);
        ft.setFromValue(1.0);
        ft.setToValue(0.7);
        ft.setCycleCount(4);
        ft.setAutoReverse(true);
        ft.play();
    }
}
