package com.example.priceadvisor.service;

import com.example.priceadvisor.datafetching.DataFetchingManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DataFetchingService {

    private final DataFetchingManager dataFetchingManager;

    @Autowired
    public DataFetchingService(DataFetchingManager dataFetchingManager) {
        this.dataFetchingManager = dataFetchingManager;
    }

    // Fetch data for all businesses
    public void fetchData() {
        dataFetchingManager.fetchAllData();
    }

    // Schedule the fetch to run every hour
    @Scheduled(fixedRate = 3600000)
    public void fetchDataEveryHour() {
        try
        {
            fetchData();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

