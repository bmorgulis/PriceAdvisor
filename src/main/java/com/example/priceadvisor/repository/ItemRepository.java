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

    @Query("SELECT i FROM Item i ORDER BY i.name ASC")
    List<Item> findAllOrderByName();}
