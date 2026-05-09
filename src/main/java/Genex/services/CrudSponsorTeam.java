package Genex.services;

import Genex.entities.Sponsor;
import Genex.entities.SponsorTeam;
import Genex.entities.Team;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrudSponsorTeam {

    private Connection getCnx() {
        return Myconnection.getInstance().getCnx();
    }

    // ── CREATE ─────────────────────────────────────────────────────────────
    public void addEntity(SponsorTeam st) {
        String sql = "INSERT INTO sponsor_team (id, sponsor_id, team_id, method, budget_amount, start_date, end_date, notes) " +
                     "VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, st.getSponsorId());
            pst.setInt   (2, st.getTeamId());
            pst.setString(3, st.getMethod() != null ? st.getMethod().name() : null);
            pst.setBigDecimal(4, st.getBudgetAmount());
            pst.setObject(5, st.getStartDate());
            pst.setObject(6, st.getEndDate());
            pst.setString(7, st.getNotes());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudSponsorTeam.addEntity: " + e.getMessage(), e);
        }
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────
    public void updateEntity(SponsorTeam st, String id) {
        String sql = "UPDATE sponsor_team SET sponsor_id=?, team_id=?, method=?, budget_amount=?, " +
                     "start_date=?, end_date=?, notes=? WHERE id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, st.getSponsorId());
            pst.setInt   (2, st.getTeamId());
            pst.setString(3, st.getMethod() != null ? st.getMethod().name() : null);
            pst.setBigDecimal(4, st.getBudgetAmount());
            pst.setObject(5, st.getStartDate());
            pst.setObject(6, st.getEndDate());
            pst.setString(7, st.getNotes());
            pst.setString(8, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudSponsorTeam.updateEntity: " + e.getMessage(), e);
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────
    public void deleteEntity(SponsorTeam st) {
        String sql = "DELETE FROM sponsor_team WHERE id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, st.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudSponsorTeam.deleteEntity: " + e.getMessage(), e);
        }
    }

    // ── READ ALL ───────────────────────────────────────────────────────────
    public List<SponsorTeam> getAll() {
        List<SponsorTeam> list = new ArrayList<>();
        String sql = "SELECT st.*, " +
                     "  s.name AS sponsor_name, s.contact_email, s.sponsor_type, " +
                     "  t.nom_team " +
                     "FROM sponsor_team st " +
                     "LEFT JOIN sponsors s ON st.sponsor_id = s.id " +
                     "LEFT JOIN team     t ON st.team_id    = t.id_team " +
                     "ORDER BY st.start_date DESC";
        try (Statement stmt = getCnx().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("CrudSponsorTeam.getAll: " + e.getMessage(), e);
        }
        return list;
    }

    // ── HELPERS ────────────────────────────────────────────────────────────
    private SponsorTeam mapRow(ResultSet rs) throws SQLException {
        SponsorTeam st = new SponsorTeam();
        st.setId(rs.getString("id"));
        st.setSponsorId(rs.getString("sponsor_id"));
        st.setTeamId(rs.getInt("team_id"));

        // Hydrate sponsor stub
        Sponsor s = new Sponsor();
        s.setId(rs.getString("sponsor_id"));
        s.setName(rs.getString("sponsor_name"));
        s.setContactEmail(rs.getString("contact_email"));
        s.setSponsorType(rs.getString("sponsor_type"));
        st.setSponsor(s);

        // Hydrate team stub
        Team t = new Team();
        t.setId_team(rs.getInt("team_id"));
        t.setNom_team(rs.getString("nom_team"));
        st.setTeam(t);

        // Method
        String methodStr = rs.getString("method");
        if (methodStr != null) {
            try { st.setMethod(SponsorTeam.SponsorMethod.valueOf(methodStr)); }
            catch (IllegalArgumentException ignored) {}
        }

        st.setBudgetAmount(rs.getBigDecimal("budget_amount"));

        Date sd = rs.getDate("start_date");
        if (sd != null) st.setStartDate(sd.toLocalDate());
        Date ed = rs.getDate("end_date");
        if (ed != null) st.setEndDate(ed.toLocalDate());

        st.setNotes(rs.getString("notes"));
        return st;
    }
}
