package Genex.services;

import Genex.entities.Quiz;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizService implements ICrud<Quiz> {

    private Connection cnx;

    public QuizService() {
        cnx = Myconnection.getInstance().getCnx();
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
}