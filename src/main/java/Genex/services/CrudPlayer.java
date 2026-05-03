package Genex.services;

import java.sql.Date;
import java.sql.ResultSet;

import Genex.entities.Player;
import Genex.utils.Myconnection;

import java.time.LocalDate;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CrudPlayer {
    public CrudPlayer() {
    }

    public void addPlayer_admin(Player p){
        CrudUser cu=new CrudUser();
        cu.addEntity(p);
        p.setId(cu.getUser_Id(p.getUsername()));
        addEntity(p);
    }

    public void addEntity(Player p) {
        String requete = "INSERT INTO players (first_name, last_name, nickname, cin, date_of_birth, nationality, city,user_id) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getPrenom());
            pst.setString(2, p.getNom());
            pst.setString(3, p.getNickname());
            pst.setString(4, p.getCin());
            pst.setDate(5,Date.valueOf(p.getBirthday()));
            pst.setString(6, p.getNationality());
            pst.setString(7, p.getCity());
            pst.setString(8, p.getId());
            pst.executeUpdate();
            System.out.println("player added successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateEntity(Player p,String cin) {
        String requete = "UPDATE players SET first_name=?, last_name=?, nickname=?, cin=?, date_of_birth=?, nationality=?, city=? WHERE cin=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getPrenom());
            pst.setString(2, p.getNom());
            pst.setString(3, p.getNickname());
            pst.setString(4, p.getCin());
            pst.setDate(5, Date.valueOf(p.getBirthday()));
            pst.setString(6, p.getNationality());
            pst.setString(7, p.getCity());
            pst.setString(8, cin);
            pst.executeUpdate();
            System.out.println("player updated successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteEntity(String cin) {
        String requete = "DELETE FROM players WHERE cin=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, cin);
            pst.executeUpdate();
            System.out.println("player deleted successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Player> getEntities() {
        List<Player> players = new ArrayList<>();
        String requete = "SELECT * FROM players";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Player p = new Player(rs.getDate("date_of_birth").toLocalDate(),rs.getString("first_name"),rs.getString("last_name"),rs.getString("nickname"), rs.getString("cin"),rs.getString("nationality"),rs.getString("city"));
                // Set the user_id as the player's ID (user_id is the primary key in players table)
                p.setId(rs.getString("user_id"));
                players.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return players;
    }

    public boolean check_cin_exists(String cin){
        String requete = "SELECT * FROM players WHERE cin=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,cin);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return true;
            }
            else{
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public boolean check_nickname_exists(String nickname){
        String requete = "SELECT * FROM players WHERE nickname=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,nickname);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return true;
            }
            else{
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

}
