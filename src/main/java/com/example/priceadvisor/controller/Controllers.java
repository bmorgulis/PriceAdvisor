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

import java.util.List;
import java.util.logging.Logger;

@Controller
public class Controllers {

    private final UserService userService;
    private final EmailNotificationService emailNotificationService;
    Logger logger = Logger.getLogger(Controllers.class.getName());

    @Autowired
    public Controllers(UserService userService, EmailNotificationService emailNotificationService) {
        this.userService = userService;
        this.emailNotificationService = emailNotificationService;
    }

    @GetMapping("/sign-in")
    public String signIn() {
        return "sign-in";
    }

    @GetMapping("/manage-users")
    public String manageUsers(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);  // Add users to the model
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            e.printStackTrace();
        }
        return "manage-users";
    }

    @GetMapping("/terms-of-use")
    public String termsOfUse() {
        return "terms-of-use";
    }

    @GetMapping("/settings")
    public String settings(Model model, RedirectAttributes redirectAttributes) {
        try {
            Integer userId = userService.getCurrentUserId();  // Use the service method to get the user ID
            User.EmailNotificationsFrequency emailNotificationsFrequency = userService.getCurrentEmailNotificationsFrequency(userId);
            model.addAttribute("emailNotificationsFrequency", emailNotificationsFrequency);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
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

            redirectAttributes.addFlashAttribute("successMessage", "User added");
            return "redirect:/manage-users";
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            String errorMessage;

            if (message.contains("duplicate")) {
                errorMessage = "The email address you entered is already associated with an user.";
            } else {
                errorMessage = "An unexpected error occurred. Please try again.";
            }

            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/manage-users";
        }
    }

    @PostMapping("/delete-users")
    public String deleteUsers(@RequestParam List<Integer> userIds, RedirectAttributes redirectAttributes) {
        try {
            // Delete the users using the UserService
            userService.deleteUsersById(userIds);

            redirectAttributes.addFlashAttribute("successMessage", "User(s) Deleted");
            return "redirect:/manage-users";  // Redirect back to manage users page
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/manage-users";  // Redirect back in case of error
        }
    }

    @PostMapping("/save-settings")
    public String saveSettings(@RequestParam(name = "emailNotificationsFrequency", required = false) User.EmailNotificationsFrequency chosenEmailNotificationsFrequency, RedirectAttributes redirectAttributes) {
        try {
            Integer userId = userService.getCurrentUserId();
            String userEmail = userService.getCurrentEmail();

            // Set the email notifications frequency to NONE if it is null
            if (chosenEmailNotificationsFrequency == null)
                chosenEmailNotificationsFrequency = User.EmailNotificationsFrequency.NONE;

            // Get the email notifications frequency currently in the database
            User.EmailNotificationsFrequency currentFrequency = userService.getCurrentEmailNotificationsFrequency(userId);

            if (currentFrequency != User.EmailNotificationsFrequency.NONE && currentFrequency != chosenEmailNotificationsFrequency) {
                emailNotificationService.unsubscribe(userEmail, currentFrequency, userService.getCurrentBusinessId());
            }

            if (chosenEmailNotificationsFrequency != User.EmailNotificationsFrequency.NONE) {
                emailNotificationService.subscribe(userEmail, chosenEmailNotificationsFrequency, userService.getCurrentBusinessId());
                userService.setCurrentEmailNotificationsFrequency(userId, chosenEmailNotificationsFrequency);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Changes Saved"); // Add success message
            return "redirect:/settings";  // Redirect back to settings page
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/settings";
        }
    }

    @GetMapping("/add-items")
    public String addItems() {
        return "add-items";
    }

    @GetMapping("/compare-prices")
    public String comparePrices() {
        // DataFetchingService dataFetchingService = new DataFetchingService();
        // dataFetchingService.fetchData();
        return "compare-prices";
    }

    @GetMapping("/watchlist")
    public String watchlist() {
        return "watchlist";
    }
}
