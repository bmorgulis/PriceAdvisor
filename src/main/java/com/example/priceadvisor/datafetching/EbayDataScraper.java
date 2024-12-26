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
public class EbayDataScraper extends CompetitorWebsiteDataScraper {

    private static final Logger logger = LoggerFactory.getLogger(EbayDataScraper.class);
    private final ItemService itemService;

    @Autowired
    public EbayDataScraper(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public BigDecimal scrapeCompetitorPrice(Item item) {
        try (WebClient webClient = createWebClient()) {

            logger.info("Scraping Ebay price for {}", item.getName());

            String searchUrl = buildSearchUrl(item);
            logger.info("Ebay search URL for {}, URL: {}", item.getName(), searchUrl);

            String searchPageContent = getPageContentAsString(webClient, searchUrl);
            logger.info("Ebay search page content for {}, URL: {}, Content: {}", item.getName(), searchUrl, searchPageContent);

            String itemUrl = scrapeItemPageUrlFromSearchPage(searchPageContent);
            logger.info("Ebay item page URL for {}, URL: {}", item.getName(), itemUrl);

            if (itemUrl != null) {
                String itemPageContent = getPageContentAsString(webClient, itemUrl);
                logger.info("Ebay item page content for {}, Item page content: {}", item.getName(), itemPageContent);
                String price = scrapePriceFromItemPage(itemPageContent);
                logger.info("Ebay price for {}, Price: {}", item.getName(), price);
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
        searchUrl += buildSearchQuery(item);
        return searchUrl;
    }

    @Override
    public String scrapeItemPageUrlFromSearchPage(String pageContent) {
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
        Pattern pricePattern = Pattern.compile("class=\"ux-textspans\">\\s+US \\$((\\d{1,3},)*\\d{1,3}\\.\\d{2})(/ea)?\\s+</span>");
        Matcher priceMatcher = pricePattern.matcher(itemPageContent);

        if (priceMatcher.find()) {
            return priceMatcher.group(1).replace(",", "");
        } else {
            return null;
        }
    }

    @Override
    public boolean saveCompetitorPriceIfChanged(Item item, BigDecimal price) {
        if (!Objects.equals(price, item.getEbayPrice())) {
            itemService.setEbayPrice(item, price);
            return true;
        }
        return false;
    }
}

