package com.example.priceadvisor.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "itemId")
    private Integer itemId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "upc")
    private Long upc;

    @Column(name = "sku")
    private String sku;

    @Column(name = "description")
    private String description;

    @Column(name = "smallBusinessPrice", precision = 10, scale = 2)
    private BigDecimal smallBusinessPrice;

    @Column(name = "amazonPrice", precision = 10, scale = 2)
    private BigDecimal amazonPrice;

    @Column(name = "walmartPrice", precision = 10, scale = 2)
    private BigDecimal walmartPrice;

    @Column(name = "ebayPrice", precision = 10, scale = 2)
    private BigDecimal ebayPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "priceSuggestion")
    private PriceSuggestion priceSuggestion;

    @Column(name = "inventoryId", nullable = false)
    private Integer inventoryId;

    // Default constructor needed for JPA
    public Item() {
    }

    public Item(String name, Long upc, String sku, String description, BigDecimal smallBusinessPrice, Integer inventoryId) {
        this.name = name;
        this.upc = upc;
        this.sku = sku;
        this.description = description;
        this.smallBusinessPrice = smallBusinessPrice;
        this.inventoryId = inventoryId;
    }

    // Getters and Setters
    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUpc() {
        return upc;
    }

    public void setUpc(Long upc) {
        this.upc = upc;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getSmallBusinessPrice() {
        return smallBusinessPrice;
    }

    public void setSmallBusinessPrice(BigDecimal smallBusinessPrice) {
        this.smallBusinessPrice = smallBusinessPrice;
    }

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

    public PriceSuggestion getPriceSuggestion() {
        return priceSuggestion;
    }

    public void setPriceSuggestion(PriceSuggestion priceSuggestion) {
        this.priceSuggestion = priceSuggestion;
    }

    public Integer getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Integer inventoryId) {
        this.inventoryId = inventoryId;
    }

    public enum PriceSuggestion {
        RAISE,
        LOWER
    }
}
