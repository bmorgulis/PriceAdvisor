package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;

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
    public boolean saveCompetitorPriceIfChanged(Item item, BigDecimal price) {
        if (!Objects.equals(price, item.getWalmartPrice())) {
            item.setWalmartPrice(price);
            return true;
        }
        return false;
    }
}