package com.example.priceadvisor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Long userId;

    @Column(name = "email", length = 255, nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Column(name = "role", length = 255)
    private String role;

    @Column(name = "businessID", nullable = false)
    private int businessId;

    @Enumerated(EnumType.STRING)
    @Column(name = "emailNotificationFrequency")
    private EmailNotificationFrequency emailNotificationFrequency;

    // Default constructor
    public User() {}

    // Constructor with parameters for login functionality
    public User(String email, String password) {
        this.email = email;
        this.password = password;
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

    public EmailNotificationFrequency getEmailNotificationFrequency() {
        return emailNotificationFrequency;
    }

    public void setEmailNotificationFrequency(EmailNotificationFrequency emailNotificationFrequency) {
        this.emailNotificationFrequency = emailNotificationFrequency;
    }

    // Enum for email notification frequency
    public enum EmailNotificationFrequency {
        HOURLY,
        DAILY,
        WEEKLY,
        MONTHLY
    }
}
