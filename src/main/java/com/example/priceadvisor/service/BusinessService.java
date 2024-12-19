package com.example.priceadvisor.service;

import com.example.priceadvisor.repository.BusinessRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BusinessService {

    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    public List<String> getAllBusinessIds() {
        return businessRepository.findAllBusinessIds();
    }
}
