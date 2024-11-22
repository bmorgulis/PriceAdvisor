package com.example.priceadvisor.controller;

import com.example.priceadvisor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.priceadvisor.entity.User;


@Controller
public class PageController {

    @Autowired
    BCryptPasswordEncoder hashPassword; //Using this to hash the password

    @Autowired //Allows Spring to resolve and inject beans.
    private UserRepository userRepository;  //Injects the UserRepository bean to interact with the database.

    @GetMapping("/terms-of-use")
    public String termsOfUse() {
        return "terms-of-use"; // This will resolve to /src/main/resources/templates/terms-of-use.html
    }

    //For adding a new user. Will be called when form is submitted on the manage-users add-user page.
    @PostMapping("/add-user")
    public String addUser(
            //Get values from form
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam(required = false) User.EmailNotificationsFrequency emailNotificationsFrequency,
            Model model) {

        //ToDo check that email is not already in the database. If it is, return an error message.
        try {
            if (userRepository.findByEmail(email).isPresent()) {
                model.addAttribute("error", "Email is already taken. Please try another.");
                return "manage-accounts"; // Return to the same page with an error message
            }

            if (emailNotificationsFrequency == null) {
                emailNotificationsFrequency = User.EmailNotificationsFrequency.WEEKLY; //default value since none was provided
            }


            // Create a new user object. sets the values from the form.
            User user = new User();
            user.setEmail(email);
            user.setPassword(hashPassword.encode(password));  //hash the password before saving it to the database
            user.setRole(role);
            user.setEmailNotificationsFrequency(emailNotificationsFrequency);

            // Save the user
            userRepository.save(user);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            model.addAttribute("error", "Email already exists. Try another.");
            ex.printStackTrace(); //log the exception
            return "manage-accounts"; // Return to the same page with an error message
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while adding the user. Try again.");
            e.printStackTrace(); //log the exception
            return "manage-accounts"; // Return to the same page with an error message
        }
        return "redirect:/manage-accounts";
    }

    //needed to redirect to the manage-accounts page after adding a user. (really manage-account page should be in templates directory)
    @GetMapping("/manage-accounts")
    public String manageAccounts() {
        return "redirect:/manage-accounts.html";
    }
}
