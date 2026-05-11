package Genex.services;

import Genex.entities.TutorialVideo;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrudTutorialVideo implements ICrud<TutorialVideo> {

    @Override
    public void addEntity(TutorialVideo v) {
        String q = "INSERT INTO tutorial_video (tutorial_id, position, title, video_url) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getTutorialId());
            ps.setInt(2, v.getPosition());
            ps.setString(3, v.getTitle());
            ps.setString(4, v.getVideoUrl());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) v.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(TutorialVideo v, String id) {
        String q = "UPDATE tutorial_video SET tutorial_id=?, position=?, title=?, video_url=? WHERE id=?";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setInt(1, v.getTutorialId());
            ps.setInt(2, v.getPosition());
            ps.setString(3, v.getTitle());
            ps.setString(4, v.getVideoUrl());
            ps.setInt(5, Integer.parseInt(id));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntity(TutorialVideo v) {
        String q = "DELETE FROM tutorial_video WHERE id=?";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setInt(1, v.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getEntity(TutorialVideo v) {
        String q = "SELECT * FROM tutorial_video WHERE id=?";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setInt(1, v.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                v.setTutorialId(rs.getInt("tutorial_id"));
                v.setPosition(rs.getInt("position"));
                v.setTitle(rs.getString("title"));
                v.setVideoUrl(rs.getString("video_url"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) v.setCreatedAt(ts.toLocalDateTime());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Liste les videos d'un tutoriel, ordonnees par position. */
    public List<TutorialVideo> getByTutorial(int tutorialId) {
        List<TutorialVideo> list = new ArrayList<>();
        String q = "SELECT * FROM tutorial_video WHERE tutorial_id=? ORDER BY position ASC, id ASC";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setInt(1, tutorialId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                list.add(new TutorialVideo(
                        rs.getInt("id"),
                        rs.getInt("tutorial_id"),
                        rs.getInt("position"),
                        rs.getString("title"),
                        rs.getString("video_url"),
                        ts != null ? ts.toLocalDateTime() : null
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public int countByTutorial(int tutorialId) {
        String q = "SELECT COUNT(*) FROM tutorial_video WHERE tutorial_id=?";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setInt(1, tutorialId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    public void deleteByTutorial(int tutorialId) {
        String q = "DELETE FROM tutorial_video WHERE tutorial_id=?";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setInt(1, tutorialId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Remplace l'ensemble des videos d'un tutoriel par la liste fournie.
       Simplification volontaire pour le flux edit cote admin : evite la
       gestion fine des ajouts/suppressions/reorderings. */
    public void replaceAll(int tutorialId, List<TutorialVideo> videos) {
        deleteByTutorial(tutorialId);
        int position = 1;
        for (TutorialVideo v : videos) {
            v.setId(0);
            v.setTutorialId(tutorialId);
            v.setPosition(position++);
            addEntity(v);
        }
    }
}
