package Genex.services;

import Genex.entities.TutorialRating;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CrudTutorialRating {

    /* Upsert sur la PK (player_id, tutorial_id) : un joueur ne peut avoir
       qu'une seule note par tutoriel, modifiable. */
    public void upsert(TutorialRating r) {
        String q = "REPLACE INTO tutorial_rating (player_id, tutorial_id, stars, comment, created_at) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setString(1, r.getPlayerId());
            ps.setInt(2, r.getTutorialId());
            ps.setInt(3, r.getStars());
            ps.setString(4, r.getComment());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public TutorialRating getByPlayer(String playerId, int tutorialId) {
        String q = "SELECT * FROM tutorial_rating WHERE player_id=? AND tutorial_id=?";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setString(1, playerId);
            ps.setInt(2, tutorialId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("created_at");
                return new TutorialRating(
                        rs.getString("player_id"),
                        rs.getInt("tutorial_id"),
                        rs.getInt("stars"),
                        rs.getString("comment"),
                        ts != null ? ts.toLocalDateTime() : null
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public double getAverage(int tutorialId) {
        String q = "SELECT AVG(stars) FROM tutorial_rating WHERE tutorial_id=?";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setInt(1, tutorialId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0.0;
    }

    public int getCount(int tutorialId) {
        String q = "SELECT COUNT(*) FROM tutorial_rating WHERE tutorial_id=?";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setInt(1, tutorialId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
