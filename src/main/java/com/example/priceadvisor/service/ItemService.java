package com.example.priceadvisor.service;

import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.repository.ItemRepository;
import jakarta.transaction.Transactional;
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

    public void saveItems(List<Item> items) {
        itemRepository.saveAll(items);  // Save the updated items
    }

    @Transactional
    public String addItem(String name, String upcAsString, String sku, String description, String priceAsString, Integer inventoryId) {
        Long upc = upcAsString == null || upcAsString.isEmpty() ? null : Long.valueOf(upcAsString);
        if (sku.isEmpty()) sku = null;
        BigDecimal price = priceAsString == null || priceAsString.isEmpty() ? null : new BigDecimal(priceAsString.replace(",", ""));
        if (description.isEmpty()) description = null;

        Item newItem = new Item(name, upc, sku, description, price, inventoryId);

        List<Item> existingItems = itemRepository.findByInventoryId(inventoryId);

        for (Item existingItem : existingItems) {

            boolean allMatchExceptPrice =
                    (name == null || name.equalsIgnoreCase(existingItem.getName())) &&
                            (upc == null || upc.equals(existingItem.getUpc())) &&
                            (sku == null || sku.equals(existingItem.getSku())) &&
                            (description == null || description.equalsIgnoreCase(existingItem.getDescription()));

            if (allMatchExceptPrice)
                return newItem.itemDetailsToString() + "\nalready exists";

            boolean upcOrSkuConflict =
                    ((upc != null && upc.equals(existingItem.getUpc())) ||
                            (sku != null && sku.equals(existingItem.getSku()))) &&
                            (!(name == null || name.equals(existingItem.getName())) ||
                                    !(description == null || description.equals(existingItem.getDescription())) ||
                                    !(price == null || price.equals(existingItem.getSmallBusinessPrice())));

            if (upcOrSkuConflict)
                return "Cannot add " + newItem.itemDetailsToString() + " because there is a conflict with " + existingItem.itemDetailsToString();
        }

        itemRepository.save(newItem);
        return null;
    }
}

