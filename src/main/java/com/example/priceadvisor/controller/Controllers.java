package com.example.priceadvisor.controller;

import com.example.priceadvisor.entity.User;
import com.example.priceadvisor.security.CustomUserDetails;
import com.example.priceadvisor.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class Controllers {

    private static final Logger logger = LoggerFactory.getLogger(Controllers.class); // Logger setup

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder; // Password encoder

    @Autowired
    public Controllers(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
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

    @PostMapping("/add-user")
    public String addUser(@RequestParam String email,
                          @RequestParam String password,
                          @RequestParam User.Role role,
                          RedirectAttributes redirectAttributes) {  // Use RedirectAttributes here
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
            logger.error("Error adding user: {}", message); // Log the error
            String errorMessage;

            if (message.contains("duplicate")) {
                errorMessage = "The email address you entered is already associated with an account.";
            } else {
                errorMessage = "An unexpected error occurred. Please try again.";
            }
            logger.error("Error message: {}", errorMessage); // Log the specific error message

            // Add the error message to the redirect attributes to persist it after redirect
            redirectAttributes.addFlashAttribute("userAddErrorMessage", errorMessage);

            return "redirect:/manage-accounts"; // Redirect to the same page
        }
    }
}
