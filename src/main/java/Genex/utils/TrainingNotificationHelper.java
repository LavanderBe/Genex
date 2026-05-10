package Genex.utils;

import Genex.entities.TrainingSession;
import Genex.services.CrudTrainingSession;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper class to show training notifications using Windows system notifications
 */
public class TrainingNotificationHelper {

    /**
     * Check and show training notifications for the current user using Windows notifications
     * Shows ALL upcoming training sessions for user's teams on every login
     */
    public static void checkAndShowNotifications() {
        try {
            System.out.println("TrainingNotificationHelper: Starting notification check...");
            
            String userId = SessionManager.getInstance().getCurrentUserId();
            System.out.println("TrainingNotificationHelper: Current user ID: " + userId);
            
            if (userId == null || userId.isEmpty()) {
                System.out.println("TrainingNotificationHelper: No user logged in, skipping notification check");
                return;
            }
            
            CrudTrainingSession crudTrainingSession = new CrudTrainingSession();
            
            // Get all team IDs the user is a member of
            System.out.println("TrainingNotificationHelper: Fetching user's teams...");
            List<String> teamIds = getTeamIdsByPlayerId(userId);
            System.out.println("TrainingNotificationHelper: User is member of " + teamIds.size() + " teams");
            
            if (teamIds.isEmpty()) {
                System.out.println("TrainingNotificationHelper: User is not a member of any team");
                return;
            }
            
            // Get all upcoming training sessions for user's teams
            List<TrainingSession> upcomingSessions = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now();
            
            for (String teamId : teamIds) {
                System.out.println("TrainingNotificationHelper: Checking sessions for team: " + teamId);
                
                List<TrainingSession> teamSessions = crudTrainingSession.getSessionsByTeam(teamId);
                
                // Filter to only upcoming sessions
                for (TrainingSession session : teamSessions) {
                    LocalDateTime sessionStart = session.getSessionDatetime().with(session.getStartTime());
                    Duration timeUntil = Duration.between(now, sessionStart);
                    
                    // Only include if session hasn't started yet (time remaining > 0)
                    if (!timeUntil.isNegative() && !timeUntil.isZero()) {
                        upcomingSessions.add(session);
                        System.out.println("TrainingNotificationHelper: Upcoming session: " + session.getTitle() + 
                                         " (starts in " + formatDuration(timeUntil) + ")");
                    }
                }
            }
            
            System.out.println("TrainingNotificationHelper: Total upcoming sessions: " + upcomingSessions.size());
            
            if (!upcomingSessions.isEmpty()) {
                System.out.println("TrainingNotificationHelper: Preparing to show " + upcomingSessions.size() + " Windows notifications");
                
                // Sort sessions by date (earliest first)
                upcomingSessions.sort((s1, s2) -> {
                    LocalDateTime dt1 = s1.getSessionDatetime().with(s1.getStartTime());
                    LocalDateTime dt2 = s2.getSessionDatetime().with(s2.getStartTime());
                    return dt1.compareTo(dt2);
                });
                
                // Show Windows notification for each upcoming training session with delay
                for (int i = 0; i < upcomingSessions.size(); i++) {
                    final int index = i;
                    final TrainingSession session = upcomingSessions.get(i);
                    
                    System.out.println("TrainingNotificationHelper: Notification " + (i+1) + " - Session: " + session.getTitle());
                    
                    // Show each notification with a delay
                    new Thread(() -> {
                        try {
                            // Wait before showing (stagger notifications)
                            if (index > 0) {
                                System.out.println("TrainingNotificationHelper: Waiting " + (index * 6) + " seconds before showing notification " + (index + 1));
                                Thread.sleep(index * 6000); // 6 seconds between each notification
                            }
                            
                            System.out.println("TrainingNotificationHelper: Showing Windows notification for: " + session.getTitle());
                            showWindowsNotification(session);
                            
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            } else {
                System.out.println("TrainingNotificationHelper: No upcoming training sessions to show");
            }
            
        } catch (Exception e) {
            System.err.println("TrainingNotificationHelper: Error checking training notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get all team IDs that a player is a member of
     */
    private static List<String> getTeamIdsByPlayerId(String playerId) {
        List<String> teamIds = new ArrayList<>();
        String query = "SELECT team_id FROM team_members WHERE player_id = ?";
        
        try {
            java.sql.PreparedStatement pst = Genex.utils.Myconnection.getInstance().getCnx().prepareStatement(query);
            pst.setString(1, playerId);
            java.sql.ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                teamIds.add(rs.getString("team_id"));
            }
            
        } catch (java.sql.SQLException e) {
            System.err.println("TrainingNotificationHelper: Error getting team IDs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return teamIds;
    }
    
    private static void showWindowsNotification(TrainingSession session) {
        try {
            System.out.println("TrainingNotificationHelper: Attempting to show Windows notification...");
            
            // Check if system tray is supported
            if (!SystemTray.isSupported()) {
                System.err.println("TrainingNotificationHelper: ERROR - System tray is NOT supported on this system!");
                System.err.println("TrainingNotificationHelper: Your Windows version or Java configuration doesn't support system tray notifications.");
                return;
            }
            
            System.out.println("TrainingNotificationHelper: System tray is supported ✓");
            
            SystemTray tray = SystemTray.getSystemTray();
            System.out.println("TrainingNotificationHelper: Got system tray instance ✓");
            
            // Create a simple 16x16 image for the tray icon (no external file needed)
            Image image = createDefaultIcon();
            System.out.println("TrainingNotificationHelper: Created default icon ✓");
            
            TrayIcon trayIcon = new TrayIcon(image, "GENEX - Training Notifications");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("GENEX - Training Notifications");
            
            // Build notification message
            String title = "🆕 Séance d'Entraînement à Venir";
            String message = buildNotificationMessage(session);
            
            System.out.println("TrainingNotificationHelper: Notification title: " + title);
            System.out.println("TrainingNotificationHelper: Notification message length: " + message.length() + " chars");
            
            // Add to tray BEFORE displaying message (required on Windows)
            tray.add(trayIcon);
            System.out.println("TrainingNotificationHelper: Added tray icon ✓");
            
            // Show notification
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            System.out.println("TrainingNotificationHelper: ✓✓✓ Windows notification DISPLAYED for: " + session.getTitle());
            
            // Remove after 10 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(10000);
                    tray.remove(trayIcon);
                    System.out.println("TrainingNotificationHelper: Removed tray icon after 10 seconds");
                } catch (Exception e) {
                    System.err.println("TrainingNotificationHelper: Error removing tray icon: " + e.getMessage());
                }
            }).start();
            
        } catch (AWTException e) {
            System.err.println("TrainingNotificationHelper: AWTException - Failed to add tray icon: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("TrainingNotificationHelper: Error showing Windows notification: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Create a simple default icon (16x16 blue square) for the system tray
     */
    private static Image createDefaultIcon() {
        int size = 16;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = image.createGraphics();
        
        // Draw a blue circle
        g.setColor(new java.awt.Color(33, 150, 243)); // Blue color
        g.fillOval(0, 0, size, size);
        
        // Draw a white "G" in the center
        g.setColor(java.awt.Color.WHITE);
        g.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
        g.drawString("G", 4, 12);
        
        g.dispose();
        return image;
    }
    
    private static String buildNotificationMessage(TrainingSession session) {
        StringBuilder message = new StringBuilder();
        
        // Title
        message.append(session.getTitle()).append("\n");
        
        // Date
        String dayOfWeek = session.getSessionDatetime().getDayOfWeek()
            .getDisplayName(TextStyle.FULL, Locale.FRENCH);
        String date = session.getSessionDatetime().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH));
        message.append("📅 ").append(dayOfWeek.substring(0, 1).toUpperCase())
               .append(dayOfWeek.substring(1)).append(", ").append(date).append("\n");
        
        // Time
        message.append("🕐 ").append(session.getStartTime())
               .append(" - ").append(session.getEndTime())
               .append(" (").append(session.getFormattedDuration()).append(")\n");
        
        // Location
        if (session.getLocation() != null && !session.getLocation().isEmpty()) {
            message.append("📍 ").append(session.getLocation()).append("\n");
        }
        
        // Countdown
        LocalDateTime sessionStart = session.getSessionDatetime().with(session.getStartTime());
        Duration timeUntil = Duration.between(LocalDateTime.now(), sessionStart);
        
        if (!timeUntil.isNegative()) {
            String countdown = formatDuration(timeUntil);
            message.append("⏰ Commence dans: ").append(countdown);
        }
        
        return message.toString();
    }
    
    private static String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        if (days > 0) {
            return days + " jour" + (days > 1 ? "s" : "") + " " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "min";
        } else {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        }
    }
}
