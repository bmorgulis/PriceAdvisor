package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.Inventory;
import com.example.priceadvisor.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Autowired
    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public Integer getInventoryIdByBusinessId(Integer businessId) {
        return inventoryRepository.findByBusinessId(businessId)
                .map(Inventory::getInventoryId)
                .orElseThrow(() -> new IllegalArgumentException("Inventory not found for businessId: " + businessId));
    }

    public Integer findBusinessIdByInventoryId(Integer inventoryId) {
        return inventoryRepository.findBusinessIdByInventoryId(inventoryId);
    }
}
