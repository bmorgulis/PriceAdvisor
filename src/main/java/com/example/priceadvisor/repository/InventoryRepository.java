package com.example.priceadvisor.repository;

import com.example.priceadvisor.entity.Inventory;
import com.example.priceadvisor.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Item, Integer> {
    // Find all items by businessId
    Inventory findByBusinessId(Integer businessId);
}
