package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WalmartDataScraper extends CompetitorWebsiteDataScraper {

    private final ItemService itemService;
    private static final Logger logger = LoggerFactory.getLogger(WalmartDataScraper.class);

    @Value("${walmart.cookie}")
    private String walmartCookie;

    @Autowired
    public WalmartDataScraper(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public BigDecimal scrapeCompetitorPrice(Item item) {
        logger.info("Scraping Walmart price for {}", item.getName());

        String searchUrl = buildSearchUrl(item);
        logger.info("Walmart search page URL for {}, URL: {}", item.getName(), searchUrl);

        String searchPageContent = getPageContentAsStringJsoupWithCookie(searchUrl, walmartCookie);
        logger.info("Walmart search page content for {}, URL: {}, Content: {}", item.getName(), searchUrl, searchPageContent);

        if (searchPageContent != null) {
            String itemUrl = scrapeItemPageUrlFromSearchPage(searchPageContent);
            logger.info("Walmart item page URL for {}, URL: {}", item.getName(), itemUrl);

            if (itemUrl != null) {
                String itemPageContent = getPageContentAsStringJsoupWithCookie(itemUrl, walmartCookie);
                logger.info("Walmart item page content for {}, Item page content: {}", item.getName(), itemPageContent);

                if (itemPageContent != null) {
                    String price = scrapePriceFromItemPage(itemPageContent);
                    logger.info("Walmart price for {}, Price: {}", item.getName(), price);

                    if (price != null) {
                        return new BigDecimal(price);
                    }
                }
            }
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
        Pattern pricePattern = Pattern.compile("<span itemprop=\"price\" data-seo-id=\"hero-price\" data-fs-element=\"price\" aria-hidden=\"false\">(Now )?\\$((\\d{1,3},)*\\d{1,3}\\.\\d{2})</span>");
        Matcher priceMatcher = pricePattern.matcher(itemPageContent);
        if (priceMatcher.find()) {
            return priceMatcher.group(2).replace(",", "");
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