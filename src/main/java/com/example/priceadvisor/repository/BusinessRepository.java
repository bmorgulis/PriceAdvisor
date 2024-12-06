package com.example.priceadvisor.repository;

import com.example.priceadvisor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends JpaRepository<User, Integer> {
}
