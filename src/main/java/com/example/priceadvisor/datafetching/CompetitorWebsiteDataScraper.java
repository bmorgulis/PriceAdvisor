package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;

import java.math.BigDecimal;

public abstract class CompetitorWebsiteDataScraper extends CompetitorWebsiteDataFetcher {

    @Override
    public BigDecimal fetchCompetitorPrice(Item item) {
        return scrapeCompetitorPrice(item);
    }

    /**
     * Extracts the URL of the first item from the search results page content.
     *
     * @param pageContent the HTML content of the search results page
     * @return the URL of the first item found or null if no item URL is found
     */
    public abstract String scrapeItemUrlFromSearchPage(String pageContent);

    public abstract BigDecimal scrapeCompetitorPrice(Item item);
}