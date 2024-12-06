package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.Business;
import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.repository.BusinessRepository;
import com.example.priceadvisor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Value("${aws.sns.base.arn}") // Inject the base ARN from the application.properties file.
    private String baseArn;

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

    public String setCurrentEmailNotificationsFrequency(Integer userId, User.EmailNotificationsFrequency emailFrequency, BusinessRepository businessRepository) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEmailNotificationsFrequency(emailFrequency);
        userRepository.save(user);

        int businessId = user.getBusinessId();

        String businessName = BusinessRepository.findById(businessId)
                .orElseThrow(() -> new IllegalArgumentException("Business not found"))
                .getName();

        String frequency = user.getEmailNotificationsFrequency().name();
        return String.format("%s%s_%s", baseArn, businessName, frequency);
    }

    public Integer getCurrentBusinessId() {
        return securityContextService.getCurrentBusinessId();
    }
}
