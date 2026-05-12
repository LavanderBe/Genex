package Genex.services;

import Genex.entities.TrainingSession;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CrudTrainingSession {

    private GoogleCalendarService calendarService;
    private TrainingNotificationService notificationService;
    private CrudTeamMember crudTeamMember;

    public CrudTrainingSession() {
        try {
            calendarService = new GoogleCalendarService();
            if (!calendarService.isInitialized()) {
                System.err.println("Warning: Google Calendar Service not initialized. Calendar sync will be disabled.");
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize Google Calendar Service: " + e.getMessage());
            calendarService = null;
        }
        
        notificationService = new TrainingNotificationService();
        crudTeamMember = new CrudTeamMember();
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
        String query = "INSERT INTO training_sessions (id, team_id, title, type, " +
                "session_datetime, start_time, end_time, notes, status, calendar_event_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            // Generate UUID for the session
            if (session.getId() == null || session.getId().isEmpty()) {
                session.setId(java.util.UUID.randomUUID().toString());
            }
            
            // Create calendar event first
            String calendarEventId = null;
            if (calendarService != null && calendarService.isInitialized()) {
                try {
                    calendarEventId = calendarService.createEvent(session);
                    session.setCalendarEventId(calendarEventId);
                    System.out.println("Calendar event created with ID: " + calendarEventId);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to create calendar event: " + e.getMessage());
                    // Continue with database operation even if calendar fails
                }
            }

            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, session.getId());
            pst.setString(2, session.getTeamId());
            pst.setString(3, session.getTitle());
            pst.setString(4, session.getType() != null ? session.getType().name() : null);
            pst.setTimestamp(5, session.getSessionDatetime() != null ? Timestamp.valueOf(session.getSessionDatetime()) : null);
            pst.setTime(6, session.getStartTime() != null ? Time.valueOf(session.getStartTime()) : null);
            pst.setTime(7, session.getEndTime() != null ? Time.valueOf(session.getEndTime()) : null);
            pst.setString(8, session.getNotes());
            pst.setString(9, session.getStatus() != null ? session.getStatus().name() : null);
            pst.setString(10, calendarEventId);

            int rowsAffected = pst.executeUpdate();
            System.out.println("Training session added successfully. Rows affected: " + rowsAffected);
            System.out.println("Session details - ID: " + session.getId() + ", Team ID: " + session.getTeamId() + ", Title: " + session.getTitle());
            
            // Send notifications to all team members
            notificationService.notifyNewTrainingSession(session);
            
        } catch (SQLException e) {
            System.err.println("Error adding session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateSession(TrainingSession session) {
        String query = "UPDATE training_sessions SET team_id=?, title=?, type=?, " +
                "session_datetime=?, start_time=?, end_time=?, notes=?, status=?, calendar_event_id=? WHERE id=?";
        try {
            // Update calendar event
            if (calendarService != null && calendarService.isInitialized() && session.getCalendarEventId() != null) {
                try {
                    boolean updated = calendarService.updateEvent(session);
                    if (updated) {
                        System.out.println("Calendar event updated successfully");
                    }
                } catch (Exception e) {
                    System.err.println("Warning: Failed to update calendar event: " + e.getMessage());
                    // Continue with database operation even if calendar fails
                }
            }

            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, session.getTeamId());
            pst.setString(2, session.getTitle());
            pst.setString(3, session.getType() != null ? session.getType().name() : null);
            pst.setTimestamp(4, session.getSessionDatetime() != null ? Timestamp.valueOf(session.getSessionDatetime()) : null);
            pst.setTime(5, session.getStartTime() != null ? Time.valueOf(session.getStartTime()) : null);
            pst.setTime(6, session.getEndTime() != null ? Time.valueOf(session.getEndTime()) : null);
            pst.setString(7, session.getNotes());
            pst.setString(8, session.getStatus() != null ? session.getStatus().name() : null);
            pst.setString(9, session.getCalendarEventId());
            pst.setString(10, session.getId());
            pst.executeUpdate();
            System.out.println("Training session updated successfully");
        } catch (SQLException e) {
            System.err.println("Error updating session: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteSession(String id) {
        // First, get the session to retrieve calendar_event_id
        TrainingSession session = getSessionById(id);
        
        // Delete from calendar if event ID exists
        if (session != null && calendarService != null && calendarService.isInitialized() && session.getCalendarEventId() != null) {
            try {
                boolean deleted = calendarService.deleteEvent(session.getCalendarEventId());
                if (deleted) {
                    System.out.println("Calendar event deleted successfully");
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to delete calendar event: " + e.getMessage());
                // Continue with database operation even if calendar fails
            }
        }

        // Delete from database
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

    /**
     * Get a single session by ID
     */
    public TrainingSession getSessionById(String id) {
        String query = "SELECT * FROM training_sessions WHERE id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapResultSetToSession(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting session by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
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

    /**
     * Auto-update past sessions to COMPLETED status
     * Marks all PLANNED or ONGOING sessions that have passed as COMPLETED
     * @return Number of sessions updated
     */
    public int autoUpdatePastSessions() {
        String query = "UPDATE training_sessions " +
                "SET status = 'COMPLETED' " +
                "WHERE status IN ('PLANNED', 'ONGOING') " +
                "AND CONCAT(session_datetime, ' ', end_time) < NOW()";
        
        try {
            Statement stmt = Myconnection.getInstance().getCnx().createStatement();
            int updatedCount = stmt.executeUpdate(query);
            
            if (updatedCount > 0) {
                System.out.println("✅ Auto-updated " + updatedCount + " past session(s) to COMPLETED");
            }
            
            return updatedCount;
        } catch (SQLException e) {
            System.err.println("Error auto-updating past sessions: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Auto-update past sessions for a specific team
     * @param teamId The team ID
     * @return Number of sessions updated
     */
    public int autoUpdatePastSessionsForTeam(String teamId) {
        String query = "UPDATE training_sessions " +
                "SET status = 'COMPLETED' " +
                "WHERE team_id = ? " +
                "AND status IN ('PLANNED', 'ONGOING') " +
                "AND CONCAT(session_datetime, ' ', end_time) < NOW()";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            int updatedCount = pst.executeUpdate();
            
            if (updatedCount > 0) {
                System.out.println("✅ Auto-updated " + updatedCount + " past session(s) to COMPLETED for team " + teamId);
            }
            
            return updatedCount;
        } catch (SQLException e) {
            System.err.println("Error auto-updating past sessions for team: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Get sessions that should be marked as completed (past sessions still in PLANNED/ONGOING)
     * @param teamId The team ID (optional, null for all teams)
     * @return List of sessions that need status update
     */
    public List<TrainingSession> getPastSessionsNeedingUpdate(String teamId) {
        List<TrainingSession> sessions = new ArrayList<>();
        String query = "SELECT * FROM training_sessions " +
                "WHERE status IN ('PLANNED', 'ONGOING') " +
                "AND CONCAT(session_datetime, ' ', end_time) < NOW()";
        
        if (teamId != null) {
            query += " AND team_id = ?";
        }
        
        query += " ORDER BY session_datetime DESC";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            if (teamId != null) {
                pst.setString(1, teamId);
            }
            
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                TrainingSession session = mapResultSetToSession(rs);
                sessions.add(session);
            }
            
            if (!sessions.isEmpty()) {
                System.out.println("⚠️ Found " + sessions.size() + " past session(s) that need status update");
            }
        } catch (SQLException e) {
            System.err.println("Error getting past sessions needing update: " + e.getMessage());
            e.printStackTrace();
        }
        
        return sessions;
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

        session.setNotes(rs.getString("notes"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            session.setStatus(TrainingSession.Status.valueOf(statusStr));
        }

        // Get calendar_event_id
        session.setCalendarEventId(rs.getString("calendar_event_id"));

        return session;
    }
}