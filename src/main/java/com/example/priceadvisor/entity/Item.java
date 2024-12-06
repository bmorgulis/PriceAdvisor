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

    @Column(name = "additionalInfo")
    private String additionalInfo;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "starred", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean starred;

    @Column(name = "inventoryId", nullable = false)
    private Integer inventoryId;

    // Default constructor needed for JPA
    public Item() {
    }

    public Item(String name, Long UPC, Long SKU, String description, String additionalInfo,
                BigDecimal price, Boolean starred, Integer inventoryId) {
        this.name = name;
        this.UPC = UPC;
        this.SKU = SKU;
        this.description = description;
        this.additionalInfo = additionalInfo;
        this.price = price;
        this.starred = starred;
        this.inventoryId = inventoryId;
    }

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

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getStarred() {
        return starred;
    }

    public void setStarred(Boolean starred) {
        this.starred = starred;
    }

    public Integer getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Integer inventoryId) {
        this.inventoryId = inventoryId;
    }
}
