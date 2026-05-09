package Genex.services;

import Genex.entities.SponsorNote;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrudSponsorNote {

    private Connection getCnx() { return Myconnection.getInstance().getCnx(); }

    public void addNote(SponsorNote note) {
        note.setId(UUID.randomUUID().toString());
        String sql = "INSERT INTO sponsor_notes (id, sponsor_id, author, note, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, note.getId());
            pst.setString(2, note.getSponsorId());
            pst.setString(3, note.getAuthor());
            pst.setString(4, note.getNote());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudSponsorNote.addNote: " + e.getMessage(), e);
        }
    }

    public void deleteNote(String noteId) {
        try (PreparedStatement pst = getCnx().prepareStatement("DELETE FROM sponsor_notes WHERE id=?")) {
            pst.setString(1, noteId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("CrudSponsorNote.deleteNote: " + e.getMessage(), e);
        }
    }

    public List<SponsorNote> getNotesForSponsor(String sponsorId) {
        List<SponsorNote> list = new ArrayList<>();
        String sql = "SELECT * FROM sponsor_notes WHERE sponsor_id=? ORDER BY created_at DESC";
        try (PreparedStatement pst = getCnx().prepareStatement(sql)) {
            pst.setString(1, sponsorId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                SponsorNote n = new SponsorNote();
                n.setId(rs.getString("id"));
                n.setSponsorId(rs.getString("sponsor_id"));
                n.setAuthor(rs.getString("author"));
                n.setNote(rs.getString("note"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) n.setCreatedAt(ts.toLocalDateTime());
                list.add(n);
            }
        } catch (SQLException e) {
            throw new RuntimeException("CrudSponsorNote.getNotesForSponsor: " + e.getMessage(), e);
        }
        return list;
    }
}
