package Genex.services;

import Genex.entities.Team;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrudTeam implements ICrud<Team> {

    public CrudTeam() {}

    // ✅ CREATE
    @Override
    public void addEntity(Team t) {

        String sql = "INSERT INTO team (coach_id, centre_id, game_id, nom, foundation_date, logo_image, contact, statut) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(sql);

            pst.setInt(1, t.getCoachId());

            if (t.getCentreId() != null)
                pst.setInt(2, t.getCentreId());
            else
                pst.setNull(2, Types.INTEGER);

            pst.setInt(3, t.getGameId());
            pst.setString(4, t.getNom());
            pst.setDate(5, Date.valueOf(t.getFoundationDate()));
            pst.setString(6, t.getLogoImage());
            pst.setString(7, t.getContact());
            pst.setString(8, t.getStatut()); // actif / inactif

            pst.executeUpdate();
            System.out.println("✅ Team added");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ UPDATE
    @Override
    public void updateEntity(Team t, String id) {

        String sql = "UPDATE team SET nom=?, centre_id=?, game_id=?, logo_image=?, contact=?, statut=? WHERE team_id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(sql);

            pst.setString(1, t.getNom());

            if (t.getCentreId() != null)
                pst.setInt(2, t.getCentreId());
            else
                pst.setNull(2, Types.INTEGER);

            pst.setInt(3, t.getGameId());
            pst.setString(4, t.getLogoImage());
            pst.setString(5, t.getContact());
            pst.setString(6, t.getStatut());

            pst.setInt(7, Integer.parseInt(id));

            pst.executeUpdate();
            System.out.println("✅ Team updated");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ❌ DELETE (soft delete better)
    @Override
    public void deleteEntity(Team t) {

        String sql = "UPDATE team SET statut='inactif' WHERE team_id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, t.getTeamId());
            pst.executeUpdate();

            System.out.println("🗑 Team soft deleted");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ READ ONE
    @Override
    public void getEntity(Team t) {

        String sql = "SELECT * FROM team WHERE team_id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(sql);
            pst.setInt(1, t.getTeamId());

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                t.setCoachId(rs.getInt("coach_id"));
                t.setCentreId((Integer) rs.getObject("centre_id"));
                t.setGameId(rs.getInt("game_id"));
                t.setNom(rs.getString("nom"));
                t.setFoundationDate(rs.getDate("foundation_date").toLocalDate());
                t.setLogoImage(rs.getString("logo_image"));
                t.setContact(rs.getString("contact"));
                t.setStatut(rs.getString("statut"));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // ✅ READ ALL
    public List<Team> getAll() {

        List<Team> list = new ArrayList<>();
        String sql = "SELECT * FROM team";

        try {
            Statement st = Myconnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Team t = new Team();

                t.setTeamId(rs.getInt("team_id"));
                t.setCoachId(rs.getInt("coach_id"));
                t.setCentreId((Integer) rs.getObject("centre_id"));
                t.setGameId(rs.getInt("game_id"));
                t.setNom(rs.getString("nom"));
                t.setFoundationDate(rs.getDate("foundation_date").toLocalDate());
                t.setLogoImage(rs.getString("logo_image"));
                t.setContact(rs.getString("contact"));
                t.setStatut(rs.getString("statut"));

                list.add(t);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }
}