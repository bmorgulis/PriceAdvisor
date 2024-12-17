package com.example.priceadvisor.repository;

import com.example.priceadvisor.entity.Business;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessRepository extends CrudRepository<Business, Integer> {

    @Query("SELECT b.businessId FROM Business b")
    List<String> findAllBusinessIds();
}
