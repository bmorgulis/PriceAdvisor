package com.example.priceadvisor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String email;
    private String password;
    private String role;
    private String businessId;
    private String emailNotificationsFrequency;

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

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getEmailNotificationsFrequency() {
        return emailNotificationsFrequency;
    }

    public void setEmailNotificationsFrequency(String emailNotificationsFrequency) {
        this.emailNotificationsFrequency = emailNotificationsFrequency;
    }
}
