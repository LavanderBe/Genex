package Genex.services;

import Genex.entities.Tounament;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CrudTournament implements ICrud<Tounament> {

    public CrudTournament() {}

    @Override
    public void addEntity(Tounament t) {

        String requete = "INSERT INTO tournaments " +
                "(tournament_name, game_id, center_id, format, participant_type, starts_at, ends_at, prize_pool, state, max_players, " +
                "challonge_id, challonge_url, challonge_url_slug, is_synced, is_started) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getTournamentName());
            pst.setString(2, t.getGame_id());
            pst.setString(3, t.getCenter_id());
            pst.setString(4, t.getFormat());
            pst.setString(5, t.getParticipant_type());
            pst.setString(6, t.getStarts_at().toString());
            pst.setString(7, t.getEnds_at().toString());
            pst.setDouble(8, t.getPrize_pool());
            pst.setString(9, t.getState() != null ? t.getState() : Tounament.TournamentState.REGISTRATION_OPEN.name());
            pst.setInt(10, t.getMaxPlayers());
            pst.setString(11, t.getChallongeId());
            pst.setString(12, t.getChallongeUrl());
            pst.setString(13, t.getChallongeUrlSlug());
            pst.setBoolean(14, t.isSynced());
            pst.setBoolean(15, t.isStarted());
            pst.executeUpdate();
            System.out.println("Tournament added successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(Tounament t, String id) {

        String requete = "UPDATE tournaments SET " +
                "tournament_name=?, game_id=?, center_id=?, format=?, participant_type=?, " +
                "starts_at=?, ends_at=?, prize_pool=?, state=?, max_players=?, " +
                "challonge_id=?, challonge_url=?, challonge_url_slug=?, is_synced=?, is_started=? WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getTournamentName());
            pst.setString(2, t.getGame_id());
            pst.setString(3, t.getCenter_id());
            pst.setString(4, t.getFormat());
            pst.setString(5, t.getParticipant_type());
            pst.setString(6, t.getStarts_at().toString());
            pst.setString(7, t.getEnds_at().toString());
            pst.setDouble(8, t.getPrize_pool());
            pst.setString(9, t.getState());
            pst.setInt(10, t.getMaxPlayers());
            pst.setString(11, t.getChallongeId());
            pst.setString(12, t.getChallongeUrl());
            pst.setString(13, t.getChallongeUrlSlug());
            pst.setBoolean(14, t.isSynced());
            pst.setBoolean(15, t.isStarted());
            pst.setString(16, id);
            pst.executeUpdate();
            System.out.println("Tournament updated successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntity(Tounament t) {

        String requete = "DELETE FROM tournaments WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getTournamentId());
            pst.executeUpdate();
            System.out.println("Tournament deleted successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getEntity(Tounament t) {

        String requete = "SELECT * FROM tournaments WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, t.getTournamentId());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                t.setTournamentName(rs.getString("tournament_name"));
                t.setGame_id(rs.getString("game_id"));
                t.setCenter_id(rs.getString("center_id"));
                t.setFormat(rs.getString("format"));
                t.setParticipant_type(rs.getString("participant_type"));
                t.setStarts_at(rs.getTimestamp("starts_at").toLocalDateTime());
                t.setEnds_at(rs.getTimestamp("ends_at").toLocalDateTime());
                t.setPrize_pool(rs.getDouble("prize_pool"));
                t.setState(rs.getString("state"));
                t.setMaxPlayers(rs.getInt("max_players"));
                t.setChallongeId(rs.getString("challonge_id"));
                t.setChallongeUrl(rs.getString("challonge_url"));
                t.setChallongeUrlSlug(rs.getString("challonge_url_slug"));
                t.setSynced(rs.getBoolean("is_synced"));
                t.setStarted(rs.getBoolean("is_started"));
                System.out.println("Tournament loaded");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Tounament> getAll() {

        List<Tounament> list = new ArrayList<>();
        String requete = "SELECT * FROM tournaments";

        try {
            Statement st = Myconnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);

            while (rs.next()) {
                Tounament t = new Tounament();
                t.setTournamentId(rs.getString("id"));
                t.setTournamentName(rs.getString("tournament_name"));
                t.setGame_id(rs.getString("game_id"));
                t.setCenter_id(rs.getString("center_id"));
                t.setFormat(rs.getString("format"));
                t.setParticipant_type(rs.getString("participant_type"));
                t.setStarts_at(rs.getTimestamp("starts_at").toLocalDateTime());
                t.setEnds_at(rs.getTimestamp("ends_at").toLocalDateTime());
                t.setPrize_pool(rs.getDouble("prize_pool"));
                t.setState(rs.getString("state"));
                t.setMaxPlayers(rs.getInt("max_players"));
                t.setChallongeId(rs.getString("challonge_id"));
                t.setChallongeUrl(rs.getString("challonge_url"));
                t.setChallongeUrlSlug(rs.getString("challonge_url_slug"));
                t.setSynced(rs.getBoolean("is_synced"));
                t.setStarted(rs.getBoolean("is_started"));
                list.add(t);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}