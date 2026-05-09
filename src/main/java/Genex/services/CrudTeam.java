package Genex.services;

import Genex.entities.Team;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Minimal read-only service — just enough to populate ComboBoxes in Finance panels. */
public class CrudTeam {

    private Connection getCnx() {
        return Myconnection.getInstance().getCnx();
    }

    public List<Team> getAll() {
        List<Team> list = new ArrayList<>();
        String sql = "SELECT team_id, nom FROM team ORDER BY nom";
        try (Statement st = getCnx().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Team t = new Team();
                t.setId_team(rs.getInt("team_id"));
                t.setNom_team(rs.getString("nom"));
                list.add(t);
            }
        } catch (SQLException e) {
            throw new RuntimeException("CrudTeam.getAll: " + e.getMessage(), e);
        }
        return list;
    }
}
