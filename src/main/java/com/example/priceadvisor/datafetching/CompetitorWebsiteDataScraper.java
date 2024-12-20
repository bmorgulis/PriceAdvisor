package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;

import java.math.BigDecimal;

public abstract class CompetitorWebsiteDataScraper extends CompetitorWebsiteDataFetcher {

    public abstract BigDecimal scrapeCompetitorPrice(Item item);
    public abstract String buildSearchUrl(Item item);
    public abstract String scrapeItemUrlFromSearchPage(String pageContent);
    public abstract String scrapePriceFromItemPage(String itemPageContent);

    @Override
    public BigDecimal fetchCompetitorPrice(Item item) {
        return scrapeCompetitorPrice(item);
    }

    public WebClient createWebClient() {
        WebClient webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        webClient.getOptions().setCssEnabled(false);
        return webClient;
    }

    public String buildSearchQuery(Item item) {
        StringBuilder query = new StringBuilder();

        appendField(query, item.getName());
        appendField(query, String.valueOf(item.getUpc()));
        appendField(query, String.valueOf(item.getSku()));
        appendField(query, item.getDescription());

        return query.toString();
    }

    private void appendField(StringBuilder query, String field) {
        if (field != null && !field.trim().isEmpty()) {
            if (!query.isEmpty()) {
                query.append("+");
            }
            query.append(field.replace(" ", "+"));
        }
    }

    public String getPageContentAsString(WebClient webClient, String url) {
        try {
            HtmlPage page = webClient.getPage(url);
            return page.asXml();
        } catch (Exception e) {
            return null;
        }
    }
}
