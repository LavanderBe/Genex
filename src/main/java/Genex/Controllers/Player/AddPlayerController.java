package Genex.Controllers.Player;

import Genex.entities.Game;
import Genex.entities.Player;
import Genex.services.*;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AddPlayerController {

    @FXML private AnchorPane card;
    @FXML private TextField prenomField, nomField, pseudoField, cinField, natField, cityField, accountNameField, emailField;
    @FXML private PasswordField passwordField;
    @FXML private DatePicker dobPicker;
    @FXML private VBox gamesToggleContainer;


    @FXML private Label SystemError;
    @FXML private Label formTitle;
    @FXML private Button btnExecute;
    @FXML private Label Identification_Error;

    private Runnable onCloseCallback;
    private final CrudUser crudUser=new CrudUser();
    private final CrudGame cg = new CrudGame();
    private final CrudPlayer cp = new CrudPlayer();
    private final CrudPlayer_Game cpg =new CrudPlayer_Game();
    private List<CheckBox> gameCheckboxes = new ArrayList<>();
    private Player playerToEdit;
    private boolean isEditMode = false;


    public void setPlayerData(Player player) {
        this.playerToEdit = player;
        this.isEditMode = true;

        // Fill Personal Data
        prenomField.setText(player.getPrenom());
        nomField.setText(player.getNom());
        pseudoField.setText(player.getNickname());
        cinField.setText(player.getCin());
        dobPicker.setValue(player.getBirthday());
        natField.setText(player.getNationality());
        cityField.setText(player.getCity());

        // Fill System Data
        accountNameField.setText(player.getUsername());
        emailField.setText(player.getEmail());
        // Password stays empty for security; only update if typed

        // 2. EDGY VISUAL UPDATES
        formTitle.setText("RECALIBRATE // PLAYER_ENTITY");
        formTitle.setTextFill(javafx.scene.paint.Color.web("#8B0D0D")); // Red for Edit
        btnExecute.setText("UPDATE_PROTOCOL");

        // 3. AUTO-SELECT GAMES
        // Assuming player.getPlayedGames() returns a list of Game names or IDs
        List<Game> lg=cpg.get_GamesPlayed(player);
        List<String> nameOf_ThePlayedGames=new ArrayList<>();
        for (Game g:lg){
            nameOf_ThePlayedGames.add(g.getNom());
        }
        for (CheckBox cb : gameCheckboxes) {
            if (nameOf_ThePlayedGames.contains(cb.getUserData())) {
                cb.setSelected(true);
            }
        }
    }

    @FXML
    public void initialize() {
        loadAvailableGames();
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

    private void loadAvailableGames() {
        gamesToggleContainer.getChildren().clear();
        List<Game> games = cg.getgames();

        for (Game game : games) {
            CheckBox cb = new CheckBox(game.getNom().toUpperCase());
            cb.getStyleClass().add("game-checkbox");
            cb.setUserData(game.getNom());
            gameCheckboxes.add(cb);
            gamesToggleContainer.getChildren().add(cb);
        }
    }

    @FXML
    private void handleSave(ActionEvent event) {
        clearErrors();

        List<String> selectedGameNames = new ArrayList<>();
        for (CheckBox cb : gameCheckboxes) {
            if (cb.isSelected()) {
                selectedGameNames.add((String) cb.getUserData());
            }
        }


        String username=accountNameField.getText();
        String email=emailField.getText();
        String pwd=passwordField.getText();


        String nom=nomField.getText();
        String prenom=prenomField.getText();
        String pseudo=pseudoField.getText();
        String cin=cinField.getText();
        String nat=natField.getText();
        String ville=cityField.getText();
        LocalDate dob = dobPicker.getValue();
        LocalDate limit = LocalDate.now().minusYears(13);
        if (isEditMode)
        {
            accountNameField.setDisable(true);



            isEditMode=false;
            if (onCloseCallback != null) {
                onCloseCallback.run();
                accountNameField.setDisable(false);
            }
        }
        else{
        if (username.isEmpty()|| !UserControl.isValidUsername(username)){
            showError(SystemError,"NOM DU COMPTE INVALIDE");
            shakeNode(card);
            return;
        }

        if (email.isEmpty() || !UserControl.isValidEmail(email)){
            showError(SystemError,"EMAIL INVALIDE");
            shakeNode(card);
            return;
        }

        if (pwd.isEmpty()){
            showError(SystemError,"MOT DE PASSE INVALDE");
            shakeNode(card);
            return;
        }

        if (crudUser.check_email(email)){
            showError(SystemError,"EMAIL DEJA PRIS");
            shakeNode(card);
            return;
        }

        if (crudUser.check_Username(username)){
            showError(SystemError,"NOM DU COMPTE DEJA PRIS");
            shakeNode(card);
            return;
        }

        if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty() || dob == null||nat.isEmpty()||ville.isEmpty()){
            showError(Identification_Error,"TOUT LES CHAMPS SONT REQUIS");
            shakeNode(card);
            return;
        }

        if (cin.length()<8||cp.check_cin_exists(cin)){
            showError(Identification_Error,"CIN INVALIDE");
            shakeNode(card);
            return;
        }

        if (cp.check_nickname_exists(pseudo)){
            showError(Identification_Error,"PSEUDO INVALIDE");
            shakeNode(card);
            return;
        }

        // used to catch if the user manually types the birthday :p
        if (dob == null || dob.isAfter(limit)) {
            showError(Identification_Error,"LE JOUEUR DOIT AVOIR 13 ANS!!!!");
            shakeNode(card); // Visual feedback
            return;
        }

        Player p=new Player(username,email,pwd,"player",prenom,nom,pseudo,cin,dob,nat,ville);
        cp.addPlayer_admin(p);
        p.setId(crudUser.getUser_Id(p.getUsername()));
        for (String name:selectedGameNames){
            Game g=cg.getGameByName(name);
            cpg.addEntity(p,g);
        }

        if (onCloseCallback != null) onCloseCallback.run();
        }
    }

    @FXML
    private void handleCancel() {
        if (onCloseCallback != null) onCloseCallback.run();
        onCloseCallback.run();
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    void handleUploadPic(ActionEvent event) {

    }

    @FXML
    void handleCreateAvatar(ActionEvent event) {

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

    private void clearErrors(){
        Identification_Error.setText("");
        SystemError.setText("");
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
        label.setTextFill(Color.web("#c60000"));
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

}