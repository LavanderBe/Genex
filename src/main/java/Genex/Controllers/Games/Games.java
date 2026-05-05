package Genex.Controllers.Games;

import Genex.entities.Game;
import Genex.services.CrudGame;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class Games {

    @FXML private FlowPane gamesGrid;
    @FXML private StackPane addGameOverlay;

    // Form Fields
    @FXML private TextField nomField, genreField;
    @FXML private ComboBox<String> platformCombo, modeCombo;
    @FXML private TextField maxPlayersField;
    @FXML private Label iconPathLabel;
    @FXML private ImageView iconPreview;
    @FXML private Label placeholderLabel;
    @FXML private Label errorLabelAdd;

    private String selectedImagePath = "";
    private final CrudGame cg=new CrudGame();

    private Game selectedGame;
    private boolean isEditMode = false;

    @FXML public void initialize() {
        platformCombo.getItems().addAll("PC", "Console", "Mobile", "Cross-Platform");
        modeCombo.getItems().addAll("Solo", "Team");
        refreshGrid();
        maxPlayersField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("([0-9]*)?") && newText.length() <= 2) {
                return change;
            }
            return null;
        }));
    }

    @FXML private void handleChooseIcon() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            placeholderLabel.setText("");
            selectedImagePath = selectedFile.toURI().toString();
            iconPathLabel.setText(selectedFile.getName());
            iconPreview.setImage(new Image(selectedImagePath));
        }
    }

    @FXML private void handleSaveGame() {
        if (isEditMode){
            if (nomField.getText().isEmpty() || selectedImagePath.isEmpty() || maxPlayersField.getText().isEmpty()||platformCombo.getValue()==null|| modeCombo.getValue()==null) {
                errorLabelAdd.setText("Champs Vides !!!!");
                errorLabelAdd.setTextFill(Color.web("8b0d0d"));
                shakeNode(addGameOverlay);
                return;
            }
            if (maxPlayersField.getText().equals("0") || maxPlayersField.getText().equals("00")) {
                errorLabelAdd.setText("Nombre De Joueuers Invalide");
                errorLabelAdd.setTextFill(Color.web("8b0d0d"));
                shakeNode(addGameOverlay);
                return;
            }
            cg.updateEntity(new Game(nomField.getText(),genreField.getText(),platformCombo.getValue(),modeCombo.getValue(),Integer.parseInt(maxPlayersField.getText()),selectedImagePath),nomField.getText());
            isEditMode=false;
            closeOverlay();
            refreshGrid();
        }
        else {
            if (cg.Nameexists(nomField.getText())) {
                errorLabelAdd.setText("Ce Jeu Existe Déja !!!");
                errorLabelAdd.setTextFill(Color.web("8b0d0d"));
                shakeNode(addGameOverlay);
                return;
            }
            if (nomField.getText().isEmpty() || selectedImagePath.isEmpty() || maxPlayersField.getText().isEmpty()||platformCombo.getValue()==null|| modeCombo.getValue()==null) {
                errorLabelAdd.setText("Champs Vides !!!!");
                errorLabelAdd.setTextFill(Color.web("8b0d0d"));
                shakeNode(addGameOverlay);
                return;
            }
            if (maxPlayersField.getText().equals("0") || maxPlayersField.getText().equals("00")) {
                errorLabelAdd.setText("Nombre De Joueuers Invalide");
                errorLabelAdd.setTextFill(Color.web("8b0d0d"));
                shakeNode(addGameOverlay);
                return;
            }

            Game g=new Game(nomField.getText(),genreField.getText(),platformCombo.getValue(),modeCombo.getValue(),Integer.parseInt(maxPlayersField.getText()),selectedImagePath);
            cg.addEntity(g);

            closeOverlay();
            refreshGrid();
        }
    }

    @FXML private void openAddOverlay() {
        addGameOverlay.setVisible(true);
        addGameOverlay.setManaged(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), addGameOverlay);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    @FXML private void closeOverlay() {
        FadeTransition ft = new FadeTransition(Duration.millis(300), addGameOverlay);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        // after lighting up the scene u gotta hide don't hide before the dim :))
        ft.setOnFinished(event -> {
            addGameOverlay.setVisible(false);
            addGameOverlay.setManaged(false);
            addGameOverlay.setOpacity(1.0);
            clearFields();
        });
        ft.play();
    }

    private void clearFields() {
        nomField.setDisable(false);
        nomField.clear();
        genreField.clear();
        selectedImagePath = "";
        maxPlayersField.setText("");
        iconPreview.setImage(null);
        modeCombo.setValue(null);
        platformCombo.setValue(null);
        placeholderLabel.setText("Pas D'image");
        iconPathLabel.setText("WAITING_FOR_DATA....");
        errorLabelAdd.setText("");
    }

    private void refreshGrid() {
        gamesGrid.getChildren().clear();
        List<Game> lg=cg.getgames();
        for (Game g: lg){
            displayGame(g);
        }
    }

    private void displayGame(Game game) {
        VBox card = new VBox();
        card.getStyleClass().add("game-data-plate");
        card.setPrefWidth(280);
        card.setSpacing(10);
        AnchorPane imageContainer = new AnchorPane();
        imageContainer.setPrefHeight(140);
        imageContainer.getStyleClass().add("card-image-box");

        ImageView iv = new ImageView();
        try {
            iv.setImage(new Image(game.getIcon_url()));
        } catch (Exception e) {
            //chay no need to panic :))
        }
        iv.setFitWidth(278);
        iv.setFitHeight(138);
        iv.setPreserveRatio(false);
        Region corner = new Region();
        corner.getStyleClass().add("card-hud-corner");
        AnchorPane.setTopAnchor(corner, -1.0);
        AnchorPane.setLeftAnchor(corner, -1.0);
        imageContainer.getChildren().addAll(iv, corner);
        VBox infoBox = new VBox(5);
        infoBox.setPadding(new Insets(10, 15, 15, 15));
        Label nameLabel = new Label(game.getNom().toUpperCase());
        nameLabel.getStyleClass().add("card-name");
        GridPane specs = new GridPane();
        specs.setHgap(10);
        specs.setVgap(5);
        addSpecRow(specs, 0, "GENRE:", game.getGenre());
        addSpecRow(specs, 1, "PLATFORM:", game.getPlatforme());
        addSpecRow(specs, 2, "MODE:", game.getMode());
        addSpecRow(specs, 3, "CAPACITY:", game.getMax_team_player() + " Players");
        HBox actions = new HBox(10);
        actions.setPadding(new Insets(10, 0, 0, 0));
        Button editBtn = new Button("MODIFY");
        editBtn.getStyleClass().add("card-btn-confirm");
        Button deleteBtn = new Button("TERMINATE");
        deleteBtn.getStyleClass().add("card-btn-cancel");
        actions.getChildren().addAll(editBtn, deleteBtn);
        infoBox.getChildren().addAll(nameLabel, specs, actions);
        card.getChildren().addAll(imageContainer, infoBox);
        gamesGrid.getChildren().add(card);
        deleteBtn.setOnAction(e -> handleTerminate(game, card));
        editBtn.setOnAction(e -> handleEdit(game));
    }

    private void addSpecRow(GridPane grid, int row, String label, String value) {
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #444466; -fx-font-family: 'Consolas'; -fx-font-size: 10px;");
        Label v = new Label(value.toUpperCase());
        v.setStyle("-fx-text-fill: #5C7CFA; -fx-font-family: 'Consolas'; -fx-font-size: 10px; -fx-font-weight: bold;");
        grid.add(l, 0, row);
        grid.add(v, 1, row);
    }

    private void handleTerminate(Game game, VBox cardNode) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("SYSTEM_WARNING");
        alert.setHeaderText("PURGE ENTITY: " + game.getNom().toUpperCase());
        alert.setContentText("Link termination is irreversible. Proceed?");
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
            cg.deleteEntity(game.getNom());
            ScaleTransition st = new ScaleTransition(Duration.millis(300), cardNode);
            st.setToX(0.2);
            st.setToY(0.2);
            FadeTransition ft = new FadeTransition(Duration.millis(400), cardNode);
            ft.setToValue(0);
            ParallelTransition pt = new ParallelTransition(st, ft);
            pt.setOnFinished(e -> gamesGrid.getChildren().remove(cardNode));
            pt.play();
        }
    }

    private void handleEdit(Game game) {
        this.selectedGame = game;
        this.isEditMode = true;
        nomField.setText(game.getNom());
        nomField.setDisable(true);
        genreField.setText(game.getGenre());
        platformCombo.setValue(game.getPlatforme());
        modeCombo.setValue(game.getMode());
        maxPlayersField.setText(String.valueOf(game.getMax_team_player()));
        iconPreview.setImage(new Image(game.getIcon_url()));
        selectedImagePath=game.getIcon_url();
        iconPathLabel.setText(game.getNom()+" Image");
        placeholderLabel.setText("");
        ((Label) addGameOverlay.lookup(".TITLE")).setText("RECALIBRATION_JEU");
        openAddOverlay();
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
}
