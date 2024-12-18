package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;

import java.math.BigDecimal;

public abstract class CompetitorWebsiteDataFetcher {
    public boolean fetchAndSaveCompetitorData(Item item) {
        BigDecimal price = fetchCompetitorPrice(item);
        return saveCompetitorPriceIfChanged(item, price);
    }

    public abstract BigDecimal fetchCompetitorPrice(Item item);

    public abstract boolean saveCompetitorPriceIfChanged(Item item, BigDecimal price);
}
