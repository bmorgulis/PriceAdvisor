package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.repository.BusinessRepository;
import com.example.priceadvisor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecurityContextService securityContextService; // Injected service
    public final BusinessRepository BusinessRepository;


    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, SecurityContextService securityContextService, BusinessRepository BusinessRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityContextService = securityContextService;
        this.BusinessRepository = BusinessRepository;
    }

    // Add a new user with hashed password
    public void addUser(String email, String password, User.Role role, Integer businessId) {
        String hashedPassword = passwordEncoder.encode(password);
        User newUser = new User(email, hashedPassword, role, businessId); // Include businessId
        userRepository.save(newUser);
    }

    public Integer getCurrentUserId() {
        return securityContextService.getCurrentUserId();  // Use injected service to get user ID
    }

    public String getCurrentEmail() {
        return securityContextService.getCurrentEmail();  // Use injected service to get user ID
    }

    public User.EmailNotificationsFrequency getCurrentEmailNotificationsFrequency(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return user.getEmailNotificationsFrequency();
    }

    public void setCurrentEmailNotificationsFrequency(Integer userId, User.EmailNotificationsFrequency emailFrequency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEmailNotificationsFrequency(emailFrequency);
        userRepository.save(user);
    }

    public Integer getCurrentBusinessId() {
        return securityContextService.getCurrentBusinessId();
    }

    public List<User> getAllUsers() {
        return userRepository.findAllOrderByEmail();
    }

    public void deleteUsersById(List<Integer> userIds) {
        List<User> usersToDelete = userRepository.findByUserIdIn(userIds);
        userRepository.deleteAll(usersToDelete);
    }

    public void saveChangedUsers(List<User> changedUsers) {
        for (User changedUser : changedUsers) {

            User existingUser = userRepository.findById(changedUser.getUserId()).orElse(null);

            if (existingUser == null) {
                throw new IllegalArgumentException("No user found with ID: " + changedUser.getUserId());
            }

            if (changedUser.getEmail() != null) {
                existingUser.setEmail(changedUser.getEmail());
            }

            if (changedUser.getRole() != null) {
                existingUser.setRole(changedUser.getRole());
            }

            if (changedUser.getPassword() != null) {
                existingUser.setPassword(passwordEncoder.encode(changedUser.getPassword()));  // Ensure this is hashed
            }

            userRepository.save(existingUser);
        }
    }

}
}

