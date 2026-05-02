package Genex.services;

import Genex.entities.Center;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CrudCenter implements ICrud<Center> {

    public CrudCenter() {}

    @Override
    public void addEntity(Center c) {

        String requete = "INSERT INTO centers " +
                "(name, address, city, contact_email, map_url) " +
                "VALUES (?,?,?,?,?)";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, c.getName());
            pst.setString(2, c.getAddress());
            pst.setString(3, c.getCity());
            pst.setString(4, c.getContactEmail());
            pst.setString(5, c.getMapUrl());
            pst.executeUpdate();
            System.out.println("Center added successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(Center c, String id) {

        String requete = "UPDATE centers SET " +
                "name=?, address=?, city=?, contact_email=?, map_url=? " +
                "WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, c.getName());
            pst.setString(2, c.getAddress());
            pst.setString(3, c.getCity());
            pst.setString(4, c.getContactEmail());
            pst.setString(5, c.getMapUrl());
            pst.setString(6, id);
            pst.executeUpdate();
            System.out.println("Center updated successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntity(Center c) {

        String requete = "DELETE FROM centers WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, c.getCenterId());
            pst.executeUpdate();
            System.out.println("Center deleted successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getEntity(Center c) {

        String requete = "SELECT * FROM centers WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, c.getCenterId());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                c.setName(rs.getString("name"));
                c.setAddress(rs.getString("address"));
                c.setCity(rs.getString("city"));
                c.setContactEmail(rs.getString("contact_email"));
                c.setMapUrl(rs.getString("map_url"));
                System.out.println("Center loaded");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Center> getAll() {

        List<Center> list = new ArrayList<>();
        String requete = "SELECT * FROM centers";

        try {
            Statement st = Myconnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);

            while (rs.next()) {
                Center c = new Center();
                c.setCenterId(rs.getString("id"));
                c.setName(rs.getString("name"));
                c.setAddress(rs.getString("address"));
                c.setCity(rs.getString("city"));
                c.setContactEmail(rs.getString("contact_email"));
                c.setMapUrl(rs.getString("map_url"));
                list.add(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}