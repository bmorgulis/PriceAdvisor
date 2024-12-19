package com.example.priceadvisor.service;

import org.springframework.stereotype.Service;

import com.example.priceadvisor.repository.InventoryRepository;
import com.example.priceadvisor.entity.Inventory;

import java.util.Optional;


@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Integer getInventoryIdByBusinessId(Integer businessId) {
        return inventoryRepository.findByBusinessId(businessId)
                .map(Inventory::getInventoryId)
                .orElseThrow(() -> new IllegalArgumentException("No inventory found for businessId: " + businessId));
    }
}
