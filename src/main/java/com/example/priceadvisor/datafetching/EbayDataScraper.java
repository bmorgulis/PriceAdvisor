package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.htmlunit.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EbayDataScraper extends CompetitorWebsiteDataScraper {
    private static final Logger logger = LoggerFactory.getLogger(EbayDataScraper.class);

    @Override
    public BigDecimal scrapeCompetitorPrice(Item item) {
        try (WebClient webClient = createWebClient()) {
            String searchUrl = buildSearchUrl(item);
            String searchPageContent = getPageContent(webClient, searchUrl);
            String itemUrl = scrapeItemUrlFromSearchPage(searchPageContent);

            if (itemUrl != null) {
                String itemPageContent = getPageContent(webClient, itemUrl);
                String price = scrapePriceFromItemPage(itemPageContent);
                return new BigDecimal(price);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String scrapePriceFromItemPage(String itemPageContent) {
        return "";
    }

    private String buildSearchUrl(Item item) {
        String searchUrl = "https://www.ebay.com/sch/i.html?_nkw=";

        searchUrl += item.getName().replace(" ", "+") + "+" +
                (item.getUPC() != null ? item.getUPC() : "") + "+" +
                (item.getSKU() != null ? item.getSKU() : "") + "+" +
                (item.getDescription() != null ? item.getDescription() : "") + "+" +
                (item.getAdditionalInfo() != null ? item.getAdditionalInfo() : "");

        return searchUrl;
    }


    @Override
    public void saveCompetitorPrice(Item item, BigDecimal price) {
        item.setEbayPrice(price);
    }

    public String scrapeItemUrlFromSearchPage(String pageContent) {
        Pattern itemPattern = Pattern.compile("href=\"(https://www\\.ebay\\.com/itm/\\d{12})");
        Matcher itemMatcher = itemPattern.matcher(pageContent);

        if (itemMatcher.find()) {
            String itemUrl = itemMatcher.group(1);
            logger.info("Extracted item URL: {}", itemUrl);
            return itemUrl;
        }
        logger.warn("No item URL found in search page content");
        return null;
    }
}

