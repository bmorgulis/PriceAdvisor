package com.example.priceadvisor.controller;

import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final SettingsService settingsService;
    private final SecurityContextService securityContextService;
    Logger logger = Logger.getLogger(Controllers.class.getName());
    private final InventoryService inventoryService;
    private final ItemService itemService;


    @Autowired
    public Controllers(UserService userService, SettingsService settingsService, InventoryService inventoryService, ItemService itemService, SecurityContextService securityContextService) {
        this.userService = userService;
        this.settingsService = settingsService;
        this.inventoryService = inventoryService;
        this.itemService = itemService;
        this.securityContextService = securityContextService;
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


    @PostMapping("/add-item")
    public String addItem(@RequestParam String name,
                          @RequestParam(required = false) Long UPC,
                          @RequestParam(required = false) Long SKU,
                          @RequestParam(required = false) String description,
                          @RequestParam(required = false) BigDecimal price,
                          RedirectAttributes redirectAttributes) {
        try {
            Integer businessId = securityContextService.getCurrentBusinessId();
            Integer inventoryId = inventoryService.getInventoryIdByBusinessId(businessId);

//            // Check if the item already exists in the inventory
//            if (itemService.itemExists(UPC, SKU, inventoryId)) {
//                redirectAttributes.addFlashAttribute("errorMessage", "The item already exists in the inventory.");
//                return "redirect:/add-items";
//            }

            // Make item through the user service add items
            itemService.addItem(name, UPC, SKU, description, price, inventoryId);

            // Add success message
            redirectAttributes.addFlashAttribute("successMessage", "Item added");
            return "redirect:/add-items";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "redirect:/add-items";
        }
    }

    @GetMapping("/compare-prices")
    public String comparePrices() {
        return "compare-prices";
    }
}
