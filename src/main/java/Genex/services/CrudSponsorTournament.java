package Genex.services;

import Genex.entities.Sponsor;
import Genex.entities.SponsorTournament;
import Genex.entities.Tounament;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrudSponsorTournament {

    private Connection getCnx() {
        return Myconnection.getInstance().getCnx();
    }

    // ── CREATE ─────────────────────────────────────────────────────────────
    public void addEntity(SponsorTournament st) {
        String sql = "INSERT INTO sponsor_tournament (id, sponsor_id, tournament_id, methode, budget_amount, start_date, end_date, notes) " +
                     "VALUES (UUID(), ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, st.getSponsorId());
            pst.setString(2, st.getTournamentId());
            pst.setString(3, st.getMethod() != null ? st.getMethod().name() : null);
            pst.setBigDecimal(4, st.getBudgetAmount());
            pst.setObject(5, st.getStartDate());
            pst.setObject(6, st.getEndDate());
            pst.setString(7, st.getNotes());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudSponsorTournament.addEntity: " + e.getMessage(), e);
        }
    }

    // ── UPDATE ─────────────────────────────────────────────────────────────
    public void updateEntity(SponsorTournament st, String id) {
        String sql = "UPDATE sponsor_tournament SET sponsor_id=?, tournament_id=?, methode=?, budget_amount=?, " +
                     "start_date=?, end_date=?, notes=? WHERE id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, st.getSponsorId());
            pst.setString(2, st.getTournamentId());
            pst.setString(3, st.getMethod() != null ? st.getMethod().name() : null);
            pst.setBigDecimal(4, st.getBudgetAmount());
            pst.setObject(5, st.getStartDate());
            pst.setObject(6, st.getEndDate());
            pst.setString(7, st.getNotes());
            pst.setString(8, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudSponsorTournament.updateEntity: " + e.getMessage(), e);
        }
    }

    // ── DELETE ─────────────────────────────────────────────────────────────
    public void deleteEntity(SponsorTournament st) {
        String sql = "DELETE FROM sponsor_tournament WHERE id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, st.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudSponsorTournament.deleteEntity: " + e.getMessage(), e);
        }
    }

    // ── READ ALL ───────────────────────────────────────────────────────────
    public List<SponsorTournament> getAll() {
        List<SponsorTournament> list = new ArrayList<>();
        String sql = "SELECT st.*, " +
                     "  s.name AS sponsor_name, s.contact_email, s.sponsor_type, " +
                     "  t.tournament_name " +
                     "FROM sponsor_tournament st " +
                     "LEFT JOIN sponsors    s ON st.sponsor_id    = s.id " +
                     "LEFT JOIN tournaments t ON st.tournament_id = t.id " +
                     "ORDER BY st.start_date DESC";
        try (Statement stmt = getCnx().createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("CrudSponsorTournament.getAll: " + e.getMessage(), e);
        }
        return list;
    }

    // ── HELPERS ────────────────────────────────────────────────────────────
    private SponsorTournament mapRow(ResultSet rs) throws SQLException {
        SponsorTournament st = new SponsorTournament();
        st.setId(rs.getString("id"));
        st.setSponsorId(rs.getString("sponsor_id"));
        st.setTournamentId(rs.getString("tournament_id"));

        // Hydrate sponsor stub
        Sponsor s = new Sponsor();
        s.setId(rs.getString("sponsor_id"));
        s.setName(rs.getString("sponsor_name"));
        s.setContactEmail(rs.getString("contact_email"));
        s.setSponsorType(rs.getString("sponsor_type"));
        st.setSponsor(s);

        // Hydrate tournament stub
        Tounament t = new Tounament();
        t.setTournamentId(rs.getString("tournament_id"));
        t.setTournamentName(rs.getString("tournament_name"));
        st.setTournament(t);

        // Method
        String methodStr = rs.getString("methode");
        if (methodStr != null) {
            try { st.setMethod(SponsorTournament.SponsorMethod.valueOf(methodStr)); }
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
