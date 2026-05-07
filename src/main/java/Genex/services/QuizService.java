package Genex.services;

import Genex.entities.Quiz;
import Genex.entities.Player;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;
import Genex.utils.SessionManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizService implements ICrud<Quiz> {

    private Connection cnx;
    private boolean quizAttemptTrackingAvailable;
    private static final Map<String, Map<Integer, QuizAttemptResult>> LOCAL_ATTEMPTS = new ConcurrentHashMap<>();
    private static final Object LOCAL_ATTEMPT_FILE_LOCK = new Object();
    private static final String LOCAL_ATTEMPT_DIR = ".genex";

    public QuizService() {
        cnx = Myconnection.getInstance().getCnx();
        quizAttemptTrackingAvailable = ensureQuizAttemptTable();
    }

    @Override
    public void addEntity(Quiz quiz) {
        String query = "INSERT INTO quiz (tutorial_id, question, option_a, option_b, option_c, option_d, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, quiz.getTutorialId());
            ps.setString(2, quiz.getQuestion());
            ps.setString(3, quiz.getOptionA());
            ps.setString(4, quiz.getOptionB());
            ps.setString(5, quiz.getOptionC());
            ps.setString(6, quiz.getOptionD());
            ps.setString(7, String.valueOf(quiz.getCorrectAnswer()));
            ps.executeUpdate();
            System.out.println("Quiz question added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateEntity(Quiz quiz, String id) {
        String query = "UPDATE quiz SET tutorial_id=?, question=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_answer=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, quiz.getTutorialId());
            ps.setString(2, quiz.getQuestion());
            ps.setString(3, quiz.getOptionA());
            ps.setString(4, quiz.getOptionB());
            ps.setString(5, quiz.getOptionC());
            ps.setString(6, quiz.getOptionD());
            ps.setString(7, String.valueOf(quiz.getCorrectAnswer()));
            ps.setInt(8, Integer.parseInt(id));
            ps.executeUpdate();
            System.out.println("Quiz question updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteEntity(Quiz quiz) {
        String query = "DELETE FROM quiz WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, quiz.getId());
            ps.executeUpdate();
            System.out.println("Quiz question deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getEntity(Quiz quiz) {
        String query = "SELECT q.*, t.title as tutorial_title FROM quiz q LEFT JOIN tutorial t ON q.tutorial_id = t.id WHERE q.id=?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, quiz.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                quiz.setTutorialId(rs.getInt("tutorial_id"));
                quiz.setQuestion(rs.getString("question"));
                quiz.setOptionA(rs.getString("option_a"));
                quiz.setOptionB(rs.getString("option_b"));
                quiz.setOptionC(rs.getString("option_c"));
                quiz.setOptionD(rs.getString("option_d"));
                String ca = rs.getString("correct_answer");
                if (ca != null && !ca.isEmpty()) quiz.setCorrectAnswer(ca.charAt(0));
                quiz.setTutorialTitle(rs.getString("tutorial_title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Quiz> getAllQuizzes() {
        List<Quiz> list = new ArrayList<>();
        String query = "SELECT q.*, t.title as tutorial_title FROM quiz q LEFT JOIN tutorial t ON q.tutorial_id = t.id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                String ca = rs.getString("correct_answer");
                Quiz q = new Quiz(
                        rs.getInt("id"),
                        rs.getInt("tutorial_id"),
                        rs.getString("question"),
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d"),
                        (ca != null && !ca.isEmpty()) ? ca.charAt(0) : 'A'
                );
                q.setTutorialTitle(rs.getString("tutorial_title"));
                list.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Quiz> getQuizzesByTutorialId(int tutorialId) {
        List<Quiz> list = new ArrayList<>();
        String query = "SELECT * FROM quiz WHERE tutorial_id=?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, tutorialId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String ca = rs.getString("correct_answer");
                Quiz q = new Quiz(
                        rs.getInt("id"),
                        rs.getInt("tutorial_id"),
                        rs.getString("question"),
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d"),
                        (ca != null && !ca.isEmpty()) ? ca.charAt(0) : 'A'
                );
                list.add(q);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Quiz> searchQuizzes(String keyword) {
        List<Quiz> list = new ArrayList<>();
        String query = "SELECT q.*, t.title as tutorial_title FROM quiz q LEFT JOIN tutorial t ON q.tutorial_id = t.id WHERE q.question LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String ca = rs.getString("correct_answer");
                Quiz qz = new Quiz(
                        rs.getInt("id"),
                        rs.getInt("tutorial_id"),
                        rs.getString("question"),
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d"),
                        (ca != null && !ca.isEmpty()) ? ca.charAt(0) : 'A'
                );
                qz.setTutorialTitle(rs.getString("tutorial_title"));
                list.add(qz);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Updates player stats after answering a quiz question.
     * XP and Accuracy are persistent in the 'players' table.
     */
    public void submitQuizResult(String userId, boolean isCorrect, int xpReward) {
        String query = "UPDATE players SET " +
                "total_attempts = total_attempts + 1, " +
                "correct_answers = correct_answers + ?, " +
                "tactical_xp = tactical_xp + ? " +
                "WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, isCorrect ? 1 : 0);
            ps.setInt(2, isCorrect ? xpReward : 0);
            ps.setString(3, userId);
            ps.executeUpdate();

            // Refresh local session data
            Player p = SessionManager.getInstance().getCurrentPlayer();
            String currentUserId = SessionManager.getInstance().getCurrentUserId();
            if (p != null && currentUserId != null && currentUserId.equals(userId)) {
                p.setTotalAttempts(p.getTotalAttempts() + 1);
                if (isCorrect) {
                    p.setCorrectAnswers(p.getCorrectAnswers() + 1);
                    p.setTacticalXp(p.getTacticalXp() + xpReward);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public QuizSubmissionStatus submitQuizResultOnce(String userId, int quizId, char selectedAnswer, char correctAnswer, int xpReward) {
        if (userId == null || userId.isBlank()) {
            return userId == null || userId.isBlank()
                    ? QuizSubmissionStatus.SESSION_MISSING
                    : QuizSubmissionStatus.FAILED;
        }
        if (hasCachedAttempt(userId, quizId) || hasLocalAttempt(userId, quizId)) {
            return QuizSubmissionStatus.ALREADY_SUBMITTED;
        }

        boolean isCorrect = Character.toUpperCase(selectedAnswer) == Character.toUpperCase(correctAnswer);
        QuizSubmissionStatus attemptStatus = saveAttemptWithFallback(userId, quizId, selectedAnswer, isCorrect);
        if (attemptStatus == QuizSubmissionStatus.ALREADY_SUBMITTED) {
            cacheAttempt(userId, quizId, selectedAnswer, isCorrect);
            saveLocalAttemptIfMissing(userId, quizId, selectedAnswer, isCorrect);
            return attemptStatus;
        }
        cacheAttempt(userId, quizId, selectedAnswer, isCorrect);
        boolean localSaved = saveLocalAttemptIfMissing(userId, quizId, selectedAnswer, isCorrect);

        boolean statsUpdated = incrementPlayerStats(userId, isCorrect, xpReward);
        if (statsUpdated) {
            refreshSessionStats(userId, isCorrect, xpReward);
            return QuizSubmissionStatus.SAVED;
        }

        // Keep UX deterministic even if storage schema is inconsistent:
        // treat the attempt as accepted so the quiz can be locked immediately.
        if (attemptStatus == QuizSubmissionStatus.FAILED && !localSaved) {
            return QuizSubmissionStatus.FAILED;
        }
        return QuizSubmissionStatus.SAVED;
    }

    public QuizAttemptResult getUserQuizAttempt(String userId, int quizId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        Map<Integer, QuizAttemptResult> attempts = getUserQuizAttempts(userId);
        return attempts.get(quizId);
    }

    public Map<Integer, QuizAttemptResult> getUserQuizAttempts(String userId) {
        Map<Integer, QuizAttemptResult> attempts = new HashMap<>();
        if (userId == null || userId.isBlank()) {
            return attempts;
        }
        attempts.putAll(getCachedAttempts(userId));
        attempts.putAll(loadLocalAttempts(userId));
        AttemptStorage storage = resolveAttemptStorage();
        if (storage == null) {
            return attempts;
        }

        String correctnessExpr = storage.correctColumn != null
                ? "a." + quoteIdentifier(storage.correctColumn)
                : "CASE WHEN UPPER(a." + quoteIdentifier(storage.answerColumn) + ") = UPPER(q.correct_answer) THEN 1 ELSE 0 END";

        StringBuilder sql = new StringBuilder("SELECT a.")
                .append(quoteIdentifier(storage.quizColumn))
                .append(" AS quiz_id, a.")
                .append(quoteIdentifier(storage.answerColumn))
                .append(" AS selected_answer, ")
                .append(correctnessExpr)
                .append(" AS is_correct FROM ")
                .append(quoteIdentifier(storage.tableName))
                .append(" a ");
        if (storage.correctColumn == null) {
            sql.append("JOIN quiz q ON q.id = a.").append(quoteIdentifier(storage.quizColumn)).append(" ");
        }
        sql.append("WHERE ").append(buildUserFilter(storage));

        try (PreparedStatement ps = cnx.prepareStatement(sql.toString())) {
            bindUserFilter(ps, storage, userId, 1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String selected = rs.getString("selected_answer");
                char selectedAnswer = (selected != null && !selected.isEmpty()) ? selected.charAt(0) : 'A';
                int storedQuizId = rs.getInt("quiz_id");
                QuizAttemptResult attempt = new QuizAttemptResult(selectedAnswer, rs.getBoolean("is_correct"));
                attempts.put(storedQuizId, attempt);
                cacheAttempt(userId, storedQuizId, attempt.getSelectedAnswer(), attempt.isCorrect());
            }
        } catch (SQLException e) {
            System.err.println("Failed to load quiz attempts: " + e.getMessage());
        }
        return attempts;
    }

    public Player getPlayerStatsByUserId(String userId) {
        String query = "SELECT tactical_xp, total_attempts, correct_answers FROM players WHERE user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Player player = new Player();
                player.setTacticalXp(rs.getInt("tactical_xp"));
                player.setTotalAttempts(rs.getInt("total_attempts"));
                player.setCorrectAnswers(rs.getInt("correct_answers"));
                return player;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load player stats.", e);
        }
        return null;
    }

    public int getGlobalRankByXp(String userId) {
        String rankQuery = "SELECT CASE " +
                "WHEN EXISTS(SELECT 1 FROM players WHERE user_id = ?) " +
                "THEN 1 + (SELECT COUNT(*) FROM players p WHERE p.tactical_xp > (SELECT tactical_xp FROM players WHERE user_id = ?)) " +
                "ELSE 0 END AS player_rank";
        try (PreparedStatement ps = cnx.prepareStatement(rankQuery)) {
            ps.setString(1, userId);
            ps.setString(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("player_rank");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to compute global rank.", e);
        }
        return 0;
    }

    private void refreshSessionStats(String userId, boolean isCorrect, int xpReward) {
        Player p = SessionManager.getInstance().getCurrentPlayer();
        String currentUserId = SessionManager.getInstance().getCurrentUserId();
        if (p != null && currentUserId != null && currentUserId.equals(userId)) {
            p.setTotalAttempts(p.getTotalAttempts() + 1);
            if (isCorrect) {
                p.setCorrectAnswers(p.getCorrectAnswers() + 1);
                p.setTacticalXp(p.getTacticalXp() + xpReward);
            }
        }
    }

    private boolean ensureQuizAttemptTable() {
        if (quizAttemptTableExists()) {
            return true;
        }

        String createTable = "CREATE TABLE IF NOT EXISTS player_quiz_attempts (" +
                "user_id VARCHAR(255) NOT NULL, " +
                "quiz_id INT NOT NULL, " +
                "selected_answer CHAR(1) NOT NULL, " +
                "is_correct TINYINT(1) NOT NULL, " +
                "attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (user_id, quiz_id)" +
                ")";
        try (Statement statement = cnx.createStatement()) {
            statement.execute(createTable);
            return true;
        } catch (SQLException e) {
            System.err.println("Quiz attempts table unavailable: " + e.getMessage());
            return quizAttemptTableExists();
        }
    }

    private boolean isQuizAttemptTrackingReady() {
        if (quizAttemptTrackingAvailable) {
            return true;
        }
        quizAttemptTrackingAvailable = ensureQuizAttemptTable();
        return quizAttemptTrackingAvailable;
    }

    private boolean quizAttemptTableExists() {
        try {
            DatabaseMetaData metaData = cnx.getMetaData();
            try (ResultSet rs = metaData.getTables(cnx.getCatalog(), null, "player_quiz_attempts", new String[]{"TABLE"})) {
                if (rs.next()) {
                    return true;
                }
            }
            try (ResultSet rs = metaData.getTables(cnx.getCatalog(), null, "PLAYER_QUIZ_ATTEMPTS", new String[]{"TABLE"})) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private QuizSubmissionStatus saveAttemptWithFallback(String userId, int quizId, char selectedAnswer, boolean isCorrect) {
        ensureAppQuizAttemptTable();
        ensureQuizAttemptTable();
        ensureLegacyQuizAttemptTable();
        AttemptStorage storage = resolveAttemptStorage();
        if (storage == null) {
            return QuizSubmissionStatus.FAILED;
        }

        String normalizedAnswer = String.valueOf(Character.toUpperCase(selectedAnswer));
        if (attemptExists(storage, userId, quizId)) {
            return QuizSubmissionStatus.ALREADY_SUBMITTED;
        }

        if (storage.userType == UserColumnType.PLAYER_ID) {
            ensurePlayerRowExists(userId);
        }

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();

        columns.append(quoteIdentifier(storage.userColumn));
        if (storage.userType == UserColumnType.USER_ID) {
            values.append("?");
            params.add(userId);
        } else {
            values.append("(SELECT id FROM players WHERE user_id = ? LIMIT 1)");
            params.add(userId);
        }

        columns.append(", ").append(quoteIdentifier(storage.quizColumn));
        values.append(", ?");
        params.add(quizId);

        columns.append(", ").append(quoteIdentifier(storage.answerColumn));
        values.append(", ?");
        params.add(normalizedAnswer);

        if (storage.correctColumn != null) {
            columns.append(", ").append(quoteIdentifier(storage.correctColumn));
            values.append(", ?");
            params.add(isCorrect);
        }

        if (storage.attemptedAtColumn != null) {
            columns.append(", ").append(quoteIdentifier(storage.attemptedAtColumn));
            values.append(", CURRENT_TIMESTAMP");
        }

        String insertSql = "INSERT INTO " + quoteIdentifier(storage.tableName) +
                " (" + columns + ") VALUES (" + values + ")";

        try (PreparedStatement ps = cnx.prepareStatement(insertSql)) {
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String s) {
                    ps.setString(i + 1, s);
                } else if (param instanceof Integer n) {
                    ps.setInt(i + 1, n);
                } else if (param instanceof Boolean b) {
                    ps.setBoolean(i + 1, b);
                }
            }
            int affected = ps.executeUpdate();
            return affected > 0 ? QuizSubmissionStatus.SAVED : QuizSubmissionStatus.FAILED;
        } catch (SQLException e) {
            if (isUniqueConstraintViolation(e)) {
                return QuizSubmissionStatus.ALREADY_SUBMITTED;
            }
            System.err.println("Failed saving quiz attempt: " + e.getMessage());
            return QuizSubmissionStatus.FAILED;
        }
    }

    private void ensureLegacyQuizAttemptTable() {
        String createTable = "CREATE TABLE IF NOT EXISTS quiz_attempts (" +
                "user_id VARCHAR(255) NOT NULL, " +
                "quiz_id INT NOT NULL, " +
                "selected_answer CHAR(1) NOT NULL, " +
                "is_correct TINYINT(1) NOT NULL, " +
                "attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (user_id, quiz_id)" +
                ")";
        try (Statement statement = cnx.createStatement()) {
            statement.execute(createTable);
        } catch (SQLException ignored) {
        }
    }

    private void ensureAppQuizAttemptTable() {
        String createTable = "CREATE TABLE IF NOT EXISTS app_player_quiz_attempts (" +
                "user_id VARCHAR(255) NOT NULL, " +
                "player_id INT NULL, " +
                "quiz_id INT NOT NULL, " +
                "selected_answer CHAR(1) NULL, " +
                "selected_option CHAR(1) NULL, " +
                "is_correct TINYINT(1) NULL, " +
                "attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (user_id, quiz_id)" +
                ")";
        try (Statement statement = cnx.createStatement()) {
            statement.execute(createTable);
        } catch (SQLException ignored) {
        }
    }

    private AttemptStorage resolveAttemptStorage() {
        try {
            DatabaseMetaData metaData = cnx.getMetaData();
            try (ResultSet tables = metaData.getTables(cnx.getCatalog(), null, "%", new String[]{"TABLE"})) {
                AttemptStorage fallback = null;
                while (tables.next()) {
                    String table = tables.getString("TABLE_NAME");
                    if (table == null) {
                        continue;
                    }
                    String lowered = table.toLowerCase();
                    if (!lowered.contains("quiz") || !(lowered.contains("attempt") || lowered.contains("submission"))) {
                        continue;
                    }
                    AttemptStorage candidate = buildAttemptStorageForTable(metaData, table);
                    if (candidate == null) {
                        continue;
                    }
                    if ("app_player_quiz_attempts".equalsIgnoreCase(table)) {
                        return candidate;
                    }
                    if ("player_quiz_attempts".equalsIgnoreCase(table)) {
                        fallback = candidate;
                    } else if (fallback == null) {
                        fallback = candidate;
                    }
                }
                return fallback;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    private AttemptStorage buildAttemptStorageForTable(DatabaseMetaData metaData, String tableName) throws SQLException {
        Map<String, String> cols = new HashMap<>();
        try (ResultSet rs = metaData.getColumns(cnx.getCatalog(), null, tableName, "%")) {
            while (rs.next()) {
                String col = rs.getString("COLUMN_NAME");
                if (col != null) {
                    cols.put(col.toLowerCase(), col);
                }
            }
        }
        String quizCol = firstExisting(cols, "quiz_id");
        String answerCol = firstExisting(cols, "selected_answer", "selected_option", "answer", "user_answer");
        if (quizCol == null || answerCol == null) {
            return null;
        }
        String userCol = firstExisting(cols, "user_id", "player_id");
        if (userCol == null) {
            return null;
        }
        UserColumnType userType = "player_id".equalsIgnoreCase(userCol) ? UserColumnType.PLAYER_ID : UserColumnType.USER_ID;
        String correctCol = firstExisting(cols, "is_correct", "correct");
        String attemptedAtCol = firstExisting(cols, "attempted_at", "created_at", "submitted_at");
        return new AttemptStorage(tableName, userCol, userType, quizCol, answerCol, correctCol, attemptedAtCol);
    }

    private String firstExisting(Map<String, String> columns, String... names) {
        for (String name : names) {
            String existing = columns.get(name.toLowerCase());
            if (existing != null) {
                return existing;
            }
        }
        return null;
    }

    private boolean attemptExists(AttemptStorage storage, String userId, int quizId) {
        String sql = "SELECT 1 FROM " + quoteIdentifier(storage.tableName) + " a WHERE " +
                buildUserFilter(storage) + " AND a." + quoteIdentifier(storage.quizColumn) + " = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            int nextIndex = bindUserFilter(ps, storage, userId, 1);
            ps.setInt(nextIndex, quizId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private String buildUserFilter(AttemptStorage storage) {
        if (storage.userType == UserColumnType.USER_ID) {
            return "a." + quoteIdentifier(storage.userColumn) + " = ?";
        }
        return "a." + quoteIdentifier(storage.userColumn) + " = (SELECT id FROM players WHERE user_id = ? LIMIT 1)";
    }

    private int bindUserFilter(PreparedStatement ps, AttemptStorage storage, String userId, int startIndex) throws SQLException {
        ps.setString(startIndex, userId);
        return startIndex + 1;
    }

    private String quoteIdentifier(String identifier) {
        return "`" + identifier + "`";
    }

    private void cacheAttempt(String userId, int quizId, char selectedAnswer, boolean isCorrect) {
        LOCAL_ATTEMPTS
                .computeIfAbsent(userId, key -> new ConcurrentHashMap<>())
                .put(quizId, new QuizAttemptResult(Character.toUpperCase(selectedAnswer), isCorrect));
    }

    private boolean hasCachedAttempt(String userId, int quizId) {
        Map<Integer, QuizAttemptResult> userAttempts = LOCAL_ATTEMPTS.get(userId);
        return userAttempts != null && userAttempts.containsKey(quizId);
    }

    private Map<Integer, QuizAttemptResult> getCachedAttempts(String userId) {
        Map<Integer, QuizAttemptResult> userAttempts = LOCAL_ATTEMPTS.get(userId);
        if (userAttempts == null) {
            return Map.of();
        }
        return new HashMap<>(userAttempts);
    }

    private boolean hasLocalAttempt(String userId, int quizId) {
        return loadLocalAttempts(userId).containsKey(quizId);
    }

    private Map<Integer, QuizAttemptResult> loadLocalAttempts(String userId) {
        Map<Integer, QuizAttemptResult> attempts = new HashMap<>();
        if (userId == null || userId.isBlank()) {
            return attempts;
        }

        Path file = resolveLocalAttemptFile(userId);
        if (file == null || !Files.exists(file)) {
            return attempts;
        }

        Properties props = new Properties();
        synchronized (LOCAL_ATTEMPT_FILE_LOCK) {
            try (InputStream input = Files.newInputStream(file)) {
                props.load(input);
            } catch (Exception e) {
                return attempts;
            }
        }

        for (String key : props.stringPropertyNames()) {
            try {
                int quizId = Integer.parseInt(key);
                String raw = props.getProperty(key, "").trim();
                String[] parts = raw.split("\\|", 2);
                if (parts.length == 0 || parts[0].isEmpty()) {
                    continue;
                }
                char selected = Character.toUpperCase(parts[0].charAt(0));
                boolean correct = parts.length > 1 && "1".equals(parts[1]);
                attempts.put(quizId, new QuizAttemptResult(selected, correct));
            } catch (NumberFormatException ignored) {
            }
        }
        return attempts;
    }

    private boolean saveLocalAttemptIfMissing(String userId, int quizId, char selectedAnswer, boolean isCorrect) {
        if (userId == null || userId.isBlank()) {
            return false;
        }
        Path file = resolveLocalAttemptFile(userId);
        if (file == null) {
            return false;
        }

        synchronized (LOCAL_ATTEMPT_FILE_LOCK) {
            Properties props = new Properties();
            try {
                if (Files.exists(file)) {
                    try (InputStream input = Files.newInputStream(file)) {
                        props.load(input);
                    }
                } else {
                    Files.createDirectories(file.getParent());
                }

                String key = String.valueOf(quizId);
                if (props.containsKey(key)) {
                    return true;
                }
                String value = Character.toUpperCase(selectedAnswer) + "|" + (isCorrect ? "1" : "0");
                props.setProperty(key, value);
                try (OutputStream output = Files.newOutputStream(file)) {
                    props.store(output, "Genex quiz attempts");
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    private Path resolveLocalAttemptFile(String userId) {
        try {
            String safeUserId = userId.replaceAll("[^a-zA-Z0-9._-]", "_");
            return Paths.get(System.getProperty("user.home"), LOCAL_ATTEMPT_DIR, "quiz_attempts_" + safeUserId + ".properties");
        } catch (Exception e) {
            return null;
        }
    }

    private enum UserColumnType {
        USER_ID,
        PLAYER_ID
    }

    private static class AttemptStorage {
        private final String tableName;
        private final String userColumn;
        private final UserColumnType userType;
        private final String quizColumn;
        private final String answerColumn;
        private final String correctColumn;
        private final String attemptedAtColumn;

        private AttemptStorage(String tableName, String userColumn, UserColumnType userType,
                               String quizColumn, String answerColumn, String correctColumn, String attemptedAtColumn) {
            this.tableName = tableName;
            this.userColumn = userColumn;
            this.userType = userType;
            this.quizColumn = quizColumn;
            this.answerColumn = answerColumn;
            this.correctColumn = correctColumn;
            this.attemptedAtColumn = attemptedAtColumn;
        }
    }

    private boolean incrementPlayerStats(String userId, boolean isCorrect, int xpReward) {
        String updateStats = "UPDATE players SET " +
                "total_attempts = total_attempts + 1, " +
                "correct_answers = correct_answers + ?, " +
                "tactical_xp = tactical_xp + ? " +
                "WHERE user_id = ?";
        try (PreparedStatement updatePs = cnx.prepareStatement(updateStats)) {
            updatePs.setInt(1, isCorrect ? 1 : 0);
            updatePs.setInt(2, isCorrect ? xpReward : 0);
            updatePs.setString(3, userId);
            int affected = updatePs.executeUpdate();
            if (affected > 0) {
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Failed updating player stats for user_id=" + userId + ": " + e.getMessage());
            return false;
        }

        if (!ensurePlayerRowExists(userId)) {
            return false;
        }

        try (PreparedStatement updatePs = cnx.prepareStatement(updateStats)) {
            updatePs.setInt(1, isCorrect ? 1 : 0);
            updatePs.setInt(2, isCorrect ? xpReward : 0);
            updatePs.setString(3, userId);
            return updatePs.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Failed updating player stats after creating row for user_id=" + userId + ": " + e.getMessage());
            return false;
        }
    }

    private boolean ensurePlayerRowExists(String userId) {
        String insertPlayerRow =
                "INSERT INTO players (first_name, last_name, nickname, cin, date_of_birth, nationality, city, user_id) " +
                "SELECT u.username, u.username, u.username, ?, CURRENT_DATE, 'UNSPECIFIED', 'UNSPECIFIED', u.id " +
                "FROM users u " +
                "WHERE u.id = ? AND NOT EXISTS (SELECT 1 FROM players p WHERE p.user_id = ?)";
        try (PreparedStatement ps = cnx.prepareStatement(insertPlayerRow)) {
            ps.setString(1, "AUTO-" + userId);
            ps.setString(2, userId);
            ps.setString(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed creating missing player row for user_id=" + userId + ": " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean isUniqueConstraintViolation(SQLException e) {
        SQLException current = e;
        while (current != null) {
            String sqlState = current.getSQLState();
            if (sqlState != null && sqlState.startsWith("23")) {
                return true;
            }
            current = current.getNextException();
        }
        return false;
    }

    public enum QuizSubmissionStatus {
        SAVED,
        ALREADY_SUBMITTED,
        SESSION_MISSING,
        FAILED
    }

    public static class QuizAttemptResult {
        private final char selectedAnswer;
        private final boolean correct;

        public QuizAttemptResult(char selectedAnswer, boolean correct) {
            this.selectedAnswer = selectedAnswer;
            this.correct = correct;
        }

        public char getSelectedAnswer() {
            return selectedAnswer;
        }

        public boolean isCorrect() {
            return correct;
        }
    }
}
