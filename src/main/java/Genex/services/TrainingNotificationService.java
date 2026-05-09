package Genex.services;

import Genex.entities.Player;
import Genex.entities.TrainingNotification;
import Genex.entities.TrainingSession;

import java.util.List;

public class TrainingNotificationService {

    private CrudTrainingNotification crudNotification;
    private CrudTeamMember crudTeamMember;

    public TrainingNotificationService() {
        this.crudNotification = new CrudTrainingNotification();
        this.crudTeamMember = new CrudTeamMember();
    }

    /**
     * Notify all team members about a new training session
     */
    public void notifyNewTrainingSession(TrainingSession session) {
        try {
            // Get all team members
            List<Player> members = crudTeamMember.getMembersByTeam(session.getTeamId());
            
            System.out.println("Notifying " + members.size() + " team members about new training session: " + session.getTitle());
            
            // Create notification for each member
            for (Player member : members) {
                TrainingNotification notification = new TrainingNotification(
                    member.getId(),
                    session.getId(),
                    session.getTeamId(),
                    TrainingNotification.NotificationType.NEW_SESSION
                );
                
                crudNotification.createNotification(notification);
            }
            
            System.out.println("✓ Notifications sent to all team members");
            
        } catch (Exception e) {
            System.err.println("Error notifying team members: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Notify all team members about an updated training session
     */
    public void notifyUpdatedTrainingSession(TrainingSession session) {
        try {
            List<Player> members = crudTeamMember.getMembersByTeam(session.getTeamId());
            
            for (Player member : members) {
                TrainingNotification notification = new TrainingNotification(
                    member.getId(),
                    session.getId(),
                    session.getTeamId(),
                    TrainingNotification.NotificationType.SESSION_UPDATED
                );
                
                crudNotification.createNotification(notification);
            }
            
            System.out.println("✓ Update notifications sent to all team members");
            
        } catch (Exception e) {
            System.err.println("Error notifying about session update: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Notify all team members about a cancelled training session
     */
    public void notifyCancelledTrainingSession(TrainingSession session) {
        try {
            List<Player> members = crudTeamMember.getMembersByTeam(session.getTeamId());
            
            for (Player member : members) {
                TrainingNotification notification = new TrainingNotification(
                    member.getId(),
                    session.getId(),
                    session.getTeamId(),
                    TrainingNotification.NotificationType.SESSION_CANCELLED
                );
                
                crudNotification.createNotification(notification);
            }
            
            System.out.println("✓ Cancellation notifications sent to all team members");
            
        } catch (Exception e) {
            System.err.println("Error notifying about session cancellation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get unread notifications for a user
     */
    public List<TrainingNotification> getUnreadNotifications(String userId) {
        return crudNotification.getUnreadNotifications(userId);
    }

    /**
     * Get remind on login notifications for a user
     */
    public List<TrainingNotification> getRemindOnLoginNotifications(String userId) {
        return crudNotification.getRemindOnLoginNotifications(userId);
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(String notificationId) {
        crudNotification.markAsRead(notificationId);
    }

    /**
     * Set remind on login flag
     */
    public void setRemindOnLogin(String notificationId, boolean remind) {
        crudNotification.setRemindOnLogin(notificationId, remind);
    }

    /**
     * Get unread notification count
     */
    public int getUnreadCount(String userId) {
        return crudNotification.getUnreadCount(userId);
    }
}
