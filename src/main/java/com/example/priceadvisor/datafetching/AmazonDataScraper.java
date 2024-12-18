package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;

import java.math.BigDecimal;
import java.util.Objects;

public class AmazonDataScraper extends CompetitorWebsiteDataScraper {
    @Override
    public BigDecimal scrapeCompetitorPrice(Item item) {
        return null;
    }

    @Override
    public String buildSearchUrl(Item item) {
        return "";
    }

    @Override
    public String scrapeItemUrlFromSearchPage(String pageContent) {
        return "";
    }

    @Override
    public String scrapePriceFromItemPage(String itemPageContent) {
        return "";
    }

    @Override
    public boolean saveCompetitorPriceIfChanged(Item item, BigDecimal price) {
        if (!Objects.equals(price, item.getAmazonPrice())) {
            item.setAmazonPrice(price);
            return true;
        }
        return false;
    }
}
