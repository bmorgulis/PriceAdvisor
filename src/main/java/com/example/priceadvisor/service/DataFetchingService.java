package com.example.priceadvisor.service;

import com.example.priceadvisor.datafetching.DataFetchingManager;
import com.example.priceadvisor.entity.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DataFetchingService {

    private final DataFetchingManager dataFetchingManager;

    @Autowired
    public DataFetchingService(@Lazy DataFetchingManager dataFetchingManager) {
        this.dataFetchingManager = dataFetchingManager;
    }

    // Fetch data for all businesses
    public void fetchData() {
        dataFetchingManager.fetchAllData();
    }

    // Fetch data for a single item immediately
    public void fetchItemDataImmediately(Item item) {
        dataFetchingManager.fetchItemDataImmediately(item);
    }

    // Schedule the fetch to run on startup and then every hour
    @Scheduled(fixedRate = 3600000)
    public void fetchDataEveryHour() {
        try {
            fetchData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
