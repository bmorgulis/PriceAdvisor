package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.service.ItemService;
import org.htmlunit.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WalmartDataScraper extends CompetitorWebsiteDataScraper {

    private final ItemService itemService;
    private static final Logger logger = LoggerFactory.getLogger(WalmartDataScraper.class);

    @Autowired
    public WalmartDataScraper(ItemService itemService) {
        this.itemService = itemService;
    }

    // This method is used to scrape the competitor price for a given item
    @Override
    public BigDecimal scrapeCompetitorPrice(Item item) {
        try (WebClient webClient = createWebClient()) { // Create a WebClient instance to scrape the data
            logger.info("Scraping Walmart price for {}", item.getName());

            String searchUrl = buildSearchUrl(item); // Build the search URL for the item
            logger.info("Walmart search URL for {}, URL: {}", item.getName(), searchUrl);

            String searchPageContent = getPageContentAsString(webClient, searchUrl);
            logger.info("Walmart search page content for {}, URL: {}, Content: {}", item.getName(), searchUrl, searchPageContent);

            String itemUrl = scrapeItemPageUrlFromSearchPage(searchPageContent);
            logger.info("Walmart item page URL for {}, URL: {}", item.getName(), itemUrl);

            if (itemUrl != null) {
                String itemPageContent = getPageContentAsString(webClient, itemUrl);
                logger.info("Walmart item page content for {}, Item page content: {}", item.getName(), itemPageContent);

                String price = scrapePriceFromItemPage(itemPageContent);
                logger.info("Walmart price for {}, Price: {}", item.getName(), price);

                if (price != null) {
                    return new BigDecimal(price);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public String buildSearchUrl(Item item) {
        String searchUrl = "https://www.walmart.com/search?q=";
        searchUrl += buildSearchQuery(item);
        return searchUrl;
    }

    @Override
    public String scrapeItemPageUrlFromSearchPage(String pageContent) {
        Pattern itemPattern = Pattern.compile("href=\"(/ip/\\S+/\\d{6,10})");
        Matcher itemMatcher = itemPattern.matcher(pageContent);

        if (itemMatcher.find()) {
            return "https://www.walmart.com" + itemMatcher.group(1);
        }
        logger.warn("No item URL found in search page content");
        return null;
    }

    @Override
    public String scrapePriceFromItemPage(String itemPageContent) {
        Pattern pricePattern = Pattern.compile("\"priceCurrency\":\"USD\",\"price\":([0-9]*\\.?[0-9]+)");
        Matcher priceMatcher = pricePattern.matcher(itemPageContent);
        if (priceMatcher.find()) {
            return priceMatcher.group(1).replace(",", "");
        } else {
            return null;
        }
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