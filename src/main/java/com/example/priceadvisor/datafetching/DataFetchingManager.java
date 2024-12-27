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

    @Autowired
    public DataFetchingManager(ItemService itemService,
                               AmazonDataScraper amazonDataScraper,
                               WalmartDataScraper walmartDataScraper,
                               EbayDataScraper ebayDataScraper) {
        this.itemService = itemService;
        this.fetchers = List.of(amazonDataScraper, walmartDataScraper, ebayDataScraper);
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
            try {
                List<Item> items = itemService.getAllItems();

                List<List<Item>> batches = createBatches(items, BATCH_SIZE);

                for (List<Item> batch : batches) {
                    processBatch(batch);
                }

            } finally {
                isFetchingInProgress.set(false);
            }
        }
    }

    private void processBatch(List<Item> batch) {
        Set<Item> itemsToSave = new CopyOnWriteArraySet<>();
        CountDownLatch latch = new CountDownLatch(batch.size() * fetchers.size());

        for (Item item : batch) {
            for (CompetitorWebsiteDataFetcher fetcher : fetchers) {
                executorService.submit(() -> {
                    try {
                        boolean priceUpdated = fetcher.fetchAndSaveCompetitorData(item);
                        if (priceUpdated) {
                            itemsToSave.add(item);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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

        if (smallBusinessPrice != null) {

            List<BigDecimal> prices = Arrays.asList(amazonPrice, walmartPrice, ebayPrice);

            boolean allPricesNull = prices.stream().allMatch(Objects::isNull);

            if (allPricesNull) {
                itemService.setPriceSuggestion(item, Item.PriceSuggestion.NONE);
            } else {
                // Filter out null values and check if smallBusinessPrice is lower or higher than all others
                boolean isLower = prices.stream().filter(Objects::nonNull).allMatch(price -> smallBusinessPrice.compareTo(price) < 0);
                boolean isHigher = prices.stream().filter(Objects::nonNull).allMatch(price -> smallBusinessPrice.compareTo(price) > 0);

                if (isLower) {
                    itemService.setPriceSuggestion(item, Item.PriceSuggestion.RAISE);
                } else if (isHigher) {
                    itemService.setPriceSuggestion(item, Item.PriceSuggestion.LOWER);
                } else {
                    itemService.setPriceSuggestion(item, Item.PriceSuggestion.NONE);
                }
            }
        }
        else {
            itemService.setPriceSuggestion(item, Item.PriceSuggestion.NONE);
        }
    }

    private List<List<Item>> createBatches(List<Item> items, int batchSize) {
        List<List<Item>> batches = new ArrayList<>();
        for (int i = 0; i < items.size(); i += batchSize) {
            batches.add(items.subList(i, Math.min(i + batchSize, items.size())));
        }
        return batches;
    }
}
