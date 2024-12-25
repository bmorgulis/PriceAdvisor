package com.example.priceadvisor.repository;

import com.example.priceadvisor.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByBusinessId(Integer businessId);

    @Query("SELECT i.businessId FROM Inventory i WHERE i.inventoryId = :inventoryId")
    Integer findBusinessIdByInventoryId(Integer inventoryId);
}
