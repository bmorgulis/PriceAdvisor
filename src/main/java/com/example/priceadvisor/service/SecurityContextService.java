package com.example.priceadvisor.service;

import com.example.priceadvisor.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SecurityContextService {

    // Provides access to the SecurityContext (can be used for any additional context handling if needed)
    public SecurityContext getSecurityContext() {
        return SecurityContextHolder.getContext();
    }

    // Get the current logged-in user's username
    public String getCurrentUsername() {
        UserDetails userDetails = (UserDetails) getSecurityContext().getAuthentication().getPrincipal();
        return userDetails.getUsername();
    }

    // Get the current logged-in user's ID (custom implementation)
    public int getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) getSecurityContext().getAuthentication().getPrincipal();
        return userDetails.getUserId();  // CustomUserDetails should provide the userId
    }
}
