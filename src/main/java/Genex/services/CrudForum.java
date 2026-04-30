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
                category VARCHAR(64) NOT NULL DEFAULT 'General',
                topic_status VARCHAR(16) NOT NULL DEFAULT 'open',
                moderation_status VARCHAR(16) NOT NULL DEFAULT 'visible',
                is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
                created_at DATETIME NOT NULL
            )
            """;

    public CrudForum() {
        ensureForumsTable();
    }

    private void ensureForumsTable() {
        try (Statement st = Myconnection.getInstance().getCnx().createStatement()) {
            st.executeUpdate(CREATE_TABLE_SQL);
            ensureColumnExists("forums", "category", "VARCHAR(64) NOT NULL DEFAULT 'General'");
            ensureColumnExists("forums", "topic_status", "VARCHAR(16) NOT NULL DEFAULT 'open'");
            ensureColumnExists("forums", "moderation_status", "VARCHAR(16) NOT NULL DEFAULT 'visible'");
            ensureColumnExists("forums", "is_pinned", "BOOLEAN NOT NULL DEFAULT FALSE");
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'initialiser la table forums.", e);
        }
    }

    private void ensureColumnExists(String tableName, String columnName, String sqlType) throws SQLException {
        DatabaseMetaData metaData = Myconnection.getInstance().getCnx().getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            if (columns.next()) {
                return;
            }
        }
        try (Statement st = Myconnection.getInstance().getCnx().createStatement()) {
            st.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + sqlType);
        }
    }

    @Override
    public void addEntity(Forum forum) {
        String requete = "INSERT INTO forums (title, description, created_by, category, topic_status, moderation_status, is_pinned, created_at) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            LocalDateTime createdAt = forum.getCreatedAt() != null ? forum.getCreatedAt() : LocalDateTime.now();
            pst.setString(1, forum.getTitle());
            pst.setString(2, forum.getDescription());
            pst.setString(3, forum.getCreatedBy());
            pst.setString(4, defaultString(forum.getCategory(), "General"));
            pst.setString(5, defaultString(forum.getTopicStatus(), "open"));
            pst.setString(6, defaultString(forum.getModerationStatus(), "visible"));
            pst.setBoolean(7, forum.isPinned());
            pst.setTimestamp(8, Timestamp.valueOf(createdAt));
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
        String requete = "UPDATE forums SET title=?, description=?, created_by=?, category=?, topic_status=?, moderation_status=?, is_pinned=? WHERE id=?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, forum.getTitle());
            pst.setString(2, forum.getDescription());
            pst.setString(3, forum.getCreatedBy());
            pst.setString(4, defaultString(forum.getCategory(), "General"));
            pst.setString(5, defaultString(forum.getTopicStatus(), "open"));
            pst.setString(6, defaultString(forum.getModerationStatus(), "visible"));
            pst.setBoolean(7, forum.isPinned());
            pst.setString(8, id);
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
        String requete = "SELECT * FROM forums ORDER BY is_pinned DESC, moderation_status='reported' DESC, created_at DESC";
        try (Statement st = Myconnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                Forum f = new Forum();
                f.setId(rs.getString("id"));
                f.setTitle(rs.getString("title"));
                f.setDescription(rs.getString("description"));
                f.setCreatedBy(rs.getString("created_by"));
                f.setCategory(defaultString(rs.getString("category"), "General"));
                f.setTopicStatus(defaultString(rs.getString("topic_status"), "open"));
                f.setModerationStatus(defaultString(rs.getString("moderation_status"), "visible"));
                f.setPinned(rs.getBoolean("is_pinned"));
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

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
