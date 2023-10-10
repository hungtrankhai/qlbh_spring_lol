package com.trankhaihung.cnpm.repository;

import com.trankhaihung.cnpm.entity.Product;
import com.trankhaihung.cnpm.entity.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Product getById(Long id);

    @Query("SELECT p FROM Product p JOIN p.wareHouse wh WHERE wh.availableQuantity > 0")
    List<Product> getAvailableProducts();



    @Query("SELECT p,wh FROM Product p JOIN p.wareHouse wh")
    List<Product> getAllProduct();
//    @Query
//    List<Product> findAll();

}
//("SELECT p FROM Product p JOIN p.wareHouse wh WHERE wh.availableQuantity > 0")