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

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters long.")
    @Pattern(regexp = ".*\\d.*" , message = "Password must contain at least one number.") // regexp = ".*\\d.*" says that the password must contain at least one number
    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Column(name = "role")
    private String role;  // Changed to Role enum

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
        this.emailNotificationsFrequency = EmailNotificationsFrequency.NONE;
    }

    public Long getUserId() {
        return userId;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
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

    public enum Role {
        ANALYST,
        MANAGER
    }

    public enum EmailNotificationsFrequency {
        NONE,
        HOURLY,
        DAILY,
        WEEKLY,
        MONTHLY
    }
}
