package Genex.Controllers.Dashboard;

import Genex.entities.Tounament;
import Genex.entities.TournamentMatch;
import Genex.services.TournamentNewsService;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TournamentNewsTickerController {

    @FXML private HBox tickerContainer;
    @FXML private Pane tickerViewport;
    @FXML private Text newsText;
    @FXML private Label liveLabel;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAX_MATCH_NEWS = 6;
    private static final double PIXELS_PER_SECOND = 85.0;

    private final TournamentNewsService newsService = new TournamentNewsService();
    private List<String> newsItems = new ArrayList<>();
    private List<String> pendingNewsItems = new ArrayList<>();
    private int currentNewsIndex = 0;
    private AnimationTimer tickerTimer;
    private Timeline refreshTimeline;
    private FadeTransition livePulse;
    private long lastFrameNanos = -1;
    private double currentX = 0;
    private boolean itemReady = false;

    @FXML
    public void initialize() {
        clipViewport();
        startLivePulse();
        newsItems = buildNewsItems();

        if (newsItems.isEmpty()) {
            newsItems.add("[INFO] No active tournament news right now");
        }

        tickerViewport.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                Platform.runLater(() -> Platform.runLater(this::startTickerLoop));
            }
        });

        if (tickerViewport.getScene() != null) {
            Platform.runLater(() -> Platform.runLater(this::startTickerLoop));
        }

        startPeriodicRefresh();
    }

    private void clipViewport() {
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(tickerViewport.widthProperty());
        clip.heightProperty().bind(tickerViewport.heightProperty());
        tickerViewport.setClip(clip);
    }

    private void startLivePulse() {
        if (liveLabel == null) {
            return;
        }
        livePulse = new FadeTransition(Duration.seconds(1), liveLabel);
        livePulse.setFromValue(1.0);
        livePulse.setToValue(0.35);
        livePulse.setCycleCount(Animation.INDEFINITE);
        livePulse.setAutoReverse(true);
        livePulse.play();
    }

    private void startTickerLoop() {
        if (newsItems.isEmpty()) {
            hideTicker();
            return;
        }

        if (tickerTimer != null) {
            tickerTimer.stop();
        }

        showTicker();
        prepareCurrentItem();
        lastFrameNanos = -1;

        tickerTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastFrameNanos < 0) {
                    lastFrameNanos = now;
                    return;
                }

                double elapsedSeconds = (now - lastFrameNanos) / 1_000_000_000.0;
                lastFrameNanos = now;
                if (elapsedSeconds > 0.08) {
                    elapsedSeconds = 0.08;
                }

                currentX -= PIXELS_PER_SECOND * elapsedSeconds;
                newsText.setTranslateX(currentX);

                double textWidth = getTextWidth();
                if (itemReady && currentX + textWidth <= 0) {
                    moveToNextItem();
                }
            }
        };
        tickerTimer.start();
    }

    private void prepareCurrentItem() {
        if (newsItems.isEmpty()) {
            return;
        }

        if (currentNewsIndex >= newsItems.size()) {
            currentNewsIndex = 0;
            if (!pendingNewsItems.isEmpty()) {
                newsItems = new ArrayList<>(pendingNewsItems);
                pendingNewsItems.clear();
            }
        }

        itemReady = false;
        newsText.setText(newsItems.get(currentNewsIndex));
        tickerViewport.applyCss();
        tickerViewport.layout();
        newsText.applyCss();

        double viewportWidth = tickerViewport.getWidth();
        if (viewportWidth <= 0 && tickerViewport.getScene() != null) {
            viewportWidth = tickerViewport.getScene().getWidth();
        }

        currentX = viewportWidth;
        newsText.setTranslateX(currentX);
        itemReady = true;
        System.out.println("[Ticker] Showing item " + (currentNewsIndex + 1) + "/" + newsItems.size()
                + " width=" + getTextWidth() + " text=" + newsItems.get(currentNewsIndex));
    }

    private void moveToNextItem() {
        currentNewsIndex++;
        prepareCurrentItem();
    }

    private double getTextWidth() {
        newsText.applyCss();
        return Math.max(newsText.getLayoutBounds().getWidth(), 1);
    }

    private void startPeriodicRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            List<String> freshItems = buildNewsItems();
            if (!freshItems.isEmpty()) {
                pendingNewsItems = freshItems;
            }
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private List<String> buildNewsItems() {
        List<String> items = new ArrayList<>();
        List<TournamentNewsService.TournamentNews> allNews = newsService.getAllTournamentNews();

        for (TournamentNewsService.TournamentNews news : allNews) {
            Tounament tournament = news.getTournament();
            switch (news.getType()) {
                case NEW_TOURNAMENT:
                    buildRegistrationOpen(items, tournament);
                    break;
                case REGISTRATION_CLOSED:
                    buildRegistrationClosed(items, tournament);
                    break;
                case ONGOING_TOURNAMENT:
                    buildOngoing(items, tournament);
                    break;
                case COMPLETED_TOURNAMENT:
                    buildCompleted(items, tournament);
                    break;
                default:
                    buildOngoing(items, tournament);
                    break;
            }
        }

        System.out.println("[Ticker] Built " + items.size() + " news items");
        return items;
    }

    private void buildRegistrationOpen(List<String> items, Tounament tournament) {
        String game = newsService.getGameName(tournament.getGame_id());
        String center = newsService.getCenterName(tournament.getCenter_id());
        int taken = newsService.getParticipantCount(tournament.getTournamentId());
        int slots = Math.max(tournament.getMaxPlayers() - taken, 0);
        List<TournamentMatch> matches = newsService.getAllMatches(tournament.getTournamentId());

        add(items, "NEW", tournament.getTournamentName()
                + " | Game: " + game
                + " | Center: " + center
                + " | Format: " + value(tournament.getFormat(), "TBD")
                + " | Type: " + value(tournament.getParticipant_type(), "SOLO")
                + " | Players: " + taken + "/" + tournament.getMaxPlayers()
                + " | Starts: " + dateTime(tournament.getStarts_at())
                + " | Prize: " + prize(tournament.getPrize_pool()));

        add(items, "JOIN", tournament.getTournamentName()
                + " | " + slots + " slots remaining | Register now before brackets lock");

        if (matches.isEmpty()) {
            add(items, "SCHEDULE", tournament.getTournamentName()
                    + " | Bracket will be generated after registration closes");
        } else {
            buildUpcomingMatches(items, tournament, matches);
            buildResults(items, tournament, matches);
        }
    }

    private void buildRegistrationClosed(List<String> items, Tounament tournament) {
        String game = newsService.getGameName(tournament.getGame_id());
        String center = newsService.getCenterName(tournament.getCenter_id());
        int taken = newsService.getParticipantCount(tournament.getTournamentId());
        List<TournamentMatch> matches = newsService.getAllMatches(tournament.getTournamentId());

        add(items, "LOCKED", tournament.getTournamentName()
                + " | Game: " + game
                + " | Center: " + center
                + " | Qualified players: " + taken
                + " | Starts: " + dateTime(tournament.getStarts_at()));

        buildResults(items, tournament, matches);
        buildUpcomingMatches(items, tournament, matches);
    }

    private void buildOngoing(List<String> items, Tounament tournament) {
        String game = newsService.getGameName(tournament.getGame_id());
        String center = newsService.getCenterName(tournament.getCenter_id());
        int round = newsService.getCurrentRound(tournament.getTournamentId());
        int taken = newsService.getParticipantCount(tournament.getTournamentId());
        List<TournamentMatch> matches = newsService.getAllMatches(tournament.getTournamentId());

        add(items, "LIVE", tournament.getTournamentName()
                + " | Game: " + game
                + " | Center: " + center
                + " | Round: " + round
                + " | Players: " + taken
                + " | Results: " + countByStatus(matches, TournamentMatch.MatchStatus.COMPLETED)
                + " | Live: " + countByStatus(matches, TournamentMatch.MatchStatus.IN_PROGRESS)
                + " | Upcoming: " + countByStatus(matches, TournamentMatch.MatchStatus.PENDING)
                + " | Prize: " + prize(tournament.getPrize_pool()));

        buildLiveMatches(items, tournament, matches);
        buildResults(items, tournament, matches);
        buildUpcomingMatches(items, tournament, matches);
    }

    private void buildCompleted(List<String> items, Tounament tournament) {
        String game = newsService.getGameName(tournament.getGame_id());
        String center = newsService.getCenterName(tournament.getCenter_id());
        String winnerId = newsService.getTournamentWinner(tournament.getTournamentId());
        String winner = winnerId == null ? "TBD" : newsService.getPlayerName(winnerId);
        List<TournamentMatch> matches = newsService.getAllMatches(tournament.getTournamentId());

        add(items, "CHAMPION", tournament.getTournamentName()
                + " | Game: " + game
                + " | Center: " + center
                + " | Winner: " + winner
                + " | Prize: " + prize(tournament.getPrize_pool()));

        buildResults(items, tournament, matches);
    }

    private void buildLiveMatches(List<String> items, Tounament tournament, List<TournamentMatch> matches) {
        matches.stream()
                .filter(match -> match.getStatus() == TournamentMatch.MatchStatus.IN_PROGRESS)
                .sorted(Comparator.comparingInt(TournamentMatch::getRound)
                        .thenComparingInt(TournamentMatch::getMatchNumber))
                .limit(MAX_MATCH_NEWS)
                .forEach(match -> add(items, "NOW", tournament.getTournamentName()
                        + " | R" + match.getRound() + "M" + match.getMatchNumber()
                        + " | " + newsService.getPlayerName(match.getPlayer1Id())
                        + " " + match.getPlayer1Score() + "-" + match.getPlayer2Score() + " "
                        + newsService.getPlayerName(match.getPlayer2Id())));
    }

    private void buildResults(List<String> items, Tounament tournament, List<TournamentMatch> matches) {
        List<TournamentMatch> completed = matches.stream()
                .filter(match -> match.getStatus() == TournamentMatch.MatchStatus.COMPLETED)
                .sorted(Comparator
                        .comparing((TournamentMatch match) ->
                                match.getCompletedTime() == null ? LocalDateTime.MIN : match.getCompletedTime())
                        .reversed())
                .limit(MAX_MATCH_NEWS)
                .collect(Collectors.toList());

        for (TournamentMatch match : completed) {
            String winner = match.getWinnerId() == null ? "TBD" : newsService.getPlayerName(match.getWinnerId());
            add(items, "RESULT", tournament.getTournamentName()
                    + " | R" + match.getRound() + "M" + match.getMatchNumber()
                    + " | " + newsService.getPlayerName(match.getPlayer1Id())
                    + " " + match.getPlayer1Score() + "-" + match.getPlayer2Score() + " "
                    + newsService.getPlayerName(match.getPlayer2Id())
                    + " | Winner: " + winner);
        }
    }

    private void buildUpcomingMatches(List<String> items, Tounament tournament, List<TournamentMatch> matches) {
        List<TournamentMatch> pending = matches.stream()
                .filter(match -> match.getStatus() == TournamentMatch.MatchStatus.PENDING)
                .sorted(Comparator
                        .comparing((TournamentMatch match) ->
                                match.getScheduledTime() == null ? LocalDateTime.MAX : match.getScheduledTime())
                        .thenComparingInt(TournamentMatch::getRound)
                        .thenComparingInt(TournamentMatch::getMatchNumber))
                .limit(MAX_MATCH_NEWS)
                .collect(Collectors.toList());

        for (TournamentMatch match : pending) {
            String p1 = newsService.getPlayerName(match.getPlayer1Id());
            String p2 = newsService.getPlayerName(match.getPlayer2Id());
            if ("TBD".equals(p1) && "TBD".equals(p2)) {
                continue;
            }
            add(items, "NEXT", tournament.getTournamentName()
                    + " | R" + match.getRound() + "M" + match.getMatchNumber()
                    + " | " + p1 + " vs " + p2
                    + " | " + (match.getScheduledTime() == null ? "TBD" : dateTime(match.getScheduledTime())));
        }
    }

    private void add(List<String> items, String tag, String text) {
        items.add("[" + tag + "] " + text);
    }

    private long countByStatus(List<TournamentMatch> matches, TournamentMatch.MatchStatus status) {
        return matches.stream().filter(match -> match.getStatus() == status).count();
    }

    private String dateTime(LocalDateTime dateTime) {
        return dateTime == null ? "TBD" : dateTime.format(DATE_FMT);
    }

    private String prize(double prize) {
        return String.format(Locale.US, "%,.0f DT", prize);
    }

    private String value(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private void hideTicker() {
        tickerContainer.setVisible(false);
        tickerContainer.setManaged(false);
    }

    private void showTicker() {
        tickerContainer.setVisible(true);
        tickerContainer.setManaged(true);
    }

    private void stopAnimation() {
        if (tickerTimer != null) {
            tickerTimer.stop();
            tickerTimer = null;
        }
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        if (livePulse != null) {
            livePulse.stop();
        }
    }

    public void cleanup() {
        stopAnimation();
    }
}
