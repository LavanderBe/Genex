package Genex.services;

import java.sql.*;

import Genex.entities.Player;
import Genex.utils.Myconnection;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        String requete = "INSERT INTO players (first_name, last_name, nickname, cin, date_of_birth, nationality, city, user_id, avatar_url) " +
                "VALUES (?,?,?,?,?,?,?,?,?)";
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
            pst.setString(9, p.getAvatar_url());
            pst.executeUpdate();
            System.out.println("player added successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateEntity(Player p,String cin) {
        String requete = "UPDATE players SET first_name=?, last_name=?, nickname=?, cin=?, date_of_birth=?, nationality=?, city=?, avatar_url=? WHERE cin=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getPrenom());
            pst.setString(2, p.getNom());
            pst.setString(3, p.getNickname());
            pst.setString(4, p.getCin());
            pst.setDate(5, Date.valueOf(p.getBirthday()));
            pst.setString(6, p.getNationality());
            pst.setString(7, p.getCity());
            pst.setString(8, p.getAvatar_url());
            pst.setString(9, cin);
            pst.executeUpdate();
            System.out.println("player updated successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void deleteEntity(String id) {
        String requete = "DELETE FROM players WHERE user_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, id);
            pst.executeUpdate();
            System.out.println("player deleted successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Player> getEntities() {
        List<Player> players = new ArrayList<>();
        String requete = "SELECT p.*, u.id as user_id, u.username FROM players p " +
                        "LEFT JOIN users u ON p.user_id = u.id";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Player p = new Player(rs.getDate("date_of_birth").toLocalDate(),rs.getString("first_name"),rs.getString("last_name"),rs.getString("nickname"), rs.getString("cin"),rs.getString("nationality"),rs.getString("city"));
                p.setId(rs.getString("user_id"));
                p.setUsername(rs.getString("username"));
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

    public Player getPlayerByUserId(String userId) {
        String query = "SELECT p.*, u.username, u.email, u.role " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE p.user_id = ?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query)) {
            pst.setString(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Player player = new Player();
                player.setId(rs.getString("user_id"));
                player.setUsername(rs.getString("username"));
                player.setEmail(rs.getString("email"));
                player.setRole(rs.getString("role"));

                player.setPrenom(rs.getString("first_name"));
                player.setNom(rs.getString("last_name"));
                player.setNickname(rs.getString("nickname"));
                player.setCin(rs.getString("cin"));

                Date birthDate = rs.getDate("date_of_birth");
                if (birthDate != null) {
                    player.setBirthday(birthDate.toLocalDate());
                }

                player.setNationality(rs.getString("nationality"));
                player.setCity(rs.getString("city"));
                player.setTacticalXp(rs.getInt("tactical_xp"));
                player.setTotalAttempts(rs.getInt("total_attempts"));
                player.setCorrectAnswers(rs.getInt("correct_answers"));
                player.setAvatar_url(rs.getString("avatar_url"));
                return player;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    public Player getPlayerByNickname(String nickname){
        String query = "SELECT p.*, u.username, u.email, u.role " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE p.nickname = ?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query)) {
            pst.setString(1, nickname);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Player player = new Player();
                player.setId(rs.getString("user_id"));

                player.setUsername(rs.getString("username"));
                player.setEmail(rs.getString("email"));
                player.setRole(rs.getString("role"));

                player.setPrenom(rs.getString("first_name"));
                player.setNom(rs.getString("last_name"));
                player.setNickname(rs.getString("nickname"));
                player.setCin(rs.getString("cin"));

                Date birthDate = rs.getDate("date_of_birth");

                player.setBirthday(birthDate.toLocalDate());

                player.setNationality(rs.getString("nationality"));
                player.setCity(rs.getString("city"));
                player.setAvatar_url(rs.getString("avatar_url"));
                return player;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Player> getEverythingPlayers(){
        String query = "SELECT p.*, u.username, u.email, u.role " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.id ";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query)) {
            ResultSet rs = pst.executeQuery();
            List <Player> allp=new ArrayList<>();
            while (rs.next()) {
                Player player = new Player();
                player.setId(rs.getString("user_id"));

                player.setUsername(rs.getString("username"));
                player.setEmail(rs.getString("email"));
                player.setRole(rs.getString("role"));

                player.setPrenom(rs.getString("first_name"));
                player.setNom(rs.getString("last_name"));
                player.setNickname(rs.getString("nickname"));
                player.setCin(rs.getString("cin"));

                Date birthDate = rs.getDate("date_of_birth");

                player.setBirthday(birthDate.toLocalDate());
                player.setNationality(rs.getString("nationality"));
                player.setCity(rs.getString("city"));
                player.setAvatar_url(rs.getString("avatar_url"));
                allp.add(player);
            }
            return allp;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Player getPlayerInfo(String id){
        String requete = "SELECT p.*, u.username, u.email, u.role " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE user_id=? ";
        try  {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                Player player = new Player();
                player.setId(rs.getString("user_id"));

                player.setUsername(rs.getString("username"));
                player.setEmail(rs.getString("email"));
                player.setRole(rs.getString("role"));

                player.setPrenom(rs.getString("first_name"));
                player.setNom(rs.getString("last_name"));
                player.setNickname(rs.getString("nickname"));
                player.setCin(rs.getString("cin"));

                Date birthDate = rs.getDate("date_of_birth");

                player.setBirthday(birthDate.toLocalDate());
                player.setNationality(rs.getString("nationality"));
                player.setCity(rs.getString("city"));
                player.setAvatar_url(rs.getString("avatar_url"));
                player.setGames_played(new CrudPlayer_Game().get_GamesPlayed(player));
                System.out.println("ya mouniiir AAAAAAAAAAAAA");
                return player;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean sendPromotionRequest(Player p){
        String requete="INSERT INTO promotion_requests (player_id, date) " +
                "Values (?,?)";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getId());
            Timestamp now = new Timestamp(System.currentTimeMillis());
            pst.setTimestamp(2, now);
            pst.executeUpdate();
            System.out.println("Request sent successfully");
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPromotionSent(Player p){
        String requete="SELECT * " +
                "FROM promotion_requests " +
                "WHERE player_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getId());
            ResultSet rs =pst.executeQuery();
            if (rs.next()) return true;
            else return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteRequest(Player p){
        String requete="DELETE FROM promotion_requests WHERE player_id=? ";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getId());
            pst.executeUpdate();
            System.out.printf("Request deleted succesfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Player> getCoaches(){
        String requete = "SELECT p.*, u.username, u.email, u.role " +
                "FROM players p " +
                "JOIN users u ON p.user_id = u.id " +
                "WHERE u.role='coach' ";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            ResultSet rs = pst.executeQuery();
            List <Player> allp=new ArrayList<>();
            while (rs.next()) {
                Player player = new Player();
                player.setId(rs.getString("user_id"));

                player.setUsername(rs.getString("username"));
                player.setEmail(rs.getString("email"));
                player.setRole(rs.getString("role"));

                player.setPrenom(rs.getString("first_name"));
                player.setNom(rs.getString("last_name"));
                player.setNickname(rs.getString("nickname"));
                player.setCin(rs.getString("cin"));

                Date birthDate = rs.getDate("date_of_birth");

                player.setBirthday(birthDate.toLocalDate());
                player.setNationality(rs.getString("nationality"));
                player.setCity(rs.getString("city"));
                player.setAvatar_url(rs.getString("avatar_url"));
                allp.add(player);
            }
            return allp;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Player> getPromotionRequesters(){
        String requete="SELECT \n" +
                "    u.id,\n" +
                "    u.username,\n" +
                "    u.email,\n" +
                "    u.role,\n" +
                "    u.created_at AS account_created_at,\n" +
                "    p.first_name,\n" +
                "    p.last_name,\n" +
                "    p.nickname,\n" +
                "    p.cin,\n" +
                "    p.date_of_birth,\n" +
                "    p.nationality,\n" +
                "    p.city,\n" +
                "    p.avatar_url,\n" +
                "    pr.date AS request_date,\n" +
                "    pr.status AS promotion_status\n" +
                "FROM promotion_requests pr\n" +
                "JOIN users u ON pr.player_id = u.id\n" +
                "JOIN players p ON pr.player_id = p.user_id\n" +
                "ORDER BY pr.date DESC;";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            ResultSet rs = pst.executeQuery();
            List <Player> allp=new ArrayList<>();
            while (rs.next()) {
                Player player = new Player();
                player.setId(rs.getString("id"));

                player.setUsername(rs.getString("username"));
                player.setEmail(rs.getString("email"));
                player.setRole(rs.getString("role"));

                player.setPrenom(rs.getString("first_name"));
                player.setNom(rs.getString("last_name"));
                player.setNickname(rs.getString("nickname"));
                player.setCin(rs.getString("cin"));

                Date birthDate = rs.getDate("date_of_birth");

                player.setBirthday(birthDate.toLocalDate());
                player.setNationality(rs.getString("nationality"));
                player.setCity(rs.getString("city"));
                player.setAvatar_url(rs.getString("avatar_url"));
                player.setStatus(rs.getString("promotion_status"));
                Date Request_date=rs.getDate("request_date");
                player.setRequest_date(Request_date.toLocalDate());
                allp.add(player);
            }
            return allp;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptRequest(Player p){
        String requete="UPDATE promotion_requests " +
                "SET status='approved' " +
                "WHERE player_id=?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, p.getId());
            pst.executeUpdate();
            System.out.println("request accepted successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rejectRequest(Player p){
        String requete="UPDATE promotion_requests " +
                "SET status='rejected' " +
                "WHERE player_id=?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, p.getId());
            pst.executeUpdate();
            System.out.println("Request rejected successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



}
