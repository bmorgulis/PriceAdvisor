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
    private final DataFetchingService dataFetchingService;

    @Autowired
    public ItemService(ItemRepository itemRepository, DataFetchingService dataFetchingService) {
        this.itemRepository = itemRepository;
        this.dataFetchingService = dataFetchingService;
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    public List<Item> findItemsByInventoryId(Integer inventoryId) {
        return itemRepository.findByInventoryIdOrderByNameAsc(inventoryId);
    }

    public void saveItems(List<Item> items) {
        itemRepository.saveAll(items);  // Save the updated items
    }

    @Transactional
    public String addItem(String name, String upcAsString, String sku, String description, String priceAsString, Integer inventoryId) {
        Long upc = parseLongOrNull(upcAsString);
        if (sku.isEmpty()) sku = null;
        BigDecimal price = parseBigDecimalOrNull(priceAsString);
        if (description.isEmpty()) description = null;

        Item newItem = new Item(name, upc, sku, description, price, inventoryId);

        List<Item> existingItems = itemRepository.findByInventoryId(inventoryId);

        for (Item existingItem : existingItems) {
            // Check if item with same details already exists (except price)
            if (isMatchingItem(existingItem, newItem)) {
                return itemDetailsToString(newItem) + "\nalready exists.";
            }

            // Check for conflict based on UPC or SKU (with price, name, or description differences)
            if (isConflictWithExistingItem(existingItem, newItem)) {
                return "Cannot add " + itemDetailsToString(newItem) + " because there is a conflict with " + itemDetailsToString(existingItem) + ".";
            }
        }

        itemRepository.save(newItem);
        dataFetchingService.fetchItemDataImmediately(newItem);

        return null;
    }

    // Helper methods for better readability and reusability
    private Long parseLongOrNull(String value) {
        return (value == null || value.isEmpty()) ? null : Long.valueOf(value);
    }

    private BigDecimal parseBigDecimalOrNull(String value) {
        return (value == null || value.isEmpty()) ? null : new BigDecimal(value.replace(",", ""));
    }

    private boolean isMatchingItem(Item existingItem, Item newItem) {
        return (newItem.getName() == null || newItem.getName().equalsIgnoreCase(existingItem.getName())) &&
                (newItem.getUpc() == null || newItem.getUpc().equals(existingItem.getUpc())) &&
                (newItem.getSku() == null || newItem.getSku().equals(existingItem.getSku())) &&
                (newItem.getDescription() == null || newItem.getDescription().equalsIgnoreCase(existingItem.getDescription()));
    }

    private boolean isConflictWithExistingItem(Item existingItem, Item newItem) {
        boolean upcOrSkuConflict =
                ((newItem.getUpc() != null && newItem.getUpc().equals(existingItem.getUpc())) ||
                        (newItem.getSku() != null && newItem.getSku().equals(existingItem.getSku())));

        boolean hasDifferentDetails =
                !(newItem.getName() == null || newItem.getName().equals(existingItem.getName())) ||
                        !(newItem.getDescription() == null || newItem.getDescription().equals(existingItem.getDescription())) ||
                        !(newItem.getSmallBusinessPrice() == null || newItem.getSmallBusinessPrice().equals(existingItem.getSmallBusinessPrice()));

        return upcOrSkuConflict && hasDifferentDetails;
    }

    public String itemDetailsToString(Item item) {
        StringBuilder details = new StringBuilder("Item ");

        String name = item.getName();
        Long upc = item.getUpc();
        String sku = item.getSku();
        String description = item.getDescription();

        if (name != null) {
            details.append("\"").append(name).append("\" ");
        }
        if (upc != null || sku != null || description != null) {
            details.append("with ");
        }
        if (upc != null) {
            details.append("UPC: \"").append(upc).append("\", ");
        }
        if (sku != null) {
            details.append("SKU: \"").append(sku).append("\", ");
        }
        if (description != null) {
            details.append("Description: \"").append(description).append("\", ");
        }

        // Remove trailing comma and space if any
        if (!details.isEmpty() && details.charAt(details.length() - 2) == ',') {
            details.setLength(details.length() - 2);
        }

        return details.toString();
    }

    public void setAmazonPrice(Item item, BigDecimal amazonPrice) {
        item.setAmazonPrice(amazonPrice);
    }

    public void setWalmartPrice(Item item, BigDecimal walmartPrice) {
        item.setWalmartPrice(walmartPrice);
    }

    public void setEbayPrice(Item item, BigDecimal ebayPrice) {
        item.setEbayPrice(ebayPrice);
    }

    public void setPriceSuggestion(Item item, Item.PriceSuggestion priceSuggestion) {
        item.setPriceSuggestion(priceSuggestion);
    }

    public void saveItem(Item item) {
        itemRepository.save(item); // The built-in save method persists the item
    }
    public void deleteItemsById(List<Integer> itemIds) {
        List<Item> itemsToDelete = itemRepository.findByItemIdIn(itemIds);
        itemRepository.deleteAll(itemsToDelete);
    }

    public void saveChangedItems(List<Item> changedItems) {
        for (Item changedItem : changedItems) {

            Item existingItem = itemRepository.findById(changedItem.getItemId()).orElse(null);

            if (existingItem == null) {
                throw new IllegalArgumentException("No user found with ID: " + changedItem.getItemId());
            }

            if (changedItem.getName() != null) {
                existingItem.setName(changedItem.getName());
            }

            existingItem.setUpc(changedItem.getUpc());

            if (changedItem.getSku() != null) {
                existingItem.setSku(changedItem.getSku());
            }
            if (changedItem.getName() != null) {
                existingItem.setName(changedItem.getName());
            }

            if (changedItem.getDescription() != null) {
                existingItem.setDescription(changedItem.getDescription());
            }

            existingItem.setSmallBusinessPrice(changedItem.getSmallBusinessPrice());

            itemRepository.save(existingItem);
        }
    }
}

