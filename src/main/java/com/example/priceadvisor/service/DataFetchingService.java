package com.example.priceadvisor.service;

import com.example.priceadvisor.datafetching.DataFetchingManager;
import com.example.priceadvisor.entity.Inventory;
import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.repository.InventoryRepository;
import com.example.priceadvisor.repository.ItemRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataFetchingService {
    private final DataFetchingManager dataFetchingManager = new DataFetchingManager();
    private final ItemRepository itemRepository;
    private final Integer inventoryId;

    @Autowired
    public DataFetchingService(SecurityContextService securityContextService, InventoryRepository inventoryRepository, ItemRepository itemRepository) {
        Integer businessId = securityContextService.getCurrentBusinessId();
        Inventory inventory = inventoryRepository.findByBusinessId(businessId);
        inventoryId = inventory.getInventoryId();
        this.itemRepository = itemRepository;
    }

    // Method to start data fetching immediately after the application starts
    @PostConstruct
    public void init() {
        fetchData(); // Trigger data fetching immediately on startup
    }

    // Method to fetch data
    public List<Item> fetchData() {
        List<Item> items = itemRepository.findByInventoryIdOrderByNameAsc(inventoryId);
        List<Item> itemsWithFetchedData = dataFetchingManager.getFetchedData(items);
        return itemsWithFetchedData;
    }

    // Method to fetch data every hour
    @Scheduled(fixedRate = 3600000) // 3600000 ms = 1 hour
    public void fetchDataEveryHour() {
        fetchData(); // Repeat data fetching every hour
    }
}
