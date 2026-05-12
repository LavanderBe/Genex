package Genex.services;

import Genex.entities.Game;
import Genex.entities.Player;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CrudPlayer_Game {
    public CrudPlayer_Game() {
    }

    public void addEntity(Player p, Game g){
        String requete="INSERT INTO player_games (player_id,game_id) " +
                "VALUES (?,?)";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getId());
            pst.setString(2, g.getId());
            pst.executeUpdate();
            System.out.println("game linked with player successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteEntity(Player p,Game g){
        String requete="DELETE FROM player_games WHERE (player_id=?) and (game_id=?)";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getId());
            pst.setString(1, g.getId());
            pst.executeUpdate();
            System.out.println("game and player unlinked successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllGames_ForPlayer(Player p){
        String requete="DELETE FROM player_games WHERE (player_id=?)";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getId());
            pst.executeUpdate();
            System.out.println("all games unlinked for the  player successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Game> get_GamesPlayed(Player p){
        String requete = "SELECT g.* " +
                "FROM player_games pg " +
                "INNER JOIN games g ON g.id = pg.game_id " +
                "WHERE pg.player_id = ?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getId());
            ResultSet rs  =pst.executeQuery();
            List<Game> l=new ArrayList<Game>();
            while (rs.next()){
                Game g=new Game(
                        rs.getString("name"),
                        rs.getString("genre"),
                        rs.getString("platform"),
                        rs.getString("team_mode"),
                        rs.getInt("max_players_per_match"),
                        rs.getString("icon_url")
                );
                l.add(g);
            }
            return l;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
