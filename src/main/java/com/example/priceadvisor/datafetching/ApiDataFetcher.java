package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;

import java.math.BigDecimal;

public abstract class ApiDataFetcher extends DataFetcher {
    @Override
    public BigDecimal fetchPrice(Item item) {
        return fetchPriceFromApi(item);
    }

    public abstract BigDecimal fetchPriceFromApi(Item item);
}