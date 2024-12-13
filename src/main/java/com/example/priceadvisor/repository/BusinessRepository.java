package com.example.priceadvisor.repository;

import com.example.priceadvisor.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BusinessRepository extends JpaRepository<Business, Integer> {
}
