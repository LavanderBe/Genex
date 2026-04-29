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

    /**
     * Authenticate a user by email and password
     * @param email User's email
     * @param password User's plain text password (should match password_hash in DB)
     * @return User object if authentication successful, null otherwise
     */
    public User authenticate(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password_hash = ?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, email);
            pst.setString(2, password); // In production, this should be hashed

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role")
                );
                user.setId(rs.getString("id"));

                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    user.setCreated_at(createdAt.toLocalDateTime());
                }

                System.out.println("User authenticated: " + user.getUsername());
                return user;
            } else {
                System.out.println("Authentication failed for email: " + email);
                return null;
            }

        } catch (SQLException e) {
            System.err.println("Error during authentication: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Find a user by email
     * @param email User's email
     * @return User object if found, null otherwise
     */
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, email);

            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getString("role")
                );
                user.setId(rs.getString("id"));

                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    user.setCreated_at(createdAt.toLocalDateTime());
                }

                return user;
            }

        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
