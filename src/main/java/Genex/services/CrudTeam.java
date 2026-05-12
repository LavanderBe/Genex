package Genex.services;

import Genex.entities.Team;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrudTeam {

    public CrudTeam() {}

    public void addEntity(Team team) {
        String query = "INSERT INTO teams (id, created_by, game_id, name, logo_image, jersey_image, contact, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            // Generate UUID for the team
            String teamId = UUID.randomUUID().toString();
            team.setId(teamId);
            
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            pst.setString(2, team.getCreatedBy());
            pst.setString(3, team.getGameId());
            pst.setString(4, team.getName());
            pst.setString(5, team.getLogoImage());
            pst.setString(6, team.getJerseyImage());
            pst.setString(7, team.getContact());
            pst.setString(8, team.getStatus() != null ? team.getStatus().name() : null);
            pst.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
            System.out.println("Team added successfully with ID: " + teamId);
            System.out.println("Logo: " + team.getLogoImage());
            System.out.println("Jersey: " + team.getJerseyImage());
        } catch (SQLException e) {
            System.err.println("Error adding team: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void updateEntity(Team team, String id) {
        String query = "UPDATE teams SET created_by=?, game_id=?, name=?, logo_image=?, jersey_image=?, contact=?, status=? " +
                "WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, team.getCreatedBy());
            pst.setString(2, team.getGameId());
            pst.setString(3, team.getName());
            pst.setString(4, team.getLogoImage());
            pst.setString(5, team.getJerseyImage());
            pst.setString(6, team.getContact());
            pst.setString(7, team.getStatus() != null ? team.getStatus().name() : null);
            pst.setString(8, id);
            pst.executeUpdate();
            System.out.println("Team updated successfully");
        } catch (SQLException e) {
            System.err.println("Error updating team: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteEntity(Team team) {
        try {
            Connection conn = Myconnection.getInstance().getCnx();
            
            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // 1. First, delete all team members
                String deleteMembersQuery = "DELETE FROM team_members WHERE team_id=?";
                PreparedStatement pstMembers = conn.prepareStatement(deleteMembersQuery);
                pstMembers.setString(1, team.getId());
                int membersDeleted = pstMembers.executeUpdate();
                System.out.println("✅ Deleted " + membersDeleted + " team members");
                
                // 2. Delete all training notifications for this team's sessions
                String deleteNotificationsQuery = "DELETE FROM training_notifications WHERE session_id IN " +
                        "(SELECT id FROM training_sessions WHERE team_id=?)";
                PreparedStatement pstNotifications = conn.prepareStatement(deleteNotificationsQuery);
                pstNotifications.setString(1, team.getId());
                int notificationsDeleted = pstNotifications.executeUpdate();
                System.out.println("✅ Deleted " + notificationsDeleted + " training notifications");
                
                // 3. Delete all training sessions for this team
                String deleteSessionsQuery = "DELETE FROM training_sessions WHERE team_id=?";
                PreparedStatement pstSessions = conn.prepareStatement(deleteSessionsQuery);
                pstSessions.setString(1, team.getId());
                int sessionsDeleted = pstSessions.executeUpdate();
                System.out.println("✅ Deleted " + sessionsDeleted + " training sessions");
                
                // 4. Delete all team messages
                String deleteMessagesQuery = "DELETE FROM team_messages WHERE team_id=?";
                PreparedStatement pstMessages = conn.prepareStatement(deleteMessagesQuery);
                pstMessages.setString(1, team.getId());
                int messagesDeleted = pstMessages.executeUpdate();
                System.out.println("✅ Deleted " + messagesDeleted + " team messages");
                
                // 5. Finally, delete the team itself
                String deleteTeamQuery = "DELETE FROM teams WHERE id=?";
                PreparedStatement pstTeam = conn.prepareStatement(deleteTeamQuery);
                pstTeam.setString(1, team.getId());
                pstTeam.executeUpdate();
                System.out.println("✅ Team deleted successfully: " + team.getName());
                
                // Commit transaction
                conn.commit();
                
            } catch (SQLException e) {
                // Rollback on error
                conn.rollback();
                System.err.println("❌ Error deleting team, rolled back transaction");
                throw e;
            } finally {
                // Restore auto-commit
                conn.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting team: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public Team getEntity(String id) {
        String query = "SELECT * FROM teams WHERE id=?";
        Team team = null;

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                team = mapResultSetToTeam(rs);
                System.out.println("Team loaded: " + team.getName());
            }
        } catch (SQLException e) {
            System.err.println("Error getting team: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return team;
    }

    public List<Team> getAll() {
        List<Team> list = new ArrayList<>();
        String query = "SELECT * FROM teams ORDER BY created_at DESC";

        try {
            Statement st = Myconnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                Team team = mapResultSetToTeam(rs);
                list.add(team);
            }
            System.out.println("Loaded " + list.size() + " teams");
        } catch (SQLException e) {
            System.err.println("Error getting all teams: " + e.getMessage());
            throw new RuntimeException(e);
        }

        return list;
    }

    private Team mapResultSetToTeam(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setId(rs.getString("id"));
        team.setCreatedBy(rs.getString("created_by"));
        team.setGameId(rs.getString("game_id"));
        team.setName(rs.getString("name"));
        team.setLogoImage(rs.getString("logo_image"));
        team.setJerseyImage(rs.getString("jersey_image"));
        team.setContact(rs.getString("contact"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            team.setStatus(Team.Status.valueOf(statusStr));
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            team.setCreatedAt(createdAt.toLocalDateTime());
        }

        return team;
    }
}