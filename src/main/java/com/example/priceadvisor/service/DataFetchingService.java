package com.example.priceadvisor.service;

import com.example.priceadvisor.datafetching.DataFetchingManager;
import com.example.priceadvisor.datafetching.FetchedData;
import com.example.priceadvisor.entity.Item;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DataFetchingService {
    DataFetchingManager dataFetchingManager;
    List<Item> items;

    public DataFetchingService(List<Item> items) {
        dataFetchingManager = new DataFetchingManager(items);
    }

    public List<Map.Entry<Item, FetchedData>> fetchData() {
        return dataFetchingManager.getFetchedData();
    }
}
