package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.User;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    public void subscribe(String userEmail, User.EmailNotificationsFrequency emailNotificationsFrequency) {
    }

    public void unsubscribe(String userEmail) {
    }
}
