package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.repository.ItemRepository;
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

    public void addItem(String name, Long UPC, Long SKU, String description, BigDecimal price, String additionalInfo, int businessId) {
        //TODO Should we include businessId? and take out starred and inventoryId
        Item newItem = new Item(name, UPC, SKU, description, additionalInfo, price, starred, inventoryId);
        itemRepository.save(newItem); //save is part of the JpaRepository interface which ItemRepository extends
    }
}
