package com.example.priceadvisor.service;

import com.example.priceadvisor.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityContextService {

    // Helper method to extract CustomUserDetails from SecurityContext
    private CustomUserDetails getCurrentUserDetails() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // Get the current logged-in user's ID
    public Integer getCurrentUserId() {
        return getCurrentUserDetails().getUserId();
    }

    public String getCurrentEmail() {
        return getCurrentUserDetails().getUsername();
    }

    // Get the current logged-in user's Business ID
    public Integer getCurrentBusinessId() {
        return getCurrentUserDetails().getBusinessId();
    }
}
