package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
public class DataFetchingManager {

    private static final ExecutorService executorService = Executors.newFixedThreadPool(500);
    private static final List<CompetitorWebsiteDataFetcher> FETCHERS = List.of(
            new AmazonDataApiFetcher(),
            new WalmartDataScraper(),
            new EbayDataScraper()
    );

    private CountDownLatch latch;


    public List<Item> fetchAllData(List<Item> items) {
        List<Item> threadSafeItems = Collections.synchronizedList(items);
        // Initialize latch dynamically based on the number of tasks
        latch = new CountDownLatch(items.size() * FETCHERS.size());  // Total tasks = items × fetchers

        for (Item item : threadSafeItems) {
            for (CompetitorWebsiteDataFetcher fetcher : FETCHERS) {
                executorService.submit(() -> {
                    try {
                        fetcher.fetchAndSaveCompetitorData(item);
                    } catch (Exception e) {
                        System.err.println("Error fetching data for item: " + item + ". " + e.getMessage());
                    } finally {
                        latch.countDown(); // Decrement the latch count when a task completes
                    }
                });
            }
        }
        return threadSafeItems;
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

    public List<Item> getFetchedData(List<Item> items) {
        List<Item> itemsWithFetchedData = fetchAllData(items);

        waitForFetchingToComplete();

        return itemsWithFetchedData;
    }
}
