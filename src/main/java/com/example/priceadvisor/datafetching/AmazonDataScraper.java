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
public class AmazonDataScraper extends CompetitorWebsiteDataScraper {

    private static final Logger logger = LoggerFactory.getLogger(AmazonDataScraper.class);
    private final ItemService itemService;

    @Autowired
    public AmazonDataScraper(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public BigDecimal scrapeCompetitorPrice(Item item) {
        try (WebClient webClient = createWebClient()) {

            logger.info("Scraping Amazon price for {}", item.getName());
            String searchUrl = buildSearchUrl(item);
            logger.info("Amazon search URL for {}, URL: {}", item.getName(), searchUrl);
            String searchPageContent = getPageContentAsString(webClient, searchUrl);
            logger.info("Amazon search page content for {}, URL: {}, Content: {}", item.getName(), searchUrl, searchPageContent);
            String itemUrl = scrapeItemPageUrlFromSearchPage(searchPageContent);
            logger.info("Amazon item page URL for {}, URL: {}", item.getName(), itemUrl);

            if (itemUrl != null) {
                String itemPageContent = getPageContentAsString(webClient, itemUrl);
                logger.info("Amazon item page content for {}, Item page content: {}", item.getName(), itemPageContent);
                String price = scrapePriceFromItemPage(itemPageContent);
                logger.info("Amazon price for {}, Price: {}", item.getName(), price);
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
        String searchUrl = "https://www.amazon.com/s?k=";
        searchUrl += buildSearchQuery(item);
        return searchUrl;
    }

    @Override
    public String scrapeItemPageUrlFromSearchPage(String pageContent) {
        Pattern itemPattern = Pattern.compile("<a class=\"a-link-normal s-line-clamp-\\d s-link-style a-text-normal\" href=\"(/\\S+/dp/[A-Z0-9]{10})");
        Matcher itemMatcher = itemPattern.matcher(pageContent);

        if (itemMatcher.find()) {
            String baseUrl = "https://www.amazon.com";
            return baseUrl + itemMatcher.group(1);
        }
        logger.warn("No item URL found in search page content");
        return null;
    }

    @Override
    public String scrapePriceFromItemPage(String itemPageContent) {
        Pattern pricePattern = Pattern.compile("<span class=\"a-price.*>\\s+<span class=\"a-offscreen\">\\s+\\$((\\d{1,3},)*\\d{1,3}\\.\\d{2})\\s+</span>");
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
            itemService.setAmazonPrice(item, price);
            return true;
        }
        return false;
    }
}
