package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.service.ItemService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DataFetchingManager {

    private static final int MAX_THREADS = 50;  // Adjust based on your system's resources
    private static final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
    private static final List<CompetitorWebsiteDataFetcher> FETCHERS = List.of(
            new AmazonDataApiFetcher(),
            new WalmartDataScraper(),
            new EbayDataScraper()
    );
    private static final int BATCH_SIZE = 50;

    private final AtomicBoolean isFetchingInProgress = new AtomicBoolean(false);
    private final ItemService itemService;  // Only inject ItemService

    private CountDownLatch latch;

    // Constructor to inject ItemService
    public DataFetchingManager(ItemService itemService) {
        this.itemService = itemService;
    }

    public void fetchAllData() {
        if (isFetchingInProgress.compareAndSet(false, true)) {
            // Fetch data only if it's not already in progress
            try {
                List<Item> items = itemService.getAllItems();
                latch = new CountDownLatch(items.size() * FETCHERS.size());

                // Split items into batches to prevent memory overload
                List<List<Item>> batches = createBatches(items, BATCH_SIZE);

                for (List<Item> batch : batches) {
                    processBatch(batch);
                }

                // Wait for all tasks to complete
                waitForFetchingToComplete();
            } finally {
                isFetchingInProgress.set(false); // Ensure flag is reset even if an exception occurs
            }
        } else {
            System.out.println("Data fetching is already in progress. Please wait...");
        }
    }

    private void processBatch(List<Item> batch) {
        // Wrap a HashSet with Collections.synchronizedSet to make it thread-safe
        Set<Item> itemsToSave = new CopyOnWriteArraySet<>();

        // Reset latch for the current batch
        latch = new CountDownLatch(batch.size() * FETCHERS.size());

        // Process each item in the batch asynchronously
        for (Item item : batch) {
            for (CompetitorWebsiteDataFetcher fetcher : FETCHERS) {
                executorService.submit(() -> {
                    try {
                        // Fetch and check if any update happened
                        boolean priceUpdated = fetcher.fetchAndSaveCompetitorData(item);
                        if (priceUpdated) {
                            // Add item to the save list if it was updated
                            itemsToSave.add(item); // No need for explicit synchronization
                        }
                    } catch (Exception e) {
                        System.err.println("Error fetching data for item: " + item + ". " + e.getMessage());
                    } finally {
                        latch.countDown(); // Decrement the latch count when a task completes
                    }
                });
            }
        }

        // Wait for all tasks in this batch to complete
        try {
            latch.await(); // Wait until all tasks in the batch are completed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            System.err.println("Batch processing interrupted: " + e.getMessage());
        }

        // Save the updated items after all tasks in the batch are completed
        if (!itemsToSave.isEmpty()) {
            itemService.saveItems(new ArrayList<>(itemsToSave));  // Convert Set to List and save only the updated items
        }
    }


    private List<List<Item>> createBatches(List<Item> items, int batchSize) {
        List<List<Item>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            batches.add(items.subList(i, Math.min(i + batchSize, items.size())));
        }
        return batches;
    }

    private void waitForFetchingToComplete() {
        try {
            latch.await(); // Wait until all tasks are completed
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Data fetching was interrupted.", e);
        }
    }
}
