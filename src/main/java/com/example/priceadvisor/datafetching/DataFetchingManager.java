package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DataFetchingManager {

    private static final int MAX_THREADS = 150;  // Adjust based on your system's resources
    private static final ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
    private static final int BATCH_SIZE = 50;

    private final AtomicBoolean isFetchingInProgress = new AtomicBoolean(false);
    private final ItemService itemService;
    private final List<CompetitorWebsiteDataFetcher> fetchers;

    private CountDownLatch latch;

    // Constructor to inject ItemService
    @Autowired
    public DataFetchingManager(ItemService itemService,
                               AmazonDataApiFetcher amazonDataApiFetcher,
                               WalmartDataScraper walmartDataScraper,
                               EbayDataScraper ebayDataScraper) {
        this.itemService = itemService;
        this.fetchers = List.of(amazonDataApiFetcher, walmartDataScraper, ebayDataScraper);
    }

    /**
     * Scrapes data for a single item immediately, bypassing the batch process.
     */
    public void fetchItemDataImmediately(Item item) {
        CountDownLatch immediateFetchLatch = new CountDownLatch(fetchers.size());
        ExecutorService immediateExecutor = Executors.newFixedThreadPool(fetchers.size());

        AtomicBoolean priceUpdated = new AtomicBoolean(false);

        for (CompetitorWebsiteDataFetcher fetcher : fetchers) {
            immediateExecutor.submit(() -> {
                try {
                    if (fetcher.fetchAndSaveCompetitorData(item)) {
                        priceUpdated.set(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    immediateFetchLatch.countDown();
                }
            });
        }

        try {
            immediateFetchLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } finally {
            immediateExecutor.shutdown();
        }

        if (priceUpdated.get()) {
            generatePriceSuggestion(item);
            itemService.saveItem(item);
        }
    }

    public void fetchAllData() {
        if (isFetchingInProgress.compareAndSet(false, true)) {
            // Fetch data only if it's not already in progress
            try {
                List<Item> items = itemService.getAllItems();
                latch = new CountDownLatch(items.size() * fetchers.size());

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
        }
    }

    private void processBatch(List<Item> batch) {
        // Wrap a HashSet with Collections.synchronizedSet to make it thread-safe
        Set<Item> itemsToSave = new CopyOnWriteArraySet<>();

        // Reset latch for the current batch
        latch = new CountDownLatch(batch.size() * fetchers.size());

        // Process each item in the batch asynchronously
        for (Item item : batch) {
            for (CompetitorWebsiteDataFetcher fetcher : fetchers) {
                executorService.submit(() -> {
                    try {
                        // Fetch and check if any update happened
                        boolean priceUpdated = fetcher.fetchAndSaveCompetitorData(item);
                        if (priceUpdated) {
                            // Add item to the save list if it was updated
                            itemsToSave.add(item); // No need for explicit synchronization
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
            e.printStackTrace();
        }

        List<Item> itemsToSaveList = new ArrayList<>(itemsToSave);

        if (!itemsToSave.isEmpty()) {
            for (Item item : itemsToSaveList) {
                generatePriceSuggestion(item);
            }
            itemService.saveItems(itemsToSaveList);
        }
    }

    private void generatePriceSuggestion(Item item) {
        BigDecimal smallBusinessPrice = item.getSmallBusinessPrice();
        BigDecimal amazonPrice = item.getAmazonPrice();
        BigDecimal walmartPrice = item.getWalmartPrice();
        BigDecimal ebayPrice = item.getEbayPrice();

        // If smallBusinessPrice is not null and at least one of the other prices is not null
        if (smallBusinessPrice != null) {
            // List of prices to compare against
            List<BigDecimal> prices = Arrays.asList(amazonPrice, walmartPrice, ebayPrice);

            // Filter out null values and check if smallBusinessPrice is lower or higher than all others
            boolean isLower = prices.stream().filter(Objects::nonNull).allMatch(price -> smallBusinessPrice.compareTo(price) < 0);
            boolean isHigher = prices.stream().filter(Objects::nonNull).allMatch(price -> smallBusinessPrice.compareTo(price) > 0);

            // Set the price suggestion based on the comparisons
            if (isLower) {
                itemService.setPriceSuggestion(item, Item.PriceSuggestion.RAISE);
            } else if (isHigher) {
                itemService.setPriceSuggestion(item, Item.PriceSuggestion.LOWER);
            } else {
                itemService.setPriceSuggestion(item, Item.PriceSuggestion.NONE);
            }
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
