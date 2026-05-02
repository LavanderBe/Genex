package Genex.services;

import Genex.entities.Game;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CrudGame {

    public CrudGame() {
    }

    public void addEntity(Game g){
        String requete="INSERT INTO games (name,genre,platform,team_mode,max_players_per_match,icon_url) " +
                "VALUES (?,?,?,?,?,?) ;";
        try {
            PreparedStatement pst= Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,g.getNom());
            pst.setString(2,g.getGenre());
            pst.setString(3,g.getPlatforme());
            pst.setString(4,g.getMode());
            pst.setInt(5,g.getMax_team_player());
            pst.setString(6,g.getIcon_url());
            pst.executeUpdate();
            System.out.println("game added succesfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateEntity(Game g,String name) {
        String requete = "UPDATE games SET name=?, genre=?, platform=?, team_mode=?, max_players_per_match=?, icon_url=? WHERE name=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, g.getNom());
            pst.setString(2, g.getGenre());
            pst.setString(3, g.getPlatforme());
            pst.setString(4, g.getMode());
            pst.setInt(5, g.getMax_team_player());
            pst.setString(6, g.getIcon_url());
            pst.setString(7,name);
            pst.executeUpdate();
            System.out.println("game updated successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteEntity(String name){
        String requete ="DELETE FROM games " +
                "WHERE name=?;";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,name);
            pst.executeUpdate();
            System.out.println("game deleted successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean Nameexists(String name){
        String requete="SELECT name " +
                "FROM games " +
                "where name=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1,name);
            ResultSet rs=pst.executeQuery();
            if (rs.next()) return true;
            else return false;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Game> getgames(){
        String requete="SELECT * " +
                "FROM GAMES;";
        Statement pst= null;
        try {
            pst = Myconnection.getInstance().getCnx().createStatement();
            ResultSet rs=pst.executeQuery(requete);
            List<Game> games=new ArrayList<>();
            while (rs.next()) {
                Game g = new Game(
                        rs.getString("name"),
                        rs.getString("genre"),
                        rs.getString("platform"),
                        rs.getString("team_mode"),
                        rs.getInt("max_players_per_match"),
                        rs.getString("icon_url")
                );
                g.setId(rs.getString("id"));
                games.add(g);
            }
            return games;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
