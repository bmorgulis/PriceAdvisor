package com.example.priceadvisor.datafetching;

import java.math.BigDecimal;

public class FetchedData {

    private BigDecimal amazonPrice;
    private BigDecimal walmartPrice;
    private BigDecimal ebayPrice;

    public BigDecimal getAmazonPrice() {
        return amazonPrice;
    }

    public void setAmazonPrice(BigDecimal amazonPrice) {
        this.amazonPrice = amazonPrice;
    }

    public BigDecimal getWalmartPrice() {
        return walmartPrice;
    }

    public void setWalmartPrice(BigDecimal walmartPrice) {
        this.walmartPrice = walmartPrice;
    }

    public BigDecimal getEbayPrice() {
        return ebayPrice;
    }

    public void setEbayPrice(BigDecimal ebayPrice) {
        this.ebayPrice = ebayPrice;
    }
}
