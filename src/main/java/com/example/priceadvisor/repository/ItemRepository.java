package com.example.priceadvisor.repository;

import com.example.priceadvisor.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByInventoryIdOrderByNameAsc(Integer inventoryId);

    List<Item> findByInventoryId(Integer inventoryId);

    List<Item> findByItemIdIn(List<Integer> itemIds);

    @Query("SELECT DISTINCT i.inventoryId FROM Item i")
    List<Integer> findAllDistinctInventoryIds();

    List<Item> findByInventoryIdAndPriceSuggestion(Integer inventoryId, Item.PriceSuggestion priceSuggestion);
}
