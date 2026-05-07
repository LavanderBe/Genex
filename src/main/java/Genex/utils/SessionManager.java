package Genex.utils;

import Genex.entities.User;
import Genex.entities.Player;

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
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Player getCurrentPlayer() {
        if (currentUser instanceof Player) {
            return (Player) currentUser;
        }
        return null;
    }

    public String getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        this.currentUser = null;
    }
}
