package Genex.services;

import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class CrudPlayerVideoProgress {

    /* Marque (ou demarque) une video comme terminee pour un joueur.
       Utilise REPLACE INTO pour upsert sur la cle composee. */
    public void setCompleted(String playerId, int tutorialVideoId, boolean completed) {
        String q = "REPLACE INTO player_video_progress (player_id, tutorial_video_id, completed, completed_at) " +
                "VALUES (?, ?, ?, " + (completed ? "CURRENT_TIMESTAMP" : "NULL") + ")";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setString(1, playerId);
            ps.setInt(2, tutorialVideoId);
            ps.setInt(3, completed ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isCompleted(String playerId, int tutorialVideoId) {
        String q = "SELECT completed FROM player_video_progress WHERE player_id=? AND tutorial_video_id=?";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setString(1, playerId);
            ps.setInt(2, tutorialVideoId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt("completed") == 1;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /* Renvoie les ids des videos d'un tutoriel completees par le joueur. */
    public Set<Integer> getCompletedVideoIds(String playerId, int tutorialId) {
        Set<Integer> ids = new HashSet<>();
        String q = "SELECT pvp.tutorial_video_id " +
                "FROM player_video_progress pvp " +
                "JOIN tutorial_video tv ON tv.id = pvp.tutorial_video_id " +
                "WHERE pvp.player_id=? AND pvp.completed=1 AND tv.tutorial_id=?";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setString(1, playerId);
            ps.setInt(2, tutorialId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) ids.add(rs.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return ids;
    }

    /* Progression 0..100 sur un tutoriel pour un joueur. 0 si aucune video. */
    public int getProgressPercent(String playerId, int tutorialId) {
        String q = "SELECT " +
                "  (SELECT COUNT(*) FROM tutorial_video WHERE tutorial_id=?) AS total, " +
                "  (SELECT COUNT(*) FROM player_video_progress pvp " +
                "     JOIN tutorial_video tv ON tv.id = pvp.tutorial_video_id " +
                "     WHERE pvp.player_id=? AND pvp.completed=1 AND tv.tutorial_id=?) AS done";
        try (PreparedStatement ps = Myconnection.getInstance().getCnx().prepareStatement(q)) {
            ps.setInt(1, tutorialId);
            ps.setString(2, playerId);
            ps.setInt(3, tutorialId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                int done = rs.getInt("done");
                if (total <= 0) return 0;
                return (int) Math.round(100.0 * done / total);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
