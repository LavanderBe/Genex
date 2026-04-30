package Genex.services;

import Genex.entities.Sponsor;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrudSponsor implements ICrud<Sponsor> {

    // Lazy — fetched on first use, not at construction time
    private Connection getCnx() {
        return Myconnection.getInstance().getCnx();
    }

    @Override
    public void addEntity(Sponsor s) {
        String sql = "INSERT INTO sponsors (id, name, logo_url, website_url, industry, contact_email, created_at) " +
                     "VALUES (UUID(), ?, ?, ?, ?, ?, NOW())";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, s.getName());
            pst.setString(2, nullIfBlank(s.getLogoUrl()));
            pst.setString(3, nullIfBlank(s.getWebsiteUrl()));
            // Store "industry | sponsorType" together, split on read
            String industryVal = buildIndustryField(s.getIndustry(), s.getSponsorType());
            pst.setString(4, nullIfBlank(industryVal));
            pst.setString(5, nullIfBlank(s.getContactEmail()));
            pst.executeUpdate();
            System.out.println("Sponsor added: " + s.getName());
        } catch (SQLException e) {
            throw new RuntimeException("addEntity failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateEntity(Sponsor s, String id) {
        String sql = "UPDATE sponsors SET name=?, logo_url=?, website_url=?, industry=?, contact_email=? WHERE id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, s.getName());
            pst.setString(2, nullIfBlank(s.getLogoUrl()));
            pst.setString(3, nullIfBlank(s.getWebsiteUrl()));
            String industryVal = buildIndustryField(s.getIndustry(), s.getSponsorType());
            pst.setString(4, nullIfBlank(industryVal));
            pst.setString(5, nullIfBlank(s.getContactEmail()));
            pst.setString(6, id);
            pst.executeUpdate();
            System.out.println("Sponsor updated: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("updateEntity failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEntity(Sponsor s) {
        String sql = "DELETE FROM sponsors WHERE id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, s.getId());
            pst.executeUpdate();
            System.out.println("Sponsor deleted: " + s.getId());
        } catch (SQLException e) {
            throw new RuntimeException("deleteEntity failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void getEntity(Sponsor s) {
        String sql = "SELECT * FROM sponsors WHERE id=?";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, s.getId());
            ResultSet rs = pst.executeQuery();
            if (rs.next()) mapRow(rs, s);
        } catch (SQLException e) {
            throw new RuntimeException("getEntity failed: " + e.getMessage(), e);
        }
    }

    public List<Sponsor> getAll() {
        List<Sponsor> list = new ArrayList<>();
        String sql = "SELECT * FROM sponsors ORDER BY created_at DESC";
        try (Statement st = getCnx().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Sponsor s = new Sponsor();
                mapRow(rs, s);
                list.add(s);
            }
        } catch (SQLException e) {
            throw new RuntimeException("getAll failed: " + e.getMessage(), e);
        }
        return list;
    }

    private void mapRow(ResultSet rs, Sponsor s) throws SQLException {
        s.setId(rs.getString("id"));
        s.setName(rs.getString("name"));
        s.setLogoUrl(rs.getString("logo_url"));
        s.setWebsiteUrl(rs.getString("website_url"));
        // industry field stores "industry|type" — split on read
        String raw = rs.getString("industry");
        if (raw != null && raw.contains("|")) {
            String[] parts = raw.split("\\|", 2);
            s.setIndustry(parts[0].trim());
            s.setSponsorType(parts[1].trim());
        } else {
            s.setIndustry(raw);
            s.setSponsorType("");
        }
        s.setContactEmail(rs.getString("contact_email"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) s.setCreatedAt(ts.toLocalDateTime());
    }

    /** Packs industry and sponsorType into one DB column: "industry|type" */
    private String buildIndustryField(String industry, String type) {
        String i = (industry == null) ? "" : industry.trim();
        String t = (type == null) ? "" : type.trim();
        if (i.isEmpty() && t.isEmpty()) return null;
        return i + "|" + t;
    }

    private String nullIfBlank(String val) {
        return (val == null || val.isBlank()) ? null : val;
    }
}
