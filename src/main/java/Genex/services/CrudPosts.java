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
        String requete = "INSERT INTO posts (forum_id, author_id, title, body, media_type, media_url, post_type, tag, post_status, moderation_status) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, post.getForumId());
            pst.setString(2, post.getAuthorId());
            pst.setString(3, post.getTitle());
            pst.setString(4, post.getBody());
            pst.setString(5, post.getMediaType());
            pst.setString(6, post.getMediaUrl());
            pst.setString(7, post.getPostType());
            pst.setString(8, post.getTag());
            pst.setString(9, post.getPostStatus());
            pst.setString(10, post.getModerationStatus());
            pst.executeUpdate();
            ResultSet generatedKeys = pst.getGeneratedKeys();
            if (generatedKeys.next()) {
                post.setId(generatedKeys.getString(1));
            }
            System.out.println("Post ajouté avec succès");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateEntity(Posts post, String id) {
        String requete = "UPDATE posts SET title=?, body=?, media_type=?, media_url=?, post_type=?, tag=?, post_status=?, moderation_status=? WHERE id=?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, post.getTitle());
            pst.setString(2, post.getBody());
            pst.setString(3, post.getMediaType());
            pst.setString(4, post.getMediaUrl());
            pst.setString(5, post.getPostType());
            pst.setString(6, post.getTag());
            pst.setString(7, post.getPostStatus());
            pst.setString(8, post.getModerationStatus());
            pst.setString(9, id);
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
                p.setMediaType(rs.getString("media_type"));
                p.setMediaUrl(rs.getString("media_url"));
                p.setPostType(rs.getString("post_type"));
                p.setTag(rs.getString("tag"));
                p.setPostStatus(rs.getString("post_status"));
                p.setModerationStatus(rs.getString("moderation_status"));
                p.setViews(rs.getInt("views"));
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
                p.setMediaType(rs.getString("media_type"));
                p.setMediaUrl(rs.getString("media_url"));
                p.setPostType(rs.getString("post_type"));
                p.setTag(rs.getString("tag"));
                p.setPostStatus(rs.getString("post_status"));
                p.setModerationStatus(rs.getString("moderation_status"));
                p.setViews(rs.getInt("views"));
                posts.add(p);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return posts;
    }
}
