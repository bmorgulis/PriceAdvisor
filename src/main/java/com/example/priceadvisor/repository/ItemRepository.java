package com.example.priceadvisor.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.priceadvisor.entity.Item;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    boolean existsByUPCOrSKU(Long UPC, Long SKU);   //this is built in to jpa. it checks if exists by UPC or SKU
}
