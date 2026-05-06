package Genex.services;

import Genex.entities.TeamMember;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CrudTeamMember {

    public CrudTeamMember() {}

    /**
     * Add a player to a team
     */
    public void addMember(TeamMember member) {
        // Check if team already has 5 members
        if (getTeamMemberCount(member.getTeamId()) >= 5) {
            throw new RuntimeException("Team is full! Maximum 5 members allowed.");
        }
        
        String query = "INSERT INTO team_members (team_id, player_id, player_name, joined_at) VALUES (?, ?, ?, ?)";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, member.getTeamId());
            pst.setString(2, member.getPlayerId());
            pst.setString(3, member.getPlayerName());
            pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
            System.out.println("Team member added successfully: " + member.getPlayerName());
        } catch (SQLException e) {
            System.err.println("Error adding team member: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the number of members in a team
     */
    public int getTeamMemberCount(String teamId) {
        String query = "SELECT COUNT(*) as count FROM team_members WHERE team_id=?";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("Error getting team member count: " + e.getMessage());
            throw new RuntimeException(e);
        }
        
        return 0;
    }

    /**
     * Check if team is full (5 members)
     */
    public boolean isTeamFull(String teamId) {
        return getTeamMemberCount(teamId) >= 5;
    }

    /**
     * Get all members of a specific team
     */
    public List<TeamMember> getMembersByTeam(String teamId) {
        List<TeamMember> members = new ArrayList<>();
        String query = "SELECT * FROM team_members WHERE team_id=? ORDER BY joined_at ASC";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                TeamMember member = mapResultSetToMember(rs);
                members.add(member);
            }
            System.out.println("Loaded " + members.size() + " members for team " + teamId);
        } catch (SQLException e) {
            System.err.println("Error getting team members: " + e.getMessage());
            throw new RuntimeException(e);
        }
        
        return members;
    }

    /**
     * Get the team a player is currently in (returns null if not in any team)
     */
    public TeamMember getPlayerCurrentTeam(String playerId) {
        String query = "SELECT * FROM team_members WHERE player_id=? LIMIT 1";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, playerId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToMember(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting player's current team: " + e.getMessage());
            throw new RuntimeException(e);
        }
        
        return null;
    }

    /**
     * Check if a player is in a specific team
     */
    public boolean isPlayerInTeam(String playerId, String teamId) {
        String query = "SELECT COUNT(*) as count FROM team_members WHERE player_id=? AND team_id=?";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, playerId);
            pst.setString(2, teamId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if player is in team: " + e.getMessage());
            throw new RuntimeException(e);
        }
        
        return false;
    }

    /**
     * Check if a player is in ANY team
     */
    public boolean isPlayerInAnyTeam(String playerId) {
        String query = "SELECT COUNT(*) as count FROM team_members WHERE player_id=?";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, playerId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if player is in any team: " + e.getMessage());
            throw new RuntimeException(e);
        }
        
        return false;
    }

    /**
     * Remove a player from their team
     */
    public void removeMember(String playerId) {
        String query = "DELETE FROM team_members WHERE player_id=?";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, playerId);
            pst.executeUpdate();
            System.out.println("Player removed from team: " + playerId);
        } catch (SQLException e) {
            System.err.println("Error removing team member: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private TeamMember mapResultSetToMember(ResultSet rs) throws SQLException {
        TeamMember member = new TeamMember();
        member.setId(rs.getString("id"));
        member.setTeamId(rs.getString("team_id"));
        member.setPlayerId(rs.getString("player_id"));
        member.setPlayerName(rs.getString("player_name"));
        
        Timestamp joinedAt = rs.getTimestamp("joined_at");
        if (joinedAt != null) {
            member.setJoinedAt(joinedAt.toLocalDateTime());
        }
        
        return member;
    }
}
