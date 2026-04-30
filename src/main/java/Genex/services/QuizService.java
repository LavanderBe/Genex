package Genex.services;

import Genex.entities.Quiz;
import Genex.utils.Myconnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizService {
    private final Connection cnx;

    public QuizService() {
        cnx = Myconnection.getInstance().getCnx();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS quiz (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "tutorial_id INT," +
                "question TEXT NOT NULL," +
                "option_a TEXT NOT NULL," +
                "option_b TEXT NOT NULL," +
                "option_c TEXT NOT NULL," +
                "option_d TEXT NOT NULL," +
                "correct_answer VARCHAR(1) NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(query);

            // Safer: handle existing table without created_at
            try {
                st.executeUpdate("ALTER TABLE quiz ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP");
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            System.err.println("Error creating quiz table: " + e.getMessage());
        }
    }

    public List<Quiz> getAllEntities() {
        List<Quiz> list = new ArrayList<>();
        // Joining with tutorial table to get the title for display
        String query = "SELECT q.*, t.title as tutorial_title FROM quiz q " +
                "LEFT JOIN tutorial t ON q.tutorial_id = t.id " +
                "ORDER BY q.created_at DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Quiz q = new Quiz();
                q.setId(rs.getInt("id"));
                q.setTutorial_id(rs.getInt("tutorial_id"));
                q.setTutorial_name(rs.getString("tutorial_title"));
                q.setQuestion(rs.getString("question"));
                q.setOption_a(rs.getString("option_a"));
                q.setOption_b(rs.getString("option_b"));
                q.setOption_c(rs.getString("option_c"));
                q.setOption_d(rs.getString("option_d"));
                q.setCorrect_option(rs.getString("correct_answer"));
                list.add(q);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching quizzes: " + e.getMessage());
        }
        return list;
    }

    public void addEntity(Quiz q) {
        String query = "INSERT INTO quiz (tutorial_id, question, option_a, option_b, option_c, option_d, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, q.getTutorial_id());
            pst.setString(2, q.getQuestion());
            pst.setString(3, q.getOption_a());
            pst.setString(4, q.getOption_b());
            pst.setString(5, q.getOption_c());
            pst.setString(6, q.getOption_d());
            pst.setString(7, q.getCorrect_option());
            pst.executeUpdate();
            System.out.println("Quiz added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding quiz: " + e.getMessage());
        }
    }

    public void updateEntity(Quiz q, String id) {
        String query = "UPDATE quiz SET tutorial_id = ?, question = ?, option_a = ?, option_b = ?, option_c = ?, option_d = ?, correct_answer = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, q.getTutorial_id());
            pst.setString(2, q.getQuestion());
            pst.setString(3, q.getOption_a());
            pst.setString(4, q.getOption_b());
            pst.setString(5, q.getOption_c());
            pst.setString(6, q.getOption_d());
            pst.setString(7, q.getCorrect_option());
            pst.setInt(8, Integer.parseInt(id));
            pst.executeUpdate();
            System.out.println("Quiz updated successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating quiz: " + e.getMessage());
        }
    }

    public void deleteEntity(String id) {
        String query = "DELETE FROM quiz WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, Integer.parseInt(id));
            pst.executeUpdate();
            System.out.println("Quiz deleted successfully!");
        } catch (SQLException e) {
            System.err.println("Error deleting quiz: " + e.getMessage());
        }
    }
}
