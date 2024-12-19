package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public List<Item> getItemsByInventoryId(Integer inventoryId) {
        return itemRepository.findByInventoryIdOrderByNameAsc(inventoryId);
    }

    public void addItem(String name, Long UPC, Long SKU, String description, BigDecimal price, int inventoryId) {
        Item newItem = new Item(name, UPC, SKU, description, price, inventoryId);
        itemRepository.save(newItem);
    }

    public void saveItems(List<Item> items) {
        itemRepository.saveAll(items);  // Save the updated items
    }

    public boolean itemExists(Long UPC, Long SKU, Integer inventoryId) {
        return itemRepository.existsByUPCOrSKUAndInventoryId(UPC, SKU, inventoryId);
    }
}
