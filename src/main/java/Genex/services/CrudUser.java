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
        String requete="INSERT INTO users (username,email,password_hash,role,created_at,salt)" +
                "VALUES (?,?,?,?,?,?);";
        try {
            PreparedStatement pst= Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,user.getUsername());
            pst.setString(2,user.getEmail());
            pst.setString(3,user.getPassword_hash());
            pst.setString(4,user.getRole());
            pst.setString(5,user.getCreated_at().toString());
            pst.setString(6,user.getSalt());
            pst.executeUpdate();
            System.out.println("User added successfully");
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
        String requete="DELETE FROM users " +
                "WHERE (username=?) AND (email=?);";
        try {
            PreparedStatement pst=Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,user.getUsername());
            pst.setString(2,user.getEmail());
            pst.executeUpdate();
            System.out.println("User deleted succesfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    //printing every info we have on user except the id
    @Override
    public void getEntity(User user) {
        String requete="SELECT * " +
                "FROM users " +
                "WHERE username=?;";
        try {
            PreparedStatement pst=Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,user.getUsername());
            ResultSet rs=pst.executeQuery();
            if (rs.next()){
            User u=new User(rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password_hash"),
                    rs.getString("role"));
            System.out.println(u);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public User getUser_byUsername(User user) {
        String requete="SELECT * " +
                "FROM users " +
                "WHERE username=?;";
        try {
            PreparedStatement pst=Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,user.getUsername());
            ResultSet rs=pst.executeQuery();
            if (rs.next()){
                User u=new User(rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role"));
                return u;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String getUser_Id(String username) {
        String requete="SELECT * " +
                "FROM users " +
                "WHERE username=?;";
        try {
            PreparedStatement pst=Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,username);
            ResultSet rs=pst.executeQuery();
            if (rs.next()){
                return rs.getString("id");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    //checks if email exists
    public boolean check_email(String email){
        String requete="SELECT email " +
                "FROM users " +
                "WHERE email=?";
        try {
            PreparedStatement pst =Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,email);
            ResultSet rs=pst.executeQuery();
            if (rs.next()){
                return true;
            }
            else return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean check_Username(String username){
        String requete="SELECT email " +
                "FROM users " +
                "WHERE username=?";
        try {
            PreparedStatement pst =Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,username);
            ResultSet rs=pst.executeQuery();
            if (rs.next()){
                return true;
            }
            else return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    //returns all the user attributes given the email
    public User getUser_withmail(String email){
        String requete="SELECT * " +
                "FROM users " +
                "WHERE email=?";
        try {
            PreparedStatement pst =Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,email);
            ResultSet rs=pst.executeQuery();
            if (rs.next()){
                User u=new User((rs.getString("username")),
                        rs.getString("email"),
                        rs.getString("salt"),
                        rs.getString("password_hash"),
                        rs.getString("role"));
                // Set the user ID - this is critical for player identification
                u.setId(rs.getString("id"));
                return u;
            }
            else return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
