package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class EbayScrapingFetcher extends ScrapingDataFetcher {

    @Override
    public BigDecimal scrapeData(Item item) {
        return null;
    }

    @Override
    public void setPrice(BigDecimal price, FetchedData fetchedData) {
        fetchedData.setEbayPrice(price);
    }
}