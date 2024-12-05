package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecurityContextService securityContextService; // Injected service

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, SecurityContextService securityContextService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityContextService = securityContextService;
    }

    // Add a new user with hashed password
    public void addUser(String email, String password, User.Role role, int businessId) {
        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(email, hashedPassword, role, businessId); // Include businessId
        userRepository.save(newUser);
    }

    // Set the email notifications frequency for a user
    public void setEmailNotificationsFrequency(int userId, User.EmailNotificationsFrequency emailFrequency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEmailNotificationsFrequency(emailFrequency);
        userRepository.save(user);
    }

    // Get email notifications frequency for a user
    public User.EmailNotificationsFrequency getEmailNotificationsFrequency(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getEmailNotificationsFrequency();
    }

    // Get the current user's ID
    public int getCurrentUserId() {
        return securityContextService.getCurrentUserId();  // Use injected service to get user ID
    }

    // Get the current user's username
    public String getCurrentUsername() {
        return securityContextService.getCurrentUsername();  // Use injected service to get username
    }
}
