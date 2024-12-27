package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;

import java.math.BigDecimal;

//For the future but not implemented yet
public abstract class CompetitorWebsiteDataApiFetcher extends CompetitorWebsiteDataFetcher {
    @Override
    public BigDecimal fetchCompetitorPrice(Item item) {
        return fetchCompetitorPriceFromApi(item);
    }

    public abstract BigDecimal fetchCompetitorPriceFromApi(Item item);
}