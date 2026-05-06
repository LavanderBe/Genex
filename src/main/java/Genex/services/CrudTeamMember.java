package Genex.services;

import Genex.entities.Player;
import Genex.entities.Team;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrudTeamMember {

    public static final int MAX_MEMBERS = 5;

    public CrudTeamMember() {}

    // ── isMember ─────────────────────────────────────────────────────
    public boolean isMember(String teamId, String playerId) {
        String query = "SELECT COUNT(*) FROM team_members WHERE team_id=? AND player_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            pst.setString(2, playerId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error checking membership: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return false;
    }

    // ── getMemberCount ───────────────────────────────────────────────
    public int getMemberCount(String teamId) {
        String query = "SELECT COUNT(*) FROM team_members WHERE team_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error getting member count: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return 0;
    }

    // ── addMember ────────────────────────────────────────────────────
    public void addMember(String teamId, String playerId) {
        if (teamId == null || playerId == null) {
            throw new IllegalArgumentException("teamId and playerId must not be null");
        }
        // No-op if already a member
        if (isMember(teamId, playerId)) {
            System.out.println("Player is already a member of this team — no-op");
            return;
        }
        // Enforce max members
        if (getMemberCount(teamId) >= MAX_MEMBERS) {
            throw new IllegalStateException("Team is full");
        }
        String query = "INSERT INTO team_members (id, team_id, player_id, joined_at) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, UUID.randomUUID().toString());
            pst.setString(2, teamId);
            pst.setString(3, playerId);
            pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
            System.out.println("Member added to team: " + teamId);
        } catch (SQLException e) {
            System.err.println("Error adding member: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ── getMembersByTeam ─────────────────────────────────────────────
    // JOINs team_members → users → players to build full Player objects
    public List<Player> getMembersByTeam(String teamId) {
        List<Player> members = new ArrayList<>();
        String query =
            "SELECT u.id, u.username, u.email, u.role, " +
            "       p.first_name, p.last_name, p.nickname, p.cin, p.date_of_birth, p.nationality, p.city " +
            "FROM team_members tm " +
            "JOIN users u ON u.id = tm.player_id " +
            "JOIN players p ON p.user_id = u.id " +
            "WHERE tm.team_id = ?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Player player = new Player();
                player.setId(rs.getString("id"));
                player.setUsername(rs.getString("username"));
                player.setEmail(rs.getString("email"));
                player.setRole(rs.getString("role"));
                player.setPrenom(rs.getString("first_name"));
                player.setNom(rs.getString("last_name"));
                player.setNickname(rs.getString("nickname"));
                player.setCin(rs.getString("cin"));
                Date dob = rs.getDate("date_of_birth");
                if (dob != null) player.setBirthday(dob.toLocalDate());
                player.setNationality(rs.getString("nationality"));
                player.setCity(rs.getString("city"));
                members.add(player);
            }
        } catch (SQLException e) {
            System.err.println("Error getting members by team: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return members; // never null
    }

    // ── removeMember ─────────────────────────────────────────────────
    public void removeMember(String teamId, String playerId) {
        String query = "DELETE FROM team_members WHERE team_id=? AND player_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            pst.setString(2, playerId);
            pst.executeUpdate();
            System.out.println("Member removed from team: " + teamId);
        } catch (SQLException e) {
            System.err.println("Error removing member: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // ── getTeamByPlayer ──────────────────────────────────────────────
    // Returns the team the player belongs to, or null if none
    public Team getTeamByPlayer(String playerId) {
        String query =
            "SELECT t.id, t.created_by, t.game_id, t.name, t.logo_image, t.contact, t.status, t.created_at " +
            "FROM teams t " +
            "JOIN team_members tm ON tm.team_id = t.id " +
            "WHERE tm.player_id = ? " +
            "LIMIT 1";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, playerId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapResultSetToTeam(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting team by player: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return null; // player has no team
    }

    // ── mapResultSetToTeam ───────────────────────────────────────────
    private Team mapResultSetToTeam(ResultSet rs) throws SQLException {
        Team team = new Team();
        team.setId(rs.getString("id"));
        team.setCreatedBy(rs.getString("created_by"));
        team.setGameId(rs.getString("game_id"));
        team.setName(rs.getString("name"));
        team.setLogoImage(rs.getString("logo_image"));
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
