package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.Business;
import com.example.priceadvisor.repository.BusinessRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    public String getBusinessName(int businessId) {
        // Retrieve the business by ID from the repository
        Optional<Business> businessOptional = businessRepository.findById(businessId);

        // Return the business name if found, else return a default message
        return businessOptional.map(Business::getName).orElse("Unknown Business");
    }
}
