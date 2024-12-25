package com.example.priceadvisor.repository;

import com.example.priceadvisor.entity.Business;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessRepository extends CrudRepository<Business, Integer> {

}
