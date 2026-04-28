package Genex.services;

import Genex.entities.Sponsor;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrudSponsor implements ICrud<Sponsor> {

    private final Connection cnx = Myconnection.getInstance().getCnx();

    @Override
    public void addEntity(Sponsor s) {
        String sql = "INSERT INTO sponsors (id, name, logo_url, website_url, industry, contact_email, created_at) " +
                     "VALUES (UUID(), ?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, s.getName());
            pst.setString(2, s.getLogoUrl());
            pst.setString(3, s.getWebsiteUrl());
            pst.setString(4, s.getIndustry());
            pst.setString(5, s.getContactEmail());
            pst.executeUpdate();
            System.out.println("Sponsor added: " + s.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(Sponsor s, String id) {
        String sql = "UPDATE sponsors SET name=?, logo_url=?, website_url=?, industry=?, contact_email=? WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, s.getName());
            pst.setString(2, s.getLogoUrl());
            pst.setString(3, s.getWebsiteUrl());
            pst.setString(4, s.getIndustry());
            pst.setString(5, s.getContactEmail());
            pst.setString(6, id);
            pst.executeUpdate();
            System.out.println("Sponsor updated: " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntity(Sponsor s) {
        String sql = "DELETE FROM sponsors WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, s.getId());
            pst.executeUpdate();
            System.out.println("Sponsor deleted: " + s.getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getEntity(Sponsor s) {
        // single fetch by id — result printed; use getAll() for UI
        String sql = "SELECT * FROM sponsors WHERE id=?";
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, s.getId());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) mapRow(rs, s);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Sponsor> getAll() {
        List<Sponsor> list = new ArrayList<>();
        String sql = "SELECT * FROM sponsors ORDER BY created_at DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Sponsor s = new Sponsor();
                mapRow(rs, s);
                list.add(s);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    private void mapRow(ResultSet rs, Sponsor s) throws SQLException {
        s.setId(rs.getString("id"));
        s.setName(rs.getString("name"));
        s.setLogoUrl(rs.getString("logo_url"));
        s.setWebsiteUrl(rs.getString("website_url"));
        s.setIndustry(rs.getString("industry"));
        s.setContactEmail(rs.getString("contact_email"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
    }
}
