package Genex.services;

import Genex.entities.Team;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CrudTeam implements ICrud<Team> {

    public CrudTeam() {}

    @Override
    public void addEntity(Team t) {

        String requete = "INSERT INTO teams " +
                "(name, game_id, foundation_date, logo_image, contact, status) " +
                "VALUES (?,?,?,?,?,?)";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getName());
            pst.setString(2, t.getGameId());
            pst.setString(3, t.getFoundationDate().toString());
            pst.setString(4, t.getLogoImage());
            pst.setString(5, t.getContact());
            pst.setString(6, t.getStatus().name());
            pst.executeUpdate();
            System.out.println("Team added successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(Team t, String id) {

        String requete = "UPDATE teams SET " +
                "name=?, game_id=?, foundation_date=?, logo_image=?, contact=?, status=? " +
                "WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getName());
            pst.setString(2, t.getGameId());
            pst.setString(3, t.getFoundationDate().toString());
            pst.setString(4, t.getLogoImage());
            pst.setString(5, t.getContact());
            pst.setString(6, t.getStatus().name());
            pst.setString(7, id);
            pst.executeUpdate();
            System.out.println("Team updated successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntity(Team t) {

        // trainings deleted automatically by ON DELETE CASCADE
        String requete = "DELETE FROM teams WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getTeamId());
            pst.executeUpdate();
            System.out.println("Team and its trainings deleted successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getEntity(Team t) {

        String requete = "SELECT * FROM teams WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getTeamId());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                t.setName(rs.getString("name"));
                t.setGameId(rs.getString("game_id"));
                t.setFoundationDate(rs.getDate("foundation_date").toLocalDate());
                t.setLogoImage(rs.getString("logo_image"));
                t.setContact(rs.getString("contact"));
                t.setStatus(Team.Status.valueOf(rs.getString("status")));
                System.out.println("Team loaded");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Team> getAll() {

        List<Team> list = new ArrayList<>();
        String requete = "SELECT * FROM teams";

        try {
            Statement st = Myconnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);

            while (rs.next()) {
                Team t = new Team();
                t.setTeamId(rs.getString("id"));
                t.setName(rs.getString("name"));
                t.setGameId(rs.getString("game_id"));
                t.setFoundationDate(rs.getDate("foundation_date").toLocalDate());
                t.setLogoImage(rs.getString("logo_image"));
                t.setContact(rs.getString("contact"));
                t.setStatus(Team.Status.valueOf(rs.getString("status")));
                list.add(t);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}