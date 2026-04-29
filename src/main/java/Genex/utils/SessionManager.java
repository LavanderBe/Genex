package Genex.utils;

import Genex.entities.User;

/**
 * Singleton class to manage the current logged-in user session
 */
public class SessionManager {

    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println("Session started for user: " + (user != null ? user.getUsername() : "null"));
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        System.out.println("Session ended for user: " + (currentUser != null ? currentUser.getUsername() : "null"));
        this.currentUser = null;
    }
}
