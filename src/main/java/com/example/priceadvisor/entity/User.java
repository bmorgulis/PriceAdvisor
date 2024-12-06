package com.example.priceadvisor.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Integer userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", length = 60, nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "emailNotificationsFrequency", nullable = false)
    private EmailNotificationsFrequency emailNotificationsFrequency;

    @Column(name = "businessId", nullable = false)
    private Integer businessId;

    // Default constructor needed for JPA
    public User() {
    }

    public User(String email, String password, Role role, Integer businessId) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.emailNotificationsFrequency = EmailNotificationsFrequency.NONE;
        this.businessId = businessId;
    }

    public Integer getUserId() {
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Integer getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Integer businessId) {
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
