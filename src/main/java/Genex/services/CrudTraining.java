package Genex.services;

import Genex.entities.Training;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CrudTraining implements ICrud<Training> {

    public CrudTraining() {}

    @Override
    public void addEntity(Training t) {

        String requete = "INSERT INTO trainings " +
                "(team_id, title, type, scheduled_at, duration_minutes, notes, opponent_team_id) " +
                "VALUES (?,?,?,?,?,?,?)";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getTeamId());
            pst.setString(2, t.getTitle());
            pst.setString(3, t.getType().name());
            pst.setString(4, t.getScheduledAt().toString());
            pst.setInt(5, t.getDurationMinutes());
            pst.setString(6, t.getNotes());
            pst.setString(7, t.getOpponentTeamId()); // null if not scrim
            pst.executeUpdate();
            System.out.println("Training added successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(Training t, String id) {

        String requete = "UPDATE trainings SET " +
                "team_id=?, title=?, type=?, scheduled_at=?, " +
                "duration_minutes=?, notes=?, opponent_team_id=? " +
                "WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getTeamId());
            pst.setString(2, t.getTitle());
            pst.setString(3, t.getType().name());
            pst.setString(4, t.getScheduledAt().toString());
            pst.setInt(5, t.getDurationMinutes());
            pst.setString(6, t.getNotes());
            pst.setString(7, t.getOpponentTeamId());
            pst.setString(8, id);
            pst.executeUpdate();
            System.out.println("Training updated successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntity(Training t) {

        String requete = "DELETE FROM trainings WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getId());
            pst.executeUpdate();
            System.out.println("Training deleted successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getEntity(Training t) {

        String requete = "SELECT * FROM trainings WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getId());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                t.setTeamId(rs.getString("team_id"));
                t.setTitle(rs.getString("title"));
                t.setType(Training.Type.valueOf(rs.getString("type")));
                t.setScheduledAt(rs.getTimestamp("scheduled_at").toLocalDateTime());
                t.setDurationMinutes(rs.getInt("duration_minutes"));
                t.setNotes(rs.getString("notes"));
                t.setOpponentTeamId(rs.getString("opponent_team_id"));
                System.out.println("Training loaded");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Get all trainings for a specific team
    public List<Training> getAll(String teamId) {

        List<Training> list = new ArrayList<>();
        String requete = "SELECT * FROM trainings WHERE team_id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, teamId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Training t = new Training();
                t.setId(rs.getString("id"));
                t.setTeamId(rs.getString("team_id"));
                t.setTitle(rs.getString("title"));
                t.setType(Training.Type.valueOf(rs.getString("type")));
                t.setScheduledAt(rs.getTimestamp("scheduled_at").toLocalDateTime());
                t.setDurationMinutes(rs.getInt("duration_minutes"));
                t.setNotes(rs.getString("notes"));
                t.setOpponentTeamId(rs.getString("opponent_team_id"));
                list.add(t);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}