package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.math.BigDecimal;

public abstract class CompetitorWebsiteDataScraper extends CompetitorWebsiteDataFetcher {

    public abstract BigDecimal scrapeCompetitorPrice(Item item);
    public abstract String buildSearchUrl(Item item);
    public abstract String scrapeItemPageUrlFromSearchPage(String pageContent);
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
        appendField(query, item.getUpc() != null ? String.valueOf(item.getUpc()) : null);
        appendField(query, item.getSku() != null ? String.valueOf(item.getSku()) : null);
        appendField(query, item.getDescription());

        return query.toString();
    }

    private void appendField(StringBuilder query, String field) {
        if (field != null && !field.trim().isEmpty()) {
            if (!query.isEmpty()) {
                query.append("+");
            }

            String sanitizedField = field.trim()
                    .replaceAll("[^a-zA-Z0-9]+", "+") // Replace non-alphanumeric characters with '+'
                    .replaceAll("\\s+", "+");         // Replace spaces with '+'

            query.append(sanitizedField);
        }
    }

    public String getPageContentAsStringHtmlUnit(WebClient webClient, String url) {
        try {
            HtmlPage page = webClient.getPage(url);
            return page.asXml();
        } catch (Exception e) {
            return null;
        }
    }

    public String getPageContentAsStringJsoupWithCookie(String url, String cookie) {
        try {
            Document document = Jsoup.connect(url)
                    .header("Cookie", cookie)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .get();

            return document.html();
        } catch (Exception e) {
            return null;
        }
    }
}
