package com.example.priceadvisor.repository;

import com.example.priceadvisor.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface BusinessRepository extends JpaRepository<Business, Integer> {
    Optional<Business> findById(Integer name);
}
