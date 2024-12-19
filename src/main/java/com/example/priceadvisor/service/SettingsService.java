package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    private final SecurityContextService securityContextService;
    private final UserService userService;
    private final EmailNotificationsService emailNotificationsService;

    @Autowired
    public SettingsService(SecurityContextService securityContextService, UserService userService, EmailNotificationsService emailNotificationsService) {
        this.securityContextService = securityContextService;
        this.userService = userService;
        this.emailNotificationsService = emailNotificationsService;
    }

    public void saveEmailNotificationFrequency(User.EmailNotificationsFrequency chosenEmailNotificationsFrequency) {
        Integer userId = securityContextService.getCurrentUserId();
        String userEmail = userService.getCurrentEmail();
        Integer businessId = securityContextService.getCurrentBusinessId();

        // Set the email notifications frequency to NONE if it is null
        if (chosenEmailNotificationsFrequency == null) {
            chosenEmailNotificationsFrequency = User.EmailNotificationsFrequency.NONE;
        }

        User.EmailNotificationsFrequency currentFrequency = userService.getCurrentEmailNotificationsFrequency(userId);

        if (currentFrequency != User.EmailNotificationsFrequency.NONE && currentFrequency != chosenEmailNotificationsFrequency) {
            emailNotificationsService.unsubscribeUserFromTopic(userEmail, currentFrequency, businessId);
        }

        if (chosenEmailNotificationsFrequency != User.EmailNotificationsFrequency.NONE) {
            emailNotificationsService.subscribeUserToTopic(userEmail, chosenEmailNotificationsFrequency, businessId);
        }

        userService.setCurrentEmailNotificationsFrequency(userId, chosenEmailNotificationsFrequency);
    }
}
