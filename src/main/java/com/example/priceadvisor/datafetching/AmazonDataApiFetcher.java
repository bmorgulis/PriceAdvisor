package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class AmazonDataApiFetcher extends CompetitorWebsiteDataApiFetcher {

    @Override
    public BigDecimal fetchCompetitorPriceFromApi(Item item) {
        return null;
    }

    @Override
    public boolean saveCompetitorPriceIfChanged(Item item, BigDecimal price) {
        if (!Objects.equals(price, item.getAmazonPrice())) {
            item.setAmazonPrice(price);
            return true;
        }
        return false;
    }
}
