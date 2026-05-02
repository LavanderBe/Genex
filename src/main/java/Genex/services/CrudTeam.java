package Genex.services;

import Genex.entities.Team;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CrudTeam {

    public CrudTeam() {}

    public void addEntity(Team team) {
        String query = "INSERT INTO teams (created_by, game_id, name, logo_image, contact, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, team.getCreatedBy());
            pst.setString(2, team.getGameId());
            pst.setString(3, team.getName());
            pst.setString(4, team.getLogoImage());
            pst.setString(5, team.getContact());
            pst.setString(6, team.getStatus() != null ? team.getStatus().name() : null);
            pst.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
            System.out.println("Team added successfully");
        } catch (SQLException e) {
            System.err.println("Error adding team: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void updateEntity(Team team, String id) {
        String query = "UPDATE teams SET created_by=?, game_id=?, name=?, logo_image=?, contact=?, status=? " +
                "WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, team.getCreatedBy());
            pst.setString(2, team.getGameId());
            pst.setString(3, team.getName());
            pst.setString(4, team.getLogoImage());
            pst.setString(5, team.getContact());
            pst.setString(6, team.getStatus() != null ? team.getStatus().name() : null);
            pst.setString(7, id);
            pst.executeUpdate();
            System.out.println("Team updated successfully");
        } catch (SQLException e) {
            System.err.println("Error updating team: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteEntity(Team team) {
        String query = "DELETE FROM teams WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, team.getId());
            pst.executeUpdate();
            System.out.println("Team deleted successfully");
        } catch (SQLException e) {
            System.err.println("Error deleting team: " + e.getMessage());
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