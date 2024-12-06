package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;

import java.math.BigDecimal;

public abstract class ScrapingDataFetcher extends DataFetcher {
    @Override
    public BigDecimal fetchPrice(Item item) {
        return scrapeData(item);
    }

    public abstract BigDecimal scrapeData(Item item);
}