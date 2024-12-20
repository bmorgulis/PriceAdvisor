package com.example.priceadvisor.datafetching;

import com.example.priceadvisor.entity.Item;
import com.example.priceadvisor.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;

@Component
public class AmazonDataApiFetcher extends CompetitorWebsiteDataApiFetcher {

    private final ItemService itemService;

    @Autowired
    public AmazonDataApiFetcher(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public BigDecimal fetchCompetitorPriceFromApi(Item item) {
        return null;
    }

    @Override
    public boolean saveCompetitorPriceIfChanged(Item item, BigDecimal price) {
        if (!Objects.equals(price, item.getAmazonPrice())) {
            itemService.setAmazonPrice(item, price);
            return true;
        }
        return false;
    }
}
