package com.example.priceadvisor.repository;

import com.example.priceadvisor.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    // Find inventory by businessId
    Optional<Inventory> findByBusinessId(Integer businessId);
}
