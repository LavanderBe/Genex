package Genex.services;

import Genex.entities.TournamentParticipants;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CrudTournamentParticipant implements ICrud<TournamentParticipants> {

    public CrudTournamentParticipant() {}

    // ─── Verification helpers ─────────────────────────────────────────────────

    private boolean playerExists(String playerId) {
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx()
                    .prepareStatement("SELECT user_id FROM players WHERE user_id=?");
            pst.setString(1, playerId);
            return pst.executeQuery().next();
        } catch (SQLException e) {
            return false;
        }
    }

    // ─── Helper to populate from ResultSet ───────────────────────────────────

    private TournamentParticipants populate(ResultSet rs) throws SQLException {
        TournamentParticipants p = new TournamentParticipants();
        p.setId(rs.getString("id"));
        p.setTournamentId(rs.getString("tournament_id"));
        p.setParticipantId(rs.getString("participant_id"));
        p.setSeed(rs.getInt("seed"));
        p.setStatus(TournamentParticipants.Status.valueOf(rs.getString("status")));
        p.setChallongeParticipantId(rs.getString("challonge_participant_id"));
        p.setEliminationReason(rs.getString("elimination_reason"));
        p.setFinalPlacement(rs.getObject("final_placement") != null ? rs.getInt("final_placement") : null);
        p.setEliminatedAtRound(rs.getObject("eliminated_at_round") != null ? rs.getInt("eliminated_at_round") : null);
        return p;
    }

    // ─── CRUD ─────────────────────────────────────────────────────────────────

    @Override
    public void addEntity(TournamentParticipants p) {
        if ("SOLO".equals(p.getTournamentType())) {
            if (!playerExists(p.getParticipantId())) {
                throw new IllegalArgumentException("Player not found: " + p.getParticipantId());
            }
        }

        String requete = "INSERT INTO tournament_participants " +
                "(tournament_id, participant_id, seed, status, challonge_participant_id) VALUES (?,?,?,?,?)";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getTournamentId());
            pst.setString(2, p.getParticipantId());
            pst.setInt(3, p.getSeed());
            pst.setString(4, p.getStatus().name());
            pst.setString(5, p.getChallongeParticipantId());
            pst.executeUpdate();
            System.out.println("Participant added successfully");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(TournamentParticipants p, String id) {
        String requete = "UPDATE tournament_participants SET " +
                "tournament_id=?, participant_id=?, seed=?, status=?, " +
                "elimination_reason=?, final_placement=?, eliminated_at_round=? WHERE id=?";

        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, p.getTournamentId());
            pst.setString(2, p.getParticipantId());
            pst.setInt(3, p.getSeed());
            pst.setString(4, p.getStatus().name());
            pst.setString(5, p.getEliminationReason());
            if (p.getFinalPlacement() != null) pst.setInt(6, p.getFinalPlacement());
            else pst.setNull(6, java.sql.Types.INTEGER);
            if (p.getEliminatedAtRound() != null) pst.setInt(7, p.getEliminatedAtRound());
            else pst.setNull(7, java.sql.Types.INTEGER);
            pst.setString(8, id);
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
                TournamentParticipants loaded = populate(rs);
                p.setTournamentId(loaded.getTournamentId());
                p.setParticipantId(loaded.getParticipantId());
                p.setSeed(loaded.getSeed());
                p.setStatus(loaded.getStatus());
                p.setChallongeParticipantId(loaded.getChallongeParticipantId());
                p.setEliminationReason(loaded.getEliminationReason());
                p.setFinalPlacement(loaded.getFinalPlacement());
                p.setEliminatedAtRound(loaded.getEliminatedAtRound());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Get all participants for a tournament
    public List<TournamentParticipants> getAll(String tournamentId) {
        List<TournamentParticipants> list = new ArrayList<>();
        String requete = "SELECT * FROM tournament_participants WHERE tournament_id=? ORDER BY seed";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, tournamentId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(populate(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    // Get a specific player's participation record in a tournament
    public TournamentParticipants getPlayerParticipation(String tournamentId, String playerId) {
        String requete = "SELECT * FROM tournament_participants WHERE tournament_id=? AND participant_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, tournamentId);
            pst.setString(2, playerId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return populate(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // Check if a player is already participating
    public boolean isPlayerParticipating(String tournamentId, String playerId) {
        String requete = "SELECT COUNT(*) FROM tournament_participants WHERE tournament_id=? AND participant_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, tournamentId);
            pst.setString(2, playerId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    // Get participant count
    public int getParticipantCount(String tournamentId) {
        String requete = "SELECT COUNT(*) FROM tournament_participants WHERE tournament_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, tournamentId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    // Remove a player from a tournament (only before start)
    public void removePlayerFromTournament(String tournamentId, String playerId) {
        String requete = "DELETE FROM tournament_participants WHERE tournament_id=? AND participant_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, tournamentId);
            pst.setString(2, playerId);
            pst.executeUpdate();
            System.out.println("Player removed from tournament");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Withdraw a player from a started tournament
    public void withdrawPlayer(String tournamentId, String playerId, int currentRound) {
        String requete = "UPDATE tournament_participants SET " +
                "status='ELIMINATED', elimination_reason='WITHDREW', eliminated_at_round=? " +
                "WHERE tournament_id=? AND participant_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, currentRound);
            pst.setString(2, tournamentId);
            pst.setString(3, playerId);
            pst.executeUpdate();
            System.out.println("Player withdrew from tournament");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Mark a player as eliminated (lost)
    public void eliminatePlayer(String tournamentId, String playerId, int round) {
        String requete = "UPDATE tournament_participants SET " +
                "status='ELIMINATED', elimination_reason='LOST', eliminated_at_round=? " +
                "WHERE tournament_id=? AND participant_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, round);
            pst.setString(2, tournamentId);
            pst.setString(3, playerId);
            pst.executeUpdate();
            System.out.println("Player eliminated at round " + round);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Set final placement for a player
    public void setFinalPlacement(String tournamentId, String playerId, int placement) {
        String requete = "UPDATE tournament_participants SET final_placement=? " +
                "WHERE tournament_id=? AND participant_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, placement);
            pst.setString(2, tournamentId);
            pst.setString(3, playerId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Update Challonge participant ID
    public void updateChallongeParticipantId(String participantRowId, String challongeParticipantId) {
        String requete = "UPDATE tournament_participants SET challonge_participant_id=? WHERE id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, challongeParticipantId);
            pst.setString(2, participantRowId);
            pst.executeUpdate();
            System.out.println("Challonge participant ID saved: " + challongeParticipantId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Get all tournaments a player has participated in
    public List<TournamentParticipants> getPlayerHistory(String playerId) {
        List<TournamentParticipants> list = new ArrayList<>();
        String requete = "SELECT * FROM tournament_participants WHERE participant_id=? ORDER BY id DESC";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, playerId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) list.add(populate(rs));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
