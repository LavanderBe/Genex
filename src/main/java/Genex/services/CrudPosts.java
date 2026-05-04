package Genex.services;

import Genex.entities.Posts;
import Genex.interfaces.ICrud;
import Genex.utils.Myconnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrudPosts implements ICrud<Posts> {

    @Override
    public void addEntity(Posts post) {
        String requete = "INSERT INTO posts (forum_id, author_id, title, body, created_at, updated_at) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, post.getForumId());
            pst.setString(2, post.getAuthorId());
            pst.setString(3, post.getTitle());
            pst.setString(4, post.getBody());
            pst.setString(5, post.getCreatedAt().toString());
            pst.setString(6, post.getUpdatedAt().toString());
            pst.executeUpdate();
            System.out.println("Post ajouté avec succès");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(Posts post, String id) {
        String requete = "UPDATE posts SET title=?, body=?, updated_at=? WHERE id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, post.getTitle());
            pst.setString(2, post.getBody());
            pst.setString(3, post.getUpdatedAt().toString());
            pst.setString(4, id);
            pst.executeUpdate();
            System.out.println("Post modifié avec succès");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteEntity(Posts post) {
        String requete = "DELETE FROM posts WHERE id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, post.getId());
            pst.executeUpdate();
            System.out.println("Post supprimé avec succès");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void getEntity(Posts post) {}

    public List<Posts> getAllPosts() {
        List<Posts> posts = new ArrayList<>();
        String requete = "SELECT * FROM posts ORDER BY created_at DESC";
        try {
            Statement st = Myconnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                Posts p = new Posts();
                p.setId(rs.getString("id"));
                p.setForumId(rs.getString("forum_id"));
                p.setAuthorId(rs.getString("author_id"));
                p.setTitle(rs.getString("title"));
                p.setBody(rs.getString("body"));
                posts.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return posts;
    }

    public List<Posts> getPostsByForum(String forumId) {
        List<Posts> posts = new ArrayList<>();
        String requete = "SELECT * FROM posts WHERE forum_id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, forumId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Posts p = new Posts();
                p.setId(rs.getString("id"));
                p.setForumId(rs.getString("forum_id"));
                p.setAuthorId(rs.getString("author_id"));
                p.setTitle(rs.getString("title"));
                p.setBody(rs.getString("body"));
                posts.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return posts;
    }
}
