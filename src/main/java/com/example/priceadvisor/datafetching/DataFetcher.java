package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;

import java.math.BigDecimal;

public abstract class DataFetcher {
    public void fetchAndSaveData(Item item, FetchedData fetchedData) {
        BigDecimal price = fetchPrice(item);
        savePrice(price, fetchedData);
    }

    public abstract BigDecimal fetchPrice(Item item);

    public void savePrice(BigDecimal price, FetchedData fetchedData)
    {
        setPrice(price, fetchedData);
    }

    public abstract void setPrice(BigDecimal price, FetchedData fetchedData);
}
