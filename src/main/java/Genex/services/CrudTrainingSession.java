package Genex.services;

import Genex.entities.TrainingSession;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CrudTrainingSession {

    public CrudTrainingSession() {
    }

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
        String deleteQuery = "DELETE FROM training_sessions WHERE id=?";
        try {
            PreparedStatement deletePst = Myconnection.getInstance().getCnx().prepareStatement(deleteQuery);
            deletePst.setString(1, id);
            deletePst.executeUpdate();
            System.out.println("Training session deleted successfully");
        } catch (SQLException e) {
            System.err.println("Error deleting session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if there's a time conflict with existing sessions for the same team on the same date
     * @param teamId The team ID
     * @param sessionDate The date of the session
     * @param startTime The start time
     * @param endTime The end time
     * @param excludeSessionId Optional session ID to exclude (for updates)
     * @return true if there's a conflict, false otherwise
     */
    public boolean hasTimeConflict(String teamId, LocalDateTime sessionDate, 
                                   java.time.LocalTime startTime, java.time.LocalTime endTime, 
                                   String excludeSessionId) {
        String query = "SELECT COUNT(*) as conflict_count FROM training_sessions " +
                "WHERE team_id = ? " +
                "AND DATE(session_datetime) = DATE(?) " +
                "AND id != ? " +
                "AND (" +
                "  (start_time <= ? AND end_time > ?) OR " +  // New session starts during existing
                "  (start_time < ? AND end_time >= ?) OR " +  // New session ends during existing
                "  (start_time >= ? AND end_time <= ?)" +     // New session wraps existing
                ")";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            pst.setTimestamp(2, Timestamp.valueOf(sessionDate));
            pst.setString(3, excludeSessionId != null ? excludeSessionId : "");
            
            // Check all overlap scenarios
            pst.setTime(4, Time.valueOf(startTime));
            pst.setTime(5, Time.valueOf(startTime));
            pst.setTime(6, Time.valueOf(endTime));
            pst.setTime(7, Time.valueOf(endTime));
            pst.setTime(8, Time.valueOf(startTime));
            pst.setTime(9, Time.valueOf(endTime));
            
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                int conflictCount = rs.getInt("conflict_count");
                if (conflictCount > 0) {
                    System.out.println("⚠️ Time conflict detected! " + conflictCount + " conflicting session(s) found.");
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error checking time conflict: " + e.getMessage());
            e.printStackTrace();
            return false; // In case of error, allow the operation
        }
    }

    /**
     * Get conflicting sessions for display to user
     */
    public List<TrainingSession> getConflictingSessions(String teamId, LocalDateTime sessionDate,
                                                        java.time.LocalTime startTime, java.time.LocalTime endTime,
                                                        String excludeSessionId) {
        List<TrainingSession> conflicts = new ArrayList<>();
        String query = "SELECT * FROM training_sessions " +
                "WHERE team_id = ? " +
                "AND DATE(session_datetime) = DATE(?) " +
                "AND id != ? " +
                "AND (" +
                "  (start_time <= ? AND end_time > ?) OR " +
                "  (start_time < ? AND end_time >= ?) OR " +
                "  (start_time >= ? AND end_time <= ?)" +
                ")";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            pst.setTimestamp(2, Timestamp.valueOf(sessionDate));
            pst.setString(3, excludeSessionId != null ? excludeSessionId : "");
            pst.setTime(4, Time.valueOf(startTime));
            pst.setTime(5, Time.valueOf(startTime));
            pst.setTime(6, Time.valueOf(endTime));
            pst.setTime(7, Time.valueOf(endTime));
            pst.setTime(8, Time.valueOf(startTime));
            pst.setTime(9, Time.valueOf(endTime));
            
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                TrainingSession session = mapResultSetToSession(rs);
                conflicts.add(session);
            }
        } catch (SQLException e) {
            System.err.println("Error getting conflicting sessions: " + e.getMessage());
            e.printStackTrace();
        }
        return conflicts;
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
