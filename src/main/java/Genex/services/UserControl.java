package Genex.services;

public class UserControl {
    public enum Role {
        ADMIN,MODERATOR,PLAYER
    }

    private UserControl() {}

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isValidUsername(String username) {
        return username != null && !username.isBlank() && username.length() >= 3;
    }

    public static boolean isValidRole(String r) {
        try {
            Role.valueOf(r.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
