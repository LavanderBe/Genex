package Genex.services;

import Genex.entities.Player;
import Genex.entities.Team;
import Genex.entities.TeamMessage;
import Genex.entities.TrainingAttendance;
import Genex.entities.TrainingSession;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrudTrainingAttendance {

    private static final int ABSENCE_LIMIT = 3;
    private static boolean tableEnsured = false;

    private final CrudTeamMember crudTeamMember = new CrudTeamMember();
    private final CrudTeamMessage crudTeamMessage = new CrudTeamMessage();

    public CrudTrainingAttendance() {
        ensureTable();
    }

    private void ensureTable() {
        if (tableEnsured) return;
        String query = "CREATE TABLE IF NOT EXISTS training_attendance (" +
                "id VARCHAR(36) PRIMARY KEY, " +
                "session_id VARCHAR(36) NOT NULL, " +
                "team_id VARCHAR(36) NOT NULL, " +
                "player_id VARCHAR(36) NOT NULL, " +
                "status ENUM('PRESENT','ABSENT') NOT NULL, " +
                "recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE KEY unique_session_player (session_id, player_id), " +
                "INDEX idx_attendance_team_player (team_id, player_id), " +
                "CONSTRAINT fk_attendance_session FOREIGN KEY (session_id) REFERENCES training_sessions(id) ON DELETE CASCADE, " +
                "CONSTRAINT fk_attendance_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE, " +
                "CONSTRAINT fk_attendance_player FOREIGN KEY (player_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")";

        try (Statement stmt = Myconnection.getInstance().getCnx().createStatement()) {
            stmt.executeUpdate(query);
            tableEnsured = true;
        } catch (SQLException e) {
            System.err.println("Error ensuring training_attendance table: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void markAttendance(String sessionId, String teamId, String playerId, TrainingAttendance.Status status) {
        String query = "INSERT INTO training_attendance (id, session_id, team_id, player_id, status, recorded_at) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE status=VALUES(status), recorded_at=VALUES(recorded_at)";

        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query)) {
            pst.setString(1, UUID.randomUUID().toString());
            pst.setString(2, sessionId);
            pst.setString(3, teamId);
            pst.setString(4, playerId);
            pst.setString(5, status.name());
            pst.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking attendance: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<TrainingAttendance.Status> getRecentPresence(String teamId, String playerId, int limit) {
        List<TrainingAttendance.Status> statuses = new ArrayList<>();
        String query = "SELECT ta.status " +
                "FROM training_sessions ts " +
                "LEFT JOIN training_attendance ta ON ta.session_id = ts.id AND ta.player_id = ? " +
                "WHERE ts.team_id = ? AND ts.status = 'COMPLETED' " +
                "ORDER BY ts.session_datetime DESC " +
                "LIMIT ?";

        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query)) {
            pst.setString(1, playerId);
            pst.setString(2, teamId);
            pst.setInt(3, limit);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                String status = rs.getString("status");
                statuses.add(status == null ? null : TrainingAttendance.Status.valueOf(status));
            }
        } catch (SQLException e) {
            System.err.println("Error loading recent presence: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return statuses;
    }

    public int getAbsenceCount(String teamId, String playerId) {
        String query = "SELECT COUNT(*) FROM training_attendance WHERE team_id=? AND player_id=? AND status='ABSENT'";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query)) {
            pst.setString(1, teamId);
            pst.setString(2, playerId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error counting absences: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
     * When a session is completed, any current member without a PRESENT record is marked ABSENT.
     * Players reaching three absences are removed from the team and a system chat message is posted.
     */
    public void finalizeMissingAttendance(TrainingSession session) {
        if (session == null || session.getId() == null || session.getTeamId() == null) return;
        if (session.getStatus() != TrainingSession.Status.COMPLETED) return;

        List<Player> members = crudTeamMember.getMembersByTeam(session.getTeamId());
        Team team = new CrudTeam().getEntity(session.getTeamId());
        String creatorId = team != null ? team.getCreatedBy() : null;
        for (Player member : members) {
            if (member.getId() == null) continue;
            if (member.getId().equals(creatorId)) continue;
            if (!hasAttendance(session.getId(), member.getId())) {
                markAttendance(session.getId(), session.getTeamId(), member.getId(), TrainingAttendance.Status.ABSENT);
            }
            kickIfNeeded(session.getTeamId(), member);
        }
    }

    private boolean hasAttendance(String sessionId, String playerId) {
        String query = "SELECT COUNT(*) FROM training_attendance WHERE session_id=? AND player_id=?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query)) {
            pst.setString(1, sessionId);
            pst.setString(2, playerId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking attendance: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return false;
    }

    private void kickIfNeeded(String teamId, Player player) {
        int absences = getAbsenceCount(teamId, player.getId());
        if (absences < ABSENCE_LIMIT) return;

        String playerName = player.getNickname() != null && !player.getNickname().isBlank()
                ? player.getNickname()
                : player.getUsername();

        crudTeamMember.removeMember(teamId, player.getId());
        deletePlayerAttendance(teamId, player.getId());

        TeamMessage message = new TeamMessage(teamId, player.getId(),
                playerName + " a ete retire de l'equipe apres 3 absences aux entrainements.");
        message.setMessageType(TeamMessage.MessageType.SYSTEM);
        crudTeamMessage.addMessage(message);
    }

    public void deletePlayerAttendance(String teamId, String playerId) {
        String query = "DELETE FROM training_attendance WHERE team_id=? AND player_id=?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query)) {
            pst.setString(1, teamId);
            pst.setString(2, playerId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting kicked player attendance: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
