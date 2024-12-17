package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;

import java.math.BigDecimal;

public abstract class CompetitorWebsiteDataFetcher {
    public void fetchAndSaveCompetitorData(Item item) {
        BigDecimal price = fetchCompetitorPrice(item);
        saveCompetitorPrice(item, price);
    }

    public abstract BigDecimal fetchCompetitorPrice(Item item);

    public abstract void saveCompetitorPrice(Item item, BigDecimal price);

    /**
     * Retrieves the HTML content of a page given its URL.
     *
     * @param webClient the WebClient instance used to fetch the page
     * @param itemUrl   the URL of the page to retrieve
     * @return the HTML content of the page as a String
     */
    public String getPageContent(WebClient webClient, String itemUrl) {
        try {
            HtmlPage page = webClient.getPage(itemUrl);
            return page.asXml();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Creates and configures a WebClient instance for fetching pages.
     *
     * @return a configured WebClient instance
     */
    public WebClient createWebClient() {
        WebClient webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        return webClient;
    }
}
