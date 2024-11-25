package com.example.priceadvisor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Long userId;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters long.")
    @Pattern(regexp = ".*\\d.*" , message = "Password must contain at least one number.") // regexp = ".*\\d.*" says that the password must contain at least one number
    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;  // Changed to Role enum

    @Column(name = "businessID", nullable = false)
    private int businessId;

    @Enumerated(EnumType.STRING)
    @Column(name = "emailNotificationsFrequency")
    private EmailNotificationsFrequency emailNotificationsFrequency;

    // Default constructor
    public User() {}

    // Constructor with parameters for login functionality
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Constructor with parameters for adding a new user to the database
    public User(String email, String password, String role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    public EmailNotificationsFrequency getEmailNotificationsFrequency() {
        return emailNotificationsFrequency;
    }

    public void setEmailNotificationsFrequency(EmailNotificationsFrequency emailNotificationsFrequency) {
        this.emailNotificationsFrequency = emailNotificationsFrequency;
    }

    // Enum for role
    public enum Role {
        ANALYST,
        MANAGER
    }

    // Enum for email notification frequency
    public enum EmailNotificationsFrequency {
        NONE,
        HOURLY,
        DAILY,
        WEEKLY,
        MONTHLY
    }
}
