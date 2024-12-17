package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AmazonDataApiFetcher extends CompetitorWebsiteDataApiFetcher {

    @Override
    public BigDecimal fetchCompetitorPriceFromApi(Item item) {
        return null;
    }

    @Override
    public void saveCompetitorPrice(Item item, BigDecimal price) {
        item.setAmazonPrice(price);
    }
}
