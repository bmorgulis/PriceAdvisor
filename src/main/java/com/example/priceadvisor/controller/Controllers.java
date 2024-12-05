package com.example.priceadvisor.controller;

import com.example.priceadvisor.entity.User;
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

    @Autowired
    public Controllers(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/terms-of-use")
    public String termsOfUse() {
        return "terms-of-use";
    }

    @GetMapping("/manage-accounts")
    public String manageAccounts() {
        return "manage-accounts";
    }

    @GetMapping("/sign-in")
    public String signIn() {
        return "sign-in";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        try {
            int userId = userService.getCurrentUserId();  // Use the service method to get the user ID
            User.EmailNotificationsFrequency emailNotificationsFrequency = userService.getEmailNotificationsFrequency(userId);
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
            int managerBusinessId = userService.getCurrentUserId(); // Get the manager's business ID using the service
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
            int userId = userService.getCurrentUserId();

            if (emailNotificationsFrequency == null)
                emailNotificationsFrequency = User.EmailNotificationsFrequency.NONE;

            userService.setEmailNotificationsFrequency(userId, emailNotificationsFrequency);

            redirectAttributes.addFlashAttribute("saveSettingsSuccess", true);
            return "redirect:/settings";  // Redirect back to settings page
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("saveSettingsErrorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/settings";
        }
    }
}
