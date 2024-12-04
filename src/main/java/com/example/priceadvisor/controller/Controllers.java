package com.example.priceadvisor.controller;

import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.security.CustomUserDetails;
import com.example.priceadvisor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
            int userId = getCurrentUserId();
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
            // Retrieve the logged-in manager's details directly from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetails managerDetails = (CustomUserDetails) authentication.getPrincipal();
            int managerBusinessId = managerDetails.getBusinessId(); // Retrieve `businessId` as an int

            // Add the new user with hashed password
            userService.addUser(email, password, role, managerBusinessId);

            // Add success flag to trigger the modal and persist it after the redirect
            redirectAttributes.addFlashAttribute("userAddSuccess", true);
            return "redirect:/manage-accounts"; // Redirect to the same page
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            String errorMessage;

            if (message.contains("duplicate")) {
                errorMessage = "The email address you entered is already associated with an account.";
            } else {
                errorMessage = "An unexpected error occurred. Please try again.";
            }

            // Add the error message to the redirect attributes to persist it after redirect
            redirectAttributes.addFlashAttribute("userAddErrorMessage", errorMessage);

            return "redirect:/manage-accounts"; // Redirect to the same page
        }
    }

    // Set the email notifications frequency for the current user
    @PostMapping("/set-email-notifications-frequency")
    public String setEmailNotificationsFrequency(@RequestParam(name = "emailNotificationsFrequency", required = false) User.EmailNotificationsFrequency emailNotificationsFrequency, RedirectAttributes redirectAttributes) {
        try {
            int userId = getCurrentUserId();

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

    private int getCurrentUserId() {
        return ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUserId();
    }
}
