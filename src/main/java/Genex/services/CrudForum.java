package Genex.services;

import Genex.entities.Forum;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CrudForum implements ICrud<Forum> {
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS forums (
                id INT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description TEXT NOT NULL,
                created_by VARCHAR(255) NOT NULL,
                created_at DATETIME NOT NULL
            )
            """;

    public CrudForum() {
        ensureForumsTable();
    }

    private void ensureForumsTable() {
        try (Statement st = Myconnection.getInstance().getCnx().createStatement()) {
            st.executeUpdate(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'initialiser la table forums.", e);
        }
    }

    @Override
    public void addEntity(Forum forum) {
        String requete = "INSERT INTO forums (title, description, created_by, created_at) VALUES (?,?,?,?)";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            LocalDateTime createdAt = forum.getCreatedAt() != null ? forum.getCreatedAt() : LocalDateTime.now();
            pst.setString(1, forum.getTitle());
            pst.setString(2, forum.getDescription());
            pst.setString(3, forum.getCreatedBy());
            pst.setTimestamp(4, Timestamp.valueOf(createdAt));
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new IllegalStateException("Échec de création du forum.");
            }
            System.out.println("Forum ajouté avec succès");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL pendant la création du forum.", e);
        }
    }

    @Override
    public void updateEntity(Forum forum, String id) {
        String requete = "UPDATE forums SET title=?, description=?, created_by=? WHERE id=?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, forum.getTitle());
            pst.setString(2, forum.getDescription());
            pst.setString(3, forum.getCreatedBy());
            pst.setString(4, id);
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new IllegalStateException("Aucun forum trouvé avec l'id " + id + ".");
            }
            System.out.println("Forum modifié avec succès");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL pendant la mise à jour du forum.", e);
        }
    }

    @Override
    public void deleteEntity(Forum forum) {
        String requete = "DELETE FROM forums WHERE id=?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, forum.getId());
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new IllegalStateException("Aucun forum trouvé avec l'id " + forum.getId() + ".");
            }
            System.out.println("Forum supprimé avec succès");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL pendant la suppression du forum.", e);
        }
    }

    @Override
    public void getEntity(Forum forum) {
        // utilisé pour chercher un forum par id
    }

    public List<Forum> getAllForums() {
        List<Forum> forums = new ArrayList<>();
        String requete = "SELECT * FROM forums ORDER BY created_at DESC";
        try (Statement st = Myconnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                Forum f = new Forum();
                f.setId(rs.getString("id"));
                f.setTitle(rs.getString("title"));
                f.setDescription(rs.getString("description"));
                f.setCreatedBy(rs.getString("created_by"));
                Timestamp createdAt = rs.getTimestamp("created_at");
                if (createdAt != null) {
                    f.setCreatedAt(createdAt.toLocalDateTime());
                }
                forums.add(f);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL pendant le chargement des forums.", e);
        }
        return forums;
    }
}
