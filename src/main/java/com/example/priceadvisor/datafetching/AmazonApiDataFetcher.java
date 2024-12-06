package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmazonApiDataFetcher extends ApiDataFetcher {

    @Override
    public BigDecimal fetchPriceFromApi(Item item) {
        return null;
    }

    @Override
    public void setPrice(BigDecimal price, FetchedData fetchedData) {
        fetchedData.setAmazonPrice(price);
    }
}
