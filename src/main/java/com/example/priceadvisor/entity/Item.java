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

    @Column(name = "UPC")
    private Long UPC;

    @Column(name = "SKU")
    private Long SKU;

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

    @Column(name = "inventoryId", nullable = false)
    private Integer inventoryId;

    // Default constructor needed for JPA
    public Item() {
    }

    public Item(String name, Long UPC, Long SKU, String description,
                BigDecimal smallBusinessPrice, Integer inventoryId) {
        this.name = name;
        this.UPC = UPC;
        this.SKU = SKU;
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

    public Long getUPC() {
        return UPC;
    }

    public void setUPC(Long UPC) {
        this.UPC = UPC;
    }

    public Long getSKU() {
        return SKU;
    }

    public void setSKU(Long SKU) {
        this.SKU = SKU;
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

    public Integer getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Integer inventoryId) {
        this.inventoryId = inventoryId;
    }
}
