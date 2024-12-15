package com.example.priceadvisor.repository;
import com.example.priceadvisor.entity.Inventory;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByBusinessId(Integer businessId);// this is a spring method that gets the inventory by business id using Jpa
}
