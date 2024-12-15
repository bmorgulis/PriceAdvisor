package com.example.priceadvisor.controller;

import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.service.EmailNotificationService;
import com.example.priceadvisor.service.InventoryService;
import com.example.priceadvisor.service.ItemService;
import com.example.priceadvisor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class Controllers {

    private final UserService userService;
    private final EmailNotificationService emailNotificationService;
    private final ItemService itemService;
    Logger logger = Logger.getLogger(Controllers.class.getName());
    private final InventoryService inventoryService;


    @Autowired
    public Controllers(UserService userService, EmailNotificationService emailNotificationService, ItemService itemService, InventoryService inventoryService) {
        this.userService = userService;
        this.emailNotificationService = emailNotificationService;
        this.itemService = itemService;
        this.inventoryService = inventoryService;
    }

    @GetMapping("/sign-in")
    public String signIn() {
        return "sign-in";
    }

    @GetMapping("/manage-accounts")
    public String manageAccounts(Model model, RedirectAttributes redirectAttributes) {
        try {
            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);  // Add users to the model
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            e.printStackTrace();
        }
        return "manage-accounts";
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
            return "redirect:/manage-accounts";
        } catch (Exception e) {
            String message = e.getMessage().toLowerCase();
            String errorMessage;

            if (message.contains("duplicate")) {
                errorMessage = "The email address you entered is already associated with an account.";
            } else {
                errorMessage = "An unexpected error occurred. Please try again.";
            }

            redirectAttributes.addFlashAttribute("errorMessage", errorMessage);
            return "redirect:/manage-accounts";
        }
    }

    @PostMapping("/delete-users")
    public String deleteUsers(@RequestParam List<String> emails, RedirectAttributes redirectAttributes) {
        try {
            // Delete the users using the UserService
            userService.deleteUsersByEmails(emails);

            redirectAttributes.addFlashAttribute("successMessage", "Account(s) Deleted");
            return "redirect:/manage-accounts";  // Redirect back to manage accounts page
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/manage-accounts";  // Redirect back in case of error
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

    //TODO still need to have some validation to make sure that the item is not already in the database

    @PostMapping("/add-item")
    public String addItem(@RequestParam String name,
                          @RequestParam Long UPC,
                          @RequestParam Long SKU,
                          @RequestParam String description,
                          @RequestParam BigDecimal price,
                          @RequestParam(required = false) String additionalInfo,
                          RedirectAttributes redirectAttributes) {
        try {
            Integer businessId = userService.getCurrentBusinessId();
            Integer inventoryId = inventoryService.getInventoryIdByBusinessId(businessId);

            // Check if the item already exists in the inventory
            if (itemService.itemExists(UPC, SKU)) {
                redirectAttributes.addFlashAttribute("errorMessage", "The item already exists in the inventory.");
                return "redirect:/add-items";
            }

            // Make item through the user service add items
            itemService.addItem(name, UPC, SKU, description, additionalInfo, price, inventoryId);

            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", "Item added");
            return "redirect:/add-items";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/add-items";
        }
    }
}
