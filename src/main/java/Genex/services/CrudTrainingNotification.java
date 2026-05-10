package Genex.services;

import Genex.entities.TrainingNotification;
import Genex.utils.Myconnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CrudTrainingNotification {

    public void createNotification(TrainingNotification notification) {
        String query = "INSERT INTO training_notifications (id, user_id, training_session_id, team_id, created_at, is_read, remind_on_login, type) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, notification.getId());
            pst.setString(2, notification.getUserId());
            pst.setString(3, notification.getTrainingSessionId());
            pst.setString(4, notification.getTeamId());
            pst.setTimestamp(5, Timestamp.valueOf(notification.getCreatedAt()));
            pst.setBoolean(6, notification.isRead());
            pst.setBoolean(7, notification.isRemindOnLogin());
            pst.setString(8, notification.getType().name());
            
            pst.executeUpdate();
            System.out.println("Notification created for user: " + notification.getUserId());
        } catch (SQLException e) {
            System.err.println("Error creating notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<TrainingNotification> getUnreadNotifications(String userId) {
        List<TrainingNotification> notifications = new ArrayList<>();
        String query = "SELECT * FROM training_notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, userId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread notifications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return notifications;
    }

    public List<TrainingNotification> getRemindOnLoginNotifications(String userId) {
        List<TrainingNotification> notifications = new ArrayList<>();
        String query = "SELECT * FROM training_notifications WHERE user_id = ? AND remind_on_login = TRUE ORDER BY created_at DESC";
        
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, userId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                notifications.add(mapResultSetToNotification(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting remind on login notifications: " + e.getMessage());
            e.printStackTrace();
        }
        
        return notifications;
    }

    public void markAsRead(String notificationId) {
        String query = "UPDATE training_notifications SET is_read = TRUE WHERE id = ?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, notificationId);
            pst.executeUpdate();
            System.out.println("Notification marked as read: " + notificationId);
        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setRemindOnLogin(String notificationId, boolean remind) {
        String query = "UPDATE training_notifications SET remind_on_login = ? WHERE id = ?";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setBoolean(1, remind);
            pst.setString(2, notificationId);
            pst.executeUpdate();
            System.out.println("Notification remind on login set to: " + remind);
        } catch (SQLException e) {
            System.err.println("Error setting remind on login: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getUnreadCount(String userId) {
        String query = "SELECT COUNT(*) FROM training_notifications WHERE user_id = ? AND is_read = FALSE";
        try {
            PreparedStatement pst = Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, userId);
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread count: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private TrainingNotification mapResultSetToNotification(ResultSet rs) throws SQLException {
        TrainingNotification notification = new TrainingNotification();
        notification.setId(rs.getString("id"));
        notification.setUserId(rs.getString("user_id"));
        notification.setTrainingSessionId(rs.getString("training_session_id"));
        notification.setTeamId(rs.getString("team_id"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            notification.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        notification.setRead(rs.getBoolean("is_read"));
        notification.setRemindOnLogin(rs.getBoolean("remind_on_login"));
        
        String typeStr = rs.getString("type");
        if (typeStr != null) {
            notification.setType(TrainingNotification.NotificationType.valueOf(typeStr));
        }
        
        return notification;
    }
}
