package com.example.priceadvisor.repository;

import com.example.priceadvisor.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    List<Item> findByInventoryIdOrderByNameAsc(Integer inventoryId);
}
