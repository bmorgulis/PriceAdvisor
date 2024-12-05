package com.example.priceadvisor.controller;

import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.service.EmailNotificationService;
import com.example.priceadvisor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class Controllers {

    private final UserService userService;
    private final EmailNotificationService emailNotificationService;

    @Autowired
    public Controllers(UserService userService, EmailNotificationService emailNotificationService) {
        this.userService = userService;
        this.emailNotificationService = emailNotificationService;
    }

    @GetMapping("/sign-in")
    public String signIn() {
        return "sign-in";
    }

    @GetMapping("/manage-accounts")
    public String manageAccounts() {
        return "manage-accounts";
    }

    @GetMapping("/terms-of-use")
    public String termsOfUse() {
        return "terms-of-use";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        try {
            Integer userId = userService.getCurrentUserId();  // Use the service method to get the user ID
            User.EmailNotificationsFrequency emailNotificationsFrequency = userService.getCurrentEmailNotificationsFrequency(userId);
            model.addAttribute("emailNotificationsFrequency", emailNotificationsFrequency);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "settings";
    }

    @PostMapping("/add-user")
    public String addUser(@RequestParam String email,
                          @RequestParam String password,
                          @RequestParam User.Role role,
                          RedirectAttributes redirectAttributes) {
        try {
            // Add the new user by passing the businessId from the security context
            Integer managerBusinessId = userService.getCurrentBusinessId(); // Get the manager's business ID using the service
            userService.addUser(email, password, role, managerBusinessId);

            redirectAttributes.addFlashAttribute("userAddSuccess", true);
            return "redirect:/manage-accounts";
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            String errorMessage;

            if (message.contains("duplicate")) {
                errorMessage = "The email address you entered is already associated with an account.";
            } else {
                errorMessage = "An unexpected error occurred. Please try again.";
            }

            redirectAttributes.addFlashAttribute("userAddErrorMessage", errorMessage);
            return "redirect:/manage-accounts";
        }
    }

    @PostMapping("/set-email-notifications-frequency")
    public String setEmailNotificationsFrequency(@RequestParam(name = "emailNotificationsFrequency", required = false) User.EmailNotificationsFrequency emailNotificationsFrequency, RedirectAttributes redirectAttributes) {
        try {
            Integer userId = userService.getCurrentUserId();
            String userEmail = userService.getCurrentEmail();

            // Set the email notifications frequency to NONE if it is null
            if (emailNotificationsFrequency == null)
                emailNotificationsFrequency = User.EmailNotificationsFrequency.NONE;

            // Get the current email notifications frequency for the user
            User.EmailNotificationsFrequency currentFrequency = userService.getCurrentEmailNotificationsFrequency(userId);

            // Unsubscribe and resubscribe to the SNS topic if the frequency has changed
            if (currentFrequency != emailNotificationsFrequency) {
                emailNotificationService.unsubscribe(userEmail);
                if (currentFrequency != User.EmailNotificationsFrequency.NONE) {
                    emailNotificationService.subscribe(userEmail, emailNotificationsFrequency);
                }
                userService.setCurrentEmailNotificationsFrequency(userId, emailNotificationsFrequency);
            }

            redirectAttributes.addFlashAttribute("saveSettingsSuccess", true); // Add success message
            return "redirect:/settings";  // Redirect back to settings page
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("saveSettingsErrorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/settings";
        }
    }

    @GetMapping("/add-items")
    public String addItems() {
        return "add-items";
    }

    @GetMapping("/compare-prices")
    public String comparePrices() {
        return "compare-prices";
    }

    @GetMapping("/watchlist")
    public String watchlist() {
        return "watchlist";
    }
}
