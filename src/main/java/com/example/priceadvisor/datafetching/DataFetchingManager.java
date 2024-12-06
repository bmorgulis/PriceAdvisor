package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
public class DataFetchingManager {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(500);
    private static final List<DataFetcher> FETCHERS = List.of(
            new AmazonApiDataFetcher(),
            new WalmartScrapingFetcher(),
            new EbayScrapingFetcher()
    );
    private final List<Item> items;
    private final Map<Item, FetchedData> fetchedDataMap = new ConcurrentHashMap<>();
    private CountDownLatch latch;

    public DataFetchingManager(List<Item> items) {
        this.items = items;
        initializeFetchedDataMap();
        fetchAllData();
    }

    private void initializeFetchedDataMap() {
        for (Item item : items) {
            fetchedDataMap.put(item, new FetchedData());
        }
    }

    public void fetchAllData() {
        // Initialize latch dynamically based on the number of tasks
        latch = new CountDownLatch(items.size() * FETCHERS.size());  // Total tasks = items × fetchers

        for (Item item : items) {
            for (DataFetcher fetcher : FETCHERS) {
                executorService.submit(() -> {
                    try {
                        fetcher.fetchAndSaveData(item, fetchedDataMap.get(item));
                    } catch (Exception e) {
                        System.err.println("Error fetching data for item: " + item + ". " + e.getMessage());
                    } finally {
                        latch.countDown(); // Decrement the latch count when a task completes
                    }
                });
            }
        }
    }

    private void waitForFetchingToComplete() {
        if (latch == null) {
            throw new IllegalStateException("fetchAllData() must be called before getFetchedData().");
        }

        try {
            latch.await(); // Wait until all tasks are completed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new IllegalStateException("Data fetching was interrupted.", e);
        }
    }

    public List<Map.Entry<Item, FetchedData>> getFetchedData() {

        waitForFetchingToComplete();
        // Convert the map entries to a list and sort them by the Item's name
        List<Map.Entry<Item, FetchedData>> sortedEntries = new ArrayList<>(fetchedDataMap.entrySet());

        // Sort the entries based on the Item's name
        sortedEntries.sort(Comparator.comparing(entry -> entry.getKey().getName()));

        return sortedEntries;
    }
}
