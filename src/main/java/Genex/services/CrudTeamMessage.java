package Genex.services;

import Genex.entities.TeamMessage;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CrudTeamMessage {
    
    /**
     * Add a new message to the database
     */
    public void addMessage(TeamMessage message) {
        String query = "INSERT INTO team_messages (id, team_id, sender_id, message, message_type, sent_at) " +
                      "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            // Generate ID if not set
            if (message.getId() == null || message.getId().isEmpty()) {
                message.setId(UUID.randomUUID().toString());
            }
            
            // Set sent time if not set
            if (message.getSentAt() == null) {
                message.setSentAt(LocalDateTime.now());
            }
            
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, message.getId());
            pst.setString(2, message.getTeamId());
            pst.setString(3, message.getSenderId());
            pst.setString(4, message.getMessage());
            pst.setString(5, message.getMessageType().name());
            pst.setTimestamp(6, Timestamp.valueOf(message.getSentAt()));
            
            pst.executeUpdate();
            System.out.println("Message added successfully: " + message.getId());
            
        } catch (SQLException e) {
            System.err.println("Error adding message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Get all messages for a team (ordered by time, newest first)
     */
    public List<TeamMessage> getMessagesByTeam(String teamId) {
        return getMessagesByTeam(teamId, 50); // Default: last 50 messages
    }
    
    /**
     * Get messages for a team with limit
     */
    public List<TeamMessage> getMessagesByTeam(String teamId, int limit) {
        List<TeamMessage> messages = new ArrayList<>();
        String query = "SELECT m.*, u.username, p.nickname " +
                      "FROM team_messages m " +
                      "LEFT JOIN users u ON m.sender_id = u.id " +
                      "LEFT JOIN players p ON m.sender_id = p.user_id " +
                      "WHERE m.team_id = ? " +
                      "ORDER BY m.sent_at DESC " +
                      "LIMIT ?";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            pst.setInt(2, limit);
            
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                TeamMessage message = new TeamMessage();
                message.setId(rs.getString("id"));
                message.setTeamId(rs.getString("team_id"));
                message.setSenderId(rs.getString("sender_id"));
                message.setMessage(rs.getString("message"));
                message.setMessageType(TeamMessage.MessageType.valueOf(rs.getString("message_type")));
                message.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                message.setEdited(rs.getBoolean("is_edited"));
                
                Timestamp editedAt = rs.getTimestamp("edited_at");
                if (editedAt != null) {
                    message.setEditedAt(editedAt.toLocalDateTime());
                }
                
                // Set sender name (prefer nickname, fallback to username)
                String nickname = rs.getString("nickname");
                String username = rs.getString("username");
                message.setSenderName(nickname != null ? nickname : username);
                
                messages.add(message);
            }
            
            // Reverse to get chronological order (oldest first)
            java.util.Collections.reverse(messages);
            
        } catch (SQLException e) {
            System.err.println("Error fetching messages: " + e.getMessage());
            throw new RuntimeException(e);
        }
        
        return messages;
    }
    
    /**
     * Get messages after a specific timestamp (for polling/refresh)
     */
    public List<TeamMessage> getNewMessages(String teamId, LocalDateTime after) {
        List<TeamMessage> messages = new ArrayList<>();
        String query = "SELECT m.*, u.username, p.nickname " +
                      "FROM team_messages m " +
                      "LEFT JOIN users u ON m.sender_id = u.id " +
                      "LEFT JOIN players p ON m.sender_id = p.user_id " +
                      "WHERE m.team_id = ? AND m.sent_at > ? " +
                      "ORDER BY m.sent_at ASC";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            pst.setTimestamp(2, Timestamp.valueOf(after));
            
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                TeamMessage message = new TeamMessage();
                message.setId(rs.getString("id"));
                message.setTeamId(rs.getString("team_id"));
                message.setSenderId(rs.getString("sender_id"));
                message.setMessage(rs.getString("message"));
                message.setMessageType(TeamMessage.MessageType.valueOf(rs.getString("message_type")));
                message.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                message.setEdited(rs.getBoolean("is_edited"));
                
                // Set sender name
                String nickname = rs.getString("nickname");
                String username = rs.getString("username");
                message.setSenderName(nickname != null ? nickname : username);
                
                messages.add(message);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching new messages: " + e.getMessage());
            throw new RuntimeException(e);
        }
        
        return messages;
    }
    
    /**
     * Update a message (for editing)
     */
    public void updateMessage(TeamMessage message) {
        String query = "UPDATE team_messages SET message = ?, is_edited = TRUE, edited_at = ? " +
                      "WHERE id = ?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, message.getMessage());
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pst.setString(3, message.getId());
            
            pst.executeUpdate();
            System.out.println("Message updated successfully: " + message.getId());
            
        } catch (SQLException e) {
            System.err.println("Error updating message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Delete a message
     */
    public void deleteMessage(String messageId) {
        String query = "DELETE FROM team_messages WHERE id = ?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, messageId);
            pst.executeUpdate();
            System.out.println("Message deleted successfully: " + messageId);
            
        } catch (SQLException e) {
            System.err.println("Error deleting message: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Get message count for a team
     */
    public int getMessageCount(String teamId) {
        String query = "SELECT COUNT(*) as count FROM team_messages WHERE team_id = ?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, teamId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
            
        } catch (SQLException e) {
            System.err.println("Error counting messages: " + e.getMessage());
        }
        return 0;
    }
}
