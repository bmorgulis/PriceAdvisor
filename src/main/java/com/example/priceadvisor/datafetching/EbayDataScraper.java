package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.htmlunit.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EbayDataScraper extends CompetitorWebsiteDataScraper {
    private static final Logger logger = LoggerFactory.getLogger(EbayDataScraper.class);

    @Override
    public BigDecimal scrapeCompetitorPrice(Item item) {
        try (WebClient webClient = createWebClient()) {String searchUrl = buildSearchUrl(item);
            String searchPageContent = getPageContentAsString(webClient, searchUrl);
            logger.info("Search page content: {}", searchPageContent);
            String itemUrl = scrapeItemUrlFromSearchPage(searchPageContent);
            logger.info("Item URL: {}", itemUrl);

            if (itemUrl != null) {
                String itemPageContent = getPageContentAsString(webClient, itemUrl);
                logger.info("Item page content: {}", itemPageContent);
                String price = scrapePriceFromItemPage(itemPageContent);
                logger.info("Price: {}", price);
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
        String searchUrl = "https://www.ebay.com/sch/i.html?_nkw=";
        searchUrl += buildSearchQuery(item);  // Use the abstracted query builder
        return searchUrl;
    }

    @Override
    public String scrapeItemUrlFromSearchPage(String pageContent) {
        Pattern itemPattern = Pattern.compile("href=\"(https://www\\.ebay\\.com/itm/\\d{12})");
        Matcher itemMatcher = itemPattern.matcher(pageContent);

        if (itemMatcher.find()) {
            return itemMatcher.group(1);
        }
        logger.warn("No item URL found in search page content");
        return null;
    }

    @Override
    public String scrapePriceFromItemPage(String itemPageContent) {
        Pattern pricePattern = Pattern.compile("class=\"ux-textspans\">\\s+US \\$(\\d+\\.\\d{2})\\s+</span>");
        Matcher priceMatcher = pricePattern.matcher(itemPageContent);

        if (priceMatcher.find()) {
            return priceMatcher.group(1);
        } else {
            return null;
        }
    }

    @Override
    public boolean saveCompetitorPriceIfChanged(Item item, BigDecimal price) {
        if (!Objects.equals(price, item.getEbayPrice())) {
            item.setEbayPrice(price);
            return true;
        }
        return false;
    }
}

