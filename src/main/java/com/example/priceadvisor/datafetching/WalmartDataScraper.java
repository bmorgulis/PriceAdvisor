package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

@Service
public class WalmartDataScraper extends CompetitorWebsiteDataScraper {

    private final ItemService itemService;

    @Autowired
    public WalmartDataScraper(ItemService itemService) {
        this.itemService = itemService;
    }

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
        if (!Objects.equals(price, item.getWalmartPrice())) {
            itemService.setWalmartPrice(item, price);
            return true;
        }
        return false;
    }
}