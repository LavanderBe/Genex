package Genex.services;

import Genex.entities.Forum;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrudForum implements ICrud<Forum> {

    @Override
    public void addEntity(Forum forum) {
        String requete = "INSERT INTO forums (title, description, created_by, created_at) VALUES (?,?,?,?)";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, forum.getTitle());
            pst.setString(2, forum.getDescription());
            pst.setString(3, forum.getCreatedBy());
            pst.setString(4, forum.getCreatedAt().toString());
            pst.executeUpdate();
            System.out.println("Forum ajouté avec succès");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(Forum forum, String id) {
        String requete = "UPDATE forums SET title=?, description=? WHERE id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, forum.getTitle());
            pst.setString(2, forum.getDescription());
            pst.setString(3, id);
            pst.executeUpdate();
            System.out.println("Forum modifié avec succès");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntity(Forum forum) {
        String requete = "DELETE FROM forums WHERE id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, forum.getId());
            pst.executeUpdate();
            System.out.println("Forum supprimé avec succès");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getEntity(Forum forum) {
        // utilisé pour chercher un forum par id
    }

    public List<Forum> getAllForums() {
        List<Forum> forums = new ArrayList<>();
        String requete = "SELECT * FROM forums";
        try {
            Statement st = Myconnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                Forum f = new Forum();
                f.setId(rs.getString("id"));
                f.setTitle(rs.getString("title"));
                f.setDescription(rs.getString("description"));
                f.setCreatedBy(rs.getString("created_by"));
                forums.add(f);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return forums;
    }
}