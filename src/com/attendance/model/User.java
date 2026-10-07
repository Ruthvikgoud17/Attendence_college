package com.attendance.model;

/**
 * Model representing an Admin or Teacher user for authentication.
 */
public class User {
    private String username;
    private String password;
    private String role; // "ADMIN" or "TEACHER"
    private String fullName;

    public User(String username, String password, String role, String fullName) {
        this.username = username;
        this.password = password;
        this.role = role.toUpperCase();
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
}
