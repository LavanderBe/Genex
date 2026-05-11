package Genex.Controllers.Tournament;

import Genex.Server.LocalHttpServer;
import Genex.entities.Center;
import Genex.entities.Game;
import Genex.entities.TournamentParticipants;
import Genex.entities.Tounament;
import Genex.services.CrudCenter;
import Genex.services.CrudGame;
import Genex.services.CrudTournament;
import Genex.services.CrudTournamentMatch;
import Genex.services.CrudTournamentParticipant;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Popup;
import javafx.util.Duration;
import javafx.util.StringConverter;
import netscape.javascript.JSObject;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TournamentHubController {

    @FXML
    private StackPane rootStackPane;

    @FXML
    private VBox contentArea;

    @FXML
    private StackPane searchFieldContainer;

    @FXML
    private TextField searchField;

    @FXML
    private VBox suggestionsBox;
    
    private Popup suggestionsPopup;

    @FXML
    private ComboBox<Game> comboGameFilter;

    @FXML
    private Button btnAddTournament;

    @FXML
    private WebView centerMapWebView;

    @FXML
    private Label mapStatusLabel;

    @FXML
    private VBox tournamentCardsContainer;

    @FXML
    private VBox emptyState;

    // Player record section
    @FXML private VBox playerRecordSection;
    @FXML private Text txtTournamentsPlayed;
    @FXML private Text txtTotalWins;
    @FXML private Text txtTotalLosses;
    @FXML private Text txtWinRatio;
    @FXML private Text txtBestPlacement;
    @FXML private VBox participationHistoryList;
    @FXML private Label emptyParticipationHistory;

    private CrudTournament crudTournament;
    private CrudGame crudGame;
    private CrudCenter crudCenter;
    private CrudTournamentMatch crudMatch = new CrudTournamentMatch();
    private CrudTournamentParticipant crudParticipant = new CrudTournamentParticipant();
    private List<Tounament> allTournaments;
    private List<Center> allCenters;
    private boolean showOnlyMyTournaments = false;
    private boolean mapBridgeListenerRegistered = false;
    private boolean mapLoaded = false;
    private final MapBridge mapBridge = new MapBridge();
    private String pendingCentersJson = "[]";

    @FXML
    public void initialize() {
        System.out.println("TournamentHubController initialized");

        // Initialize CRUD services
        crudTournament = new CrudTournament();
        crudGame = new CrudGame();
        crudCenter = new CrudCenter();

        // Load map through the same local HTTP server principle used by captcha
        setupMapWebView();

        // Setup role-based UI
        setupRoleBasedUI();

        // Load games for filter
        loadGamesFilter();

        // Setup search listener
        setupSearchListener();

        // Setup game filter listener
        setupGameFilterListener();

        // Load tournaments from database
        loadTournamentsFromDatabase();
    }

    private void setupMapWebView() {
        if (centerMapWebView == null || mapBridgeListenerRegistered) {
            return;
        }

        try {
            LocalHttpServer.start();

            WebEngine engine = centerMapWebView.getEngine();
            engine.setOnError(event -> System.err.println("[Tournament Map JS Error] " + event.getMessage()));
            engine.setOnAlert(event -> System.out.println("[Tournament Map JS Alert] " + event.getData()));

            engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    mapLoaded = true;
                    JSObject window = (JSObject) engine.executeScript("window");
                    window.setMember("javaBridge", mapBridge);
                    pushCentersToMap();
                } else if (newState == Worker.State.FAILED) {
                    mapLoaded = false;
                    System.err.println("[Tournament Map] Failed to load local map page.");
                }
            });

            mapBridgeListenerRegistered = true;
            engine.load("http://localhost:7654/tournament-map.html");
        } catch (Exception e) {
            System.err.println("Error initializing tournament map WebView: " + e.getMessage());
        }
    }

    private void setupRoleBasedUI() {
        Genex.entities.User currentUser = Genex.utils.SessionManager.getInstance().getCurrentUser();
        
        if (currentUser != null && "player".equalsIgnoreCase(currentUser.getRole())) {
            btnAddTournament.setVisible(false);
            btnAddTournament.setManaged(false);
            comboGameFilter.setVisible(false);
            comboGameFilter.setManaged(false);
            // Show player record
            loadPlayerRecord(currentUser.getId());
        } else {
            btnAddTournament.setText("+ Nouveau Tournoi");
            btnAddTournament.setOnAction(event -> openAddTournamentModal());
            // Hide record for admin
            if (playerRecordSection != null) {
                playerRecordSection.setVisible(false);
                playerRecordSection.setManaged(false);
            }
        }
    }

    private void loadPlayerRecord(String playerId) {
        try {
            playerRecordSection.setVisible(true);
            playerRecordSection.setManaged(true);

            int tournamentsPlayed = crudMatch.getTotalTournamentsPlayed(playerId);
            int totalWins = crudMatch.getTotalWins(playerId);
            int totalLosses = crudMatch.getTotalLosses(playerId);
            Integer bestPlacement = crudMatch.getBestPlacement(playerId);

            int total = totalWins + totalLosses;
            int ratio = total > 0 ? (int) ((totalWins * 100.0) / total) : 0;

            txtTournamentsPlayed.setText(String.valueOf(tournamentsPlayed));
            txtTotalWins.setText(String.valueOf(totalWins));
            txtTotalLosses.setText(String.valueOf(totalLosses));
            txtWinRatio.setText(ratio + "%");

            if (bestPlacement != null) {
                String medal = bestPlacement == 1 ? "🥇 1er" :
                               bestPlacement == 2 ? "🥈 2ème" :
                               bestPlacement == 3 ? "🥉 3ème" :
                               bestPlacement + "ème";
                txtBestPlacement.setText(medal);
            } else {
                txtBestPlacement.setText("-");
            }
        } catch (Exception e) {
            System.err.println("Error loading player record: " + e.getMessage());
        }
    }

    private void loadParticipationHistory(String playerId) {
        if (participationHistoryList == null || emptyParticipationHistory == null) {
            return;
        }

        participationHistoryList.getChildren().removeIf(node -> node.getStyleClass().contains("history-row"));

        try {
            List<TournamentParticipants> history = crudParticipant.getPlayerHistory(playerId);
            emptyParticipationHistory.setVisible(history.isEmpty());
            emptyParticipationHistory.setManaged(history.isEmpty());

            for (TournamentParticipants participation : history) {
                Tounament tournament = findTournamentById(participation.getTournamentId());
                participationHistoryList.getChildren().add(createHistoryRow(participation, tournament));
            }
        } catch (Exception e) {
            emptyParticipationHistory.setText("Historique indisponible pour le moment.");
            emptyParticipationHistory.setVisible(true);
            emptyParticipationHistory.setManaged(true);
            System.err.println("Error loading participation history: " + e.getMessage());
        }
    }

    private HBox createHistoryRow(TournamentParticipants participation, Tounament tournament) {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("history-row");

        VBox info = new VBox(5);
        HBox.setHgrow(info, Priority.ALWAYS);

        Text name = new Text(tournament != null ? tournament.getTournamentName() : "Tournoi inconnu");
        name.getStyleClass().add("history-name");

        Text meta = new Text(buildHistoryMeta(participation, tournament));
        meta.getStyleClass().add("history-meta");

        info.getChildren().addAll(name, meta);

        StackPane placementBadge = new StackPane();
        placementBadge.getStyleClass().add("history-placement-badge");
        Text placement = new Text(buildPlacementText(participation));
        placement.getStyleClass().add("history-placement-text");
        placementBadge.getChildren().add(placement);

        row.getChildren().addAll(info, placementBadge);
        return row;
    }

    private String buildHistoryMeta(TournamentParticipants participation, Tounament tournament) {
        String state = participation.getStatus() != null ? participation.getStatus().name() : "INCONNU";
        if (participation.isWinner()) {
            state = "VAINQUEUR";
        } else if (participation.withdrewFromTournament()) {
            state = "RETIRE";
        } else if (participation.isEliminated()) {
            state = "ELIMINE";
        } else if (participation.isActive()) {
            state = "EN COMPETITION";
        }

        String date = "";
        if (tournament != null && tournament.getStarts_at() != null) {
            date = " | " + tournament.getStarts_at().format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH));
        }

        String round = participation.getEliminatedAtRound() != null
                ? " | Round " + participation.getEliminatedAtRound()
                : "";

        return state + date + round;
    }

    private String buildPlacementText(TournamentParticipants participation) {
        if (participation.getFinalPlacement() != null) {
            return "#" + participation.getFinalPlacement();
        }
        if (participation.isWinner()) {
            return "#1";
        }
        if (participation.isActive()) {
            return "...";
        }
        return "-";
    }

    private void toggleMyTournaments() {
        showOnlyMyTournaments = !showOnlyMyTournaments;
        
        if (showOnlyMyTournaments) {
            btnAddTournament.setText("Tous les Tournois");
            btnAddTournament.getStyleClass().add("active-filter");
        } else {
            btnAddTournament.setText("Mes Tournois");
            btnAddTournament.getStyleClass().remove("active-filter");
        }
        
        filterTournaments();
    }

    private void loadGamesFilter() {
        try {
            List<Game> games = crudGame.getgames();
            
            // Add "All Games" option
            Game allGamesOption = new Game();
            allGamesOption.setId("ALL");
            allGamesOption.setNom("Tous les jeux");
            
            comboGameFilter.getItems().add(allGamesOption);
            comboGameFilter.getItems().addAll(games);
            
            // Set default selection to "All Games"
            comboGameFilter.setValue(allGamesOption);

            // Set custom string converter to display game name
            comboGameFilter.setConverter(new StringConverter<Game>() {
                @Override
                public String toString(Game game) {
                    return game != null ? game.getNom() : "";
                }

                @Override
                public Game fromString(String string) {
                    return null;
                }
            });
        } catch (Exception e) {
            System.err.println("Error loading games for filter: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTournaments();
            showSuggestions(newValue);
        });
        
        // Hide suggestions when focus is lost
        searchField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && suggestionsPopup != null) {
                Platform.runLater(() -> {
                    if (suggestionsPopup != null && suggestionsPopup.isShowing()) {
                        suggestionsPopup.hide();
                    }
                });
            }
        });
    }

    private void setupGameFilterListener() {
        comboGameFilter.valueProperty().addListener((observable, oldValue, newValue) -> {
            filterTournaments();
        });
    }

    private void filterTournaments() {
        tournamentCardsContainer.getChildren().clear();

        String searchText = searchField.getText();
        Game selectedGame = comboGameFilter.getValue();

        List<Tounament> filtered = allTournaments.stream()
                .filter(t -> {
                    // Filter by search text - using startsWith for more precise filtering
                    boolean matchesSearch = searchText == null || searchText.trim().isEmpty() ||
                            t.getTournamentName().toLowerCase().startsWith(searchText.toLowerCase()) ||
                            t.getFormat().toLowerCase().startsWith(searchText.toLowerCase());

                    // Filter by game
                    boolean matchesGame = selectedGame == null || 
                            "ALL".equals(selectedGame.getId()) ||
                            (t.getGame_id() != null && t.getGame_id().equals(selectedGame.getId()));

                    // Filter by player's joined tournaments (if player and filter active)
                    boolean matchesMyTournaments = true;
                    if (showOnlyMyTournaments) {
                        Genex.entities.User currentUser = Genex.utils.SessionManager.getInstance().getCurrentUser();
                        if (currentUser != null) {
                            String playerId = currentUser.getId();
                            Genex.services.CrudTournamentParticipant crudParticipant = new Genex.services.CrudTournamentParticipant();
                            matchesMyTournaments = crudParticipant.isPlayerParticipating(t.getTournamentId(), playerId);
                        }
                    }

                    return matchesSearch && matchesGame && matchesMyTournaments;
                })
                .toList();

        displayTournaments(filtered);
    }

    private void updateEmptyState() {
        boolean isEmpty = tournamentCardsContainer.getChildren().isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);
    }

    @FXML
    private void openAddTournamentModal() {
        try {
            System.out.println("Opening Add Tournament Drawer...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tournament/AddTournamentModal.fxml"));
            StackPane drawerOverlay = loader.load();

            // Add drawer overlay to the stack
            rootStackPane.getChildren().add(drawerOverlay);

            // Get controller and set callbacks
            AddTournamentModalController controller = loader.getController();
            controller.setOnSaveCallback(tournament -> {
                System.out.println("Saving tournament: " + tournament.getTournamentName());

                // Save to database
                crudTournament.addEntity(tournament);

                // Remove drawer overlay and reload
                rootStackPane.getChildren().remove(drawerOverlay);
                loadTournamentsFromDatabase();
            });

            // Handle close without saving
            controller.setOnCloseCallback(() -> {
                rootStackPane.getChildren().remove(drawerOverlay);
            });

        } catch (Exception e) {
            System.err.println("Error opening Add Tournament Drawer");
            e.printStackTrace();
        }
    }

    private void loadTournamentsFromDatabase() {
        try {
            System.out.println("Loading tournaments from database...");

            // Get all tournaments from database
            allTournaments = crudTournament.getAll();
            allCenters = crudCenter.getAll();

            System.out.println("Loaded " + allTournaments.size() + " tournaments");

            // Apply current filters
            filterTournaments();
            renderCentersMap();

            Genex.entities.User currentUser = Genex.utils.SessionManager.getInstance().getCurrentUser();
            if (currentUser != null && "player".equalsIgnoreCase(currentUser.getRole())) {
                loadParticipationHistory(currentUser.getId());
            }

        } catch (Exception e) {
            System.err.println("Error loading tournaments from database");
            e.printStackTrace();
        }
    }

    private void displayTournaments(List<Tounament> tournaments) {
        tournamentCardsContainer.getChildren().clear();

        for (Tounament tournament : tournaments) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tournament/TournamentCard.fxml"));
                Parent card = loader.load();

                TournamentCardController cardController = loader.getController();
                cardController.setTournament(tournament);
                cardController.setRootStackPane(rootStackPane, contentArea);

                // Set callback to reload tournaments when card is updated/deleted
                cardController.setOnUpdateCallback(this::loadTournamentsFromDatabase);

                tournamentCardsContainer.getChildren().add(card);

            } catch (Exception e) {
                System.err.println("Error creating tournament card for: " + tournament.getTournamentName());
                e.printStackTrace();
            }
        }

        updateEmptyState();
    }

    private void renderCentersMap() {
        if (centerMapWebView == null) {
            return;
        }

        List<CenterMapPin> pins = new ArrayList<>();
        if (allCenters != null) {
            for (Center center : allCenters) {
                Optional<double[]> coordinates = extractCoordinates(center.getMapUrl());
                coordinates.ifPresent(values -> pins.add(new CenterMapPin(center, values[0], values[1],
                        getTournamentsForCenter(center.getCenterId()).size())));
            }
        }

        if (mapStatusLabel != null) {
            int totalCenters = allCenters == null ? 0 : allCenters.size();
            mapStatusLabel.setText(pins.size() + "/" + totalCenters + " centres positionnes");
        }

        pendingCentersJson = buildCentersJson(pins);
        if (mapLoaded) {
            pushCentersToMap();
        }
    }

    public class MapBridge {
        public void openCenterFromMap(String centerId) {
            Platform.runLater(() -> {
                Center center = findCenterById(centerId);
                if (center != null) {
                    openCenterTournamentsDrawer(center);
                }
            });
        }
    }

    private void openCenterTournamentsDrawer(Center center) {
        StackPane drawerOverlay = new StackPane();
        drawerOverlay.getStyleClass().add("overlay-bg");
        drawerOverlay.setAlignment(Pos.CENTER_RIGHT);
        drawerOverlay.getStylesheets().add(getClass().getResource("/Fxml/Tournament/AddTournamentModal.css").toExternalForm());
        drawerOverlay.getStylesheets().add(getClass().getResource("/Fxml/Tournament/TournamentHub.css").toExternalForm());

        VBox drawer = new VBox(16);
        drawer.getStyleClass().add("drawer");
        drawer.setPrefWidth(460);
        drawer.setMaxWidth(460);
        drawer.setPadding(new Insets(28));
        StackPane.setAlignment(drawer, Pos.CENTER_RIGHT);

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(center.getName());
        title.getStyleClass().add("drawer-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("center-close-button");
        closeButton.setPrefWidth(48);
        closeButton.setPrefHeight(38);
        closeButton.setOnAction(event -> rootStackPane.getChildren().remove(drawerOverlay));
        header.getChildren().addAll(title, spacer, closeButton);

        Label subtitle = new Label("Tournois heberges dans ce centre");
        subtitle.getStyleClass().add("center-drawer-subtitle");

        VBox centerInfo = new VBox(7);
        centerInfo.getStyleClass().add("center-info-block");
        centerInfo.getChildren().addAll(
                createInfoLabel("Adresse: " + safeText(center.getAddress())),
                createInfoLabel("Ville: " + safeText(center.getCity())),
                createInfoLabel("Contact: " + safeText(center.getContactEmail()))
        );

        VBox tournamentList = new VBox(12);
        List<Tounament> centerTournaments = getTournamentsForCenter(center.getCenterId());
        if (centerTournaments.isEmpty()) {
            Label empty = new Label("Aucun tournoi n'est encore associe a ce centre.");
            empty.getStyleClass().add("center-empty-label");
            tournamentList.getChildren().add(empty);
        } else {
            for (Tounament tournament : centerTournaments) {
                tournamentList.getChildren().add(createCenterTournamentRow(tournament, drawerOverlay));
            }
        }

        ScrollPane scrollPane = new ScrollPane(tournamentList);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        drawer.getChildren().addAll(header, subtitle, new Separator(), centerInfo, scrollPane);
        drawerOverlay.getChildren().add(drawer);
        rootStackPane.getChildren().add(drawerOverlay);
    }

    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("center-info-line");
        label.setWrapText(true);
        return label;
    }

    private HBox createCenterTournamentRow(Tounament tournament, StackPane drawerOverlay) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("center-tournament-row");

        VBox details = new VBox(5);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label name = new Label(tournament.getTournamentName());
        name.getStyleClass().add("center-tournament-name");
        name.setWrapText(true);

        Label meta = new Label(buildTournamentMeta(tournament));
        meta.getStyleClass().add("center-tournament-meta");
        meta.setWrapText(true);

        details.getChildren().addAll(name, meta);

        Button viewButton = new Button("VOIR");
        viewButton.getStyleClass().add("center-view-button");
        viewButton.setMinWidth(112);
        viewButton.setPrefWidth(112);
        viewButton.setPrefHeight(42);
        viewButton.setOnAction(event -> {
            rootStackPane.getChildren().remove(drawerOverlay);
            openTournamentDetail(tournament);
        });

        row.getChildren().addAll(details, viewButton);
        return row;
    }

    private void openTournamentDetail(Tounament tournament) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Fxml/Tournament/TournamentDetail.fxml"));
            Node detailPage = loader.load();

            TournamentDetailController controller = loader.getController();
            controller.setTournament(tournament);
            controller.setRootStackPane(rootStackPane);

            rootStackPane.getChildren().clear();
            rootStackPane.getChildren().add(detailPage);

            FadeTransition ft = new FadeTransition(Duration.millis(300), detailPage);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        } catch (Exception e) {
            System.err.println("Error opening tournament detail page from map");
            e.printStackTrace();
        }
    }

    private List<Tounament> getTournamentsForCenter(String centerId) {
        if (centerId == null || allTournaments == null) {
            return List.of();
        }

        return allTournaments.stream()
                .filter(tournament -> centerId.equals(tournament.getCenter_id()))
                .toList();
    }

    private Center findCenterById(String centerId) {
        if (centerId == null || allCenters == null) {
            return null;
        }

        return allCenters.stream()
                .filter(center -> centerId.equals(center.getCenterId()))
                .findFirst()
                .orElse(null);
    }

    private Tounament findTournamentById(String tournamentId) {
        if (tournamentId == null || allTournaments == null) {
            return null;
        }

        return allTournaments.stream()
                .filter(tournament -> tournamentId.equals(tournament.getTournamentId()))
                .findFirst()
                .orElse(null);
    }

    private String buildTournamentMeta(Tounament tournament) {
        String dates = "";
        if (tournament.getStarts_at() != null && tournament.getEnds_at() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH);
            dates = tournament.getStarts_at().format(formatter) + " - " + tournament.getEnds_at().format(formatter);
        }

        String state = tournament.getState();
        try {
            state = Tounament.TournamentState.valueOf(tournament.getState()).getDisplayName();
        } catch (Exception ignored) {
        }

        return safeText(tournament.getFormat()) + " | " + safeText(state) + (dates.isEmpty() ? "" : " | " + dates);
    }

    private void pushCentersToMap() {
        if (centerMapWebView == null) {
            return;
        }

        try {
            centerMapWebView.getEngine().executeScript(
                    "if (typeof setCenters === 'function') { setCenters(" + pendingCentersJson + "); }"
            );
        } catch (Exception e) {
            System.err.println("Error pushing centers to map: " + e.getMessage());
        }
    }

    private String buildCentersJson(List<CenterMapPin> pins) {
        StringBuilder markers = new StringBuilder();
        markers.append("[");
        for (CenterMapPin pin : pins) {
            Center center = pin.center();
            markers.append("{")
                    .append("\"id\":\"").append(jsonEscape(center.getCenterId())).append("\",")
                    .append("\"name\":\"").append(jsonEscape(center.getName())).append("\",")
                    .append("\"city\":\"").append(jsonEscape(center.getCity())).append("\",")
                    .append("\"address\":\"").append(jsonEscape(center.getAddress())).append("\",")
                    .append("count:").append(pin.tournamentCount()).append(",")
                    .append("lat:").append(pin.latitude()).append(",")
                    .append("lng:").append(pin.longitude())
                    .append("},");
        }
        if (!pins.isEmpty()) {
            markers.setLength(markers.length() - 1);
        }
        markers.append("]");
        return markers.toString();
    }

    private Optional<double[]> extractCoordinates(String mapUrl) {
        if (mapUrl == null || mapUrl.trim().isEmpty()) {
            return Optional.empty();
        }

        String decoded = mapUrl.replace("%2C", ",");
        List<Pattern> patterns = List.of(
                Pattern.compile("@(-?\\d+(?:\\.\\d+)?),\\s*(-?\\d+(?:\\.\\d+)?)"),
                Pattern.compile("[?&](?:q|ll|query)=(-?\\d+(?:\\.\\d+)?),\\s*(-?\\d+(?:\\.\\d+)?)"),
                Pattern.compile("!3d(-?\\d+(?:\\.\\d+)?)!4d(-?\\d+(?:\\.\\d+)?)"),
                Pattern.compile("(-?\\d{1,2}(?:\\.\\d+)?),\\s*(-?\\d{1,3}(?:\\.\\d+)?)")
        );

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(decoded);
            if (matcher.find()) {
                double latitude = Double.parseDouble(matcher.group(1));
                double longitude = Double.parseDouble(matcher.group(2));
                if (latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180) {
                    return Optional.of(new double[]{latitude, longitude});
                }
            }
        }

        return Optional.empty();
    }

    private String safeText(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private String jsonEscape(String value) {
        return safeText(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ");
    }

    private record CenterMapPin(Center center, double latitude, double longitude, int tournamentCount) {
    }

    public void addTournamentCard(Parent card) {
        tournamentCardsContainer.getChildren().add(card);
        updateEmptyState();
    }

    public void clearTournaments() {
        tournamentCardsContainer.getChildren().clear();
        updateEmptyState();
    }

    private void showSuggestions(String query) {
        // Hide existing popup
        if (suggestionsPopup != null && suggestionsPopup.isShowing()) {
            suggestionsPopup.hide();
        }

        // Hide if query is empty or too short
        if (query == null || query.trim().length() < 1) {
            return;
        }

        if (allTournaments == null || allTournaments.isEmpty()) {
            return;
        }

        // Find matching tournaments
        String lowerQuery = query.toLowerCase().trim();
        List<Tounament> matches = allTournaments.stream()
                .filter(t -> t.getTournamentName().toLowerCase().startsWith(lowerQuery))
                .limit(5)
                .toList();

        // Hide if no matches
        if (matches.isEmpty()) {
            return;
        }

        // Create popup content
        VBox popupContent = new VBox();
        popupContent.getStyleClass().add("suggestions-box");
        popupContent.setStyle(
            "-fx-background-color: linear-gradient(to bottom, rgba(31, 30, 78, 0.98), rgba(15, 15, 35, 0.98));" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: rgba(139, 13, 13, 0.6);" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1.5;" +
            "-fx-padding: 4;" +
            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.5), 15, 0, 0, 4);"
        );
        popupContent.setPrefWidth(250);
        popupContent.setMaxWidth(250);

        // Create suggestion items
        for (Tounament tournament : matches) {
            Label suggestion = new Label(tournament.getTournamentName());
            suggestion.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 13;" +
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 8 12;" +
                "-fx-max-width: Infinity;"
            );
            
            suggestion.setOnMouseEntered(e -> 
                suggestion.setStyle(
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13;" +
                    "-fx-background-color: rgba(139, 13, 13, 0.3);" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 8 12;" +
                    "-fx-max-width: Infinity;"
                )
            );
            
            suggestion.setOnMouseExited(e -> 
                suggestion.setStyle(
                    "-fx-text-fill: white;" +
                    "-fx-font-size: 13;" +
                    "-fx-background-color: transparent;" +
                    "-fx-background-radius: 6;" +
                    "-fx-cursor: hand;" +
                    "-fx-padding: 8 12;" +
                    "-fx-max-width: Infinity;"
                )
            );
            
            suggestion.setOnMouseClicked(event -> {
                searchField.setText(tournament.getTournamentName());
                if (suggestionsPopup != null) {
                    suggestionsPopup.hide();
                }
                filterTournaments();
            });
            
            popupContent.getChildren().add(suggestion);
        }

        // Create and show popup
        suggestionsPopup = new Popup();
        suggestionsPopup.setAutoHide(true);
        suggestionsPopup.getContent().add(popupContent);
        
        // Show popup below search field
        var bounds = searchField.localToScreen(searchField.getBoundsInLocal());
        if (bounds != null) {
            suggestionsPopup.show(searchField, bounds.getMinX(), bounds.getMaxY() + 5);
        }
    }
}
