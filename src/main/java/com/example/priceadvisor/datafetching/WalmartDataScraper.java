package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalmartDataScraper extends CompetitorWebsiteDataScraper {


    @Override
    public String scrapeItemUrlFromSearchPage(String pageContent) {
        return "";
    }

    @Override
    public BigDecimal scrapeCompetitorPrice(Item item) {
        return null;
    }

    @Override
    public void saveCompetitorPrice(Item item, BigDecimal price) {
        item.setWalmartPrice(price);
    }
}