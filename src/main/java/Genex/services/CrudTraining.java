package Genex.services;

import Genex.entities.TrainingSession;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrudTraining implements ICrud<TrainingSession> {

    public CrudTraining() {}

    // ✅ CREATE
    @Override
    public void addEntity(TrainingSession s) {

        String sql = "INSERT INTO training_session (team_id, created_by, titre, type, session_datetime, duration_minutes, location_type, centre_id, notes, statut) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(sql);

            pst.setInt(1, s.getTeamId());
            pst.setInt(2, s.getCreatedBy());
            pst.setString(3, s.getTitre());
            pst.setString(4, s.getType());
            pst.setTimestamp(5, Timestamp.valueOf(s.getSessionDateTime()));
            pst.setInt(6, s.getDurationMinutes());
            pst.setString(7, s.getLocationType());

            if (s.getCentreId() != null)
                pst.setInt(8, s.getCentreId());
            else
                pst.setNull(8, Types.INTEGER);

            pst.setString(9, s.getNotes());
            pst.setString(10, s.getStatut());

            pst.executeUpdate();
            System.out.println("✅ Session added");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ UPDATE
    @Override
    public void updateEntity(TrainingSession s, String id) {

        String sql = "UPDATE training_session SET titre=?, type=?, session_datetime=?, duration_minutes=?, location_type=?, centre_id=?, notes=?, statut=? WHERE session_id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(sql);

            pst.setString(1, s.getTitre());
            pst.setString(2, s.getType());
            pst.setTimestamp(3, Timestamp.valueOf(s.getSessionDateTime()));
            pst.setInt(4, s.getDurationMinutes());
            pst.setString(5, s.getLocationType());

            if (s.getCentreId() != null)
                pst.setInt(6, s.getCentreId());
            else
                pst.setNull(6, Types.INTEGER);


            pst.setString(7, s.getNotes());
            pst.setString(8, s.getStatut());
            pst.setInt(9, Integer.parseInt(id));

            pst.executeUpdate();
            System.out.println("✅ Session updated");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ❌ DELETE (with rule)
    @Override
    public void deleteEntity(TrainingSession s) {

        try {
            String check = "SELECT statut FROM training_session WHERE session_id=?";
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(check);
            pst.setInt(1, s.getSessionId());

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String statut = rs.getString("statut");

                if (!statut.equals("planifie") && !statut.equals("annule")) {
                    System.out.println("❌ Cannot delete this session");
                    return;
                }
            }

            String delete = "DELETE FROM training_session WHERE session_id=?";
            pst = Myconnection.getInstance().getCnx().prepareStatement(delete);
            pst.setInt(1, s.getSessionId());
            pst.executeUpdate();

            System.out.println("🗑 Session deleted");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ READ ONE
    @Override
    public void getEntity(TrainingSession s) {

        String sql = "SELECT * FROM training_session WHERE session_id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, s.getSessionId());

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                s.setTeamId(rs.getInt("team_id"));
                s.setCreatedBy(rs.getInt("created_by"));
                s.setTitre(rs.getString("titre"));
                s.setType(rs.getString("type"));
                s.setSessionDateTime(rs.getTimestamp("session_datetime").toLocalDateTime());
                s.setDurationMinutes(rs.getInt("duration_minutes"));
                s.setLocationType(rs.getString("location_type"));
                s.setCentreId((Integer) rs.getObject("centre_id"));
                s.setNotes(rs.getString("notes"));
                s.setStatut(rs.getString("statut"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ READ ALL
    public List<TrainingSession> getAll() {

        List<TrainingSession> list = new ArrayList<>();
        String sql = "SELECT * FROM training_session";

        try {
            Statement st = Myconnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                TrainingSession s = new TrainingSession();

                s.setSessionId(rs.getInt("session_id"));
                s.setTeamId(rs.getInt("team_id"));
                s.setCreatedBy(rs.getInt("created_by"));
                s.setTitre(rs.getString("titre"));
                s.setType(rs.getString("type"));
                s.setSessionDateTime(rs.getTimestamp("session_datetime").toLocalDateTime());
                s.setDurationMinutes(rs.getInt("duration_minutes"));
                s.setLocationType(rs.getString("location_type"));
                s.setCentreId((Integer) rs.getObject("centre_id"));
                s.setNotes(rs.getString("notes"));
                s.setStatut(rs.getString("statut"));

                list.add(s);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }
}