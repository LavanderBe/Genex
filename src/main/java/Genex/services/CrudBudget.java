package Genex.services;

import Genex.entities.Budget;
import Genex.entities.Sponsor;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrudBudget implements ICrud<Budget> {

    private final Connection cnx = Myconnection.getInstance().getCnx();

    @Override
    public void addEntity(Budget b) {
        String sql = "INSERT INTO budget (id, sponsor_id, fiscal_year, allocated_amount, spent_amount) " +
                     "VALUES (UUID(), ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, b.getSponsorId());
            pst.setInt(2, b.getFiscalYear());
            pst.setBigDecimal(3, b.getAllocatedAmount());
            pst.setBigDecimal(4, b.getSpentAmount());
            pst.executeUpdate();
            System.out.println("Budget added for sponsor: " + b.getSponsorId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(Budget b, String id) {
        String sql = "UPDATE budget SET sponsor_id=?, fiscal_year=?, allocated_amount=?, spent_amount=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, b.getSponsorId());
            pst.setInt(2, b.getFiscalYear());
            pst.setBigDecimal(3, b.getAllocatedAmount());
            pst.setBigDecimal(4, b.getSpentAmount());
            pst.setString(5, id);
            pst.executeUpdate();
            System.out.println("Budget updated: " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntity(Budget b) {
        String sql = "DELETE FROM budget WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, b.getId());
            pst.executeUpdate();
            System.out.println("Budget deleted: " + b.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getEntity(Budget b) {
        String sql = "SELECT b.*, s.name AS sponsor_name, s.industry, s.contact_email, s.logo_url, s.website_url " +
                     "FROM budget b LEFT JOIN sponsors s ON b.sponsor_id = s.id WHERE b.id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, b.getId());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) mapRow(rs, b);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Budget> getAll() {
        List<Budget> list = new ArrayList<>();
        String sql = "SELECT b.*, s.name AS sponsor_name, s.industry, s.contact_email, s.logo_url, s.website_url " +
                     "FROM budget b LEFT JOIN sponsors s ON b.sponsor_id = s.id ORDER BY b.fiscal_year DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Budget b = new Budget();
                mapRow(rs, b);
                list.add(b);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private void mapRow(ResultSet rs, Budget b) throws SQLException {
        b.setId(rs.getString("id"));
        b.setFiscalYear(rs.getInt("fiscal_year"));
        b.setAllocatedAmount(rs.getBigDecimal("allocated_amount"));
        b.setSpentAmount(rs.getBigDecimal("spent_amount"));

        String sponsorId = rs.getString("sponsor_id");
        if (sponsorId != null) {
            Sponsor s = new Sponsor();
            s.setId(sponsorId);
            s.setName(rs.getString("sponsor_name"));
            s.setIndustry(rs.getString("industry"));
            s.setContactEmail(rs.getString("contact_email"));
            s.setLogoUrl(rs.getString("logo_url"));
            s.setWebsiteUrl(rs.getString("website_url"));
            b.setSponsor(s);
        }
    }
}
