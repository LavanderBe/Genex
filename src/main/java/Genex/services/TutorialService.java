package Genex.services;

import Genex.entities.Tutorial;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TutorialService implements ICrud<Tutorial> {
    private final Connection cnx;

    public TutorialService() {
        cnx = Myconnection.getInstance().getCnx();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String query = "CREATE TABLE IF NOT EXISTS tutorial (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(255) NOT NULL," +
                "description TEXT," +
                "video_url VARCHAR(255)," +
                "category VARCHAR(100)," +
                "difficulty VARCHAR(50)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(query);
        } catch (SQLException e) {
            System.err.println("Error creating tutorial table: " + e.getMessage());
        }
    }

    @Override
    public void addEntity(Tutorial t) {
        String query = "INSERT INTO tutorial (title, description, video_url, category, difficulty) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setString(1, t.getTitle());
            pst.setString(2, t.getDescription());
            pst.setString(3, t.getVideo_url());
            pst.setString(4, t.getCategory());
            pst.setString(5, t.getDifficulty());
            pst.executeUpdate();
            System.out.println("Tutorial added successfully!");
        } catch (SQLException e) {
            System.err.println("Error adding tutorial: " + e.getMessage());
        }
    }

    @Override
    public void updateEntity(Tutorial t, String id) {
        String query = "UPDATE tutorial SET title = ?, description = ?, video_url = ?, category = ?, difficulty = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, t.getTitle());
            pst.setString(2, t.getDescription());
            pst.setString(3, t.getVideo_url());
            pst.setString(4, t.getCategory());
            pst.setString(5, t.getDifficulty());
            pst.setInt(6, Integer.parseInt(id));
            pst.executeUpdate();
            System.out.println("Tutorial updated successfully!");
        } catch (SQLException e) {
            System.err.println("Error updating tutorial: " + e.getMessage());
        }
    }

    @Override
    public void deleteEntity(Tutorial t) {
        String query = "DELETE FROM tutorial WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, t.getId());
            pst.executeUpdate();
            System.out.println("Tutorial deleted successfully!");
        } catch (SQLException e) {
            System.err.println("Error deleting tutorial: " + e.getMessage());
        }
    }

    @Override
    public void getEntity(Tutorial t) {
        // Implementation if single fetch is needed
    }

    public List<Tutorial> getAll() {
        List<Tutorial> list = new ArrayList<>();
        String query = "SELECT * FROM tutorial";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Tutorial t = new Tutorial();
                t.setId(rs.getInt("id"));
                t.setTitle(rs.getString("title"));
                t.setDescription(rs.getString("description"));
                t.setVideo_url(rs.getString("video_url"));
                t.setCategory(rs.getString("category"));
                t.setDifficulty(rs.getString("difficulty"));
                t.setCreated_at(rs.getTimestamp("created_at"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching tutorials: " + e.getMessage());
        }
        return list;
    }
}
