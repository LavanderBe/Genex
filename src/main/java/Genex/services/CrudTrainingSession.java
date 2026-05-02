package Genex.services;

import Genex.entities.TrainingSession;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CrudTrainingSession {

    public CrudTrainingSession() {}

    public List<TrainingSession> getAllSessions() {
        List<TrainingSession> sessions = new ArrayList<>();
        String query = "SELECT * FROM training_sessions ORDER BY session_datetime DESC";

        try {
            Statement stmt = Myconnection.getInstance().getCnx().createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                TrainingSession session = mapResultSetToSession(rs);
                sessions.add(session);
            }
            System.out.println("Loaded " + sessions.size() + " training sessions");
        } catch (SQLException e) {
            System.err.println("Error getting all sessions: " + e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }

    public List<TrainingSession> getSessionsByTeam(String teamId) {
        List<TrainingSession> sessions = new ArrayList<>();
        String query = "SELECT * FROM training_sessions WHERE team_id=? ORDER BY session_datetime DESC";

        try {
            System.out.println("Querying sessions for team_id: " + teamId);
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                TrainingSession session = mapResultSetToSession(rs);
                System.out.println("Found session: " + session.getTitle() + " (ID: " + session.getId() + ")");
                sessions.add(session);
            }
            System.out.println("Loaded " + sessions.size() + " sessions for team " + teamId);
        } catch (SQLException e) {
            System.err.println("Error getting sessions by team: " + e.getMessage());
            e.printStackTrace();
        }
        return sessions;
    }

    public void addSession(TrainingSession session) {
        String query = "INSERT INTO training_sessions (team_id, title, type, " +
                "session_datetime, start_time, end_time, location, notes, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, session.getTeamId());
            pst.setString(2, session.getTitle());
            pst.setString(3, session.getType() != null ? session.getType().name() : null);
            pst.setTimestamp(4, session.getSessionDatetime() != null ? Timestamp.valueOf(session.getSessionDatetime()) : null);
            pst.setTime(5, session.getStartTime() != null ? Time.valueOf(session.getStartTime()) : null);
            pst.setTime(6, session.getEndTime() != null ? Time.valueOf(session.getEndTime()) : null);
            pst.setString(7, session.getLocation());
            pst.setString(8, session.getNotes());
            pst.setString(9, session.getStatus() != null ? session.getStatus().name() : null);

            int rowsAffected = pst.executeUpdate();
            System.out.println("Training session added successfully. Rows affected: " + rowsAffected);
            System.out.println("Session details - Team ID: " + session.getTeamId() + ", Title: " + session.getTitle());
        } catch (SQLException e) {
            System.err.println("Error adding session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateSession(TrainingSession session) {
        String query = "UPDATE training_sessions SET team_id=?, title=?, type=?, " +
                "session_datetime=?, start_time=?, end_time=?, location=?, notes=?, status=? WHERE id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, session.getTeamId());
            pst.setString(2, session.getTitle());
            pst.setString(3, session.getType() != null ? session.getType().name() : null);
            pst.setTimestamp(4, session.getSessionDatetime() != null ? Timestamp.valueOf(session.getSessionDatetime()) : null);
            pst.setTime(5, session.getStartTime() != null ? Time.valueOf(session.getStartTime()) : null);
            pst.setTime(6, session.getEndTime() != null ? Time.valueOf(session.getEndTime()) : null);
            pst.setString(7, session.getLocation());
            pst.setString(8, session.getNotes());
            pst.setString(9, session.getStatus() != null ? session.getStatus().name() : null);
            pst.setString(10, session.getId());
            pst.executeUpdate();
            System.out.println("Training session updated successfully");
        } catch (SQLException e) {
            System.err.println("Error updating session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteSession(String id) {
        String query = "DELETE FROM training_sessions WHERE id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, id);
            pst.executeUpdate();
            System.out.println("Training session deleted successfully");
        } catch (SQLException e) {
            System.err.println("Error deleting session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private TrainingSession mapResultSetToSession(ResultSet rs) throws SQLException {
        TrainingSession session = new TrainingSession();
        session.setId(rs.getString("id"));
        session.setTeamId(rs.getString("team_id"));
        session.setTitle(rs.getString("title"));

        String typeStr = rs.getString("type");
        if (typeStr != null) {
            session.setType(TrainingSession.Type.valueOf(typeStr));
        }

        Timestamp datetime = rs.getTimestamp("session_datetime");
        if (datetime != null) {
            session.setSessionDatetime(datetime.toLocalDateTime());
        }

        Time startTime = rs.getTime("start_time");
        if (startTime != null) {
            session.setStartTime(startTime.toLocalTime());
        }

        Time endTime = rs.getTime("end_time");
        if (endTime != null) {
            session.setEndTime(endTime.toLocalTime());
        }

        session.setLocation(rs.getString("location"));
        session.setNotes(rs.getString("notes"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            session.setStatus(TrainingSession.Status.valueOf(statusStr));
        }

        return session;
    }
}