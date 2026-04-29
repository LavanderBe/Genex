package Genex.entities;

import java.time.LocalDateTime;


public class User {
    private String id;
    private String username;
    private String email;
    private String password_hash;
    private String role;
    private LocalDateTime created_at;

    public enum Role {
        ADMIN,MODERATOR,PLAYER
    }

    public User(String username, String email, String password_hashed, String role) {
        this.username = username;
        this.email = email;
        this.password_hash = password_hashed;
        this.role = role;
        this.created_at = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword_hash() {
        return password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password_hash='" + password_hash + '\'' +
                ", role='" + role + '\'' +
                ", created_at=" + created_at +
                '}';
    }
}