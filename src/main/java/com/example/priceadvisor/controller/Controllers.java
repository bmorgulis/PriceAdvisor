package com.example.priceadvisor.controller;

import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.service.EmailNotificationService;
import com.example.priceadvisor.service.SettingsService;
import com.example.priceadvisor.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final SettingsService settingsService;
    Logger logger = Logger.getLogger(Controllers.class.getName());

    @Autowired
    public Controllers(UserService userService, SettingsService settingsService, EmailNotificationService emailNotificationService) {
        this.userService = userService;
        this.settingsService = settingsService;
    }

    @GetMapping("/sign-in")
    public String signIn() {
        return "sign-in";
    }

    @GetMapping("/manage-users")
    public String manageUsers(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);
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
            Integer userId = userService.getCurrentUserId();
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
            Integer managerBusinessId = userService.getCurrentBusinessId();
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
            userService.deleteUsersById(userIds);

            redirectAttributes.addFlashAttribute("successMessage", "User(s) Deleted");
            return "redirect:/manage-users";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/manage-users";
        }
    }

    @PostMapping("/edit-users")
    public String saveUserChanges(@RequestParam("editedUsers") String editedUsersJson, RedirectAttributes redirectAttributes) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<User> editedUsers = objectMapper.readValue(editedUsersJson, objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));

            userService.saveChangedUsers(editedUsers);

            redirectAttributes.addFlashAttribute("successMessage", "Changes Saved");

            return "redirect:/manage-users";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/manage-users";
        }
    }

    @PostMapping("/save-settings")
    public String saveSettings(@RequestParam(name = "emailNotificationsFrequency", required = false) User.EmailNotificationsFrequency chosenEmailNotificationsFrequency, RedirectAttributes redirectAttributes) {
        try {
            settingsService.saveEmailNotificationFrequency(chosenEmailNotificationsFrequency);

            redirectAttributes.addFlashAttribute("successMessage", "Changes Saved");
            return "redirect:/settings";
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
