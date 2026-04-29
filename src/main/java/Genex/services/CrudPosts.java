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
                created_at DATETIME NOT NULL,
                updated_at DATETIME NOT NULL
            )
            """;

    public CrudPosts() {
        ensurePostsTable();
    }

    private void ensurePostsTable() {
        try (Statement st = Myconnection.getInstance().getCnx().createStatement()) {
            st.executeUpdate(CREATE_TABLE_SQL);
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'initialiser la table posts.", e);
        }
    }

    @Override
    public void addEntity(Posts post) {
        String requete = "INSERT INTO posts (forum_id, author_id, title, body, created_at, updated_at) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            LocalDateTime createdAt = post.getCreatedAt() != null ? post.getCreatedAt() : LocalDateTime.now();
            LocalDateTime updatedAt = post.getUpdatedAt() != null ? post.getUpdatedAt() : createdAt;
            pst.setString(1, post.getForumId());
            pst.setString(2, post.getAuthorId());
            pst.setString(3, post.getTitle());
            pst.setString(4, post.getBody());
            pst.setTimestamp(5, Timestamp.valueOf(createdAt));
            pst.setTimestamp(6, Timestamp.valueOf(updatedAt));
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
        String requete = "UPDATE posts SET title=?, body=?, updated_at=? WHERE id=?";
        try (PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete)) {
            LocalDateTime updatedAt = post.getUpdatedAt() != null ? post.getUpdatedAt() : LocalDateTime.now();
            pst.setString(1, post.getTitle());
            pst.setString(2, post.getBody());
            pst.setTimestamp(3, Timestamp.valueOf(updatedAt));
            pst.setString(4, id);
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
        String requete = "SELECT * FROM posts WHERE forum_id=? ORDER BY updated_at DESC";
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
        String requete = "SELECT * FROM posts ORDER BY updated_at DESC";
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
}
