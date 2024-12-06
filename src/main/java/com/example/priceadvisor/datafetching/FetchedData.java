package com.example.priceadvisor.datafetching;

import java.math.BigDecimal;

public class FetchedData {
    private BigDecimal amazonPrice;
    private BigDecimal walmartPrice;
    private BigDecimal ebayPrice;

    public void setAmazonPrice(BigDecimal amazonPrice) {
        this.amazonPrice = amazonPrice;
    }

    public void setWalmartPrice(BigDecimal walmartPrice) {
        this.walmartPrice = walmartPrice;
    }

    public void setEbayPrice(BigDecimal ebayPrice) {
        this.ebayPrice = ebayPrice;
    }
}
