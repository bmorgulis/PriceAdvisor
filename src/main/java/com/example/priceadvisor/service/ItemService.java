package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.repository.InventoryRepository;
import com.example.priceadvisor.repository.ItemRepository;
import com.example.priceadvisor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public void addItem(String name, Long UPC, Long SKU, String description, String additionalInfo, BigDecimal price, int inventoryId) {
//        System.out.println("Received item: " + name + ", " + UPC + ", " + SKU + ", " + description + ", " + additionalInfo + ", " + price + ", " + inventoryId);
        Item newItem = new Item(name, UPC, SKU, description, additionalInfo, price, inventoryId, false);
        itemRepository.save(newItem);
    }

    public boolean itemExists(Long UPC, Long SKU) {
        return itemRepository.existsByUPCOrSKU(UPC, SKU);
    }
}
