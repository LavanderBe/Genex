package Genex.services;

import Genex.entities.Tutorial;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TutorialService implements ICrud<Tutorial> {

    private Connection cnx;

    public TutorialService() {
        try {
            cnx = Myconnection.getInstance().getCnx();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Database connection is unavailable.", e);
        }
    }

    @Override
    public void addEntity(Tutorial tutorial) {
        String query = "INSERT INTO tutorial (title, description, video_url, category, difficulty, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, tutorial.getTitle());
            ps.setString(2, tutorial.getDescription());
            ps.setString(3, tutorial.getVideoUrl());
            ps.setString(4, tutorial.getCategory());
            ps.setString(5, tutorial.getDifficulty());
            ps.setDate(6, Date.valueOf(tutorial.getCreatedAt() != null ? tutorial.getCreatedAt() : LocalDate.now()));
            ps.executeUpdate();
            System.out.println("Tutorial added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateEntity(Tutorial tutorial, String id) {
        String query = "UPDATE tutorial SET title=?, description=?, video_url=?, category=?, difficulty=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, tutorial.getTitle());
            ps.setString(2, tutorial.getDescription());
            ps.setString(3, tutorial.getVideoUrl());
            ps.setString(4, tutorial.getCategory());
            ps.setString(5, tutorial.getDifficulty());
            ps.setInt(6, Integer.parseInt(id));
            ps.executeUpdate();
            System.out.println("Tutorial updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteEntity(Tutorial tutorial) {
        String query = "DELETE FROM tutorial WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, tutorial.getId());
            ps.executeUpdate();
            System.out.println("Tutorial deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getEntity(Tutorial tutorial) {
        String query = "SELECT * FROM tutorial WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, tutorial.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                tutorial.setTitle(rs.getString("title"));
                tutorial.setDescription(rs.getString("description"));
                tutorial.setVideoUrl(rs.getString("video_url"));
                tutorial.setCategory(rs.getString("category"));
                tutorial.setDifficulty(rs.getString("difficulty"));
                tutorial.setCreatedAt(rs.getDate("created_at").toLocalDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Tutorial> getAllTutorials() {
        List<Tutorial> list = new ArrayList<>();
        String query = "SELECT * FROM tutorial ORDER BY created_at DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Tutorial t = new Tutorial(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("video_url"),
                        rs.getString("category"),
                        rs.getString("difficulty"),
                        rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null
                );
                list.add(t);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load tutorials from database.", e);
        }
        return list;
    }

    public List<Tutorial> searchTutorials(String keyword) {
        List<Tutorial> list = new ArrayList<>();
        String query = "SELECT * FROM tutorial WHERE title LIKE ? OR category LIKE ? OR difficulty LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Tutorial t = new Tutorial(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("video_url"),
                        rs.getString("category"),
                        rs.getString("difficulty"),
                        rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null
                );
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Enhancement: Returns tutorials with calculated progression for a player.
     * Requires a 'player_progress' table.
     */
    public List<Tutorial> getAllTutorialsWithProgression(String playerId) {
        List<Tutorial> list = new ArrayList<>();
        String query = "SELECT t.*, COALESCE(p.progress_percent, 0) as progress " +
                "FROM tutorial t " +
                "LEFT JOIN player_progress p ON t.id = p.tutorial_id AND p.player_id = ? " +
                "ORDER BY t.created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, playerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Tutorial t = new Tutorial(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("video_url"),
                        rs.getString("category"),
                        rs.getString("difficulty"),
                        rs.getDate("created_at") != null ? rs.getDate("created_at").toLocalDate() : null
                );
                // Note: If you want to store progress in the Tutorial entity, add a progress field.
                // For now, we return the base list. In a real app, you'd wrap this or add the field.
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
