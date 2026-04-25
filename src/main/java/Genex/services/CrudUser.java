package Genex.services;

import Genex.entities.User;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;


import java.sql.*;

public class CrudUser implements ICrud<User> {
    public CrudUser() {
    }

    @Override
    public void addEntity(User user) {
        String requete="INSERT INTO users (username,email,password_hash,role,created_at)" +
                "VALUES (?,?,?,?,?);";
        try {
            PreparedStatement pst= Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,user.getUsername());
            pst.setString(2,user.getEmail());
            pst.setString(3,user.getPassword_hash());
            pst.setString(4,user.getRole());
            pst.setString(5,user.getCreated_at().toString());
            pst.executeUpdate();
            System.out.println("User Updated successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // this is going to be an update using the id
    @Override
    public void updateEntity(User user,String id) {
        String requete="UPDATE users " +
                "SET username=?,email=?,password_hash=? " +
                "WHERE id=? ";
        try {
            PreparedStatement pst= Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,user.getUsername());
            pst.setString(2,user.getEmail());
            pst.setString(3,user.getPassword_hash());
            pst.setString(4,id);
            pst.executeUpdate();
            System.out.println("User added successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void deleteEntity(User user) {

    }

    @Override
    public void getEntity(User user) {

    }
}
