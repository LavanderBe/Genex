package Genex.services;
import Genex.entities.TournamentParticipants;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CrudTournamentParticipant implements ICrud<TournamentParticipants> {

    public CrudTournamentParticipant() {}

    @Override
    public void addEntity(TournamentParticipants p) {

        String requete = "INSERT INTO tournament_participants " +
                "(tournament_id, participant_id, seed, status) " +
                "VALUES (?,?,?,?)";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);

            pst.setString(1, p.getTournamentId());
            pst.setString(2, p.getParticipantId());
            pst.setInt(3, p.getSeed());
            pst.setString(4, p.getStatus().name());

            pst.executeUpdate();
            System.out.println("Participant added successfully");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(TournamentParticipants p, String id) {

        String requete = "UPDATE tournament_participants SET " +
                "tournament_id=?, participant_id=?, seed=?, status=? " +
                "WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);

            pst.setString(1, p.getTournamentId());
            pst.setString(2, p.getParticipantId());
            pst.setInt(3, p.getSeed());
            pst.setString(4, p.getStatus().name());
            pst.setString(5, id);

            pst.executeUpdate();
            System.out.println("Participant updated successfully");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntity(TournamentParticipants p) {

        String requete = "DELETE FROM tournament_participants WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);

            pst.setString(1, p.getId());

            pst.executeUpdate();
            System.out.println("Participant deleted successfully");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getEntity(TournamentParticipants p) {

        String requete = "SELECT * FROM tournament_participants WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);

            pst.setString(1, p.getId());
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                p.setTournamentId(rs.getString("tournament_id"));
                p.setParticipantId(rs.getString("participant_id"));
                p.setSeed(rs.getInt("seed"));
                p.setStatus(TournamentParticipants.Status.valueOf(rs.getString("status")));
                System.out.println("Participant loaded");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Get all participants for a specific tournament
    public List<TournamentParticipants> getAll(String tournamentId) {

        List<TournamentParticipants> list = new ArrayList<>();

        String requete = "SELECT * FROM tournament_participants WHERE tournament_id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, tournamentId);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                TournamentParticipants p = new TournamentParticipants();
                p.setId(rs.getString("id"));
                p.setTournamentId(rs.getString("tournament_id"));
                p.setParticipantId(rs.getString("participant_id"));
                p.setSeed(rs.getInt("seed"));
                p.setStatus(TournamentParticipants.Status.valueOf(rs.getString("status")));
                list.add(p);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return list;
    }
}