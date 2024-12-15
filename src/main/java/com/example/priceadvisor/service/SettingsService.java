package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    private final UserService userService;
    private final EmailNotificationService emailNotificationService;

    @Autowired
    public SettingsService(UserService userService, EmailNotificationService emailNotificationService) {
        this.userService = userService;
        this.emailNotificationService = emailNotificationService;
    }

    public void saveEmailNotificationFrequency(User.EmailNotificationsFrequency chosenEmailNotificationsFrequency) {
        Integer userId = userService.getCurrentUserId();
        String userEmail = userService.getCurrentEmail();
        Integer businessId = userService.getCurrentBusinessId();

        // Set the email notifications frequency to NONE if it is null
        if (chosenEmailNotificationsFrequency == null) {
            chosenEmailNotificationsFrequency = User.EmailNotificationsFrequency.NONE;
        }

        User.EmailNotificationsFrequency currentFrequency = userService.getCurrentEmailNotificationsFrequency(userId);

        if (currentFrequency != User.EmailNotificationsFrequency.NONE && currentFrequency != chosenEmailNotificationsFrequency) {
            emailNotificationService.unsubscribeUserFromTopic(userEmail, currentFrequency, businessId);
        }

        if (chosenEmailNotificationsFrequency != User.EmailNotificationsFrequency.NONE) {
            emailNotificationService.subscribeUserToTopic(userEmail, chosenEmailNotificationsFrequency, businessId);
            userService.setCurrentEmailNotificationsFrequency(userId, chosenEmailNotificationsFrequency);
        }
    }
}