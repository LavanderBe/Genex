package Genex.services;

import Genex.entities.Posts;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CrudPosts implements ICrud<Posts> {
    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS posts (
                id INT AUTO_INCREMENT PRIMARY KEY,
                forum_id VARCHAR(64) NOT NULL,
                author_id VARCHAR(255) NOT NULL,
                title VARCHAR(255) NOT NULL,
                body TEXT NOT NULL,
                media_type VARCHAR(16),
                media_url VARCHAR(1024),
                post_type VARCHAR(16) NOT NULL DEFAULT 'text',
                tag VARCHAR(64),
                post_status VARCHAR(16) NOT NULL DEFAULT 'published',
                moderation_status VARCHAR(16) NOT NULL DEFAULT 'visible',
                views INT NOT NULL DEFAULT 0,
                created_at DATETIME NOT NULL,
                updated_at DATETIME NOT NULL
            )
            """;

    private static final String CREATE_POST_VERSIONS_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS post_versions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                post_id INT NOT NULL,
                title VARCHAR(255) NOT NULL,
                body TEXT NOT NULL,
                media_url VARCHAR(1024),
                updated_at DATETIME NOT NULL
            )
            """;

    public CrudPosts() {
        ensurePostsTable();
    }

    private void ensurePostsTable() {
        try (Statement st = Myconnection.getInstance().getCnx().createStatement()) {
            st.executeUpdate(CREATE_TABLE_SQL);
            st.executeUpdate(CREATE_POST_VERSIONS_TABLE_SQL);
            ensureColumnExists("posts", "media_type", "VARCHAR(16)");
            ensureColumnExists("posts", "media_url", "VARCHAR(1024)");
            ensureColumnExists("posts", "post_type", "VARCHAR(16) NOT NULL DEFAULT 'text'");
            ensureColumnExists("posts", "tag", "VARCHAR(64)");
            ensureColumnExists("posts", "post_status", "VARCHAR(16) NOT NULL DEFAULT 'published'");
            ensureColumnExists("posts", "moderation_status", "VARCHAR(16) NOT NULL DEFAULT 'visible'");
            ensureColumnExists("posts", "views", "INT NOT NULL DEFAULT 0");
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'initialiser la table posts.", e);
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
    public void addEntity(Posts post) {
        String requete = "INSERT INTO posts (forum_id, author_id, title, body, media_type, media_url, post_type, tag, post_status, moderation_status, views, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            LocalDateTime createdAt = post.getCreatedAt() != null ? post.getCreatedAt() : LocalDateTime.now();
            LocalDateTime updatedAt = post.getUpdatedAt() != null ? post.getUpdatedAt() : createdAt;
            pst.setString(1, post.getForumId());
            pst.setString(2, post.getAuthorId());
            pst.setString(3, post.getTitle());
            pst.setString(4, post.getBody());
            pst.setString(5, post.getMediaType());
            pst.setString(6, post.getMediaUrl());
            pst.setString(7, defaultString(post.getPostType(), "text"));
            pst.setString(8, post.getTag());
            pst.setString(9, defaultString(post.getPostStatus(), "published"));
            pst.setString(10, defaultString(post.getModerationStatus(), "visible"));
            pst.setInt(11, Math.max(0, post.getViews()));
            pst.setTimestamp(12, Timestamp.valueOf(createdAt));
            pst.setTimestamp(13, Timestamp.valueOf(updatedAt));
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new IllegalStateException("Échec de création du post.");
            }
            System.out.println("Post ajouté avec succès");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL pendant la création du post.", e);
        }
    }

    @Override
    public void updateEntity(Posts post, String id) {
        savePostVersion(id);

        String requete = "UPDATE posts SET title=?, body=?, media_type=?, media_url=?, post_type=?, tag=?, post_status=?, moderation_status=?, views=?, updated_at=? WHERE id=?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            LocalDateTime updatedAt = post.getUpdatedAt() != null ? post.getUpdatedAt() : LocalDateTime.now();
            pst.setString(1, post.getTitle());
            pst.setString(2, post.getBody());
            pst.setString(3, post.getMediaType());
            pst.setString(4, post.getMediaUrl());
            pst.setString(5, defaultString(post.getPostType(), "text"));
            pst.setString(6, post.getTag());
            pst.setString(7, defaultString(post.getPostStatus(), "published"));
            pst.setString(8, defaultString(post.getModerationStatus(), "visible"));
            pst.setInt(9, Math.max(0, post.getViews()));
            pst.setTimestamp(10, Timestamp.valueOf(updatedAt));
            pst.setString(11, id);
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new IllegalStateException("Aucun post trouvé avec l'id " + id + ".");
            }
            System.out.println("Post modifié avec succès");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL pendant la mise à jour du post.", e);
        }
    }

    @Override
    public void deleteEntity(Posts post) {
        String requete = "DELETE FROM posts WHERE id=?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, post.getId());
            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new IllegalStateException("Aucun post trouvé avec l'id " + post.getId() + ".");
            }
            System.out.println("Post supprimé avec succès");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL pendant la suppression du post.", e);
        }
    }

    @Override
    public void getEntity(Posts post) {}

    public List<Posts> getPostsByForum(String forumId) {
        List<Posts> posts = new ArrayList<>();
        String requete = "SELECT * FROM posts WHERE forum_id=? ORDER BY moderation_status='reported' DESC, updated_at DESC";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            pst.setString(1, forumId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                posts.add(mapPost(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL pendant le chargement des posts.", e);
        }
        return posts;
    }

    public List<Posts> getAllPosts() {
        List<Posts> posts = new ArrayList<>();
        String requete = "SELECT * FROM posts ORDER BY moderation_status='reported' DESC, updated_at DESC";
        try (Statement st = Myconnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                posts.add(mapPost(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL pendant le chargement des posts.", e);
        }
        return posts;
    }

    private Posts mapPost(ResultSet rs) throws SQLException {
        Posts p = new Posts();
        p.setId(rs.getString("id"));
        p.setForumId(rs.getString("forum_id"));
        p.setAuthorId(rs.getString("author_id"));
        p.setTitle(rs.getString("title"));
        p.setBody(rs.getString("body"));
        p.setMediaType(rs.getString("media_type"));
        p.setMediaUrl(rs.getString("media_url"));
        p.setPostType(defaultString(rs.getString("post_type"), "text"));
        p.setTag(rs.getString("tag"));
        p.setPostStatus(defaultString(rs.getString("post_status"), "published"));
        p.setModerationStatus(defaultString(rs.getString("moderation_status"), "visible"));
        p.setViews(Math.max(0, rs.getInt("views")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (createdAt != null) {
            p.setCreatedAt(createdAt.toLocalDateTime());
        }
        if (updatedAt != null) {
            p.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        return p;
    }

    private void savePostVersion(String postId) {
        String versionQuery = """
                INSERT INTO post_versions (post_id, title, body, media_url, updated_at)
                SELECT id, title, body, media_url, updated_at
                FROM posts WHERE id=?
                """;
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(versionQuery)) {
            pst.setString(1, postId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur SQL pendant la sauvegarde d'historique du post.", e);
        }
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
